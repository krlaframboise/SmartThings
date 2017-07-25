/**
 *  Zooz Power Switch v0.0
 *  (Model: ZEN15)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    0.0 (07/25/2017)
 *      - Beta Release
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
		name: "Zooz Power Switch", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
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
		
		attribute "lastCheckin", "string"
		attribute "current", "number"

		fingerprint mfr:"027A", prod:"0101", model:"000D", deviceJoinName: "Zooz Power Switch"		
	}

	simulator { }
	
	preferences {
		configParams?.each {			
			getOptionsInput(it)
		}
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon", defaultState: true
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "val", label:'${currentValue} ${unit}', unit: "kWh", defaultState: true
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "val", label:'${currentValue} ${unit}', unit: "W", defaultState: true
		}
		valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "val", label:'${currentValue} ${unit}', unit: "V", defaultState: true
		}
		valueTile("current", "device.current", width: 2, height: 2) {
			state "val", label:'${currentValue} ${unit}', unit: "A", defaultState: true
		}
		main "switch"
		details(["switch", "overload", "refresh", "energy", "power", "voltage", "current"])
	}
}

private getOptionsInput(param) {
	if (param.prefName) {
		input "${param.prefName}", "enum",
			title: "${param.name}:",
			defaultValue: "${param.val}",
			required: false,
			displayDuringSetup: true,
			options: param.options?.collect { name, val -> name }
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		def cmds = configure()
		return cmds ? response(cmds) : []
	}
}

def configure() {
	def cmds = []
	
	configParams.each { param ->	
		cmds += updateConfigVal(param)
	}

	if (!getAttrValue("switch")) {
		cmds << switchBinaryGetCmd()
	}
	if (!getAttrValue("power")) {
		cmds << meterGetCmd(meterScalePower)
	}
	if (!getAttrValue("energy")) {
		cmds << meterGetCmd(meterScaleEnergy)
	}
	if (!getAttrValue("voltage")) {
		cmds << meterGetCmd(meterScaleVoltage)
	}
	if (!getAttrValue("current")) {
		cmds << meterGetCmd(meterScaleCurrent)
	}
	
	return delayBetweenCmds(cmds)
}

private updateConfigVal(param) {
	def cmds = []	
	if (hasPendingChange(param)) {
		def newVal = getParamIntVal(param)
		logTrace "${param.name}(#${param.num}): changing ${getParamStoredIntVal(param)} to ${newVal}"
		cmds << configSetCmd(param, newVal)
		cmds << configGetCmd(param)
	}	
	return cmds
}

private hasPendingChange(param) {
	return (getParamIntVal(param) != getParamStoredIntVal(param))
}


def on() {
	logTrace "Turning On"
	return delayBetweenCmds([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	])
}

def off() {
	logTrace "Turning Off"
	return delayBetweenCmds([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	])
}

def refresh() {
	logTrace "Refreshing"
	return delayBetweenCmds([
		switchBinaryGetCmd(),
		meterGetCmd(meterScaleEnergy),
		meterGetCmd(meterScalePower),
		meterGetCmd(meterScaleVoltage),
		meterGetCmd(meterScaleCurrent)
	])
}


private meterGetCmd(scale) {
	return secureCmd(zwave.meterV3.meterGet(scale: scale))
}

private meterResetCmd() {
	return secureCmd(zwave.meterV3.meterReset())
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private configSetCmd(param, val) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: val))
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

private delayBetweenCmds(cmds, delay=50) {
	return cmds ? delayBetween(cmds, delay) : []
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
		result << createEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
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

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x27: 1,	// All Switch
		0x2B: 1,	// Scene Activation
		0x2C: 1,	// Scene Actuator Configuration
		0x32: 3,	// Meter v4
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x70: 2,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	def val = cmd.scaledConfigurationValue
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		def name = configParam.options?.find { it.value == val}?.key
		logDebug "${configParam.name}(#${configParam.num}) = ${name != null ? name : val} (${val})"
		state["configVal${cmd.parameterNumber}"] = val
		
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"
	def result = []
	result << createSwitchEvent(cmd.value, false)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: ${cmd}"
	def result = []
	result << createSwitchEvent(cmd.value, true)
	return result
}

private createSwitchEvent(value, physical) {
	def eventVal = (value == 0xFF) ? "on" : "off"
	def map = createEventMap("switch", eventVal, true, "Switch is ${eventVal}")
	map.physical = physical
	return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	// logTrace "MeterReport: $cmd"
	def result = []	
	def name
	def unit 
	def val = cmd.scaledMeterValue
	
	switch (cmd.scale) {
		case meterScaleEnergy:
			name = "energy"
			unit = "kWh"
			break
		case meterScalePower:
			name = "power"
			unit = "W"
			break
		case meterScaleVoltage:
			name = "voltage"
			unit = "V"
			break
		case meterScaleCurrent:
			name = "current"
			unit = "A"
			break
		default:
			logDebug "Unknown Meter Scale: $cmd"
	}
	
	if (name && getAttrValue("$name") != val) {
		result << createEvent(createEventMap(name, val, null, "${name} is ${val} ${unit}", unit))
	}
	return result
}

// Meter Scales
private getMeterScaleEnergy() { return 0 }
private getMeterScalePower() { return 2 }
private getMeterScaleVoltage() { return 4 }
private getMeterScaleCurrent() { return 5 }


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


// Configuration Parameters
private getConfigParams() {
	return [
		overloadProtectionParam,
		powerFailureRecoveryParam,
		onOffNotificationsParam,
		ledIndicatorParam,
		powerValueChangeParam,
		powerPercentageChangeParam,
		powerReportIntervalParam,
		energyReportIntervalParam,
		voltageReportIntervalParam,
		electricityReportIntervalParam
	]
}

private getOverloadProtectionParam() {
	return createConfigParamMap(20, "Overload Protection", 1, null, null, 1) // 1:Enable
}

private getPowerFailureRecoveryParam() {
	return createConfigParamMap(21, "Power Failure Recovery", 1, ["Remember last status${defaultOptionSuffix}":0, "Turn On":1, "Turn Off":2], "powerFailureRecovery")
}

private getOnOffNotificationsParam() {
	return createConfigParamMap(24, "On/Off Notifications", 1, null, null, 2) // 2:Manual switch only
}

private getLedIndicatorParam() {
	return createConfigParamMap(27, "LED Power Consumption Indicator", 1, ["Always Show${defaultOptionSuffix}":0, "Show for 5 seconds when turned on or off":1], "ledIndicator")
}

private getPowerValueChangeParam() {
	return createConfigParamMap(151, "Power Report Value Change", 2, getPowerValueOptions(), "powerValueChange")
}

private getPowerPercentageChangeParam() {
	return createConfigParamMap(152, "Power Report Percentage Change", 1, getPercentageOptions(10, [zeroName: "No Reports"]), "powerPercentageChange")
}

private getPowerReportIntervalParam() {
	return createConfigParamMap(171, "Power Reporting Interval", 4, getIntervalOptions(30, [zeroName:"No Reports"]), "powerReportingInterval")
}

private getEnergyReportIntervalParam() {
	return createConfigParamMap(172, "Energy Reporting Interval", 4, getIntervalOptions(300, [zeroName:"No Reports"]), "energyReportingInterval")	
}

private getVoltageReportIntervalParam() {
	return createConfigParamMap(173, "Voltage Reporting Interval", 4, getIntervalOptions(0, [zeroName:"No Reports"]), "voltageReportingInterval")	
}

private getElectricityReportIntervalParam() {
	return createConfigParamMap(174, "Electrical Current Reporting Interval", 4, getIntervalOptions(0, [zeroName:"No Reports"]), "electricityReportingInterval")	
}

private getParamStoredIntVal(param) {
	return state["configVal${param.num}"]	
}

private getParamIntVal(param) {
	return param.options ? convertOptionSettingToInt(param.options, param.val) : param.val
}

private createConfigParamMap(num, name, size, options, prefName, val=null) {
	if (val == null) {
		val = (settings?."${prefName}" ?: findDefaultOptionName(options))
	}
	return [
		num: num, 
		name: name, 
		size: size, 
		options: options, 
		prefName: prefName,
		val: val
	]
}


// Settings
private getDebugOutputSetting() {
	return settings?.debugOutput != false
}


private getIntervalOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : (9 * 60 * 60)
	
	[0,5,10,15,30,45].each {
		if (withinRange(it, min, max)) {
			if (it == 0 && data?.zeroName != null) {
				options["${data?.zeroName}"] = it
			}
			else {
				options["${it} Second${x == 1 ? '' : 's'}"] = it
			}
		}
	}

	[1,2,3,4,5,10,15,30,45].each {
		def val = (it * 60)
		if (withinRange(val, min, max)) {
			options["${it} Minute${x == 1 ? '' : 's'}"] = val
		}
	}

	[1,2,3,6,9,12,18].each {
		def val = (it * 60 * 60)
		if (withinRange(val, min, max)) {
			options["${it} Hour${x == 1 ? '' : 's'}"] = val
		}
	}	
	
	[1,3,5].each {
		def val = (it * 60 * 60 * 24)
		if (withinRange(val, min, max)) {
			options["${it} Day${x == 1 ? '' : 's'}"] = val
		}
	}
	
	[1,2].each {
		def val = (it * 60 * 60 * 24 * 7)
		if (withinRange(val, min, max)) {
			options["${it} Week${x == 1 ? '' : 's'}"] = val
		}
	}	
	return setDefaultOption(options, defaultVal)
}

private getPowerValueOptions() {
	def options = [:]	
	[0,1,2,3,4,5,10,25,50,75,100,150,200,250,300,400,500,750,1000,1250,1500,1750,2000,2500,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000,12500,15000].each {		
		if (it == 0) {
			options["No Reports"] = it
		}
		else {
			options["${it} Watts"] = it
		}
	}
	return setDefaultOption(options, 50)
}

private getPercentageOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : 100
		
	[0,1,2,3,4,5].each {
		if (withinRange(it, min, max)) {
			if (it == 0 && data?.zeroName != null) {
				options["${data?.zeroName}"] = it
			}
			else {
				options["${it}%"] = it
			}
		}
	}
	
	for (int i = 10; i <= 100; i += 5) {
		if (withinRange(i, min, max)) {
			options["${i}%"] = i
		}
	}
	
	return setDefaultOption(options, defaultVal)
}

private withinRange(val, min, max) {
	return ((min == null || val >= min) && (max == null || val <= max))
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { name, val -> "${settingVal}" == name }?.value, 0)
}

private setDefaultOption(options, defaultVal) {
	def name = options.find { key, val -> val == defaultVal }?.key
	if (name != null) {
		return changeOptionName(options, defaultVal, "${name}${defaultOptionSuffix}")
	}
	else {
		return options
	}	
}

private changeOptionName(options, optionVal, newName) {
	def result = [:]	
	options?.each { name, val ->
		if (val == optionVal) {
			name = "${newName}"
		}
		result["${name}"] = val
	}
	return result
}

private findDefaultOptionName(options) {
	def option = options?.find { name, val ->
		name?.contains("${defaultOptionSuffix}") 
	}
	return option?.key ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def newVal = "${value}"	
	displayed = (displayed == null ? (getAttrValue(name) != newVal) : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true
	]
	if (unit) {
		eventMap.unit = unit
	}
	if (desc) {
		logDebug desc
		eventMap.descriptionText = "${device.displayName} - ${desc}"
	}
	else {
		logTrace "Creating Event: ${eventMap}"
	}
	return eventMap
}

private getAttrValue(attrName) {
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

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}