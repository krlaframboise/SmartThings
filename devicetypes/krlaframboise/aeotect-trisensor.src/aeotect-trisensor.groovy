/**
 *  Aeotec TriSensor v1.0
 *  (Model: ZWA005-A)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-aeotec-trisensor/140556?u=krlaframboise    
 *
 *  Changelog:
 *
 *    1.0 (10/27/2018)
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
		name: "Aeotect TriSensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid: "generic-motion-4"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		attribute "pendingChanges", "string"
		
		fingerprint mfr:"0371", prod:"0102", model:"0005", deviceJoinName: "Aeotec TriSensor"
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'MOTION', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'NO MOTION', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} lux'
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% BATTERY', unit:"%"
		}
		
		valueTile("pendingChanges", "device.pendingChanges", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pendingChanges", label:'${currentValue}'
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}		
		
		main(["motion", "temperature", "illuminance"])
		details(["motion", "temperature", "illuminance", "battery", "refresh", "pendingChanges"])
	}
	
	simulator { }
	
	preferences {
		configParams.each {
			if (it.name) {
				if (it.range) {
					getNumberInput(it)
				}
				else {
					getOptionsInput(it)
				}
			}
		}

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
		defaultValue: "${param.value}",
		options: param.options
}

private getNumberInput(param) {
	input "configVal${param.num}", "number",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		range: param.range
}

def installed() {
	initializeCheckInInterval()
	state.refreshConfig = true
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		initializeCheckInInterval()
		refreshPendingChanges()
		
		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."	
	}		
}

private initializeCheckInInterval() {	
	if (!device?.currentValue("checkInterval")) {
		// Have device flagged as offline if it goes more than 8hrs 5min without checking in.
		sendEvent(name: "checkInterval", value: ((4 * 60 * 60 * 2) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}


def configure() {
	logTrace "configure()"
	
	initializeCheckInInterval()
	
	runIn(8, executeConfigure)	
}

def executeConfigure() {
	def cmds = [
		sensorBinaryGetCmd(),
		batteryGetCmd(),
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(lightSensorType)
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
			
			if (param.num == tempOffsetParam.num) {
				cmds << "delay 3000"
				cmds << sensorMultilevelGetCmd(tempSensorType)
			}
			else if (param.num == lightOffsetParam.num) {
				cmds << "delay 3000"
				cmds << sensorMultilevelGetCmd(lightSensorType)
			}
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
	refreshPendingChanges()
	return []
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by holding the z-button for 2 seconds."
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
		cmds += [
			sensorBinaryGetCmd(),
			sensorMultilevelGetCmd(tempSensorType),
			sensorMultilevelGetCmd(lightSensorType)
		]
		state.refreshSensors = false
	}
		
	if (cmds) {
		cmds = delayBetween(cmds, 1000)
		cmds << "delay 3000"
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


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	if (cmd.sensorValue != [255, 255]) { // Bug in beta device
		switch (cmd.sensorType) {
			case tempSensorType:
				def unit = cmd.scale ? "F" : "C"
				def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
				
				sendEvent(getEventMap("temperature", temp, true, null, getTemperatureScale()))
				break		
				
			case lightSensorType:			
				sendEvent(getEventMap("illuminance", cmd.scaledSensorValue, true, null, "lux"))
				break		
			default:
				logDebug "Unknown Sensor Type: ${cmd.sensorType}"
		}
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	runIn(4, refreshPendingChanges)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}		
	return []
}

def refreshPendingChanges() {
	sendEvent(name: "pendingChanges", value: "${pendingChanges} Pending Changes", displayed: false)
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				sendEvent(getEventMap("motion", "inactive"))
				break
			case 8:
				sendEvent(getEventMap("motion", "active"))
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	
	sendEvent(getEventMap("motion", cmd.sensorValue ? "active" : "inactive"))
	
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
	return secureCmd(zwave.wakeUpV1.wakeUpNoMoreInformation())
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV1.wakeUpIntervalGet())
}

private batteryGetCmd() {	
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorBinaryGetCmd() {
	return secureCmd(zwave.sensorBinaryV2.sensorBinaryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType) ? 0 : 1	
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
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
		0x30: 2,	// SensorBinary
		0x31: 5,	// SensorMultilevel
		0x55: 1,  // TransportServices
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 1,  // Configuration
		0x71: 3,  // Notification
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 1,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version
		0x8E: 2,	// MultChannelAssociation
		0x98: 1,	// Security
		0x9F: 1		// Security 2
	]
}


private canReportBattery() {
	return state.refreshSensors || (!isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000)))
}

private getPendingChanges() {
	return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


// Sensor Types
private getTempSensorType() { return 1 }
private getLightSensorType() { return 3 }


// Configuration Parameters
private getConfigParams() {
	[
		motionRetriggerTimeParam,
		motionClearTimeParam,
		motionSensitivityParam,
		motionLEDEnabledParam,
		motionLEDColorParam,
		tempScaleParam,
		tempThresholdParam,
		tempIntervalParam,
		tempOffsetParam,
		temperatureLEDColorParam,
		// temperatureAlarmValueParam,
		lightThresholdParam,		
		lightIntervalParam,		
		lightOffsetParam,
		lightLEDColorParam,
		lightCalibratedCoefficientParam,
		batteryLEDColorParam,
		wakeUpLEDColorParam,		
		binarySensorReportEnabledParam,
		basicSetGroupsEnabledParam,
		basicSetGroupValuesParam
	]
}


private getMotionRetriggerTimeParam() {
	return getParam(1, "Motion Retrigger Time", 2, 15, timeOptions)
}

private getMotionClearTimeParam() {
	return getParam(2, "Motion Clear Time", 2, 30, timeOptions)
}

private getMotionSensitivityParam() {
	return getParam(3, "Motion Sensitivity", 1, 11, motionSensitivityOptions)
}

private getBinarySensorReportEnabledParam() {
	return getParam(4, "Binary Sensor Report", 1, 0, enabledDisabledOptions)
}

private getBasicSetGroupsEnabledParam() {
	return getParam(5, "Send Basic Set to Associated Nodes", 1, 3, basicSetGroupsEnabledOptions)
}

private getBasicSetGroupValuesParam() {
	return getParam(6, "Send Basic Set Group 2 Values", 1, 0, basicSetValueOptions)
}

// private getTemperatureAlarmValueParam() {
	// return getParam(7, "Temperature Alarm Value", 2, 750, null, "-400..1185")
// }

private getMotionLEDEnabledParam() {
	return getParam(10, "Motion LED", 1, 1, enabledDisabledOptions)
}

private getMotionLEDColorParam() {
	return getParam(11, "Motion LED Color", 1, 2, ledColorOptions)
}

private getTemperatureLEDColorParam() {
	return getParam(12, "Temperature LED Color", 1, 0, ledColorOptions)
}

private getLightLEDColorParam() {
	return getParam(13, "Light LED Color", 1, 0, ledColorOptions)
}

private getBatteryLEDColorParam() {
	return getParam(14, "Battery LED Color", 1, 0, ledColorOptions)
}

private getWakeUpLEDColorParam() {
	return getParam(15, "Wake Up LED Color", 1, 0, ledColorOptions)
}

private getTempScaleParam() {
	def defaultScale = 1 //"${getTemperatureScale()}" == "F" ? 1 : 0
	return getParam(20, "Temperature Scale", 1, defaultScale, ["0":"C", "1":"F"])
}

private getTempThresholdParam() {
	return getParam(21, "Temperature Reporting Threshold", 2, 20, tempThresholdOptions)
}

private getLightThresholdParam() {
	return getParam(22, "Light Reporting Threshold", 2, 100, lightThresholdOptions)
}

private getTempIntervalParam() {
	return getParam(23, "Temperature Reporting Interval", 2, 3600, timeOptions)
}

private getLightIntervalParam() {
	return getParam(24, "Light Reporting Interval", 2, 3600, timeOptions)
}

private getTempOffsetParam() {
	return getParam(30, "Temperature Offset", 2, 0, tempOffsetOptions)
}

private getLightOffsetParam() {
	return getParam(31, "Light Offset", 2, 0, lightOffsetOptions)
}

private getLightCalibratedCoefficientParam() {
	return getParam(100, "Light Calibrated Coefficient", 2, 1024, null, "1..32767")
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
	return options?.collect { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
}


// Setting Options
private getEnabledDisabledOptions() {
	 return [
		"0":"Disabled", 
		"1":"Enabled"
	]
}

private getMotionSensitivityOptions() {
	def options = [
		"0":"Motion Disabled",
		"1":"Least Sensitive"
	]	
	(2..10).each {
		options["${it}"] = "${it}"
	}
	options["11"] = "Most Sensitive"
	return options
}

private getLedColorOptions() {
	[
		"0":"Disable",
		"1":"Red",
		"2":"Green",
		"3":"Blue",
		"4":"Yellow",
		"5":"Pink",
		"6":"Cyan",
		"7":"Purple"
		// "8":"Orange" (Shown in manual, but not supported by device)
	]
}

private getTimeOptions() {
	def options = [:]
	
	options["1"] = "1 Second"
	[2,3,4,5,10,15,20,30,45].each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["60"] = "1 Minute"
	[2,3,4,5,10,15,20,30,45].each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	options["3600"] = "1 Hour"
	[2,3,4,5,6,7,8,9].each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}
	return options
}

private getLightOffsetOptions() {
	def options = [:]
	[-1000,-750,-500,-400,-300,-200,-100,-75,-50,-25,-10,-5].each {
		options["${it}"] = "${it} lux"
	}
	
	options["0"] = "No Offset"
	
	[5,10,25,50,75,100,200,300,400,500,750,1000].each {
		options["${it}"] = "${it} lux"
	}	
	return options
}

private getLightThresholdOptions() {
	def options = [:]
	
	options["0"] = "No Reports"
	
	[1,5,10,25,50,75,100,250,500,750,1000,1500,2000,2500,5000,7500,10000].each {
		options["${it}"] = "${it} lux"
	}
	return options
}

private getBasicSetGroupsEnabledOptions() {
	[
		"0":"Disabled All Groups",
		"1":"Enabled Group 2",
		"2":"Enabled Group 3",
		"3":"Enabled All Groups"
	]
}

private getBasicSetValueOptions() {
	[
		"0":"0xFF Detected / 0x00 Clear",
		"1":"0x00 Detected / 0xFF Clear",
		"2":"0xFF Detected",
		"3":"0x00 Detected",
		"4":"0x00 Clear",
		"5":"0xFF Clear"
	]
}

private getTempThresholdOptions() {	
	def options = [:]
	options["0"] = "No Reports"
	
	for (int i = 1; i <= 100; i += 1) {  // actual range 0..250
		options["${i}"] = "${i / 10}°"
	}
	return options
}

private getTempOffsetOptions() {
	def options = [:]
	for (int i = -100; i < 0; i += 1) {  // actual range -200..200
		options["${i}"] = "${i / 10}°"
	}
	
	options["0"] = "No Offset"
	
	for (int i = 1; i <= 100; i += 1) {
		options["${i}"] = "${i / 10}°"
	}
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
		return [(byte) ((val >> 8) & 0xff),(byte) (val & 0xff)]
	}
	else {
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