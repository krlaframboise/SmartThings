/**
 *  Generic Computer v1.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *       This device handler was created using the SmartThings *       zwave Metering Switch device handler as a starting point.
 *
 *  URL to documentation:
 *    
 *
 *  Changelog:
 *
 *    1.0 (08/28/2016)
 *      - Initial Release 
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "Generic Computer", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Sensor"
		capability "Configuration"
		capability "Refresh"
		capability "Polling"
		capability "Switch"
		capability "Switch Level"		
		capability "Energy Meter"
		capability "Power Meter"
		capability "Music Player"

		attribute "lastPoll", "number"
		
		command "reset"
		command "shutdown"
		command "increaseLevel"
		command "decreaseLevel"
		command "openComcast"
		command "openNetflix"
		command "openHulu"
		command "openVudu"
		command "openPandora"
		command "closeFirefox"
	}

	preferences {
		input "webserverHost", "text",
			title: "Webserver Host:",
			defaultValue: "192.168.1.2:8080",
			displayDuringSetup: true,
			required: false
		input "webserverPath", "text",
			title: "Root Path:",
			defaultValue: "/",
			displayDuringSetup: true,
			required: false
		input "webserverUsername", "text",
			title: "Webserver Username:",
			displayDuringSetup: true,
			required: false
		input "webserverPassword", "password",
			title: "Webserver Password:",
			displayDuringSetup: true,
			required: false
	}
		
	tiles(scale: 2) {	
    multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4, canChangeIcon: true) {
        tileAttribute("device.status", key: "PRIMARY_CONTROL") {
            attributeState("paused", label:"Paused",)
            attributeState("playing", label:"Playing")
            attributeState("stopped", label:"Stopped")
        }
        tileAttribute("device.status", key: "MEDIA_STATUS") {
            attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
            attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
            attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
        }
        tileAttribute("device.status", key: "PREVIOUS_TRACK") {
            attributeState("status", action:"music Player.previousTrack", defaultState: true)
        }
        tileAttribute("device.status", key: "NEXT_TRACK") {
            attributeState("status", action:"music Player.nextTrack", defaultState: true)
        }
        tileAttribute ("device.level", key: "SLIDER_CONTROL") {
            attributeState("level", action:"music Player.setLevel")
        }
        tileAttribute ("device.mute", key: "MEDIA_MUTED") {
            attributeState("unmuted", action:"music Player.mute", nextState: "muted")
            attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
        }
        tileAttribute("device.trackDescription", key: "MARQUEE") {
            attributeState("trackDescription", label:"${currentValue}", defaultState: true)
        }
    }
		standardTile("switch", "device.switch", width: 2, height: 2) {
				state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
				state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		standardTile("netflix", "generic", width: 2, height: 2) {
				state "default", label: 'Netflix', action: "openNetflix"
		}
		standardTile("vudu", "generic", width: 2, height: 2) {
				state "default", label: 'Vudu', action: "openVudu"
		}
		standardTile("hulu", "generic", width: 2, height: 2) {
				state "default", label: 'Hulu', action: "openHulu"
		}		
		standardTile("pandora", "generic", width: 2, height: 2) {
				state "default", label: 'Pandora', action: "openPandora"
		}
		standardTile("comcast", "generic", width: 2, height: 2) {
				state "default", label: 'Comcast', action: "openComcast"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 1) {
			state "default", label:'${currentValue} kWh'
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 1) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
				
		main "mediaMulti"
		details(["mediaMulti","switch","comcast","pandora","netflix","hulu","vudu","energy","power","refresh","reset"])
	}
}

def increaseLevel() {
	logDebug "Executing increaseLevel"
	return setLevel(device.currentValue("level") + 10)
}

def decreaseLevel() {
	logDebug "Executing decreaseLevel"
	return setLevel(device.currentValue("level") - 10)
}

def setLevel(level, rate=null) {	
	if (level > 100) {
		level = 100
	}
	else if (level < 0) {
		level = 0
	}
	else if (level % 10 > 0) {
		level = level + (10 - level % 10) // Event ghost configured for multiples of 10.
	}	
	def val = level.toString().replace(".0", "")
	sendEvent(name: "level", value: val)
	logDebug "Setting level to $val%"
	
	return executeEventGhostCommand("setVolume${val}")
}

def on() {
	logDebug "Executing on() Command"
	sendEvent(name: "status", value: "Turning On")
	state.offPending = false
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
	logDebug "Executing off() Command"
	return shutdown()
}

private shutdown() {
	logDebug "Shutting Down Computer"
	sendEvent(name: "status", value: "Shutting Down", isStateChange: true)
	state.offPending = true
	return executeEventGhostCommand("forceShutdown")
}

def turnOff() {
	logDebug "Turning Off Computer"
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def play() {
	sendEvent(name: "status", value: "unpaused", isStateChange: true)
	logDebug "Executing play() Command"
	return executeEventGhostCommand("unpauseShow")
}

def pause() {
	logDebug "Executing pause() Command"
	sendEvent(name: "status", value: "paused", isStateChange: true)
	return executeEventGhostCommand("pauseShow")
}
    
def stop() {
	logDebug "Executing stop() Command"
	sendEvent(name: "status", value: "Stopped", isStateChange: true)
	return executeEventGhostCommand("pause")
}

def nextTrack() {
	logDebug "nextTrack Not Implemented"
}

def previousTrack() {
	logDebug "previousTrack Not Implemented"	
}

def mute() {
	logDebug "Executing mute() Command"
	sendEvent(name: "mute", value: "muted", isStateChange: true)
	return executeEventGhostCommand("mute")
}

def unmute() {
	logDebug "Executing unmute() Command"
	sendEvent(name: "mute", value: "unmuted", isStateChange: true)
	return executeEventGhostCommand("unmute")
}

def openNetflix() {
	logDebug "Executing openNetflix()"
	return executeEventGhostCommand("openNetflix")
}

def openVudu() {
	logDebug "Executing openVudu()"
	return executeEventGhostCommand("openVudu")
}

def openComcast() {
	logDebug "Executing openComcast()"
	return executeEventGhostCommand("openComcast")
}

def openHulu() {
	logDebug "Executing openHulu()"
	return executeEventGhostCommand("openHulu")
}

def closeFirefox() {
	logDebug "Executing closeFirefox()"
	return executeEventGhostCommand("closeFirefox")
}

def executeEventGhostCommand(cmd) {
	def result
	if (settings.webserverHost && settings.webserverPath){
		logInfo "Sending $cmd command to the EventGhost webserver"
			result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "${settings.webserverPath}?${cmd}",
				headers: getHeaders(),
				query: []
		)
	}
	else {
		log.warn "You must specify a Webserver Host and Path in order to use the ${cmd} command."
	}
	return result
}

private getHeaders() {
	def headers = [HOST: "${settings.webserverHost}"]
	
	if (settings.webserverUsername) {
		def encodedCredentials = "${settings.webserverUsername}:${settings.webserverPassword}".getBytes().encodeBase64()
		
		headers.Authorization = "Basic $encodedCredentials"
	}
	return headers
}

def poll() {
	logDebug "Executing poll()"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
	logDebug "Executing refresh()"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def configure() {
	logDebug "Executing configure()"
	zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}

def reset() {
	logDebug "Executing reset()"
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
}

def updated() {
	if (!state.configured) {
		sendEvent(name: "switch", value: "on", displayed: false)
		sendEvent(name: "level", value: 100, displayed: false)
		sendEvent(name: "mute", value: "unmuted", displayed: false)
		sendEvent(name: "status", value: "", displayed: false)
		state.configured = true
	}
	try {
		if (!state.MSR) {
			response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
		}
	} 
	catch (e) { 
		logDebug e 
	}
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
		if (cmd) {
			sendEvent(name: "lastPoll", value: new Date().time, displayed: false)
			result = zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
		def val = Math.round(cmd.scaledMeterValue)
		def result = []
		result <<	createEvent(name: "power", value: val, unit: "W")
		if (val <= 1 && state.offPending) {
			state.offPending = false
			result << response(turnOff())
		}
		return result
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def evt = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	logDebug "msr: $msr"
	updateDataValue("MSR", msr)

	// retypeBasedOnMSR()

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (msr.startsWith("0086") && !state.aeonconfig) {  // Aeon Labs meter
		state.aeonconfig = 1
		result << response(delayBetween([
			zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),   // report power in watts
			zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),   // report energy in kWh
			zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),    // no third report
			//zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	} else {
		result << response(delayBetween([
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	}

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "$device.displayName: Unhandled: $cmd"
	[:]
}

private logDebug (msg) {
	log.debug msg
}

private logInfo(msg) {
	log.info msg
}