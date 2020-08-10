/**
 *  Zooz RGBW Dimmer v1.1.2
 *  (Model: ZEN31)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-zooz-rgbw-dimmer-zen31/178616?u=krlaframboise
 *
 *
 *  Changelog:
 *
 *    1.1.2 (08/10/2020)
 *      - Added ST workaround for S2 Supervision bug with MultiChannel Devices.
 *
 *    1.1.1 (03/13/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.1 (12/08/2019)
 *      - Complete rewrite of DTH.
 *      - Added sliders for each channel and a color that controls RGB.
 *		- Added optional child dimmers that allow you to toggle and dim each channel and a dimmer for color that controls RGB.
 *		- Added optional child switches for each Preset Program
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
	0x22: 1,	// Application Status
	0x26: 3,	// Switch Multilevel
	0x31: 5,	// SensorMultilevel
	0x32: 3,	// Meter
	0x33: 3,	// Color Control
	0x55: 1,	// Transport Service
	0x56: 1,	// CRC16 Encap
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// Multi Channel (4)
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x71: 3,	// Notification (v4)
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x75: 1,	// Protection
	0x7A: 2,	// Firmware Update Md
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map COLOR_COMPONENTS = [white:0, red:2, green:3, blue:4]
@Field static Map PRESET_PROGRAMS = [fireplace:6, storm:7, rainbow:8, polarLights:9, police:10]
@Field static List DIMMER_NAMES = ["white", "color", "red", "green", "blue"]


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
		attribute "redSwitch", "string"
		attribute "greenSwitch", "string"
		attribute "blueSwitch", "string"
		attribute "activeProgram", "string"

		attribute "whiteLevel", "number"
		attribute "colorLevel", "number"
		attribute "redLevel", "number"
		attribute "greenLevel", "number"
		attribute "blueLevel", "number"

		command "whiteOn"
		command "whiteOff"
		command "colorOn"
		command "colorOff"
		command "redOn"
		command "redOff"
		command "greenOn"
		command "greenOff"
		command "blueOn"
		command "blueOff"

		command "setWhiteLevel", ["NUMBER"]
		command "setColorLevel", ["NUMBER"]
		command "setRedLevel", ["NUMBER"]
		command "setGreenLevel", ["NUMBER"]
		command "setBlueLevel", ["NUMBER"]
		command "startFireplaceProgram"
		command "startStormProgram"
		command "startRainbowProgram"
		command "startPolarLightsProgram"
		command "startPoliceProgram"
		command "startProgram", ["NUMBER"]
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

		standardTile("whiteLabel", "generic", width: 2, height: 1) {
			state "default", label:'W', action:"", backgroundColor:"#fefebe"//#ffffcc"
		}

		standardTile("whiteSwitch", "device.whiteSwitch", width: 2, height: 1) {
			state "on", label:'${name}', action:"whiteOff", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"whiteOn", backgroundColor:"#ffffff"
		}

		controlTile("whiteSlider", "device.whiteLevel", "slider", width: 2,	height: 1) {
			state "whiteLevel", action:"setWhiteLevel"
		}

		standardTile("colorLabel", "generic", width: 2, height: 1) {
			state "default", label:'RGB', action:"", backgroundColor:"#ffffff"
		}

		standardTile("colorSwitch", "device.colorSwitch", width: 2, height: 1) {
			state "on", label:'${name}', action:"colorOff", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"colorOn", backgroundColor:"#ffffff"
		}

		controlTile("colorSlider", "device.colorLevel", "slider", width: 2, height: 1) {
			state "colorLevel", action:"setColorLevel"
		}

		standardTile("redLabel", "generic", width: 2, height: 1) {
			state "default", label:'R', action:"", backgroundColor:"#ff0000"
		}

		standardTile("redSwitch", "device.redSwitch", width: 2, height: 1) {
			state "on", label:'${name}', action:"redOff", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"redOn", backgroundColor:"#ffffff"
		}

		controlTile("redSlider", "device.redLevel", "slider", width: 2, height: 1) {
			state "redLevel", action:"setRedLevel"
		}

		standardTile("greenLabel", "generic", width: 2, height: 1) {
			state "default", label:'G', action:"", backgroundColor:"#00ff00"
		}

		standardTile("greenSwitch", "device.greenSwitch", width: 2, height: 1) {
			state "on", label:'${name}', action:"greenOff", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"greenOn", backgroundColor:"#ffffff"
		}

		controlTile("greenSlider", "device.greenLevel", "slider", width: 2, height: 1) {
			state "greenLevel", action:"setGreenLevel"
		}

		standardTile("blueLabel", "generic", width: 2, height: 1) {
			state "default", label:'B', action:"", backgroundColor:"#0000ff"
		}

		standardTile("blueSwitch", "device.blueSwitch", width: 2, height: 1) {
			state "on", label:'${name}', action:"blueOff", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"blueOn", backgroundColor:"#ffffff"
		}

		controlTile("blueSlider", "device.blueLevel", "slider",	width: 2, height: 1) {
			state "blueLevel", action:"setBlueLevel"
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

		standardTile("stop", "device.activeProgram", width: 2, height: 2) {
			state "default", label:'Stop', action: "stopProgram", backgroundColor:"#00a0dc"
			state "0", label:'Stop', action: "stopProgram", backgroundColor:"#ffffff"

		}

		main(["switch"])
		details(["switch", "whiteLabel", "whiteSlider", "whiteSwitch", "redLabel", "redSlider", "redSwitch", "greenLabel", "greenSlider", "greenSwitch", "blueLabel", "blueSlider", "blueSwitch", "colorLabel", "colorSlider", "colorSwitch", "power", "syncStatus", "firmwareVersion", "refresh", "configure", "stop", "fireplace", "storm", "rainbow", "polarLights", "police", "rgbSelector"])
	}

	preferences {		
		input title: "\n*** ATTENTION ***",
			description: "The settings screen in the new SmartThings Mobile App crashes constantly so you should use the Classic Mobile App to change these settings.", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
			
		input title: "\n\nDevice Configuration",
			description: "", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
		
		getParamInput(powerRecoveryParam)
		getParamInput(dimmerRampRateLocalParam)
		getParamInput(dimmerRampRateRemoteParam)
		getParamInput(powerReportingFrequencyParam)
		getParamInput(energyReportingThresholdParam)
		getParamInput(energyReportingFrequencyParam)
		getParamInput(switchModeParam)
		getParamInput(input1TypeParam)
		getParamInput(input2TypeParam)
		getParamInput(input3TypeParam)
		getParamInput(input4TypeParam)
		getParamInput(input1SceneParam)
		getParamInput(input2SceneParam)
		getParamInput(input3SceneParam)
		getParamInput(input4SceneParam)
		// getParamInput(powerReportingThresholdParam)
		// getParamInput(analogVoltageReportingThresholdParam)
		// getParamInput(analogReportingFrequencyParam)
		// getParamInput(singleClickOnFrameParam)
		// getParamInput(singleClickOffFrameParam)
		// getParamInput(doubleClickOffFrameParam)
		// getParamInput(presetProgramsParam)
		
		input title: "\n\nCreate Child Devices for Color Channels",
			description: "The new SmartThings Mobile App doesn't support custom user interfaces so you can't control the channels independently like you can with the Classic Mobile App, but enabling a 'Create Child Dimmer for ...' setting will create a child dimmer device for the corresponding channel.\n\nThe child device can be used in both mobile apps to turn that channel on/off or change its brightness.\n\nWARNING: Disabling the setting will delete the child dimmer it created when that setting was enabled.\n\nYOU MUST INSTALL THE 'CHILD DIMMER' DTH TO USE THIS FEATURE", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
			
		getBoolInput("createWhiteDimmer", "Create Child Dimmer for White?", false)
		getBoolInput("createColorDimmer", "Create Child Dimmer for Color?\n(Allows you to control Red, Green, and Blue together.)", false)
		getBoolInput("createRedDimmer", "Create Child Dimmer for Red?", false)
		getBoolInput("createGreenDimmer", "Create Child Dimmer for Green?", false)
		getBoolInput("createBlueDimmer", "Create Child Dimmer for Blue?", false)

		input title: "\n\nCreate Child Devices for the Preset Programs",
			description: "Enabling a 'Create Child Switch for ...' setting will create a child switch device for the corresponding Preset Program.\n\nTurning on that child device will start the program and turning it off will stop the program.\n\nWARNING: Disabling the setting will delete the child device it created when that setting was enabled.", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
			
		getBoolInput("createFireplaceSwitch", "Create Child Switch for Fireplace Program?", false)
		getBoolInput("createStormSwitch", "Create Child Switch for Storm Program?", false)
		getBoolInput("createRainbowSwitch", "Create Child Switch for Rainbow Program?", false)
		getBoolInput("createPolarLightsSwitch", "Create Child Switch for Polar Lights Program?", false)
		getBoolInput("createPoliceSwitch", "Create Child Switch for Police Program?", false)

		input title: "\n\nAdvanced Settings",
			description: "", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
			
		getBoolInput("assumeSuccess", "Send Color Related Events Immediately?", assumeSuccessSetting)
		
		input title: "",
			description: "***BETA FEATURE***\n\nBy default the device waits until it receives confirmation of changes before creating the events which might cause the new mobile app and other integrations to report that the device is not responding.\n\nDisabling this setting will make the device create the events immediately and not request reports that it can use to verify the result which makes the UI respond a lot faster and cuts down on z-wave traffic, but the switch and level states shown in SmartThings might sometimes be wrong.  If that happens you can tap the 'Refresh' tile to update the states shown in SmartThings.\n\n", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
		
		input "commandDelay", "enum",
			title: "Delay Between Z-Wave Commands:",
			required: false,
			displayDuringSetup: true,
			defaultValue: "${commandDelaySetting}", 
			options: setDefaultOption(commandDelayOptions, commandDelaySetting)
		
		input title: "", 
			description: "When sending z-wave commands to the device the optimum delay to use between each command depends on the strength of your z-wave mesh so if you're having issues with performance or reliability you should try increasing or decreasing the 'Delay Between Z-Wave Commands' setting above.\n\n", 
			displayDuringSetup: false, 
			type: "paragraph", 
			element: "paragraph"
			
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

private getCommandDelaySetting() {
	return safeToInt(settings?.commandDelay, 100)
}

private getAssumeSuccessSetting() {
	return settings?.assumeSuccess == false ? false : true
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

		def cmds = []
		if (state.isDeviceInitialized) {

			addRemoveChildSwitches()

			runIn(5, updateSyncStatus)
			if (pendingChanges > 0) {
				cmds += getConfigureCmds()
			}
		}
		else {
			runIn(10, initializeDevice)
		}
		return cmds ? response(defaultDelayBetween(cmds)) : []
	}
}

private initialize() {
	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name: "supportedButtonValues", value:["pushed", "held", "pushed_2x", "pushed_3x"], displayed: false)
		sendEvent(name: "numberOfButtons", value:4, displayed: false)
	}

	if (device.currentValue("activeProgram") == null) {
		sendEvent(name: "activeProgram", value: "0", displayed: false)
	}

	if (device.currentValue("switch")) {
		sendEvent(name: "switch", value: "on", displayed: false)
	}

	DIMMER_NAMES.each {
		if (device.currentValue("${it}Switch")) {
			sendEvent(name: "${it}Switch", value: "on", displayed: false)
		}
		if (device.currentValue("${it}Level")) {
			sendEvent(name: "${it}Level", value: 100, displayed: false)
		}
	}

	if (!device.currentValue("color")) {
		sendEvent(name: "color", value: "#FFFFFF", displayed: false)
		sendEvent(name: "hue", value: 0, displayed: false)
		sendEvent(name: "saturation", value: 0, displayed: false)
	}

	def checkInterval = (6 * 60 * 60) + (5 * 60)
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	startHealthPollSchedule()
}


private addRemoveChildSwitches() {
	if (settings) {
		DIMMER_NAMES.each {
			if (settings["create${it.capitalize()}Dimmer"]) {
				addChildDimmer(it)
			}
			else {
				removeChildDevice(it)
			}
		}

		PRESET_PROGRAMS.each {			
			if (settings["create${it.key.capitalize()}Switch"]) {
				addChildProgramSwitch(it.key)
			}
			else {
				removeChildDevice(it.key)
			}
		}
	}
}

private addChildDimmer(childName) {
	if (!findChildDevice(childName)) {
		logDebug "Creating Child Dimmer for ${childName.capitalize()}"

		try {
			def child = addChildDevice(
				"krlaframboise",
				"Child Dimmer",
				getChildDNI(childName),
				null,
				[
					completedSetup: true,
					isComponent: false,
					label: "${device.displayName}-${childName.capitalize()}"
				]
			)

			if (child) {
				child.refresh()
			}
		}
		catch (e) {
			log.warn "Unable to create child device for '${childName}' because the 'Child Dimmer' device type handler has not been installed and published."
		}
	}
}

private addChildProgramSwitch(childName) {
	if (!findChildDevice(childName)) {
		logDebug "Creating Child Switch for ${childName.capitalize()} Program"

		def child = addChildDevice(
			"smartthings",
			"Child Switch",
			getChildDNI(childName),
			null,
			[
				completedSetup: true,
				isComponent: false,
				label: "${device.displayName}-${childName.capitalize()} Program"
			]
		)

		child?.sendEvent(name:"switch", value:"off", displayed: false)
	}
}

private removeChildDevice(childName) {
	def child = findChildDevice(childName)
	if (child) {
		log.warn "Removing ${child.displayName}} "
		deleteChildDevice(child.deviceNetworkId)
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

	state.syncAll = true

	def cmds = []
	if (!state.isDeviceInitialized) {
		runIn(10, initializeDevice)
	}
	else {
		runIn(2, updateSyncStatus)
		cmds += getConfigureCmds()
	}
	return defaultDelayBetween(cmds)
}


def initializeDevice() {
	addRemoveChildSwitches()

	state.isDeviceInitialized = true

	def cmds = [
		switchMultilevelSetCmd(100, 0),
		switchColorSetCmd(activeRGBW)
	]

	cmds += getRefreshCmds()

	cmds += getConfigureCmds()

	sendCommands(defaultDelayBetween(cmds))
}

private getConfigureCmds() {
	def cmds = []

	if (state.syncAll) {
		cmds << multiChannelAssociationRemoveCmd(1)
		cmds << associationSetCmd(1)
		cmds << associationSetCmd(2)
	}

	configParams.each {
		if (it.options) {
			def storedVal = getParamStoredValue(it.num)
			if (state.syncAll || it.value != storedVal) {
				if (storedVal != null) {
					logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
					cmds << configSetCmd(it, it.value)
				}
				cmds << configGetCmd(it)
			}
		}
	}
	state.syncAll = false
	return cmds
}


def refresh() {
	logDebug "refresh()..."

	updateSyncStatus()

	return defaultDelayBetween(getRefreshCmds())
}

private getRefreshCmds() {
	def cmds = [
		versionGetCmd(),
		meterGetCmd(meterScalePower),
		configGetCmd(presetProgramsParam),
		switchMultilevelGetCmd()
	]
	cmds += switchColorGetChangedCmds()
	return cmds
}


def startFireplaceProgram() { return startProgram(6) }
def startStormProgram() { return startProgram(7) }
def startRainbowProgram() { return startProgram(8) }
def startPolarLightsProgram() { return startProgram(9) }
def startPoliceProgram() { return startProgram(10) }

def startProgram(program) {
	logDebug "startProgram($program)..."
	
	def cmds = [
		configSetCmd(presetProgramsParam, safeToInt(program))
	]
	
	if (assumeSuccessSetting) {	
		handlePresetProgramParamReport(safeToInt(program))
	}
	else {
		cmds << configGetCmd(presetProgramsParam)
	}
	return cmds
}


def stopProgram() {
	logDebug "stopProgram()..."

	return defaultDelayBetween([
		configSetCmd(presetProgramsParam, 0),
		configGetCmd(presetProgramsParam)
	])
}


def on() {
	logDebug "on()..."

	if (activePresetProgram) {
		handlePresetProgramStopping()
	}
	
	def cmds = []

	def rgbw = activeRGBW
	if (!rgbwHasValue(rgbw)) {
		rgbw = lastRGBW
		if (!rgbwHasValue(rgbw)) {
			rgbw = defaultRGBW
		}
		cmds << switchColorSetCmd(rgbw)
		
		if (!assumeSuccessSetting) {
			cmds += switchColorGetChangedCmds()
		}
	}

	storeRGBW(rgbw)

	cmds += getOnOffCmds(0xFF)
	
	if (assumeSuccessSetting) {
		updateAllColorAttributes(rgbw, "on")		
	}

	return defaultDelayBetween(cmds)
}


def off() {
	logDebug "off()..."
	
	if (activePresetProgram) {
		handlePresetProgramStopping()
	}
	
	if (assumeSuccessSetting) {
		updateAllColorAttributes(activeRGBW, "off")
	}
	
	return defaultDelayBetween(getOnOffCmds(0x00))
}

private getOnOffCmds(value) {
	return [
		basicSetCmd(value),
		switchMultilevelGetCmd()
	]
}


def setLevel(level) {
	return setLevel(level, dimmerRampRateRemoteParam.value)
}

def setLevel(level, duration) {
	logDebug "setLevel($level, $duration)..."

	if (!safeToInt(level)) {
		level = 1
	}
	
	if (activePresetProgram) {
		handlePresetProgramStopping()
	}
	
	def cmds = []	
	def rgbw = activeRGBW

	if (deviceIsOff) {
		if (!rgbwHasValue(rgbw)) {
			rgbw = defaultRGBW
			cmds << switchColorSetCmd(rgbw)
			
			if (!assumeSuccessSetting) {
				cmds += switchColorGetChangedCmds()
			}
		}
	}
	
	storeRGBW(rgbw)

	cmds << switchMultilevelSetCmd(level, duration)
	cmds << switchMultilevelGetCmd()
	
	if (assumeSuccessSetting) {
		updateAllColorAttributes(rgbw, "on")		
	}
	
	return defaultDelayBetween(cmds)
}


def setSaturation(percent) {
	setColor([saturation: percent])
}


def setHue(value) {
	setColor([hue: value])
}


def setColor(data) {
	logDebug "setColor(${data})..."

	def rgb

	if (data.red != null && data.green != null && data.blue != null) {
		rgb = [data.red, data.green, data.blue]
	}
	else if (data.hex) {
		rgb = colorUtil.hexToRgb(data.hex)
	}
	else {
		rgb = huesatToRGB(data.hue, data.saturation)
	}

	if (rgb) {
		def rgbw = activeRGBW
		rgbw.red = rgb[0]
		rgbw.green = rgb[1]
		rgbw.blue = rgb[2]
		
		sendCommands(defaultDelayBetween(getDimmerOnCmds("color", rgbw)))
	}
	else {
		log.warn "Unable to get RGB from ${data}"
	}
}


def setWhiteLevel(level) {
	childSetLevel(getChildDNI("white"), level)
}

def setColorLevel(level) {
	childSetLevel(getChildDNI("color"), level)
}

def setRedLevel(level) {
	childSetLevel(getChildDNI("red"), level)
}

def setGreenLevel(level) {
	childSetLevel(getChildDNI("green"), level)
}

def setBlueLevel(level) {
	childSetLevel(getChildDNI("blue"), level)
}

void childSetLevel(childDNI, level, duration=0) {
	logDebug "childSetLevel(${childDNI}, ${level}, ${duration})..."

	if (!level) {
		childOff(childDNI)
	}
	else {
		def childName =  getChildName(childDNI)
		if (childName) {
			def rgbw = activeRGBW

			if (childName != "color") {
				rgbw["${childName}"] = levelToColor(level)
			}
			else {
				def colorLevel = safeToLevel(device.currentValue("colorLevel"))
				def scale = (level / colorLevel)

				rgbw.red = safeToColor(rgbw.red * scale)
				rgbw.green = safeToColor(rgbw.green * scale)
				rgbw.blue = safeToColor(rgbw.blue * scale)
			}

			sendCommands(defaultDelayBetween(getDimmerOnCmds(childName, rgbw)))
		}
		else {
			log.warn "Unknown Child: ${childDNI}"
		}
	}
}


def whiteOn() {
	childOn(getChildDNI("white"))
}

def colorOn() {
	childOn(getChildDNI("color"))
}

def redOn() {
	childOn(getChildDNI("red"))
}

def greenOn() {
	childOn(getChildDNI("green"))
}

def blueOn() {
	childOn(getChildDNI("blue"))
}

void childOn(childDNI) {
	logDebug "childOn(${childDNI})..."

	def childName = getChildName(childDNI)
	if (childName) {

		def program = PRESET_PROGRAMS.find { it.key == childName }
		if (program) {
			sendCommands(startProgram(program.value))
		}
		else if (DIMMER_NAMES.find { it == childName }) {
			def rgbw = activeRGBW
			if (childName == "color") {
				if (!rgbwHasColorValue(rgbw)) {
					rgbw.red = getChildLastColor("red")
					rgbw.green = getChildLastColor("green")
					rgbw.blue = getChildLastColor("blue")
				}
			}
			else {
				if (!rgbw["${childName}"]) {
					rgbw["${childName}"] = getChildLastColor(childName)
				}
			}
			sendCommands(defaultDelayBetween(getDimmerOnCmds(childName, rgbw)))
		}
	}
	else {
		log.warn "Unknown Child: ${childDNI}"
	}
}

private getDimmerOnCmds(childName, rgbw) {	
	if (activePresetProgram) {
		handlePresetProgramStopping()
	}
	
	if (childName == "color") {
		if (!rgbwHasColorValue(rgbw)) {
			rgbw.red = 255
			rgbw.green = 255
			rgbw.blue = 255
		}

		if (deviceIsOff) {
			// Device is off so only color channels should turn on.
			state.lastWhite = rgbw.white
			rgbw.white = 0
		}
	}
	else {
		if (!rgbw["${childName}"]) {
			rgbw["${childName}"] = 255
		}

		if (deviceIsOff) {
			// Device is off so only the specified channel should turn on.
			rgbw.each {
				if (it.key != childName) {
					storeChildLastColor(childName, it.value)
					it.value = 0
				}
			}
		}
	}

	def cmds = [
		switchColorSetCmd(rgbw)
	]
	
	if (deviceIsOff) {
		cmds += getOnOffCmds(0xFF)
	}

	if (assumeSuccessSetting) {
		updateAllColorAttributes(rgbw, "on", childName)		
	}
	else {
		cmds += switchColorGetChangedCmds(rgbw)
	}	
	return cmds
}


def whiteOff() {
	childOff(getChildDNI("white"))
}

def colorOff() {
	childOff(getChildDNI("color"))
}

def redOff() {
	childOff(getChildDNI("red"))
}

def greenOff() {
	childOff(getChildDNI("green"))
}

def blueOff() {
	childOff(getChildDNI("blue"))
}

void childOff(childDNI) {
	logDebug "childOff(${childDNI})..."

	def childName = getChildName(childDNI)
	if (childName) {

		def cmds = []
		if (DIMMER_NAMES.find { it == childName }) {
			cmds += getDimmerOffCmds(childName)
		}
		else if (PRESET_PROGRAMS.find { it.key == childName }) {
			cmds += stopProgram()
		}

		sendCommands(defaultDelayBetween(cmds))

	}
	else {
		log.warn "Unknown Child: ${childDNI}"
	}
}

private getDimmerOffCmds(childName) {
	if (activePresetProgram) {
		handlePresetProgramStopping()
	}
	
	def rgbw = activeRGBW

	if (childName == "color") {
		rgbw.each {
			if (it.key != "white") {
				storeChildLastColor(it.key, it.value)
				rgbw["${it.key}"] = 0
			}
		}
	}
	else if (childName == "white") {
		storeChildLastColor(childName, rgbw.white)
		rgbw.white = 0
	}
	else {
		def lastValue = rgbw["${childName}"]

		rgbw["${childName}"] = 0

		if (rgbwHasColorValue(rgbw)) {
			lastValue = 0
		}
		storeChildLastColor(childName, lastValue)
	}

	def cmds = []
	if (rgbwHasValue(rgbw)) {
		cmds << switchColorSetCmd(rgbw)
		if (!assumeSuccessSetting) {
			cmds += switchColorGetChangedCmds(rgbw)
		}
	}
	else {
		cmds += getOnOffCmds(0x00)
	}
	
	if (assumeSuccessSetting) {
		updateAllColorAttributes(rgbw, "off", childName)		
	}	
	return cmds
}


void childRefresh(childDNI) {
	logDebug "childRefresh(${childDNI})..."

	def childName = getChildName(childDNI)
	if (childName) {
		def cmds = []
		if (childName != "color") {
			def id = COLOR_COMPONENTS.find { it.key == childName }?.value
			if (id != null) {
				cmds << switchColorGetCmd(id)
			}
		}
		else {
			COLOR_COMPONENTS.findAll { it.key != "white" }.each {
				cmds << switchColorGetCmd(it.value)
			}
		}

		cmds << switchMultilevelGetCmd()

		sendCommands(defaultDelayBetween(cmds))
	}
	else {
		log.warn "Unknown Child: ${childDNI}"
	}
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

private basicSetCmd(val) {
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private switchMultilevelSetCmd(level, duration=0) {
	def levelVal = safeToLevel(level, 99)
	def durationVal = validateRange(duration, 0, 0, 100)

	return secureCmd(zwave.switchMultilevelV2.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

private switchMultilevelGetCmd() {
	return secureCmd(zwave.switchMultilevelV2.switchMultilevelGet())
}

private switchColorSetCmd(rgbw) {
	storeRGBW(rgbw)

	return secureCmd(zwave.switchColorV3.switchColorSet(red: safeToInt(rgbw.red), green:safeToInt(rgbw.green), blue:safeToInt(rgbw.blue), warmWhite:safeToInt(rgbw.white)))//, dimmingDuration: dimmerRampRateRemoteParam.value))
}

private switchColorGetChangedCmds(rgbw=[:]) {
	def oldRGBW = activeRGBW

	def cmds = []
	COLOR_COMPONENTS.each {
		if (rgbw["${it.key}"] != oldRGBW["${it.key}"]) {
			cmds << switchColorGetCmd(it.value)
		}
	}
	return cmds
}

private switchColorGetCmd(colorComponentId) {
	return secureCmd(zwave.switchColorV3.switchColorGet(colorComponentId: colorComponentId))
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private associationSetCmd(group) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier:group, nodeId:[zwaveHubNodeId]))
}

private associationRemoveCmd(group) {
	return secureCmd(zwave.associationV2.associationRemove(groupingIdentifier:group))
}

private associationGetCmd(group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier:group))
}

private multiChannelAssociationRemoveCmd(group) {
	return secureCmd(zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:group, nodeId:[zwaveHubNodeId]))
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


private defaultDelayBetween(cmds) {
	return cmds ? delayBetween(cmds, commandDelaySetting) : []
}

private sendCommands(cmds) {
	def actions = []

	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}

	if (actions) {
		sendHubCommand(actions)
	}
	return []
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
	return options?.collectEntries { k, v ->
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

	[30, 45, 60, 90, 120]. each {
		options["${it}"] = "${it} Seconds"
	}

	// (2..15).each {	// max 127 minutes
		// options["${127 + it}"] = "${it} Minutes"
	// }
	return options
}

private getCommandDelayOptions() {
	def options = [50:"50ms"]	
	(1..20).each {
		options["${it * 100}"] = "${it * 100}ms"
	}
	return options
}


def parse(description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			if (descriptionMatchesCommand(description, switchMultilevelReportCommand)) {
				result = handleSwitchMultilevelReport(cmd, description)
			}
			else if (descriptionMatchesCommand(description, switchColorReportCommand)) {
				result = handleSwitchColorReport(cmd, description)
			}
			else {
				result = zwaveEvent(cmd)
			}
		}
		else {
			logDebug("Couldn't zwave.parse '$description'")
		}
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
	// Workaround that was added to all SmartThings Multichannel DTHs.
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	
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
	sendEventIfChanged("firmwareVersion", version)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorSet cmd) {
	logTrace "${cmd}"

	def rgbw = [:]

	COLOR_COMPONENTS.each { name, id ->
		def value = cmd.colorComponents?.find { it.key == id }?.value
		if (value != null) {
			rgbw["${name}"] = value
			state["${name}Value"] = value
			sendChildEvent(name, "level", colorToLevel(value), "%")
		}
	}

	if (rgbw) {
		sendColorEvents(rgbw)

		sendCommands([ switchMultilevelGetCmd() ])

		if (rgbwHasValue(rgbw) && deviceIsOff) {
			// The color was changed with an input switch, but that won't turn the device on so wait 3 seconds and turn it on if it's still off.
			runIn(3, physicalOnBackup)
		}
	}
	return []
}

def physicalOnBackup() {
	if (deviceIsOff) {
		log.warn "turning device on after physical change"
		sendCommands(defaultDelayBetween(getOnOffCmds(0xFF)))
	}
}


def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
	logTrace "${cmd}"
	// Ignoring because SmartThings didn't fully implement the Switch Color v3 command class so the DTH is manually parsing those commands to get the targetValue.
	return []
}

private handleSwitchColorReport(cmd, description) {
	def targetValue = parsePayloadTargetValue(description, switchColorReportCommand, 2)

	logTrace "${cmd}, targetValue: ${targetValue}"

	if (targetValue == null) {
		targetValue = cmd.value
	}

	def name = COLOR_COMPONENTS.find { it.value == cmd.colorComponentId }.key
	if (name) {		
		updateColorAttributes(name, targetValue)
		runIn(2, sendColorEvents)
	}
	return []
}

private updateAllColorAttributes(rgbw, switchValue, childName=null) {	
	logTrace "updateAllColorAttributes($rgbw, $switchValue, $childName)..."
	
	rgbw.each {
		def colorSwitchValue
		if (!childName || (childName == "color" && it.key != "white") || (childName == it.key)) {
			colorSwitchValue = (switchValue == "on" && it.value) ? "on" : "off"
			updateColorAttributes(it.key, it.value, colorSwitchValue)
		}
	}
	
	if (!childName || childName != "white") {
		if (switchValue == "off" && childName != "color" && rgbwHasColorValue(rgbw)) {
			switchValue = ""  // Don't force switch state because R, G, or B was turned off, but other colors are still on.
		}		
		sendColorEvents(rgbw, switchValue)
	}
}

private updateColorAttributes(name, colorValue, switchValue=null) {
	sendChildEvent(name, "level", colorToLevel(colorValue), "%")
		
	state["${name}Value"] = colorValue
	
	if (!switchValue) {
		switchValue = (deviceIsOff || !colorValue) ? "off" : "on"
	}
	
	if (device.currentValue("${name}Switch") != switchValue) {
		sendChildEvent(name, "switch", switchValue)
	}
}


def sendColorEvents(rgbw=null, switchValue=null) {
	logTrace "sendColorEvents(${rgbw})"

	def isDigital = (rgbw = null)
	rgbw = rgbw ?: activeRGBW

	def rgb = rgbw.findAll { it.key != "white" }

	def colorLevel = colorToLevel(rgb?.max { it.value }?.value)
	if (colorLevel) {

		def hsv = rgbToHSV(rgb.red, rgb.green, rgb.blue)
		def hex = rgbToHex(rgb.red, rgb.green, rgb.blue)

		sendEventIfChanged("color", hex)
		sendEventIfChanged("hue", hsv.hue)
		sendEventIfChanged("saturation", hsv.saturation)
	}
	
	sendChildEvent("color", "level", colorLevel, "%")
	
	if (switchValue) {
		sendChildEvent("color", "switch", (switchValue == "off" || !colorLevel) ? "off" : "on")
	}

	if (isDigital) {
		def expectedRGBW = storedRGBW

		def cmds = switchColorGetChangedCmds(expectedRGBW)
		if (cmds) {
			state.storedRGBW = [:]
			sendCommands(defaultDelayBetween(cmds))
		}
	}

	if (switchValue == null) {
		syncChildSwitches(rgbw)
	}
}

private syncChildSwitches(rgbw) {
	def synced = true
	
	rgbw.each {
		def expected = (deviceIsOff || !it.value) ? "off" : "on"
		if (expected != device.currentValue("${it.key}Switch")) {
			synced = false
		}
	}
	
	def expected = (deviceIsOff || !rgbwHasColorValue(rgbw)) ? "off" : "on"
	if (device.currentValue("colorSwitch") != expected) {
		synced = false
	}

	if (!synced) {
		sendCommands([ switchMultilevelGetCmd() ])
	}
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicSet: ${cmd}"
	// Ignoring these reports because SmartThings is automatically requesting them every 5 minutes.
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logTrace "${cmd}"
	// Ignoring these reports because SmartThings doesn't support v4 of the Switch Multilevel command class so the DTH is manually parsing those commands to get the targetValue.
	return []
}

private handleSwitchMultilevelReport(cmd, description) {
	def targetValue = parsePayloadTargetValue(description, switchMultilevelReportCommand, 1)

	logTrace "${cmd}, targetValue: ${targetValue}"

	if (targetValue == null) {
		targetValue = cmd.value
	}

	if (targetValue) {
		sendEventIfChanged("level", targetValue, "%")
	}

	handleSwitchReport(targetValue)

	return []
}

private handleSwitchReport(rawValue) {
	def value = (rawValue ? "on" : "off")

	sendEventIfChanged("switch", value)

	if (value == "off") {
		DIMMER_NAMES.each {
			sendChildEvent(it, "switch", "off")
		}
	}
	else {
		def rgbw = activeRGBW
		rgbw.each {
			sendChildEvent(it.key, "switch", (it.value ? "on" : "off"))
		}
		sendChildEvent("color", "switch", (rgbwHasColorValue(rgbw) ? "on" : "off"))
	}
}


def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		logTrace "CentralSceneNotification: ${cmd}"

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
	logTrace "MeterReport: ${cmd}"

	def val = roundTwoPlaces(cmd.scaledMeterValue)

	if (cmd.scale == meterScalePower) {
		sendEventIfChanged("power", val, "W")
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: ${cmd}"

	if (cmd.parameterNumber == presetProgramsParam.num) {
		handlePresetProgramParamReport(cmd.scaledConfigurationValue)
	}
	else {
		updateSyncStatus("Syncing...")

		runIn(5, updateSyncStatus)

		def param = configParams.find { it.num == cmd.parameterNumber }
		if (param) {

			if (param.num == switchModeParam.num) {
				handleSwitchModeParamReport(cmd.scaledConfigurationValue)
			}

			state["configVal${param.num}"] = cmd.scaledConfigurationValue

			logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		}
		else {
			logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
		}
	}
	return []
}

private handlePresetProgramStopping() {	
	if (assumeSuccessSetting) {
		handlePresetProgramParamReport(0)
	}
	else {
		sendCommands([
			"delay 5000",
			configGetCmd(presetProgramsParam)
		])
	}
}

private handlePresetProgramParamReport(value) {
	def lastValue = activePresetProgram
	
	sendEventIfChanged("activeProgram", "${value}")

	def cmds = [
		"delay 3000"
	]
	
	if (!value) {
		PRESET_PROGRAMS.each {
			sendPresetProgramSwitchEvent(it.value, "off")
		}		
		if (lastValue) {
			cmds += switchColorGetChangedCmds()
		}
	}
	else {
		sendPresetProgramSwitchEvent(value, "on")		
		if (lastValue) {
			sendPresetProgramSwitchEvent(lastValue, "off")
		}
	}

	if (value != lastValue) {
		cmds << switchMultilevelGetCmd()
	}
	
	sendCommands(cmds)
}

private sendPresetProgramSwitchEvent(program, switchValue) {	
	def childName = PRESET_PROGRAMS.find { it.value == program }?.key	
	if (childName) {
		def child = findChildDevice(childName)	
		if (child && child.currentValue("switch") != switchValue) {
			logDebug "${child.displayName}: switch is ${switchValue}" 
			child?.sendEvent(name: "switch", value: switchValue)
		}
	}
}

private handleSwitchModeParamReport(value) {
	if (value != getParamStoredValue(switchModeParam.num)) {

		// The state of the device gets messed up after changing this configuration parameter so set the colors back to their last value and turn the devic eon.

		def cmds = [
			"delay 3000",
			switchColorSetCmd(activeRGBW)
		]

		cmds += getOnOffCmds(0xFF)

		sendCommands(defaultDelayBetween(cmds))
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=null) {
	log.warn "unhandled: $cmd" + ((endpoint != null) ? " (${endpoint})" : "")
	return []
}


private parsePayloadTargetValue(description, command, targetValueIndex) {
	def targetValue = null
	try {
		if (description.contains("command: 9881")) {
			description = description.replace("payload: 00 ${command}","payload:")
		}

		def payload = description.split(", ")?.find { it.startsWith("payload:") }?.replace("payload: ", "")?.split(" ")

		if (payload?.size() > targetValueIndex) {
			targetValue = Integer.parseInt(payload[targetValueIndex], 16)
		}
	}
	catch (e) {
		log.warn "Unable to parse targetValue from ${description}"
	}
	return targetValue
}

private descriptionMatchesCommand(description, command) {
	def insecureCmd = "command: " + "${command}".replace(" ", "")
	def secureCmd = "command: 9881, payload: 00 ${command}"
	return "${description}".contains(insecureCmd) || "${description}".contains(secureCmd)
}

private getSwitchMultilevelReportCommand() { "26 03" }

private getSwitchColorReportCommand() { "33 04" }


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

	sendEvent(name: "syncStatus", value: status, displayed: false)
}

private getPendingChanges() {
	return configParams.count { isConfigParamSynced(it) ? 0 : 1 }
}

private isConfigParamSynced(param) {
	return (!param.options || param.value == getParamStoredValue(param.num))
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"], -1)
}


private storeRGBW(rgbw) {
	state.rgbw = rgbw
	state.rgbwTime = new Date().time
}

private getStoredRGBW() {
	def expiredCutoffMS = (10 * 1000) // 10 seconds
	def rgbwTime = state.rgbwTime ?: 0
	if ((new Date().time - rgbwTime) <= expiredCutoffMS) {
		return state.rgbw
	}
	else {
		return [:]
	}
}

private getActiveRGBW() {
	return [
		red: safeToColor(state.redValue),
		green: safeToColor(state.greenValue),
		blue: safeToColor(state.blueValue),
		white: safeToColor(state.whiteValue)
	]
}

private getLastRGBW() {
	return [
		red: safeToColor(state.lastRed),
		green: safeToColor(state.lastGreen),
		blue: safeToColor(state.lastBlue),
		white: safeToColor(state.lastWhite)
	]
}

private getDefaultRGBW() {
	return [ red: 255, green: 255, blue: 255, white: 255 ]
}


private rgbwHasValue(rgbw) {
	return (rgbwHasColorValue(rgbw) || rgbw?.white)
}

private rgbwHasColorValue(rgbw) {
	return (rgbw?.red || rgbw?.green || rgbw?.blue)
}

private colorToLevel(color) {
	return safeToLevel((safeToInt(safeToColor(color)) / 255) * 100)
}

private levelToColor(level) {
	return safeToColor((safeToLevel(level, 99) / 99) * 255)
}

private safeToColor(value) {
	return validateRange(value, 0, 0, 255)
}

private safeToLevel(value, max=100) {
	return validateRange(value, 100, 0, max)
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


private sendEventIfChanged(name, value, unit="") {
	if (device.currentValue(name) != value) {
		sendEvent(getEventMap(name, value, unit))
	}
	// else {
		// logTrace "${name} is ${value}${unit}"
	// }
}

private getEventMap(name, value, unit="") {
	def desc = "${name} is ${value}${unit}"

	def eventMap = [
		name: name,
		value: value,
		descriptionText: "${device.displayName}: ${desc}"
	]

	if (unit) {
		eventMap.unit = unit
	}

	logDebug "${desc}"

	return eventMap
}

private sendChildEvent(childName, name, value, unit="") {
	if (name in ["level", "switch"]) {
		sendEventIfChanged("${childName}${name.capitalize()}", value, unit)
	}

	def child = findChildDevice(childName)
	if (child) {
		def evtMap = [
			name: name,
			value: value,
			descriptionText: "${child.displayName}: ${name} is ${value}${unit}"
		]

		if (unit) {
			evtMap.unit = unit
		}

		if (child.currentValue(name) != value) {
			child.sendEvent(evtMap)
			// logDebug "${evtMap.descriptionText}"
		}
		// else {
			// logTrace "${evtMap.descriptionText}"
		// }
	}
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


private getDeviceIsOff() {
	return (device.currentValue("switch") == "off")
}

private getActivePresetProgram() {
	return safeToInt(device.currentValue("activeProgram"))
}


private findChildDevice(childName) {
	return childDevices?.find { "${it.deviceNetworkId}".endsWith("-${childName.toUpperCase()}") }
}

private getChildDNI(childName) {
	return "${device.deviceNetworkId}-${childName.toUpperCase()}"
}

private getChildName(childDNI) {
	def names = []
	names += DIMMER_NAMES.collect { it }
	names += PRESET_PROGRAMS.collect { it.key }

	def name = ""
	names.each {
		if ("${childDNI}".endsWith("-${it.toUpperCase()}")) {
			name = it
		}
	}
	return name
}

private storeChildLastColor(childName, value) {
	state["last${childName.capitalize()}"] = value
}

private getChildLastColor(childName) {
	return safeToColor(state["last${childName.capitalize()}"])
}


private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}