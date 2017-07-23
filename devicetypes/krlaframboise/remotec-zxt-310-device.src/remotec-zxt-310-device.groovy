/**
 *  Remotec ZXT-310 Device v1.0
 *
 *     (Virtual device used by the Remotec ZXT-310 Device Manager SmartApp)
 *  
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *
 *  Changelog:
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
	definition (name: "Remotec ZXT-310 Device", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Button"
		capability "Momentary"
		capability "Refresh"
						
		attribute "epNumber", "number"
		attribute "epDetails", "string"
		command "refreshData"		
		
		btns.each { btn ->
			attribute "btn${btn}Status", "string"
			attribute "btn${btn}Label", "string"
			command "pushButton${btn}"
		}
	}

	simulator {

	}
	
	preferences {			
		btns.each { btn ->
			input "btn${btn}Label", "text",
				title: "Button ${btn} Label:",
				displayDuringSetup: true,
				required: false,
				defaultValue: getBtnLabelSetting(btn)
			input "btn${btn}Trigger", "enum",
				title: "Button ${btn} Trigger:",
				displayDuringSetup: true,
				required: false,
				defaultValue: getBtnTriggerSetting(btn),
				options: btnTriggerOptions.collect { it.name }
			input "btn${btn}Delay", "enum",
				title: "Button ${btn} Delay: (seconds)",
				displayDuringSetup: true,
				required: false,
				defaultValue: getBtnDelaySetting(btn),
				options: btnDelayOptions.collect { it.name }
			input "btn${btn}Repeat", "enum",
				title: "Button ${btn} Repeat:",
				displayDuringSetup: true,
				required: false,
				defaultValue: getBtnRepeatSetting(btn),
				options: btnRepeatOptions.collect { it.name }				
		}		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			displayDuringSetup: false, 
			required: false,
			defaultValue: debugOutputSetting
	}

	tiles(scale: 2) {
		standardTile("switch", "device.switch", width: 2, height: 2, key: "PRIMARY_CONTROL", canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		valueTile("epDetails", "device.epDetails", width: 2, height: 2, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff"			
		}		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon", backgroundColor: "#ffffff"
		}
		standardTile("btn1Label", "device.btn1Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '1. ${currentValue}'
		}
		standardTile("btn1Status", "device.btn1Status", width: 2, height: 2) {
			state "assigned", label: '1', action: "pushButton1", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '1\n(empty)', action: "pushButton1", backgroundColor: unassignedBtnColor
			
		}
		standardTile("btn2Label", "device.btn2Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '2. ${currentValue}'
		}
		standardTile("btn2Status", "device.btn2Status", width: 2, height: 2) {
			state "assigned", label: '2', action: "pushButton2", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '2\n(empty)', action: "pushButton2", backgroundColor: unassignedBtnColor
		}
		standardTile("btn3Label", "device.btn3Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '3. ${currentValue}'
		}
		standardTile("btn3Status", "device.btn3Status", width: 2, height: 2) {
			state "assigned", label: '3', action: "pushButton3", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '3\n(empty)', action: "pushButton3", backgroundColor: unassignedBtnColor
		}
		standardTile("btn4Label", "device.btn4Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '4. ${currentValue}'
		}
		standardTile("btn4Status", "device.btn4Status", width: 2, height: 2) {
			state "assigned", label: '4', action: "pushButton4", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '4\n(empty)', action: "pushButton4", backgroundColor: unassignedBtnColor
		}
		standardTile("btn5Label", "device.btn5Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '5. ${currentValue}'
		}
		standardTile("btn5Status", "device.btn5Status", width: 2, height: 2) {
			state "assigned", label: '5', action: "pushButton5", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '5\n(empty)', action: "pushButton5", backgroundColor: unassignedBtnColor
		}
		standardTile("btn6Label", "device.btn6Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '6. ${currentValue}'
		}
		standardTile("btn6Status", "device.btn6Status", width: 2, height: 2) {
			state "assigned", label: '6', action: "pushButton6", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '6\n(empty)', action: "pushButton6", backgroundColor: unassignedBtnColor
		}
		standardTile("btn7Label", "device.btn7Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '7. ${currentValue}'
		}
		standardTile("btn7Status", "device.btn7Status", width: 2, height: 2) {
			state "assigned", label: '7', action: "pushButton7", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '7\n(empty)', action: "pushButton7", backgroundColor: unassignedBtnColor
		}
		standardTile("btn8Label", "device.btn8Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '8. ${currentValue}'
		}
		standardTile("btn8Status", "device.btn8Status", width: 2, height: 2) {
			state "assigned", label: '8', action: "pushButton8", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '8\n(empty)', action: "pushButton8", backgroundColor: unassignedBtnColor
		}
		standardTile("btn9Label", "device.btn9Label", width: 2, height: 1, decoration: "flat") {
			state "default", label: '9. ${currentValue}'
		}
		standardTile("btn9Status", "device.btn9Status", width: 2, height: 2) {
			state "assigned", label: '9', action: "pushButton9", nextState: "assigned", defaultState: true, backgroundColor: assignedBtnColor
			state "unassigned", label: '9\n(empty)', action: "pushButton9", backgroundColor: unassignedBtnColor
		}		
		
		main ("switch")
		details(["switch", "epDetails", "refresh", "btn1Label", "btn2Label", "btn3Label", "btn1Status", "btn2Status", "btn3Status", "btn4Label", "btn5Label", "btn6Label", "btn4Status", "btn5Status", "btn6Status", "btn7Label", "btn8Label", "btn9Label", "btn7Status", "btn8Status", "btn9Status"])
	}
}

private getUnassignedBtnColor() { return "#ffffff" }
private getAssignedBtnColor() { return "#79b821" }


def updated() {		
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time
		logTrace "Executing updated()"		
		
		initialize()
		
		btns.each { btn ->
			sendEventIfNew("btn${btn}Label", getBtnLabelSetting(btn))			
		}
	}
}

private initialize() {
	if (!state.isConfigured) {
		sendEventIfNew("numberOfButtons", btns.size())
		sendEventIfNew("button", "pushed")
		sendEventIfNew("switch", "off")
		sendEventIfNew("level", 100)
		btns.each { btn ->
			sendEventIfNull("btn${btn}Label", "")
			sendEventIfNull("btn${btn}Status", "unassigned")
		}
		state.isConfigured = true
	}
}

def refresh() {
	try {
		parent.refreshChildData(device.deviceNetworkId)
	}
	catch (e) {
		logDebug "Refresh request to parent failed."
	}
}

void refreshData(jsonData) {
	logTrace "refreshData(${jsonData})"
	def slurper = new groovy.json.JsonSlurper()
	def data = slurper.parseText(jsonData)
	
	initialize()
	
	if (data && data.epNum) {
		sendEventIfNew("epNumber", data.epNum)
		sendEventIfNew("epDetails", "EP${data.epNum}\nIR Port ${data.port}")

		btns.each { btn ->
			sendEventIfNew("btn${btn}Status", data["btn${btn}Status"])			
		}
	}
	else {
		logDebug "Unable to refreshData for json: ${jsonData}"
	}
}

private sendEventIfNew(name, value) {
	if (value != null && device.currentValue(name) != value) {
		sendEvent(createEventMap(name, value, false))
	}
}

private sendEventIfNull(name, value) {
	if (device.currentValue(name) == null) {
		sendEvent(createEventMap(name, value, false))
	}
}


def on() { 
	logTrace "Executing on()"
	sendEvent(createEventMap("switch", "on", false))
	pushTriggeredBtns("on")
    return []
}

def off() {
	logTrace "Executing off()"
	sendEvent(createEventMap("switch", "off", false))
	pushTriggeredBtns("off")
    return []
}

def push() {
	logTrace "Executing push()"
	pushTriggeredBtns("push")
}

private pushTriggeredBtns(eventName) {
	btns.each { btn ->
		if (eventName in getBtnTriggerSettingEvents(btn)) {
			pushButton(btn)
		}
	}
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

def pushButton1() { return pushButton(1) }
def pushButton2() { return pushButton(2) }
def pushButton3() { return pushButton(3) }
def pushButton4() { return pushButton(4) }
def pushButton5() { return pushButton(5) }
def pushButton6() { return pushButton(6) }
def pushButton7() { return pushButton(7) }
def pushButton8() { return pushButton(8) }
def pushButton9() { return pushButton(9) }

def pushButton(btn) {
	if (device.currentValue("btn${btn}Status") == "assigned") {
		logDebug "Button ${btn} Pushed"
		sendEvent([
			name: "button",
			value: "pushed", 
			data: getBtnData(btn),
			displayed: true, 
			isStateChange: true, 
			descriptionText: "EP${epNumber} Button ${btn} Pushed"
		])
	}
}

private getBtnData(btn) {
	return [
		buttonNumber: btn,
		epNum: epNumber,
		delay: getBtnDelaySettingMilliseconds(btn),
		repeat: convertOptionSettingToInt(btnRepeatOptions, getBtnRepeatSetting(btn))
	]
}

private getEpNumber() {
	return device.currentValue("epNumber")
}


def parse(String description) {	
	logDebug "Unknown Description: $description"
}

// Settings
private getDebugOutputSetting() {
	return (settings?.debugOutput != false)
}
private getBtnLabelSetting(btn) {
	def settingName = "btn${btn}Label"
	return (settings && settings[settingName]) ? settings[settingName] : ""
}
private getBtnDelaySettingMilliseconds(btn) {
	return ((convertOptionSettingToInt(btnDelayOptions, getBtnDelaySetting(btn)) ?: 0) * 1000)
}
private getBtnDelaySetting(btn) {
	return getOptionSetting("btn${btn}Delay", btnDelayOptions)
}
private getBtnRepeatSetting(btn) {
	return getOptionSetting("btn${btn}Repeat", btnRepeatOptions)
}
private getBtnTriggerSettingEvents(btn) {
	def name = getOptionSetting("btn${btn}Trigger", btnTriggerOptions)
	return btnTriggerOptions.find { it.name == name }?.events ?: []
}
private getBtnTriggerSetting(btn) {
	return getOptionSetting("btn${btn}Trigger", btnTriggerOptions)
}
private getOptionSetting(settingName, options) {
	if (settings && settings["${settingName}"]) {
		return settings["${settingName}"]
	}
	else {
		return findDefaultOptionName(options)
	}
}

private getBtns() {
	def result = []
	(1..9).each { 
		result << it
	}
	return result
}

private getBtnDelayOptions() {
	def result = []
	result << [name: formatDefaultOptionName("No Delay"), value: 0]
	(1..5).each { // 1-5 Seconds
		def suffix = (it == 1 ? "" : "s")
		result << [name: "${it} Second${suffix}", value: it]
	}
	result << [name: "10 Seconds", value: 10]
	(1..4).each {
		result << [name: "${it * 15} Seconds", value: (it * 15)]
	}	
	return result
}

private getBtnRepeatOptions() {
	def result = []
	result << [name: formatDefaultOptionName("No Repeat"), value: 0]
	(1..10).each {
		result << [name: "${it}", value: it]
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

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "${msg}"
}