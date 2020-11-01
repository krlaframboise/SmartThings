/*
 *  HomeSeer Status LED Blinking Color Child v1.0
 *  	(Child Device for HomeSeer HS-WD200+, and HS-FC200+)
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

@Field static int colorTempMinBlinkingValue = 4500


metadata {
    definition (
		name: "HomeSeer Status LED Blinking Color Child",
		namespace: "HomeSeer",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "oic.d.light",
		mnmn: "SmartThingsCommunity",
		vid: "2e1a6c3f-54f4-33ed-b344-c61e2f0547b6"
	) {
		capability "Switch"
        capability "Color Control"
		capability "Color Temperature"
		capability "platemusic11009.message"
		capability "platemusic11009.messageTwo"
        capability "platemusic11009.hsStatusLedBlinkingColor"
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

	state.lastBlinking = false
	state.lastColorName = "white"

	initialize()
}

def updated() {
	logDebug "updated()..."

    initialize()
}

void initialize() {
	state.debugLoggingEnabled = (parent.safeToInt(settings?.debugOutput, 1) != 0)

	if (!device.currentValue("workaround")) {
		state.ledNum = (parent.safeToInt(getDataValue("ledNum")) ?: null)

		String name = getDataValue("ledName")
		sendEvent(name: "workaround", value: (name ? "statusLed${name}" : "statusLedAll"))
	}

	if (!device.currentValue("message")) {
		sendEvent(name:"message", value:"<small><b>Switch Off</b>: LED Off<br><b>Color Control:</b> LED Color</small>")
	}

	if (!device.currentValue("messageTwo")) {
		sendEvent(name:"messageTwo", value:"<small><b>Color Temperature</b>:<br>Above ${colorTempMinBlinkingValue} = Blink Enabled</small>")
	}

	if (device.currentValue("colorTemperature") == null) {
		sendEvent(name: "colorTemperature", value: 1, unit: "K")
	}

	if (device.currentValue("statusLedColor") == null) {
		sendColorEvents("white")
	}
}


def setStatusLedColor(color) {
	logDebug "setStatusLedColor($color)..."

	executeSetStatusLedColor(color, parent.isBlinkingColorName(color))
}


def off() {
    logDebug "off()..."

	executeSetStatusLedColor("off")
}


def on() {
    logDebug "on()..."

	executeSetStatusLedColor(state.lastColorName)
}


def setColorTemperature(temperature) {
	logDebug "setColorTemperature($temperature)..."

	sendEvent(name: "colorTemperature", value: temperature, unit: "K")

	boolean blinking = (temperature >= colorTempMinBlinkingValue)
	executeSetStatusLedColor(state.lastColorName, blinking)
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

	executeSetStatusLedColor(findClosestMatchingSupportedColor(hex))
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


void executeSetStatusLedColor(String color, blinking=null) {
	blinking = (blinking == null) ? parent.isBlinkingColorName(color) : blinking
	String colorName = parent.formatColorName(color, blinking)

	parent.executeSetStatusLedColor(colorName, state.ledNum)
}


void sendColorEvents(statusLedColor) {
	sendEventIfNew("statusLedColor", statusLedColor)

	if (statusLedColor != "off") {
		state.lastColorName = statusLedColor

		sendBlinkingEvents(statusLedColor)

		sendEventIfNew("switch", "on")

		String color = parent.formatColorName(statusLedColor, false)
		String hex = supportedColors.get(color)
		sendEventIfNew("color", hex)

		def hsv = colorUtil.hexToHsv(hex)
		sendEventIfNew("hue", parent.safeToPercentInt(hsv[0]))
		sendEventIfNew("saturation", parent.safeToPercentInt(hsv[1]))
	}
	else {
		sendEventIfNew("switch", "off")
	}
}


void sendBlinkingEvents(statusLedColor) {
	boolean blinking = parent.isBlinkingColorName(statusLedColor)
	state.lastBlinking = blinking

	int colorTemp = parent.safeToInt(device.currentValue("colorTemperature"))

	Integer newColorTemp = null
	if (blinking && (colorTemp < colorTempMinBlinkingValue)) {
		newColorTemp = (colorTempMinBlinkingValue + 1000)
	}
	else if (!blinking && (colorTemp > colorTempMinBlinkingValue)) {
		newColorTemp = (colorTempMinBlinkingValue - 1000)
	}

	if (newColorTemp != null) {
		sendEventIfNew("colorTemperature", newColorTemp, "K")
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