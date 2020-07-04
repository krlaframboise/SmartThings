/**
 *  Zooz Power Strip v1.0.5
 *     (Model: ZEN20)
 *  
 *  Capabilities:
 *	  Switch, Refresh
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-power-strip/68860?u=krlaframboise
 *
 *  Changelog:
 *
 *  1.0.5 (10/30/2018)
 *    - Added support for new mobile app.
 *
 *  1.0.4 (03/11/2016)
 *    - Adjusted health check to allow it to skip a checkin before going offline.
 *    - Removed Polling capability.
 *
 *  1.0.3 (02/18/2016)
 *    - Added Health Check and self polling.
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
	definition (name: "Zooz Power Strip", namespace: "krlaframboise", author: "Kevin LaFramboise", vid:"generic-switch") {
		capability "Configuration"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckin", "string"
		
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
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
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
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		initializeCheckin()
		
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

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
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
	logTrace "healthPoll()"
	sendHubCommand(new physicalgraph.device.HubAction(versionGetCmd()))
}

// Executed by SmartThings if the specified checkInterval is exceeded.
def ping() {
	logTrace "ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return versionGetCmd()
	}	
}

def configure() {
	state.isConfigured = true
	def cmds = []
	cmds << switchAllSetCmd(255)		
	return cmds		
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
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		result << createLastCheckinEvent()
	}	
	return result
}

private createLastCheckinEvent() {
	logDebug "Device Checked In"
	state.lastCheckinTime = new Date().time
	return createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
}

private convertToLocalTimeString(dt) {
	return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(location.timeZone.ID))
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

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
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

private versionGetCmd() {
	return zwave.versionV1.versionGet().format()
}

private multiChannelEncap(cmd, endpoint) {
	if (endpoint) {
		return zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}


private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getCheckinIntervalOptions() {
	[
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: formatDefaultOptionName("12 Hours"), value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
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

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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
	// log.trace "${msg}"
}