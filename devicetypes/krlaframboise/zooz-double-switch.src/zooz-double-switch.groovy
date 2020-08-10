/**
 *  Zooz Double Switch v1.0.1
 *  	(Model: ZEN30)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.1 (08/10/2020)
 *      - Added ST workaround for S2 Supervision bug with MultiChannel Devices.
 *
 *    1.0 (06/23/2020)
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
	0x25: 1,	// SwitchBinary
	0x26: 3,	// SwitchMultilevel
	0x55: 1,	// TransportService
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// MultiChannel
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x8E: 2,	// MultiChannelAssociation
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map endpoints = ["dimmer": 0, "switch": 1]

@Field static Map ledModeOptions = [0:"On When Off [DEFAULT]", 1:"On When On", 2:"Always Off", 3:"Always On"]

@Field static Map ledColorOptions = [0:"White [DEFAULT]", 1:"Blue", 2:"Green", 3:"Red"]

@Field static Map ledBrightnessOptions = [0:"100%", 1:"60% [DEFAULT]", 2:"30%"]

@Field static Map autoOnOffIntervalOptions = [0:"Disabled [DEFAULT]", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]

@Field static Map powerFailureOptions = [0:"Dimmer Off / Relay Off", 1:"Dimmer Off / Relay On", 2:"Dimmer On / Relay Off", 3:"Dimmer Remember / Relay Remember [DEFAULT]", 4:"Dimmer Remember / Relay On", 5:"Dimmer Remember / Relay Off", 6:"Dimmer On / Relay Remember", 7:"Dimmer Off / Relay Remember", 8:"Dimmer On / Relay On"]

@Field static Map rampRateOptions = [1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds", 8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds", 11:"11 Seconds", 12:"12 Seconds", 13:"13 Seconds", 14:"14 Seconds", 15:"15 Seconds", 20:"20 Seconds", 25:"25 Seconds", 30:"30 Seconds", 35:"35 Seconds", 40:"40 Seconds", 45:"45 Seconds", 50:"50 Seconds", 55:"55 Seconds", 60:"60 Seconds", 65:"65 Seconds", 70:"70 Seconds", 75:"75 Seconds", 80:"80 Seconds", 85:"85 Seconds", 90:"90 Seconds", 95:"95 Seconds", 99:"99 Seconds"]

@Field static Map brightnessOptions = [1:"1%", 5:"5%", 10:"10%", 15:"15%", 20:"20%", 25:"25%", 30:"30%", 35:"35%", 40:"40%", 45:"45%", 50:"50%", 55:"55%",60:"60%", 65:"65%", 70:"70%", 75:"75%", 80:"80%", 85:"85%", 90:"90%", 95:"95%", 99:"99%"]

@Field static Map doubleTapFunctionOptions = [0:"Turn on Full Brightness [DEFAULT]", 1:"Turn on Maximum Brightness"]

@Field static Map dimmerDigitalRampRateBehaviorOptions = [0:"Match Physical [DEFAULT]", 1:"Custom"]

@Field static Map brightnessControlOptions = [0:"Double Tap Maximum [DEFAULT]", 1:"Single Tap Custom", 2:"Single Tap Maximum"]

@Field static Map loadControlOptions = [0:"Physical Disabled", 1:"Physical / Digital Enabled [DEFAULT]", 2:"Physical / Digital Disabled"]

@Field static Map paddleControlOptions = [0:"Normal [DEFAULT]", 1:"Reverse", 2:"Toggle"]

@Field static Map physicalDisabledBehaviorOptions = [0:"Change Status/LED [DEFAULT]", 1:"Don't Change Status/LED"]

metadata {
	definition (
		name: "Zooz Double Switch",
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

		fingerprint mfr: "027A", prod: "A000", model: "A008", deviceJoinName: "Zooz Double Switch" // ZEN30
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

		runIn(5, executeConfigureCmds)
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
		sendEvent(name:"supportedButtonValues", value:JsonOutput.toJson(["pushed","held","pushed_2x","pushed_3x","pushed_4x","pushed_5x","down","down_hold","down_2x","down_4x","down_5x","up","up_hold","up_2x","up_3x","up_4x","up_5x"]), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("pushed")
	}
	
	if (!childDevices) {		
		addChildDevice(
			"smartthings",
			"Child Switch",
			"${device.deviceNetworkId}:1",
			null,
			[
				completedSetup: true,
				label: "${device.displayName}-Relay",
				isComponent: false
			]
		)
	}
}


def configure() {
	logDebug "configure()..."

	if (state.resyncAll == null) {
		state.resyncAll = true
		runIn(2, executeRefreshCmds)
		runIn(8, executeConfigureCmds)
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
	refreshSyncStatus()

	List<String> cmds = []

	if (state.resyncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	BigDecimal firmware = firmwareVersion
	
	configParams.each { param ->
		if (firmwareSupportsParam(firmware, param)) {
			
			Integer storedVal = getParamStoredValue(param.num)
			if (state.resyncAll || (storedVal != param.value)) {
				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param, param.value)
				cmds << configGetCmd(param)
			}
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
	def durationVal = validateRange(duration, 1, 0, 30)
	return [ switchMultilevelSetCmd(levelVal, durationVal) ]
}


def refresh() {
	logDebug "refresh()..."

	executeRefreshCmds()
}

void executeRefreshCmds() {
	refreshSyncStatus()
	
	List<String> cmds = [
		switchMultilevelGetCmd(),
		switchBinaryGetCmd()
	]	
	sendCommands(delayBetween(cmds, 500))
}


def childOn(dni) {
	logDebug "childOn(${dni})..."
	state.pendingRelay = true
	sendCommands([ switchBinarySetCmd(0xFF) ])
}


def childOff(dni) {
	logDebug "childOff(${dni})..."
	state.pendingRelay = true
	sendCommands([ switchBinarySetCmd(0x00) ])
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


String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String switchBinaryGetCmd() {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoints.switch)
}

String switchBinarySetCmd(Integer val) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val), endpoints.switch)
}

String switchMultilevelSetCmd(Integer value, Integer duration) {
	return multiChannelCmdEncapCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: duration, value: value), endpoints.dimmer)
}

String switchMultilevelGetCmd() {
	return multiChannelCmdEncapCmd(zwave.switchMultilevelV3.switchMultilevelGet(), endpoints.dimmer)
}

String configSetCmd(Map param, Integer value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String multiChannelCmdEncapCmd(cmd, endpoint) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
	}
	else {
		return secureCmd(cmd)
	}
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


void zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	// Workaround that was added to all SmartThings Multichannel DTHs.
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)	
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
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
	updateSyncingStatus()
	
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


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def subVersion = String.format("%02d", cmd.applicationSubVersion)
	def fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEventIfNew("firmwareVersion", fullVersion)
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	logTrace "${cmd} (${endpoint})"
	sendSwitchEvents(cmd.value, endpoint, "physical")
}


void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endPoint=0) {
	logTrace "${cmd} (${endpoint})"
	
	String type = (state.pendingRelay ? "digital" : "physical")
	state.pendingRelay = false
	
	sendSwitchEvents(cmd.value, endpoints.switch, type)
}


void zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, endpoint=0) {
	logTrace "${cmd} (${endpoint})"
	
	sendSwitchEvents(cmd.value, endpoints.dimmer, "digital")

	state["pendingSwitch${endpoint}"] = false
}


void sendSwitchEvents(rawVal, Integer endpoint, String type) {
	String switchVal = rawVal ? "on" : "off"
	
	if (endpoint == endpoints.dimmer) {
		sendEventIfNew("switch", switchVal, true, type)		
	}
	else {
		def child = childDevices[0]		
		if ((child != null) && (child.currentValue("switch") != switchVal)) {
			logDebug "${child.displayName} switch is ${switchVal}"
			child.sendEvent(name: "switch", value: switchVal, type: type)
		}
	}
	
	if ((rawVal > 0) && (endpoint == endpoints.dimmer)) {
		sendEventIfNew("level", rawVal, true, type, "%")
	}
}


void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String actionType
		String btnVal
		String displayName = ""
		
		switch (cmd.sceneNumber) {
			case 1:
				actionType = "up"
				break
			case 2:
				actionType = "down"
				break
			case 3:
				actionType = "pushed"
				displayName = "${childDevices[0]?.displayName} "
		}
		
		switch (cmd.keyAttributes){
			case 0:
				btnVal = actionType
				break
			case 1:
				btnVal = (actionType == "pushed") ? "released" : "${actionType}_released"				
				logDebug "${btnVal} is not supported by SmartThings"
				btnVal = null
				break
			case 2:
				btnVal = (actionType == "pushed") ? "held" : "${actionType}_hold"
				break
			default:
				btnVal = "${actionType}_${cmd.keyAttributes - 1}x"
		}

		if (btnVal) {
			logDebug "${displayName}Button ${btnVal}"
			sendButtonEvent(btnVal)
		}
	}
}

void sendButtonEvent(String value) {	
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void updateSyncingStatus() {
	runIn(4, refreshSyncStatus)
	
	if (device.currentValue("syncStatus") != "Syncing...") {
		sendEvent(name:"syncStatus", value:"Syncing...", displayed:false)
	}
}

void refreshSyncStatus() {
	Integer changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}


Integer getPendingChanges() {
	BigDecimal firmware = firmwareVersion	
	return configParams.count { 
		((firmwareSupportsParam(firmware, it)) && (it.value != getParamStoredValue(it.num))) 
	}
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}

List<Map> getConfigParams() {
	return [
		powerFailureParam,
		ledSceneControlParam,
		relayLedModeParam,
		relayLedColorParam,
		relayLedBrightnessParam,
		relayAutoOffParam,
		relayAutoOnParam,
		relayLoadControlParam,
		relayPhysicalDisabledBehaviorParam,
		dimmerLedModeParam,
		dimmerLedColorParam,
		dimmerLedBrightnessParam,
		dimmerAutoOffParam,
		dimmerAutoOnParam,
		dimmerRampRateParam,
		dimmerPaddleHeldRampRateParam,
		dimmerDigitalRampRateBehaviorParam,
		dimmerMinimumBrightnessParam,
		dimmerMaximumBrightnessParam,
		dimmerCustomBrightnessParam,
		dimmerBrightnessControlParam,
		dimmerDoubleTapFunctionParam,
		dimmerLoadControlParam,
		dimmerPhysicalDisabledBehaviorParam,
		dimmerNightModeBrightnessParam,
		dimmerPaddleControlParam
	]
}

Map getDimmerLedModeParam() {
	return getParam(1, "Dimmer LED Indicator Mode", 1, 0, ledModeOptions)
}

Map getRelayLedModeParam() {
	return getParam(2, "Relay LED Indicator Mode", 1, 0, ledModeOptions)
}

Map getDimmerLedColorParam() {
	return getParam(3, "Dimmer LED Indicator Color", 1, 0, ledColorOptions)
}

Map getRelayLedColorParam() {
	return getParam(4, "Relay LED Indicator Color", 1, 0, ledColorOptions)
}

Map getDimmerLedBrightnessParam() {
	return getParam(5, "Dimmer LED Indicator Brightness", 1, 1, ledBrightnessOptions)
}

Map getRelayLedBrightnessParam() {
	return getParam(6, "Relay LED Indicator Brightness", 1, 1, ledBrightnessOptions)
}

Map getLedSceneControlParam() {
	return getParam(7, "LED Indicator Mode for Scene Control", 1, 1, [0:"LED Enabled", 1:"LED Disabled [DEFAULT]"])
}

Map getDimmerAutoOffParam() {
	return getParam(8, "Dimmer Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getDimmerAutoOnParam() {
	return getParam(9, "Dimmer Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getRelayAutoOffParam() {
	return getParam(10, "Relay Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getRelayAutoOnParam() {
	return getParam(11, "Relay Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getPowerFailureParam() {
	return getParam(12, "On Oﬀ Status After Power Failure", 1, 3, powerFailureOptions)
}

Map getDimmerRampRateParam() {
	Map options = [0:"Instant"]
	options << rampRateOptions
	
	return getParam(13, "Dimmer Physical Ramp Rate", 1, 1, setDefaultOption(options, 1))
}

Map getDimmerMinimumBrightnessParam() {
	return getParam(14, "Dimmer Minimum Brightness", 1, 1, setDefaultOption(brightnessOptions, 1))
}

Map getDimmerMaximumBrightnessParam() {
	return getParam(15, "Dimmer Maximum Brightness", 1, 99, setDefaultOption(brightnessOptions, 99))
}

Map getDimmerDoubleTapFunctionParam() {
	return getParam(17, "Dimmer Double Tap Function", 1, 0, doubleTapFunctionOptions)
}

Map getDimmerBrightnessControlParam() {
	return getParam(18, "Dimmer Brightness Control", 1, 0, brightnessControlOptions)
}

Map getDimmerLoadControlParam() {
	return getParam(19, "Dimmer Load Control", 1, 1, loadControlOptions)
}

Map getRelayLoadControlParam() {
	return getParam(20, "Relay Load Control", 1, 1, loadControlOptions)
}

Map getDimmerPaddleHeldRampRateParam() {
	return getParam(21, "Dimming Speed when Paddle is Held", 1, 4, setDefaultOption(rampRateOptions, 4))
}

Map getDimmerDigitalRampRateBehaviorParam() {
	// Removed in firmware 1.05
	return getParam(22, "Dimming Speed for Digital Control [FIRMWARE <= 1.03]", 1, 0, dimmerDigitalRampRateBehaviorOptions, 0, 1.04)
}

Map getDimmerCustomBrightnessParam() {	
	Map options = [0:"Last Brightness [DEFAULT]"]
	options << brightnessOptions
	return getParam(23, "Custom Brightness", 1, 0, options)
}

Map getDimmerPhysicalDisabledBehaviorParam() {
	return getParam(24, "Dimmer Physical Disabled Behavior [FIRMWARE >= 1.05]", 1, 0, physicalDisabledBehaviorOptions, 1.05)
}

Map getRelayPhysicalDisabledBehaviorParam() {
	return getParam(25, "Relay Physical Disabled Behavior [FIRMWARE >= 1.05]", 1, 0, physicalDisabledBehaviorOptions, 1.05)
}

Map getDimmerNightModeBrightnessParam() {
	Map options = [0:"Disabled"]
	options << brightnessOptions
	return getParam(26, "Night Mode Brightness [FIRMWARE >= 1.05]", 1, 20, setDefaultOption(options, 20), 1.05)
}

Map getDimmerPaddleControlParam() {
	return getParam(27, "Paddle Orientation for Dimmer [FIRMWARE >= 1.05]", 1, 0, paddleControlOptions, 1.05)
}


Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options, BigDecimal minVer=null, BigDecimal maxVer=null) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options, minVer: minVer, maxVer: maxVer]
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
}

boolean firmwareSupportsParam(BigDecimal firmware, Map param) {
	return (((param.minVer == null) || (firmware >= param.minVer)) && ((param.maxVer == null) || (firmware <= param.maxVer)))
}

BigDecimal getFirmwareVersion() {
	String version = device?.currentValue("firmwareVersion")
	return ((version != null) && version.isNumber()) ? version.toBigDecimal() : 0.0
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