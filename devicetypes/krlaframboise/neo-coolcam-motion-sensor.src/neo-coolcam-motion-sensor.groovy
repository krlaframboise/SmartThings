/**
 *  Neo Coolcam Motion Sensor v1.0
 *  (Model: NAS-PD01ZU-T / NAS-PD01ZE-T)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-neo-coolcam-motion-sensor-nas-pd01zu-t/143096?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.0 (11/21/2018)
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
		name: "Neo Coolcam Motion Sensor", 
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
		attribute "syncStatus", "string"
		
		fingerprint mfr:"0258", prod:"0003", model:"008D", deviceJoinName: "Neo Coolcam Motion Sensor" //US Version		
		fingerprint mfr: "0258", prod: "0003", model: "108D", deviceJoinName: "NEO Coolcam Motion Sensor" //EU Version
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
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}
		
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}		
		
		main(["motion", "temperature", "illuminance"])
		details(["motion", "temperature", "illuminance", "battery", "refresh", "syncStatus"])
	}
	
	simulator { }
	
	preferences {
		getParamInput(motionEnabledParam)
		getParamInput(motionSensitivityParam)
		getParamInput(motionClearIntervalParam)
		getParamInput(motionRetriggerIntervalParam)
		getParamInput(suppressDuplicateMotionEventsParam)
		getParamInput(ledEnabledParam)
		getParamInput(temperatureReportingThresholdParam)
		getParamInput(luxReportingIntervalParam)
		getParamInput(luxReportingThresholdParam)		
		getParamInput(ambientLightIntensityCalibrationParam)
		getParamInput(basicSetLevelParam)
		getParamInput(basicSetMotionLuxThresholdParam)
		getParamInput(basicSetMotionLuxEnabledParam)
				
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
		cmds += [
			sensorBinaryGetCmd()
			// ,
			// sensorMultilevelGetCmd(tempSensorType),
			// sensorMultilevelGetCmd(lightSensorType)
		]
		state.refreshSensors = false
	}
	
	if (wakeUpIntervalSetting != state.wakeUpInterval) {
		logDebug "Changing Wake Up Interval to ${wakeUpIntervalSetting} Seconds"
		cmds << wakeUpIntervalSetCmd(wakeUpIntervalSetting)
		cmds << wakeUpIntervalGetCmd()
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
	
	sendEvent(getEventMap("motion", cmd.sensorValue ? "active" : "inactive", true))
	
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

private wakeUpIntervalSetCmd(value) {		
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:value, nodeid:zwaveHubNodeId))
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
	def scale = (sensorType == tempSensorType ? 0 : 1)
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
		0x31: 5,	// SensorMultilevel (7)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 1,  // WakeUp
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


// Sensor Types
private getTempSensorType() { return 1 }
private getLightSensorType() { return 3 }


// Configuration Parameters
private getConfigParams() {
	[
		motionSensitivityParam,
		motionClearIntervalParam,
		basicSetLevelParam,
		motionEnabledParam,
		basicSetMotionLuxThresholdParam,
		motionRetriggerIntervalParam,
		luxReportingIntervalParam,
		basicSetMotionLuxEnabledParam,
		luxReportingThresholdParam,
		temperatureReportingThresholdParam,
		ledEnabledParam,
		suppressDuplicateMotionEventsParam,
		ambientLightIntensityCalibrationParam
	]
}

private getMotionSensitivityParam() {
	return getParam(1, "Motion Sensitivity", 1, 20, motionSensitivityOptions)
}

private getMotionClearIntervalParam() {
	return getParam(2, "Motion Clear Interval", 2, 30, motionClearIntervalOptions)
}

private getBasicSetLevelParam() {
	return getParam(3, "Basic Set Level", 1, 255, basicSetLevelOptions) 
}

private getMotionEnabledParam() {
	return getParam(4, "Motion Detection", 1, 255,["0":"Disabled", "255":"Enabled"])
}

private getBasicSetMotionLuxThresholdParam() {
	return getParam(5, "Basic Set Motion Light Threshold", 2, 100, luxLevelOptions)
}

private getMotionRetriggerIntervalParam() {
	return getParam(6, "Motion Retrigger Interval", 1, 8, motionRetriggerIntervalOptions)
}

private getLuxReportingIntervalParam() {
	return getParam(7, "Light Reporting Interval", 2, 180, luxReportingIntervalOptions)
}

private getBasicSetMotionLuxEnabledParam() {
	return getParam(8, "Basic Set Motion Light Level Enabled", 1, 0, enabledDisabledOptions)
}

private getLuxReportingThresholdParam() {
	return getParam(9, "Light Reporting Threshold", 1, 100, luxThresholdOptions)
}

private getTemperatureReportingThresholdParam() {
	return getParam(10, "Temperature Reporting Threshold", 1, 5, temperatureReportingThresholdOptions)
}

private getLedEnabledParam() {
	return getParam(11, "LED Enabled", 1, 1, enabledDisabledOptions)
}

private getSuppressDuplicateMotionEventsParam() {
	return getParam(12, "Suppress Duplicate Motion Events", 1, 1, enabledDisabledOptions)
}

private getAmbientLightIntensityCalibrationParam() {
	return getParam(99, "Ambient Light Intensity Calibration", 2, 1000, ambientLightIntensityCalibrationOptions)
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

private getEnabledDisabledOptions() {
	 return [
		"0":"Disabled", 
		"1":"Enabled"
	]
}

private getMotionSensitivityOptions() {
	def options = ["8":"Most Sensitive"]
	
	(2..24).each {
		options["${it * 10}"] = "${it}"		
	}

	options["255"] = "Least Sensitive"
	return options
}

private getLuxThresholdOptions() {
	def options = ["0":"Disabled"]
	
	(1..5).each {
		options["${it}"] = "${it}"
	}
	
	[10,15,20,25,50,75,100].each {
		options["${it}"] = "${it}"
	}
	return options
}

private getLuxLevelOptions() {
	def options = ["0":"Disabled"]
	
	(1..5).each {
		options["${it}"] = "${it}"
	}
	
	[10,15,20,25,50,75,100,150,200,250,300,350,400,450,500,550,600,650,700,750,800,850,900,950,1000].each {
		options["${it}"] = "${it}"
	}
	return options
}

private getMotionClearIntervalOptions() {
	def options = [:]
	
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

private getMotionRetriggerIntervalOptions() {
	def options = ["1":"1 Second"]
	
	[2,3,4,5,6,7,8].each {
		options["${it}"] = "${it} Seconds"
	}
	
	return options
}

private getLuxReportingIntervalOptions() { 
	def options = ["60":"1 Minute"]
	
	options["90"] = "1 Minute 30 Seconds"
	[2,3,4,5,10,15].each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	options["3600"] = "1 Hour"
	[2,3,4,5,6,7,8,9,10].each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}
	
	return options
}

private getAmbientLightIntensityCalibrationOptions() {
	def options = [:]
	
	[1,25,50,100,150,200,250,500,750,1000,1500,2000,2500,5000,7500,10000,15000,20000,25000,30000,35000,40000,45000,50000,55000,60000,65000].each {
		options["${it}"] = "${it}"
	}
		
	return options
}

private getTemperatureReportingThresholdOptions() {
	def options = [:]
	
	(1..127).each {
		options["${it}"] = "${it * 0.1}°C / ${(((it * 0.1) * 9) / 5)}°F"
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