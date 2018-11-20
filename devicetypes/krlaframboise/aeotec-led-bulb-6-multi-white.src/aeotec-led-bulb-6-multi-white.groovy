/**
 *  Aeotec LED Bulb 6 Multi-White v1.0
 *  (Model: ZWA001-A)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *
 *  Changelog:
 *
 *    1.0 (11/19/2018)
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
	definition (name: "Aeotec LED Bulb 6 Multi-White", namespace: "krlaframboise", author: "Kevin LaFramboise", vid: "generic-rgbw-color-bulb") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch Level"
		capability "Switch"
		capability "Color Temperature"
		capability "Refresh"		
		capability "Configuration"
		capability "Health Check"
		
		attribute "lastCheckIn", "string"
		attribute "colorTemperatureValue", "number"
		
		fingerprint mfr:"0371", model: "0001", prod:"0103", deviceJoinName:"Aeotec LED Bulb 6 Multi-White"
	}
	
	simulator {	}
	
	tiles(scale:2) {
			multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
     }
	
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height:2, width:2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		valueTile("colorTempVal", "device.colorTemperatureValue", decoration: "flat", height:2, width:2) {
			state "default", label:'${currentValue}°K'
		}
				
		controlTile("colorTempControl", "device.colorTemperature", "slider", height: 2, width: 2, inactiveLabel: false) {
			state "colorTemperature", label: "Color Temp", action:"setColorTemperature"
		}
		
		main(["switch"])
		details(["switch", "refresh", "colorTempVal", "colorTempControl"])
	}
	
	preferences {		
		input "toggleDuration", "enum",
			title: "Transition Speed:",
			defaultValue: "1",
			required: false,
			options: [
				["0": "Instant"],
				["1": "Fast [DEFAULT]"],
				["2": "Medium"],
				["3": "Slow"]
			]
		
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}

private getToggleDurationSetting() {
	return safeToInt(settings?.toggleDuration, 1)
}


def installed() {
	logTrace "installed()..."
	initializeCheckIn()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()..."
		
		initializeCheckIn()		
	}
}

private initializeCheckIn() {
	def checkInterval = (6 * 60 * 60) + (5 * 60)
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}

def ping() {
	logTrace "ping()"
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
	}	
	
	healthPoll()
}

def healthPoll() {
	logTrace "healthPoll()"	
	sendHubCommand([new physicalgraph.device.HubAction(versionGetCmd())])
}


def configure() {
	def cmds = [
		configSetCmd(80, 1, 1) // Enable Basic Reports
	]		
	
	cmds += setColorTemperature(4500)	
	cmds += refresh()
	
	return delayBetween(cmds, 500)
}


def on() {
	logDebug "on()..."	
	def level = (device.currentValue("level") ?: 99)
	return [switchMultilevelSetCmd(level, toggleDurationSetting)]
}

def off() {
	logDebug "off()..."
	return [switchMultilevelSetCmd(0, toggleDurationSetting)]
}


def setLevel(level) {
	return setLevel(level, toggleDurationSetting)
}

def setLevel(level, duration) {
	logDebug "setLevel($level, $duration)..."
	if (duration > 15) {
		duration = 15
	}
	return [switchMultilevelSetCmd(level, duration)]
}


def refresh() {
	logDebug "refresh()..."	
	return [basicGetCmd()]
} 
  

def setColorTemperature(temperature) {
	logDebug "setColorTemperature($temperature)..."
	
	def percentage
	
	temperature = safeToInt(temperature, 4500) 
	
	if (temperature >= 0 && temperature <= 100) {			
		if (!temperature) temperature = 1
		percentage = temperature
		// Convert percentage into Kalvin
		temperature = (Math.round((temperature / 100) * 38) + 27) * 100
	}
	
	if (temperature > 6500) temperature = 6500
	if (temperature < 2700) temperature = 2700
	
	if (!percentage) {
		percentage = (((temperature / 100) - 27) / 38) * 100
		if (percentage < 0) percentage = 1
	}
	
	sendEvent(getEventMap("colorTemperature", percentage, "%"))
	sendEvent(getEventMap("colorTemperatureValue", temperature, "K"))
		
	def warmVal = (temperature < 5000) ? 255 : 0
	def coldVal = warmVal ? 0 : 255
	def paramNum = warmVal ? warmWhiteParam : coldWhiteParam
	
	return delayBetween([
		configSetCmd(paramNum, 2, temperature),
		switchColorSetCmd(warmVal, coldVal)
	], 500)	
}


private basicSetCmd(val) {
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private switchMultilevelSetCmd(level, duration) {
	def levelVal = validateRange(level, 99, 0, 99)
	
	def durationVal = validateRange(duration, defaultDimmingDurationSetting, 0, 100)
			
	return secureCmd(zwave.switchMultilevelV2.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

private switchColorSetCmd(warmWhite, coldWhite) {
	return secureCmd(zwave.switchColorV3.switchColorSet(warmWhite: warmWhite, coldWhite: coldWhite)) 
}

private configSetCmd(num, size, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: num, size: size,scaledConfigurationValue: value))
}

private configGetCmd(num) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: num))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}


def parse(description) {	
	def result = null
	
	try {
		if (description != "updated") {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result = zwaveEvent(cmd)			
			} 
			else {
				logDebug("Couldn't zwave.parse '$description'")
			}
		}
		
		if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
			sendLastCheckInEvent()
		}
	}
	catch (e) {
		log.error "Unable to parse ${description}: ${e}"
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
		0x26: 2,	// Switch Multilevel
		0x27: 1,	// All Switch
		0x2B: 1,	// Scene Activation
		0x2C: 1,	// Scene Actuator Configuration
		0x33: 1,	// Color Control
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x70: 1,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport $cmd"
	def value = cmd.value ? "on" : "off"
	
	if (device.currentValue("switch") != value) {
		sendEvent(getEventMap("switch", value))
	}
	
	if (cmd.value && device.currentValue("level") != cmd.value) {
		sendEvent(getEventMap("level", cmd.value, "%"))
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


private validateRange(val, defaultVal, lowVal, highVal) {
	val = safeToInt(val, defaultVal)
	if (val > highVal) {
			return highVal
	}
	else if (val < lowVal) {
		return lowVal
	}
	else {
		return val
	}
}

private safeToInt(val, defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	}
	else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	}
	else {
		return  defaultVal
	}
}


private getEventMap(name, value, unit=null) {
	def map = [
		name: "$name",
		value: value,
		isStateChange: true,
		descriptionText: "${name} is ${value}"
	]
	if (unit) {
		map.unit = unit
	}
	return map
}

private getWarmWhiteParam() { return 81 }
private getColdWhiteParam() { return 82 }


private sendLastCheckInEvent() {
	state.lastCheckInTime = new Date().time
	logDebug "Device Checked In"	
	sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
}

private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt?.format("MM/dd/yyyy hh:mm:ss a", TimeZone?.getTimeZone(timeZoneId))
		}
		else {
			return "${dt}"
		}	
	}
	catch (e) {
		return "${dt}"
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