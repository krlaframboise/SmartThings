/**
 *  GoControl Contact Sensor v1.7.1
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
		
		attribute "internalContact", "enum", ["open", "closed"]
		attribute "externalContact", "enum", ["open", "closed"]
		attribute "lastCheckin", "number"

		fingerprint deviceId: "0x2001", inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"			
		fingerprint type:"2001", cc:"71,85,80,72,30,86,84"
		fingerprint mfr:"014F", prod:"2001", model:"0102"
	}

	// simulator metadata
	simulator {
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}
	
	preferences {
		input "reportBatteryEvery", "number", 
			title: "Report Battery Every? (Hours)", 
			defaultValue: 4,
			range: "4..167",
			displayDuringSetup: true, 
			required: false
		input "mainContactBehavior", "enum",
			title: "Main Contact Behavior:",
			defaultValue: "Last Changed",
			required: false,
			options: ["Last Changed Contact (Default)", "Internal Contact Only", "External Contact Only", "Both Contacts Closed", "Both Contacts Open"]
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
			state "default", label: "Refresh", action: "refresh", icon:""
		}
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh"])
	}
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
		result << "delay 5000"
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
	def otherVal = device.currentValue((attr == "internalContact") ? "externalContact" : "internalContact")
	def displayed = null
	if (settings?.mainContactBehavior?.contains("Only")) {
		displayed = false
	}

	result << createEvent(getEventMap("$attr", val, displayed))
	
	def mainVal = getMainContactVal(attr, val, otherVal)	
	if (mainVal) {
		result << createEvent(getEventMap("contact", mainVal))
	}
	return result
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

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	//log.trace "$msg"
}