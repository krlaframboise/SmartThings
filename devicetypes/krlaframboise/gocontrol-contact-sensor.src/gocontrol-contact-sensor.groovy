/**
 *  GoControl Contact Sensor v1.9.2
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
		author: "Kevin LaFramboise"
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

		fingerprint deviceId: "0x2001", inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"			
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
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}
	
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
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
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh"])
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
		sendEvent(getEventMap("tamper", "clear"))		
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
	}
	return result
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

private wakeUpIntervalSetCmd(minutesVal) {
	state.checkinIntervalMinutes = minutesVal
	logTrace "wakeUpIntervalSetCmd(${minutesVal})"
	
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:(minutesVal * 60), nodeid:zwaveHubNodeId).format()
}

// Settings
private getUseExternalDeviceSetting() {
	return settings?.useExternalDevice ?: false
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