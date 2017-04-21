/**
 *  Zooz Water Sensor v1.0.4
 *  (Model: ZSE30)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-water-sensor/78223?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.0.4 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.0.3 (03/12/2017)
 *      - Fixed wakeup report so that it doesn't send the configuration every time the device wakes up.
 *
 *    1.0.1 (02/18/2017)
 *      - Added health check
 *
 *    1.0 (02/16/2017)
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
		name: "Zooz Water Sensor", 
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
		attribute "lastUpdate", "string"
		
		fingerprint deviceId: "0xA102", inClusters: "0x30, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"027A", prod:"0003", model:"0085"
	}
	
	simulator { }
	
	preferences {
		input "audibleAlarmEnabled", "bool",
			title: "Audible Alarm Enabled?",
			defaultValue: audibleAlarmEnabledSetting,
			required: false,
			displayDuringSetup: true
		input "firstBeepDuration", "number",
			title: "Alarm First Beep Duration [10-255]\n(10 Seconds - 4.25 Minutes)",
			defaultValue: firstBeepDurationSetting,
			required: false,
			displayDuringSetup: true,
			range: "10..255"
		input "beepDuration", "number",
			title: "Alarm Beep Duration [5-255]\n(5 Seconds - 4.25 Minutes)",
			defaultValue: beepDurationSetting,
			required: false,
			displayDuringSetup: true,
			range: "5..255"
		input "beepInterval", "number",
			title: "Alarm Beep Interval [1-255]\n(1 Minute - 4.25 Hours)",
			defaultValue: beepIntervalSetting,
			required: false,
			displayDuringSetup: true,
			range: "1..255"
		input "totalAlarmDuration", "number",
			title: "Total Alarm Duration [0-255]\n(0 = Until Dry)\n(1 Minute - 4.25 Hours)",
			defaultValue: totalAlarmDurationSetting,
			required: false,
			displayDuringSetup: true,
			range: "0..255"
		input "wakeUpInterval", "number",
			title: "Minimum Check-in Interval [1-167]\n(1 = 1 Hour)\n(167 = 7 Days)",
			defaultValue: checkinIntervalSetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false
		input "batteryReportingInterval", "number",
			title: "Battery Reporting Interval [1-24]\n(1 Hour - 24 Hours)",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			range: "1..24"
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
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}
					
		main "water"
		details(["water", "refresh", "battery", "lastUpdate"])
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
		sendEvent(getEventMap("water", "dry", false))		
		cmds << "delay 1000"
	}
	
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.value, refreshAll)	
	}
	
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false)
	
	initializeCheckin()
	cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		
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
	def checkInterval = ((checkinIntervalSettingSeconds * 2) + (2 * 60))
	
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	if (state.pendingChanges != false) {
		cmds += configure()
	}
	else if (state.pendingRefresh || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting} hours."
	}
	
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingSeconds * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	if (val < 1) {
		val = 1
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
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
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
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
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


// Settings
private getAlarmEnabledSetting() {
	return true // Always report water when detected.
}

private getAudibleAlarmEnabledSetting() {
	return (settings?.audibleAlarmEnabled == null) ? true : settings?.audibleAlarmEnabled
}

private getFirstBeepDurationSetting() {
	return safeToInt(settings?.firstBeepDuration,	60)
}

private getBeepDurationSetting() {
	return safeToInt(settings?.beepDuration, 5)
}

private getTotalAlarmDurationSetting() {
	return safeToInt(settings?.totalAlarmDuration, 120)
}

private getBeepIntervalSetting() {
	return safeToInt(settings?.beepInterval, 1)
}

private getCheckinIntervalSetting() {
	return safeToInt(settings?.wakeUpInterval, 12)
}

private getCheckinIntervalSettingSeconds() {
	return (checkinIntervalSetting * 60 * 60)
}

private getBatteryReportingIntervalSetting() {
	return safeToInt(settings?.batteryReportingInterval, 12)
}

private getBatteryReportingIntervalSettingSeconds() {
	return (batteryReportingIntervalSetting * 60 * 60)
}

private getDebugOutputSetting() {
	return (settings?.debugOutput == null) ? true : settings?.debugOutput
}


// Configuration Parameters
private getConfigData() {
	return [
		[paramNum: 1, name: "Total Alarm Duration", value: totalAlarmDurationSetting],
		[paramNum: 2, name: "Beep Interval", value: beepIntervalSetting],
		[paramNum: 3, name: "First Beep Duration", value: firstBeepDurationSetting],
		[paramNum: 4, name: "Beep Duration", value: beepDurationSetting],
		[paramNum: 5, name: "Audible Alarm Enabled", value: audibleAlarmEnabledSetting ? 1 : 0],
		[paramNum: 6, name: "Alarm Enabled", value: alarmEnabledSetting ? 1 : 0]
	]	
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
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}