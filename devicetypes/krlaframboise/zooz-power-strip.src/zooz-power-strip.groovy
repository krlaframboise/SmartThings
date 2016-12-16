/**
 *  Zooz Power Strip v1.0.0
 *     (Model: ZEN20)
 *  
 *
 *
 *  Capabilities:
 *	  Switch, Refresh, Polling
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
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
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Polling"

		attribute "lastPoll", "number"
		
		(1..5).each { n ->
			attribute "ch${n}Switch", "enum", ["on", "off"]
			command "ch${n}On"
			command "ch${n}Off"
		}
		
		fingerprint mfr: "015D", prod: "0651", model: "F51C"
	}

	simulator {

	}
	
	preferences {
		input "defaultChannel", "enum",
			title: "What should the default On/Off command control?",
			defaultValue: "CH1",
			options: ["CH1", "CH2", "CH3", "CH4", "CH5", "All"]
		input "multiSwitchDelay", "number",
			title: "Multi-Switch Delay in Milliseconds:",
			description: "Increase this number if you have the default On/Off setting set to 'All' and outlets are being skipped.",
			defaultValue: 1500
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
		cmds += refresh()
		return response(cmds)
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def poll() {
	logDebug "Executing poll()"
	return refresh()
}

def refresh() {
	logDebug "Executing refresh()"	
	def result = []	
	(1..5).each { n ->
		result << basicGetCmd(n)
	}
	return delayBetween(result, settings?.multiSwitchDelay ?: 1500)
}

def on() {
	logDebug "Executing on()"
	return defaultOnOff(0xFF)
}

def off() {
	logDebug "Executing off()"
	return defaultOnOff(0x00)
}

private defaultOnOff(val) {
	def result = []
	
	if (settings?.defaultChannel == "All") {
		(1..5).each { n ->
			result += chOnOff(val, n)
			result << "delay ${settings?.multiSwitchDelay ?: 1500}"
		}		
	}
	else {
		result += chOnOff(val, getDefaultChannel())
	}
	return result
}

private getDefaultChannel() {
	return settings?.defaultChannel?.replace("CH","")?.toInteger() ?: 1
}

def ch1On() { chOnOff(0xFF, 1) }
def ch1Off() { chOnOff(0x00, 1) }
def ch2On() { chOnOff(0xFF, 2) }
def ch2Off() { chOnOff(0x00, 2) }
def ch3On() { chOnOff(0xFF, 3) }
def ch3Off() { chOnOff(0x00, 3) }
def ch4On() { chOnOff(0xFF, 4) }
def ch4Off() { chOnOff(0x00, 4) }
def ch5On() { chOnOff(0xFF, 5) }
def ch5Off() { chOnOff(0x00, 5) }

private chOnOff(val, channel) {
	logDebug "Turning CH${channel} ${(val == 0xFF) ? 'On' : 'Off'}"
	return delayBetween([
		basicSetCmd(val, channel),
		basicGetCmd(channel)
	], 50)
}

def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, [0x59:1, 0x85:2, 0x20:1, 0x5A:1, 0X72:2, 0X8E:2, 0X60:3, 0X73:1, 0X25:1, 0X86:1])
	
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {	
	def result = []
	def currentAttr = "ch${endpoint}Switch"
	def newVal = cmd.value ? "on" : "off"
	def display = (device.currentValue(currentAttr) != newVal)
	
	logDebug "CH${endpoint} is ${newVal}"
	
	result << createEvent(name:currentAttr, value:newVal,displayed: display)
	
	if (settings?.defaultChannel == "All") {
		if (cmd.value) {
			result << createSwitchEvent(newVal)
		}
		else {
			def foundOn = false
			(1..5).each { n -> 
				if (n != endpoint && device.currentValue("ch${n}Switch") == "on") {
					foundOn = true
				}
			}
			if (!foundOn) {
				result << createSwitchEvent(newVal)
			}
		}	
	}
	else if (getDefaultChannel() == endpoint) {
		result << createSwitchEvent(newVal)
	}
	
	return result
}

private createSwitchEvent(newVal) {
	def display = (device.currentValue("switch") != newVal)
	return createEvent(name:"switch", value:newVal,displayed: display)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}

private basicGetCmd(endpoint=1) {
	return multiChannelEncap(zwave.basicV1.basicGet(), endpoint)
}
private basicSetCmd(val, endpoint=1) {	
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


private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} $msg"
}
