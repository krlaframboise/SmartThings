/**
 *  Zooz RGBW Dimmer v1.0
 *  (Model: ZEN31)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0 (11/25/2019)
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
import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x26: 2,	// Switch Multilevel
	0x27: 1,	// All Switch
	0x2B: 1,	// Scene Activation
	0x2C: 1,	// Scene Actuator Configuration
	0x32: 3,	// Meter
	0x33: 3,	// Color Control
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

@Field static Map COLOR_COMPONENTS = [white:0, red:2, green:3, blue:4]
@Field static Map ENDPOINTS = [white:5,red:2, green:3, blue:4, hsb:3]

metadata {
	definition (name: "Zooz RGBW Dimmer", namespace: "krlaframboise", author: "Kevin LaFramboise", ocfDeviceType: "oic.d.light", vid: "generic-rgbw-color-bulb") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch Level"
		capability "Switch"
		capability "Power Meter"
		capability "Color Control"
		capability "Refresh"
		capability "Configuration"
		capability "Button"
		
		attribute "firmwareVersion", "string"
		attribute "syncStatus", "string"
		attribute "whiteSwitch", "string"
		attribute "colorSwitch", "string"
		attribute "activeProgram", "string"
		
		command "whiteOn"
		command "whiteOff"
		command "colorOn"
		command "colorOff"
		
		command "startFireplaceProgram"
		command "startStormProgram"
		command "startRainbowProgram"
		command "startPolarLightsProgram"
		command "startPoliceProgram"
		command "stopProgram"
				
		fingerprint mfr:"027A", prod:"0902", model:"2000", deviceJoinName:"Zooz RGBW Dimmer"
	}

	simulator {	}
	
	tiles(scale:2) {
			multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
     }

		standardTile("whiteSwitch", "device.whiteSwitch", width: 2, height: 2) {
			state "on", label:'white ${name}', action:"whiteOff", backgroundColor:"#00a0dc"
			state "off", label:'white ${name}', action:"whiteOn", backgroundColor:"#ffffff"
		}
		
		standardTile("colorSwitch", "device.colorSwitch", width: 2, height: 2) {
			state "on", label:'color ${name}', action:"colorOff", backgroundColor:"#00a0dc"
			state "off", label:'color ${name}', action:"colorOn", backgroundColor:"#ffffff"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh"
		}
		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		
		standardTile("configure", "device.generic", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure"
		}
		
		controlTile("rgbSelector", "device.color", "color", height: 6, width: 6, inactiveLabel: false) {
			state "color", action:"setColor"
		}
		
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:2, height: 2) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		standardTile("fireplace", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Fireplace', action: "startFireplaceProgram", backgroundColor:"#ffffff"
			state "6", label:'Fireplace', action: "stopProgram", backgroundColor:"#00a0dc"
		}
		
		standardTile("storm", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Storm', action: "startStormProgram", backgroundColor:"#ffffff"
			state "7", label:'Storm', action: "stopProgram", backgroundColor:"#00a0dc"
		}
		
		standardTile("rainbow", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Rainbow', action: "startRainbowProgram", backgroundColor:"#ffffff"
			state "8", label:'Rainbow', action: "stopProgram", backgroundColor:"#00a0dc"
		}
		
		standardTile("polarLights", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Polar Lights', action: "startPolarLightsProgram", backgroundColor:"#ffffff"
			state "9", label:'Polar Lights', action: "stopProgram", backgroundColor:"#00a0dc"
		}
		
		standardTile("police", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Police', action: "startPoliceProgram", backgroundColor:"#ffffff"
			state "10", label:'Police', action: "stopProgram", backgroundColor:"#00a0dc"
		}		

		main(["switch"])
		details(["switch", "power", "colorSwitch", "whiteSwitch", "refresh", "syncStatus", "configure", "stop", "fireplace", "storm", "rainbow", "polarLights", "police", "firmwareVersion", "rgbSelector"])
	}
	
	preferences {		
		getBoolInput("createColorSwitch", "Create Child On/Off Switch for Color?", false)
		getBoolInput("createWhiteSwitch", "Create Child On/Off Switch for White?", false)
	
		getParamInput(powerRecoveryParam)
		getParamInput(dimmerRampRateLocalParam)
		getParamInput(dimmerRampRateRemoteParam)
		
		// getParamInput(powerReportingThresholdParam)
		getParamInput(powerReportingFrequencyParam)
		getParamInput(energyReportingThresholdParam)
		getParamInput(energyReportingFrequencyParam)
		// getParamInput(analogVoltageReportingThresholdParam)
		// getParamInput(analogReportingFrequencyParam)
		
		getParamInput(switchModeParam)
		
		getParamInput(input1TypeParam)
		getParamInput(input2TypeParam)
		getParamInput(input3TypeParam)
		getParamInput(input4TypeParam)
		
		getParamInput(input1SceneParam)
		getParamInput(input2SceneParam)
		getParamInput(input3SceneParam)
		getParamInput(input4SceneParam)
		
		// getParamInput(activeReportsParam)
		// getParamInput(singleClickOnFrameParam)
		// getParamInput(singleClickOffFrameParam)
		// getParamInput(doubleClickOffFrameParam)
		// getParamInput(presetProgramsParam)
		
		getBoolInput("debugOutput", "Enable Debug Logging?", true)
	}
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}", 
		defaultValue: defaultVal, 
		required: false	
}

private getParamInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,		
		displayDuringSetup: true,
		defaultValue: "${param.value}",
		options: param.options
}


def installed() {
	logDebug "installed()..."
	
	initialize()
}


def updated() {	
	logDebug "updated()..."
	
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {	
		state.lastUpdated = new Date().time
		
		initialize()
		
		addRemoveChildSwitches()
		
		if (state.isDeviceInitialized) {
			runIn(5, updateSyncStatus)
			runIn(3, executeConfigureCmds)
		}
		else {
			runIn(8, executeInitializeCmds)
		}
	}
}

private initialize() {
	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name: "supportedButtonValues", value:["pushed", "held", "pushed_2x", "pushed_3x"], displayed: false)
		sendEvent(name: "numberOfButtons", value:4, displayed: false)	
	}
	
	if (device.currentValue("activeProgram") == null) {
		sendActiveProgramEvent(0)
	}
	
	if (device.currentValue("switch")) {
		sendEvent(name: "switch", value: "on", displayed: false)
		sendEvent(name: "colorSwitch", value: "on", displayed: false)
		sendEvent(name: "whiteSwitch", value: "on", displayed: false)
	}
	
	if (!device.currentValue("color")) {
		sendEvent(name: "color", value: "#FFFFFF", displayed: false)
		sendEvent(name: "hue", value: 0, displayed: false)
		sendEvent(name: "saturation", value: 0, displayed: false)
	}

	def checkInterval = (6 * 60 * 60) + (5 * 60)	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	
	startHealthPollSchedule()
}


private addRemoveChildSwitches() {
	if (settings.createColorSwitch) {
		addChildSwitch("color")
	}
	else {
		removeChildSwitch("color")
	}
	
	if (settings.createWhiteSwitch) {
		addChildSwitch("white")
	}
	else {
		removeChildSwitch("white")
	}
}

private addChildSwitch(childType) {
	def dni = getChildSwitchDNI(childType)
	if (!findChildSwitch(dni)) {
		logDebug "Creating Child ${childType.capitalize()} Switch"
		
		def child = addChildDevice(
			"smartthings",
			"Child Switch",
			dni,
			null, 
			[
				completedSetup: true,
				isComponent: false,
				label: "${device.displayName}-${childType.capitalize()}"
			]
		)
		
		if (child) {
			child.sendEvent(name: "switch", value: device.currentValue("${childType}Switch"))
		}
	}
}

private removeChildSwitch(childType) {
	def dni = getChildSwitchDNI(childType)
	if (findChildSwitch(dni)) {
		log.warn "Removing Child ${childType.capitalize()} Switch"
		deleteChildDevice(dni)
	}
}


def ping() {
	logDebug "ping()..."
	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		
		healthPoll()
		
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in and had to be pinged.
		startHealthPollSchedule()
	}	
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}


def healthPoll() {
	logDebug "healthPoll()..."	
	sendCommands([manufacturerSpecificGetCmd()])
}


def configure() {
	logDebug "configure()..."
			
	if (!state.isDeviceInitialized) {
		runIn(8, executeInitializeCmds)
	}
	else {
		state.syncAll = true
		runIn(2, updateSyncStatus)
		executeConfigureCmds()
	}	
	return []
}


def executeInitializeCmds() {
	state.isDeviceInitialized = true	
	state.whiteEnabled = true
	state.colorEnabled = true
	
	def cmds = [
		versionGetCmd(),
		switchColorRGBSetCmd([255, 255, 255]),
		switchColorWhiteSetCmd(255),
		switchMultilevelSetCmd(99, 0)
	]
	
	cmds += getRefreshCmds()
	
	sendCommands(cmds, 1000)
}


def executeConfigureCmds() {
	def cmds = []
	
	configParams.each { 
		if (it.options) {
			def storedVal = getParamStoredValue(it.num)
			if (state.syncAll || "${it.value}" != "${storedVal}") {
				if (storedVal != null) {
					logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
					cmds << configSetCmd(it, it.value)
				}
				cmds << configGetCmd(it)
			}
		}
	}
	
	state.syncAll = false
	
	sendCommands(cmds, 1000)
}

private sendCommands(cmds, delay=100) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, delay)
	return []
}


def refresh() {
	logDebug "refresh()..."
	
	updateSyncStatus()
	
	if (device.currentValue("activeProgram") != "0") {
		sendActiveProgramEvent(0)
	}
	
	def cmds = [
		versionGetCmd(),
		meterGetCmd(meterScalePower),
		configGetCmd(presetProgramsParam)
	]
	
	cmds += getRefreshCmds()
	
	return delayBetween(cmds, 1000)
} 

private getRefreshCmds() {
	def cmds = [
		basicGetCmd(),		
		switchMultilevelGetCmd()
	]
	
	COLOR_COMPONENTS.each {
		cmds << switchColorGetCmd(it.value)
	}
	return cmds
}


def startFireplaceProgram() { return startProgram(6) }
def startStormProgram() { return startProgram(7) }
def startRainbowProgram() { return startProgram(8) }
def startPolarLightsProgram() { return startProgram(9) }
def startPoliceProgram() { return startProgram(10) }

private startProgram(program) {
	logDebug "startProgram($program)..."
	
	sendActiveProgramEvent(program)
	
	return delayBetween([
		configSetCmd(presetProgramsParam, safeToInt(program)),
		configGetCmd(presetProgramsParam)
	], 2000)
}


def stopProgram() {
	logDebug "stopProgram()..."
	
	return delayBetween([
		configSetCmd(presetProgramsParam, 0),
		configGetCmd(presetProgramsParam)
	], 2000)
}


void childOn(dni) {
	logDebug "childOn(${dni})..."
	def cmds = []
	if ("${dni}".endsWith("COLOR")) {
		cmds += colorOn()
	}
	else {
		cmds += whiteOn()
	}
	sendCommands(cmds)
}


void childOff(dni) {
	logDebug "childOff(${dni})..."
	def cmds = []
	if ("${dni}".endsWith("COLOR")) {
		cmds += colorOff()
	}
	else {
		cmds += whiteOff()
	}
	sendCommands(cmds)
}


def whiteOn() {
	logDebug "whiteOn()..."	
	
	state.whiteEnabled = true
	sendSwitchEvent("whiteSwitch", "on", "digital")
	
	def cmds = []
	
	if (device.currentValue("switch") == "off" && state.colorEnabled) {
		state.colorEnabled = false
		cmds << switchColorRGBSetCmd([0,0,0])
		cmds << "delay 500"
	}
	
	cmds << switchColorWhiteSetCmd(255)
	
	if (device.currentValue("switch") == "off") {
		cmds << "delay 500"
		cmds += on()
	}	
	return cmds
}


def whiteOff() {
	logDebug "whiteOff()..."
	
	sendSwitchEvent("whiteSwitch", "off", "digital")
	
	def cmds = []	
	if (state.colorEnabled) {
		state.whiteEnabled = false
		cmds << switchColorWhiteSetCmd(0)
	}
	else {
		cmds += off()		
	}
	return cmds
}


def colorOn() {
	logDebug "colorOn()..."
	def data = [hex: device.currentValue("color")]
	return getSetColorCmds(data)	
}


def colorOff() {
	logDebug "colorOff()..."
	
	sendSwitchEvent("colorSwitch", "off", "digital")
	
	def cmds = []	
	if (state.whiteEnabled) {
		state.colorEnabled = false
		cmds << switchColorRGBSetCmd([0, 0, 0])
	}
	else {
		cmds += off()
	}
	return cmds
}

private sendSwitchEvent(name, value, type) {
	def desc = "${name} is ${value} (${type})"
	
	logDebug "${desc}"
	
	if (device.currentValue(name) != value) {
		sendEvent(name: name, value: value, type: type, descriptionText: "${device.displayName}: ${desc}")
	}
	
	if (name != "switch") {
		sendChildSwitchEvent(name, value, type)
	}
}

private sendChildSwitchEvent(name, value, type) {
	def childType = name.contains("color") ? "color" : "white"
	def child = findChildSwitch(getChildSwitchDNI(childType))
	if (child && child?.currentValue("switch") != value) {
		child.sendEvent(name: "switch", value: value, type: type)
	}
}


def on() {
	logDebug "on()..."	
	
	state.pendingSwitch = "on"
		
	return delayBetween([
		basicSetCmd(0xFF),
		basicGetCmd()
	], 500)
}


def off() {
	logDebug "off()..."
	
	state.pendingSwitch = "off"
	
	def rampRate = dimmerRampRateRemoteParam.value
	def delayMs = !rampRate ? 1000 : ((rampRate + 2) * 1000)
	
	if (rampRate > 3) {
		sendSwitchEvent("switch", "off", "digital")
	}
	
	return [
		basicSetCmd(0x00),
		"delay ${delayMs}",
		basicGetCmd()
	]
}


def setLevel(level) {
	return setLevel(level, 0)
}

def setLevel(level, duration) {
	logDebug "setLevel($level, $duration)..."

	return delayBetween([
		switchMultilevelSetCmd(level, 0),
		switchMultilevelGetCmd()
	], 500)
}


def setSaturation(percent) {
	logDebug "setSaturation($percent)..."
	
	def data = [saturation: percent]
	return getSetColorCmds(data)
}


def setHue(value) {
	logDebug "setHue($value)..."
	
	def data = [hue: value]
	return getSetColorCmds(data)
}


def setColor(value) {
	logDebug "setColor($value)..."
	
	return getSetColorCmds(value)
}


private getSetColorCmds(data) {	
	def rgb
	def hex
	
	if (data.hex) {		
		rgb = data.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		hex = data.hex
	}
	else {
		rgb = huesatToRGB(data.hue, data.saturation)		
		hex = rgbToHex(rgb)
	}
	
	def cmds = []
	
	if (rgb) {
		state.colorEnabled = true
		
		sendSwitchEvent("colorSwitch", "on", "digital")		
		sendEvent(getEventMap("color", hex))
	
		def hsv = rgbToHSV(rgb)	
		sendEvent(getEventMap("hue", hsv.hue))
		sendEvent(getEventMap("saturation", hsv.saturation))
		
		if (device.currentValue("switch") == "off" && state.whiteEnabled) {
			state.whiteEnabled = false
			cmds << switchColorWhiteSetCmd(0)
			cmds << "delay 500"
		}
		
		cmds << switchColorRGBSetCmd(rgb)
		
		if (device.currentValue("switch") == "off") {
			cmds << "delay 500"
			cmds += on()
		}
	}
	else {
		log.warn "Unable to get RGB from ${data}"
	}
	return cmds
}


private manufacturerSpecificGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private meterGetCmd(meterScale) {
	return secureCmd(zwave.meterV3.meterGet(scale: meterScale))
}

private basicSetCmd(val, endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicSet(value: val), endpoint)
}

private basicGetCmd(endpoint) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endpoint)
}

private switchMultilevelSetCmd(level, duration) {
	def levelVal = validateRange(level, 99, 0, 99)
	def durationVal = validateRange(duration, 0, 0, 100)
	
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

private switchMultilevelGetCmd() {
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelGet())
}

private switchColorRGBSetCmd(rgb) {
	return secureCmd(zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2]))
}

private switchColorWhiteSetCmd(value) {
	return secureCmd(zwave.switchColorV3.switchColorSet(warmWhite: value))
}

private switchColorGetCmd(colorId) {	
	return secureCmd(zwave.switchColorV3.switchColorGet(colorComponent: colorId))
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private multiChannelCmdEncapCmd(cmd, endpoint) {	
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
	}
	else {
		return secureCmd(cmd)
	}
}

private secureCmd(cmd) {
	if (isSecurityEnabled()) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private isSecurityEnabled() {
	try {
		return zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))
	}
	catch (e) {
		return false
	}
}

private getConfigParams() {
	return [
		powerRecoveryParam,
		input1TypeParam,
		input2TypeParam,
		input3TypeParam,
		input4TypeParam,
		input1SceneParam,
		input2SceneParam,
		input3SceneParam,
		input4SceneParam,
		// powerReportingThresholdParam,
		powerReportingFrequencyParam,
		// analogVoltageReportingThresholdParam,
		// analogReportingFrequencyParam,
		energyReportingThresholdParam,
		energyReportingFrequencyParam,
		switchModeParam,
		dimmerRampRateLocalParam,
		dimmerRampRateRemoteParam
		// activeReportsParam,
		// singleClickOnFrameParam,
		// singleClickOffFrameParam,
		// doubleClickOffFrameParam,
		// presetProgramsParam
	]
}

private getPowerRecoveryParam() {
	def options = ["0":"Off", 1:"Remember Last Status", 2:"On"]
	return getParam(1, "On Off Status After Power Failure", 1, 0, options)
}

private getInput1TypeParam() {
	return getParam(20, "Input 1 Type", 1, 2, inputTypeOptions)
}

private getInput2TypeParam() {
	return getParam(21, "Input 2 Type", 1, 2, inputTypeOptions)
}

private getInput3TypeParam() {
	return getParam(22, "Input 3 Type", 1, 2, inputTypeOptions)
}

private getInput4TypeParam() {
	return getParam(23, "Input 4 Type", 1, 2, inputTypeOptions)
}

private getInput1SceneParam() {
	return getParam(40, "Input 1 Scene Activation", 1, 15, sceneControlOptions)
}

private getInput2SceneParam() {
	return getParam(41, "Input 2 Scene Activation", 1, 15, sceneControlOptions)
}

private getInput3SceneParam() {
	return getParam(42, "Input 3 Scene Activation", 1, 15, sceneControlOptions)
}

private getInput4SceneParam() {
	return getParam(43, "Input 4 Scene Activation", 1, 15, sceneControlOptions)
}

private getPowerReportingThresholdParam() {   // *** NOT IN MANUAL ***
	def options = [0:"Disabled", 1:"1%", 15:"15%", 500:"500%"]
	return getParam(61, "Power Reporting Threshold", 2, 15, options)
}

private getPowerReportingFrequencyParam() {
	return getParam(62, "Power Reporting Frequency", 2, 3600, reportingFrequencyOptions)
}

private getAnalogVoltageReportingThresholdParam() {
	def options = [0:"Disabled", 1:"0.1V", 5:"0.5V", 100:"10V"]
	return getParam(63, "Analog Sensor Voltage Reporting Threshold", 2, 5, options)
}

private getAnalogReportingFrequencyParam() {
	return getParam(64, "Analog Sensor Reporting Frequency", 2, 0, reportingFrequencyOptions)
}

private getEnergyReportingThresholdParam() {
	def options = [0:"Disabled", 1:"0.01 kWh", 10:"0.1 kWh", 500:"5 kWh"]
	return getParam(65, "Energy Reporting Threshold", 2, 10, options)
}

private getEnergyReportingFrequencyParam() {
	return getParam(66, "Energy Reporting Frequency", 2, 3600, reportingFrequencyOptions)
}

private getSwitchModeParam() {
	def options = [0:"RGBW Mode", 1:"HSB Mode"]
	return getParam(150, "RGBW / HSB Wall Switch Mode", 1, 0, options)
}

private getDimmerRampRateLocalParam() {
	return getParam(151, "Dimmer Ramp Rate (local control)", 2, 3, rampRateOptions)
}

private getDimmerRampRateRemoteParam() {
	return getParam(152, "Dimmer Ramp Rate (remote control)", 2, 3, rampRateOptions)
}

private getActiveReportsParam() {  // *** NOT IN MANUAL ***
	def options = [
		1:"Root/EP1 Switch Color Report (RGBW)",
		2:"Root/EP1 Central Scene Report",
		4:"EP2 Switch Multilevel Report (Red)",
		8:"EP3 Switch Multilevel Report (Green)",
		16:"EP4 Switch Multilevel Report (Blue)",
		32:"EP5 Switch Multilevel Report (White)",
		64:"EP6 Sensor Multilevel Report  (analog input 1)",
		128:"EP7 Sensor Multilevel Report  (analog input 2)",
		256:"EP8 Sensor Multilevel Report  (analog input 3)",
		512:"EP9 Sensor Multilevel Report  (analog input 4)"
	]
	return getParam(153, "Active Reports", 2, 1023, options)
}

private getSingleClickOnFrameParam() {
	return getParam(154, "Single Click ON Trigger for Associated Devices", 4, 4294967295)//, [0-99/255 for each byte]
}

private getSingleClickOffFrameParam() {
	return getParam(155, "Single Click OFF Trigger for Associated Devices", 4, 0)
	//, [0-99/255 for each byte]
}

private getDoubleClickOffFrameParam() {
	return getParam(156, "Double Click ON Trigger for Associated Devices", 4, 1667457891)
	//, [0-99/255 for each byte]
}

private getPresetProgramsParam() {
	// def options = [
		// 0:"Disabled", 
		// 6:"Fireplace", 
		// 7:"Storm", 
		// 8:"Rainbow", 
		// 9:"Polar Lights", 
		// 10:"Police"
	// ]
	return getParam(157, "Preset Programs", 1, 0) 
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
private getInputTypeOptions() {
	return [
		0:"Analog Sensor with No Pull-up",
		1:"Analog Sensor with Pull-up",
		2:"Momentary Switch", 
		3:"Toggle Switch (any change)",
		4:"Toggle Switch (up=on/down=off)"
	]
}

private getSceneControlOptions() {
	return [
		0:"Disabled",
		1:"Push Once",
		2:"Push Twice",
		3:"Push Once or Twice",
		4:"Push 3 Times",
		5:"Push Once or 3 Times",
		6:"Push Twice or 3 Times",
		7:"Push Once, Twice, or 3 Times",
		8:"Held and Released",
		15:"Push Once, Twice, 3 Times, or Held and Released"
	]
}

private getReportingFrequencyOptions() {
	def options = [0:"Disabled"]
	
	options["30"] = "30 Seconds"
	options["45"] = "45 Seconds"
	options["60"] = "1 Minute"
	
	(2..15).each {
		options["${it * 60}"] = "${it} Minutes"
	}
	
	options["${30 * 60}"] = "30 Minutes"
	options["${45 * 60}"] = "45 Minutes"
	options["${60 * 60}"] = "1 Hour"
	
	(2..9).each {
		options["${it * 60 * 60}"] = "${it} Hours"
	}
	return options
}

private getRampRateOptions() {
	def options = [
		0:"Instant", 
		1:"1 Second"
	]
	
	(2..15).each {
		options["${it}"] = "${it} Seconds"
	}
	
	[30, 45]. each {
		options["${it}"] = "${it} Seconds"
	}
		
	options["60"] = "1 Minute"
	
	(2..15).each {	// max 127 minutes
		options["${127 + it}"] = "${it} Minutes"	
	}	
	return options
}


def parse(description) {	
	def result = null
	
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)			
		} 
		else {
			logDebug("Couldn't zwave.parse '$description'")
		}
	}
	
	if (!isDuplicateCommand(state.lastCheckinTime, (5 * 60 * 1000))) {
		sendLastCheckinEvent()
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


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		log.warn "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "Device Successfully Polled"	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	sendEvent(getEventMap("firmwareVersion", version))
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
	logTrace "SwitchColorReport: $cmd"

	state["cc${cmd.colorComponentId}Value"] = cmd.value

	if ("${cmd.colorComponentId}" == "${COLOR_COMPONENTS.white}") {
		def whiteEnabled = (cmd.value != 0)
		state.whiteEnabled = whiteEnabled
		
		def whiteSwitchVal = (device.currentValue("switch") == "on" && whiteEnabled) ? "on" : "off"
		sendSwitchEvent("whiteSwitch", whiteSwitchVal, "physical")
	}
	else {
		runIn(2, checkForColorComponentChanges)
	}
	return []
}

def checkForColorComponentChanges() {
	log.warn "checkForColorComponentChanges()..."
	def rgb = [
		state["cc${COLOR_COMPONENTS.red}Value"],
		state["cc${COLOR_COMPONENTS.green}Value"],
		state["cc${COLOR_COMPONENTS.blue}Value"]
	]
	
	logTrace "checkForColorComponentChanges: [colorEnabled: ${state.colorEnabled}, rgb: ${rgb}]"
	
	if (rgb[0] != null && rgb[1] != null && rgb[2] != null) {	
		def colorEnabled = (rgb.find { it != 0 } != null)
		
		state.colorEnabled = colorEnabled

		if (colorEnabled) {
			def hsv = rgbToHSV(rgb)
			def hex = rgbToHex(rgb)

			sendEvent(getEventMap("color", hex))
			sendEvent(getEventMap("hue", hsv.hue))
			sendEvent(getEventMap("saturation", hsv.saturation))
			
			if (device.currentValue("switch") == "on" && device.currentValue("colorSwitch") == "off") {
				sendSwitchEvent("colorSwitch", "on", "physical")
			}
		}
	}
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv2.SwitchMultilevelReport cmd, endpoint=null) {
	logTrace "SwitchMultilevelReport: ${cmd}" + (endpoint ? " - Endpoint ${endpoint}" : "")
	
	if (!endpoint) {
		if (cmd.value) {
			sendEvent(getEventMap("level", cmd.value, "%"))
		}
			
		if (device.currentValue("switch") != (cmd.value ? "on" : "off")) {
			handleSwitchReport(cmd.value)
		}		
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=null) {	
	logTrace "BasicReport: $cmd" + (endpoint ? " - Endpoint ${endpoint}" : "")

	if (!endpoint) {
		handleSwitchReport(cmd.value)
	}
	return []
}

private handleSwitchReport(rawValue) {
	def value = (rawValue ? "on" : "off")
	def type = ((state.pendingSwitch == value) ? "digital" : "physical")
	
	sendSwitchEvent("switch", value, type)
	
	if (state.whiteEnabled || value == "off") {
		sendSwitchEvent("whiteSwitch", value, type)
	}
	if (state.colorEnabled || value == "off") {		
		sendSwitchEvent("colorSwitch", value, type)
	}
	
	state.pendingSwitch = null
}


def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {		
		state.lastSequenceNumber = cmd.sequenceNumber
	
		logTrace "${cmd}"

		def action
		switch (cmd.keyAttributes){
			case 0:
				action = "pushed"
				break
			case 1:
				action = "released"
				break
			case 2:
				action = "held"
				break
			case 3:
				action = "pushed_2x"
				break
			case 4:
				action = "pushed_3x"
				break
		}

		if (action){
			sendButtonEvent(cmd.sceneNumber, action, "physical")
		}
	}
	return []
}

private sendButtonEvent(buttonNumber, action, type) {
	def desc = "button ${buttonNumber} ${action} (${type})"

	logDebug "${desc}"

	sendEvent(name: "button", value: action, type: type, displayed: true, isStateChange: true, data: [buttonNumber: buttonNumber], descriptionText: "${device.displayName}: ${desc}")
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logTrace "MeterReport: $cmd"
	
	def val = roundTwoPlaces(cmd.scaledMeterValue)
		
	if (cmd.scale == meterScalePower) {
		sendEvent(getEventMap("power", val, "W"))
	}	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: ${cmd}"
	
	if (cmd.parameterNumber == presetProgramsParam.num) {
		if (device.currentValue("activeProgram") != "0") {
			sendCommands(getRefreshCmds(), 2000)
		}
		sendActiveProgramEvent(cmd.scaledConfigurationValue)
	}
	else {
		updateSyncStatus("Syncing...")	
	
		runIn(5, updateSyncStatus)
	
		def param = configParams.find { it.num == cmd.parameterNumber }
		if (param) {
			state["configVal${param.num}"] = cmd.scaledConfigurationValue
			logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"			
		}
		else {
			logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
		}
	}	
	return []
}

private sendActiveProgramEvent(program) {
	sendEvent(getEventMap("activeProgram", "${program}"))
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "unhandled: $cmd"
	return []
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
		state.syncing = false
	}
	else if (device.currentValue("${syncStatus}") != "${status}") {
		state.syncing = true
	}
	
	sendEvent(name: "syncStatus", value: status, displayed: false)
}

private getPendingChanges() {
	return configParams.count { isConfigParamSynced(it) ? 0 : 1 }
}

private isConfigParamSynced(param) {
	return (!param.options || "${param.value}" == "${getParamStoredValue(param.num)}")
}

private getParamStoredValue(paramNum) {
	return state["configVal${paramNum}"]
}


private validateLevel(val) {
	return validateRange(val, device.currentValue("level"), 1, 99)
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

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
}

private getMeterScalePower() { 
	return 2 
}


private rgbToHex(red, green, blue) {
	return colorUtil.rgbToHex(red as int, green as int, blue as int)
}

private rgbToHSV(red, green, blue) {
	def hex = colorUtil.rgbToHex(red as int, green as int, blue as int)
	def hsv = colorUtil.hexToHsv(hex)
	return [hue: hsv[0], saturation: hsv[1], value: hsv[2]]
}

private huesatToRGB(hue, sat) {
	def color = colorUtil.hsvToHex(Math.round(hue) as int, Math.round(sat) as int)
	return colorUtil.hexToRgb(color)
}


private getEventMap(name, value, unit="") {	
	def eventMap = [
		name: name,
		value: value,
		descriptionText: getDescriptionText("${name} is ${value}${unit}")
	]
	
	if (unit) {
		eventMap.unit = unit
	}	
	return eventMap
}


private sendLastCheckinEvent() {
	state.lastCheckinTime = new Date().time
	logDebug "Device Checked In"	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
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


private getChildSwitchDNI(childType) {
	return "${device.deviceNetworkId}-${childType.toUpperCase()}"
}

private findChildSwitch(dni) {
	return childDevices.find { it.deviceNetworkId == dni }
}


private getDescriptionText(msg) {
	logDebug "${msg}"
	return "${device.displayName}: ${msg}"
}

private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}