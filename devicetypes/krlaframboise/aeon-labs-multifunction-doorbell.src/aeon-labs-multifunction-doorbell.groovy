/**
 *  Aeon Labs Multifunction Doorbell v 1.8.13
 **************************************************************************************
 **************************************************************************************
 *
 *  !!! THIS DTH IS NO LONGER SUPPORTED !!!
 *   Use the Aeotec Doorbell DTH instead: 
 *  https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-doorbell.src/aeotec-doorbell.groovy
 *
 **************************************************************************************
 **************************************************************************************
 *
 * 
 *  Capabilities:
 *					Switch, Alarm, Music Player, Tone,
 *					Button, Battery, Presence Sensor, Polling, Refresh
 *
 *	Author: 
 *					Kevin LaFramboise (krlaframboise)
 *					(Based off of the "Aeon Doorbell" device type)
 *
 *	Changelog:
 *
 *	1.8.13 (08/06/2016)
 *		- Added Audio notification capability and fixed
 *      parameters of all music related commands to
 *			match documentation.
 *    - Removed debugging data
 *      
 *	1.8.10 (07/21/2016)
 *		- Fixed fingerprint or v1 hubs.
 *
 *	1.8.9 (07/17/2016)
 *		- Added new fingerprint format for specific product
 *      because some devices aren't coming across with
 *      the security command class in the raw description.
 *
 *	1.8.8 (07/14/2016)
 *		- Changed skip play interval from 25 to 10 so that
 *      overlapping tracks can be used to extend beyond
 *      the 20 second maximum.
 *
 *	1.8.7 (06/23/2016)
 *		- Attempt to fix stuck status on Things screen.
 *
 *	1.8.4 (05/12/2016)
 *		- Added lastPoll attribute and improved functionality.
 *
 *	1.8.3 (03/13/2016)
 *		- Fixed UI Presence Tile bug caused by Android 2.1.0 Update.
 *
 *	1.8.2 (03/11/2016)
 *		- Add catch to prevent speak commands from executing
 *			more than once per second to eliminate duplicate
 *			tracks being played when using Rule Machine Triggers.			
 *
 *	1.8.1 (02/29/2016)
 *		- Added track number support for SHM - Audio Notifications, 
 *			Speaker Notify with Sound - Custom Message, and
 *			Rule Machine - Speak Message on Music Device.
 *		- Made TTS functionality accept track numbers and commands.
 *
 *	1.7 (02/28/2016)
 *		- Fixed fingerprint so that it doesn't conflict
 *			with the Aeon Labs Multifunction Siren.
 *
 *	1.6 (02/11/2016)
 *		- Added association set command which has already
 *			solved the secure pairing issue for a couple of users.
 *		-	Made secure pairing optional by adding the 
 *			"Use Secure Commands" preference so it should work
 *			for people that are unable to get it pair securely.
 *		- Added "Log Configuration on Refresh" preference
 *			that when enabled, displays all the configuration
 *			settings in the info log when the Refresh button
 *			is pressed. 
 *		- Split reporting from configuration so that it doesn't
 *			take so long for the settings to save.
 *		- Made it always send the configuration on settings save
 *			and removed the "Force Configuration" preference because
 *			it's no longer needed.
 *		- Added all missing commands from supported 
 *			capabilities.
 *
 *	1.5 (02/08/2016)
 *		-	Fixed fingerprint
 *		- Removed extra association lines.
 *		- Added firmware report.
 *		- Defaulted the Debug Output preference to True.
 *		- Defaulted the Force Configuration preference to True.
 *
 *	1.4 (02/04/2016)
 *		-	Modified polling functionality so that it doesn't
 *			use unschedule which should eliminate the false offlines.
 *
 *	1.3 (01/27/2016)
 *		-	Replaced the Beacon Capability with Presence Sensor
 *			because I haven't found any SmartApps that support
 *			Beacon and the Android Mobile app occasionally
 *			generated Presence errors.
 *
 *	1.2 (01/25/2016)
 *		-	Added the Beacon, Polling and Refresh capabilities
 *			which can be used to determine if your internet is
 *			down or you've lost power.
 *		-	Added catch so if the UI does get stuck it will
 *			automatically unstick after 25 seconds instead of 
 *			requiring the user to attempt the buttons 3 times.
 *
 *	1.1 (01/22/2016)
 *		-	Fixed bug that caused the mobile app to stop playing
 *			after a specific sequence of events.
 *
 *	1.0 (01/21/2016)
 *		-	Initial Release
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
		capability "Audio Notification"
		capability "Tone"		
		capability "Battery"
		capability "Button"
		capability "Polling"
		capability "Presence Sensor"
		capability "Refresh"

		attribute "lastPoll", "number"
		
		command "pushButton"
		command "playTrackAtVolume"
		
		fingerprint mfr: "0086", prod: "0104", model: "0038"		

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"
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
		
		input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: true, required: false
		
		input "silentButton", "bool", title: "Enable Silent Button?\n(If you want to use the button for something other than a doorbell, you need to also set the Doorbell Track to a track that doesn't have a corresponding sound file.)", defaultValue: false, required: false
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
		standardTile("previous", "device.musicPlayer", label: 'Previous Track', width: 2, height: 2) {
			state "default", label:'<<', action:"previousTrack", backgroundColor: "#694489"
		}
		standardTile("trackDescription", "device.trackDescription", label: 'Play Track', wordWrap: true, width: 2, height: 2) {
			state "trackDescription", label:'PLAY\n${currentValue}', action: "play", backgroundColor: "#694489"
		}		
		standardTile("next", "device.musicPlayer", label: 'Next Track', width: 2, height: 2) {
			state "default", label:'>>', action:"nextTrack", backgroundColor: "#694489"
		}
		standardTile("refresh", "device.refresh", label: 'Refresh', width: 2, height: 2) {
			state "default", label:'REFRESH', action: "refresh"
		}
		valueTile("battery", "device.battery",  width: 2, height: 2) {
			state "battery", label:'BATTERY\n${currentValue}%', unit:"", backgroundColor: "#000000"
		}
		valueTile("onlineStatus", "device.presence",  wordWrap: true, width: 2, height: 2) {
			state "present", label: 'ONLINE ', backgroundColor: "#00FF00"
			state "not present", label: 'OFFLINE', backgroundColor: "#FF0000"
			state "default", label: 'UNKNOWN', defaultState: true
		}
		main "status"
		details(["status", "playBell", "playTone", "playAlarm", "previous", "trackDescription", "next", "refresh", "battery", "onlineStatus"])
	}
}


def pushButton() {	
	if (!state.isPlaying) {
		state.pushingButton = true
		logDebug("Pushing doorbell button")
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
	return startPlayingTrack(state.alarmTrack, "alarm", "$device.displayName alarm is on")	
}

// Tone Commands.beep
def beep() {	
	startPlayingTrack(state.toneTrack, "beep", "Beeping!")
}

// Music Player Commands
def mute() { handleUnsupportedCommand("mute") }
def unmute() { handleUnsupportedCommand("unmute") }
def resumeTrack(map) { handleUnsupportedCommand("resumeTrack") }
def restoreTrack(map) { handleUnsupportedCommand("restoreTrack") }


// Audio Notification Capability Commands
def playSoundAndTrack(String URI, Number duration=0, String track, Number volume=0) {
	playTrack(URI, volume)
}
def playTrackAndResume(String URI, Number volume=0) {
	playTrack(URI, volume)
}	
def playTrackAndRestore(String URI, Number volume=0) {
	playTrack(URI, volume)
}	

// Documented as part of the Audio Notification capability
// but not actually part of it so it must be declared.
def playTrackAtVolume(String URI, Number volume) {
	playTrack(URI, volume)
}

def playTrack(String URI, Number volume=0) {
	def text = getTextFromTTSUrl(URI)
	playText(!text ? URI : text, volume)	
}

def getTextFromTTSUrl(ttsUrl) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	if (ttsUrl?.toString()?.toLowerCase()?.contains(urlPrefix)) {
		return ttsUrl.replace(urlPrefix,"").replace(".mp3","")
	}
	return null
}

def playTextAndResume(String message, Number volume=0) {
	playText(message, volume)
}	
def playTextAndRestore(String message, Number volume=0) {
	playText(message, volume)
}
def playText(String message, Number volume=0) {
	if (!isDuplicateCall(state.lastPlayText, 1)) {		
		state.lastPlayText = new Date().time
		
		def cmds = []
		if (isNumeric(message)) {
			cmds += playTrackNumber(message, volume)
		}
		else {
			switch (message?.toLowerCase()) {
				case "beep":
					cmds += beep()
					break
				case "pushbutton":
					cmds += pushButton()
					break
				case ["siren", "strobe", "both", "on"]:
					cmds += both()
					break
				case ["stop", "off"]:
					cmds += off()
					break
				case "play":
					cmds += play()
					break
				default:
					logDebug "'$message' is not a valid command or track number."
			}
		}
		return cmds
	}
}

def previousTrack() {	
	def newTrack = (validateTrackNumber(state.currentTrack) - 1)
	if (newTrack < minTrack()) {
		newTrack = minTrack()
	} 
	logDebug("Previous Track: $newTrack")
	setTrack(newTrack)	
}

def nextTrack() {
	def newTrack = (validateTrackNumber(state.currentTrack) + 1)
	if (newTrack > maxTrack()) {
		newTrack = maxTrack()
	}
	logDebug("Next Track: $newTrack")
	setTrack(newTrack)
}

def setTrack(track) {	
	state.currentTrack = validateTrackNumber(track)	
	logDebug("currentTrack set to ${state.currentTrack}")
	
	sendEvent(name:"trackDescription", value: track, descriptionText:"Track $track", isStateChange: true, displayed: false)
}

def pause() {
	stop()
}

def stop() {
	off()
}

def play() {
	playTrackNumber(state.currentTrack)
}

def playTrackNumber(track, volume=null) {
	startPlayingTrack(track, "play", "Playing track $track")	
}

private startPlayingTrack(track, status, desc) {
	def result = []
	
	if (canPlay()) {
		logDebug("Playing Track $track ($status: $desc)")
		
		if (status == "alarm") {
			sendEvent(name: "alarm", value: "both", displayed: false)
			sendEvent(name: "switch", value: "on", displayed: false)
		}
		
		sendEvent(name: "status", value: status, descriptionText: desc, isStateChange: true)
						
		result << secureCommand(playTrackCommand(track))
	}	
	return result
}

private canPlay() {
	def result = false
	
	if (!isDuplicateCall(state.lastPlay, 1)) {
		
		if ((!state.isPlaying && !state.pushingButton) || isStuckPlaying()) {
			state.lastPlay = new Date().time
			state.isPlaying = true
			result = true
		
		} else {
			logDebug("Skipped Play because already playing")
		}	
	
	} else {
		def current = new Date().time
		logDebug("Duplicate Play Call ${state.lastPlay} - $current")
	}
	result
}

// if the last play was more than 25 seconds ago
// or it has skipped 3 times in a row, it's most likely stuck.
private isStuckPlaying() {
	def result = false
	def currentTime = new Date().time
	
	if (state.lastPlay == null || (state.lastPlay + (10 * 1000)) < currentTime) {		
		result = true
	} else if (state.playSkipCount == null) {
		state.playSkipCount = 1
	} 
	else {		
		state.playSkipCount = (state.playSkipCount + 1)		
		if (state.playSkipCount >= 3) {			
			result = true
		} 		
	}	
	result
}

def setLevel(level) {
	logDebug("Setting soundLevel to $level")
	return [secureCommand(soundLevelSetCommand(level))]	
}

def refresh() {	
	writeToInfoLog("Current Track: ${state.currentTrack}")
	writeToInfoLog("Alarm Track: ${state.alarmTrack}")
	writeToInfoLog("Beep Track: ${state.toneTrack}")
	writeToInfoLog("Silent Button Enabled: ${state.silentButton}")
	writeToInfoLog("Debug Logging Enabled: ${state.debugOutput}")
	writeToInfoLog("Use Secure Commands: ${state.useSecureCommands}")				
	
	sendEvent(getPresenceEventMap(""))

	def result = []			
	result += off()
	result += poll()	
	result += secureDelayBetween(reportCommands())

	return result	
}

def poll() {
	logDebug "Starting Poll"
	state.polling = true
	runIn(10, checkPoll)	
	return [secureCommand(reportVersionCommand())]
}

void checkPoll() {
	if (state.polling) {
		log.warn "Poll Failed"
		sendEvent(getPresenceEventMap("not present"))		
	}
}

private getPresenceEventMap(presenceVal) {	
	def displayedVal = (presenceVal && (device.currentValue("presence") != presenceVal))
	[
		name: "presence", 
		value: presenceVal, 
		displayed: displayedVal, 
		isStateChange: true
	]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {		
	def result = []	
	
	if (cmd.value == 0) {
		clearPlayingStatus()		
		
		result << createEvent(name:"status", value: "off", displayed: true, isStateChange: true)
		
		result << createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off", displayed: false, isStateChange: true)
		
		result << createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off", displayed: false, isStateChange: true)		
	
	} 
	else if (cmd.value == 255) {		
		if (state.isPlaying) {
			//logDebug("Something is playing")			
		} 
		else {
			state.isPlaying = true
			logDebug("Doorbell button was pushed.")					
			
			result << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName doorbell button was pushed", isStateChange: true)
		
			if (!state.silentButton) {				
				result << createEvent(name: "status", value: "bell", descriptionText: "$device.displayName doorbell is ringing", isStateChange: true)
			} 
			else {
				logDebug("Silent Button Enabled (If it's still making sound then you need to verify that the doorbell track doesn't have a corresponding file. I recommend using track 100)")
			}
			result += response(poll())
		}
	}	
	return result
}

private clearPlayingStatus() {
	state.playSkipCount = 0
	state.isPlaying = false
	state.pushingButton = false
	state.lastPlay = null
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCommands = true
	logDebug("Secure Commands Supported")	
	
	def cmds = []
	cmds << "delay 2000"
	cmds += configure()
	
	return response(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug("WakeUpNotification: $cmd")
	
	def result = []
	
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)

	// Only ask for battery if we haven't had a BatteryReport in 24 hours.
	if (!state.lastBatteryReport || (new Date().time) - state.lastBatteryReport > 24*60*60*1000) {		
		result << response(reportBatteryHealthCommand())
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
	writeToInfoLog("Firmware: $cmd")
}   

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def result = []
	if (state.polling) {
		logDebug "Poll Successful"
		state.polling = false
		result << createEvent(getPresenceEventMap("present"))
		result << createEvent(name: "lastPoll", value: new Date().time, displayed: false, isStateChange: true)
	}
	else {
		writeToInfoLog("Version: $cmd")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	writeToInfoLog("Association: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def parameterName
	switch (cmd.parameterNumber) {
		case 8:
			parameterName = "Sound Level"
			break
		case 5:
			parameterName = "Doorbell Track"
			break
		case 2:
			parameterName = "Sound Repeat Times"
			break
		case 80:
			parameterName = "Device Notification Type"
			break
		case 81:
			parameterName = "Send Low Battery Notifications"
			break
		case 42:
			parameterName = null
			batteryHealthReport(cmd)			
			break
		default:	
			parameterName = "Parameter #${cmd.parameterNumber}"
	}		
	if (parameterName) {
		writeToInfoLog("${parameterName}: ${cmd.configurationValue}")
	} 
}

private batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	
	def batteryValue = (cmd.configurationValue == [0]) ? 100 : 1
	def batteryLevel = (batteryValue == 100) ? "normal" : "low"
		
	sendEvent(name: "battery", value: batteryValue, unit: "%", descriptionText: "${device.displayName}'s battery is $batteryLevel.", isStateChange: true)	
	
	writeToInfoLog("Battery: $batteryValue")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def parse(String description) {	
	def result = null
	if (description.startsWith("Err 106")) {
		def msg = "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again."
		log.warn "$msg"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true, descriptionText: "$msg")		
	}
	else if (description != "updated") {	
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	else {
		logDebug("Did Not Parse: $description")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])

	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def updated() {
	if (!isDuplicateCall(state.lastUpdated, 1)) {
		state.lastUpdated = new Date().time		
		
		def cmds = []
		
		if (!state.useSecureCommands) {
			logDebug "Checking for Secure Command Support"
			state.useSecureCommands = true
			cmds << secureCommand(supportedSecurityGetCmd())
			state.useSecureCommands = false			
		}
		
		cmds += configure()
		
		return response(cmds)
	}
}

//Configuration.configure
def configure() {
	logDebug("Configuration being sent to ${device.displayName}")
	
	initializePreferences()

	def request = []
	
	request << associationSetCommand()	
	request << deviceNotificationTypeSetCommand()
	request << sendLowBatteryNotificationsSetCommand()	
	request << defaultTrackSetCommand(state.bellTrack)
	request << soundRepeatSetCommand(state.soundRepeat)
	request << soundLevelSetCommand(state.soundLevel)	
	
	return secureDelayBetween(request)
}

private reportCommands() {	
	def result = []		
	result << reportDeviceNotificationTypeCommand()
	result += reportAssociationCommands()
	result << reportSoundRepeatCommand()
	result << reportFirmwareCommand()
	result << reportDefaultTrackCommand()
	result << reportVersionCommand()
	result << reportSoundLevelCommand()	
	result << reportBatteryHealthCommand()		
	result << reportSendLowBatteryNotificationsCommand()		
	return result
}

private associationSetCommand() {
	return zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
}
private reportAssociationCommands() {
	return [
		zwave.associationV1.associationGet(groupingIdentifier:1),		
		zwave.associationV1.associationGet(groupingIdentifier:2)
	]
}

private supportedSecurityGetCmd() {	
	return zwave.securityV1.securityCommandsSupportedGet()
}

private reportFirmwareCommand() {
	return zwave.firmwareUpdateMdV2.firmwareMdGet()
}

private reportVersionCommand() {
	return zwave.versionV1.versionGet()	
}

private reportBatteryHealthCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 42)
}

private deviceNotificationTypeSetCommand() {
	// Enable to send notifications to associated devices (Group 1) (0=nothing, 1=hail CC, 2=basic CC report)
	return zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 2)
}
private reportDeviceNotificationTypeCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 80)
}

private sendLowBatteryNotificationsSetCommand() {
	return zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: 1)
}
private reportSendLowBatteryNotificationsCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 81)
}

private soundRepeatSetCommand(newSoundRepeat) {	
	newSoundRepeat = validateSoundRepeat(newSoundRepeat)
	return zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: newSoundRepeat)
}
private reportSoundRepeatCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 2)	
}

private defaultTrackSetCommand(newDefaultTrack) {
	newDefaultTrack = validateTrackNumber(newDefaultTrack)
	return zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: newDefaultTrack)
}
private reportDefaultTrackCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 5)
}

private soundLevelSetCommand(newSoundLevel) {	
	newSoundLevel = validateSoundLevel(newSoundLevel)
	return zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: newSoundLevel)	
}
private reportSoundLevelCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 8)
}

private playTrackCommand(track) {
	track = validateTrackNumber(track)
	return zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: track)
}

private initializePreferences() {	
	clearPlayingStatus()
	state.bellTrack = validateTrackNumber(bellTrack)
	state.alarmTrack = validateTrackNumber(alarmTrack)
	state.toneTrack = validateTrackNumber(toneTrack)
	state.soundLevel = validateSoundLevel(soundLevel)
	state.soundRepeat = validateSoundRepeat(soundRepeat)	
	state.silentButton = validateBooleanPref(silentButton, false)
	state.debugOutput = validateBooleanPref(debugOutput)
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
	def intValue = isNumeric(value) ? value.toInteger() : defaultValue
	def result = intValue
	if (intValue > maxValue) {
		result = maxValue
	} else if (intValue < minValue) {
		result = minValue
	} 
	
	if (result != intValue) {
		logDebug("$value is invalid, defaulting to $result.")
	}
	result
}

private isNumeric(val) {
	return val?.toString()?.isNumber()
}

int minTrack() {
	return 1
}

int maxTrack() {
	return 100
}

private validateBooleanPref(pref, defaultVal=true) {
	if (pref == null) {
		return defaultVal
	}
	else {
		return (pref == true || pref == "true")
	}
}


private secureDelayBetween(commands, delay=200) {
	delayBetween(commands.collect{ secureCommand(it) }, delay)
}

private secureCommand(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCommands) {		
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

private logDebug(msg) {	
	if (state.debugOutput) {
		log.debug msg
	}
}

private handleUnsupportedCommand(cmd) {
	writeToInfoLog("Command $cmd is not supported")
}
 
private writeToInfoLog(msg) {
	log.info "${device.displayName} $msg"
}
