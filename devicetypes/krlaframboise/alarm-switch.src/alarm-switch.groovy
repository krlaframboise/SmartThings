/**
 *  Alarm Switch v1.0
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
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
	definition (name: "Alarm Switch", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
    capability "Alarm"
		capability "Switch"
		capability "Polling"

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
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "on", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "siren", label:'Siren On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"				
			}
		}
		
		valueTile("switch", "device.switch", label: 'On', width: 2, height: 2) {
			state "off", label:'Turn On', action: "on", icon:""
			state "on", label:'Turn Off', action: "off", icon:"", backgroundColor:"#79b821"
		}	
		      
		main "alarm"
		details(["alarm","switch"])
	}
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
	def request = [
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	]
	
	if (settings.alarmDuration) {
		logDebug "Alarm will automatically turn off in ${settings.alarmDuration} seconds."
		request << "delay ${settings.alarmDuration * 1000}"
		request += off()
	}
	return request
}

def off() {
	logDebug "Turning Off"
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	]
}

def poll() {
	logDebug "Polling"
	[
		zwave.basicV1.basicGet().format()
	]
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = createEvents(cmd)
	}
	return result
}

def createEvents(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	boolean alarmOn = cmd.value
	def switchValue = alarmOn ? "on" : "off"
	def alarmValue = alarmOn ? getAlarmValue() : "off"
	
	if (!alarmOn) {
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

def createEvents(physicalgraph.zwave.Command cmd) {
	logDebug "UNEXPECTED COMMAND: $cmd"
}

private logDebug(msg) {
	if (state.debugOutput) {
		log.debug "$msg"
	}
}