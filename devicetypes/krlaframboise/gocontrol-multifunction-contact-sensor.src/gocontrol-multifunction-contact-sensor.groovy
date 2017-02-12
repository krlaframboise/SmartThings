/**
 *  GoControl Multifunction Contact Sensor v1.0
 *  (WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    
 *  Changelog:
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
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		
		attribute "primaryStatus", "enum", ["open", "closed", "wet", "dry", "active", "inactive"]
		attribute "secondaryStatus", "enum", ["", "open", "closed", "wet", "dry", "active", "inactive"]
		attribute "internalContact", "enum", ["open", "closed"]
		attribute "externalContact", "enum", ["open", "closed"]
		attribute "lastCheckin", "number"

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
		input "mainContactBehavior", "enum",
			title: "Main Contact Behavior:",
			defaultValue: mainContactBehaviorSetting,
			required: false,
			options: ["Last Changed Contact", "Internal Contact Only", "External Contact Only", "Both Contacts Closed", "Both Contacts Open"]
		input "reportBatteryEvery", "number", 
			title: "Report Battery Every? (Hours)", 
			defaultValue: 4,
			range: "4..167",
			displayDuringSetup: true, 
			required: false
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
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
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
					label:"dry", 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:"wet", 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "", 
					label:''
				attributeState "closed", 
					label:'Closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'Open', 
					icon:"st.contact.contact.open", 
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
			state "default", label: "Refresh", action: "refresh", icon:""
		}
		
		main("mainTile")
		details(["mainTile", "battery", "tampering", "refresh"])
	}
}

private refreshMainTile() {
	logTrace "refreshMainTile()"
	sendStatusEvent("primaryStatus", primaryStatusAttrSetting)
	sendStatusEvent("secondaryStatus", secondaryStatusAttrSetting)
}

private updated() {
	refreshMainTile()
}

private sendStatusEvent(statusName, statusAttribute) {
	def val = (statusAttribute == "none") ? "" : device.currentValue("${statusAttribute}")
	
	sendEvent(getEventMap(statusName, val))
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
	
	if (mainTileChanged) {
		refreshMainTile()
	}
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"
		// Give inclusion time to finish.
		cmds << "delay 1000"			
	}
		
	cmds << batteryGetCmd()
	cmds << basicGetCmd()
	return delayBetween(cmds, 250)
}

// Resets the tamper attribute to clear and requests the device to be refreshed.
def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getEventMap("tamper", "clear"))		
	}
	else {
		logDebug "The battery will be refresh the next time the device wakes up.  If you want the battery to update immediately, open the back cover of the device, wait until the red light turns solid, and then put the cover back on."
		state.lastBatteryReport = null
	}
}

def parse(String description) {		
	def result = []
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
	
	if (canCheckin()) {
		result << createEvent(name: "lastCheckin",value: new Date().time, isStateChange: true, displayed: false)
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

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (!state.isConfigured) {
		result += configure()
	}
	else if (canReportBattery()) {
		result << batteryGetCmd()
		result << "delay 2000"
	}
	else {
		logDebug "Skipping battery check because it was already checked within the last ${settings?.reportBatteryEvery} hours."
	}
	
	if (result) {
		result << "delay 1000"
	}
	
	result << wakeUpNoMoreInfoCmd()
	
	return response(delayBetween(result, 250))
}

private canReportBattery() {
	def reportEveryHours = settings?.reportBatteryEvery ?: 6
	def reportEveryMS = (reportEveryHours * 60 * 60 * 1000)
		
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
	state.isConfigured = true
	state.lastBatteryReport = new Date().time	
	[
		createEvent(map)
	]
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
		}
	}
	return result
}

private handleTamperEvent(alarmLevel) {
	def result = []		
	
	if (alarmLevel == 0xFF) {
		state.lastBatteryReport = null
		logDebug "Tamper Detected"
		result << createEvent(getEventMap("tamper", "detected"))
	}	
	
	return result
}

private handleContactEvent(alarmLevel, attr) {
	def result = []
	def val = (alarmLevel == 0xFF) ? "open" : "closed"
	def internalActive = (attr == "internalContact")
	def otherVal = device.currentValue(internalActive ? "externalContact" : "internalContact")
	
	def displayed = null
	if (mainContactBehaviorSetting.contains("Only")) {
		displayed = false
	}

	result << createEvent(getEventMap("$attr", val, displayed))
	
	def mainVal = getMainContactVal(attr, val, otherVal)	
	if (mainVal) {
		result << createEvent(getEventMap("contact", mainVal))
	}
	
	// Create water, motion, and status events based off these values instead of the device attributes due to race condition.
	def eventData = [
		activeAttr: attr,
		contactVal: mainVal,
		internalVal: (internalActive ? val : otherVal ),
		externalVal: (internalActive ? otherVal : val )
	]
	result += createOtherEvents(eventData)
	
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

private createOtherEvents(eventData) {
	def result = []
	eventData.motionVal = determineMotionVal(eventData)
	if (eventData.motionVal) {
		result += createEvent(getEventMap("motion", eventData.motionVal))
	}
	
	eventData.waterVal = determineWaterVal(eventData)
	if (eventData.waterVal) {
		result << createEvent(getEventMap("water", eventData.waterVal))
	}
	
	result += createMainTileEvents(eventData)
	
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

private createMainTileEvents(eventData) {
	def result = []
	
	def data = [
		["none", ""],
		["motion", eventData.motionVal],
		["water", eventData.waterVal],
		["contact", eventData.contactVal],
		["internalContact", eventData.internalVal],
		["externalContact", eventData.externalVal]
	]
	
	[["primaryStatus", primaryStatusAttrSetting], ["secondaryStatus", secondaryStatusAttrSetting]].each { status, statusAttr ->
		def eventVal = data.find { attr, val -> "$statusAttr" == "$attr" }		
		if (eventVal && eventVal[1]) {
			result << createEvent(getEventMap("${status}", eventVal[1], false))
		}
	}
	
	return result
}

private getEventMap(eventName, newVal, displayed=null) {	
	def isNew = device.currentValue(eventName) != newVal
	def desc = "${eventName.capitalize()} is ${newVal}"
	logDebug "${desc}"
	[
		name: eventName, 
		value: newVal, 
		displayed: (displayed != null) ? displayed : isNew,
		descriptionText: desc
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

private getMainContactBehaviorSetting() {
	return settings?.mainContactBehavior ?: "Last Changed Contact"
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
		"externalContact", 
		"internalContact", 
		"motion", 
		"water"
	]
}

private getSecondaryStatusOptions() {
	return [
		"none",
		"contact", 
		"externalContact", 
		"internalContact", 
		"motion", 
		"water"
	]
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}