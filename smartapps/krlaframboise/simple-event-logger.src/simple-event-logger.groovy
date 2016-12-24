/**
 *  Simple Event Logger v 0.0.2
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    N/A
 *
 *  Changelog:
 *
 *    0.0.0 (12/23/2016)
 *      - Beta Release
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
 
include 'asynchttp_v1'
 
definition(
    name: "Simple Event Logger",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Allows you to choose devices and attributes and it logs the device, event name, event value, event time, and event description of all the events that have occured since the last time it ran.",
    category: "My Apps",
iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@3x.png")
		
preferences {
	page(name: "mainPage")
	page(name: "devicesPage")
	page(name: "attributesPage")
	page(name: "optionsPage")
	page(name: "createTokenPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		if (state.allConfigured && state.loggingStatus) {
			getLoggingStatusContent()
		}
		if (state.devicesConfigured) {
			section() {
				getPageLink("devicesPageLink", "Choose Devices", "devicesPage", null, buildSummary(getSelectedDeviceNames()))
			}
		}
		else {			
			getDevicesPageContent()
		}
		
		if (state.attributesConfigured) {
			section() {
				getPageLink("attributesPageLink", "Choose Events", "attributesPage", null, buildSummary(getSupportedAttributes()))
			}			
		}
		else {
			getAttributesPageContent()
		}
				
		if (state.optionsConfigured) {
			section() {
				getPageLink("optionsPageLink", "Other Options", "optionsPage")
			}
		}
		else {			
			getOptionsPageContent()			
		}

		section() {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false		
		}
	}
}

private getLoggingStatusContent() {
	section("Logging Status") {
		def time = state.loggingStatus?.endTime ? getFormattedLocalTime(state.loggingStatus?.endTime) : ""		
		def status = state.loggingStatus?.success ? "Successful" : "Failed"
				
		paragraph "${time} - ${status}\nLogged: ${state.loggingStatus?.eventsLogged}\nTotal Logged: ${state.loggingStatus?.totalEventsLogged}\nAvailable Log Space: ${state.loggingStatus?.freeSpace}"
	}
}

def devicesPage() {
	dynamicPage(name:"devicesPage") {
		getDevicesPageContent()
	}
}

private getDevicesPageContent() {
	section("Choose Devices") {
		paragraph "Select all the devices that should log events.  If a device appears in more than one list, you only need to select it once."
		
		getCapabilities().each { 
			input "${it.cap}Pref", "capability.${it.cap}",
				title: "${it.title}:",
				multiple: true,
				submitOnChange: true,
				hideWhenEmpty: true,
				required: false
		}
			
	}
}

def attributesPage() {
	dynamicPage(name:"attributesPage") {
		getAttributesPageContent()
	}
}

private getAttributesPageContent() {
	def supportedAttr = getSupportedAttributes()
	if (supportedAttr) {
		section("Choose Events") {			
			input "allowedAttributes", "enum",
				title: "Which events should be logged?",
				required: true,
				multiple: true,					
				submitOnChange: true,
				options: supportedAttr
		}
	
		if (settings?.allowedAttributes) {
			section ("Device Event Exclusions (Optional)") {
				paragraph "This section allows you to exclude devices for each of the events being logged."
				
				settings.allowedAttributes.sort().each { attr ->
					def attrDevices = getSelectedDevices()?.findAll{ device ->
							device.hasAttribute("${attr}")
						}?.collect { it.displayName }?.unique()?.sort()
					if (attrDevices) {
						input "${attr}Exclusions", "enum",
							title: "Exclude ${attr} events:",
							required: false,
							multiple: true,
							options: attrDevices
					}
				}
			}
		}
	}
	else {
		section("Choose Events") {
			paragraph "You need to select devices before you can choose events."
		}
	}
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {
		getOptionsPageContent()
	}
}

private getOptionsPageContent() {
	section ("Logging Options") {
		input "logFrequency", "enum",
			title: "Log Events Every:",
			required: false,
			defaultValue: "5 Minutes",
			options: ["5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
		input "maxEvents", "number",
			title: "Maximum number of events to log for each device per execution. (1 - 50)",
			range: "1..50",
			defaultValue: 10,
			required: false
		input "logDesc", "bool",
			title: "Log Event Descripion?",
			defaultValue: true,
			required: false
	}
	section("Google Sheets Web App") {
		input "googleWebAppUrl", "text",
			title: "Google Sheets Web App Url:\n\n(A popup box with a URL is shown after Deploying the Google Sheets Script.  Copy and paste that entire URL into this field.)",
			required: true
	}
	section("OAuth Token") {
		getPageLink("createTokenPageLink", "Generate New OAuth Token", "createTokenPage", null, state.accessToken ? "" : "The SmartApp was unable to generate an OAuth token which usually happens if you haven't gone into the IDE and enabled OAuth in this SmartApps settings.  Once OAuth is enabled, you can click this link to try again.")
	}
	section("Live Logging Options") {
		input "logging", "enum",
			title: "Types of messages to write to Live Logging:",
			multiple: true,
			required: false,
			defaultValue: ["debug", "info"],
			options: ["debug", "info", "trace"]
	}
}

def createTokenPage() {
	dynamicPage(name:"createTokenPage") {
		
		disposeAppEndpoint()
		initializeAppEndpoint()		
		
		section() {
			if (state.accessToken) {				
				paragraph "A new token has been generated."
			}
			else {
				paragraph "Unable to generate a new OAuth token.\n\n${getInitializeEndpointErrorMessage()}"				
			}
		}
	}
}

private getPageLink(linkName, linkText, pageName, args=null,desc="") {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "$desc",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	href(map)
}

private buildSummary(items) {
	def summary = ""
	items?.each {
		summary += summary ? "\n" : ""
		summary += "  ► ${it}"
	}
	return summary
}

def uninstalled() {
	logTrace "Executing uninstalled()"
	disposeAppEndpoint()
}

private disposeAppEndpoint() {
	if (state.accessToken || state.endpoint) {
		try {
			logTrace "Revoking access token"
			revokeAccessToken()
		}
		catch (e) {
			log.warn "Unable to remove access token: $e"
		}
		state.accessToken = ""
		state.endpoint = ""
	}
}

def installed() {
	logTrace "Executing installed()"
	initializeAppEndpoint()
}

def updated() {
	logTrace "Executing updated()"
		
	unschedule()
	unsubscribe()
	
	initializeAppEndpoint()
	
	if (settings?.logFrequency && settings?.maxEvents && settings?.logDesc != null && settings?.googleWebAppUrl) {
		state.optionsConfigured = true
	}
	else {
		logDebug "Unconfigured - Options"
	}
	
	if (settings?.allowedAttributes) {
		state.attributesConfigured = true
	}
	else {
		logDebug "Unconfigured - Choose Events"
	}
	
	if (getSelectedDevices()) {
		state.devicesConfigured = true
	}
	else {
		logDebug "Unconfigured - Choose Devices"
	}
	
	state.allConfigured = (state.optionsConfigured && state.attributesConfigured && state.devicesConfigured)
	
	if  (state.allConfigured) {
		def logFrequency = (settings?.logFrequency ?: "5 Minutes").replace(" ", "")
		
		"runEvery${logFrequency}"(logNewEvents)				
	}
	else {
		logDebug "Event Logging is disabled because there are unconfigured settings."
	}
}

def logNewEvents() {
	def endDate = new Date()
	def startTime = safeToLong(state.loggingStatus?.endTime)
	
	// Set Start Date to 1 second after last execusions End Date or to the previous day if this is the first execution.
	def startDate = startTime ? new Date(startTime + 1000) : new Date() -1 
	
	logTrace "Retrieving Events from ${startDate} to ${endDate}"
	
	def events = getNewEvents(startDate, endDate)
	def eventCount = events?.size ?: 0
	
	logDebug "Found ${eventCount} events between ${getFormattedLocalTime(startDate.time)} and ${getFormattedLocalTime(endDate.time)}"
	
	if (events) {
		def jsonOutput = new groovy.json.JsonOutput()
		def jsonData = jsonOutput.toJson([time: endDate.time, appId: app.id, token: state.accessToken, events: events])
	
		def params = [
			uri: "${settings?.googleWebAppUrl}",
			contentType: "application/json",
			body: jsonData
		]	
		
		logTrace "Logging ${eventCount} events to Google Sheets"
		
		asynchttp_v1.post(processLogEventsResponse, params)
	}
	else {
		logTrace "There were no new events to Log to Google Sheets"
		def loggingStatus = state.loggingStatus ?: [:]
		loggingStatus.success = true
		loggingStatus.eventsLogged = 0
		loggingStatus.time = endDate.time
		state.loggingStatus = loggingStatus
	}
}

// Google Sheets redirects the post to a temporary url so the response is usually 302 which is page moved.
def processLogEventsResponse(response, data) {
	logTrace "Google Sheets Logging Response: ${response?.status}"
}


private initializeAppEndpoint() {		
	try {
		if (!state.accessToken) {
			logDebug "Creating Access Token"
			state.accessToken = createAccessToken()
			state.endpoint = apiServerUrl("/api/token/${state.accessToken}/smartapps/installations/${app.id}/")		
		}		
	} 
	catch(e) {
		log.warn "${getInitializeEndpointErrorMessage()}"
		state.endpoint = null
		state.accessToken = null
	}
}

private getInitializeEndpointErrorMessage() {
	return "This SmartApp requires OAuth so please follow these steps to enable it:\n1.  Go into the My SmartApps section of the IDE\n2. Click the pencil icon next to this SmartApp to open the properties\n3.Click the 'OAuth' link\n4. Click 'Enable OAuth in Smart App'."
}

mappings {
	path("/logging-result/:result") {action: [GET: "api_updateLoggingStatus"]}
}

def api_updateLoggingStatus() {
	def result = "${params?.result}".split("§")
	def loggingStatus
	if (result.size() >= 3) {
		loggingStatus = [
			success: result[0] ? true : false,
			endTime: safeToLong(result[1]),
			eventsLogged: safeToLong(result[2]),
			totalEventsLogged: safeToLong(result[3]),
			freeSpace: calculateFreeSpace(safeToLong(result[3]))
		]
	}
	else {
		loggingStatus = state.loggingStatus ?: [:]
		loggingStatus.success = false
		loggingStatus.eventsLogged = 0		
		logDebug "Unexpected Logging Status Result: $result"
	}
	state.loggingStatus = loggingStatus
	logDebug "api_updateLoggingStatus: ${state.loggingStatus}"
}

private calculateFreeSpace(totalLogged) {
	def cellsPerRow = 5
	def cellLimit = 4000000 - cellsPerRow // exclude header row	
	def allowedRows = cellLimit / cellsPerRow

	double allAttributes = (totalLogged / allowedRows) * 100
	return "${(100 - allAttributes).round(2)}%"
}

private getNewEvents(startDate, endDate) {	
	def events = []
	def maxEvents = settings?.maxEvents ?: 10
	
	getSelectedDevices().each  { device ->

		def deviceAllowedAttrs = getDeviceAllowedAttrs(device.displayName)
				
		device.eventsBetween(startDate, endDate, [max: maxEvents])?.flatten().each { event ->
			
			if ("${event.source}" == "DEVICE" && deviceAllowedAttrs.find { attr -> attr.toLowerCase() == event.name.toLowerCase() }) {				
				events << [
					time: getFormattedLocalTime(event.date.time),
					device: event.displayName,
					name: event.name,
					value: event.value,
					desc: getEventDesc(event.descriptionText)
				]
			}
		}
	}

	return events.sort { it.time }
}

private getFormattedLocalTime(utcTime) {	
	def localTZ = TimeZone.getTimeZone(location.timeZone.ID)
	def localDate = new Date(utcTime + localTZ.getOffset(utcTime))	
	return localDate.format("MM/dd/yyyy HH:mm:ss")
}

private getEventDesc(desc) {
	if (settings?.logDesc && !desc.contains("device.displayName")) {
		return desc
	}
	else {
		return ""
	}
}

private getDeviceAllowedAttrs(deviceName) {
	def deviceAllowedAttrs = []
		
	settings?.allowedAttributes?.each { attr ->
		def attrExcludedDevices = settings."${attr}Exclusions"
		
		if (!attrExcludedDevices.find { it.toLowerCase() == deviceName.toLowerCase() }) {
			deviceAllowedAttrs << "${attr}"
		}			
	}
	return deviceAllowedAttrs
}

private getSupportedAttributes() {
	def supportedAttributes = []
	def devices = getSelectedDevices()
	
	if (devices) {
		getAllAttributes().each { attr ->
			if (devices?.find { it.hasAttribute("${attr}") }) {
				supportedAttributes << "${attr}"
			}
		}
	}
	
	return supportedAttributes.unique().sort()
}

private getAllAttributes() {
	def attributes = []	
	getCapabilities().each { cap ->
		if (cap.attr) {
			if (cap.attr instanceof Collection) {
				cap.attr.each { attr ->
					attributes << "${attr}"
				}
			}
			else {
				attributes << "${cap.attr}"				
			}
		}
	}	
	return attributes
}

private getSelectedDeviceNames() {
	return getSelectedDevices()?.collect { it.displayName}?.sort()
}

private getSelectedDevices() {
	def devices = []
	getCapabilities().each {
		if (settings?."${it.cap}Pref") {
			devices << settings?."${it.cap}Pref"
		}
	}	
	return devices?.flatten()?.unique()
}

private getCapabilities() {
	[
		[title: "Acceleration Sensors", cap: "accelerationSensor", attr: "acceleration"],
		[title: "Actuators", cap: "actuator"],
		[title: "Alarms", cap: "alarm", attr: "alarm"],
		[title: "Batteries", cap: "battery", attr: "battery"],
		[title: "Beacons", cap: "beacon", attr: "presence"],
		[title: "Bulbs", cap: "bulb", attr: "switch"],
		[title: "Buttons", cap: "button", attr: "button"],
		[title: "Carbon Dioxide Measurement Sensors", cap: "carbonDioxideMeasurement", attr: "carbonDioxide"],
		[title: "Carbon Monoxide Detectors", cap: "carbonMonoxideDetector", attr: "carbonMonoxide"],
		[title: "Contact Sensors", cap: "contactSensor", attr: "contact"],
		[title: "Doors", cap: "doorControl", attr: "door"],
		[title: "Energy Meters", cap: "energyMeter", attr: "energy"],
		[title: "Garage Doors", cap: "garageDoorControl", attr: "door"],
		[title: "Illuminance Measurement Sensors", cap: "illuminanceMeasurement", attr: "illuminance"],
		[title: "Lights", cap: "light", attr: "switch"],
		[title: "Locks", cap: "lock", attr: "lock"],
		[title: "Motion Sensors", cap: "motionSensor", attr: "motion"],
		[title: "Outlets", cap: "outlet", attr: "switch"],
		[title: "pH Measurement Sensors", cap: "phMeasurement", attr: "ph"],
		[title: "Power Meters", cap: "powerMeter", attr: "power"],
		[title: "Power Sources", cap: "powerSource", attr: "powerSource"],
		[title: "Presence Sensors", cap: "presenceSensor", attr: "presence"],
		[title: "Relative Humidity Measurement Sensors", cap: "relativeHumidityMeasurement", attr: "humidity"],
		[title: "Relay Switches", cap: "relaySwitch", attr: "switch"],
		[title: "Sensors", cap: "sensor"],
		[title: "Shock Sensors", cap: "shockSensor", attr: "shock"],
		[title: "Sleep Sensors", cap: "sleepSensor", attr: "sleeping"],
		[title: "Smoke Detectors", cap: "smokeDetector", attr: "smoke"],
		[title: "Sound Pressure Level Sensors", cap: "soundPressureLevel", attr: "soundPressureLevel"],
		[title: "Sound Sensors", cap: "soundSensor", attr: "sound"],
		[title: "Speech Recognition Sensors", cap: "speechRecognition", attr: "phraseSpoken"],
		[title: "Switches", cap: "switch", attr: "switch"],
		[title: "Switch Level Sensors", cap: "switchLevel", attr: "level"],
		[title: "Tamper Alert Sensors", cap: "tamperAlert", attr: "tamper"],
		[title: "Temperature Measurement Sensors", cap: "temperatureMeasurement", attr: "temperature"],
		[title: "Thermostats", cap: "thermostat", attr: ["coolingSetpoint", "heatingSetpoint", "temperature", "thermostatFanMode", "thermostatMode", "thermostatOperatingStatethermostatSetpoint"]],
		[title: "Three Axis Sensors", cap: "threeAxis", attr: "threeAxis"],
		[title: "Touch Sensors", cap: "touchSensor", attr: "touch"],
		[title: "Ultraviolet Index Sensors", cap: "ultravioletIndex", attr: "ultravioletIndex"],
		[title: "Valves", cap: "valve", attr: "valve"],
		[title: "Voltage Measurement Sensors", cap: "voltageMeasurement", attr: "voltage"],
		[title: "Water Sensors", cap: "waterSensor", attr: "water"],
		[title: "Window Shades", cap: "windowShade", attr: "windowShade"]
	]
}

long safeToLong(val, defaultVal=0) {
	try {
		if (val && (val instanceof Long || "${val}".isLong())) {
			return "$val".toLong()
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		return defaultVal
	}
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}