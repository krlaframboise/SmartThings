/*
 *  HomeSeer Indicator Light Sensor v1.0
 *  	(Model: HS-FS100-L)
 *
 *
 *  Changelog:
 *
 *    1.0 (10/31/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 HomeSeer
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

import groovy.json.JsonOutput
import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x25: 1,	// SwitchBinary
	0x30: 2,	// SensorBinary
	0x31: 5,	// SensorMultilevel (7)
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
	0x84: 1,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x9F: 1		// Security S2
]

@Field static int tempSensorType = 1

metadata {
	definition (
		name: "HomeSeer Indicator Light Sensor HS-FS100-L",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfdevicetype: "x.com.st.d.sensor.multifunction",		
		mnmn: "SmartThingsCommunity",
		vid: "5ace7f16-3751-331f-b179-48f86a6644e0"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Alarm"
		capability "Battery"
		capability "Button"
		capability "Health Check"
		capability "Power Source"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Tone"
		capability "platemusic11009.firmware"
		capability "platemusic11009.hsIndicatorLightSensor"

		attribute "lastCheckIn", "string"
		
		fingerprint mfr:"000C", prod:"0202", model:"0001", deviceJoinName: "HomeSeer Indicator Light Sensor"
	}

	tiles(scale: 2) { }

	simulator { }

	preferences {
		getParamInput(lightSensitivityParam)
		getParamInput(lightDetectionDelayParam)
		getParamInput(tempReportingIntervalParam)
		getParamInput(notificationBuzzerParam)
		
		input "events", "paragraph",
			title:"Button Pushed Events are Created for Light Changes",
			description: "Button Pushed: No Light<br>Button Pushed 2x: Light<br>Button Pushed 3x: Color Change"
	}
}

private getParamInput(param) {
	input "${param.pref}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		options: param.options
}


def installed() {
	logDebug "installed()..."
	state.refreshTemp = true
	state.refreshStatus = true
	initialize()
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logDebug "updated()"

		initialize()

		runIn(2, configure)
	}
}

private initialize() {
	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 12) + (60 * 5)), displayed: false, data:[protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	if (!device.currentValue("alarm")) {
		sendEvent(name: "alarm", value: "off")
	}
	
	if (!device.currentValue("indicatorLight")) {
		state.refreshStatus = true
		sendEvent(name: "indicatorLight", value: "no light")			
	}
	
	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name: "supportedButtonValues", value: JsonOutput.toJson(["pushed", "pushed_2x", "pushed_3x"]), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name: "numberOfButtons", value: 1)		
	}
	
	if (!device.currentValue("button")) {
		sendEvent(name: "button", value: "pushed", data:[buttonNumber: 1])
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

	def cmds = []

	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}

	if (state.refreshTemp) {
		cmds << sensorMultilevelGetCmd(tempSensorType)
	}

	if (!device.currentValue("firmwareVersion") || state.refreshStatus) {
		cmds << versionGetCmd()
	}

	if (state.refreshStatus) {
		cmds << sensorBinaryGetCmd()
	}

	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if ("${storedVal}" != "${param.value}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}

	if (cmds) {		
		cmds = delayBetween(cmds, 500)
		
		if (state.batteryInclusion) {
			logForceWakeupMessage()
		}
	}
	
	if (state.batteryInclusion) {
		if (cmds) {
			cmds << "delay 2000"
		}
		cmds << wakeUpNoMoreInfoCmd()
	}

	return sendCommands(cmds)
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
	
	log.warn "${msg} To force the device to wake up immediately, press the button on the device immediately after changing a setting or refreshing the device details screen by swiping down.  This is not necessary if the device is powered by USB."
}


def ping() {
	logDebug "ping()"

	if (!state.batteryInclusion) {
		sendCommands([ 
			sensorMultilevelGetCmd(tempSensorType)
		])
	}
}


def both() {
	return siren()
}

def strobe() {
	return siren()
}

def siren() {
	sendEvent(name: "alarm", value: "siren")
	runIn(1, off)
	
	return beep()
}

def off() {
	sendEvent(name: "alarm", value: "off")
}


def beep() {
	logDebug "beep()..."
	if (state.batteryInclusion) {
		log.warn "Beep requires USB power."
	}
	return [ switchBinarySetCmd(0xFF) ]
}


def refresh() {
	logDebug "refresh()..."

	state.lastBattery = null
	state.refreshTemp = true
	state.refreshStatus = true

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


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	logDebug "Device Woke Up"

	configure()
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

	sendEvent(getEventMap("battery", val, "%"))
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"

	state.refreshTemp = false

	if (cmd.sensorType == tempSensorType) {
		def unit = cmd.scale ? "F" : "C"
		def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
		sendEvent(getEventMap("temperature", temp, getTemperatureScale()))
	}
	else {
		logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
}


def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"

	if (state.refreshStatus) {
		state.refreshStatus = false
		handleLightEvent(cmd.sensorValue ? 1 : 0)
	}
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"

	state.refreshStatus = false

	switch (cmd.notificationType) {
		case 0x05:
			// water event
			break
		case 0x14:
			handleLightEvent(cmd.event)
			break
		default:
			logDebug "Unknown notificationType: ${cmd.notificationType}"
	}
}

private handleLightEvent(event) {
	switch (event) {
		case 0:
			sendLightEvents("no light", "pushed")
			break
		case 1:
			sendLightEvents("light", "pushed_2x")
			break
		case 2:
			sendLightEvents("color change", "pushed_3x")
			break
		default:
			logDebug "Sensor is ${event}"
	}
}

private sendLightEvents(indicatorLight, buttonAction) {
	def evt = getEventMap("indicatorLight", indicatorLight)
	if (indicatorLight == "color change") {
		evt.isStateChange = true
	}
	sendEvent(evt)

	sendEvent(name: "button", value: buttonAction, displayed: false, isStateChange: true, data: [buttonNumber: 1])
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport ${cmd}"

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.configurationValue[0]

		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"
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
	return zwave.wakeUpV1.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private versionGetCmd() {
	return zwave.versionV1.versionGet().format()
}

private sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType ? 0 : 1)
	return zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType).format()
}

private sensorBinaryGetCmd() {
	return zwave.sensorBinaryV2.sensorBinaryGet().format()
}

private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private configGetCmd(param) {
	return zwave.configurationV1.configurationGet(parameterNumber: param.num).format()
}

private configSetCmd(param) {
	return zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: [param.value]).format()
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

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


private getConfigParams() {
	[
		lightSensitivityParam,
		waterBuzzerFrequencyParam,
		tempReportingIntervalParam,
		notificationBuzzerParam,
		lightDetectionDelayParam
	]
}

private getLightSensitivityParam() {
	return getParam(1, "Light Sensitivity (with light cable)", 1, 0, [0:"High", 1:"Medium", 2:"Low"], "lightSensitivity")
}

private getWaterBuzzerFrequencyParam() {
	return getParam(2, "Water Detection Buzzer Frequency (with water cable)", 1, 0, [1:"Every 5 Minutes", 0:"Every 10 Minutes", 2:"Every 30 Minutes"], "waterBuzzerFrequency")
}

private getTempReportingIntervalParam() {
	return getParam(3, "Temperature Reporting Interval (when usb powered)", 1, 60, [30:"30 Seconds", 60:"1 Minute", 180:"3 Minutes", 240:"4 Minutes"], "tempInterval")
}

private getNotificationBuzzerParam() {
	return getParam(4, "Notification Buzzer", 1, 1, [0:"Disabled", 1:"Enabled"], "buzzerEnabled")
}

private getLightDetectionDelayParam() {
	def options = [:]
	(0..20).each {
		options["${it}"] = (it == 1) ? "${it} Second" : "${it} Seconds"
	}

	return getParam(5, "Light Detection Delay (detect blinking)", 1, 0, options, "lightDetectionDelay")
}

private getParam(num, name, size, defaultVal, options, pref) {
	def val = safeToInt((settings ? settings["${pref}"] : null), defaultVal)

	def map = [num: num, name: name, size: size, value: val, pref: pref]
	if (options) {
		map.options = setDefaultOption(options, defaultVal)
	}
	return map
}

private setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


private canReportBattery() {
	return ((state.refreshStatus || !isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000))))
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


private logDebug(msg) {
	log.debug "$msg"
}

private logTrace(msg) {
	// log.trace "$msg"
}