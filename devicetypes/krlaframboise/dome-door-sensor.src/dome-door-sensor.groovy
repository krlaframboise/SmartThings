/**
 *  Dome Door Sensor v0.0.0
 *  (Model: DMWD1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  
 *    
 *
 *  Changelog:
 *
 *    0.0.0 (01/31/2017)
 *      - Test Release
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
		name: "Dome Door Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
				
		attribute "lastCheckin", "number"
		
		fingerprint deviceId: "0x0701", inClusters: "0x30, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"021F", prod:"0003", model:"0101"
		fingerprint mfr:"0258", prod:"0003", model:"0082"
	}
	
	simulator { }
	
	preferences {
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
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
			}			
		}	
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}
					
		main "contact"
		details(["contact", "refresh", "battery"])
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
		
	if (!state.isConfigured) {
		state.isConfigured = true
		state.pendingChanges = false
		state.pendingRefresh = false

		logTrace "Waiting 1 second because this is the first time being configured"			
		cmds << "delay 1000"		
		cmds << sensorBinaryGetCmd()
		cmds << batteryGetCmd()
	}
	
	cmds << wakeUpIntervalSetCmd(convertOptionSettingToInt(wakeUpIntervalOptions, wakeUpIntervalSetting) * 60 * 60)
		
	logDebug "Sending configuration to device."
	return delayBetween(cmds, 1000)
}

// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.pendingRefresh = true
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressind the connect button once."
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
	def result = []
	
	if (!state.isConfigured || state.pendingChanges) {
		state.pendingChanges = false
		result += configure()
	}	
	
	if (state.pendingRefresh || canReportBattery()) {
		state.pendingRefresh = false
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
	[
		createEvent(getEventMap("battery", val, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	def val = (cmd.sensorValue == 0xFF ? "open" : "closed")
	def result = []
	result << createEvent(getEventMap("contact", val))
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private getEventMap(name, value, unit=null) {	
	def isStateChange = (device.currentValue(name) != value)	
	def eventMap = [
		name: name,
		value: value,
		displayed: isStateChange,
		isStateChange: isStateChange,
		descriptionText: "${name} ${value}${unit ? unit : ''}"
	]
	if (unit) {
		eventMap.unit = unit
	}	
	if (isStateChange) {
		logDebug "${eventMap.descriptionText}"
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

private sensorBinaryGetCmd() {
	return zwave.sensorBinaryV2.sensorBinaryGet().format()
}

// Settings
private getWakeUpIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
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