/*
 *  HomeSeer Floodlight Sensor HS-FLS100-G2 (v1.0)
 *
 *  Changelog:
 *
 *    1.0 (02/07/2021)
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
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x30: 1,	// Sensor Binary
	0x31: 5,	// SensorMultilevel
	0x55: 1,	// Transport Service (2)
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration (3)
	0x71: 3,	// Notification
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// FirmwareUpdateMd
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x8E: 2,	// Multichannel association (3)
	0x9F: 1		// Security S2
]

@Field static Map tempSensor = [type: 1, scale: 1]

@Field static Map lightSensor = [type: 3, scale: 1]
		
@Field static Map pirTriggerOffPeriodOptions = [8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds", 11:"11 Seconds", 12:"12 Seconds", 13:"13 Seconds", 14:"14 Seconds", 15:"15 Seconds", 16:"16 Seconds", 17:"17 Seconds", 18:"18 Seconds", 19:"19 Seconds", 20:"20 Seconds", 25:"25 Seconds", 30:"30 Seconds", 35:"35 Seconds", 40:"40 Seconds", 45:"45 Seconds", 50:"50 Seconds", 55:"55 Seconds", 60:"1 Minute", 66:"1.1 Minutes", 72:"1.2 Minutes", 78:"1.3 Minutes", 84:"1.4 Minutes", 90:"1.5 Minutes", 96:"1.6 Minutes", 102:"1.7 Minutes", 108:"1.8 Minutes", 114:"1.9 Minutes", 120:"2 Minutes", 135:"2.25 Minutes", 150:"2.5 Minutes", 165:"2.75 Minutes", 180:"3 Minutes [DEFAULT]", 210:"3.5 Minutes", 240:"4 Minutes", 270:"4.5 Minutes", 300:"5 Minutes", 330:"5.5 Minutes", 360:"6 Minutes", 390:"6.5 Minutes", 420:"7 Minutes", 450:"7.5 Minutes", 480:"8 Minutes", 510:"8.5 Minutes", 540:"9 Minutes", 570:"9.5 Minutes", 600:"10 Minutes", 630:"10.5 Minutes", 660:"11 Minutes", 690:"11.5 Minutes", 720:"12 Minutes"]

@Field static Map luxSensorThresholdOptions = [10:"10 lux", 20:"20 lux", 30:"30 lux", 40:"40 lux", 50:"50 lux [DEFAULT]", 60:"60 lux", 70:"70 lux", 80:"80 lux", 90:"90 lux", 100:"100 lux", 125:"125 lux", 150:"150 lux", 175:"175 lux", 200:"200 lux", 225:"225 lux", 250:"250 lux", 275:"275 lux", 300:"300 lux", 325:"325 lux", 350:"350 lux", 375:"375 lux", 400:"400 lux", 425:"425 lux", 450:"450 lux", 475:"475 lux", 500:"500 lux", 525:"525 lux", 550:"550 lux", 575:"575 lux", 600:"600 lux", 625:"625 lux", 650:"650 lux", 675:"675 lux", 700:"700 lux", 725:"725 lux", 750:"750 lux", 775:"775 lux", 800:"800 lux", 825:"825 lux", 850:"850 lux", 875:"875 lux", 900:"900 lux"]

@Field static Map reportingIntervalOptions = [1:"1 Minutes", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 35:"35 Minutes", 40:"40 Minutes", 45:"45 Minutes", 60:"1 Hour [DEFAULT]", 75:"1.25 Hours", 90:"1.5 Hours", 120:"2 Hours", 150:"2.5 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 540:"9 Hours", 720:"12 Hours", 900:"15 Hours", 1080:"18 Hours", 1260:"21 Hours", 1440:"24 Hours"]

@Field static Map tempOffsetOptions =  [156:"-10.0°F / -5.6°C", 166:"-9.0°F / -5.0°C", 176:"-8.0°F / -4.4°C", 186:"-7.0°F / -3.9°C", 196:"-6.0°F / -3.3°C", 206:"-5.0°F / -2.8°C", 216:"-4.0°F / -2.2°C", 226:"-3.0°F / -1.7°C", 236:"-2.0°F / -1.1°C", 246:"-1.0°F / -0.6°C", 0:"0.0°F / 0.0°C [DEFAULT]", 10:"1.0°F / 0.6°C", 20:"2.0°F / 1.1°C", 30:"3.0°F / 1.7°C", 40:"4.0°F / 2.2°C", 50:"5.0°F / 2.8°C", 60:"6.0°F / 3.3°C", 70:"7.0°F / 3.9°C", 80:"8.0°F / 4.4°C", 90:"9.0°F / 5.0°C", 100:"10.0°F / 5.6°C"]

@Field static Map debugLoggingOptions = [0:"Disabled", 1:"Enabled [DEFAULT]"]

metadata {
	definition (
		name: "HomeSeer Floodlight Sensor HS-FLS100-G2",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		mnmn: "SmartThingsCommunity",
		vid: "c0442576-0910-3bb8-8c3c-d7acdd25462e"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Health Check"
		capability "Refresh"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
        capability "platemusic11009.firmware"

		attribute "lastCheckIn", "string"

		fingerprint mfr: "000C", prod: "0201", model: "000C", deviceJoinName: "HomeSeer Floodlight Sensor"
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
	if (!isDuplicateCommand(state.lastUpdated, 500)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		if (!state.isFirstRun) {
			state.isFirstRun = true
			refresh()
		}
		else {
			executeConfigure()
		}
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
}


void executeConfigure() {
	List<String> cmds = []

	int changes = (safeToInt(configParams.count { it.value != getParamStoredValue(it.num) }))
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	configParams.each { param ->
		Integer storedVal = getParamStoredValue(param.num)
		if (storedVal != param.value) {

			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param, param.value)
			cmds << configGetCmd(param)
		}
	}
	sendCommands(cmds)
}


def ping() {
	logDebug "ping()..."
	
	sendCommands([ basicGetCmd() ])
	return []
}


def on() {
	logDebug "on()..."
	
	sendCommands([
		switchBinarySetCmd(0xFF)
	])
}


def off() {
	logDebug "off()..."
	
	sendCommands([
		switchBinarySetCmd(0x00)
	])
}


def refresh() {
	logDebug "refresh()..."

	List<String> cmds = []
	
	cmds += [
		switchBinaryGetCmd(),
		notificationGetCmd(),
		sensorMultilevelGetCmd(tempSensor),
		sensorMultilevelGetCmd(lightSensor),
		versionGetCmd()
	]
	
	sendCommands(cmds)
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


String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String notificationGetCmd() {
	return secureCmd(zwave.notificationV3.notificationGet(event: 8, notificationType: 7, v1AlarmType: 0))
}

String basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(Integer value) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: value))
}

String sensorMultilevelGetCmd(sensor) {	
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: sensor.scale, sensorType: sensor.type))
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
		int val = ((param == tempOffsetParam) ?  cmd.configurationValue[0] : cmd.scaledConfigurationValue)
				
		setParamStoredValue(param.num, val)

		logDebug "${param.name}(#${param.num}) = ${val}"
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
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
		sendEventIfNew("motion", (cmd.event ? "active" : "inactive"))		
	}
	else if (cmd.notificationType == 8) {
		logDebug "Device Powered On"
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


void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "${cmd}"

	sendEventIfNew("switch", (cmd.value ? "on" : "off"))
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "$cmd"	
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
		pirTriggerOffPeriodParam,
		pirSensitivityParam,
		pirTriggerAlertParam,
		luxSensorThresholdParam,
		luxTempReportingIntervalParam,
		tempOffsetParam,
		floodlightControlModeParam,
		floodlightLuxSensorControlParam
	]
}

Map getPirTriggerOffPeriodParam() {
	return getParam(1, "PIR Trigger Off Period", 2, 180, pirTriggerOffPeriodOptions)
}

Map getLuxSensorThresholdParam() {
	return getParam(2, "Lux Sensor Threshold", 2, 50, luxSensorThresholdOptions)
}

Map getLuxTempReportingIntervalParam() {
	return getParam(3, "Lux/Temperature Reporting Interval", 2, 10, reportingIntervalOptions)
}

Map getPirTriggerAlertParam() {
	return getParam(4, "PIR Trigger Alert", 1, 1, [0: "Disable Alert", 1:"Enable Alert [DEFAULT]"])
}

Map getFloodlightControlModeParam() {
	return getParam(5, "Floodlight Control Mode", 1, 1, [1:"PIR/Lux or Z-wave Controller [DEFAULT]", 0:"Z-wave Controller Only"])
}

Map getFloodlightLuxSensorControlParam() {
	return getParam(6, "Floodlight Lux Sensor Control", 1, 0, [0:"Lux and PIR Trigger [DEFAULT]", 1:"Lux Only"])
}

Map getTempOffsetParam() {
	return getParam(7, "Measured Temperature Offset", 1, 0, tempOffsetOptions) 
}

Map getPirSensitivityParam() {
	return getParam(8, "PIR Sensitivity Level", 1, 2, [0:"Low", 1:"Mid", 2:"High [DEFAULT]"])
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
