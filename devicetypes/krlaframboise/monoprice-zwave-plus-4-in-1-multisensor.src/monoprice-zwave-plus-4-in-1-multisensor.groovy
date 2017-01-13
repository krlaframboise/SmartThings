/**
 *  Monoprice Z-Wave Plus 4-in-1 Multisensor 1.0.2
 *
 *  Monoprice Z-Wave Plus 4-in-1 Motion Sensor with Temperature, Humidity, and Light Sensors (P/N 15902)
 *
 *  Zooz Z-Wave 4-in-1 Sensor (ZSE40)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  
 *    
 *
 *  Changelog:
 *
 *    1.0.2 (01/12/2017)
 *      - Fixed light tile text and changed minimum light threshold to 5 because that's the lowest value supported by the latest firmware version of the zooz 4-in-1 sensor.
 *
 *    1.0.1 (01/12/2017)
 *      - Changed illuminance from lux to %.
 *
 *    1.0 (01/08/2017)
 *      - Initial Release
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
		name: "Monoprice Z-Wave Plus 4-in-1 Multisensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"		
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"

		attribute "lastCheckin", "number"
			
		fingerprint deviceId: "0x0701", inClusters: "0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x80, 0x71, 0x31, 0x70, 0x84, 0x7A"
		fingerprint type:"0701", cc: "86,72,5A,85,59,73,80,71,31,70,84,7A"
		fingerprint mfr:"0109", prod:"2021", model:"2101"
	}
	
	simulator { }
	
	preferences {
		// input "tempUnit", "enum",
			// title: "Temperature Unit",
			// displayDuringSetup: true,
			// required:true,
			// options: tempUnits.collect { it.name }?.sort()
		input "tempTrigger", "number",
			title: "Temperature Change Trigger [1-50]\nC Examples:(0.1°C=1, 1°C=10, 5°C=50)\nF Examples(0°F=1, 1°F=6, 2°F=11, 3°F=17, 4°F=22, 9°F=50)",
			displayDuringSetup: true,
			required:false,
			defaultValue: tempTriggerSetting,
			range: "1..50"
		input "humidityTrigger", "number",
			title: "Humidity Change Trigger [1-50]%",
			displayDuringSetup: true,
			required:false,
			defaultValue: humidityTriggerSetting,
			range: "1..50"			
		input "lightTrigger", "number",
			title: "Light Change Trigger [1-50]%",
			displayDuringSetup: true,
			required:false,
			defaultValue: lightTriggerSetting,
			range: "5..50"
		input "motionTime", "number",
			title: "Motion Retrigger Time (Minutes)",
			displayDuringSetup: true,
			required:false,
			defaultValue: motionTimeSetting,
			range: "1..255"
		input "motionSensitivity", "number",
			title: "Motion Sensitivity [1-7] (Most Sensitive = 1)",
			displayDuringSetup: true,
			required:false,
			defaultValue: motionSensitivitySetting,
			range: "1..7"
		input "ledMode", "enum",
			title: "LED Mode",
			displayDuringSetup: true,
			required: true,
			options: ledModes.collect { it.name }?.sort()
		input "checkinInterval", "number",
			title: "Minimum Check-in Interval (Hours)",
			defaultValue: checkinIntervalSetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false
		input "reportBatteryEvery", "number", 
			title: "Battery Reporting Interval (Hours)", 
			description: "This setting can't be less than the Minimum Check-in Interval.",
			defaultValue: reportBatteryEverySetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false		
		input "autoClearTamper", "bool", 
			title: "Automatically Clear Tamper?",
			description: "The tamper detected event is raised when the device is opened.  This setting allows you to decide whether or not to have the clear event automatically raised when the device closes.",
			defaultValue: false,
			displayDuringSetup: true, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "inactive", 
					label:'no motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
			}
		}
		
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			])
		}
		
		valueTile("humidity", "device.humidity", decoration: "flat", width: 2, height: 2){
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
		
		valueTile("illuminance", "device.illuminance", decoration: "flat", width: 2, height: 2){
			state "illuminance", label:'${currentValue}% light', unit:""
		}		
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No Tamper", backgroundColor: "#cccccc"			
		}
	
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:""
		}
		
		main("motion")
		details(["motion", "temperature", "humidity", "illuminance", "battery", "tampering", "refresh"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {				
		state.lastUpdated = new Date().time
		
		logTrace "updated()"
		
		state.pendingChanges = true
	}	
}


def configure() {	
	logTrace "configure()"
	def cmds = []
		
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		// Give inclusion time to finish.
		cmds << "delay 1000"
		state.pendingChanges = true
		state.pendingRefresh = true		
	}

	if (state.pendingChanges) {	
		cmds << wakeUpIntervalSetCmd(checkinIntervalSetting * 60 * 60)
		
		//cmds += configSetCmds(tempUnitParamNum, tempUnitSetting)
		cmds += configSetCmds(tempTriggerParamNum, tempTriggerSetting)
		cmds += configSetCmds(humidityTriggerParamNum, humidityTriggerSetting)
		cmds += configSetCmds(lightTriggerParamNum, lightTriggerSetting)
		cmds += configSetCmds(motionTimeParamNum, motionTimeSetting)
		cmds += configSetCmds(motionSensitivityParamNum, motionSensitivitySetting)
		cmds += configSetCmds(ledModeParamNum, ledModeSetting)
	}

	if (state.pendingRefresh) {
		cmds += [
			batteryGetCmd(),
			basicGetCmd(),
			sensorMultilevelGetCmd(tempSensorType),
			sensorMultilevelGetCmd(humiditySensorType),
			sensorMultilevelGetCmd(lightSensorType)
		]
	}
			
	logDebug "Sending configuration to device."
	return delayBetween(cmds, 100)
}

// Settings
// private getTempUnitSetting() {
	// def unit = tempUnits.find { it.name == settings?.tempUnit }?.value
	// return safeToInt(unit, 0x00)
// }
private getTempTriggerSetting() {
	return safeToInt(settings?.tempTrigger, 10)
}
private getHumidityTriggerSetting() {
	return safeToInt(settings?.humidityTrigger, 10)
}
private getLightTriggerSetting() {
	return safeToInt(settings?.lightTrigger, 10)
}
private getMotionTimeSetting() {
	return safeToInt(settings?.motionTime, 3)
}
private getMotionSensitivitySetting() {
	return safeToInt(settings?.motionSensitivity, 4)
}
private getLedModeSetting() {
	def ledMode = ledModes.find { it.name == settings?.ledMode }?.value
	return safeToInt(ledMode, 3)
}
private getCheckinIntervalSetting() {
	return (safeToInt(settings?.checkinInterval, 6))
}
private getReportBatteryEverySetting() {
	return (safeToInt(settings?.reportBatteryEvery, 6))
}
private getAutoClearTamperSetting() {
	return (settings?.autoClearTamper ?: false)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}

// private getTempUnits() {
	// return [
		// [name: "Celsius", value: 0x00],
		// [name: "Fahrenheit", value: 0xFF]		
	// ]
// }

private getLedModes() {
	return [
		[name: "Off", value: 1],
		[name: "Temperature Pulse / Motion Flash", value: 2],
		[name: "Temperature Flash / Motion Flash", value: 3]
	]
}

// Sensor Types
private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }
private getLightSensorType() { return 3 }

// Configuration Parameters
//private getTempUnitParamNum() { return 1 }
private getTempTriggerParamNum() { return 2 }
private getHumidityTriggerParamNum() { return 3 }
private getLightTriggerParamNum() { return 4 }
private getMotionTimeParamNum() { return 5 }
private getMotionSensitivityParamNum() { return 6 }
private getLedModeParamNum() { return 7 }

		
def parse(String description) {
	def result = []
	
	if (description.startsWith("Err 106")) {
		state.useSecureCmds = false
		log.warn "Secure Inclusion Failed: ${description}"
		result << createEvent( name: "secureInclusion", value: "failed", eventType: "ALERT", descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	}
	else if (description.startsWith("Err")) {
		log.warn "Parse Error: $description"
		result << createEvent(descriptionText: "$device.displayName $description", isStateChange: true)
	}
	else {
		def cmd = zwave.parse(description, getCommandClassVersions())
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}
	
	if (canCheckin()) {
		result << createEvent(name: "lastCheckin",value: new Date().time, isStateChange: true, displayed: false)
	}
	
	return result
}

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(getCommandClassVersions())
		
	def result = []
	if (encapCmd) {
		state.useSecureCmds = true
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

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x31: 5,	// Sensor Multilevel (v7)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (canSendConfiguration()) {
		result += configure()
		result << "delay 5000"
	}
	else if (canReportBattery()) {
		result << batteryGetCmd()
		result << "delay 2000"
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last $reportEveryHours hours."
	}

	if (result) {
		result << "delay 5000"
	}
	
	result << wakeUpNoMoreInfoCmd()
	
	return response(result)
}

private canSendConfiguration() {
	return (!state.isConfigured || state.pendingRefresh != false	|| state.pendingChanges != false)
}

private canReportBattery() {
	def reportEveryMS = (reportBatteryEverySetting * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def map = [ 
		name: "battery", 		
		unit: "%"
	]
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.isStateChange = true
	}
	else {	
		def isNew = (device.currentValue("battery") != cmd.batteryLevel)
		map.value = cmd.batteryLevel
		map.displayed = isNew
		map.isStateChange = isNew
		logDebug "Battery is ${cmd.batteryLevel}%"
	}	
	
	state.lastBatteryReport = new Date().time	
	[
		createEvent(map)
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: $cmd"
	def configVal = cmd.configurationValue[0]
	def paramVal = configVal
	def paramName
	switch (cmd.parameterNumber) {
		// case tempUnitParamNum:
			// paramName = "Temperature Unit"
			// paramVal = tempUnits.find { it.value == configVal }?.name
			// break
		case tempTriggerParamNum:
			paramName = "Temperature Change Trigger"
			paramVal = "${configVal / 10}° C"
			break
		case humidityTriggerParamNum:
			paramName = "Humidity Change Trigger"
			paramVal = "${configVal}%"
			break
		case lightTriggerParamNum:
			paramName = "Light Change Trigger"
			paramVal = "${configVal}%"
			break
		case motionTimeParamNum:
			paramName = "Motion Retrigger Time"
			paramVal = "${configVal} Minutes"
			break
		case motionSensitivityParamNum:
			paramName = "Motion Sensitivity"
			paramVal = configVal
			break
		case ledModeParamNum:
			paramName = "LED Mode"
			paramVal = ledModes.find { it.value == configVal }?.name
			break
		default:	
			paramName = "Parameter #${cmd.parameterNumber}"
	}		
	if (paramName) {
		if (paramVal == null) {
			paramVal = configVal
		}
		logDebug "${paramName}: ${paramVal}"
	} 
	state.isConfigured = true
	state.pendingRefresh = false
	state.pendingChanges = false
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"	
	return handleMotionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "Basic Set: $cmd"	
	return handleMotionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	if (cmd.notificationType == 7) {
		if (cmd.eventParameter[0] == 3 || cmd.event == 3) {		
			result += handleTamperEvent(cmd.v1AlarmLevel)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def result = []	
	def eventMap
	
	switch (cmd.sensorType) {
		case tempSensorType:
			def cmdScale = (cmd.scale == 0 ? "C" : "F")
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			eventMap = getEventMap("temperature", temp)
			eventMap.unit = getTemperatureScale()
			break
		
		case humiditySensorType:
			eventMap = getEventMap("humidity", cmd.scaledSensorValue)
			eventMap.unit = "%"
			break
		
		case lightSensorType:
			eventMap = getEventMap("illuminance", cmd.scaledSensorValue)
			eventMap.unit = "%"
			break
	}
	
	if (eventMap) {
		result << createEvent(eventMap)
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private handleMotionEvent(val) {
	return [
		createEvent(getEventMap("motion", (val == 0xFF ? "active" : "inactive")))
	]	
}

private handleTamperEvent(val) {
	def result = []
	def tamperVal
	if (val == 0xFF) {
		tamperVal = "detected"
	}
	else if (val == 0) {
		if (autoClearTamperSetting) {
			tamperVal = "clear"
		}
		else {
			logDebug "Tamper is Clear"
		}
	}
	if (tamperVal) {
		result << createEvent(getEventMap("tamper", tamperVal))
	}
	return result
}

// Resets the tamper attribute to clear and requests the device to be refreshed.
def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getEventMap("tamper", "clear"))		
	}
	else {
		logDebug "The configuration and attributes will be refresh the next time the device wakes up.  If you want this to happen immediately, use a paperclip to push the button on the bottom of the device."
		state.pendingRefresh = true
	}
}

def getEventMap(eventName, newVal) {	
	def isNew = device.currentValue(eventName) != newVal
	def desc = "${eventName.capitalize()} is ${newVal}"
	logDebug "${desc}"
	[
		name: eventName, 
		value: newVal, 
		displayed: isNew,
		descriptionText: desc
	]
}

private wakeUpIntervalSetCmd(val) {
	logTrace "wakeUpIntervalSetCmd(${val})"
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId))
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	logTrace "Requesting battery report"
	return secureCmd(zwave.batteryV1.batteryGet())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 2, sensorType: sensorType))
}

private configSetCmds(paramNumber, val) {	
	logTrace "Setting configuration param #${paramNumber} to ${val}"
	return [
		secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [val])),
		configGetCmd(paramNumber)
	]
}

private configGetCmd(paramNumber) {
	logTrace "Requesting configuration report for param #${paramNumber}"
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private secureCmd(cmd) {
	if (state.useSecureCmds == false) {
		return cmd.format()
	}
	else {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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
	//log.trace "$msg"
}