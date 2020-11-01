/*
 *  HomeSeer Wall Switch HS-WS200+ (v1.0)
 *
 *  Changelog:
 *
 *    1.0 (10/31/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 HomeSeer
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

@Field int defaultCmdDelay = 500

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x27: 1,	// Switch All
	0x2B: 1,	// Scene Activation
	0x2C: 1,	// Scene Actuator Conf
	0x55: 1,	// Transport Service (2)
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration (3)
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static String normalModeChildDTH = "HomeSeer Normal Mode Child"
@Field static String statusBlinkChildDTH = "HomeSeer Status LED Blink Frequency Child"
@Field static String statusLedChildDTH = "HomeSeer Status LED Color Child"

@Field static String normalModeChildDNI = "NormalMode"
@Field static String statusColorChildDNI = "StatusModeColor"
@Field static String statusBlinkChildDNI = "StatusModeBlink"

@Field static Map normalLedBehaviorOptions = [0:"LED ON if load is OFF [DEFAULT]", 1:"LED OFF if load is OFF"]

@Field static Map paddleLoadOrientationOptions = [0:"Top of Paddle turns load ON [DEFAULT]", 1:"Bottom of Paddle turns load ON"]

@Field static Map centralSceneEnabledOptions = [0:"Enabled [DEFAULT]", 1:"Disabled"]

@Field static Map ledModeOptions = [0:"normal", 1:"status"]

@Field static Map normalLedColorOptions = [0:"white", 1:"red", 2:"green", 3:"blue", 4:"magenta", 5:"yellow", 6:"cyan"]

@Field static Map statusLedColorOptions = [0:"off", 1:"red", 2:"green", 3:"blue", 4:"magenta", 5:"yellow", 6:"cyan", 7:"white"]

@Field static Map statusBlinkFrequencyOptions = [0:"Blinking Disabled", 1:"100ms", 2:"200ms", 3:"300ms", 4:"400ms", 5:"500ms", 6:"600ms", 7:"700ms", 8:"800ms", 9:"900ms", 10:"1s", 11:"1.1s", 12:"1.2s", 13:"1.3s", 14:"1.4s", 15:"1.5s", 20:"2s", 25:"2.5s", 30:"3s", 35:"3.5s", 40:"4s", 45:"4.5s", 50:"5s", 60:"6s", 70:"7s", 80:"8s", 90:"9s", 100:"10s", 110:"11s", 120:"12s", 130:"13s", 140:"14s", 150:"15s", 160:"16s", 170:"17s", 180:"18s", 190:"19s", 200:"20s", 210:"21s", 220:"22s", 230:"23s", 240:"24s", 250:"25s"]

@Field static Map createChildOptions = [0:"No [DEFAULT]", 1:"Yes"]

@Field static Map debugLoggingOptions = [0:"Disabled", 1:"Enabled [DEFAULT]"]


metadata {
	definition (
		name: "HomeSeer Wall Switch HS-WS200",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		mnmn: "SmartThingsCommunity",
		vid: "fd10ff4b-abc6-3e9d-ac19-9fa46a1ad85d"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Button"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "platemusic11009.firmware"
		capability "platemusic11009.hsLedMode"
		capability "platemusic11009.hsNormalLedColor"
		capability "platemusic11009.hsStatusLedColor"
		capability "platemusic11009.hsStatusLedBlinkFrequency"

		attribute "lastCheckIn", "string"

		command "setNormalLedMode"
		command "setStatusLedMode"
		
		// color name or color #, blink frequency #(optional)
		command "setStatusLedColorBlinkFrequency", ["string", "number"]

		fingerprint mfr: "000C", prod: "4447", model: "3035", deviceJoinName: "HomeSeer Wall Switch"
	}

	simulator { }

	preferences {
		settingsConfigParams.each {
			createEnumInput("configParam${it.num}", "${it.name}:", it.value, it.options)
		}
		createEnumInput("debugOutput", "Debug Logging", 1, debugLoggingOptions)

		createWorkaroundPreferences()
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

	initialize()

	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		runIn(1, executeConfigureCmds, [overwrite: true])
	}
	return []
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)	

	if (!device.currentValue("checkInterval")) {
		int checkInterval = ((60 * 60 * 3) + (5 * 60))
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}

	if (!device.currentValue("ledMode")) {
		sendEvent(name: "ledMode", value: "normal")
	}

	if (!device.currentValue("normalLedColor")) {
		sendEvent(name: "normalLedColor", value: "white")
	}

	if (!device.currentValue("statusLedColor")) {
		sendEvent(name: "statusLedColor", value: "white")
	}

	if (device.currentValue("statusLedBlinkFrequency") == null) {
		sendEvent(name: "statusLedBlinkFrequency", value: 0)
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value: ["down","down_hold","down_2x","down_3x","down_4x","down_5x","up","up_hold","up_2x","up_3x","up_4x","up_5x"].encodeAsJSON(), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("up")
	}

	runIn(3, initializeChildDevices)
}


def configure() {
	logDebug "configure()..."

	if (!state.isConfigured) {
		runIn(8, executeConfigureCmds, [overwrite: true])
	}
	else {
		executeConfigureCmds()
	}
	return []
}

void executeConfigureCmds() {
	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (!state.isConfigured || !device.currentValue("switch")) {
		cmds << switchBinaryGetCmd()
	}

	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if (state.lifelineAssoc != true) {
		cmds << lifelineAssociationSetCmd()
		cmds << lifelineAssociationGetCmd()
	}

	if (!state.isConfigured) {
		allConfigParams.each {
			cmds << configGetCmd(it)
		}
	}
	else {
		settingsConfigParams.each { param ->
			Integer storedVal = getParamStoredValue(param.num)
			if (storedVal != param.value) {

				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param, param.value)
				cmds << configGetCmd(param)
			}
		}
	}

	state.isConfigured = true
	sendCommands(cmds, 500)
}

int getPendingChanges() {
	int configChanges = safeToInt(settingsConfigParams.count { it.value != getParamStoredValue(it.num) })
	return (configChanges + (state.lifelineAssoc != true ? 1 : 0))
}


def ping() {
	logDebug "ping()..."
	return [ switchBinaryGetCmd() ]
}


def setNormalLedMode() {
	setLedMode("normal")
}

def setStatusLedMode() {
	setLedMode("status")
}

def setLedMode(mode) {
	logDebug "setLedMode($mode)..."

	List<String> cmds = []

	Integer value = findIntKeyByValue(ledModeOptions, mode)
	if (value != null) {
		cmds += [
			configSetCmd(ledModeParam, value),
			configGetCmd(ledModeParam)
		]
	}
	else {
		log.warn "${mode} is not a valid LED Mode"
	}
	sendCommands(cmds)
}


def setStatusLedColorBlinkFrequency(color, blinkFrequency=null) {
	if (blinkFrequency != null) {
		setStatusLedBlinkFrequency(blinkFrequency)
	}

	if (color) {
		setStatusLedColor(color)
	}
}


def setStatusLedBlinkFrequency(frequency) {
	logDebug "setStatusLedBlinkFrequency($frequency)..."

	Map param = statusLedBlinkFrequencyParam

	sendCommands([
		configSetCmd(param, safeToIntRange(frequency, 0, 0, 255)),
		configGetCmd(param)
	])
}


def setNormalLedColor(color) {
	logDebug "setNormalLedColor($color)..."

	sendCommands(getChangeLedColorCmds(normalLedColorParam, normalLedColorOptions, color))
}


def setStatusLedColor(color) {
	logDebug "setStatusLedColor($color)..."

	sendCommands(getChangeLedColorCmds(statusLedColorParam, statusLedColorOptions, color))
}


List<String> getChangeLedColorCmds(param, options, color) {
	List<String> cmds = []

	Integer colorId = ("${color}".isInteger() ? safeToInt(color) : findIntKeyByValue(options, color.toLowerCase().trim()))
	if (colorId != null) {
		cmds += [
			configSetCmd(param, colorId),
			configGetCmd(param)
		]
	}
	else {
		log.warn "${color} is not a ${param.name}"
	}
	return cmds
}


def on() {
	logDebug "on()..."

	state.pendingSwitch = true

	sendCommands([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	])
}


def off() {
	logDebug "off()..."

	state.pendingSwitch = true

	sendCommands([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	])
}


def refresh() {
	logDebug "refresh()..."

	List<String> cmds = [
		switchBinaryGetCmd(),
		versionGetCmd(),
		lifelineAssociationGetCmd()
	]

	allConfigParams.each {
		cmds << configGetCmd(it)
	}

	sendCommands(cmds, 500)
}


List<String> sendCommands(List<String> cmds, Integer delay=null) {
	if (cmds) {
		delay = ((delay == null) ? defaultCmdDelay : delay)

		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
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

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(Integer value) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: value))
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
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "${cmd}"

	Map param = allConfigParams.find { it.num == cmd.parameterNumber }
	if (param) {
		int val = cmd.scaledConfigurationValue

		setParamStoredValue(param.num, val)

		switch (param.num) {
			case ledModeParam.num:
				sendEventIfNew("ledMode", ledModeOptions.get(val))
				findChild(normalModeChildDNI)?.sendLedModeEvents(ledModeOptions.get(val))
				break

			case normalLedColorParam.num:
				sendEventIfNew("normalLedColor", normalLedColorOptions.get(val))
				findChild(normalModeChildDNI)?.sendColorEvents(normalLedColorOptions.get(val))
				break

			case statusLedBlinkFrequencyParam.num:
				sendEventIfNew("statusLedBlinkFrequency", val)
				findChild(statusBlinkChildDNI)?.sendBlinkFrequencyEvents(val)
				break

			case statusLedColorParam.num:
				sendEventIfNew("statusLedColor", statusLedColorOptions.get(val))
				findChild(statusColorChildDNI)?.sendColorEvents(statusLedColorOptions.get(val))
				break

			default:
				logDebug "${param.name}(#${param.num}) = ${val}"
		}
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	if (cmd.groupingIdentifier == 1) {
		logDebug "Lifeline Association: ${cmd.nodeId}"
		state.lifelineAssoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEventIfNew("firmwareVersion", fullVersion.toBigDecimal())
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "${cmd}"

	sendSwitchEvent(cmd.value)
}


void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "${cmd}"

	sendSwitchEvent(cmd.value)
}

void sendSwitchEvent(rawValue) {
	String value = rawValue ? "on" : "off"
	String type = state.pendingSwitch ? "digital" : "physical"
	state.pendingSwitch = false

	sendEventIfNew("switch", value, type)
}


void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String paddle = (cmd.sceneNumber == 1) ? "up" : "down"
		String btnVal
		switch (cmd.keyAttributes){
			case 0:
				btnVal = paddle
				break
			case 1:
				logDebug "${paddle}_released is not supported by SmartThings"
				break
			case 2:
				btnVal = paddle + "_hold"
				break
			case { it >= 3 && it <= 7}:
				btnVal = paddle + "_${cmd.keyAttributes - 1}x"
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
	String desc = "${device.displayName} ${value}"
	logDebug(desc)

	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true, descriptionText: desc)
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}



Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getAllConfigParams() {
	List<Map> params = [
		ledModeParam,
		normalLedColorParam,
		statusLedColorParam
	]

	params += settingsConfigParams

	return params
}

List<Map> getSettingsConfigParams() {
	return [
		normalLedBehaviorParam,
		statusLedBlinkFrequencyParam,
		paddleLoadOrientationParam,
		centralSceneEnabledParam
	]
}

Map getNormalLedBehaviorParam() {
	return getParam(3, "Normal LED Behavior", 1, 0, normalLedBehaviorOptions)
}

Map getPaddleLoadOrientationParam() {
	return getParam(4, "Paddle's Load Orientation", 1, 0, paddleLoadOrientationOptions)
}

Map getCentralSceneEnabledParam() {
	return getParam(6, "Enable/Disable Central Scene (FIRMWARE >= 5.12)", 1, 0, centralSceneEnabledOptions)
}

Map getLedModeParam() {
	return getParam(13, "LED Mode", 1, 0, ledModeOptions)
}

Map getNormalLedColorParam() {
	return getParam(14, "Normal LED Color", 1, 0, normalLedColorOptions)
}

Map getStatusLedColorParam() {
	return getParam(21, "Status LED Color", 1, 0, statusLedColorOptions)
}

Map getStatusLedBlinkFrequencyParam() {
	return getParam(31, "Status LED Blink Frequency", 1, 0, statusBlinkFrequencyOptions)
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options]
}


void sendEventIfNew(String name, value, String type=null) {
	String desc = "${device.displayName}: ${name} is ${value}"
	if (device.currentValue(name) != value) {

		logDebug(desc)

		Map evt = [name: name, value: value, descriptionText: desc]

		if (type) {
			evt.type = type
		}
		sendEvent(evt)
	}
}


Integer findIntKeyByValue(Map items, value) {
	return safeToInt(items.find { it.value == value }?.key, null)
}


int safeToPercentInt(val, int defaultVal=0) {
	return safeToIntRange(val, defaultVal, 0, 99)
}

Integer safeToIntRange(val, Integer defaultVal, Integer lowVal, Integer highVal) {
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
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	}
	else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	}
	else {
		return  defaultVal
	}
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled) {
		log.debug(msg)
	}
}



// *** CHILD DEVICE WORKAROUND CODE ***

void createWorkaroundPreferences() {
	input "workarounds", "paragraph",
		title:"<br><br>Workarounds",
		description: "The Automations app doesn't support custom capabilities yet so the settings below create child devices that can be used with Automations to control specific functionality.  They also serve as a backup method of controlling the device if SmartThings makes a breaking change to Custom Capabilities."

	input "normalModeText", "paragraph",
		title:"<br><br>LED Mode",
		description: "The 'Normal Mode' child device is a Color Switch that requires the custom handler 'HomeSeer Normal Mode Child'.<br>SWITCH: Sets LED Mode to 'Normal' when ON and 'Status' when OFF.<br>COLOR PICKER: Changes the Normal LED Color to closest match."
	createEnumInput("normalModeChildEnabled", "Create 'Normal Mode' Child Device?", 0, createChildOptions)

	input "ledColorText", "paragraph",
		title:"<br><br>Status Mode Color",
		description: "The 'Status Mode Color' child device is a Color Switch which requires the custom handler 'HomeSeer Status LED Color Child'.<br>COLOR PICKER: Changes the Status LED Color to closest match.<br>SWITCH: Toggles the Status LED Color between 'Off' and last selected color."
	createEnumInput("statusColorChildEnabled", "Create 'Status Mode Color' Child Device?", 0, createChildOptions)

	input "ledBlinkText", "paragraph",
		title:"<br><br>Status Mode Blink Frequency",
		description: "The 'Status Mode Blink Frequency' child device is a Dimmer Switch and it requires the custom handler 'HomeSeer Status LED Blink Frequency Child'.<br>SWITCH: Toggles the Status LED Blink Frequency between 'Off' and the last value.<br>DIMMER: Changes Status LED Blink Frequency to the selected value.   (1=100ms, 2=200ms, ..., 100=10s)."
	createEnumInput("statusBlinkChildEnabled", "Create 'Status Mode Blink Frequency' Child Device?", 0, createChildOptions)
}


void initializeChildDevices() {
	def modeChild = initializeChildDevice("normalModeChildEnabled", normalModeChildDTH, normalModeChildDNI, "Normal Mode")
	modeChild?.sendLedModeEvents(device.currentValue("ledMode"))
	modeChild?.sendColorEvents(device.currentValue("normalLedColor"))

	def blinkChild = initializeChildDevice("statusBlinkChildEnabled", statusBlinkChildDTH, statusBlinkChildDNI, "Status Mode Blink Frequency")
	if (blinkChild) {
		sendCommands(["delay 1000", configGetCmd(statusLedBlinkFrequencyParam)])
	}

	def colorChild = initializeChildDevice("statusColorChildEnabled", statusLedChildDTH, statusColorChildDNI, "Status Mode Color")
	colorChild?.sendColorEvents(device.currentValue("statusLedColor"))
}

private initializeChildDevice(String settingName, String dthName, String dniSuffix, String nameSuffix) {
	boolean childEnabled = (settings && (safeToInt(settings["${settingName}"]) != 0))

	def child = childDevices?.find { it.deviceNetworkId?.endsWith(dniSuffix) }

	if (childEnabled && !child) {
		return createChildDevice("HomeSeer", dthName, dniSuffix, nameSuffix)
	}
	else if (!childEnabled && child) {
		removeChildDevice(child)
		return null
	}
	else {
		return null
	}
}

private createChildDevice(String namespace, String dthName, String dniSuffix, String nameSuffix) {
	try {
		logDebug "Creating ${nameSuffix} Child Device"
		return addChildDevice(
			namespace,
			dthName,
			"${device.deviceNetworkId}:${dniSuffix}",
			null,
			[
				completedSetup: true,
				label: "${device.displayName}-${nameSuffix}",
				isComponent: false
			]
		)
	}
	catch (e) {
		log.warn "Unable to create child device for ${nameSuffix}.  Make sure you've installed the custom DTH '${dthName}'"
		return null
	}
}

void removeChildDevice(child) {
	try {
		log.warn "Removing ${child.displayName} "
		deleteChildDevice(child.deviceNetworkId)
	}
	catch (e) {
		log.warn "Unable to remove ${child?.displayName}"
	}
}


private findChild(String dni) {
	return childDevices?.find { it.deviceNetworkId?.endsWith(dni) }
}


void logTrace(String msg) {
	// log.trace(msg)
}