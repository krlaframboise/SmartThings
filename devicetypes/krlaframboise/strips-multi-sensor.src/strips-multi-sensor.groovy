/**
 *  Strips Multi-Sensor 1.0.1
 *  (Models: Strips Drip / Strips Comfort)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to Documentation: https://community.smartthings.com/t/release-strips-drip-strips-comfort/135276?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.0.1 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.0 (08/31/2018)
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
		name: "Strips Multi-Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-motion-4"
		// vid:"generic-leak"		
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Water Sensor"
		capability "Temperature Measurement"		
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		attribute "lastUpdate", "string"
		
		attribute "pendingChanges", "number"
		
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"		
		attribute "firmwareVersion", "string"
		
		fingerprint mfr:"019A", prod:"0003", model:"000A", deviceJoinName: "Strips Multi-Sensor" // Same fingerprint for Strips Drip and Strips Comfort
	}
	
	simulator { }
	
	preferences {
		input "primaryTileStatus", "enum",
			title: "Primary Status:",
			defaultValue: primaryTileStatusSetting,
			required: false,
			options: primaryStatusOptions

		input "secondaryTileStatus", "enum",
			title: "Secondary Status:",
			defaultValue: secondaryTileStatusSetting,
			required: false,
			options: secondaryStatusOptions		
			
		[
			ledAlarmParam,
			leakageAlarmParam,
			leakageLevelParam,
			lightReportingParam,
			tempLightReportingIntervalParam,
			tempReportingParam
		].each {
			getOptionsInput(it)
		}
		
		input "tempOffset", "enum",
			title: "Temperature Offset:",
			required: false,
			defaultValue: "0",
			options: setDefaultOption(tempOffsetOptions, "0")
		
		input "decimalPlaces", "enum", 
			title: "Round values to how many decimal places?", 
			defaultValue: 1, 
			required: false,
			options: [[0:"0"],[1:"1"]]
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false		
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "primaryStatus", 
					label:'${currentValue}', 
					icon:"${resourcesUrl}temperature.png",
					// icon:"${resourcesUrl}light.png",					
					backgroundColor:"#ffffff"			
				attributeState "Dry", 
					label:'DRY', 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "Wet", 
					label:'WET', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
				attributeState "dry", label:'DRY'
				attributeState "wet", label:'WET'
			}
		}
		
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			icon: "${resourcesUrl}temperature.png"
		}
		
		valueTile("illuminance", "device.illuminance", width: 2, height: 2){
			state "default", label:'${currentValue}lx', icon: "${resourcesUrl}light.png"
		}
		
		standardTile("water", "device.water", width: 2, height: 2){		
			state "dry", label:'Dry', icon: "st.alarm.water.dry"
			state "wet", label:'Wet', icon: "st.alarm.water.wet"
		}			
		
		valueTile("battery", "device.battery", width: 2, height: 2){
			state "default", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "1", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
		}
			
		
		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'
			state "0", label: 'No Pending Changes'
			state "-1", label:'Updating Settings'
			state "-2", label:'Refresh Pending'
			state "-3", label:'Refreshing'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}'
		}
		
		valueTile("lastActivity", "device.lastCheckIn", decoration: "flat", width: 2, height: 2){
			state "lastCheckIn", label:'Last\nActivity\n\n${currentValue}'
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration: "flat", width: 2, height: 2){
			state "firmwareVersion", label:'Firmware \n${currentValue}'
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"${resourcesUrl}refresh.png"
		}
		
		main("mainTile")
		details(["mainTile", "temperature", "illuminance", "battery", "water", "refresh","pending", "firmwareVersion", "lastActivity", "lastUpdate"])
	}
}

private getResourcesUrl() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/"
}

private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		options: param.options
}


def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
	
		initializeOffsets()
		initializePrimaryTile()
		
		if (checkForPendingChanges()) {
			logForceWakeupMessage("The configuration will be updated the next time the device wakes up.")
		}		
	}	
}

private checkForPendingChanges() {
	def changes = 0
	configParams.each {
		if (hasPendingChange(it)) {		
			changes += 1
		}
	}
	
	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, "", false))
	}
	return (changes != 0)
}


private initializeOffsets() {
	def eventMaps = []
	
	if (state.actualTemp != null) {
		eventMaps += createTempEventMaps(state.actualTemp, true)
	}
	
	eventMaps += createStatusEventMaps(eventMaps, true)
	
	eventMaps?.each { eventMap ->
		
		eventMap.descriptionText = getDisplayedDescriptionText(eventMap)
		
		sendEvent(eventMap)
	}
}

private initializePrimaryTile() {
	def currentStatus = device.currentValue("primaryStatus")
	def newStatus = getDescriptionText(device.currentState(primaryTileStatusSetting))
	
	if ("${newStatus}" != "${currentStatus}") {
		sendEvent(name: "primaryStatus", value: newStatus, displayed: false)
	}
}

private getDisplayedDescriptionText(eventMap) {
	def deviceName = "${device.displayName}"
	if (eventMap?.displayed && eventMap?.descriptionText && !eventMap?.descriptionText?.contains(deviceName)) {
		return "${deviceName}: ${eventMap.descriptionText}"
	}
	else {
		return eventMap?.descriptionText
	}
}

def configure() {	
	logTrace "configure()"
	
	def cmds = []		
	if (!getAttrValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
	
	if (!device.currentValue("water")) {
		sendWaterEvents(0)
	}
	
	if (state.pendingRefresh != false || state.refreshAll || !allAttributesHaveValues()) {
		runIn(5, finalizeConfiguration)
		sendEvent(createEventMap("pendingChanges", -3, "", false))
		
		cmds += [
			wakeUpIntervalGetCmd(),
			// sensorMultilevelGetCmd(waterSensorType),
			sensorMultilevelGetCmd(tempSensorType),
			sensorMultilevelGetCmd(lightSensorType)
		]
	}
	
	cmds << batteryGetCmd()
	
	if (state.configured != true) {		
		configParams.each { param ->
			cmds << configGetCmd(param)
		}
	}
	else {
		configParams.each { param ->
			cmds += updateConfigVal(param)
		}	
	}
	
	return cmds ? delayBetween(cmds, 500) : []
}

private allAttributesHaveValues() {
	return (getAttrValue("temperature") != null && 
		getAttrValue("water") != null && 
		getAttrValue("illuminance") != null && 
		getAttrValue("battery") != null)
}

private updateConfigVal(param) {
	def result = []	
	if (hasPendingChange(param) || state.refreshAll) {	
		logDebug "${param.name}(#${param.num}): changing ${getParamStoredVal(param)} to ${param.value}"
		result << configSetCmd(param, param.value)
		result << configGetCmd(param)
	}		
	return result
}

private hasPendingChange(param) {
	return (param.value != getParamStoredVal(param))
}

private getParamStoredVal(param) {
	return state["configVal${param.num}"]	
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

// Settings
private getDecimalPlacesSetting() {
	return safeToInt(settings?.decimalPlaces, 1)
}

private getPrimaryTileStatusSetting() {
	return settings?.primaryTileStatus ?: "temperature"
}
private getSecondaryTileStatusSetting() {	
	return settings?.secondaryTileStatus ?: "none"
}
private getTempOffsetSetting() {
	return safeToDec(settings?.tempOffset, 0)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}

private getPrimaryStatusOptions() {
	return [
		"water":"Water",
		"temperature":"Temperature",
		"illuminance":"Light"
	]
}

private getSecondaryStatusOptions() {
	return [
		"none":"None",
		"water":"Water",
		"temperature":"Temperature",
		"illuminance":"Light",
		"combined":"Combined Values"
	]
}


// Sensor Types
private getTempSensorType() { return 1 }
private getLightSensorType() { return 3 }
private getWaterSensorType() { return 31 }

// Configuration Parameters
private getConfigParams() {
	return [
		ledAlarmParam,
		tempLightReportingIntervalParam,
		tempReportingParam,
		lightReportingParam,
		leakageAlarmParam,
		leakageLevelParam,
		leakageAlarmIntervalParam
	]
}


private getLedAlarmParam() {
	return getParam(2, "LED", 1, 1, enabledDisabledOptions)
}

private getTempLightReportingIntervalParam() {
	return getParam(3, "Temperature/Light Reporting Frequency", 1, 1, ["0":"Off", "1":"Normal", "2":"Frequent"])
}

private getTempReportingParam() {
	return getParam(4, "Temperature Reporting", 1, 1, enabledDisabledOptions)
}

private getLightReportingParam() {
	return getParam(9, "Ambient Light Reporting", 1, 1, enabledDisabledOptions) // 2:Determined by Light Thresholds
}

private getLeakageAlarmParam() {
	return getParam(12, "Water Reporting", 1, 1, enabledDisabledOptions)
}

private getLeakageLevelParam() {
	return getParam(13, "Water Sensitivity", 1, 10, leakageLevelOptions)
}

private getLeakageAlarmIntervalParam() {
	return getParam(14, "Leakage Alarm Interval", 1, 1, moistureIntervalOptions)
}


private getParam(num, name, size, defaultVal, options=null, range=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val]
	if (options) {		
		map.options = setDefaultOption(options, defaultVal)
	}
	if (range) map.range = range
	
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

private getLeakageLevelOptions() {
	def options = [
		"1":"1 (Most Sensitive)"
	]	
	for (int i = 1; i <= 18; i += 1) {
		options["${i * 5}"] = "${i + 1}"
	}	
	options["95"] = "20 (Least Sensitive)"
	return options
}

private getMoistureIntervalOptions() {
	def options = ["0":"Off"]
	
	[1,2,3,4,5,10,12,18].each {
		options["${it}"] = "${it} Hour${it == 1 ? '' : 's'}"
	}
	
	(1..5).each {
		options["${it * 24}"] = "${it} Day${it == 1 ? '' : 's'}"
	}
	return options
}


private getTempOffsetOptions() {
	def options = [:]
	for (int i = -50; i < 0; i += 1) {
		options["${i / 10}"] = "${i / 10}°"
	}
	
	options["0"] = "No Offset"
	
	for (int i = 1; i <= 50; i += 1) {
		options["${i / 10}"] = "${i / 10}°"
	}
	return options
}


def parse(String description) {
	def result = []
	
	sendLastCheckInEvent()
	
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
	def encapCmd = cmd.encapsulatedCommand(getCommandClassVersions())
		
	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"		
	}
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x31: 5,	// Sensor Multilevel (v7)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	// Set the Health Check interval so that it reports offline 5 minutes after it's missed 2 checkins.
	def val = ((cmd.seconds * 2) + (5 * 60))
	
	def eventMap = createEventMap("checkInterval", val, "", false)

	eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
	
	sendEvent(eventMap)
	
	return [ ]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	logDebug "Device Woke Up"
	
	cmds += configure()
		
	if (cmds) {
		cmds << "delay 2000"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}

private sendLastCheckInEvent() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time			

		sendEvent(createEventMap("lastCheckIn", convertToLocalTimeString(new Date()), "", false))
	}
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	
	sendEvent(createEventMap("battery", val, "%", true))
	return []
}	

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "ManufacturerSpecificReport: ${cmd}"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	logDebug "Firmware Version: ${version}"
	
	if (getAttrValue("firmwareVersion") != "${version}") {
		sendEvent(name: "firmwareVersion", value: "${version}", displayed: false)
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport: ${cmd}"
	sendUpdatingEvent()
	
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

private sendUpdatingEvent() {
	if (getAttrValue("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, "", false))
	}
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	
	state.refreshAll = false
	state.pendingRefresh = false
	state.configured = true
	
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), "", false))
	return []
}

private sendWaterEvents(val) {
	def waterVal = (val ? "wet" : "dry")
	
	def eventMaps = []
	eventMaps += createEventMaps("water", waterVal, "", true, false)	
	eventMaps += createStatusEventMaps(eventMaps, false)
	
	eventMaps?.each {
		it.descriptionText = getDisplayedDescriptionText(it)		
		sendEvent(it)
	}
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	def cmds = []	
	if (cmd.notificationType == 7) {
		if (cmd.event == 4 && cmd.notificationStatus == 255) {
			logDebug "Device Woke Up"
			cmds += configure()
			cmds << "delay 2000"
			cmds << wakeUpNoMoreInfoCmd()
		}
	}	
	else if (cmd.notificationType == 5) {
		sendWaterEvents(cmd.event)
	}
	return cmds ? response(cmds) : []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	def eventMaps = []	
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = cmd.scale == 1 ? "F" : "C"
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
			
			eventMaps += createTempEventMaps(temp, false)
			break		
		
		case lightSensorType:
			eventMaps += createEventMaps("illuminance", cmd.scaledSensorValue, "lx", true, onlyIfNew)
			break		
		
		case waterSensorType:
			// sendWaterEvents(cmd.scaledSensorValue)
			break
	}
	
	eventMaps += createStatusEventMaps(eventMaps, false)
	
	def result = []
	eventMaps?.each {
		logTrace "Creating Event: ${it}"
		it.descriptionText = getDisplayedDescriptionText(it)
		result << createEvent(it)
	}
	return result
}

private createTempEventMaps(val, onlyIfNew) {
	state.actualTemp = val
	def scale = getTemperatureScale()
	def offsetVal = applyOffset(val, tempOffsetSetting, "Temperature", "°${scale}")
	return createEventMaps("temperature", offsetVal, scale, true, onlyIfNew)	
}


private applyOffset(val, offsetVal, name, unit) {
	if (offsetVal) {
		logTrace "Before Applying ${offsetVal}${unit} ${name} Offset to ${val}${unit}"
		val = (safeToDec(val, 0) + safeToDec(offsetVal, 0))		
		logTrace "After Applying ${offsetVal}${unit} ${name} Offset to ${val}${unit}"
	}
	return roundVal(val, decimalPlacesSetting)
}

private createStatusEventMaps(eventMaps, onlyIfNew) {
	def result = []
	
	def primaryStatus = eventMaps?.find { it.name == primaryTileStatusSetting }?.descriptionText
	if (primaryStatus) {
		result += createEventMaps("primaryStatus", primaryStatus, "", false, onlyIfNew)
	}	
	
	def secondaryStatus = getSecondaryStatus(eventMaps)
	if (secondaryStatus || secondaryTileStatusSetting == "none") {
		result += createEventMaps("secondaryStatus", secondaryStatus, "", false, onlyIfNew)
	}
	return result
}


private getSecondaryStatus(eventMaps) {
	def status = ""
	if (secondaryTileStatusSetting == "combined"){
		def waterStatus = getAttrStatusText("water", eventMaps)
		def lightStatus = getAttrStatusText("illuminance", eventMaps)
		def tempStatus = getAttrStatusText("temperature", eventMaps)		
		status = "${waterStatus} / ${tempStatus} / ${lightStatus}"
	}
	else if (status != "none") {
		status = getAttrStatusText(secondaryTileStatusSetting, eventMaps)
	}
	return status
}

private getAttrStatusText(attrName, eventMaps=null) {
	def status = (eventMaps?.find { it.name == attrName }?.descriptionText)
	if (status) {
		return status
	}
	else {
		return getDescriptionText(device.currentState(attrName))
	}	
}

private getDescriptionText(data) {
	switch (data?.name ?: "") {
		case "water":
			return "${data.value}" == "wet" ? "Wet" : "Dry"
			break
		case "temperature":
			return "${data.value}°${data.unit}"					
			break
		case "illuminance":
			return "${data.value} LUX"
			break
		default:
			return ""
	}	
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


def refresh() {	
	if (state.pendingRefresh) {	
		state.refreshAll = true
		logForceWakeupMessage "All configuration settings and sensor data will be requested from the device the next time it wakes up."
	}
	else {
		state.pendingRefresh = true		
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
		sendEvent(createEventMap("pendingChanges", -2, "", false))
	}
	return []
}


private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by using a paper clip to push the button on the bottom of the device."
}

private createEventMaps(eventName, newVal, unit, displayed, onlyIfNew) {
	def result = []
	if (!onlyIfNew || getAttrValue(eventName) != newVal) {
		def eventMap = createEventMap(eventName, newVal, unit, displayed)
		def desc = getDescriptionText(eventMap)
		if (desc) {
			eventMap.descriptionText = desc
		}
		result << eventMap
	}
	return result
}

private createEventMap(eventName, newVal, unit="", displayed=null) {
	def oldVal = getAttrValue(eventName)
	def isNew = "${oldVal}" != "${newVal}"
	def desc = "${eventName.capitalize()} is ${newVal}${unit}"
	
	if (displayed == null) {
		displayed = isNew
	}
	
	if (displayed) {
		logDebug "${desc}"
	}
	else {
		logTrace "${desc}"
	}
	
	return [
		name: eventName, 
		value: newVal, 
		displayed: displayed,
		isStateChange: true,
		unit: unit,
		descriptionText: "${device.displayName}: ${desc}"
	]
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

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensorType))
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


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	def decVal = "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal	
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private roundVal(val, places) {
	if ("${val}".isNumber()) {
		def dblVal = "${val}".toDouble()
		if (places) {
			return dblVal.round(places)
		}
		else {
			dblVal.round()
		}		
	}
	else {
		return val
	}
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	def localDt = "$dt"
	try {
		if (timeZoneId) {
			localDt = dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
	}
	catch (e) {
		// Hub TimeZone probably not set.
	}
	return localDt
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