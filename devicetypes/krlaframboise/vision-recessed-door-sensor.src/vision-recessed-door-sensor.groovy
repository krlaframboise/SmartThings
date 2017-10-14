/**
 *  Vision Recessed Door Sensor v1.0
 *    (Model: ZD2105US-5)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (10/14/2017)
 *      - Initial Release 
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
		name: "Vision Recessed Door Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"

		attribute "lastCheckin", "string"
		
		// zw:Ss type:0701 mfr:0109 prod:2022 model:2201 ver:5.01 zwv:4.05 lib:03 cc:5E,98 sec:86,72,5A,85,59,73,80,71,84,7A role:06 ff:8C07 ui:8C07
			
		fingerprint mfr:"0109", prod:"2022", model:"2201", deviceJoinName: "Vision Recessed Door Sensor"
	}
	
	simulator { }
	
	preferences {
		input "reportBatteryEvery", "number", 
			title: "Battery Reporting Interval (Hours)",
			defaultValue: 8,
			range: "4..167",
			displayDuringSetup: true, 
			required: false
		input "autoClearTamper", "bool", 
			title: "Automatically Clear Tamper?",
			description: "The tamper detected event is raised when the device is opened.  This setting allows you to decide whether or not to have the clear event automatically raised when the device closes.",
			defaultValue: false,
			displayDuringSetup: true, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'Closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#00a0dc"
				attributeState "open", 
					label:'Open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#e86d13"
			}
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% Battery', unit:""
		}		
		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#e86d13"
			state "clear", label:"No Tamper", backgroundColor: "#ffffff"			
		}
	
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh"])
	}
}

def updated() {	
	logTrace "updated()"	
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!state.isConfigured) {
		state.isConfigured = true
		sendEvent(name: "contact", value: "open")
		sendEvent(name: "tamper", value: "detected")
		
		// Give inclusion time to finish.
		logTrace "Waiting for inclusion to finish"
		
		cmds << "delay 5000"
	}
	
	initializeCheckin()
		
	cmds << batteryGetCmd()
	
	return cmds
}

private initializeCheckin() {
	// SmartThings automatically sets wakeup interval to 4 hours when included so allow health check to skip 1 check and be 5 additional minutes late.
	def checkInterval = (((4 * 2 * 60) + 5) * 60)
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def parse(String description) {
	def result = []
	
	if (description?.startsWith("Err")) {
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
	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time	
		result << createLastCheckinEvent()
	}
	return result
}

private createLastCheckinEvent() {
	logDebug "Device Checked In"	
	return createEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		state.useSecureCmds = true
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"	
	}
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	if (!state.isConfigured) {
		cmds += configure()
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last $reportEveryHours hours."
	}
	
	if (cmds) {
		cmds << "delay 1000"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}

private canReportBattery() {
	def reportEveryHours = settings?.reportBatteryEvery ?: 6
	def reportEveryMS = (reportEveryHours * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	state.lastBatteryReport = new Date().time	
	[
		createEvent(createEventMap("battery", val, null, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	if (cmd.notificationType == 0x06) {
		result += handleContactEvent(cmd.event)
	}
	else if (cmd.notificationType == 0x07) {		
		result += handleTamperEvent(cmd.event)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private handleContactEvent(event) {
	def result = []
	def val
	if (event == 0xFF || event == 0x16) {
		val = "open"
	}
	else if(event == 0 || event == 0x17) {
		val = "closed"
	}
	if (val) {
		result << createEvent(createEventMap("contact", val))
	}
	return result
}

private handleTamperEvent(event) {
	def result = []
	def val
	if (event == 0x03) {
		val = "detected"
	}
	else if (event == 0) {
		if (settings?.autoClearTamper) {
			val = "clear"
		}
		else {
			logDebug "Tamper is Clear"
		}
	}
	if (val) {
		result << createEvent(createEventMap("tamper", val))
	}
	return result
}

// Resets the tamper attribute to clear and requests the device to be refreshed.
def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(createEventMap("tamper", "clear", false))
	}
}

def createEventMap(eventName, newVal, displayed=null, unit=null) {
	if (displayed == null) {
		displayed = (device.currentValue(eventName) != newVal)
	}
	if (displayed) {
		logDebug "${eventName.capitalize()} is ${newVal}"
	}
	def eventMap = [
		name: eventName, 
		value: newVal, 
		displayed: displayed,
		isStateChange: true
	]
	if (unit) {
		eventMap.unit = unit
	}
	return eventMap
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private secureCmd(cmd) {
	// if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
	if (state.useSecureCmds) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
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