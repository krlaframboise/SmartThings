/**
 *  Blink Wireless Camera v 1.5
 *  (https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.5 (7/03/2016)
 *      - Added updatedAt attribute
 *
 *    1.4 (5/18/2016)
 *      - Fixed word wrap issues caused by last mobile app update.
 *      - Removed scheduling functionality and letting parent
 *        control that instead.
 *      - Enhanced UI feedback by utilizing the nextState feature.
 *
 *    1.3 (4/22/2016)
 *      - Switched from carousel to htmltile.
 *      - Added multicamera support.
 *      - Fixed missing icons caused by android update.
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
		attribute "updatedAt", "String"
		attribute "syncModuleSignal", "number"
		attribute "wifiSignal", "number"
		attribute "activeEventNumber", "number"
		attribute "activeEventDesc", "String"
		attribute "imageDataJpeg", "String"
		
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
		htmlTile(name:"imageHTML", 
			action: "getImageHTML", , 
			refreshInterval: 1, 
			width: 6, 
			height: 4)
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
			state "enabled", label:'Enabled',
				nextState: "disabling",
				action:"disableCamera",
				icon:"",
				backgroundColor:"#99c2ff"
			state "disabling", label:'Disabling',
				action:"disableCamera",
				icon:""
			state "disabled", label:'Disabled',
				nextState:"enabling",
				action:"enableCamera",
				icon:""
			state "enabling", label:'Enabling',
				icon:"",
				action:"enableCamera",
				backgroundColor:"#99c2ff"
    }		
		valueTile("systemStatus", "device.systemStatus", width: 2, height: 2) {      
			state "disarmed", label:'Disarmed',
				nextState: "arming",
				action: "switch.on", 
				icon: "", 
				backgroundColor: "#99c2ff"
			state "arming", label:'Arming', 
				action: "switch.on", 
				icon: "", 
				backgroundColor: "#ff9999"
			state "armed", label:'Armed',
				nextState: "disarming",
				action: "switch.off", 
				icon: "", 
				backgroundColor: "#ff9999"
			state "disarming", label:'Disarming',
				action: "switch.off", 
				icon: "", 
				backgroundColor: "#99c2ff"
    }
		valueTile("temperature", "device.temperature", width: 1, height: 1, decoration: "flat") {
			state("temperature", label:'${currentValue}°'
			)
		}
		valueTile("motion", "device.motion",  width: 1, height: 1, decoration: "flat") {
			state "inactive", label:'No Motion'//, icon: "st.motion.motion.inactive"
			state "active", label:'Motion', 				
				backgroundColor:"#99c2ff"//,icon: "st.motion.motion.active"
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
      state "default", label:'Prev', 
				action:"previousEvent"//,icon:"st.thermostat.thermostat-left"
    }
		valueTile("moveNextEvent", "generic", width: 1, height: 1) {
      state "default", label:'Next', 
				action:"nextEvent"//, icon:"st.thermostat.thermostat-right"
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
      //"cameraDetails",
			"imageHTML",
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
	state.homescreenImageSource = ""
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
	generateEvent(getUpdatedAtEventData(details))	
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

private getUpdatedAtEventData(camera) {
	return [
		name: "updatedAt",
		value: "${camera.updatedAt}"
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
		logDebug "No Events Found"
		state.cameraEvents?.clear()
		updateCameraEventAttributes(0)
		resetStatus()
	}
}

def refreshEvents(events) {
	def newEvent = false	
	if (!state.cameraEvents) {
		state.cameraEvents = []
	}
	
	state.cameraEvents.removeAll { item ->
		!events?.find { it.eventId == item.eventId }
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
	def desc = (eventNumber == 0) ? "" : "($eventNumber) ${getFormattedEventTime(state.cameraEvents[eventNumber-1].eventTime)}"
	generateEvent([
		name: "activeEventDesc",
		value: "$desc",
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
	if (sourceUrl) {
		generateEvent(getActionStatusEventData("loading", "Loading Event Image"))
		def encodedImage = parent.getBase64EncodedImage(sourceUrl)
		if (encodedImage) {			
			generateEvent([
				name: "imageDataJpeg",
				value: encodedImage,
				displayed: false
			], false)
			resetStatus()			
		}
	}
	else {
		log.info "The image for this event has been deleted."
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

mappings {
    path("/getImageHTML") {action: [GET: "getImageHTML"]}
}

def getImageHTML() {	
	def imageData = device.currentValue("imageDataJpeg")
	renderHTML {
    	head {
        	"""
            	<style type="text/css">img{max-width:100%;max-height:100%;}</style>
            """
        }
        body {
        	"""
               <img src='data:image/jpeg;base64,$imageData' />
            """
        }
    }
}

private logDebug(msg) {
	if (settings.debugOutput) {
		log.debug "$msg"
	}
}