/*
 *  HomeSeer Status LED Blink Frequency Child v1.0
 *  	(Child Device for HomeSeer HS-WS200+, HS-WD200+, and HS-FC200+)
 *
 *	Allows users to change the Status LED Blinking Frequency using any of the built-in SmartApps.
 *
 *
 *  Changelog:
 *
 *    1.0 (10/31/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 HomeSeer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

metadata {
    definition (
		name: "HomeSeer Status LED Blink Frequency Child",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.sensor",
		mnmn: "SmartThingsCommunity",
		vid: "e1a0204d-e4e2-3b7e-851b-67a9a493a9de"
	) {
        capability "Switch Level"
		capability "Switch"
		capability "platemusic11009.message"
		capability "platemusic11009.messageTwo"
		capability "platemusic11009.hsStatusLedBlinkFrequency"
		capability "platemusic11009.hsChildDeviceWorkaround"
    }

	preferences() {
		input "debugOutput", "enum",
			title: "Debug Logging",
			required: false,
			defaultValue: 1,
			options: [0:"Disabled", 1:"Enabled [DEFAULT]"]
	}
}


def parse(String description) {
	logDebug "parse($description)..."

	return []
}


def installed() {
    logDebug "installed()..."

	initialize()
}

def updated() {
	logDebug "updated().."

    initialize()
}

void initialize() {
	state.debugLoggingEnabled = (parent.safeToInt(settings?.debugOutput, 1) != 0)

	if (!device.currentValue("workaround")) {
		sendEvent(name: "workaround", value: "statusLedBlinkFrequency")
	}

	if (!device.currentValue("message")) {
		sendEvent(name: "message", value: "<small><b>Switch Off:</b> Blink Off<br><b>Switch On:</b> Blink Frequency Value</small>")
	}

	if (!device.currentValue("messageTwo")) {
		sendEvent(name: "messageTwo", value: "<small><b>Dimmer:</b> Blink Frequency (1=100ms)</small>")
	}

	if (device.currentValue("statusLedBlinkFrequency") == null) {
		sendBlinkFrequencyEvents(3)
	}
}


def sendBlinkFrequencyEvents(frequency) {
	sendEventIfNew("statusLedBlinkFrequency", frequency)
	sendEventIfNew("switch", (frequency ? "on" : "off"))
	sendEventIfNew("level", (frequency > 100 ? 100 : frequency), "%")

	if (frequency) {
		state.lastFrequency = frequency
	}
}


def setStatusLedBlinkFrequency(frequency) {
	logDebug "setStatusLedBlinkFrequency($frequency)"

	parent.setStatusLedBlinkFrequency(frequency)
}


def setLevel(level, rate=null) {
	logDebug "setLevel($level, $rate)..."

	parent.setStatusLedBlinkFrequency(level)
}


def off() {
    logDebug "off()..."

	parent.setStatusLedBlinkFrequency(0)
}


def on() {
    logDebug "on()..."

	parent.setStatusLedBlinkFrequency(parent.safeToInt(state.lastFrequency, 1))
}


void sendEventIfNew(String name, value, String unit="") {
	if (device.currentValue(name) != value) {
		def desc = "${device.displayName}: ${name} is ${value}${unit}"
		logDebug(desc)

		Map evt = [
			name: name,
			value: value,
			descriptionText: desc
		]

		if (unit) {
			evt.unit = unit
		}

		sendEvent(evt)
	}
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled) {
		log.debug(msg)
	}
}