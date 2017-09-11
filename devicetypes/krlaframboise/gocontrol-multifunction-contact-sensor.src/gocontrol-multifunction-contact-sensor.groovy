/**
 *  GoControl Multifunction Contact Sensor v1.2.1
 *  (WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-gocontrol-linear-multifunction-contact-sensor/77659?u=krlaframboise
 *    
 *  Changelog:
 *
 *    1.2.1 (09/10/2017)
 *    	- Removed old style fingerprint to eliminate conflicts with other generic sensors. 
 *
 *    1.2 (07/01/2017)
 *    	- Updated colors to match SmartThing's new color theme.
 *    	- Modified Health Check feature so that it doesn't set the checkin interval until it confirms that the wakeup interval has been changed.
 *    	- Added settings that allows you to specify the number of checkins that have to be missed before reporting the device as offline.  Setting it to 10 will practically disable the Health Check feature.
 *    	- Removed ability to manually change icon because it prevents the icon from changing with the attribute state.
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

		//fingerprint deviceId: "0x2001", inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"		
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
		input "missedCheckins", "enum",
			title: "How many checkins does the device need to miss before it's reported as offline?",
			defaultValue: missedCheckinsSetting,
			required: false,
			displayDuringSetup: true,
			options: missedCheckinsOptions.collect { it.name }
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
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'Closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#00a0dc"
				attributeState "open", 
					label:'Open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#e86d13"
				attributeState "garage-closed", 
					label:'Closed', 
					icon:"st.doors.garage.garage-closed", 
					backgroundColor:"#00a0dc"
				attributeState "garage-open", 
					label:'Open', 
					icon:"st.doors.garage.garage-open", 
					backgroundColor:"#e86d13"				
				attributeState "inactive", 
					label:'No Motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'Motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#00a0dc"
				attributeState "dry", 
					label:"Dry", 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:"Wet", 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#00a0dc"
				attributeState "clear", 
					label:'Clear', 
					icon:"st.alarm.smoke.clear", 
					backgroundColor:"#ffffff"
				attributeState "detected", 
					label:'Detected', 
					icon:"st.alarm.smoke.smoke", 
					backgroundColor:"#e86d13"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "", 
					label:''
				attributeState "closed", 
					label:'CLOSED', 
					icon:"st.contact.contact.closed"
				attributeState "open", 
					label:'OPEN', 
					icon:"st.contact.contact.open"
				attributeState "garage-closed", 
					label:'CLOSED', 
					icon:"st.doors.garage.garage-closed"
				attributeState "garage-open", 
					label:'OPEN', 
					icon:"st.doors.garage.garage-open"
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"st.motion.motion.inactive"
				attributeState "active", 
					label:'MOTION', 
					icon:"st.motion.motion.active"
				attributeState "dry", 
					label:"DRY", 
					icon:"st.alarm.water.dry"
				attributeState "wet", 
					label:"WET", 
					icon:"st.alarm.water.wet"
				attributeState "clear", 
					label:'CLEAR', 
					icon:"st.alarm.smoke.clear"
				attributeState "detected", 
					label:'DETECTED', 
					icon:"st.alarm.smoke.smoke"
			}
		}	
				
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% \nBattery', unit:""
		}		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#e86d13"
			state "clear", label:"No \nTamper", backgroundColor: "#cccccc"			
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
		state.isConfigured = true
		logTrace "Waiting 1 second because this is the first time being configured"
		// Give inclusion time to finish.
		cmds << "delay 1000"			
	}

	if (state.checkinIntervalSeconds != (checkinIntervalSettingSeconds)) {
		logTrace "Updating wakeup interval"
		cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		cmds << wakeUpIntervalGetCmd()
	}
	
	if (!isDuplicateCommand(state.lastBatteryReport, (batteryReportingIntervalSettingMinutes * 60 * 1000))) {
		cmds << batteryGetCmd()
	}	
	
	return cmds ? delayBetween(cmds, 500) : []
}

private refreshMainTile() {
	logTrace "refreshMainTile()"
	createContactEventMaps("internalContact", device.currentValue("internalContact"))?.each {
		sendEvent(it)
	}
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
	
	if (!isDuplicateCommand(state.lastCheckin, 60000)) {
		state.lastCheckin = new Date().time
		sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
	
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
	
	cmds += configure()
	
	if (cmds) {
		cmds << "delay 1000"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return sendResponse(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	def result = []
	
	state.checkinIntervalSeconds = cmd.seconds
	
	// Set the Health Check interval so that it reports offline 5 minutes after it missed the # of checkins specified in the settings.
	def threshold = convertOptionSettingToInt(missedCheckinsOptions, missedCheckinsSetting)
	def checkInterval = ((cmd.seconds * threshold) + (5 * 60))
	
	result << createEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return result
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
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
	// logTrace "Basic Set: $cmd"	
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
		logTrace "Tamper Detected"
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

private wakeUpIntervalSetCmd(val) {
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
}

private wakeUpIntervalGetCmd() {
	return zwave.wakeUpV2.wakeUpIntervalGet().format()
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

private getSmokeDetectedEventSetting() {
	return settings?.smokeDetectedEvent ?: "none"
}

private getSmokeClearEventSetting() {
	return settings?.smokeClearEvent ?: "default"
}

private getMainContactBehaviorSetting() {
	return settings?.mainContactBehavior ?: "Last Changed Contact"
}

private getCheckinIntervalSettingSeconds() {
	return (convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) * 60) ?: (6 * 60 * 60)
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getMissedCheckinsSetting() {
	return settings?.missedCheckins ?: findDefaultOptionName(missedCheckinsOptions)
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

private getMissedCheckinsOptions() {
	def items = []
	(1..10).each {
		items << [
			name: (it == 3) ? formatDefaultOptionName("$it") : "$it", 
			value: it
		]
	}
	return items
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