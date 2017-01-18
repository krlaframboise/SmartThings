/**
 *  Zooz/Monoprice 4-in-1 Multisensor 1.1.2
 *
 *  Zooz Z-Wave 4-in-1 Sensor (ZSE40)
 *
 *  Monoprice Z-Wave Plus 4-in-1 Motion Sensor with Temperature, Humidity, and Light Sensors (P/N 15902)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  
 *    
 *
 *  Changelog:
 *
 *    1.1.2 (01/17/2017)
 *      - Removed device join names from fingerprint.
 *      = Added association line to config.
 *
 *    1.1.0 (01/15/2017)
 *      - Added firmware attribute
 *			- Added new fingerprint for latest model of the Zooz 4-in-1 sensor.
 *			- Added new LED Indicator Mode for updated Zooz model.
 *			- Added temperature scale setting
 *			- Replaced enum settings with numbers because defaultValue doesn't work with enums on the Android Mobile App.
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
		name: "Zooz/Monoprice 4-in-1 Multisensor", 
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
		attribute "firmwareVersion", "string"
						
		fingerprint deviceId: "0x0701", inClusters: "0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x80, 0x71, 0x31, 0x70, 0x84, 0x7A"
		
		fingerprint mfr:"027A", prod:"2021", model:"2101"
		
		fingerprint mfr:"0109", prod:"2021", model:"2101"
	}
	
	simulator { }
	
	preferences {
		input "tempScale", "number",
			title: "Temperature Scale [0-1]${getNameValueSettingDesc(tempUnits)}",
			displayDuringSetup: true,
			required:false,
			defaultValue: tempScaleSetting,
			range: "0..1"
		input "tempTrigger", "number",
			title: "Temperature Change Trigger [1-50]\n(1 = 0.1°)\n(50 = 5.0°)",			
			displayDuringSetup: true,
			required:false,
			defaultValue: tempTriggerSetting,
			range: "1..50"
		input "humidityTrigger", "number",
			title: "Humidity Change Trigger [1-50]\n(1% - 50%)",
			displayDuringSetup: true,
			required:false,
			defaultValue: humidityTriggerSetting,
			range: "1..50"			
		input "lightTrigger", "number",
			title: "Light Change Trigger [5-50]\n(5% - 50%)",
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
			title: "Motion Sensitivity [1-7]\n(1 = Most Sensitive)\n(7 = Least Sensitive)",
			displayDuringSetup: true,
			required:false,
			defaultValue: motionSensitivitySetting,
			range: "1..7"
		input "ledIndicatorMode", "number",
			title: "LED Indicator Mode [1-4]${getNameValueSettingDesc(ledIndicatorModes)}",
			displayDuringSetup: true,
			required: false,
			defaultValue: ledIndicatorModeSetting,
			range: "1..4"
		input "checkinInterval", "number",
			title: "Minimum Check-in Interval [1-167]\n(1 = 1 Hour)\n(167 = 7 Days)",
			defaultValue: checkinIntervalSetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false
		input "reportBatteryEvery", "number", 
			title: "Battery Reporting Interval [1-167]\n(1 = 1 Hour)\n(167 = 7 Days)", 
			description: "This setting can't be less than the Minimum Check-in Interval.",
			defaultValue: reportBatteryEverySetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false		
		input "autoClearTamper", "bool", 
			title: "Automatically Clear Tamper?\n(The tamper detected event is raised when the device is opened.  This setting allows you to decide whether or not to have the clear event automatically raised when the device closes.)",
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
			state "illuminance", label:'${currentValue}% light', unit: ""
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
		
		if (settings?.ledIndicatorModeSetting == 4) {
			logWarn "LED Indicator Mode #4 is only available in the Zooz device with firmware v16.9 and above."
		}
				
		// This attribute is created by one of the other DTHs for this product and it's been reported that it still appears in the main tile of this DTH so this should prevent that.
		if (device.currentValue("sensorlevels")) {
			sendEvent(getEventMap("sensorLevels", ""))
		}
		
		if (!device.currentValue("tamper")) {
			sendEvent(getEventMap("tamper", "clear"))
		}

		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		state.pendingChanges = true
	}	
}

def configure() {	
	logTrace "configure()"
	
	def cmds = []		
	if (!device.currentValue("firmwareVersion")) {
		// Give inclusion time to finish.
		logTrace "Waiting 1 second because this is the first time being configured"		
		cmds << associationSetCmd()
		cmds << versionGetCmd()

	}
	
	if (state.pendingChanges) {
		cmds += delayBetween([
			wakeUpIntervalSetCmd(checkinIntervalSetting * 60 * 60),
			configSetCmd(tempScaleParamNum, tempScaleSetting),
			configSetCmd(tempTriggerParamNum, tempTriggerSetting),
			configSetCmd(humidityTriggerParamNum, humidityTriggerSetting),
			configSetCmd(lightTriggerParamNum, lightTriggerSetting),
			configSetCmd(motionTimeParamNum, motionTimeSetting),
			configSetCmd(motionSensitivityParamNum, motionSensitivitySetting),
			configSetCmd(ledIndicatorModeParamNum, ledIndicatorModeSetting),
			configGetCmd(tempScaleParamNum),
			configGetCmd(tempTriggerParamNum),
			configGetCmd(humidityTriggerParamNum),
			configGetCmd(lightTriggerParamNum),
			configGetCmd(motionTimeParamNum),
			configGetCmd(motionSensitivityParamNum),
			configGetCmd(ledIndicatorModeParamNum)
		], 50)
	}
	else {
		cmds += refreshSensorData()
	}
	return response(cmds)
}

private refreshSensorData() {
	logDebug "Refreshing Sensor Data"
	return delayBetween([
		batteryGetCmd(),
		basicGetCmd(),
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(humiditySensorType),
		sensorMultilevelGetCmd(lightSensorType)
	], 50)
}

// Settings
private getTempScaleSetting() {
	return safeToInt(settings?.tempScale, (isNewZoozDevice() ? 1 : 0))
}
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
private getLedIndicatorModeSetting() {
	return safeToInt(settings?.ledIndicatorMode, 3)
}
private getCheckinIntervalSetting() {
	return (safeToInt(settings?.checkinInterval, (isNewZoozDevice() ? 12 : 6)))
}
private getReportBatteryEverySetting() {
	return (safeToInt(settings?.reportBatteryEvery, checkinIntervalSetting))
}
private getAutoClearTamperSetting() {
	return (settings?.autoClearTamper ?: false)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}

private getNameValueSettingDesc(nameValueMap) {
	def desc = ""
	nameValueMap?.sort { it.value }.each { 
		desc = "${desc}\n(${it.value} - ${it.name})"
	}
	return desc
}

private getTempUnits() {
	return [
		[name: "Celsius", unit: "C", value: 0],
		[name: "Fahrenheit", unit: "F", value: 1]
	]
}

private getLedIndicatorModes() {
	return [
		[name: "Temperature Off / Motion Off", value: 1],
		[name: "Temperature Pulse / Motion Flash", value: 2],
		[name: "Temperature Flash / Motion Flash", value: 3],
		[name: "Temperature Off / Motion Flash", value: 4]
	]
}

private isNewZoozDevice() {
	return (settings?.firmwareVersion != 265)
}

// Sensor Types
private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }
private getLightSensorType() { return 3 }

// Configuration Parameters
private getTempScaleParamNum() { return 1 }
private getTempTriggerParamNum() { return 2 }
private getHumidityTriggerParamNum() { return 3 }
private getLightTriggerParamNum() { return 4 }
private getMotionTimeParamNum() { return 5 }
private getMotionSensitivityParamNum() { return 6 }
private getLedIndicatorModeParamNum() { return 7 }

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
	def lastCheckin = device?.currentValue("lastCheckin")
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
	
	if (state.pendingChanges != false) {
		result += configure()
	}
	else if (state.pendingRefresh) {
		result += refreshSensorData()
	}
	else if (canReportBattery()) {
		result << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${reportBatteryEverySetting} hours."
	}
	
	if (result) {
		result << "delay 1000"
	}
	result << wakeUpNoMoreInfoCmd()
	
	return response(result)
}

private canReportBattery() {
	def reportEveryMS = (reportBatteryEverySetting * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	state.lastBatteryReport = new Date().time	
	[
		createEvent(getEventMap("battery", val, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "ManufacturerSpecificReport: ${cmd}"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionCommandClassReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	logDebug "Firmware Version: ${version}"
	
	return [createEvent(name: "firmwareVersion", value: "${version}", displayed: false)] 
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: $cmd"
	def configVal = cmd.configurationValue[0]
	def paramVal = configVal
	def paramName
	switch (cmd.parameterNumber) {
		case tempScaleParamNum:
			paramName = "Temperature Unit"
			paramVal = tempUnits.find { it.value == configVal }?.name
			break
		case tempTriggerParamNum:
			paramName = "Temperature Change Trigger"
			paramVal = "${configVal / 10}°"
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
		case ledIndicatorModeParamNum:
			paramName = "LED Indicator Mode"
			paramVal = ledIndicatorModes.find { it.value == configVal }?.name
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
			def unit = tempUnits.find { it.value == cmd.scale }?.unit
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)			
			eventMap = getEventMap("temperature", temp, getTemperatureScale())
			break
		
		case humiditySensorType:
			eventMap = getEventMap("humidity", cmd.scaledSensorValue, "%")
			break
		
		case lightSensorType:
			eventMap = getEventMap("illuminance", cmd.scaledSensorValue, "%")
			break
	}
	
	if (eventMap) {
		result << createEvent(eventMap)
	}
	
	state.lastRefreshed = new Date().time
	state.pendingRefresh = false
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
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
		state.pendingRefresh = true
	}
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by using a paper clip to push the button on the bottom of the device."
}

private getEventMap(eventName, newVal, unit="") {	
	def isNew = device?.currentValue(eventName) != newVal
	def desc = "${eventName.capitalize()} is ${newVal}${unit}"
	logDebug "${desc}"
	[
		name: eventName, 
		value: newVal, 
		displayed: isNew,
		descriptionText: desc,
		unit: unit
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
	return secureCmd(zwave.batteryV1.batteryGet())
}

private manufacturerSpecificGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private associationSetCmd() {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 2, sensorType: sensorType))
}

private configSetCmds(paramNumber, val) {	
	logTrace "Setting config param #${paramNumber} to ${val}"
	return [
		secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [val])),
		configGetCmd(paramNumber)
	]
}

private configSetCmd(paramNumber, val) {	
	logTrace "Setting config param #${paramNumber} to ${val}"
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [val]))	
}

private configGetCmd(paramNumber) {
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