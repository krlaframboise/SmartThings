/**
 *  Zooz MultiRelay v1.0
 *  (Models: ZEN16)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation: https://community.smartthings.com/t/release-zooz-multirelay-zen16/181057
 *
 *  Changelog:
 *
 *    1.0 (12/19/2019)
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
		name: "Zooz MultiRelay",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise",
		vid:"generic-switch"
	) {
		capability "Actuator"
		capability "Switch"
		capability "Outlet"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		attribute "firmwareVersion", "string"
		attribute "lastCheckIn", "string"

		(1..3).each {
			attribute "relay${it}Switch", "string"
			attribute "relay${it}Name", "string"

			command "relay${it}On"
			command "relay${it}Off"
		}

		fingerprint manufacturer: "027A", prod: "A000", model: "A00A", deviceJoinName: "Zooz MultiRelay"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure", icon:"st.secondary.tools"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}

		valueTile("relay1Name", "device.relay1Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay1Switch", "device.relay1Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay1Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay1On"
		}

		valueTile("relay2Name", "device.relay2Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay2Switch", "device.relay2Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay2Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay2On"
		}

		valueTile("relay3Name", "device.relay3Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay3Switch", "device.relay3Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay3Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay3On"
		}


		main (["switch"])
		details(["switch", "refresh", "syncStatus", "configure", "relay1Name", "relay1Switch", "relay2Name", "relay2Switch", "relay3Name", "relay3Switch", "firmwareVersion"])
	}

	preferences {

		getBoolInput("createRelay1", "Create Child Switch for Relay 1", false)
		getBoolInput("createRelay2", "Create Child Switch for Relay 2", false)
		getBoolInput("createRelay3", "Create Child Switch for Relay 3", false)

		configParams.each {
			getOptionsInput(it)
		}

		getBoolInput("debugOutput", "Enable Debug Logging", true)
	}
}


private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.value}",
		displayDuringSetup: true,
		options: param.options
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool",
		title: "${title}?",
		defaultValue: defaultVal,
		required: false
}


def installed () {
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time

		initialize()

		refreshChildSwitches()

		def cmds = getConfigureCmds()
		return cmds ? response(cmds) : []
	}
}

private initialize() {
	if (!device.currentValue("switch")) {
		sendEvent(name: "switch", value: "off", displayed: false)
	}

	(1..3).each {
		if (!device.currentValue("relay${it}Switch")) {
			sendEvent(name: "relay${it}Switch", value: "off", displayed: false)
			sendEvent(name: "relay${it}Name", value: "Relay ${it}", displayed: false)
		}
	}

	if (!device.currentValue("checkInterval")) {
		def checkInterval = (6 * 60 * 60) + (5 * 60)
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	unschedule()

	runEvery3Hours(ping)
}

private refreshChildSwitches() {
	if (settings) {
		(1..3).each {
			def child = findChildByEndpoint(it)
			if (child && !settings["createRelay${it}"]) {
				log.warn "Removing ${child.displayName}} "
				deleteChildDevice(child.deviceNetworkId)
				child = null
			}
			else if (!child && settings["createRelay${it}"]) {
				child = addChildSwitch(it)
				child?.sendEvent(getEventMap("switch", device.currentValue("relay${it}Switch"), false))
			}

			def relayName = child ? child.displayName : "Relay ${it}"
			if (relayName != device.currentValue("relay${it}Name")) {
				sendEvent(getEventMap("relay${it}Name", relayName, false))
			}
		}
	}
}

private addChildSwitch(endpoint) {
	def name = "Relay ${endpoint}"

	logDebug "Creating Child Switch for ${name}"

	return addChildDevice(
		"smartthings",
		"Child Switch",
		getChildDNI(endpoint),
		device.getHub().getId(),
		[
			completedSetup: true,
			isComponent: false,
			label: "${device.displayName}-${name}",
			componentLabel: "${name}",
			componentName: "${name}",
			data: [endpoint: "${endpoint}"]
		]
	)
}


def configure() {
	runIn(10, updateSyncStatus)

	if (!pendingChanges) {
		state.resyncAll = true
	}

	return getConfigureCmds()
}

private getConfigureCmds() {
	def cmds = []

	if (state.resyncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	configParams.each {
		def storedVal = getParamStoredValue(it.num)
		if (state.resyncAll || "${storedVal}" != "${it.value}") {
			if (state.configured) {
				logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
				cmds << configSetCmd(it)
			}
			cmds << configGetCmd(it)
		}
	}
	return cmds ? delayBetween(cmds, 500) : []
}


def ping() {
	logDebug "ping()..."
	return sendCommands([ switchBinaryGetCmd() ])
}


def on() {
	logDebug "on()..."
	return [ switchBinarySetCmd(0xFF) ]
}


def off() {
	logDebug "off()..."
	return [ switchBinarySetCmd(0x00) ]
}


def relay1On() { relayOn(1) }
def relay2On() { relayOn(2) }
def relay3On() { relayOn(3) }

private relayOn(endpoint) {
	logDebug "relay${endpoint}On()..."
	executeChildOnOff(0xFF, endpoint)
}

def childOn(dni) {
	logDebug "childOn(${dni})..."
	executeChildOnOff(0xFF, getChildEndpoint(findChildByDNI(dni)))
}


def relay1Off() { relayOff(1) }
def relay2Off() { relayOff(2) }
def relay3Off() { relayOff(3) }

private relayOff(endpoint) {
	logDebug "relay${endpoint}Off()..."
	executeChildOnOff(0x00, endpoint)
}

def childOff(dni) {
	logDebug "childOff(${dni})..."
	executeChildOnOff(0x00, getChildEndpoint(findChildByDNI(dni)))
}

private executeChildOnOff(value, endpoint) {
	sendCommands([ switchBinarySetCmd(value, endpoint) ])
}


def refresh() {
	logDebug "refresh()..."

	refreshChildSwitches()

	def cmds = []
	(0..3).each {
		cmds << basicGetCmd(it)
	}
	sendCommands(delayBetween(cmds, 250))
	return []
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions)
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd(endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endpoint)
}

private switchBinaryGetCmd(endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
}

private switchBinarySetCmd(val, endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val), endpoint)
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private multiChannelCmdEncapCmd(cmd, endpoint) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
	}
	else {
		return secureCmd(cmd)
	}
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}


private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x55: 1,	// Transport Service
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x60: 3,	// Multi Channel
		0x6C: 1,	// Supervision
		0x70: 2,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x85: 2,	// Association
		0x86: 1,	// Version
		0x8E: 2,	// Multi Channel Association
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {
	def result = []
	try {
		if (!"${description}".contains("command: 5E02")) {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)
			}
			else {
				log.warn "Unable to parse: $description"
			}
		}

		if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
			state.lastCheckInTime = new Date().time
			sendEvent(getEventMap("lastCheckIn", convertToLocalTimeString(new Date()), false))
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


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)

	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"

	def subVersion = String.format("%02d", cmd.applicationSubVersion)
	def fullVersion = "${cmd.applicationVersion}.${subVersion}"

	if (fullVersion != device.currentValue("firmwareVersion")) {
		sendEvent(getEventMap("firmwareVersion", fullVersion))
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	state.configured = true

	updateSyncStatus("Syncing...")
	runIn(10, updateSyncStatus)

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
	state.resyncAll = false
	return []
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
	if (device.currentValue("syncStatus") != status) {
		sendEvent(getEventMap("syncStatus", status, false))
	}
}

private getPendingChanges() {
	return (configParams.count { isConfigParamSynced(it) ? 0 : 1 })
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


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=0) {
	logTrace "SwitchBinaryReport: ${cmd}" + (endpoint ? " (Endpoint ${endpoint})" : "")
	// Using for ping
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	logTrace "BasicReport: ${cmd}" + (endpoint ? " (Endpoint ${endpoint})" : "")

	def value = (cmd.value == 0xFF) ? "on" : "off"

	if (endpoint) {
		def child = findChildByEndpoint(endpoint)
		if (child) {
			def desc = "${child.displayName}: switch is ${value}"
			logDebug "${desc}"
			child.sendEvent(name: "switch", value: value, descriptionText: desc)
		}

		sendEvent(getEventMap("relay${endpoint}Switch", value, !child))
	}
	else {
		sendEvent(getEventMap("switch", value))
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=null) {
	logDebug "Unhandled zwaveEvent: $cmd" + (endpoint ? " (Endpoint ${endpoint})" : "")
	return []
}


// Configuration Parameters
private getConfigParams() {
	return [
		powerFailureRecoveryParam,
		relay1TypeParam,
		relay2TypeParam,
		relay3TypeParam,
		ledIndicatorModeParam,
		relay1AutoOffParam,
		relay1AutoOnParam,
		relay2AutoOffParam,
		relay2AutoOnParam,
		relay3AutoOffParam,
		relay3AutoOnParam,
		relay1ManualControlParam,
		relay2ManualControlParam,
		relay3ManualControlParam
	]
}

private getPowerFailureRecoveryParam() {
	def options = [
		0:"Turn All Relays Off",
		1:"Restore Relay States From Before Power Failure",
		2:"Turn All Relays On",
		3:"Restore Relay 1 and Relay 2 States and Turn Relay 3 Off",
		4:"Restore Relay 1 and Relay 2 States and Turn Relay 3 On"
	]
	return getParam(1, "On/Off Status Recovery After Power Failure", 1, 1, options)
}

private getRelay1TypeParam() {
	return getParam(2, "Switch Type for Relay 1", 1, 2, relayTypeOptions)
}

private getRelay2TypeParam() {
	return getParam(3, "Switch Type for Relay 2", 1, 2, relayTypeOptions)
}

private getRelay3TypeParam() {
	return getParam(4, "Switch Type for Relay 3", 1, 2, relayTypeOptions)
}

private getLedIndicatorModeParam() {
	def options = [
		0:"On when ALL Relays are Off", 1:"On when ANY Relay is On",
		2:"Always Off",
		3:"Always On"
	]
	return getParam(5, "LED Indicator Control", 1, 0, options)
}

private getRelay1AutoOffParam() {
	return getParam(6, "Auto Turn-Off Timer for Relay 1", 4, 0, autoOnOffOptions)
}

private getRelay1AutoOnParam() {
	return getParam(7, "Auto Turn-On Timer for Relay 1", 4, 0, autoOnOffOptions)
}

private getRelay2AutoOffParam() {
	return getParam(8, "Auto Turn-Off Timer for Relay 2", 4, 0, autoOnOffOptions)
}

private getRelay2AutoOnParam() {
	return getParam(9, "Auto Turn-On Timer for Relay 2", 4, 0, autoOnOffOptions)
}

private getRelay3AutoOffParam() {
	return getParam(10, "Auto Turn-Off Timer for Relay 3", 4, 0, autoOnOffOptions)
}

private getRelay3AutoOnParam() {
	return getParam(11, "Auto Turn-On Timer for Relay 3", 4, 0, autoOnOffOptions)
}

private getRelay1ManualControlParam() {
	return getParam(12, "Enable/Disable Manual Control for Relay 1", 1, 1, enabledOptions)
}

private getRelay2ManualControlParam() {
	return getParam(13, "Enable/Disable Manual Control for Relay 2", 1, 1, enabledOptions)
}

private getRelay3ManualControlParam() {
	return getParam(14, "Enable/Disable Manual Control for Relay 3", 1, 1, enabledOptions)
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
	return options?.collect { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


private getRelayTypeOptions() {
	return [
		0:"Momentary Switch",
		1:"Toggle Switch",
		2:"Toggle Switch (any change)"
	]
}

private getAutoOnOffOptions() {
	def options = [0:"Disabled"]
	options = getTimeOptionsRange(options, "Minute", 1, [1,2,3,4,5,6,7,8,9,10,15,20,25,30,45])
	options = getTimeOptionsRange(options, "Hour", 60, [1,2,3,4,5,6,7,8,9,10,12,18])
	options = getTimeOptionsRange(options, "Day", (60 * 24), [1,2,3,4,5,6])
	options = getTimeOptionsRange(options, "Week", (60 * 24 * 7), [1,2])
	return options
}

private getTimeOptionsRange(options, name, multiplier, range) {
	range?.each {
		options["${(it * multiplier)}"] = "${it} ${name}${it == 1 ? '' : 's'}"
	}
	return options
}

private getEnabledOptions() {
	return [0:"Disabled", 1:"Enabled"]
}


private getEventMap(name, value, displayed=true) {
	def desc = "${device.displayName}: ${name} is ${value}"

	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		descriptionText: "${desc}"
	]

	if (displayed) {
		logDebug "${desc}"
	}
	else {
		logTrace "${desc}"
	}
	return eventMap
}


private findChildByEndpoint(endpoint) {
	return childDevices?.find { getChildEndpoint(it) == endpoint }
}

private findChildByDNI(dni) {
	return childDevices?.find { it.deviceNetworkId == dni }
}

private getChildEndpoint(child) {
	return child ? safeToInt(child.getDataValue("endpoint")) : 0
}

private getChildDNI(endpoint) {
	return "${device.deviceNetworkId}-EP${endpoint}"
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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