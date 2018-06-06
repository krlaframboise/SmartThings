/**
 *  Zooz Motion Sensor ZSE18 v1.0.1
 *  (Model: ZSE18)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *    1.0.1 (06/05/2018)
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
		name: "Zooz Motion Sensor ZSE18", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		
		fingerprint mfr:"027A", prod:"0301", model:"0012", deviceJoinName: "Zooz Motion Sensor ZSE18"
	}
	
	simulator { }
	
	preferences {			
		getOptionsInput(motionSensitivityParam)
		getOptionsInput(motionClearedDelayParam)
		getOptionsInput(shockAlarmParam)
		getOptionsInput(ledParam)
		// getOptionsInput(sendBasicSetParam)
		// getOptionsInput(basicSetValueParam)
		// getOptionsInput(sensorBinaryReportsParam)			
		// getOptionsInput(lowBatteryAlarmParam)
			
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {			
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"${resourcesUrl}motion-inactive.png", backgroundColor:"#ffffff"
				attributeState "active", 
					label:'MOTION', 
					icon:"${resourcesUrl}motion-active.png", backgroundColor:"#00a0dc"
			}	
		}

		standardTile("motion", "device.motion", decoration: "flat", width: 2, height: 2) {			
			state "inactive", label:'NO MOTION', icon: "${resourcesUrl}motion-inactive.png"
			state "active", label:'MOTION', icon: "${resourcesUrl}motion-active.png"
		}		
		
		standardTile("acceleration", "device.acceleration", decoration: "flat", width: 2, height: 2) {
			state "inactive", label:'INACTIVE', icon: "${resourcesUrl}acceleration-inactive.png"
			state "active", label:'ACTIVE', icon: "${resourcesUrl}acceleration-active.png"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"${resourcesUrl}refresh.png"
		}
		
		standardTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}%', icon: "${resourcesUrl}battery-default.png"
			state "100", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "1", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
		}
		
		main(["mainTile", "motion", "acceleration"])
		details(["mainTile", "motion", "motionMulti", "acceleration", "battery", "refresh"])
	}
}

private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.defaultValue}",
		options: param.options
}

private getResourcesUrl() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/"
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."		
	}		
}

def configure() {
	logTrace "configure()"
	def cmds = []
	
	if (!state.isConfigured) {
		logTrace "Waiting 2 second because this is the first time being configured"
		cmds << "delay 2000"
	}
	
	if (!device.currentValue("motion")) {
		cmds << sensorBinaryGetCmd(12)
	}
	
	if (!device.currentValue("acceleration")) {
		cmds << sensorBinaryGetCmd(8)		
	}
	
	if (canRequestBattery()) {
		cmds << batteryGetCmd()
	}
	
	configParams.each { 
		cmds += updateConfigVal(it)
	}	
		
	if (!device.currentValue("checkInterval")) {
		// ST sets default wake up interval to 4 hours so make it report offline if it goes 8 hours 5 minutes without checking in.
		def wakeUpIntervalSecs = (4 * 60 * 60)		
		cmds << wakeUpIntervalSetCmd(wakeUpIntervalSecs)
		
		def checkInInterval = ((wakeUpIntervalSecs * 2) + (5 * 60)) 
		sendEvent(name: "checkInterval", value: checkInInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
		
	return cmds ? delayBetween(cmds, 1000) : []
}

private canRequestBattery() {
	return !isDuplicateCommand(state.lastBattery, (60 * 60 * 1000))
}

private updateConfigVal(param) {
	def result = []
	def configVal = state["configVal${param.num}"]
	
	if ("${configVal}" != "${param.value}") {
		logDebug "Changing ${param.name} (#${param.num}) from ${configVal} to ${param.value}"
		result << configSetCmd(param)
		result << configGetCmd(param)
	}		
	return result
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.lastBattery = null
	configParams.each {
		state."configVal${it.num}" = null
	}
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by holding the z-button for 5 seconds."
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
	cmds += configure()
	
	if (cmds) {
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

// Stores the configuration values so that it only updates them when they've changed or a refresh was requested.
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = hexBytesToInt(cmd.configurationValue, cmd.size)
		
		if (val == 0) {
			if (param.num == shockAlarmParam.num) {
				sendAccelerationEvent("inactive")
			}
			else if (param.num == motionSensitivityParam.num) {
				sendMotionEvent("inactive")
			}
		}
	
		logDebug "${param.name} (#${param.num}) = ${val}"	
		state."configVal${param.num}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	
	state.isConfigured = true
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				if (cmd.eventParameter[0] == 3 || cmd.eventParameter[0] == 9) {
					sendAccelerationEvent("inactive")					
				}
				else {
					sendMotionEvent("inactive")
				}		
				break
			case { it == 3 || it == 9}:
				sendAccelerationEvent("active")				
				break
			case 8:
				sendMotionEvent("active")
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	
	switch (cmd.sensorType) {
		case 8:
			sendAccelerationEvent(cmd.sensorValue ? "active" : "inactive")
			break		
		case 12:
			sendMotionEvent(cmd.sensorValue ? "active" : "inactive")
			break			
		default:
			logDebug "Unknown Sensor Type: $cmd"
	}
	return []
}

private sendMotionEvent(value) {
	logDebug "Motion ${value}"
	sendEvent(getEventMap("motion", value))
}

private sendAccelerationEvent(value) {
	logDebug "Acceleration ${value}"
	sendEvent(getEventMap("acceleration", value))
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
		isStateChange: isStateChange
	]
	if (desc) {
		eventMap.descriptionText = desc
	}
	if (unit) {
		eventMap.unit = unit
	}		
	return eventMap
}


private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpIntervalSetCmd(seconds) {	
	log.warn "wakeUpIntervalSetCmd($seconds)"
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
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
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: intToHexBytes(param.value, param.size)))
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
		0x55: 1,  // TransportServices (2)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (5)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd (3)
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
		// 0x6C ???
		// Security S2
		// Supervision
	]
}

// Configuration Parameters
private getConfigParams() {
	[
		motionSensitivityParam,
		sendBasicSetParam,
		basicSetValueParam,
		shockAlarmParam,
		motionClearedDelayParam,
		sensorBinaryReportsParam,
		ledParam,
		lowBatteryAlarmParam
	]	
}

private getMotionSensitivityParam() {
	return getParam(12, "Motion Sensitivity", 1, 8, motionSensitivityOptions)
}

private getSendBasicSetParam() {
	return getParam(14, "Send Basic Set", 1, 0, enabledDisabledOptions)
}

private getBasicSetValueParam() {
	return getParam(15, "Basic Set Value", 1, 0, [
		["0": "Active: 0xFF / Inactive: 0x00"], 
		["1": "Active: 0x00 / Inactive: 0xFF"]
	])
}

private getShockAlarmParam() {
	return getParam(17, "Shock Alarm", 1, 1, enabledDisabledOptions)
}

private getMotionClearedDelayParam() {
	return getParam(18, "Motion Cleared Delay", 2, 30, motionClearedDelayOptions)
}

private getSensorBinaryReportsParam() {
	return getParam(19, "Sensor Binary Reports", 1, 0, enabledDisabledOptions)
}

private getLedParam() {
	return getParam(20, "Motion LED", 1, 1, enabledDisabledOptions)
}

private getLowBatteryAlarmParam() {
	return getParam(32, "Low Battery Level", 1, 10, [["10":"10%"],["25":"25%"],["50":"50%"]])
}

private getParam(num, name, size, defaultVal, options) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, defaultValue: defaultVal, value: val]
	
	map.options = options?.collect {
		it.collect { k, v ->
			if ("${k}" == "${defaultVal}") {
				v = "${v} [DEFAULT]"		
			}
			["$k": "$v"]
		}
	}.flatten()	
	
	return map
}


private getMotionSensitivityOptions() {	
	def options = [
		["0": "Disabled"],
		["1": "1 (Least Sensitive)"]
	]
	
	(2..7).each {
		options << ["${it}": "${it}"]
	}
	
	options << ["8": "8 (Most Sensitive)"]	
	return options
}

private getMotionClearedDelayOptions() {
	[
		["0": "0 Seconds"],
		["1": "1 Seconds"],
		["2": "2 Seconds"],
		["3": "3 Seconds"],
		["4": "4 Seconds"],
		["5": "5 Seconds"],
		["10": "10 Seconds"],		
		["15": "15 Seconds"],
		["30": "30 Seconds"],
		["45":"45 Seconds"],
		["60":"1 Minute"],  
		["120":"2 Minutes"], 
		["180":"3 Minutes"], 
		["240":"4 Minutes"], 
		["300":"5 Minutes"], 
		["420":"7 Minutes"], 
		["600":"10 Minutes"],
		["900":"15 Minutes"],
		["1800":"30 Minutes"],
		["3600":"60 Minutes"]
	]
}

private getEnabledDisabledOptions() {
	[
		["0": "Disabled"],
		["1": "Enabled"]
	]
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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
		return [(byte) ((val >> 8) & 0xff),(byte) (val & 0xff)]
	}
	else {
		return [val]
	}
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
	log.trace "$msg"
}