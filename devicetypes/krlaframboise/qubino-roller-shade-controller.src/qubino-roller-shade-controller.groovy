/**
 *  Qubino Roller Shade Controller v1.0.1
 *		(ZMNHOD3)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0.1 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.0 (12/15/2018)
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
		name: "Qubino Roller Shade Controller", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch-power-energy"
	) {
		capability "Actuator"
		capability "Window Shade"
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"		
		capability "Energy Meter"
		capability "Configuration"
		capability "Refresh"
		
		attribute "lastCheckIn", "string"
		attribute "secondaryStatus", "string"
		attribute "energyTime", "number"
		attribute "energyDuration", "string"
		attribute "powerLow", "number"
		attribute "powerHigh", "number"
		
		command "reset"
		command "calibrate"
		
		fingerprint mfr:"0159", prod:"0003", model:"0053"
	}

	simulator { }
		
	tiles(scale: 2) {
		multiAttributeTile(name:"shade", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"closing"
				attributeState "closed", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"opening"
				attributeState "opening", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"closing"
				attributeState "closing", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"opening"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
			}
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "energy", label:'${currentValue} kWh', backgroundColor: "#cccccc"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		valueTile("powerHigh", "device.powerHigh", width: 2, height: 1, decoration:"flat") {
			state "powerHigh", label:'High: ${currentValue} W'
		}
		valueTile("powerLow", "device.powerLow", width: 2, height: 1, decoration:"flat") {
			state "powerLow", label:'Low: ${currentValue} W'
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("reset", "device.reset", width: 2, height: 2) {
			state "default", label:'Reset', action: "reset", icon:"st.secondary.refresh-icon"
		}
		standardTile("calibrate", "device.calibrate", width: 2, height: 2) {
			state("calibrate", label:'Calibrate', action:'calibrate', icon:"st.secondary.preferences-tile")
		}
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure", icon:"st.secondary.tools"
		}		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:4, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		
		main("shade")
		details(["shade", "energy", "power", "powerLow", "powerHigh", "refresh", "reset", "calibrate","syncStatus", "configure"])
	}
	
	
	preferences {			
		getOptionsInput(powerReportingThresholdParam)
		getOptionsInput(powerReportingIntervalParam)
		// getOptionsInput(motorMovingTimeParam)
		// getOptionsInput(motorOperationDetectionParam)
		// getOptionsInput(powerConsumptionMaxDelayTimeParam)
		// getOptionsInput(powerConsumptionLimitSwitchDelayTimeParam)
		// getOptionsInput(nextMotorMovementTimeDelayParam)
		// getOptionsInput(temperatureOffsetParam)
		// getOptionsInput(temperatureReportingParam)
		
		getBoolInput("debugOutput", "Enable Debug Logging", true)
	}
}

private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		displayDuringSetup: true,
		options: param.options
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}?", 
		defaultValue: defaultVal, 
		required: false
}


def installed () { 
	sendEvent(name:"energyTime", value:new Date().time, displayed: false)
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		unschedule()
		
		runIn(2, updateSecondaryStatus)
		
		runEvery3Hours(ping)
		
		def cmds = configure()
		return cmds ? response(cmds) : []
	}	
}


def configure() {	
	updateHealthCheckInterval()
	
	runIn(10, updateSyncStatus)
			
	def cmds = []
	def delay = 1000
	
	if (device.currentValue("power") == null) {
		cmds += getRefreshCmds()
		cmds << "delay ${delay}"
	}
	
	if (device.currentValue("energy") == null) {
		cmds += getResetCmds()
		cmds << "delay ${delay}"
	}
	
	if (device.currentValue("switch")) {
		cmds += getConfigureCmds()
	}
	
	return cmds
}

private updateHealthCheckInterval() {
	def minReportingInterval = (3 * 60 * 60)
	
	if (state.minReportingInterval != minReportingInterval) {
		state.minReportingInterval = minReportingInterval
			
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((minReportingInterval * 2) + (5 * 60))
		
		def eventMap = createEventMap("checkInterval", checkInterval, false)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}
}

private getConfigureCmds() {
	def cmds = []	
	
	configParams.each { 
		def storedVal = getParamStoredValue(it.num)
		if (state.resyncAll || "${storedVal}" != "${it.value}") {
			if (state.configured) {
				logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
				cmds << configSetCmd(it, it.value)
			}
			cmds << configGetCmd(it)
		}
	}
	return cmds ? delayBetween(cmds, 2000) : []
}


def ping() {
	logDebug "ping()..."
	return sendCommands([basicGetCmd()])
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, 100)
	return []
}


def on() {
	logDebug "on()..."
	return open()
}

def open() {
	logDebug "open()..."
	return delayBetween([
		switchMultilevelSetCmd(0xFF),
		switchMultilevelGetCmd()
	], 500)
}


def off() {
	logDebug "off()..."
	return close()
}

def close() {
	logDebug "close()..."
	return delayBetween([
		switchMultilevelSetCmd(0x00),
		switchMultilevelGetCmd()
	], 500)
}


def setLevel(level) {
	logDebug "setLevel(${level})..."
	
	if (level > 99) level = 99
	
	return delayBetween([
		switchMultilevelSetCmd(level),
		switchMultilevelGetCmd()
	], 500)
}


def calibrate() {
	logDebug "calibrate()..."
	def cmds = delayBetween([	
		configSetCmd(forceCalibrationParam, 1),
		configGetCmd(forceCalibrationParam)
	], 1000)
	cmds << "delay 120000"
	cmds += delayBetween([
		configSetCmd(forceCalibrationParam, 0),
		configGetCmd(forceCalibrationParam)
	], 1000)
	return cmds
}


def refresh() {
	logDebug "refresh()..."
	return getRefreshCmds()	
}

private getRefreshCmds() {
	return delayBetween([ 
		switchBinaryGetCmd(),
		switchMultilevelGetCmd(),
		// sensorMultilevelGetCmd(sensorTemperature),
		meterGetCmd(meterEnergy),
		meterGetCmd(meterPower)
	], 1000)
}




def reset() {
	logDebug "reset()..."
	
	def cmds = getResetCmds()	
	cmds << "delay 1000"
	cmds += getRefreshCmds()	
	return cmds		
}

private getResetCmds() {
	def power = getAttrVal("power") ?: 0
		
	sendEvent(createEventMap("powerLow", power, false))
	sendEvent(createEventMap("powerHigh", power, false))
	sendEvent(createEventMap("energyTime", new Date().time, false))
	
	return [meterResetCmd()]
}


private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private switchMultilevelGetCmd() {
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelGet())
}

private switchMultilevelSetCmd(level) {
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0))
}

private sensorMultilevelGetCmd(sensor) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensor.sensorType, scale: sensor.scale))
}

private meterGetCmd(meter) {
	return secureCmd(zwave.meterV2.meterGet(scale: meter.scale))
}

private meterResetCmd() {
	return secureCmd(zwave.meterV2.meterReset())
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
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
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x26: 3,	// Switch Multilevel
		0x27: 1,	// Switch All
		0x32: 3,	// Meter v4
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x60: 3,	// Multichannel
		0x70: 2,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x8E: 2	// Multi Channel Association		
	]
}


def parse(String description) {	
	def result = []
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)		
		}
		else {
			log.warn "Unable to parse: $description"
		}
			
		if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
			state.lastCheckInTime = new Date().time
			sendEvent(createEventMap("lastCheckIn", convertToLocalTimeString(new Date()), false))
		}
	}
	catch (e) {
		log.error "${e}"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)	
	
	def result = []
	if (encapsulatedCmd) {
		result += zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	state.configured = true
	
	updateSyncStatus("Syncing...")
	runIn(10, updateSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)				
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}		
	state.resyncAll = false	
	return []
}

def updateSyncStatus(status=null) {	
	if (status == null) {	
		def changes = getPendingChanges()
		if (changes > 0) {
			status = "${changes} Pending Change" + ((changes > 1) ? "s" : "")
		}
		else {
			status = "Synced"
		}
	}	
	if ("${syncStatus}" != "${status}") {
		sendEvent(createEventMap("syncStatus", status, false))		
	}
}

private getSyncStatus() {
	return device.currentValue("syncStatus")
}

private getPendingChanges() {
	return (configParams.count { isConfigParamSynced(it) ? 0 : 1 })
}

private isConfigParamSynced(param) {
	return (param.value == getParamStoredValue(param.num))
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"
	
	def value = (cmd.value == 0xFF) ? "on" : "off"
	
	sendEvent(createEventMap("switch", value))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: ${cmd}"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd){
	logDebug "SwitchMultilevelReport $cmd"
	
	sendEvent(name:"switch", value: cmd.value ? "on" : "off", isStateChange: true)
	
	sendEvent(name:"windowShade", value: cmd.value ? "open" : "closed", isStateChange: true)
	
	if(cmd.value > 99){
		sendEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} is uncalibrated! Please press calibrate!", isStateChange: true)
	}
	else{
		sendEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} moved to ${cmd.value==99 ? 100 : cmd.value}%", isStateChange: true)
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	sendLastCheckinEvent()
	
	def result = []
	if (cmd.sensorType == tempSensorType) {
		result += handleTemperatureEvent(cmd)
	}
	else {
		logDebug "Unknown Sensor Type: ${cmd}"
	} 
	return result
}

private handleTemperatureEvent(cmd) {
	def result = []
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	
	def val = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)	
	if ("$val".endsWith(".")) {
		val = safeToInt("${val}"[0..-2])
	}
			
	result << createEvent(createEventMap("temperature", val, null, "Temperature ${val}°${getTemperatureScale()}", getTemperatureScale()))
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def val = roundTwoPlaces(cmd.scaledMeterValue)
		
	switch (cmd.scale) {
		case meterEnergy.scale:			
			sendEnergyEvents(val)
			break
		case meterPower.scale:
			sendPowerEvents(val)
			break
		default:
			logDebug "Unknown Meter Scale: $cmd"
	}
	
	runIn(2, updateSecondaryStatus)
	return []
}


private sendPowerEvents(value) {
	def highLowNames = [] 
	
	sendEvent(createEventMap("power", value, meterPower.displayed, meterPower.unit))
	
	if (getAttrVal("powerHigh") == null || value > getAttrVal("powerHigh")) {
		highLowNames << "powerHigh"
	}
	
	if (getAttrVal("powerLow") == null || value < getAttrVal("powerLow")) {
		highLowNames << "powerLow"
	}
	
	highLowNames.each {
		sendEvent(createEventMap("$it", value, false, meterPower.unit))
	}	
}


private sendEnergyEvents(value) {
	sendEvent(createEventMap("energy", value, meterEnergy.displayed, meterEnergy.unit))
	
	sendEvent(createEventMap("energyDuration", calculateEnergyDuration(), false))
}

private calculateEnergyDuration() {
	def energyTimeMS = getAttrVal("energyTime")
	if (!energyTimeMS) {
		return "Unknown"
	}
	else {
		def duration = roundTwoPlaces((new Date().time - energyTimeMS) / 60000)
		
		if (duration >= (24 * 60)) {
			return getFormattedDuration(duration, (24 * 60), "Day")
		}
		else if (duration >= 60) {
			return getFormattedDuration(duration, 60, "Hour")
		}
		else {
			return getFormattedDuration(duration, 0, "Minute")
		}
	}
}

private getFormattedDuration(duration, divisor, name) {
	if (divisor) {
		duration = roundTwoPlaces(duration / divisor)
	}	
	return "${duration} ${name}${duration == 1 ? '' : 's'}"
}


def updateSecondaryStatus() {
	def power = getAttrVal("power") ?: 0
	def energy = getAttrVal("energy") ?: 0
	def duration = getAttrVal("energyDuration") ?: ""
				
	if (duration) {
		duration = " - ${duration}"
	}
		
	def status = ""
		
	status =  "${status}${power} ${meterPower.unit} / ${energy} ${meterEnergy.unit}${duration}"
	
	if (getAttrVal("secondaryStatus") != "${status}") {
		sendEvent(createEventMap("secondaryStatus", status, false))
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


private getMeterEnergy() { 
	return [name:"energy", scale:0, unit:"kWh"]	
}

private getMeterPower() { 
	return [name:"power", scale:2, unit:"W"]	
}

private getSensorTemperature() { 
	return [name: "temperature", scale:0, unit:"°"]
}


// Configuration Parameters
private getConfigParams() {
	return [
		// forceCalibrationParam,		
		// switchAllParam,
		powerReportingThresholdParam,
		powerReportingIntervalParam,
		operatingModeParam,
		// slatsTiltFullTurnTimeParam,
		// slatsPositionParam,
		motorMovingTimeParam,		
		motorOperationDetectionParam,
		powerConsumptionMaxDelayTimeParam,
		powerConsumptionLimitSwitchDelayTimeParam,
		nextMotorMovementTimeDelayParam,
		temperatureOffsetParam,
		temperatureReportingParam
	]	
}

// private getSwitchAllParam() {
	// return getParam(10, "Switch All Behavior", 2, 255, [255:"All On/ All Off", 0:"All On", 1:"All Off", 2:"Disabled"])
// }

private getPowerReportingThresholdParam() {
	return getParam(40, "Power Reporting Threshold", 1, 10, powerReportingThresholdOptions) //1-100
}

private getPowerReportingIntervalParam() {
	return getParam(42, "Power Reporting Interval", 2, 300, powerReportingIntervalOptions) //0-32767
}

private getOperatingModeParam() {
	return getParam(71, "Operating Mode", 1, 0, [0:"Shutter/Shade Mode", 1:"Venetian/Slat Mode"]) //0,1
}

private getSlatsTiltFullTurnTimeParam() {
	return getParam(72, "Slats Tilting Full Turn Time", 2, 100, turnTimeOptions) //0-32767
}

private getSlatsPositionParam() {
	return getParam(73, "Slats Position", 1, 1, [0:"Slats return to previous position only after being activated", 1:"Slats return to previous position"]) //0,1
}

private getMotorMovingTimeParam() {
	return getParam(74, "Motor Moving Time", 2, 0, movingTimeOptions) //0-32767
}


// ****  DIFFERENCE  ****
private getMotorOperationDetectionParam() {
	return getParam(76, "Motor Operation Detection", 1, 6, motorOperationDetectionOptions) //5-100 (0.5-10W DC) 0,1-127 (1-127W AC)
}

private getForceCalibrationParam() {
	return getParam(78, "Force Calibration", 1, 0, [0:"Calibration Off", 1:"Calibration On"]) //0,1
}

private getPowerConsumptionMaxDelayTimeParam() {
	return getParam(85, "Power Consumption Max Delay Time", 1, 8, getDelayTimeOptions(3, 50, [0:"Automatic"])) // 3-50
}

// **** MISSING from AC Model ****
private getPowerConsumptionLimitSwitchDelayTimeParam() {
	return getParam(86, "Power Consumption at Limit Switch Delay Time", 1, 8, getDelayTimeOptions(3, 50))
} // 3-50

private getNextMotorMovementTimeDelayParam() {
	return getParam(90, "Next Motor Movement Time Delay", 1, 5, getDelayTimeOptions(1, 30)) //1-30
}

private getTemperatureOffsetParam() {
	return getParam(110, "Temperature Offset", 2, 32536, temperatureOffsetOptions) //1-100, 1001-1100 (negative)
}

private getTemperatureReportingParam() {
	return getParam(120, "Temperature Reporting Threshold", 1, 5, temperatureReportingOptions) // 1-127
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


// Configuration Param Options

private getPowerReportingThresholdOptions() {
	def options = [0:"Disabled"]
	[1,2,3,4,5,10,15,20,25,30,35,40,45,50,60,70,80,90,100].each {
		options["${it}"] = "${it}%"
	}
	return options
}

private getPowerReportingIntervalOptions() {		
	def options = [0:"Disabled"]
	[5,10,15,30,45].each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["60"] = "1 Minute"
	[2,3,4,5,10,15,30,45].each {
		options["${it * 60}"] = "${it} Minutes"
	}	
	
	options["${60 * 60}"] = "1 Hour"
	[2,3,4,5,6,7,8,9].each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}	
	return options
}

private getTurnTimeOptions() {
	def options = [0:"Disabled"]
	[10,20,30,40,50,60,70,80,90].each {
		options["${it}"] = "${it / 100} Seconds"
	}
	
	options["100"] = "1 Second"
	[1.5,2,2.5,3,3.5,4,4.5,5,10,15,20,25,30,35,40,45,50,55].each {
		options["${it * 100}"] = "${it} Seconds"
	}	
	
	options["${100 * 60}"] = "1 Minute"
	[1.5,2,2.5,3,3.5,4,4.5,5].each {
		options["${it * 60 * 100}"] = "${roundOnePlace(it)} Minutes"
	}		
	return options	
}

private getMovingTimeOptions() {
	def options = [0:"Disabled"]
	(1..9).each {
		options["${it}"] = "${it / 10} Seconds"
	}
	
	options["10"] = "1 Second"
	[2,3,4,5,6,7,8,9,10,15,20,25,30,35,40,45,50,55].each {
		options["${it * 10}"] = "${it} Seconds"
	}
	
	options["${10 * 60}"] = "1 Minute"
	[1.5,2,2.5,3,3.5,4,4.5,5].each {
		options["${it * 60 * 10}"] = "${roundOnePlace(it)} Minutes"
	}	
	return options	
}

private getMotorOperationDetectionOptions() {
	def options = [:]	
	[5,6,7,8,9,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100].each {
		options["${it}"] = "${roundOnePlace(it / 10)} W"
	}		
	return options
}

private getDelayTimeOptions(min, max, options=[:]) {
	(1..9).each {
		if (it >= min) {
			options["${it}"] = "${it / 10} Seconds"
		}
	}
	
	options["10"] = "1.0 Second"
	
	[1.5,2,2.5,3,3.5,4,4.5,5].each {
		if ((it * 10) <= max) {
			options["${it * 10}"] = "${roundOnePlace(it)} Seconds"
		}
	}	
	return options
}

private getTemperatureOffsetOptions() {
	def options = [:]
	[10,9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,4.5,4,3.5,3,2.5,2,1.5,1,0.5].each {
		options["${1000 + it * 10}"] = getTempOptionText(it, "-")
	}
	
	options["32536"] = "No Offset"
	
	[0.5,1.0,1.5,2,2.5,3,3.5,4,4.5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5,10].each {
		options["${it * 10}"] = getTempOptionText(it)
	}
	return options	
}

private getTemperatureReportingOptions() {
	def options = [0:"Disabled"]
	[5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100].each {
		options["${it}"] = getTempOptionText(it / 10)
	}
	return options
}

private getTempOptionText(cVal, prefix="") {
	return "${prefix}${roundOnePlace(cVal)}°C / ${prefix}${roundOnePlace((cVal * 9) / 5)}°F"	
}


private createEventMap(name, value, displayed=null, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} - ${name} is ${value}"
	]
	
	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText} ${unit}"
	}	
	return eventMap
}

private getAttrVal(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logDebug "$ex"
		return null
	}
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
}

private roundOnePlace(val) {
	if ("${val}".isNumber()) {
		def dblVal = "${val}".toDouble() * 1.0
		return dblVal.round(1)
	}
	else {
		return val
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
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}