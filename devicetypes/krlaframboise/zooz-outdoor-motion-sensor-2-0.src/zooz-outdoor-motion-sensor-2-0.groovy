/**
 *  Zooz Outdoor Motion Sensor 2.2  (FIRMWARE >= 2.0)
 *    (Model: ZSE29)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-zooz-outdoor-motion-sensor-ver-2-0-zse29/180195
 *
 *
 *  Changelog:
 *
 *    2.2 (05/24/2020)
 *      - Add lifeline association during configure if it hasn't already been added.
 *      - Added syncStatus tile.
 *
 *    2.1 (05/04/2020)
 *      - Added support for associations.
 *      - Updated wake up instructions and made them also display on update.
 *      - Changed icons to urls to get them to show in the Android classic app.
 *      - Added icons for lux and battery
 *
 *    2.0.2 (03/18/2020)
 *      - Force state change on all battery events and make it request the battery every time it wakes up.
 *
 *    2.0.1 (03/13/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    2.0 (12/08/2019)
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

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x31: 5,	// Sensor Multilevel (v7)
	0x59: 1,	// AssociationGrpInfo
	0x55: 1,	// Transport Service
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x71: 3,	// Notification (4)
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// Firmware Update Md (3)
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x98: 1,	// Security 0
	0x9F: 1		// Security 2
]

metadata {
	definition (name:"Zooz Outdoor Motion Sensor 2.0", namespace:"krlaframboise", author: "Kevin LaFramboise", ocfDeviceType: "x.com.st.d.sensor.motion", vid: "generic-motion-5") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		attribute "syncStatus", "string"
		attribute "firmwareVersion", "string"
		attribute "firmwareSupported", "string"
		attribute "associatedDeviceNetworkIds", "string"

		fingerprint mfr: "027A", prod: "0001", model: "0005", deviceJoinName: "Zooz Outdoor Motion Sensor 2.0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "inactive",
					label:'NO MOTION',
					icon:"${resourcesUrl}motion-inactive.png", backgroundColor:"#ffffff"
				attributeState "active",
					label:'MOTION',
					icon:"${resourcesUrl}motion-inactive.png", backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.tamper", key: "SECONDARY_CONTROL") {
				attributeState("clear", label:'')
				attributeState("detected", label:'TAMPERING')
			}
		}

		valueTile("illuminance", "device.illuminance", width: 2, height: 2){
			state "default", label:'${currentValue} lux', icon: "${resourcesUrl}light.png"
		}

		standardTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}%', icon: "${resourcesUrl}battery-default.png"
			state "100", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "99", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "98", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "97", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "96", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "95", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "1", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
			state "2", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
			state "3", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
			state "4", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
			state "5", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"${resourcesUrl}refresh.png"
		}

		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "syncStatus", label:'${currentValue}'
		}

		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}

		valueTile("firmwareSupported", "device.firmwareSupported", decoration:"flat", width:4, height: 2) {
			state "yes", label:''
			state "no", label:'This DTH only supports firmware 2.0 and above'
		}

		standardTile("assocLabel", "device.associatedDeviceNetworkIds", decoration: "flat", width: 3, height: 1) {
			state "default", label:'Associated Device Network Ids:'
			state "none", label:""
		}

		standardTile("assocDNIs", "device.associatedDeviceNetworkIds", decoration: "flat", width: 3, height: 1) {
			state "default", label:'${currentValue}'
			state "none", label:""
		}

		main("mainTile")
		details(["mainTile", "illuminance", "battery", "refresh", "syncStatus", "firmwareVersion", "assocLabel", "assocDNIs", "firmwareSupported"])
	}

	preferences {

		getParamInput(motionEnabledParam)
		getParamInput(motionSensitivityParam)
		getParamInput(motionClearedDelayParam)
		getParamInput(luxLevelTriggerParam)
		getParamInput(luxReportingParam)

		getOptionsInput("checkInInterval", "Check In Interval:", checkInIntervalSetting, checkInIntervalOptions)

		input "assocInstructions", "paragraph",
			title: "Device Associations",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this motion sensor control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id below.  It supports up to 4 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "assocDNIs", "string",
			title: "Enter Device Network IDs for Association: (Enter 0 to clear field in new iOS mobile app)",
			required: false

		getParamInput(group2BasicSetParam)

		input "debugOutput", "bool",
			title: "Enable debug logging?",
			defaultValue: true,
			displayDuringSetup: true,
			required: false
	}
}

private getParamInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		options: param.options
}

private getOptionsInput(name, title, defaultValue, options) {
	input "${name}", "enum",
		title: "${title}:",
		required: false,
		defaultValue: "${defaultValue}",
		options: setDefaultOption(options, defaultValue)
}

private getAssocDNIsSetting() {
	def val = settings?.assocDNIs
	return ((val && (val.trim() != "0")) ? val : "") // new iOS app has no way of clearing string input so workaround is to have users enter 0.
}

private getResourcesUrl() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/"
}


def installed() {
	sendEvent(name: "tamper", value: "clear", displayed: false)

	state.syncAll = true
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time
		logDebug "updated()"

		logForceWakeupMessage "The setting changes will be sent to the device the next time it wakes up."

		refreshSyncStatus()
	}
}


def configure() {
	logDebug "configure()"

	def cmds = getConfigureCmds()
	return (cmds ? delayBetween(cmds, 500) : [])
}

private getConfigureCmds() {
	def cmds = []

	runIn(4, refreshSyncStatus)

	if (!state.checkInInterval) {
		// First time configuring so give it time for inclusion to finish.
		cmds << "delay 2000"
	}

	if (state.syncAll || state.checkInInterval != checkInIntervalSetting) {
		cmds << wakeUpIntervalSetCmd(checkInIntervalSetting)
		cmds << wakeUpIntervalGetCmd()
	}

	if (state.refreshAll || !device.currentValue("illuminance")) {
		cmds << sensorMultilevelGetCmd(lightSensorType)
	}

	if (state.refreshAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	cmds << batteryGetCmd()

	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if (state.syncAll || "${storedVal}" != "${param.value}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}

	cmds += getConfigureAssocsCmds()

	state.syncAll = false
	state.refreshAll = false
	return cmds
}

private firmwareSupportsParam(param) {
	return (!param.minFirmware || !firmwareVersion || firmwareVersion >= param.minFirmware)
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}


private getConfigureAssocsCmds() {
	def cmds = []

	if (!device.currentValue("associatedDeviceNetworkIds")) {
		sendEventIfNew("associatedDeviceNetworkIds", "none", false)
	}

	def settingNodeIds = assocDNIsSettingNodeIds

	def newNodeIds = settingNodeIds?.findAll { !(it in state.assocNodeIds) }
	if (newNodeIds) {
		cmds << associationSetCmd(2, newNodeIds)
	}

	def oldNodeIds = state.assocNodeIds?.findAll { !(it in settingNodeIds) }
	if (oldNodeIds) {
		cmds << associationRemoveCmd(2, oldNodeIds)
	}

	if (cmds || state.syncAll) {
		cmds << associationGetCmd(2)
	}

	if (!state.group1Assoc || state.syncAll) {
		if (state.group1Assoc == false) {
			logDebug "Adding missing lifeline association..."
			cmds << associationSetCmd(1, [zwaveHubNodeId])
		}
		cmds << associationGetCmd(1)
	}

	return cmds
}


private getAssocDNIsSettingNodeIds() {
	def nodeIds = convertHexListToIntList(assocDNIsSetting?.split(","))

	if (assocDNIsSetting && !nodeIds) {
		log.warn "'${assocDNIsSetting}' is not a valid value for the 'Device Network Ids for Association' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
	}
	else if (nodeIds?.size() >  4) {
		log.warn "The 'Device Network Ids for Association' setting contains more than 4 Ids so only the first 4 will be associated."
	}

	return nodeIds
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"
}


def refresh() {	
	logDebug "refresh()..."
	
	state.refreshAll = true
	if (!pendingChanges) {
		logForceWakeupMessage "The next time the device wakes up, all settings will be sent to it and the sensor data will be requested."
		state.syncAll = true
	}
	else {
		logForceWakeupMessage "The next time the device wakes up, the sensor data will be requested."
	}

	refreshSyncStatus()
	
	return []
}

private logForceWakeupMessage(msg) {
	log.warn "${msg}  You can force the device to wake up immediately by pressing the tamper switch 3 times."
}


private wakeUpIntervalSetCmd(seconds) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private associationSetCmd(group, nodes) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: group, nodeId: nodes))
}

private associationRemoveCmd(group, nodes) {
	return secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: group, nodeId: nodes))
}

private associationGetCmd(group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: group))
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 1, sensorType: sensorType))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}


def parse(String description) {
	def result = []
	try {
		sendLastCheckInEvent()

		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unknown Description: $desc"
		}
	}
	catch (e) {
		log.error "$e"
	}
	return result
}

private sendLastCheckInEvent() {
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time
		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
}

private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}
	}
	catch (e) {
		return "$dt"
	}
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)

	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"

	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	state.checkInInterval = cmd.seconds

	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 2) + (5 * 60))

	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	return []
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up..."
	
	def cmds = getConfigureCmds()
	if (cmds) {
		cmds = delayBetween(cmds, 500)
		cmds << "delay 1500"
	}
	cmds << wakeUpNoMoreInfoCmd()

	return response(cmds)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)

	if (val > 100) {
		val = 100
	}

	logDebug "Battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%", isStateChange: true)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"

	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	def supported = (safeToDec(version) >= 2.0 ? "yes" : "no")

	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)
		sendEvent(name:"firmwareSupported", value: supported, displayed:false)
	}

	if (supported == "no") {
		log.warn "This DTH was written for the Zooz Outdoor Motion Sensor 2.0, but your device has firmware ${version} so you need to use the DTH for the old model:https://community.smartthings.com/t/release-zooz-outdoor-motion-sensor-zse29/142893"
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "${cmd}"

	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	if (cmd.groupingIdentifier == 1) {
		logDebug "Lifeline Association: ${cmd.nodeId}"
		
		state.group1Assoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false		
	}
	else if (cmd.groupingIdentifier == 2) {
		logDebug "Group 2 Association: ${cmd.nodeId}"
		
		state.assocNodeIds = cmd.nodeId

		def dnis = convertIntListToHexList(cmd.nodeId)?.join(", ") ?: "none"
		sendEventIfNew("associatedDeviceNetworkIds", dnis, false)
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "BasicSet: $cmd"

	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"

	def val = cmd.scaledSensorValue
	switch (cmd.sensorType) {
		case lightSensorType:
			sendEvent(name:"illuminance", value:cmd.scaledSensorValue, unit:"lux", isStateChange: true)
			break
		default:
			logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"

	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 3:
				sendTamperEventMap("detected")
				break
			case 8:
				sendMotionEventMap("active")
				break
			case 0:
				if (cmd.eventParametersLength && cmd.eventParameter[0] == 8) {
					sendMotionEventMap("inactive")
				}
				else {
					sendTamperEventMap("clear")
				}
				break
			default:
				logTrace "Unknown Notification Event: ${cmd.event}"
		}
	}
	else if (cmd.notificationType == 8 && cmd.event == 1) {
		logDebug "Device Powered On"
		def cmds = configure()
		return cmds ? response(cmds) : []
	}
	else {
		logTrace "Unknown Notification Type: ${cmd.notificationType}"
	}
	return []
}

private sendTamperEventMap(val) {
	logDebug "Tamper is ${val}"
	sendEvent(name:"tamper", value:val, displayed:(val == "detected"), isStateChange: true)
}

private sendMotionEventMap(val) {
	logDebug "Motion is ${val}"
	sendEvent(name:"motion", value:val, isStateChange: true)
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logTrace "ConfigurationReport ${cmd}"

	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.scaledConfigurationValue

		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
	return []
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}


private updateSyncingStatus() {
	sendEventIfNew("syncStatus", "Syncing...", false)
}

def refreshSyncStatus() {
	def changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

private getPendingChanges() {
	def pendingParams = configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
	def pendingAssocs = getConfigureAssocsCmds()?.size() ?: 0
	def pendingCheckInInterval = (state.checkInInterval == checkInIntervalSetting) ? 0 : 1
	return (pendingParams + pendingAssocs + pendingCheckInInterval)
}

private sendEventIfNew(name, value, displayed=false) {
	if (device.currentValue("${name}") != value) {
		sendEvent(name: name, value: value, displayed: displayed)
	}
}


private getCheckInIntervalSetting() {
	return safeToInt(settings?.checkInInterval, 14400)
}


// Configuration Parameters
private getConfigParams() {
	[
		group2BasicSetParam,
		motionEnabledParam,
		motionSensitivityParam,
		luxLevelTriggerParam,
		motionClearedDelayParam,
		luxReportingParam
	]
}

private getGroup2BasicSetParam() {
	return getParam(1, "Association Basic Set Value", 1, 99, [0:"Off", 10:"10%", 20:"20%", 30:"30%", 40:"40%", 50:"50%", 60:"60%", 70:"70%", 80:"80%", 90:"90%", 99:"100%"])
}

private getMotionEnabledParam() {
	return getParam(2, "Motion Enabled", 1, 1, [0:"Disabled", 1:"Enabled"])
}

private getMotionSensitivityParam() {
	return getParam(3, "Motion Sensitivity", 1, 10, motionSensitivityOptions)
}

private getLuxLevelTriggerParam() {
	return getParam(4, "Lux Level Motion Trigger", 2, 0, luxLevelTriggerOptions)
}

private getMotionClearedDelayParam() {
	return getParam(5, "Motion Cleared Delay", 2, 0, motionClearedDelayOptions)
}

private getLuxReportingParam() {
	return getParam(6, "Lux Reporting Frequency", 2, 30, luxReportingOptions)
}


private getParam(num, name, size, defaultVal, options=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}

	return map
}

private setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


// Setting Options

private getMotionSensitivityOptions() {
	def options = [1:"Least Sensitive"]

	(2..9).each {
		options["${it}"] = "${it}"
	}
	options["10"] = "Most Sensitive"
	return options
}

private getLuxLevelTriggerOptions() {
	def options = [
		0:"Set Manually by Lux Knob",
		1:"Ignore Lux and Always Report Motion",
		10:"10 lux"
	]
	(1..36).each {
		options["${it * 25}"] = "${it * 25} lux"
	}
	return options
}

private getMotionClearedDelayOptions() {
	def options = [0:"Set Manually on Knob"]

	(5..30).each {
		options["${it}"] = "${it} Seconds"
	}

	options["45"] = "45 Seconds"
	options["60"] = "1 Minute"
	options["75"] = "1 Minute 15 Seconds"
	options["90"] = "1 Minute 30 Seconds"
	options["105"] = "1 Minute 45 Seconds"
	options["120"] = "2 Minutes"
	options["150"] = "2 Minutes 30 Seconds"

	[180,240,300,360,420,480,540,600,660,720].each {
		options["${it}"] = "${it / 60} Minutes"
	}
	return options
}

private getLuxReportingOptions() {
	def options = [1:"1 Minute"]

	(2..15).each {
		options["${it}"] = "${it} Minutes"
	}

	options["30"] = "30 Minutes"
	options["45"] = "45 Minutes"
	options["60"] = "1 Hour"

	(2..23).each {
		options["${it * 60}"] = "${it} Hours"
	}

	options["1440"] = "1 Day"

	return options
}

private getCheckInIntervalOptions() {
	[
		600: "10 Minutes",
		1800: "30 Minutes",
		3600: "1 Hour",
		7200: "2 Hours",
		14400: "4 Hours",
		28800: "8 Hours",
		43200: "12 Hours",
		86400: "1 Day"
	]
}


private getLightSensorType() { return 3 }


private getFirmwareVersion() {
	return safeToDec(device?.currentValue("firmwareVersion"))
}

private convertIntListToHexList(intList) {
	def hexList = []
	intList?.each {
		hexList.add(Integer.toHexString(it).padLeft(2, "0").toUpperCase())
	}
	return hexList
}

private convertHexListToIntList(String[] hexList) {
	def intList = []

	hexList?.each {
		try {
			it = it.trim()
			intList.add(Integer.parseInt(it, 16))
		}
		catch (e) { }
	}
	return intList
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

private logDebug(msg) {
	if (settings?.debugOutput	!= false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}