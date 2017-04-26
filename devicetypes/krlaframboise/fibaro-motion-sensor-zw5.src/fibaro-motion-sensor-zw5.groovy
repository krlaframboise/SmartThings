/**
 *  Fibaro Motion Sensor v0.0.1
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
 *    0.0.1 (04/25/2017)
 *      - Beta Release
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
		attribute "axisX", "number"
		attribute "axisY", "number"
		attribute "axisZ", "number"
		
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x98, 0x7A", outClusters: ""
		
		fingerprint mfr:"010F", prod:"0801", model:"2001"
	}

	simulator { }
	
	preferences {
		getOptionsInput(motionSensitivityParam)
		getOptionsInput(motionRetriggerParam)
		// getOptionsInput(motionModeParam)
		// getOptionsInput(motionNightThresholdParam)
		getOptionsInput(vibrationSensitivityParam)
		getOptionsInput(vibrationRetriggerParam)
		getOptionsInput(vibrationTypeParam)
						
		getBoolInput("displayVibrationEvents", "Display Vibration Events?", false)		
		
		getOptionsInput(vibrationLedParam)		
		// getOptionsInput(lightReportingThresholdParam)
		getOptionsInput(lightReportingIntervalParam)
		getOptionsInput(tempReportingThresholdParam)
		getOptionsInput(tempReportingIntervalParam)
		getOptionsInput(tempMeasuringIntervalParam)
		// getOptionsInput(tempOffsetParam)		
		getOptionsInput(ledModeParam)
		// getOptionsInput(ledBrightnessParam)
		// getOptionsInput(ledBrightnessLowThresholdParam)
		// getOptionsInput(ledBrightnessHighThresholdParam)
		// getOptionsInput(ledBlueTempThresholdParam)
		// getOptionsInput(ledRedTempThresholdParam)

		input "wakeUpInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { name, val -> name }
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { name, val -> name }
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
			state "earthquakeInactive", label:'Clear'//, icon:"", backgroundColor:"#ffffff"
			state "earthquakeActive", label:'Earthquake', icon:"st.secondary.activity"//, backgroundColor:"#53a7c0"
			state "tamperClear", label:'Clear', icon:""//, backgroundColor:"#ffffff"
			state "tamperDetected", label:'Tamper', icon:"st.security.alarm.alarm"//, backgroundColor:"#e86d13"
		}
		
		valueTile("vibrationData", "device.vibrationData", inactiveLabel: false, width: 2, height: 2) {
			state "vibrationData", label:'${currentValue}'
		}
		
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label:'${currentValue} lux'
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "motion"
		details(["motion", "temperature", "illuminance", "vibrationStatus", "vibrationData", "battery", "refresh", "lastUpdate"])
	}
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
		logTrace "Waiting 1 second because this is the first time being configured"
		cmds << "delay 1000"	
	}
	
	cmds += initializeCheckin()
				
	configParams.each { param ->	
		cmds += updateConfigVal(param, state.pendingRefresh)		
	}
	
	if (!cmds) {
		state.pendingChanges = false
		cmds += refreshSensorData()
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
		"axisZ": 0
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
	if (state.pendingRefresh || state.checkinIntervalMinutes != checkinIntervalSettingMinutes) {
		
		state.checkinIntervalMinutes = checkinIntervalSettingMinutes
		
		result << wakeUpIntervalSetCmd(checkinIntervalSettingMinutes * 60)
		
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((checkinIntervalSettingMinutes * 3 * 60) + (5 * 60))
	
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	return result	
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def refresh() {
	def vibrationType = vibrationTypeSettingName
	def vibrationAttr = getAttrValue("vibrationStatus")
	
	if ((vibrationType == "tamper" && vibrationAttr != "tamperClear") || (vibrationType == "earthquake" && vibrationAttr != "earthquakeInactive")) {
	
		sendVibrationStatusEvents(0x00, true)
		
		if (vibrationType == "earthquake") {
			sendEarthquakeEvents(0, true)
		}
	}
			
	state.pendingRefresh = true
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."		
}

def parse(String description) {
	def result = []
	// logTrace "parse: $description"
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (description.startsWith("Err 106")) {
		log.warn "Secure Inclusion Failed: ${description}"
		result << createEvent( name: "secureInclusion", value: "failed", eventType: "ALERT", descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	}
	else if (description.startsWith("Err")) {
		log.warn "Parse Error: $description"
		result << createEvent(descriptionText: "$device.displayName $description", isStateChange: true)
	}
	else {
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
	else if (cmd.commandClassIdentifier == 0x5E) {
		logTrace "Unable to parse ZwaveplusInfo cmd"
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		result << createEvent(descriptionText: "$cmd")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	// logTrace "Crc16Encap: ${cmd}"
	def result = []
	def cmdClass = getVersionSafeCmdClass(cmd.commandClass)
	def parsedCmd = cmdClass?.command(cmd.command)?.parse(cmd.data)
	if (parsedCmd) {
		
		// Ignoring these events because they're only sent when the action button is pushed.
		// result += zwaveEvent(parsedCmd)
		
	}
	else {
		logDebug "Unable to parse crc16encap command"
	}
	return result	
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (state.pendingChanges != false) {
		result += configure()
	}
	else if (state.pendingRefresh || canReportBattery()) {
		state.pendingRefresh = false
		result += refreshSensorData()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting} hours."
	}
		
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
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
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
	[
		createEvent(createEventMap("battery", val, null, null, "%"))
	]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	def val = (cmd.scaledConfigurationValue == -1 ? 255 : cmd.scaledConfigurationValue)
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		logDebug "${configParam.name}(#${configParam.num}) = ${val}"
		state["configVal${cmd.parameterNumber}"] = val		
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}
	
	if (state.pendingChanges || state.pendingRefresh) {
		sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
		state.pendingChanges = false
		state.pendingRefresh = false	
	}	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	// logTrace "SensorMultilevelReport: ${cmd}"
	switch (cmd.sensorType) {
		case tempSensorType:
			sendTempEvent(cmd)
			break
		case lightSensorType:
			logDebug "Illuminance ${cmd.scaledSensorValue} lux"
			sendEvent(createEventMap("illuminance", cmd.scaledSensorValue, null, null, "lux"))
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

private sendEarthquakeEvents(val, refreshing=false) {
	// logTrace "sendEarthquakeEvents(${val})"
	def mVal = roundTwoPlaces(safeToDec(val))
	if (mVal > 0 || refreshing) {		
		sendEvent(createEventMap("vibrationData", "Mag. ${mVal}", false))
	}
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
		}
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
		map = createEventMap("acceleration", val ? "active" : "inactive", displayVibrationEventsSetting)
	}
	else {
		map = createEventMap("tamper", val ? "detected" : "clear", displayVibrationEventsSetting)
	}
	
	def valName = "${map.value}".capitalize()
	def dataVal = "${vibrationType}${valName}"
	if ((dataVal != "tamperClear" && dataVal != "earthquakeInactive") || refreshing) {
		sendEvent(map)
		sendEvent(createEventMap("vibrationStatus", dataVal, false))
	}
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
		// motionModeParam,
		// motionNightThresholdParam,
		vibrationSensitivityParam,
		vibrationRetriggerParam,
		vibrationTypeParam,
		vibrationLedParam,
		// lightReportingThresholdParam,
		lightReportingIntervalParam,
		tempReportingThresholdParam,
		tempReportingIntervalParam,
		tempMeasuringIntervalParam,
		// tempOffsetParam,
		ledModeParam
		// ledBrightnessParam,
		// ledBrightnessLowThresholdParam,
		// ledBrightnessHighThresholdParam,
		// ledBlueTempThresholdParam,
		// ledRedTempThresholdParam,
		
	]
}

private getMotionSensitivityParam() {
	return createConfigParamMap(1, "Motion Sensitivity", 2, motionSensitivityOptions, "motionSensitivity")
}

private getMotionBlindTimeParam() {
	def val	
	switch(convertOptionSettingToInt(retriggerOptions, motionRetriggerParam.val)) {
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
	return createConfigParamMap(6, "Motion Retrigger", 2, retriggerOptions, "motionRetrigger")
}

private getMotionModeParam() {
	return createConfigParamMap(8, "Motion Mode", 1, motionModeOptions, "motionMode")
}

private getMotionNightThresholdParam() {
	return createConfigParamMap(9, "Motion Night Threshold", 2, minLuxOptions, "motionNightThreshold")
}

private getVibrationSensitivityParam() {
	return createConfigParamMap(20, "Vibration Sensitivity", 1, vibrationSensitivityOptions, "vibrationSensitivity")
}

private getVibrationRetriggerParam() {
	return createConfigParamMap(22, "Vibration Retrigger", 2, retriggerOptions, "vibrationRetrigger")
}

private getVibrationTypeParam() {
	return createConfigParamMap(24, "Vibration Type", 1, vibrationTypeOptions, "vibrationType")
}

private getVibrationLedParam() {
	return createConfigParamMap(89, "Vibration LED", 1, vibrationLedOptions, "vibrationLedParam")
}

private getLightReportingThresholdParam() {
	return createConfigParamMap(40, "Light Reporting Threshold", 2, minLuxOptions, "lightReportingThreshold")
}

private getLightReportingIntervalParam() {
	return createConfigParamMap(42, "Light Reporting Interval", 2, lightReportingIntervalOptions, "lightReportingInterval")
}

private getTempReportingThresholdParam() {
	return createConfigParamMap(60, "Temperature Reporting Threshold", 1, tempReportingThresholdOptions, "tempReportingThreshold")
}

private getTempMeasuringIntervalParam() {
	return createConfigParamMap(62, "Temperature Measuring Interval", 2, tempMeasuringIntervalOptions, "tempMeasuringInterval")
}

private getTempReportingIntervalParam() {
	return createConfigParamMap(64, "Temperature Reporting Interval", 2, tempReportingIntervalOptions, "tempReportingInterval")
}

private getTempOffsetParam() {
	return createConfigParamMap(66, "Temperature Offset", 2, tempOffsetOptions, "tempOffset")
}

private getLedModeParam() {
	return createConfigParamMap(80, "LED Mode", 1, ledModeOptions, "ledMode")
}

private getLedBrightnessParam() {
	return createConfigParamMap(81, "LED Brightness", 1, ledBrightnessOptions, "ledBrightness")
}

private getLedBrightnessLowThresholdParam() {
	return createConfigParamMap(82, "LED Brightness 1% Threshold", 2, ledBrightnessLowThresholdOptions, "ledBrightnessLowThreshold")
}

private getLedBrightnessHighThresholdParam() {
	return createConfigParamMap(83, "LED Brightness 100% Threshold", 2, maxLuxOptions, "ledBrightnessHighThreshold")
}

private getLedBlueTempThresholdParam() {
	return createConfigParamMap(86, "Blue LED Temperature Threshold", 1, minTempOptions, "ledBlueTempThreshold")
}

private getLedRedTempThresholdParam() {
	return createConfigParamMap(87, "Red LED Temperature Threshold", 1, maxTempOptions, "ledRedTempThreshold")
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

private getVibrationTypeSetting() {
	return settings?.vibrationType ?: findDefaultOptionName(vibrationTypeOptions)
}

private getDisplayVibrationEventsSetting() {
	return settings?.displayVibrationEvents ?: false
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 120
}

private getCheckinIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: checkinIntervalSettingMinutes
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}


private getVibrationLedOptions() {
	return setDefaultOption([
		"Disabled": 0,
		"Flashing White, Red, and Blue": 1
	], 1)
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

private getLedModeOptions() {
	return setDefaultOption([
		"Disabled": 0,
		"White (flashlight)": 2,
		"White": 3,
		"Red": 4,
		"Green": 5,
		"Blue": 6,
		"Yellow": 7,
		"Cyan": 8,
		"Magenta": 9
	], 5)
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

private getRetriggerOptions() {
	return getIntervalOptions(30)
}

private getLightReportingIntervalOptions() {
	return getIntervalOptions(0, "No Reports")
}

private getLightReportingThresholdOptions() {
	return getMinLuxOptions(200, "0 Lux")
}

private getTempReportingIntervalOptions() {
	return getIntervalOptions(0, "No Reports")
}

private getTempReportingThresholdOptions() {
	def options = [:]
	for (int i = 1; i <= 100; i += (i == 1 ? 4 : 5)) {
		def name = "${i.toBigDecimal() * 0.1} °C / ${(((i.toBigDecimal() * 0.1)*9)/5)} °F"
		options["${name}"] = i
	}
	return setDefaultOption(options, 10)
}

private getTempMeasuringIntervalOptions() {
	return getIntervalOptions(900, "No Reports")
}

private getIntervalOptions(defaultVal=null, zeroValName=null) {
	def options = [:]
	
	if (zeroValName) {
		options["${zeroValName}"] = 0
	}
	
	[5,10,15,30,45].each {
		options["${it} Seconds"] = it
	}

	options["1 Minute"] = 60
	[2,4,8,15,30,45].each {
		options["${it} Minutes"] = (it * 60)
	}

	options["1 Hour"] = (60 * 60)
	[2,4,6,8].each {
		options["${it} Hours"] = (it * 60 * 60)
	}
	return setDefaultOption(options, defaultVal)
}

private getMinLuxOptions(defaultVal=null, zeroValName=null) {
	
}

private getCheckinIntervalOptions() {
	return setDefaultOption([
		"10 Minutes":10,
		"15 Minutes":15,
		"30 Minutes":30,
		"1 Hour":60,
		"2 Hours":120,
		"3 Hours":180,
		"6 Hours":360,
		"9 Hours":540,
		"12 Hours":720,
		"18 Hours":1080,
		"24 Hours":1440
	], 120)
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { name, val -> "${settingVal}" == name }?.value, 0)
}

private setDefaultOption(options, defaultVal) {
	def result = [:]
	if (defaultVal != null) {
		options.each { name, val ->
			if (val == defaultVal) {
				name = "${name}${defaultOptionSuffix}"
			}
			result["${name}"] = val
		}
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
		displayed: displayed,
		isStateChange: isStateChange
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