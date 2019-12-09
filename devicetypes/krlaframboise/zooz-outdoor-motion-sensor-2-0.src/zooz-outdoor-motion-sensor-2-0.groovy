/**
 *  Zooz Outdoor Motion Sensor 2.0  (FIRMWARE >= 2.0)
 *    (Model: ZSE29)  
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  
 *   
 *
 *  Changelog:
 *
 *    2.0 (12/08/2019)
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
	definition (name:"Zooz Outdoor Motion Sensor 2.0", namespace:"krlaframboise", author: "Kevin LaFramboise", ocfDeviceType: "x.com.st.d.sensor.motion", vid: "generic-motion-5") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		attribute "firmwareVersion", "string"
		attribute "firmwareSupported", "string"
		
		fingerprint mfr: "027A", prod: "0001", model: "0005", deviceJoinName: "Zooz Outdoor Motion Sensor 2.0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc")
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc")
			}
			tileAttribute ("device.tamper", key: "SECONDARY_CONTROL") {
				attributeState("clear", label:'')
				attributeState("detected", label:'TAMPERING')
			}
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label: '${currentValue} lux', unit: ""
		}				
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}
		standardTile("refresh", "command.refresh", width: 2, height: 2) {
			state "default", label:"Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:2, height: 2) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		valueTile("firmwareSupported", "device.firmwareSupported", decoration:"flat", width:4, height: 2) {
			state "yes", label:''
			state "no", label:'This DTH only supports firmware 2.0 and above'
		}
		main("mainTile")
		details(["mainTile", "illuminance", "battery", "refresh", "firmwareVersion", "firmwareSupported"])
	}
	
	preferences {
		
		// getParamInput(group2BasicSetParam)
		// getParamInput(motionEnabledParam)
		getParamInput(motionSensitivityParam)
		getParamInput(motionClearedDelayParam)
		getParamInput(luxLevelTriggerParam)		
		getParamInput(luxReportingParam)
				
		getOptionsInput("checkInInterval", "Check In Interval:", checkInIntervalSetting, checkInIntervalOptions)
		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
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

private getOptionsInput(name, title, defaultValue, options) {
	input "${name}", "enum",
		title: "${title}:",
		required: false,
		defaultValue: "${defaultValue}",
		options: setDefaultOption(options, defaultValue)
}


def installed() {
	state.refreshAll = true
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		if (state.checkInInterval != checkInIntervalSetting) {
			refresh()
		}		
	}
}


def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!state.checkInInterval) {
		// First time configuring so give it time for inclusion to finish.
		cmds << "delay 2000"			
	}
	
	if (state.refreshAll || state.checkInInterval != checkInIntervalSetting) {
		cmds << wakeUpIntervalSetCmd(checkInIntervalSetting)
		cmds << wakeUpIntervalGetCmd()
	}
	
	if (state.refreshAll || !device.currentValue("illuminance")) {
		cmds << sensorMultilevelGetCmd(lightSensorType)
	}
	
	if (state.refreshAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
		
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	configParams.each { param ->		
		def storedVal = getParamStoredValue(param.num)		
		if (state.refreshAll || "${storedVal}" != "${param.value}") {			
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param)
			cmds << configGetCmd(param)
		}
	}
	
	state.refreshAll = false
	return cmds ? delayBetween(cmds, 1000) : []
}

private firmwareSupportsParam(param) {
	return (!param.minFirmware || !firmwareVersion || firmwareVersion >= param.minFirmware)
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}


def refresh() {	
	log.warn "The settings will be sent to the device the next time it wakes up.  You can force the device to wake up immediately by removing the battery for a few seconds and then putting it back in."
	state.refreshAll = true
	return []
}


private wakeUpIntervalSetCmd(seconds) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 1, sensorType: sensorType))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))	
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
		0x31: 5,	// Sensor Multilevel (v7)
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 2,	// Configuration
		0x71: 3,	// Notification (4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x80: 1,	// Battery
		0x84: 2,  // WakeUp
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {	
	def result = []	
	try {
		sendLastCheckInEvent()
		
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unknown Description: $desc"
		}
	}
	catch (e) {
		log.error "$e"
	}
	return result
}

private sendLastCheckInEvent() {	
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time		
		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
}

private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}	
	}
	catch (e) {
		return "$dt"
	}
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


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	
	state.checkInInterval = cmd.seconds
	
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 2) + (5 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logDebug "Device Woke Up"
	
	def cmds = configure()		
	if (cmds) {
		cmds << "delay 1000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	
	return response(cmds)	
}

private canReportBattery() {
	def reportEveryMS = (12 * 60 * 60 * 1000) // 12 Hours		
	return (state.refreshAll || !state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	
	if (val > 100) {
		val = 100
	}	
	
	state.lastBatteryReport = new Date().time	
	
	logDebug "Battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%")
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	def supported = (safeToDec(version) >= 2.0 ? "yes" : "no")
	
	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)		
		sendEvent(name:"firmwareSupported", value: supported, displayed:false)
	}
	
	if (supported == "no") {
		log.warn "This DTH was written for the Zooz Outdoor Motion Sensor 2.0, but your device has firmware ${version} so you need to use the DTH for the old model:https://community.smartthings.com/t/release-zooz-outdoor-motion-sensor-zse29/142893"
	}
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "BasicSet: $cmd"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	def val = cmd.scaledSensorValue
	switch (cmd.sensorType) {
		case lightSensorType:
			sendEvent(name:"illuminance", value:cmd.scaledSensorValue, unit:"lux", isStateChange: true)			
			break
		default:
			logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 3:
				sendTamperEventMap("detected")
				break
			case 8:
				sendMotionEventMap("active")				
				break
			case 0:
				if (cmd.eventParametersLength && cmd.eventParameter[0] == 8) { 
					sendMotionEventMap("inactive")
				}
				else {
					sendTamperEventMap("clear")
				}
				break
			default:
				logTrace "Unknown Notification Event: ${cmd.event}"
		}		
	}	
	else if (cmd.notificationType == 8 && cmd.event == 1) {
		logDebug "Device Powered On"
		def cmds = configure()
		return cmds ? response(cmds) : []
	}
	else {
		logTrace "Unknown Notification Type: ${cmd.notificationType}"
	}	
	return []
}

private sendTamperEventMap(val) {
	logDebug "Tamper is ${val}"
	sendEvent(name:"tamper", value:val, displayed:(val == "detected"), isStateChange: true)
}

private sendMotionEventMap(val) {
	logDebug "Motion is ${val}"
	sendEvent(name:"motion", value:val, isStateChange: true)
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}		
	return []
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}


private getCheckInIntervalSetting() {
	return safeToInt(settings?.checkInInterval, 14400)
}


// Configuration Parameters
private getConfigParams() {
	[
		// group2BasicSetParam,
		motionEnabledParam,
		motionSensitivityParam,
		luxLevelTriggerParam,
		motionClearedDelayParam,
		luxReportingParam
	]
}

private getGroup2BasicSetParam() {
	return getParam(1, "Group 2 Basic Set Value", 1, 99, [0:"Off", 25:"25%", 50:"50%", 75:"75%", 99:"100%"])
}

private getMotionEnabledParam() {	
	return getParam(2, "Motion Enabled", 1, 1, [0:"Disabled", 1:"Enabled"])
}

private getMotionSensitivityParam() {
	return getParam(3, "Motion Sensitivity", 1, 10, motionSensitivityOptions)
}

private getLuxLevelTriggerParam() {
	return getParam(4, "Lux Level Motion Trigger", 2, 0, luxLevelTriggerOptions)
}

private getMotionClearedDelayParam() {
	return getParam(5, "Motion Cleared Delay", 2, 0, motionClearedDelayOptions)
}

private getLuxReportingParam() {
	return getParam(6, "Lux Reporting Frequency", 2, 30, luxReportingOptions)
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


// Setting Options

private getMotionSensitivityOptions() {
	def options = [1:"Least Sensitive"]
		
	(2..9).each {
		options["${it}"] = "${it}"
	}	
	options["10"] = "Most Sensitive"
	return options	
}

private getLuxLevelTriggerOptions() {
	def options = [
		0:"Set Manually by Lux Knob", 
		1:"Ignore Lux and Always Report Motion",
		10:"10 lux"
	]
	(1..36).each {
		options["${it * 25}"] = "${it * 25} lux"
	}	
	return options
}

private getMotionClearedDelayOptions() {
	def options = [0:"Set Manually on Knob"]
	
	(5..30).each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["45"] = "45 Seconds"
	options["60"] = "1 Minute"
	options["75"] = "1 Minute 15 Seconds"
	options["90"] = "1 Minute 30 Seconds"
	options["105"] = "1 Minute 45 Seconds"
	options["120"] = "2 Minutes"
	options["150"] = "2 Minutes 30 Seconds"
	
	[180,240,300,360,420,480,540,600,660,720].each {
		options["${it}"] = "${it / 60} Minutes"
	}	
	return options	
}

private getLuxReportingOptions() {
	def options = [1:"1 Minute"]
	
	(2..15).each {
		options["${it}"] = "${it} Minutes"
	}
	
	options["30"] = "30 Minutes"
	options["45"] = "45 Minutes"
	options["60"] = "1 Hour"
	
	(2..23).each {
		options["${it * 60}"] = "${it} Hours"
	}
	
	options["1440"] = "1 Day"	
	
	return options	
}

private getCheckInIntervalOptions() {
	[
		600: "10 Minutes",
		1800: "30 Minutes",
		3600: "1 Hour",
		7200: "2 Hours",
		14400: "4 Hours",
		28800: "8 Hours",
		43200: "12 Hours",
		86400: "1 Day"
	]
}


private getLightSensorType() { return 3 }


private getFirmwareVersion() {
	return safeToDec(device?.currentValue("firmwareVersion"))
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
	if (settings?.debugOutput	!= false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}