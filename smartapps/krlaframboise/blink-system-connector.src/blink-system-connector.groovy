/**
 *  Blink System Connector v 1.2
 *  (https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.2 (4/7/2016)
 *      - Added ability to take photos and enable/disable
 *        the camera.
 *
 *    1.1 (4/5/2016)
 *      - Made date fields use local time. 
 *      - Fixed timeout exceptions by retrieving less events.
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
definition(
  name: "Blink System Connector",
  namespace: "krlaframboise",
  author: "Kevin LaFramboise",
  description: "Provides Blink System Integration.",
  category: "My Apps",
  iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/blink-camera.png",
  iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/blink-camera-2x.png",
  iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/blink-camera-3x.png",
  singleInstance: true
)

preferences {
  section("SmartThings Hub") {
    input "hostHub", "hub", title: "Select Hub", multiple: false, required: true
  }
  section("Blink Credentials") {
    input name: "blinkUser", type: "text", title: "Email", required: true
    input name: "blinkPassword", type: "password", title: "Password", required: true
  }
  section("Smart Home Monitor") {
    input "shmEnabled", "bool", title: "Integrate with Smart Home Monitor", required: true, defaultValue: true
  }
	section("options") {
		input "disableImages", "bool", 
			title: "Disable image functionality?", 
			defaultValue: false
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true
			required: false
	}
}

def installed() {
  addBlinkCameras()  
	initialize()	
}

def addBlinkCameras() {
	getCameras().each {
		try {
			if (!getChildDevice(it.dni)) {
				logDebug "Adding Blink Camera - ${it.cameraName}"
				addBlinkCamera(it)
			}
		}
		catch (e) {
			log.error "Error Adding Camera ${it.cameraName}: ${e}"
		}
	}
}

private addBlinkCamera(camera) {
	addChildDevice(
		"krlaframboise", 
		"Blink Wireless Camera", 
		camera.dni, 
		settings.hostHub?.id, 
		[
			"name": "Blink Wireless Camera",
			label: "Blink - ${camera.cameraName}", 
			completedSetup: true
		]
	)
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()	
}

def initialize() {
	subscribe(location, "alarmSystemStatus", shmHandler)
	runEvery5Minutes("refreshChildren")
	runIn(3, refreshChildren, [overwrite: false])
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def refreshChildren() {
	if (timeElapsed(state.nextRefresh)) {
		state.nextRefresh = addToCurrentTime(5)
		
		getCameras().each {			
			logDebug "Refreshing ${it.cameraName}"
			
			def childDevice = getChildDevice(it.dni)
			if (childDevice) {
				childDevice.refresh(it)
			}
			else {
				log.error "Could not find Child Device for: ${it}"
			}
		}
	}
}

def getSystemStatus() {
	return getNetwork()?.armed ? "armed" : "disarmed"
}

def getImage(url) {
	if (!imageFeatureDisabled()) {
		logDebug "Getting Image: $url"
		
		if (!url.toLowerCase().endsWith(".jpg")) {
			url = "${url}.jpg"
		}
		
		def resp = getFromBlink(buildRequest("${url}"))
		
		if (resp?.isSuccess() && resp?.getContentType() == "image/jpeg") {
			return resp.data
		}
		else {
			log.error "Unable to get image $url"
			return null
		}
	}
	else {
		log.info "Image feature disabled"
	}
}

def imageFeatureDisabled() {
	return settings.disableImages ? true : false
}

def takePhoto(dni) {
	def request = buildRequest("${getCameraRequestPath(dni)}/thumbnail")
	def data = postToBlink(request)?.data
	if (!data) {
		log.error "Failed to take photo"
	}
	refreshChildren()
}

def enableCamera(dni) {
	return setCameraStatus(dni, "enable")
}

def disableCamera(dni) {
	return setCameraStatus(dni, "disable")
}

private setCameraStatus(dni, status) {
	def request = buildRequest("${getCameraRequestPath(dni)}/$status")
	def data = postToBlink(request)?.data
	if (!data) {
		log.error "Failed to set status of camera $cameraId to $status"
	}
	log.debug "camera status response: $data}"
	refreshChildren()
}

private getCameraRequestPath(dni) {
	return "/network/${getNetworkIdFromDNI(dni)}/camera/${getCameraIdFromDNI(dni)}"
}

def arm() {
	setStatus("arm")
}

def disarm() {
	setStatus("disarm")
}

def setStatus(status) {
	def networkId = getNetwork()?.id
	def request = buildRequest("/network/${networkId}/${status}")
	
	def data = postToBlink(request)?.data
	if (!data) {
		log.error "Failed to set status: ${status}"
	}
	refreshChildren()
	runIn(5, refreshChildren, [overwrite: false])
}

private getCommandStatus(commandId) {
	def networkId = getNetwork()?.id
	def request = buildRequest("/network/$networkId/command/$commandId")
	def data = getFromBlink(request)?.data
	if (!data) {
		log.error "Failed to get status of command id $commandId"
	}
	return data	
}

private getCameras() {
	def networkId = getNetwork()?.id
	def events = getEvents()
	def request = buildRequest("/homescreen")
	def devicesData = getFromBlink(request)?.data?.devices
	def cameras = devicesData?.findAll{ it.device_type == "camera" }?.collect {
		if (it.device_type == "camera") {
			return getCamera(networkId, it, findCameraEvents(events, it.device_id))
		}
	}		
	if (!cameras) {
		log.error "Failed to get cameras"
	}
	return cameras
}

private getCamera(networkId, homescreen, events) {
	def camera = [
		dni: getCameraDNI(
				networkId,
				homescreen.device_id
		),
		cameraId: homescreen.device_id,
		cameraName: homescreen.name,
		motionDetectionEnabled: homescreen.enabled,
		status: homescreen.active,
		photoUrl: homescreen.thumbnail,
		temperature: homescreen.temp,
		battery: homescreen.battery,
		syncModuleSignal: homescreen.lfr_strength,
		wifiSignal: homescreen.wifi_strength,
		systemArmed: homescreen.armed,
		networkId: networkId,
		updatedAt: getFormattedLocalTime(homescreen.updated_at),
		events: events
	]
}

def getFormattedLocalTime(utcDateString) {
	def localTZ = TimeZone.getTimeZone(location.timeZone.ID)		
	def utcDate = Date.parse(
		"yyyy-MM-dd'T'HH:mm:ss", 
		utcDateString.replace("+00:00", "")).time
	def localDate = new Date(utcDate + localTZ.getOffset(utcDate))	
	return localDate.format("MM/dd/yyyy hh:mm:ss a")
}

private findCameraEvents(events, cameraId) {
	return events.findAll { it.cameraId == cameraId }?.take(6)
}

def getCameraDNI(networkId, cameraId) {
	if (networkId && cameraId) {
		return "blink-$networkId-$cameraId"
	}
	else {
		log.error "Unable to get Camera DNI for [networkId: $networkId, cameraId: $cameraId]"
	}
}

private getNetworkIdFromDNI(dni) {
	return getFromDNI(dni, 1)
}

private getCameraIdFromDNI(dni) {
	return getFromDNI(dni, 2)
}

private getFromDNI(dni, index) {
	def dniParts = dni?.split("-")	
	if (dniParts?.size() == 3 && index < 3) {
		return dniParts[index]
	}
}


private getHomescreens() {
	def request = buildRequest("/homescreen")
	def data = getFromBlink(request)?.data	
	if (data) {		
		state.systemArmed = data.network?.armed ? true : false
		def homescreens = []
		data.devices?.each {
			if (it.device_type == "camera") {
				homescreens << it
			}
		}
	}
	else {
		log.error "Unable to retrieve homescreen"
		return null
	}
}

private getEvents() {
	def networkId = getNetwork()?.id
	def request = buildRequest("/events/network/$networkId")
	def eventData = getFromBlink(request)?.data?.event
	def events = eventData?.findAll { it.camera_id }?.collect {		
			[
				eventId: it.id,
				cameraId: it.camera_id,
				eventType: it.type,
				photoUrl: it.video_url?.replace(".mp4", ""),
				eventTime: getFormattedLocalTime(it.created_at)
			]
	}
	if (!events) {
		log.error "Failed to get events"
	}
	return events
}

private getNetwork() {
	def networks = getNetworks()
	if (networks && networks?.size() > 0) {
		return networks[0]
	}
	else {
		return null
	}
}

private getNetworks() {	
  def request = buildRequest("/networks")
	def networks = getFromBlink(request)?.data?.networks	
	if (!networks) {
		log.error "Unable to retrieve networks"
	}
	return networks
}

private getAuthToken() {
	if (!state.authToken) {
		logDebug "Retrieving AuthToken"
		def requestBody = [
			email:    settings.blinkUser,
			password: settings.blinkPassword,
			client_specifier: "iPhone 9.2 | 2.2 | 222"
		]
		def request = buildRequest("/login", null, requestBody)	
		state.authToken = postToBlink(request)?.data?.authtoken?.authtoken
		if (!state.authToken) {
			log.error "Failed to login: ${response}"
		}
	}
	return state.authToken
}

private buildRequest(path) {
	return buildRequest(path, [TOKEN_AUTH: getAuthToken()], null)
}

private buildRetryRequest(request) {
	def authToken = getAuthToken()
	if (request.headers?.TOKEN_AUTH) {
		request.headers.TOKEN_AUTH = authToken
	}
	else {
		request.headers = [TOKEN_AUTH: authToken]
	}
	return request
}

private buildRequest(path, requestHeaders, requestBody) {
	def request = [
		uri:  "https://prod.immedia-semi.com",
		path: path
	]
	
	if (requestHeaders) {
		request.headers = requestHeaders
	}
	
	if (requestBody) {
		request.body = requestBody
	}
	return request
}

private getFromBlink(request) {
	try {
		httpGet(request) { objResponse ->
			return objResponse
    }
  }
	catch (e) {
		if (canRetryRequest(e.message)) {
			return getFromBlink(buildRetryRequest(request))
		}
		else {
			log.error "Get from Blink ${request?.path} Exception: ${e}"
			return null
		}		
  }
}

private postToBlink(request) {
	try {
		httpPostJson(request) { objResponse ->
			return objResponse
    }
  }
	catch (e) {
		if (canRetryRequest(e.message)) {			
			return postToBlink(buildRetryRequest(request))
		}
		else {
			log.error "Post to Blink ${request?.path} Exception: ${e}"
			return null
		}
  }
}

private canRetryRequest(errorMessage) {
	if (state.authToken && errorMessage.contains("Unauthorized")) {
		state.authToken = null
		return true
	}
	else {
		return false
	}
}

private timeElapsed(time) {
	return ((!time) || (time < new Date().time))
}

private addToCurrentTime(seconds) {
	return ((new Date().time) + (seconds * 1000))
}

def shmHandler(evt) {
	if (settings.shmEnabled && state.shmStatus != evt.value) {
		
		state.shmStatus = evt.value
		def status = getSystemStatus()
		
		if (evt.value == "away" && status != "armed") {
			arm()
		}
		else if (status != "disarmed") {
			disarm()
		}		
	}	
}

private logDebug(msg) {
	if (settings.debugOutput) {
		log.debug "$msg"
	}
}