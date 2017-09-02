/**
 *  Other Hub Bridge 0.1 (ALPHA)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    0.1 (09/02/2017)
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

		attribute "status", "string"
		attribute "refreshed", "string"
		attribute "deviceList", "string"		
		attribute "deviceSummary", "string"
	}

	preferences {
		input "otherHubIP", "string",
			title: "Other Hub IP:\n(Example: 192.168.0.0)",
			required: true		
		input "otherHubPort", "string",
			title: "Other Hub Port:\n(Example: 8080)",
			required: true
		input "refreshInterval", "enum",
			title: "Refresh Interval:",
			defaultValue: refreshIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: refreshIntervalOptions.collect { it.name }
		input "debugLogging", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles() {     	
		standardTile("status", "device.status", height:1, width:1, key: "PRIMARY_CONTROL") {
			state "default", label:'${currentValue}', icon: "st.Lighting.light99-hue"
		}
		standardTile("refreshed", "device.refreshed", decoration: "flat", height:1, width:1) {
			state "default", label:'Refreshed \n ${currentValue}'
		}
		standardTile("refresh", "device.refresh", height:1, width:1) {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("deviceSummary", "device.deviceSummary", decoration: "flat", height:3, width:3) {
			state "default", label:'${currentValue}'
		}

		main "status"
		details(["status", "refreshed", "refresh", "deviceSummary"])
	}
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		logDebug "updated()..."
		initialize()
	}
}

private initialize() {
	def dni = otherHubDeviceNetworkId
	if (dni != ":" && device.deviceNetworkId != dni) {
		log.warn "Attempting to change the Device Network Id from ${device.deviceNetworkId} to ${dni}, but you might have to make this change manually in the IDE."
		device.deviceNetworkId = "$dni"		
	}
	else if (!state.deviceList) {
		runIn(2, refresh)
	}
	
	unschedule()
	
	if (dni != ":") {
		switch (refreshIntervalSettingMinutes) {
			case 0:
				// Auto Refresh Disabled
				break
			case 5:
				runEvery5Minutes(autoRefresh)
				break
			case 10:
				runEvery10Minutes(autoRefresh)
				break
			case 15:
				runEvery15Minutes(autoRefresh)
				break
			case 30:
				runEvery30Minutes(autoRefresh)
				break
			case [60, 120]:
				runEvery1Hour(autoRefresh)
				break
			default:
				runEvery3Hours(autoRefresh)			
		}
	}
	else {
		log.warn "Auto Refresh Disabled because the Other Hub Settings are incomplete or invalid."
	}
}

def autoRefresh() {
	logDebug "autoRefresh()..."
	// def minimumRefreshInterval = (refreshIntervalSettingMinutes * 60 * 1000)
	
	// if(!state.lastRefresh || ((new Date().time - state.lastRefresh) >= minimumRefreshInterval)) {
		refresh()
	// }
}

def refresh() {
  logTrace "refresh()..."
	if (!state.updating || state.skippedRefresh >= 3) {
		sendEvent(name: "status", value: "refreshing", displayed: false, isStateChange: true)
		logDebug "Requesting Device List"
		state.skippedRefresh = 0
		state.updating = false		
		sendRequests(["/device/list/data"])
	}
	else {
		logDebug "Refresh already in progress"
		state.skippedRefresh = (state.skippedRefresh != null ? state.skippedRefresh : 0) + 1
	}		
	return []
}

def refreshDevices() {
	def paths = unrefreshedDevicePaths	
	if (!paths) {
		logDebug "Refreshing All Devices"
		state.lastRefresh = new Date().time
		paths = unrefreshedDevicePaths
	}	
	else {
		logDebug "Refreshing Unrefreshed Devices"
	}
	sendRequests(paths)
}

private getUnrefreshedDevicePaths() {
	def paths = []
	state.deviceList?.findAll { it.refreshedAt != state.lastRefresh }?.each {		
		paths << "/device/ui/data/${it.id}"
	}	
	return paths
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
	sendHubCommand(cmds, 5000)
}

private getHubAddress() {
	return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private getOtherHubAddress() {
	return "${otherHubIPSetting}:${otherHubPortSetting}"
}

def parse(String description) {
	// logTrace "parse: ${description}"

	def msg = parseLanMessage(description)
	if (isDeviceDetailsData(msg?.data)) {
		storeDevice(msg?.data)
		runIn(15, finishStoringDevices, [overwrite: true])
	}
	else if (msg?.data) {
		storeDeviceList(msg?.data)
		if (!state.updating) {
			state.updating = true
			runIn(5, refreshDevices)
		}
	}
	return []
}

private isDeviceDetailsData(data) {
	return "${data?.id}".isNumber()
}

private storeDeviceList(data) {
	if (state.deviceList == null) {
		state.deviceList = []
	}
	
	def ids = state.deviceList.collect { "${it.id}" }
	
	data?.each { dev ->
		def desc = "[id:${dev.id}, displayName:${dev.name}, lastActivity:${dev.lastActivityTime}]"
		
		if (dev.name == "Device") {
			logTrace "Ignoring Device: ${desc}"
		}
		else {
			def item = state.deviceList.find { it.id == dev.id }
			if (item) {
				// logTrace "Updating Device: $desc"
		
				ids.remove(ids?.find { "$it" == "${dev.id}" })				
				item.displayName = dev.name
				item.lastActivity = dev.lastActivityTime
			}
			else {
				// logTrace "Adding Device: ${desc}"
				
				state.deviceList << [id: dev.id, name: dev.name, lastActivity:dev.lastActivityTime]
			}			
		}		
	}
	
	// Remove devices that no longer exist.
	ids.each { id ->
		logTrace "Removing Device Id: $id"
		
		state.deviceList.remove(state.deviceList.find { "${it.id}" == "$id" })
	}
	logDebug "Found ${data?.size() ?: 0} Devices"
}

private storeDevice(data) {
	logTrace "storeDevice: ${data?.name}"
	def item = state.deviceList.find { it.id == data.id }
	if (item) {
		def attrValues = getAttributeValues(data)
		item.deviceNetworkId = "OtherHub${data.deviceNetworkId}"
		item.status = "Online"			
		item.currentValues = attrValues
		item.capabilities = getCapabilities(attrValues)
		item.refreshedAt = state.lastRefresh		
	}
	else {
		log.warn "Id ${data.id} not found in deviceList"
	}
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


def finishStoringDevices() {
	def jsonVal = groovy.json.JsonOutput.toJson(state.deviceList)
	
	sendEvent(name:"deviceList", value: jsonVal, displayed: false, isStateChange: true)	
	
	sendEvent(name:"deviceSummary", value: deviceSummary, displayed: false, isStateChange: true)
			
	if (unrefreshedDevicePaths) {
		if (!isDuplicateCommand(state.lastRefresh, (5 * 60 * 1000))) {
			// It's within the minimum reporting interval so refresh the devices that were missed the previous run.
			runIn(0, refreshDevices)
		}
		else {
			sendRefreshedEvent()
		}
	}
	else {
		logDebug "All Devices Refreshed"
		state.updating = false
		sendEvent(name: "status", value: "Online", displayed: false, isStateChange: true)
		sendRefreshedEvent()
	}	
}

private sendRefreshedEvent() {
	def dt = state.lastRefresh ? new Date(state.lastRefresh) : new Date()
	sendEvent(name: "refreshed", value:  convertToLocalTimeString(dt), displayed: false, isStateChange: true)
}

private getDeviceSummary() {
	def lines = []
	state.deviceList?.each {
		lines << "${it.displayName}"
	}
	return lines ? lines.sort().join("\n") : ""
}

// Settings
private getOtherHubIPSetting() {
	return settings?.otherHubIP ?: ""
}

private getOtherHubPortSetting() {
	return settings?.otherHubPort ?: ""
}

private getRefreshIntervalSettingMinutes() {
	return convertOptionSettingToInt(refreshIntervalOptions, refreshIntervalSetting)
}

private getRefreshIntervalSetting() {
	return settings?.refreshInterval ?: "5 Minutes"
}


private getRefreshIntervalOptions() {
	[
		[name: "Disabled", value: 0],
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: "12 Hours", value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
	]
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private getOtherHubDeviceNetworkId() {
	def portHex = convertToHex(otherHubPortSetting, "%04x")
	return "${otherHubIPHex}:${portHex}"
}

private getOtherHubIPHex() {
	return otherHubIPSetting?.tokenize( "." )?.collect { convertToHex(it, "%02x") }?.join()
}

private convertToHex(val, hexFormat) {
	if ("$val".isInteger()) {
		return "$val".format(hexFormat, "$val".toInteger())?.toUpperCase()
	}
	else {
		return ""
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugLogging != false) {
		log.debug msg
	}
}

private logTrace(msg) {
	log.trace "$msg"
}