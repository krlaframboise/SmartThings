/**
 *  Polling Cree Bulb 1.2.2
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *	Changelog: 
 *
 *	1.2.2 (05/23/2016)
 *    - Fixed bug with the switch level event not being reported.
 *
 *	1.2.1 (05/21/2016)
 *    - Made the poll command only refresh if it thinks
 *      internal polling is no longer running.
 *
 *	1.2 (05/20/2016)
 *    - Added preference for dim rate, cleaned up the config
 *      refresh sections and updated fingerprint to new format.
 *
 *	1.1.2 (05/13/2016)
 *    - Completely re-wrote the Cree Bulb Device Handler.
 *    - Included self polling feature.
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
	definition (name: "Polling Cree Bulb", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
    capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Polling"
		
		attribute "lastPoll", "number"
		
		fingerprint profileId: "C05E", inClusters: "0000,1000,0004,0003,0005,0006,0008", outClusters: "0000,0019", manufacturer: "CREE", model: "Connected A-19 60W Equivalent", deviceJoinName: "Cree Bulb"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", 
				label: '${name}', 
				action: "switch.off", 
				icon: "st.switches.light.on", 
				backgroundColor: "#79b821", 
				nextState:"turningOff"
			state "off", 
				label: '${name}', 
				action: "switch.on", 
				icon: "st.switches.light.off", 
				backgroundColor: "#ffffff", 
				nextState:"turningOn"
 			state "turningOn", 
				label:'Turning On', 
				action: "switch.off", 
				icon:"st.switches.light.on", 
				backgroundColor:"#79b821", 
				nextState:"turningOff"
      state "turningOff", 
				label:'Turning Off', 
				action: "switch.on", 
				icon:"st.switches.light.off", 
				backgroundColor:"#ffffff", 
				nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", decoration: "flat") {
			state "default", 
				label:'', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}

		main(["switch"])
		details(["switch", "level", "levelSliderControl", "refresh"])
	}

	preferences {
		input "dimRate", "enum", 
			title: "Dim Rate", 
			options: ["Instant", "Normal", "Slow", "Very Slow"],
			defaultValue: "Normal", 
			required: false, 
			displayDuringSetup: true		
		input "infoLoggingEnabled", "bool", 
			title: "Enable info logging?", 
			defaultValue: true,
			required: false,
			displayDuringSetup: false
		input "debugLoggingEnabled", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false,
			required: false,
			displayDuringSetup: false
	}
}

// Parse incoming device messages to generate events
def parse(String description) {			
	def result = []
	def evt = zigbee.getEvent(description)
	if (evt) {
		if (evt.name == "switch") {
			logEvent("Switch turned ${evt.value}",evt)
			result << createEvent(evt)
		}
		else if (evt.name == "level") {
			logEvent("Switch Level changed to ${evt.value}", evt)
			result << createEvent(evt)
		}
		else {
			logDebug "Unknown Event: $evt"
		}
	}
	else {
		def map = zigbee.parseDescriptionAsMap(description)		
		if (map) {
			logDebug "Unknown Command: $map"
			//result += handleUnknownDescriptionMap(map)
		}
		else { 
			logDebug "Unknown Command: $description"
		}
	}
	result << createEvent(name: "lastPoll", value: new Date().time, displayed: false, isStateChange: true)
	return result
}

// private handleUnknownDescriptionMap(map) {
	// def result = []
	// if ("${map.command}" == "0A") {	
	
		// if (map.clusterInt == 6) {
			// logDebug "Switch Reported\n$map"
			// result += response(zigbee.onOffRefresh())
		// }
		// else if (map.clusterInt == 8) {
			// logDebug "Switch Level Reported\n$map"
			// result += response(zigbee.levelRefresh())
		// }
	// }
	// return result
// }

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time // This method is often called twice which occassionally causes the SmartThings mobile app to crash.
		logDebug "Updating Settings"		
		if (!state.configured) {
			state.currentDimRate = settings.dimRate
			state.configured = true
			return response(configure())
		}
		else if (state.currentDimRate != settings.dimRate) {
			state.currentDimRate = settings.dimRate
			return response(refresh())
		}			
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def on() {
	log.debug "on()"
	//logDebug "Turning On"
	zigbee.on()
}

def off() {
	log.debug "off()"
	//logDebug "Turning Off"
	zigbee.off()
}

def setLevel(value) {
	log.debug "setLevel($value)"
	value = validateLevel(value)
	logDebug "Changing Switch Level to $value"	
	return zigbee.setLevel(value, getDimRate()) +
		zigbee.readAttribute(0x0008, 0x0000)
}

private Integer validateLevel(value) {
	if (value == 0) {
		return 1
	}
	else {
		return value ?: 100
	}
}

private getDimRate() {
	switch (settings.dimRate) {		
		case "Normal":
			return 15			
		case "Slow":
			return 25
		case "Very Slow":
			return 35
		default:
			return 0
	}
}

def poll() {
	def minimumPollMinutes = 30
	def lastPoll = device.currentValue("lastPoll")
	if ((new Date().time - lastPoll) > (minimumPollMinutes * 60 * 1000)) {
		logDebug "Poll: Refreshing because lastPoll was more than ${minimumPollMinutes} minutes ago."
		refresh()
	}
	else {
		logDebug "Poll: Skipped because lastPoll was within ${minimumPollMinutes} minutes"
	}
}

def refresh() {
	logDebug "Refreshing Switch and Switch Level"
	return zigbee.onOffRefresh() + 
		zigbee.levelRefresh() + 
		zigbee.onOffConfig() + 
		zigbee.levelConfig() // + configureSwitchReporting()
}

def configure() {
	logDebug "Configuring Reporting and Bindings."	
	return zigbee.onOffConfig() + 
		zigbee.levelConfig() + //configureSwitchReporting() + 
		zigbee.onOffRefresh() + 
		zigbee.levelRefresh()
}

// private configureSwitchReporting() {
	// def interval = 1800 // 30 Minutes
	// zigbee.configureReporting(0x0006, 0x0000, 0x10, 1, interval, null)
// }

private logEvent(msg, evt) {
	if (validateBool(settings.infoLoggingEnabled, true) && device.currentValue(evt.name) != evt.value) {
		log.info "${device.displayName}: $msg"
	}
	else {
		logDebug msg
	}
}

private logDebug(msg) {
	if (validateBool(settings.debugLoggingEnabled, false)) {
		log.debug "${device.displayName}: $msg"
	}
}

private validateBool(value, defaultValue) {
	if (value == null) {
		return defaultValue
	}
	else {
		return value
	}
}