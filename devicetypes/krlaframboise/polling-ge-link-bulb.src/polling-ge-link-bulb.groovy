/**
 *  Polling GE Link Bulb v1.1
 *
 *  Author: Kevin LaFramboise (krlaframboise)
 *
 *  1.1 - 05/30/2016
 *    - Improved self polling and made configurable
 *
 *  1.0 - 05/20/2016
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
	definition (name: "Polling GE Link Bulb", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Polling"

		attribute "lastPoll", "number"		

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE_Appliances", model: "ZLL Light", deviceJoinName: "GE Link Bulb"
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
				label:'${name}', 
				action: "switch.off", 
				icon:"st.switches.light.on", 
				backgroundColor:"#79b821", 
				nextState:"turningOff"
      state "turningOff", 
				label:'${name}', 
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
		input "dimOnOff", "bool", 
			title: "Dim transition for On/Off commands?", 
			defaultValue: false, 
			required: false, 
			displayDuringSetup: true
		input "dimRate", "enum", 
			title: "Dim Rate", 
			options: ["Instant", "Normal", "Slow", "Very Slow"],
			defaultValue: "Normal", 
			required: false, 
			displayDuringSetup: true		
		input "selfPollingInterval", "number",
			title: "Self Polling Interval (Minutes)",
			defaultValue: 120,
			required: false,
			displayDuringSetup: false						
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
	def evt =  zigbee.getEvent(description)
	if (evt) {
		if (description?.contains("on/off") || (evt.name == "switch" & evt.value != device.currentValue("switch"))) {			
			logEvent("Switch turned ${evt.value}", evt)
			result << createEvent(evt)
		}
		else if (evt.name == "level" && evt.value != 0) {
			logEvent("Switch Level changed to ${evt.value}", evt)
			result << createEvent(evt)
		}
	}
	else {
		def descMap = zigbee.parseDescriptionAsMap(description)
		logDebug "Unknown Description: $description\n$descMap"
	}
	
	if (canPoll()) {
		result << createEvent(name: "lastPoll",value: new Date().time, isStateChange: true, displayed: false)
	}
	return result
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time // This method is often called twice which occassionally causes the SmartThings mobile app to crash.
		logDebug "Updating Settings"		
		if (!state.configured) {
			state.currentDimRate = settings.dimRate
			state.currentDimOnOff = settings.dimOnOff
			state.selfPollingInterval = settings.selfPollingInterval			
			state.configured = true
			return response(configure())
		}
		else if (state.currentDimOnOff != settings.dimOnOff || state.currentDimRate != settings.dimRate || state.selfPollingInterval != settings.selfPollingInterval) {
			state.currentDimRate = settings.dimRate
			state.currentDimOnOff = settings.dimOnOff
			state.selfPollingInterval = settings.selfPollingInterval
			return response(refresh())
		}			
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def on() {
	logDebug "Turning On"
	zigbee.on()
}

def off() {
	logDebug "Turning Off"
	zigbee.off()
}

def setLevel(value) {
	value = validateLevel(value)
	logDebug "Changing Switch Level to $value"	
	zigbee.setLevel(value, getDimRate())
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
	if (canPoll()) {
		logDebug "Poll: Refreshing because lastPoll was more than ${minimumPollMinutes} minutes ago."
		refresh()
	}
	else {
		logDebug "Poll: Skipped because lastPoll was within ${minimumPollMinutes} minutes"
	}
}

private canPoll() {
	def minimumPollMinutes = 29
	def lastPoll = device.currentValue("lastPoll")
	return ((new Date().time - lastPoll) > (minimumPollMinutes * 60 * 1000))
}


def refresh() {
	logDebug "Refreshing Switch and Switch Level"		
	return configureSwitchReporting() + 
		configureSwitchLevelReporting() +
		zigbee.onOffConfig() + 
		zigbee.levelConfig() + 
		setOnOffDimRate() 
}

def configure() {
	logDebug "Configuring Reporting and Bindings."	
	return zigbee.onOffConfig() + 
		zigbee.levelConfig() + 
		setOnOffDimRate() + 
		configureSwitchReporting() + 
		configureSwitchLevelReporting()
}

private setOnOffDimRate() {
	logDebug "Setting Dim Rate for Off and On Commands"
	def onOffDimRate = settings.dimOnOff ? getDimRate() : 0
	zigbee.writeAttribute(0x0008, 0x0010, 0x21, zigbee.convertToHexString(onOffDimRate, 4))	
}

private configureSwitchReporting() {
	def interval = ((settings.selfPollingInterval ?: 120) * 60)
	zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, interval, null)
}

private configureSwitchLevelReporting() {
	def interval = ((settings.selfPollingInterval ?: 120) * 60)
	zigbee.configureReporting(0x0008, 0x0000, 0x20, 1, interval, 0x01)
}

private logEvent(msg, evt) {
	if (validateBool(settings.infoLoggingEnabled, true) && device.currentValue(evt.name) != evt.value) {
		log.info "${device.displayName}: $msg"
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