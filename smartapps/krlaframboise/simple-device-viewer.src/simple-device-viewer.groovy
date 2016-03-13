/**
 *  Simple Device Viewer v 1.0
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
	page(name:"setupPage", uninstall:true)
}

def mainPage() {
	dynamicPage(name:"mainPage") {				
		section() {			
			def knownCaps = getKnownCapabilities()	
			getAllDeviceCapabilities().each {
				if (it in knownCaps) {
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
		}
	}
}

def capabilityPage(params) {
	dynamicPage(name:"capabilityPage") {
		section() {
			def titles = getDevicesByCapability(params.capabilityName)?.collect {
				getDeviceCapabilityTitle(it, params.capabilityName) 
			}
			titles.unique().each { paragraph "$it" }
		}
	}
}

def getCapabilityPageLink(cap) {	
	def linkName = cap.toString().replace(" ", "")	
	return href(
		name: "${linkName}Link", 
		title: getCapabilityLinkTitle(cap),
		description: "",
		page: "capabilityPage", 
		params: [capabilityName: cap]
	)	
}

def getCapabilityLinkTitle(cap) {
	def pluralCap 
	switch (cap) {
		case "Switch":
			pluralCap = "Switches"
			break
		case "Battery":
			pluralCap = "Batteries"
			break
		default:
			pluralCap = "${cap}s"
	}
	return "View $pluralCap"
}

def getDevicesByCapability(cap) {	
	return getAllDevices()
		.findAll { it.hasCapability(cap) }
		.sort() { it.displayName.toLowerCase() }		
}

def getDeviceCapabilityTitle(device, cap) {
	def status = ""
	
	switch (cap) {
		case "Battery":
			status = addPadding("${device.currentBattery}%", 4, "")
			break
		case "Contact Sensor":
			status = addPadding(device.currentContact, 6, "open")
			break
		case "Motion Sensor":
			status = addPadding(device.currentMotion, 9, "motion")			
			break
		case "Switch":
			status = addPadding(device.currentSwitch, 3, "on")
			break
		case "Presence Sensor":
			status = addPadding(device.currentPresence, 11, "present")
			break
		case "Water Sensor":
			status = addPadding(device.currentWater, 3, "wet")
			break
		default:
			status = "???"
	}
	return "| ${status?.toUpperCase()} | - ${device.displayName}"
}

private addPadding(text, length, activeText) {
	if (!text || text.contains("null")) {
		text = ""
	}
	(text == activeText ? "*" : "").concat(text)
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
}

def getAllDeviceCapabilities() {
	def allCapabilities = getAllDevices()?.collect { it.capabilities }.flatten()
	return allCapabilities.collect { it.toString() }.unique().sort()
}

def getAllDevices() {
	return settings.collect {k, device -> device}.flatten().unique()
}

def getKnownCapabilities() {
	return [
		"Battery",
		"Switch", 
		"Presence Sensor", 
		"Motion Sensor",
		"Contact Sensor",
		"Water Sensor"
	]
}