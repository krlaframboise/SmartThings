/**
 *  Simple Device Viewer v 1.3
 *  (https://community.smartthings.com/t/release-simple-device-viewer/42481/15?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.3 (03/19/2016)
 *      - Added "Setup Thresholds" section that allows you
 *        to specify battery low level, temp high/low, and
 *        last event time.
 *      - Added threshold icons and value sorting for screens
 *        Temp, Battery, and Last Events.
 *      - Added "Other Settings" section that allows you to
 *        enable/disable icons and value sorting.
 *
 *    1.2 (03/17/2016)
 *      - Added page headings
 *      - Added ability to toggle switches from Switches screen.
 *      - Added "Turn Off All Switches" link to Switches page.
 *
 *    1.1 (03/17/2016)
 *      - Initial Release
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
definition(
    name: "Simple Device Viewer",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Provides information about the state of the specified devices.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home5-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home5-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home5-icn@3x.png")

 preferences {
	page(name:"mainPage", uninstall:true, install:true)
  page(name:"capabilityPage")
	page(name:"lastEventPage")
	page(name:"toggleSwitchPage")
	page(name:"devicesPage")
	page(name:"thresholdsPage")
	//page(name:"notificationsPage")
	page(name:"otherSettingsPage")
}

def mainPage() {
	if (!state.capabilitySettings) {
		storeCapabilitySettings()
	}
	dynamicPage(name:"mainPage") {				
		section() {	
			if (getAllDevices().size() != 0) {
				state.lastCapabilitySetting = null
				href(
					name: "lastEventLink", 
					title: "All Devices - Last Event",
					description: "",
					page: "lastEventPage", 
					params: []
				)
				getCapabilityPageLink(null)			
			}		
			getSelectedCapabilitySettings().each {
				if (devicesHaveCapability(it.name)) {
					getCapabilityPageLink(it)
				}
			}
		}
		section("Settings") {			
			href(
				name: "devicesLink", 
				title: "Choose Devices & Capabilities",
				description: "",
				page: "devicesPage", 
				params: []
			)
			href(
				name: "thresholdsLink", 
				title: "Setup Thresholds",
				description: "",
				page: "thresholdsPage", 
				params: []
			)
			//href(
			//	name: "notificationsLink", 
			//	title: "Setup Notifications",
			//	description: "",
			//	page: "notificationsPage", 
			//	params: []
			//)
			href(
				name: "otherSettingsLink", 
				title: "Other Settings",
				description: "",
				page: "otherSettingsPage", 
				params: []
			)
			paragraph ""
		}
	}
}


def devicesPage() {
	dynamicPage(name:"devicesPage") {		
		section ("Choose Devices") {
			paragraph "Select all the devices that you want to be able to view in this application.\n\nYou can use any of the fields below to select a device, but you only need to select each device once.  Duplicates are automatically removed so selecting a device more than once won't hurt anything."
			input "actuators", "capability.actuator",
				title: "Which Actuators?",
				multiple: true,
				required: false
			input "sensors", "capability.sensor",
				title: "Which Sensors?",
				multiple: true,
				required: false			
			state.capabilitySettings.each {
				input "${getPrefName(it)}Devices",
					"capability.${getPrefName(it)}",
					title: "Which ${getPluralName(it)}?",
					multiple: true,
					required: false
			}			
		}
		section ("Display Options") {
			paragraph "All the capabilities supported by the selected devices are shown on the main screen by default, but this field allows you to limit the list to specific capabilities." 
			input "selectedCapabilities", "enum",
				title: "Display Which Capabilities?",
				multiple: true,
				options: getCapabilityNames(),
				required: false
		}
	}
}

def thresholdsPage() {
	dynamicPage(name:"thresholdsPage") {		
		section () {
			paragraph "The thresholds specified on this page are used to determine icons in the SmartApp and when to send notifications."			
		}
		section("Battery Thresholds") {
			input "lowBatteryThreshold", "number",
				title: "Enter Low Battery %:",
				multiple: false,
				defaultValue: 25			
		}
		section("Temperature Thresholds") {
			input "lowTempThreshold", "number",
				title: "Enter Low Temperature:",
				required: false,
				defaultValue: 63
			input "highTempThreshold", "number",
				title: "Enter High Temperature:",
				required: false,
				defaultValue: 73			
		}
		section("Last Event Thresholds") {
			input "lastEventThreshold", "number",
				title: "Last event should be within:",
				required: false,
				defaultValue: 7
			input "lastEventThresholdUnit", "enum",
				title: "Choose unit of time:",
				required: false,
				defaultValue: "days",
				options: ["minutes", "seconds", "hours", "days"]			
		}
	}
}

def notificationsPage() {
	dynamicPage(name:"notificationsPage") {
		section ("Setup Notifications") {
		
		}
		section ("Notification Types") {
			paragraph "When enabled, notifications will be sent when the device value goes above or below the threshold specified on the Set Thresholds screen."
			input "batteryNotificationsEnabled", "bool",
				title: "Send Battery Notifications?",
				defaultValue: false,
				required: false
			input "temperatureNotificationsEnabled", "bool",
				title: "Send Temperature Notifications?",
				defaultValue: false,
				required: false
			input "lastEventNotificationsEnabled", "bool",
				title: "Send Last Event notification?",
				defaultValue: false,
				required: false
		}
	}
}

def otherSettingsPage() {
	dynamicPage(name:"otherSettingsPage") {		
		section ("Other Settings") {
			label(name: "label",
				title: "Assign a name",
				required: false)
			input "iconsEnabled", "bool",
				title: "Display Device State Icons?",
				defaultValue: true,
				required: false
			input "batterySortByValue", "bool",
				title: "Sort by Battery Value?",
				defaultValue: false,
				required: false
			input "tempSortByValue", "bool",
				title: "Sort by Temperature Value?",
				defaultValue: false,
				required: false
			input "lastEventSortByValue", "bool",
				title: "Sort by Last Event Value?",
				defaultValue: false,
				required: false
		}
	}
}

def lastEventPage() {
	dynamicPage(name:"lastEventPage") {		
		section ("Time Since Last Event") {
			getParagraphs(getAllDeviceLastEventListItems())			
		}		
	}
}

def toggleSwitchPage(params) {
	dynamicPage(name:"toggleSwitchPage") {		
		section () {
			if (params.deviceId) {
				def device = params.deviceId ? getAllDevices().find { it.id == params.deviceId } : null
				toggleSwitch(device, device?.currentSwitch == "off" ? "on" : "off")
			}
			else {
				getDevicesByCapability("Switch").each {
					toggleSwitch(it, "off")
				}
			}			
		}		
	}
}

def toggleSwitch(device, newState) {
	if (device) {	
		if (newState == "on") {
			device.on()
		}
		else {
			device.off()
		}
		paragraph "Turned ${device.displayName} ${newState.toUpperCase()}"
	}
}

def capabilityPage(params) {
	dynamicPage(name:"capabilityPage") {	
		def capSetting = params.capabilitySetting ? params.capabilitySetting : state.lastCapabilitySetting
		
		if (capSetting) {
			state.lastCapabilitySetting = capSetting
			section("${getPluralName(capSetting)}") {
				if (capSetting.name == "Switch") {
					href(
						name: "allOffSwitchLink", 
						title: "Turn Off All Switches",
						description: "",
						page: "toggleSwitchPage"
					)			
					getSwitchToggleLinks()
				}
				else {
					getParagraphs(getDeviceCapabilityListItems(capSetting))
				}
			}
		}
		else {
			section("All Selected Capabilities") {
				getParagraphs(getAllDevices().collect { 
					getDeviceAllCapabilitiesListItem(it) 
				})
			}
		}			
	}
}

def getParagraphs(listItems) {
	listItems.sort { it.sortValue }
	return listItems.unique().each { 
		it.image = it.image ? it.image : ""
		paragraph image: "${it.image}",	"${it.title}"
	}
}

def getCapabilityPageLink(cap) {		
	return href(
		name: cap ? "${getPrefName(cap)}Link" : "allDevicesLink", 
		title: cap ? "${getPluralName(cap)}" : "All Devices - States",
		description: "",
		page: "capabilityPage", 
		params: [capabilitySetting: cap]
	)	
}

def devicesHaveCapability(name) {	
	return getAllDevices().find { it.hasCapability(name) } ? true : false
}

def getDevicesByCapability(name) {
	return getAllDevices()
		.findAll { it.hasCapability(name.toString()) }
		.sort() { it.displayName.toLowerCase() }		
}

def getDeviceAllCapabilitiesListItem(device) {
	def listItem = [
		sortValue: device.displayName
	]	
	getSelectedCapabilitySettings().each {
		if (device.hasCapability(it.name)) {
			listItem.status = (listItem.status ? "${listItem.status}, " : "").concat(getDeviceCapabilityStatusItem(device, it).status)
		}
	}
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	return listItem
}

def getDeviceCapabilityListItems(cap) {
	getDevicesByCapability(cap.name)?.collect {
		def listItem = getDeviceCapabilityStatusItem(it, cap)
		listItem.title = getDeviceStatusTitle(it, listItem.status)
		listItem
	}
}

def getSwitchToggleLinks() {
	def cap = state.capabilitySettings.find { it.name == "Switch" }
	getDevicesByCapability("Switch")?.collect {
		href(
			name: "switchLink${it.id}", 
			title: "${getDeviceStatusTitle(it, getDeviceCapabilityStatusItem(it, cap).status)}",
			description: "",
			page: "toggleSwitchPage", 
			params: [deviceId: it.id]
		)			
	}
}

def getAllDeviceLastEventListItems() {
	getAllDevices().collect {
		getDeviceLastEventListItem(it)		
	}
}

def getDeviceLastEventListItem(device) {
	def now = new Date().time
	def lastEventTime = getDeviceLastEventTime(device)
	
	def listItem = [
		value: lastEventTime ? now - lastEventTime : Long.MAX_VALUE,
		status: lastEventTime ? "${getTimeSinceLastEvent(now - lastEventTime)}" : "No Events"
	]
	
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	listItem.sortValue = lastEventSortByValue ? listItem.value : device.displayName
	listItem.image = getLastEventImage(lastEventTime)
	return listItem
}

def getDeviceLastEventTime(device) {
	def lastEventTime = device.events(max: 1)?.date?.time
	if (lastEventTime && lastEventTime.size() > 0) {
		return lastEventTime[0]
	}
}

def getTimeSinceLastEvent(ms) {
	if (ms < msSecond()) {
		return "$ms Milliseconds"
	}
	else if (ms < msMinute()) {
		return "${calculateTimeSince(ms, msSecond())} Seconds"
	}
	else if (ms < msHour()) {
		return "${calculateTimeSince(ms, msMinute())} Minutes"
	}
	else if (ms < msDay()) {
		return "${calculateTimeSince(ms, msHour())} Hours"
	}
	else {
		return "${calculateTimeSince(ms, msDay())} Days"
	}		
}

long msSecond() {
	return 1000
}

long msMinute() {
	return msSecond() * 60
}

long msHour() {
	return msMinute() * 60
}

long msDay() {
	return msHour() * 24
}

def calculateTimeSince(ms, divisor) {
	return "${((float)(ms / divisor)).round()}"
}

String getDeviceStatusTitle(device, status) {
	if (!status || status == "null") {
		status = "N/A"
	}
	return "| ${status?.toUpperCase()} | - ${device.displayName}"
}

def getDeviceCapabilityStatusItem(device, cap) {
	def item = [
		image: "",
		sortValue: device.displayName,
		value: device.currentValue(getAttributeName(cap)).toString()
	]
	item.status = item.value
	if ("${item.status}" != "null") {
	
		if (item.status == getActiveState(cap)) {
			item.status = "*${item.status}"
		}
		
		switch (cap.name) {
			case "Battery":			
				item.status = "${item.status}%"
				item.image = getBatteryImage(item.value)
				if (batterySortByValue) {
					item.sortValue = item.value ? item.value.toInteger() : 0
				}				
				break
			case "Temperature Measurement":
				item.status = "${item.status}°${location.temperatureScale}"
				item.image = getTemperatureImage(item.value)
				if (tempSortByValue) {
					item.sortValue = item.value ? item.value.toInteger() : 0
				}
				break
		}
	}
	else {
		item.status = "N/A"
	}
	return item
}

def getSelectedCapabilitySettings() {
	if (!selectedCapabilities || selectedCapabilities.size() == 0) {
		return state.capabilitySettings
	}
	else {
		return state.capabilitySettings.findAll { it.name in selectedCapabilities }		
	}
}

def getCapabilityNames() {
	state.capabilitySettings.collect { it.name }
}

String getAttributeName(capabilitySetting) {
	capabilitySetting.attributeName ? capabilitySetting.attributeName : capabilitySetting.name.toLowerCase()
}

String getActiveState(capabilitySetting) {
	capabilitySetting.activeState ? capabilitySetting.activeState : capabilitySetting.name.toLowerCase()
}

String getPrefName(capabilitySetting) {
	capabilitySetting.prefName ? capabilitySetting.prefName : capabilitySetting.name.toLowerCase()
}

String getPluralName(capabilitySetting) {
	capabilitySetting.pluralName ? capabilitySetting.pluralName : "${capabilitySetting.name}s"
}

def getAllDeviceCapabilities() {
	def allCapabilities = getAllDevices()?.collect { it.capabilities }.flatten()
	return allCapabilities.collect { it.toString() }.unique().sort()
}

def getAllDevices() {
	def values = settings.collect {k, device -> device}
	return values.findAll { isDevice(it) }.flatten().unique()
}

boolean isDevice(obj) {
	try {
		return obj?.id ? true : false
	}
	catch (e) {
		return false
	}
}

String getLastEventImage(lastEventTime) {
	if (iconsAreEnabled() && lastEventIsOld(lastEventTime)) {		
		return "https://raw.githubusercontent.com/krlaframboise/Resources/master/warning.png"
	}
}

boolean lastEventIsOld(lastEventTime) {	
	try {
		if (!lastEventTime) {
			return true
		}
		else {
			def threshold = lastEventThreshold ? lastEventThreshold : 7
			def unitMS
			switch (lastEventThresholdUnit) {
				case "seconds":
					unitMS = msSecond()
					break
				case "minutes":
					unitMS = msMinute()
					break
				case "hours":
					unitMS = msHour()
					break
				default:
					unitMS = msDay()
			}
			return ((new Date().time - (threshold * unitMS)) > lastEventTime)
		}
	}
	catch (e) {
		return true
	}
}



String getBatteryImage(batteryLevel) {
	if (iconsAreEnabled() && batteryIsLow(batteryLevel)) {
		return  "https://raw.githubusercontent.com/krlaframboise/Resources/master/low-battery.png"
	}
}

boolean batteryIsLow(batteryLevel) {
	return isBelowThreshold(batteryLevel, lowBatteryThreshold, 25)
}

String getTemperatureImage(tempVal) {	
	if (iconsAreEnabled()) {
		if (tempIsHigh(tempVal)) {
			return "https://raw.githubusercontent.com/krlaframboise/Resources/master/high-temp.png"
		}
		else if (tempIsLow(tempVal)) {
			return "https://raw.githubusercontent.com/krlaframboise/Resources/master/low-temp.png"
		}
		else {
			return "https://raw.githubusercontent.com/krlaframboise/Resources/master/normal-temp.png"
		}
	}
}

boolean tempIsHigh(val) {
	return isAboveThreshold(val, highTempThreshold, 73)
}

boolean tempIsLow(val) {
	return isBelowThreshold(val, lowTempThreshold, 63)
}

boolean iconsAreEnabled() {
	return (iconsEnabled || iconsEnabled == null)
}

boolean isAboveThreshold(val, threshold, int defaultThreshold) {
	try {
		return val.toInteger() > (!threshold ? defaultThreshold : threshold.toInteger())
	}
	catch (e) {
		return false
	}
}

boolean isBelowThreshold(val, threshold, int defaultThreshold) {
	try {
		return val.toInteger() < (threshold ? threshold.toInteger() : defaultThreshold)
	}
	catch (e) {
		return false
	}	
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	//log.debug "$settings"	
	storeCapabilitySettings()
}

void storeCapabilitySettings() {
	state.capabilitySettings = []
	
	state.capabilitySettings << [
		name: "Switch",
		pluralName: "Switches",		
		activeState: "on"
	]
	state.capabilitySettings << [
		name: "Motion Sensor", 
		prefName: "motionSensor",
		attributeName: "motion",
		activeState: "active"
	]
	state.capabilitySettings << [
		name: "Contact Sensor",
		prefName: "contactSensor",
		attributeName: "contact",
		activeState: "open"
	]
	state.capabilitySettings << [
		name: "Presence Sensor",
		prefName: "presenceSensor",
		attributeName: "presence",
		activeState: "present"
	]
	state.capabilitySettings << [
		name: "Battery",
		pluralName: "Batteries"
	]
	state.capabilitySettings << [
		name: "Water Sensor",
		prefName: "waterSensor",
		attributeName: "water",
		activeState: "wet"
	]
	state.capabilitySettings << [
		name: "Alarm",
		activeState: "off"
	]
	state.capabilitySettings << [
		name: "Lock",
		activeState: "locked"
	]
	state.capabilitySettings << [
		name: "Temperature Measurement",
		pluralName: "Temperature Sensors",
		prefName: "temperatureMeasurement",
		attributeName: "temperature"
	]
	state.capabilitySettings << [
		name: "Smoke Detector",
		prefName: "smokeDetector",
		attributeName: "smoke",
		activeState: "detected"
	]
	state.capabilitySettings << [
		name: "Carbon Monoxide Detector",
		prefName: "carbonMonoxideDetector",
		attributeName: "carbonMonoxide",
		activeState: "detected"
	]	
	state.capabilitySettings.sort { it.name }
}