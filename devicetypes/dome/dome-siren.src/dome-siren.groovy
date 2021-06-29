/*
 *  Dome Siren v2.0
 *
 *  Changelog:
 *
 *    2.0 (06/13/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 Dome
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x59: 1,  // AssociationGrpInfo
	0x5A: 1,  // DeviceResetLocally
	0x5E: 2,  // ZwaveplusInfo
	0x70: 1,  // Configuration
	0x71: 3,  // Notification v4
	0x72: 2,  // ManufacturerSpecific
	0x73: 1,  // Powerlevel
	0x80: 1,  // Battery
	0x85: 2,  // Association
	0x86: 1,  // Version (2)
	0x87: 1,  // Indicator
	0x25: 1,  // Switch Binary
]

@Field static int sirenMode = 1

@Field static int notificationTypeSiren = 14

@Field static Map configParams = [
	sirenVolume: [num: 1, attr: "sirenVolume"],
	sirenLength: [num: 2, attr: "sirenLength"],
	chimeRepeat: [num: 3, attr: "chimeRepeat"],
	chimeVolume: [num: 4, attr: "chimeVolume"],
	sirenSound: [num: 5, attr: "sirenSound"],
	mode: [num: 7],
	sirenLed: [num: 8],
	chimeLed: [num: 9, attr: "chimeLed"]
]


metadata {
	definition (
		name: "Dome Siren",
		namespace: "Dome",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "x.com.st.d.siren",
		mnmn: "SmartThingsCommunity",
		vid: "a3dae490-2fe1-3d7a-9125-c7b8cfb55b5e"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Alarm"
		capability "Chime"
		capability "Battery"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
		capability "platemusic11009.domeSirenSound"
		capability "platemusic11009.domeSirenVolume"
		capability "platemusic11009.domeSirenLength"
		capability "platemusic11009.domeChimeSound"
		capability "platemusic11009.domeChimeVolume"
		capability "platemusic11009.domeChimeRepeat"
		capability "platemusic11009.domeChimeLed"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"021F", prod:"0003", model:"0088", deviceJoinName: "Dome Siren"
	}

	simulator { }

	preferences {
		input "debugOutput", "enum",
			title: "Enable Debug Logging?",
			required: false,
			displayDuringSetup: false,
			defaultValue: 1,
			options: [0:"No", 1:"Yes [DEFAULT]"]
	}
}


def installed() {
	logDebug "installed()..."

	initialize()

	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		if (!state.refreshed) {
			runIn(3, refresh)
		}
	}
	return []
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		def checkInterval = ((60 * 60) + (5 * 60))
		sendEvent([name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]])
	}

	if (!device.currentValue("chimeSound")) {
		sendEvent(name: "chimeSound", value: "10")
	}

	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)
}


def configure() {
	logDebug "configure()..."
	runIn(3, refresh)
}


def refresh() {
	logDebug "refresh()..."
	List<String> cmds = [
		batteryGetCmd(),
		basicGetCmd()
	]

	if (state.mode != sirenMode) {
		cmds << configSetCmd(configParams.mode, sirenMode)
	}

	cmds += configParams.collect { name, param -> configGetCmd(param) }

	state.refreshed = true
	
	sendCommands(cmds, 500)
}


def ping() {
	logDebug "ping()..."
	return [ batteryGetCmd() ]
}


def setSirenVolume(sirenVolume) {
	logDebug "setSirenVolume(${sirenVolume})..."
	return getChangeParamCmds(configParams.sirenVolume, sirenVolume)
}

def setSirenSound(sirenSound) {
	logDebug "setSirenSound(${sirenSound})..."
	return getChangeParamCmds(configParams.sirenSound, sirenSound)
}

def setSirenLength(sirenLength) {
	logDebug "setSirenLength(${sirenLength})..."
	return getChangeParamCmds(configParams.sirenLength, sirenLength)
}

def setChimeVolume(chimeVolume) {
	logDebug "setChimeVolume(${chimeVolume})..."
	List<String> cmds = []
	cmds += getChangeParamCmds(configParams.chimeVolume, chimeVolume)

	// firmware bug causes the siren sound parameter to get overwritten when the chime volume parameter is changed.
	cmds << "delay 100"
	cmds += getChangeParamCmds(configParams.sirenSound, device.currentValue("sirenSound"))
	return cmds
}

def setChimeSound(chimeSound) {
	logDebug "setChimeSound(${chimeSound})..."
	sendEvent(name: "chimeSound", value: chimeSound)
}

def setChimeRepeat(chimeRepeat) {
	logDebug "setChimeRepeat(${chimeRepeat})..."	
	return getChangeParamCmds(configParams.chimeRepeat, chimeRepeat)
}

def setChimeLed(chimeLed) {
	logDebug "setChimeLed(${chimeLed})..."
	return getChangeParamCmds(configParams.chimeLed, chimeLed)
}

List<String> getChangeParamCmds(Map param, value) {
	return delayBetween([ 
		configSetCmd(param, safeToInt(value)),
		configGetCmd(param)
	])
}


def off() {
	logDebug "off()..."
	return delayBetween([ 
		switchBinarySetCmd(0x00),
		basicGetCmd()
	])
}


def on() {
	logDebug "on()..."
	sendEventIfNew("switch", "on")
	return both()
}


def strobe() {
	return both()
}

def both() {
	logDebug "both()..."
	return getSirenCmds(true)
}

def siren() {
	logDebug "siren()..."
	return getSirenCmds(false)
}

List<String> getSirenCmds(boolean ledEnabled) {
	List<String> cmds = []

	if (state.sirenLed != ledEnabled) {
		cmds << configSetCmd(configParams.sirenLed, (ledEnabled ? 1 : 0))
		cmds << configGetCmd(configParams.sirenLed)
	}

	cmds << switchBinarySetCmd(0xFF)

	state.pendingAlarm = (ledEnabled ? "both" : "siren")

	return delayBetween(cmds)
}


def chime() {
	logDebug "chime()..."
	state.pendingChime = true	
	int chimeSound = safeToInt(device.currentValue("chimeSound"), 10)	
	
	List<String> cmds = [
		indicatorSetCmd(chimeSound)
	]	
	
	if ((safeToInt(device.currentValue("chimeRepeat")) <= 2) && (chimeSound in [9, 10])) {
		// Fixes problem where these sounds stop playing before the on/beep events are raised causing the off events to never get raised.		
		cmds << "delay 2000"
		cmds << basicGetCmd()
	}	
	return cmds
}


void sendCommands(List<String> cmds, Integer delay=200) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}


String indicatorGetCmd() {
	return zwave.indicatorV1.indicatorGet().format()
}

String indicatorSetCmd(int value) {
	return zwave.indicatorV1.indicatorSet(value: value).format()
}

String basicGetCmd() {
	return zwave.basicV1.basicGet().format()
}

String switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}

String switchBinarySetCmd(int value) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: value).format()
}

String batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

String configSetCmd(Map param, int value) {
	if (value > 127) {
		value = (value - 256) // workaround for device using 1 byte parameters for values above 127.
	}
	return zwave.configurationV1.configurationSet(parameterNumber: param.num, size: 1, scaledConfigurationValue: value).format()
}

String configGetCmd(Map param) {
	return zwave.configurationV1.configurationGet(parameterNumber: param.num).format()
}


def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} else {
		log.warn "Unable to parse: $description"
	}

	updateLastCheckIn()
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time
		sendEvent(name: "lastCheckIn", value: new Date().time, displayed: false)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int value = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (value > 100) {
		value = 100
	}
	logDebug "Battery is ${value}%"
	sendEvent(name: "battery", value: value, unit: "%")
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	// logDebug "${cmd}"

	Map param = configParams.find { name, param ->
		param.num == cmd.parameterNumber 
	}?.value

	if (param) {
		Integer value = cmd.configurationValue[0]
		if (param.attr) {
			sendEventIfNew(param.attr, value)
		} else {
			switch (param.num) {
				case configParams.mode.num:
					logDebug "Mode is ${value}"
					state.mode = value
					break
				case configParams.sirenLed.num:
					logDebug "Siren LED is ${value}"
					state.sirenLed = (value ? true : false)
					break
			}
		}
	}
}


void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logDebug "${cmd}"
	if (cmd.value) {
		handleAlarmChimeEvents(cmd.value)
	}
}

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logDebug "${cmd}"
	handleAlarmChimeEvents(cmd.value)
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	if (cmd.notificationType == notificationTypeSiren) {
		handleAlarmChimeEvents(cmd.event)
	}
}

void handleAlarmChimeEvents(value) {
	if (value) {
		sendEventIfNew("switch", "on")

		if (state.pendingChime) {
			sendEventIfNew("chime", "chime")			
		}

		if (state.pendingAlarm) {
			sendEventIfNew("alarm", state.pendingAlarm)			
		}
		
		state.pendingChime = false
		state.pendingAlarm = false
	} else {
		sendEventIfNew("alarm", "off")
		sendEventIfNew("chime", "off")
		sendEventIfNew("switch", "off")
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void sendEventIfNew(String name, value) {
	if (device.currentValue(name) != value) {
		sendEvent(getEventMap(name, value))
	}
}

Map getEventMap(String name, value, String unit="") {
	Map event = [
		name: name,
		value: value,
		displayed: true,
		isStateChange: true,
		descriptionText: "${name} is ${value}${unit}"
	]
	if (unit) {
		event.unit = unit
	}
	logDebug(event.descriptionText)
	return event
}


Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	} else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	} else {
		return  defaultVal
	}
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}