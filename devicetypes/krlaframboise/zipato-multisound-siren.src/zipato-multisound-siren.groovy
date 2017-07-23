/**
 *  Zipato Multisound Siren v1.6.1
 *     (Zipato Z-Wave Indoor Multi-Sound Siren -
 *        Model:PH-PSE02)
 *  
 *https://community.smartthings.com/t/release-zipato-phileo-multisound-siren-ph-pse02-us/53748?u=krlaframboise
 *
 *  Capabilities:
 *	  Configuration, Alarm, Audio Notification, 
 *    Switch, Tone, Tamper Alert, Refresh
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *  1.6.1 (07/23/2017)
 *    	- Added legacy fingerprint support for security cc check.
 *
 *  1.6 (07/22/2017)
 *    	- Fixed issue caused by the hub firmware update 000.018.00018
 *    	- If you're on hub v1 or you installed the device prior to May 2016, make sure you test the device after updating to this version.
 *
 *  1.5.6 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *    	- Bug fix for location timezone issue.
 *
 *  1.5.5 (03/21/2017)
 *    - Fix for SmartThings TTS url changing.
 *
 *  1.5.4 (03/10/2017)
 *    - Improved health check
 *    - Removed polling capability.
 *
 *  1.5.3 (02/19/2017)
 *    - Minor bug fixes.
 *
 *  1.5.1 (02/18/2017)
 *    - Switched from basic to notification for playing sounds.
 *    - Added Health Check Capability with hourly self polling.
 *
 *  1.4.2 (08/06/2016)
 *    - Adjusted chirp timing resulting in 97% accuracy.
 *
 *  1.4 (08/05/2016)
 *    - Fixed UI issue with buttons sticking.
 *    - Added Beep Repeat and Beep Repeat Delay fields
 *      so that the Door and Beep sound can be played
 *      one or more times when beep button is pressed.
 *
 *  1.3.3 (08/01/2016)
 *    - Fix iOS value tile issue for enabled button
 *      and possible casting issue in the configreport method.
 *
 *  1.3 (07/28/2016)
 *    - Bug fixes
 *
 *  1.2 (07/28/2016)
 *    - Initial Release
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
	definition (name: "Zipato Multisound Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"
		capability "Alarm"
		capability "Audio Notification"
		capability "Music Player"
		capability "Speech Synthesis"
		capability "Switch"
		capability "Tone"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"

		attribute "status", "enum", ["off", "on", "alarm", "beep"]
		attribute "alarmState", "enum", ["enabled", "disabled"]
		attribute "lastCheckin", "string"

		command "playTrackAtVolume"
		command "playSoundAndTrack"
		command "enableAlarm"
		command "disableAlarm"
		
		fingerprint mfr: "013C", prod: "0004", model: "000A"
		
		fingerprint deviceId: "0x1005", inClusters: "0x71,0x20,0x25,0x85,0x70,0x72,0x86,0x30,0x59,0x73,0x5A,0x98,0x7A"
	}

	simulator {
		// reply messages
		reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
		reply "988100200100,9881002002": "command: 9881, payload: 00200300"
		reply "9881002001FF,delay 3000,988100200100,9881002002": "command: 9881, payload: 00200300"
	}
	
	preferences {
		input "sirenSound", "enum", 
			title: "Siren Sound:", 
			defaultValue: "Emergency", 
			displayDuringSetup: true,
			required: false,
			options: getSoundNames()
		input "strobeSound", "enum", 
			title: "Strobe Sound:", 
			defaultValue: "Emergency", 
			displayDuringSetup: true,
			required: false,
			options: getSoundNames()
		input "bothSound", "enum", 
			title: "Both Sound:", 
			defaultValue: "Emergency", 
			displayDuringSetup: true,
			required: false,
			options: getSoundNames()
		input "switchOnSound", "enum", 
			title: "Switch On Sound:", 
			defaultValue: "Emergency", 
			displayDuringSetup: true,
			required: false,
			options: getSoundNames()
		input "alarmDuration", "enum", 
			title: "Alarm Duration:", 
			defaultValue: "3 Minutes",
			displayDuringSetup: true, 
			required: false,
			options: ["30 Seconds", "1 Minute", "2 Minutes", "3 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "45 Minutes", "1 Hour", "Unlimited"]		
		input "beepSound", "enum", 
			title: "Beep Sound:", 
			defaultValue: "Beep", 
			displayDuringSetup: true,
			required: false,
			options: ["Beep","Door"]
		input "repeatBeepSound", "number", 
			title: "Repeat Beep Sound (0-20):", 
			defaultValue: "0",
			range: "0..20",
			displayDuringSetup: true, 
			required: false
		input "repeatBeepDelay", "number", 
			title: "Beep Delay in Milliseconds:", 
			defaultValue: "2000",
			displayDuringSetup: true, 
			required: false
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false			
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "off", 
					icon:"st.security.alarm.clear", 
					backgroundColor:"#ffffff"
				attributeState "on", 
					label:'On!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 					
					backgroundColor: "#99c2ff"					
				attributeState "alarm", 
					label:'Alarm!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 
					backgroundColor:"#ff9999"
				attributeState "beep", 
					label:'Beeping!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 
					backgroundColor:"#99FF99"
			}
		}
		
		standardTile("playSiren", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Siren', 
				action:"alarm.siren", 
				icon:"st.security.alarm.clear", 
				backgroundColor:"#ff9999"
			state "siren",
				label:'Turn Off',
				action:"alarm.off",
				icon:"st.alarm.alarm.alarm", 
				background: "#ffffff"	
		}
		
		standardTile("playStrobe", "device.alarm", width: 2, height: 2){
			state "default", 
				label:'Strobe', 
				action:"alarm.strobe", 
				icon:"st.security.alarm.clear", 
				backgroundColor:"#ff9999"
			state "strobe",
				label:'Turn Off',
				action:"alarm.off",
				icon:"st.alarm.alarm.alarm", 
				background: "#ffffff"	
		}
		
		standardTile("playBoth", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Both', 
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
				icon:"st.security.alarm.clear", 
				backgroundColor:"#99c2ff"
			state "on",
				label:'Turn Off',
				action:"switch.off",
				icon:"st.alarm.alarm.alarm", 
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
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("alarmState", "device.alarmState", width: 2, height: 2) {
			state "enabled", label:'Disable', action: "disableAlarm", icon:"st.custom.sonos.unmuted", backgroundColor: "#FFFFFF"
			state "disabled", label:'Enable', action: "enableAlarm", icon:"st.custom.sonos.muted", backgroundColor: "#cccccc"
		}
				
		main "status"
		details(["status", "playSiren", "playStrobe", "playBoth", "playOn", "playBeep", "alarmState", "refresh"])
	}
}

private getSoundNames() {
	[
		"Ambulance",
		"Beep",
		"Chirp",
		"Door",
		"Emergency",
		"Fire",
		"Police"
	]
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		initializeCheckin()
	
		def cmds = []
		if (!state.isConfigured) {
			cmds += configure()			
		}
		else {
			cmds << alarmDurationSetCmd()
			cmds += refresh()
		}		
		return sendResponse(cmds)
	}
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private initializeCheckin() {
	// Set the Health Check interval so that it can miss 2 checkins plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	switch (checkinIntervalSettingMinutes) {
		case 5:
			runEvery5Minutes(healthPoll)
			break
		case 10:
			runEvery10Minutes(healthPoll)
			break
		case 15:
			runEvery15Minutes(healthPoll)
			break
		case 30:
			runEvery30Minutes(healthPoll)
			break
		case [60, 120]:
			runEvery1Hour(healthPoll)
			break
		default:
			runEvery3Hours(healthPoll)			
	}
}

def healthPoll() {
	logTrace "healthPoll()"	
	sendHubCommand(new physicalgraph.device.HubAction(versionGetCmd()))
}

def ping() {
	logTrace "ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return versionGetCmd()
	}	
}

// Initializes variables and sends settings to device
def configure() {
	def cmds = []
	
	cmds += delayBetween([
		alarmDurationSetCmd(),
		alarmDurationGetCmd(),
		disableAlarmSetCmd(false),
		disableAlarmGetCmd(),
		notificationTypeGetCmd(),
		basicGetCmd()
	], 500)
	
	return cmds
}

def refresh() {
	logTrace "Executing refresh()"
	
	logDebug "\nStrobe Sound: ${settings.strobeSound}\nSiren Sound: ${settings.sirenSound}\nBoth Sound: ${settings.bothSound}\nOn Sound: ${settings.switchOnSound}\nBeep Sound: ${settings.beepSound}"
	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getTamperEventMap("clear"))
	}
	
	delayBetween([
		alarmDurationGetCmd(),
		disableAlarmGetCmd(),
		notificationTypeGetCmd(),
		basicGetCmd()
	], 250)
}

// Unsuported Music Player commands
def unmute() { handleUnsupportedCommand("unmute") }
def nextTrack() { handleUnsupportedCommand("nextTrack") }
def setLevel(number) { handleUnsupportedCommand("setLevel") }
def previousTrack() { handleUnsupportedCommand("previousTrack") }


private handleUnsupportedCommand(cmd) {
	log.info "Command $cmd is not supported"
}

// Turns siren off
def pause() { off() }
def stop() { off() }
def mute() { off() }

// Turns siren on
def play() { both() }

// Audio Notification Capability Commands
def playSoundAndTrack(URI, duration=null, track=null, volume=null) {
	speak(URI)
}
def playText(message, volume=null) {
	speak(message)
}
def playTextAndResume(message, volume=null, otherVolume=null) {
	speak(message)
}	
def playTextAndRestore(message, volume=null, otherVolume=null) {
	speak(message)
}
def playTrack(URI, volume=null) {
	speak(URI)
}	
def playTrackAndResume(URI, volume=null, otherVolume=null) {
	speak(URI)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	speak(URI)
}	

// Documented as part of the Audio Notification capability
// but not actually part of it so it must be declared.
def playTrackAtVolume(URI, volume) {
	speak(URI)
}

def speak(text) {	
	def status
	def soundNumber = getSoundNumber(text)
		
	switch (soundNumber) {
		case 1..5:
			status = "alarm"
			break
		case 6..7:
			status = "beep"
			break			
		default:
			status = "off"
	}
	
	setPlayStatus(status, "off", "off")
	logDebug "Executing speakText($text)"
	playSound(soundNumber)
}

def enableAlarm() {
	logTrace "Executing enableAlarm()"	
	[
		disableAlarmSetCmd(false),
		"delay 200",
		disableAlarmGetCmd()
	]
}

def disableAlarm() {
	logTrace "Executing disableAlarm()"
	[
		disableAlarmSetCmd(true),
		"delay 200",
		disableAlarmGetCmd()
	]
}

def on() {
	logTrace "Executing on()"
	setPlayStatus("on", "off", "on")	
	return playSound(getSoundNumber(settings.switchOnSound))
}

def off() {
	logTrace "Executing off()"
	setPlayStatus("off", "off", "off")	
	[
		basicSetCmd(0x00),
		basicGetCmd()
	]
}

def strobe() {
	playAlarm(settings.strobeSound, "strobe")	
}

def siren() {
	playAlarm(settings.sirenSound, "siren")	
}

def both() {	
	playAlarm(settings.bothSound, "both")
}

private playAlarm(soundName, alarmType) {
	logTrace "Executing ${alarmType}()"	
	setPlayStatus("alarm", alarmType, "off")	
	playSound(getSoundNumber(soundName))
}

def beep() {
	logTrace "Executing beep()"	
	setPlayStatus("beep", "off", "off")	
	playSound(getSoundNumber(settings.beepSound))
}

private setPlayStatus(statusVal, alarmVal, switchVal) {
	state.playStatus = [
		status: statusVal, 
		alarm: alarmVal, 
		switch: switchVal
	]
}

private getSoundNumber(soundName) {
	soundName = (soundName == null) ? "" : soundName?.toString()?.toLowerCase()
	
	if (soundName?.contains("/")) {
		def startIndex = soundName.lastIndexOf("/") + 1
		soundName = soundName.substring(startIndex, soundName.size())?.toLowerCase()?.replace(".mp3","")
	}
	
	soundName = soundName?.toString()?.toLowerCase()
	
	switch (soundName) {
		case ["stop", "off", "0"]:
			return 0
			break
		case ["emergency", "1", "255"]: // 0x01, 0x07
			return 1
			break
		case ["fire", "2"]: // 0x02, 0x0A
			return 2
			break
		case ["ambulance", "3"]: // 0x03, 0x0A
			return 3
			break
		case ["police", "4"]: // 0x01, 0x0A
			return 4
			break
		case ["door", "5"]: // 0x16, 0x06
			return 5
			break
		case ["beep", "6"]: // 0x05, 0x0A
			return 6
			break
		case ["chirp", "7"]:
			return 7
			break
		case ["siren", "strobe", "both"]:
			return getSoundNumber(settings?."${soundName}Sound")
			break
		case "on":
			return getSoundNumber(settings?.switchOnSound)
			break
		default:
			return 1
	}
}

private playSound(soundNumber) {
	def result = []

	soundNumber = validateRange(soundNumber, 1, 1, 7)	
	if (soundNumber == 7) {
		logInfo "Chirping"
		result << notificationReportCmd(1)		
		result << "delay 100"
		result << basicSetCmd(0x00)
	}
	else if (state.playStatus?.status == "beep") {
		def beepCount = safeToInt(settings.repeatBeepSound, 0)
		def repeatDelay = safeToInt(settings.repeatBeepDelay, 2000)
		logInfo "Playing sound #$soundNumber $beepCount time(s)"
		for (int beep = 0; beep <= beepCount; beep++) {
			result << notificationReportCmd(soundNumber)
			result << "delay $repeatDelay"
		}
	}
	else {
		logInfo "Playing Sound #$soundNumber"
		result << notificationReportCmd(soundNumber)
	}
	state.lastSound = soundNumber	
	result << basicGetCmd()
	return result
}

def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)		
	}
	else {
		log.warn "Unable to parse: $description"
	}
		
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		result << createLastCheckinEvent()
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)	
	
	def result = []
	if (encapsulatedCmd) {
		result += zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}

private getCommandClassVersions() {
	[
		0x25: 1,	// Switch Binary
		0x30: 2,  // Sensor Binary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // Firmware Update Md
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x98: 1   // Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def result = []
	def parameterName
	def configVal = cmd.configurationValue ? cmd.configurationValue[0] : null
	def displayVal = "$configVal"
	
	state.isConfigured = true
	
	switch (cmd.parameterNumber) {
		case 7:
			parameterName = "Notification Type"
			displayVal = (configVal == 0) ? "Notification Report" : "Sensor Binary Report"						
			break
		case 29:
			parameterName = "Alarm State"
			displayVal = (configVal == 0) ? "enabled" : "disabled"
			result << createEvent(name: "alarmState", value: displayVal, displayed: (device.currentValue("alarmState") != displayVal), descriptionText: "Alarm is $displayVal")
			break
		case 31:
			parameterName = "Alarm Duration"
			displayVal = (configVal == 0) ? "Unlimited" : "${configVal * 30} Seconds"			
			break
		default:	
			parameterName = "Parameter #${cmd.parameterNumber}"
	}		
	if (parameterName) {
		logDebug "${parameterName}: ${displayVal}"
	} 
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {	
	def result = []
	def forceEvent = false
	
	if (cmd.value == 0 && device.currentValue("status") == "off" && state.lastSound >= 5) {
		// Door Sound or Beep Sound played and they don't
		// turn on the device so force the on event.
		state.lastSound = null
		result += createStatusEvents(0xFF, false)
		forceEvent = true
	}
	result += createStatusEvents(cmd.value, forceEvent)
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	// Ignoring these reports.	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
}

private createLastCheckinEvent() {
	state.lastCheckinTime = new Date().time
	logDebug "Device Checked In"	
	return createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
}

def createStatusEvents(val, forceEvent) {
	def newSwitch
	def newAlarm
	def newStatus = val
	def currentPlayStatus = state.playStatus
	def result = []
	
	if (val == 0x00) {
		logDebug "${device.displayName} turned off"
		
		newStatus = (device.currentValue("status") != "off" || canForceOffEvent(forceEvent, currentPlayStatus?.status)) ? "off" : null 
		
		newAlarm = (device.currentValue("alarm") != "off" || canForceOffEvent(forceEvent, currentPlayStatus?.alarm)) ? "off" : null
		
		newSwitch = (device.currentValue("switch") != "off" || canForceOffEvent(forceEvent, currentPlayStatus?.switch)) ? "off" : null		
	}
	else {
		logDebug "${device.displayName} turned on"		
		if (!currentPlayStatus) {
			currentPlayStatus = [status: "alarm", alarm: "off",switch: "off"]
		}
		newStatus = currentPlayStatus.status
		newAlarm = (device.currentValue("alarm") != currentPlayStatus.alarm) ? currentPlayStatus.alarm : null
		newSwitch = (device.currentValue("switch") != currentPlayStatus.switch) ? currentPlayStatus.switch : null
	}
		
	if (newStatus) {
		result << createEvent(name: "status", value: newStatus, displayed: (device.currentValue("status") != newStatus), isStateChange: true)		
	}
	
	if (newAlarm) {
		result << createEvent(name: "alarm", value: newAlarm, displayed: false, isStateChange: true)
	}
	
	if (newSwitch) {
		result << createEvent(name: "switch", value: newSwitch, displayed: false, isStateChange: true)
	}
	
	return result
}

private canForceOffEvent(forceEvent, playStatusVal) {
	return (forceEvent && playStatusVal != null && playStatusVal != "off")
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	logDebu "NotificationReport: $cmd"	
	if (cmd.notificationType == 7 && cmd.event == 3) {
		result << createTamperEvent(cmd.notificationStatus)
	}	
	return result
}

def createTamperEvent(val) {
	def tamperState = (val == 0xFF) ? "detected" : "clear"
	if (device.currentValue("tamper") != tamperState) {
		logDebug "Tamper is $tamperState"
		return createEvent(getTamperEventMap(tamperState))
	}
}

def getTamperEventMap(val) {	
	def isNew = device.currentValue("tamper") != val
	[
		name: "tamper", 
		value: val, 
		displayed: isNew,
		descriptionText: "Tamper is $val"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}

private basicSetCmd(val) {	
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private notificationReportCmd(sound) {
	return secureCmd(zwave.notificationV3.notificationReport(event: getSoundEvent(sound), notificationType: getSoundNotificationType(sound)))
}

private getSoundEvent(sound) {
	def result
	switch (sound) {
		case 2:
			result = 0x02			
			break
		case 3:
			result = 0x03
			break
		case 5:
			result = 0x16
			break
		case 6:
			result = 0x05
			break
		default:
			result = 0x01
	}
	return result
}

private getSoundNotificationType(sound) {
	def result
	switch (sound) {
		case 1:
			result = 0x07
			break
		case 5:
			result = 0x06
			break
		default:
			result = 0x0A
	}
	return result
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private alarmDurationSetCmd() {	
	//(0 - 127)
	//0: disabled
	//1: 30 seconds
	//6: 3 Minutes (default)
	//127: 63.5 Minutes (max)	
	def val = validateRange(getAlarmDurationNumber(settings.alarmDuration), 6, 0, 127)
	return configSetCmd(31, val)
}

private getAlarmDurationNumber(val) {
	switch(val) {
		case "Unlimited":
			return 0
			break
		case "30 Seconds":
			return 1
			break
		case "1 Minute":
			return 2
			break
		case "2 Minutes":
			return 4
			break
		case "3 Minutes":
			return 6
			break
		case "5 Minutes":
			return 10
			break
		case "10 Minutes":
			return 20
			break
		case "15 Minutes":
			return 30
			break
		case "30 Minutes":
			return 60
			break
		case "45 Minutes":
			return 90
			break
		case "1 Hour":
			return 120
			break
		default:
			return 6 // 3 Minutes
	}	
}

private alarmDurationGetCmd() {
	return configGetCmd(31)
}

private disableAlarmGetCmd() {
	return configGetCmd(29)
}

private disableAlarmSetCmd(disable) {
	return configSetCmd(29, disable ? 1 : 0)
}

private notificationTypeGetCmd() {
	return configGetCmd(7)
}

private configSetCmd(paramNumber, configValue) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, scaledConfigurationValue: configValue))
}

private configGetCmd(paramNumber) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getCheckinIntervalOptions() {
	[
		[name: "5 Minutes", value: 5],
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

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

int validateRange(val, int defaultVal, int minVal, int maxVal) {
	def intVal = safeToInt(val, defaultVal)
		
	if ("$val" != "$intVal") {
		logDebug "Using $defaultVal because $val is invalid"
		return defaultVal
	}
	else if (intVal > maxVal) {
		logDebug "Using $maxVal because $intVal is too high"
		return maxVal
	}
	else if (intVal < minVal) {
		logDebug "Using $minVal because $intVal is too low"
		return minVal
	}
	else {
		return intVal
	}
}

private int safeToInt(val, int defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "$msg"
}

private logTrace(msg) {
	// log.trace "${msg}"
}