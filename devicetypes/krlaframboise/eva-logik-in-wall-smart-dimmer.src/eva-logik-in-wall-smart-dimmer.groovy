/**
 *  EVA LOGIK In-Wall Smart Dimmer v1.1
 *
 *  	Models: Eva Logik (ZW31) / MINOSTON (MS11Z)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation: https://community.smartthings.com/t/release-eva-logik-zw31-minoston-ms11z-in-wall-dimmer/198305
 *
 *  Changelog:
 *
 *    1.1 (06/21/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Kevin LaFramboise
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

import groovy.json.JsonOutput
import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x26: 3,	// Switch Multilevel
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map paddleControlOptions = [0:"Normal [DEFAULT]", 1:"Reverse", 2:"Toggle"]
@Field static Integer reversePaddle = 1
@Field static Integer togglePaddle = 2

@Field static Map ledModeOptions = [0:"Off When On [DEFAULT]", 1:"On When On", 2:"Always Off", 3:"Always On"]

@Field static Map associationReportsOptions = [0:"None", 1:"Physical [DEFAULT]", 2:"3-way", 3:"3-way and Physical", 4:"Digital", 5:"Digital and Physical", 6:"Digital and 3-way", 7:"Digital, Physical, and 3-way", 8:"Timer", 9:"Timer and Physical", 10:"Timer and 3-way", 11:"Timer, 3-Way, and Physical", 12:"Timer and Digital", 13:"Timer, Digital, and Physical", 14:"Timer, Digital, and 3-way", 15:"All"]

@Field static Map autoOnOffIntervalOptions = [0:"Disabled [DEFAULT]", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]

@Field static Map powerFailureRecoveryOptions = [0:"Turn Off [DEFAULT]", 1:"Turn On", 2:"Restore Last State"]

@Field static Map dimmingDurationOptions = [1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds", 8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds"]

@Field static Map brightnessOptions = [0:"Disabled", 1:"1%", 5:"5%", 10:"10% [DEFAULT]", 15:"15%", 20:"20%", 25:"25%", 30:"30%", 35:"35%", 40:"40%", 45:"45%", 50:"50%", 55:"55%",60:"60%", 65:"65%", 70:"70%", 75:"75%", 80:"80%", 85:"85%", 90:"90%", 95:"95%", 99:"99%"]

metadata {
	definition (
		name: "EVA LOGIK In-Wall Smart Dimmer",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise",
		ocfDeviceType: "oic.d.switch"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Button"

		attribute "firmwareVersion", "string"
		attribute "lastCheckIn", "string"
		attribute "syncStatus", "string"

		fingerprint mfr: "0312", prod: "AA00", model: "AA02", deviceJoinName: "Eva Logik In-Wall Smart Dimmer" // ZW31

		fingerprint mfr: "0312", prod: "FF00", model: "FF04", deviceJoinName: "Minoston In-Wall Smart Dimmer" // MS11Z
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh"
		}
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		standardTile("sync", "device.configure", width: 2, height: 2) {
			state "default", label: 'Sync', action: "configure"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		main "switch"
		details(["switch", "refresh", "syncStatus", "sync", "firmwareVersion"])
	}

	preferences {
		configParams.each {
			createEnumInput("configParam${it.num}", "${it.name}:", it.value, it.options)
		}

		createEnumInput("debugOutput", "Enable Debug Logging?", 1, [0:"No", 1:"Yes [DEFAULT]"])
	}
}

void createEnumInput(String name, String title, Integer defaultVal, Map options) {
	input name, "enum",
		title: title,
		required: false,
		defaultValue: defaultVal.toString(),
		options: options
}


def installed() {
	logDebug "installed()..."

	if (state.debugLoggingEnabled == null) {
		state.debugLoggingEnabled = true
	}
	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

		initialize()

		runIn(5, executeConfigureCmds, [overwrite: true])
	}
	return []
}

void initialize() {
	def checkInterval = ((60 * 60 * 3) + (5 * 60))

	Map checkIntervalEvt = [name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]]

	if (!device.currentValue("checkInterval")) {
		sendEvent(checkIntervalEvt)
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value:JsonOutput.toJson(["pushed", "down","down_2x","up","up_2x"]), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("pushed")
	}
}


def configure() {
	logDebug "configure()..."

	if (state.resyncAll == null) {
		state.resyncAll = true
		runIn(8, executeConfigureCmds, [overwrite: true])
	}
	else {
		if (!pendingChanges) {
			state.resyncAll = true
		}
		executeConfigureCmds()
	}
	return []
}

void executeConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []

	if (!device.currentValue("switch")) {
		cmds << switchMultilevelGetCmd()
	}

	if (state.resyncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if ((state.resyncAll == true) || (state.lifelineAssoc != true)) {
		cmds << lifelineAssociationSetCmd()
		cmds << lifelineAssociationGetCmd()
	}
	
	if (autoOffIntervalParam.value || autoOnIntervalParam.value) {
		logDebug "Only 'up_2x' and 'down_2x' button events are supported when an Auto On/Off setting is enabled."
	}
	else if (paddleControlParam.value == togglePaddle) {
		logDebug "Only 'pushed', 'up_2x', and 'down_2x' button events are supported when the Paddle Control setting is Toggle."
	}

	configParams.each { param ->
		Integer storedVal = getParamStoredValue(param.num)
		if (state.resyncAll || (storedVal != param.value)) {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param, param.value)
			cmds << configGetCmd(param)
		}
	}

	state.resyncAll = false
	if (cmds) {
		sendCommands(delayBetween(cmds, 500))
	}
}


def ping() {
	logDebug "ping()..."
	return [ switchMultilevelGetCmd() ]
}


def on() {
	logDebug "on()..."
	return getSetLevelCmds(null)
}


def off() {
	logDebug "off()..."
	return getSetLevelCmds(0x00)
}


def setLevel(level) {
	logDebug "setLevel($level)..."
	return getSetLevelCmds(level)
}


def setLevel(level, duration) {
	logDebug "setLevel($level, $duration)..."
	return getSetLevelCmds(level, duration)
}

private getSetLevelCmds(level, duration=null) {
	if (level == null) {
		level = device.currentValue("level")
	}
	def levelVal = validateRange(level, 99, 0, 99)

	if (duration == null) {
		duration = pushDimmingDurationParam.value
	}
	def durationVal = validateRange(duration, 1, 0, 30)

	return [ switchMultilevelSetCmd(levelVal, durationVal) ]
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()

	sendCommands([switchMultilevelGetCmd()])
}


private sendCommands(cmds) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions)
	}
	return []
}


String lifelineAssociationGetCmd() {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: 1))
}

String lifelineAssociationSetCmd() {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String switchMultilevelSetCmd(Integer value, Integer duration) {
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: duration, value: value))
}

String switchMultilevelGetCmd() {
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelGet())
}

String configSetCmd(Map param, Integer value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	try {
		if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		}
		else {
			return cmd.format()
		}
	}
	catch (ex) {
		return cmd.format()
	}
}


def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	}
	else {
		log.warn "Unable to parse: $description"
	}

	updateLastCheckIn()
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	}
}

String convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}
	}
	catch (ex) {
		return "$dt"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)

	def result = []
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		Integer val = cmd.scaledConfigurationValue
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	if (cmd.groupingIdentifier == 1) {
		updateSyncingStatus()
		runIn(4, refreshSyncStatus)

		logDebug "Lifeline Association: ${cmd.nodeId}"
		state.lifelineAssoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def subVersion = String.format("%02d", cmd.applicationSubVersion)
	def fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEventIfNew("firmwareVersion", fullVersion)
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "${cmd}"
	sendSwitchEvents(cmd.value, "physical")
}


void zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logTrace "${cmd}"
	sendSwitchEvents(cmd.value, "digital")
}


void sendSwitchEvents(rawVal, String type) {
	String oldSwitch = device.currentValue("switch")
	Integer oldLevel = device.currentValue("level")
	String switchVal = rawVal ? "on" : "off"
	
	if (autoOffIntervalParam.value || autoOnIntervalParam.value) {
		type = null
	}

	sendEventIfNew("switch", switchVal, true, type)

	if (rawVal) {
		sendEventIfNew("level", rawVal, true, type, "%")
	}

	if (type == "physical") {
		if (paddleControlParam.value == togglePaddle) {
			sendButtonEvent("pushed")
		}
		else {
			boolean paddlesReversed = (paddleControlParam.value == reversePaddle)
			String btnVal = ((rawVal && !paddlesReversed) || (!rawVal && paddlesReversed)) ? "up" : "down"

			if ((oldSwitch == "on") && (btnVal == "up") && (oldLevel > rawVal)) {
				btnVal = "down"
			}

			sendButtonEvent(btnVal)
		}
	}
}


void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String paddle = (cmd.sceneNumber == 1) ? "down" : "up"
		String btnVal
		switch (cmd.keyAttributes){
			case 0:
				btnVal = paddle
				break
			case 3:
				btnVal = paddle + "_2x"
				break
			default:
				logDebug "keyAttributes ${cmd.keyAttributes} not supported"
		}

		if (btnVal) {
			sendButtonEvent(btnVal)
		}
	}
}

void sendButtonEvent(String value) {
	logDebug "Button 1 ${value}"
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void updateSyncingStatus() {
	sendEventIfNew("syncStatus", "Syncing...", false)
}

void refreshSyncStatus() {
	Integer changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}


Integer getPendingChanges() {
	Integer configChanges = configParams.count { it.value != getParamStoredValue(it.num) }
	return (configChanges + (state.lifelineAssoc != true ? 1 : 0))
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	return [
		paddleControlParam,
		ledModeParam,
		autoOffIntervalParam,
		autoOnIntervalParam,
		// associationReportsParam,
		powerFailureRecoveryParam,
		pushDimmingDurationParam,
		holdDimmingDurationParam,
		minimumBrightnessParam
	]
}

Map getPaddleControlParam() {
	return getParam(1, "Paddle Control", 1, 0, paddleControlOptions)
}

Map getLedModeParam() {
	return getParam(2, "LED Indicator Mode", 1, 0, ledModeOptions)
}

Map getAutoOffIntervalParam() {
	return getParam(4, "Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getAutoOnIntervalParam() {
	return getParam(6, "Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getAssociationReportsParam() {
	return getParam(7, "Association Settings", 1, 1, associationReportsOptions)
}

Map getPowerFailureRecoveryParam() {
	return getParam(8, "Power Failure Recovery", 1, 0, powerFailureRecoveryOptions)
}

Map getPushDimmingDurationParam() {
	return getParam(9, "Push Dimming Duration", 1, 1, setDefaultOption(dimmingDurationOptions, 1))
}

Map getHoldDimmingDurationParam() {
	return getParam(10, "Hold Dimming Duration", 1, 4, setDefaultOption(dimmingDurationOptions, 4))
}

Map getMinimumBrightnessParam() {
	return getParam(11, "Minimum Brightness", 1, 10, brightnessOptions)
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options]
}

Map setDefaultOption(Map options, Integer defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


void sendEventIfNew(String name, value, boolean displayed=true, String type=null, String unit="") {
	String desc = "${name} is ${value}${unit}"
	if (device.currentValue(name) != value) {
		
		if (name != "syncStatus") {
			logDebug(desc)
		}

		Map evt = [name: name, value: value, descriptionText: "${device.displayName} ${desc}", displayed: displayed]

		if (type) {
			evt.type = type
		}
		if (unit) {
			evt.unit = unit
		}
		sendEvent(evt)
	}
	else {
		logTrace(desc)
	}
}


Integer validateRange(val, Integer defaultVal, Integer lowVal, Integer highVal) {
	Integer intVal = safeToInt(val, defaultVal)
	if (intVal > highVal) {
		return highVal
	}
	else if (intVal < lowVal) {
		return lowVal
	}
	else {
		return intVal
	}
}

Integer safeToInt(val, Integer defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}

void logTrace(String msg) {
	// log.trace "$msg"
}