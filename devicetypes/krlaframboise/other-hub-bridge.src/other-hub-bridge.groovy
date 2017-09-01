/**
 *  Other Hub Bridge 0.0 (ALPHA)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    0.0 (09/01/2017)
 *			- Alpha Relase
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Other Hub Bridge", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Sensor"
		capability "Refresh"
		capability "Polling"

		attribute "status", "string"
		attribute "allDevices", "string"

	}

	preferences {
		input("otherHubIP", "string",
			title: "Other Hub IP Address:\n(Example: 192.168.0.0)",
			required: true
		)
		input("otherHubPort", "string",
			title: "Other Hub Port:\n(Example: 8080)",
			required: true
		)
	}

	tiles(scale: 2) {     	
		standardTile("status", "device.status", height:2, width:6, key: "PRIMARY_CONTROL") {
			state "default", label:'${currentValue}', icon: "st.Lighting.light99-hue"
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}

		main "status"
		details(["status", "refresh"])
	}
}

def updated() {
	log.debug "updated()..."
	refresh()
}

def poll() {
	logTrace "poll()..."
	if (!state.updating) {
		refresh()
	}
}

def refresh() {
  logTrace "refresh()..."
	state.updating = true
	sendRequest("/device/list/data")
	return []
}

def refreshDevices() {
	state.allDevices = []
	def paths = []
	state.deviceList?.each {
		paths << "/device/ui/data/${it.id}"
	}
	sendRequests(paths)
}

private sendRequest(path, method="GET") {
	// logTrace "${method} ${path}"
	sendHubCommand(new physicalgraph.device.HubAction(
		method: "$method",
		path: "$path",
		headers: ["HOST": otherHubAddress],
		query: [
			callback: hubAddress
		]
	))	
}

private sendRequests(paths, method="GET") {
	// logTrace "${method} ${paths}"
	def cmds = []
	paths.each {
		cmds << new physicalgraph.device.HubAction(
			method: "$method",
			path: "$it",
			headers: ["HOST": otherHubAddress],
			query: [
				callback: hubAddress
			]
		)
	}
	sendHubCommand(cmds, 1000)
}

private getHubAddress() {
	return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private getOtherHubAddress() {
	return "${settings?.otherHubIP}:${settings?.otherHubPort}"
}

def parse(String description) {
	// logTrace "parse: ${description}"

	def msg = parseLanMessage(description)
	if (isDeviceDetailsData(msg?.data)) {
		storeDevice(msg?.data)
		runIn(5, finishStoringDevices)
	}
	else {
		storeDeviceList(msg?.data)
		runIn(3, refreshDevices)
	}
	return []
}

private isDeviceDetailsData(data) {
	return "${data?.id}".isNumber()
}

private storeDeviceList(data) {
	log.info "Found ${data?.size() ?: 0} Devices"
	def deviceList = []	
	data?.each {
		deviceList << [id: it.id, lastActivity:it.lastActivityTime]
	}
	state.deviceList = deviceList	
}

private storeDevice(data) {
	logTrace "storeDevice: ${data?.name}"
	def attrValues = getAttributeValues(data)
	
	state.allDevices << [
		deviceNetworkId: "OtherHub${data.deviceNetworkId}",
			id: data.id,
			displayName: "${data.name}",
			status: "Online",
			lastActivity: findLastActivity(data),
			currentValues: attrValues,
			capabilities: getCapabilities(attrValues)
	]
}

def finishStoringDevices() {
	log.info "Stored ${state.allDevices?.size() ?: 0} Devices"
	def jsonVal = groovy.json.JsonOutput.toJson(state.allDevices)
	sendEvent(name:"allDevices", value: jsonVal, displayed: false, isStateChange: true)
	state.updating = false
}

private getAttributeValues(data) {
	def attrValues =[:]
	supportedCapabilities.each { key, value ->
		def attr = data.currentStates["${key}"]
		if (attr) {
			attrValues["${attr.name}"] = (attr.dataType == "NUMBER") ? attr.numberValue : attr.value
		}
	}
	return attrValues
}

private getCapabilities(attrValues) {
	def caps = []
	attrValues?.each { attr ->
		def capName = supportedCapabilities["${attr?.key}"]
		if (capName) {
			caps << capName
		}
	}
	return caps
}

private getSupportedCapabilities() {
	[
		"acceleration": "Acceleration Sensor",
		"alarm": "Alarm",
		"battery": "Battery",
		"carbonMonoxide": "Carbon Monoxide Detector",
		"contact": "Contact Sensor",
		"energy": "Energy Meter",
		"illuminance": "Illuminance Measurement",
		"lock": "Lock",
		"motion": "Motion Sensor",
		"power": "Power Meter",
		"presence": "Presence Sensor",
		"humidity": "Relative Humidity Measurement",
		"smoke": "Smoke Detector",
		"switch": "Switch",
		"temperature": "Temperature Measurement",
		"valve": "Valve",
		"water": "Water Sensor"
	]
}

private findLastActivity(data) {
	def utcDate = state.deviceList?.find { it.id == data.id }?.lastActivity
	if (utcDate) {
		return convertToLocalTime(utcDate)
	}
	else {
		return null
	}
}

private convertToLocalTime(utcDateString) {
	try {
		def utcDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", utcDateString.replace("+00:00", ""))
		
		// def timeZoneOffset = getTimeZoneOffset(utcDate)
		// if (timeZoneOffset) {
			// return new Date(utcDate.time + timeZoneOffset)
		// }
		// else {
			return utcDate
		// }
	}
	catch (e) {
		log.error "$e"
		return null
	}	
}

private getTimeZoneOffset(utcDate) {
	def timeZoneId = location?.timeZone?.ID
	def localTZ = timeZoneId ? TimeZone.getTimeZone(timeZoneId) : null	
	return localTZ ? localTZ.getOffset(utcDate.time) : null
}


private logTrace(msg) {
	log.trace "$msg"
}