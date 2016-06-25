/**
 *  Zipato Multisound Siren v0.0.0 (TEST)
 *  (PH-PSE02.US)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    
 *
 *  Changelog:
 *
 *    0.0.0 (TEST)
 *      - Logging Device Responses
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
	definition (name: "Zipato Multisound Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Alarm"
		capability "Switch"

		command "test"
		command "logResponses"

		fingerprint deviceId: "0x1005", inClusters: "0x71,0x20,0x25,0x85,0x70,0x72,0x86,0x30,0x59,0x73,0x5A,0x98,0x7A"
	}

	simulator {
		// reply messages
		reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
		reply "988100200100,9881002002": "command: 9881, payload: 00200300"
		reply "9881002001FF,delay 3000,988100200100,9881002002": "command: 9881, payload: 00200300"
	}
	
	preferences {
		input "sound", "number", title: "Siren sound (1-5)", defaultValue: 1, displayDuringSetup: true
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
			tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			}
		}
		
		standardTile("turnOn", "device.alarm", width: 2, height: 2) {
			state "default", label:'Both', action:"alarm.both", icon:""
		}
		
		standardTile("turnOff", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action:"alarm.off", icon:""
		}

		standardTile("logResponses", "generic", width: 2, height: 2) {
			state "default", label:'Log', action:"logResponses", icon:""
		}

		main "alarm"
		details(["alarm", "turnOn", "turnOff", "logResponses"])
	}
}

def updated() {
	// if(!state.sound) state.sound = 1
	// if(!state.volume) state.volume = 3

	// log.debug "settings: ${settings.inspect()}, state: ${state.inspect()}"

	// Short sound = (settings.sound as Short) ?: 1
	// Short volume = (settings.volume as Short) ?: 3

	// if (sound != state.sound || volume != state.volume) {
		// state.sound = sound
		// state.volume = volume
		// return response([
			// secure(zwave.configurationV1.configurationSet(parameterNumber: 37, size: 2, configurationValue: [sound, volume])),
			// "delay 1000",
			// offSetCmd()
		// ])
	// }
}

def on() {
	log.debug "sending on"
	return onSetCmds()	
}

def off() {
	log.debug "sending off"
	return offSetCmds()	
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}

def test() {
	def result = []
	result << onSetCmd()
	result << "delay 3000"
	result += offSetCmds()
	return result
}

def logResponses() {
	log.debug "Requesting Device Information"
	delayBetween([
		basicGetCmd(),
		supportedSecurityGetCmd(),
		switchBinaryGetCmd(),
		sensorBinaryGetCmd(),
		powerlevelGetCmd(),
		switchMultilevelGetCmd(),
		configGetCmd(7),
		configGetCmd(29),
		configGetCmd(31),
		versionGetCmd()
	], 500)
}

def parse(String description) {
	log.debug "parse($description)"
	def result = null
	def cmd = zwave.parse(description, [0x71: 4, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1, 0x72: 2, 0x86: 1, 0x59: 1, 0x73: 1, 0x98: 1, 0x7A: 1, 0x5A: 1])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "Parse returned ${result?.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand([0x71: 4, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1])
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "BasicReport: $cmd"
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off", displayed: false]),
		createEvent([name: "alarm", value: cmd.value ? "both" : "off"])
	]
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	log.debug "Version: $cmd"
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def parameterName
	switch (cmd.parameterNumber) {
		case 7:
			parameterName = "Notification Type"
			// Bit4: 
			// 0: Using Notification Report
			// 1: Using Sensor Binary Report
			break
		case 29:
			parameterName = "Disable Alarm"
			// Disable the alarm function.
			// 0: Enable Alarm
			// 1: Disable Alarm
			// Caution: After the power up, this configuration is always 0
			break
		case 31:
			parameterName = "Alarm Duration"
			// (0 - 127)
			//0: disabled
			//1: 30 seconds
			//6: 3 Minutes (default)
			//127: 63.5 Minutes (max)			
			break
		default:	
			parameterName = "Parameter #${cmd.parameterNumber}"
	}		
	if (parameterName) {
		log.debug "${parameterName}: ${cmd.configurationValue}"
	} 
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "zwaveEvent: $cmd"
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

private onSetCmds() {
	[
		onSetCmd(),
		basicGetCmd()
	]
}
private onSetCmd() {
	basicSetCmd(0xFF)
}

private offSetCmds() {
	[
		offSetCmd(),
		basicGetCmd()
	]
}

private offSetCmd() {
	basicSetCmd(0x00)
}

private basicSetCmd(value) {
	secureCmd(zwave.basicV1.basicSet(value: value))
}

private basicGetCmd() {
	secureCmd(zwave.basicV1.basicGet())
}

private supportedSecurityGetCmd() {
	secureCmd(zwave.securityV1.securityCommandsSupportedGet())
}

private configGetCmd(paramNumber) {
	secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private configSetCmd(paramNumber, configValue) {
	secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, scaledConfigurationValue: configValue))
}

private switchMultilevelGetCmd() {
	secureCmd(zwave.switchMultilevelV1.switchMultilevelGet())
}

private switchMultilevelSetCmd() {
	// 1 or 255: Emergency sound.
	// 2: Fire alert.
	// 3: Ambulance sound.
	// 4: Police car sound.
	// 5: Door chime.
	// 6~99: Beep Beep.
	// 0: means stop the sound.
}

private switchBinaryGetCmd() {
	secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private sensorBinaryGetCmd() {
	secureCmd(zwave.sensorBinaryV2.sensorBinaryGet())
}

private versionGetCmd() {
	secureCmd(zwave.versionV1.versionGet())
}

private powerlevelGetCmd() {
	secureCmd(zwave.powerlevelV1.powerlevelGet())
}

private secureCmd(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}
