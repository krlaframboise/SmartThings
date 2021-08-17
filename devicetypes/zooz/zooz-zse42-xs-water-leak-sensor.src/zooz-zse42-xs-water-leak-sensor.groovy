/*
 *  Zooz ZSE42 XS Water Leak Sensor v1.0
 *
 *
 *  Changelog:
 *
 *    1.0 (08/16/2021)
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
	0x30: 1,	// SensorBinary
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x71: 3,	// Alarm v1 or Notification v4
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// FirmwareUpdateMd
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 1,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x9F: 1 	// Security 2
]

@Field static Map associationGroups = [2:"associationGroupTwo"]
@Field static int waterAlarm = 5
@Field static int wakeUpIntervalSeconds = 43200

metadata {
	definition (
		name: "Zooz ZSE42 XS Water Leak Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType:"x.com.st.d.sensor.moisture",
		vid: "a15f6dd7-982f-3cf0-aa9b-4e04114f3f8e",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Sensor"
		capability "Water Sensor"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"
		capability "platemusic11009.firmware"
		capability "platemusic11009.associationGroupTwo"
		capability "platemusic11009.syncStatus"

		fingerprint mfr:"027A", prod:"7000", model:"E002", deviceJoinName: "Zooz ZSE42 XS Water Leak Sensor" // zw:Ss2a type:0701 mfr:027A prod:7000 model:E002 ver:1.05 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,85,8E,59,72,5A,87,73,80,71,30,70,84,7A
	}

	preferences {
		configParams.each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			} else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
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
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this remote control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  Group 2 supports up to 5 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "group2AssocDNIs", "string",
			title: "Enter Device Network IDs for Group 2 Relay 1 Association:",
			required: false
	}
}

def installed() {
	logDebug "installed()..."
	state.pendingRefresh = true
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time
		runIn(2, refreshSyncStatus)

		logDebug "updated()..."
		initialize()

		if (pendingChanges) {
			logForceWakeupMessage("The configuration changes will be sent to the device the next time it wakes up.")
		}
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)

	if (!device.currentValue("water")) {
		sendEvent(name: "water", value: "dry")
	}

	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((wakeUpIntervalSeconds * 2) + 300), displayed: falsle, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}

def configure() {
	logDebug "configure()..."
	state.pendingRefresh = true
	sendCommands(getConfigureCmds())
}

List<String> getConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << batteryGetCmd()
		cmds << sensorBinaryGetCmd()
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if (state.pendingRefresh || (state.wakeUpInterval != wakeUpIntervalSeconds)) {
		logDebug "Changing wake up interval to ${wakeUpIntervalSeconds} seconds"
		cmds << wakeUpIntervalSetCmd(wakeUpIntervalSeconds)
		cmds << wakeUpIntervalGetCmd()
	}

	configParams.each {
		Integer storedVal = getParamStoredValue(it.num)
		Integer settingVal = getSettingValue(it.num)
		if ((settingVal != null) && (settingVal != storedVal)) {
			logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${settingVal}"
			cmds << configSetCmd(it, settingVal)
			cmds << configGetCmd(it)
		}
	}

	cmds += getConfigureAssocsCmds()

	state.pendingRefresh = false

	return cmds
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
	} else if (nodeIds?.size() >  5) {
		log.warn "The 'Device Network Ids for Association Group ${group}' setting contains more than 5 Ids so only the first 5 will be associated."
	}

	return nodeIds
}

def ping() {
	logDebug "ping()"
}

def refresh() {
	logDebug "refresh()..."
	refreshSyncStatus()
	state.pendingRefresh = true
	logForceWakeupMessage("The device will be refreshed the next time it wakes up.")
}

void logForceWakeupMessage(String msg) {
	log.warn "${msg}  To force the device to wake up immediately press the action button 4x quickly."
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

String wakeUpIntervalSetCmd(int seconds) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

String wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
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

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String sensorBinaryGetCmd() {
	return secureCmd(zwave.sensorBinaryV1.sensorBinaryGet())
}

String configSetCmd(Map param, int value) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		return cmd.format()
	}
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

def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} else {
		log.warn "Unable to parse: $description"
	}
	return []
}

void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up..."
	List<String> cmds = []
	cmds += getConfigureCmds()

	if (cmds) {
		cmds << "delay 500"
	} else {
		cmds << batteryGetCmd()
	}

	cmds << wakeUpNoMoreInfoCmd()
	sendCommands(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logDebug "Wake Up Interval = ${cmd.seconds} seconds"
	state.wakeUpInterval = cmd.seconds
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : safeToInt(cmd.batteryLevel))
	if (val > 100) val = 100
	if (val < 1) val = 1

	logDebug "${device.displayName}: battery is ${val}%"
	sendEvent(name: "battery", value: val, unit: "%", isStateChange: true, displayed:true)
}

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	logDebug "${cmd}"
	sendWaterEvent(cmd.sensorValue)
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	if (cmd.notificationType == waterAlarm) {
		sendWaterEvent(cmd.event)
	}
}

void sendWaterEvent(rawVal) {
	sendEventIfNew("water", (rawVal ? "wet" : "dry"))
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"
	saveGroupAssociations(cmd.groupingIdentifier, cmd.nodeId)
}

void zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logDebug "${cmd}"
	// The handler doesn't get association reports for 700 series devices when not joined with S2 so this report was requested to confirm the device is responding and saved based on the assumption that they were applied.
	associationGroups.each { group, name ->
		String assocSetting = settings["group${group}AssocDNIs"] ?: ""
		saveGroupAssociations(group, convertHexListToIntList(assocSetting?.split(",")))
	}
}

void saveGroupAssociations(groupId, nodeIds) {
	logDebug "saveGroupAssociations(${groupId}, ${nodeIds})"
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
	runIn(4, refreshSyncStatus)

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)
	} else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: ${cmd}"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	int configChanges = safeToInt(configParams.count {
	((getSettingValue(it.num) != null) && (getSettingValue(it.num) != getParamStoredValue(it.num))) })
	int pendingAssocs = (getConfigureAssocsCmds(true)?.size() ? 1 : 0)

	return (configChanges + pendingAssocs)
}

Integer getSettingValue(int paramNum) {
	return safeToInt(settings ? settings["configParam${paramNum}"] : null, null)
}

Integer getParamStoredValue(int paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

void setParamStoredValue(int paramNum, int value) {
	state["configVal${paramNum}"] = value
}

void sendEventIfNew(String name, value, boolean displayed=true) {
	String desc = "${device.displayName}: ${name} is ${value}"
	if (device.currentValue(name) != value) {
		if (name != "syncStatus") {
			logDebug(desc)
		}
		sendEvent(name: name, value: value, descriptionText: desc, displayed: displayed)
	}
}

List<Map> getConfigParams() {
	return [
		ledParam,
		leakClearDelayParam,
		lowBatteryParam,
		leakCommandGroup2Param
	]
}

Map getLedParam() {
	return [num: 1, name: "LED Indicator", size: 1, defaultVal: 1, options: [1:"Enabled [DEFAULT]", 0:"Disabled"]]
}

Map getLeakClearDelayParam() {
	return [num: 2, name: "Leak Alert Clear Delay", size: 4, defaultVal: 0, range:"0..3600"]
}

Map getLowBatteryParam() {
	return [num: 4, name: "Low Battery Alert", size: 1, defaultVal: 20, options: [10:"10%", 20:"20% [DEFAULT]", 30:"30%", 40:"40%", 50:"50%"]]
}

Map getLeakCommandGroup2Param() {
	return [num: 5, name: "Leak Alert Command for Group 2", size: 1, defaultVal: 0, options: [0:"None [DEFAULT]", 1:"On", 2:"Off"]]
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
		} catch (e) {
		}
	}
	return intList
}

Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	} else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	} else {
		return defaultVal
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