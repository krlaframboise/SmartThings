/**
 *  GoControl Multifunction Siren v 1.0
 *  
 *  Capabilities:
 *      Alarm, Tone, Switch, Battery, Polling
 *      Speech Synthesis, Music Player
 *
 *	Author: 
 *      Kevin LaFramboise (krlaframboise)
 *
 *  Url to Documentation: https://community.smartthings.com/t/release-gocontrol-linear-multifunction-siren/47024?u=krlaframboise
 *
 *	Changelog:
 *
 *	  1.0 (05/04/2016)
 *		  -	Initial Release
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
			defaultValue: 50, 
			displayDuringSetup: true, 
			required: false
		input "strobeBeep", "bool", 
			title: "Strobe instead of Beep?\n(Enabling this will cause the light to flash when the beep command is executed instead of making a sound.)", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
		input "bothAlarmTypeOverride", "enum",
			title: "What should the 'both' and 'on' commands turn on?\n(Some SmartApps like Smart Home Monitor use the both command so you can't use just the siren or the strobe.  This setting allows you to override the default action of those commands.)",
			defaultValue: "Siren and Strobe",
			displayDuringSetup: true,
			required: false,
			options: ["Siren and Strobe", "Siren Only", "Strobe Only"]
		input "staticAlarmType", "bool", 
			title: "Always use both/on setting?\n(When enabled, the type of alarm chosen for both/on will override all commands.  This usually needs to be enabled in order for the beep feature to work correctly, but must be disabled if the strobe beep setting is enabled.)", 
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
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "siren", label:'Siren On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"							
			}
		}
		valueTile("off", "device.status", label: 'off', width: 2, height: 2) {
			state "default", label:'Turn Off', action: "off", icon:"", backgroundColor: "#99c2ff"
			state "off", label:'Off', action: "off"
		}
		valueTile("testBeep", "device.status", label: 'beep', width: 2, height: 2) {
			state "default", label:'Test Beep', action:"beep", icon:"", backgroundColor: "#99FF99"
			state "beep", label:'Stop Beep', action: "off", icon:"", backgroundColor: "#99c2ff"			
		}						
		valueTile("testSiren", "device.status", label: 'Siren', width: 2, height: 2) {
			state "default", label:'Test Siren', action: "siren", icon:"", backgroundColor: "#ff9999"					
			state "siren", label:'Stop Siren', action: "off", icon:"", backgroundColor: "#99c2ff"			
		
		}
		valueTile("testStrobe", "device.status", label: 'Strobe', width: 2, height: 2) {
			state "default", label:'Test Strobe', action: "strobe", icon:"", backgroundColor: "#ff9999"
			state "strobe", label:'Stop Strobe', action: "off", icon:"", backgroundColor: "#99c2ff"
		}
		valueTile("testBoth", "device.status", label: 'Both', width: 2, height: 2) {
			state "default", label:'Test Both', action: "both", icon:"", backgroundColor: "#ff9999"
			state "both", label:'Stop Both', action: "off", icon:"", backgroundColor: "#99c2ff"						
		}		
		valueTile("battery", "device.battery", label: '', decoration: "flat", width: 2, height: 2) {
			state "battery", label:'Battery ${currentValue}%', icon:""
		}		
				
		main "status"
		details(["status", "testSiren", "testStrobe", "testBoth", "off", "testBeep", "battery"])
	}
}

// Stores preferences and displays device settings.
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time
		state.staticAlarmType = validateBoolean(settings.staticAlarmType, false)
		state.strobeBeep = validateBoolean(settings.strobeBeep, false)
		state.debugOutput = validateBoolean(settings.debugOutput, true)
		
		if (state.strobeBeep) {
			// Static alarm type other than strobe will cause the strobe beep to actually beep.
			state.staticAlarmType = false
		}
		
		logDebug "Updating"

		def cmds = []		
		cmds << autoOffSetCmd(getAutoOffTimeValue())
		
		if (state.staticAlarmType) {
			cmds << sirenAndStrobeAlarmTypeSetCmd()
		}
		
		if (canPoll()) {
			cmds += poll()
		}
		cmds += turnOff()		
		response(delayBetween(cmds, 200))
	}
}

private getAutoOffTimeValue() {
	def result
	def autoOffSeconds
	switch (settings.autoOffTime) {
		case "60 Seconds":
			autoOffSeconds = 60
			result = 1
			break
		case "120 Seconds":
			autoOffSeconds = 120
			result = 2
			break
		case "Disable Auto Off":
			autoOffSeconds = null
			result = 3
			break
		default:
			autoOffSeconds = 30
			result = 0 // 30 Seconds
	}
	state.autoOffMS = autoOffSeconds  ? (autoOffSeconds * 1000) : null
	return result
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def poll() {	
	if (canPoll()) {
		logDebug "${device.displayName} Polling"
		state.lastPoll = new Date().time
		[
			batteryGetCmd()
		]
	}
}

private canPoll() {
	return (!state.lastPoll || (new Date().time) - state.lastPoll > 24*60*60*1000)
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
	text = cleanTextCmd(text)
	def cmds
	switch (text) {
		case "siren":
			cmds = siren()
			break
		case "strobe":
			cmds = strobe()
			break
		case ["both", "on", "play"]:
			cmds = both()
			break
		case ["stop", "off", "pause", "mute"]:
			cmds = off()
			break
		case "beep":
			cmds = beep()
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
	
	if (text.contains("beep") && args?.size() == 1) {
		cmds += beep(args[0])
	}	
	return cmds
}

private getComplexCmdArgs(text) {
	for (prefix in ["beep"]) {
		text = removeCmdPrefix(text, prefix)
	}
	
	def args = text.tokenize("_")
	if (args.every { node -> isNumeric(node) }) {
		return args
	}
	else {
		return null
	}	
}

private removeCmdPrefix(text, prefix) {
	if (text.startsWith(prefix)) {
		return text.replace("$prefix_", "").replace(prefix, "")
	}
	else {
		return text
	}
}


// Turns on siren and strobe
def on() {
	both()	
}

def beep() {
	beep(settings.beepLength)
}

// Turns on and then off after specified milliseconds.
def beep(beepLengthMS) {	
	beepLengthMS = validateRange(beepLengthMS, 50, 0, Integer.MAX_VALUE, "Beep Length")
	
	logDebug "Executing beep(${beepLengthMS}) Command"
	
	def alarmTypeCmd = state.strobeBeep ? strobeOnlyAlarmTypeSetCmd() : sirenOnlyAlarmTypeSetCmd()
	state.lastAlarmType = null
	
	def result = []	
	if (!state.staticAlarmType) {
		result << alarmTypeCmd		
		result << "delay 250"
	}
	result << switchOnSetCmd()
	if (beepLengthMS > 1000) {
		result << switchGetCmd()
	}
	if (beepLengthMS > 0) {
		result << "delay $beepLengthMS"
	}
	result += switchOffSetCmds()
	return result
}

// Turns on siren and strobe
def both() {
	logDebug "Executing both() command"
	turnOn(sirenAndStrobeAlarmTypeSetCmd())	
}

// Turns on strobe
def strobe() {
	logDebug "Executing strobe() command"
	turnOn(strobeOnlyAlarmTypeSetCmd())
}

// Turns on siren
def siren() {
	logDebug "Executing siren() command"
	turnOn(sirenOnlyAlarmTypeSetCmd())
}

private turnOn(alarmTypeCmd) {
	def result = []
	if (!state.staticAlarmType) {
		result << alarmTypeCmd
		result << "delay 250"
	}
	result << switchOnSetCmd()
	result << "delay 10"	
	result << switchGetCmd()
	
	if (state.autoOffMS) {
		result << "delay ${state.autoOffMS}"
		result += switchOffSetCmds()
	}
	
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

private sirenAndStrobeAlarmTypeSetCmd() {
	switch (settings.bothAlarmTypeOverride) {
		case "Siren Only":
			return sirenOnlyAlarmTypeSetCmd()
			break
		case "Strobe Only":
			return strobeOnlyAlarmTypeSetCmd()
			break
		default:
			return alarmTypeSetCmd(0)
	}
}

private sirenOnlyAlarmTypeSetCmd() {
	alarmTypeSetCmd(1)
}

private strobeOnlyAlarmTypeSetCmd() {
	alarmTypeSetCmd(2)
}

private alarmTypeSetCmd(alarmType) {
	state.lastAlarmType = alarmType
	configSetCmd(0, 1, validateRange(alarmType, 0, 0, 2, "Alarm Type"))
}

private autoOffSetCmd(autoOff) {	
	configSetCmd(1, 1, validateRange(autoOff, 0, 0, 3, "Auto Off"))
}

private configSetCmd(paramNumber, paramSize, paramValue) {
	zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: paramSize, scaledConfigurationValue: paramValue).format()
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
	//zwave.switchBinaryV1.switchBinaryGet().format()
	zwave.basicV1.basicGet().format()
}

private batteryGetCmd() {
	zwave.batteryV1.batteryGet().format()
}

// Parses incoming message
def parse(String description) {	
	def result = null
	if (description.startsWith("Err")) {
		log.error "Unknown Error: $description"		
	}
	else if (description != null && description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x80: 1, 0x70: 2, 0x72: 2, 0x86: 1])		
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	return result
}

// def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	// logDebug "BinaryReport: $cmd"
// }

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {	
	def status = null
	if (cmd.value == 0) {
		state.lastAlarmType = null
		status = "off"
	}
	else if (state.lastAlarmType == null) {
		status = "beep"
	}

	def alarm = getLastAlarmStateValue()
	if (!status) {
		status = alarm
	}

	return [
		createEvent(
			name:"switch", 
			value: (cmd.value == 0) ? "off" : "on", 
			isStateChange: true, 
			displayed: false
		),
		createEvent(
			name: "status", 
			value: status, 
			isStateChange: true, 
			displayed: false
		),
		createEvent(
			name:"alarm", 
			value: alarm, 
			isStateChange: true, 
			displayed: (currentValue("alarm") != alarm)
		)
	]		
}

private getLastAlarmStateValue() {
	def result
	switch (state.lastAlarmType) {
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
		unit: "%", 
		isStateChange: true 
	]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}	
	return createEvent(map)
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"	
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

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "$msg"
	}
}