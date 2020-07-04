/**
 *  Neo Coolcam Power Plug v1.2.7
 *  (Models: NAS-WR02ZU, NAS-WR02ZE, NAS-WR01ZE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-neo-coolcam-power-plug/144274?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.2.7 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.2.6 (05/30/2019)
 *      - Added fingerprint for new US model and fixed model check.
 *
 *    1.2.5 (02/25/2019)
 *      - ***WARNING*** because of the paramater number changes mentioned below I had to change the setting names causing all the settings to revert back to their default values.
 *      - Added support for new EU model that has different config params.
 *      	- All param numbers changed so numbers are determined by the "prod" in raw description.
 *      	- Meter Reporting Threshold changed from (0-100%) to (0.01-16A) so still displaying percentage settings, but performing conversion to A for new model before sending to device.
 *      	- Meter Reporting Interval range changed from (1-65535) to (30-32767) so removed options that are not supported by both models.
 *      	- Switch Timer Period maximum value changed from 65535 to 32767 so removed options that are not supported by both models.
 *      	- The options that were removed were just commented out so if you have the original model you can find them near the bottom of the code in the "Setting Options" section and uncomment them.
 *
 *    1.1 (01/30/2019)
 *      - Added security encapsulation because the latest EU version of this device supports it.
 *
 *    1.0 (11/29/2018)
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
		name: "Neo Coolcam Power Plug", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch-power-energy"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"		
		capability "Outlet"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
				
		attribute "lastCheckin", "string"
		attribute "syncStatus", "string"
		attribute "history", "string"
		attribute "current", "number"
		attribute "energyTime", "number"
		attribute "energyDuration", "string"
		
		["power", "voltage", "current"].each {
			attribute "${it}Low", "number"
			attribute "${it}High", "number"
		}
				
		command "reset"

		fingerprint mfr: "0258", prod: "0003", model: "0087", deviceJoinName: "NEO Coolcam Power Plug"
		
		fingerprint mfr: "0258", prod: "0003", model: "1087", deviceJoinName: "NEO Coolcam Power Plug"  //EU
		
		fingerprint mfr: "0258", prod: "0100", model: "1027", deviceJoinName: "NEO Coolcam Power Plug" // New US
		
		fingerprint mfr: "0258", prod: "0200", model: "1027", deviceJoinName: "NEO Coolcam Power Plug" // New EU		
	}

	simulator { }
	
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh"
		}
		standardTile("reset", "device.reset", width: 2, height: 2) {
			state "refresh", label:'Reset', action: "reset"
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "energy", label:'${currentValue} kWh', backgroundColor: "#cccccc"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "voltage", label:'${currentValue} V', backgroundColor: "#cccccc"
		}
		valueTile("current", "device.current", width: 2, height: 2) {
			state "current", label:'${currentValue} A', backgroundColor: "#cccccc"
		}
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		standardTile("sync", "device.configure", width: 2, height: 2) {
			state "default", label: 'Sync', action: "configure"
		}	
		valueTile("history", "device.history", decoration:"flat",width: 4, height: 4) {
			state "history", label:'${currentValue}'
		}
		main "switch"
		details(["switch", "power", "voltage", "current", "energy", "syncStatus", "sync", "history", "refresh", "reset"])
	}
	
	preferences {
		getParamInput(meterReportsEnabledParam)
		getParamInput(meterReportingIntervalParam)
		getParamInput(powerReportingThresholdParam)
		getParamInput(ledEnabledParam)
		getParamInput(physicalButtonEnabledParam)
		getParamInput(rememberSwitchStateParam)
		getParamInput(overloadProtectionParam)
		getParamInput(overloadAlarmParam)		
		getParamInput(switchTimerEnabledParam)
		getParamInput(switchTimerPeriodParam)

		input "debugOutput", "bool", 
			title: "Enable Debug Logging", 
			defaultValue: true, 
			required: false
	}
}

private getParamInput(param) {
	input "${param.pref}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		options: param.options
}


// Meters
private getMeterEnergy() { 
	return getMeterMap("energy", 0, "kWh") 
}

private getMeterPower() { 
	return getMeterMap("power", 2, "W")
}

private getMeterVoltage() { 
	return getMeterMap("voltage", 4, "V") 
}

private getMeterCurrent() { 
	return getMeterMap("current", 5, "A")
}

private getMeterMap(name, scale, unit) {
	return [name:name, scale:scale, unit:unit]
}


def installed() {
	logDebug "installed()..."
	state.refreshConfig = true
	
	state.newModel = isNewModel()
	
	return refresh()
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		logDebug "updated()..."
		
		executeConfigure()
	}
}


def configure() {
	logDebug "configure()..."
	
	state.refreshConfig = true
	
	runIn(2, executeConfigure)	
}

def executeConfigure() {
	def cmds = []
	
	if (state?.newModel == null) {
		state.newModel = isNewModel()
	}
	
	if (!device.currentValue("switch")) {
		cmds += reset()
	}
	
	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		def paramVal = param.value
		
		if (param == overloadAlarmParam && overloadProtectionParam.value <= paramVal) {
			log.warn "Unable to set Current Overload Alarm Threshold to ${paramVal} because it must be lower than Current Overload Protection Threshold"
			paramVal = (overloadProtectionParam.value - 1)
		}
		
		if (state.newModel && param.num == powerReportingThresholdParam.num) {
			paramVal = getNewModelPowerReportingThreshold(paramVal)
		}
		
		if (state.refreshConfig || "${storedVal}" != "${paramVal}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${paramVal}"
			cmds << configSetCmd(param, paramVal)
			cmds << configGetCmd(param)
		}
	}
	
	state.refreshConfig = false
	if (cmds) {
		sendCommands(cmds)
	}
}

private getNewModelPowerReportingThreshold(oldModelVal) {	
	def newModelVal
	if (oldModelVal == 0) {
		// new model doesn't support disabled so using least frequent reporting instead
		newModelVal = 1600
	}
	else {
		// New model supports 1-1600 (x0.01A) instead of 0-100%
		newModelVal = Math.round((oldModelVal / 100) * 1600)
	}
	return newModelVal
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, 500)
	return []
}


void updateHealthCheckInterval() {
	def minInterval = minReportingInterval
	
	if (state.minReportingInterval != minInterval) {
		state.minReportingInterval = minInterval
			
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((minInterval * 3) + (5 * 60))
		
		def eventMap = getEventMap("checkInterval", checkInterval, false)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}
}

private getMinReportingInterval() {
	if (meterReportsEnabledParam.val) {
		return meterReportingIntervalParam.val
	}
	else {
		return (60 * 60 * 12)
	}
}


def ping() {
	logDebug "Pinging device because it has not checked in"
	return [switchBinaryGetCmd()]
}


def on() {
	logDebug "on()..."
	return delayBetween([
		basicSetCmd(0xFF),
		basicGetCmd()
	], 100)
}


def off() {
	logDebug "off()..."
	return delayBetween([
		basicSetCmd(0x00),
		basicGetCmd()
	], 100)
}


def refresh() {
	logDebug "refresh()..."
	
	refreshSyncStatus()
	
	return delayBetween([
		switchBinaryGetCmd(),
		meterGetCmd(meterEnergy),
		meterGetCmd(meterPower),
		meterGetCmd(meterVoltage),
		meterGetCmd(meterCurrent)
	], 500)
}


def reset() {
	logDebug "reset()..."
	["power", "voltage", "current"].each {
		sendEvent(getEventMap("${it}Low", getAttrVal(it), false))
		sendEvent(getEventMap("${it}High", getAttrVal(it), false))
	}
	sendEvent(getEventMap("energyTime", new Date().time, false))
	
	def result = []
	result << meterResetCmd()
	result << "delay 500"
	result += refresh()
	return result
}


private meterGetCmd(meter) {
	return secureCmd(zwave.meterV3.meterGet(scale: meter.scale))
}

private meterResetCmd() {
	return secureCmd(zwave.meterV3.meterReset())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private basicSetCmd(val) {
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: intToHexBytes(value, param.size)))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
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
		0x27: 1,	// All Switch
		0x32: 3,	// Meter v4
		0x55: 1,	// Transport Service
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 1,	// Configuration
		0x71: 3,  // Notification v8
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association
		0x98: 1,	// Security 0
		0x9F: 1		// Security S2
	]
}


def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)		
	}
	else {
		log.warn "Unable to parse: $description"
	}
		
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time
		sendEvent(getEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
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


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	updateSyncingStatus()
	runIn(4, refreshSyncStatus, [overwrite: true])
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = hexBytesToInt(cmd.configurationValue,cmd.size)
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
		
		if (param == meterReportsEnabledParam || param == meterReportingIntervalParam) {
			updateHealthCheckInterval()
		}
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}		
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"
	sendSwitchEvent(cmd.value, "physical")
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: ${cmd}"
	sendSwitchEvent(cmd.value, "digital")
	return []
}

private sendSwitchEvent(value, type) {
	def eventVal = (value == 0xFF) ? "on" : "off"
	def map = getEventMap("switch", eventVal, null, "Switch is ${eventVal}")
	map.type = type
	sendEvent(map)
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logTrace "MeterReport: $cmd"
	def val = roundTwoPlaces(cmd.scaledMeterValue)
	
	if (val >= 0) {
		def meter 
		switch (cmd.scale) {
			case meterEnergy.scale:			
				sendEvent(getEventMap("energyDuration", calculateEnergyDuration(), false))
				meter = meterEnergy
				break
			case meterPower.scale:
				meter = meterPower
				break
			case meterVoltage.scale:
				meter = meterVoltage
				break
			case meterCurrent.scale:
				meter = meterCurrent
				break
			default:
				logDebug "Unknown Meter Scale: $cmd"
		}

		if (meter?.name && getAttrVal("${meter.name}") != val) {
			sendEvent(getEventMap(meter.name, val, null, null, meter.unit))
			
			if (meter.name != meterEnergy.name) {
				sendHighLowEvents(meter, val)
			}
			
			runIn(5, refreshHistory)
		}	
	}
	return []
}


private sendHighLowEvents(meter, val) {
	def highLowNames = [] 
	def highName = "${meter.name}High"
	def lowName = "${meter.name}Low"
	if (!getAttrVal(highName) || val > getAttrVal(highName)) {
		highLowNames << highName
	}
	if (!getAttrVal(lowName) || meter.value < getAttrVal(lowName)) {
		highLowNames << lowName
	}
	
	highLowNames.each {
		sendEvent(getEventMap("$it", val, false, null, meter.unit))
	}	
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


def refreshHistory() {
	def history = ""
	def items = [:]
			
	items["energyDuration"] = "Duration"
	["power", "voltage", "current"].each {
		items["${it}Low"] = "${it.capitalize()} - Low"
		items["${it}High"] = "${it.capitalize()} - High"
	}
	
	items.each { attrName, caption ->
		def attr = device.currentState("${attrName}")
		def val = attr?.value ?: ""
		def unit = attr?.unit ?: ""
		history += "${caption}: ${val} ${unit}\n"
	}
	sendEvent(getEventMap("history", history, false))
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
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

private getPendingChanges() {	
	def total = 0
	configParams.each {
		def val = it.value
		if (state.newModel && it.num == powerReportingThresholdParam.num) {
			val = getNewModelPowerReportingThreshold(val)
		}		
		total += ("${val}" == "${getParamStoredValue(it.num)}") ? 0 : 1
	}
	return total
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


// Configuration Parameters
private getConfigParams() {
	return [
		meterReportsEnabledParam,
		meterReportingIntervalParam,
		overloadProtectionParam,
		overloadAlarmParam,
		ledEnabledParam,
		powerReportingThresholdParam,
		rememberSwitchStateParam,
		switchTimerEnabledParam,
		switchTimerPeriodParam,
		physicalButtonEnabledParam
	]
}

private getMeterReportsEnabledParam() {
	def num = state?.newModel ? 6 : 1
	return getParam(num, "Meter Reports Enabled", 1, 1, enabledDisabledOptions, "meterEnabled")
}

private getMeterReportingIntervalParam() {
	def num = state?.newModel ? 7 : 2
	return getParam(num, "Meter Reporting Interval", 2, 300, meterReportingIntervalOptions, "meterInterval")
}

private getOverloadProtectionParam() {
	def num = state?.newModel ? 8 : 3	
	return getParam(num, "Current Overload Protection Threshold", 1, 13, getOverloadOptions(2, 16), "overloadProtection")
}

private getOverloadAlarmParam() {
	def num = state?.newModel ? 10 : 4	
	return getParam(num, "Current Overload Alarm Threshold", 1, 12, getOverloadOptions(1, 15), "overloadAlarm")
}

private getLedEnabledParam() {
	def num = state?.newModel ? 3 : 5
	return getParam(num, "LED Enabled", 1, 1, enabledDisabledOptions, "ledEnabled")
}

private getPowerReportingThresholdParam() {	
	def num = state?.newModel ? 9 : 6
	def size = state?.newModel ? 2 : 1
	return getParam(num, "Power Reporting Threshold", size, 5, powerReportingThresholdOptions, "meterThreshold")
}

private getRememberSwitchStateParam() {
	def num = state?.newModel ? 1 : 7	
	return getParam(num, "Remember Switch State After Power Failure", 1, 1, enabledDisabledOptions, "rememberState")
}

private getSwitchTimerEnabledParam() {
	def num = state?.newModel ? 4 : 8
	return getParam(num, "Switch Off Timer Enabled", 1, 0, enabledDisabledOptions, "timerEnabled")
}

private getSwitchTimerPeriodParam() {
	def num = state?.newModel ? 5 : 9	
	return getParam(num, "Swith Off Timer Period", 2, 120, switchTimerPeriodOptions, "timerPeriod")
}

private getPhysicalButtonEnabledParam() {
	def num = state?.newModel ? 2 : 10
	return getParam(num, "Physical Button Enabled", 1, 1, enabledDisabledOptions, "btnEnabled")
}


private getParam(num, name, size, defaultVal, options, pref) {
	def val = safeToInt((settings ? settings["${pref}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val, pref:pref]
	map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
	map.options = setDefaultOption(options, defaultVal)
		
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
private getEnabledDisabledOptions() {
	 return [
		"0":"Disabled", 
		"1":"Enabled"
	]
}

private getMeterReportingIntervalOptions() {
	def options = [:]
	
	// if (!state?.newModel) {
		// [5,10,15,20].each {
			// options["${it}"] = "${it} Seconds"
		// }
	// }
	
	[30,45].each {
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
	(2..9).each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}	
	
	// if (state?.newModel) {
		// (10..18).each {
			// options["${it * 60 * 60}"] = "${it} Hours"
		// }	
	// }
	return options
}

private getOverloadOptions(min, max) {
	def options = [:]	
	(1..16).each {
		if (it >= min && it <= max) {
			options["${it}"] = "${it}A"
		}
	}	
	return options
}

private getPowerReportingThresholdOptions() {
	def options = ["0":"Disabled"]	
	[1,2,3,4,5,10,15,20,25,30,35,40,45,50,60,70,80,90,100].each {
		options["${it}"] = "${it}%"
	}	
	return options
}

private getSwitchTimerPeriodOptions() {
	def options = ["1":"1 Minute"]
	
	(2..10).each {
		options["${it}"] = "${it} Minutes"
	}
	
	[15,20,25,30,45].each {
		options["${it}"] = "${it} Minutes"
	}
	
	options["60"] = "1 Hour"
	options["90"] = "1 Hour 30 Minutes"
	options["120"] = "2 Hours"
	options["150"] = "2 Hours 30 Minutes"
	
	(3..12).each {
		options["${it * 60}"] = "${it} Hours"
	}
	
	[15,18,21].each {
		options["${it * 60}"] = "${it} Hours"
	}
	
	options["60 * 24"] = "1 Day"
	(2..6).each {
		options["${it * 60 * 24}"] = "${it} Days"
	}
	
	options["60 * 24 * 7"] = "1 Week"
	(2..3).each {
		options["${it * 60 * 24 * 7}"] = "${it} Weeks"
	}
	
	// if (!state?.newModel) {
		// (4..6).each {
			// options["${it * 60 * 24 * 7}"] = "${it} Weeks"
		// }
	// }
	
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


private getEventMap(name, value, displayed=null, desc=null, unit=null) {	
	desc = desc ?: "${name} is ${value}"
	
	def eventMap = [
		name: name,
		value: value,
		displayed: (displayed == null ? ("${getAttrVal(name)}" != "${value}") : displayed),
		isStateChange: true
	]
	
	if (unit) {
		eventMap.unit = unit
		desc = "${desc} ${unit}"
	}
	
	if (desc && eventMap.displayed) {
		logDebug desc
		eventMap.descriptionText = "${device.displayName} - ${desc}"
	}
	else {
		logTrace "Creating Event: ${eventMap}"
	}
	return eventMap
}

private getAttrVal(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logTrace "$ex"
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

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isNewModel() {
	return "${device.rawDescription}".contains("prod:0200") || "${device.rawDescription}".contains("prod:0100")
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