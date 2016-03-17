/**
 *  Simple Device Viewer v 1.1
 *
 *	Author: 
 *					Kevin LaFramboise (krlaframboise)
 *
 *	Changelog:
 *
 *	1.1 (03/17/2016)
 *		- Initial Release
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
	page(name:"setupPage")
}

def mainPage() {
	if (!state.capabilitySettings) {
		storeCapabilitySettings()
	}
	dynamicPage(name:"mainPage") {				
		section() {	
			if (getAllDevices().size() != 0) {
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
			href(
				name: "setupLink", 
				title: "Setup",
				description: "",
				page: "setupPage", 
				params: []
			)
			paragraph ""
		}
	}
}

def setupPage() {
	dynamicPage(name:"setupPage") {		
		section ("Devices") {
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
		section ("Options") {
			input "selectedCapabilities", "enum",
				title: "Display Which Capabilities?",
				multiple: true,
				options: getCapabilityNames(),
				required: false
		}
	}
}

def lastEventPage() {
	dynamicPage(name:"lastEventPage") {		
		section ("Time Since Last Event") {
			getAllDeviceLastEventTitles().each { paragraph "$it" }
		}		
	}
}

def capabilityPage(params) {
	dynamicPage(name:"capabilityPage") {
		section() {
			def titles
			if (params.capabilitySetting) {
				titles = getDeviceCapabilityTitle(params.capabilitySetting)
			}
			else {
				titles = getAllDevices().collect {
					getDeviceAllCapabilitiesTitle(it)
				}
			}
			titles.unique().each { paragraph "$it" }
		}
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

def getDeviceAllCapabilitiesTitle(device) {
	def allStatuses = null	
	getSelectedCapabilitySettings().each {
		if (device.hasCapability(it.name)) {
			allStatuses = (allStatuses ? "$allStatuses, " : "")
				.concat(getDeviceCapabilityStatus(device, it))			
		}
	}	
	return getDeviceStatusTitle(device, allStatuses)
}

def getDeviceCapabilityTitle(cap) {
	getDevicesByCapability(cap.name)?.collect {
		def status = getDeviceCapabilityStatus(it, cap)
		getDeviceStatusTitle(it, status)
	}
}

def getAllDeviceLastEventTitles() {
	getAllDevices().collect {
		def now = new Date().time
		def lastEvent = it.events(max: 1)?.date?.time
		if (lastEvent) {
			getDeviceStatusTitle(it, "${getTimeSinceLastEvent(now - lastEvent)}")			
		}
		else {
			getDeviceStatusTitle(it, "No Events")
		}
	}
}

def getTimeSinceLastEvent(ms) {
	def sec = 1000
	def min = sec * 60
	def hr = min * 60
	def day = hr * 24
	def timeType
	
	if (ms < sec) {
		return "$ms Milliseconds"
	}
	else if (ms < min) {
		return "${getTimeSince(ms, sec)} Seconds"
	}
	else if (ms < hr) {
		return "${getTimeSince(ms, min)} Minutes"
	}
	else if (ms < day) {
		return "${getTimeSince(ms, hr)} Hours"
	}
	else {
		return "${getTimeSince(ms, day)} Days"
	}		
}

def getTimeSince(ms, divisor) {
	return "${((float)(ms / divisor)).round()}"
}

def getDeviceStatusTitle(device, status) {
	if (!status || status == "null") {
		status = "N/A"
	}
	return "| ${status?.toUpperCase()} | - ${device.displayName}"
}

def getDeviceCapabilityStatus(device, cap) {
	def status = device.currentValue(getAttribute(cap)).toString()
	if ("$status" != "null") {
	
		if (status == getActiveState(cap)) {
			status = "*$status"
		}
		
		switch (cap.name) {
			case "Battery":			
				status = "$status%"
				break
			case "Temperature Measurement":
				status = "$status°${location.temperatureScale}"
				break
		}
	}
	else {
		status = "N/A"
	}

	return status
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "$settings"	
	storeCapabilitySettings()
}

def storeCapabilitySettings() {
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

def getAttribute(capabilitySetting) {
	capabilitySetting.attributeName ? capabilitySetting.attributeName : capabilitySetting.name.toLowerCase()
}

def getActiveState(capabilitySetting) {
	capabilitySetting.activeState ? capabilitySetting.activeState : capabilitySetting.name.toLowerCase()
}

def getPrefName(capabilitySetting) {
	capabilitySetting.prefName ? capabilitySetting.prefName : capabilitySetting.name.toLowerCase()
}

def getPluralName(capabilitySetting) {
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

def isDevice(obj) {
	try {
		return obj?.id ? true : false
	}
	catch (e) {
		return false
	}
}