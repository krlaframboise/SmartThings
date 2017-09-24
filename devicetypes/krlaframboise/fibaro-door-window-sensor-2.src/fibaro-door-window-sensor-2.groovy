/**
 *  Fibaro Door/Window Sensor 2 v1.0
 *  (Model: FGDW-002)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (09/12/2017)
 *      - Initial Release
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
		name: "Fibaro Door/Window Sensor 2", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"

		attribute "lastCheckin", "string"
		attribute "lastUpdate", "string"
		attribute "pendingChanges", "number"
				
		// fingerprint deviceId: "0x0701", inClusters: "0x22,0x31,0x56,0x59,0x5A,0x5E,0x70,0x71,0x72,0x73,0x7A,0x80,0x84,0x85,0x86,0x8E,0x98"
		
		fingerprint mfr:"010F", prod:"0702", model:"2000", deviceJoinName: "Fibaro Door/Window Sensor 2"
	}
	
	simulator { }
	
	preferences {
		configParams.each {
			getOptionsInput(it)
		}
	
		getOptionsInput("checkinInterval", "Checkin Interval", checkinIntervalSetting, wakeUpIntervalOptions)
		
		getOptionsInput("batteryReportingInterval", "Battery Reporting Interval", batteryReportingIntervalSetting,wakeUpIntervalOptions)
		
		getBoolInput("debugOutput", "Enable debug logging?", debugOutputSetting)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#00a0dc"
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#e86d13"
			}
		}
		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#e86d13"
			state "clear", label:"", backgroundColor: "#ffffff"
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
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}% Battery', unit:""
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}

		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'			
			state "0", label:''
			state "-1", label:'Updating Settings'
			state "-2", label:'Pending Refresh'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "contact"
		details(["contact", "temperature", "tampering", "battery", "refresh", "pending", "lastUpdate"])
	}
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}", 
		defaultValue: defaultVal, 
		required: false
}

private getOptionsInput(name, title, defaultVal, options) {
	return getOptionsInput([
		prefName: "${name}",
		name: "${title}",
		val: defaultVal,
		options: options
	])
}

private getOptionsInput(param) {
	if (param?.prefName) {
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
		logTrace "updated()"

		if (checkForPendingChanges()) {
			logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		}
	}	
	return []
}

def configure() {	
	logTrace "configure()"
	def cmds = []
		
	if (checkinIntervalChanged) {
		logTrace "Updating wakeup interval"
		cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		cmds << wakeUpIntervalGetCmd()
	}
					
	configParams.each { param ->	
		cmds += updateConfigVal(param)
	}
	
	if (state.pendingRefresh != false) {
		state.pendingRefresh = false
		if (device.currentValue("pendingChanges") == -2) {
			sendEvent(createEventMap("pendingChanges", "0"))
		}
		cmds += [
			batteryGetCmd(),
			sensorMultilevelGetCmd(tempSensorType, 0)
		]
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
	}	
	return cmds ? delayBetween(cmds, 500) : []	
}

private updateConfigVal(param) {
	def result = []	
	if (hasPendingChange(param)) {	
		def newVal = getParamIntVal(param)
		logDebug "${param.name}(#${param.num}): changing ${getParamStoredIntVal(param)} to ${newVal}"
		result << configSetCmd(param, newVal)
		result << configGetCmd(param)
	}		
	return result
}

private checkForPendingChanges() {
	def changes = 0
	configParams.each {
		if (hasPendingChange(it)) {
			changes += 1
		}
	}
	if (checkinIntervalChanged) {
		changes += 1
	}
	if (changes != getAttrVal("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, false))
	}
	return (changes != 0)
}

private getCheckinIntervalChanged() {
	return (state.checkinInterval != checkinIntervalSettingSeconds)
}

private hasPendingChange(param) {
	return (getParamIntVal(param) != getParamStoredIntVal(param) || state.refreshAll)
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
	return []
}

def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		logDebug "Clearing Tamper"
		sendEvent(createEventMap("tamper", "clear", false, "Tamper Clear"))
	}
	else if (state.pendingRefresh) {	
		sendEvent(createEventMap("pendingChanges", configParams.size(), false))			
		state.refreshAll = true
		logForceWakeupMessage "All configuration settings will be sent to the device and its data will be refreshed the next time it wakes up."
	}
	else {
		state.pendingRefresh = true
		sendEvent(createEventMap("pendingChanges", -2))
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	}
	return []
}


def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"	
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	sendLastCheckinEvent()
	
	cmds += configure()
		
	if (cmds) {
		cmds << "delay 1200"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}

private sendLastCheckinEvent() {
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time			

		logDebug "Device Checked In"
		sendEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	state.checkinInterval = cmd.seconds
	
	sendUpdatingEvent()
	
	logDebug "Checkin Interval = ${cmd.seconds / 60} Minutes"
		
	// Set the Health Check interval so that it reports offline 5 minutes after it's missed 2 checkins.
	def val = ((cmd.seconds * 2) + (5 * 60))
	
	def eventMap = createEventMap("checkInterval", val, false)

	eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
	
	runIn(5, finalizeConfiguration)

	return [ createEvent(eventMap) ]
}

private getTempSensorType() { 
	return 1 
}

private canReportBattery() {
	return !isDuplicateCommand(state.lastBatteryReport, (batteryReportingIntervalSettingSeconds * 1000))	
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	state.lastBatteryReport = new Date().time	
	[
		createEvent(createEventMap("battery", val, null, "Battery ${val}%", "%"))
	]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport: ${cmd}"
	
	sendUpdatingEvent()
	if (getAttrVal("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, false))
	}
	
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
	
	runIn(5, finalizeConfiguration)
	return []
}

private sendUpdatingEvent() {
	if (getAttrVal("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, false))
	}
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	state.refreshAll = false
	
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), false))	
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

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	logTrace "AlarmReport[zwaveAlarmEvent: $cmd.zwaveAlarmEvent, zwaveAlarmType: $cmd.zwaveAlarmType]"
	def result = []
	if (cmd.zwaveAlarmType == 0x06) {
		result += handleContactEvent(cmd.zwaveAlarmEvent)
	}
	else if (cmd.zwaveAlarmType == 0x07) {
		result += handleTamperEvent(cmd.zwaveAlarmEvent)
	}
	return result
}

private handleContactEvent(event) {
	def result = []	
	def val = (event == 0xFF || event == 0x16) ? "open" : "closed"
	result << createEvent(createEventMap("contact", val, null, "Contact ${val.capitalize()}"))
	return result
}

private handleTamperEvent(event) {
	def result = []
	def val
	if (event == 0xFF || event == 0x03) {
		val = "detected"
	}
	else if (event == 0) {
		val = "clear"		
	}
	if (val) {
		result << createEvent(createEventMap("tamper", val, null, "Tamper ${val.capitalize()}"))
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled Command: $cmd"
	return []
}

private sensorMultilevelGetCmd(sensorType, scale) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensorType, scale: scale))
}

private wakeUpIntervalSetCmd(val) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId))
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
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

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x22: 1,	// Application Status
		0x31: 5,	// Sensor Multilevel (v7)
		0x56: 1,	// Crc16 Encap
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration (v1)
		0x71: 2,  // Notification (v4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x8E: 2,	// Multi Channel Association
		0x98: 1		// Security
	]
}

// Configuration Parameters
private getConfigParams() {
	return [
		openClosedReportingParam,
		ledIndicatorParam,
		tamperCancelParam,
		tamperCancelDelayParam,
		tempMeasurementParam,
		tempReportingParam,
		tempThresholdParam,
		tempOffsetParam
	]
}

private getOpenClosedReportingParam() {
	return createConfigParamMap(1, "Report Closed when magnet is", 1, ["Closed${defaultOptionSuffix}": 0, "Open": 1], "openClosedReporting")
}

private getLedIndicatorParam() {
	return createConfigParamMap(2, "Open/Close LED Indicator", 1, ["Disabled": 0, "Open/Close": 1, "Wake Up": 2, "Open / Close / Wake Up": 3, "Tampering": 4, "Open / Close / Tampering": 5, "Wake Up / Tampering${defaultOptionSuffix}": 6, "Open / Close / Wake Up / Tampering": 7], "ledIndicator")
}

private getTamperCancelParam() {
	return createConfigParamMap(31, "Tamper Alarm Cancellation?", 1, ["Disabled": 0, "Enabled${defaultOptionSuffix}":1], "tamperCancel")
}

private getTamperCancelDelayParam() {
	return createConfigParamMap(30, "Tamper Alarm Cancellation Delay", 2, getIntervalOptions(5, [zeroName:"Disabled", max:32400]), "tamperCancelDelay")
}

private getTempMeasurementParam() {
	return createConfigParamMap(50, "Temperature Measurement Interval", 2, getIntervalOptions(300, [zeroName:"Disabled", max:32400]), "tempMeasurement")
}

private getTempReportingParam() {
	def options = ["Disabled${defaultOptionSuffix}":0]
	options += getIntervalOptions(0, [min:300, max:32400])
	return createConfigParamMap(52, "Temperature Reporting Interval", 2, options, "tempReporting")
}

private getTempThresholdParam() {
	return createConfigParamMap(51, "Temperature Reporting Threshold", 2, getTempOptions(10, [zeroName:"Disabled", max:300]), "tempThreshold")
}

private getTempOffsetParam() {
	return createConfigParamMap(53, "Temperature Offset", 2, getTempOptions(0, [zeroName:"None", min:-100, max:100]), "tempOffset")
}


// Associations in Z-Wave network Security Mode [num:3, size:1, default:3, range:0..3] //0:none, 1:2nd on/off, 2:third tamper, 3: 2nd and third
// 2nd association group triggers [num:11, size:1, default:0, range:0..2]
// Association for opening - value sent [num:12, Size:2, default:255, range:0..255]  
// Association for closing - value sent [num:13, size:2, default:0, range:0..255]
// Association for opening - time delay (seconds) [num:14, size:2, default:0, range:0..32400]
// Association for closing - time delay (seconds) [num:15, size:2, default:0, range:0..32400]
// Temperature alarm reports [num:54, size:1, default:0, range:0..3]
// High temperature alarm threshold [num:55, size:2, default:350, range:1..600] //(0.1C Step)
// Low temperature alarm threshold [num:56, size:2, default:100, range:0..599] //(0.1C Step)

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
private getCheckinIntervalSettingSeconds() {
	return convertOptionSettingToInt(wakeUpIntervalOptions, checkinIntervalSetting)
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}

private getBatteryReportingIntervalSettingSeconds() {
	return convertOptionSettingToInt(wakeUpIntervalOptions,batteryReportingIntervalSetting)
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}

private getDebugOutputSetting() {
	return settings?.debugOutput != false
}


// Options
private getTempOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = ((data?.zeroName && (!data?.min || data?.min > 0)) ? 0 : (data?.min != null ? data.min : 1))
	def max = data?.max != null ? data?.max : 100
	
	for (int i = min; i <= max; i += ((i < 5 && i >= -5) ? 1 : (i == 1 ? 4 : 5))) {
		if (i == 0 && data?.zeroName != null) {
			options["${data?.zeroName}"] = i
		}
		else {
			options["${i.toBigDecimal() * 0.1}°C / ${(((i.toBigDecimal() * 0.1)*9)/5)}°F"] = i
		}
	}
	return setDefaultOption(options, defaultVal)
}

private getIntervalOptions(defaultVal=null, data=[:]) {
	def options = [:]
	def min = data?.zeroName ? 0 : (data?.min != null ? data.min : 1)
	def max = data?.max != null ? data?.max : (9 * 60 * 60)
	
	[0,1,2,3,4,5,10,15,30,45].each {
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
		if (withinRange((it * 60), min, max)) {
			options["${it} Minute${x == 1 ? '' : 's'}"] = (it * 60)
		}
	}

	[1,2,3,6,9,12,18].each {
		if (withinRange((it * 60 * 60), min, max)) {
			options["${it} Hour${x == 1 ? '' : 's'}"] = (it * 60 * 60)
		}
	}	
	return setDefaultOption(options, defaultVal)
}

private withinRange(val, min, max) {
	return ((min == null || val >= min) && (max == null || val <= max))
}

private getWakeUpIntervalOptions() {
	def options = [:]
	options << getIntervalOptionsRange("Minute", 60, [5,10,15,30,45])
	options << getIntervalOptionsRange("Hour", (60 * 60), [1,2,3,6,9,12,18])
	options << getIntervalOptionsRange("Day", (60 * 60 * 24), [1])	
	return setDefaultOption(options, (6 * 60 * 60))
}

private getIntervalOptionsRange(name, multiplier, range) {
	def options = [:]
	range?.each {
		options["${it} ${name}${it == 1 ? '' : 's'}"] = (it * multiplier)
	}
	return options
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

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressing the button on the bottom of the device."
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: (displayed == null ? ("${getAttrVal(name)}" != "${value}") : displayed),
		isStateChange: true
	]
	if (unit) {
		eventMap.unit = unit
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
