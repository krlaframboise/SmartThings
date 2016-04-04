/**
 *  Blink Wireless Camera v 1.0
 *  (https://community.smartthings.com/t/???)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (4/3/2016)
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
metadata {
  definition (name: "Blink Wireless Camera", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Sensor"
    capability "Switch"
    capability "Refresh"
    capability "Polling"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Motion Sensor"
		capability "Image Capture"
		
		attribute "homescreenImage", "String"
		attribute "status", "enum", ["armed", "arming", "disabled", "disarmed", "disarming"]
		attribute "systemStatus", "enum", ["armed", "disarmed"]
		attribute "description", "String"
		attribute "syncModuleSignal", "number"
		attribute "wifiSignal", "number"
		attribute "activeEventNumber", "number"
		attribute "activeEventDesc", "String"
		
		command "toggleArmed"
		command "displayEventImage"
		command "nextEvent"
		command "previousEvent"
  }

	preferences {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"mainStatus", type: "generic", width: 6, height: 4){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "disarmed", label:'${name}', 
					action: "", 
					icon:"st.camera.dropcam", 
					backgroundColor: "#99c2ff"
				attributeState "disabled", label:'${name}', 
					action: "", 
					icon:"st.camera.dropcam", 
					backgroundColor: "#ffffff"
				attributeState "armed", label:'${name}', 
					action: "", 
					icon:"st.camera.dropcam", 
					backgroundColor:"#ff9999"
			}
		}		
		carouselTile("cameraDetails", "device.image", width: 6, height: 4, decoration: "flat") { 
		}
		valueTile("status", "device.status", width: 2, height: 2) {
			state "", label: '',
				action:"toggleArmed",
				icon:"",
				defaultState: true
			state "disarming", label:'Disarming\nCamera',
				action:"switch.off",
				icon:""
			state "disarmed", label:'Camera\nDisarmed',
				action:"switch.on",
				icon:"",
				backgroundColor:"#99c2ff"			
			state "armed", label:'Camera\nArmed', 
				action:"switch.off", 
				icon:"", 
				backgroundColor:"#ff9999"
			state "arming", label:'Arming\nCamera', 
				action:"switch.off",
				icon:""
			state "disabled", label:'Camera\nDisabled', 
				action:"toggleArmed", 
				icon:""
    }		
		valueTile("systemStatus", "device.systemStatus", width: 2, height: 2) {      
			state "disarmed", label:'System\nDisarmed', 
				action: "switch.on", 
				icon: "", 
				backgroundColor: "#99c2ff"
			state "armed", label:'System\nArmed', 
				action: "switch.off", 
				icon: "", 
				backgroundColor: "#ff9999"
    }
		valueTile("temperature", "device.temperature", width: 1, height: 1, decoration: "flat") {
			state("temperature", label:'${currentValue}°'
			)
		}
		valueTile("motion", "device.motion",  width: 1, height: 1) {
			state "inactive", label:'', 
				icon: "st.motion.motion.inactive"
			state "active", label:'', 
				icon: "st.motion.motion.inactive", 
				backgroundColor:"#99c2ff"
		}
		valueTile("battery", "device.battery",  width: 2, height: 1, decoration: "flat") {
			state "battery", label:'BATTERY\n${currentValue}%', 
				unit:"", 
				icon: ""
		}
		valueTile("syncModuleSignal", "device.syncModuleSignal",  width: 1, height: 1, decoration: "flat") {
			state "syncModuleSignal", label:'SYNC\n${currentValue}%'
		}
		valueTile("wifiSignal", "device.wifiSignal",  width: 1, height: 1, decoration: "flat") {
			state "wifiSignal", label:'WIFI\n${currentValue}%'
		}
    standardTile("refresh", "generic", width: 2, height: 2) {
      state "default", label:'', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh", 
				backgroundColor:"#ffffff"
    }		
		valueTile("viewEventImage", "device.activeEventNumber", width: 2, height: 2) {
      state "default", label:'Load\nImage', 
				action:"displayEventImage", 
				icon:""
    }
		valueTile("eventDesc", "device.activeEventDesc", width: 3, height: 2, decoration: "flat") {
      state "default", label:'${currentValue}', 
				action:"", 
				icon:""
    }
		valueTile("movePrevEvent", "generic", width: 1, height: 1) {
      state "default", label:'', 
				action:"previousEvent", 
				icon:"st.thermostat.thermostat-up"
    }
		valueTile("moveNextEvent", "generic", width: 1, height: 1) {
      state "default", label:'', 
				action:"nextEvent", 
				icon:"st.thermostat.thermostat-down"
    }		
		valueTile("desc", "device.description",  width: 6, height: 2, decoration: "flat") {
			state "description", label:'${currentValue}', 
				backgroundColor: "#ffffff"
		}

    main "mainStatus"
    details([
      "cameraDetails",
			"temperature",
			"motion",			
			"battery",
			"syncModuleSignal",
			"wifiSignal",
			"status",
			"systemStatus",
			"refresh",
			"viewEventImage",
			"eventDesc",
			"movePrevEvent",
			"moveNextEvent",
			"desc"
    ])
  }
}

def updated() {
	poll()	
}

private toggleArmed() {
	if (device.currentValue("systemStatus") == "armed") {
		off()
	}
	else {
		on()
	}
}
	
def on() {
	logDebug "Arming"
	generateEvent(getStatusEventData("arming"))
  parent.arm()
}

def off() {
	logDebug "Disarming"
	generateEvent(getStatusEventData("disarming"))
	parent.disarm()
}

def poll() {
	logDebug "${device.displayName} Polling"
	refresh()
}

def refresh() {
	parent.refreshChildren()	
}

def refresh(camera) {
	logDebug "Refreshing Camera"
	generateEvent(getStatusEventData(camera.status))
	generateEvent(getSystemStatusEventData(camera.systemArmed))
	generateEvent(getTemperatureEventData(camera.temperature))
	generateEvent(getBatteryEventData(camera.battery))
	generateEvent(getSignalEventData("syncModuleSignal",camera.syncModuleSignal))
	generateEvent(getSignalEventData("wifiSignal",camera.wifiSignal))	
	generateEvent(getSwitchEventData(camera.status))	
	generateEvent(getDescriptionEventData(camera))	
	refreshEvents(camera.events)
	refreshHomescreenImage(camera.thumbnailUrl)	
	runIn(60, poll)
}

private getTemperatureEventData(temp) {
	return [
		name: "temperature",
		value: safeToInteger(temp),
		unit: "°${location.temperatureScale}"
	]
}

private getBatteryEventData(battery) {
	def batteryLevel = battery ? safeToInteger((battery/3)*100) : 0	
	return [
		name: "battery",
		value: batteryLevel,
		unit: "%"
	]	
}

private getSignalEventData(name, val) {
	def signal = val ? safeToInteger((val/5)*100) : 0	
	return [
		name: "$name",
		value: signal,
		unit: "%"
	]	
}

private getSwitchEventData(status) {
	return [
		name: "switch",
		value: (status == "armed") ? "on" : "off",
		displayed: false
	]
}

private getStatusEventData(status) {
	return [
		name: "status",
		value: status,
		displayed: (status in ["armed","disarmed"]) ? true : false
	]
}

private getSystemStatusEventData(armed) {
	return [
		name: "systemStatus",
		value: armed ? "armed" : "disarmed",
		displayed: false
	]
}

private getDescriptionEventData(camera) {
	return [
		name: "description",
		value: getDescription(camera)
	]
}

private getDescription(camera) {
	return "${camera.cameraName}\n" +
		"DNI: ${camera.dni}\n" +
		"Updated: ${camera.updatedAt}"
}

def generateEvent(eventData) {
	try {
		if ("${device.currentValue(eventData.name)}" != "${eventData.value}") {
			sendEvent(
				name: eventData.name, 
				value: eventData.value, 
				unit: eventData.unit ? eventData.unit : "",
				displayed: eventData.displayed ? true : false,
				isStateChange: true
			)
		}			
	}
	catch (e) {
		log.error "Unable to generate event ($eventData): $e"
	}		
}

def refreshEvents(events) {
	def newEvent = false	
	if (!state.cameraEvents) {
		state.cameraEvents = []
	}
	
	events.take(15).reverse().each { 	
		if (eventIsNew(it.eventId)) {
			newEvent = true
			if (state.cameraEvents.size() == 15) {
				state.cameraEvents.pop()
			}
			state.cameraEvents.add(0, it)
		}
	}
	
	if (newEvent) {
		updateCameraEventAttributes(1)
		if (device.currentValue("systemStatus") == "armed") {
			logDebug "Setting Motion Active"
			generateEvent([
				name: "motion",
				value: "active",
				displayed: true
			])
			runIn(5, setMotionInactive)
		}
	}
}

def eventIsNew(eventId) {
	def found = state.cameraEvents.find { it.eventId == eventId }
	return found ? false : true
}

def setMotionInactive() {
	logDebug "Setting Motion Inactive"
	generateEvent([
		name: "motion",
		value: "inactive",
		displayed: true
	])
}

def refreshHomescreenImage(url) {
	if (url != state.homescreenImageSource) {
		logDebug "Refreshing homescreen image: $url"
		loadImage(url, getHomescreenImageName())
		state.homescreenImageSource = url		
	}	
}

def nextEvent() {
	def nextIndex = safeToInteger(device.currentValue("activeEventNumber"))
	if (nextIndex < safeToInteger(state.cameraEvents?.size())) {
		updateCameraEventAttributes(nextIndex + 1)
	}
}

def previousEvent() {
	def prevIndex = safeToInteger(device.currentValue("activeEventNumber")) - 2
	if ((prevIndex) >= 0) {
		updateCameraEventAttributes(prevIndex + 1)
	}
}

def updateCameraEventAttributes(eventNumber) {
	generateEvent([
		name: "activeEventNumber",
		value: eventNumber,
		displayed: false
	])
	generateEvent([
		name: "activeEventDesc",
		value: state.cameraEvents[eventNumber-1].eventTime,
		displayed: false
	])
}

def displayEventImage() {	
	def activeIndex = safeToInteger(device.currentValue("activeEventNumber"))-1
			
	if (state.cameraEvents.size() > activeIndex) {
		def url = state.cameraEvents[activeIndex]?.thumbnailUrl
		loadImage(url, getEventImageName())			
	}
}

private loadImage(sourceUrl, newImageName) {
	if (sourceUrl && !parent.imagesAreDisabled()) {				
		def imageBytes = parent.getThumbnailImage(sourceUrl)
		if(imageBytes) {
			storeImage(newImageName, imageBytes)
			generateEvent([
				name: "image",
				value: "smartthings-smartsense-camera: $newImageName",
				displayed: true
			])
		}
	}
	else {
		log.info "The image feature has been disabled in the Blink System Connector SmartApp."
	}
}

private getHomescreenImageName() {
	def dni = device.deviceNetworkId
	return "${dni}homescreen.jpg".replaceAll("-", "")
}

private getEventImageName() {
	def dni = device.deviceNetworkId
	return "${dni}event.jpg".replaceAll("-", "")
}

int safeToInteger(val) {
	try {
		if ("$val".isNumber()) {
			return "$val".toDouble()?.trunc()?.toInteger()
		}
		else {
			return 0
		}		
	}
	catch (e) {
		return 0
	}
}

private logDebug(msg) {
	if (settings.debugOutput) {
		log.debug "$msg"
	}
}