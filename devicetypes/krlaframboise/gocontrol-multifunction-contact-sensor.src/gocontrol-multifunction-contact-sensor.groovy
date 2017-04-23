/**
 *  GoControl Multifunction Contact Sensor v1.1.8
 *  (WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-gocontrol-linear-multifunction-contact-sensor/77659?u=krlaframboise
 *    
 *  Changelog:
 *
 *    1.1.8 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.1.7 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.1.6 (03/18/2017)
 *      -  Forced correct icon for contact-garage when primary or secondary status is first set to it.
 *
 *    1.1.5 (03/17/2017)
 *      -  Added smoke capability and garage door icons. 
 *
 *    1.1.4 (03/12/2017)
 *      -  Adjusted health check to allow it to skip a checkin before going offline. 
 *
 *    1.1.2 (02/26/2017)
 *      -  Removed descriptionText from events so that it uses the default full device name.
 *
 *    1.1.1 (02/21/2017)
 *      -  Added Health Check
 *
 *    1.0 (02/11/2017)
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
		name: "GoControl Multifunction Contact Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Water Sensor"
		capability "Motion Sensor"
		capability "Smoke Detector"
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"
		
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "internalContact", "enum", ["open", "closed"]
		attribute "externalContact", "enum", ["open", "closed"]
		attribute "lastCheckin", "string"

		fingerprint deviceId: "0x2001", inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"		
		fingerprint mfr:"014F", prod:"2001", model:"0102"
	}

	simulator {	}
	
	preferences {
		input "primaryStatusAttr", "enum",
			title: "Primary Status Attribute:",
			defaultValue: primaryStatusAttrSetting,
			required: false,
			options: primaryStatusOptions
		input "secondaryStatusAttr", "enum",
			title: "Secondary Status Attribute:",
			defaultValue: secondaryStatusAttrSetting,
			required: false,
			options: secondaryStatusOptions
		input "motionActiveEvent", "enum",
			title: "Motion Active Event:",
			defaultValue: motionActiveEventSetting,
			required: false,
			options: eventOptions
		input "motionInactiveEvent", "enum",
			title: "Motion Inactive Event:",
			defaultValue: motionInactiveEventSetting,
			required: false,
			options: eventOptions
		input "waterWetEvent", "enum",
			title: "Water Wet Event:",
			defaultValue: waterWetEventSetting,
			required: false,
			options: eventOptions
		input "waterDryEvent", "enum",
			title: "Water Dry Event:",
			defaultValue: waterDryEventSetting,
			required: false,
			options: eventOptions
		input "smokeDetectedEvent", "enum",
			title: "Smoke Detected Event:",
			defaultValue: smokeDetectedEventSetting,
			required: false,
			options: eventOptions
		input "smokeClearEvent", "enum",
			title: "Smoke Clear Event:",
			defaultValue: smokeClearEventSetting,
			required: false,
			options: eventOptions
		input "mainContactBehavior", "enum",
			title: "Main Contact Behavior:",
			defaultValue: mainContactBehaviorSetting,
			required: false,
			options: ["Last Changed Contact", "Internal Contact Only", "External Contact Only", "Both Contacts Closed", "Both Contacts Open"]
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
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}
	
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'Closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'Open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
				attributeState "garage-closed", 
					label:'Closed', 
					icon:"st.doors.garage.garage-closed", 
					backgroundColor:"#79b821"
				attributeState "garage-open", 
					label:'Open', 
					icon:"st.doors.garage.garage-open", 
					backgroundColor:"#ffa81e"				
				attributeState "inactive", 
					label:'No Motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'Motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
				attributeState "dry", 
					label:"Dry", 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:"Wet", 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
				attributeState "clear", 
					label:'Clear', 
					icon:"st.alarm.smoke.clear", 
					backgroundColor:"#ffffff"
				attributeState "detected", 
					label:'Detected', 
					icon:"st.alarm.smoke.smoke", 
					backgroundColor:"#53a7c0"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "", 
					label:''
				attributeState "closed", 
					label:'CLOSED', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'OPEN', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
				attributeState "garage-closed", 
					label:'CLOSED', 
					icon:"st.doors.garage.garage-closed", 
					backgroundColor:"#79b821"
				attributeState "garage-open", 
					label:'OPEN', 
					icon:"st.doors.garage.garage-open", 
					backgroundColor:"#ffa81e"				
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'MOTION', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
				attributeState "dry", 
					label:"DRY", 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:"WET", 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
				attributeState "clear", 
					label:'CLEAR', 
					icon:"st.alarm.smoke.clear", 
					backgroundColor:"#ffffff"
				attributeState "detected", 
					label:'DETECTED', 
					icon:"st.alarm.smoke.smoke", 
					backgroundColor:"#53a7c0"
			}
		}	
				
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No Tamper", backgroundColor: "#cccccc"			
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		main("mainTile")
		details(["mainTile", "battery", "tampering", "refresh"])
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time
		
		refreshMainTile()
	}
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	def mainTileChanged = false
	if (!device.currentValue("contact")) {
		sendEvent(name: "contact", value: "open", isStateChange: true, displayed: false)
		mainTileChanged = true
	}
	
	if (!device.currentValue("motion")) {
		def motionVal = settings?.motionActiveEvent == "default" ? "active" : "inactive"
		sendEvent(name: "motion", value: "$motionVal", isStateChange: true, displayed: false)
		mainTileChanged = true
	}
	
	if (!device.currentValue("water")) {
		def waterVal = settings?.waterWetEvent == "default" ? "wet" : "dry"
		sendEvent(name: "water", value: "$waterVal", isStateChange: true, displayed: false)
		mainTileChanged = true
	}
	
	if (!device.currentValue("smoke")) {
		def smokeVal = settings?.smokeDetectedEvent == "default" ? "detected" : "clear"
		sendEvent(name: "smoke", value: "$smokeVal", isStateChange: true, displayed: false)
		mainTileChanged = true
	}
	
	if (mainTileChanged) {
		refreshMainTile()
	}
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"
		// Give inclusion time to finish.
		cmds << "delay 1000"			
	}

	initializeCheckin()	
	
	cmds << wakeUpIntervalSetCmd(checkinIntervalSettingMinutes)	
	cmds << batteryGetCmd()
	cmds << basicGetCmd()
	return delayBetween(cmds, 250)
}

private refreshMainTile() {
	logTrace "refreshMainTile()"
	createContactEventMaps("internalContact", device.currentValue("internalContact"))?.each {
		sendEvent(it)
	}
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 3 * 60) + (5 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

// Resets the tamper attribute to clear and requests the device to be refreshed.
def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(createEventMap("tamper", "clear"))		
	}
	else {
		logDebug "The battery will be refresh the next time the device wakes up.  If you want the battery to update immediately, open the back cover of the device, wait until the red light turns solid, and then put the cover back on."
		state.lastBatteryReport = null
	}
}

def parse(String description) {
	def result = []
	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (description.startsWith("Err")) {
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
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x30: 2,  // Sensor Binary
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	if (!state.isConfigured) {
		cmds += configure()
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
		cmds << "delay 2000"
	}
	
	if (cmds) {
		cmds << "delay 1000"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return sendResponse(cmds)
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


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"	
	def result = []
	
	if (device.currentValue("internalContact")) {
		result += handleContactEvent("internalContact", cmd.value)
	}
	if (device.currentValue("externalContact")) {
		result += handleContactEvent("externalContact", cmd.value)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "Basic Set: $cmd"	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0x02:
				result += handleContactEvent(cmd.v1AlarmLevel, "internalContact")				
				break
			case 0x03:
				result += handleTamperEvent(cmd.v1AlarmLevel)
				break
			case 0xFE:
				result += handleContactEvent(cmd.v1AlarmLevel, "externalContact")
				break
			default:
				logDebug "Unexpected NotificationReport: $cmd"
		}
	}
	return result
}

private handleTamperEvent(alarmLevel) {
	def result = []		
	
	if (alarmLevel == 0xFF) {
		state.lastBatteryReport = null
		logDebug "Tamper Detected"
		result << createEvent(createEventMap("tamper", "detected"))
	}	
	
	return result
}

private handleContactEvent(alarmLevel, attr) {
	def result = []
	def val = (alarmLevel == 0xFF) ? "open" : "closed"
	
	createContactEventMaps(attr, val)?.each {
		result << createEvent(it)
	}	
	
	return result
}

private createContactEventMaps(attr, val) {
	def result = []
	
	def internalActive = (attr == "internalContact")
	def otherVal = device.currentValue(internalActive ? "externalContact" : "internalContact")
	
	def displayed = null
	if (mainContactBehaviorSetting.contains("Only")) {
		displayed = false
	}

	result << createEventMap("$attr", val, displayed)
	
	def mainVal = getMainContactVal(attr, val, otherVal)	
	if (mainVal) {
		result << createEventMap("contact", mainVal)
	}
	
	// Create water, motion, and status events based off these values instead of the device attributes due to race condition.
	def eventData = [
		activeAttr: attr,
		contactVal: mainVal,
		internalVal: (internalActive ? val : otherVal ),
		externalVal: (internalActive ? otherVal : val )
	]
	result += createOtherEventMaps(eventData)
	
	return result
}

private getMainContactVal(activeAttr, activeVal, otherVal) {
	def mainVal
	switch (mainContactBehaviorSetting) {
		case "Last Changed Contact":
			mainVal = activeVal
			break
		case "Internal Contact Only":
			if (activeAttr == "internalContact") {
				mainVal = activeVal
			}
			break
		case "External Contact Only":
			if (activeAttr == "externalContact") {
				mainVal = activeVal
			}
			break
		case "Both Contacts Closed":
			mainVal = (activeVal == "closed" && otherVal == "closed") ? "closed" : "open"
			break
		case "Both Contacts Open":
			mainVal = (activeVal == "open" && otherVal == "open") ? "open" : "closed"
			break
		default:
			mainVal = activeVal
	}	
	return mainVal
}

private createOtherEventMaps(eventData) {
	def result = []
	eventData.motionVal = determineMotionVal(eventData)
	if (eventData.motionVal) {
		result += createEventMap("motion", eventData.motionVal)
	}
	
	eventData.waterVal = determineWaterVal(eventData)
	if (eventData.waterVal) {
		result << createEventMap("water", eventData.waterVal)
	}
	
	eventData.smokeVal = determineSmokeVal(eventData)
	if (eventData.smokeVal) {
		result << createEventMap("smoke", eventData.smokeVal)
	}
	
	result += createMainTileEventMaps(eventData)
	
	return result
}

private determineMotionVal(eventData) {
	if (eventSettingMatchesEventVal(motionActiveEventSetting, eventData)) {
		return "active"
	}
	else if (eventSettingMatchesEventVal(motionInactiveEventSetting, eventData)) {
		return "inactive"
	}
	else {
		return null
	}	
}

private determineWaterVal(eventData) {
	if (eventSettingMatchesEventVal(waterWetEventSetting, eventData)) {
		return "wet"
	}
	else if (eventSettingMatchesEventVal(waterDryEventSetting, eventData)) {
		return "dry"
	}
	else {
		return null
	}
}

private determineSmokeVal(eventData) {
	if (eventSettingMatchesEventVal(smokeDetectedEventSetting, eventData)) {
		return "detected"
	}
	else if (eventSettingMatchesEventVal(smokeClearEventSetting, eventData)) {
		return "clear"
	}
	else {
		return null
	}
}

private eventSettingMatchesEventVal(eventSetting, eventData) {
	if (eventSetting == "default") {
		return true
	}
	else {
		def eventVal
		switch (eventSetting) {
			case { it?.startsWith("contact") }:
				eventVal = eventData.contactVal
				break
			case { it?.startsWith("internal") }:
				eventVal = eventData.internalVal
				break
			case { it?.startsWith("external") }:
				eventVal = eventData.externalVal
				break
			default:
				eventVal = null
		}
		return (eventVal && eventSetting.endsWith(".${eventVal}"))
	}
}

private createMainTileEventMaps(eventData) {
	def result = []
	
	def data = [
		["none", ""],
		["motion", eventData.motionVal],
		["water", eventData.waterVal],
		["smoke", eventData.smokeVal],
		["contact", eventData.contactVal],
		["contact-garage", "garage-${eventData.contactVal}"],
		["internalContact", eventData.internalVal],
		["externalContact", eventData.externalVal]
	]
	
	[["primaryStatus", primaryStatusAttrSetting], ["secondaryStatus", secondaryStatusAttrSetting]].each { status, statusAttr ->
		def eventVal = data.find { attr, val -> "$statusAttr" == "$attr" }		
		if (eventVal && eventVal[1] != null) {
			result << createEventMap("${status}", eventVal[1], false)
		}
	}
	
	return result
}

private createEventMap(eventName, newVal, displayed=null) {	
	def isNew = device.currentValue(eventName) != newVal
	def desc = "${eventName.capitalize()} is ${newVal}"
	logDebug "${desc}"
	[
		name: eventName, 
		value: newVal, 
		displayed: (displayed != null) ? displayed : isNew
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}
private basicGetCmd() {
	return zwave.basicV1.basicGet().format()
}

private batteryGetCmd() {
	logTrace "Requesting battery report"
	return zwave.batteryV1.batteryGet().format()
}

private wakeUpIntervalSetCmd(minutesVal) {
	state.checkinIntervalMinutes = minutesVal
	logTrace "wakeUpIntervalSetCmd(${minutesVal})"
	
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:(minutesVal * 60), nodeid:zwaveHubNodeId).format()
}


// Settings
private getPrimaryStatusAttrSetting() {
	return settings?.primaryStatusAttr ?: "contact"
}

private getSecondaryStatusAttrSetting() {	
	return settings?.secondaryStatusAttr ?: "none"
}

private getMotionActiveEventSetting() {
	return settings?.motionActiveEvent ?: "none"
}

private getMotionInactiveEventSetting() {
	return settings?.motionInactiveEvent ?: "default"
}

private getWaterWetEventSetting() {
	return settings?.waterWetEvent ?: "none"
}

private getWaterDryEventSetting() {
	return settings?.waterDryEvent ?: "default"
}

private getSmokeDetectedEventSetting() {
	return settings?.smokeDetectedEvent ?: "none"
}

private getSmokeClearEventSetting() {
	return settings?.smokeClearEvent ?: "default"
}

private getMainContactBehaviorSetting() {
	return settings?.mainContactBehavior ?: "Last Changed Contact"
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 360
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: 360
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
}


// Options 
private getEventOptions() {
	return [
		"default",
		"none", 
		"contact.open",
		"contact.closed",
		"externalContact.open", 
		"externalContact.closed", 
		"internalContact.open", 
		"internalContact.closed"
	]
}

private getPrimaryStatusOptions() {
	return [
		"contact",
		"contact-garage",
		"externalContact",
		"internalContact", 
		"motion",
		"smoke", 
		"water"
	]
}

private getSecondaryStatusOptions() {
	return [
		"none",
		"contact",
		"contact-garage",
		"externalContact",
		"internalContact", 
		"motion",
		"smoke",
		"water"
	]
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

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}