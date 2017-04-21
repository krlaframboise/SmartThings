/**
 *  Dome Leak Sensor v1.1.2
 *  (Model: DMWS1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-dome-leak-sensor-official/76154?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.1.2 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.1.1 (03/12/2017)
 *      - Cleaned code for publication
 *      - Added health check functionality
 *
 *    1.0.1 (01/30/2017)
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
		name: "Dome Leak Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Water Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
				
		attribute "lastCheckin", "string"

		fingerprint deviceId: "0xA102", inClusters: "0x30, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"021F", prod:"0003", model:"0085"
	}
	
	simulator { }
	
	preferences {
		input "audibleAlarmEnabled", "enum",
			title: "Enable/Disable Audible Alarm:",
			defaultValue: audibleAlarmEnabledSetting,
			required: false,
			displayDuringSetup: true,
			options: audibleAlarmEnabledOptions.collect { it.name }
		input "initialAlarmDuration", "enum",
			title: "Initial Alarm Duration:",
			defaultValue: initialAlarmDurationSetting,
			required: false,
			displayDuringSetup: true,
			options: initialAlarmDurationOptions.collect { it.name }
		input "reminderAlarmDuration", "enum",
			title: "Reminder Alarm Duration:",
			defaultValue: reminderAlarmDurationSetting,
			required: false,
			displayDuringSetup: true,
			options: reminderAlarmDurationOptions.collect { it.name }
		input "reminderAlarmInterval", "enum",
			title: "Reminder Interval:",
			defaultValue: reminderAlarmIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: reminderAlarmIntervalOptions.collect { it.name }		
		input "totalAlarmDuration", "enum",
			title: "Total Alarm Duration:",
			defaultValue: totalAlarmDurationSetting,
			required: false,
			displayDuringSetup: true,
			options: totalAlarmDurationOptions.collect { it.name }
		input "wakeUpInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"				
			}			
		}	
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}
					
		main "water"
		details(["water", "refresh", "battery"])
	}
}

// Sets flag so that configuration is updated the next time it wakes up.
def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		state.pendingChanges = true
	}		
}

// Initializes the device state when paired and updates the device's configuration.
def configure() {
	logTrace "configure()"
	def cmds = []
	def refreshAll = (!state.isConfigured || state.pendingRefresh || !settings?.ledAlarm)
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		sendEvent(getEventMap("water", "dry", false))		
		cmds << "delay 1000"
	}
	
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.value, refreshAll)	
	}
	
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	initializeCheckin()
	cmds << wakeUpIntervalSetCmd(checkinIntervalSettingMinutes)
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 1000)
	}
	else {
		return cmds
	}	
}

private updateConfigVal(paramNum, val, refreshAll) {
	def result = []
	def configVal = state["configVal${paramNum}"]
	
	if (refreshAll || (configVal != val)) {
		result << configSetCmd(paramNum, val)
		result << configGetCmd(paramNum)
	}	
	return result
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

// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.pendingRefresh = true
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressing the connect button once."
}


// Processes messages received from device.
def parse(String description) {
	def result = []
	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)

	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	
	return result
}

// Updates devices configuration, requests battery report, and/or creates last checkin event.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	if (state.pendingChanges != false || state.pendingRefresh) {
		cmds += configure()
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting}."
	}
	
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	
	return response(cmds)
}

// Creates the event for the battery level.
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

// Stores the configuration values so that it only updates them when they've changed or a refresh was requested.
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	def name = configData.find { it.paramNum == cmd.parameterNumber }?.name
	if (name) {	
		def val = cmd.configurationValue[0]
	
		logDebug "${name} = ${val}"
	
		state."configVal${cmd.parameterNumber}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	state.isConfigured = true
	state.pendingRefresh = false
	state.pendingChanges = false
	return []
}

// Creates event for wet/dry report.
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 0x05) {
		switch (cmd.event) {
			case 0:
				logDebug "Sensor is Dry"				
				result << createEvent(getEventMap("water", "dry"))
				break
			case 2:
				logDebug "Sensor is Wet"
				result << createEvent(getEventMap("water", "wet"))
				break
			default:
				logDebug "Sensor is ${cmd.event}"
		}
	}
	return result
}

// Using notification cc for reporting wet/dry so ignoring this event.
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	return []
}

// Logs unexpected events from the device.
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

private wakeUpIntervalSetCmd(minutesVal) {
	state.checkinIntervalMinutes = minutesVal
	logTrace "wakeUpIntervalSetCmd(${minutesVal})"
	
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:(minutesVal * 60), nodeid:zwaveHubNodeId).format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private configGetCmd(paramNum) {
	return zwave.configurationV1.configurationGet(parameterNumber: paramNum).format()
}

private configSetCmd(paramNum, val) {
	return zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val).format()
}

private getCommandClassVersions() {
	[
		0x30: 2,	// Sensor Binary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1		// Version (2)
	]
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

// Settings
private getAlarmEnabledSetting() {
	return settings?.alarmEnabled ?: findDefaultOptionName(alarmEnabledOptions)
}

private getAudibleAlarmEnabledSetting() {
	return settings?.audibleAlarmEnabled ?: findDefaultOptionName(audibleAlarmEnabledOptions)
}

private getInitialAlarmDurationSetting() {
	return settings?.initialAlarmDuration	?: findDefaultOptionName(initialAlarmDurationOptions)
}

private getReminderAlarmDurationSetting() {
	return settings?.reminderAlarmDuration ?: findDefaultOptionName(reminderAlarmDurationOptions)
}

private getTotalAlarmDurationSetting() {
	return settings?.totalAlarmDuration ?: findDefaultOptionName(totalAlarmDurationOptions)
}

private getReminderAlarmIntervalSetting() {
	return settings?.reminderAlarmInterval ?: findDefaultOptionName(reminderAlarmIntervalOptions)
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 720
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


// Configuration Parameters
private getConfigData() {
	return [
		[paramNum: 1, name: "Total Alarm Duration", value: convertOptionSettingToInt(totalAlarmDurationOptions, totalAlarmDurationSetting)],
		[paramNum: 2, name: "Reminder Alarm Interval", value: convertOptionSettingToInt(reminderAlarmIntervalOptions, reminderAlarmIntervalSetting)],
		[paramNum: 3, name: "Initial Alarm Duration", value: convertOptionSettingToInt(initialAlarmDurationOptions, initialAlarmDurationSetting)],
		[paramNum: 4, name: "Reminder Alarm Duration", value: convertOptionSettingToInt(reminderAlarmDurationOptions, reminderAlarmDurationSetting)],
		[paramNum: 5, name: "Audible Alarm Enabled", value: (audibleAlarmEnabledSetting == "Disabled") ? 0 : 1],
		[paramNum: 6, name: "Alarm Enabled", value: (alarmEnabledSetting == "Disabled") ? 0 : 1]
	]	
}

private getReminderAlarmDurationOptions() {
	return getSecondOptions(5, 5)	
}
private getInitialAlarmDurationOptions() {
	return getSecondOptions(60, 10)	
}
private getSecondOptions(defaultVal, minVal=1) {
	[
		[name: "1 Second", value: 1],
		[name: "3 Seconds", value: 3],
		[name: "5 Seconds", value: 5],
		[name: "10 Seconds", value: 10],
		[name: "15 Seconds", value: 15],
		[name: "30 Seconds", value: 30],
		[name: "45 Seconds", value: 45],
		[name: "1 Minute", value: 60],
		[name: "2 Minutes", value: 120],
		[name: "3 Minutes", value: 180],
		[name: "4 Minutes", value: 240]
	].findAll { it.value >= minVal }?.each {
		if (it.value == defaultVal) {
			it.name = formatDefaultOptionName("${it.name}")
		}
	}
}

private getReminderAlarmIntervalOptions() {
	return getMinuteOptions(1)
}
private getTotalAlarmDurationOptions() {
	def options = []
	options << [name: "Until water is removed", value: 0]
	options += getMinuteOptions(120)
	return options
}
private getMinuteOptions(defaultVal) {
	[
		[name: "1 Minute", value: 1],
		[name: "3 Minutes", value: 3],
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "45 Minutes", value: 45],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "4 Hours", value: 240]
	].each {
		if (it.value == defaultVal) {
			it.name = formatDefaultOptionName("${it.name}")
		}
	}
}

private getAlarmEnabledOptions() {
	return getEnabledOptions(1)
}
private getAudibleAlarmEnabledOptions() {
	return getEnabledOptions(1)
}
private getEnabledOptions(defaultVal) {
	[
		[name: "Disabled", value: 0],
		[name: "Enabled", value: 1]
	].each {
		if (it.value == defaultVal) {
			it.name = formatDefaultOptionName("${it.name}")
		}
	}
}

private getCheckinIntervalOptions() {
	[
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: formatDefaultOptionName("12 Hours"), value: 720],
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