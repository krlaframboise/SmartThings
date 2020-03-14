/**
 *  HomeSeer Flex Sensor v1.0.1
 *  (Model: HS-FS100+)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-homeseer-flex-sensor/157045
 *
 *  Changelog:
 *
 *    1.0.1 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.0 (03/09/2019)
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
		name: "HomeSeer Flex Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.sensor.multifunction",
		vid: "generic-motion"
		// ocfDeviceType: "x.com.st.d.sensor.moisture",
		// vid:"generic-leak"
	) {
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Water Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Button"
		capability "Refresh"
		capability "Tone"
		capability "Health Check"
				
		attribute "status", "string"
		attribute "batteryStatus", "string"
		attribute "lastCheckIn", "string"
		attribute "syncStatus", "string"
		
		fingerprint mfr:"000C", prod:"0202", model:"0001"
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "No Light", 
					label:'${currentValue}', 
					icon: "st.illuminance.illuminance.dark",
					backgroundColor:"#ffffff"
				attributeState "Color Change", 
					label:'${currentValue}', 
					icon:"st.illuminance.illuminance.light", 
					backgroundColor:"#9ffda1"
				attributeState "Light", 
					label:'${currentValue}', 
					icon:"st.illuminance.illuminance.bright", 
					backgroundColor:"#33ff3b"
				attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"               
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#00a0dc"
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
		
		valueTile("battery", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}% Battery'
			state "usb", label:'USB'
		}
		
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		
		standardTile("configure", "device.configure", width: 2, height: 2, decoration: "flat") {
			state "default", label:'Sync', action: "configuration.configure", icon:"st.secondary.tools"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: 'Refresh', action: "refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("beep", "tone.beep", width: 2, height: 2, decoration: "flat") {
			state "default", label: 'Beep\n(usb only)', action: "tone.beep", icon:"st.Entertainment.entertainment2"
		}
				
		main "mainTile"
		details(["mainTile", "temperature", "syncStatus", "battery", "refresh", "configure", "beep" ])
	}
	
	simulator { }
	
	preferences {
		getParamInput(lightSensitivityParam)
		getParamInput(waterBuzzerFrequencyParam)
		getParamInput(tempReportingIntervalParam)		
		getParamInput(notificationBuzzerParam)
		getParamInput(lightDetectionDelayParam)
		
		input "sensorType", "enum",
			title: "Attached Sensor Type:",
			required: false,
			defaultValue: "Light",
			options: ["Light":"Light", "Water":"Water"]
		
		input "events", "paragraph", 
			title:"Button Pushed Events are Created for Light Changes",
			description: "Button 1: Light Not Detected\nButton 2: Light Detected\nButton 3: Color Change Detected"
	}
}

private getParamInput(param) {
	input "${param.pref}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		options: param.options
}

private getSensorTypeSetting() {
	return settings?.sensorType ?: "Light"
}


def installed() {
	logDebug "installed()..."
	state.refreshTemp = true
	state.refreshStatus = true
	initialize()
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logDebug "updated()"
		
		if ("${state.sensorType}" != "${sensorTypeSetting}") {			
			if (state.sensorType) {
				handleSensorTypeChange()
			}
			state.sensorType = sensorTypeSetting
		}
		
		initialize()

		refreshSyncStatus()
		
		def cmds = configure()
		return cmds ? response(cmds) : []
	}		
}

private handleSensorTypeChange() {
	def oldStatus = getAttrVal("status") 
	if ("${sensorTypeSetting}" == "Light") {
		handleLightEvent(("${oldStatus}" == "wet") ? 1 : 0)
		if (getAttrVal("water") == "wet") {
			sendEvent(getEventMap("water", "dry", false))
		}
	}
	else {
		handleWaterEvent(("${oldStatus}" == "No Light") ? 0 : 1)		
	}
}


def configure() {
	logDebug "configure()..."
	
	initialize()

	def cmds = []
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	if (state.refreshTemp) {
		cmds << sensorMultilevelGetCmd(tempSensorType)
	}
	
	if (state.refreshStatus) {
		cmds << sensorBinaryGetCmd()
	}
	
	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if ("${storedVal}" != "${param.value}") {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}
	
	if (cmds) {
		if (batteryPoweredInclusion()) {
			logForceWakeupMessage("sync", "Configuration changes will be sent to the device the next time it wakes up.")
		}
	}
	
	refreshSyncStatus()
	return cmds ? delayBetween(cmds, 500) : []
}


private initialize() {
	if (!getAttrVal("water")) {
		sendEvent(getEventMap("water", "dry", false))
	}
	if (!getAttrVal("numberOfButtons")) {
		sendEvent(getEventMap("numberOfButtons", 3, false))
	}
	if (getAttrVal("battery") == null) {
		sendEvent(getEventMap("battery", 100, false))
		sendEvent(getEventMap("batteryStatus", "usb", false))
	}
	if (!getAttrVal("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 12) + (60 * 5)), displayed: false, data:[protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}


def ping() {
	logDebug "ping()"	
	
	if (!batteryPoweredInclusion()) {
		return [basicGetCmd()]
	}
}


def beep() {
	logDebug "beep()..."
	if (batteryPoweredInclusion()) {
		log.warn "Beep requires USB power."
	}
	return [ switchBinarySetCmd(0xFF) ]
}


def refresh() {	
	logDebug "refresh()..."
	
	runIn(2, refreshSyncStatus)
	
	state.lastBattery = null
	state.refreshTemp = true		
	state.refreshStatus = true
	
	if (batteryPoweredInclusion()) {
		logForceWakeupMessage("refresh", "The sensor data will be refreshed the next time the device wakes up.")
	}
	
	def cmds = [
		sensorMultilevelGetCmd(tempSensorType),
		sensorBinaryGetCmd()
	]
	
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	return delayBetween(cmds, 500)	
}

private logForceWakeupMessage(action, msg) {
	log.warn "${msg}  To ${action} the data immediately, push the physical button on the device and then immediately tap the ${action} tile.  This is not necessary if the device is powered by USB."
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
		
		sendEvent(getEventMap("lastCheckIn", convertToLocalTimeString(new Date()), false))
	}
	catch (e) {
		log.error "$e"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	logDebug "Device Woke Up"
	
	def cmds = configure()
			
	if (cmds) {
		cmds << "delay 1000"
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
	
	sendEvent(getEventMap("battery", val, true, "%"))
	sendEvent(getEventMap("batteryStatus", val, false))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	state.refreshTemp = false
	
	if (cmd.sensorType == tempSensorType) {
		def unit = cmd.scale ? "F" : "C"
		def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
		sendEvent(getEventMap("temperature", temp, true, getTemperatureScale()))
	}
	else {
		logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	
	if (state.refreshStatus) {
		state.refreshStatus = false
		
		if ("${sensorTypeSetting}" == "Light") {
			handleLightEvent(cmd.sensorValue ? 1 : 0)			
		}
		else {
			handleWaterEvent(cmd.sensorValue)
		}
	}
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	state.refreshStatus = false
	
	switch (cmd.notificationType) {
		case 0x05:
			handleWaterEvent(cmd.event)
			break
		case 0x14:
			handleLightEvent(cmd.event)		
			break
		default:
			logDebug "Unknown notificationType: ${cmd.notificationType}"
	}
	
	return []
}

private handleWaterEvent(event) {
	sendEvent(getEventMap("status", event ? "wet" : "dry", true))
	sendEvent(getEventMap("water", event ? "wet" : "dry", false))
}

private handleLightEvent(event) {
	switch (event) {
		case 0:
			sendLightEvents("No Light", 1)
			break
		case 1:
			sendLightEvents("Light", 2)
			break
		case 2:
			sendLightEvents("Color Change", 3)
			break
		default:
			logDebug "Sensor is ${event}"
	}
}

private sendLightEvents(status, button) {
	sendEvent(getEventMap("status", status, true))
	
	def map = getEventMap("button", "pushed", false)
	map.data = [buttonNumber: button]
	map.isStateChange = true
	sendEvent(map)	
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	updateSyncingStatus()
	runIn(2, refreshSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = hexBytesToInt(cmd.configurationValue, param.size)
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}		
	return []
}

private updateSyncingStatus() {
	if (getAttrVal("syncStatus") != "Syncing...") {
		sendEvent(getEventMap("syncStatus", "Syncing...", false))
	}
}

def refreshSyncStatus() {
	def changes = pendingChanges	
	sendEvent(getEventMap("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false))
	
	if (!pendingChanges && batteryPoweredInclusion()) {
		sendCommands([wakeUpNoMoreInfoCmd()])
	}
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, 100)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Ignored Command: $cmd"
	return []
}


private getEventMap(name, value, displayed=null, unit=null) {	
	def isStateChange = ("${getAttrVal(name)}" != "${value}")
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange,
		descriptionText: "${device.displayName} ${name} is ${value}"
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
	return zwave.wakeUpV1.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {	
	return zwave.batteryV1.batteryGet().format()
}

private sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType ? 0 : 1)
	return zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType).format()
}

private sensorBinaryGetCmd() {
	return zwave.sensorBinaryV2.sensorBinaryGet().format()
}

private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private configGetCmd(param) {
	return zwave.configurationV1.configurationGet(parameterNumber: param.num).format()
}

private configSetCmd(param) {
	return zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: intToHexBytes(param.value, param.size)).format()
}


private getCommandClassVersions() {
	[
		0x25: 1,	// SwitchBinary
		0x30: 2,	// SensorBinary
		0x31: 5,	// SensorMultilevel (7)
		0x55: 1,	// Transport Service (V2)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (4)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x80: 1,  // Battery
		0x84: 1,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version
		0x9F: 1	// Security S2
	]
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


// Configuration Parameters
private getConfigParams() {
	[
		lightSensitivityParam,
		waterBuzzerFrequencyParam,
		tempReportingIntervalParam,		
		notificationBuzzerParam,
		lightDetectionDelayParam
	]
}

private getLightSensitivityParam() {
	return getParam(1, "Light Sensitivity (with light cable)", 1, 0, [0:"High", 1:"Medium", 2:"Low"], "lightSensitivity")	
}

private getWaterBuzzerFrequencyParam() {
	return getParam(2, "Water Detection Buzzer Frequency (with water cable)", 1, 0, [1:"Every 5 Minutes", 0:"Every 10 Minutes", 2:"Every 30 Minutes"], "waterBuzzerFrequency")	
}

private getTempReportingIntervalParam() {
	return getParam(3, "Temperature Reporting Interval (when usb powered)", 1, 60, [30:"30 Seconds", 60:"1 Minute", 180:"3 Minutes", 240:"4 Minutes"], "tempInterval")
}

private getNotificationBuzzerParam() {
	return getParam(4, "Notification Buzzer", 1, 1, [0:"Disabled", 1:"Enabled"], "buzzerEnabled")
}

private getLightDetectionDelayParam() {
	def options = [:]
	(0..20).each {
		options["${it}"] = (it == 1) ? "${it} Second" : "${it} Seconds"
	}
	
	return getParam(5, "Light Detection Delay (detect blinking)", 1, 0, options, "lightDetectionDelay")	
}

private getParam(num, name, size, defaultVal, options, pref) {
	def val = safeToInt((settings ? settings["${pref}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val, pref: pref]
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


private canReportBattery() {
	return (batteryPoweredInclusion() && !isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000)))
}

private batteryPoweredInclusion() {
	return zwaveInfo?.cc?.find { "${it}" == "80" } ? true : false
}

private getTempSensorType() { 
	return 1
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
		if (val > 32767) val = (val - 65536)
		return [(byte) ((val >> 8) & 0xff),(byte) (val & 0xff)]
	}
	else {
		if (val > 127) val = (val - 256)
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

private getAttrVal(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logTrace "$ex"
		return null
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}


private logDebug(msg) {
	log.debug "$msg"
}

private logTrace(msg) {
	// log.trace "$msg"
}