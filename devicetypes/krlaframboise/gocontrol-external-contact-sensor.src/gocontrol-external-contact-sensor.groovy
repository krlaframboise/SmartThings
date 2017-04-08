/**
 *  GoControl External Contact Sensor v1.0
 *  (Child Device Handler for WADWAZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    
 *  Changelog:
 *
 *    1.0 (04/08/2017)
 *      -  Initial Release
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
metadata {
	definition (
		name: "GoControl External Contact Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Water Sensor"
		capability "Motion Sensor"
		capability "Smoke Detector"
		
		attribute "primaryStatus", "string"
		
		command "open"
		command "close"
	}

	simulator {	}
	
	preferences {
		input "primaryStatusAttr", "enum",
			title: "Primary Status Attribute:",
			defaultValue: primaryStatusAttrSetting,
			required: false,
			options: primaryStatusOptions
		input "contactOpen", "enum",
			title: "Contact Open Event:",
			defaultValue: contactOpenSetting,
			required: false,
			options: eventOptions
		input "contactClosed", "enum",
			title: "Contact Closed Event:",
			defaultValue: contactClosedSetting,
			required: false,
			options: eventOptions
		input "motionActive", "enum",
			title: "Motion Active Event:",
			defaultValue: motionActiveSetting,
			required: false,
			options: eventOptions
		input "motionInactive", "enum",
			title: "Motion Inactive Event:",
			defaultValue: motionInactiveSetting,
			required: false,
			options: eventOptions
		input "smokeDetected", "enum",
			title: "Smoke Detected Event:",
			defaultValue: smokeDetectedSetting,
			required: false,
			options: eventOptions
		input "smokeClear", "enum",
			title: "Smoke Clear Event:",
			defaultValue: smokeClearSetting,
			required: false,
			options: eventOptions		
		input "waterWet", "enum",
			title: "Water Wet Event:",
			defaultValue: waterWetSetting,
			required: false,
			options: eventOptions
		input "waterDry", "enum",
			title: "Water Dry Event:",
			defaultValue: waterDrySetting,
			required: false,
			options: eventOptions
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: debugOutputSetting, 
			displayDuringSetup: true, 
			required: false			
	}
	
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'Closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'Open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
				attributeState "garage-closed", 
					label:'Closed', 
					icon:"st.doors.garage.garage-closed", 
					backgroundColor:"#79b821"
				attributeState "garage-open", 
					label:'Open', 
					icon:"st.doors.garage.garage-open", 
					backgroundColor:"#ffa81e"				
				attributeState "inactive", 
					label:'No Motion', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'Motion', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
				attributeState "dry", 
					label:"Dry", 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:"Wet", 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
				attributeState "clear", 
					label:'Clear', 
					icon:"st.alarm.smoke.clear", 
					backgroundColor:"#ffffff"
				attributeState "detected", 
					label:'Detected', 
					icon:"st.alarm.smoke.smoke", 
					backgroundColor:"#53a7c0"
			}			
		}	
							
		main("mainTile")
		details(["mainTile"])
	}
}

def open() {
	handleContactEvent("open")
}

def close() {
	handleContactEvent("closed")
}

def parse(String description) {		
	return []
}

def updated() {	
log.trace "updated"
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time

		if (!state.isConfigured) {		
			configure()
		}
		
		handleContactEvent(state.sensorVal, false)	
	}
}

def configure() {	
	logTrace "configure()"
	
	state.sensorVal = "closed"
	
	eventSettings.each {	
		if (!device.currentValue("${it.attr}")) {
			def val = (it.aEvent == "default" ? it.aVal : it.iVal)
			sendEvent(name: "${it.attr}", value: "$val", isStateChange: true, displayed: false)
		}
	}
	state.isConfigured = true	
}

private void handleContactEvent(sensorVal, displayed=null) {
	def eventMaps = []
	
	state.sensorVal = "${sensorVal}"
	
	eventSettings.each {
		def eventVal = determineEventVal(it, sensorVal)
		if (eventVal) {
			
			eventMaps += createEventMap(it.attr, eventVal, displayed)
			
			if (primaryStatusAttrSetting.startsWith(it.attr)) {				
				eventMaps += createEventMap("primaryStatus", (
				primaryStatusAttrSetting.contains("garage") ? "garage-${eventVal}" : eventVal), false)
			}
		}		
	}
	eventMaps?.each { 
		sendEvent(it) 
	}
}

private determineEventVal(eventSettings, sensorVal) {
	if (eventSettingMatchesEventVal(eventSettings.aEvent, sensorVal)) {
		return eventSettings.aVal
	}
	else if (eventSettingMatchesEventVal(eventSettings.iEvent, sensorVal)) {
		return eventSettings.iVal
	}
	else {
		return null
	}
}

private eventSettingMatchesEventVal(eventSetting, sensorVal) {
	return (eventSetting == "default" || eventSetting?.endsWith(sensorVal))	
}

private createEventMap(eventName, newVal, displayed=null) {	
	def isNew = device.currentValue(eventName) != newVal
	def desc = "${eventName.capitalize()} is ${newVal}"
	
	def result = []
	if (isNew) {
		logDebug "${desc}"
		result << [
			name: eventName, 
			value: newVal, 
			displayed: (displayed != null) ? displayed : isNew
		]
	}
	else {
		logTrace "Ignored: ${desc}"
	}
	return result
}

// Settings
private getEventSettings() {
	return [
		[attr: "contact", aEvent: contactOpenSetting, aVal: "open", iEvent: contactClosedSetting, iVal: "closed"],
		[attr: "motion", aEvent: motionActiveSetting, aVal: "active", iEvent: motionInactiveSetting, iVal: "inactive"],
		[attr: "water", aEvent: waterWetSetting, aVal: "wet", iEvent: waterDrySetting, iVal: "dry"],
		[attr: "smoke", aEvent: smokeDetectedSetting, aVal: "detected", iEvent: smokeClearSetting, iVal: "clear"]
	]
}

private getPrimaryStatusAttrSetting() {
	return settings?.primaryStatusAttr ?: "contact"
}

private getContactOpenSetting() {
	return settings?.contactOpen ?: "contact.open"
}

private getContactClosedSetting() {
	return settings?.contactClosed ?: "default"
}

private getMotionActiveSetting() {
	return settings?.motionActive ?: "none"
}

private getMotionInactiveSetting() {
	return settings?.motionInactive ?: "default"
}

private getWaterWetSetting() {
	return settings?.waterWet ?: "none"
}

private getWaterDrySetting() {
	return settings?.waterDry ?: "default"
}

private getSmokeDetectedSetting() {
	return settings?.smokeDetected ?: "none"
}

private getSmokeClearSetting() {
	return settings?.smokeClear ?: "default"
}

private getDebugOutputSetting() {
	return settings?.debugOutput || settings?.debugOutput == null
}

// Options 
private getEventOptions() {
	return [
		"default",
		"none", 
		"contact.open",
		"contact.closed"
	]
}

private getPrimaryStatusOptions() {
	return [
		"contact",
		"contact-garage", 
		"motion",
		"smoke", 
		"water"
	]
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}
