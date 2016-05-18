/**
 *  Blink System Connector v 1.4
 *  (https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.4 (5/18/2016)
 *      - Fixed the way the settings are displayed
 *        during new installation.
 *      - Fixed ui issue that prevented the SmartApp from
 *        working on Windows Phone.
 *      - Improved the way it polls the devices.
 *
 *    1.3 (4/22/2016)
 *      - Added multicamera support.
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
	page(name:"mainPage")
  page(name:"blinkSetupPage")
	page(name:"optionsPage")
	page(name:"toggleArmedPage")
	page(name:"addCamerasPage")
	page(name:"refreshCamerasPage")
	page(name:"cameraPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {				
		if (state.completedSetup) {
			state.lastCameraViewed = null				
			section("System Status") {
				getSystemStatusParagraph()
			}
			section() {
				getToggleArmedPageLink()
				getPageLink("addCamerasPageLink", 
					"Add Cameras", 
					"addCamerasPage")
				getPageLink("refreshCamerasPageLink", 
					"Refresh Cameras", 
					"refreshCamerasPage")
			}
			section("Cameras") {
				getCameraPageLinks()
			}
			section("Settings") {
				if(state.completedSetup) {
					getPageLink("optionsPageLink",
						"Options",
						"optionsPage")
				}
				getPageLink("blinkSetupPageLink",
					"Blink Acount Settings",
					"blinkSetupPage")
			}	
		}
		else {
			getBlinkSetupPageContents()
			getOptionsPageContents()
		}
	}
}

private getSystemStatusParagraph() {
	try {
		def data = getHomeScreenData()
		def network = data?.network
		def devices = data?.devices
		def syncModule = devices?.find { it.device_type == "sync_module" }
		def cameraCount = devices?.count { it.device_type == "camera" }
		def disabledCount = devices?.count { it.device_type == "camera" && !it.enabled}

		state.systemArmed = network?.armed ? true : false

		paragraph "System Armed: ${network?.armed}\n" +
			"System Status: ${network?.status}\n" +
			"Notifications: ${network?.notifications}\n" +
			"Sync Module Status: ${syncModule?.status}\n" +
			"Sync Module Response: ${getFormattedLocalTime(syncModule?.last_hb)}\n" +
			"Cameras: ${cameraCount}\n" +
			"Disabled Cameras: ${disabledCount}"
	}
	catch (e) {
		def msg = "Unable to connect to Blink.  This is usually caused by either an invalid username/password or the Blink service being temporarily down."
		log.error "getSystemStatusParagraph Error: $e"
	}
}

private getToggleArmedPageLink() {
	def newStatus = state.systemArmed ? "Disarm" : "Arm"
	getPageLink("toggleSystemArmed", 
		"${newStatus} System",
		"toggleArmedPage")
}

def toggleArmedPage() {
	dynamicPage(name:"toggleArmedPage") {
		section() {
			def newStatus = state.systemArmed ? "Disarm" : "Arm"
			def result
			if (state.systemArmed) {
				result = disarm()
			}
			else {
				result = arm()
			}
			if (result) {				
				paragraph "The system is now ${newStatus}ed"
			}
			else {
				paragraph "Unable to $newStatus System"
			}
		}
	}
}

def addCamerasPage() {
	dynamicPage(name:"addCamerasPage") {
		section("Add Cameras") {
			def msg = addCameras()
			if (!msg) {
				paragraph "No new cameras were found"
			}
			else {
				paragraph "$msg"
			}
		}
	}
}

private addCameras() {
	def msg = ""
	getCameras().each {				
		try {
			if (!getChildDevice(it.dni)) {
				logDebug "Adding Camera - ${it.cameraName}"
				addCamera(it)
				msg += "Added Camera ${it.cameraName}\n"
				getChildDevice(it.dni)?.refreshDetails(it)						
			}
		}
		catch (e) {
			msg += "Unable to add camera ${it.cameraName}\n"
			log.error "Error Adding Camera ${it.cameraName}: ${e}"
		}
	}
	return msg
}

private addCamera(camera) {
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

def refreshCamerasPage() {
	dynamicPage(name:"refreshCamerasPage") {
		section("Camera Details") {
			paragraph "${refreshAllCameraDetails()}"
		}
		section("Camera Events") {
			paragraph "${refreshAllCameraEvents()}"
		}
	}
}

private getCameraPageLinks() {
	try {
		getChildDevices().sort{it.displayName}.each {
			def dni = it.deviceNetworkId
			def cameraId = getCameraIdFromDNI(dni)
			if (cameraId) {
				getPageLink("camera${cameraId}PageLink",
					"${it.displayName} (${it.currentStatus})",
					"cameraPage",
					[dni: "${dni}"])
			}		
		}
	}
	catch (e) {
		log.error "Unable to list camera links: $e"
	}
}

def cameraPage() {
	dynamicPage(name:"cameraPage") {		
		def dni = "${params.dni}"
		section ("Camera $dni") {
			paragraph "This feature is not available yet."
		}
	}
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {		
		getOptionsPageContents()
	}
}

private getOptionsPageContents() {
	section ("Options") {
		input "shmEnabled", "bool", 
			title: "Integrate with Smart Home Monitor?",
			required: false, 
			defaultValue: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true,
			required: false
	}
}

def blinkSetupPage() {
	dynamicPage(name:"blinkSetupPage") {		
		getBlinkSetupPageContents()
	}
}

private getBlinkSetupPageContents() {
	section () {
		input "hostHub", "hub", 
			title: "Select SmartThings Hub",
			multiple: false, 
			required: true		
	}
	section ("Blink Account Settings") {
		input "blinkUser", "text", 
			title: "Blink Account Email",
			required: true
		input "blinkPassword", "password", 
			title: "Blink Account Password", 
			required: true
	}	
}

private getPageLink(linkName, linkText, pageName, args=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "",
		page: "$pageName"
	]
	if (args) {
		map.params = args
	}
	href(map)
}

def installed() {
	initialize()	
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

private initialize() {
	if (settings.hostHub && settings.blinkUser && settings.blinkPassword && !state.completedSetup) {
		state.completedSetup = true
		logDebug "${addCameras()}"
	}
	if (state.completedSetup) {
		if (settings.shmEnabled) {
			subscribe(location, "alarmSystemStatus", shmHandler)
		}		
		runEvery5Minutes(refreshAllCameraDetails)	
	}
}

def uninstalled() {
	removeAllCameras(getChildDevices())
}

def childUninstalled() {

}

private removeAllCameras(devices) {
	devices.each {
		removeCamera(it.deviceNetworkId)
	}
}

def removeCamera(dni) {
	deleteChildDevice(dni)
}

def getSystemStatus() {
	return getNetwork()?.armed ? "armed" : "disarmed"
}

def arm() {
	schedule("23 0/1 * * * ?", refreshAllCameraEvents)
	return setStatus("arm")	
}

def disarm() {
	runIn(5, unscheduleRefreshAllCameraEvents)
	return setStatus("disarm")	
}

def unscheduleRefreshAllCameraEvents() {
	unschedule(refreshAllCameraEvents)
}

private setStatus(status) {
	def networkId = getNetwork()?.id
	def request = buildRequest("/network/${networkId}/${status}")
	
	def data = postToBlink(request)?.data
	
	if (data) {
		runIn(5, refreshAllCameraDetails)
	}
	else {
		log.error "Failed to set status: ${status}"
	}
	return data	
}

def getBase64EncodedImage(url) {
	def imageBytes = getImage(url)?.buf
	if(imageBytes) {
		return imageBytes.encodeBase64()
	}
}

def getImage(url) {
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

def takePhoto(dni) {
	def request = buildRequest("${getCameraRequestPath(dni)}/thumbnail")
	def data = postToBlink(request)?.data
	if (data) {
		return waitForCommandToComplete(getNetworkIdFromDNI(dni), data.id, 8)
	}
	else {
		log.error "Failed to take photo"
		return false
	}
}

boolean waitForCommandToComplete(networkId, commandId, timeoutSecs) {
	def abortTime = addToCurrentTime(timeoutSecs * 1000)
	def successful = false
	def completed = false
	
	while (!timeElapsed(abortTime) && !completed) {		
		pause(500)
		def commandStatus = getCommandStatus(networkId, commandId)?.data
		
		if (commandStatus?.complete) {
			successful = commandStatus?.status_msg?.toLowerCase()?.contains("succeeded")				
			completed = true
		}
		else if (status?.status_msg) {				
			logDebug "commandCheckResponse: ${commandStatus.data}"
		}
	}	
	return (completed && successful)
}

private getCommandStatus(networkId, commandId) {
	def request = buildRequest("/network/${networkId}/command/${commandId}")
	
	return getFromBlink(request)
}

def enableCamera(dni) {
	setCameraStatus(dni, "enable")	
}

def disableCamera(dni) {
	setCameraStatus(dni, "disable")
}

private setCameraStatus(dni, status) {
	def request = buildRequest("${getCameraRequestPath(dni)}/$status")
	def data = postToBlink(request)?.data
	if (!data) {
		log.error "Failed to set status of camera $cameraId to $status"
	}
	//log.debug "camera status response: $data}"
	//refreshCamera(dni)
}

private getCameraRequestPath(dni) {
	return "/network/${getNetworkIdFromDNI(dni)}/camera/${getCameraIdFromDNI(dni)}"
}

def refreshAllCameraDetails() {
	def msg = ""
	try {
		if (!state.refreshingDetails) {
			state.refreshingDetails = true
			
			logDebug "Refreshing Details of All Cameras"
			
			getCameras().each {
				def childDevice = getChildDevice(it.dni)
				if (childDevice) {
					logDebug "Refreshing ${it.cameraName}"
					childDevice.refreshDetails(it)
					msg += "${it.cameraName}: Refreshed\n"
				}
				else {
					logDebug "Skipped ${it.cameraName}"
					msg += "${it.cameraName}: Skipped\n"
				}
			}
		}
		else {
			msg += "Refresh Already Running\n"
		}
	}
	catch(e) {
		log.error "refreshAllCameraDetails Error: $e"
		msg += "Refresh Failed: ${e}\n"
	}
	state.refreshingDetails = false
	return msg
}

def getCameraDetails(dni) {
	return getCameras().find { it?.dni == dni }
}

private getCameras() {
	def networkId = getNetwork()?.id
	def cameras = getHomeScreenData()?.devices?.findAll{ it.device_type == "camera" }?.collect {	
		return getCamera(networkId, it)
	}
	if (cameras) {
		return cameras.sort { it.cameraName }
	}
	else {
		log.error "Failed to get cameras"
	}	
}

private getCamera(networkId, device) {
	return [
		dni: getCameraDNI(
				networkId,
				device.device_id
		),
		cameraId: device.device_id,
		cameraName: device.name,
		motionDetectionEnabled: device.enabled,
		status: device.active,
		photoUrl: device.thumbnail,
		temperature: device.temp,
		battery: device.battery,
		syncModuleSignal: device.lfr_strength,
		wifiSignal: device.wifi_strength,
		systemArmed: device.armed,
		networkId: networkId,
		updatedAt: getFormattedLocalTime(device.updated_at)
	]
}

private getHomeScreenData() {
	def request = buildRequest("/homescreen")
	return getFromBlink(request)?.data
}

def refreshAllCameraEvents() {
	def msg = ""
	try {
		if (!state.refreshingEvents) {
			state.refreshingEvents = true
			logDebug "Refreshing All Camera Events"
			
			getChildDevices().sort{it.displayName}.each {
				logDebug "Refreshing ${it.displayName} Events"
				def events = getCameraEvents(it.deviceNetworkId)
				if (events) {
					it.refreshEvents(events)
					msg += "${it.displayName}: Refreshed\n"
				}
				else {
					msg += "${it.displayName}: No Events Found\n"
				}
			}
		}
		else {
			msg += "Refresh Already Running\n"
		}
	}
	catch(e) {		
		log.error "refreshAllCameraEvents Error: $e"
		msg += "Refresh Failed: ${e}\n"
	}
	state.refreshingEvents = false
	return msg
}

def getCameraEvents(dni) {
	def request = buildRequest("/events${getCameraRequestPath(dni)}")
	def events = getFromBlink(request)?.data?.event?.collect {
		[
			eventId: it.id,
			cameraId: it.camera_id,
			eventType: it.type,
			photoUrl: it.video_url?.replace(".mp4", ""),
			eventTime: getFormattedLocalTime(it.created_at)
		]
	}
	events?.removeAll { !it.photoUrl }
	return events?.take(5)
}

private getCameraDevice(dni) {
	def device = getChildDevice(dni)
	if (!device) {		
		logDebug "Device $dni not found"
	}
	return device
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

private canRetryRequest(errorMsg) {
	if (state.authToken && errorMsg.contains("Unauthorized")) {
		state.authToken = null
		return true
	}
	else {
		return false
	}
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

def getFormattedLocalTime(utcDateString) {
	def localTZ = TimeZone.getTimeZone(location.timeZone.ID)		
	def utcDate = Date.parse(
		"yyyy-MM-dd'T'HH:mm:ss", 
		utcDateString.replace("+00:00", "")).time
	def localDate = new Date(utcDate + localTZ.getOffset(utcDate))	
	return localDate.format("MM/dd/yyyy hh:mm:ss a")
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

private timeElapsed(time) {
	return ((!time) || (time < new Date().time))
}

private addToCurrentTime(seconds) {
	return ((new Date().time) + (seconds * 1000))
}

private logDebug(msg) {
	if (settings.debugOutput) {
		log.debug "$msg"
	}
}