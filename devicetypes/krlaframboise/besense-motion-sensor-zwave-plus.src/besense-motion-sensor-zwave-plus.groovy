/**
 *  BeSense Motion Sensor ZWave Plus v1.0.3
 *  (Model: IX30/IX32)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0.3 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.0.2 (07/30/2018)
*    	- Added support for new mobile app.
 *
 *    1.0.1 (07/04/2018)
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
		name: "BeSense Motion Sensor ZWave Plus", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid: "generic-motion-5"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		attribute "pendingChanges", "string"
		attribute "configStatus", "string"
		attribute "firmwareVersion", "string"
		
		fingerprint mfr:"0214", prod:"0003", model:"0002", deviceJoinName: "BeSense Motion Sensor ZWave Plus"
		
		// fingerprint mfr:"0214", prod:"0003", model:"0002", deviceJoinName: "BeSense PIR Wall Sensor" // IX30
		
		// fingerprint mfr:"0214", prod:"0003", model:"0002", deviceJoinName: "BeSense 360 Ceiling Sensor" // IX32
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'MOTION', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'NO MOTION', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
			tileAttribute ("device.tamper", key: "SECONDARY_CONTROL") {
				attributeState("clear", label:'')
				attributeState("detected", label:'TAMPERING')
			}
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'battery ${currentValue}%', unit:"%"
		}
		
		valueTile("pendingChanges", "device.pendingChanges", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pendingChanges", label:'${currentValue}'
		}
		
		valueTile("configStatus", "device.configStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configStatus", label:'${currentValue}'
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration: "flat", width: 2, height: 2){
			state "firmwareVersion", label:'firmware ${currentValue}'
		}
		
		main(["motion"])
		details(["motion", "battery", "configStatus", "pendingChanges", "refresh", "firmwareVersion"])
	}
	
	simulator { }
	
	preferences {
		getOptionsInput(basicSetLevelParam)
		getOptionsInput(turnOffLightTimeParam)
		getOptionsInput(alarmEliminationTimeParam)
		getOptionsInput(ledEnabledParam)
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}
}

private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.defaultValue}",
		options: param.options
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		refreshPendingChanges()
		
		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."		
	}		
}


def configure() {
	logTrace "configure()"
	def cmds = []	
	
	if (!device.currentValue("motion")) {		
		logTrace "Waiting 2 second because this is the first time being configured"
		cmds << "delay 2000"		
		cmds << sensorBinaryGetCmd(12)
	}
	
	if (!device.currentValue("tamper")) {
		cmds << "delay 750"
		cmds << sensorBinaryGetCmd(8)		
	}
	
	if (device.currentValue("firmwareVersion") == null) {
		cmds << "delay 750"
		cmds << versionGetCmd()
	}
	
	if (device.currentValue("battery") == null) {
		cmds << "delay 750"
		cmds << batteryGetCmd()
	}
	
	if (state.wakeUpInterval != expectedWakeUpInterval) {
		cmds << "delay 750"
		cmds << wakeUpIntervalSetCmd(expectedWakeUpInterval)
		cmds << "delay 500"
		cmds << wakeUpIntervalGetCmd()
	}
		
	if (!cmds) {
		configParams.each { param ->
			def storedVal = getParamStoredValue(param.num)
			if ("${storedVal}" != "${param.value}") {
				logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
				cmds << configSetCmd(param)
				cmds << configGetCmd(param)
			}		
		}
		return cmds ? delayBetween(cmds, 500) : []
	}
	else {
		return cmds
	}
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}


def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	if (!state.pendingRefresh) {	
		state.pendingRefresh = true
	}
	else {
		configParams.each {
			if (settings && settings["configParam${it.num}"] != null) {
				setParamStoredValue(it.num, null)
			}
		}	
	}
	refreshPendingChanges()
	return []
}

private logForceWakeupMessage(msg) {
	log.warn "${msg}  You can force the device to wake up immediately by pressing the tamper button."
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
		
		sendLastCheckInEvent()	
	}
	catch (e) {
		log.error "$e"
	}
	return result
}

private sendLastCheckInEvent() {
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
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


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up"
	
	def cmds = []	
	if (pendingChanges > 0) {
		updateConfigStatus("Activating Changes")
		cmds += configure()
	}
	else if ( state.pendingRefresh) {
		updateConfigStatus("synced")
	}
	
	state.pendingRefresh = false		
	
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()	
	return response(cmds)
}

private updateConfigStatus(status) {
	sendEvent(name: "configStatus", value: status, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	
	logDebug "Battery ${val}%"
	sendEvent(getEventMap("battery", val, "%"))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"

	logDebug "Firmware Version: ${version}"
	
	sendEvent(name: "firmwareVersion", value: "${version}", displayed: false)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {	
	logTrace "WakeUpIntervalReport $cmd"
	state.wakeUpInterval = cmd.seconds
	
	sendEvent(name: "checkInterval", value: ((cmd.seconds * 2) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	runIn(3, refreshPendingChanges)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}	
	runIn(3, refreshPendingChanges)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 7) {
		// Ignoring Tamper and Motion events because they're being handled from the Sensor Binary Reports.		
	}
	else {
		logDebug "Unknown Notification Type: ${cmd}"
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	
	switch (cmd.sensorType) {
		case 8:
			sendTamperEvent(cmd.sensorValue ? "detected" : "clear")
			break
		
		case 12:
			sendMotionEvent(cmd.sensorValue ? "active" : "inactive")
			break
			
		default:
			logDebug "Unknown Sensor Type: $cmd"
	}
	return []
}

private sendTamperEvent(value) {	
	logDebug "tamper ${value}"
	sendEvent(getEventMap("tamper", value))
}

private sendMotionEvent(value) {
	logDebug "motion ${value}"
	sendEvent(getEventMap("motion", value))
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Ignored Command: $cmd"
	return []
}


private getEventMap(name, value, unit=null) {		
	def eventMap = [
		name: name,
		value: value,
		displayed: true,
		isStateChange: true,
		descriptionText: (desc ?: "${device.displayName} ${name} is ${value}")
	]
	
	if (unit) {
		eventMap.unit = unit
	}
	return eventMap
}


def refreshPendingChanges() {
	def changes = pendingChanges
		
	sendEvent(name: "pendingChanges", value: "${changes} pending change${changes == 1 ? '' : 's'}", displayed: false)
	
	updateConfigStatus((changes || state.pendingRefresh) ? "press tamper switch to activate changes" : "synced")
}


private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpIntervalSetCmd(seconds) {	
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private batteryGetCmd() {	
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorBinaryGetCmd(sensorType) {
	return secureCmd(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: sensorType))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}


private getCommandClassVersions() {
	[
		0x30: 2,  // SensorBinary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1   // Version (2)
	]
}


private getPendingChanges() {
	return (configParams.count { it.value != getParamStoredValue(it.num) }) + ((state.wakeUpInterval != expectedWakeUpInterval) ? 1 : 0)
}

private getExpectedWakeUpInterval() {
	return (24 * 60 * 60) // 24 Hours
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
		basicSetLevelParam,
		turnOffLightTimeParam,
		alarmEliminationTimeParam,
		ledEnabledParam		
	].findAll { 
		!it.firmware || it.firmware <= firmwareVersion 
	}
}

private getBasicSetLevelParam() {
	return getParam(1, "Basic Set Level", 1, -1, basicSetLevelOptions)
}

private getTurnOffLightTimeParam() {
	return getParam(2, "Turn Off Light Time", 1, 4, timeOptions)
}

private getAlarmEliminationTimeParam() {
	return getParam(3, "Alarm Elimination Time", 1, 4, timeOptions)
}

private getLedEnabledParam() {
	return getParam(5, "PIR LED Enabled (FIRMWARE >= 18.0)", 1, 1, enabledDisabledOptions, firmwareV2)
}

private getParam(num, name, size, defaultVal, options, firmware=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, defaultValue: defaultVal, value: val, firmware: firmware]
	
	
	map.options = options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
	
	return map
}


// Options

private getEnabledDisabledOptions() {
	[
		"0": "Disabled",
		"1": "Enabled"
	]
}

private getBasicSetLevelOptions() {
	def options = [
		"-1": "ON (0xFF)"
	]
	
	(1..10).each {
		options["${it * 10}"] = "${it * 10}"
	}
	return options
}

private getTimeOptions() {
	def options = [:]
	(1..24).each {
		options["${it}"] = "${it * 5} Seconds"
	}
	return options
}


private getFirmwareVersion() {
	return safeToDec(device.currentValue("firmwareVersion"), 0.0)
}
private getFirmwareV1() { return 17.6 }
private getFirmwareV2() { return 18.0 }


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
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