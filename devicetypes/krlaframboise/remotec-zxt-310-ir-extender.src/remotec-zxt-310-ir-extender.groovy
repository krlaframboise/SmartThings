/**
 *  Remotec ZXT-310 IR Extender v1.0.2
 *     Remotec Z-Wave-to-AV IR Extender(Model: ZXT-310)
 *  
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *
 *  Changelog:
 *
 *  1.0.2 (10/13/2017)
 *    	- SmartThings broke the ability to set state values to null so added workaround.
 *
 *  1.0.1 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *    	- Bug fix for location timezone issue.
 *
 *  1.0.0 (04/02/2017)
 *    - Initial Release
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
	definition (name: "Remotec ZXT-310 IR Extender", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Button"
		capability "Momentary"
		capability "Configuration"		
		capability "Health Check"
				
		attribute "lastCheckin", "string"
		attribute "remoteStatus", "string"
		attribute "learningMode", "string"
		attribute "activeEP", "number"
		
		epOptions.each { ep ->
			attribute "ep${ep.num}Data", "string"
		}
		
		btnOptions.each { btn ->
			attribute "btn${btn.num}Status", "string"
			command "pushButton${btn.num}"
		}
		
		command "pushLearn"
		command "pushButton"
				
		epOptions.each { ep ->
			command "setActiveEP${ep.num}"
		}
						
		fingerprint mfr: "5254", prod: "0100", model: "8371"
	}

	simulator {

	}
	
	preferences {
		epOptions.findAll { it.num != 1 }?.each { ep ->
			input "ep${ep.num}Port", "enum",
				title: "Port for ${ep.name} Buttons:",
				displayDuringSetup: true,
				required: false,
				defaultValue: getEpPortSetting(ep.num),
				options: portOptions.collect { it.name }
		}
		
		btnOptions.each { btn ->
			input "btn${btn.num}Trigger", "enum",
				title: "Active EP - Button ${btn.num} Trigger:",
				displayDuringSetup: true,
				required: false,
				defaultValue: getBtnTriggerSetting(btn.num),
				options: btnTriggerOptions.collect { it.name }
		}				
		input "checkinInterval", "enum",
			title: "Checkin Interval:",			
			displayDuringSetup: true,
			required: false,
			defaultValue: checkinIntervalSetting,
			options: checkinIntervalOptions.collect { it.name }
		input "switchAutoOff", "bool",
			title: "Enable Swith Auto Off?",
			displayDuringSetup: true,
			required: false,
			defaultValue: switchAutoOffSetting
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			displayDuringSetup: false, 
			required: false,
			defaultValue: debugOutputSetting
	}

	tiles(scale: 2) {
		standardTile("epStatus", "device.epStatus", width: 2, height: 2, key: "PRIMARY_CONTROL", canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", icon: "st.unknown.zwave.remote-controller"
		}
		//st.Weather.weather12 (moisture)
		standardTile("switch", "device.switch", width: 2, height: 2) {
			state "on", label: '${name}', action: "switch.off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}		
		standardTile("ep1", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP1', action: "setActiveEP1", backgroundColor: "#ffffff"
			state "1", label: 'EP1', action: "", backgroundColor: assignedBtnColor
		}
		standardTile("ep2", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP2', action: "setActiveEP2", backgroundColor: "#ffffff"
			state "2", label: 'EP2', action: "", backgroundColor: assignedBtnColor
		}
		standardTile("ep3", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP3', action: "setActiveEP3", backgroundColor: "#ffffff"
			state "3", label: 'EP3', action: "", backgroundColor: assignedBtnColor
		}
		standardTile("ep4", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP4', action: "setActiveEP4", backgroundColor: "#ffffff"
			state "4", label: 'EP4', action: "", backgroundColor: assignedBtnColor
		}
		standardTile("ep5", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP5', action: "setActiveEP5", backgroundColor: "#ffffff"
			state "5", label: 'EP5', backgroundColor: assignedBtnColor
		}
		standardTile("ep6", "device.activeEP", width: 1, height: 1) {
			state "default", label: 'EP6', action: "setActiveEP6", backgroundColor: "#ffffff"
			state "6", label: 'EP6', backgroundColor: assignedBtnColor
		}		
		standardTile("learn", "device.learningMode", width: 2, height: 2) {
			state "default", label: "Learn", action: "pushLearn", backgroundColor: "#ffffff"
			state "on", label: "Learn", action: "pushLearn", backgroundColor: learningBtnColor
		}
		standardTile("remoteStatus", "device.remoteStatus", width: 2, height: 2) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff"
		}
		standardTile("btn1Status", "device.btn1Status", width: 2, height: 2) {
			state "unassigned", label: '1\n(empty)', action: "pushButton1", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '1\n(learn)', action: "pushButton1", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '1', action: "pushButton1", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn2Status", "device.btn2Status", width: 2, height: 2) {
			state "unassigned", label: '2\n(empty)', action: "pushButton2", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '2\n(learn)', action: "pushButton2", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '2', action: "pushButton2", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn3Status", "device.btn3Status", width: 2, height: 2) {
			state "unassigned", label: '3\n(empty)', action: "pushButton3", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '3\n(learn)', action: "pushButton3", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '3', action: "pushButton3", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn4Status", "device.btn4Status", width: 2, height: 2) {
			state "unassigned", label: '4\n(empty)', action: "pushButton4", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '4\n(learn)', action: "pushButton4", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '4', action: "pushButton4", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn5Status", "device.btn5Status", width: 2, height: 2) {
			state "unassigned", label: '5\n(empty)', action: "pushButton5", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '5\n(learn)', action: "pushButton5", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '5', action: "pushButton5", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn6Status", "device.btn6Status", width: 2, height: 2) {
			state "unassigned", label: '6\n(empty)', action: "pushButton6", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '6\n(learn)', action: "pushButton6", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '6', action: "pushButton6", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn7Status", "device.btn7Status", width: 2, height: 2) {
			state "unassigned", label: '7\n(empty)', action: "pushButton7", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '7\n(learn)', action: "pushButton7", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '7', action: "pushButton7", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn8Status", "device.btn8Status", width: 2, height: 2) {
			state "unassigned", label: '8\n(empty)', action: "pushButton8", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '8\n(learn)', action: "pushButton8", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '8', action: "pushButton8", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn9Status", "device.btn9Status", width: 2, height: 2) {
			state "unassigned", label: '9\n(empty)', action: "pushButton9", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "learning", label: '9\n(learn)', action: "pushButton9", nextState: "learning", backgroundColor: learningBtnColor
			state "assigned", label: '9', action: "pushButton9", nextState: "assigned", backgroundColor: assignedBtnColor
		}		
		
		main ("epStatus")
		details(["ep1", "ep2", "ep3", "ep4", "ep5", "ep6","learn", "remoteStatus", "switch", "btn1Status", "btn2Status", "btn3Status", "btn4Status", "btn5Status", "btn6Status", "btn7Status", "btn8Status", "btn9Status"])
	}
}

private getUnassignedBtnColor() { return "#ffffff" }
private getAssignedBtnColor() { return "#79b821" }
private getLearningBtnColor() { return "#00ffff" }

def updated() {		
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time

		logTrace "Executing updated()"
		
		def cmds = configure()
		return cmds ? sendResponse(cmds) : []
	}
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	if (device.currentValue("checkInterval") != checkInterval) {
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	switch (checkinIntervalSettingMinutes) {
		case 5:
			runEvery5Minutes(healthPoll)
			break
		case 10:
			runEvery10Minutes(healthPoll)
			break
		case 15:
			runEvery15Minutes(healthPoll)
			break
		case 30:
			runEvery30Minutes(healthPoll)
			break
		case [60, 120]:
			runEvery1Hour(healthPoll)
			break
		default:
			runEvery3Hours(healthPoll)			
	}
}

// Executed by internal schedule and requests version report to determine if the device is still online.
def healthPoll() {
	// logTrace "Executing healthPoll()"
	sendHubCommand(new physicalgraph.device.HubAction(versionGetCmd()))
}

// Executed by SmartThings if the specified checkInterval is exceeded.
def ping() {
	logTrace "Executing ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return versionGetCmd()
	}	
}

def configure() {
	logTrace "Executing configure()"
	
	if (!state.ep1Data) {
		initializeEPData()
	}
	
	if (!state.isConfigured) {		
		initializeAttr("numberOfButtons", btnOptions.size())
		initializeAttr("button", "pushed")
		initializeAttr("switch", "off")
		initializeAttr("level", 100)
		initializeAttr("learningMode", "off")
		setActiveEP(1)	
		state.isConfigured = true
	}
	
	initializeCheckin()
	
	def cmds = []
	epOptions.each { ep ->
		def oldVal = state["ep${ep.num}Data"]?.port
		def newVal = getEpPortSettingValue(ep.num)
		
		if (oldVal != newVal) {
			if (ep.num != 1) {				
				cmds << configSetCmd(irPortMappingParam, newVal, ep.num)
				cmds << configGetCmd(irPortMappingParam, ep.num)
			}
		}
	}
	return cmds ? delayBetween(cmds, 1000) : []	
}

private initializeAttr(name, value) {
	if (!device?.currentValue(name)) {
		sendEvent(createEventMap(name, value, false))
	}
}

private initializeEPData() {
	epOptions.each { ep ->
		def name = "ep${ep.num}Data"
		def epData = state["$name"] ?: [epNum: ep.num]
		btnOptions.each { btn ->
			epData["btn${btn.num}Status"] = "unassigned"
		}
		if (ep.num == 1) {
			epData.port = 1 // EP 1 Port can't be changed
		}		
		state["$name"] = epData
		sendEPDataEvent("$name", epData)
	}
}

def on() { 
	logTrace "Executing on()"
	sendEvent(createEventMap("switch", "on", false))
	if (switchAutoOffSetting) {
		runIn(1, autoOff)
	}
	return pushTriggeredBtns("on")
}

def autoOff() {
	logTrace "Executing autoOff()"
	def cmds = []
	off()?.each {
		cmds << new physicalgraph.device.HubAction(it)
	}
	if (cmds) {
		sendHubCommand(cmds)
	}	
}

def off() {
	logTrace "Executing off()"
	sendEvent(createEventMap("switch", "off", false))
	return pushTriggeredBtns("off")
}

def push() {
	logTrace "Executing push()"
	return pushTriggeredBtns("push")
}

private pushTriggeredBtns(eventName) {
	def result = []
	btnOptions.each { btn ->
		if (eventName in getBtnTriggerSettingEvents(btn.num)) {
			result += pushButton(btn.num, null, true)
			result << "delay 500"
		}
	}
	return result
}

def setLevel(level, rate=null) {
	logTrace "Executing setLevel($level)"
	return pushButton(extractBtnFromLevel(level))	
}

private extractBtnFromLevel(level) {
	def btn = safeToInt(level, 1)
	if (btn >= 10) {
		if ((btn % 10) != 0) {
			btn = (btn - (btn % 10))
		}
		btn = (btn / 10)
	}
	return btn
}

def setActiveEP1() { return setActiveEP(1) }
def setActiveEP2() { return setActiveEP(2) }
def setActiveEP3() { return setActiveEP(3) }
def setActiveEP4() { return setActiveEP(4) }
def setActiveEP5() { return setActiveEP(5) }
def setActiveEP6() { return setActiveEP(6) }

private setActiveEP(epNum) {
	logDebug "Executing setActiveEP(${epNum})"	
	
	sendEvent(createEventMap("activeEP", epNum, false))
	sendEvent(createEventMap("epStatus","EP${epNum}", false))
	sendRemoteStatusEvent("")

	def epData = state["ep${epNum}Data"] ?: [:]
	btnOptions.each { btn ->
		def attrName = "btn${btn.num}Status"
		sendEvent(createEventMap("$attrName", (epData["$attrName"] ?: "unassigned"), false))
	}
}


def pushLearn() {
	logTrace "Executing pushLearn()"
	def learning = "off"
	def status = ""
	def autoReset = false
	
	if (!learningModeActive) {
		learning = "on"
		status = "Learning On\nPush Button (1-9)"
	}
	else if (state.activeLearnBtn) {
		updateBtnStatus(state.activeLearnBtn, "assigned")
		status = "${state.activeLearnBtn.name}\nLearned"
		state.activeLearnBtn = false
		autoReset = true
	}
	
	sendEvent(createEventMap("learningMode", learning, false))
	sendRemoteStatusEvent(status, autoReset)
}

def pushButton1() { return pushButton(1) }
def pushButton2() { return pushButton(2) }
def pushButton3() { return pushButton(3) }
def pushButton4() { return pushButton(4) }
def pushButton5() { return pushButton(5) }
def pushButton6() { return pushButton(6) }
def pushButton7() { return pushButton(7) }
def pushButton8() { return pushButton(8) }
def pushButton9() { return pushButton(9) }

def pushButton(String jsonData) {
	def slurper = new groovy.json.JsonSlurper()
	def data = slurper.parseText(jsonData)
	return pushButton(data.buttonNumber, data.epNum, true, data.delay, data.repeat)
}

def pushButton(btnNum, epNum=null, ignoreLearning=false, delay=null, repeat=null) {
	def btn = [
		num: btnNum,
		ep: epNum ?: activeEPNum,
		delay: delay,
		repeat: repeat,
		oldStatus: device.currentValue("btn${btnNum}Status")
	]
	btn.name = "EP${btn.ep} Button ${btn.num}"

	def result = []
	if (!ignoreLearning && btn.oldStatus == "learning") {
		result += resetLearnKey(btn)
	}
	else if (!ignoreLearning && learningModeActive) {
		result += learnKey(btn)
	}
	else {
		result += sendKey(btn)
	}
	return result
}

private getActiveEPNum() {
	return device.currentValue("activeEP") ?: 1
}

private getLearningModeActive() {
	return (device.currentValue("learningMode") == "on")
}

private updateBtnStatus(btn, status) {
	def epEvent = (status != "learning")	
	updateEPData(btn.ep, "btn${btn.num}Status", status ?: "unassigned", epEvent, true)
}

private updateEPData(epNum, fieldName, val, epEvent=true, fieldEvent=false) {
	def epName = "ep${epNum}Data"
		
	state["$epName"]["$fieldName"] = val
	
	if (epEvent) {
		sendEPDataEvent(epNum, state["$epName"])
	}	
	
	if (fieldEvent) {
		sendEvent(name: fieldName, value: val, displayed: false, isStateChange: true)
	}
}

private sendEPDataEvent(epNum, data) {
	def jsonOutput = new groovy.json.JsonOutput()
	def jsonData = jsonOutput.toJson(data)	
	sendEvent(name: "ep${epNum}Data", value: jsonData, displayed: false, isStateChange: true)
}

private resetLearnKey(btn) {
	logDebug "Resetting ${btn.name}"
	state.activeLearnBtn = false
	updateBtnStatus(btn, "")
	sendRemoteStatusEvent("${btn.name} Reset", true)
	return []
}

private learnKey(btn) {
	def result = []
	
	if (state.activeLearnBtn) {
		logDebug "Cancelling Learning because button ${btn.num} was pushed while learning button ${state.activeLearnBtn.num}"
		updateBtnStatus(state.activeLearnBtn, state.activeLearnBtn.oldStatus)
		state.activeLearnBtn = false
		pushLearn()
	}
	else {
		logDebug "Learning Code for ${btn.name}"
		def key = btnOptions.find { it.num == btn.num }?.key
		if (key) {			
			state.activeLearnBtn = btn
			sendRemoteStatusEvent("Hold Remote button until LED blinks twice then Tap Learn")
			updateBtnStatus(btn, "learning")
					
			// result += delayBetween([
				// configSetCmd(irCodeLearningParam, key, btn.ep),
				// configGetCmd(learningStatusParam, btn.ep)
			// ], 16000)
			result << configSetCmd(irCodeLearningParam, key, btn.ep)
		}
		else {
			logTrace "Key Number not found for ${btn.name}"
		}
	}
	return result
}

private sendKey(btn) {
	logDebug "Sending ${btn.name} Code"
	def result = []
	def keyHex = btnOptions.find { it.num == btn.num }?.keyHex
	if (keyHex) {
		state.activeBtn = btn
		
		for (int i = 0; i <= safeToInt(btn.repeat, 0); i++) {		
			if (btn.delay) {
				result << "delay ${btn.delay}"
			}		
			result += delayBetween([
				simpleAvControlSetCmd(keyHex, btn.ep),
				basicGetCmd(btn.ep)
			], 1000)
		}
	}
	else {
		logTrace "KeyHex not found for ${btn.name}"
	}
	return result
}


def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	
	if (cmd) {
		result += zwaveEvent(cmd)		
	}
	else {
		logDebug "Unknown Description: $description"
	}	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		result << createLastCheckinEvent()
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	
	if (encapsulatedCommand) {
		// logTrace "MultiChannelCmdEncap: ${encapsulatedCommand}"
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, ep=null) {
	// logTrace "ConfigurationReport${getEPSuffix(ep)}: ${cmd}\nPayload: ${cmd.payload}"
	def val = (cmd.scaledConfigurationValue == -1 ? 255 : cmd.scaledConfigurationValue)
	def result = []
	
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		logDebug "${configParam.name}${getEPSuffix(ep)} = ${val}"
		if (configParam.attr) {
			result << createEvent(name: configParam.attr, value: val, displayed: false)
		}
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber}${getEPSuffix(ep)} = ${val}"
	}
		
	if (cmd.parameterNumber == learningStatusParam.num) {
		result += handleLearningStatus(safeToInt(val, 0))
	}
	if (cmd.parameterNumber == irPortMappingParam.num) {
		updateEPData(ep, "port", val)
	}
	return result
}

private handleLearningStatus(learningStatus) {
	def result = []
	def btn = state.activeLearnBtn
	def btnStatus = ""
	def status = ""
		
	if (btn) {
		if (learningStatus == 2) {
			logDebug "Waiting for ${btn.name} Code"
			result << "delay 1000"
			result << configGetCmd(learningStatusParam, safeToInt(btn.ep, 1))
		}
		else if (learningStatus <= 2) {
			logDebug "Learning\nSuccessful"
			status = "${btn.name} Learned"
			btnStatus = "assigned"
		}
		else {
			logDebug "Learning Failed with Code ${learningStatus}"
			status = "Learning\nFailed"
			btnStatus = "${btn.oldStatus}"
		}
		
		if (btnStatus) {
			updateBtnStatus(btn, btnStatus)
			sendRemoteStatusEvent(status, true)			
			state.activeLearnBtn = false
		}
	}
	return result ? sendResponse(result) : []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep=null) {	
	// logTrace "BasicReport${getEPSuffix(ep)}: ${cmd}"
	def result = []
	if (state.activeBtn && state.activeBtn?.num) {
		result << createEvent(createBtnEventMap(state.activeBtn))
		state.activeBtn = false
	}
	return result
}

private createBtnEventMap(btn, displayed=true) {
	return [
		name: "button", 
		value: "pushed", 
		data: [buttonNumber: btn.num, epNumber: btn.ep], 
		displayed: displayed, 
		isStateChange: true, 
		descriptionText: "EP${btn.ep} Button ${btn.num} Pushed"
	]
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep=null) {
	logDebug "Unhandled${getEPSuffix(ep)}: $cmd"
	return []
}


// Z-Wave Commands
private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x60: 3,	// Multi Channel
		0x70: 2,  // Configuration
		0x72: 2,  // ManufacturerSpecific
		0x86: 1,	// Version (2)
		0x94: 1	  // Simple AV Control (4)
	]
}


private versionGetCmd() {
	return zwave.versionV1.versionGet().format()
}

private multiChannelEndPointGetCmd() {
	return zwave.multiChannelV3.multiChannelEndPointGet().format()
}

private basicGetCmd(ep) {
	return multiChannelEncapCmd(zwave.basicV1.basicGet(), ep)
}

private basicSetCmd(val, ep) {	
	return multiChannelEncapCmd(zwave.basicV1.basicSet(value: val), ep)
}

private configSetCmd(param, val, ep) {
	return multiChannelEncapCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: val), ep)
}

private configGetCmd(param, ep) {
	return multiChannelEncapCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num), ep)
}

private simpleAvControlSetCmd(keyHex, ep) {
	logTrace "Executing simpleAvControlSetCmd(${keyHex}, ${ep})"
	state.sequence = (state.sequence ?: 0) + 1
	def cmd = multiChannelEncapCmd(zwave.simpleAvControlV1.simpleAvControlSet(itemId: 0x0000, keyAttributes: 0x00, sequenceNumber: state.sequence), ep)
	return "${cmd}${keyHex}"
}

private simpleAvControlGetCmd(ep) {
	return multiChannelEncapCmd(zwave.simpleAvControlV1.simpleAvControlGet(), ep)
}

private simpleAvControlSupportedGetCmd(reportNo, ep) {
	return multiChannelEncapCmd(zwave.simpleAvControlV1.simpleAvControlSupportedGet(reportNo: reportNo), ep)
}

private multiChannelEncapCmd(cmd, ep) {
	if (ep) {
		return zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}


// Configuration Parameters
private getConfigParams() {
	return [
		irDeviceCodeParam, 
		irPortMappingParam, 
		irOutputPowerLevelParam, 
		irCodeLearningParam, 
		irTransmissionModeParam, 
		epSelectionParam,
		downloadStatusRegisterParam,
		learningStatusParam,
		simpleAVControlDownloadKeyParam,
		libraryVersionParam
	]
}
private getSimpleAVControlDownloadKeyParam() {
	return createConfigParamMap(21, "Simple AV Control Download Key", 2)
}
private getDownloadStatusRegisterParam() {
	return createConfigParamMap(22, "Download Status Register", 1)
}
private getIrCodeLearningParam() {
	return createConfigParamMap(25, "IR Code Learning", 2)
}
private getLearningStatusParam() {
	return createConfigParamMap(26, "Learning Status Register", 1)
}
private getIrDeviceCodeParam() { 
	return createConfigParamMap(27, "IR Device Code", 2)
}
private getIrOutputPowerLevelParam() {
	// 0x00:Normal, 0xFF:High (Default) - Port 1 can't be changed
	return createConfigParamMap(28, "IR Output Power Level", 1)
}
private getIrPortMappingParam() {
	// Port 1 can't be changed.
	return createConfigParamMap(29, "IR Port Mapping", 1)
}
private getIrTransmissionModeParam() {
	// 0x00:Continuous (default), 0xFF:Single Shot
	return createConfigParamMap(31, "IR Transmission Mode", 1)
}
private getLibraryVersionParam() {
	return createConfigParamMap(36, "Library Version", 1)
}
private getEpSelectionParam() {
	return createConfigParamMap(38, "EP Selection", 1)
}

private createConfigParamMap(num, name, size, attr=null) {
	return [num: num, name: name, size: size, attr: attr]
}


// Settings
private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 120
}
private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput != false)
}
private getSwitchAutoOffSetting() {
	return settings?.switchAutoOff ?: false
}
private getEpPortSettingValue(epNum) {
	return convertOptionSettingToInt(portOptions, getEpPortSetting(epNum)) ?: 1	
}
private getEpPortSetting(epNum) {
	return getOptionSetting("ep${epNum}Port", portOptions)
}
private getBtnTriggerSettingEvents(btnNum) {
	def name = getOptionSetting("btn${btnNum}Trigger", btnTriggerOptions)
	return btnTriggerOptions.find { it.name == name }?.events ?: []
}
private getBtnTriggerSetting(btnNum) {
	return getOptionSetting("btn${btnNum}Trigger", btnTriggerOptions)
}
private getOptionSetting(settingName, options) {
	if (settings && settings["${settingName}"]) {
		return settings["${settingName}"]
	}
	else {
		return findDefaultOptionName(options)
	}
}

// Returns maps of the 9 buttons and key code and key hex string.
private getBtnOptions() {
	def result = []
	def btnNum = 1
	(7..15).each { key ->
		def keyHex = (key < 10) ? key : ["A", "B", "C", "D", "E", "F"][key - 10]
		result << [num: btnNum, key: key, keyHex: "000${keyHex}"]
		btnNum += 1
	}	
	return result
}

private getEpOptions() {
	def result = []
	(1..6).each {
		result << [num: it, name: "EP${it}"]
	}
	return result
}


private getPortOptions() {
	def result = []
	result << [name: formatDefaultOptionName("Internal Port"), value: 1]
	(2..6).each {
		result << [name: "External Port ${it}", value: it]
	}
	return result
}

private getBtnTriggerOptions() {
	return [		
		[name: formatDefaultOptionName("None"), events: [""]],
		[name: "Momentary Switch Push", events: ["push"]],
		[name: "Switch On", events: ["on"]],
		[name: "Switch Off", events: ["off"]],		
		[name: "Switch On/Off", events: ["on", "off"]]
	]
}

private getCheckinIntervalOptions() {
	def result = []	
	[5, 10, 15, 30].each {
		result << [name: "${it} Minutes", value: it]
	}
	result << [name: "1 Hour", value: 60]
	result << [name: formatDefaultOptionName("2 Hours"), value: 120]
	
	int i = 3
	for (i = 3; i < 25; i = i + 3) {
		result << [name: "${i} Hours", value: (60 * i)]	
	}	
	return result
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private formatDefaultOptionName(val) {
	return "${val}${defaultOptionSuffix}"
}

private findDefaultOptionName(options) {
	def option = options?.find { it.name?.contains("${defaultOptionSuffix}") }
	return option?.name ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private getEPSuffix(ep) {
	return (ep == null ? "" : " (EP${ep})")
}

private sendRemoteStatusEvent(val, autoReset=false) {
	if (val && autoReset) {
		state.tempRemoteStatus = val
		runIn(3, resetRemoteStatus, [overwrite: false])
	}
	if (val) {
		logDebug "$val"
	}
	sendEvent(createEventMap("remoteStatus", val, false))
}

def resetRemoteStatus() {
	if (state.tempRemoteStatus && state.tempRemoteStatus == device.currentValue("remoteStatus")) {
		state.tempRemoteStatus = false
		sendRemoteStatusEvent("")
	}
}

private createEventMap(name, value, displayed=null) {
	displayed = (displayed == null) ? (device?.currentValue("$name") != value) : displayed	
	return [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true
	]
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private createLastCheckinEvent() {
	//logTrace "Device Checked In"
	state.lastCheckinTime = new Date().time
	return createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
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

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "${msg}"
}