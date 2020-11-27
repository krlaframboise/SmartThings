/*
 *  HomeSeer Multi-Sensor HSM200 (v1.0)
 *
 *  Changelog:
 *
 *    1.0 (11/26/2020)
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

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x26: 3,	// Switch Multilevel (4)
	0x31: 5,	// SensorMultilevel
	0x33: 3,	// Switch Color
	0x55: 1,	// Transport Service (2)
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration (3)
	0x71: 3,	// Notification
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x77: 1,	// Node Naming
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map tempSensor = [type: 1, scale: 0]

@Field static Map lightSensor = [type: 3, scale: 1]
		
@Field static Map colorComponentIds = [red:2, green:3, blue:4]

@Field static Map supportedColors = [red: "FF0000", green: "00FF00", blue: "0000FF", magenta: "FF00FF", yellow: "FFFF00", cyan: "00FFFF", white: "FFFFFF"]

@Field static Map motionClearTimeOptions = [1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes [DEFAULT]", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 35:"35 Minutes", 40:"40 Minutes", 45:"45 Minutes", 50:"50 Minutes", 60:"60 Minutes", 70:"70 Minutes", 80:"80 Minutes", 90:"90 Minutes", 100:"100 Minutes", 110:"110 Minutes", 120:"120 Minutes"] // 0:"Never send off" (doesn't work)

@Field static Map reportingIntervalOptions = [1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 35:"35 Minutes", 40:"40 Minutes", 45:"45 Minutes", 50:"50 Minutes", 60:"60 Minutes [DEFAULT]", 70:"70 Minutes", 80:"80 Minutes", 90:"90 Minutes", 100:"100 Minutes", 110:"110 Minutes", 120:"120 Minutes"]

@Field static Map tempAdjustmentOptions =  [136:"-12.0°F / -6.7°C", 146:"-11.0°F / -6.1°C", 156:"-10.0°F / -5.6°C", 166:"-9.0°F / -5.0°C", 176:"-8.0°F / -4.4°C", 186:"-7.0°F / -3.9°C", 196:"-6.0°F / -3.3°C", 206:"-5.0°F / -2.8°C", 216:"-4.0°F / -2.2°C", 226:"-3.0°F / -1.7°C", 236:"-2.0°F / -1.1°C", 246:"-1.0°F / -0.6°C", 0:"0.0°F / 0.0°C [DEFAULT]", 10:"1.0°F / 0.6°C", 20:"2.0°F / 1.1°C", 30:"3.0°F / 1.7°C", 40:"4.0°F / 2.2°C", 50:"5.0°F / 2.8°C", 60:"6.0°F / 3.3°C", 70:"7.0°F / 3.9°C", 80:"8.0°F / 4.4°C", 90:"9.0°F / 5.0°C", 100:"10.0°F / 5.6°C", 110:"11.0°F / 6.1°C", 120:"12.0°F / 6.7°C"]

@Field static Map debugLoggingOptions = [0:"Disabled", 1:"Enabled [DEFAULT]"]


metadata {
	definition (
		name: "HomeSeer Multi-Sensor HSM200",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "x.com.st.d.sensor.motion",
		mnmn: "SmartThingsCommunity",
		vid: "2754f5e9-f380-3338-bd97-00d066562984"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
        capability "Color Control"
		capability "platemusic11009.hsStatusLedColor"
		capability "platemusic11009.firmware"

		attribute "lastCheckIn", "string"

		fingerprint mfr: "000C", prod: "0004", model: "0001", deviceJoinName: "HomeSeer Multi-Sensor"
	}

	simulator { }

	preferences {
		configParams.each {
			createEnumInput("configParam${it.num}", "${it.name}:", it.value, it.options)
		}
		createEnumInput("debugOutput", "Debug Logging", 1, debugLoggingOptions)
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

	initialize()

	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		runIn(1, executeConfigureCmds, [overwrite: true])
	}
	return []
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	if (!device.currentValue("checkInterval")) {
		int checkInterval = ((60 * 60 * 3) + (5 * 60))
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}
	
	if (!device.currentValue("motion")) {
		sendEvent(name: "motion", value: "active")
	}
	
	if (!device.currentValue("switch")) {
		refresh()
	}
}


def configure() {
	logDebug "configure()..."

	if (!state.isConfigured) {
		refresh()
		runIn(8, executeConfigureCmds, [overwrite: true])
	}
	else {
		executeConfigureCmds()
	}
	return []
}

void executeConfigureCmds() {
	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (!state.isConfigured || !device.currentValue("switch")) {
		cmds += switchColorGetCmds()
	}

	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if (state.lifelineAssoc != true) {
		logDebug "Setting lifeline Association"
		cmds << lifelineAssociationSetCmd()
		cmds << lifelineAssociationGetCmd()
	}

	if (!state.isConfigured) {
		configParams.each {
			cmds << configGetCmd(it)
		}
	}
	else {
		configParams.each { param ->
			Integer storedVal = getParamStoredValue(param.num)
			if (storedVal != param.value) {

				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param, param.value)
				cmds << configGetCmd(param)
			}
		}
	}

	state.isConfigured = true
	sendCommands(cmds, 500)
}

int getPendingChanges() {
	int configChanges = safeToInt(configParams.count { it.value != getParamStoredValue(it.num) })
	return (configChanges + (state.lifelineAssoc != true ? 1 : 0))
}


def ping() {
	logDebug "ping()..."
	
	sendCommands([ basicGetCmd() ])
	return []
}


def on() {
	logDebug "on()..."
	
	setStatusLedColor(storedColorName)
}


def off() {
	logDebug "off()..."
	
	setStatusLedColor("off")
}


def setSaturation(saturation) {
	logDebug "setSaturation($saturation)..."

	setColor([hue: device.currentValue("hue"), saturation: saturation])
}

def setHue(hue) {
	logDebug "setHue($hue)..."

    setColor([hue: hue, saturation: device.currentValue("saturation")])
}

def setColor(color) {
	logDebug "setColor($color)..."

	int hue = safeToPercentInt(color.hue)
	int saturation = safeToPercentInt(color.saturation)

	String hex = color.hex
	if (hex) {
		def hsv = colorUtil.hexToHsv(hex)
		hue = safeToPercentInt(hsv[0])
		saturation = safeToPercentInt(hsv[1])
	}
	else {
		hex = colorUtil.hsvToHex(hue, saturation)
	}

	sendEvent(name: "hue", value: hue, displayed: false)
    sendEvent(name: "saturation", value: saturation, displayed: false)
	sendEvent(name: "color", value: hex, displayed: false)

	setStatusLedColor(findClosestMatchingSupportedColor(hex))
}

String findClosestMatchingSupportedColor(originalHex) {
	String closestColor = "white"
	int closestDistance = 1000
	def rgb = colorUtil.hexToRgb(originalHex)

	supportedColors.each { name, hex ->
		def supportedRGB = colorUtil.hexToRgb(hex)

		int distance = (Math.abs(rgb[0] - supportedRGB[0]) + Math.abs(rgb[1] - supportedRGB[1]) + Math.abs(rgb[2] - supportedRGB[2]))

		if (distance < closestDistance) {
			closestDistance = distance
			closestColor = name
		}
	}
	return closestColor
}


def setStatusLedColor(colorName) {
	logDebug "setStatusLedColor($colorName)..."
	
	colorName = "${colorName}".toLowerCase()

	int level = (colorName == "off") ? 0 : 99
	def rgb
	if (!level) {
		rgb = [0,0,0]
	}
	else {
		String hex = supportedColors.get(colorName)
		if (hex) {
			rgb = colorUtil.hexToRgb(hex)
		}
	}
	
	if (rgb) {
		List<String> cmds = [
			switchColorSetCmd(rgb)						
		]
		cmds += switchColorGetCmds()
		
		sendCommands(cmds, 200)
	}
	else {
		logDebug "${colorName} is not a supported color"
	}
}


def refresh() {
	logDebug "refresh()..."

	List<String> cmds = []
	
	cmds += switchColorGetCmds()
	
	cmds += [
		sensorMultilevelGetCmd(tempSensor),
		sensorMultilevelGetCmd(lightSensor),
		versionGetCmd(),
		lifelineAssociationGetCmd()
	]
	
	sendCommands(cmds, 300)
}


List<String> sendCommands(List<String> cmds, Integer delay=null) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, safeToInt(delay, 300))
	}
	return []
}


String lifelineAssociationGetCmd() {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: 1))
}

String lifelineAssociationSetCmd() {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

String sensorMultilevelGetCmd(sensor) {	
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: sensor.scale, sensorType: sensor.type))
}

String switchColorSetCmd(Integer red, Integer green, Integer blue) {
	return secureCmd(zwave.switchColorV3.switchColorSet(red: safeToInt(red), green:safeToInt(green), blue:safeToInt(blue)))
}

List<String> switchColorGetCmds() {
	return colorComponentIds.collect { switchColorGetCmd(it.value) }
}

String switchColorGetCmd(int colorComponentId) {
	return secureCmd(zwave.switchColorV3.switchColorGet(colorComponentId: colorComponentId))
}

String configSetCmd(Map param, Integer value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	try {
		if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		}
		else {
			return cmd.format()
		}
	}
	catch (ex) {
		return cmd.format()
	}
}


def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	}
	else {
		log.warn "Unable to parse: $description"
	}

	updateLastCheckIn()
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	}
}

String convertToLocalTimeString(dt) {
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


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "$cmd"

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		int val = cmd.scaledConfigurationValue
		
		if ((param == tempAdjustmentParam) && (getParamStoredValue(param.num) != val)) {
			sendCommands([ sensorMultilevelGetCmd(tempSensor) ])
		}

		setParamStoredValue(param.num, val)

		logDebug "${param.name}(#${param.num}) = ${val}"
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "$cmd"
	
	if (cmd.groupingIdentifier == 1) {
		logDebug "Lifeline Association: ${cmd.nodeId}"
		state.lifelineAssoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEventIfNew("firmwareVersion", fullVersion.toBigDecimal())
}


void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "$cmd"
	
	if (cmd.notificationType == 7) {
		String value = (cmd.event ? "active" : "inactive")
		
		logDebug "${device.displayName} motion is ${value}"		
		sendEvent(name: "motion", value: value, isStateChange: true)
	}
	else {
		logDebug "Unknown notification type: ${cmd.notificationType}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "$cmd"

	def val = cmd.scaledSensorValue
	switch (cmd.sensorType) {
		case tempSensor.type:
			String unit = cmd.scale ? "F" : "C"
			def temp = convertTemperatureIfNeeded(val, unit, cmd.precision)
			sendEventIfNew("temperature", temp, getTemperatureScale())
			break

		case lightSensor.type:
			sendEventIfNew("illuminance", safeToInt(val), "lux")
			break

		default:
			logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
	logTrace "$cmd"

	state[cmd.colorComponent.toString()] = cmd.value	
	runIn(1, sendColorEvents)
}

void sendColorEvents() {
	String hex = storedHex
	if (hex == "000000") {
		sendEventIfNew("statusLedColor", "off")
		sendEventIfNew("switch", "off")
	}
	else {
		String colorName = supportedColors.find { it.value == hex }?.key
		if (colorName) {
			state.colorName = colorName
			
			sendEventIfNew("switch", "on")
			sendEventIfNew("statusLedColor", colorName)
			sendEventIfNew("color", hex)

			def hsv = colorUtil.hexToHsv(hex)
			sendEventIfNew("hue", safeToPercentInt(hsv[0]))
			sendEventIfNew("saturation", safeToPercentInt(hsv[1]))
		}
	}
}

String getStoredHex() {
	return String.format("%02x%02x%02x", safeToInt(state.red), safeToInt(state.green), safeToInt(state.blue)).toUpperCase()
}

String getStoredColorName() {
	return state.colorName ?: "white"
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "$cmd"
	// Randomly sent every once in a while so ignoring it.
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	return [
		motionClearTimeParam,
		// associationLevelParam,
		lightReportingIntervalParam,
		tempReportingIntervalParam,
		tempAdjustmentParam
	]
}

Map getMotionClearTimeParam() {
	return getParam(1, "Motion Clear Time", 1, 10, motionClearTimeOptions)
}

// Map getAssociationLevelParam() {
	// return getParam(2, "Association Level Param", 1, 0, [:])
// }

Map getLightReportingIntervalParam() {
	return getParam(3, "Light Reporting Interval", 1, 60, reportingIntervalOptions)
}

Map getTempReportingIntervalParam() {
	return getParam(4, "Temperature Reporting Interval", 1, 60, reportingIntervalOptions)
}

Map getTempAdjustmentParam() {
	return getParam(5, "Temperature Adjustment", 1, 0, tempAdjustmentOptions)
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options]
}


void sendEventIfNew(String name, value, String unit="") {
	String desc = "${device.displayName}: ${name} is ${value}${unit}"

	if (device.currentValue(name) != value) {
		logDebug(desc)
		
		Map evt = [name: name, value: value, descriptionText: desc]
		if (unit) {
			evt.unit = unit
		}
		sendEvent(evt)
	}
	else {
		logTrace(desc)
	}
}


int safeToPercentInt(val, int defaultVal=0) {
	return safeToIntRange(val, defaultVal, 0, 99)
}

Integer safeToIntRange(val, Integer defaultVal, Integer lowVal, Integer highVal) {
	Integer intVal = safeToInt(val, defaultVal)
	if (intVal > highVal) {
		return highVal
	}
	else if (intVal < lowVal) {
		return lowVal
	}
	else {
		return intVal
	}
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

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled) {
		log.debug(msg)
	}
}

void logTrace(String msg) {
	// log.trace(msg)
}