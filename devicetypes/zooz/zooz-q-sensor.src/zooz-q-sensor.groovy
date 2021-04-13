/*
 *  Zooz Q Sensor - ZSE11 v1.0
 *
 *  Changelog:
 *
 *    1.0 (04/12/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 Zooz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x30: 2, // SensorBinary
	0x31: 5, // SensorMultilevel
	0x55: 1, // Transport Service
	0x59: 1, // AssociationGrpInfo
	0x5A: 1, // DeviceResetLocally
	0x5E: 2, // ZwaveplusInfo
	0x6C: 1, // Supervision
	0x70: 1, // Configuration
	0x71: 3, // Notification
	0x72: 2, // ManufacturerSpecific
	0x73: 1, // Powerlevel
	0x7A: 2, // FirmwareUpdateMd
	0x80: 1, // Battery
	0x84: 2, // WakeUp
	0x85: 2, // Association
	0x86: 1, // Version
	0x98: 1, // Security S0
	0x9F: 1	 // Security S2
]

@Field static int tempSensorType = 1
@Field static int lightSensorType = 3
@Field static int humiditySensorType = 5
@Field static int motionSensorType = 12


metadata {
	definition (
		name: "Zooz Q Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "x.com.st.d.sensor.motion",
		mnmn: "SmartThingsCommunity",
		vid: "10675715-68d9-311b-b972-e293e26bd41e"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Power Source"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"027A", prod:"0200", model:"0006", deviceJoinName: "Zooz Q Sensor" // EU Version
		fingerprint mfr:"027A", prod:"0201", model:"0006", deviceJoinName: "Zooz Q Sensor" // US Version
		fingerprint mfr:"027A", prod:"0202", model:"0006", deviceJoinName: "Zooz Q Sensor" // AU Version
	}

	simulator { }

	preferences {
		configParams.each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			}
			else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					range: param.range
			}
		}

		input "debugOutput", "enum",
			title: "Enable Debug Logging?",
			required: false,
			displayDuringSetup: false,
			defaultValue: 1,
			options: [0:"No", 1:"Yes [DEFAULT]"]
	}
}


def installed() {
	logDebug "installed()..."

	state.firstConfig = true
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		if (pendingChanges) {
			if (device.currentValue("powerSource") == "battery") {
				logForceWakeupMessage("Configuration changes will be sent to the device the next time it wakes up.")
			}
			else {
				runIn(2, executeConfigure)
			}
		}
	}
}

void executeConfigure() {
	sendCommands(getConfigCmds())
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	refreshSyncStatus()

	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 24) + (60 * 5)), displayed: false, data:[protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	
	if (!device.currentValue("tamper")) {
		sendEvent(name: "tamper", value: "clear")
	}
	
	if (device.currentValue("powerSource") == null) {
		String powerSource = (((zwaveInfo?.cc?.find { it.toString() == "80" }) || (zwaveInfo?.sec?.find { it.toString() == "80" })) ? "battery" : "dc")
		
		sendEvent(name: "powerSource", value: powerSource)
		
		if (powerSource == "dc") {
			sendEvent(name: "battery", value: 100, unit: "%")
		}		
	}
}


def configure() {
	logDebug "configure()..."

	state.firstConfig = true

	runIn(2, executeRefresh)
}

void executeRefresh() {
	sendCommands(getRefreshCmds())
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()
	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getEventMap("tamper", "clear"))
	}

	if (device.currentValue("powerSource") == "battery") {
		state.pendingRefresh = true
		logForceWakeupMessage("The sensor values will be requested the next time the device wakes up.")
	}
	else {
		sendCommands(getRefreshCmds())
	}
}

List<String> getRefreshCmds() {
	List<String> cmds = [		
		versionGetCmd(),
		sensorBinaryGetCmd(motionSensorType),
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(lightSensorType),
		sensorMultilevelGetCmd(humiditySensorType),
		batteryGetCmd()
	]

	int changes = pendingChanges
	if (state.firstConfig || !changes) {
		state.firstConfig = false

		configParams.each { param ->
			cmds << configGetCmd(param)
		}
	}
	else if (changes) {
		cmds += getConfigCmds()
	}
	
	state.pendingRefresh = false
	return cmds
}


List<String> getConfigCmds() {
	List<String> cmds = []

	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if ("${storedVal}" != "${param.value}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}
	return cmds
}

void sendCommands(cmds, delay=750) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}


void logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by holding the z-button for 3 seconds."
}


def parse(String description) {
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}

		updateLastCheckIn()
	}
	catch (e) {
		log.error "$e"
	}
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time

		def dt = new Date()
		String localTime = "$dt"
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			dt = dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}

		sendEvent(name: "lastCheckIn", value: dt, displayed: false)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up"

	List<String> cmds = []

	if (state.pendingRefresh) {
		cmds += getRefreshCmds()
	}
	else {
		cmds += getConfigCmds()
	}

	if (cmds) {
		cmds << "delay 2000"
	}
	else {
		cmds << batteryGetCmd()
	}

	cmds << wakeUpNoMoreInfoCmd()

	sendCommands(cmds)
}


void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}

	if (device.currentValue("powerSource") != "battery") {
		sendEvent(getEventMap("powerSource", "battery"))
	}

	sendEvent(getEventMap("battery", val, true, "%"))
}


void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	switch (cmd.sensorType) {
		case tempSensorType:
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, (cmd.scale ? "F" : "C"), cmd.precision)
			sendEvent(getEventMap("temperature", temp, true, "${temperatureScale}"))
			break

		case lightSensorType:
			sendEvent(getEventMap("illuminance", cmd.scaledSensorValue, true, "lux"))
			break

		case humiditySensorType:
			sendEvent(getEventMap("humidity", cmd.scaledSensorValue, true, "%"))
			break

		default:
			logDebug "Unhandled: ${cmd}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 7) {
		if ((cmd.event == 3) || (cmd.eventParameter[0] == 3)) {
			sendEvent(getEventMap("tamper", (cmd.event ? "detected" : "clear")))
		}
		else {
			logDebug "Unhandled: ${cmd}"
		}		
	}
	else {
		logDebug "Unhandled: ${cmd}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	if (cmd.sensorType == motionSensorType) {
		sendEvent(getEventMap("motion", (cmd.sensorValue ? "active" : "inactive")))
	}
	else {
		logDebug "Unknown Sensor Binary Type ${cmd.sensorType}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	sendEvent(getEventMap("firmwareVersion", (cmd.applicationVersion + (cmd.applicationSubVersion / 100))))
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	// logTrace "${cmd}"

	if (device.currentValue("syncStatus") != "Syncing...") {
		sendEvent(name: "syncStatus", value: "Syncing...", displayed: false)
	}

	runIn(4, refreshSyncStatus)

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.scaledConfigurationValue

		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}
}

void refreshSyncStatus() {
	int changes = pendingChanges
	String status = (changes ?  "${changes} Pending Changes" : "Synced")

	if (device.currentValue("syncStatus") != status) {
		sendEvent(name: "syncStatus", value: status, displayed: false)
	}
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Ignored Command: $cmd"
}


Map getEventMap(name, value, displayed=true, unit=null) {
	Map eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} ${name} is ${value}"
	]

	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}
	if (displayed) {
		logDebug "${eventMap.descriptionText}"
	}
	return eventMap
}


String wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV1.wakeUpNoMoreInformation())
}

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String sensorBinaryGetCmd(sensorType) {
	return secureCmd(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: sensorType))
}

String sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType ? 0 : 1)
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
}

String configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

String secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}


int getPendingChanges() {
	return safeToInt(configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }, 0)
}

Integer getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	[
		motionSensitivityParam,
		motionResetParam,
		motionLedParam,
		reportingFrequencyParam,
		temperatureThresholdParam,
		humidityThresholdParam,
		lightThresholdParam
	]
}

Map getMotionSensitivityParam() {
	return getParam(12, "Motion Sensitivity", 1, 6, [0:"Motion Disabled", 1:"1 - Least Sensitive", 2:"2", 3:"3", 4:"4", 5:"5", 6:"6 [DEFAULT]", 7:"7", 8:"8 - Most Sensitive"]) //1:least - 8:most
}

Map getMotionResetParam() {
	return getParam(13, "Motion Clear Time (10-3600 Seconds)", 2, 30, null, "10..3600") //10-3600 seconds
}

Map getMotionLedParam() {
	return getParam(19, "Motion LED", 1, 1, [0:"Disabled", 1:"Enabled [DEFAULT]"])
}

Map getReportingFrequencyParam() {
	return getParam(172, "Minimum Reporting Frequency (1-774 Hours)", 2, 4, null, "1..744") // 1-744 Hours
}

Map getTemperatureThresholdParam() {
	return getParam(183, "Temperature Reporting Threshold (1-144°F)", 2, 1, null, "1..144") // 1-144°F
}

Map getHumidityThresholdParam() {
	return getParam(184, "Humidity Reporting Threshold (0:No Reports, 1-80%)", 1, 5, null, "0..80") // 0:disabled, 1-80%
}

Map getLightThresholdParam() {
	return getParam(185, "Light Reporting Threshold (0:No Reports, 1-30000 lux)", 2, 50, null, "0..30000") //0 disabled, 1-30000 lux
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options, range=null) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, defaultVal: defaultVal, value: val, options: options, range: range]
}


Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	}
	else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	}
	else {
		return  defaultVal
	}
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	return (!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time))
}

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}

void logTrace(String msg) {
	// log.trace "$msg"
}