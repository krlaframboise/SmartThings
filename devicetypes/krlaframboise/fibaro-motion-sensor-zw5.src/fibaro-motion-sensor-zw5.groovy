/**
 *  Fibaro Motion Sensor v1.0.7
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
 *    1.0.7 (05/10/2017)
 *      - Misc enhancements
 *
 *    1.0 (05/05/2017)
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
		attribute "motionStatus", "string"
		attribute "vibrationStatus", "string"
		attribute "pendingChanges", "number"
		attribute "axisX", "number"
		attribute "axisY", "number"
		attribute "axisZ", "number"
		attribute "earthquake", "number"
		
		command "refreshConfig"
		
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x98, 0x7A", outClusters: ""
		
		fingerprint mfr:"010F", prod:"0801", model:"2001"
	}

	simulator { }
	
	preferences {
		getOptionsInput(motionSensitivityParam)
		getOptionsInput(motionRetriggerParam)
		getOptionsInput(motionModeParam)
		getOptionsInput(motionNightThresholdParam)
		
		getOptionsInput(vibrationSensitivityParam)
		getOptionsInput(vibrationRetriggerParam)
		getOptionsInput(vibrationTypeParam)
						
		getBoolInput("displayVibrationEvents", "Display vibration events on Activity Feed?", false)		
		
		getOptionsInput(lightReportingThresholdParam)
		getOptionsInput(lightReportingIntervalParam)
		
		getOptionsInput(tempReportingThresholdParam)
		getOptionsInput(tempReportingIntervalParam)
		getOptionsInput(tempMeasuringIntervalParam)
		getOptionsInput(tempOffsetParam)		
		
		getOptionsInput(ledBrightnessParam)
		getOptionsInput(ledBrightnessLowThresholdParam)
		getOptionsInput(ledBrightnessHighThresholdParam)
		getOptionsInput(vibrationLedModeParam)
		getOptionsInput("motionLedMode", "Motion LED Mode", motionLedModeSetting, motionLedModeOptions)
		getOptionsInput("motionLedColor", "Motion LED Color", motionLedColorSetting, motionLedColorOptions)
		getOptionsInput(ledBlueTempThresholdParam)
		getOptionsInput(ledRedTempThresholdParam)
		
		getOptionsInput("wakeUpInterval", "Checkin Interval", checkinIntervalSetting, checkinIntervalOptions)
		
		getOptionsInput("batteryReportingInterval", "Battery Reporting Interval", batteryReportingIntervalSetting, checkinIntervalOptions)
				
		getBoolInput("debugOutput", "Enable debug logging?", true)		
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "inactive", 
					label:'No Motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#cccccc"
				attributeState "active", 
					label:'Motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.motionStatus", key: "SECONDARY_CONTROL") {
				attributeState "motionStatus", label:'${currentValue}'			
			}
		}

		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state "inactive", label:'Inactive', icon:"st.motion.acceleration.inactive", backgroundColor:"#cccccc"
			state "active", label:'Active', icon:"st.motion.acceleration.active", backgroundColor:"#00a0dc"
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
	
		valueTile("vibrationStatus", "device.vibrationStatus", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'${currentValue}'
			state "disabled", label:'Vibration Disabled'
			state "tamper", label:'Tamper', icon:"st.security.alarm.alarm", backgroundColor: "#ff0000"
		}
		
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} lux'
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
		standardTile("refresh", "device.generic", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("refreshConfig", "device.generic", width: 2, height: 2) {
			state "default", label:'Update All', action: "refreshConfig", icon:"st.secondary.preferences"
		}
		
		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'
			state "0", label: ''
			state "-1", label:'Updating Settings'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "motion"
		details(["motion", "acceleration", "illuminance", "temperature", "refresh", "vibrationStatus", "battery", "refreshConfig", "pending", "lastUpdate"])
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

		if (checkForPendingChanges()) {
			logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		}
	}	
	return []
}

def configure() {	
	logTrace "configure()"
	def cmds = []
		
	initializeVibrationAttributes()
	
	if (getAttrValue("pendingChanges") == null) {
		logTrace "Waiting 1 second because this is the first time being configured"
		cmds << "delay 1000"
		cmds += refreshSensorData()
	}
	else if (state.pendingRefresh) {
		state.pendingRefresh = false
		cmds += refreshSensorData()		
	}
	
	cmds += initializeCheckin()
				
	configParams.each { param ->	
		cmds += updateConfigVal(param)
	}
	
	if (!cmds && canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	if (cmds) {
		cmds << basicGetCmd()
	}		
	return cmds ? delayBetween(cmds, 500) : []	
}

private initializeVibrationAttributes(refreshAll=false) {
	def vibrationAttrs = [
		"acceleration": "inactive",
		"tamper": "clear",
		"vibrationStatus": "",
		"threeAxis": "0,0,0",
		"axisX": 0,
		"axisY": 0,
		"axisZ": 0,
		"earthquake": 0
	]	
	vibrationAttrs.each { name, val ->
		def attrVal = getAttrValue("${name}")
		if (attrVal == null || (attrVal != val && attrVal != "disabled" && refreshAll)) {
			sendEvent(createEventMap("${name}", val, false))
		}
	}
}

private updateConfigVal(param) {
	def result = []	
	if (hasPendingChange(param)) {
	logTrace "has pending change: ${param.name}"
		def newVal = getParamIntVal(param)
		logDebug "${param.name}(#${param.num}): changing ${getParamStoredIntVal(param)} to ${newVal}"
		result << configSetCmd(param, newVal)
		result << configGetCmd(param)
	}		
	return result
}

private checkForPendingChanges() {
	def changes = 0
	configParams.each {
		if (hasPendingChange(it)) {
			changes += 1
		}
	}
	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, false))
	}
	return (changes != 0)
}

private hasPendingChange(param) {
	return (getParamIntVal(param) != getParamStoredIntVal(param) || state.refreshAll)
}

private initializeCheckin() {
	def result = []
	def intervalSeconds = convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
	
	if (state.refreshAll || state.checkinInterval != intervalSeconds) {		
		
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
	return []
}

def refresh() {
	initializeVibrationAttributes(true)	
	state.pendingRefresh = true
	return []
}

def refreshConfig() {
	sendEvent(createEventMap("pendingChanges", configParams.size(), false))
	state.refreshAll = true
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	return []
}

def parse(String description) {
	def result = []
	// logTrace "parse: $description"
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (!description?.startsWith("Err")) {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			// logTrace "Parse: $cmd"
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
	sendResponse(result)
	return []
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
	if (getAttrValue("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, false))
	}
	
	def val = (cmd.scaledConfigurationValue == -1 ? 255 : cmd.scaledConfigurationValue)
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		def name = configParam.options?.find { it.value == val}?.key
		logDebug "${configParam.name}(#${configParam.num}) = ${name != null ? name : val} (${val})"
		state["configVal${cmd.parameterNumber}"] = val
		
		if (configParam.num == vibrationSensitivityParam.num && name == "Disabled") {
			sendEvent(createEventMap("vibrationStatus", "disabled", false))
		}
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}
	
	runIn(10, finalizeConfiguration)
	return []
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	
	state.refreshAll = false
	
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), false))	
	return []
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
			if (vibrationTypeSettingName == "threeAxis") {
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
	def position = "${x}x,${y}y,${z}z"
	
	sendEvent(createEventMap("threeAxis", "${x},${y},${z}", false))
	
	sendEvent(createVibrationStatusEventMap(position, "Position changed to ${position}"))
	return []
}

private sendLightEvents(val) {
	logDebug "Illuminance ${val} lux"
	sendEvent(createEventMap("illuminance", val, null, null, "lux"))
	
	def status = ""
	if (!motionModeParam.val?.contains("Always")) {
		status = (val > getParamIntVal(motionNightThresholdParam)) ? "Day" : "Night"
		if (!motionModeParam.val?.contains("${status}")) {
			status = "${status} (Motion Disabled)"
		}
	}
	
	if (getAttrValue("motionStatus") != "") {
		sendEvent(createEventMap("motionStatus", "${status}", false))
	}	
}

private sendEarthquakeEvents(val) {
	if (val) {
		def mVal = roundTwoPlaces(safeToDec(val))
		sendEvent(createEventMap("earthquake", mVal, false, null, "M"))
		sendEvent(createVibrationStatusEventMap("${mVal}M Earthquake Detected"))		
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {	
	logTrace "BasicReport: $cmd"
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

private sendVibrationStatusEvents(val) {
	// logTrace "sendVibrationStatusEvents(${val})"
	sendEvent(createEventMap("acceleration", val ? "active" : "inactive", false))
	
	if (vibrationTypeSettingName == "tamper" && val && getAttrValue("tamper") != "detected") {
		def map = createEventMap("tamper", val ? "detected" : "clear", false)
		sendEvent(map)
		sendEvent(createVibrationStatusEventMap("Tampering Detected"))
	}
}

private createVibrationStatusEventMap(val, desc=null) {
	return createEventMap("vibrationStatus", val, (val && displayVibrationEventsSetting), desc)
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
	return zwaveInfo?.zw?.contains("s") && zwaveInfo?.sec?.contains(Integer.toHexString(cmd.commandClassId)?.toUpperCase())
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x30: 2,	// Sensor Binary
		0x31: 5,	// Sensor Multilevel
		0x56: 1,	// Crc16 Encap
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration
		0x71: 3,  // Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association
		0x9C: 1,	// Sensor Alarm
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
private getEarthquakeSensorType() { return 25 }
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
	return createConfigParamMap(81, "LED Brightness", 1, getPercentageOptions(50, [zeroName: "Determined by Illuminance"]), "ledBrightness")
}

private getLedBrightnessLowThresholdParam() {
	return createConfigParamMap(82, "Use 1% LED Brightness When Illuminance is Below", 2, getLuxOptions(100), "ledBrightnessLowThreshold")
}

private getLedBrightnessHighThresholdParam() {
	return createConfigParamMap(83, "Use 100% LED Brightness When Illuminance is Above", 2, getLuxOptions(1000, [min:5, max:5000]), "ledBrightnessHighThreshold")
}

private getLedBlueTempThresholdParam() {
	return createConfigParamMap(86, "Use Blue LED When Temperature is Below", 2, getTempValueOptions(18, [min:0, max:30]), "ledBlueTempThreshold")
}

private getLedRedTempThresholdParam() {
	return createConfigParamMap(87, "Use Red LED When Temperature is Above", 2, getTempValueOptions(28, [min:10, max:40]), "ledRedTempThreshold")
}

private getParamStoredIntVal(param) {
	return state["configVal${param.num}"]	
}

private getParamIntVal(param) {
	return param.options ? convertOptionSettingToInt(param.options, param.val) : param.val
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
	if (vibrationTypeSetting?.startsWith("Position")) {
		return "threeAxis"
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
	def options = ["Most Sensitive": 15]
	
	def val = 11
	(2..19).each {
		val += 13
		options["${it}"] = val
	}
	options["Least Sensitive"] = 254
	return setDefaultOption(options, 24)
}

private getMotionLedModeOptions() {
	return setDefaultOption([
		"Disabled": 0,
		"Blink when motion is detected": 1,
		"Blink when detected and short blink when detected again": 2,
		"Blink when detected and 2 short blinks when detected again":3
	], 2)
}

private getMotionLedColorOptions() {
	return setDefaultOption([
		"Blue, Green, or Red Depending on Temperature": 1,
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
		"Earthquake": 1,
		"Tampering": 0,
		"Position (x,y,z)": 2,
	], 0)
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
			options["${i.toBigDecimal() * 0.1} C / ${(((i.toBigDecimal() * 0.1)*9)/5)} F"] = i
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
			options["${i} C / ${(((i*9)/5) + 32)} F"] = i
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
