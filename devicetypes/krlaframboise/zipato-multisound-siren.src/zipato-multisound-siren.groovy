/**
 *  Zipato Multisound Siren v1.4.2
 *     (Zipato Z-Wave Indoor Multi-Sound Siren -
 *        Model:PH-PSE02.US)
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
		capability "Speech Synthesis"
		capability "Switch"
		capability "Tone"
		capability "Tamper Alert"
		capability "Refresh"

		attribute "status", "enum", ["off", "on", "alarm", "beep"]
		attribute "alarmState", "enum", ["enabled", "disabled"]

		command "playTrackAtVolume"
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
		
		def cmds = []
		if (!state.isConfigured) {
			state.useSecureCmds = false
			cmds += configure()			
		}
		else {
			logDebug "Secure Commands ${state.useSecureCmds ? 'Enabled' : 'Disabled'}"
			
			cmds << alarmDurationSetCmd()
			
			cmds += refresh()
		}		
		return response(cmds)
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

// Initializes variables and sends settings to device
def configure() {
	def cmds = []
	
	logDebug "Configuring ${state.useSecureCmds ? 'Secure' : 'Non-Secure'} Device"		
	
	cmds += delayBetween([
		alarmDurationSetCmd(),
		alarmDurationGetCmd(),
		disableAlarmSetCmd(false),
		disableAlarmGetCmd(),
		notificationTypeGetCmd(),
		basicGetCmd()
	], 200)
	
	if (!state.useSecureCmds) {
		cmds << supportedSecurityGetCmd()
	}
	
	return cmds
}

def refresh() {
	logDebug "Executing refresh()"
	
	logDebug "\nStrobe Sound: ${settings.strobeSound}\nSiren Sound: ${settings.sirenSound}\nBoth Sound: ${settings.bothSound}\nOn Sound: ${settings.switchOnSound}\nBeep Sound: ${settings.beepSound}"
	
	if (device.currentValue("tamper") != "clear") {
		sendEvent(getTamperEventMap("detected"))
	}
	
	delayBetween([
		alarmDurationGetCmd(),
		disableAlarmGetCmd(),
		notificationTypeGetCmd(),
		basicGetCmd()
	], 100)
}

// Audio Notification Capability Commands
def playSoundAndTrack(String URI, Number duration=0, String track, Number volume=0) {
	speak(URI)
}
def playText(String message, Number volume=0) {
	speak(message)
}
def playTextAndResume(String message, Number volume=0) {
	speak(message)
}	
def playTextAndRestore(String message, Number volume=0) {
	speak(message)
}
def playTrack(String URI, Number volume=0) {
	speak(URI)
}	
def playTrackAndResume(String URI, Number volume=0) {
	speak(URI)
}	
def playTrackAndRestore(String URI, Number volume=0) {
	speak(URI)
}	

// Documented as part of the Audio Notification capability
// but not actually part of it so it must be declared.
def playTrackAtVolume(String URI, Number volume) {
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
	logDebug "Executing enableAlarm()"	
	[
		disableAlarmSetCmd(false),
		"delay 200",
		disableAlarmGetCmd()
	]
}

def disableAlarm() {
	logDebug "Executing disableAlarm()"
	[
		disableAlarmSetCmd(true),
		"delay 200",
		disableAlarmGetCmd()
	]
}

def on() {
	logDebug "Executing on()"
	setPlayStatus("on", "off", "on")	
	return playSound(getSoundNumber(settings.switchOnSound))
}

def off() {
	logDebug "Executing off()"
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
	logDebug "Executing ${alarmType}()"	
	setPlayStatus("alarm", alarmType, "off")	
	playSound(getSoundNumber(soundName))
}

def beep() {
	logDebug "Executing beep()"	
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
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	soundName = (soundName == null) ? "" : soundName?.toString()?.toLowerCase()
	
	if (soundName?.contains(urlPrefix)) {
		soundName = soundName?.replace(urlPrefix,"").replace(".mp3","")
	}
	
	switch (soundName?.toString()?.toLowerCase()) {
		case ["stop", "off", "0"]:
			return 0
			break
		case ["emergency", "1", "255"]:
			return 1
			break
		case ["fire", "2"]:
			return 2
			break
		case ["ambulance", "3"]:
			return 3
			break
		case ["police", "4"]:
			return 4
			break
		case ["door", "5"]:
			return 5
			break
		case ["beep", "6"]:
			return 6
			break
		case ["chirp", "7"]:
			return 7
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
		result << basicSetCmd(1)
		result << "delay 100"
		result << basicSetCmd(0x00)
	}
	else if (state.playStatus?.status == "beep") {
		def beepCount = safeToInteger(settings.repeatBeepSound, 0)
		def repeatDelay = safeToInteger(settings.repeatBeepDelay, 2000)
		logInfo "Playing sound #$soundNumber $beepCount time(s)"
		for (int beep = 0; beep <= beepCount; beep++) {
			result << basicSetCmd(soundNumber)
			result << "delay $repeatDelay"
		}
	}
	else {
		logInfo "Playing Sound #$soundNumber"
		result << basicSetCmd(soundNumber)
	}
	state.lastSound = soundNumber	
	result << basicGetCmd()
	return result
}

def parse(String description) {	
	def result = null
	def cmd = zwave.parse(description, [0x71: 3, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1, 0x72: 2, 0x86: 1, 0x59: 1, 0x73: 1, 0x98: 1, 0x7A: 1, 0x5A: 1])
	
	if (cmd) {
		result = zwaveEvent(cmd)		
	}
	else {
		logDebug "Unknown Description: $description"
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand([0x71: 3, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1, 0x72: 2, 0x86: 1, 0x59: 1, 0x73: 1, 0x98: 1, 0x7A: 1, 0x5A: 1])	
	
	if (encapsulatedCmd) {
		return zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}

// Acknowledges secure commands and configures device using secure commands.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCmds = true
	logDebug "Secure Inclusion Detected"
	def result = []
	result += response(configure())
	return result	
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
	// This doesn't get called for the sounds "beep" or "door" so ignoring these events and using basic report instead.	
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
	logDebug "NotificationReport: $cmd"	
	if (cmd.notificationType == 7 && cmd.event == 3) {
		return createTamperEvent(cmd.notificationStatus)
	}	
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
}

private basicSetCmd(val) {	
	// 1 or 255: Emergency sound.
	// 2: Fire alert.
	// 3: Ambulance sound.
	// 4: Police car sound.
	// 5: Door chime.
	// 6~99: Beep Beep.
	// 0: means stop the sound.
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
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

private supportedSecurityGetCmd() {
	logDebug "Checking for Secure Command Support"	
	state.useSecureCmds = true // force secure cmd	
	def cmd = secureCmd(zwave.securityV1.securityCommandsSupportedGet())	
	state.useSecureCmds = false // reset secure cmd	
	return cmd
}

private secureCmd(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCmds) {		
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} 
	else {		
		return cmd.format()
	}
}

int validateRange(val, int defaultVal, int minVal, int maxVal) {
	def intVal = safeToInteger(val, defaultVal)
		
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

private int safeToInteger(val, int defaultVal=0) {
	try {
		val = "$val"
		if (val?.isFloat()) {
			return val.toFloat().round().toInteger()
		}
		else if (val?.isInteger()){
			return val.toInteger()
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return defaultVal
	}
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} $msg"
}
