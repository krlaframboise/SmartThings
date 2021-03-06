/*
 *  Zooz Scene Controller Button v1.0
 *  	(Required Child Device for Zooz Scene Controller ZEN32)
 *
 *	Allows users to change the Associations, LED Mode, LED Color, and LED Brightness.
 *
 *
 *  Changelog:
 *
 *    1.0 (03/06/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 Zooz
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
		name: "Zooz Scene Controller Button",
		namespace: "Zooz",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "x.com.st.d.remotecontroller",
		mnmn: "SmartThingsCommunity",
		vid: "b6b02aa9-b685-347e-a8bd-4f0824a4a58e"
	) {
		capability "Sensor"
		capability "Button"
		capability "Refresh"
        capability "platemusic11009.zoozLedColor"
		capability "platemusic11009.zoozLedBrightness"
		capability "platemusic11009.zoozLedMode"
		capability "platemusic11009.basicSetAssociationGroup"
		capability "platemusic11009.multilevelAssociationGroup"
    }

	preferences() {

		input "debugOutput", "enum",
			title: "Enable Debug Logging?",
			required: false,
			defaultValue: 1,
			options: [0:"No", 1:"Yes [DEFAULT]"]

		input "assocInstructions", "paragraph",
			title: "Device Associations",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this button control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  Both groups support up to 5 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the setting(s) below and then remove that device from SmartThings, you MUST come back and remove it from the settings below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "basicSetDNIs", "string",
			title: "Enter Device Network IDs for the Button's Basic Set Association Group:",
			required: false

		input "multilevelDNIs", "string",
			title: "Enter Device Network IDs for the Button's Multilevel Association Group:",
			required: false
	}
}


def parse(String description) {
	logTrace "parse(${description})..."
	return []
}

def parse(Map event) {
	logTrace "parse(${event})..."

	if ((event.name == "button") || (device.currentValue(event.name) != event.value)) {
		event.descriptionText = "${device.displayName}: ${event.name} is ${event.value}"
		logDebug(event.descriptionText)
		sendEvent(event)
	}
	return []
}


def installed() {
    logDebug "installed()..."

	initialize()
}


def updated() {
	logDebug "updated().."

    initialize()

	parent.childUpdated(device.deviceNetworkId, [basicSetAssociationGroupDNIs: settings?.basicSetDNIs, multilevelAssociationGroupDNIs: settings?.multilevelDNIs])
}

void initialize() {
	state.debugLoggingEnabled = (settings?.debugOutput != 0)

	if (!state.initialized) {
		sendEvent(name: "supportedButtonValues", value: ["pushed", "held", "pushed_2x", "pushed_3x", "pushed_4x", "pushed_5x"].encodeAsJSON())
		sendEvent(name: "numberOfButtons", value: 1)
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1])
		sendEvent(name: "ledMode", value: "onWhenOff")
		sendEvent(name: "ledBrightness", value: "medium")
		sendEvent(name: "ledColor", value: "white")
		sendEvent(name: "basicSetAssociationGroup", value: "")
		sendEvent(name: "multilevelAssociationGroup", value: "")

		state.initialized = true
	}
}


def refresh() {
	logDebug "refresh()..."

	updated()

	parent.childRefresh(device.deviceNetworkId)
}


def setLedMode(mode) {
	logDebug "setLedMode(${mode})..."

	parent.childSetLedMode(device.deviceNetworkId, mode)
}


def setLedColor(color) {
	logDebug "setLedColor(${color})..."

	parent.childSetLedColor(device.deviceNetworkId, color)
}


def setLedBrightness(brightness) {
	logDebug "setLedBrightness(${brightness})..."

	parent.childSetLedBrightness(device.deviceNetworkId, brightness)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled) {
		log.debug(msg)
	}
}

void logTrace(String msg) {
	// log.trace "$msg"
}