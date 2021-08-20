/*
 *  Zooz ZAC36 Titan Valve Actuator v0.0
 *
 *  Changelog:
 *
 *    0.0 (08/20/2021)
 *      - Beta Release
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
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x80: 1,	// Battery
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 1,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static int tempSensorType = 1
@Field static int fahrenheitParamValOffset = 256
@Field static int negativeParamValOffset = 4096
@Field static int heatAlarm = 4
@Field static int heatAlarmHigh = 2
@Field static int heatAlarmLow = 6
@Field static int powerDisconnected = 2
@Field static int powerManagement = 8
@Field static int waterAlarm = 5
@Field static int waterValve = 15
@Field static int normalValveOperation = 0

@Field static Map configParams = [
	inverseWaterValveReportParam: [num:17, label:"Inverse Water Valve Report", size:1], 
	tempThresholdParam: [num:34, label:"Temperature Reporting Threshold (°F)", size:2, defaultVal:4, range:"0..255", isTempVal:true], 
	tempOffsetParam: [num:35, label:"Temperature Sensor Offset (°F)", size:2, defaultVal:0, range:"-255..255", isTempVal:true],
	overheatAlarmTriggerParam: [num:36, label:"Overheat Alarm Trigger (°F)", size:2, defaultVal:104, range:"0..255", isTempVal:true],
	overheatCancelTriggerParam: [num:37, label:"Overheat Cancellation Trigger (°F)", size:2, defaultVal:86, range:"0..255", isTempVal:true],
	freezeAlarmTriggerParam: [num:40, label:"Freeze Alarm Trigger (°F)", size:2, defaultVal:32, range:"0..255", isTempVal:true],
	freezeCancelTriggerParam: [num:41, label:"Freeze Cancellation Trigger (°F)", size:2, defaultVal:36, range:"0..255", isTempVal:true],
	freezeAlarmValveControlParam: [num:42, label:"Valve Control with Freeze Alarm", size:1, defaultVal:1, options:[1:"Enabled [DEFAULT]", 0:"Disabled"]],
	leakAlarmValveControlParam: [num:51, label:"Valve Control with Leak Alarm", size:1, defaultVal:1, options:[1:"Enabled [DEFAULT]", 0:"Disabled"]],
	// autoTestModeParam: [num:61, label:"Auto Test Mode", size:1, defaultVal:3, options:[3: "Enabled when included and excluded [DEFAULT]", 1:"Enabled when excluded", 2:"Enabled when included"]],
	// autoTestModeFrequencyParam: [num:62, label:"Auto Test Mode Frequency (Days)", size:1, defaultVal:14, range:"1..30"],
	soundAlarmParam: [num:65, label:"Sound Alarm and Notifications", size:1, defaultVal:1, options:[1:"Enabled [DEFAULT]", 0:"Disabled"]],
	ledBrightnessParam: [num:66, label:"LED Indicator Brightness", size:1, defaultVal:66, range:"0..99"],
	keylockProtectionParam: [num:67, label:"Keylock Protection", size:1, defaultVal:0, options:[0:"Disabled [DEFAULT]", 1:"Enabled"]]
]

metadata {
	definition (
		name: "Zooz ZAC36 Titan Valve Actuator",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.watervalve",
		mnmn: "SmartThingsCommunity",
		vid: "14a923d9-c2f3-37a0-9ae5-531446a97b51"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Valve"
		capability "Water Sensor"
		capability "Configuration"
		capability "Temperature Measurement"
		capability "Power Source"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		fingerprint mfr:"027A", prod:"0101", model: "0036", deviceJoinName:"Zooz ZAC36 Titan Valve Actuator" // zw:Ls2a type:1000 mfr:027A prod:0101 model:0036 ver:1.07 zwv:7.13 lib:03 cc:5E,55,98,9F,6C,22 sec:25,85,8E,59,71,86,72,5A,87,73,7A,31,70,80
	}

	preferences {
		configParams.each { name, param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.label}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			} else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.label}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					range: param.range
			}
		}

		input "debugLogging", "enum",
			title: "Logging:",
			required: false,
			defaultValue: "1",
			options: ["0":"Disabled", "1":"Enabled [DEFAULT]"]
	}
}

def installed() {
	logDebug "installed()..."

	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		runIn(2, executeConfigureCmds)
	}
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 3) + 300), displayed: falsle, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	if (device.currentValue("powerSource") == null) {
		sendEvent(name: "powerSource", value: "dc", displayed: false)
	}
	
	if (device.currentValue("battery") == null) {
		sendEvent(name: "battery", value: 100, unit: "%")
	}

	if (device.currentValue("water") == null) {
		sendEvent(name: "water", value: "dry", displayed: false)
	}

	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)
}

def configure() {
	logDebug "configure()..."

	refresh()

	runIn(15, executeConfigureCmds)

	return []
}

void executeConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []
	
	if (getParamStoredValue(configParams.inverseWaterValveReportParam.num) == null) {
		cmds << configGetCmd(configParams.inverseWaterValveReportParam)
	}

	configParams.each { name, param ->
		Integer storedVal = getParamStoredValue(param.num)
		Integer settingVal = getSettingValue(param.num)
		if ((settingVal != null) && (settingVal != storedVal)) {
			logDebug "CHANGING ${param.label}(#${param.num}) from ${storedVal} to ${settingVal}"
			
			if ((param == configParams.tempOffsetParam) && (settingVal < 0)) {
				// Adjust high byte for negative value
				settingVal = (settingVal + negativeParamValOffset)
			}
			
			if (param.isTempVal) {
				// Adjust high byte for farenheit value
				settingVal = (settingVal + fahrenheitParamValOffset)
			}
			
			cmds << configSetCmd(param, settingVal)
			cmds << configGetCmd(param)
		}
	}

	if (cmds) {
		sendCommands(cmds)
	}
}

def ping() {
	logDebug "ping()..."
	return [ batteryGetCmd() ]
}

def close() {
	logDebug "close()..."
	return delayBetween([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	], 200)
}

def open() {
	logDebug "open()..."
	return delayBetween([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	], 200)
}

def refresh() {
	logDebug "refresh()..."
	sendCommands([
		batteryGetCmd(),
		configGetCmd(configParams.inverseWaterValveReportParam),
		switchBinaryGetCmd(),
		versionGetCmd(),
		sensorBinaryGetCmd(),
		sensorMultilevelGetCmd(tempSensorType)
	])
	
	if (device.currentValue("syncStatus") != "Synced") {
		executeConfigureCmds()
	}
}

void sendCommands(List<String> cmds, Integer delay=500) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String sensorBinaryGetCmd() {
	return secureCmd(zwave.sensorBinaryV1.sensorBinaryGet())
}

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

String indicatorGetCmd() {
	return secureCmd(zwave.indicatorV1.indicatorGet())
}

String indicatorSetCmd(int value) {
	return secureCmd(zwave.indicatorV1.indicatorSet(value: value))
}

String sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType ? 0 : 1)
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
}

String configSetCmd(Map param, int value) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		return cmd.format()
	}
}

def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} else {
		log.warn "Unable to parse: $description"
	}
	return []
}

void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logTrace "$cmd"
	runIn(4, refreshSyncStatus)

	Map param = configParams.find { name, param -> param.num == cmd.parameterNumber }?.value
	if (param) {
		int value = cmd.scaledConfigurationValue
		
		if ((param == configParams.tempOffsetParam) && (value > negativeParamValOffset)) {
			value = (value - negativeParamValOffset)// Adjust high byte for negative value
		}
		
		if (param.isTempVal) {		
			value = (value - fahrenheitParamValOffset) 	// Adjust high byte for farenheit value.
		}
		
		logDebug "${param.label}(#${param.num}) = ${value}"
		setParamStoredValue(param.num, value)
	} else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : safeToInt(cmd.batteryLevel))
	if (val > 100) val = 100
	if (val < 1) val = 1

	logDebug "${device.displayName}: battery is ${val}%"
	sendEvent(name: "battery", value: val, unit: "%", isStateChange: true, displayed:true)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logDebug "${cmd}"
	sendValveEvent(cmd.value)
}

void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "$cmd"	
	switch (cmd.sensorType) {
		case tempSensorType:
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, (cmd.scale ? "F" : "C"), cmd.precision)			
			sendEventIfNew("temperature", temp, true, temperatureScale)
			break
		default:
			logDebug "Unhandled: ${cmd}"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	logDebug "${cmd}"	
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "$cmd"	
	switch (cmd.notificationType) {
		case heatAlarm:
			sendHeatAlarmEvent(cmd.event)
			break
		case waterAlarm:
			sendWaterEvent(cmd.event)
			break
		case waterValve:
			// sendValveEvent(cmd.event) (using switch binary reports instead)
			break
		case powerManagement:
			sendEventIfNew("powerSource", (cmd.event == powerDisconnected ? "battery" : "dc"))
			break
		default:
			logDebug "Unhandled: ${cmd}"
	}	
}

void sendHeatAlarmEvent(event) {
	switch (event) {
		case heatAlarmHigh:
			logDebug "High Temp"
			break
		case heatAlarmLow:
			logDebug "Low Temp"
			break
		default:			
			logDebug "Normal Temp"
	}
}

void sendWaterEvent(rawVal) {
	sendEventIfNew("water", (rawVal ? "wet" : "dry"))
}

void sendValveEvent(rawVal) {
	rawVal = ((getParamStoredValue(configParams.inverseWaterValveReportParam.num) == normalValveOperation) ? rawVal : (rawVal ? false : true))
	sendEventIfNew("valve", (rawVal ? "open" : "closed"))
	sendEventIfNew("switch", (rawVal ? "on" : "off"))
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	return safeToInt(configParams.count { name, param ->
	((getSettingValue(param.num) != null) && (getSettingValue(param.num) != getParamStoredValue(param.num))) })
}

Integer getSettingValue(int paramNum) {
	return safeToInt(settings ? settings["configParam${paramNum}"] : null, null)
}

Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}

void sendEventIfNew(String name, value, boolean displayed=true, String unit="") {
	String desc = "${device.displayName}: ${name} is ${value}${unit}"
	if (device.currentValue(name) != value) {
		if (name != "syncStatus") {
			logDebug(desc)
		}		
		Map event = [name: name, value: value, descriptionText: desc, displayed: displayed]
		if (unit) {
			event.unit = unit
		}
		sendEvent(event)
	}
}

Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	} else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	} else {
		return defaultVal
	}
}

boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}

void logTrace(String msg) {
	log.trace "$msg"
}