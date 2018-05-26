/**
 *  Zooz Motion Sensor ZSE18 v1.0
 *  (Model: ZSE18)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *    0.1 (05/26/2018)
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
		name: "Zooz Motion Sensor ZSE18", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		
		fingerprint mfr:"027A", prod:"0301", model:"0012", deviceJoinName: "Zooz Motion Sensor ZSE18"
	}
	
	simulator { }
	
	preferences {
		input "shockAlarmEnabled", "enum",
			title: "Enable/Disable Shock Alarm:",
			defaultValue: shockAlarmEnabledSetting,
			required: false,
			displayDuringSetup: true,
			options: shockAlarmEnabledOptions
		input "motionClearedDelay", "enum",
			title: "Motion Cleared Delay:",
			defaultValue: motionClearedDelaySetting,
			required: false,
			displayDuringSetup: true,
			options: motionClearedDelayOptions
		input "motionSensitivity", "enum",
			title: "Motion Detection Sensitivity:",
			defaultValue: motionSensitivitySetting,
			required: false,
			displayDuringSetup: true,
			options: motionSensitivityOptions
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {			
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"${resourcesUrl}motion-inactive.png", backgroundColor:"#ffffff"
				attributeState "active", 
					label:'MOTION', 
					icon:"${resourcesUrl}motion-active.png", backgroundColor:"#00a0dc"
			}			
			tileAttribute ("device.acceleration", key: "SECONDARY_CONTROL") {
				attributeState "inactive", label:'NO VIBRATION'
				attributeState "active", label:'VIBRATION'
			}
		}

		standardTile("motion", "device.motion", decoration: "flat", width: 2, height: 2) {			
			state "Inactive", label:'INACTIVE', icon: "${resourcesUrl}motion-inactive.png"
			state "active", label:'ACTIVE', icon: "${resourcesUrl}motion-active.png"
		}		
		
		standardTile("acceleration", "device.acceleration", decoration: "flat", width: 2, height: 2) {			
			state "Inactive", label:'INACTIVE', icon: "${resourcesUrl}acceleration-inactive.png"
			state "active", label:'ACTIVE', icon: "${resourcesUrl}acceleration-active.png"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon:"${resourcesUrl}refresh.png"
		}
		
		valueTile("battery", "device.battery", width: 2, height: 2){
			state "default", label:'${currentValue}%', icon: "${resourcesUrl}battery.png"
			state "1", label:'${currentValue}%', icon: "${resourcesUrl}battery-low.png"
		}
		
		main "mainTile"
		details(["mainTile", "motion", "motionMulti", "acceleration", "battery", "refresh"])
	}
}

private getResourcesUrl() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/"
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."		
	}		
}

def configure() {
	logTrace "configure()"
	def cmds = []
	
	if (!state.isConfigured) {
		logTrace "Waiting 2 second because this is the first time being configured"
		sendEvent(getEventMap("motion", "inactive", false))
		sendEvent(getEventMap("acceleration", "inactive", false))
		cmds << "delay 2000"		
	}
	
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.size, it.value)	
	}	
	if (cmds) {
		logDebug "Sending configuration to device."
	}
	
	if (!isDuplicateCommand(state.lastBattery, (60 * 60 * 1000))) {
		cmds << batteryGetCmd()
	}
	
	if (!device.currentValue("checkInterval")) {
		// ST sets default wake up interval to 4 hours so make it report offline if it goes 8 hours 5 minutes without checking in.
		def wakeUpIntervalSecs = (4 * 60 * 60)		
		cmds << wakeUpIntervalSetCmd(wakeUpIntervalSecs)
		
		def checkInInterval = ((wakeUpIntervalSecs * 2) + (5 * 60)) 
		sendEvent(name: "checkInterval", value: checkInInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
		
	return cmds ? delayBetween(cmds, 1000) : []
}

private updateConfigVal(paramNum, paramSize, val) {
	def result = []
	def configVal = state["configVal${paramNum}"]
	
	if ("${configVal}" != "${val}") {
		result << configSetCmd(paramNum, paramSize, val)
		result << configGetCmd(paramNum)
	}		
	return result
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.lastBattery = null
	configData.each {
		state."configVal${it.paramNum}" = null
	}
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by holding the z-button for 5 seconds."
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
	
	sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up"
	
	def cmds = []	
	cmds += configure()
	if (cmds) {
		cmds << "delay 2000"
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

// Stores the configuration values so that it only updates them when they've changed or a refresh was requested.
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport ${cmd}"
	
	def name = configData.find { it.paramNum == cmd.parameterNumber }?.name
	if (name) {	
		def val = hexBytesToInt(cmd.configurationValue, cmd.size)
		
		if (name == "Shock Alarm Enabled" && !shockAlarmEnabledSetting) {
			sendEvent(getEventMap("acceleration", "inactive", false))
		}
	
		logDebug "${name} = ${val}"	
		state."configVal${cmd.parameterNumber}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	
	state.isConfigured = true
	return []
}

// Creates motion/acceleration events.
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	def result = []	
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				if (cmd.eventParameter[0] == 3 || cmd.eventParameter[0] == 9) {
					logDebug "Acceleration Inactive"				
					result << createEvent(getEventMap("acceleration", "inactive"))
				}
				else {
					logDebug "Motion Inactive"				
					result << createEvent(getEventMap("motion", "inactive"))
				}		
				break
			case { it == 3 || it == 9}:
				logDebug "Acceleration Active"
				result << createEvent(getEventMap("acceleration", "active"))
				break
			case 8:
				logDebug "Motion Active"
				result << createEvent(getEventMap("motion", "active"))
				break
			default:
				logDebug "Unknown Notification Event: ${cmd}"
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	// Ignoring event because motion events are being handled by notification report.
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
		isStateChange: isStateChange
	]
	if (desc) {
		eventMap.descriptionText = desc
	}
	if (unit) {
		eventMap.unit = unit
	}		
	return eventMap
}


private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpIntervalSetCmd(seconds) {	
	log.warn "wakeUpIntervalSetCmd($seconds)"
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private batteryGetCmd() {	
	return secureCmd(zwave.batteryV1.batteryGet())
}

private configGetCmd(paramNum) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNum))
}

private configSetCmd(paramNum, size, val) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: size, configurationValue: intToHexBytes(val, size)))
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
		0x55: 1,  // TransportServices (2)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (5)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd (3)
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
		// 0x6C ???
		// Security S2
		// Supervision
	]
}


// Settings
private getShockAlarmEnabledSetting() {
	return settings?.shockAlarmEnabled != null ? safeToInt(settings?.shockAlarmEnabled) : 1
}

private getMotionSensitivitySetting() {
	return settings?.motionSensitivity != null	? safeToInt(settings?.motionSensitivity) : 5
}

private getMotionClearedDelaySetting() {
	return settings?.motionClearedDelay != null ? safeToInt(settings?.motionClearedDelay) : 30
}


// Configuration Parameters
private getConfigData() {
	return [
		[paramNum: 12, name: "Motion Sensitivity", value: motionSensitivitySetting, size: 1],
		[paramNum: 18, name: "Motion Cleared Delay", value: motionClearedDelaySetting, size: 2],
		[paramNum: 17, name: "Shock Alarm Enabled", value: shockAlarmEnabledSetting, size: 1]
		// paramNum: 14 = Send Basic Set (0:disabled, 1:enabled)
		// paramNum: 15 - Basic Set Value (0:255, 1: 0)
		// paramNum: 32 - Low Battery Level (10-50%)
	]	
}

private getMotionSensitivityOptions() {	
	def options = [
		["0": "Disabled"],
		["1": "1 (Least Sensitive)"]
	]
	
	(2..7).each {
		options << ["${it}": "${it}"]
	}
	
	options << ["8": "8 (Most Sensitive)"]	
	return options
}

private getMotionClearedDelayOptions() {
	[
		["0": "0 Seconds"],
		["1": "1 Seconds"],
		["2": "2 Seconds"],
		["3": "3 Seconds"],
		["4": "4 Seconds"],
		["5": "5 Seconds"],
		["10": "10 Seconds"],		
		["15": "15 Seconds"],
		["30": "30 Seconds"],
		["45":"45 Seconds"],
		["60":"1 Minute"],  
		["120":"2 Minutes"], 
		["180":"3 Minutes"], 
		["240":"4 Minutes"], 
		["300":"5 Minutes"], 
		["420":"7 Minutes"], 
		["600":"10 Minutes"],
		["900":"15 Minutes"],
		["1800":"30 Minutes"],
		["3600":"60 Minutes"]
	]
}

private getShockAlarmEnabledOptions() {
	[
		["0": "Disabled"],
		["1": "Enabled"]
	]
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
		return [(byte) ((val >> 8) & 0xff),(byte) (val & 0xff)]
	}
	else {
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
