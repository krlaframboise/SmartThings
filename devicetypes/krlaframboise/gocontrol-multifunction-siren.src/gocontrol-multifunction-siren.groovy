/**
 *  GoControl Multifunction Siren v 1.2
 *  
 *  Capabilities:
 *      Alarm, Tone, Switch, Battery, Polling
 *      Speech Synthesis, Music Player
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *  Url to Documentation:
 *      https://community.smartthings.com/t/release-gocontrol-linear-multifunction-siren/47024?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.2 (06/12/2016)
 *      - *** BREAKING CHANGES ***
 *            - Removed strobe instead of beep option from settings.
 *            - Removed always use both/on option from settings.
 *            - Removed beep(length) command, but it still works 
 *              in the speakText or playText fields.
 *      - Added settings for default alarm delay time and
 *        default strobe during delay.
 *      - Added commands customAlarm, customSiren, customBoth
 *      - Made it check the current alarmtype and only change it
 *        as needed which prevents it from always flashing before
 *        turning on.
 *      - Misc bug fixes.
 *
 *    1.1 (05/21/2016)
 *      - Improved polling functionality.
 *
 *    1.0.3 (05/04/2016 - 05/12/2016)
 *      - Enhanced reporting of status, alarm, and switch state.
 *      - Enhanced activity feed messages.
 *      - Enhanced debug logging.
 *      - Improved beep reliability a little bit.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "GoControl Multifunction Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Music Player"
		capability "Polling"
		capability "Speech Synthesis"
		capability "Switch"
		capability "Tone"
		
		attribute "lastPoll", "number"

		command "customBeep" // beepLengthMS
		command "customBoth" // delaySeconds, autoOffSeconds, useStrobe
		command "customSiren" // delaySeconds, autoOffSeconds, useStrobe
		command "customStrobe" // delaySeconds, autoOffSeconds
		
		// Music and Sonos Related Commands
		command "playSoundAndTrack"
		command "playTrackAndRestore"
		command "playTrackAndResume"
		command "playTextAndResume"
		command "playTextAndRestore"

		fingerprint deviceId: "0x1000", inClusters: "0x25,0x80,0x70,0x72,0x86"
	}

	simulator {
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
	}

	preferences {
		input "autoOffTime", "enum", 
			title: "Automatically turn off after:\n(You should disable this feature while testing)", 
			defaultValue: "30 Seconds",
			displayDuringSetup: true, 
			required: false,
			options: ["30 Seconds", "60 Seconds", "120 Seconds", "Disable Auto Off"]
		input "beepLength", "number", 
			title: "Length of Beep in Milliseconds", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "bothAlarmTypeOverride", "enum",
			title: "What should the 'both' and 'on' commands turn on?\n(Some SmartApps like Smart Home Monitor use the both command so you can't use just the siren or the strobe.  This setting allows you to override the default action of those commands.)",
			defaultValue: "Siren and Strobe",
			displayDuringSetup: true,
			required: false,
			options: ["Siren and Strobe", "Siren Only", "Strobe Only"]
		input "alarmDelaySeconds", "number", 
			title: "Alarm Delay in Seconds", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "alarmDelayStrobe", "bool", 
			title: "Strobe during alarm delay?", 
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
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'Off', action: "alarm.off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "turningOff", label:'Turning Off', action: "alarm.off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "alarmPending", label:'Alarm Pending!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "siren", label:'Siren On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "alarm.off", nextState: "turningOff", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"							
			}
		}
		valueTile("off", "device.status", label: '', width: 2, height: 2) {
			state "off", label:'Off', action: "alarm.off", icon:"", backgroundColor: "#ffffff"
			state "turningOff", label:'Turning Off', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#ffffff"			
			state "siren", label:'Turn Off', action: "alarm.off", nextState: "turningOff", icon:"", backgroundColor: "#99c2ff"
			state "strobe", label:'Turn Off', action: "alarm.off", nextState: "turningOff", icon:"", backgroundColor: "#99c2ff"
			state "both", label:'Turn Off', action: "alarm.off", nextState: "turningOff", icon:"", backgroundColor: "#99c2ff"
			state "beep", label:'Turn Off', action: "alarm.off", nextState: "turningOff", icon:"", backgroundColor: "#99c2ff"
			state "alarmPending", label:'Cancel Alarm', action: "alarm.off", nextState: "turningOff", icon:"", backgroundColor: "#99c2ff"
		}	
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'Battery ${currentValue}%', unit:""
		}		
		valueTile("testBeep", "device.status", label: 'beep', width: 2, height: 2) {
			state "default", label:'Test Beep', action:"tone.beep", nextState: "turningOn", icon:"", backgroundColor: "#99FF99"
			state "turningOn", label:'Turning On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"
			state "beep", label:'Beeping', action: "alarm.off", nextState: "alarm.off", icon:"", backgroundColor: "#99c2ff"
		}	
				
		valueTile("testSiren", "device.status", label: 'Siren', width: 2, height: 2) {
			state "off", label:'Test Siren', action: "alarm.siren", nextState: "turningOn", icon:"", backgroundColor: "#ff9999"
			state "turningOn", label:'Turning On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"
			state "siren", label:'Siren On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"		
		}
		valueTile("testStrobe", "device.status", label: 'Strobe', width: 2, height: 2) {
			state "default", label:'Test Strobe', action: "alarm.strobe", nextState: "turningOn", icon:"", backgroundColor: "#ff9999"
			state "turningOn", label:'Turning On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"
			state "strobe", label:'Strobe On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"
		}
		valueTile("testBoth", "device.status", label: 'Both', width: 2, height: 2) {
			state "default", label:'Test Both', action: "alarm.both", nextState: "turningOn", icon:"", backgroundColor: "#ff9999"
			state "turningOn", label:'Turning On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"
			state "both", label:'Both On', action: "alarm.off", nextState: "off", icon:"", backgroundColor: "#99c2ff"						
		}		
				
		main "status"
		details(["status", "testSiren", "testStrobe", "testBoth", "off", "testBeep", "battery"])
	}
}

// Stores preferences and displays device settings.
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		state.debugOutput = validateBoolean(settings.debugOutput, true)
		state.alarmDelaySeconds = validateRange(settings.alarmDelaySeconds, 0, 0, Integer.MAX_VALUE, "alarmDelaySeconds") 
		state.alarmDelayStrobe = validateBoolean(settings.alarmDelayStrobe, false)
		logDebug "Updating"

		def cmds = []		
		cmds << autoOffSetCmd(getAutoOffTimeValue())
		cmds << batteryGetCmd()
		cmds += turnOff()		
		response(delayBetween(cmds, 20))
	}
}

private getAutoOffTimeValue() {
	def result	
	switch (settings.autoOffTime) {
		case "60 Seconds":
			state.autoOffSeconds = 60
			result = 1
			break
		case "120 Seconds":
			state.autoOffSeconds = 120
			result = 2
			break
		case "Disable Auto Off":
			state.autoOffSeconds = null
			result = 3
			break
		default:
			state.autoOffSeconds = 30
			result = 0 // 30 Seconds
	}
	return result
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def poll() {
	def result = []
	def minimumPollMinutes = 30
	def lastPoll = device.currentValue("lastPoll")
	if ((new Date().time - lastPoll) > (minimumPollMinutes * 60 * 1000)) {
		logDebug "Poll: Refreshing because lastPoll was more than ${minimumPollMinutes} minutes ago."
		result << batteryGetCmd()
	}
	else {
		logDebug "Poll: Skipped because lastPoll was within ${minimumPollMinutes} minutes"		
	}
	return result
}

// Turns on siren and strobe
def on() {
	both()	
}

def beep() {
	customBeep(settings.beepLength)
}

// Turns on and then off after specified milliseconds.
def customBeep(beepLengthMS) {	
	beepLengthMS = validateRange(beepLengthMS, 0, 0, Integer.MAX_VALUE, "Beep Length")
	
	logDebug "Executing ${beepLengthMS} Millisecond Beep"
	
	def result = []	
	result += alarmTypeSetCmds(getSirenOnlyAlarmType())
	result << "delay 200"
	result << switchOnSetCmd()
	
	if (beepLengthMS > 0) {
		result << "delay $beepLengthMS"
	}
	result += switchOffSetCmds()
		
	sendEvent(getStatusEventMap("beep"))
	
	return result	
}

// Turns on siren and strobe using default autooff
def both() {	
	customBoth(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren and strobe using specified auto off, delay and whether or not it should strobe during delay.
def customBoth(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenAndStrobeAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Turns on strobe using default autooff
def strobe() {
	customStrobe(state.alarmDelaySeconds, state.autoOffSeconds)	
}

// Turn on strobe using specified autooff.
def customStrobe(delaySeconds, autoOffSeconds) {	
	turnOn(getStrobeOnlyAlarmType(), delaySeconds, autoOffSeconds, false)
}

// Turns on siren using default auto off
def siren() {
	customSiren(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren using specified auto off, delay and whether or not it should strobe during delay.
def customSiren(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenOnlyAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Stores a map with the alarm settings and requests the
// current alarmType from the device.
def turnOn(alarmType, delaySeconds, autoOffSeconds, useStrobe) {
	delaySeconds = validateRange(delaySeconds, 0, 0, Integer.MAX_VALUE, "delaySeconds")
	autoOffSeconds = validateRange(autoOffSeconds, 0, 0, Integer.MAX_VALUE, "autoOffSeconds")
	useStrobe = validateBoolean(useStrobe, false)
	
	state.activeAlarm = [
		alarmType: alarmType,
		autoOffSeconds: autoOffSeconds,
		delaySeconds: delaySeconds,
		useStrobe: useStrobe
	]
	
	def result = []
	if (delaySeconds > 0 && !useStrobe) {
		result += startDelayedAlarm()
	}
	// else if (validateBoolean(settings.alwaysSetAlarmType, false)) {
		// result += turnOn(null)
	// }
	else {
		result << alarmTypeGetCmd()
	}
	return result
}

def turnOn(currentAlarmType) {
	def result = []
	def activeAlarm = state.activeAlarm
	
	if (activeAlarm) {	
		if (activeAlarm.delaySeconds > 2 && activeAlarm.useStrobe) {			
			logDebug "Turning on strobe for ${activeAlarm.delaySeconds} seconds"
			
			if (getStrobeOnlyAlarmType() != currentAlarmType) {
				result += alarmTypeSetCmds(getStrobeOnlyAlarmType())
			}
			result << switchOnSetCmd()
			result += startDelayedAlarm()
			result << zwave.basicV1.basicSet(value: 0x00).format()
		}
		else {
			state.activeAlarm.alarmPending = false
			
			if (activeAlarm.alarmType != currentAlarmType) {
				result += alarmTypeSetCmds(activeAlarm.alarmType)
			}
			
			result << switchOnSetCmd()
			result << switchGetCmd()

			if (activeAlarm.autoOffSeconds) {
				logDebug "Turning on Alarm for ${activeAlarm.autoOffSeconds} seconds."
				result << "delay ${activeAlarm.autoOffSeconds * 1000}"
				result += switchOffSetCmds()			
			}	
			else {
				logDebug "Turning on Alarm"
			}
		}
	}
	return delayBetween(result, 100)
}

private startDelayedAlarm() {
	def result = []
	def delaySeconds = state.activeAlarm.delaySeconds
	
	result << "delay ${delaySeconds * 1000}"
	result << alarmTypeGetCmd()
	
	logDebug "Alarm delayed by ${delaySeconds} seconds"
	sendEvent(getStatusEventMap("alarmPending"))
	
	state.activeAlarm.delaySeconds = null
	state.activeAlarm.useStrobe = null
	state.activeAlarm.alarmPending = true
	
	return result
}

// Turns off siren and strobe
def off() {
	logDebug "Executing off() command"
	turnOff()
}

private turnOff() {
	return switchOffSetCmds()
}

private getSirenAndStrobeAlarmType() {
	def overriding = true
	def result
	switch (settings.bothAlarmTypeOverride) {
		case "Siren Only":
			result = getSirenOnlyAlarmType()
			break
		case "Strobe Only":
			result = getStrobeOnlyAlarmType()
			break
		default:
			overriding = false
			result = 0
	}
	if (overriding) {
		logDebug "Overriding \"both\" command with \"${settings.bothAlarmTypeOverride}\""
	}
	return result
}

private getSirenOnlyAlarmType() {
	return 1
}

private getStrobeOnlyAlarmType() {
	return 2
}

private alarmTypeSetCmds(alarmType) {
	alarmType = validateRange(alarmType, 0, 0, 2, "Alarm Type")
	
	def result = [
		configSetCmd(0, 1, alarmType)
	]

	if (alarmType == 1) {
		// Prevents strobe light from staying on when setting to Siren Only.
		result << switchBinaryGetCmd()
	}	
	return result	
}

private alarmTypeGetCmd() {
	configGetCmd(0)
}

private autoOffSetCmd(autoOff) {	
	configSetCmd(1, 1, validateRange(autoOff, 0, 0, 3, "Auto Off"))
}

private configSetCmd(paramNumber, paramSize, paramValue) {
	zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: paramSize, scaledConfigurationValue: paramValue).format()
}

private configGetCmd(paramNumber) {
	zwave.configurationV2.configurationGet(parameterNumber: paramNumber).format()
}

private switchOnSetCmd() {
	zwave.basicV1.basicSet(value: 0xFF).format()
}

private switchOffSetCmds() {
	return delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		switchGetCmd()
	], 20)
}

private switchGetCmd() {	
	zwave.basicV1.basicGet().format()
}

private switchBinaryGetCmd() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

private batteryGetCmd() {
	zwave.batteryV1.batteryGet().format()
}

// Parses incoming message
def parse(String description) {	
	def result = []
	if (description.startsWith("Err")) {
		log.error "Unknown Error: $description"		
	}
	else if (description != null && description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x80: 1, 0x70: 2, 0x72: 2, 0x86: 1])		
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	result << createEvent(name:"lastPoll", value: new Date().time, displayed: false, isStateChange: true)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	//logDebug "BinaryReport: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (cmd.parameterNumber == 0) {		
		return response(turnOn(cmd.configurationValue[0]))		
	}
	else {
		logDebug "$cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def switchValue = (cmd.value == 0) ? "off" : "on"
	def alarmValue = null
	
	if (cmd.value == 0) {
		if (device.currentValue("alarm") != "off") {
			logDebug "Alarm is off"
		}
		state.activeAlarm = null
		alarmValue = "off"
	}
	else {		
		alarmValue = getLastAlarmStateValue()			
	}	
	
	return getCommandEvents(alarmValue, switchValue, alarmValue)
}

private getCommandEvents(alarmValue, switchValue, statusValue) {
	[		
		createEvent(
			getStatusEventMap(statusValue)
		),
		createEvent(
			name:"alarm",
			description: "Alarm is $alarmValue",
			value: alarmValue, 
			isStateChange: true, 
			displayed: false
		),
		createEvent(
			name:"switch", 
			description: "Switch is $switchValue",
			value: switchValue, 
			isStateChange: true, 
			displayed: false
		)
	]	
}

private getStatusEventMap(statusValue) {
	return [
		name: "status",
		description: "Status is $statusValue",
		value: statusValue, 
		isStateChange: true, 
		displayed: true
	]
}

private getLastAlarmStateValue() {
	def result
	switch (state.activeAlarm?.alarmType) {
		case 0:
			result = "both"
			break
		case 1:
			result = "siren"
			break
		case 2:
			result = "strobe"
			break			
		default:
			result = "off"
	}		
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ 
		name: "battery", 
		unit: "%"
	]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.isStateChange = true
		map.displayed = true
	} else {
		map.value = cmd.batteryLevel
		map.displayed = false
	}	
	[createEvent(map)]
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"	
}

def speak(text) {
	playText(text)
}

// Unsuported Music Player commands
def unmute() { handleUnsupportedCommand("unmute") }
def resumeTrack(map) { handleUnsupportedCommand("resumeTrack") }
def restoreTrack(map) { handleUnsupportedCommand("restoreTrack") }
def nextTrack() { handleUnsupportedCommand("nextTrack") }
def setLevel(number) { handleUnsupportedCommand("setLevel") }
def previousTrack() { handleUnsupportedCommand("previousTrack") }
def setTrack(string) { handleUnsupportedCommand("setTrack") }

private handleUnsupportedCommand(cmd) {
	log.info "Command $cmd is not supported"
}

// Turns siren off
def pause() { off() }
def stop() { off() }
def mute() { off() }

// Turns siren on
def play() { both() }

// Commands necessary for SHM, Notify w/ Sound, and Rule Machine TTS functionality.
def playSoundAndTrack(text, other, other2, other3) {
	playTrackAndResume(text, other, other2) 
}
def playTrackAndRestore(text, other, other2) {
	playTrackAndResume(text, other, other2) 
}
def playTrackAndResume(text, other, other2) {
	playText(getTextFromTTSUrl(text))
}
def getTextFromTTSUrl(ttsUrl) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	if (ttsUrl?.toString()?.toLowerCase()?.contains(urlPrefix)) {
		return ttsUrl.replace(urlPrefix,"").replace(".mp3","")
	}
	return ttsUrl
}

def playTextAndResume(text, other) { playText(text) }
def playTextAndRestore(text, other) { playText(text) }
def playTrack(text) { playText(text) }

def playText(text) {
	logDebug "Executing playText($text) Command"
	text = cleanTextCmd(text)
	def cmds
	switch (text) {
		case ["on", "play"]:
			cmds = both()
			break
		case ["stop", "off", "pause", "mute"]:
			cmds = off()
			break
		default:
			if (text) {
				cmds = parseComplexCommand(text)
			}
	}
	if (!cmds) {
		logDebug "'$text' is not a valid command."
	}
	else {
		return cmds
	}
}

def cleanTextCmd(text) {
	return text?.
		toLowerCase()?.
		replace(",", "_")?.
		replace(" ", "")?.
		replace("(", "")?.
		replace(")", "")
}

def parseComplexCommand(text) {	
	def cmds = []
	def args = getComplexCmdArgs(text)
	if (text.contains("beep")) {		
		cmds += (args?.size() == 1) ? customBeep(args[0]) : beep()
	}	
	else if (text.contains("strobe")) {	
		cmds += (args?.size() == 2) ? customStrobe(args[0], args[1]) : strobe()
	}
	else if (text.contains("both")) {
		cmds += (args?.size() == 3) ? customBoth(args[0], args[1], args[2]) : both()
	}
	else if (text.contains("siren")) {
		cmds += (args?.size() == 3) ? customSiren(args[0], args[1], args[2]) : siren()
	}
	return cmds
}

private getComplexCmdArgs(text) {
	def args = removeCmdPrefix(text).tokenize("_")
	if (args.every { node -> isNumeric(node) || node in ["true","false"]}) {
		return args
	}
	else {
		return null
	}	
}

private removeCmdPrefix(text) {
	for (prefix in ["custombeep","beep","customboth","both","customsiren","siren","customstrobe","strobe"]) {
		if (text.startsWith(prefix)) {
			return text.replace("${prefix}_", "").replace("$prefix", "")
		}		
	}
	return text	
}

private isNumeric(val) {
	return val?.toString()?.isNumber()
}

private validateBoolean(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private currentValue(attributeName) {
	def val = device.currentValue(attributeName)
	return val ? val : ""	
}

private int validateRange(val, defaultVal, minVal, maxVal, desc) {
	try {
		def result
		def errorType = null
		if (isNumeric(val)) {
			result = val.toInteger()
		}
		else {
			errorType = "invalid"
			result = defaultVal
		}
		
		if (result > maxVal) {
			errorType = "too high"
			result = maxVal
		} else if (result < minVal) {
			errorType = "too low"
			result = minVal
		} 

		if (errorType) {
			logDebug("$desc: $val is $errorType, using $result instead.")
		}
		return result
	}
	catch (e) {
		log.error "$desc: Using $defaultVal because $val validation generated error.  ($e)"
		return defaultVal
	}
}

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "${device.displayName}: $msg"
	}
}

def describeCommands() {
	return [
		"customBeep": [ display: "Custom Beep", description: "{0} Beep Length in Milliseconds", parameters:["number"]], // beepLengthMS
		"customBoth": [ display: "Custom Strobe and Siren", description: "(delaySeconds: {0}, autoOffSeconds: {1}, useStrobe: {2})", parameters:["number", "number", "bool"]],
		"customSiren": [ display: "Custom Siren", description: "", parameters:["number", "number", "bool"]], // delaySeconds, autoOffSeconds, useStrobe
		"customStrobe": [ display: "Custom Strobe", description: "", parameters:["number", "number"]] // delaySeconds, autoOffSeconds
	]
}