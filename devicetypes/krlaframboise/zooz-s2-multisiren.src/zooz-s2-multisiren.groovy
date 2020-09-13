/**
 *  Zooz S2 Multisiren v1.5.1
 *  (Models: ZSE19)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation: https://community.smartthings.com/t/release-zooz-s2-multisiren-zse19/142891?u=krlaframboise
 *
 *
 *  Changelog:
 *
 *    1.5.1 (09/13/2020)
 *      - Removed vid which makes it fully supported in the new mobile app.
 *      - Create switch on/off events when "switch on action" setting is not turn on alarm to prevent network errors.
 *      - Create temporary level event in setLevel to prevent network errors.
 *
 *    1.4 (05/24/2020)
 *      - Added lifeline association check and add the association if it wasn't automatically added during inclusion.
 *
 *    1.3.2 (05/18/2020)
 *      - Fixed bug with health check interval.
 *
 *    1.3.1 (03/13/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.3 (08/07/2019)
 *      - Enhanced UI for new mobile app.
 *			- Added Tone capability and "Beep Sound" setting.
 *
 *    1.2 (05/06/2018)
 *      - Added volume setting.
 *
 *    1.1 (12/09/2018)
 *      - Added tamper capability.
 *      - Added music player, audio notification, and speech synthesis capabilities to ensure that all sounds can be played using the built-in smart apps.
 *
 *    1.0 (11/06/2018)
 *      - Initial Release
 *
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
		name: "Zooz S2 Multisiren", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.siren"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Alarm"
		capability "Switch"		
		capability "Audio Notification"
		capability "Music Player"
		capability "Tone"
		capability "Speech Synthesis"
		capability "Switch Level"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Tamper Alert"
		capability "Health Check"
				
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"		
		attribute "lastCheckin", "string"
		
		
		// Music Player commands used by some apps
		command "playSoundAndTrack"
		command "playTrackAtVolume"
		command "playText"
		command "playSound"
		

		fingerprint mfr:"027A", prod:"000C", model:"0003", deviceJoinName: "Zooz S2 Multisiren"
	}

	simulator { }
		
	tiles(scale: 2) {
		multiAttributeTile(name:"primaryStatus", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "alarm", label: 'ALARM', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#00a0dc"
				attributeState "play", label: 'PLAYING', action: "alarm.off", icon: "st.alarm.beep.beep", backgroundColor: "#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
			}
		}
		
		standardTile("sliderText", "generic", width: 2, height: 2) {
			state "default", label:'Play Sound #'
		}
		
		controlTile("slider", "device.level", "slider",	height: 2, width: 4) {
			state "level", action:"switch level.setLevel"
		}
		
		standardTile("off", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "alarm.off"
		}
		
		standardTile("on", "device.switch", width: 2, height: 2) {
			state "default", label:'On', action: "switch.on"
		}
		
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "default", label:'Alarm', action: "alarm.both"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configuration.configure", icon:"st.secondary.tools"
		}
		
		valueTile("battery", "device.battery", width: 2, height: 2) {
			state "default", label:'${currentValue}% Battery'
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:3, height: 1) {
			state "syncStatus", label:'${currentValue}'
		}
		
		main "primaryStatus"
		details(["primaryStatus", "sliderText", "slider", "off", "alarm", "on", "refresh", "configure", "battery", "firmwareVersion", "syncStatus"])
	}
		
	preferences {	
		configParams.each { param ->
			input "configParam${param.num}", "enum",
				title: "${param.name}:",
				required: false,
				defaultValue: "${param.value}",
				displayDuringSetup: true,
				options: param.options
		}
		
		input "switchOnAction", "enum",
			title: "Switch On Action",
			defaultValue: "0",
			required: false,
			options: setDefaultOption(switchOnActionOptions, "0")
			
		input "beepSound", "enum",
			title: "Beep Sound",
			defaultValue: "1",
			required: false,
			options: setDefaultOption(chimeSoundOptions, "1")
			
		input "chimeVolume", "enum",
			title: "Chime Volume",
			defaultValue: chimeVolumeSetting,
			required: false,
			options: setDefaultOption(chimeVolumeOptions, "32")

		// input "tempOffset", "enum",
			// title: "Temperature Offset",
			// defaultValue: "0",
			// required: false,
			// options: setDefaultOption(tempOffsetOptions, "0")
			
		// input "humidityOffset", "enum",
			// title: "Humidity Offset",
			// defaultValue: "0",
			// required: false,
			// options: setDefaultOption(humidityOffsetOptions, "0")
		
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}

private getChimeVolumeSetting() {
	return settings?.chimeVolume ?: "32"
}

def installed () { 
	return response(refresh())
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		logDebug "updated()..."

		runIn(2, updateSyncStatus)
		
		def cmds = []
		if (pendingChanges > 0) {
			cmds += configure()
		}
		return cmds ? response(cmds) : []
	}	
}


def configure() {	
	logDebug "configure()..."

	runIn(5, updateSyncStatus)
			
	def cmds = []
	
	if (!device.currentValue("switch")) {
		sendEvent(getEventMap("switch", "off"))
		resetLevel()
		sendEvent(getEventMap("alarm", "off"))
		sendEvent(getEventMap("primaryStatus", "off"))
	}
	
	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
		
	if (!state.linelineAssoc) {
		if (state.linelineAssoc != null) {
			logDebug "Adding missing lineline association..."
			cmds << lifelineAssociationSetCmd()
		}
		cmds << lifelineAssociationGetCmd()
	}
	
	logDebug "CHANGING Volume to ${Integer.parseInt(chimeVolumeSetting, 16)}%"
	cmds << soundSwitchConfigSetVolumeCmd(chimeVolumeSetting)
	state.chimeVolume = chimeVolumeSetting
	
	configParams.each { 
		logDebug "CHANGING ${it.name}(#${it.num}) from ${getParamStoredValue(it.num)} to ${it.value}"
		cmds << configSetCmd(it, it.value)
		cmds << configGetCmd(it)
		cmds << configGetCmd(it)		
	}
	
	return delayBetween(cmds, 250)
}


def on() {
	logDebug "on()..."
	if (settings?.switchOnAction == "on") {
		return both()
	}
	else {
		sendEvent(name: "switch", value:"on")
		runIn(2, resetSwitch)
		
		def sound = safeToInt(settings?.switchOnAction, 0)
		if (sound) {
			return playSound(sound)
		}	
		else {
			log.warn "Ignoring 'on' command because the Switch On Action setting is set to 'Do Nothing'"
		}
	}
}

def resetSwitch() {
	sendEvent(name: "switch", value:"off")
}


def beep() {
	logDebug "beep()..."
	return playSound(safeToInt(settings?.beepSound, 0))
}


// Music Player Commands
def play() {
	return on()
}

def pause() {
	return off()
}

def stop() {
	return off()
}

def mute() {
	logUnsupportedCommand("mute()")
}
def unmute() {
	logUnsupportedCommand("unmute()")
}
def nextTrack() {
	logUnsupportedCommand("nextTrack()")
}
def previousTrack() {
	logUnsupportedCommand("previousTrack()")
}
private logUnsupportedCommand(cmdName) {
	logTrace "This device does not support the ${cmdName} command."
}
 
 
// Audio Notification Capability Commands
def playSoundAndTrack(URI, duration=null, track, volume=null) {	
	playTrack(URI, volume)
}
def playTrackAtVolume(URI, volume) {
	playTrack(URI, volume)
}

def playTrackAndResume(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTextAndResume(message, volume=null) {
	playText(message, volume)
}	
def playTextAndRestore(message, volume=null) {
	playText(message, volume)
}

def speak(message) {
	// Using playTrack in case url is passed in.
	playTrack("$message", null)
}

def playTrack(URI, volume=null) {
	logTrace "Executing playTrack($URI, $volume)"
	def text = getTextFromTTSUrl(URI)
	playText(!text ? URI : text, volume)	
}

private getTextFromTTSUrl(URI) {
	if (URI?.toString()?.contains("/")) {
		def startIndex = URI.lastIndexOf("/") + 1
		return URI.substring(startIndex, URI.size())?.toLowerCase()?.replace(".mp3","")
	}
	return null
}

def playText(message, volume=null) {
	playSound(message)
}


def setLevel(level, duration=null) {
	sendEvent(name: "level", value: level, unit: "%")
	runIn(2, resetLevel)
		
	logDebug "setLevel(${level})..."	
	playSound(level)
}

def resetLevel() {
	sendEvent(name: "level", value: 0, unit: "%")
}


def playSound(sound) {
	logDebug "playSound(${sound})"
		
	def cmds = []
	def val = safeToInt(sound, 0)
	if (val) {
		if (device.currentValue("alarm") == "off") {
			runIn(2, clearStatus)
			sendEvent(getEventMap("primaryStatus", "play"))
			cmds << configSetCmd(playSoundParam, val)
		}
		else {
			log.warn "Can't play sound #${val} beacuse alarm is on"
		}		
	}
	else {
		log.warn "${sound} is not a valid sound number"
	}	
	return cmds
}

def clearStatus() {
	if (device.currentValue("alarm") == "off") {
		sendEvent(getEventMap("primaryStatus", "off"))
	}
}


def siren() {
	return both()
}

def strobe() { 
	return both() 
}

def both() { 
	log.debug "Turning on Siren..."
	return delayBetween([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	], 500)
}


def off() {
	logDebug "off()..."	
	
	if (settings?.switchOnAction != "on") {
		resetSwitch()
	}
	
	return delayBetween([
		switchBinarySetCmd(0),
		configSetCmd(playSoundParam, 0)
	], 500)
}


def refresh() {
	logDebug "refresh()..."
	
	runIn(5, updateSecondaryStatus)
	
	state.lastBattery = null
	return delayBetween([		
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(humiditySensorType)
	], 2500)
}


def ping() {
	logDebug "ping()..."	
	return sendResponse([basicGetCmd()])
}

private sendResponse(cmds) {
	def hubCmds = []
	cmds.each {
		hubCmds << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(hubCmds, 100)
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private lifelineAssociationSetCmd() {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
}

private lifelineAssociationGetCmd() {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: 1))
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 0, sensorType: sensorType))
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private soundSwitchConfigSetVolumeCmd(volume) {
	def cmd = "7905${volume}01"
	if (isSecurityEnabled()) {
		return "988100${cmd}"
	}
	else {
		return cmd
	}
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
	if (isSecurityEnabled()) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private isSecurityEnabled() {
	return zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))
}


private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x31: 5,	// SensorMultilevel
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service (V2)
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x70: 2,	// Configuration
		0x71: 3,	// Notification (v4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x79: 1,	// Sound Switch
		0x7A: 2,	// Firmware Update Md
		0x80: 1,	// Battery
		0x85: 2,	// Association
		0x86: 1,	// Version (3)
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {	
	def result = []
	try {
		if ("${description}".startsWith("Err 106")) {
			log.warn "secure inclusion failed"
		}
		else {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)		
			}
			else {
				log.warn "Unable to parse: $description"
			}
		}
			
		if (!isDuplicateCommand(state.lastCheckinTime, 59000)) {
			state.lastCheckinTime = new Date().time
			sendEvent(getEventMap("lastCheckin", convertToLocalTimeString(new Date())))
		}
		
		if (!isDuplicateCommand(state.lastBattery, (12 * 60 * 60 * 1000))) {			
			result << response(batteryGetCmd())
		}
	}
	catch (e) {
		log.error "${e}"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)	
	
	def result = []
	if (encapsulatedCmd) {
		result += zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	
	if (cmd.notificationType == 7) {
		def tamperVal 
		if (cmd.event == 3) {		
			tamperVal = "detected"
		}
		else if (cmd.event == 0 && cmd.eventParameter[0] == 3) {
			tamperVal = "clear"
		}
		logDebug "Tamper ${tamperVal}"
		sendEvent(getEventMap("tamper", tamperVal))
		runIn(1, updateSecondaryStatus)
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	
	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)
	}
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "AssociationReport: ${cmd}"
	
	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)
	
	if (cmd.groupingIdentifier == 1) {
		state.linelineAssoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	
	state.lastBattery = new Date().time
	
	sendEvent(getEventMap("battery", val, true, "%"))
	return []
}	


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	logTrace "ConfigurationReport: ${cmd}"
	
	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)	

		if (param == reportingIntervalParam) {
			updateHealthCheckInterval(val)
		}
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}		
	return []
}

private updateHealthCheckInterval(minutes) {
	def minReportingInterval = (((reportingIntervalParam.value < 60) ? 60 : reportingIntervalParam.value) * 60)
	
	if (state.minReportingInterval != minReportingInterval) {
		state.minReportingInterval = minReportingInterval
			
		// Set the Health Check interval so that it can be skipped three times plus 5 minutes.
		def checkInterval = ((minReportingInterval * 3) + (5 * 60))
		
		def eventMap = getEventMap("checkInterval", checkInterval)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}	
}

def updateSyncStatus(status=null) {	
	if (status == null) {	
		def changes = getPendingChanges()
		if (changes > 0) {
			status = "${changes} Pending Change" + ((changes > 1) ? "s" : "")
		}
		else {
			status = "Synced"
		}
	}	
	if ("${syncStatus}" != "${status}") {
		sendEvent(getEventMap("syncStatus", status))
	}
}

private getSyncStatus() {
	return device.currentValue("syncStatus")
}

private getPendingChanges() {
	return (configParams.count { isConfigParamSynced(it) ? 0 : 1 } + (settings?.chimeVolume != state.chimeVolume ? 1 : 0) + (!state.linelineAssoc ? 1 : 0))
}

private isConfigParamSynced(param) {
	return (param.value == getParamStoredValue(param.num))
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"
	
	def primaryStatus = "off"
	def alarmVal = "off"
	
	if (cmd.value) {
		primaryStatus = "alarm"
		alarmVal = "both"
	}
	
	sendEvent(getEventMap("alarm", alarmVal, true))
	sendEvent(getEventMap("primaryStatus", primaryStatus))
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=0) {
	logTrace "BasicReport: ${cmd}"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"
	
	runIn(2, updateSecondaryStatus)
	
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = cmd.scale ? "F" : "C"
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
			
			sendEvent(getEventMap("temperature", temp, true, getTemperatureScale()))
			break		
			
		case humiditySensorType:			
			sendEvent(getEventMap("humidity", cmd.scaledSensorValue, true, "%"))
			break		
		default:
			logDebug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	
	return []
}

def updateSecondaryStatus() {
	def temp = device.currentValue("temperature")
	def humidity = device.currentValue("humidity")
	def value = "${temp}°${getTemperatureScale()} / ${humidity}% RH"
	
	if (device.currentValue("tamper") == "detected") {
		value = "TAMPERING / ${value}"
	}
	
	sendEvent(name:"secondaryStatus", value:value, displayed: false)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }


private getConfigParams() {
	return [
		alarmDurationParam,
		reportingIntervalParam
	]
}

private getAlarmDurationParam() {
	return getParam(1, "Alarm Duration", 2, 180, alarmDurationOptions)
}

private getReportingIntervalParam() {
	return getParam(2, "Temperature/Humidity Reporting Interval", 2, 30, reportingIntervalOptions) 
}

private getPlaySoundParam() {
	return getParam(3, "Play Sound by Number", 1, 0)
}

private getParam(num, name, size, defaultVal, options=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}
	
	return map
}

private setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
}


private getAlarmDurationOptions() {
	def options = [:]
	[10,15,20,25,30,45].each {
		options["${it}"] = "${it} Seconds"
	}
	
	options["60"] = "1 Minute"
	options["90"] = "1 Minute 30 Seconds"
	
	(2..10).each {
		options["${it * 60}"] = "${it} Minutes"
	}
	return options
}

private getReportingIntervalOptions() {
	def options = ["1":"1 Minute"]
	
	[2,3,4,5,10,15,20,30,45].each {
		options["${it}"] = "${it} Minutes"
	}
	
	options["60"] = "1 Hour"
	
	[2,3,4,5,6,7,8,9,10,11,12,15,18,21].each {
		options["${it * 60}"] = "${it} Hours"		
	}
	
	options["1440"] = "1 Day"
	
	return options
}

private getSwitchOnActionOptions() {
	def options = [
		"0":"Do Nothing",
		"on": "Turn On Siren"	
	]
	
	(1..37).each {
		options["${it}"] = "Play Sound #${it}"
	}	
	return options
}

private getChimeSoundOptions() {
	def options = [:]	
	(1..37).each {
		options["${it}"] = "Sound #${it}"
	}	
	return options
}

private getChimeVolumeOptions() {
	def options = [:]
	[1,10,20,30,40,50,60,70,80,90,100].each {
		options["${convertToHex(it)}"] = "${it}%"
	}
	return options
}

private convertToHex(num) {
	return Integer.toHexString(num).padLeft(2, "0").toUpperCase()
}

private getTempOffsetOptions() {
	def options = [:]
	
	(-10..-1).each {
		options["${it}"] = "${it}°"
	}
	
	options["0"] = "No Offset"
	
	(1..10).each {
		options["${it}"] = "${it}°"
	}
	
	return options
}

private getHumidityOffsetOptions() {
	def options = [:]
	
	(-10..-1).each {
		options["${it}"] = "${it}%"
	}
	
	options["0"] = "No Offset"
	
	(1..10).each {
		options["${it}"] = "${it}%"
	}
	return options
}


private getEventMap(name, value, displayed=false, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} - ${name} is ${value}"
	]
	
	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText} ${unit}"
	}	
	return eventMap
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
}

private convertToLocalTimeString(dt) {	
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}