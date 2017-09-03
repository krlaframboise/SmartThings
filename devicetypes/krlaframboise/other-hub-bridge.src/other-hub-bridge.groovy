/**
 *  Other Hub Bridge 0.0.3 (ALPHA)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    0.0.3 (09/03/2017)
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
		capability "Bridge"
		capability "Refresh"
		capability "Health Check"

		attribute "lastCheckin", "string"
		attribute "status", "string"
		attribute "refreshed", "string"
		attribute "deviceList", "string"		
		attribute "deviceSummary", "string"
	}

	preferences {
		input "excludedDeviceIds", "string",
			title: "Excluded Device Ids:\n(example: 32,25,43)", 
			required: false
		input "refreshInterval", "enum",
			title: "Refresh Interval:",
			defaultValue: refreshIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: refreshIntervalOptions.collect { it.name }
		input "commandDelay", "number",
			title: "Enter the delay that should be used between commands being sent to the device: (milliseconds)",
			defaultValue: commandDelaySetting,
			required: false
		input "debugLogging", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
		input "infoLogging", "bool", 
			title: "Enable Info logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
		input "debugLogging", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
		input "traceLogging", "bool", 
			title: "Enable trace logging?", 
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
		unschedule()
		initialize()
	}
}

private initialize() {
	if (!state.deviceList) {
		runIn(2, refresh)
	}
	
	def checkInterval = ((refreshIntervalSettingMinutes * 60) + (60 * 5))
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "LAN", hubHardwareId: device.hub.hardwareID])	

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

def autoRefresh() {
	logDebug "autoRefresh()..."
	refresh()
}

def ping() {
	logDebug "ping()..."
	sendRequests(["/installedapp/list/data"])
}

def refresh() {
  logTrace "refresh()..."
	if (!state.updating || state.skippedRefresh >= 3) {
		logDebug "Requesting Device List"
		
		state.skippedRefresh = 0		
		state.lastRefresh = new Date().time
		scheduleFinishRefresh()
		
		sendEvent(name: "status", value: "Refreshing", displayed: false, isStateChange: true)		
		
		sendRequests(["/device/list/data"])		
	}
	else {
		logDebug "Refresh already in progress"
		state.skippedRefresh = (state.skippedRefresh ?: 0) + 1
	}
	return []
}

private scheduleFinishRefresh() {
	def delay = "${(commandDelaySetting / 1000)}".toDouble()?.round(0)?.toInteger() ?: 1
	runIn((delay * 3), finishRefresh)
}

def refreshDevices() {	
	sendRequests(unrefreshedDevicePaths)	
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
	def hostAddress = otherHubAddress
	if (hostAddress) {
	
		def cmds = []
		paths.each {
			cmds << new physicalgraph.device.HubAction(
				method: "$method",
				path: "$it",
				headers: ["HOST": "$hostAddress"],
				query: [
					callback: hubAddress
				]
			)
		}
		
		sendHubCommand(cmds, commandDelaySetting)
	}
	else {
		log.warn "Invalid otherHubAddress: ${otherHubAddress}"
	}
}

private getHubAddress() {
	return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private getOtherHubAddress() {
	def ip = getDataValue("ip")
	def port = getDataValue("port")
	return (ip != null && port != null) ? "${convertHexToIP(ip)}:${convertHexToInt(port)}" : ""
}

def sync(ip, port) {
	logTrace "sync($ip, $port)..."
	def existingIp = getDataValue("ip")
	def existingPort = getDataValue("port")
	if (ip && ip != existingIp) {
		updateDataValue("ip", ip)
	}
	if (port && port != existingPort) {
		updateDataValue("port", port)
	}
}


def parse(String description) {
	def msg = parseLanMessage(description)
	// logTrace "parsedLanMessage: $msg"
	
	sendLastCheckinEvent()
	
	if (isDeviceDetailsData(msg?.data)) {
		storeDevice(msg?.data)		
		scheduleFinishRefresh()
	}
	else if (isSmartAppListData(msg?.data)) {	
		logInfo "${device.displayName} is Online" // Device Watch Pinged Device
	}
	else if (isDeviceListData(msg?.data)) {		
		storeDeviceList(msg?.data)
		
		if (!state.updating) {
			state.updating = true
			logInfo "Refreshing ${state.deviceList?.size() ?: 0} Devices"
			runIn(1, refreshDevices)
		}
	}
	return []
}

private sendLastCheckinEvent() {
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time
		sendEvent(name:"lastCheckin", value: convertToLocalTimeString(new Date()), display:false)
	}
}

private isDeviceDetailsData(data) {
	return fieldInData("currentStates", data)
}

private isDeviceListData(data) {
	return fieldInData("lastActivityTime", data)
}

private isSmartAppListData(data) {
	return fieldInData("appTypeId", data)
}

private fieldInData(field, data) {
	return (data?.toString()?.contains("${field}:") == true)
}

private storeDeviceList(data) {
	if (state.deviceList == null) {
		state.deviceList = []
	}
	
	def ids = state.deviceList.collect { "${it.id}" }
	
	data?.each { dev ->
		def desc = "[id:${dev.id}, displayName:${dev.name}, lastActivity:${dev.lastActivityTime}]"
		
		if (!excludedDeviceIdsSetting?.find { "$it" == "${dev.id}"}) {
			def item = state.deviceList.find { it.id == dev.id }
			if (item) {
				// logTrace "Updating Device: $desc"
		
				ids.remove(ids?.find { "$it" == "${dev.id}" })				
				item.displayName = "${dev.name}"
				item.lastActivity = dev.lastActivityTime
			}
			else {
				// logTrace "Adding Device: ${desc}"
				
				state.deviceList << [id: dev.id, displayName: "${dev.name}", lastActivity:dev.lastActivityTime]
			}			
		}		
	}
	
	// Remove devices that no longer exist.
	ids.each { id ->
		logTrace "Removing Device Id: $id"
		
		state.deviceList.remove(state.deviceList.find { "${it.id}" == "$id" })
	}
	
	if (!device.currentValue("deviceSummary")) {
		sendDeviceSummaryEvent()
	}
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


def finishRefresh() {
	def jsonVal = groovy.json.JsonOutput.toJson(state.deviceList)
	
	sendEvent(name:"deviceList", value: jsonVal, displayed: false, isStateChange: true)	
	
	sendDeviceSummaryEvent()
	
	def skipped = unrefreshedDevicePaths?.size() ?: 0
	def total = state.deviceList?.size() ?: 0
	if (skipped) {
		if (isDuplicateCommand(state.lastRefresh, (4 * 60 * 1000))) {
			// It's within the minimum reporting interval so refresh the devices that were missed the previous run.
			logInfo "Attempting to Refresh ${skipped} Skipped Devices."
			runIn(0, refreshDevices)
		}
		else {
			logInfo "${(total - skipped)} Devices Refreshed / ${skipped} Devices Skipped"
			sendRefreshedEvent()
		}
	}
	else {
		logInfo "${total} Devices Refreshed"
		state.updating = false
		sendEvent(name: "status", value: "Online", displayed: false, isStateChange: true)
		sendRefreshedEvent()
	}	
	return []
}

private sendDeviceSummaryEvent() {
	sendEvent(name:"deviceSummary", value: deviceSummary, displayed: false, isStateChange: true)
}

private sendRefreshedEvent() {
	def dt = state.lastRefresh ? new Date(state.lastRefresh) : new Date()
	sendEvent(name: "refreshed", value:  convertToLocalTimeString(dt), displayed: false, isStateChange: true)
}

private getDeviceSummary() {
	def lines = []
	state.deviceList?.each {
		lines << "${it.displayName} (${it.id})"
	}
	return lines ? lines.sort().join("\n") : ""
}


// Settings
private getCommandDelaySetting() {
	return safeToInt(settings?.commandDelay, 3000)
}

private getExcludedDeviceIdsSetting() {
	return settings?.excludedDeviceIds?.split(",")?.collect { it.trim() } ?: []
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

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]), convertHexToInt(hex[2..3]), convertHexToInt(hex[4..5]), convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logInfo(msg) {
	if (settings?.infoLogging != false) {
		log.info msg
	}
}

private logDebug(msg) {
	if (settings?.debugLogging != false) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (settings?.traceLogging != false) {
		log.trace "$msg"
	}
}