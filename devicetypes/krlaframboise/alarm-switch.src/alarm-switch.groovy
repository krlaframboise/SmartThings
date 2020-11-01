/**
 *  Alarm Switch v1.3
 *
 *  Capabilities:
 *    Switch, Alarm, Polling
 *
 *  Description:
 *    Provides alarm capabilities to regular on/off switch
 *    so that it shows up like an alarm and can be used as
 *    an alarm in SmartApps like SHM.
 *
 *  URL to documentation:
 *    n/a
 
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.3 (02/21/2020)
 *      - Implemented health check capability
 *
 *    1.2 (05/21/2016)
 *      - Fixed bug in polling feature.
 *      - UI Enhancements
 *
 *    1.0 (05/10/2016)
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
	definition (name: "Alarm Switch", namespace: "krlaframboise", author: "Kevin LaFramboise",ocfDeviceType: "oic.d.switch",vid:"generic-switch") {
		capability "Actuator"
		capability "Alarm"
		capability "Switch"		
		capability "Outlet"
		capability "Health Check"		
		
		attribute "lastCheckIn", "number"

		fingerprint inClusters: "0x20,0x25,0x86,0x80,0x85,0x72,0x71"
	}

	simulator {
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"		
	}

	preferences {
		input "alarmDuration", "number", 
			title: "Turn off after: (seconds)", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "alarmType", "enum",
			title: "What type of alarm is this?",
			defaultValue: "Siren and Strobe",
			displayDuringSetup: true,
			required: false,
			options: ["Siren and Strobe", "Siren Only", "Strobe Only"]
		input "debugOutput", "bool", 
			title: "Enable Debug Logging", 
			defaultValue: true, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "alarm.both", nextState: "turningOn", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "turningOn", label:'Turning On!', action: "alarm.off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "turningOff", label:'Turning Off!', action: "alarm.both", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "siren", label:'Siren On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "alarm.off", nextState: "turningOff", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"				
			}
		}
		
		standardTile("switch", "device.switch", label: 'On', width: 4, height: 4) {
			state "off", label:'Off', action: "switch.on", nextState: "turningOn", icon:"st.switches.switch.off", background: "#ffffff"
			state "turningOn", label:'Turning On', action: "switch.off", icon:"st.switches.switch.on", background: "#79b821"
			state "on", label:'On', nextState: "turningOff", action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "turningOff", label:'Turning Off', action: "switch.on", icon:"st.switches.switch.off", background: "#ffffff"
		}	
		      
		main "alarm"
		details(["alarm","switch"])
	}
}

def updated() {			
	logDebug "updated()..."
	
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: ((6 * 60 * 60) + (60 * 2)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	unschedule()
	runEvery3Hours(ping)
}

def ping() {
	logDebug "Pinging device because it has not checked in"
	return zwave.versionV1.versionGet().format()
}


def strobe() {
	on()
}

def both() {
	on()
}

def siren() {
	on()
}

def on() {
	logDebug "Turning On"
	
	def cmds = delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	], 100)
	
	if (settings.alarmDuration) {
		logDebug "Alarm will automatically turn off in ${settings.alarmDuration} seconds."
		cmds << "delay ${settings.alarmDuration * 1000}"
		
		cmds += delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.basicV1.basicGet().format()
		], 100)		
	}
	return cmds
}

def off() {
	logDebug "Turning Off"
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	], 100)
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, [0x20: 1, 0x86: 1])
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	result << createEvent(name: "lastCheckIn",value: new Date().time, isStateChange: true, displayed: false)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	boolean alarmOn = cmd.value
	def switchValue = alarmOn ? "on" : "off"
	def alarmValue = alarmOn ? getAlarmValue() : "off"
	
	if (!alarmOn && device.currentValue("alarm") != "off") {
		logDebug "Alarm Turned Off"
	}
	
	[
		createEvent([name: "switch", value: switchValue, displayed: false]),
		createEvent([name: "alarm", value: alarmValue])
	]
}

private getAlarmValue() {
	def alarmValue
	switch (settings.alarmType) {
		case "Siren Only":
			alarmValue = "siren"
			break
		case "Strobe Only":
			alarmValue = "strobe"
			break
		default:
			alarmValue = "both"
	}
	return alarmValue
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
	logDebug("Version: $cmd")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "UNEXPECTED COMMAND: $cmd"
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}