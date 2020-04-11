/*
 *  Zooz Garage Door v1.0	(Device Handler)
 *
 *
 * WARNING: Using a homemade garage door opener can be dangerous so use this code at your own risk.
 *
 *  Changelog:
 *
 *    1.0 (04/11/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Zooz
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
		name: "Zooz Garage Door",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.garagedoor",
		mnmn:"SmartThings",
		vid:"SmartThings-smartthings-Z-Wave_Garage_Door_Opener"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Contact Sensor"
		capability "Door Control"
        capability "Garage Door Control"
		capability "Refresh"
		capability "Health Check"
		capability "Switch"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"door", type: "device.door", width: 6, height: 4){
			tileAttribute ("device.door", key: "PRIMARY_CONTROL") {
				attributeState "closed", label:'${name}', action:"open", icon:"${resourcesPath}/garage-closed.png", backgroundColor:"#00a0dc", nextState: "opening"
				attributeState "open", label:'${name}', action:"close", icon:"${resourcesPath}/garage-open.png", backgroundColor:"#e86d13", nextState: "closing"
				attributeState "opening", label:'${name}', action:"close", icon:"${resourcesPath}/garage-opening.png", backgroundColor:"#e86d13"
				attributeState "closing", label:'${name}', action:"open", icon:"${resourcesPath}/garage-closing.png", backgroundColor:"#e86d13"
			}
		}
		standardTile("openDoor", "device.door", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "default", label:'Open', action:"open", icon:"${resourcesPath}/garage-opening.png"
		}
		standardTile("closeDoor", "device.door", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "default", label:'Close', action:"close", icon:"${resourcesPath}/garage-closing.png"
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration:"flat") {
			state "default", label:'Refresh', action: "refresh", icon:"${resourcesPath}/refresh.png"
		}
		main(["door"])
		details(["door", "openDoor", "closeDoor", "refresh"])
	}

	preferences {
		input "debugLoggingEnabled", "enum",
			title: "Debug Logging:",
			required: false,
			defaultValue: 1,
			options: [0:"Disabled", 1:"Enabled [DEFAULT]"]
	}
}

String getResourcesPath() {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz"
}


def installed() {
	initialize()
}

def updated() {
	logDebug "updated()..."
	initialize()
}

void initialize() {
	if (!device.currentValue("door")) {
		sendEvent(name: "door", value: "open")
	}
	
	if (!device.currentValue("contact")) {
		sendEvent(name: "contact", value: "open")
	}
	
	if (!device.currentValue("switch")) {
		sendEvent(name: "switch", value: "off")
	}
	
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}


def parse(String description) {
	logDebug "parse(description)..."
}


def ping() {
	sendEvent(name: "healthStatus", value: "online", isStateChange: true)
}


def on() {
	if (device.currentValue("contact") == "open") {
		close()
	}
	else {
		open()
	}
}


def off() {
	sendEvent(name: "switch", value: "off")
}


def open() {
	logDebug "open()..."
	parent.childOpen(device.deviceNetworkId)
}


def close() {
	logDebug "close()..."
	parent.childClose(device.deviceNetworkId)
}


def refresh() {
	logDebug "refresh()..."
	parent.childRefresh(device.deviceNetworkId)
}


void logDebug(msg) {
	if ((settings?.debugLoggingEnabled == null) || (settings?.debugLoggingEnabled == 1)) {
		log.debug "$msg"
	}
}