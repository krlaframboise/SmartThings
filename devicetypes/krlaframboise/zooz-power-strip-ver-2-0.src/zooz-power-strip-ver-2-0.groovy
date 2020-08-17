/**
 *  Zooz Power Strip VER 2.2.3
 *  (Models: ZEN20)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation: https://community.smartthings.com/t/release-zooz-power-strip-ver-2-0/138231?u=krlaframboise
 *
 *
 *  Changelog:
 *
 *    2.2.3 (08/16/2020)
 *      - Removed componentLabel and componentName from child outlet devices which fixes the timeout issue in the new mobile app.
 *
 *    2.2.2 (08/10/2020)
 *      - Added ST workaround for S2 Supervision bug with MultiChannel Devices.
 *
 *    2.2.1 (03/13/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    2.2 (08/25/2019)
 *      - Added new configuration parameters for firmware 2.2.
 *
 *    2.1.0 (11/05/2018)
 *      - Removed USB Child Device
 *      - Display actual outlet names and their power levels.
 *      - Configuration changes for firmware 2.0.
 *
 *    2.0.6 (10/16/2018)
 *      - Added new DTH for USB ports that doesn't display them as switches and works in the new mobile app.  If the new DTH isn't installed it will default to the SmartThings Virtual Switch which also works in the new mobile app.
 *
 *    2.0.5 (10/01/2018)
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
		name: "Zooz Power Strip VER 2.0", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch-power-energy"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"		
		capability "Outlet"
		// capability "Acceleration Sensor"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"		
		attribute "lastCheckin", "string"
		attribute "energyTime", "number"
		attribute "energyDuration", "string"
		attribute "powerLow", "number"
		attribute "powerHigh", "number"		
		attribute "usb1Switch", "string"
		attribute "usb2Switch", "string"
		
		(1..5).each {
			attribute "ch${it}Power", "number"
			attribute "ch${it}Switch", "string"
			attribute "ch${it}Name", "string"
			command "ch${it}On"
			command "ch${it}Off"
		}		
		
		command "reset"

		fingerprint manufacturer: "027A", prod: "A000", model: "A004", deviceJoinName: "Zooz Power Strip VER 2.0"
	}

	simulator { }
		
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
			}
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "energy", label:'${currentValue} kWh', backgroundColor: "#cccccc"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		valueTile("powerHigh", "device.powerHigh", width: 2, height: 1, decoration:"flat") {
			state "powerHigh", label:'High: ${currentValue} W'
		}
		valueTile("powerLow", "device.powerLow", width: 2, height: 1, decoration:"flat") {
			state "powerLow", label:'Low: ${currentValue} W'
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("reset", "device.reset", width: 2, height: 2) {
			state "default", label:'Reset', action: "reset", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure", icon:"st.secondary.tools"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:3, height: 1) {
			state "syncStatus", label:'${currentValue}'
		}
		
		valueTile("ch1Name", "device.ch1Name", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ch1Power", "device.ch1Power", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("ch1Switch", "device.ch1Switch", width:1, height: 1) {
			state "on", label:'ON', action:"ch1Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"ch1On"
		}
		valueTile("ch2Name", "device.ch2Name", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ch2Power", "device.ch2Power", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("ch2Switch", "device.ch2Switch", width:1, height: 1) {
			state "on", label:'ON', action:"ch2Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"ch2On"
		}		
		valueTile("ch3Name", "device.ch3Name", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ch3Power", "device.ch3Power", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("ch3Switch", "device.ch3Switch", width:1, height: 1) {
			state "on", label:'ON', action:"ch3Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"ch3On"
		}		
		valueTile("ch4Name", "device.ch4Name", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ch4Power", "device.ch4Power", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("ch4Switch", "device.ch4Switch", width:1, height: 1) {
			state "on", label:'ON', action:"ch4Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"ch4On"
		}		
		valueTile("ch5Name", "device.ch5Name", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ch5Power", "device.ch5Power", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("ch5Switch", "device.ch5Switch", width:1, height: 1) {
			state "on", label:'ON', action:"ch5Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"ch5On"
		}		
		valueTile("usb1Name", "generic", decoration:"flat", width:5, height: 1) {
			state "default", label:'USB 1 (READ-ONLY)'
		}
		valueTile("usb1Switch", "device.usb1Switch", decoration:"flat", width:1, height: 1) {
			state "on", label:'ON'
			state "off", label:'OFF'
		}
		valueTile("usb2Name", "generic", decoration:"flat", width:5, height: 1) {
			state "default", label:'USB 2 (READ-ONLY)'
		}
		valueTile("usb2Switch", "device.usb2Switch", decoration:"flat", width:1, height: 1) {
			state "on", label:'ON'
			state "off", label:'OFF'
		}
		
		main (["switch"])
		details(detailsTiles)
	}
	
	
	preferences {		
	
		input "mainSwitchDelay", "enum",
			title: "Main Switch Outlet Delay:",
			defaultValue: "0",
			required: false,
			options:mainSwitchDelayOptions
	
		getOptionsInput(manualControlParam)
		getOptionsInput(ledIndicatorModeParam)
		
		configParams.each {
			if (it.num != manualControlParam.num && it.num != ledIndicatorModeParam.num) {
				getOptionsInput(it)
			}
		}
		
		// input "inactivePower", "enum",
			// title: "Report Acceleration Inactive when Power is Below:",
			// defaultValue: "1.9",
			// required: false,
			// displayDuringSetup: true,
			// options: inactivePowerOptions
		
		// getBoolInput("displayAcceleration", "Display Acceleration in Secondary Status", false)

		["Power", "Energy"].each {
			getBoolInput("display${it}", "Display ${it} Activity", true)
		}
		
		getBoolInput("debugOutput", "Enable Debug Logging", true)
	}
}

private getDetailsTiles() {
	def tiles = ["switch", "energy", "power", "powerHigh", "powerLow", "refresh", "reset", "configure", "firmwareVersion", "syncStatus"]
	(1..5).each {
		tiles << "ch${it}Name"
		tiles << "ch${it}Power"
		tiles << "ch${it}Switch"
	}
	(1..2).each {
		tiles << "usb${it}Name"
		tiles << "usb${it}Switch"
	}
	return tiles
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
	sendEvent(name:"energyTime", value:new Date().time, displayed: false)
	
	// Make sure the outlets get created if using the new mobile app.
	runIn(30, createChildDevices) 
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time

		unschedule()
		
		runIn(2, updateSecondaryStatus)
		
		runEvery3Hours(ping)
		
		def cmds = []
		
		if (childDevices?.size() != 7) {
			cmds += createChildDevices()
		}
		
		cmds += configure()
		return cmds ? response(cmds) : []
	}	
}


def createChildDevices() {
	def cmds = []
	
	(1..5).each { endPoint ->
		if (!findChildByEndPoint(endPoint)) {			
			def dni = "${getChildDeviceNetworkId(endPoint)}"
			
			addChildOutlet(dni, endPoint)
			childUpdated(dni)
			
			cmds += childReset(dni)
		}
	}
	
	if (usbReportingEnabledParam.value != false) {
		(6..7).each { endPoint ->		
			def dni = "${getChildDeviceNetworkId(endPoint)}"		
			if (!findChildByDeviceNetworkId(dni)) {	
				addChildUSB("smartthings", "Virtual Switch", dni, endPoint)
				cmds << switchBinaryGetCmd(endPoint)
			}
		}
	}
	return cmds ? delayBetween(cmds, 1000) : []
}

private addChildOutlet(dni, endPoint) {
	logDebug "Creating CH${endPoint} Child Device"
	addChildDevice(
		"krlaframboise", 
		"Zooz Power Strip Outlet VER 2.0", 
		dni, 
		null, 
		[
			completedSetup: true,
			isComponent: false,
			label: "${device.displayName}-CH${endPoint}"
		]
	)
}
	
private addChildUSB(namespace, deviceType, dni, endPoint) {
	def usb = endPoint - 5
	logDebug "Creating USB${usb} Child Device"
	addChildDevice(
		namespace,
		deviceType,
		dni, 
		null, 
		[
			completedSetup: true,
			isComponent: true,
			label: "${device.displayName}-USB${usb}",
			componentName: "USB${usb}",
			componentLabel: "USB ${usb} (READ-ONLY)"
		]
	)
}


def configure() {	
	updateHealthCheckInterval()
	
	runIn(10, updateSyncStatus)
			
	def cmds = []
	def delay = 500

	cmds << versionGetCmd()
	cmds << "delay ${delay}"
	
	if (device.currentValue("power") == null) {
		cmds += getRefreshCmds()
		cmds << "delay ${delay}"
	}
	
	if (device.currentValue("energy") == null) {
		cmds += getResetCmds()
		cmds << "delay ${delay}"
	}
	
	if (device.currentValue("switch")) {
		cmds += getConfigureCmds()
	}
	
	return cmds
}

private updateHealthCheckInterval() {
	def minReportingInterval = (3 * 60 * 60)
	
	if (state.minReportingInterval != minReportingInterval) {
		state.minReportingInterval = minReportingInterval
			
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((minReportingInterval * 2) + (5 * 60))
		
		def eventMap = createEventMap("checkInterval", checkInterval, false)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}
}

private getConfigureCmds() {
	def cmds = []	
	
	if (state.resyncAll) {
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
	return cmds ? delayBetween(cmds, 250) : []
}


def ping() {
	logDebug "ping()..."
	return sendCommands([basicGetCmd()])
}


def on() {
	logDebug "on()..."
	return getAllSwitchCmds(0xFF)
}

def off() {
	logDebug "off()..."
	return getAllSwitchCmds(0x00)
}

private getAllSwitchCmds(value) {
	def cmds = []
	if (mainSwitchDelaySetting) {
		(1..5).each { endPoint ->
			cmds += [
				switchBinarySetCmd(value, endPoint),
				"delay 500",
				switchBinaryGetCmd(endPoint),
				"delay ${mainSwitchDelaySetting}"
			]			
		}
	}
	else {
		cmds += getChildSwitchCmds(value, null)
		cmds << "delay 2000"
		(5..1).each { endPoint ->
			cmds << "delay 2000"
			cmds << switchBinaryGetCmd(endPoint)
		}
	}	
	return cmds
}


def childUpdated(dni) {
	logDebug "childUpdated(${dni})"
	def child = findChildByDeviceNetworkId(dni)
	def endPoint = getEndPoint(dni)
	def nameAttr = "ch${endPoint}Name"
	if (child && "${child.displayName}" != "${device.currentValue(nameAttr)}") {
		sendEvent(name: nameAttr, value: child.displayName, displayed: false)
	}
}


def ch1On() { childOn(getChildDeviceNetworkId(1)) }
def ch2On() { childOn(getChildDeviceNetworkId(2)) }
def ch3On() { childOn(getChildDeviceNetworkId(3)) }
def ch4On() { childOn(getChildDeviceNetworkId(4)) }
def ch5On() { childOn(getChildDeviceNetworkId(5)) }

def childOn(dni) {
	logDebug "childOn(${dni})..."
	sendCommands(getChildSwitchCmds(0xFF, dni))
}


def ch1Off() { childOff(getChildDeviceNetworkId(1)) }
def ch2Off() { childOff(getChildDeviceNetworkId(2)) }
def ch3Off() { childOff(getChildDeviceNetworkId(3)) }
def ch4Off() { childOff(getChildDeviceNetworkId(4)) }
def ch5Off() { childOff(getChildDeviceNetworkId(5)) }

def childOff(dni) {
	logDebug "childOff(${dni})..."
	sendCommands(getChildSwitchCmds(0x00, dni))
}

private getChildSwitchCmds(value, dni) {
	def endPoint = getEndPoint(dni)	
	return delayBetween([
		switchBinarySetCmd(value, endPoint),
		switchBinaryGetCmd(endPoint)
	], 500)
}


def refresh() {
	logDebug "refresh()..."
	def cmds = getRefreshCmds()
	
	(6..7).each {
		cmds << "delay 250"
		cmds << switchBinaryGetCmd(it)
	}
		
	childDevices.each {
		def dni = it.deviceNetworkId
		def endPoint = getEndPoint(dni)		
		if (!isUsbEndPoint(endPoint)) {
			cmds << "delay 250"
			cmds += getRefreshCmds(dni)
			
			if (!device.currentValue("ch${endPoint}Name")) {
				childUpdated(dni)
			}
		}
	}
	return cmds	
}

def childRefresh(dni) {
	logDebug "childRefresh($dni)..."
	sendCommands(getRefreshCmds(dni))
}

private getRefreshCmds(dni=null) {
	def endPoint = getEndPoint(dni)
	delayBetween([ 
		switchBinaryGetCmd(endPoint),
		meterGetCmd(meterEnergy, endPoint),
		meterGetCmd(meterPower, endPoint)
	], 250)
}


def reset() {
	logDebug "reset()..."
	
	runIn(10, refresh)
	
	def cmds = getResetCmds()	
	childDevices.each { child ->
		cmds << "delay 1000"
		if (child.hasAttribute("power")) {
			cmds += getResetCmds(child.deviceNetworkId)
		}
	}
	return cmds		
}

def childReset(dni) {
	logDebug "childReset($dni)"
	
	def cmds = getResetCmds(dni)
	cmds << "delay 3000"
	cmds += getRefreshCmds(dni)
	sendCommands(cmds)
}

private getResetCmds(dni=null) {
	def endPoint = getEndPoint(dni)
	def child = findChildByDeviceNetworkId(dni)
	def power = getAttrVal("power", child) ?: 0
		
	executeSendEvent(child, createEventMap("powerLow", power, false))
	executeSendEvent(child, createEventMap("powerHigh", power, false))
	executeSendEvent(child, createEventMap("energyTime", new Date().time, false))
	
	return [meterResetCmd(endPoint)]
}


private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions, 100)
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private meterGetCmd(meter, endPoint) {
	return multiChannelCmdEncapCmd(zwave.meterV3.meterGet(scale: meter.scale), endPoint)
}

private meterResetCmd(endPoint) {
	return multiChannelCmdEncapCmd(zwave.meterV3.meterReset(), endPoint)
}

private switchBinaryGetCmd(endPoint) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endPoint)
}

private switchBinarySetCmd(val, endPoint) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val), endPoint)
}

private multiChannelCmdEncapCmd(cmd, endPoint) {	
	return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endPoint).encapsulate(cmd))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
	if (securityEnabled) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}


private getSecurityEnabled() {
	try {
		return (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" ")))
	}
	catch (e) {
		// zwaveInfo throws exception if device had to be manually created.
	}
}


private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x32: 3,	// Meter v4
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x60: 3,	// Multi Channel (4)
		0x6C: 1,	// Supervision
		0x70: 2,	// Configuration
		0x71: 3,	// Notification
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {	
	def result = []
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)		
		}
		else {
			log.warn "Unable to parse: $description"
		}
			
		if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
			state.lastCheckinTime = new Date().time
			sendEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
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
	// Workaround that was added to all SmartThings Multichannel DTHs.
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	
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
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	
	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)
	}
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	state.configured = true
	
	updateSyncStatus("Syncing...")
	runIn(10, updateSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)				
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
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
	if ("${syncStatus}" != "${status}") {
		executeSendEvent(null, createEventMap("syncStatus", status, false))		
	}
}

private getSyncStatus() {
	return device.currentValue("syncStatus")
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


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endPoint=0) {
	logTrace "SwitchBinaryReport: ${cmd} (CH${endPoint})"
	
	def value = (cmd.value == 0xFF) ? "on" : "off"
	
	executeSendEvent(findChildByEndPoint(endPoint), createEventMap("switch", value))
	
	def switchName = isUsbEndPoint(endPoint) ? "usb${endPoint - 5}Switch" : "ch${endPoint}Switch"
	sendEvent(name: switchName, value:value, displayed: false)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=0) {
	logTrace "BasicReport: ${cmd} (CH${endPoint})"
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, endPoint=0) {
	def val = roundTwoPlaces(cmd.scaledMeterValue)
	def child = findChildByEndPoint(endPoint)	
	
	switch (cmd.scale) {
		case meterEnergy.scale:			
			sendEnergyEvents(child, val)
			break
		case meterPower.scale:
			sendPowerEvents(child, val)
			sendEvent(name: "ch${endPoint}Power", value: val, unit:"w", displayed: false)
			break
		default:
			logDebug "Unknown Meter Scale: $cmd"
	}
	
	runIn(2, updateSecondaryStatus)
	return []
}


private sendPowerEvents(child, value) {
	def highLowNames = [] 
	
	executeSendEvent(child, createEventMap("power", value, meterPower.displayed, meterPower.unit))
	
	// sendAccelerationEvent(child, value)
		
	if (getAttrVal("powerHigh", child) == null || value > getAttrVal("powerHigh", child)) {
		highLowNames << "powerHigh"
	}
	
	if (getAttrVal("powerLow", child) == null || value < getAttrVal("powerLow", child)) {
		highLowNames << "powerLow"
	}
	
	highLowNames.each {
		executeSendEvent(child, createEventMap("$it", value, false, meterPower.unit))
	}	
}

// private sendAccelerationEvent(child, value) {
	// def status
	
	// def deviceActive = (getAttrVal("acceleration", child) == "active")
	// if (value >= inactivePowerSetting &&  !deviceActive) {
		// status ="active"
	// }
	// else if (value < inactivePowerSetting && deviceActive){
		// status = "inactive"
	// }
	// else if (!getAttrVal("acceleration", child)) {
		// status = "inactive"
	// }
	
	// if (status) {
		// executeSendEvent(child, createEventMap("acceleration", status, false))
	// }
// }


private sendEnergyEvents(child, value) {
	executeSendEvent(child, createEventMap("energy", value, meterEnergy.displayed, meterEnergy.unit))
	
	executeSendEvent(child, createEventMap("energyDuration", calculateEnergyDuration(child), false))
}

private calculateEnergyDuration(child) {
	def energyTimeMS = getAttrVal("energyTime", child)
	if (!energyTimeMS) {
		return "Unknown"
	}
	else {
		def duration = roundTwoPlaces((new Date().time - energyTimeMS) / 60000)
		
		if (duration >= (24 * 60)) {
			return getFormattedDuration(duration, (24 * 60), "Day")
		}
		else if (duration >= 60) {
			return getFormattedDuration(duration, 60, "Hour")
		}
		else {
			return getFormattedDuration(duration, 0, "Minute")
		}
	}
}

private getFormattedDuration(duration, divisor, name) {
	if (divisor) {
		duration = roundTwoPlaces(duration / divisor)
	}	
	return "${duration} ${name}${duration == 1 ? '' : 's'}"
}


def updateSecondaryStatus() {
	(0..5).each { endPoint ->	
		def child = findChildByEndPoint(endPoint)
		def power = getAttrVal("power", child) ?: 0
		def energy = getAttrVal("energy", child) ?: 0
		def duration = getAttrVal("energyDuration", child) ?: ""
		// def active = getAttrVal("acceleration", child) ?: "inactive"
		
		if (duration) {
			duration = " - ${duration}"
		}
		
		def status = ""
		
		// status = settings?.displayAcceleration ? "${active.toUpperCase()} / " : ""
		
		status =  "${status}${power} ${meterPower.unit} / ${energy} ${meterEnergy.unit}${duration}"
		
		if (getAttrVal("secondaryStatus", child) != "${status}") {
			executeSendEvent(child, createEventMap("secondaryStatus", status, false))
		}
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


// Meters
private getMeterEnergy() { 
	return getMeterMap("energy", 0, "kWh", settings?.displayEnergy != false) 
}

private getMeterPower() { 
	return getMeterMap("power", 2, "W", settings?.displayPower != false)
}

private getMeterMap(name, scale, unit, displayed) {
	return [name:name, scale:scale, unit:unit, displayed:displayed]
}

private getUsbPort1EndPoint() { return 6 }
private getUsbPort2EndPoint() { return 7 }


// Configuration Parameters
private getConfigParams() {
	def params = [
		powerFailureRecoveryParam,
		powerReportingThresholdParam,
		powerReportingFrequencyParam,
		energyReportingFrequencyParam,
		overloadProtectionParam,
		manualControlParam,
		ledIndicatorModeParam
	]

	params += autoOffEnabledParams
	params += autoOffIntervalParams
	params += autoOnEnabledParams
	params += autoOnIntervalParams
	
	if (isSupportedFirmware(2.2)) {
		params += meterReportingEnabledParams
		params << usbReportingEnabledParam
	}

	return params?.sort { it.num }
}

private getPowerFailureRecoveryParam() {
	return getParam(1, "On/Off Status Recovery After Power Failure", 1, 0, [0:"Restore Outlets States From Before Power Faiure", 1:"Turn Outlets On", 2:"Turn Outlets Off"])
}

private getPowerReportingThresholdParam() {
	def size = isOriginalFirmware() ? 2 : 4
	return getParam(2, "Power Reporting Threshold", size, 5, powerReportingThresholdOptions) 
}

private getPowerReportingFrequencyParam() {
	return getParam(3, "Power Reporting Frequency", 4, 300, frequencyOptions) 
}

private getEnergyReportingFrequencyParam() {
	return getParam(4, "Energy Reporting Frequency", 4, 300, frequencyOptions) 
}

private getOverloadProtectionParam() {
	def defaultVal = isOriginalFirmware() ? 1500 : 1800
	return getParam(5, "Overload Protection", 2, defaultVal, overloadOptions) 
}

private getAutoOffEnabledParams() {
	def params = []
	def ch = 1
	[6, 10, 14, 18, 22].each {
		params << getParam(it, "CH${ch} Auto Turn-Off Timer Enabled", 1, 0, enabledOptions) 
		ch += 1
	}	
	return params
}

private getAutoOffIntervalParams() {
	def params = []
	def ch = 1
	[7, 11, 15, 19, 23].each {
		params << getParam(it, "CH${ch} Auto Turn-Off After", 4, 60, autoOnOffIntervalOptions)
		ch += 1
	}	
	return params
}

private getAutoOnEnabledParams() {
	def params = []
	def ch = 1
	[8, 12, 16, 20, 24].each {
		params << getParam(it, "CH${ch} Auto Turn-On Timer Enabled", 1, 0, enabledOptions) 
		ch += 1
	}	
	return params
}

private getAutoOnIntervalParams() {
	def params = []
	def ch = 1
	[9, 13, 17, 21, 25].each {
		params << getParam(it, "CH${ch} Auto Turn-On After", 4, 60, autoOnOffIntervalOptions)
		ch += 1
	}	
	return params
}

private getManualControlParam() {
	return getParam(26, "Manual Control", 1, 1, enabledOptions)
}

private getLedIndicatorModeParam() {
	return getParam(27, "LED Indicator Mode", 1, 1, [0:"LED On When Switch Off", 1:"LED On When Switch On", 2:"LED Always Off"])
}

private getMeterReportingEnabledParams() {
	def params = []
	
	params << getParam(28, "Meter Reporting Enabled (FIRWARE >= 2.2)", 1, 1, enabledOptions)
	
	def ch = 1
	(29..33).each {
		params << getParam(it, "CH${ch} Meter Reporting Enabled (FIRWARE >= 2.2)", 1, 1, enabledOptions)
		ch += 1
	}	
	return params
}

private getUsbReportingEnabledParam() {
	return getParam(34, "USB Reporting Enabled (FIRWARE >= 2.2)", 1, 1, enabledOptions)
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


private getOverloadOptions() {
	def options = [0:"Disabled (Not Recommended)"]
	options += powerOptions
	return options
}

private getPowerReportingThresholdOptions() {
	def options = [0:"Disabled"]
	options += powerOptions
	return options
}

private getPowerOptions() {
	def options = [:]
	[1,2,3,4,5,10,15,20,25,50,75,100,150,200,250,500,750,1000,1250,1500].each {
		options["${it}"] = "${it} W"
	}		
	if (!isOriginalFirmware()) {
		options["1800"] = "1800 W"
	}
	return options
}

private getFrequencyOptions() {
	def options = [:]
	options = getTimeOptionsRange(options, "Second", 1, [5,10,15,30,45])
	options = getTimeOptionsRange(options, "Minute", 60, [1,2,3,4,5,10,15,30,45])
	options = getTimeOptionsRange(options, "Hour", (60 * 60), [1,2,3,6,9,12,24])
	return options
}

private getAutoOnOffIntervalOptions() {
	def options = [:]
	options = getTimeOptionsRange(options, "Minute", 1, [1,2,3,4,5,6,7,8,9,10,15,20,25,30,45])
	options = getTimeOptionsRange(options, "Hour", 60, [1,2,3,4,5,6,7,8,9,10,12,18])
	options = getTimeOptionsRange(options, "Day", (60 * 24), [1,2,3,4,5,6])
	options = getTimeOptionsRange(options, "Week", (60 * 24 * 7), [1,2])
	return options
}

private getMainSwitchDelayOptions() {
	def options = [0:"Disabled",500:"500 Milliseconds"]
	options = getTimeOptionsRange(options, "Second", 1000, [1,2,3,4,5,10])
	return setDefaultOption(options, 0)
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

// private getInactivePowerOptions() {
	// def options = [:]
	// [1.9,2,2.1,2.2,2.3,2.4,2.5,2.75,3,3.5,4,4.5,5,7.5,10,15,25,50,75,100,150,200,250,500,1000,1500,2000].each {
		// options[it] = "${it} W"
	// }
	// return setDefaultOption(options, 1.9)
// }


// Settings
private getMainSwitchDelaySetting() {
	return safeToInt(settings?.mainSwitchDelay)
}

// private getInactivePowerSetting() {
	// return safeToDec(settings?.inactivePower) ?: 1.9
// }


private executeSendEvent(child, evt) {
	if (evt.displayed == null) {
		evt.displayed = (getAttrVal(evt.name, child) != evt.value)
	}

	if (evt) {
		if (child) {
			if (evt.descriptionText) {
				evt.descriptionText = evt.descriptionText.replace(device.displayName, child.displayName)
				logDebug "${evt.descriptionText}"
			}
			child.sendEvent(evt)						
		}
		else {
			sendEvent(evt)
		}
	}
}

private createEventMap(name, value, displayed=null, unit=null) {	
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

private getAttrVal(attrName, child=null) {
	try {
		if (child) {
			return child?.currentValue("${attrName}")
		}
		else {
			return device?.currentValue("${attrName}")
		}
	}
	catch (ex) {
		logTrace "$ex"
		return null
	}
}

private findChildByEndPoint(endPoint) {
	def dni = getChildDeviceNetworkId(endPoint)
	return findChildByDeviceNetworkId(dni)
}

private findChildByDeviceNetworkId(dni) {
	return childDevices?.find { it.deviceNetworkId == dni }
}

private getEndPoint(childDeviceNetworkId) {
	return safeToInt("${childDeviceNetworkId}".reverse().take(1))
}

private getChildDeviceNetworkId(endPoint) {
	if (isUsbEndPoint(endPoint)) {
		return "${device.deviceNetworkId}-USB${endPoint - 5}"		
	}
	else {
		return "${device.deviceNetworkId}-CH${endPoint}"
	}
}

private isUsbEndPoint(endPoint) {
	return endPoint > 5
}

private isOriginalFirmware() {
	def fw = device?.currentValue("firmwareVersion")
	return "${fw}" == "1.0"
}

private isSupportedFirmware(minFirmware) {
	def fw = device?.currentValue("firmwareVersion")
	return fw ? safeToDec(fw) >= minFirmware : true
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0.0) {
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