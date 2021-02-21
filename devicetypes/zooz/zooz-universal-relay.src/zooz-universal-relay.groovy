/*
 *  Zooz Universal Relay - ZEN17 v1.0
 *
 *  Changelog:
 *
 *    1.0 (02/20/2021)
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
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// Multi Channel
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// Firmware Update Md
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security 0
	0x9F: 1		// Security 2
]

@Field static Map associationGroups = [2:"associationGroupTwo", 3:"associationGroupThree"]

@Field static List<Map> childInputTypes = [
	[configVal: 10, attribute: "switch", active: "on", inactive: "off", sensorType: 0, name: "Simulated Switch", namespace: "smartthings/testing"],
	[configVal: 4, attribute: "water", active: "wet", inactive: "dry", sensorType: 6, name: "Simulated Water Sensor", namespace: "smartthings/testing"],
	[configVal: 9, attribute: "smoke", active: "detected", inactive: "clear", sensorType: 4, name: "Simulated Smoke Alarm", namespace: "smartthings/testing"],
	[configVal: 6, attribute: "motion", active: "active", inactive: "inactive", sensorType: 12, name: "Simulated Motion Sensor", namespace: "smartthings/testing"],
	[configVal: 7, attribute: "contact", active: "open", inactive: "closed", sensorType: 10, name: "Simulated Contact Sensor", namespace: "smartthings/testing"]
]

metadata {
	definition (
		name: "Zooz Universal Relay",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		vid: "6e4ebb68-977d-3da4-a6b6-fd6d5074128e",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Actuator"
		capability "Switch"
		capability "Outlet"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"
		capability "platemusic11009.firmware"
		capability "platemusic11009.associationGroupTwo"
		capability "platemusic11009.associationGroupThree"
		capability "platemusic11009.syncStatus"

		attribute "lastCheckIn", "string"

		fingerprint manufacturer: "027A", prod: "7000", model: "A00A", deviceJoinName: "Zooz Universal Relay"
	}

	simulator { }

	preferences {

		configParams.each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					options: param.options
			}
			else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					range: param.range
			}
		}

		input "debugLogging", "enum",
			title: "Logging:",
			required: false,
			defaultValue: "1",
			options: ["0":"Disabled", "1":"Enabled [DEFAULT]"]

		input "assocInstructions", "paragraph",
			title: "Device Associations",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this remote control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  Group 2 and Group 3 supports up to 5 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "group2AssocDNIs", "string",
			title: "Enter Device Network IDs for Group 2 Relay 1 Association:",
			required: false

		input "group3AssocDNIs", "string",
			title: "Enter Device Network IDs for Group 3 Relay 2 Association:",
			required: false
	}
}


def installed () {
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time

		initialize()

		createChildRelays()
		createChildInputs()

		executeConfigureCmds()
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	sendEventIfNew("switch", "off", false)

	if (!device.currentValue("checkInterval")) {
		def checkInterval = (6 * 60 * 60) + (5 * 60)
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	if (device.currentValue("associationGroupTwo") == null) {
		sendEvent(name: "associationGroupTwo", value: "")
	}

	if (device.currentValue("associationGroupThree") == null) {
		sendEvent(name: "associationGroupThree", value: "")
	}
}


void executeConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	configParams.each {
		Integer storedVal = getParamStoredValue(it.num)
		Integer settingVal = safeToInt((settings ? settings["configParam${it.num}"] : null), null)
		if ((settingVal != null) && (settingVal != storedVal)) {
			logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${settingVal}"
			cmds << configSetCmd(it, settingVal)
			cmds << configGetCmd(it)
		}
	}

	cmds += getConfigureAssocsCmds()

	sendCommands(cmds)
}


List<String> getConfigureAssocsCmds(boolean countOnly=false) {
	List<String> cmds = []

	boolean failedS2 = (device?.getDataValue("networkSecurityLevel") == "ZWAVE_S2_FAILED")

	associationGroups.each { group, name ->
		boolean changes = false

		def stateNodeIds = state["${name}NodeIds"]
		def settingNodeIds = getAssocDNIsSettingNodeIds(group)

		def newNodeIds = settingNodeIds?.findAll { !(it in stateNodeIds) }
		if (newNodeIds) {
			if (!countOnly) {
				logDebug "Adding Nodes ${newNodeIds} to Association Group ${group}"
			}

			cmds << associationSetCmd(group, newNodeIds)
			changes = true
		}

		def oldNodeIds = stateNodeIds?.findAll { !(it in settingNodeIds) }
		if (oldNodeIds) {
			if (!countOnly) {
				logDebug "Removing Nodes ${oldNodeIds} from Association Group ${group}"
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

List<Integer> getAssocDNIsSettingNodeIds(int group) {
	String assocSetting = settings["group${group}AssocDNIs"] ?: ""

	List<Integer> nodeIds = convertHexListToIntList(assocSetting?.split(","))

	if (assocSetting && !nodeIds) {
		log.warn "'${assocSetting}' is not a valid value for the 'Device Network Ids for Association Group ${group}' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
	}
	else if (nodeIds?.size() >  5) {
		log.warn "The 'Device Network Ids for Association Group ${group}' setting contains more than 5 Ids so only the first 5 will be associated."
	}

	return nodeIds
}


def ping() {
	logDebug "ping()..."
	return sendCommands([ versionGetCmd() ])
}


def on() {
	logDebug "on()..."
	return [ switchBinarySetCmd(0xFF) ]
}

def off() {
	logDebug "off()..."
	return [ switchBinarySetCmd(0x00) ]
}


def configure() {
	logDebug "configure()..."

	runIn(4, refresh)
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()
	
	createChildRelays()
	createChildInputs()

	def cmds = [
		versionGetCmd(),
		switchBinaryGetCmd()
	]
	
	(1..4).each {
		cmds << switchBinaryGetCmd(it)
		cmds << sensorBinaryGetCmd(it)
	}

	configParams.each {
		cmds << configGetCmd(it)
	}

	sendCommands(cmds, 250)
}

void sendCommands(List<String> cmds, Integer delay=100) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}


String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String manufacturerSpecificGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

String associationSetCmd(int group, nodes) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: group, nodeId: nodes))
}

String associationGetCmd(int group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: group))
}

String associationRemoveCmd(int group, nodes) {
	return secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: group, nodeId: nodes))
}

String sensorBinaryGetCmd(int endpoint) {
	return multiChannelCmdEncapCmd(zwave.sensorBinaryV1.sensorBinaryGet(), endpoint)
}

String switchBinaryGetCmd(Integer endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
}

String switchBinarySetCmd(int value, Integer endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: value), endpoint)
}

String configSetCmd(Map param, int value) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

String multiChannelCmdEncapCmd(cmd, Integer endpoint) {
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


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
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


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "$cmd"

	sendEventIfNew("firmwareVersion", (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "$cmd"

	logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"
	saveGroupAssociations(cmd.groupingIdentifier, cmd.nodeId)
}

void zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "$cmd"

	// The handler doesn't get association reports for 700 series devices when not joined with S2 so this report was requested to confirm the device is responding and saved based on the assumption that they were applied.

	associationGroups.each { group, name ->
		String assocSetting = settings["group${group}AssocDNIs"] ?: ""
		saveGroupAssociations(group, convertHexListToIntList(assocSetting?.split(",")))
	}
}

void saveGroupAssociations(groupId, nodeIds) {
	logTrace "saveGroupAssociations(${groupId}, ${nodeIds})"

	runIn(3, refreshSyncStatus)

	String name = associationGroups.get(safeToInt(groupId))
	if (name) {
		state["${name}NodeIds"] = nodeIds

		def dnis = convertIntListToHexList(nodeIds)?.join(", ") ?: ""
		if (dnis) {
			dnis = "[${dnis}]" // wrapping it with brackets prevents ST from attempting to convert the value into a date.
		}
		sendEventIfNew(name, dnis, false)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logTrace "${cmd}"

	runIn(4, refreshSyncStatus)

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)

		if (param in relayTypeParams) {
			createChildInputs()
		}
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	int configChanges = safeToInt(configParams.count { ((getSettingValue(it.num) != null) && (getSettingValue(it.num) != getParamStoredValue(it.num))) })
	int pendingAssocs = (getConfigureAssocsCmds(true)?.size() ? 1 : 0)

	return (configChanges + pendingAssocs)
}

Integer getSettingValue(int paramNum) {
	return safeToInt(settings && settings["configVal${paramNum}"], null)
}

Integer getParamStoredValue(int paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

void setParamStoredValue(int paramNum, int value) {
	state["configVal${paramNum}"] = value
}


void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=0) {
	logTrace "${cmd} (Endpoint ${endpoint})"

	String value = (cmd.value == 0xFF) ? "on" : "off"

	if (endpoint) {
		def child = findChildByDNI(getRelayDNI(endpoint == 1 ? 1 : 2))		
		if (child) {
			String desc = "${child.displayName}: switch is ${value}"
			if (child.currentValue("switch") != value) {
				logDebug "${desc}"
				child.sendEvent(name: "switch", value: value, descriptionText: desc)
			}
			else {
				logTrace "${desc}"
			}
		}
	}
	else {
		sendEventIfNew("switch", value)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, endpoint=0) {
	logTrace "${cmd} (Endpoint ${endpoint})"

	// handleChildInputEvent((cmd.event && (cmd.event != 23)), endpoint)
}


void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd, endpoint=0) {
	logTrace "${cmd} (Endpoint ${endpoint})"

	handleChildInputEvent(cmd.sensorValue, endpoint)
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	logTrace "${cmd} (Endpoint ${endpoint})"

	handleChildInputEvent(cmd.value, endpoint)
}

void handleChildInputEvent(rawValue, endpoint) {
	int relay = (endpoint == 1 ? 1 : 2)
	
	childDevices?.each { child ->
		if (child.deviceNetworkId.contains(getInputSuffix(relay))) {
			
			Map inputType = childInputTypes.find { child.deviceNetworkId == getInputDNI(it, relay) }
			if (inputType) {
				
				def value = (rawValue ? inputType.active : inputType.inactive)
				logDebug "${child.displayName} ${inputType.attribute} ${value}"
				
				child.sendEvent(name: inputType.attribute, value: value)
			}
		}		
	}	
}


void zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=0) {
	logTrace "Unhandled zwaveEvent: ${cmd} (Endpoint ${endpoint})"
}


List<Map> getConfigParams() {
	return [
		powerFailureRecoveryParam,
		ledIndicatorModeParam,
		relay1TypeParam,
		relay1InputTriggerParam,
		relay2TypeParam,
		relay2InputTriggerParam,
		dcMotorModeParam,
		relay1AutoOffParam,
		relay1AutoOffUnitParam,
		relay1AutoOnParam,
		relay1AutoOnUnitParam,
		relay2AutoOffParam,
		relay2AutoOffUnitParam,
		relay2AutoOnParam,
		relay2AutoOnUnitParam
	]
}

Map getPowerFailureRecoveryParam() {
	return [num: 1, name: "On/Off Status Recovery After Power Failure", size: 1, options: [0:"Turn All Relays Off", 1:"Restore Relay States From Before Power Failure [DEFAULT]", 2:"Turn All Relays On", 3:"Restore Relay 1 and Turn On Relay 2", 4:"Restore Relay 2 and Turn On Relay 1"]]
}

List<Map> getRelayTypeParams() {
	return [
		relay1TypeParam,
		relay2TypeParam
	]
}

Map getRelay1TypeParam() {
	return getRelayTypeParam(2, 1)
}
Map getRelay2TypeParam() {
	return getRelayTypeParam(3, 2)
}
Map getRelayTypeParam(num, relay) {
	Map options = [0:"Momentary Switch", 1:"Toggle Switch", 2:"Toggle Switch (any change) [DEFAULT]", 3:"Garage Door", 4:"Water Sensor", 6:"Motion Sensor", 7:"Contact Sensor", 9:"Smoke Detector", 10:"On/Off Report"]

	// These options aren't included because ST doesn't have virtual devices for them.
	//[5:"Heat Alarm", 8:"CO Detector"]

	return [num: num, name: "Input Type for Relay ${relay}", size: 1, options: options, relay: relay]
}

Map getLedIndicatorModeParam() {
	return [num: 5, name: "LED Indicator Control", size: 1, options: [0:"On when ALL Relays are Off [DEFAULT]", 1:"On when ANY Relay is On", 2:"Always Off", 3:"Always On"]]
}

Map getRelay1AutoOffParam() {
	return getAutoOnOffParam(6, "Off", 1)
}
Map getRelay1AutoOnParam() {
	return getAutoOnOffParam(7, "On", 1)
}
Map getRelay2AutoOffParam() {
	return getAutoOnOffParam(8, "Off", 2)
}
Map getRelay2AutoOnParam() {
	return getAutoOnOffParam(9, "On", 2)
}
Map getAutoOnOffParam(num, onOff, relay) {
	return [num: num, name: "Auto Turn-${onOff} Timer for Relay ${relay} (0=Disabled, 1-65535)", size: 4, range: "0..65535"]
}

Map getRelay1InputTriggerParam() {
	return getInputTriggerParam(10, 1)
}
Map getRelay2InputTriggerParam() {
	return getInputTriggerParam(11, 2)
}
Map getInputTriggerParam(num, relay) {
	return [num: num, name: "Input Trigger for Relay ${relay}", size: 1, options: [0:"Disabled", 1:"Enabled [DEFAULT]"]]
}

Map getRelay1AutoOffUnitParam() {
	return getAutoOnOffUnitParam(15, "Off", 1)
}
Map getRelay1AutoOnUnitParam() {
	return getAutoOnOffUnitParam(16, "On", 1)
}
Map getRelay2AutoOffUnitParam() {
	return getAutoOnOffUnitParam(17, "Off", 2)
}
Map getRelay2AutoOnUnitParam() {
	return getAutoOnOffUnitParam(18, "On", 2)
}
Map getAutoOnOffUnitParam(num, onOff, relay) {
	return [num: num, name: "Auto Turn-${onOff} Timer Unit for Relay ${relay}", size: 1, options: [0:"Minutes [DEFAULT]", 1:"Seconds", 2:"Hours"]]
}

Map getDcMotorModeParam() {
	return [num: 24, name: "DC Motor Mode", size: 1, options: [0:"Disabled [DEFAULT]", 1:"Enabled"]]
}


void sendEventIfNew(String name, value, boolean displayed=true) {
	String desc = "${device.displayName}: ${name} is ${value}"
	if (device.currentValue(name) != value) {

		if (name != "syncStatus") {
			logDebug(desc)
		}

		sendEvent(name: name, value: value, descriptionText: desc, displayed: displayed)
	}
	else {
		logTrace(desc)
	}
}


def childOn(dni) {
	logDebug "childOn(${dni})..."
	sendChildOnOffCmds(0xFF, dni)
}

def childOff(dni) {
	logDebug "childOff(${dni})..."
	sendChildOnOffCmds(0x00, dni)
}

void sendChildOnOffCmds(int value, String dni) {
	sendCommands([
		switchBinarySetCmd(value, (dni.endsWith(getRelaySuffix(1)) ? 1 : 2))
	])
}


void createChildRelays() {
	logTrace "createChildRelays()..."

	(1..2).each { relay ->
		String dni = getRelayDNI(relay)

		def child = findChildByDNI(dni)
		if (!child) {
			String name = "Relay ${relay}"
			logDebug "Creating Child Switch for ${name}"

			addChildDevice("smartthings", "Child Switch", dni, device.hubId,
				[
					completedSetup: true,
					label: "${device.displayName}-${name}",
					isComponent: false,
					data: [relay: relay]
				]
			)			
		}
	}
}


void createChildInputs() {
	logTrace "createChildInputs()..."
	
	boolean added = false

	relayTypeParams.each { param ->

		Map inputType = childInputTypes.find { it.configVal == getParamStoredValue(param.num) }
		if (inputType) {

			String dni = getInputDNI(inputType, param.relay)

			if (!findChildByDNI(dni)) {
				String name = "Relay ${param.relay}"
				logDebug "Creating Child ${inputType.attribute} for ${name}"

				addChildDevice(inputType.namespace, inputType.name, dni, device.hubId,
					[
						completedSetup: true,
						label: "${device.displayName}-${name} ${inputType.attribute.capitalize()}",
						isComponent: false,
						data: [ relay: param.relay ]
					]
				)
				
				added = true
			}
		}
	}
	
	if (added) {
		sendCommands([
			sensorBinaryGetCmd(1),
			sensorBinaryGetCmd(2),
			sensorBinaryGetCmd(3),
			sensorBinaryGetCmd(4)
		], 300)
	}
}


String getRelayDNI(int relay) {
	return "${device.deviceNetworkId}${getRelaySuffix(relay)}"
}

String getRelaySuffix(int relay) {
	return ":R" + relay.toString()
}

String getInputDNI(Map inputType, int relay) {
	return "${device.deviceNetworkId}${getInputSuffix(relay)}-${inputType.attribute}"
}

String getInputSuffix(int relay) {
	return ":I" + relay.toString()
}

private findChildByDNI(String dni) {
	return childDevices?.find { it.deviceNetworkId == dni }
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