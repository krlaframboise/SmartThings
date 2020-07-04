/**
 *  Ecolink Wireless Switch v1.0.1
 *  (Models: TLS-ZWAVE5, DLS-ZWAVE5, DDLS2-ZWAVE5)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
 *    
 *
 *  Changelog:
 *
 *    1.0.2 (07/04/2018)
 *      - Added double toggle switch fingerprint.
 *
 *    1.0.1 (02/10/2018)
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
		
		attribute "lastCheckIn", "string"
					
		fingerprint mfr:"014A", prod:"0006", model:"0001", deviceJoinName: "Ecolink Rocker Switch"
		
		fingerprint mfr:"014A", prod:"0006", model:"0002", deviceJoinName: "Ecolink Toggle Switch"
		
		fingerprint mfr:"014A", prod:"0006", model:"0003", deviceJoinName: "Ecolink Double Rocker Switch"
		
		fingerprint mfr:"014A", prod:"0006", model:"0004", deviceJoinName: "Ecolink Double Toggle Switch"
	}

	simulator { }

	preferences { 
		input "checkInInterval", "enum",
			title: "Check In Interval:",
			defaultValue: checkInIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkInIntervalOptions.collect { it.name }
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
		
		valueTile("lastActivity", "device.lastCheckIn", decoration: "flat", inactiveLabel:false, width: 2, height: 2){
			state "lastCheckIn", label:'Last\nActivity\n\n${currentValue}'
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
		
		initializeCheckInSchedule()	
	}
	return result ? response(result) : []
}

private initializeCheckInSchedule(){
	sendEvent(name: "checkInterval", value: ((checkInIntervalSettingMinutes * 2 * 60) + (60 * 2)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	unschedule()
	
	switch (checkInIntervalSetting) {
		case "15 Minutes":
			runEvery15Minutes(scheduledCheckIn)
			break
		case "30 Minutes":
			runEvery30Minutes(scheduledCheckIn)
			break
		case { it in ["1 Hour", "2 Hours"] }:
			runEvery1Hour(scheduledCheckIn)
			break
		default:
			runEvery3Hours(scheduledCheckIn)
	}
}

def scheduledCheckIn() {
	def result = []
	if (canCheckIn()) {
		logTrace "scheduledCheckIn()"
		result += sendResponse([batteryGetCmd()])
	}
	else {
		logTrace "Ignored scheduled check in"
	}
	return result
}

private canCheckIn() {
	return (!state.lastCheckIn || ((new Date().time) - state.lastCheckIn > (checkInIntervalSettingMinutes * 60 * 1000))) 
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
	def result = []
	result << switchBinaryGetCmd()
	result << batteryGetCmd()	
	return delayBetween(result, 500)
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
	
	updateLastCheckIn()
		
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

private updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time
		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)	
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
	logTrace "BasicReport: $cmd"	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: $cmd"
	
	def type = (state.pendingValue == cmd.value) ? "digital" : "physical"
	state.pendingValue = null
		
	return [
		createEvent(name: "switch", value: cmd.value ? "on" : "off", type: type)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	state.lastCheckIn = new Date().time
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val >= 99) {
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


private getCheckInIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkInIntervalOptions, checkInIntervalSetting) ?: 720
}

private getCheckInIntervalSetting() {
	return settings?.checkInInterval ?: findDefaultOptionName(checkInIntervalOptions)
}


private getCheckInIntervalOptions() {
	[
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