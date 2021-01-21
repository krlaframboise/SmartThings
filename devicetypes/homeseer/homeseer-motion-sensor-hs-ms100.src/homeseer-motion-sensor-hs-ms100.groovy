/*
 *  HomeSeer Motion Sensor HS-MS100+ (v1.0.1)
 *
 *  Changelog:
 *
 *    1.0.1 (01/20/2021)
 *      - Made it send commands on save when on batteries instead of only sending when it receives the wake up notification.
 *
 *    1.0 (01/17/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 HomeSeer
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
	0x30: 2,	// SensorBinary
	0x55: 1,	// Transport Service (V2)
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x71: 3,	// Notification (4)
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// Firmware Update Md
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x9F: 1		// Security S2
]

@Field static Map sensorTypes = [motion:12, acceleration:8]

metadata {
	definition (
		name: "HomeSeer Motion Sensor HS-MS100",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "x.com.st.d.sensor.motion",
		mnmn: "SmartThingsCommunity",
		vid: "a8505ccf-26cb-37bc-a7b6-8f27eb0adaf0"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Battery"
		capability "Refresh"
		capability "Power Source"
		capability "Health Check"
		capability "platemusic11009.firmware"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"000C", prod:"0201", model:"0009", deviceJoinName: "HomeSeer Motion Sensor"
	}

	simulator { }

	preferences {
		configParams.each {		
			createEnumInput("configParam${it.num}", "${it.name}:", it.value, it.options)
		}
		createEnumInput("debugOutput", "Debug Logging", 1, [0:"Disabled", 1:"Enabled [DEFAULT]"])
	}
}

void createEnumInput(String name, String title, Integer defaultVal, Map options) {
	input name, "enum",
		title: title,
		required: false,
		defaultValue: defaultVal.toString(),
		options: options
}


def installed() {
	logDebug "installed()..."
	state.syncAll = true

	initialize()
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logDebug "updated()"

		initialize()

		def cmds = []
		// if (device.currentValue("powerSource") == "dc") {
			cmds += getConfigureCmds()
		// }
		// else {
			// logForceWakeupMessage()
		// }

		return cmds ? response(delayBetween(cmds, 500)) : []
	}
}

private initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)	
	
	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 12) + (60 * 5)), displayed: false, data:[protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	if (!device.currentValue("motion")) {
		sendEvent(name: "motion", value: "inactive")
	}

	if (!device.currentValue("acceleration")) {
		sendEvent(name: "acceleration", value: "inactive")
	}

	if (device.currentValue("battery") == null) {
		sendEvent(name: "battery", value: 100, unit: "%")
	}

	state.batteryInclusion = (((zwaveInfo?.cc?.find { it.toString() == "80" }) || (zwaveInfo?.sec?.find { it.toString() == "80" })) ? true : false)

	def powerSrc = (state.batteryInclusion ? "battery" : "dc")
	if (device.currentValue("powerSource") != powerSrc) {
		sendEvent(name: "powerSource", value: powerSrc)
	}
}


def configure() {
	logDebug "configure()..."

	// if (!state.batteryInclusion) {
		def cmds = getConfigureCmds()
		if (cmds) {
			sendCommands(delayBetween(cmds, 500))
		}
	// }
	// else {
		// logForceWakeupMessage()
	// }
	return []
}

private getConfigureCmds() {
	def cmds = []

	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}

	if (state.syncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if (state.syncAll) {
		cmds << sensorBinaryGetCmd(sensorTypes.motion)
		cmds << sensorBinaryGetCmd(sensorTypes.acceleration)
	}

	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if (state.syncAll || ("${storedVal}" != "${param.value}")) {
			if (!state.syncAll) {
				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param)
			}
			cmds << configGetCmd(param)
		}
	}
	
	return cmds
}

private logForceWakeupMessage() {
	def changes = pendingChanges
	def msg = ""

	if (pendingChanges) {
		msg = "The ${changes} pending change(s) will be sent to the device the next time it wakes up."
	}
	else {
		msg = "The sensor data will be requested the next time the device wakes up."
	}

	log.warn "${msg} To force the device to wake up immediately, hold the action button for 5 seconds.  This is not necessary if the device was included as a line-powered device."
}


def ping() {
	logDebug "ping()"

	if (!state.batteryInclusion) {
		sendCommands([
			sensorBinaryGetCmd(sensorTypes.motion)
		])
	}
}


def refresh() {
	logDebug "refresh()..."

	state.syncAll = true

	configure()
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

		sendLastCheckInEvent()

	}
	catch (e) {
		log.error "$e"
	}
	return []
}

private sendLastCheckInEvent() {
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	}
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up"

	def cmds = []

	if (!isDuplicateCommand(state.lastWakeUp, 10000)) {
		state.lastWakeUp = new Date().time
		cmds += getConfigureCmds()
	}

	if (cmds) {
		cmds = delayBetween(cmds, 500)
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()

	sendCommands(cmds)
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEvent(getEventMap("firmwareVersion", fullVersion.toBigDecimal()))
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	state.lastBattery = new Date().time

	if (device.currentValue("powerSource") != "battery") {
		sendEvent(getEventMap("powerSource", "battery"))
	}

	def evt = getEventMap("battery", val, "%")
	evt.isStateChange = true
	sendEvent(evt)
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "$cmd"

	// Notification reports are only sent if the Send Binary Sensor setting is disabled.
	
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				if (cmd.eventParameter[0] == 3 || cmd.eventParameter[0] == 9) {
					sendAccelerationEvent("inactive")
				}
				else {
					sendMotionEvent("inactive")
				}
				break
			case { it == 3 || it == 9}:
				sendAccelerationEvent("active")
				break
			case 8:
				sendMotionEvent("active")
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	switch (cmd.sensorType) {
		case sensorTypes.acceleration:
			sendAccelerationEvent(cmd.sensorValue ? "active" : "inactive")
			break
		case sensorTypes.motion:
			sendMotionEvent(cmd.sensorValue ? "active" : "inactive")
			break
		default:
			logDebug "Unknown Sensor Type: $cmd"
	}
}

private sendMotionEvent(value) {
	sendEvent(getEventMap("motion", value))
}

private sendAccelerationEvent(value) {
	sendEvent(getEventMap("acceleration", value))
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport ${cmd}"

	state.syncAll = false
	
	def val = cmd.scaledConfigurationValue
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${val}"
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Ignored Command: $cmd"
}


private getEventMap(name, value, unit=null) {
	def eventMap = [
		name: name,
		value: value,
		displayed: true,
		descriptionText: "${device.displayName} ${name} is ${value}"
	]

	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}

	logDebug "${eventMap.descriptionText}"
	return eventMap
}


private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private sensorBinaryGetCmd(sensorType) {
	return secureCmd(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: sensorType))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}

private sendCommands(cmds) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions)
	}
	return []
}


private getPendingChanges() {
	return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


private getConfigParams() {
	[
		motionSensitivityParam,
		motionClearedDelayParam,
		shockAlarmParam,
		lowBatteryAlarmParam,
		sendBasicSetParam,
		basicSetValueParam,
		binarySensorParam
	]
}

private getMotionSensitivityParam() {
	return getParam(12, "Motion Sensor Sensitivity", 1, 8, [0:"Motion Sensor Disabled", 1:"1-Low Sensitivity", 2:"2", 3:"3", 4:"4", 5:"5", 6:"6",7:"7",8:"8-High Sensitivity [DEFAULT]"])
}

private getSendBasicSetParam() {
	return getParam(14, "Enable/Disable Basic Set Command", 1, 0, [0:"Disable [DEFAULT]", 1:"Enable"])
}

private getBasicSetValueParam() {
	return getParam(15, "Set Value for Basic Set Command", 1, 0, [
		0:"Motion 255 / Motion Times Out 0 [DEFAULT]",
		1:"Motion 0 / Motion Times Out 255"
	])
}

private getShockAlarmParam() {
	return getParam(17, "Enable Shock Sensor", 1, 1, [0:"Disable", 1:"Enable [DEFAULT]"])
}

private getMotionClearedDelayParam() {
	return getParam(18, "Motion Timeout Interval", 2, 600, [0:"0 Seconds",1:"1 Seconds",2:"2 Seconds",3:"3 Seconds",4:"4 Seconds",5:"5 Seconds",10:"10 Seconds",15:"15 Seconds",30:"30 Seconds",45:"45 Seconds",60:"1 Minute",120:"2 Minutes",180:"3 Minutes",240:"4 Minutes",300:"5 Minutes",420:"7 Minutes",600:"10 Minutes [DEFAULT]",900:"15 Minutes",1800:"30 Minutes",2700:"45 Minutes", 3600:"1 Hour", 7200:"2 Hours", 10800:"3 Hours", 14400:"4 Hours"])
}

private getBinarySensorParam() {
	return getParam(19, "Enable Binary Sensor for Motion", 1, 1, [0:"Disable", 1:"Enable [DEFAULT]"])
}

private getLowBatteryAlarmParam() {
	return getParam(32, "Set Value for Low Battery", 1, 20, [10:"10%",15:"15%", 20:"20% [DEFAULT]", 25:"25%", 30:"30%", 35:"35%", 40:"40%", 45:"45%", 50:"50%"]) // manual says 0-50, but device ignores values below 10.
}

private getParam(num, name, size, defaultVal, options) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, defaultValue: defaultVal, value: val, options: options]
}


private canReportBattery() {
	return (state.batteryInclusion && (state.syncAll || !isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000))))
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}
	}
	catch (ex) {
		return "$dt"
	}
}


private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled) {
		log.debug(msg)
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}