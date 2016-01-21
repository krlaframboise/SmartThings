/**
 *  Aeon Labs Multifunction Doorbell 
 *
 *  Capabilities: Switch, Alarm, Music Player, Tone, Button, Battery
 *
 *
 *	Author: Kevin LaFramboise (krlaframboise)
 *					
 *					* Based off of the "Aeon Doorbell" device type
 *					which was originally written by robertvandervoort
 *					and then forked by jamesdawson3
 *
 *	Changelog:
 *
 *	1.0 - Initial Release (01/21/2016)
 * 
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
	definition (name: "Aeon Labs Multifunction Doorbell", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"			
		capability "Switch"
		capability "Alarm"	
		capability "Music Player"
		capability "Tone"		
		capability "Battery"
		capability "Button"		
		
		command "pushButton"
		
		fingerprint deviceId: "0x1005", noneClusters: "0x26 0x2B 0x2C 0x27 0x73 0x70 0x86 0x72"
	}

	simulator {
		status "basic report on": zwave.basicV1.basicReport(value:0xFF).incomingMessage()		
		status "basic report off": zwave.basicV1.basicReport(value:0x00).incomingMessage()		
		
    reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
    reply "988100200100,9881002002": "command: 9881, payload: 00200300"
		reply "9881002001FF,delay 3000,988100200100,9881002002": "command: 9881, payload: 00200300"		
	}

	preferences {
		input "bellTrack", "number", title: "Doorbell Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false	
			
		input "toneTrack", "number", title: "Beep Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false
		
		input "alarmTrack", "number", title: "Alarm Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false
		
		input "soundLevel", "number", title: "Sound Level (1-10)", defaultValue: 10, displayDuringSetup: true,  required: false
		
		input "soundRepeat", "number", title: "Sound Repeat: (1-100)", defaultValue: 1, displayDuringSetup: true, required: false		
				
		input "debugOutput", "boolean", title: "Enable debug logging?", defaultValue: false, displayDuringSetup: true, required: false
		
		input "silentButton", "boolean", title: "Enable Silent Button?\n(If you want to use the button for something other than a doorbell, you need to also set the Doorbell Track to a track that doesn't have a corresponding sound file.)", defaultValue: false, required: false
		
		input "forceConfigure", "boolean", title: "Force Configuration Refresh? (Leave this disabled unless your experiencing problems with your settings not getting applied)", defaultValue: false, required: false
	}	
	
	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "bell", label:'Doorbell Ringing!', action: "off", icon:"st.Home.home30", backgroundColor:"#99c2ff"
				attributeState "alarm", label:'Alarm!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"
				attributeState "play", label:'Playing!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#694489"
			}
		}		
		standardTile("playBell", "device.tone", label: 'Doorbell', width: 2, height: 2) {
			state "default", label:'Doorbell', action:"pushButton", icon:"st.Home.home30", backgroundColor: "#99c2ff"
		}
		standardTile("playTone", "device.tone", label: 'Beep', width: 2, height: 2) {
			state "default", label:'Beep', action:"beep", icon:"st.Entertainment.entertainment2", backgroundColor: "#99FF99"
		}
		standardTile("playAlarm", "device.alarm", label: 'Alarm', width: 2, height: 2) {
			state "default", label:'Alarm', action: "both", icon:"st.alarm.alarm.alarm", backgroundColor: "#ff9999"
		}
		valueTile("previous", "device.musicPlayer", label: 'Previous Track', width: 2, height: 2) {
			state "default", label:'<<', action:"previousTrack", backgroundColor: "#694489"
		}
		valueTile("trackDescription", "device.trackDescription", label: 'Play Track', wordWrap: true, width: 2, height: 2) {
			state "trackDescription", label:'PLAY\n${currentValue}', action: "play", backgroundColor: "#694489"
		}		
		valueTile("next", "device.musicPlayer", label: 'Next Track', width: 2, height: 2) {
			state "default", label:'>>', action:"nextTrack", backgroundColor: "#694489"
		}
		valueTile("battery", "device.battery",  width: 2, height: 2) {
			state "battery", label:'BATTERY\n${currentValue}%', unit:"", backgroundColor: "#000000"
		}
		main "status"
		details(["status", "playBell", "playTone", "playAlarm", "previous", "trackDescription", "next", "battery"])
	}
}


def pushButton() {	
	if (!state.isPlaying) {
		state.pushingButton = true
		writeToDebugLog("Turning on doorbell")
		secureDelayBetween([
			zwave.basicV1.basicSet(value: 0xFF)
		])
	}
}

def off() {
	secureDelayBetween([ 
		zwave.basicV1.basicSet(value: 0x00)
	])	
}


def on() {
	both()
}

// Alarm Commands
def strobe() {
	both()
}
def siren() {
	both()
}
def both() {
	def result = []	
	
	if (canPlay()) {
		writeToDebugLog("Alarm command received")

		result << sendEvent(name: "alarm", value: "both")
		result << sendEvent(name: "switch", value: "on", displayed: false)
		result << sendEvent(name:"status", value: "alarm", descriptionText: "$device.displayName alarm is on", isStateChange: true)					
				
		result += response(secureDelayBetween(playTrackCommands(state.alarmTrack)))
	}
	return result
}

// Tone Commands.beep
def beep() {
	writeToDebugLog("Beep Command received")
	playTrack(state.toneTrack, "beep", "Beeping!")
}


// Music Player Commands
def previousTrack() {	
	def newTrack = (validateTrackNumber(state.currentTrack) - 1)
	if (newTrack < minTrack()) {
		newTrack = minTrack()
	} 
	writeToDebugLog("Previous Track: $newTrack")
	setTrack(newTrack)	
}

def nextTrack() {
	def newTrack = (validateTrackNumber(state.currentTrack) + 1)
	if (newTrack > maxTrack()) {
		newTrack = maxTrack()
	}
	writeToDebugLog("Next Track: $newTrack")
	setTrack(newTrack)
}

def setTrack(track) {	
	state.currentTrack = validateTrackNumber(track)	
	writeToDebugLog("currentTrack set to ${state.currentTrack}")
	
	sendEvent(name:"trackDescription", value: track, descriptionText:"Track $track", isStateChange: true, displayed: false)
}

def stop() {
	off()
}

def play() {
	playTrack(state.currentTrack)
}

def playTrack(track) {
	playTrack(track, "play", "Playing track $track")	
}

def playTrack(track, status, desc) {
	def result = []
	
	if (canPlay()) {
		writeToDebugLog("Playing Track $track ($status: $desc)")
		
		result << sendEvent(name: "status", value: status, descriptionText: desc, isStateChange: true)			
				
		result += response(secureDelayBetween(playTrackCommands(track)))		
	}	
	return result
}

def canPlay() {
	def result 
	
	if (!state.isPlaying && !state.pushingButton) {
		state.isPlaying = true
		result = true
	} else {
		writeToDebugLog("Skipped Play because already playing")
		result = false
	}
	result
}

def setLevel(level) {
	writeToDebugLog("Setting soundLevel to $level")
	secureDelayBetween(soundLevelCommands(level))
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {		
	def result = []	
	
	if (cmd.value == 0) {
		state.isPlaying = false
		state.pushingButton = false
		writeToDebugLog("BasicReport OFF")
		
		result << createEvent(name:"status", value: "off")
		
		result << createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off")
		
		result << createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off")		
	
	} 
	else if (cmd.value == 255) {
		writeToDebugLog("BasicReport ON")
		
		if (state.isPlaying) {
			//writeToDebugLog("Something is playing")			
		} 
		else {
			state.isPlaying = true
			writeToDebugLog("Doorbell button was pushed.")		

			result << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName doorbell button was pushed", isStateChange: true)
		
			if (!state.silentButton) {				
				result << createEvent(name: "status", value: "bell", descriptionText: "$device.displayName doorbell is ringing", isStateChange: true)
			} 
			else {
				writeToDebugLog("Silent Button Enabled (If it's still making sound then you need to verify that the doorbell track doesn't have a corresponding file. I recommend using track 100)")
			}								
		}
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	writeToDebugLog("WakeUpNotification: $cmd")
	
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	// Only ask for battery if we haven't had a BatteryReport in 8 hours.
	if (!state.lastBatteryReport || (new Date().time) - state.lastBatteryReport > 8*60*60*1000) {		
		request += batteryHealthCommands()
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def parameterName
	switch (cmd.parameterNumber) {
		case 8:
			parameterName = "Sound Level"
			break
		case 5:
			parameterName = "Default Doorbell Track"
			break
		case 2:
			parameterName = "Sound Repeat Times"
			break
		case 42:
			parameterName = null
			batteryHealthReport(cmd)			
			break
		default:	
			parameterName = "Parameter #${cmd.parameterNumber}"
	}		
	if (parameterName) {
		writeToDebugLog("${parameterName} changed to ${cmd.configurationValue}.")
	}
}

def batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	
	def map = [ name: "battery", unit: "%" ]	
	map.isStateChange = true		
	map.value = (cmd.configurationValue == [0]) ? 100 : 1
	
	def batteryLevel = (map.value == 100) ? "normal" : "low"
	map.descriptionText = "${device.displayName}'s battery is $batteryLevel."	

	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	writeToDebugLog("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}
def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true, descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	}
	else if (description != "updated") {	
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])

	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}


def updated() {
	if (!isDuplicateCall(state.lastUpdated, 2)) {
		state.lastUpdated = new Date().time
		
		initializePreferences()
		
		if (state.isConfigured && !state.forceConfigure) {
			writeToDebugLog("Updating preferences")						
			
			def request = soundRepeatCommands(state.soundRepeat)
			request += soundLevelCommands(state.soundLevel)	
			request += defaultTrackCommands(state.bellTrack)
			
			if (!state.lastBatteryReport) {
				writeToDebugLog("Requesting battery health")
				request += batteryHealthCommands()
			}
			response(secureDelayBetween(request))
		
		} else {
			response(configure())			
		}		
	}
}

//Configuration.configure
def configure() {
	writeToDebugLog("Configuration being sent to ${device.displayName}")
	
	initializePreferences()

	def request = [
		//associate with group 1 and remove any group 2 association
		zwave.associationV1.associationRemove(groupingIdentifier:2, nodeId:zwaveHubNodeId),		
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId),		
		zwave.associationV1.associationGet(groupingIdentifier:1),		
		zwave.associationV1.associationGet(groupingIdentifier:2),
		
		zwave.versionV1.versionGet(),		
		zwave.firmwareUpdateMdV2.firmwareMdGet(),

		// Enable to send notifications to associated devices (Group 1) (0=nothing, 1=hail CC, 2=basic CC report)
		zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 2),
		
		zwave.configurationV1.configurationGet(parameterNumber: 80),

		// send low battery notifications
		zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: 1),
		
		zwave.configurationV1.configurationGet(parameterNumber: 81)		
	]
	
	request += batteryHealthCommands()	
	request += defaultTrackCommands(state.bellTrack)
	request += soundRepeatCommands(state.soundRepeat)
	request += soundLevelCommands(state.soundLevel)
	
	state.isConfigured = true
	
	secureDelayBetween(request)
}

def batteryHealthCommands() {
	return [
		zwave.configurationV1.configurationGet(parameterNumber: 42)
	]
}

def playTrackCommands(track) {
	return [
		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: validateTrackNumber(track))
	]
}

def soundRepeatCommands(newSoundRepeat) {	
	return [		
		zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: validateSoundRepeat(newSoundRepeat)),
	
		zwave.configurationV1.configurationGet(parameterNumber: 2)	
	]
}

def defaultTrackCommands(newDefaultTrack) {
	return [
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: validateTrackNumber(newDefaultTrack)),
		
	 zwave.configurationV1.configurationGet(parameterNumber: 5)
	]
}

def soundLevelCommands(newSoundLevel) {	
	return [
		zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: validateSoundLevel(newSoundLevel)),
	
		zwave.configurationV1.configurationGet(parameterNumber: 8)
	]
}

private initializePreferences() {	
	state.bellTrack = validateTrackNumber(bellTrack)
	state.alarmTrack = validateTrackNumber(alarmTrack)
	state.toneTrack = validateTrackNumber(toneTrack)
	state.soundLevel = validateSoundLevel(soundLevel)
	state.soundRepeat = validateSoundRepeat(soundRepeat)	
	state.forceConfigure = validateBooleanPref(forceConfigure)
	state.silentButton = validateBooleanPref(silentButton)
}

int validateSoundRepeat(soundRepeat) {
	validateNumberRange(soundRepeat, 1, 1, 100)
}

int validateSoundLevel(soundLevel) {
	validateNumberRange(soundLevel, 5, 0, 10)
}

int validateTrackNumber(track) {
	validateNumberRange(track, 2, minTrack(), maxTrack())
}

int validateNumberRange(value, defaultValue, minValue, maxValue) {
	def result = value
	if (!value) {
		result = defaultValue
	} else if (value > maxValue) {
		result = maxValue
	} else if (value < minValue) {
		result = minValue
	} 
	
	if (result != value) {
		writeToDebugLog("$value is invalid, defaulting to $result.")
	}
	result
}

int minTrack() {
	return 1
}

int maxTrack() {
	return 100
}

def validateBooleanPref(pref) {
	return (pref == "true")	
}


private secureDelayBetween(commands, delay=100) {
	delayBetween(commands.collect{ secureCommand(it) }, delay)
}

private secureCommand(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private isDuplicateCall(lastRun, allowedEverySeconds) {
	def result = false
	if (lastRun) {
		result =((new Date().time) - lastRun) < (allowedEverySeconds * 1000)
	}
	result
}

private writeToDebugLog(msg) {	
	if (debugOutput == "true") {
		log.debug msg
	}
}