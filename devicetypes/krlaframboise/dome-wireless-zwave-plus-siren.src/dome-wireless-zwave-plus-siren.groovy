/**
 *  Dome Wireless Z-Wave Plus Siren v1.0.1
 *  (Model: DMS01)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-dome-wireless-z-wave-plus-siren/71091?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.0.1 (01/03/2016)
 *      - Bug fix for settings not saving properly.
 *
 *    1.0 (12/31/2016)
 *      - Initial Release
 *
 *
 *	I appreciate the support and feedback I have received here, with a special thanks to those that have donated at https://www.paypal.me/krlaframboise.  It is not required, but it does motivate me and it allows me to continue buying devices I really don't need, just so that I can create a DTH.
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
		name: "Dome Wireless Z-Wave Plus Siren", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Tone"
		capability "Polling"

		attribute "lastCheckin", "number"
		attribute "status", "enum", ["alarm", "beep", "off", "on", "custom"]
		
		command "customChime"
		
		fingerprint deviceId: "0x1005", inClusters: "0x25, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x85, 0x86, 0x87"
		
		fingerprint mfr:"021F", prod:"0003", model:"0088" 
		
		fingerprint type:"1005", cc: "25,59,5A,5E,70,71,72,73,80,85,86,87"
	}
	
	simulator { }
	
	preferences {
		input "sirenSound", "number",
			title: "Siren Sound (1-10):",
			range: "1..10",
			displayDuringSetup: true,
			defaultValue: sirenSoundSetting
		input "sirenVolume", "enum",
			title: "Siren Volume:",
			options: volumeOptions,
			required: true,
			displayDuringSetup: true
		input "sirenLength", "enum",
			title: "Siren Auto Off:",
			options: ["30 Seconds", "1 Minute", "5 Minutes", noLengthMsg],
			required: true,
			displayDuringSetup: true
		input "onChimeSound", "number",
			title: "Switch On Chime Sound (1-10):",
			range: "1..10",
			required: false,
			displayDuringSetup: true,
			defaultValue: onChimeSoundSetting
		input "beepChimeSound", "number",
			title: "Beep Chime Sound (1-10):",
			range: "1..10",
			required: false,
			displayDuringSetup: true,
			defaultValue: beepChimeSoundSetting
		input "chimeVolume", "enum",
			title: "Chime Volume:",
			options: volumeOptions,
			required: true,
			displayDuringSetup: true
		input "reportBatteryEvery", "number", 
			title: "Battery Reporting Interval (Hours)", 
			range: "1..167",
			required: false,
			displayDuringSetup: true,
			defaultValue: reportBatteryEverySetting
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false			
		// Not providing Toggle Secondary Chime feature so this setting is not needed
		// input "chimeLength", "enum",
			// title: "Chime Length:",
			// options: ["1 Minute", "5 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "2 Hours", noLengthMsg],
			// required: true,
			// displayDuringSetup: true		
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "off", 
					icon: "st.security.alarm.clear",
					backgroundColor:"#ffffff"
				attributeState "on", 
					label:'Chime (On)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 					
					backgroundColor: "#99c2ff"					
				attributeState "alarm", 
					label:'Siren!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 
					backgroundColor:"#ff9999"
				attributeState "beep", 
					label:'Chime (Beep)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 
					backgroundColor:"#99ff99"
				attributeState "custom", 
					label:'Chime (Custom)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 
					backgroundColor:"#cc99cc"				
			}
		}
		
		standardTile("playAlarm", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Alarm', 
				action:"alarm.both", 
				icon:"st.security.alarm.clear", 
				backgroundColor:"#ff9999"
			state "both",
				label:'Turn Off',
				action:"alarm.off",
				icon:"st.alarm.alarm.alarm", 
				background: "#ffffff"	
		}
				
		standardTile("playOn", "device.switch", width: 2, height: 2) {
			state "default", 
				label:'Turn On', 
				action:"switch.on", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor:"#99c2ff"
			state "on",
				label:'Turn Off',
				action:"switch.off",
				icon:"st.Entertainment.entertainment2", 
				background: "#ffffff"	
		}
		
		standardTile("playBeep", "device.status", width: 2, height: 2) {
			state "default", 
				label:'Beep', 
				action:"tone.beep", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor: "#99FF99"
			state "beep",
				label:'Stop',
				action:"off",
				icon:"st.Entertainment.entertainment2", 
				background: "#ffffff"	
		}
		
		standardTile("turnOff", "device.off", width: 2, height: 2) {
			state "default", 
				label:'Off', 
				action:"switch.off", 
				backgroundColor: "#ffffff"			
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
				
		main "status"
		details(["status", "playAlarm", "playOn", "playBeep", "turnOff", "refresh", "battery"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		state.activeEvents = []
		logTrace "updated()"
		
		if (state.firstUpdate == false) {
			def result = []
			result += configure()
			if (result) {
				return response(result)
			}
		}
		else {
			// Skip first time updating because it calls the configure method while it's already running.
			state.firstUpdate = false
		}
	}	
}

def configure() {
	logTrace "configure()"
	def cmds = []
	def refreshAll = (!state.isConfigured || state.pendingRefresh || !settings?.sirenSound)
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		cmds << "delay 1000"			
	}

	// Chime Volume (param 4) must be changed before Siren Sound (param 5) because the device has a bug that causes param 5 to change every time param 4 is updated.  Setting the value of param 5 has no effect on param 4.
	cmds += updateConfigVal(chimeVolumeParamNum, convertVolumeNameToVal(chimeVolumeSetting), refreshAll)
	
	cmds += updateConfigVal(sirenVolumeParamNum, convertVolumeNameToVal(sirenVolumeSetting), refreshAll)
	
	cmds += updateConfigVal(sirenLengthParamNum, convertSirenLengthNameToVal(sirenLengthSetting), refreshAll)
	
	cmds += updateConfigVal(sirenSoundParamNum, sirenSoundSetting, cmds ? true : false)
		
	// Not providing Toggle Secondary Chime feature so there's no reason to set these setting.
	// if (toggleSecondaryChimeSetting == 2) {		
		// cmds += updateConfigVal(chimeSoundParamNum, chimeSoundSetting, refreshAll)
	
		// cmds += updateConfigVal(chimeLengthParamNum, convertChimeLengthNameToVal(chimeLengthSetting), refreshAll)	
	// }
	
	if (refreshAll) {
		cmds += updateConfigVal(toggleSecondaryChimeParamNum, toggleSecondaryChimeSetting, refreshAll)
		cmds << switchBinaryGetCmd()
	}
		
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 250)
	}
	else {
		return cmds
	}	
}

private updateConfigVal(paramNum, val, refreshAll) {
	def result = []
	def configVal = state["configVal${paramNum}"]
	
	if (refreshAll || (configVal != val)) {
		result << configSetCmd(paramNum, val)
		result << configGetCmd(paramNum)
	}
	
	return result
}

def off() {
	logDebug "Turning Off()"
	return sirenToggleCmds(0x00)	
}

def on() {	
	logDebug "Playing On Chime (#${onChimeSoundSetting})"	
	addPendingSound("switch", "on")
	return chimePlayCmds(onChimeSoundSetting)
}

def beep() {
	logDebug "Playing Beep Chime (#${beepChimeSoundSetting})"	
	addPendingSound("status", "beep")
	return chimePlayCmds(beepChimeSoundSetting)
}

def customChime(sound) {	
	def val = validateSound(sound, beepChimeSoundSetting)
	if ("${sound}" != "${val}") {
		logDebug "Playing Custom Chime (#${val}) - (${sound} is not a valid sound number)"
	}
	else {
		logDebug "Playing Custom Chime (#${val})"	
	}	
	addPendingSound("status", "custom")	
	return chimePlayCmds(val)
}

def siren() { return both() }
def strobe() { return both() }
def both() {
	logDebug "Playing Siren (#${sirenSoundSetting})"
	addPendingSound("alarm", "both")

	def result = []
	result << configGetCmd(sirenSoundParamNum)
	result << "delay 1000"
	result += sirenToggleCmds(0xFF)
	return result
}

private addPendingSound(name, value) {
	state.pendingSound = [name: "$name", value: "$value", time: new Date().time]
}

def refresh() {	
	logTrace "refresh()"
	state.pendingRefresh = true
	return configure()
}

def poll() {
	if (canCheckin() && canReportBattery()) {
		logDebug "Requesting battery report because device was polled."
		return [batteryGetCmd()]
	}
	else {
		logDebug "Ignored poll request because it hasn't been long enough since the last poll."
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

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

private getCommandClassVersions() {
	[
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x87: 1,  // Indicator
		0x25: 1,  // Switch Binary
	]
}

private canReportBattery() {	
	def reportEveryMS = (reportBatteryEverySetting * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	//logTrace "BatteryReport: $cmd"
	def map = [ 
		name: "battery", 		
		unit: "%"
	]
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.isStateChange = true
		logDebug "${map.descriptionText}"
	}
	else {	
		def isNew = (device.currentValue("battery") != cmd.batteryLevel)
		map.value = cmd.batteryLevel
		// map.displayed = isNew
		// map.isStateChange = isNew
		map.displayed = false
		map.isStateChange = true
		logDebug "Battery is ${cmd.batteryLevel}%"
	}	
	
	state.lastBatteryReport = new Date().time	
	[
		createEvent(map)
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	def name = convertParamNumToName(cmd.parameterNumber)	
	if (name) {	
		def val = cmd.configurationValue[0]
	
		logDebug "${name} = ${val}"
	
		state."configVal${cmd.parameterNumber}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	state.isConfigured = true
	state.pendingRefresh = false	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def result = []
		
	if (cmd.value == 0x00) {
		result += handleDeviceOff()
	}
	else {
		// The on events are handled by either the indicator report handler or the notification report handler because they identify the action performed and not just the current state of the device.
		logTrace "Ignored 'on' switch binary event"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	return handleDeviceOn(state.activeSound, state.pendingSound)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	
	if (cmd.notificationType == 14) {
		if (cmd.event == 0x01) {
			// Raises the alarm.both command when the siren is turned on, but ignores the chime because the chime on events get raised when the indicator report is received. 
			if (state.pendingSound?.name == "alarm") {
				result += handleDeviceOn(state.activeSound, state.pendingSound)
			}
			else if (!state.pendingSound) {
				logTrace "Unable to handle 'on' notification event because the pending sound has not been set."
			}
			else {
				logTrace "Ignored 'on' notification event for chime."
			}
		}
		else {
			logTrace "Ignored 'off' notification event"
		}
	}
	return result
}

private handleDeviceOn(activeSound, pendingSound) {
	def result = []
	def activeSoundName = activeSound?.name
	
	state.activeSound = pendingSound
		
	if (activeSoundName && (pendingSound != activeSound) && (device.currentValue(activeSoundName) != "off")) {
		// Raising the off event for the previously active sound if it's still playing prevents the UI buttons from getting stuck when multiple sounds are played without breaks between them.
		result << createEvent(getEventMap([name: activeSoundName, value: "off"], true))
	}
	
	if (pendingSound) {
		result << createEvent(getEventMap(pendingSound, true))
		
		def statusVal = ""
		if (pendingSound.name == "alarm") {
			statusVal = "alarm"
		}
		else if (pendingSound.name == "switch") {
			statusVal = "on"
		}
		if (statusVal) {
			result << createEvent(getStatusEventMap(statusVal))
		}
	}
	else {
		logTrace "Unable to create event on because the pending sound has not been set."
	}	
	return result
}

private handleDeviceOff() {
	def result = []
	// The events switch.on and alarm.both also generate a status event so it creates events for any of the attributes that haven't already been set to off.
	["alarm", "switch", "status"].each { n ->		
		if (device.currentValue(n) != "off") {
		
			def displayed = false
			if ("${n}" == "${state.activeSound?.name}") {
				// Only the active event was initially displayed so it's the only off event that gets displayed.
				displayed = true
				state.activeSound = null
			}
			
			result << createEvent(getEventMap([name: "$n", value: "off"], displayed))
		}
	}

	return result
}

private getStatusEventMap(val) {
	return getEventMap([name: "status", value: val], false)
}

private getEventMap(event, displayed=false) {	
	def isStateChange = (device.currentValue(event.name) != event.value)
	def eventMap = [
		name: event.name,
		value: event.value,
		displayed: displayed,
		isStateChange: isStateChange
	]
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private chimePlayCmds(sound) {
	// It has to request the indicator report because it's not sent automatically.
	def cmds = [
		indicatorSetCmd(sound),
		indicatorGetCmd()
	]
	if (sound == 9 || sound == 10) {
		// Fixes problem where these sounds stop playing before the on/beep events are raised causing the off events to never get raised.
		cmds << "delay 3000"
		cmds << switchBinaryGetCmd()
	}	
	return cmds
}
private indicatorGetCmd() {
	return zwave.indicatorV1.indicatorGet().format()
}
private indicatorSetCmd(val) {
	return zwave.indicatorV1.indicatorSet(value: val).format()
}

private sirenToggleCmds(val) {
	// The switch binary reports are sent automatically when the device changes state, but it still has to request it to ensure that it gets sent when the device is already in the state it's being set to.  If the report is skipped, the attributes might not get set back to off afterwards.
	return delayBetween([
		switchBinarySetCmd(val),
		switchBinaryGetCmd()
	], 50)
}
private switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}
private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private configGetCmd(paramNum) {
	return zwave.configurationV1.configurationGet(parameterNumber: paramNum).format()
}

private configSetCmd(paramNum, val) {		
	return zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val).format()
}


// Settings
private getReportBatteryEverySetting() {
	return safeToInt(settings?.reportBatteryEvery, 8)
}
private getSirenSoundSetting() {
	return validateSound(settings?.sirenSound, 9)
}
private getSirenVolumeSetting() {
	return settings?.sirenVolume ?: "Medium"
}
private getSirenLengthSetting() {
	return settings?.sirenLength ?: "1 Minute"
}
private getOnChimeSoundSetting() {
	return validateSound(settings?.onChimeSound, 1)
}
private getBeepChimeSoundSetting() {
	 return validateSound(settings?.beepChimeSound, 3)
}
private getChimeVolumeSetting() {
	return settings?.chimeVolume ?: "Medium"
}
private getChimeLengthSetting() {
	return settings?.chimeLength ?: "1 Minute"
}

private getToggleSecondaryChimeSetting() {	
	// The Toggle Secondary Chime feature is too complex to try and explain on the settings screen and would just confuse people so it's set to the default value.
	
	// When Toggle Secondary Chime is set 01 (default), it works like this:
	
	//  * The Binary SwitchS command class is used to play the siren and it uses the sound stored in the device configuration.
	
	//  * The Indicator command class is used to play the chime, but the chime sound has to be specified and the one stored in the configuration is ignored.  
	
	// Changing the Toggle Secondary Chime to 02 causes them to switch so the chime sound in configuration is played when it's turned on and the siren sound has to be specified.	
	return 1		
}

private validateSound(sound, defaultVal) {
	def val = safeToInt(sound, defaultVal)
	if (val > 10) {
		val = 10
	}
	else if (val < 1) {
		val = 1
	}
	return val
}

private getSirenSoundParamNum() { return 5 }
private getSirenVolumeParamNum() { return 1 }
private getSirenLengthParamNum() { return 2 }
private getChimeSoundParamNum() { return 6 }
private getChimeVolumeParamNum() { return 4 }
private getChimeLengthParamNum() { return 3 }
private getToggleSecondaryChimeParamNum() { return 7 }

private getNoLengthMsg() { 
	return "Play until battery is depleted" 
}

private getVolumeOptions() { ["Low", "Medium", "High"] }

private convertParamNumToName(paramNum) {
	def name = ""
	switch (paramNum) {
		case sirenSoundParamNum:
			name = "Siren Sound"
			break
		case sirenVolumeParamNum:
			name = "Siren Volume"
			break
		case sirenLengthParamNum:
			name = "Siren Length"
			break
		case chimeSoundParamNum:
			name = "Chime Sound"
			break
		case chimeVolumeParamNum:
			name = "Chime Volume"
			break
		case chimeLengthParamNum:
			name = "Chime Length"
			break
		case toggleSecondaryChimeParamNum:
			name = "Toggle Secondary Chime"
			break
		default:
			name = "Unknown"
	}
	return name
}

private convertVolumeNameToVal(name) {
	def val
	switch ("${name}".toLowerCase()) {
		case "low":
			val = 1
			break
		case "medium":
			val = 2
			break
		case "high":
			val = 3
			break
		default:
			val = 2
	}
	return val			
}

private convertSirenLengthNameToVal(name) {
	def val
	switch ("${name}"?.toLowerCase()) {
		case "30 seconds":
			val = 1
			break
		case "1 minute":
			val = 2
			break
		case "5 minutes":
			val = 3
			break
		case noLengthMsg.toLowerCase():
			val = 255
			break
		default:
			val = 2
	}
	return val
}

private convertChimeLengthNameToVal(name) {
	def val
	switch ("${name}"?.toLowerCase()) {
		case "1 minute":
			val = 1
			break
		case "5 minutes":
			val = 5
			break
		case "15 minutes":
			val = 15
			break
		case "30 minutes":
			val = 30
			break
		case "1 hour":
			val = 60
			break
		case "2 hours":
			val = 120
			break
		case noLengthMsg.toLowerCase():
			val = 0
			break
		default:
			val = 1
	}
	return val
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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
	//log.trace "$msg"
}