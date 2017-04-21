/**
 *  Wireless Smoke Detector Sensor v1.0.1
 *  (Model: ZWN-SD)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0.1 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.0 (03/11/2017)
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
		name: "Wireless Smoke Detector Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Smoke Detector"
		capability "Battery"
		capability "Health Check"
			
		attribute "lastCheckin", "string"
		attribute "lastTested", "string"
		
		fingerprint deviceId: "0xA107", inClusters: "0x72, 0x80, 0x84, 0x85, 0x86, 0x9C"
		
		fingerprint mfr:"011A", prod:"0601", model:"0902"		
	}
	
	simulator { }
	
	preferences {
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: batteryReportingIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"smoke", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
				attributeState "clear", 
					label:'clear', 
					icon:"st.alarm.smoke.clear", 
					backgroundColor:"#79b821"
				attributeState "detected", 
					label:'smoke', 
					icon:"st.alarm.smoke.smoke", 
					backgroundColor:"#e86d13"
			}
		}	
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
		valueTile("lastCheckin", "device.lastCheckin", decoration: "flat", width: 2, height: 2){
			state "lastCheckin", label:'Last Checkin\n\n${currentValue}', unit:""
		}
		
		valueTile("lastTested", "device.lastTested", decoration: "flat", width: 2, height: 2){
			state "lastTested", label:'Last Tested\n\n${currentValue}', unit:""
		}
					
		main "smoke"
		details(["smoke", "battery", "lastCheckin", "lastTested"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		initializeCheckin()
	}	
}

private initializeCheckin() {
	// The device wakes up every 4 hours by default.  Changing the wakeup interval sometimes causes the device to stop responding so I've removed that functionality.  Sets expected interval to 8 hours and 2 minutes which allows it to get skipped once.
	def checkInterval = (4 * 2 * 60 * 60) + (60 * 2) 
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
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
		0x20: 1,  // Basic
		0x72: 2,  // ManufacturerSpecific
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x9C: 1		// Sensor Alarm
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
		cmds << "delay 1000"
	}	
	cmds << wakeUpNoMoreInfoCmd()
	
	return response(cmds)
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
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
	
	def isNew = (device.currentValue("battery") != val)
			
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%", display: isNew, isStateChange: isNew)

	return result
}	

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "BasicSet: $cmd"	
	def result = []
	result += createSmokeEvents(cmd.value)
	return result
}

// Based on documentation this is how the device should report smoke detected, but based on all my testing, it only uses BasicSet to report smoke.  Leaving this code in place in case a future firmware or different model supports it.
def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	logTrace "SensorAlarmReport: $cmd"	
	def result = []
	result += createSmokeEvents(cmd.sensorState)
	return result
}

private createSmokeEvents(val) {
	def smokeVal = (val == 0xFF ? "detected" : "clear")
	def isNew = (device.currentValue("smoke") != smokeVal)
	def result = []
	result << createEvent(name: "smoke", value: smokeVal, displayed: isNew, isStateChange: isNew)
	result << createEvent(name: "lastTested", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	logTrace "Requesting battery report"
	return zwave.batteryV1.batteryGet().format()
}

// Settings
private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(batteryReportingIntervalOptions, batteryReportingIntervalSetting) ?: 240
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(batteryReportingIntervalOptions)
}

private getBatteryReportingIntervalOptions() {
	[		
		[name: formatDefaultOptionName("4 Hours"), value: 240],
		[name: "8 Hours", value: 480],
		[name: "12 Hours", value: 720],
		[name: "16 Hours", value: 960],
		[name: "20 Hours", value: 1200],
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