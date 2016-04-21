/**
 *  Blink Wireless Camera v 1.3 (alpha)
 *  (https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.3 (alpha) (4/21/2016)
 *      - This version is still buggy and incomple, I'm only
 *        releasing it because I now have more than one camera
 *        so I know that the prior versions didn't work with
 *        more than one.
 *
 *    1.2 (4/7/2016)
 *      - Added ability to take photos and enable/disable
 *        the camera.
 *      - UI Enhancements
 *
 *    1.1 (4/5/2016)
 *      - Made date fields display formatted local time.
 *      - UI Enhancements
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
		attribute "status", "enum", ["armed", "disarmed", "enabled", "disabled", "offline"]
		attribute "actionStatus", "enum", ["ready", "arming", "disabling", "disarming", "enabling", "loading", "refreshing", "taking"]
		attribute "systemStatus", "enum", ["armed", "disarmed", "offline"]
		attribute "description", "String"
		attribute "syncModuleSignal", "number"
		attribute "wifiSignal", "number"
		attribute "activeEventNumber", "number"
		attribute "activeEventDesc", "String"
		
		command "enableCamera"
		command "disableCamera"
		command "armSystem"
		command "disarmSystem"
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
		valueTile("actionStatus", "device.actionStatus", width: 2, height: 1, decoration: "flat") {
			state "ready", label: '   Ready   ',
				action:"",
				icon:""
			state "arming", label:'Arming\nSystem',
				action:"",
				icon:""
			state "disabling", label:'Disabling\nCamera', 
				action:""
			state "disarming", label:'Disarming\nSystem', 
				action:"", 
				icon:""
			state "enabling", label:'Enabling\nCamera', 
				action:"", 
				icon:""
			state "loading", label:'Loading\nPhoto',
				action:"",
				icon:""
			state "refreshing", label:'Refreshing\nCamera',
				action:"",
				icon:""
			state "taking", label:'Taking\nPhoto',
				action:"", 
				icon:""
    }
		valueTile("status", "device.status", width: 2, height: 2) {
			state "enabled", label:'Camera\nEnabled',
				action:"disableCamera",
				icon:"",
				backgroundColor:"#99c2ff"
			state "disabled", label:'Camera\nDisabled',
				action:"enableCamera",
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
    standardTile("takePhoto", "generic", width: 1, height: 1) {
      state "default", label:'', 
				action:"take", 
				icon:"st.camera.take-photo", 
				backgroundColor:"#ffffff"
    }
    standardTile("refresh", "generic", width: 1, height: 1) {
      state "default", label:'', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh", 
				backgroundColor:"#ffffff"
    }
		valueTile("eventsLabel", "generic", width: 1, height: 1, decoration: "flat") {
      state "default", label: 'Events:', 
				action:"", 
				icon:""
    }
		valueTile("loadEventImage", "device.activeEventNumber", width: 1, height: 1) {
      state "default", label:'Load', 
				action:"displayEventImage", 
				icon:""
    }
		valueTile("eventDesc", "device.activeEventDesc", width: 2, height: 1, decoration: "flat") {
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
			"temperature",
			"motion",			
			"battery",
			"syncModuleSignal",
			"wifiSignal",
      "cameraDetails",
			"status",
			"takePhoto",
			"refresh",
			"systemStatus",
			"actionStatus",			
			"eventsLabel",
			"eventDesc",
			"movePrevEvent",
			"moveNextEvent",
			"loadEventImage",
			"desc"
    ])
  }
}

def installed() {
	runIn(1, refresh)
}

def updated() {
	unschedule()
	//schedule("23 0/1 * * * ?", poll)	
	runEvery5Minutes(poll)	
}

def armSystem() {
	on()
}

def disarmSystem() {
	off()
}

def enableCamera() {
	generateEvent(getActionStatusEventData("enabling", "Enabling Camera"))
	parent.enableCamera(device.deviceNetworkId)	
	runIn(5, refreshDetails)
}

def disableCamera() {
	generateEvent(getActionStatusEventData("disabling", "Disabling Camera"))
	parent.disableCamera(device.deviceNetworkId)
	runIn(5, refreshDetails)
}

def take() {
	generateEvent(getActionStatusEventData("taking", "Taking Photo"))
	if (parent.takePhoto(device.deviceNetworkId)) {
		refreshDetails()
	}
	else {
		generateFailedStatusEvent()
	}
}

def generateFailedStatusEvent(changeActionStatus=true) {
	generateEvent(getActionStatusEventData("failed", "Failed", changeActionStatus))
	runIn(2, resetStatus)
}

def resetStatus() {
	generateEvent(getActionStatusEventData("", ""))
}
	
def on() {
	generateEvent(getActionStatusEventData("arming", "Arming System"))
  parent.arm()
	runIn(5, refreshDetails)
}

def off() {
	generateEvent(getActionStatusEventData("disarming", "Disarming System"))
	parent.disarm()
	runIn(5, refreshDetails)
}

def poll() {
	logDebug "Polling ${device.displayName}"
	refresh()
}

def refresh() {
	refreshDetails()
	runIn(5, refreshEvents)
}

def refreshDetails() {
	generateEvent(getActionStatusEventData("refreshing", "Refreshing Camera Details", true))
	def details = parent.getCameraDetails(device.deviceNetworkId)
	if (details) {
		refreshDetails(details)
		generateEvent(getActionStatusEventData("", "Camera Details Refreshed", true))
	}
	else {
		logDebug "Unable to get camera details."
		generateFailedStatusEvent(true)
	}
}

def refreshDetails(details) {
	generateEvent(getStatusEventData(details.status))
	generateEvent(getSystemStatusEventData(details.systemArmed))
	generateEvent(getTemperatureEventData(details.temperature))
	generateEvent(getBatteryEventData(details.battery))
	generateEvent(getSignalEventData("syncModuleSignal",details.syncModuleSignal))
	generateEvent(getSignalEventData("wifiSignal",details.wifiSignal))	
	generateEvent(getSwitchEventData(details.status))	
	generateEvent(getDescriptionEventData(details))	
	refreshHomescreenImage(details.photoUrl)
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

private getActionStatusEventData(status, desc, changeActionStatus=true) {
	if (changeActionStatus) {
		return [
			name: "actionStatus",
			value: status,
			displayed: false
		]
	}
}

private getStatusEventData(status) {
	return [
		name: "status",
		value: status,
		displayed: true
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

def generateEvent(eventData, onlyWhenChanged=true) {
	try {
		if (eventData && "${device.currentValue(eventData.name)}" != "${eventData.value}") {
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

def refreshEvents() {
	generateEvent(getActionStatusEventData("refreshing", "Refreshing Events", true))
	def events = parent.getCameraEvents(device.deviceNetworkId)
	if (events) {		
		refreshEvents(events)
		generateEvent(getActionStatusEventData("", "Camera Events Refreshed", true))		
	}
	else {
		logDebug "Unable to get camera events."
		generateFailedStatusEvent(true)
	}
}

def refreshEvents(events) {
	def newEvent = false	
	if (!state.cameraEvents) {
		state.cameraEvents = []
	}
	
	events.take(5).reverse().each { 	
		if (eventIsNew(it.eventId)) {
			newEvent = true
			if (state.cameraEvents.size() >= 5) {
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

def refreshSettings(cameraSettings) {

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
		value: "($eventNumber) ${getFormattedEventTime(state.cameraEvents[eventNumber-1].eventTime)}",
		displayed: false
	])
}

def getFormattedEventTime(eventTime) {
	eventTime.replaceFirst(" ", "\n")
}

def displayEventImage() {	
	def activeIndex = safeToInteger(device.currentValue("activeEventNumber"))-1
			
	if (state.cameraEvents.size() > activeIndex) {
		def url = state.cameraEvents[activeIndex]?.photoUrl
		loadImage(url, getEventImageName())			
	}
}

private loadImage(sourceUrl, newImageName) {		
	if (sourceUrl && !parent.imageFeatureDisabled()) {
		generateEvent(getActionStatusEventData("loading", "Loading Event Image"))
		def imageBytes = parent.getImage(sourceUrl)
		if(imageBytes) {
		
			storeImage(newImageName, imageBytes)			

			generateEvent([
				name: "image",
				value: newImage,
				displayed: false
			], true)
			generateEvent(getActionStatusEventData("", ""))			
		}
	}
	else if (!sourceUrl) {
		log.info "The image for this event has been deleted."
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