/**
 *  Aeoc Labs Multifunction Siren v 1.0
 *
 *  Capabilities:
 *					Switch, Alarm, Tone
 *
 *	Author: 
 *					Kevin LaFramboise (krlaframboise)
 *
 *	Changelog:
 *
 *	1.0 (02/28/2016)
 *		-	Initial Release
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
 definition (name: "Aeon Labs Multifunction Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
	capability "Actuator"
	capability "Switch"
	capability "Alarm"
	capability "Tone"
	capability "Configuration"

	attribute "status", "enum", ["off", "alarm", "customAlarm", "beep", "beepSchedule", "customBeep", "customBeepSchedule"]

	command "customAlarm", ["number", "number", "number"]
	command "customBeep", ["number", "number", "number", "number", "number"]
	command "startBeep"
	command "startCustomBeep", ["number", "number", "number", "number", "number", "number", "number"]

	command "customBeep1"
	command "customBeep2"
	command "customBeep3"
	command "customBeep4"
	command "customBeep5"
	command "customBeep6"

	fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x85,0x59,0x72,0x2B,0x2C,0x86,0x7A,0x73", outClusters: "0x5A,0x82"
 }

 simulator {
	reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
	reply "988100200100,9881002002": "command: 9881, payload: 00200300"
 }

	preferences {
		input "sirenSound", "number", 
			title: "Siren Sound (1-5)", 
			defaultValue: 1, 
			range: "1..5",
			displayDuringSetup: true, 
			required: false
		input "sirenVolume", "number", 
			title: "Siren Volume (1-3)", 
			defaultValue: 1, 
			range: "1..3",
			displayDuringSetup: true, 
			required: false
		input "alarmDuration", "number", 
			title: "Turn siren off after: (seconds)", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "beepSound", "number", 
			title: "Beep Sound (1-5)", 
			defaultValue: 3, 
			range: "1..5",
			displayDuringSetup: false, 
			required: false
		input "beepVolume", "number", 
			title: "Beep Volume (1-3)", 
			defaultValue: 1, 
			range: "1..3",
			displayDuringSetup: false, 
			required: false
		input "beepRepeat", "number", 
			title: "Beep Repeat (1-100)", 
			defaultValue: 1, 
			range: "1..100",
			displayDuringSetup: false, 
			required: false
		input "beepRepeatDelay", "number", 
			title: "Time Between Beeps in Milliseconds", 
			defaultValue: 1000, 
			displayDuringSetup: false, 
			required: false
		input "beepLength", "number", 
			title: "Length of Beep in Milliseconds", 
			defaultValue: 100, 
			displayDuringSetup: false, 
			required: false
		input "beepEvery", "number", 
			title: "Scheduled Beep Every (seconds)", 
			defaultValue: 10,
			displayDuringSetup: false,
			required: false
		input "beepStopAfter", "number", 
			title: "Stop Scheduled Beep After (seconds)", 
			defaultValue: 60,
			displayDuringSetup: false,
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "alarm", label:'Alarm Sounding!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "customAlarm", label:'Custom Alarm Sounding!', action: "off", icon:"", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"
				attributeState "beepSchedule", label:'Scheduled\nBeeping!', action: "off", icon:"", backgroundColor:"#99FF99"
				attributeState "customBeep", label:'Custom Beeping!', action: "off", icon:"", backgroundColor:"#694489"
				attributeState "customBeepSchedule", label:'Scheduled Custom Beeping!', action: "off", icon:"", backgroundColor:"#694489"
			}
		}
		standardTile("playAlarm", "device.alarm", label: 'Alarm', width: 2, height: 2) {
			state "default", label:'Alarm', action: "both", icon:"st.alarm.alarm.alarm", backgroundColor: "#ff9999"
			state "both", label:'Stop', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor: "#ffffff"
		}
		standardTile("playBeep", "device.status", label: 'Beep', width: 2, height: 2) {
			state "default", label:'Beep', action:"beep", icon:"st.Entertainment.entertainment2", backgroundColor: "#99FF99"
		}
		valueTile("playBeepSchedule", "device.status", label: 'Scheduled Beep', width: 2, height: 2) {
			state "default", label:'Scheduled\nBeep', action:"startBeep",backgroundColor: "#99FF99"
			state "beepSchedule", label:'Stop Schedule', action:"off", icon: "", backgroundColor: "#ffffff"
		}
		valueTile("playCustomBeep1", "device.status", label: 'Custom Beep 1', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 1', action:"customBeep1",backgroundColor: "#694489"
		}
		valueTile("playCustomBeep2", "device.status", label: 'Custom Beep 2', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 2', action:"customBeep2",backgroundColor: "#694489"
		}
		valueTile("playCustomBeep3", "device.status", label: 'Custom Beep 3', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 3', action:"customBeep3",backgroundColor: "#694489"
		}
		valueTile("playCustomBeep4", "device.status", label: 'Custom Beep 4', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 4', action:"customBeep4",backgroundColor: "#694489"
		}
		valueTile("playCustomBeep5", "device.status", label: 'Custom Beep 5', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 5', action:"customBeep5",backgroundColor: "#694489"
		}
		valueTile("playCustomBeep6", "device.status", label: 'Custom Beep 6', width: 2, height: 2, wordWrap: true) {
			state "default", label:'Custom\nBeep 6', action:"customBeep6",backgroundColor: "#694489"
		}
		main "status"
		details(["status", "playAlarm", "playBeep", "playBeepSchedule", "playCustomBeep1", "playCustomBeep2", "playCustomBeep3", "playCustomBeep4", "playCustomBeep5", "playCustomBeep6"])
	}
}

// Turns on siren and strobe
def on() {
	both()
}

// Turns off siren and strobe
def off() {
	secureDelayBetween([
		offSetCmd(),
		offGetCmd()
	])
}

// Turns on siren and strobe
def strobe() {
	both()
}

// Turns on siren and strobe
def siren() {
	both()
}

// Turns on siren and strobe
def both() {
	logDebug "Command both() called"
	state.currentStatus = "alarm"
	sendCurrentStatusEvent(false)
	sendEvent(name:"alarm", value: "both", isStateChange: true)
	sendEvent(name:"switch", value: "on", isStateChange: true, displayed: false)

	customAlarm(settings.sirenSound, settings.sirenVolume, settings.alarmDuration)
}

// Plays sound at volume for duration.
def customAlarm(sound, volume, duration) {
	if (state.currentStatus != "alarm") {
		logDebug "Command customAlarm($sound, $volume, $duration) called"
		state.currentStatus = "customAlarm"
		sendCurrentStatusEvent()
	}

	sound = validateSound(sound)
	volume = validateVolume(volume)
	duration = validateRange(duration, 0, 0, Integer.MAX_VALUE, "Alarm Duration")

	def cmds = []
	cmds << secureCmd(sirenSoundVolumeSetCmd(sound, volume))

	if (duration > 0) {
		cmds << "delay ${duration * 1000}"
		cmds += off()
	}

	return cmds
}

// Plays the default beep.
def beep() {
	logDebug "Command beep() called"
	state.currentStatus = "beep"
	sendCurrentStatusEvent()

	customBeep(
		settings.beepSound,
		settings.beepVolume,
		settings.beepRepeat,
		settings.beepRepeatDelay,
		settings.beepLength
	)
}

// Plays short beep.
def customBeep1() {
	customBeep(3, 1, 1, 1, 100)
}

// Plays medium beep
def customBeep2() {
	customBeep(3, 1, 1, 1, 250)
}

// Plays long beep
def customBeep3() {
	customBeep(3, 1, 1, 1, 500)
}

// Plays 3 short beeps
def customBeep4() {
	customBeep(3, 1, 3, 100, 100)
}

// Plays 3 medium beeps
def customBeep5() {
	customBeep(3, 1, 3, 250 , 500)
}

// Plays 3 long beeps
def customBeep6() {
	customBeep(3, 1, 3, 500, 250)
}

// Repeatedly plays the default beep based on the beepEvery and beepStopAfter settings.
def startBeep() {
	logDebug "Command startBeep() called"
	state.currentStatus = "beepSchedule"

	startCustomBeep(
		settings.beepEvery,
		settings.beepStopAfter,
		settings.beepSound,
		settings.beepVolume,
		settings.beepRepeat,
		settings.beepRepeatDelay,
		settings.beepLength
	)
}

// Repeatedly plays specified beep at specified in specified intervals.
def startCustomBeep(beepEverySeconds, stopAfterSeconds, sound, volume, repeat=1, repeatDelayMS=1000, beepLengthMS=100) {
	state.beepSchedule = [
		"startTime": (new Date().time),
		"beepEvery": validateBeepEvery(beepEverySeconds),
		"stopAfter": validateBeepStopAfter(stopAfterSeconds),
		"sound": sound,
		"volume": volume,
		"repeat": repeat,
		"repeatDelay": repeatDelayMS,
		"beepLength": beepLengthMS
	]

	if (state.currentStatus != "beepSchedule") {
		logDebug "Command startCustomBeep(${state.beepSchedule}) called"
		state.currentStatus = "customBeepSchedule"
	}
	sendCurrentStatusEvent()

	return scheduledCustomBeep()
}

private scheduledCustomBeep() {
	def beepSchedule = state.beepSchedule

	def cmds = []
	if (scheduleStillActive(beepSchedule.startTime, beepSchedule.stopAfter)) {
		cmds += customBeep(
			beepSchedule.sound,
			beepSchedule.volume,
			beepSchedule.repeat,
			beepSchedule.repeatDelay,
			beepSchedule.beepLength
		)
	}

	if (nextScheduleStillActive(beepSchedule.startTime, beepSchedule.stopAfter, beepSchedule.beepEvery)) {
		cmds << "delay ${beepSchedule.beepEvery * 1000}"
		cmds << secureCmd(zwave.basicV1.basicGet())
	}
	else {
		cmds += off()
	}
	return cmds
}

private nextScheduleStillActive(startTime, stopAfter, runEvery) {
	def adjustedStartTime = (startTime - (runEvery * 1000))
	return scheduleStillActive(adjustedStartTime, stopAfter)
}

private scheduleStillActive(startTime, stopAfter) {
	def endTimeMS = startTime + (stopAfter * 1000)
	return (new Date().time < endTimeMS) 
}

// Plays specified beep.
def customBeep(sound, volume, repeat=1, repeatDelayMS=1000, beepLengthMS=100) {
	logDebug "Command customBeep($sound, $volume, $repeat, $repeatDelayMS, $beepLengthMS) called"

	if (state.currentStatus == "off") {
		state.currentStatus = "customBeep"
		sendCurrentStatusEvent()
	}

	int maxMS = 18000
	sound = validateSound(sound, 3)
	volume = validateVolume(volume)
	beepLengthMS = validateRange(beepLengthMS, 100, 1, maxMS, "Beep Length")
	repeatDelayMS = validateRepeatDelay(repeatDelayMS, beepLengthMS, maxMS)
	repeat = validateRepeat(repeat, beepLengthMS, repeatDelayMS, maxMS)

	def cmds = []
	for (int repeatIndex = 1; repeatIndex <= repeat; repeatIndex++) {
		cmds << secureCmd(sirenSoundVolumeSetCmd(sound, volume))
		cmds << "delay $beepLengthMS"
		cmds << secureCmd(offSetCmd())
		if (repeat != 1) {
			cmds << "delay $repeatDelayMS"
		}
	}

	if (!isBeepScheduleStatus(state.currentStatus)) {
		cmds += off()
	}
	return cmds
}

private sendCurrentStatusEvent(display=true) {
	sendEvent(name:"status", value: state.currentStatus, isStateChange: true, displayed: display)
}

private isBeepScheduleStatus(status) {
	return (status in ["beepSchedule", "customBeepSchedule"])
}

// Checks if the device supports security commands.
def configure() {
	logDebug "Checking for secure inclusion"
	state.useSecureCommands = null

	def cmds = secureDelayBetween([
		supportedSecurityGetCmd(),
		sendNotificationsSetCmd()
	])

	state.useSecureCommands = false
	response(cmds)
}

// Stores preferences and displays device settings.
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time
		state.debugOutput = validateBool(debugOutput, true)
		logDebug "Updating"

		def cmds = []
		if (!state.useSecureCommands) {
			logDebug "Checking for Secure Command Support"
			state.useSecureCommands = null
			cmds << secureCmd(supportedSecurityGetCmd())
		}
		cmds << secureCmd(zwave.firmwareUpdateMdV2.firmwareMdGet())
		cmds += off()
		response(cmds)
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private sirenSoundVolumeSetCmd(int sound, int volume) {
	zwave.configurationV1.configurationSet(parameterNumber: 37, size: 2, configurationValue: [validateSound(sound), validateVolume(volume)])
}

private sendNotificationsSetCmd() {
	zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 0)
}

private offSetCmd() {
	zwave.switchBinaryV1.switchBinarySet(switchValue: 0)
}

private offGetCmd() {
	zwave.switchBinaryV1.switchBinaryGet()
}

private supportedSecurityGetCmd() {
	zwave.securityV1.securityCommandsSupportedGet()
}

// Parses incoming message warns if not paired securely
def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		state.useSecureCommands = false
		def msg = "Secure Inclusion Failed.  You may need to remove and add the device again while pushing the action button repeatedly during the Inclusion process."
		log.warn "$msg"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true, descriptionText: "$msg")
	}
	else if (description != null && description != "updated") {
		def cmd = zwave.parse(description, [0x98: 1, 0x20: 1, 0x70: 1, 0x7A: 2, 0x25: 1])

		if (cmd != null) {
			result = zwaveEvent(cmd)
		} 
	}
	result
}

// Unencapsulates the secure command.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	if (cmd != null) {
		def encapCmd = cmd.encapsulatedCommand([0x20: 1, 0x85: 2, 0x70: 1, 0x7A: 2, 0x25: 1])

		if (encapCmd) {
			zwaveEvent(encapCmd)
		}
	}
}

// Enables secure command setting.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCommands = true
	logInfo "Secure Commands Supported"
}

// Writes firmware to the Info Log.
def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
	logInfo "Firmware: $cmd"
}   

// Handles device reporting off
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	if (cmd.value == 0) {
		def alarmActive = (state.currentStatus == "alarm")
		state.currentStatus = "off"
		[
			createOffEvent("alarm", alarmActive),
			createOffEvent("status", !alarmActive),
			createOffEvent("switch", false)
		]
	}
}

private createOffEvent(eventName, displayEvent) {
	createEvent(name: eventName, value: "off", isStateChange: true, displayed: displayEvent)
}

// Handles the scheduling of beeps.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (isBeepScheduleStatus(state.currentStatus)) {
		return [response(scheduledCustomBeep())]
	}
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

private secureDelayBetween(cmds, delay=100) {
	delayBetween(cmds.collect{ secureCmd(it) }, delay)
}

private secureCmd(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCommands == null || state.useSecureCommands) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		cmd.format()
	}
}

private int validateSound(sound, int defaultSound=1) {
	return validateRange(sound, defaultSound, 1, 5, "Sound")
}

private int validateVolume(volume, int defaultVolume=1) {
	return validateRange(volume, defaultVolume, 1, 3, "Volume")
}

private int validateRepeatDelay(repeatDelayMS, int beepLengthMS, int maxMS) {
	int repeatDelayMaxMS = (beepLengthMS == maxMS) ? 1 : (maxMS - beepLengthMS)
	return validateRange(repeatDelayMS, 1000, 1, repeatDelayMaxMS, "Repeat Delay")
}

private int validateRepeat(repeat, int beepLengthMS, int repeatDelayMS, int maxMS) {
	int combinedMS = (beepLengthMS + repeatDelayMS)
	int maxRepeat = (combinedMS >= maxMS) ? 1 : (maxMS / combinedMS).toInteger()
	return validateRange(repeat, 1, 1, maxRepeat, "Repeat")
}

private int validateBeepEvery(seconds) {
	validateRange(seconds, 10, 5, Integer.MAX_VALUE, "Beep Every")
}

private int validateBeepStopAfter(seconds) {
	validateRange(seconds, 60, 10, Integer.MAX_VALUE, "Beep Stop After")
}

private int validateRange(val, defaultVal, minVal, maxVal, desc) {
	def result = val
	if (!val) {
		result = defaultVal
	} else if (val > maxVal) {
		result = maxVal
	} else if (val < minVal) {
		result = minVal
	} 

	if (result != val) {
		logDebug("$desc: $val is invalid, defaulting to $result.")
	}
	result
}

private validateBool(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} - $msg"
}
