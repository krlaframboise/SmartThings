/**
 *  Fibaro Motion Sensor v1.0.1
 *  (Model: FGMS-001)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0.1 (05/04/2017)
 *      - Initial Release
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (
		name: "Fibaro Motion Sensor ZW5", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.sensor.motion"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Temperature Measurement"
		capability "Acceleration Sensor"
		capability "Three Axis"
		capability "Tamper Alert"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckin", "string"
		attribute "lastUpdate", "string"
		attribute "vibrationData", "string"
		attribute "vibrationStatus", "string"
		attribute "nightDay", "string"
		attribute "axisX", "number"
		attribute "axisY", "number"
		attribute "axisZ", "number"
		attribute "earthquakeMagnitude", "number"
		
		command "clearActivity"
		
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x98, 0x7A", outClusters: ""
		
		fingerprint mfr:"010F", prod:"0801", model:"2001"
	}

	simulator { }
	
	preferences {
		getParagraphInput("", "")
		getOptionsInput(motionSensitivityParam)
		getOptionsInput(motionRetriggerParam)
		getOptionsInput(motionModeParam)
		getOptionsInput(motionNightThresholdParam)
		
		getParagraphInput("", "")
		getOptionsInput(vibrationSensitivityParam)
		getOptionsInput(vibrationRetriggerParam)
		getOptionsInput(vibrationTypeParam)
						
		getBoolInput("displayVibrationEvents", "Display Vibration Events?", false)		
		
		getParagraphInput("", "")
		getOptionsInput(lightReportingThresholdParam)
		getOptionsInput(lightReportingIntervalParam)
		
		getParagraphInput("", "")
		getOptionsInput(tempReportingThresholdParam)
		getOptionsInput(tempReportingIntervalParam)
		getOptionsInput(tempMeasuringIntervalParam)
		getOptionsInput(tempOffsetParam)		
		
		getParagraphInput("", "")
		getOptionsInput(ledBrightnessParam)
		getOptionsInput(ledBrightnessLowThresholdParam)
		getOptionsInput(ledBrightnessHighThresholdParam)
		getOptionsInput(vibrationLedModeParam)
		getOptionsInput("motionLedMode", "Motion LED Mode", motionLedModeSetting, motionLedModeOptions)
		getOptionsInput("motionLedColor", "Motion LED Color", motionLedColorSetting, motionLedColorOptions)
		getOptionsInput(ledBlueTempThresholdParam)
		getOptionsInput(ledRedTempThresholdParam)
		
		getParagraphInput("", "")
		getOptionsInput("wakeUpInterval", "Checkin Interval", checkinIntervalSetting, checkinIntervalOptions)
		
		getOptionsInput("batteryReportingInterval", "Battery Reporting Interval", batteryReportingIntervalSetting, checkinIntervalOptions)
				
		getBoolInput("debugOutput", "Enable debug logging?", true)		
	}

	tiles(scale: 2) {
				
		standardTile("motion", "device.motion", width: 4, height: 4) {
			state "inactive", label:'No Motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
			state "active", label:'Motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}

		standardTile("vibrationStatus", "device.vibrationStatus", inactiveLabel: false, width: 2, height: 2) {
			state "accelerationInactive", label:'Inactive', icon:"st.motion.acceleration.inactive", backgroundColor:"#cccccc"
			state "accelerationActive", label:'Active', icon:"st.motion.acceleration.active", backgroundColor:"#00a0dc"
			state "earthquakeInactive", label:'No Earthquakes'
			state "earthquakeActive", label:'Earthquake', icon:"st.secondary.activity"
			state "tamperClear", label:'Tamper Clear', icon:""
			state "tamperDetected", label:'Tamper', icon:"st.security.alarm.alarm"
		}
		
		valueTile("nightDay", "device.nightDay", inactiveLabel: false, width: 2, height: 1) {
			// state "day", label:'', icon:"st.Weather.weather14"
			state "day", label:'', icon:"http://cdn.device-icons.smartthings.com/Weather/weather14-icn@2x.png"
			state "night", label:'', icon:"st.Weather.weather4"
		}
		
		valueTile("vibrationData", "device.vibrationData", inactiveLabel: false, width: 2, height: 2) {
			state "vibrationData", label:'${currentValue}'
		}
		
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 1) {
			state "illuminance", label:'${currentValue} lux'
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
		standardTile("clear", "device.generic", width: 2, height: 2) {
			state "default", label:'Clear Activity', action: "clearActivity", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh Configuration', action: "refresh", icon:"st.secondary.preferences"
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "motion"
		details(["motion", "temperature", "nightDay", "illuminance", "vibrationStatus", "vibrationData", "battery", "clear", "refresh", "lastUpdate"])
	}
}

private getParagraphInput(title, desc) {
	input "", "paragraph", 
		title: "${title}", 
		description: "${desc}", 
		required: false, 
		displayDuringSetup: true
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}", 
		defaultValue: defaultVal, 
		required: false
}

private getOptionsInput(name, title, defaultVal, options) {
	return getOptionsInput([
		prefName: "${name}",
		name: "${title}",
		val: defaultVal,
		options: options
	])
}

private getOptionsInput(param) {
	input "${param.prefName}", "enum",
		title: "${param.name}:",
		defaultValue: "${param.val}",
		required: false,
		displayDuringSetup: true,
		options: param.options?.collect { name, val -> name }
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		if (vibrationTypeSettingName == "tamper" && getAttrValue("vibrationData")) {
			sendEvent(createEventMap("vibrationData", "", false))
		}
		
		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		state.pendingChanges = true
	}	
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	def refreshAll = state.pendingRefresh
	
	initializeOptionalAttributes()
	
	if (state.pendingChanges == null) {
		sendEvent(createEventMap("vibrationData", "", false))
		logTrace "Waiting 1 second because this is the first time being configured"
		cmds << "delay 1000"
		cmds += refreshSensorData()
	}
	
	cmds += initializeCheckin()
				
	configParams.each { param ->	
		cmds += updateConfigVal(param, refreshAll)
	}
	
	if (!cmds && canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	if (cmds) {
		cmds << basicGetCmd()
	}		
	return cmds ? delayBetween(cmds, 500) : []	
}

private initializeOptionalAttributes() {
	def optionalAttrs = [
		"acceleration": "inactive",
		"tamper": "clear",
		"threeAxis": "0,0,0",
		"axisX": 0,
		"axisY": 0,
		"axisZ": 0,
		"earthquakeMagnitude": 0
	]	
	optionalAttrs.each { name, val ->
		if (getAttrValue("${name}") == null) {
			sendEvent(createEventMap("${name}", val, false))
		}
	}
}

private updateConfigVal(param, refreshAll) {
	def result = []
	def newVal = param.options ? convertOptionSettingToInt(param.options, param.val) : param.val
	def oldVal = state["configVal${param.num}"]	
	
	if (refreshAll || (oldVal != newVal)) {
		logDebug "${param.name}(#${param.num}): changing ${oldVal} to ${newVal}"
		result << configSetCmd(param, newVal)
		result << configGetCmd(param)
	}	
	return result
}

private initializeCheckin() {
	def result = []
	def intervalSeconds = convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
	
	if (state.pendingRefresh || state.checkinInterval != intervalSeconds) {		
		
		state.checkinInterval = intervalSeconds
		
		result << wakeUpIntervalSetCmd(intervalSeconds)
		
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((intervalSeconds * 3) + (5 * 60))
	
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	return result	
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def refresh() {
	state.pendingRefresh = true
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."		
}

def clearActivity() {
	def vibrationType = vibrationTypeSettingName
	def vibrationAttr = getAttrValue("vibrationStatus")
	
	if ((vibrationType == "tamper" && vibrationAttr != "tamperClear") || (vibrationType == "earthquake" && vibrationAttr != "earthquakeInactive")) {
	
		sendVibrationStatusEvents(0x00, true)
		
		if (vibrationType == "earthquake") {
			sendEarthquakeEvents(0, true)
		}
	}
}

def parse(String description) {
	def result = []
	// logTrace "parse: $description"
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (!description?.startsWith("Err")) {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		// logTrace "secure cmd: $encapCmd"
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"	
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	// Ignoring these events because they're only sent when the action button is pushed.
	
	def result = []
	def cmdClass = getVersionSafeCmdClass(cmd.commandClass)
	def parsedCmd = cmdClass?.command(cmd.command)?.parse(cmd.data)
	if (parsedCmd) {			
		result += zwaveEvent(parsedCmd)		
	}
	else {
		log.warn "Unable to parse crc16encap command"
	}
	return result	
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	result += configure()
		
	if (result) {
		result << "delay 1200"
	}	
	result << wakeUpNoMoreInfoCmd()
	return sendResponse(result)
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private refreshSensorData() {
	logDebug "Refreshing Sensor Data"
	return delayBetween([
		batteryGetCmd(),
		sensorMultilevelGetCmd(lightSensorType, 1),
		sensorMultilevelGetCmd(tempSensorType, 0)		
	], 500)
}

private canReportBattery() {
	def reportEveryMS = (convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled Command: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	// logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	sendEvent(createEventMap("battery", val, null, null, "%"))
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	def val = (cmd.scaledConfigurationValue == -1 ? 255 : cmd.scaledConfigurationValue)
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		def name = configParam.options?.find { it.value == val}?.key
		logDebug "${configParam.name}(#${configParam.num}) = ${name != null ? name : val} (${val})"
		state["configVal${cmd.parameterNumber}"] = val		
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}
	
	runIn(10, finalizeConfiguration)
	return []
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	if (state.pendingChanges || state.pendingRefresh) {
		sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
		state.pendingChanges = false
		state.pendingRefresh = false	
	}	
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	// logTrace "SensorMultilevelReport: ${cmd}"
	switch (cmd.sensorType) {
		case tempSensorType:
			sendTempEvent(cmd)
			break
		case lightSensorType:			
			sendLightEvents(cmd.scaledSensorValue)
			break
		case earthquakeSensorType:		
			if (vibrationTypeSettingName == "earthquake") {
				sendEarthquakeEvents(cmd.scaledSensorValue)
			}
		case { threeAxisSensorTypes.find { type -> type == it } }:			
			if (vibrationTypeSettingName == "acceleration") {
				sendThreeAxisEvents(cmd)
			}
			break
		default:
			logDebug "Unknown Sensor Type: ${cmd}"
	} 
	return []
}

private sendTempEvent(cmd) {
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def val = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
	logDebug "Temperature ${val} °${getTemperatureScale()}"
	sendEvent(createEventMap("temperature", val, null, null, getTemperatureScale()))			
}

private sendThreeAxisEvents(cmd) {
	// logTrace "handleThreeAxisEvent(${cmd})"
	def val = cmd.scaledSensorValue?.toInteger()
	def map
	
	switch (cmd.sensorType) {
		case threeAxisXSensorType:
			state.x = val
			map = createEventMap("axisX", val, false)
			break
		case threeAxisYSensorType:
			state.y = val
			map = createEventMap("axisY", val, false)
			break
		case threeAxisZSensorType:
			state.z = val
			map = createEventMap("axisZ", val, false)
			break
		default:
			map = null
	}	
	if (map) {
		sendEvent(map)
	}
	runIn(3, generateThreeAxisEvent)
}

def generateThreeAxisEvent() {
	def x = getAttrValue("axisX")
	def y = getAttrValue("axisY")
	def z = getAttrValue("axisZ")
	
	sendEvent(createEventMap("threeAxis", "${x},${y},${z}", false))
	sendEvent(createEventMap("vibrationData", "${x}x,${y}y,${z}z", false))
}

private sendLightEvents(val) {
	logDebug "Illuminance ${val} lux"
	sendEvent(createEventMap("illuminance", val, null, null, "lux"))
	sendEvent(createEventMap("nightDay", (val > convertOptionSettingToInt(motionNightThresholdParam.options, motionNightThresholdParam.val)) ? "day" : "night", false))
}

private sendEarthquakeEvents(val, refreshing=false) {
	logTrace "sendEarthquakeEvents(${val})"
	def mVal = roundTwoPlaces(safeToDec(val))
	if (mVal > 0 || refreshing) {		
		sendEvent(createEventMap("vibrationData", mVal ? "Mag. ${mVal}" : "", false))
		sendEvent(createEventMap("earthquakeMagnitude", mVal, mVal ? displayVibrationEventsSetting : false, mVal ? "Magnitude ${mval} Earthquake Detected" : null))
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {	
	logTrace "BasicReport: $cmd"
	
	state.pendingChanges = false
	state.pendingRefresh = false
	
	sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	sendMotionEvent(cmd.value ? 0xFF : 0x00)	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	// logTrace "NotificationReport:${cmd}"
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				if (cmd.eventParameter[0] == 3) {
					sendVibrationStatusEvents(0x00)
				}
				else if (cmd.eventParameter[0] == 8) {
					sendMotionEvent(0x00)
				}
				break
			case 3:
				sendVibrationStatusEvents(0xFF)
				break
			case 8:
				sendMotionEvent(0xFF)
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
	else {
		logDebug "Unknown Notification Type: ${cmd}"
	}
	return []
}

private sendMotionEvent(val) {
	def motionVal = val ? "active" : "inactive"
	logDebug "motion ${motionVal}"
	sendEvent(createEventMap("motion", motionVal))
}

private sendVibrationStatusEvents(val, refreshing=false) {
	// logTrace "sendVibrationStatusEvents(${val})"
	def map
	def vibrationType = vibrationTypeSettingName
	if (vibrationType == "acceleration" || vibrationType == "earthquake") {
		map = createEventMap("acceleration", val ? "active" : "inactive", (vibrationType == "acceleration" && displayVibrationEventsSetting))
	}
	else {
		map = createEventMap("tamper", val ? "detected" : "clear", displayVibrationEventsSetting)
	}
	
	def valName = "${map.value}".capitalize()
	def dataVal = "${vibrationType}${valName}"
	
	if (!map.displayed) {
		logDebug "${map.name} is ${map.value}"
	}
	
	if ((dataVal != "tamperClear" && dataVal != "earthquakeInactive") || refreshing) {
		sendEvent(map)
		sendEvent(createEventMap("vibrationStatus", dataVal, false))
	}
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private sensorMultilevelGetCmd(sensorType, scale) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensorType, scale: scale))
}

private wakeUpIntervalSetCmd(val) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId))
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private configSetCmd(param, val) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: val))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}


private secureCmd(cmd) {
	if (canSecureCmd(cmd)) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private canSecureCmd(cmd) {
	// This code was extracted from example by @ClassicGOD
	return zwaveInfo?.zw?.contains("s") && zwaveInfo?.sec?.contains(Integer.toHexString(cmd.commandClassId))
}

private getCommandClassVersions() {
	// sec:20,5A,85,84,71,8E,70,30,9C
	// cc:5E,86,72,59,80,73,56,22,31,98,7A
	[
		0x20: 1,	// Basic *sec
		0x30: 2,	// Sensor Binary *sec
		0x31: 5,	// Sensor Multilevel
		0x56: 1,	// Crc16 Encap
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally *sec
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration *sec
		0x71: 3,  // Notification v4 *sec
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp *sec
		0x85: 2,  // Association *sec
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association *sec
		0x9C: 1,	// Sensor Alarm *sec
		0x98: 1		// Security
	]
}

private getVersionSafeCmdClass(cmdClass) {
	def version = commandClassVersions[safeToInt(cmdClass)]
	if (version) {
		return zwave.commandClass(cmdClass, version)
	}
	else {
		return zwave.commandClass(cmdClass)
	}
}

private getTempSensorType() { return 1 }
private getLightSensorType() { return 3 }
private getearthquakeSensorType() { return 25 }
private getThreeAxisSensorTypes() { 
	return [
		threeAxisXSensorType, 
		threeAxisYSensorType, 
		threeAxisZSensorType
	]  
}
private getThreeAxisXSensorType() { return 54 }
private getThreeAxisYSensorType() { return 53 }
private getThreeAxisZSensorType() { return 52 }

// Configuration Parameters
private getConfigParams() {
	return [		
		motionSensitivityParam,
		motionBlindTimeParam,
		motionRetriggerParam,
		motionModeParam,
		motionNightThresholdParam,
		vibrationSensitivityParam,
		vibrationRetriggerParam,
		vibrationTypeParam,		
		lightReportingThresholdParam,
		lightReportingIntervalParam,
		tempReportingThresholdParam,
		tempReportingIntervalParam,
		tempMeasuringIntervalParam,
		tempOffsetParam,
		vibrationLedModeParam,
		motionLedModeParam,
		ledBrightnessParam,
		ledBrightnessLowThresholdParam,
		ledBrightnessHighThresholdParam,
		ledBlueTempThresholdParam,
		ledRedTempThresholdParam
		// tamperCancellationParam
	]
}

private getMotionSensitivityParam() {
	return createConfigParamMap(1, "Motion Sensitivity", 2, motionSensitivityOptions, "motionSensitivity")
}

private getMotionBlindTimeParam() {
	def val	
	def motionRetrigger = motionRetriggerParam
	switch(convertOptionSettingToInt(motionRetrigger.options, motionRetrigger.val)) {
		case 5:
			val = 2
			break
		case 10:
			val = 6
			break
		default:
			val = 15
	}
	return createConfigParamMap(2, "Motion Sensor's Blind Time", 1, null, "motionBlindTime", val)
}

private getMotionRetriggerParam() {
	return createConfigParamMap(6, "Motion Retrigger", 2, getIntervalOptions(30), "motionRetrigger")
}

private getMotionModeParam() {
	return createConfigParamMap(8, "Motion Mode", 1, ["Always Detect Motion${defaultOptionSuffix}": 0, "Only Detect Motion During the Day":1, "Only Detect Motion at Night":2], "motionMode")
}

private getMotionNightThresholdParam() {
	return createConfigParamMap(9, "Motion Night Threshold", 2, getLuxOptions(200), "motionNightThreshold")
}

private getVibrationSensitivityParam() {
	return createConfigParamMap(20, "Vibration Sensitivity", 1, vibrationSensitivityOptions, "vibrationSensitivity")
}

private getVibrationRetriggerParam() {
	return createConfigParamMap(22, "Vibration Retrigger", 2, getIntervalOptions(15), "vibrationRetrigger")
}

private getVibrationTypeParam() {
	return createConfigParamMap(24, "Vibration Type", 1, vibrationTypeOptions, "vibrationType")
}

// private getTamperCancellationParam() {
	// return createConfigParamMap(25, "Tamper Cancellation", 1, ["Do Not Send Tamper Cancellation Reports":0, "Send Tamper Cancellation Reports":1], "tamperCancellation")
// }

private getVibrationLedModeParam() {
	return createConfigParamMap(89, "Vibration LED", 1, ["Disabled": 0, "Flashing White, Red, and Blue${defaultOptionSuffix}": 1], "vibrationLedParam")	
}

private getLightReportingThresholdParam() {
	return createConfigParamMap(40, "Light Reporting Threshold", 2, getLuxOptions(200, [zeroName:"No Reports"]), "lightReportingThreshold")
}

private getLightReportingIntervalParam() {
	return createConfigParamMap(42, "Light Reporting Interval", 2, getIntervalOptions(0, [zeroName:"No Reports"]), "lightReportingInterval")
}

private getTempReportingThresholdParam() {
	return createConfigParamMap(60, "Temperature Reporting Threshold", 2, getTempOptions(10, [zeroName: "Don't send reports based on temperature change"]), "tempReportingThreshold")
}

private getTempMeasuringIntervalParam() {
	return createConfigParamMap(62, "Temperature Measuring Interval", 2, getIntervalOptions((15 * 60), [zeroName:"Don't Measure Temperature"]), "tempMeasuringInterval")
}

private getTempReportingIntervalParam() {
	return createConfigParamMap(64, "Temperature Reporting Interval", 2, getIntervalOptions(0, [zeroName:"No Reports"]), "tempReportingInterval")
}

private getTempOffsetParam() {
	return createConfigParamMap(66, "Temperature Offset", 2, getTempOptions(0, [zeroName:"No Offset", min:-100, max:100]), "tempOffset")
}

private getMotionLedModeParam() {
	def ledMode = convertOptionSettingToInt(motionLedModeOptions, motionLedModeSetting)
	def ledColor = convertOptionSettingToInt(motionLedColorOptions, motionLedColorSetting)

	def val	= ledMode == 0 ? 0 : (((ledMode - 1) * 9) + ledColor)
	if (ledMode == 3 && ledColor > 1) {
		val = (val - 1) // Adjust value because the Flashlight option isn't available for the 3rd mode.
	}
	return createConfigParamMap(80, "Motion LED Mode/Color", 1, null, "motionLedMode", val)	
}

private getLedBrightnessParam() {
	return createConfigParamMap(81, "LED Brightness", 1, getPercentageOptions(50, [zeroName: "Determined by LED Brightness Thresholds"]), "ledBrightness")
}

private getLedBrightnessLowThresholdParam() {
	return createConfigParamMap(82, "LED Brightness 1% Threshold", 2, getLuxOptions(100), "ledBrightnessLowThreshold")
}

private getLedBrightnessHighThresholdParam() {
	return createConfigParamMap(83, "LED Brightness 100% Threshold", 2, getLuxOptions(1000, [min:5, max:5000]), "ledBrightnessHighThreshold")
}

private getLedBlueTempThresholdParam() {
	return createConfigParamMap(86, "Blue LED Temperature Threshold", 2, getTempValueOptions(18, [min:0, max:30]), "ledBlueTempThreshold")
}

private getLedRedTempThresholdParam() {
	return createConfigParamMap(87, "Red LED Temperature Threshold", 2, getTempValueOptions(28, [min:10, max:40]), "ledRedTempThreshold")
}

private createConfigParamMap(num, name, size, options, prefName, val=null) {
	if (val == null) {
		val = (settings?."${prefName}" ?: findDefaultOptionName(options))
	}
	return [
		num: num, 
		name: name, 
		size: size, 
		options: options, 
		prefName: prefName,
		val: val
	]
}


// Settings
private getVibrationTypeSettingName() {
	if (vibrationTypeSetting?.startsWith("Acceleration")) {
		return "acceleration"
	}
	else if (vibrationTypeSetting?.startsWith("Earthquake")) {
		return "earthquake"
	}
	else {
		return "tamper"
	}
}

private getMotionLedModeSetting() {
	return settings?.motionLedMode != null ? settings.motionLedMode : findDefaultOptionName(motionLedModeOptions)
}

private getMotionLedColorSetting() {
	return settings?.motionLedColor != null ? settings?.motionLedColor : findDefaultOptionName(motionLedModeOptions)
}

private getVibrationTypeSetting() {
	return settings?.vibrationType ?: findDefaultOptionName(vibrationTypeOptions)
}

private getDisplayVibrationEventsSetting() {
	return settings?.displayVibrationEvents ?: false
}

private getCheckinIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}

private getMotionSensitivityOptions() {
	def options = ["Most Sensitive": 8]
	
	def val = 7
	(2..19).each {
		val += 13
		options["${it}"] = val
	}
	options["Least Sensitive"] = 255
	
	return setDefaultOption(options, 20)
}

private getMotionLedModeOptions() {
	return setDefaultOption([
		"Disabled": 0,
		"Long blink when motion is detected": 1,
		"Long blink when motion is detected and short blink when motion is detected again.": 2,
		"Long blink when motion is detected and 2 short blinks when motion is detected again.":3
	], 2)
}

private getMotionLedColorOptions() {
	return setDefaultOption([
		"Color determined by Red/Blue Temperature Thresholds": 1,
		"White Flashlight (10 Seconds)": 2,
		"White": 3,
		"Red": 4,
		"Green": 5,
		"Blue": 6,
		"Yellow": 7,
		"Cyan": 8,
		"Magenta": 9
	], 1)
}

private getVibrationTypeOptions() {
	return setDefaultOption([
		"Acceleration": 2,
		"Earthquake": 1,
		"Tamper Alert": 0
	], 2)
}

private getVibrationSensitivityOptions() {
	def options = ["Disabled": 0, "Most Sensitive": 1]	
	def val = 1
	(2..19).each {
		val += (it % 3) ? 6 : 7
		options["${it}"] = val
	}
	options["Least Sensitive"] = 121
	return setDefaultOption(options, 20)
}

private getLightReportingThresholdOptions() {
	return getMinLuxOptions(200, "0 Lux")
}

private getCheckinIntervalOptions() {
	return getIntervalOptions((2 * 60 * 60), [min:(5 * 60), max:(18*60*60)])
}

private getTempOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = ((data?.zeroName && (!data?.min || data?.min > 0)) ? 0 : (data?.min != null ? data.min : 1))
	def max = data?.max != null ? data?.max : 100
	
	for (int i = min; i <= max; i += ((i < 5 && i >= -5) ? 1 : (i == 1 ? 4 : 5))) {
		if (i == 0 && data?.zeroName != null) {
			options["${data?.zeroName}"] = i
		}
		else {
			options["${i.toBigDecimal() * 0.1}°C / ${(((i.toBigDecimal() * 0.1)*9)/5)}°F"] = i
		}
	}
	return setDefaultOption(options, defaultVal)
}

private getTempValueOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : 40
	
	for (int i = min; i <= max; i += 1) {
		if (i == 0 && data?.zeroName != null) {
			options["${data?.zeroName}"] = i
		}
		else {
			options["${i}°C / ${(((i*9)/5) + 32)}°F"] = i
		}
	}
	return setDefaultOption(options, defaultVal)
}

private getIntervalOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : (9 * 60 * 60)
	
	[0,1,2,3,4,5,10,15,30,45].each {
		if (withinRange(it, min, max)) {
			if (it == 0 && data?.zeroName != null) {
				options["${data?.zeroName}"] = it
			}
			else {
				options["${it} Second${x == 1 ? '' : 's'}"] = it
			}
		}
	}

	[1,2,3,4,5,10,15,30,45].each {
		if (withinRange((it * 60), min, max)) {
			options["${it} Minute${x == 1 ? '' : 's'}"] = (it * 60)
		}
	}

	[1,2,3,6,9,12,18].each {
		if (withinRange((it * 60 * 60), min, max)) {
			options["${it} Hour${x == 1 ? '' : 's'}"] = (it * 60 * 60)
		}
	}	
	return setDefaultOption(options, defaultVal)
}

private getLuxOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : 2500
	
	[0,1,2,3,4,5,10,25,50,75,100,150,200,250,300,400,500,750,1000,1250,1500,1750,2000,2500,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000,12500,15000,17500,20000,25000,30000].each {
		if (withinRange(it, min, max)) {
			if (it == 0 && data?.zeroName != null) {
				options["${data?.zeroName}"] = it
			}
			else {
				options["${it} lux"] = it
			}
		}
	}
	return setDefaultOption(options, defaultVal)
}

private getPercentageOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : 100
		
	[0,1,2,3,4,5].each {
		if (withinRange(it, min, max)) {
			if (it == 0 && data?.zeroName != null) {
				options["${data?.zeroName}"] = it
			}
			else {
				options["${it}%"] = it
			}
		}
	}
	
	for (int i = 10; i <= 100; i += 5) {
		if (withinRange(i, min, max)) {
			options["${i}%"] = i
		}
	}
	
	return setDefaultOption(options, defaultVal)
}

private withinRange(val, min, max) {
	return ((min == null || val >= min) && (max == null || val <= max))
}

private getMinLuxOptions(defaultVal=null, zeroValName=null) {
	
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { name, val -> "${settingVal}" == name }?.value, 0)
}

private setDefaultOption(options, defaultVal) {
	def name = options.find { key, val -> val == defaultVal }?.key
	if (name != null) {
		return changeOptionName(options, defaultVal, "${name}${defaultOptionSuffix}")
	}
	else {
		return options
	}	
}

private changeOptionName(options, optionVal, newName) {
	def result = [:]	
	options?.each { name, val ->
		if (val == optionVal) {
			name = "${newName}"
		}
		result["${name}"] = val
	}
	return result
}

private findDefaultOptionName(options) {
	def option = options?.find { name, val ->
		name?.contains("${defaultOptionSuffix}") 
	}
	return option?.key ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by opening the device and pressing the connect button."
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def newVal = "${value}"	
	def isStateChange = displayed ?: (getAttrValue(name) != newVal)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed
		// ,isStateChange: isStateChange
	]
	if (desc) {
		eventMap.descriptionText = desc
	}
	if (unit) {
		eventMap.unit = unit
	}	
	// logTrace "Creating Event: ${eventMap}"
	return eventMap
}

private getAttrValue(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logTrace "$ex"
		return null
	}
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}
