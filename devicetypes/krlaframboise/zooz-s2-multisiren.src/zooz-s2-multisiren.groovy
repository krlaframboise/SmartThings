/**
 *  Zooz S2 Multisiren v1.0
 *  (Models: ZSE19)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation:
 *
 *
 *  Changelog:
 *
 *    1.0 (11/06/2018)
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
		name: "Zooz S2 Multisiren", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-siren"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Alarm"
		capability "Switch"		
		capability "Switch Level"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
				
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"		
		attribute "lastCheckin", "string"

		fingerprint mfr:"027A", prod:"000C", model:"0003", deviceJoinName: "Zooz S2 Multisiren"
	}

	simulator { }
		
	tiles(scale: 2) {
		multiAttributeTile(name:"primaryStatus", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "alarm", label: 'ALARM', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#00a0dc"
				attributeState "play", label: 'PLAYING', action: "alarm.off", icon: "st.alarm.beep.beep", backgroundColor: "#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
			}
		}
		
		standardTile("sliderText", "generic", width: 2, height: 2) {
			state "default", label:'Play Sound #'
		}
		
		controlTile("slider", "device.level", "slider",	height: 2, width: 4) {
			state "level", action:"switch level.setLevel"
		}
		
		standardTile("off", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "alarm.off"
		}
		
		standardTile("on", "device.switch", width: 2, height: 2) {
			state "default", label:'On', action: "switch.on"
		}
		
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "default", label:'Alarm', action: "alarm.both"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configuration.configure", icon:"st.secondary.tools"
		}
		
		valueTile("battery", "device.battery", width: 2, height: 2) {
			state "default", label:'${currentValue}% Battery'
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:3, height: 1) {
			state "syncStatus", label:'${currentValue}'
		}
		
		main "primaryStatus"
		details(["primaryStatus", "sliderText", "slider", "off", "alarm", "on", "refresh", "configure", "battery", "firmwareVersion", "syncStatus"])
	}
		
	preferences {	
		configParams.each { param ->
			input "configParam${param.num}", "enum",
				title: "${param.name}:",
				required: false,
				defaultValue: "${param.value}",
				displayDuringSetup: true,
				options: param.options
		}
		
		input "switchOnAction", "enum",
			title: "Switch On Action",
			defaultValue: "0",
			required: false,
			options: setDefaultOption(switchOnActionOptions, "0")

		// input "tempOffset", "enum",
			// title: "Temperature Offset",
			// defaultValue: "0",
			// required: false,
			// options: setDefaultOption(tempOffsetOptions, "0")
			
		// input "humidityOffset", "enum",
			// title: "Humidity Offset",
			// defaultValue: "0",
			// required: false,
			// options: setDefaultOption(humidityOffsetOptions, "0")
		
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}


def installed () { 
	return response(refresh())
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time

		runIn(2, updateSyncStatus)
		
		def cmds = []
		if (pendingChanges > 0) {
			cmds += configure()
		}
		return cmds ? response(cmds) : []
	}	
}


def configure() {	
	runIn(5, updateSyncStatus)
			
	def cmds = []
	
	if (!device.currentValue("switch")) {
		sendEvent(getEventMap("switch", "off"))
		sendEvent(getEventMap("level", 0))
		sendEvent(getEventMap("alarm", "off"))
		sendEvent(getEventMap("primaryStatus", "off"))
	}
	
	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
	
	configParams.each { 
		logDebug "CHANGING ${it.name}(#${it.num}) from ${getParamStoredValue(it.num)} to ${it.value}"
		cmds << configSetCmd(it, it.value)
		cmds << configGetCmd(it)
		cmds << configGetCmd(it)		
	}
	
	return delayBetween(cmds, 250)
}


def on() {
	logDebug "on()..."
	if (settings?.switchOnAction == "on") {
		return both()
	}
	else {
		def sound = safeToInt(settings?.switchOnAction, 0)
		if (sound) {
			return setLevel(sound)
		}	
		else {
			log.warn "Ignoring 'on' command because the Switch On Action setting is set to 'Do Nothing'"
		}
	}
}


def setLevel(level, duration=null) {
	logDebug "setLevel(${level})..."	
	def cmds = []
	def val = safeToInt(level, 0)
	if (val) {
		if (device.currentValue("alarm") == "off") {
			runIn(2, clearStatus)
			sendEvent(getEventMap("primaryStatus", "play"))
			cmds << configSetCmd(playSoundParam, val)
		}
		else {
			log.warn "Can't play sound #${val} beacuse alarm is on"
		}		
	}
	else {
		log.warn "${val} is not a valid sound number"
	}	
	return cmds
}

def clearStatus() {
	if (device.currentValue("alarm") == "off") {
		sendEvent(getEventMap("primaryStatus", "off"))
	}
}


def siren() {
	return both()
}

def strobe() { 
	return both() 
}

def both() { 
	log.debug "Turning on Siren..."
	return delayBetween([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	], 500)
}


def off() {
	logDebug "off()..."	
	return delayBetween([
		switchBinarySetCmd(0),
		configSetCmd(playSoundParam, 0)
	], 500)
}


def refresh() {
	logDebug "refresh()..."
	
	runIn(5, updateSecondaryStatus)
	
	state.lastBattery = null
	return delayBetween([		
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(humiditySensorType)
	], 2500)
}


def ping() {
	logDebug "ping()..."	
	return sendResponse([basicGetCmd()])
}

private sendResponse(cmds) {
	def hubCmds = []
	cmds.each {
		hubCmds << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(hubCmds, 100)
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 0, sensorType: sensorType))
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
		0x31: 5,	// SensorMultilevel
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service (V2)
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 2,	// Configuration
		0x71: 3,	// Notification (v4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x79: 1,	// Sound Switch
		0x7A: 2,	// Firmware Update Md
		0x80: 1,	// Battery
		0x85: 2,	// Association
		0x86: 1,	// Version (3)
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {	
	def result = []
	try {
		if ("${description}".startsWith("Err 106")) {
			log.warn "secure inclusion failed"
		}
		else {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)		
			}
			else {
				log.warn "Unable to parse: $description"
			}
		}
			
		if (!isDuplicateCommand(state.lastCheckinTime, 59000)) {
			state.lastCheckinTime = new Date().time
			sendEvent(getEventMap("lastCheckin", convertToLocalTimeString(new Date())))
		}
		
		if (!isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000))) {			
			result << response(batteryGetCmd())
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


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	
	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)
	}
	return []	
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
	
	state.lastBattery = new Date().time
	
	sendEvent(getEventMap("battery", val, true, "%"))
	return []
}	


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport: ${cmd}"
	
	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)	

		if (param == reportingIntervalParam) {
			updateHealthCheckInterval(val)
		}
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}		
	return []
}

private updateHealthCheckInterval(minutes) {
	def minReportingInterval = calculateMinimumReportingInterval()
	
	if (state.minReportingInterval != minReportingInterval) {
		state.minReportingInterval = minReportingInterval
			
		// Set the Health Check interval so that it can be skipped three times plus 5 minutes.
		def checkInterval = ((minReportingInterval * 3) + (5 * 60))
		
		def eventMap = getEventMap("checkInterval", checkInterval)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}	
}

private calculateMinimumReportingInterval() {
	if (reportingIntervalParam.value < (30 * 60)) {
		return (30 * 60)
	}
	else {
		return reportingIntervalParam.value
	}
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
		sendEvent(getEventMap("syncStatus", status))
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
	
	def primaryStatus = "off"
	def alarmVal = "off"
	
	if (cmd.value) {
		primaryStatus = "alarm"
		alarmVal = "both"
	}
	
	sendEvent(getEventMap("alarm", alarmVal, true))
	sendEvent(getEventMap("primaryStatus", primaryStatus))
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=0) {
	logTrace "BasicReport: ${cmd}"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	runIn(2, updateSecondaryStatus)
	
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = cmd.scale ? "F" : "C"
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
			
			sendEvent(getEventMap("temperature", temp, true, getTemperatureScale()))
			break		
			
		case humiditySensorType:			
			sendEvent(getEventMap("humidity", cmd.scaledSensorValue, true, "%"))
			break		
		default:
			logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	
	return []
}

def updateSecondaryStatus() {
	def temp = device.currentValue("temperature")
	def humidity = device.currentValue("humidity")
	
	sendEvent(name:"secondaryStatus", value:"${temp}°${getTemperatureScale()} / ${humidity}% RH", displayed: false)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }


private getConfigParams() {
	return [
		alarmDurationParam,
		reportingIntervalParam
	]
}

private getAlarmDurationParam() {
	return getParam(1, "Alarm Duration", 2, 180, alarmDurationOptions)
}

private getReportingIntervalParam() {
	return getParam(2, "Temperature/Humidity Reporting Interval", 2, 30, reportingIntervalOptions) 
}

private getPlaySoundParam() {
	return getParam(3, "Play Sound by Number", 1, 0)
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
	return options?.collect { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
}


private getAlarmDurationOptions() {
	def options = [:]
	[10,15,20,25,30,45].each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["60"] = "1 Minute"
	options["90"] = "1 Minute 30 Seconds"
	
	(2..10).each {
		options["${it * 60}"] = "${it} Minutes"
	}
	return options
}

private getReportingIntervalOptions() {
	def options = ["1":"1 Minute"]
	
	[2,3,4,5,10,15,20,30,45].each {
		options["${it}"] = "${it} Minutes"
	}
	
	options["60"] = "1 Hour"
	
	[2,3,4,5,6,7,8,9,10,11,12,15,18,21].each {
		options["${it * 60}"] = "${it} Hours"		
	}
	
	options["1440"] = "1 Day"
	
	return options
}

private getSwitchOnActionOptions() {
	def options = [
		"0":"Do Nothing",
		"on": "Turn On Siren"	
	]
	
	(1..99).each {
		options["${it}"] = "Play Sound #${it}"
	}	
	return options
}

private getTempOffsetOptions() {
	def options = [:]
	
	(-10..-1).each {
		options["${it}"] = "${it}°"
	}
	
	options["0"] = "No Offset"
	
	(1..10).each {
		options["${it}"] = "${it}°"
	}
	
	return options
}

private getHumidityOffsetOptions() {
	def options = [:]
	
	(-10..-1).each {
		options["${it}"] = "${it}%"
	}
	
	options["0"] = "No Offset"
	
	(1..10).each {
		options["${it}"] = "${it}%"
	}
	return options
}


private getEventMap(name, value, displayed=false, unit=null) {	
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
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}