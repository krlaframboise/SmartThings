/**
 *  Aeotec Doorbell v 1.2
 *
 *  Capabilities:
 *					Switch, Alarm, Tone, Battery, Configuration
 *
 *	Author: 
 *					Kevin LaFramboise (krlaframboise)
 *
 *	Changelog:
 *
 *	1.2 (02/17/2016)
 *		-	Fixed bug causing error on install.
 *
 *	1.1 (02/15/2016)
 *		-	Consolidated code.
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
		controlTile("doorbellSlider", "device.doorbellTrack", "slider", width: 4, height: 1, range: "(1..100)") {
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
		controlTile("beepSlider", "device.beepTrack", "slider", width: 4, height: 1, range: "(1..100)") {
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
		controlTile("alarmSlider", "device.alarmTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "alarmTrack", action:"setAlarmTrack"
		}
		valueTile("battery", "device.battery", decoration: "flat", height:2, width:2) {
			state "default", label: 'Battery\n${currentValue}%'
		}
		main "status"
		details(["status", "volume", "volumeSlider", "repeat", "repeatSlider", "playDoorbell", "doorbellTrack","doorbellSlider",  "playBeep", "beepTrack", "beepSlider", "playAlarm", "alarmTrack", "alarmSlider", "battery"])
	}
}

// Stops playing track and raises events switch.off, alarm.off, status.off
def off() {
	secureDelayBetween([
		deviceNotifyTypeSetCmd(true),
		zwave.basicV1.basicSet(value: 0x00)])
}

// Plays doorbellTrack and raises switch.on event
def on() {
	//[secureCommand(zwave.basicV1.basicSet(value: 0xFF))]
	sendDoorbellEvents()
	secureDelayBetween([
		deviceNotifyTypeSetCmd(false),
		zwave.basicV1.basicSet(value: 0xFF),
		deviceNotifyTypeSetCmd(true)])
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def strobe() {
	both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def siren() {
	both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def both() {
	playTrack(getAttr("alarmTrack"), "alarm", "Alarm Sounding!")
}

// Plays beepTrack and raises status.beep event
def beep() {
	playTrack(getAttr("beepTrack"), "beep", "Beeping!")
}

// Plays specified track and raises status.play event
def playTrack(track) {
	playTrack(track, "play", "Playing Track $track!")
}

// Plays specified track and raises specified status event
def playTrack(track, status, desc) {
	logDebug("$desc")
	def descText = "${device.displayName} $desc"

	if (status == "alarm") {
		sendEvent(name: "alarm", value: "both", descriptionText: descText)
	}

	sendEvent(name: "status", value: status, descriptionText: descText, isStateChange: true)

	track = validateTrack(track)
	secureDelayBetween([
		deviceNotifyTypeSetCmd(false),
		configSetCmd(6, track),
		deviceNotifyTypeSetCmd(true)])
}

// Handles device reporting on and off
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		handleDeviceTurningOff()
	} 
	else if (cmd.value == 255) {
		sendDoorbellEvents()
	}
}

// Raises events switch.off, alarm.off, and status.off
def handleDeviceTurningOff() {
	[
		createEvent(name:"status", value: "off", isStateChange: true),
		createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off", isStateChange: true, displayed: false),
		createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off", isStateChange: true, displayed: false)
	]
}

private sendDoorbellEvents() {
	def desc = "Doorbell Ringing!"
	logDebug("$desc")
	sendEvent(name: "status", value: "doorbell", descriptionText: "${device.displayName} $desc", isStateChange: true)
	sendEvent(name: "switch", value: "on", displayed: false, isStateChange: true)
}

// Checks battery level if hasn't been checked recently
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug("WakeUpNotification: $cmd")

	def result = []
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)

	// Request every 24 hours
	if (!state.lastBatteryReport || (new Date().time) - state.lastBatteryReport > 24*60*60*1000) {
		result << response(batteryHealthGetCmd())
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	result
}

// Raises battery event and writes level to Info Log
private batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	def batteryValue = (cmd.configurationValue == [0]) ? 100 : 1
	def batteryLevel = (batteryValue == 100) ? "normal" : "low"

	sendEvent(name: "battery", value: batteryValue, unit: "%", descriptionText: "$batteryLevel", isStateChange: true)
	logInfo("Battery: $batteryValue")
}

// Writes parameter settings to Info Log
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def name
	switch (cmd.parameterNumber) {
		case 8:
			name = "Volume"
			break
		case 5:
			name = "Doorbell Track"
			break
		case 2:
			name = "Repeat Times"
			break
		case 80:
			name = "Device Notification Type"
			break
		case 81:
			name = "Send Low Battery Notifications"
			break
		case 42:
			name = null
			batteryHealthReport(cmd)
			break
		default:
			name = "Parameter #${cmd.parameterNumber}"
	}
	if (name) {
		logInfo("${name}: ${cmd.configurationValue}")
	} 
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

// Parses incoming message warns if not paired securely
def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		def msg = "This sensor failed to complete the network security key exchange. You may need to remove and re-add the device or disable Use Secure Commands in the settings"
		log.warn "$msg"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true, descriptionText: "$msg")
	}
	else if (description != "updated") {
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		} 
		else {
			logDebug("No Command: $cmd")
		}
	}
	else {
		logDebug("Did Not Parse: $description")
	}
	result
}

// Unencapsulates the secure command.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
	if (encapCmd) {
		zwaveEvent(encapCmd)

	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

// Sends configuration to device
def updated() {
	response(configure())
}

// Sends secure configuration to device
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	 response(configure())
}

// Initializes variables and sends settings to device
def configure() {
	log.debug "Sending configuration to ${device.displayName}"

	state.debugOutput = validateBool(debugOutput)
	state.useSecureCommands = validateBool(useSecureCommands)

	setDoorbellTrack(getAttr("doorbellTrack"))
	setAlarmTrack(getAttr("alarmTrack"))
	setBeepTrack(getAttr("beepTrack"))
	setVolume(getAttr("volume"))
	setRepeat(getAttr("repeat"))

	secureDelayBetween([
		assocSetCmd(),
		deviceNotifyTypeSetCmd(true),
		sendLowBatterySetCmd(),
		doorbellGetCmd(),
		repeatGetCmd(),
		volumeGetCmd(),
		batteryHealthGetCmd()
	])
}

// Sets volume attribute and device setting
def setVolume(volume) {
	volume = validateRange(volume, 5, 1, 10)
	sendAttrChangeEvent("volume", volume)
	[secureCommand(configSetCmd(8, volume))]
}

// Sets repeat attribute and device setting
def setRepeat(repeat) {
	repeat = validateRange(repeat, 1, 1, 100)
	sendAttrChangeEvent("repeat", repeat)
	[secureCommand(configSetCmd(2, repeat))]
}

// Sets doorbellTrack attribute and setting
def setDoorbellTrack(track) {
	track = validateTrack(track)
	sendAttrChangeEvent("doorbellTrack", track)
	[secureCommand(configSetCmd(5, track))]
}

// Sets beepTrack attribute
def setBeepTrack(track) {
	sendAttrChangeEvent("beepTrack", validateTrack(track))
}

// Sets alarmTrack attribute
def setAlarmTrack(track) {
	sendAttrChangeEvent("alarmTrack", validateTrack(track))
}

private sendAttrChangeEvent(attrName, attrVal) {
	sendEvent(name: attrName, value: attrVal, descriptionText: "${device.displayName} $attrName set to $attrVal", displayed: false)
}

int validateTrack(track) {
	validateRange(track, 1, 1, 100)
}

int validateRange(val, defaultVal, minVal, maxVal) {
	def result = val
	if (!val) {
		result = defaultVal
	} else if (val > maxVal) {
		result = maxVal
	} else if (val < minVal) {
		result = minVal
	} 

	if (result != val) {
		logDebug("$val is invalid, defaulting to $result.")
	}
	result
}

private validateBool(pref) {
	(pref == true || pref == "true")
}

private assocSetCmd() {
	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
}

private batteryHealthGetCmd() {
	configGetCmd(42)
}

private deviceNotifyTypeSetCmd(notify) {
	// 0=nothing, 1=hail, 2=basic
	configSetCmd(80, (notify ? 2 : 0))
}

private sendLowBatterySetCmd() {
	configSetCmd(81, 1)
}

private repeatGetCmd() {
	configGetCmd(2)
}

private doorbellGetCmd() {
	configGetCmd(5)
}

private volumeGetCmd() {
	configGetCmd(8)
}

private configGetCmd(int paramNum) {
	zwave.configurationV1.configurationGet(parameterNumber: paramNum)
}
private configSetCmd(int paramNum, int val) {
	zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val)
}

private getAttr(attrName) {
	device.currentValue(attrName)
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

private logDebug(msg) {
	if (state.debugOutput) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} $msg"
}
