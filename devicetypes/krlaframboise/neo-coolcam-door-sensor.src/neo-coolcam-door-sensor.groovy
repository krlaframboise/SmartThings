/**
 *  Neo Coolcam Door Sensor v1.0.1
 *  (Model: NAS-DS02ZU / NAS-DS02ZE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-neo-coolcam-door-window-sensor/145827
 *
 *  Changelog:
 *
 *    1.0.1 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.0 (12/10/2018)
 *      - Initial Release
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
	definition (
		name: "Neo Coolcam Door Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid: "generic-contact"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		attribute "syncStatus", "string"
		
		fingerprint mfr: "0258", prod: "0003", model: "0082", deviceJoinName: "NEO Coolcam Door Sensor"  //US Version
		fingerprint mfr: "0258", prod: "0003", model: "1082", deviceJoinName: "NEO Coolcam Door Sensor" //EU Version
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", label:'closed', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
				attributeState "open", label:'open', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
			}
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}
		
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}		
		
		main("contact")
		details(["contact", "battery", "refresh", "syncStatus"])
	}
	
	simulator { }
	
	preferences {
		getParamInput(basicSetOffDelayParam)
		getParamInput(basicSetLevelParam)
						
		input "wakeUpInterval", "enum",
			title: "Wake Up Interval:",
			required: false,
			defaultValue: "${wakeUpIntervalSetting}",
			options:  setDefaultOption(wakeUpIntervalOptions, wakeUpIntervalSetting)
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
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

private getWakeUpIntervalSetting() {
	return safeToInt(settings?.wakeUpInterval, 14400)
}


def installed() {
	state.refreshConfig = true
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		refreshSyncStatus()
		
		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."	
	}		
}


def configure() {
	logTrace "configure()"
	
	runIn(8, executeConfigure)	
}

def executeConfigure() {
	def cmds = [
		wakeUpIntervalGetCmd(),
		sensorBinaryGetCmd(),
		batteryGetCmd()
	]
	
	cmds += getConfigCmds()
	
	sendCommands(delayBetween(cmds, 500))
}

private getConfigCmds() {
	def cmds = []
	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if (state.refreshConfig) {
			cmds << configGetCmd(param)
		}
		else if ("${storedVal}" != "${param.value}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}
	state.refreshConfig = false
	return cmds
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, 100)
	return []
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}


// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.lastBattery = null
	if (!state.refreshSensors) {	
		state.refreshSensors = true
	}
	else {
		state.refreshConfig = true		
	}
	refreshSyncStatus()
	return []
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressing the z-button."
}


def parse(String description) {
	def result = []
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
		
		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	}
	catch (e) {
		log.error "$e"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd) {
	state.wakeUpInterval = cmd.seconds

	logDebug "Wake Up Interval = ${cmd.seconds} Seconds"
	
	updateSyncingStatus()
	runIn(4, refreshSyncStatus)
	
	// Set the Health Check interval so that it reports offline 5 minutes after it's missed 3 checkins.
	def val = ((cmd.seconds * 3) + (5 * 60))
	
	def eventMap = getEventMap("checkInterval", val, false)

	eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
	
	sendEvent(eventMap)
	
	return [ ]
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	logDebug "Device Woke Up"
	
	def cmds = []	
	if (state.refreshConfig || pendingChanges > 0) {
		cmds += getConfigCmds()
	}
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
		
	if (state.refreshSensors) {
		cmds << sensorBinaryGetCmd()
		state.refreshSensors = false
	}
	
	if (wakeUpIntervalSetting != state.wakeUpInterval) {
		logDebug "Changing Wake Up Interval to ${wakeUpIntervalSetting} Seconds"
		cmds << wakeUpIntervalSetCmd(wakeUpIntervalSetting)
		cmds << wakeUpIntervalGetCmd()
	}
		
	if (cmds) {
		cmds = delayBetween(cmds, 1000)
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()	
	return response(cmds)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	state.lastBattery = new Date().time
	
	logDebug "Battery ${val}%"
	sendEvent(getEventMap("battery", val, null, null, "%"))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	updateSyncingStatus()
	runIn(4, refreshSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = hexBytesToInt(cmd.configurationValue,cmd.size)
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}		
	return []
}

private updateSyncingStatus() {
	if (device.currentValue("syncStatus") != "Syncing...") {
		sendEvent(getEventMap("syncStatus", "Syncing...", false))
	}
}

def refreshSyncStatus() {
	def changes = pendingChanges	
	sendEvent(name: "syncStatus", value: (changes ?  "${changes} Pending Changes" : "Synced"), displayed: false)
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
		
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	
	sendEvent(getEventMap("contact", cmd.sensorValue ? "open" : "closed", true))
	
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Ignored Command: $cmd"
	return []
}


private getEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def isStateChange = (device.currentValue(name) != value)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange,
		descriptionText: desc ?: "${device.displayName} ${name} is ${value}"
	]
	
	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}		
	if (displayed) {
		logDebug "${eventMap.descriptionText}"
	}
	return eventMap
}


private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV1.wakeUpNoMoreInformation().format()
}

private wakeUpIntervalSetCmd(value) {		
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:value, nodeid:zwaveHubNodeId).format()
}

private wakeUpIntervalGetCmd() {
	return zwave.wakeUpV1.wakeUpIntervalGet().format()
}

private batteryGetCmd() {	
	return zwave.batteryV1.batteryGet().format()
}

private sensorBinaryGetCmd() {
	return zwave.sensorBinaryV2.sensorBinaryGet().format()
}

private configGetCmd(param) {
	return zwave.configurationV1.configurationGet(parameterNumber: param.num).format()
}

private configSetCmd(param) {
	return zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: intToHexBytes(param.value, param.size)).format()
}


private getCommandClassVersions() {
	[
		0x30: 2,	// SensorBinary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 1,  // WakeUp (2)
		0x85: 2,  // Association
		0x86: 1,	// Version
	]
}


private canReportBattery() {
	return state.refreshSensors || (!isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000)))
}

private getPendingChanges() {	
	return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" } + (wakeUpIntervalSetting != state.wakeUpInterval ? 1 : 0)
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


// Configuration Parameters
private getConfigParams() {
	[
		basicSetOffDelayParam,
		basicSetLevelParam
	]
}

private getBasicSetOffDelayParam() {
	return getParam(1, "Basic Set Off Delay", 2, 0, basicSetOffDelayOptions)
}

private getBasicSetLevelParam() {
	return getParam(2, "Basic Set Level Param", 1, 255, basicSetLevelOptions)
}

private getParam(num, name, size, defaultVal, options=null, range=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}
	if (range) map.range = range
	
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

private getWakeUpIntervalOptions() {
	def options = [:]
	
	[5,10,15,20,25,30,45].each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	options["${60 * 60}"] = "1 Hour"
	[2,3,4,5,6,9,12,15,18].each {
		options["${it * 60 * 60}"] = "${it} Hour"
	}
	
	options["${60 * 60 * 24}"] = "1 Day"
	[2,3,4,5,6].each {
		options["${it * 60 * 60 * 24}"] = "${it} Day"
	}
	
	options["${60 * 60 * 24 * 7}"] = "1 Week"
	[2,3,4].each {
	options["${it * 60 * 60 * 24 * 7}"] = "${it} Week"
	}	

	return options
}


private getBasicSetOffDelayOptions() {
	def options = ["0":"Send Off Immediately"]
	
	[9,10,15,20,30,45].each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["60"] = "1 Minute"	
	options["90"] = "1 Minute 30 Seconds"	
	options["120"] = "2 Minutes"
	options["150"] = "2 Minutes 30 Seconds"	
	
	(3..10).each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	[15,20,25,30,45].each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	options["${60 * 60}"] = "1 Hour"
	[2,3,4,5,6,9,12,15,18].each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}
	
	return options
}

private getBasicSetLevelOptions() {
	def options = ["0":"Off"]
	
	(1..19).each {
		options["${it * 5}"] = "${it * 5}%"
	}
	
	options["99"] = "99%"
	options["255"] = "On"
	
	return options
}


private hexBytesToInt(val, size) {
	if (size == 2) {
		return val[1] + (val[0] * 0x100)
	}
	else {
		return val[0]
	}
}

private intToHexBytes(val, size) {
	if (size == 2) {
		if (val > 32767) val = (val - 65536)
		return [(byte) ((val >> 8) & 0xff),(byte) (val & 0xff)]
	}
	else {
		if (val > 127) val = (val - 256)
		return [val]
	}
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}