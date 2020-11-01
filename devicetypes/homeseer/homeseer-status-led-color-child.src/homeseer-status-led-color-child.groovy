/*
 *  HomeSeer Status LED Color Child v1.0
 *  	(Child Device for HomeSeer HS-WS200+)
 *
 *	Allows users to change the Status LED Color using any of the built-in SmartApps.
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
		name: "HomeSeer Status LED Color Child",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.light",
		mnmn: "SmartThingsCommunity",
		vid: "7561d501-2c43-3c60-8e32-97504ba6b95d"
	) {
		capability "Switch"
        capability "Color Control"
		capability "platemusic11009.message"
        capability "platemusic11009.hsStatusLedColor"
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
		sendEvent(name: "workaround", value: "statusLedColor")
	}

	if (!device.currentValue("message")) {
		sendEvent(name:"message", value:"<small><b>Switch Off</b>: LED Off<br><b>Color Control:</b> LED Color</small>")
	}

	if (!device.currentValue("statusLedColor")) {
		sendColorEvents("white")
	}
}


def setStatusLedColor(color) {
	logDebug "setStatusLedColor($color)..."

	parent.setStatusLedColor(color)
}


def off() {
    logDebug "off()..."

	parent.setStatusLedColor("off")
}


def on() {
    logDebug "on()..."

	parent.setStatusLedColor(state.lastColor)
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

	parent.setStatusLedColor(findClosestMatchingSupportedColor(hex))
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


void sendColorEvents(statusLedColor) {
	sendEventIfNew("statusLedColor", statusLedColor)

	if (statusLedColor != "off") {
		state.lastColor = statusLedColor

		sendEventIfNew("switch", "on")

		String hex = supportedColors.get(statusLedColor)
		sendEventIfNew("color", hex)

		def hsv = colorUtil.hexToHsv(hex)
		sendEventIfNew("hue", parent.safeToPercentInt(hsv[0]))
		sendEventIfNew("saturation", parent.safeToPercentInt(hsv[1]))
	}
	else {
		sendEventIfNew("switch", "off")
	}
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