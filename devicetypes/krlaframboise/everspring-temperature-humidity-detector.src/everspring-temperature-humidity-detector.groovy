/**
 *  Everspring Temperature/Humidity Detector v1.0
 *  (Model: ST814-2)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0 (05/10/2017)
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
		name: "Everspring Temperature/Humidity Detector", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"		
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "lastCheckin", "string"
		attribute "lastUpdate", "string"
		attribute "pendingChanges", "number"
			
		fingerprint deviceId: "0x0701", inClusters: "0x20,0x31,0x60,0x70,0x71,0x72,0x80,0x84,0x85,0x86"
		fingerprint mfr:"0060", prod:"0006", model:"0001"
	}

	simulator { }
	
	preferences {
		input "primaryStatus", "enum",
			title: "Primary Capability:",
			defaultValue: primaryStatusSetting,
			required: false,
			displayDuringSetup: true,
			options: ["Humidity", "Temperature"]			

		configParams.each {
			input "${it.prefName}", "${it.type}",
				title: getInputTitle("${it.name}", it.details),
				defaultValue: it.defaultVal,
				range: "${it.min}..${it.altMax == null ? it.max : it.altMax}",
				required: false,
				displayDuringSetup: true				
		}
		
		input "tempOffset", "number",
			title: getInputTitle("Temperature Offset", ["-100~-1 = -10° to -0.1°", "0 = No Offset", "1~100 = 0.1° to 10°"]),
			range: "-100..100",
			defaultValue: 0, 
			required: false,
			displayDuringSetup: true
			
		input "humidityOffset", "number",
			title: getInputTitle("Humidity Offset", ["-25~-1 = -25%RH to -1%RH", "0 = No Offset", "1~25 = 1%RH to 25%RH"]),
			range: "-25..25",
			defaultValue: 0, 
			required: false,
			displayDuringSetup: true
	
		input "wakeUpInterval", "number",
			title: getInputTitle("Checkin Interval", ["10~1440 = 10 Minutes to 1 Day"]),
			range: "10..1440",
			defaultValue: checkinIntervalSetting, 
			required: false,
			displayDuringSetup: true
			
		input "batteryReportingInterval", "number",
			title: getInputTitle("Battery Reporting Interval", ["10~1440 = 10 Minutes to 1 Day"]),
			range: "10..1439",
			defaultValue: batteryReportingIntervalSetting, 
			required: false,
			displayDuringSetup: true
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true,
			required: false,
			displayDuringSetup: true
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "primaryStatus",
					label:'${currentValue}',
					icon: "st.Weather.weather2",
					backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "secondaryStatus", label:'${currentValue}'
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
		
		valueTile("humidity", "device.humidity", width: 2, height: 2){
			state "humidity", label:'${currentValue}% RH',
			backgroundColors:[
				[value:0, color:"#D6F3FF"],				
				[value:100, color:"#00A0DC"]
			]
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}% Battery', unit:""
		}
		
		standardTile("refresh", "device.generic", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'
			state "0", label: ''
			state "-1", label:'Sending Changes'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "mainTile"
		details(["mainTile", "temperature", "humidity", "battery", "refresh", "pending", "lastUpdate"])
	}
}

private getInputTitle(title, details) {
	details?.each {
		title = "${title}\n[${it}]"
	}
	return title
}

def updated() {
	def result = []
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		if (state.tempOffset != tempOffsetSetting && state.origTemp != null) {
			state.tempOffset = tempOffsetSetting
			sendTempEvent(state.origTemp.temp, state.origTemp.scale, state.origTemp.prec)
		}
		
		if (state.humidityOffset != humidityOffsetSetting && state.origHumidity != null) {
			state.humidityOffset = humidityOffsetSetting
			sendHumidityEvent(state.origHumidity)
		}
		
		if (!state.primaryStatus) {
			state.primaryStatus = primaryStatusSetting
		}
		else if (primaryStatusSetting != state.primaryStatus) {
			state.primaryStatus = primaryStatusSetting
			def secondary = getAttrValue("secondaryStatus") ?: ""
			
			sendEvent(createEventMap("secondaryStatus", getAttrValue("primaryStatus") ?: "", false))
			
			sendEvent(createEventMap("primaryStatus", secondary, false))			
		}

		if (checkForPendingChanges()) {
			logDebug "The configuration will be updated the next time the device wakes up."
		}		
	}		
	return result
}

def configure() {	
	logTrace "configure()"
	def cmds = []
		
	if (!state.configured) {
		logTrace "Waiting 5 second because this is the first time being configured"
		cmds << "delay 1000"		
	}
	
	if (state.refreshAll || state.checkinIntervalSeconds != (checkinIntervalSetting * 60)) {		
		cmds << wakeUpIntervalSetCmd((checkinIntervalSetting * 60))
		cmds << wakeUpIntervalGetCmd()
	}
				
	configParams.each { param ->	
		cmds += updateConfigVal(param)
	}
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	if (state.configured) {
		cmds << "delay 5000"
		cmds << wakeUpNoMoreInfoCmd()
	}
	return cmds ? delayBetween(cmds, 250) : []
}

private updateConfigVal(param) {
	def result = []
	def settingVal = getConfigSetting(param)
	
	if (hasPendingChange(settingVal, param)) {
		logDebug "${param.name}(#${param.num}): changing ${getParamStoredIntVal(param)} to ${settingVal}"
		
		result << configSetCmd(param, settingVal)
		result << configGetCmd(param)				
		
	}		
	return result
}

private checkForPendingChanges() {
	def changes = 0
	configParams.each {
		if (hasPendingChange(getConfigSetting(it), it)) {
			changes += 1
		}
	}
	
	if (state.checkinIntervalSeconds != (checkinIntervalSetting * 60)) {
		changes += 1
	}
	
	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, false))
	}
	return (changes != 0)
}

private hasPendingChange(settingVal, param) {
	return (settingVal != getParamStoredIntVal(param) || state.refreshAll)
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
	return null
}


def refresh() {	
	sendEvent(createEventMap("pendingChanges", configParams.size(), false))
	state.pendingRefresh = true
	state.refreshAll = true
	logDebug "The sensor data will be refreshed the next time the device wakes up."
	return null
}


def parse(String description) {
	def result = []
	// logTrace "parse: $description"
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (!description?.startsWith("Err")) {
		def cmd = zwave.parse(description, commandClassVersions)
		// logTrace "Parsed Cmd: $cmd"
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	return handleWakeupEvent(true)
}

private handleWakeupEvent(canConfigure) {
	def cmds = []
	
	if (canConfigure && checkForPendingChanges()) {
		cmds += configure()
	}
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	if (getAttrValue("temperature") == null) {
		cmds << sensorMultilevelGetCmd(sensorTemp)
	}
	
	if (getAttrValue("humidity") == null) {
		cmds << sensorMultilevelGetCmd(sensorHumidity)
	}
			
	if (cmds) {
		cmds << "delay 1000"
	}		
	cmds << wakeUpNoMoreInfoCmd()
	
	sendResponse(cmds)
	return []
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSetting * 60 * 1000)
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled Command: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"	
	sendEvent(createBatteryEventMap(cmd.batteryLevel))
	return []
}

private createBatteryEventMap(val) {
	if (val == 255) {
		val = 1
	}
	else if (val > 100) {
		val = 100
	}
	logDebug "Battery ${val}%"
	state.lastBatteryReport = new Date().time	
	return createEventMap("battery", val, null, null, "%")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	state.checkinIntervalSeconds = cmd.seconds
	
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 3) + (5 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	if (getAttrValue("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, false))
	}
	
	def val = cmd.scaledConfigurationValue		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		logDebug "${configParam.name}(#${configParam.num}) = ${val}"
		state["configVal${cmd.parameterNumber}"] = val
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}
	
	runIn(5, finalizeConfiguration)
	return []
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	
	state.refreshAll = false
	state.configured = true
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), false))	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def cmds = []
	// logTrace "SensorMultilevelReport: ${cmd}"
	
	def canConfigure = false
	switch (cmd.sensorType) {
		case sensorTemp.sensorType:
			sendTempEvent(cmd.scaledSensorValue, cmd.scale, cmd.precision)
			canConfigure = true			
			break
		case sensorHumidity.sensorType:
			sendHumidityEvent(cmd.scaledSensorValue)
			break
		default:
			logDebug "Unknown Sensor Type: ${cmd}"
	} 
	
	cmds += handleWakeupEvent(canConfigure)  // Handle like wakeup event because the device stays awake for 10 minutes every time it sends a report.
	
	return cmds ? sendResponse(cmds) : []
}

private sendTempEvent(origVal, scale, prec) {
	def cmdScale = scale == 1 ? "F" : "C"
	def localScale = getTemperatureScale()
	
	def val = convertTemperatureIfNeeded(origVal, cmdScale, prec)
	
	val = roundTwoPlaces(safeToDec(val) + safeToDec(tempOffsetSetting * 0.1))
	
	logDebug "Temperature is ${val}°${localScale}"
	
	state.origTemp = [temp: origVal, scale: scale, prec: prec]
	
	sendEvent(createEventMap("temperature", val, null, null, localScale))
	sendStatusEvent("Temperature", "${val}°${localScale}")
}

private sendHumidityEvent(origVal) {
	def val = (origVal + humidityOffsetSetting)	
	
	logDebug "Relative Humidity is ${val}%"
	
	state.origHumidity = origVal
	sendEvent(createEventMap("humidity", val, null, null, "%"))
	sendStatusEvent("Humidity", "${val}% RH")
}

private sendStatusEvent(status, value) {
	def name = (primaryStatusSetting == status) ? "primaryStatus" : "secondaryStatus"
	sendEvent(createEventMap("${name}", value, false))
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	logTrace "AlarmReport: ${cmd}"
	if (cmd.alarmType == alarmPowerApplied.alarmType && cmd.alarmLevel == alarmPowerApplied.alarmLevel) {
		logDebug "Power Applied"
	}
	else if (cmd.alarmType == alarmLowBattery.alarmType && cmd.alarmLevel == alarmLowBattery.alarmLevel) {
		sendEvent(createBatteryEventMap(255))
	}
	else {
		logDebug "Unknown AlarmReport: ${cmd}"
	}
	return []
}


private sensorMultilevelGetCmd(sensor) {
	return zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensor.sensorType, scale: sensor.scale).format()
}

private wakeUpIntervalSetCmd(val) {
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
}

private wakeUpIntervalGetCmd(val) {
	return zwave.wakeUpV2.wakeUpIntervalGet().format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private configSetCmd(param, val) {
	return zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: val).format()
}

private configGetCmd(param) {
	return zwave.configurationV2.configurationGet(parameterNumber: param.num).format()
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x31: 5,	// SensorMultilevel
		0x60: 3,	// MultiChannel
		0x70: 2,  // Configuration
		0x71: 2,  // Alarm
		0x72: 2,  // ManufacturerSpecific
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)		
	]
}

private getSensorTemp() { [sensorType: 1, scale: 42] }
private getSensorHumidity() { [sensorType: 5, scale: 1] }
private getAlarmPowerApplied() { [alarmType: 2, alarmLevel: 1] }
private getAlarmLowBattery() { [alarmType: 1, alarmLevel:255] }

// Configuration Parameters
private getConfigParams() {
	return [
		reportingIntervalParam,
		tempReportingParam,
		humidityReportingParam
	]
}

private getReportingIntervalParam() {
	return [num:6, name:"Temperature/Humidity Reporting Interval", size:2, type:"number", min:0, max:1439, defaultVal:0, details:["0 = Disabled", "1~1439 = 1 Minute to 24 Hours"], prefName:"reportingInterval"]	
}

private getTempReportingParam() {
	return [num:7, name:"Temperature Reporting Threshold", size:1, type:"number", min:0, max:70, defaultVal:2, details:["0 = Disabled", "1~70 = 1°C to 70°C"], prefName:"tempReporting"]	
}

private getHumidityReportingParam() {
	return [num:8, name:"Humidity Reporting Threshold", size:1, type:"number", min:0, max:70, defaultVal:5, details:["0 = Disabled", "5~70 = 5% to 70%"], prefName:"humidityReporting"]	
}

private getParamStoredIntVal(param) {
	return state["configVal${param.num}"]	
}

// Settings
private getPrimaryStatusSetting() {
	return settings?.primaryStatus ?: "Temperature"
}

private getConfigSetting(param) {
	def val = safeToInt(settings?."${param.prefName}", param.defaultVal)
		
	if (val > param.max && val != param.altMax) {
		val = param.max
	}
	else if (val < param.min) {
		val = param.min
	}
	
	return val	
}

private getTempOffsetSetting() {
	return safeToDec(settings?.tempOffset, 0)
}

private getHumidityOffsetSetting() {
	return safeToInt(settings?.humidityOffset, 0)
}

private getCheckinIntervalSetting() {
	return safeToInt(settings?.wakeUpInterval, 60)
}

private getBatteryReportingIntervalSetting() {
	return safeToInt(settings?.batteryReportingInterval, 480)
}

private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}


private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def newVal = "${value}"	
	def isStateChange = displayed ?: (getAttrValue(name) != newVal)
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
	// logTrace "Creating Event: ${eventMap}"
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

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
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
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}
