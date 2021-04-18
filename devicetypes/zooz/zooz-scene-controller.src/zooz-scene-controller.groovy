/*
 *  Zooz Scene Controller - ZEN32 v1.0.1
 *
 *		YOU MUST ALSO INSTALL: Zooz Scene Controller Button
 *
 *  Changelog:
 *
 *    1.0.1 (04/18/2021)
 *      - Create child devices after delay during inclusion which will hopefully make them appear with the parent when inclusion finishes.
 *
 *    1.0 (03/06/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 Zooz
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
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
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
	0x87: 1,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static List<Map> buttons = [
	[btnNum: 1, basicSetGroup: 4, multilevelGroup:5, ledModeParamNum: 2, ledColorParamNum: 7, ledBrightnessParamNum: 12, name: "Button 1"],
	[btnNum: 2, basicSetGroup: 6, multilevelGroup:7, ledModeParamNum: 3, ledColorParamNum: 8, ledBrightnessParamNum: 13, name: "Button 2"],
	[btnNum: 3, basicSetGroup: 8, multilevelGroup:9, ledModeParamNum: 4, ledColorParamNum: 9, ledBrightnessParamNum: 14, name: "Button 3"],
	[btnNum: 4, basicSetGroup: 10, multilevelGroup:11, ledModeParamNum: 5, ledColorParamNum: 10, ledBrightnessParamNum: 15, name: "Button 4"],
	[btnNum: 5, basicSetGroup: 2, multilevelGroup:3, ledModeParamNum: 1, ledColorParamNum: 6, ledBrightnessParamNum: 11, name: "Relay Button"]
]

@Field static int relayBtnNum = 5

@Field static int assocMaxNodes = 5

@Field static Map ledModeOptions = [0:"onWhenOff", 1:"onWhenOn", 2:"alwaysOff", 3:"alwaysOn"]

@Field static Map ledColorOptions = [0:"white", 1:"blue", 2:"green", 3:"red"]

@Field static Map ledBrightnessOptions = [0:"bright", 1:"medium", 2:"low"]

@Field static Map autoOnOffOptions = [0:"Timer Disabled [DEFAULT]", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]


metadata {
	definition (
		name: "Zooz Scene Controller",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		mnmn: "SmartThingsCommunity",
		vid: "9dc0b4b5-d711-3bc5-b684-59dec4c9bc19"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Button"
		capability "platemusic11009.firmware"
        capability "platemusic11009.zoozLedColor"
		capability "platemusic11009.zoozLedBrightness"
		capability "platemusic11009.zoozLedMode"
		capability "platemusic11009.basicSetAssociationGroup"
		capability "platemusic11009.multilevelAssociationGroup"
		capability "platemusic11009.syncStatus"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"027A", prod:"7000", model: "A008", deviceJoinName:"Zooz Scene Controller"
	}

	simulator { }

	preferences {
		configParams.each { param ->
			createEnumInput("configParam${param.num}", "${param.name}:", param.value, param.options)
		}

		createEnumInput("debugOutput", "Enable Debug Logging?", 1, [0:"No", 1:"Yes [DEFAULT]"])

		input "assocInstructions", "paragraph",
			title: "Device Associations",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this button control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  Both groups support up to 5 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the setting(s) below and then remove that device from SmartThings, you MUST come back and remove it from the settings below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "basicSetAssociationGroupDNIs", "string",
			title: "Enter Device Network IDs for Relay's Basic Set Association Group:",
			required: false

		input "multilevelAssociationGroupDNIs", "string",
			title: "Enter Device Network IDs for Relay's Multilevel Association Group:",
			required: false
	}
}

void createEnumInput(String name, String title, Integer defaultVal, Map options) {
	input name, "enum",
		title: title,
		required: false,
		defaultValue: defaultVal,
		options: options
}


def installed() {
	logDebug "installed()..."

	runIn(2, initialize)

	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		runIn(2, executeConfigureCmds)
	}
	return []
}

void initialize() {
	if (!state.initialized) {

		def checkInterval = ((60 * 60 * 3) + (5 * 60))
		sendEvent([name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]])

		state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

		sendEvent(name:"supportedButtonValues", value: ["pushed","held","pushed_2x","pushed_3x","pushed_4x","pushed_5x"].encodeAsJSON())
		sendEvent(name:"numberOfButtons", value:1)
		sendButtonEvent(findButtonByNum(relayBtnNum), "pushed")
		sendEvent(name: "basicSetAssociationGroup", value: "")
		sendEvent(name: "multilevelAssociationGroup", value: "")

		state.initialized = true
	}

	def children = childDevices
	buttons.each { btn ->
		if (btn.btnNum != relayBtnNum) {
			if (!children?.find { findButtonByDNI(it.deviceNetworkId) == btn }) {
				addChildButton(btn)
			}
		}
	}
}


def configure() {
	logDebug "configure()..."
	
	state.resyncAll = true

	runIn(5, initialize)
	runIn(20, executeConfigureCmds)

	return []
}

void executeConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []

	if (!device.currentValue("switch")) {
		cmds << switchBinaryGetCmd()
	}

	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
	
	if (state.resyncAll) {
		allConfigParams.each { param ->
			cmds << configGetCmd(param)
		}
	}
	else {
		configParams.each { param ->
			Integer storedVal = getParamStoredValue(param.num)
			if (storedVal != param.value) {			
				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param, param.value)			
				cmds << configGetCmd(param)
			}
		}
	}

	if (state.resyncAll != null) { 
		cmds += getConfigureRelayBtnAssocsCmds()
	}

	state.resyncAll = false
	if (cmds) {
		sendCommands(cmds)
	}
}


List<String> getConfigureRelayBtnAssocsCmds(boolean countOnly=false) {
	return getConfigureAssocsCmds(findButtonByNum(relayBtnNum), settings, countOnly)
}

List<String> getConfigureAssocsCmds(Map btn, Map dniSettings, boolean countOnly=false) {
	List<String> cmds = []
	boolean failedS2 = failedS2Inclusion

	getButtonAssociationGroups(btn).each { group, name ->
		boolean changes = false
		
		def stateNodeIds = state["group${group}NodeIds"]
		def settingNodeIds = getAssocDNIsSettingNodeIds(group, dniSettings["${name}DNIs"])

		def newNodeIds = settingNodeIds?.findAll { !(it in stateNodeIds) }
		if (newNodeIds) {
			if (!countOnly) {
				logDebug "Adding Nodes ${newNodeIds} to Button ${btn.btnNum} ${name} (#${group})"
			}

			cmds << associationSetCmd(group, newNodeIds)
			changes = true
		}

		def oldNodeIds = stateNodeIds?.findAll { !(it in settingNodeIds) }
		if (oldNodeIds) {
			if (!countOnly) {
				logDebug "Removing Nodes ${oldNodeIds} from Button ${btn.btnNum} ${name} (#${group})"
			}
			cmds << associationRemoveCmd(group, oldNodeIds)
			changes = true
		}

		if (!countOnly && !failedS2 && (changes || state.refreshAll)) {
			cmds << associationGetCmd(group)
		}
	}

	if (!countOnly && failedS2 && cmds) {
		// The handler doesn't get association reports for 700 series devices when not joined with S2 so requesting manufacturer report as a way to confirm the device is responding and if it responds then it assumes the association changes were successful.
		cmds << manufacturerSpecificGetCmd()
	}
	return cmds
}

List<Integer> getAssocDNIsSettingNodeIds(int group, String assocSetting) {
	List<Integer> nodeIds = convertHexListToIntList(assocSetting?.split(","))

	if (assocSetting && !nodeIds) {
		log.warn "'${assocSetting}' is not a valid value for the 'Device Network Ids for Association Group ${group}' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
	}
	else if (nodeIds?.size() >  5) {
		log.warn "The 'Device Network Ids for Association Group ${group}' setting contains more than 5 Ids so only the first 5 will be associated."
	}
	return nodeIds
}

Map getAssociationGroups() {
	Map groups = [:]
	buttons.each { btn ->
		groups += getButtonAssociationGroups(btn)	
	}
	return groups
}

Map getButtonAssociationGroups(Map btn) {
	Map groups = [:]
	groups[btn.basicSetGroup] = "basicSetAssociationGroup"
	groups[btn.multilevelGroup] = "multilevelAssociationGroup"
	return groups
}


def ping() {
	logDebug "ping()..."
	return [ switchBinaryGetCmd() ]
}


def setLedMode(mode) {
	logDebug "setLedMode($mode)..."
	setButtonLedMode(findButtonByNum(relayBtnNum), mode)
}

void setButtonLedMode(Map btn, String mode) {
	mode = mode?.toLowerCase()?.trim()

	Integer value = ledModeOptions.find { it.value.toLowerCase() == mode.toLowerCase() }?.key
	if (value != null) {
		sendConfigCmds(getLedModeParam(btn), value)
	}
	else {
		log.warn "${mode} is not a valid LED Mode"
	}
}


def setLedColor(color) {
	logDebug "setLedColor($color)..."
	setButtonLedColor(findButtonByNum(relayBtnNum), color)
}

void setButtonLedColor(Map btn, String color) {
	color = color?.toLowerCase()?.trim()

	Integer value = ledColorOptions.find { it.value.toLowerCase() == color }?.key
	if (value != null) {
		sendConfigCmds(getLedColorParam(btn), value)
	}
	else {
		log.warn "${color} is not a valid LED Color"
	}
}


def setLedBrightness(brightness) {
	logDebug "setLedBrightness($brightness)..."
	setButtonLedBrightness(findButtonByNum(relayBtnNum), brightness)
}

void setButtonLedBrightness(Map btn, String brightness) {
	brightness = brightness?.toLowerCase()?.trim()

	Integer value = ledBrightnessOptions.find { it.value == brightness }?.key
	if (value != null) {
		sendConfigCmds(getLedBrightnessParam(btn), value)
	}
	else {
		log.warn "${brightness} is not a valid LED Brightness"
	}
}


def on() {
	logDebug "on()..."
	return [ switchBinarySetCmd(0xFF) ]
}


def off() {
	logDebug "off()..."
	return [ switchBinarySetCmd(0x00) ]
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()

	initialize()

	if (isDuplicateCommand(state.lastRefresh, 2000)) {
		state.resyncAll = true
		runIn(5, executeConfigureCmds)
	}
	state.lastRefresh = new Date().time

	List<String> cmds = [
		switchBinaryGetCmd(),
		versionGetCmd()
	]

	buttons.each { btn ->
		cmds += getButtonRefreshCmds(btn)
	}

	sendCommands(cmds)
}

List<String> getButtonRefreshCmds(btn) {
	return [
		configGetCmd(getLedModeParam(btn)),
		configGetCmd(getLedColorParam(btn)),
		configGetCmd(getLedBrightnessParam(btn))
		
	]
}


void sendConfigCmds(Map param, int value) {
	sendCommands([
		configSetCmd(param, value),
		configGetCmd(param)
	])
}

void sendCommands(List<String> cmds, Integer delay=500) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}


String associationSetCmd(int group, nodes) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: group, nodeId: nodes))
}

String associationRemoveCmd(int group, nodes) {
	return secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: group, nodeId: nodes))
}

String associationGetCmd(int group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: group))
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

String indicatorGetCmd() {
	return secureCmd(zwave.indicatorV1.indicatorGet())
}

String indicatorSetCmd(int value) {
	return secureCmd(zwave.indicatorV1.indicatorSet(value: value))
}

String configSetCmd(Map param, int value) {
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

	runIn(4, refreshSyncStatus)

	Map param = allConfigParams.find { it.num == cmd.parameterNumber }
	if (param) {		
		int value = cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${value}"
		setParamStoredValue(param.num, value)
				
		handleLedEvent(param.num, value)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void handleLedEvent(int paramNum, int configVal) {
	buttons.each { btn ->		
		String name = ""
		String value = ""
		
		switch (paramNum) {
			case btn.ledModeParamNum:
				name = "ledMode"
				value = ledModeOptions.get(configVal)
				break
				
			case btn.ledColorParamNum:
				name = "ledColor"
				value = ledColorOptions.get(configVal)
				break
				
			case btn.ledBrightnessParamNum:
				name = "ledBrightness"
				value = ledBrightnessOptions.get(configVal)
				break
		}
		
		if (name) {
			if (btn.btnNum == relayBtnNum) {
				sendEventIfNew(name, value)
			}
			else {
				sendChildEvent(btn, name, value)
			}
		}
	}	
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "$cmd"

	logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"
	saveGroupAssociations(cmd.groupingIdentifier, cmd.nodeId)
}

void zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "$cmd"

	// The handler doesn't get association reports for 700 series devices when not joined with S2 so this report was requested to confirm the device is responding and saved based on the assumption that they were applied.

	associationGroups.each { groupId, name ->
		String assocSetting = settings["group${groupId}AssocDNIs"] ?: ""
		saveGroupAssociations(groupId, convertHexListToIntList(assocSetting?.split(",")))
	}
}

void saveGroupAssociations(groupId, nodeIds) {
	logTrace "saveGroupAssociations(${groupId}, ${nodeIds})"

	runIn(3, refreshSyncStatus)

	String name = associationGroups.get(safeToInt(groupId))
	if (name) {
		state["group${groupId}NodeIds"] = nodeIds

		def dnis = convertIntListToHexList(nodeIds)?.join(", ") ?: ""
		if (dnis) {
			dnis = "[${dnis}]" // wrapping it with brackets prevents ST from attempting to convert the value into a date.
		}
		
		Map btn = buttons.find { ((it.basicSetGroup == groupId) || (it.multilevelGroup == groupId)) }
		if (btn?.btnNum == relayBtnNum) {
			sendEventIfNew(name, dnis, false)
		}
		else {
			sendChildEvent(btn, name, dnis)
		}
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	logDebug "Firmware Version: ${fullVersion}"

	sendEventIfNew("firmwareVersion", fullVersion.toBigDecimal())
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "${cmd}"
	sendSwitchEvents(cmd.value)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "${cmd}"
	sendSwitchEvents(cmd.value)
}

void sendSwitchEvents(rawVal) {
	sendEventIfNew("switch", (rawVal ? "on" : "off"))
}


void zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	logTrace "${cmd}"
}


void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		logTrace "${cmd}"

		Map btn = findButtonByNum(cmd.sceneNumber)
		if (btn) {
			String value
			switch (cmd.keyAttributes){
				case 0:
					value = "pushed"
					break
				case 1:
					logDebug "released is not supported by SmartThings, but the event is being created so you can use it in other apps like WebCoRE."
					value = "released"
					break
				case 2:
					value = "held"
					break
				default:
					value = "pushed_${cmd.keyAttributes - 1}x"
			}

			sendButtonEvent(btn, value)
		}
		else {
			logDebug "Scene ${cmd.sceneNumber} is not a valid Button Number"
		}
	}
}

void sendButtonEvent(Map btn, String value) {
	if (btn.btnNum == relayBtnNum) {
		String desc = "${btn.name} ${value}"
		logDebug(desc)
		
		sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true, descriptionText: "${device.displayName} ${desc}")
	}
	else {
		sendChildEvent(btn, "button", value, [buttonNumber: 1])
	}
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}


int getPendingChanges() {
	int configChanges = safeToInt(configParams.count { it.value != getParamStoredValue(it.num) })
	int pendingAssocs = getConfigureRelayBtnAssocsCmds(true)?.size()

	return (configChanges + pendingAssocs)
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}

List<Map> getAllConfigParams() {
	List<Map> params = configParams
	buttons.each { btn ->
		params << getLedModeParam(btn)
		params << getLedColorParam(btn)
		params << getLedBrightnessParam(btn)
	}
	return params
}

List<Map> getConfigParams() {
	return [
		autoOffParam,
		autoOnParam,
		powerFailureRecoveryParam,
		relayControlParam,
		disabledRelayBehaviorParam,
		threeWaySwitchTypeParam
	]
}

Map getLedModeParam(Map btn) {
	return [num: btn.ledModeParamNum, name: "${btn.name} LED Mode", size: 1, options: ledModeOptions, btnNum: btn.btnNum]
}

Map getLedColorParam(Map btn) {
	return [num: btn.ledColorParamNum, name: "${btn.name} LED Color", size: 1, options: ledColorOptions, btnNum: btn.btnNum]
}

Map getLedBrightnessParam(Map btn) {
	return [num: btn.ledBrightnessParamNum, name: "${btn.name} LED Brightness", size: 1, options: ledColorBrightnessOptions, btnNum: btn.btnNum]
}

Map getAutoOffParam() {
	return getParam(16, "Auto Turn-Off Timer", 4, 0, autoOnOffOptions)
}

Map getAutoOnParam() {
	return getParam(17, "Auto Turn-On Timer", 4, 0, autoOnOffOptions)
}

Map getPowerFailureRecoveryParam() {
	return getParam(18, "Behavior After Power Outage", 1, 0, [0:"Restores Last Status [DEFAULT]", 1:"Forced to Off", 2:"Forced to On"])
}

Map getRelayControlParam() {
	return getParam(19, "Relay Control", 1, 1, [1:"Enable Physical and Z-Wave [DEFAULT]", 0:"Disable Physical", 2:"Disable Physical and Z-Wave"])
}

Map getDisabledRelayBehaviorParam() {
	return getParam(20, "Disabled Relay Behavior", 1, 0, [0:"Reports Status / Changes LED", 1:"Doesn't Report Status / Change LED [DEFAULT]"])
}

Map getThreeWaySwitchTypeParam() {
	return getParam(21, "3-Way Switch Type", 1, 0, [0:"Toggle On/Off Switch [DEFAULT]", 1:"Momentary Switch (ZAC99)"])
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options]
}


void sendEventIfNew(String name, value, boolean displayed=true) {
	String desc = "${name} is ${value}"
	if (device.currentValue(name) != value) {

		if (name != "syncStatus") {
			logDebug(desc)
		}

		sendEvent(name: name, value: value, descriptionText: "${device.displayName} ${desc}", displayed: displayed)
	}
	else {
		logTrace(desc)
	}
}


List<String> convertIntListToHexList(List<Integer> intList) {
	List<String> hexList = []
	intList?.each {
		hexList.add(Integer.toHexString(it).padLeft(2, "0").toUpperCase())
	}
	return hexList
}

List<Integer> convertHexListToIntList(String[] hexList) {
	List<Integer> intList = []

	hexList?.each {
		try {
			it = it.trim()
			intList.add(Integer.parseInt(it, 16))
		}
		catch (e) { }
	}
	return intList
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
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}


// -------------- CHILD DEVICES --------------
	 
void addChildButton(Map btn) {
	try {
		logDebug "Creating Child Device for ${btn.name}"
		addChildDevice(
			"Zooz",
			"Zooz Scene Controller Button",
			"${device.deviceNetworkId}:${btn.btnNum}",
			device.hubId,
			[
				isComponent: false,
				completedSetup: true,
				label: "${device.displayName}-${btn.name}"
			]
		)
	}
	catch (e) {
		log.warn "Unable to create child device for ${btn.name}.  You will get this error if you don't have the 'Zooz Scene Controller Button' DTH installed, but it can also happen during inclusion."
	}
}


void childUpdated(String dni, Map data) {
	logTrace "childUpdated(${dni}, ${data})..."

	Map btn = findButtonByDNI(dni)
	if (btn) {
		sendCommands(getConfigureAssocsCmds(btn, data))
	}
}


void childRefresh(String dni) {
	logTrace "childRefresh(${dni})..."

	Map btn = findButtonByDNI(dni)
	if (btn) {
		sendCommands(getButtonRefreshCmds(btn))
	}
}


void childSetLedMode(String dni, String mode) {
	logTrace "childSetLedMode(${dni}, ${mode})..."

	Map btn = findButtonByDNI(dni)
	if (btn) {
		setButtonLedMode(btn, mode)
	}
}


void childSetLedColor(String dni, String color) {
	logTrace "childSetLedColor(${dni}, ${color})..."

	Map btn = findButtonByDNI(dni)
	if (btn) {
		setButtonLedColor(btn, color)
	}
}


void childSetLedBrightness(String dni, String brightness) {
	logTrace "childSetLedBrightness(${dni}, ${brightness})..."

	Map btn = findButtonByDNI(dni)
	if (btn) {
		setButtonLedBrightness(btn, brightness)
	}
}


void sendChildEvent(Map btn, String name, value, data=null) {
	def child = findChildByButton(btn)
	if (child) {
		Map evt = [
			name: name, 
			value: value
		]

		if (data) {
			evt.isStateChange = true
			evt.data = data
		}
		child.parse(evt)
	}
	else {
		log.warn "Unable to create ${btn.name} ${name} ${value} event because the child device doesn't exist."
	}
}


def findChildByButton(Map btn) {
	return childDevices?.find { btn == findButtonByDNI(it.deviceNetworkId) }
}

Map findButtonByDNI(String dni) {
	Integer btnNum = safeToInt("${dni}".reverse().take(1), null)
	if (btnNum) {
		return findButtonByNum(btnNum)
	}
	else {
		log.warn "${dni} is not a valid Button DNI"
	}
}

Map findButtonByNum(Integer btnNum) {
	return buttons.find { it.btnNum == btnNum }
}


void logTrace(String msg) {
	// log.trace "$msg"
}
