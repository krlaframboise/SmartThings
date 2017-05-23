/**
 *  Fibaro Swipe Gesture Controller v1.0.1
 *  (Model: FGGC-001)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-fibaro-swipe/88073?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.0.1 (05/22/2017)
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
		name: "Fibaro Swipe Gesture Controller", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Button"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "lastCheckin", "string"
		attribute "lastUpdate", "string"
		attribute "pendingChanges", "number"
		
		gestures.each { gest ->
			attribute "btn${gest.btnNum}Label", "string"			
		}
		
		fingerprint deviceId: "0x1801", inClusters: "0x56, 0x59, 0x5A, 0x5B, 0x5E, 0x70, 0x72, 0x73, 0x7A, 0x80, 0x84, 0x85, 0x86, 0x8E, 0x98", outClusters: ""
		
		fingerprint mfr:"010F", prod:"0D01", model:"2000", deviceJoinName: "Fibaro SWIPE"
	}

	simulator { }
	
	preferences {
		getOptionsInput(deviceOrientationParam)
		getOptionsInput(powerSavingModeParam)
		getOptionsInput(ledModeParam)		
		
		getOptionsInput("buzzer", "Buzzer Mode", buzzerSetting, buzzerOptions)
		
		getBoolInput("doubleSwipeEnabled", "Enable Double Swipe Gestures?", doubleSwipeEnabledSetting)
		
		gestures.each { gest ->
			getTextInput("btn${gest.btnNum}Label", "${gest.name} Label", "")
			if (gest.seqNum) {
				getOptionsInput("seq${gest.seqNum}", "${gest.name} Gestures", getSeqSetting(gest.seqNum), seqOptions)
			}
		}
				
		getOptionsInput("wakeUpInterval", "Checkin Interval", checkinIntervalSetting, checkinIntervalOptions)
		
		getOptionsInput("batteryReportingInterval", "Battery Reporting Interval", batteryReportingIntervalSetting, checkinIntervalOptions)
				
		getBoolInput("debugOutput", "Enable debug logging?", debugOutputSetting)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "default", label:'', icon:"${iconSwipe}", backgroundColor:"#cccccc"
				attributeState "pushed1", label:'Swipe Up', icon:"${iconUp}"
				attributeState "pushed2", label:'Swipe Down', icon:"${iconDown}"
				attributeState "pushed3", label:'Swipe Left', icon:"${iconLeft}"
				attributeState "pushed4", label:'Swipe Right', icon:"${iconRight}"
				attributeState "held5", label:'CW Circle Started', icon:"${iconCircleCW}"
				attributeState "pushed5", label:'CW Circle Stopped', icon:"${iconCircleCW}"
				attributeState "held6", label:'CCW Circle Started', icon:"${iconCircleCCW}"
				attributeState "pushed6", label:'CCW Circle Stopped', icon:"${iconCircleCCW}"
				attributeState "pushed7", label:'Dbl Swipe Up', icon:"${iconDoubleUp}"
				attributeState "pushed8", label:'Dbl Swipe Down', icon:"${iconDoubleDown}"
				attributeState "pushed9", label:'Dbl Swipe Left', icon:"${iconDoubleLeft}"
				attributeState "pushed10", label:'Dbl Swipe Right', icon:"${iconDoubleRight}"
				attributeState "pushed11", label:'Sequence 1', icon:"${iconSwipe}"
				attributeState "pushed12", label:'Sequence 2', icon:"${iconSwipe}"
				attributeState "pushed13", label:'Sequence 3', icon:"${iconSwipe}"
				attributeState "pushed14", label:'Sequence 4', icon:"${iconSwipe}"
				attributeState "pushed15", label:'Sequence 5', icon:"${iconSwipe}"
				attributeState "pushed16", label:'Sequence 6', icon:"${iconSwipe}"
				
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "secondaryStatus", label:'${currentValue}'			
			}
		}
		
		valueTile("btn1Label", "device.btn1Label", width: 2, height: 2) {
			state "default", label:'Button 1\n${currentValue}', icon: "${iconUp}"
		}		
				
		valueTile("btn2Label", "device.btn2Label", width: 2, height: 2) {
			state "default", label:'Button 2\n${currentValue}', icon: "${iconDown}"
		}		
				
		valueTile("btn3Label", "device.btn3Label", width: 2, height: 2) {
			state "default", label:'Button 3\n${currentValue}', icon: "${iconLeft}"
		}		
		
		valueTile("btn4Label", "device.btn4Label", width: 2, height: 2) {
			state "default", label:'Button 4\n${currentValue}', icon: "${iconRight}"
		}
		
		valueTile("btn5Label", "device.btn5Label", width: 2, height: 2) {
			state "default", label:'Button 5\n${currentValue}', icon: "${iconCircleCW}"
		}
		
		valueTile("btn6Label", "device.btn6Label", width: 2, height: 2) {
			state "default", label:'Button 6\n${currentValue}', icon: "${iconCircleCCW}"
		}
		
		valueTile("btn7Label", "device.btn7Label", width: 2, height: 2) {
			state "default", label:'Button 7\n${currentValue}', icon: "${iconDoubleUp}"
		}		
				
		valueTile("btn8Label", "device.btn8Label", width: 2, height: 2) {
			state "default", label:'Button 8\n${currentValue}', icon: "${iconDoubleDown}"
		}		
				
		valueTile("btn9Label", "device.btn9Label", width: 2, height: 2) {
			state "default", label:'Button 9\n${currentValue}', icon: "${iconDoubleLeft}"
		}		
		
		valueTile("btn10Label", "device.btn10Label", width: 2, height: 2) {
			state "default", label:'Button 10\n${currentValue}', icon: "${iconDoubleRight}"
		}
				
		valueTile("btn11Label", "device.btn11Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
		}		
				
		valueTile("btn12Label", "device.btn12Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
		}		
				
		valueTile("btn13Label", "device.btn13Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
		}		
				
		valueTile("btn14Label", "device.btn14Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
		}		
		
		valueTile("btn15Label", "device.btn15Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
		}		
		
		valueTile("btn16Label", "device.btn16Label", width: 3, height: 2) {
			state "default", label:'${currentValue}'
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
			state "-1", label:'Updating Settings'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}

		main "mainTile"
		details(["mainTile", "battery", "pending", "btn1Label", "btn2Label", "btn3Label", "btn4Label", "btn5Label", "btn6Label", "btn7Label", "btn8Label", "btn9Label", "btn10Label", "btn11Label", "btn12Label", "btn13Label", "btn14Label", "btn15Label", "btn16Label", "refresh", "lastUpdate"])
	}
}

private getTextInput(name, title, defaultVal) {
	input "${name}", "text", 
		title: "${title}:", 
		defaultValue: defaultVal, 
		required: false
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
	input "${param.prefName}", "enum",
		title: "${param.name}:",
		defaultValue: "${param.val}",
		required: false,
		displayDuringSetup: true,
		options: param.options?.collect { name, val -> name }
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		syncSettingAttributes()
		
		if (checkForPendingChanges()) {
			logForceWakeupMessage("The configuration will be updated the next time the device wakes up.")
		}		
	}	
	return []
}

private syncSettingAttributes() {
	gestures.each {				
		// Sync Button Labels
		def labelAttr = "btn${it.btnNum}Label"
		def labelVal = getBtnLabelSetting(it.btnNum)
		
		if (!doubleSwipeEnabledSetting && it.name.contains("Double")) {
			labelVal = "[Double Swipe Disabled]"
		}
		
		if (it.seqNum) {
			labelVal = "Sequence ${it.seqNum}\n(${getSeqSetting(it.seqNum)})\nButton ${it.btnNum}\n${labelVal}"
		}
		
		if (getAttrValue(labelAttr) != labelVal) {
			sendEvent(createEventMap(labelAttr, labelVal, false))
		}
	}	
}

def configure() {	
	logTrace "configure()"
	def cmds = []

	if (!getAttrValue("numberOfButtons")) {
		sendEvent(name: "numberOfButtons", value: gestures.size())
	}
	
	if (getAttrValue("pendingChanges") == null) {
		logTrace "Waiting 1 second because this is the first time being configured"
		cmds << "delay 1000"		
	}
	
	if (state.refreshAll || state.checkinIntervalSeconds != checkinIntervalSettingSeconds) {
		cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		cmds << wakeUpIntervalGetCmd()
	}
			
	configParams.each { param ->	
		cmds += updateConfigVal(param)
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
	
	if (state.checkinIntervalSeconds != checkinIntervalSettingSeconds) {
		changes += 1
	}
	
	configParams.each {
		if (hasPendingChange(it)) {
			changes += 1
		}
	}
	
	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, false))
	}
	return (changes != 0)
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
	sendEvent(createEventMap("pendingChanges", configParams.size(), false))
	state.refreshAll = true
	logForceWakeupMessage("The sensor data will be refreshed the next time the device wakes up.")
	return []
}

def parse(String description) {
	def result = []
	logTrace "parse: $description"
	
	if (!isDuplicateCommand(state.lastCheckin, 60000)) {
		state.lastCheckin = new Date().time
		result << createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
	
	if (!description?.startsWith("Err")) {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			// logTrace "Parse: $cmd"
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		// logTrace "secure cmd: $encapCmd"
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"	
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def result = []
	def cmdClass = getVersionSafeCmdClass(cmd.commandClass)
	def parsedCmd = cmdClass?.command(cmd.command)?.parse(cmd.data)
	if (parsedCmd) {
		logTrace "Parsed crc16: ${parsedCmd}"
		result += zwaveEvent(parsedCmd)
	}
	else {
		log.warn "Unable to parse crc16encap command"
	}
	return result	
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (checkForPendingChanges()) {
		result += configure()
	}
	
	if (canReportBattery()) {
		result << batteryGetCmd()
	}
		
	if (result) {
		result << "delay 1200"
	}	
	result << wakeUpNoMoreInfoCmd()
	return sendResponse(result)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	def result = []
	
	state.checkinIntervalSeconds = cmd.seconds
	
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 3) + (5 * 60))
	
	result << createEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return result
}

private sendResponse(cmds) {
	return response(cmds)	
}

private canReportBattery() {
	def reportEveryMS = (convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled Command: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def result = []
	
	def val = (cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	
	result << createEvent(createEventMap("battery", val, null, null, "%"))
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def result = []
	// logTrace "ConfigurationReport: ${cmd}"
	if (getAttrValue("pendingChanges") != -1) {
		result << createEvent(createEventMap("pendingChanges", -1, false))
	}
	
	def val = (cmd.scaledConfigurationValue == -1 ? 255 : cmd.scaledConfigurationValue)
		
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
	
	runIn(10, finalizeConfiguration)
	return result
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	
	state.refreshAll = false
	
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), false))	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	// logTrace "CentralSceneNotification: ${cmd}"
	def result = []
	def circleGestureStart = 2
	
	def gesture = gestures.find{ it.sceneNum == cmd.sceneNumber && (it.keyAttr == cmd.keyAttributes || cmd.keyAttributes == 1) }
	
	logDebug "${gesture?.name} (keyAttributes:${cmd.keyAttributes}, sequenceNumber:${cmd.sequenceNumber})"
		
	if (gesture) {
		def val = (cmd.keyAttributes == circleGestureStart) ? "held" : "pushed"
	
	result << createEvent(
			name: "button", 
			value: val, 
			descriptionText: "${gesture.name} / Button ${gesture.btnNum} ${val}",
			data: [buttonNumber: gesture.btnNum],			
			displayed: true,
			isStateChange: true)
		
		result << createEvent(createEventMap("primaryStatus", "${val}${gesture.btnNum}", false))
		
		result << createEvent(createEventMap("secondaryStatus", "Button #${gesture.btnNum} ${val.capitalize()}", false))
		
		runIn(3, resetStatuses)
	}	
	return result
}

def resetStatuses() {
	sendEvent(createEventMap("primaryStatus", "", false))
	sendEvent(createEventMap("secondaryStatus", "", false))
}

private wakeUpIntervalSetCmd(val) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId))
}

private wakeUpIntervalGetCmd(val) {
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
	if (canSecureCmd(cmd)) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private canSecureCmd(cmd) {
	// This code was extracted from example by @ClassicGOD
	return zwaveInfo?.zw?.contains("s") && zwaveInfo?.sec?.contains(Integer.toHexString(cmd.commandClassId)?.toUpperCase())
}

private getCommandClassVersions() {
	[	        
		0x20: 1,	// Basic
		0x56: 1,	// Crc16 Encap
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5B: 1,	// Central Scene (2)
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration (1?)
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association (3)
		0x98: 1		// Security
	]
}

private getVersionSafeCmdClass(cmdClass) {
	def version = commandClassVersions[safeToInt(cmdClass)]
	if (version) {
		return zwave.commandClass(cmdClass, version)
	}
	else {
		return zwave.commandClass(cmdClass)
	}
}


// Configuration Parameters
private getConfigParams() {
	def params = [
		deviceOrientationParam,
		buzzerModeParam,
		buzzerEnabledParam,
		ledModeParam,
		toggleModeParam,
		lifelineScenesParam,
		powerSavingModeParam
		// poweringModeParam
	]	
	params += seqParams	
	return params
}

private getSeqParams() {
	def params = []
	(1..6).each {
		params << createConfigParamMap((it + 30), "Sequence ${it}", 2, null, null, (convertOptionSettingToInt(seqOptions, getSeqSetting(it))))
	}
	return params
}

private getDeviceOrientationParam() {
	return createConfigParamMap(1, "Device Orientation", 1, ["Default Orientation${defaultOptionSuffix}":0, "180 Degree Rotation":1, "90 Degree Clockwise Rotation":2, "90 Degree Counter-Clockwise Rotation":3], "deviceOrientation")
}

private getPowerSavingModeParam() {
	return createConfigParamMap(6, "Power Saving Mode", 1, ["Standby Mode (Hold Gesture Required)${defaultOptionSuffix}":0, "Simple Mode (Slow Gestures)":1, "Power Saving Disabled":2], "powerSavingMode")
}

// private getPoweringModeParam() {
	// return createConfigParamMap(5, "Check if powered by USB every", 2,  null, "powerSavingMode")
// }

private getLedModeParam() {
	return createConfigParamMap(3, "LED Mode", 1, ["Disabled${defaultOptionSuffix}": 0, "Enabled":1], "ledMode")
}

private getBuzzerEnabledParam() {
	return createConfigParamMap(2, "Buzzer Enabled", 1, null, null, (convertOptionSettingToInt(buzzerOptions, buzzerSetting) > 0 ? 1 : 0))
}

private getBuzzerModeParam() {	
	return createConfigParamMap(4, "Buzzer Mode", 1, null, null, (convertOptionSettingToInt(buzzerOptions, buzzerSetting) ?: 3))
}

private getLifelineScenesParam() {
	return createConfigParamMap(10, "LifeLine Scenes", 1, null, null, 63) // Send all scenes to lifeline group.
}

private getToggleModeParam() {
	def val = 15 // Enable for all assocation groups
	if (doubleSwipeEnabledSetting) {
		val = 0 // Disable for all groups.
	}
	return createConfigParamMap(12, "Toggle Mode", 1, null, null, val)
}


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

private getGestures() {
	def gestures = [
		[name: "Swipe Up", sceneNum: 1, keyAttr:0, btnNum: 1, icon: iconUp, simple: true],
		[name: "Swipe Down", sceneNum: 2, keyAttr:0, btnNum: 2, icon: iconDown, simple: true],
		[name: "Swipe Left", sceneNum: 3, keyAttr:0, btnNum: 3, icon: iconLeft, simple: true],
		[name: "Swipe Right", sceneNum: 4, keyAttr:0, btnNum: 4, icon: iconRight, simple: true],
		[name: "Clockwise Circle", sceneNum: 5, keyAttr: 2, btnNum: 5, icon: iconCircleCW],
		[name: "Counter-Clockwise Circle", sceneNum: 6, keyAttr: 2, btnNum: 6, icon: iconCircleCCW],
		[name: "Double Swipe Up", sceneNum: 1, keyAttr:3, btnNum: 7, icon: iconDoubleUp],
		[name: "Double Swipe Down", sceneNum: 2, keyAttr:3, btnNum: 8, icon: iconDoubleDown],
		[name: "Double Swipe Left", sceneNum: 3, keyAttr:3, btnNum: 9, icon: iconDoubleLeft],
		[name: "Double Swipe Right", sceneNum: 4, keyAttr:3, btnNum: 10, icon: iconDoubleRight]
	]
	gestures += sequences
	return gestures
}

private getSequences() {
	def items = []
	(1..6).each {
		items << [seqNum: it, name: "Sequence ${it}", sceneNum: (it + 6), keyAttr: 0, btnNum: (it + 10)]
	}
	return items
}

private getIconSwipe() { "${resourcesUrl}/swipe.png" }
private getIconUp() { "${resourcesUrl}/up.png" }
private getIconDown() { "${resourcesUrl}/down.png" }
private getIconLeft() { "${resourcesUrl}/left.png" }
private getIconRight() { "${resourcesUrl}/right.png" }
private getIconDoubleUp() { "${resourcesUrl}/double-up.png" }
private getIconDoubleDown() { "${resourcesUrl}/double-down.png" }
private getIconDoubleLeft() { "${resourcesUrl}/double-left.png" }
private getIconDoubleRight() { "${resourcesUrl}/double-right.png" }
private getIconCircleCW() { "${resourcesUrl}/clockwise.png" }
private getIconCircleCCW() { "${resourcesUrl}/counter-clockwise.png" }

private getResourcesUrl() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/fibaro-swipe"
}

// Settings
private getCheckinIntervalSettingSeconds() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
}

private getCheckinIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getDoubleSwipeEnabledSetting() {
	return (settings?.doubleSwipeEnabled != null) ? settings?.doubleSwipeEnabled : true
}

private getBuzzerSetting() {
	return (settings?.buzzer != null) ? settings?.buzzer : findDefaultOptionName(buzzerOptions)
}

private getDebugOutputSetting() {
	return (settings?.debugOutput != null) ? settings?.debugOutput : true
}

private getBtnLabelSetting(btnNum) {
	def name = "btn${btnNum}Label"
	return (settings && settings[name] != null) ? settings[name] : ""	
}

private getSeqSetting(seqNum) {
	def name = "seq${seqNum}"
	return (settings && settings[name] != null) ? settings[name] : ""
}


private getSeqOptions() {
	def options = ["": 0]
	def simpleGests = gestures.findAll { it.simple }?.each { it.name = it.name.replace("Swipe ", "") }
	
	simpleGests.each { gest1 ->	
		simpleGests.each { gest2 ->

			if (gest1 != gest2) {
				options << getSeqOption(gest1, gest2, null)
			}
		
			simpleGests.each { gest3 ->				
				if (gest1 != gest2 && gest2 != gest3) {				
					options << getSeqOption(gest1, gest2, gest3)
				}
			}
		}
	}
	return options.sort()
}

private getSeqOption(gest1, gest2, gest3) {	
	def name = "${gest1.name} / ${gest2.name}"

	if (gest3) {
		name = "${name} / ${gest3.name}"
	}
	
	def val = ((gest1.btnNum * 256) + (gest2.btnNum * 16) + (gest3?.btnNum ?: 0))
	return ["${name}":val]
}

private getBuzzerOptions() {
	return [
		"Disabled": 0, 
		"Successful Recognition": 1, 
		"Failed Recognition": 2, 
	 	"All Recognition${defaultOptionSuffix}": 3
	]
}

private getCheckinIntervalOptions() {
	return getIntervalOptions((2 * 60 * 60), [min:(5 * 60), max:(18*60*60)])
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
	logDebug "${msg}  You can force the device to wake up by holding your hand in front of the screen until it enters the menu and then swiping up."
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def newVal = "${value}"	
	def isStateChange = displayed ?: (getAttrValue(name) != newVal)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed
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