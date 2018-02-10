/**
 *  Ecolink Wireless Switch v1.0
 *  (Models: TLS-ZWAVE5, DLS-ZWAVE5, DDLS2-ZWAVE5)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *    1.0 (02/10/2018)
 *      - Initial Release
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
		name: "Ecolink Wireless Switch", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "oic.d.switch"
	) {
		capability "Actuator"
		capability "Sensor"		
 		capability "Switch"
		capability "Light"
		capability "Battery"
		capability "Health Check"
		capability "Refresh"
		
		attribute "lastCheckin", "string"
					
		fingerprint mfr:"014A", prod:"0006", model:"0001", deviceJoinName: "Ecolink Rocker Switch"
		
		fingerprint mfr:"014A", prod:"0006", model:"0002", deviceJoinName: "Ecolink Toggle Switch"
		
		fingerprint mfr:"014A", prod:"0006", model:"0003", deviceJoinName: "Ecolink Double Rocker Switch"
	}

	simulator { }

	preferences { 
		input "checkinInterval", "number", 
			title: "Checkin Interval Minutes: (15-1440)", 
			range: "15..1440",
			defaultValue: 240, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", nextState: "turningOff", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", nextState: "turningOn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
				attributeState "off", label: '${name}', action: "switch.on", nextState: "turningOn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", nextState: "turningOff", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			}
			tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'Battery ${currentValue}%'
			}
		}
		
		standardTile("refresh", "generic", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("lastActivity", "device.lastCheckin", decoration: "flat", inactiveLabel:false, width: 2, height: 2){
			state "lastCheckin", label:'Last\nActivity\n\n${currentValue}'
		}
		
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2){
			state "default", label:'${currentValue}% \nBattery', unit: ""
		}

		main "switch"
		details(["switch","refresh","lastActivity","battery"])
	}
}


def updated() {		
	def result = []
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		logTrace "updated()"
		
		if (!state.lastUpdated) {
			result += delayBetween([
				switchBinaryGetCmd(), 
				batteryGetCmd()
			], 500)
		}
		state.lastUpdated = new Date().time
		
		initializeCheckinSchedule()	
	}
	return result ? response(result) : []
}

private initializeCheckinSchedule(){
	sendEvent(name: "checkInterval", value: ((checkinIntervalSettingSeconds * 2) + getMinuteSeconds(2)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	unschedule()
	
	switch (checkIntervalSeconds) {
		case getMinuteSeconds(15):
			runEvery15Minutes(scheduledCheckin)
			break
		case getMinuteSeconds(30):
			runEvery30Minutes(scheduledCheckin)
			break
		case { it in [getHourSeconds(3), getHourSeconds(6), getHourSeconds(9), getHourSeconds(12)] }:
			runEvery3Hours(scheduledCheckin)
			break
		default:
			runEvery1Hour(scheduledCheckin)
	}
}

def scheduledCheckin() {
	def result = []
	if (canCheckin()) {
		logTrace "scheduledCheckin()"
		result += sendResponse([batteryGetCmd()])
	}
	else {
		logTrace "Ignored scheduled checkin"
	}
	return result
}

private canCheckin() {
	return (!state.lastCheckin || ((new Date().time) - state.lastCheckin > (checkinIntervalSettingSeconds * 1000))) 
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

def ping() {
	logTrace "ping()"	
	return refresh()
}

def refresh() {
	logTrace "refresh()"
	return [switchBinaryGetCmd(), batteryGetCmd()]
}

def on() {	
	logDebug "on()"
	return toggleSwitch(0xFF)
}

def off() {
	logDebug "off()"
	return toggleSwitch(0x00)
}

private toggleSwitch(val) {
	state.pendingValue = val
	return [
		switchBinarySetCmd(val)
	]
}

private switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}

private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}


def parse(String description) {
	// logTrace "description: $description"
	def result = []
	
	updateLastCheckin()
		
	if ("$description".contains("command: 5E02,")) {
		logTrace "Ignoring Zwave Plus Command Class because it causes zwave.parse to throw a null exception"
	}
	else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {			
			result += zwaveEvent(cmd)
		}	
	}
	return result
}

private updateLastCheckin() {
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time
		sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)	
	}
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x59: 1,	// AssociationGrpInfo
		0x5E: 2,	// ZwaveplusInfo
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x80: 1,	// Battery
		0x85: 2,	// Association
		0x86: 1		// Version (2)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicrReport: $cmd"	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: $cmd"
	
	def type = (state.pendingValue == cmd.value) ? "digital" : "physical"
	state.pendingValue = null
	
	sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: type, displayed: true, isStateChange: true)
	
	return [
		createEvent(name: "switch", value: cmd.value ? "on" : "off", type: type)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	state.lastCheckin = new Date().time
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	return [
		createEvent(name: "battery", value: val, unit: "%")
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unexpected Command: $cmd"
	return []
}


private getCheckinIntervalSettingSeconds() {	
	return settings?.checkInterval ? getMinuteSeconds(settings.checkinInterval) : getHourSeconds(4)
}

private getHourSeconds(hours) {
	return (hours * 60 * 60)
}

private getMinuteSeconds(minutes) {
	return (minutes * 60)
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
	return !lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}