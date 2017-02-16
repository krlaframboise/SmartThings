/**
 *  Dome Motion Sensor v1.0
 *  (Model: DMMS1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0 (02/15/2017)
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
		name: "Dome Motion Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		
		attribute "lastCheckin", "number"
		
		fingerprint deviceId: "0x0701", inClusters: "0x30, 0x31, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"021F", prod:"0003", model:"0083"
	}
	
	simulator { }
	
	preferences {
		input "ledEnabled", "enum",
			title: "Enable/Disable LED:",
			defaultValue: ledEnabledSetting,
			required: false,
			displayDuringSetup: true,
			options: ledEnabledOptions.collect { it.name }
		input "motionClearedDelay", "enum",
			title: "Motion Cleared Delay:",
			defaultValue: motionClearedDelaySetting,
			required: false,
			displayDuringSetup: true,
			options: motionClearedDelayOptions.collect { it.name }
		input "motionSensitivity", "enum",
			title: "Motion Detection Sensitivity:",
			defaultValue: motionSensitivitySetting,
			required: false,
			displayDuringSetup: true,
			options: motionSensitivityOptions.collect { it.name }
		input "lightReporting", "enum",
			title: "Light Reporting Interval:",
			defaultValue: lightReportingSetting,
			required: false,
			displayDuringSetup: true,
			options: lightReportingOptions.collect { it.name }
		input "lightSensitivity", "enum",
			title: "Light Reporting Sensitivity:",
			defaultValue: lightSensitivitySetting,
			required: false,
			displayDuringSetup: true,
			options: lightSensitivityOptions.collect { it.name }
		input "wakeUpInterval", "enum",
			title: "Wake Up Interval:",
			defaultValue: wakeUpIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: wakeUpIntervalOptions.collect { it.name }
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: wakeUpIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: wakeUpIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "inactive", 
					label:'No Motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'Motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
			}			
			tileAttribute ("device.illuminance", key: "SECONDARY_CONTROL") {
				attributeState "illuminance", 
					label:'Light is ${currentValue} lux', 
					backgroundColor:"#ffffff"
			}
		}	
		
		valueTile("illuminance", "device.illuminance", decoration: "flat", width: 2, height: 2){
			state "illuminance", label:'Light\n${currentValue} lx', unit: ""
		}	
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}
					
		main "motion"
		details(["motion", "illuminance", "refresh", "battery"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		state.pendingChanges = true
	}		
}

def configure() {
	logTrace "configure()"
	def cmds = []
	def refreshAll = (!state.isConfigured || state.pendingRefresh || !settings?.ledAlarm)
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		sendEvent(getEventMap("motion", "inactive", false))
		sendEvent(getEventMap("illuminance", 0, false, null, "lx"))
		cmds << "delay 1000"
	}
	
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.size, it.value, refreshAll)	
	}
	
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	cmds << wakeUpIntervalSetCmd(convertOptionSettingToInt(wakeUpIntervalOptions, wakeUpIntervalSetting) * 60 * 60)
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 1000)
	}
	else {
		return cmds
	}	
}

private updateConfigVal(paramNum, paramSize, val, refreshAll) {
	def result = []
	def configVal = state["configVal${paramNum}"]
	
	if (refreshAll || (configVal != val)) {
		result << configSetCmd(paramNum, paramSize, val)
		result << configGetCmd(paramNum)
	}	
	return result
}


// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.pendingRefresh = true
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressing the connect button once."
}

def parse(String description) {
	def result = []

	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	
	if (canCheckin()) {
		result << createEvent(name: "lastCheckin",value: new Date().time, isStateChange: true, displayed: false)
	}
	
	return result
}

private getCommandClassVersions() {
	[
		0x30: 2,	// Sensor Binary
		0x31: 5,	// Sensor Multilevel
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1		// Version (2)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (state.pendingChanges != false) {
		result += configure()
	}
	else if (state.pendingRefresh || canReportBattery()) {
		result << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting}."
	}
	
	if (result) {
		result << "delay 2000"
	}
	result << wakeUpNoMoreInfoCmd()
	
	return response(result)
}

private canReportBattery() {
	def reportEveryMS = (convertOptionSettingToInt(wakeUpIntervalOptions, batteryReportingIntervalSetting) * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	[
		createEvent(getEventMap("battery", val, null, null, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	def name = configData.find { it.paramNum == cmd.parameterNumber }?.name
	if (name) {	
		def val = hexToInt(cmd.configurationValue, cmd.size)
	
		logDebug "${name} = ${val}"
	
		state."configVal${cmd.parameterNumber}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	state.isConfigured = true
	state.pendingRefresh = false	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	// logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 0x07) {
		switch (cmd.event) {
			case 0x00:
				logDebug "Motion Inactive"				
				result << createEvent(getEventMap("motion", "inactive"))
				break
			case 0x08:
				logDebug "Motion Active"
				result << createEvent(getEventMap("motion", "active"))
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def result = []	
	logTrace "SensorMultilevelReport $cmd"
	if (cmd.sensorType == 3) {
		result << createEvent(getEventMap("illuminance", cmd.scaledSensorValue, null, null, "lx"))
	}
	
	state.lastRefreshed = new Date().time
	state.pendingRefresh = false
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	// logTrace "SensorBinaryReport: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private getEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def isStateChange = (device.currentValue(name) != value)
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
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}

private wakeUpIntervalSetCmd(val) {
	logTrace "wakeUpIntervalSetCmd(${val})"
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 2, sensorType: sensorType))
}

private configGetCmd(paramNum) {
	return zwave.configurationV1.configurationGet(parameterNumber: paramNum).format()
}

private configSetCmd(paramNum, size, val) {
	return zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: size, scaledConfigurationValue: val).format()
}


// Settings
private getLedEnabledSetting() {
	return settings?.ledEnabled ?: findDefaultOptionName(ledEnabledOptions)
}

private getMotionSensitivitySetting() {
	return settings?.motionSensitivity	?: findDefaultOptionName(motionSensitivityOptions)
}

private getLightSensitivitySetting() {
	return settings?.lightSensitivity ?: findDefaultOptionName(lightSensitivityOptions)
}

private getMotionClearedDelaySetting() {
	return settings?.motionClearedDelay ?: findDefaultOptionName(motionClearedDelayOptions)
}

private getLightReportingSetting() {
	return settings?.lightReporting ?: findDefaultOptionName(lightReportingOptions)
}

private getWakeUpIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}


// Configuration Parameters
private getConfigData() {
	return [
		[paramNum: 1, name: "Motion Sensitivity", value: convertOptionSettingToInt(motionSensitivityOptions, motionSensitivitySetting), size: 1],
		[paramNum: 2, name: "Motion Cleared Delay", value: convertOptionSettingToInt(motionClearedDelayOptions, motionClearedDelaySetting), size: 2],
		[paramNum: 7, name: "Light Reporting Interval", value: convertOptionSettingToInt(lightReportingOptions, lightReportingSetting), size: 2],
		[paramNum: 9, name: "Light Sensitivity", value: convertOptionSettingToInt(lightSensitivityOptions, lightSensitivitySetting), size: 1],
		[paramNum: 10, name: "LED Enabled", value: convertOptionSettingToInt(ledEnabledOptions, ledEnabledSetting), size: 1],
	]	
}

private getMotionSensitivityOptions() {
	return getSensitivityOptions(18, 8, 127, 5)	
}
private getLightSensitivityOptions() {
	return getSensitivityOptions(49, 1, 100, 4)	
}
private getSensitivityOptions(defaultVal, minVal, maxVal, interval) {	
	def options = []

	options << [name: "1 (Most Sensitive)", value: minVal]

	(2..24).each {
		minVal += interval
		options << [name: "${it}", value: minVal]
	}

	options << [name: "25 (Least Sensitive)", value: maxVal]
	
	options.each {
		if (it.value == defaultVal) {
			it.name = formatDefaultOptionName("${it.name}")
		}
	}
	return options
}

private getLightReportingOptions() {
	[
		[name: "1 Minute", value: 60],
		[name: "2 Minutes", value: 120],
		[name: formatDefaultOptionName("3 Minutes"), value: 180],
		[name: "4 Minutes", value: 240],
		[name: "5 Minutes", value: 300],
		[name: "10 Minutes", value: 600],
		[name: "30 Minutes", value: 1800],
		[name: "1 Hour", value: 3600],
		[name: "2 Hours", value: 7200],
		[name: "4 Hours", value: 1440],
		[name: "8 Hours", value: 28800]
	]
}

private getMotionClearedDelayOptions() {
	[
		[name: "10 Seconds", value: 10],		
		[name: "15 Seconds", value: 15],
		[name: formatDefaultOptionName("30 Seconds"), value: 30],
		[name: "45 Seconds", value: 45],
		[name: "1 Minute", value: 60],
		[name: "2 Minutes", value: 120],
		[name: "3 Minutes", value: 180],
		[name: "4 Minutes", value: 240],
		[name: "5 Minutes", value: 300],
		[name: "7 Minutes", value: 420],
		[name: "10 Minutes", value: 600]
	]
}

private getLedEnabledOptions() {
	[
		[name: "Disabled", value: 0],
		[name: formatDefaultOptionName("Enabled"), value: 1]
	]
}

private getWakeUpIntervalOptions() {
	[
		[name: "2 Hours", value: 2],
		[name: "4 Hours", value: 4],
		[name: "6 Hours", value: 6],
		[name: "8 Hours", value: 8],
		[name: formatDefaultOptionName("12 Hours"), value: 12],
		[name: "18 Hours", value: 18],
		[name: "24 Hours", value: 24]
	]
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private formatDefaultOptionName(val) {
	return "${val}${defaultOptionSuffix}"
}

private findDefaultOptionName(options) {
	def option = options?.find { it.name?.contains("${defaultOptionSuffix}") }
	return option?.name ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=-1) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private hexToInt(hex, size) {
	if (size == 2) {
		return hex[1] + (hex[0] * 0x100)
	}
	else {
		return hex[0]
	}
}

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}