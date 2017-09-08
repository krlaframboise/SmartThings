/**
 *  Other Hub Bridge 0.1 (BETA)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    0.1. (09/05/2017)
 *			- Beta Relase
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
		attribute "progress", "string"
		attribute "refreshed", "string"
		
		command "childOn"
		command "childOff"
		command "childRefresh"
		command "childEvent"
	}

	preferences {
		input "excludedDeviceIds", "string",
			title: "Excluded Device Ids:\n(example: 32,25,43)", 
			required: false
		input "refreshInterval", "enum",
			title: "Refresh Interval:",
			defaultValue: "Disabled",
			required: false,
			displayDuringSetup: true,
			options: refreshIntervalOptions.collect { it.name }
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

	tiles (scale: 2) {     	
		standardTile("status", "device.status", height:2, width:2, key: "PRIMARY_CONTROL") {
			state "default", label:'${currentValue}', icon: "st.Lighting.light99-hue"
		}
		standardTile("refreshed", "device.refreshed", decoration: "flat", height:2, width:2) {
			state "default", label:'Refreshed \n ${currentValue}'
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("progress", "device.progress", decoration: "flat", height:2, width:6) {
			state "default", label:'${currentValue}'
		}
		childDeviceTiles("deviceList")
	}
}

def installed() {
	logDebug "installed()..."
}

def uninstalled() {
	logTrace "uninstalled()..."
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
	if (state.retries == null) {
		state.retries = 0
	}
	
	initializeHealthCheck()

	switch (refreshIntervalSettingMinutes) {
		case 0:
			// Auto Refresh Disabled
			break
		case 5:
			runEvery5Minutes(refresh)
			break
		case 10:
			runEvery10Minutes(refresh)
			break
		case 15:
			runEvery15Minutes(refresh)
			break
		case 30:
			runEvery30Minutes(refresh)
			break
		case [60, 120]:
			runEvery1Hour(refresh)
			break
		default:
			runEvery3Hours(refresh)			
	}
}

private initializeHealthCheck() {
	if (refreshIntervalSettingMinutes || state.checkInterval) {
		def checkInterval = (24 * 60 * 60) // default 1 day
		if (refreshIntervalSettingMinutes) {
			checkInterval = ((refreshIntervalSettingMinutes * 60) + (5 * 60))
		}
		if (checkInterval != state.checkInterval) {
			state.checkInterval = checkInterval
			
			sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "LAN", hubHardwareId: device.hub.hardwareID])
		}
	}
}

void ping() {
	logDebug "ping()..."
	sendRequests(["/installedapp/list/data"])
}

void childOn(deviceId) {
	logTrace "childOn(${deviceId})..."
	sendRunAction(deviceId, "switch.on")	
}

void childOff(deviceId) {
	logTrace "childOff(${deviceId})..."
	sendRunAction(deviceId, "switch.off")	
}

private sendRunAction(deviceId, command) {
	// logTrace "${method} ${paths}"	
	
	def child = findChildByDeviceId(deviceId)
	if (child) {
				
		def msg = "Sending command ${command} to ${child.displayName}"				
		logInfo "$msg"
		sendMainEvent("progress", msg)
	
		sendHubCommand(new physicalgraph.device.HubAction(
			method: "POST",
			path: "/device/runaction",
			headers: ["HOST": "${otherHubAddress}", "Content-Type": "application/json"],
			body: [deviceId: "${deviceId}", action: "${command}", args: []]
		))
	}
	else {
		log.warn "Unable to send command ${command} for Device ${deviceId}"
	}
}

void childEvent(deviceId, name, value) {
	logTrace "childEvent($deviceId, $name, $value)"
	def child = findChildByDeviceId(deviceId)
	if (child) {
		def slurper = new groovy.json.JsonSlurper()
		def data = child.currentOtherHubData ? slurper.parseText(child.currentOtherHubData) : [attrs:[:], caps:[:]]
		
		data.attrs."$name" = value
		
		def now = new Date()
		data.activity = now		
		
		
		sendChildDataEvents(child, data)
	}
	else {
		log.warn "Device ${deviceId} not found"
	}
}

void childRefresh(deviceId) {
	state.pendingAction = true
	sendRequests([getDevicePath(deviceId)])
}

private findChildByDeviceId(deviceId) {
	return childDevices?.find { "${it.currentDeviceId}" == "$deviceId" }
}

void refresh() {
  logTrace "refresh()..."
	if (!state.refreshing || state.skippedRefresh >= 3) {
		logDebug "Requesting Device List"
		
		state.skippedRefresh = 0		
		state.lastRefresh = new Date().time
				
		sendMainEvent("status", "Refreshing")		
		sendMainEvent("progress", "Requesting Device List")
		
		sendRequests(["/device/list/data"])
	}
	else {
		logDebug "Refresh already in progress"
		state.skippedRefresh = (state.skippedRefresh ?: 0) + 1
	}
}

private scheduleFinishRefresh() {
	runIn(30, finishRefresh)
}

void refreshDevices() {
	sendRequests(unrefreshedDevicePaths)	
}

private getUnrefreshedDevicePaths() {
	def paths = []

	state.deviceList?.each { dev ->
		if (!childDevices?.find { "${it.currentDeviceId}" == "${dev.id}" && "${it?.currentLastRefresh}" == "${state.lastRefresh}" }) {
			paths << getDevicePath("${dev.id}")
		}
	}
	
	return paths
}

private getDevicePath(deviceId) {
	return "/device/ui/data/${deviceId}"
}

private void sendRequests(paths) {
	// logTrace "${method} ${paths}"
	def hostAddress = otherHubAddress
	if (hostAddress) {
	
		def cmds = []
		paths.each {
			cmds << new physicalgraph.device.HubAction(
				method: "GET",
				path: "$it",
				headers: ["HOST": "$hostAddress"],
				query: [
					callback: hubAddress
				]
			)			
		}
		
		scheduleFinishRefresh()
		sendHubCommand(cmds, 0)
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

void sync(ip, port) {
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
		updateChildDeviceDetails(msg?.data)
	}
	else if (isSmartAppListData(msg?.data)) {	
		logInfo "${device.displayName} is Online" // Device Watch Pinged Device
	}
	else if (isDeviceListData(msg?.data)) {
		updateChildDeviceList(msg?.data)
		
		logInfo "Refreshing ${state.deviceList?.size() ?: 0} Devices"
		state.refreshing = true
		refreshDevices()
	}
	return []
}

private sendLastCheckinEvent() {
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time
		sendMainEvent("lastCheckin", convertToLocalTimeString(new Date()))
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

private updateChildDeviceList(data) {
	logTrace "updateChildDeviceList..."
	def deviceList = []
	
	data?.each { dev ->
		if (dev.deviceTypeName != "Device" && !excludedDeviceIdsSetting?.find { "$it" == "${dev.id}"}) {
			deviceList << [id: "${dev.id}", lastActivityTime: dev.lastActivityTime, name: "${dev.name}"]
		}
	}
	
	if (deviceList.size() > 0) {
		childDevices?.each { child ->
			if (!deviceList.find { "${it.id}" == "${child?.currentDeviceId}" }) {
				logInfo "Deleting ${child.displayName}"
				// deleteChildDevice(child.deviceNetworkId)
			}
		}
	}
	
	sendMainEvent("progress", "Refreshing ${deviceList.size()} Devices")
	state.deviceList = deviceList
}

private updateChildDeviceDetails(data) {
	// logTrace "updateChildDevice: ${data?.name}"
	
	def lastActivity = state.deviceList?.find { "${it.id}" == "${data.id}" }?.lastActivityTime
	
	def attrs = getAttributes(data)
	def caps = getCapabilities(attrs)
	
	def child = childDevices?.find { "${it.currentDeviceId}" == "${data.id}" }
	
	if (!child) {
		state.devicesAdded = true
		if (childHasAttribute(attrs, "switch")) {
			logTrace "Adding Switch: ${data.name}"
			child = addNewChildDevice(data, "Other Hub Switch")
		}
		else if (childHasCapability(caps, "Motion Sensor")) {
			logTrace "Adding Motion Sensor: ${data.name}"
			child = addNewChildDevice(data, "Other Hub Motion Sensor")
		}		
		else if (childHasCapability(caps, "Contact Sensor")) {
			logTrace "Adding Contact Sensor: ${data.name}"
			child = addNewChildDevice(data, "Other Hub Contact Sensor")		
		}		
				
		if (!child) {
			logTrace "Adding Device: ${data.name}"
			child = addNewChildDevice(data, "Other Hub Device")
		}
		
		if (child) {		
			sendChildEvent(child, "deviceId", "${data.id}")
		}
	}
	else {
		logTrace "Updating ${data.name}"
	}
	
	sendChildEvent(child, "lastRefresh", state.lastRefresh)
	
	if (child) {
		def otherHubData = [
			id: child.deviceNetworkId, 
			displayName: child.displayName, 
			activity: lastActivity,
			attrs: attrs,
			caps: caps
		]
		
		sendChildDataEvents(child, otherHubData)
	}
}

private childHasAttribute(attrs, attr) {
	return attrs?.find { k,v -> "$k" == "$attr" } ? true : false
}

private childHasCapability(caps, cap) {
	return caps?.find { "$it" == "$cap" } ? true : false
}

private addNewChildDevice(data, deviceType) {
	try {
		return addChildDevice(
			"krlaframboise",
			"${deviceType}",
			"${getChildDNI(data.id)}", 
			null,
			[
				name: "OHB-${data.name}",
				label: "OHB-${data.name}",				
				// componentName: "OHB-Device${data.id}", 
				// componentLabel: "${data.name}",
				isComponent: false,
				completedSetup: true
			])
	}
	catch (e) {
		if ("$e".contains("UnknownDeviceTypeException")) {
			log.warn "Device Type Handler Not Installed: ${deviceType}"
		}
		else {
			log.error "$e"
		}
	}
}

private sendChildDataEvents(child, data) {
	if (data?.attrs) {
		sendChildEvent(child, "status", getChildStatus(data.attrs))
		
		sendChildCapabilityEvent(child, "Switch", "switch", (data?.attrs?."switch"?.toLowerCase() ?: "off"))
		
		sendChildCapabilityEvent(child, "Motion Sensor", "motion", (data?.attrs?."motion"?.toLowerCase() ?: "inactive"))
		
		sendChildCapabilityEvent(child, "Contact Sensor", "contact", (data?.attrs?."contact"?.toLowerCase() ?: "closed"))
	}
	
	if (data) {
		sendChildEvent(child, "otherHubData", groovy.json.JsonOutput.toJson(data))
	}
}

private sendChildCapabilityEvent(child, capName, attrName, value) {
	if (child.hasCapability("${capName}")) {		
		if (value) {
	
			def oldValue = child."current${attrName.capitalize()}"
			if ("${oldValue}" != "$value") {
			
				child.sendEvent(name: "${attrName}", value: value, displayed: true, isStateChange: true)
				
			}
		}
	}
}

private getChildStatus(attrs) {
	def attrStatuses = []
	attrs?.each { k, v ->
		def attrStatus = "$v"
		switch("$k") {
			case "battery":
				attrStatus = "${attrStatus}%"
				break
			case "temperature":
				attrStatus = "${attrStatus}°"
				break
		}
		if (!attrStatuses.find { "$it" == "$attrStatus" }) {
			attrStatuses << "$attrStatus"
		}
	}
	return attrStatuses?.join("/") ?: ""
}

private sendChildEvent(child, name, value, displayed=false) {
	// logTrace "sendChildEvent(${child}, ${name}, ${value}, ${displayed})"
	child?.sendEvent(name: "$name", value: value, displayed: displayed)
}

private getChildDNI(deviceId) {
	return "OHB-Device${deviceId}"
}

private getAttributes(data) {
	def attrValues =[:]
	supportedCapabilities.each { key, value ->
		def attr = data.currentStates["${key}"]
		if (attr) {
			attrValues["${attr.name}"] = ("${attr.dataType}" == "NUMBER") ? attr.numberValue : attr.value
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

private sendMainEvent(name, value, displayed=false) {
	sendEvent(name: "$name", value: value, displayed: displayed)
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


void finishRefresh() {
	def skipped = unrefreshedDevicePaths?.size() ?: 0
	
	if (state.refreshing && !state.devicesAdded && !state.pendingAction && skipped && state.retries <= 3) {
		state.retries = (state.retries + 1)
			
		// It's within the minimum reporting interval so refresh the devices that were missed the previous run.
		def msg = "Refreshing ${skipped} Skipped Devices."
		logInfo "$msg"
		sendMainEvent("progress", "$msg")
		runIn(3, refreshDevices)
	}
	else if (!state.refreshing && state.pendingAction) {
		state.pendingAction = false		
		sendRefreshedEvent("Device Refreshed")
	}
	else {
		def total = state.deviceList?.size() ?: 0
		def refreshStatus = skipped ? "${total - skipped} of ${total}" : "All"
	
		state.refreshing = false
		state.pendingAction = false
		state.devicesAdded = false		
		
		sendRefreshedEvent("${refreshStatus} Devices Refreshed")
	}	
}

private sendRefreshedEvent(msg) {
	logInfo "$msg"
	state.retries = 0
	def dt = state.lastRefresh ? new Date(state.lastRefresh) : new Date()
	sendMainEvent("refreshed", convertToLocalTimeString(dt))
	
	sendMainEvent("status", "Online")
	sendMainEvent("progress", "$msg")	
	
	runIn(5, clearProgress)
}

def clearProgress() {
	sendMainEvent("progress", "")
}


// Settings
private getExcludedDeviceIdsSetting() {
	return settings?.excludedDeviceIds?.split(",")?.collect { it.trim() } ?: []
}

private getRefreshIntervalSettingMinutes() {
	return convertOptionSettingToInt(refreshIntervalOptions, refreshIntervalSetting)
}

private getRefreshIntervalSetting() {
	return settings?.refreshInterval ?: "Disabled"
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
	return safeToInt(options?.find { "${settingVal}" == "${it.name}" }?.value, 0)
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