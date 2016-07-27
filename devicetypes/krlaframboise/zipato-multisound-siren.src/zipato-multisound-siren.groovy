/**
 *  Zipato Multisound Siren v1.0 (Alpha)
 *  (PH-PSE02.US)
 *  Zipato Z-Wave Indoor Multi-Sound Siren (PH-PSE02.US)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    
 *
 *  Changelog:
 *
 *  1.0.0 (07/27/2016)
 *    - Alpha Release
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
		capability "Alarm"
		capability "Audio Notification"
		capability "Switch"
		capability "Tone"
		capability "Tamper Alert"
		capability "Refresh"

		attribute "status", "enum", ["off", "on", "alarm", "beep"]
				
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
		input "beepRepeat", "number", 
			title: "Beep Repeat:", 
			defaultValue: 1, 
			range: "1..93",
			displayDuringSetup: true, 
			required: false
		input "alarmDuration", "enum", 
			title: "Alarm Duration:", 
			defaultValue: "3 Minutes",
			displayDuringSetup: true, 
			required: false,
			options: ["30 Seconds", "1 Minute", "2 Minutes", "3 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "45 Minutes", "1 Hour", "Unlimited"]			
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
					icon:"st.alarm.alarm.alarm", 
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
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#ff9999"
			state "siren",
				label:'Off',
				action:"alarm.off",
				icon:"",
				background: "#ffffff"	
		}
		
		standardTile("playStrobe", "device.alarm", width: 2, height: 2){
			state "default", 
				label:'Strobe', 
				action:"alarm.strobe", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#ff9999"
			state "strobe",
				label:'Off',
				action:"alarm.off",
				icon:"",
				background: "#ffffff"	
		}
		
		standardTile("playBoth", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Both', 
				action:"alarm.both", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#ff9999"
			state "both",
				label:'Off',
				action:"alarm.off",
				icon:"",
				background: "#ffffff"	
		}
		
		standardTile("playOn", "device.switch", width: 2, height: 2) {
			state "default", 
				label:'On', 
				action:"switch.on", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#99c2ff"
			state "on",
				label:'Off',
				action:"switch.off",
				icon:"",
				background: "#ffffff"	
		}
		
		standardTile("playBeep", "device.status", width: 2, height: 2) {
			state "default", 
				label:'Beep', 
				action:"tone.beep", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor: "#99FF99"
			state "beep",
				label:'Off',
				action:"off",
				icon:"st.Entertainment.entertainment2", 
				background: "#ffffff"	
		}
		
		standardTile("turnOff", "device.off", width: 2, height: 2) {
			state "default", label:'Off', action:"switch.off", icon:""
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'', action: "refresh", icon:"st.secondary.refresh"
		}
				
		main "status"
		details(["status", "playSiren", "playStrobe", "playBoth", "playOn", "playBeep", "turnOff", "refresh"])		
	}
}

private getSoundNames() {
	[
		"Ambulance",
		"Beep",
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
		alarmEnabledGetCmd(),
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
	
	logDebug "\nStrobe Sound: ${settings.strobeSound}\nSiren Sound: ${settings.sirenSound}\nBoth Sound: ${settings.bothSound}\nOn Sound: ${settings.switchOnSound}\nBeep Repeat: ${settings.beepRepeat}"
	
	delayBetween([
		alarmDurationGetCmd(),
		alarmEnabledGetCmd(),
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
def playTrackAtVolume(String URI, Number volume) {
	speak(URI)
}

// Speech Synthesis Commands
def speak(text) {	
	def status
	def soundNumber = getSoundNumber(text)
		
	switch (soundNumber) {
		case 1..5:
			status = "alarm"
			break
		case 6..99:
			status = "beep"
			break
		default:
			status = "off"
	}
	
	setPlayStatus(status, "off", "off")
	logDebug "Executing speakText($text)"
	playSound(soundNumber)
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
	logDebug "Executing strobe()"
	playAlarm(settings.strobeSound, "strobe")
}

def siren() {
	logDebug "Executing siren()"
	playAlarm(settings.sirenSound, "siren")
}

def both() {	
	logDebug "Executing both()"
	playAlarm(settings.bothSound, "both")
}

private playAlarm(soundName, alarmType) {
	setPlayStatus("alarm", alarmType, "off")
	logDebug "Executing ${alarmType}()"	
	playSound(getSoundNumber(soundName))
}

def beep() {
	logDebug "Executing beep()"	
	setPlayStatus("beep", "off", "off")	
	playSound(getSoundNumber("beep"))
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
		case { it?.startsWith("beep") }:
			return getBeepSoundNumber(soundName)
			break
		default:
			return 1
	}
}

private getBeepSoundNumber(soundName) {
	def beepRepeat = 1
	
	soundName = soundName?.toLowerCase()
	if (soundName == "beep") {
		beepRepeat = validateRange(settings.beepRepeat, 1, 1, 94)
	}
	else if (soundName?.startsWith("beep ")) {
		beepRepeat = validateRange(soundName.replace("beep ", ""), 1, 1, 94)		
	}
	
	return (beepRepeat + 5) // beep sound number range (6-99)
}

private playSound(soundNumber) {
	soundNumber = validateRange(soundNumber, 1, 0, 99)	
	if (soundNumber == 0) {
		logInfo "Stopping Sound"
	}
	else {
		logInfo "Playing Sound #$soundNumber"
	}		
	return [
		basicSetCmd(soundNumber)
	]	
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def parameterName
	def configVal = cmd.configurationValue ? cmd.configurationValue[0] : null
	
	state.isConfigured = true
	
	switch (cmd.parameterNumber) {
		case 7:
			parameterName = "Notification Type"
			configVal = (configVal == 0) ? "Notification Report" : "Sensor Binary Report"						
			break
		case 29:
			parameterName = "Alarm Enabled"
			configVal = (configVal == 0) ? "Yes" : "No"			
			break
		case 31:
			parameterName = "Alarm Duration"
			configVal = (configVal == 0) ? "Unlimited" : "${configVal * 30} Seconds"			
			break
		default:	
			parameterName = "Parameter #${cmd.parameterNumber}"
	}		
	if (parameterName) {
		logDebug "${parameterName}: ${configVal}"
	} 
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	def result = null
	switch(cmd.sensorType) {
		case 1:
			result = createStatusEvents(cmd.sensorValue)
			break
		case 8:
			result = createTamperEvent(cmd.sensorValue)
			break
		default:
			logDebug "SensorBinaryReport: $cmd"
	}
	return result
}

def createStatusEvents(val) {
	def newSwitch
	def newAlarm
	def newStatus = val
	def currentPlayStatus = state.playStatus
	def result = []
	
	if (val == 0x00) {
		newStatus = (device.currentValue("status") != "off") ? "off" : null 
		newAlarm = (device.currentValue("alarm") != "off") ? "off" : null
		newSwitch = (device.currentValue("switch") != "off") ? "off" : null
	}
	else {
		if (!currentPlayStatus) {
			currentPlayStatus = [status: "alarm", alarm: "off",switch: "off"]
		}
		newStatus = currentPlayStatus.status
		newAlarm = (device.currentValue("alarm") != currentPlayStatus.alarm) ? currentPlayStatus.alarm : null
		newSwitch = (device.currentValue("switch") != currentPlayStatus.switch) ? currentPlayStatus.switch : null
	}
	
	// logDebug "\nPlay Status: ${currentPlayStatus}\nCurrent State: [alarm:${device.currentValue('alarm')}, status:${device.currentValue('status')}, switch:${device.currentValue('switch')}]\nNew State: [alarm:$newAlarm, status:$newStatus, switch:$newSwitch]"
	
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

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "NotificationReport: $cmd"	
	if (cmd.notificationType == 7 && cmd.event == 3) {
		return createTamperEvent(cmd.notificationStatus)
	}
	else {
		return createTamperEvent(0x00)
	}
}

def createTamperEvent(val) {
	def tamperState = (val == 0xFF) ? "detected" : "clear"
	logDebug "createTamperEvent($tamperState)"
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

private alarmEnabledGetCmd() {
	return configGetCmd(29)
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
