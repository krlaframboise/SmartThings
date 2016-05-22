/**
 *  Home Presence Manager v 1.3
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    
 *
 *  Changelog:
 *
 *    1.3 (05/21/2016)
 *      - Added buttonTimer back so delayed off works.
 *      - Added check for room active based on contact sensor.
 *      - Removed preferences for features that haven't
 *        been implemented yet.
 *      - Added preference to disable info logging.
 *      - Added optional info logging.
 *
 *    1.2.6 (04/26/2016)
 *      - Fixed bug that broke app for Windows Phone.
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
    name: "Home Presence Manager",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Uses motion sensors, contact sensors and virtual presence sensors to keep track of the room you're in so that you can make the lights stay on until you exit instead of relying on inactivity timeouts",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@3x.png")

preferences {
	page(name:"mainPage")
  page(name:"roomPage")
	page(name:"optionsPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install: true) {				
		section("Rooms") {
			state.newRoomNum = null			
			
			def rooms = []
			for (int roomNum = 1; roomNum <= 25; roomNum++) {
				def room = getRoom(roomNum)
				if (room) {
					rooms << room
				}
			}			
			rooms.sort { it.roomName.toLowerCase() }
			
			for (room in rooms) {
				getRoomPageLink(room)
			}
			
			href(
				name: "optionsLink", 
				title: "Options",
				description: "",
				page: "optionsPage", 
				required: false
			)	
		}
	}
}

def getRoom(roomNum) {
	def roomName = getRoomName(roomNum)
	
	if (!roomName && !state.newRoomNum) {
		state.newRoomNum = roomNum
		roomName = "< Add New Room >"
	}
	
	if (roomName) {
		return [roomName: roomName, roomNumber: roomNum]		
	}
	else {
		return null
	}
}

def getRoomPageLink(room) {	
	return href(
		name: "room${room.roomNumber}Link", 
		title: "${room.roomName}",
		description: "",
		page: "roomPage", 
		required: false,
		params: [roomNumber: room.roomNumber]
	)	
}

def roomPage(params) {
	dynamicPage(name:"roomPage") {
		def roomNumber = params.roomNumber.toInteger()
		def roomName = getRoomName(roomNumber)
		def roomLabel = roomName ? roomName : "New Room"		
		
		section("$roomLabel") {
			input "roomName$roomNumber", "text", 
				title: "Room Name", 
				defaultValue: roomName, 
				required: false
			input "presence$roomNumber", "capability.presenceSensor",
				title: "Which presence sensor represents this room?",
				required: false
		}		
		section("Motion Sensors") {
			input "motion$roomNumber", "capability.motionSensor", 
				title: "Which motion sensors indicate presence?", 
				multiple: true, 
				required: false
			input "motionTimeout$roomNumber", "number", 
				title: "How many seconds after motion stops does it take the sensor to report inactive?", 
				required: false
		}			
		section ("Contact Sensors") {
			input "contact$roomNumber", "capability.contactSensor", 
				title: "Which contact sensors indicate presence?", 
				multiple: true, 
				required: false
			input "contactPresence$roomNumber", "enum",
				title: "Present when contact is?",
				options: ["open", "closed", "toggle open", "toggle closed"],
				required: false
		}			
		
		section ("Lights") {
			input "lights$roomNumber", "capability.switch",
				title: "Turn which lights on when present?",
				multiple: true,
				required: false
			input "lightOffDelay$roomNumber", "number",
				title: "Off delay in Minutes?\n(This feature will only work if you've specified a Button Timer in the options.)",
				defaultValue: 0,
				required: false
		}		
	}
}

def getRoomName(roomNumber) {
	if (settings."roomName$roomNumber") {
		return settings."roomName$roomNumber"
	}
	else {
		return ""
	}	
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {		
		section("Options") {
			mode title: "Only for mode(s)",
				required: false
			//input "multiPersonMode", "capability.switch",
			//	title: "Which Switch Indicates Multi People Present?",
			//	required: false
			input "infoLoggingEnabled", "bool",
				title: "Info Logging Enabled?",
				defaultValue: true,
				required: false
			input "debugLoggingEnabled", "bool",
				title: "Debug Logging Enabled?",
				defaultValue: false,
				required: false							
		}
		section ("Scheduling") {
			paragraph "Leave this field empty unless you're using an external timer to turn on a switch at regular intervals.  This field is required for the light delay off feature to work."

			input "buttonTimer", "capability.button",
				title: "Which Button Timer?",
				required: false
		}
	}
}

def installed() {
	logDebug "Installed"

	initialize()
}

def updated() {	
	if (!isDuplicateCall(state.lastUpdate, 1)) {
		state.lastUpdate = new Date().time
		logDebug "Updated"
		unschedule()
		unsubscribe()
		initialize()
	}
}

def initialize() {		
	if (!isDuplicateCall(state.lastInitialize, 1)) {
		state.lastInitialize = (new Date().time)
		
		if (buttonTimer) {
			subscribe(buttonTimer, "button.pushed", timerEventHandler)
		}
		
		initializeRooms()
				
		for (room in state.rooms) {
			
			subscribe(presenceSensor(room), "presence", presenceEventHandler)
		
			subscribe(motionSensors(room), "motion.active", motionActiveEventHandler)
			
			subscribe(motionSensors(room), "motion.inactive", motionInactiveEventHandler)
			
			subscribe(contactSensors(room), "contact", contactEventHandler)
		}
	}
}

def timerEventHandler(evt) {
	def btnNumber = evt.data.replace("{\"buttonNumber\":", "").replace("}", "").substring(2)
	def roomNumber = validateRoomNumber(btnNumber)
	
	if (roomNumber > 0) {
		def room = findRoomByRoomNumber(roomNumber)
		if (!isPresent(room)) {
			changeSwitchState(room, "on", "off")
		}
	}
}

int validateRoomNumber(roomNumber) {
	try {
		return roomNumber.toInteger()
	}
	catch (e) {
		return 0
	}	
}

def initializeRooms() {
	def roomCount = 25
	if (state.rooms?.size() != roomCount) {
		createRooms(roomCount)
	}
	
	for (room in state.rooms) {
		room.roomName = settings["roomName${room.roomNumber}"]
		if (!room.roomName) room.roomName = "Room ${room.roomNumber}"
		
		room.motionTimeout = settings["motionTimeout${room.roomNumber}"]
		if (!room.motionTimeout) room.motionTimeout = 65
		
		room.pendingExit = false
	}		
}

def createRooms(roomCount) {
	def lastActivity = ((new Date().time) - (120 * 1000))
	state.rooms = []
	
	for (int roomNumber = 1; roomNumber < roomCount; roomNumber++) {
		state.rooms << [
			roomNumber: roomNumber,
			lastActivity: lastActivity]
	}
}

def presenceEventHandler(evt) {
	def room = findRoomByDeviceId(evt.device.id)	
	if (evt.value == "present") {
		turnOnLights(room)
	}
	else {
		turnOffLights(room)		
	}
}

def motionActiveEventHandler(evt) {
	def room = findRoomByDeviceId(evt.device.id)
	logDebug "${room.roomName} Motion is Active"
	
	room.lastActivity = new Date().time
	room.pendingExit = false
	
	handlePresentRoom(room)
}

def motionInactiveEventHandler(evt) {	
	def room = findRoomByDeviceId(evt.device.id)
	logDebug "${room.roomName} Motion is Inactive"	
	handleMotionInactiveRoom(room)
}

def contactEventHandler(evt) {
	def room = findRoomByDeviceId(evt.device.id)
	def presenceType = contactPresenceType(room)
	def currentlyPresent = isPresent(room)
	
	logDebug "${room.roomName} contact is ${evt.value}"
	
	if (presenceType == evt.value) {
		handlePresentRoom(room, true)
	}
	else if (currentlyPresent && (!presenceType.contains("toggle"))) {
		handleNotPresentRoom(room)
	}
	else if (presenceType.contains(evt.value)) {
		if (currentlyPresent) {
			handleNotPresentRoom(room)
		}
		else {
			handlePresentRoom(room, true)
		}
	}
}

def handleMotionInactiveRoom(room) {	
	if (otherRoomsAreActive(room)) {
		logDebug "Exiting ${room.roomName} because other rooms are active."
		handleNotPresentRoom(room)
	}
	else {
		logDebug "${room.roomName} is Pending Exit"
		room.lastActivity = new Date().time
		room.pendingExit = true
		handleRoomsPendingExit(room, false)
	}
}

def otherRoomsAreActive(room) {
	for (otherRoom in state.rooms) {
		if (room.roomNumber != otherRoom.roomNumber 
		&& roomIsActive(otherRoom, room.lastActivity))	{
			logDebug "${otherRoom.roomName} is Active (otherRoomsAreActive)\n$otherRoom"
			return true
		}
	}	
	return false
}


def handlePresentRoom(room, forceExit=false) {
	turnOnLights(room)
	if (!isPresent(room)) {
		logInfo "Setting ${room.roomName} to Present"
		presenceSensor(room)?.arrived()
	}	
	handleRoomsPendingExit(room, forceExit)	
}

def handleRoomsPendingExit(ignoreRoom, forceExit) {
	for (room in state.rooms) {		
		if (room.roomNumber != ignoreRoom.roomNumber
		&& (room.pendingExit || forceExit)) {
			logDebug "Exiting ${room.roomName} because ${ignoreRoom.roomName} is Present"
			handleNotPresentRoom(room)
		}
	}
}

def handleNotPresentRoom(room) {
	if (presenceSensor(room)) {
		logInfo "Setting ${room.roomName} to Not Present"
		presenceSensor(room)?.departed()
	}
	room.pendingExit = false	
}

private turnOnLights(room) {
	changeSwitchState(room, "off", "on")
}

private turnOffLights(room) {
	def offDelay = lightOffDelay(room) 
	if (buttonTimer && offDelay > 0) {
		logDebug "${room.roomName}'s light(s) will turn off in $offDelay minutes"
		buttonTimer.pushButtonIn("10" + room.roomNumber.toString(), offDelay * 60)
	}	
	else {
		changeSwitchState(room, "on", "off")
	}
}

private changeSwitchState(room, oldState, newState) {
	lights(room)?.each {
		if (it.currentSwitch == oldState) {
			logInfo "Turning $newState ${it.displayName}"
			(newState == "on") ? it.on() : it.off()
		}
	}
}

def roomIsActive(room, motionStartTime=null) {
	return ((hasActiveMotion(room)
		&& !hasPendingMotion(room, motionStartTime))
		|| hasActiveContact(room))
}

def hasPendingMotion(room, startTime) {
	if (hasActiveMotion(room) && !startTime) {			
		return ((new Date().time) <	(startTime + (room.motionTimeout * 1000)))	
	}
	else {
		return false
	}
}

def hasActiveContact(room) {
	return (!contactPresenceType(room) && isPresent(room))
}

def isPresent(room) {	
	return (presenceSensor(room)?.currentPresence == "present")
}

def hasActiveMotion(room) {	
	return (motionSensors(room)?.currentMotion == "active")
}

def findRoomByRoomNumber(roomNumber) {	
	for (room in state.rooms) {		
		if (room.roomNumber == roomNumber) {
			return room
		}
	}
	return null	
}

def findRoomByDeviceId(deviceId) {
	def currentRoom = null
		
	for (room in state.rooms) {
		
		if (!currentRoom) {
			def device = presenceSensor(room)?.find() {it.id == deviceId} 
			if (!device) { 
				device = motionSensors(room)?.find() {it.id == deviceId} 
			}
			if (!device) {
				device = contactSensors(room)?.find() {it.id == deviceId} 
			}
			
			if (device != null) {
				currentRoom = room
			}		
		}
	}	
	return currentRoom
}

def presenceSensor(room) {
	return getSetting(room, "presence")	
}

def motionSensors(room) {
	return getSetting(room, "motion")	
}

def contactSensors(room) {
	return getSetting(room, "contact")
}

def contactPresenceType(room) {
	if (getSetting(room, "contactPresence")) {
		return getSetting(room, "contactPresence")
	}
	else {
		return ""
	}
}

def lights(room) {
	return getSetting(room, "lights")	
}

int lightOffDelay(room) {
	def delay = getSetting(room, "lightOffDelay")
	if (delay?.isNumber()) {
		return delay.toInteger()
	}
	else {
		return 0
	}
}

private getSetting(room, settingName) {
	if (room?.roomNumber) {
		return settings["$settingName${room.roomNumber}"]
	}
	else {
		return null
	}
}

private isDuplicateCall(lastRun, allowedEverySeconds) {
	def result = false
	if (lastRun) {
		result =((new Date().time) - lastRun) < (allowedEverySeconds * 1000)
	}
	result
}

private logInfo(msg) {
	if (validateBool(settings.infoLoggingEnabled, true)) {
		log.info "$msg"
		logDebug msg
	}
}

private logDebug(msg) {
	if (validateBool(settings.debugLoggingEnabled, false)) {
		log.debug "$msg"
	}
}

private validateBool(value, defaultValue) {
	if (value == null) {
		return defaultValue
	}
	else {
		return value
	}
}