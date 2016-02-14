/**
 *  Aeotec Doorbell v 1.0
 *
 *  Capabilities:
 *					Switch, Alarm, Tone, Battery, Configuration
 *
 *	Author: 
 *					Kevin LaFramboise (krlaframboise)
 *
 *	Changelog:
 *
 *	1.0 (02/14/2016)
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
	definition (name: "Aeotec Doorbell", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"			
		capability "Switch"
		capability "Alarm"	
		capability "Tone"		
		capability "Battery"		
				
		attribute "alarmTrack", "number"
		attribute "beepTrack", "number"
		attribute "doorbellTrack", "number"
		attribute "repeat", "number"		
		attribute "status", "enum", ["off", "doorbell", "beep", "alarm", "play"]
		attribute "volume", "number"
		
		command "playTrack", ["number"]
		command "setAlarmTrack", ["number"]
		command "setBeepTrack", ["number"]
		command "setDoorbellTrack", ["number"]		
		command "setRepeat", ["number"]	
		command "setVolume", ["number"]
		
		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98"
	}

	simulator {
	}

	preferences {
		input "useSecureCommands", "bool", 
			title: "Use Secure Commands?\n(Leave On unless you're unable to get Include Secure to work.",
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false				
	}	
	
	tiles(scale: 2) {		
		standardTile("status", "device.status", label: '', width: 2, height: 2) {
			state "off", 
				label:'Off', 
				action: "on", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#ffffff"
			state "doorbell", 
				label:'Doorbell!', 
				action: "off", 
				icon:"st.Home.home30", 
				backgroundColor:"#99c2ff"
			state "alarm", 
				label:'Alarm!', 
				action: "off", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor:"#ff9999"
			state "beep", 
				label:'Beeping!', 
				action: "off", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor:"#99FF99"
			state "play", 
				label:'Playing!', 
				action: "off", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor:"#694489"
		}		
		valueTile("volume", "device.volume", decoration: "flat", height:1, width:2) {
			state "volume", label: 'Volume ${currentValue}'
		}
		controlTile("volumeSlider", "device.volume", "slider", height: 1, width: 2, range: "(1..10)") {
			state "volume", action:"setVolume"
		}
		valueTile("repeat", "device.repeat", decoration: "flat", height:1, width:2) {
			state "repeat", label: 'Repeat ${currentValue}'
		}
		controlTile("repeatSlider", "device.repeat", "slider", height: 1, width: 2, range: "(1..25)") {
			state "repeat", action:"setRepeat"
		}		
		standardTile("playDoorbell", "device.switch", label: 'Doorbell', width: 2, height: 2) {
			state "default", 
			label:'Doorbell', 
			action:"on", 
			icon:"st.Home.home30", 
			backgroundColor: "#99c2ff"
		}
		valueTile("doorbellTrack", "device.doorbellTrack", decoration: "flat", width:4, height: 1) {
			state "doorbellTrack", label: 'Doorbell Track: ${currentValue}'
		}
		controlTile("doorbellTrackSlider", "device.doorbellTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "doorbellTrack", action:"setDoorbellTrack"
		}			
		standardTile("playBeep", "device.tone", label: 'Beep', width: 2, height: 2) {
			state "default", 
			label:'Beep', 
			action:"beep", 
			icon:"st.Entertainment.entertainment2", 
			backgroundColor: "#99FF99"
		}
		valueTile("beepTrack", "device.beepTrack", decoration: "flat", height:1, width:4) {
			state "beepTrack", label: 'Beep Track: ${currentValue}'
		}
		controlTile("beepTrackSlider", "device.beepTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "beepTrack", action:"setBeepTrack"
		}		
		standardTile("playAlarm", "device.alarm", label: 'Alarm', width: 2, height: 2) {
			state "default", 
			label:'Alarm', 
			action: "both", 
			icon:"st.alarm.alarm.alarm", 
			backgroundColor: "#ff9999"
		}		
		valueTile("alarmTrack", "device.alarmTrack", decoration: "flat", height:1, width:4) {
			state "alarmTrack", label: 'Alarm Track: ${currentValue}'
		}
		controlTile("alarmTrackSlider", "device.alarmTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "alarmTrack", action:"setAlarmTrack"
		}		
		valueTile("battery", "device.battery", decoration: "flat", height:2, width:2) {
			state "default", label: 'Battery\n${currentValue}%'
		}	
		main "status"
		details(["status", "volume", "volumeSlider", "repeat", "repeatSlider", "playDoorbell", "doorbellTrack","doorbellTrackSlider",  "playBeep", "beepTrack", "beepTrackSlider", "playAlarm", "alarmTrack", "alarmTrackSlider", "battery"])
	}
}

// Stops the track that's playing and raises the events switch.off, alarm.off, status.off.
def off() {		
	return secureDelayBetween([
		deviceNotificationTypeSetCommand(true),
		zwave.basicV1.basicSet(value: 0x00)])
}

// Plays the doorbell track and raises the switch.on event.
def on() {
	sendDoorbellEvents()
	return secureDelayBetween([
		deviceNotificationTypeSetCommand(false),
		zwave.basicV1.basicSet(value: 0xFF),
		deviceNotificationTypeSetCommand(true)])		
}

// Plays the track specified in the Alarm Track Preference and raises the events alarm.both and status.alarm.
def strobe() {
	both()
}

// Plays the track specified in the Alarm Track Preference and raises the events alarm.both and status.alarm.
def siren() {
	both()
}

// Plays the track specified in the Alarm Track Preference and raises the events alarm.both and status.alarm.
def both() {		
	return playTrack(device.currentValue("alarmTrack"), "alarm", "Alarm Sounding!")	
}

// Plays the track specified in the Beep Track preference and raises the status.beep event.
def beep() {	
	playTrack(device.currentValue("beepTrack"), "beep", "Beeping!")
}

// Plays the specified track and raises the event status.play.
def playTrack(track) {
	playTrack(track, "play", "Playing Track $track!")	
}

// Plays the specified track and raises the specified status event with the specified description.
def playTrack(track, status, desc) {
	writeToDebugLog("$desc")
	def descText = "${device.displayName} $desc"
	
	if (status == "alarm") {
		sendEvent(name: "alarm", value: "both", descriptionText: descText)	
	}
	
	sendEvent(name: "status", value: status, descriptionText: descText, isStateChange: true)
					
	track = validateTrackNumber(track)
	return secureDelayBetween([
		deviceNotificationTypeSetCommand(false),
		playTrackCommand(track),
		deviceNotificationTypeSetCommand(true)])	
}

// Handles the device reporting on and off.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {			
	if (cmd.value == 0) {		
		return handleDeviceTurningOff()
	} 
	else if (cmd.value == 255) {				
		sendDoorbellEvents()
	}	
}

// Raises events switch.off, alarm.off, and status.off.  
def handleDeviceTurningOff() {		
	return [	
		createEvent(name:"status", value: "off", isStateChange: true),
		createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off", isStateChange: true, displayed: false),
		createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off", isStateChange: true, displayed: false)
	]
}

private sendDoorbellEvents() {
	def desc = "Doorbell Ringing!"	
	writeToDebugLog("$desc")	
	sendEvent(name: "status", value: "doorbell", descriptionText: "${device.displayName} $desc", isStateChange: true)	
	sendEvent(name: "switch", value: "on", displayed: false, isStateChange: true)	
}

// Checks the battery level if it hasn't been checked recently.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	writeToDebugLog("WakeUpNotification: $cmd")
	
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

// Raises the battery event with its level and writes it to the Info log.
private batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	
	def batteryValue = (cmd.configurationValue == [0]) ? 100 : 1
	def batteryLevel = (batteryValue == 100) ? "normal" : "low"
			
	sendEvent(name: "battery", value: batteryValue, unit: "%", descriptionText: "$batteryLevel", isStateChange: true)	
	
	writeToInfoLog("Battery: $batteryValue")
}

// Writes configuration settings for a particular parameter to the Info log.
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def parameterName
	switch (cmd.parameterNumber) {
		case 8:
			parameterName = "Volume"
			break
		case 5:
			parameterName = "Doorbell Track"
			break
		case 2:
			parameterName = "Repeat Times"
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

// Writes any unexpected commands to the debug log.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	writeToDebugLog("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

// Parses the incoming message into commands and raises secureInclusion.failed event if the device did not pair securely.  
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
		else {
			writeToDebugLog("No Command: $cmd")
		}
	}
	else {
		writeToDebugLog("Did Not Parse: $description")
	}
	return result
}

// Unencapsulates the secure command.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

// Initializes all state variables and sends the settings to the device.
def updated() {	
	return response(configure())
}

// Sends secure configuration to device.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	return response(configure())
}

// Initializes all state variables and sends the settings to the device.
def configure() {
	log.debug "Configuration being sent to ${device.displayName}"
	
	state.debugOutput = validateBooleanPref(debugOutput)
	state.useSecureCommands = validateBooleanPref(useSecureCommands)
	
	setDoorbellTrack(device.currentValue("doorbellTrack"))
	setAlarmTrack(device.currentValue("alarmTrack"))
	setBeepTrack(device.currentValue("beepTrack"))
	setVolume(device.currentValue("volume"))
	setRepeat(device.currentValue("repeat"))		
			
	def result = [
		associationSetCommand(),
		deviceNotificationTypeSetCommand(true),
		sendLowBatteryNotificationsSetCommand(),	
		reportDoorbellTrackCommand(),
		reportRepeatCommand(),
		reportVolumeCommand(),
		reportBatteryHealthCommand()
	]	
	return secureDelayBetween(result)
}

// Sets the device's volume setting and attribute.
def setVolume(volume) {	
	volume = validateNumberRange(volume, 5, 1, 10)
	sendEvent(name: "volume", value: "$volume", descriptionText: "${device.displayName} Volume is $volume", isStateChange: true)	
	return [secureCommand(volumeSetCommand(volume))]	
}

// Sets the device's repeat setting and attribute.
def setRepeat(repeat) {
	repeat = validateNumberRange(repeat, 1, 1, 100)
	sendEvent(name: "repeat", value: "$repeat", descriptionText: "${device.displayName} Repeat is $repeat", isStateChange: true)	
	return [secureCommand(repeatSetCommand(repeat))]	
}

// Sets the device's doorbell track setting and attribute.
def setDoorbellTrack(track) {
	track = validateTrackNumber(track)
	sendEvent(name: "doorbellTrack", value: "$track", descriptionText: "${device.displayName} Doorbell Track is $track", displayed: false)	
	return [secureCommand(doorbellTrackSetCommand(track))]	
}

// Sets the device's beep track attribute.
def setBeepTrack(track) {
	track = validateTrackNumber(track)
	sendEvent(name: "beepTrack", value: "$track", descriptionText: "${device.displayName} Beep Track is $track", displayed: false)	
}

// Sets the device's alarm track attribute.
def setAlarmTrack(track) {
	track = validateTrackNumber(track)
	sendEvent(name: "alarmTrack", value: "$track", descriptionText: "${device.displayName} Alarm Track is $track", displayed: false)	
}

int validateTrackNumber(track) {
	validateNumberRange(track, 1, 1, 100)
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

private validateBooleanPref(pref) {
	return (pref == true || pref == "true")	
}

private reportBatteryHealthCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 42)
}

private associationSetCommand() {
	return zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
}

private deviceNotificationTypeSetCommand(reportNotifications) {
	// Enable to send notifications to associated devices (Group 1) (0=nothing, 1=hail CC, 2=basic CC report)
	def value = reportNotifications ? 2 : 0	
	return zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: value)
}

private sendLowBatteryNotificationsSetCommand() {	
	return zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: 1)
}

private repeatSetCommand(repeat) {		
	return zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: repeat)
}

private reportRepeatCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 2)	
}

private doorbellTrackSetCommand(track) {
	return zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: track)
}

private reportDoorbellTrackCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 5)
}

private volumeSetCommand(volume) {	
	return zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: volume)	
}

private reportVolumeCommand() {
	return zwave.configurationV1.configurationGet(parameterNumber: 8)
}

private playTrackCommand(track) {	
	return zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: track)
}

private secureDelayBetween(commands, delay=100) {	
	delayBetween(commands.collect{ secureCommand(it) }, delay)
}

private secureCommand(physicalgraph.zwave.Command cmd) {		
	if (state.useSecureCommands) {		
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {		
		cmd.format()
	}
}

private writeToDebugLog(msg) {
	if (state.debugOutput) {
		log.debug "$msg"
	}	
}

private writeToInfoLog(msg) {
	log.info "${device.displayName} $msg"
}
