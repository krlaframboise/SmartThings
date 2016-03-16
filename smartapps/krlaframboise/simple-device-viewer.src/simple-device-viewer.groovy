/**
 *  Simple Device Viewer v 1.0.2
 *
 *  Copyright 2016 Kevin LaFramboise
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
    description: "Provides information about the state of the specified devices",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home5-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home5-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home5-icn@3x.png")

 preferences {
	page(name:"mainPage", uninstall:true, install:true)
  page(name:"capabilityPage")
	page(name:"setupPage")
}

def mainPage() {
	if (!state.capabilitySettings) {
		storeCapabilitySettings()
	}
	dynamicPage(name:"mainPage") {				
		section() {	
			getCapabilityPageLink(null)			
			state.capabilitySettings.each {
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
		title: cap ? "${getPluralName(cap)}" : "All Devices",
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
		.findAll { it.hasCapability(name) }
		.sort() { it.displayName.toLowerCase() }		
}

def getDeviceAllCapabilitiesTitle(device) {
	def allStatuses = null
	state.capabilitySettings.each {
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

def getDeviceStatusTitle(device, status) {
	return "| ${status?.toUpperCase()} | - ${device.displayName}"
}

def getDeviceCapabilityStatus(device, cap) {
	def status = device.currentValue(getAttribute(cap)).toString()
	def activeState = cap.activeState ? cap.activeState : cap.name.toLowerCase()		
	
	if (status == activeState) {
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

def getAttribute(capabilitySetting) {
	capabilitySetting.attributeName ? capabilitySetting.attributeName : capabilitySetting.name.toLowerCase()
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
	return settings.collect {k, device -> device}.flatten().unique()
}