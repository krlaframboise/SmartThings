/**
 *  GoControl Contact Sensor v1.10.3
 *  (WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.10.3 (10/15/2018)
 *    	- Added support for new mobile app.
 *
 *    1.10.2 (09/10/2017)
 *    	- Removed old style fingerprint to eliminate conflicts with other generic sensors.
 *
 *    1.10.1 (07/01/2017)
 *    	- WARNING: This version may temporarily cause the main tile to display the wrong status, but it will correct itself the next time the device wakes up.  It doesn't effect the actual values being reported and you can immediately correct the primary tile by opening the settings and tapping Done.
 *    	- Added setting for displaying garage icons.
 *    	- Modified Health Check feature so that it doesn't set the checkin interval until it confirms that the wakeup interval has been changed.
 *    	- Updated colors to match SmartThing's new color theme.
 *    	- Added settings that allows you to specify the number of checkins that have to be missed before reporting the device as offline.  Setting it to 10 will practically disable the Health Check feature.
 *
 *    1.9.2 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.9.1 (04/20/2017)
 *			- Added workaround for ST Health Check bug.
 *
 *    1.9 (04/08/2017)
 *      - Added child device functionality for external contact. 
 *
 *    1.8.3 (03/12/2017)
 *      - Adjusted health check to allow it to skip a checkin before going offline.
 *
 *    1.8.1 (02/21/2017)
 *      - Added Health Check.
 *
 *    1.7.1 (01/10/2017)
 *      - Stopped displaying the internal/external events when the Main Contact Behavior is set to one of the "Only" options.
 *
 *    1.7 (01/07/2017)
 *      - Added Configuration command class to initialize the battery and contact values during installation.
 *      - Added setting that controls the behavior of the main contact based on the state of the internal and/or external contacts.
 *      - Removed lastPoll attribute and added lastCheckin since the device doesn't support polling.
 *
 *    1.6.1 (08/02/2016)
 *      - Fixed iOS UI issue caused by using multiple states with a value tile.
 *
 *    1.6 (06/22/2016)
 *      - Added support for the external contact.
 *      - Added attributes for internal and external contact so you can use them independently, but the main contact reflects the last state of either contact.
 *
 *    1.5 (06/19/2016)
 *      -  Bug with initial battery reporting.
 *
 *    1.4.3 (06/17/2016)
 *      -  Fixed issue with battery level being debug logged.
 *
 *    1.4.2 (05/21/2016)
 *      -  Fixing polling so that it doesn't require forcing state changes or always displaying events.
 *
 *    1.4.1 (05/5/2016)
 *      -  UI Enhancements
 *      -  Added Debug Logging
 *      -  Fixed default tamper state
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
		name: "GoControl Contact Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-contact"
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"
		
		attribute "internalContact", "enum", ["open", "closed"]
		attribute "externalContact", "enum", ["open", "closed"]
		attribute "lastCheckin", "string"
		attribute "lastOpen", "string"
		attribute "lastClosed", "string"
		attribute "primaryStatus", "string"

		// fingerprint deviceId: "0x2001", inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"			
		fingerprint mfr:"014F", prod:"2001", model:"0102"
	}

	// simulator metadata
	simulator {
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}
	
	preferences {
		input "mainContactBehavior", "enum",
			title: "Main Contact Behavior:",
			defaultValue: "Last Changed",
			required: false,
			options: ["Last Changed Contact (Default)", "Internal Contact Only", "External Contact Only", "Both Contacts Closed", "Both Contacts Open"]		
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
		input "useExternalDevice", "bool", 
			title: "Create Device for External Sensor?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
		input "useGarageIcons", "bool", 
			title: "Use Garage Icons?", 
			defaultValue: false, 
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
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#00a0dc"
				attributeState "open", 
					label:'open', 
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
		
		valueTile("lastCheckin", "device.lastCheckin", decoration: "flat", width: 2, height: 2){
			state "lastCheckin", label:'Checked In \n\n${currentValue}'
		}
		
		valueTile("lastOpen", "device.lastOpen", decoration: "flat", width: 2, height: 2){
			state "lastOpen", label:'Opened \n\n${currentValue}'
		}
		
		valueTile("lastClosed", "device.lastClosed", decoration: "flat", width: 2, height: 2){
			state "lastClosed", label:'Closed \n\n${currentValue}'
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% \nBattery', unit:""
		}
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh", "lastOpen", "lastClosed", "lastCheckin"])
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		if (useExternalDeviceSetting && !getChildDevices()) {
			createChildDevice()
			def child = getChildDevice()
			if (child) {
				log.debug "Updating"
				child.update()
			}
		}
		
		if (!device.currentValue("primaryStatus") || useGarageIconsSetting != state.useGarageIcons) {
			state.useGarageIcons = useGarageIconsSetting
			sendEvent(createPrimaryStatusEventMap(device.currentValue("contact")))
		}
		
	}
}

private void createChildDevice() {
	try {
		logDebug "Creating Child Device"
		def options = [
			completedSetup: true, 
			label: "${device.label} - External",
			isComponent: false
		]
		addChildDevice("krlaframboise", "GoControl External Contact Sensor", childDeviceNetworkId, null, options)	
	}
	catch (e) {
		log.warn("You need to install the GoControl External Contact Sensor DTH in order to use this feature.\n$e")
	}
}

def uninstalled() {
	logTrace "Executing uninstalled()"
	devices?.each {
		logDebug "Removing ${it.displayName}"
		deleteChildDevice(it.deviceNetworkId)
	}
}

def childUninstalled() {
	// Required to prevent warning on uninstall.
}

private getChildDeviceNetworkId() {
	return "${device.deviceNetworkId}-ext"
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!device.currentValue("contact")) {
		sendEvent(name: "contact", value: "open", isStateChange: true, displayed: false)
		sendEvent(createPrimaryStatusEventMap("open"))
	}
	else if (!device.currentValue("primaryStatus")) {
		sendEvent(createPrimaryStatusEventMap(device.currentValue("contact")))
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
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}	
	return cmds ? delayBetween(cmds, 500) : []
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
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
		}
	}
	return result
}

private handleTamperEvent(alarmLevel) {
	def result = []		
	
	if (alarmLevel == 0xFF) {		
		logDebug "Tamper Detected"
		result << createEvent(getEventMap("tamper", "detected"))
	}
	
	return result
}

private handleContactEvent(alarmLevel, attr) {
	def result = []
	def val = (alarmLevel == 0xFF) ? "open" : "closed"
	def otherVal = device.currentValue((attr == "internalContact") ? "externalContact" : "internalContact")
	def displayed = null
	if (settings?.mainContactBehavior?.contains("Only")) {
		displayed = false
	}
	
	if (attr == "externalContact") {
		handleChildContactEvent(val)
	}

	result << createEvent(getEventMap("$attr", val, displayed))
	
	def mainVal = getMainContactVal(attr, val, otherVal)	
	if (mainVal) {
		result << createEvent(getEventMap("contact", mainVal))
		result << createEvent(createPrimaryStatusEventMap(mainVal))		
	}
	
	sendEvent(name: "last${mainVal?.capitalize()}", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	return result
}

private createPrimaryStatusEventMap(val) {
	if (useGarageIconsSetting) {
		val = "garage-${val}"
	}
	return getEventMap("primaryStatus", val, false)
}

private void handleChildContactEvent(val) {
	def child = getChildDevice()
	if (child ) {
		logDebug "Executing Child ${val.capitalize()}()"
		if (val == "open") {
			child?.open()
		}
		else {
			child?.close()
		}
	}
	return null
}

private getChildDevice() {
	return getChildDevices()?.find { it && "${it}" != "null" }
}

private getMainContactVal(activeAttr, activeVal, otherVal) {
	def mainVal
	switch (settings?.mainContactBehavior) {
		case "Last Changed Contact (Default)":
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

private getEventMap(eventName, newVal, displayed=null) {	
	def isNew = device.currentValue(eventName) != newVal
	def desc = "${device.displayName} is ${newVal}"
	
	displayed = (displayed != null) ? displayed : isNew
	
	if (displayed) {
		logDebug "${desc}"
	}
	
	[
		name: eventName, 
		value: newVal, 
		displayed: displayed,
		isStateChange: true,
		descriptionText: desc
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
}

private basicGetCmd() {
	return zwave.basicV1.basicGet().format()
}

private batteryGetCmd() {
	logTrace "Requesting battery report"
	return zwave.batteryV1.batteryGet().format()
}

private wakeUpIntervalSetCmd(val) {
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
}

private wakeUpIntervalGetCmd() {
	return zwave.wakeUpV2.wakeUpIntervalGet().format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

// Settings
private getUseExternalDeviceSetting() {
	return settings?.useExternalDevice ?: false
}

private getUseGarageIconsSetting() {
	return settings?.useGarageIcons ?: false
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