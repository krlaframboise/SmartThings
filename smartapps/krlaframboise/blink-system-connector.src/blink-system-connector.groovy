/**
 *  Blink System Connector v 1.5.1
 *  (https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.5.1 (9/29/2016)
 *      - Fixed uninstall bug, but still can't uninstall
 *        while cameras are being used in SmartApps.
 *      - Added additional logging for Blink requests.
 *
 *    1.5 (7/3/2016)
 *      - Added Web Dashboard
 *      - Added SHM preferences for stay and away so you 
 *        can specify which cameras should be armed.
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
  page(name:"blinkAccountSettingsPage")
	page(name:"otherSettingsPage")
	page(name:"shmSettingsPage")
	page(name:"dashboardSettingsPage")
	page(name:"enableDashboardPage")
	page(name:"disableDashboardPage")
	page(name:"toggleArmedPage")
	page(name:"addCamerasPage")
	page(name:"refreshCamerasPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		if (state.completedSetup) {
	
			section() {
				getDashboardHref()
				getToggleArmedPageLink()
			}
					
			section("System Status") {
				getSystemStatusParagraph()
			}
			
			section("Cameras") {
				getCameraList()
				getPageLink("addCamerasPageLink", 
					"Add Cameras", 
					"addCamerasPage")
				getPageLink("refreshCamerasPageLink", 
					"Refresh Cameras", 
					"refreshCamerasPage")
			}
			section("Settings") {				
				getPageLink("blinkAccountSettingsPageLink",
					"Blink Acount Settings",
					"blinkAccountSettingsPage")
				getPageLink("dashboardSettingsPageLink",
					"Dashboard Settings",
					"dashboardSettingsPage")
				getPageLink("shmSettingsPageLink",
					"Smart Home Monitor Settings",
					"shmSettingsPage")
				getPageLink("otherSettingsPageLink",
					"Other Settings",
					"otherSettingsPage")
			}	
		}
		else {
			getBlinkAccountSettingsPageContents()
			getDashboardSettingPageContents()
			getSHMSettingsPageContents()
			getOtherSettingsPageContents()
		}
	}
}

private getDashboardHref() {
	if (!state.endpoint) {
		href "enableDashboardPage", title: "Enable Dashboard", description: ""
	} 
	else {
		href "", title: "View Dashboard", style: "external", url: api_dashboardUrl()
	}
}

private getSystemStatusParagraph() {
	try {
		def data = getHomeScreenData()
		def network = data?.network
		def devices = data?.devices
		def syncModule = devices?.find { it.device_type == "sync_module" }
		def cameraCount = devices?.count { it.device_type == "camera" }
		def disabledCount = devices?.count { it.device_type == "camera" && (it.currentStatus == "disabled")}

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
				paragraph "The system is now ${newStatus}ed\n\nPlease wait a few seconds and then press Done"
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

private getCameraList() {
	getSortedDevices().each {
		paragraph image: "${getCameraStatusImageUrl(it.currentStatus)}",	"${it.displayName}"
	}
}

private getSortedDevices() {
	return getChildDevices().sort{it.displayName}
}

def shmSettingsPage() {
	dynamicPage(name:"shmSettingsPage") {		
		getSHMSettingsPageContents()
	}
}

private getSHMSettingsPageContents() {
	section ("Smart Home Monitor Settings") {
		input "shmArmedAwayCameras", "enum",
			title: "Arm these cameras when SHM is Armed(Away)",
			multiple: true,
			required: false,			
			options: getCameraOptions() 
		input "shmArmedStayCameras", "enum",
			title: "Arm these cameras when SHM is Armed(Stay)",
			multiple: true,
			required: false,
			options: getCameraOptions()
	}
}

private getCameraOptions() {
	def options = []
	getChildDevices().each {
		options << it.displayName
	}	
	return options
}

def otherSettingsPage() {
	dynamicPage(name:"otherSettingsPage") {		
		getOtherSettingsPageContents()
	}
}

private getOtherSettingsPageContents() {
	section ("Other Settings") {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true,
			required: false
	}
}

def dashboardSettingsPage() {
	dynamicPage(name:"dashboardSettingsPage") {		
		getDashboardSettingPageContents()
	}
}

def getDashboardSettingPageContents() {
	section ("Dashboard Settings") {
		if (state.endpoint) {
			log.info "Dashboard Url: ${api_dashboardUrl()}"
			input "dashboardRefreshInterval", "number", 
				title: "Dashboard Refresh Interval: (seconds)",
				defaultValue: 300,
				required: false
			input "dashboardMenuPosition", "enum", 
				title: "Menu Position:", 
				defaultValue: "Top of Page",
				required: false,
				options: ["Top of Page", "Bottom of Page"]
			input "dashboardShowCameraDetails", "bool", 
				title: "Show Camera Details?", 
				defaultValue: true,
				required: false
			paragraph ""
			getPageLink("disableDashboardPageLink",
				"Disable Dashboard",
				"disableDashboardPage")
		}
		else {
			getPageLink("enableDashboardPageLink",
				"Enable Dashboard",
				"enableDashboardPage")
		}
	}
}

private disableDashboardPage() {	
	dynamicPage(name: "disableDashboardPage", title: "") {
		section() {
			if (state.endpoint) {
				try {
					revokeAccessToken()
				}
				catch (e) {
					logDebug "Unable to revoke access token: $e"
				}
				state.endpoint = null
			}	
			paragraph "The Dashboard has been disabled! Tap Done to continue"	
		}
	}
}

private enableDashboardPage() {
	dynamicPage(name: "enableDashboardPage", title: "") {
		section() {
			if (initializeAppEndpoint()) {
				paragraph "The Dashboard is now enabled. Tap Done to continue"
			} 
			else {
				paragraph "Please go to your SmartThings IDE, select the My SmartApps section, click the 'Edit Properties' button of the Blink System Connector app, open the OAuth section and click the 'Enable OAuth in Smart App' button. Click the Update button to finish.\n\nOnce finished, tap Done and try again.", title: "Please enable OAuth for Blink System Connector", required: true, state: null
			}
		}
	}
}

def blinkAccountSettingsPage() {
	dynamicPage(name:"blinkAccountSettingsPage") {		
		getBlinkAccountSettingsPageContents()
	}
}

private getBlinkAccountSettingsPageContents() {
	section ("Blink Account Settings") {
		input "hostHub", "hub", 
			title: "Select SmartThings Hub",
			multiple: false, 
			required: true		
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
		if (shmEnabled()) {
			subscribe(location, "alarmSystemStatus", shmHandler)
		}		
		runEvery5Minutes(refreshAllCameraDetails)	
	}
}

// Revokes the dashboard access token, if applicable.
def uninstalled() {
	if (state.endpoint) {
		try {
			logDebug "Revoking dashboard access token"
			revokeAccessToken()
		}
		catch (e) {
			log.warn "Unable to revoke dashboard access token: $e"
		}
	}
	try {
		logDebug "Removing Cameras"
		removeAllCameras(getChildDevices())
	}
	catch (e) {
		log.warn "Unable to remove cameras"
	}
}

def childUninstalled() {

}

private removeAllCameras(devices) {
	devices?.each {
		logDebug "Removing ${it.displayName}"
		deleteChildDevice(it.deviceNetworkId)
	}
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

private systemArmed() {
	getHomeScreenData() // Updates state.systemArmed
	return state.systemArmed	
}

private getHomeScreenData() {
	def request = buildRequest("/homescreen")
	def data = getFromBlink(request)?.data
	state.systemArmed = data?.network?.armed ? true : false
	return data
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
		def response = postToBlink(request)
		state.authToken = response?.data?.authtoken?.authtoken
		if (!state.authToken) {
			log.error "Failed to login:\nResponse:${response}\nData:${response?.data}"
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
		log.warn "Post to Blink ${request?.path} Exception: ${e}"
		if (canRetryRequest(e.message)) {			
			return postToBlink(buildRetryRequest(request))
		}
		else {			
			return null
		}
  }
}

private canRetryRequest(errorMsg) {	
	def result = (state.authToken && errorMsg.contains("Unauthorized"))
	state.authToken = null
	return result
}

def shmHandler(evt) {
	handleSHM()
}

def handleSHM() {	
	def status = location.currentState("alarmSystemStatus")?.value ?: ""
	
	if (systemArmed() && !state.refreshDetails) {
		state.refreshDetails = true
		logDebug "Disarming cameras because SHM is ${status}"
		disarm()
		
		if (status != "off") {
			runIn(3, handleSHM)
		}
	}
	else {
		try {
			state.refreshDetails = true
			if (status == "away") {
				shmArmCameras(settings.shmArmedAwayCameras, status)
			}	
			else if (status == "stay") {
				shmArmCameras(settings.shmArmedStayCameras, status)
			}
		}
		catch (e) {
			log.error "shmHandler Exception: $e"
		}
		state.refreshDetails = false
	}
}

private shmArmCameras(cameraNames, shmStatus) {	
	getChildDevices().each {
		if (it.displayName in cameraNames) {
			shmArmCamerasDisabledToggle(it, false)
		}
		else {
			shmArmCamerasDisabledToggle(it, true)
		}
	}

	logDebug "Arming cameras because SHM is Armed(${shmStatus})"
	arm()
}

private shmArmCamerasDisabledToggle(camera, isDisabling) {
	def isDisabled = (camera.currentStatus == "disabled")
	if (isDisabled != isDisabling) {		
		if (isDisabling) {
			logDebug "Disabling ${camera.displayName}"
			disableCamera(camera.deviceNetworkId)
		}
		else {
			logDebug "Enabling ${camera.displayName}"
			enableCamera(camera.deviceNetworkId)			
		}
	}
}

boolean shmEnabled() {
	return settings.shmArmedAwayCameras || settings.shmArmedStayCameras
}

def getFormattedLocalTime(utcDateString) {
	if (utcDateString) {
		def localTZ = TimeZone.getTimeZone(location.timeZone.ID)		
		def utcDate = Date.parse(
			"yyyy-MM-dd'T'HH:mm:ss", 
			utcDateString.replace("+00:00", "")).time
		def localDate = new Date(utcDate + localTZ.getOffset(utcDate))	
		return localDate.format("MM/dd/yyyy hh:mm:ss a")
	}
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


/********************************************
*    Dashboard
********************************************/
private initializeAppEndpoint() {	
	if (!state.endpoint) {
		try {
			def accessToken = createAccessToken()
			if (accessToken) {
				state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")				
			}
		} 
		catch(e) {
			state.endpoint = null
		}
	}
	return state.endpoint
}

mappings {
	path("/dashboard") {action: [GET: "api_dashboard"]}
	path("/dashboard/:action") {action: [GET: "api_dashboard"]}
	path("/camera/:index") {action: [GET: "api_camera"]}	
	path("/camera/:index/:action") {action: [GET: "api_camera"]}	
}

private api_dashboardUrl() {
	return "${state.endpoint}dashboard"
}

private api_cameraUrl(cameraIndex) {
	return "${state.endpoint}camera/$cameraIndex"
}

def api_dashboard() {
	def action = params?.action
	def msg
	if (action) {
		switch (action) {
			case { it in ["arm", "disarm"]}:
				msg = !setStatus(action) ? "Unable to ${action} system" : getSuccessMessageHtml("System ${action}ed successfully")
				break
			case "update":
				msg = updateCameras()
				break;
			case "take":
				msg = takePhotos()
				break
			default:
				msg = null
		}
	}
	renderDashboardHtml(msg)
}

def takePhotos() {
	def msg = ""
	def success = 0
	def failed = 0
	
	getChildDevices().each {		
		logDebug "Taking photo with ${it.displayName}"
		if (takePhoto(it.deviceNetworkId)) {
			success += 1
		}
		else {
			failed += 1
			logDebug "Unable to take photo with ${it.displayName}"
		}
	}

	if (success > 0) {
		msg = getSuccessMessageHtml("${success} of ${success + failed} photos were successfully taken")				
	}
	else {
		msg = "Unable to take photos"
	}
	return msg
}

def updateCameras() {
	def msg = refreshAllCameraDetails()
	
	if (!msg.contains("Refresh Failed:")) {
		msg = getSuccessMessageHtml("Cameras updated successfully")
	}
	return msg
}

private renderDashboardHtml(msg) {
	def armed = systemArmed()
	def status = armed ? "armed" : "disarmed"
	def action = armed ? "Disarm" : "Arm"
	def header = getHeaderMessageHtml(msg, status)
	def nav = getNavHtml([
		[text: "${action} System", url: "${api_dashboardUrl()}/${action.toLowerCase()}"],
		[text: "Take Photos", url: "${api_dashboardUrl()}/take"],
		[text: "Update Cameras", url: "${api_dashboardUrl()}/update"],
		[text: "Reload Page", url: api_dashboardUrl()]
	])		
	def footer = "<footer><textarea rows=\"2\">${api_dashboardUrl()}</textarea></footer>"
	
	renderHtmlPage(getBodyHtml(header, getAllCamerasDetailHtml(), nav, footer), getDashboardCss(), api_dashboardUrl())	
}

def api_camera() {
	def cameraIndex = safeToInteger(params.index)
	def camera = getCameraByIndex(cameraIndex)
	def action = params.action
	def imageOnly = false
	def msg
	
	if (camera) {
		switch (action) {
			case "take":
				camera.take()
				msg = "Photo taken"
				break
			case "update":
				camera.refresh()
				msg = "Camera refreshed"
				break
			case "enable":
				camera.enableCamera()
				msg = "Camera enabled" 
				break
			case "disable":
				camera.disableCamera()
				msg = "Camera disabled"
				break
			case "image":
				imageOnly = true
				break
			default:
				msg = null
		}
		if (msg) {
			msg = getSuccessMessageHtml(msg)
		}
	}
	else {
		msg = (msg ? "${msg}<br><br>" : "") + "Camera ${cameraIndex + 1} Not Found"
	}

	if (imageOnly) {
		def imageUrl = "${api_cameraUrl(cameraIndex)}/image"
		renderHtmlPage("<div>${camera.displayName}<br><a href=\"${imageUrl}\">${getCameraImageHtml(camera.currentImageDataJpeg)}</a></div>", "body{text-align:center;margin:0 0 0 0;}img{width:100%;}", imageUrl)
	}
	else {
		renderCameraHtml(camera, cameraIndex, msg)
	}
}

private renderCameraHtml(camera, cameraIndex, msg) {
	def status = camera?.currentStatus ?: ""
	def action = (camera?.currentStatus == "disabled") ? "Enable" : "Disable"
	def header = getHeaderMessageHtml(msg, status)
	def nav = getNavHtml([
		[text: "${action} Camera", url: "${api_cameraUrl(cameraIndex)}/${action.toLowerCase()}"],
		[text: "Take Photo", url: "${api_cameraUrl(cameraIndex)}/take"],
		[text: "Update Camera", url: "${api_cameraUrl(cameraIndex)}/update"],
		[text: "Reload Page", url: api_cameraUrl(cameraIndex)],
		[text: "Go Back to Dashboard", url: api_dashboardUrl()]
	])
	
	def content = ""
	if (camera) {		
		content = getCameraSectionHtml(camera, cameraIndex, false, true)
	}
		
	renderHtmlPage(getBodyHtml(header, content, nav, ""), getCameraCss(), api_cameraUrl(cameraIndex))
}

private getBodyHtml(header, content, nav, footer) {
	if (settings.dashboardMenuPosition != "Bottom of Page") {
		return "$header$nav$content$footer"
	}
	else {
		return "$header$content$nav$footer"
	}
}
private getSuccessMessageHtml(partialMessage) {
	return "${partialMessage}, but you may not see the change until the page is reloaded a couple of times.<br><br>*** If you want to manually refresh the page, use the \"Reload Page\" button below instead of the browser's refresh button ***"
}

private getNavHtml(menuItems) {
	def html = ""
	if (menuItems) {
		html += "<nav><ul class=\"menu\">"
		menuItems.each { 
			html += "<li><a class=\"btn\" href=\"${it.url}\" onclick=\"this.innerText='Please Wait...'\">${it.text}</a></li>"	
		}
		html += "</ul></nav>"
	}
	return html
}

private getHeaderMessageHtml(msg, status) {
	msg = msg ? "<div class=\"message\">$msg</div>" : ""
	
	return "<header><div class=\"${status}\">${status.toUpperCase()}</div>${msg}</header>"
}

private renderHtmlPage(html, css, url) {
	render contentType: "text/html", 
		data: "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><meta http-equiv=\"refresh\" content=\"${settings.dashboardRefreshInterval ?: 30}; URL=${url}\"><style type=\"text/css\">${css}</style></head><body>${html}</body></html>"
}

private int safeToInteger(val, int defaultVal=0) {
	try {
		val = "$val"
		if (val?.isFloat()) {
			return val.toFloat().round().toInteger()
		}
		else if (val?.isInteger()){
			return val.toInteger()
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return defaultVal
	}
}

private getCameraByIndex(index) {
	def cameras = getSortedDevices()	
	if (cameras?.size() > index) {
		return cameras[index]
	}	
}

private getAllCamerasDetailHtml() {
	def result = ""
	def index = 0
	getSortedDevices().each {		
		result += getCameraSectionHtml(it, index, true, settings.dashboardShowCameraDetails)
		index += 1
	}	
	return result
}

private getCameraSectionHtml(camera, cameraIndex, useLink, includeDetails) {
	def sectionHeader = getCameraSectionHeaderHtml(camera, cameraIndex, useLink)
	def details = (includeDetails == false) ? "" : "<br><strong>DNI: </strong>${camera.deviceNetworkId}<br><strong>Status: </strong>${camera.currentStatus}<br><strong>Battery: </strong>${camera.currentBattery}%<br><strong>Motion: </strong>${camera.currentMotion}<br><strong>Temperature: </strong>${camera.currentTemperature}°<br><strong>WiFi Signal: </strong>${camera.currentWifiSignal}%<br><strong>Sync Module Signal: </strong>${camera.currentSyncModuleSignal}%<br><strong>Updated: </strong>${camera.currentUpdatedAt}"
	
	return "<section class=\"camera-details ${camera.currentStatus}\">${sectionHeader}<p>${getCameraImageHtml(camera.currentImageDataJpeg)}${details}</p></section>"
}

private getCameraSectionHeaderHtml(camera, cameraIndex, useLink) {
	def tag = useLink ? "a" : "span"
	def attr = useLink ? " href=\"${api_cameraUrl(cameraIndex)}\"" : ""
	def style = "style=\"background-image:url('${getCameraStatusImageUrl(camera.currentStatus)}');\""
	return "<h1><${tag} ${style}${attr}>${camera.displayName}</${tag}></h1>"
}

private getCameraImageHtml(imageData) {
	return "<img src=\"data:image/jpeg;base64,${imageData}\">"
}

private getDashboardCss() {
	return "${getCommonCss()}img{max-width:100%;}textarea{display:block;width:100%;margin-top:50px;}.camera-details{border-top:1px solid #000000;}@media (max-width: 639px){.camera-details{width:100%;}}@media (min-width:640px){.camera-details{width:292px;display:inline-block;margin:10px 10px 10px 10px;}header .message{width:75%;margin-left:auto;margin-right:auto;}}"
}


private getCameraCss() {
	return "${getCommonCss()}img{max-width:95%;}"
}

private getCommonCss() {
	return "body{text-align:center;font-family:Helvetica,arial,sans-serif;margin:0 0 10px 0;}section, footer{margin: 8px 8px 8px 8px;}p{width:auto;margin-left:auto;margin-right:auto;line-height:1.5;}img{margin-bottom:10px;}a,a:link,a:hover,a:visited{color:#0000EE;}h1{font-size:120%;line-height:1.3;}h1 a, h1 span{background:no-repeat left center;background-size:contain; padding: 12px 0px 12px 75px;display:inline-block;min-height:50px;vertical-align:middle;}ul.menu{margin:0 0 0 0;padding:0 0 0 0;text-align:center;background-image:none;} a.btn{width:75%;margin:3px 10px 3px 10px; background:#3498db; background-image:linear-gradient(to bottom, #3498db, #2980b9); border-radius: 10px; color: #ffffff; font-size: 125%;padding: 10px 20px 10px 20px; text-decoration:none;display:inline-block;}.btn:hover{background: #3cb0fd; background-image: linear-gradient(to bottom, #3cb0fd, #3498db); text-decoration: none;}header .message{font-size:125%;color:#FF0000;padding-bottom:15px;margin: 15px 15px 15px 15px;}header .armed, header .disarmed, header .disabled {width:100%;margin:0 0 8px 0;padding:8px 0 8px 0;color:#000000;font-weight:bold;font-size:150%;}header .armed{background-color: #ff9999;}header .disarmed{background-color: #99c2ff;}header .disabled {background-color: #9ca1a4;}"
}

private getCameraStatusImageUrl(status) {
	return "https://raw.githubusercontent.com/krlaframboise/Resources/master/blink-camera-${status}.png"
}