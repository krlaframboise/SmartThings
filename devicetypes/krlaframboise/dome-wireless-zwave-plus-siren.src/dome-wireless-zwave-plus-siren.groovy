/**
 *  Dome Wireless Z-Wave Plus Siren v1.0.6
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
 *    1.0.6 (01/??/2016)
 *      - 
 *
 *    1.0.5 (01/21/2016)
 *      - Added settings for Alarm Delay and Alarm Delay Beep.
 *      - Made siren turn the siren on immediately and both turn it on after the specified durationn.
 *      - Added beep feature during the alarm delay.
 *
 *    1.0.4 (01/20/2016)
 *      - Added device join name and removed switch capability.
 *      - Replaced chime repeat with dropdown
 *      - Removed switch capability
 *
 *    1.0.3 (01/20/2016)
 *      - Split chime tiles/commands into bells, chimes, and sirens.
 *      - Added Chime & Siren LED settings
 *      - Added repeat chime setting
 *      - Changed Alarm Sound setting to dropdown
 *      - Added support for the built in SHM Audio Notification messages.
 *
 *    1.0.2 (01/19/2016)
 *      - Added support for audio related capabilities to allow the chime feature to be used with any SmartApp.
 *      - Added tile for each chime.
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
		//capability "Switch"
		capability "Tone"
		capability "Polling"
		capability "Speech Synthesis"
		capability "Audio Notification"
		capability "Music Player"
		
		
		attribute "lastCheckin", "number"
		attribute "status", "enum", ["alarm", "pending", "off", "chime"]
		
		//attribute "status", "enum", ["alarm", "beep", "off", "on", "custom"]
		
		
		// Required for Speaker notify with sound
		command "playSoundAndTrack"
		command "playTrackAtVolume"		

		//command "customChime"
		command "bell1"
		command "bell2"
		command "bell3"
		command "bell4"
		command "bell5"
		command "chime1"
		command "chime2"
		command "chime3"
		command "siren1"
		command "siren2"
	}
	
	simulator { }
	
	preferences {
		input "sirenSound", "enum",
			title: "Alarm Sound:",
			displayDuringSetup: true,
			required: false,
			defaultValue: sirenSoundSetting,
			options: sirenSoundOptions.collect { it.name }
		input "sirenVolume", "enum",
			title: "Alarm Volume:",
			required: false,
			defaultValue: sirenVolumeSetting,			
			displayDuringSetup: true,
			options: volumeOptions.collect { it.name }
		input "sirenLength", "enum",
			title: "Alarm Duration:",
			defaultValue: sirenLengthSetting,
			required: false,
			displayDuringSetup: true,
			options: sirenLengthOptions.collect { it.name }
		input "sirenLED", "enum",
			title: "Alarm LED:",
			defaultValue: sirenLEDSetting,
			required: false,
			displayDuringSetup: true,
			options: ledOptions.collect { it.name }
		input "sirenDelay", "enum",
			title: "Alarm Delay:",
			defaultValue: sirenDelaySetting,
			required: false,
			displayDuringSetup: true,
			options: sirenDelayOptions.collect { it.name }
		input "sirenDelayBeep", "enum",
			title: "Alarm Delay Beep:",
				defaultValue: sirenDelayBeepSetting,
				required: false,
				displayDuringSetup: true,
				options: sirenDelayBeepOptions.collect { it.name }
		// input "onChimeSound", "number",
			// title: "Switch On Chime Sound (1-10):",
			// range: "1..10",
			// required: false,
			// displayDuringSetup: true,
			// defaultValue: onChimeSoundSetting
		// input "beepChimeSound", "number",
			// title: "Beep Chime Sound (1-10):",
			// range: "1..10",
			// required: false,
			// displayDuringSetup: true,
			// defaultValue: beepChimeSoundSetting
		input "chimeVolume", "enum",
			title: "Chime Volume:",
			required: false,
			defaultValue: chimeVolumeSetting,
			displayDuringSetup: true,
			options: volumeOptions.collect { it.name }
		input "chimeRepeat", "enum",
			title: "Chime Repeat:",
			required: false,
			displayDuringSetup: true,
			defaultValue: chimeRepeatSetting,
			options: chimeRepeatOptions.collect { it.name }
		input "chimeLED", "enum",
			title: "Chime LED:",
			defaultValue: chimeLEDSetting,
			required: false,
			displayDuringSetup: true,
			options: ledOptions.collect { it.name }
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
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "off", 
					icon: "st.security.alarm.clear",
					backgroundColor:"#ffffff"
				attributeState "alarm", 
					label:'Alarm!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 
					backgroundColor:"#ff9999"
				attributeState "pending", 
					label:'Alarm Pending!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 
					backgroundColor:"#ff9999"
				attributeState "chime", 
					label:'Chime!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 					
					backgroundColor: "#cc99cc"
				// attributeState "bell", 
					// label:'Bell!', 
					// action: "off", 
					// icon:"st.Seasonal Winter.seasonal-winter-002", 
					// backgroundColor:"#99ff99"
				// attributeState "custom", 
					// label:'Chime!', 
					// action: "off", 
					// icon:"st.Entertainment.entertainment2", 
					// backgroundColor:"#cc99cc"
				// attributeState "on", 
					// label:'Chime (On)!', 
					// action: "off", 
					// icon:"st.Entertainment.entertainment2", 					
					// backgroundColor: "#99c2ff"
				// attributeState "beep", 
					// label:'Chime (Beep)!', 
					// action: "off", 
					// icon:"st.Entertainment.entertainment2", 
					// backgroundColor:"#99ff99"
			}
		}
		
		standardTile("playAlarm", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Alarm', 
				action:"alarm.siren", 
				icon:"st.security.alarm.clear", 
				backgroundColor:"#ff9999"
			state "siren",
				label:'Turn Off',
				action:"alarm.off",
				icon:"st.alarm.alarm.alarm", 
				background: "#ffffff"	
		}
				
		// standardTile("playOn", "device.switch", width: 2, height: 2) {
			// state "default", 
				// label:'Turn On', 
				// action:"switch.on", 
				// icon:"st.Entertainment.entertainment2", 
				// backgroundColor:"#99c2ff"
			// state "on",
				// label:'Turn Off',
				// action:"switch.off",
				// icon:"st.Entertainment.entertainment2", 
				// background: "#ffffff"	
		// }
		
		// standardTile("playBeep", "device.status", width: 2, height: 2) {
			// state "default", 
				// label:'Beep', 
				// action:"tone.beep", 
				// icon:"st.Entertainment.entertainment2", 
				// backgroundColor: "#99FF99"
			// state "beep",
				// label:'Stop',
				// action:"off",
				// icon:"st.Entertainment.entertainment2", 
				// background: "#ffffff"	
		// }
		
		standardTile("turnOff", "device.off", width: 2, height: 2) {
			state "default", 
				label:'Off', 
				action:"alarm.off", 
				icon:"st.security.alarm.clear",
				backgroundColor: "#ffffff"			
		}
				
		standardTile("playBell1", "device.status", width: 2, height: 2) {
			state "default", label:'Bell 1', action:"bell1", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"
		}
		
		standardTile("playBell2", "device.status", width: 2, height: 2) {
			state "default", label:'Bell 2', action:"bell2", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"
		}
		
		standardTile("playBell3", "device.status", width: 2, height: 2) {
			state "default", label:'Bell 3', action:"bell3", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"
		}
		
		standardTile("playBell4", "device.status", width: 2, height: 2) {
			state "default", label:'Bell 4', action:"bell4", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"
		}
		
		standardTile("playBell5", "device.status", width: 2, height: 2) {
			state "default", label:'Bell 5', action:"bell5", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"
		}
		
		standardTile("playChime1", "device.status", width: 2, height: 2) {
			state "default", label:'Chime 1', action:"chime1", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"
		}
		
		standardTile("playChime2", "device.status", width: 2, height: 2) {
			state "default", label:'Chime 2', action:"chime2", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"
		}
		
		standardTile("playChime3", "device.status", width: 2, height: 2) {
			state "default", label:'Chime 3', action:"chime3", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"
		}
		
		standardTile("playSiren1", "device.status", width: 2, height: 2) {
			state "default", label:'Siren 1', action:"siren1", icon:"st.security.alarm.clear",backgroundColor: "#ff9999"
		}
		
		standardTile("playSiren2", "device.status", width: 2, height: 2) {
			state "default", label:'Siren 2', action:"siren2", icon:"st.security.alarm.clear",backgroundColor: "#ff9999"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
				
		main "status"
		details(["status", "playAlarm", "turnOff", "refresh", "playSiren1", "playSiren2", "playChime1", "playBell1", "playBell2", "playChime2", "playBell3", "playBell4", "playChime3", "playBell5", "battery"])
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
	
	def summary = ""
	
	// Chime Volume (param 4) must be changed before Siren Sound (param 5) because the device has a bug that causes param 5 to change every time param 4 is updated.
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.value, refreshAll)	
	}
			
	if (refreshAll) {
		cmds << switchBinaryGetCmd()
	}
		
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 1000)
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

def mute() { logUnsupportedCommand("mute()") }
def unmute() { logUnsupportedCommand("unmute()") }
def nextTrack() { logUnsupportedCommand("nextTrack()") }
def previousTrack() { logUnsupportedCommand("previousTrack()") }
private logUnsupportedCommand(cmdName) {
	logTrace "This device does not support the ${cmdName} command."
}
 
// Audio Notification Capability Commands
def playSoundAndTrack(URI, duration=null, track, volume=null) {	
	playText(URI, volume)
}
def playTrackAtVolume(URI, volume=null) {
	playText(URI, volume)
}
def playTrackAndResume(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playText(URI, volume)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playText(URI, volume)
}	
def playTextAndResume(message, volume=null) {
	playText(message, volume)
}	
def playTextAndRestore(message, volume=null) {
	playText(message, volume)
}
def playTrack(URI, volume=null) {
	playText(URI, volume)	
}
def speak(message) {
	playText(message, null)
}
def playText(message, volume=null) {
	logTrace "Executing playText($message)"
	def text = getTextFromTTSUrl(message) ?: message
	
	def sound = soundMessages.find { it.name == "${text?.toLowerCase()?.replace('_', '')}" }?.value
	
	return startChime(validateSound(sound ?: text, 1))
}

private getTextFromTTSUrl(URI) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	def urlPrefix2 = "http://s3.amazonaws.com/smartapp-media/sonos/"
	
	def text = ""	
	[urlPrefix, urlPrefix2].each {
		if (URI?.toString()?.toLowerCase()?.contains(it)) {
			text = URI.replace(it, "").replace(".mp3", "")
		}
	}	
	return text ?: URI
}


def pause() { return off() }
def stop() { return off() }
def off() {
	logDebug "Turning Off()"
	state.pendingSiren = false
	state.sirenStartTime = null
	return sirenToggleCmds(0x00)
}

def play() { return on() }
def on() {	
	logDebug "Playing On Chime (#${onChimeSoundSetting})"	
	addPendingSound("switch", "on")
	return chimePlayCmds(onChimeSoundSetting)
}

def beep() {
	def beepSound = 10
	logDebug "Beeping (#${beepSound})"
	if (state.pendingSiren) {	
		return [
			indicatorSetCmd(beepSound),
			"delay 1000",
			indicatorGetCmd()
		]
	}
	else {
		addPendingSound("status", "chime")
		return chimePlayCmds(beepSound)
	}
}

def bell1() { playText("bell1") }
def bell2() { playText("bell2") }
def bell3() { playText("bell3") }
def bell4() { playText("bell4") }
def bell5() { playText("bell5") }
def chime1() { playText("chime1") }
def chime2() { playText("chime2") }
def chime3() { playText("chime3") }
def siren1() { playText("siren1") }
def siren2() { playText("siren2") }

private startChime(sound) {	
	if (!state.sirenStartTime) {
		def val = validateSound(sound, 1)
		if ("${sound}" != "${val}") {
			logDebug "Playing Chime (#${val}) - (${sound} is not a valid sound number)"
		}
		else {
			logDebug "Playing Chime (#${val})"	
		}	
		addPendingSound("status", "chime")	
		return chimePlayCmds(val)
	}
	else {
		logDebug "Unable to start chime because alarm is on or pending."
	}
}

def strobe() { 
	return siren() 
}

def both() {
	def delayMS = (convertOptionSettingToInt(sirenDelayOptions, sirenDelaySetting) * 1000)
	
	if (delayMS != 0) {		
		state.pendingSound = []		
		sendEvent(name: "status", value: "pending")

		state.pendingSiren = true
		state.sirenStartTime = new Date().time + delayMS
		
		def result = []
		if (sirenDelayBeepSetting == "On") {
			result += beep()
		}		
		result << "delay ${delayMS}"
		result << basicGetCmd()
		return result
	}
	else {
		return siren()
	}
}

def siren() { 
	state.sirenStartTime = new Date().time
	state.pendingSiren = false
	logDebug "Turning on Siren (${sirenSoundSetting})"
	addPendingSound("alarm", "siren")
	
	def result = []
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
	logTrace "BatteryReport: $cmd"
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
	def name = configData.find { it.paramNum == cmd.parameterNumber }?.name
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (state.pendingSiren) {
		state.pendingSiren = false
		return response(siren())
	}
	else {
		return []
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def result = []	
	if (cmd.value == 0x00 && !state.pendingSiren) {
		result += handleDeviceOff()
	}
	else {
		// The on events are handled by either the indicator report handler or the notification report handler because they identify the action performed and not just the current state of the device.
		logTrace "Ignored 'on' switch binary event"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	def beepDelayMS = 2000
	if (state.pendingSiren) {
		if ((state.sirenStartTime - beepDelayMS) > new Date().time) {
			def result = []
			result << "delay ${beepDelayMS}"
			result += beep()
			return response(result)
		}
	}
	else {
		return handleDeviceOn(state.activeSound, state.pendingSound)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	
	if (cmd.notificationType == 14) {
		if (cmd.event == 0x01) {
			// Raises the alarm.both command when the siren is turned on, but ignores the chime because the chime on events get raised when the indicator report is received. 
			if (state.pendingSound?.name == "alarm") {
				result += handleDeviceOn(state.activeSound, state.pendingSound)
			}
			else if (!state.pendingSiren) {
				if (!state.pendingSound) {
					logTrace "Unable to handle 'on' notification event because the pending sound has not been set."
				}
				else {
					logTrace "Ignored 'on' notification event for chime."
				}
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
	state.pendingSiren = false
	state.sirenStartTime = null
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

private basicGetCmd() {
	return zwave.basicV1.basicGet().format()
}

private sirenToggleCmds(val) {
	def cmds = []
	
	if (val == 0x00) {
		cmds << indicatorSetCmd(0xFF)		
	}
	cmds += [
		switchBinarySetCmd(val),
		switchBinaryGetCmd()
	]
	
	return delayBetween(cmds, 100)		
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


// Configuration Parameters
private getConfigData() {
	// [paramNum: 6, name: "Chime Sound"]
	return [		
		[paramNum: 5, name: "Siren Sound", value: convertOptionSettingToInt(sirenSoundOptions, sirenSoundSetting)],
		[paramNum: 1, name: "Siren Volume", value: convertOptionSettingToInt(volumeOptions, sirenVolumeSetting)],
		[paramNum: 2, name: "Siren Length", value: convertOptionSettingToInt(sirenLengthOptions, sirenLengthSetting)],
		[paramNum: 8, name: "Siren LED", value: convertOptionSettingToInt(ledOptions, sirenLEDSetting)],
		[paramNum: 4, name: "Chime Volume", value: convertOptionSettingToInt(volumeOptions, chimeVolumeSetting)],
		[paramNum: 3, name: "Chime Repeat", value: convertOptionSettingToInt(chimeRepeatOptions, chimeRepeatSetting)],
		[paramNum: 9, name: "Chime LED", value: convertOptionSettingToInt(ledOptions, chimeLEDSetting)],
		[paramNum: 7, name: "Chime Mode", value: chimeModeSetting]
	]	
}

// Settings
private getReportBatteryEverySetting() {
	return safeToInt(settings?.reportBatteryEvery, 8)
}
private getSirenSoundSetting() {
	return settings?.sirenSound ?: findDefaultOptionName(sirenSoundOptions)
}
private getSirenVolumeSetting() {
	return settings?.sirenVolume ?: findDefaultOptionName(volumeOptions)
}
private getSirenLengthSetting() {
	return settings?.sirenLength ?: findDefaultOptionName(sirenLengthOptions)
}
private getSirenLEDSetting() {
	return settings?.sirenLED ?: findDefaultOptionName(ledOptions)
}
private getSirenDelaySetting() {
	return settings?.sirenDelay ?: findDefaultOptionName(sirenDelayOptions)
}
private getSirenDelayBeepSetting() {
	return settings?.sirenDelayBeep ?: findDefaultOptionName(sirenDelayBeepOptions)
}
// private getOnChimeSoundSetting() {
	// return validateSound(settings?.onChimeSound, 1)
// }
// private getBeepChimeSoundSetting() {
	 // return validateSound(settings?.beepChimeSound, 3)
// }
private getChimeVolumeSetting() {
	return settings?.chimeVolume ?: findDefaultOptionName(volumeOptions)
}
private getChimeRepeatSetting() {
	return settings?.chimeRepeat ?: findDefaultOptionName(chimeRepeatOptions)
}
private getChimeLEDSetting() {
	return settings?.chimeLED ?: findDefaultOptionName(ledOptions)
}
private getChimeModeSetting() {
	return 1 // Chime Mode should always be disabled.
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

private getSoundMessages() {
	[
		[name: "bell1", value: 1],
		[name: "bell2", value: 3],
		[name: "bell3", value:2],
		[name: "bell4", value:5],
		[name: "bell5", value:6],
		[name: "chime1", value:4],
		[name: "chime2", value:9],
		[name: "chime3", value:10],
		[name: "siren1", value:7],
		[name: "siren2", value:8],
		[name: "alarm", value: 7],
		[name: "the+mail+has+arrived", value: 4],
		[name: "a+door+opened", value: 9],
		[name: "there+is+motion", value: 10],
		[name: "smartthings+detected+a+flood", value: 8],
		[name: "smartthings+detected+smoke", value: 7],
		[name: "someone+is+arriving", value: 4],
		[name: "piano2", value: 2]
	]
}

private getChimeRepeatOptions() {
	def options = []
	options << [name: formatDefaultOptionName("1"), value: 1]
	(2..25).each { 
		options << [name: "$it", value: it]
	}
	return options
}

private getSirenSoundOptions() {
	[
		[name: "Alarm 1", value: 1],
		[name: "Alarm 2", value: 7],
		[name: "Alarm 3", value: 8],
		[name: formatDefaultOptionName("Alarm 4"), value: 9],
		[name: "Alarm 5", value: 10]
	]
}

private getVolumeOptions() { 
	[
		[name: "Low", value: 1],
		[name: formatDefaultOptionName("Medium"), value: 2],
		[name: "High", value: 3]
	]
}

private getSirenDelayOptions() { 
	[
		[name: formatDefaultOptionName("Off"), value: 0],
		[name: "15 Seconds", value: 15],
		[name: "30 Seconds", value: 30],
		[name: "60 Seconds", value: 60],
		[name: "90 Seconds", value: 90]
	]
}

private getLedOptions() {
	[
		[name: "Off", value: 0],
		[name: formatDefaultOptionName("On"), value: 1]
	]
}

private getSirenDelayBeepOptions() { 
	[
		[name: formatDefaultOptionName("Off"), value: 0],
		[name: "On", value: 1]
	]
}

private getSirenLengthOptions() {
	[
		[name: "30 Seconds", value: 1],
		[name: formatDefaultOptionName("1 Minute"), value: 2],
		[name: "5 Minutes", value: 3],
		[name: "${noLengthMsg}", value: 4] // config value is 255
	]
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


private getNoLengthMsg() { 
	return "Play until battery is depleted" 
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 1)
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
	// log.trace "$msg"
}