/*
 *  HomeSeer Normal Mode Child v1.0
 *  	(Child Device for HomeSeer HS-WS200+, HS-WD200+, and HS-FC200+)
 *
 *	Allows users to toggle Normal/Status mode and change the Normal Mode LED Color using any of the built-in SmartApps.
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

import groovy.transform.Field

@Field static Map supportedColors = [red: "FF0000", green: "00FF00", blue: "0000FF", magenta: "FF00FF", yellow: "FFFF00", cyan: "00FFFF", white: "FFFFFF"]


metadata {
    definition (
		name: "HomeSeer Normal Mode Child",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.sensor",
		mnmn: "SmartThingsCommunity",
		vid: "40c65c8c-8121-3425-acaf-80ac244c954c"
	) {
		capability "Switch"
        capability "Color Control"
		capability "platemusic11009.message"
		capability "platemusic11009.messageTwo"
		capability "platemusic11009.hsLedMode"
        capability "platemusic11009.hsNormalLedColor"
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

	if (!device.currentValue("message")) {
		sendEvent(name:"message", value:"<small><b>Switch On:</b> Normal LED Mode<br><b>Switch Off:</b> Status LED Mode</small>")
	}

	if (!device.currentValue("messageTwo")) {
		sendEvent(name:"messageTwo", value:"<small><b>Color Control:</b><br>Changes Normal LED Color</small>")
	}

	if (device.currentValue("ledMode") == null) {
		sendLedModeEvents("normal")
	}

	if (device.currentValue("normalLedColor") == null) {
		sendColorEvents("white")
	}
}


def setLedMode(mode) {
	logDebug "setLedMode($mode)..."

	parent.setLedMode(mode)
}


def setNormalLedColor(color) {
	logDebug "setNormalLedColor($color)..."

	parent.setNormalLedColor(color)
}


def off() {
    logDebug "off()..."

	parent.setStatusLedMode()
}


def on() {
    logDebug "on()..."

	parent.setNormalLedMode()
}


def setSaturation(saturation) {
	logDebug "setSaturation($saturation)..."

	setColor([hue: device.currentValue("hue"), saturation: saturation])
}

def setHue(hue) {
	logDebug "setHue($hue)..."

    setColor([hue: hue, saturation: device.currentValue("saturation")])
}

def setColor(color) {
	logDebug "setColor($color)..."

	int hue = parent.safeToPercentInt(color.hue)
	int saturation = parent.safeToPercentInt(color.saturation)

	String hex = color.hex
	if (hex) {
		def hsv = colorUtil.hexToHsv(hex)
		hue = parent.safeToPercentInt(hsv[0])
		saturation = parent.safeToPercentInt(hsv[1])
	}
	else {
		hex = colorUtil.hsvToHex(hue, saturation)
	}

	sendEvent(name: "hue", value: hue)
    sendEvent(name: "saturation", value: saturation)

	parent.setNormalLedColor(findClosestMatchingSupportedColor(hex))
}

String findClosestMatchingSupportedColor(originalHex) {
	String closestColor = "white"
	int closestDistance = 1000
	def rgb = colorUtil.hexToRgb(originalHex)

	supportedColors.each { name, hex ->
		def supportedRGB = colorUtil.hexToRgb(hex)

		int distance = (Math.abs(rgb[0] - supportedRGB[0]) + Math.abs(rgb[1] - supportedRGB[1]) + Math.abs(rgb[2] - supportedRGB[2]))

		if (distance < closestDistance) {
			closestDistance = distance
			closestColor = name
		}
	}
	return closestColor
}


void sendLedModeEvents(ledMode) {
	sendEventIfNew("ledMode", ledMode)
	sendEventIfNew("switch", (ledMode == "normal" ? "on" : "off"))
}


void sendColorEvents(normalLedColor) {
	sendEventIfNew("normalLedColor", normalLedColor)

	String hex = supportedColors[normalLedColor]
	sendEventIfNew("color", hex)

	def hsv = colorUtil.hexToHsv(hex)
	sendEventIfNew("hue", parent.safeToPercentInt(hsv[0]))
	sendEventIfNew("saturation", parent.safeToPercentInt(hsv[1]))
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