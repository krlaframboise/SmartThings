/*
 *  Zooz ZEN71 700 Switch VER. 1.0
 *
 *  Changelog:
 *
 *    1.0 (12/31/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Zooz
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

@Field static Map assocGroups = [2:"associationGroupTwo"]
@Field static int assocMaxNodes = 5
@Field static BigDecimal assocMinFirmware = 1.03

@Field static Map autoOnOffOptions = [0:"Timer Disabled [DEFAULT]", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]


metadata {
	definition (
		name: "Zooz ZEN71 700 Switch",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		mnmn: "SmartThingsCommunity",
		vid: "4aea41e7-f884-3451-9605-eda6d88a7ad3"
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
		capability "platemusic11009.associationGroupTwo"
		capability "platemusic11009.syncStatus"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"027A", prod:"7000", model: "A001", deviceJoinName:"Zooz ZEN71 700 Switch"

		command "ledIndicatorOn"
		// command "ledIndicatorOff" // not supported by firmware, but should be...
	}

	simulator { }

	preferences {
		configParams.each { param ->
			createEnumInput("configParam${param.num}", "${param.name}:", param.value, param.options)
		}

		createEnumInput("debugOutput", "Enable Debug Logging?", 1, [0:"No", 1:"Yes [DEFAULT]"])

		input "assocInstructions", "paragraph",
			title: "Device Associations (MINIMUM FIRMWARE ${assocMinFirmware})",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this remote control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  The group(s) support up to ${assocMaxNodes} associations and you can use commas to separate the device network ids.",
			required: false

		input "assocInstructions2", "paragraph",
			title: "Associations Require S2 Security",
			description: "If the device fails to join with S2 Security you won't be able to use Associations. The 'networkSecurityLevel' field in the IDE will show 'ZWAVE_S2_FAILED' if it failed.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		assocGroups.each { group, name ->
			input "group${group}AssocDNIs", "string",
				title: "Enter Device Network IDs for Group ${group} Association:",
				required: false
		}
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

		runIn(2, executeConfigureCmds)
	}
	return []
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		def checkInterval = ((60 * 60 * 3) + (5 * 60))
		sendEvent([name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]])
	}

	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value: ["down","down_hold","down_2x","down_3x","down_4x","down_5x","up","up_hold","up_2x","up_3x","up_4x","up_5x"].encodeAsJSON(), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("up")
	}

	assocGroups.each { group, name ->
		if (device.currentValue(name) == null) {
			sendEvent(name: name, value: "")
		}
	}
}


def configure() {
	logDebug "configure()..."

	runIn(60, executeConfigureCmds)

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

	configParams.each { param ->
		Integer storedVal = getParamStoredValue(param.num)
		if (state.resyncAll || storedVal != param.value) {
			if (state.resyncAll != null) {
				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param, param.value)
			}
			cmds << configGetCmd(param)
		}
	}

	if (state.resyncAll != null) {
		cmds += getConfigureAssocsCmds()
	}

	state.resyncAll = false
	if (cmds) {
		sendCommands(cmds)
	}
}

private getConfigureAssocsCmds() {
	List<String> cmds = []

	assocGroups.each { group, name ->
		boolean changes = false

		def stateNodeIds = state["${name}NodeIds"]
		def settingNodeIds = getAssocDNIsSettingNodeIds(group)

		def newNodeIds = settingNodeIds?.findAll { !(it in stateNodeIds) }
		if (newNodeIds) {
			logDebug "Adding Nodes ${newNodeIds} to Association Group ${group}"
			cmds << associationSetCmd(group, newNodeIds)
			changes = true
		}

		def oldNodeIds = stateNodeIds?.findAll { !(it in settingNodeIds) }
		if (oldNodeIds) {
			logDebug "Removing Nodes ${oldNodeIds} from Association Group ${group}"
			cmds << associationRemoveCmd(group, oldNodeIds)
			changes = true
		}

		if (changes || state.resyncAll) {
			cmds << associationGetCmd(group)
		}
	}

	if (cmds) {
		if (device.currentValue("firmwareVersion") < assocMinFirmware) {
			log.warn "Firmware must be ${assocMinFirmware} or above to use Associations"
			cmds = []
		}
		else if (device.getDataValue("networkSecurityLevel") == "ZWAVE_S2_FAILED") {
			log.warn "Associations can't be used when S2 Security Failed"
			cmds = []
		}
	}
	return cmds
}

List<Integer> getAssocDNIsSettingNodeIds(int group) {
	String assocSetting = settings["group${group}AssocDNIs"] ?: ""

	List<Integer> nodeIds = convertHexListToIntList(assocSetting?.split(","))

	if (assocSetting && !nodeIds) {
		log.warn "'${assocSetting}' is not a valid value for the 'Device Network Ids for Association Group ${group}' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
	}
	else if (nodeIds?.size() >  assocMaxNodes) {
		log.warn "The 'Device Network Ids for Association Group ${group}' setting contains more than ${assocMaxNodes} Ids so only the first ${assocMaxNodes} will be associated."
	}

	return nodeIds
}


def ping() {
	logDebug "ping()..."
	return [ switchBinaryGetCmd() ]
}


def ledIndicatorOn() {
	logDebug "ledIndicatorOn()..."
	return delayBetween([ 
		indicatorSetCmd(0xFF), 
		indicatorGetCmd() 
	], 200)
}

def ledIndicatorOff() { 	
	logDebug "ledIndicatorOff()..."
	return delayBetween([ 
		indicatorSetCmd(0x00), 
		indicatorGetCmd() 
	], 200)
}


def on() {
	logDebug "on()..."
	return delayBetween([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	], 200)
}


def off() {
	logDebug "off()..."
	return delayBetween([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	], 200)
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()

	sendCommands([
		switchBinaryGetCmd(),
		versionGetCmd()
	])

	if (isDuplicateCommand(state.lastRefresh, 2000)) {
		state.resyncAll = true
		runIn(3, executeConfigureCmds)
	}
	state.lastRefresh = new Date().time

	return []
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
	logTrace "${cmd}"

	runIn(3, refreshSyncStatus)

	logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"

	String name = assocGroups.get(safeToInt(cmd.groupingIdentifier))
	if (name) {
		state["${name}NodeIds"] = cmd.nodeId

		def dnis = convertIntListToHexList(cmd.nodeId)?.join(", ") ?: ""
		if (dnis) {
			dnis = "[${dnis}]" // wrapping it with brackets prevents ST from attempting to convert the value into a date.
		}
		sendEventIfNew(name, dnis, false)
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

		String paddle = (cmd.sceneNumber == 1) ? "up" : "down"
		String btnVal
		switch (cmd.keyAttributes){
			case 0:
				btnVal = paddle
				break
			case 1:
				logDebug "${paddle}_released is not supported by SmartThings"
				btnVal = paddle + "_released"
				break
			case 2:
				btnVal = paddle + "_hold"
				break
			default:
				btnVal = paddle + "_${cmd.keyAttributes - 1}x"
		}

		sendButtonEvent(btnVal)
	}
}

void sendButtonEvent(String value) {
	String desc = "paddle ${value}"
	logDebug(desc)

	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true, descriptionText: "${device.displayName} ${desc}")
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
	int pendingAssocs = (getConfigureAssocsCmds()?.size() ? 1 : 0)

	return (configChanges + pendingAssocs)
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	return [
		paddleOrientationParam,
		ledIndicatorParam,
		ledColorParam,
		ledBrightnessParam,
		autoOffParam,
		autoOnParam,
		powerFailureRecoveryParam,
		threeWaySwitchTypeParam,
		relayControlParam,
		disabledRelayBehaviorParam,
		sceneControlParam
	]
}

Map getPaddleOrientationParam() {
	return getParam(1, "Paddle Orientation", 1, 0, [0:"Up for On, Down for Off [DEFAULT]", 1:"Up for Off, Down for On", 2:"Up or Down for On/Off"])
}

Map getLedIndicatorParam() {
	return getParam(2, "LED Indicator", 1, 0, [0:"LED On When Switch Off [DEFAULT]", 1:"LED On When Switch On", 2:"LED Always Off", 3:"LED Always On"])
}

Map getAutoOffParam() {
	return getParam(3, "Auto Turn-Off Timer", 4, 0, autoOnOffOptions)
}

Map getAutoOnParam() {
	return getParam(5, "Auto Turn-On Timer", 4, 0, autoOnOffOptions)
}

Map getPowerFailureRecoveryParam() {
	return getParam(8, "Behavior After Power Outage", 1, 2, [0:"Forced to Off", 1:"Forced to On", 2:"Restores Last Status [DEFAULT]"])
}

Map getSceneControlParam() {
	return getParam(9, "Scene Control", 1, 0, [0:"Disabled [DEFAULT]", 1:"Enabled"])
}

Map getRelayControlParam() {
	return getParam(11, "Relay Control", 1, 1, [1:"Enable paddle and Z-Wave [DEFAULT]", 0:"Disable Paddle", 2:"Disable Paddle and Z-Wave"])
}

Map getThreeWaySwitchTypeParam() {
	return getParam(12, "3-Way Switch Type", 1, 0, [0:"Toggle On/Off Switch [DEFAULT]", 1:"Momentary Switch (ZAC99)"])
}

Map getDisabledRelayBehaviorParam() {
	return getParam(13, "Disabled Relay Behavior", 1, 0, [0: "Reports Status / Changes LED [DEFAULT]", 1: "Doesn't Report Status / Change LED"])
}

Map getLedColorParam() {
	return getParam(14, "LED Indicator Color", 1, 1, [0:"White", 1:"Blue [DEFAULT]", 2:"Green", 3:"Red"])
}

Map getLedBrightnessParam() {
	return getParam(15, "LED Indicator Brightness", 1, 1, [0:"Bright (100%)", 1:"Medium (60%) [DEFAULT]", 2:"Low (30%)"])
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

void logTrace(String msg) {
	// log.trace "$msg"
}