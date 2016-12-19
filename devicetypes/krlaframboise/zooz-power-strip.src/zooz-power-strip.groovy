/**
 *  Zooz Power Strip v1.0.2
 *     (Model: ZEN20)
 *  
 *  Capabilities:
 *	  Switch, Refresh, Polling
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *  1.0.2 (12/19/2016)
 *    - Fixed issue with button events.
 *
 *  1.0.1 (12/18/2016)
 *    - Enhanced Main Switch functionality.
 *
 *  1.0.0 (12/16/2016)
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
	definition (name: "Zooz Power Strip", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Configuration"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Polling"		

		attribute "lastPoll", "number"
		
		(1..5).each { ch ->
			attribute "ch${ch}Switch", "enum", ["on", "off"]
			command "ch${ch}On"
			command "ch${ch}Off"
		}
		
		fingerprint mfr: "015D", prod: "0651", model: "F51C"
	}

	simulator {

	}
	
	preferences {
		(1..5).each { ch ->
			input "ch${ch}Behavior", "enum",
				title: "CH${ch} Main Switch Behavior:",
				options: ["On/Off", "On", "Off", "None"],
				defaultValue: "On/Off",
				required: false
		}
		input "mainSwitchDelay", "number",
			title: "Main Switch Delay (milliseconds):",
			defaultValue: 0,
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false			
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "switch.on", 
					icon:"st.Appliances.appliances17", 
					backgroundColor: "#ffffff"
				attributeState "on", 
					label:'On', 
					action: "switch.off", 
					icon:"st.Appliances.appliances17", 
					backgroundColor: "#79b821"
			}
		}				
		standardTile("CH1", "device.ch1Switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: 'CH1 ${name}', action: "ch1Off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: 'CH1 ${name}', action: "ch1On", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		standardTile("CH2", "device.ch2Switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: 'CH2 ${name}', action: "ch2Off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: 'CH2 ${name}', action: "ch2On", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		standardTile("CH3", "device.ch3Switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: 'CH3 ${name}', action: "ch3Off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: 'CH3 ${name}', action: "ch3On", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		standardTile("CH4", "device.ch4Switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: 'CH4 ${name}', action: "ch4Off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: 'CH4 ${name}', action: "ch4On", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		standardTile("CH5", "device.ch5Switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: 'CH5 ${name}', action: "ch5Off", icon:"st.Appliances.appliances17",  backgroundColor: "#79b821"
			state "off", label: 'CH5 ${name}', action: "ch5On", icon:"st.Appliances.appliances17",  backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
				
		main "switch"
		details(["switch", "CH1", "CH2", "CH3", "CH4", "CH5", "refresh"])
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time

		def cmds = []
		if (!state?.isConfigured) {
			cmds += configure()
		}		
		cmds += refresh()
		
		initializeMainSwitch()		
		return response(cmds)
	}
}

private initializeMainSwitch() {	
	state.mainSwitchOnCHs = []
	state.mainSwitchOffCHs = []
	
	(1..5).each { ch ->
		["On","Off"].each { action ->
			def chBehavior = settings?."ch${ch}Behavior"
			if (!chBehavior || chBehavior.contains(action)) {
				state."mainSwitch${action}CHs" << ch
			}
		}
	}	
}

def configure() {
	state.isConfigured = true
	def cmds = []
	cmds << switchAllSetCmd(255)		
	return cmds		
}

def poll() {
	logDebug "Executing poll()"
	return refresh()
}

def refresh() {
	logDebug "Executing refresh()"	
	def result = []	
	(1..5).each { ch ->
		result << basicGetCmd(ch)
	}
	return delayBetween(result, 500)
}

def on() { return executeMainSwitch("on") }
def off() { return executeMainSwitch("off") }
private executeMainSwitch(val) {
	logDebug "Executing ${val}()"
	def result = []
	def cmd = val.capitalize()
	def switchDelay = settings?.mainSwitchDelay
	def switchCHs = state."mainSwitch${cmd}CHs"
	
	if (!switchDelay && switchCHs?.size()== 5) {
		logDebug "Turning All CHs ${cmd}"
		result << "switchAll${cmd}Cmd"()		
	}
	else {
		switchCHs?.each { ch ->
			if (switchDelay) {
				result << "ch${cmd}"(ch)
				result << "delay ${switchDelay}"
			}
			else {
				result << basicSetCmd((val == "on" ? 0xFF : 0x00), ch)
				result << "delay 50"				
			}			
		}		
	}
	result << "delay 1000"
	result += refresh()
	return result
}

def ch1On() { return chOn(1) }
def ch1Off() { return chOff(1) }
def ch2On() { return chOn(2) }
def ch2Off() { return chOff(2) }
def ch3On() { return chOn(3) }
def ch3Off() { return chOff(3) }
def ch4On() { return chOn(4) }
def ch4Off() { return chOff(4) }
def ch5On() { return chOn(5) }
def ch5Off() { return chOff(5) }

private chOn(ch) {
	logDebug "Turning CH${ch} On"
	return chOnOff(ch, 0xFF)
}

private chOff(ch) {
	logDebug "Turning CH${ch} Off"
	return chOnOff(ch, 0x00)
}

private chOnOff(ch, val) {
	return delayBetween([
		basicSetCmd(val, ch),
		basicGetCmd(ch)
	], 50)
}

def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, [0x59:1, 0x85:2, 0x20:1, 0x5A:1, 0X72:2, 0X8E:2, 0X60:3, 0X73:1, 0X25:1, 0x27:1, 0X86:1])
	
	if (cmd) {
		result += zwaveEvent(cmd)		
	}
	else {
		logDebug "Unknown Description: $description"
	}	
	result << createEvent(name: "lastPoll", value: new Date().time, displayed: false, isStateChange: true)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20:1, 0X25:1, 0x59:1])
	
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {	
	return handleSwitchEvent(cmd.value, endpoint, "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {	
	return handleSwitchEvent(cmd.value, endpoint, "digital")
}

private handleSwitchEvent(val, ch, type) {
	def result = []
	def attrName = "ch${ch}Switch"
	def newVal = val ? "on" : "off"
	def display = (device.currentValue(attrName) != newVal)
	
	logDebug "CH${ch} is ${newVal}"
	
	result << createEvent(name:attrName, value:newVal,displayed: display, type: type)

	def switchCHs = state?."mainSwitch${newVal.capitalize()}CHs"
		
	if (switchCHs?.find { n -> "$n" == "$ch" }) {
		
		def ignoreMainSwitch = false
		if (newVal == "off") {			
			switchCHs?.each { otherCH ->			
				if (ch != otherCH && device.currentValue("ch${otherCH}Switch") == "on") {
					ignoreMainSwitch = true
				}
			}
		}
		if (!ignoreMainSwitch) {
			result << createSwitchEvent(newVal, type)
		}
	}
	
	return result
}

private createSwitchEvent(newVal, type) {
	return createEvent(name:"switch", value:newVal,displayed: true, type: "$type")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}

private switchAllOnCmd() {
	return zwave.switchAllV1.switchAllOn().format()
}

private switchAllOffCmd() {
	return zwave.switchAllV1.switchAllOff().format()
}

private switchAllSetCmd(mode) {
	// None: 0, All On: 1, All Off: 2, All On/Off: 255
	return zwave.switchAllV1.switchAllSet(mode: mode).format()
}

private basicGetCmd(endpoint=null) {
	return multiChannelEncap(zwave.basicV1.basicGet(), endpoint)
}
private basicSetCmd(val, endpoint=null) {	
	return multiChannelEncap(zwave.basicV1.basicSet(value: val), endpoint)
}

private multiChannelEncap(cmd, endpoint) {
	if (endpoint) {
		return zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	}
	else {
		return cmd.format()
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
