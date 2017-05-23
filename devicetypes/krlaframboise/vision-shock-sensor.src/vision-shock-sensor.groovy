/**
 *  Vision Shock Sensor v1.1
 *  (ZS 5101)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-vision-shock-sensor-zs-5101/81628?u=krlaframboise
 *    
 *  Changelog:
 *
 *    1.1 (05/23/2017)
 *    	- Added support for the Monoprice Shock Sensor
 *
 *    1.0.2 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.0.1 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.0 (03/13/2017)
 *      -  Initial Release
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
		name: "Vision Shock Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Acceleration Sensor"
		capability "Contact Sensor"
		capability "Motion Sensor"
		capability "Water Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"
				
		attribute "lastCheckin", "string"
		attribute "lastActivity", "string"
		attribute "lastUpdate", "string"
		attribute "primaryStatus", "enum", ["aActive", "aInactive", "mActive", "mInactive"]
		attribute "secondaryStatus", "enum", ["open", "closed", "active", "inactive", "wet", "dry", "detected", "clear", ""]
		
		fingerprint mfr:"0109", prod:"2003", deviceJoinName:"Vision Shock Sensor"
		
		fingerprint deviceId: "0x2001", inClusters: "0x30, 0x71, 0x72, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"0109", prod:"2003", model:"0307", deviceJoinName:"Monoprice Shock Sensor"
		
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x80, 0x72, 0x86, 0x22, 0x85, 0x59, 0x5A, 0x7A, 0x71, 0x73, 0x84"		
	}
	
	simulator { }
	
	preferences {	
		input "primaryCapability", "enum",
			title: "Primary Capability:",
			defaultValue: primaryCapabilitySetting,
			required: false,
			displayDuringSetup: true,
			options: primaryCapabilityOptions.collect { it.name }
		input "secondaryCapability", "enum",
			title: "Secondary Capability (Tamper/External Sensor):",
			defaultValue: secondaryCapabilitySetting,
			required: false,
			displayDuringSetup: true,
			options: secondaryCapabilityOptions.collect { it.name }
		input "checkinInterval", "enum",
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
		multiAttributeTile(name:"primaryTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "aInactive", 
					label:'INACTIVE', 
					icon:"st.motion.acceleration.inactive", 
					backgroundColor:"#ffffff"
				attributeState "aActive", 
					label:'ACTIVE', 
					icon:"st.motion.acceleration.active", 
					backgroundColor:"#53a7c0"				
				attributeState "mInactive", 
					label:'NO MOTION', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "mActive", 
					label:'MOTION', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "", label:''
				attributeState "closed", label:'CLOSED'
				attributeState "open", label:'OPEN'
				attributeState "inactive", label:'NO MOTION'
				attributeState "active", label:'MOTION'
				attributeState "dry", label:'DRY'
				attributeState "wet", label:'WET'
				attributeState "detected", label:'TAMPER DETECTED'
				attributeState "clear", label:'TAMPER CLEAR'				
			}
		}	
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% Battery', unit:""
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("lastActivity", "device.lastActivity", decoration: "flat", width: 2, height: 2){
			state "lastActivity", label:'Last\nActivity\n\n${currentValue}'
		}
					
		main "primaryTile"
		details(["primaryTile", "battery", "lastActivity", "refresh"])
	}
}

// Sets flag so that configuration is updated the next time it wakes up.
def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		state.pendingChanges = true
		refresh()
	}
	return []
}

// Initializes the device state when paired and updates the device's configuration.
def configure() {
	logTrace "configure()"
	def cmds = []
	
	if (!state.isConfigured) {
		sendEvent(createLastActivityEventMap())
		state.isConfigured = true		
		logTrace "Waiting 1 second because this is the first time being configured"		
		cmds << "delay 1000"
	}
	
	initializeCheckin()
	
	cmds << wakeUpIntervalSetCmd(checkinIntervalSettingMinutes)
		
	return cmds
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	logTrace "initializeCheckin() checkInterval=${checkInterval}"
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def refresh() {
	logTrace "refresh()"
	createPrimaryEventMaps(false)?.each {
		sendEvent(it)
	}
	createSecondaryEventMaps(false)?.each {
		sendEvent(it)
	}
	resetAttribute("contact", "closed")
	resetAttribute("acceleration", "inactive")
	resetAttribute("motion", "inactive")
	resetAttribute("water", "dry")
	resetAttribute("tamper", "clear")
	return []
}

private resetAttribute(attr, val) {
	if (attr != primaryCapabilitySettingAttribute && attr != secondaryCapabilitySettingAttribute && device.currentValue(attr) != val) {
		sendEvent(name: attr, value: val, displayed: false)
	}
}

// Processes messages received from device.
def parse(String description) {
	def result = []

	if (!isDuplicateCommand(state.lastCheckin, 30000)) {
		state.lastCheckin = new Date().time
		sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
	
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
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
		
	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
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
		0x22: 1,  // Application Status (Model 0308)
		0x30: 2,	// Sensor Binary
		0x59: 1,  // AssociationGrpInfo (Model 0308)
		0x5A: 1,  // DeviceResetLocally (Model 0308)
		0x5E: 2,  // ZwaveplusInfo (Model 0308)
		0x7A: 2,  // Firmware Update MD (Model 0308)
		0x71: 3,  // Alarm v1 or Notification (v4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel (Model 0308)
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,  // Version (v2)
		0x98: 1   // Security (Model 0308)
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

// Updates devices configuration, if needed, and creates the event with the last lastcheckin event.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (!state.isConfigured || state.pendingChanges) {
		state.pendingChanges = false
		cmds += configure()
	}
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
		
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	
	return response(cmds)
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

// Creates the event for the battery level.
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val <= 0) {
		val = 1
	}
	state.lastBatteryReport = new Date().time		
	[
		createEvent(createEventMap("battery", val, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// logTrace "Basic Set: $cmd"	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"	
	return []
}

// Logs unexpected events from the device.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

// Contact event is being created using Sensor Binarry command class so this event is ignored.
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	// logTrace "NotificationReport: $cmd"
		
	def result = []
	if (cmd.notificationType == 7) {
		if (cmd.event == 2 || cmd.v1AlarmType == 2) {
			createPrimaryEventMaps(cmd.v1AlarmLevel == 0XFF)?. each {
				result << createEvent(it)
			}
		}
		else if (cmd.event == 3) {
			createSecondaryEventMaps(true)?.each {
				result << createEvent(it)
			}
		}
		result << createEvent(createLastActivityEventMap())
	}	
	return result
}

private createLastActivityEventMap() {
	return [name: "lastActivity", value: convertToLocalTimeString(new Date()), displayed: false]
}

private createPrimaryEventMaps(isActive) {	
	def statusVal
	if (primaryCapabilitySettingAttribute == "motion") {
		statusVal = isActive ? "mActive" : "mInactive"
	}
	else {
		statusVal = isActive ? "aActive" : "aInactive"
	}
	def result = []
	result << createEventMap(primaryCapabilitySettingAttribute, isActive ? "active" : "inactive")
	result << [name: "primaryStatus", value: statusVal, displayed: false]
	return result
}

private createSecondaryEventMaps(isActive) {
	def val
	switch (secondaryCapabilitySettingAttribute) {
		case "motion":
			val = isActive ? "active" : "inactive"
			break
		case "contact":
			val = isActive ? "open" : "closed"
			break
		case "water":
			val = isActive ? "wet" : "dry"
			break
		case "tamper":
			val = isActive ? "detected" : "clear"
			break
		default:
			val = ""
	}
	
	def result = []
	if (val) {
		result << createEventMap(secondaryCapabilitySettingAttribute, val)
	}
	result << [name: "secondaryStatus", value: val, displayed: false]
	return result
}

private createEventMap(name, value, unit=null) {	
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
	// logTrace "Creating Event: ${eventMap}"
	return eventMap
}

private wakeUpIntervalSetCmd(minutesVal) {
	state.checkinIntervalMinutes = minutesVal
	logTrace "wakeUpIntervalSetCmd(${minutesVal})"
	
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:(minutesVal * 60), nodeid:zwaveHubNodeId))
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private secureCmd(cmd) {
	logTrace "canSecureCmd(${cmd}) = ${canSecureCmd(cmd)}"
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

// Settings
private getPrimaryCapabilitySettingAttribute() {
	return primaryCapabilityOptions?.find { primaryCapabilitySetting == it.name }?.value ?: "acceleration"
}

private getPrimaryCapabilitySetting() {
	return settings?.primaryCapability ?: findDefaultOptionName(primaryCapabilityOptions)
}

private getSecondaryCapabilitySettingAttribute() {
	return secondaryCapabilityOptions?.find { secondaryCapabilitySetting == it.name }?.value
}

private getSecondaryCapabilitySetting() {
	return settings?.secondaryCapability ?: findDefaultOptionName(secondaryCapabilityOptions)
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 720
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: checkinIntervalSettingMinutes
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
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

private getPrimaryCapabilityOptions() {
	[
		[name: formatDefaultOptionName("Acceleration Sensor"), value: "acceleration"],
		[name: "Motion Sensor", value: "motion"]
	]
}

private getSecondaryCapabilityOptions() {
	[
		[name: "Contact Sensor", value: "contact"],
		[name: "Motion Sensor", value: "motion"],
		[name: "None", value: ""],
		[name: formatDefaultOptionName("Tamper Alert"), value: "tamper"],
		[name: "Water Sensor", value: "water"]
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