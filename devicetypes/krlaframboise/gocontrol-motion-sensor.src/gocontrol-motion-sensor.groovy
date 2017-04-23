/**
 *  GoControl Motion Sensor v1.3.2
 *    (Model: WAPIRZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.3.2 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.3.1 (04/20/2017)
 *      - Added fingerprint.
 *      - Added workaround for ST Health Check bug.
 *
 *    1.3 (03/12/2017)
 *      - Added Health Check.
 *
 *    1.2.1 (07/31/2016)
 *      - Fix iOS UI bug with tamper tile.
 *      - Removed secondary tile.
 *
 *    1.1 (06/17/2016)
 *      - Fixed tamper detection
 *
 *    1.0 (06/17/2016)
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
	definition (name:"GoControl Motion Sensor", namespace:"krlaframboise", author: "Kevin LaFramboise") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Temperature Measurement"		
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		attribute "lastCheckin", "string"
 
		fingerprint mfr:"014F", prod:"2002", model:"0203"
 
		fingerprint deviceId:"0x2001", inClusters:"0x71, 0x85, 0x80, 0x72, 0x30, 0x86, 0x31, 0x70, 0x84"
	}

	preferences {		
		input "temperatureOffset", "number",
			title: "Temperature Offset:\n(Allows you to adjust the temperature being reported if it's always high or low by a specific amount.  Example: Enter -3 to make it report 3° lower or enter 3 to make it report 3° higher.)",
			range: "-100..100",
			defaultValue: tempOffsetSetting,
			displayDuringSetup: true,
			required: false
		input "temperatureThreshold", "number",
			title: "Temperature Change Threshold:\n(You can use this setting to prevent the device from bouncing back and forth between the same two temperatures.  Example:  If the device is repeatedly reporting 68° and 69°, you can change this setting to 2 and it won't report a new temperature unless 68° changes to 66° or 70°.)",
			range: "1..100",
			defaultValue: tempThresholdSetting,
			displayDuringSetup: true,
			required: false
		input "retriggerWaitTime", "number", 
			title: "Re-Trigger Wait Time (Minutes)\n(When the device detects motion, it waits for at least 1 minute of inactivity before sending the inactive event.  The default re-trigger wait time is 3 minutes.)", 
			range: "1..255", 
			defaultValue: retriggerWaitTimeSetting, 
			displayDuringSetup: true,
			required: false
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "reportBatteryEvery", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
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
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No Tamper", backgroundColor: "#cccccc"			
		}
		standardTile("refresh", "command.refresh", width: 2, height: 2) {
			state "default", label:"Reset", action: "refresh", icon:""
		}
		main("motion")
		details(["motion", "temperature", "refresh", "tampering", "battery"])
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"

		refresh()
	}
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!device.currentValue("motion")) {
		sendEvent(name: "motion", value: "active", isStateChange: true, displayed: false)
	}
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"
		// Give inclusion time to finish.
		cmds << "delay 1000"			
	}
	
	initializeCheckin()
	
	cmds += [
		wakeUpIntervalSetCmd(checkinIntervalSettingMinutes),
		retriggerWaitTimeSetCmd(),
		batteryGetCmd(),
		temperatureGetCmd()
	]
	return delayBetween(cmds, 250)
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def refresh() {	
	clearTamperDetected()
	logDebug "The re-trigger wait time will be sent to the device the next time it wakes up.  If you want this change to happen immediately, open the back cover of the device until the red light turns solid and then close it."
	state.pendingConfig = true
}

private clearTamperDetected() {	
	if (device.currentValue("tamper") != "clear") {
		logDebug "Resetting Tamper"
		sendEvent(getTamperEventMap("clear"))			
	}
}

private retriggerWaitTimeSetCmd() {
	logTrace "Setting re-trigger wait time to ${retriggerWaitTimeSetting} minutes"
	
	return zwave.configurationV1.configurationSet(scaledConfigurationValue: retriggerWaitTimeSetting, parameterNumber: 1, size: 1).format()	
}

private wakeUpIntervalSetCmd(minutesVal) {
	state.checkinIntervalMinutes = minutesVal
	logTrace "wakeUpIntervalSetCmd(${minutesVal})"
	
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:(minutesVal * 60), nodeid:zwaveHubNodeId).format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private temperatureGetCmd() {
	return zwave.sensorMultilevelV2.sensorMultilevelGet().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}


def parse(String description) {	
	def result = []
	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	def cmd = zwave.parse(description, [0x71: 2, 0x80: 1, 0x30: 1, 0x31: 2, 0x70: 1, 0x84: 1])
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unknown Description: $desc"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification"
	def result = []

	if (state.pendingConfig) {
		state.pendingConfig = false
		result += configure()
	}
	else if (canReportBattery()) {
		result << batteryGetCmd()
	}
	if (result) {
		result << "delay 2000"
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

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {	
	def motionVal = cmd.value ? "active" : "inactive"
	def desc = "Motion is $motionVal"
	logDebug "$desc"
	def result = []
	result << createEvent(name: "motion", 
			value: motionVal, 
			isStateChange: true, 
			displayed: true, 
			descriptionText: "$desc")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	
	def isNew = (device.currentValue("battery") != val)
			
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%", display: isNew, isStateChange: isNew)

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	def result = []
	if (cmd.alarmType == 7 && cmd.alarmLevel == 0xFF && cmd.zwaveAlarmEvent == 3) {
		logDebug "Tampering Detected"
		result << createEvent(getTamperEventMap("detected"))
	}	
	return result
}

def getTamperEventMap(val) {
	[
		name: "tamper", 
		value: val, 
		isStateChange: true, 
		displayed: (val == "detected"),
		descriptionText: "Tamper is $val"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def result = []
	
	if (cmd.sensorType == 1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		def newTemp = safeToInt(convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), 0)
				
		if (tempOffsetSetting != 0) {
			newTemp = (newTemp + tempOffsetSetting)
			logDebug "Adjusted temperature by ${tempOffsetSetting}°"
		}		
		
		def highTemp = (getCurrentTemp() + tempThresholdSetting)
		def lowTemp = (getCurrentTemp() - tempThresholdSetting)
		if (newTemp >= highTemp || newTemp <= lowTemp) {
			result << createEvent(
				name: "temperature",
				value: newTemp,
				unit: getTemperatureScale(),
				isStateChange: true,
				displayed: true)
		}
		else {
			logDebug "Ignoring new temperature of $newTemp° because the change is within the ${tempThresholdSetting}° threshold."
		}
	}
	return result
}

private getRetriggerWaitTimeSetting() {
	return safeToInt(settings?.retriggerWaitTime, 3)
}

private getCurrentTemp() {
	return safeToInt(device.currentValue("temperature"), 0)
}

private getTempThresholdSetting() {
	return safeToInt(settings?.temperatureThreshold, 1)
}

private getTempOffsetSetting() {
	return safeToInt(settings?.temperatureOffset, 0)
}

// Settings
private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 360
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: 720
}

private getBatteryReportingIntervalSetting() {
	return settings?.reportBatteryEvery ?: findDefaultOptionName(checkinIntervalOptions)
}

private getCheckinIntervalOptions() {
	[
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: formatDefaultOptionName("6 Hours"), value: 360],
		[name: "9 Hours", value: 540],
		[name: "12 Hours", value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
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
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}