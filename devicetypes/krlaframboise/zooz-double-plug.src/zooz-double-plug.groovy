/**
 *  Zooz Double Plug v1.2.5
 *  (Models: ZEN25)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.2.5 (08/16/2020)
 *      - Removed componentLabel and componentName from child outlet devices which fixes the timeout issue in the new mobile app.
 *
 *    1.2.4 (08/10/2020)
 *      - Added ST workaround for S2 Supervision bug with MultiChannel Devices.
 *
 *    1.2.3 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.2.2 (02/03/2019)
 *      - Fixed unit on Power Threshold setting.
 *
 *    1.2 (01/31/2019)
 *      - Changed USB tile to standardTile
 *
 *    1.1 (01/26/2019)
 *      - Fixed typo
 *      - Stopped forcing isStateChange to true so that only events that duplicate events aren't shown.
 *      - Fixed config parameter options.
 *      - Fixed other misc issues.
 *
 *    1.0 (01/21/2019)
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
		name: "Zooz Double Plug", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch-power-energy"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"		
		capability "Outlet"
		capability "Power Meter"
		capability "Voltage Measurement"
		capability "Energy Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"		
		attribute "lastCheckin", "string"
		attribute "energyTime", "number"
		attribute "current", "number"
		attribute "energyDuration", "string"
		attribute "usbSwitch", "string"
		
		["power", "voltage", "current"].each {
			attribute "${it}Low", "number"
			attribute "${it}High", "number"
		}
		
		attribute "leftPower", "number"
		attribute "leftSwitch", "string"
		attribute "leftName", "string"
		attribute "rightPower", "number"
		attribute "rightSwitch", "string"
		attribute "rightName", "string"
		attribute "usbSwitch", "string"
						
		command "leftOff"
		command "leftOn"
		command "rightOff"
		command "rightOn"		
		command "reset"

		fingerprint manufacturer: "027A", prod: "A000", model: "A003", deviceJoinName: "Zooz Double Plug"
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
		valueTile("energy", "device.energy", width: 2, height: 1) {
			state "energy", label:'Energy: ${currentValue} kWh', decoration:"flat"
		}
		valueTile("energyDuration", "device.energyDuration", width: 4, height: 1) {
			state "energyDuration", label:'${currentValue}', decoration:"flat"
		}
		valueTile("power", "device.power", width: 2, height: 1) {
			state "power", label:'Power: ${currentValue} W', decoration:"flat"
		}
		valueTile("powerHigh", "device.powerHigh", width: 2, height: 1, decoration:"flat") {
			state "powerHigh", label:'High: ${currentValue} W'
		}
		valueTile("powerLow", "device.powerLow", width: 2, height: 1, decoration:"flat") {
			state "powerLow", label:'Low: ${currentValue} W'
		}		
		valueTile("voltage", "device.voltage", width: 2, height: 1) {
			state "voltage", label:'Voltage: ${currentValue} V', decoration:"flat"
		}
		valueTile("voltageHigh", "device.voltageHigh", width: 2, height: 1, decoration:"flat") {
			state "voltageHigh", label:'High: ${currentValue} V'
		}
		valueTile("voltageLow", "device.voltageLow", width: 2, height: 1, decoration:"flat") {
			state "voltageLow", label:'Low: ${currentValue} V'
		}		
		valueTile("current", "device.current", width: 2, height: 1) {
			state "current", label:'Current: ${currentValue} A', decoration:"flat"
		}
		valueTile("currentHigh", "device.currentHigh", width: 2, height: 1, decoration:"flat") {
			state "currentHigh", label:'High: ${currentValue} A'
		}
		valueTile("currentLow", "device.currentLow", width: 2, height: 1, decoration:"flat") {
			state "currentLow", label:'Low: ${currentValue} A'
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
		
		valueTile("leftName", "device.leftName", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("leftPower", "device.leftPower", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("leftSwitch", "device.leftSwitch", width:1, height: 1) {
			state "on", label:'ON', action:"leftOff", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"leftOn"
		}
		valueTile("rightName", "device.rightName", decoration:"flat", width:4, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("rightPower", "device.rightPower", decoration:"flat", width:1, height: 1) {
			state "default", label:'${currentValue} W'
		}
		standardTile("rightSwitch", "device.rightSwitch", width:1, height: 1) {
			state "on", label:'ON', action:"rightOff", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"rightOn"
		}		
		valueTile("usbName", "generic", decoration:"flat", width:5, height: 1) {
			state "default", label:'USB (READ-ONLY)'
		}
		standardTile("usbSwitch", "device.usbSwitch", width:1, height: 1) {
			state "on", label:'ON', backgroundColor: "#00a0dc"
			state "off", label:'OFF'
		}
				
		main (["switch"])
		details(["switch", "energy", "energyDuration", "power", "powerHigh", "powerLow", "voltage", "voltageHigh", "voltageLow", "current", "currentHigh", "currentLow", "refresh", "reset", "configure", "firmwareVersion", "syncStatus", "leftName", "leftPower", "leftSwitch", "rightName", "rightPower", "rightSwitch", "usbName", "usbSwitch"])
	}
	
	preferences {
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
	sendEvent(name:"energyTime", value:new Date().time, displayed: false)
	
	// Make sure the outlets get created if using the new mobile app.
	runIn(10, createChildDevices) 
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		unschedule()
		
		runEvery3Hours(ping)
		
		if (childDevices?.size() != 3) {
			runIn(2, createChildDevices)
		}
		
		def cmds = getConfigureCmds()
		return cmds ? response(cmds) : []
	}	
}


def createChildDevices() {
	(1..2).each { endPoint ->
		if (!findChildByEndPoint(endPoint)) {			
			def dni = "${getChildDeviceNetworkId(endPoint)}"
			
			addChildOutlet(dni, endPoint)
			childUpdated(dni)
			
			sendCommands(childReset(dni))
		}
	}
	
	def dni = "${getChildDeviceNetworkId(3)}"
	if (!findChildByDeviceNetworkId(dni)) {	
		addChildUSB("smartthings", "Virtual Switch", dni)
		sendCommands([switchBinaryGetCmd(3)])
	}	
}

private addChildOutlet(dni, endPoint) {
	def name = getEndPointName(endPoint)?.toUpperCase()
	
	logDebug "Creating ${name} Outlet Child Device"	
	addChildDevice(
		"krlaframboise", 
		"Zooz Double Plug Outlet", 
		dni, 
		device.getHub().getId(), 
		[
			completedSetup: true,
			isComponent: false,
			label: "${device.displayName}-${name}"
		]
	)
}
	
private addChildUSB(namespace, deviceType, dni) {
	logDebug "Creating USB Child Device"
	addChildDevice(
		namespace,
		deviceType,
		dni, 
		device.getHub().getId(), 
		[
			completedSetup: true,
			isComponent: true,
			label: "${device.displayName}-USB",
			componentName: "USB",
			componentLabel: "USB (READ-ONLY)"
		]
	)
}


def configure() {	
	updateHealthCheckInterval()
	
	runIn(10, updateSyncStatus)
	
	if (!pendingChanges) {
		state.resyncAll = true
	}
			
	def cmds = []
		
	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
		cmds << "delay 500"
	}
	
	if (device.currentValue("power") == null) {
		cmds += getRefreshCmds()
		cmds << "delay 500"
	}
	
	if (device.currentValue("energy") == null) {
		cmds += getResetCmds()
		cmds << "delay 500"
	}
	
	cmds += getConfigureCmds()
	
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
	return cmds ? delayBetween(cmds, 500) : []
}


def ping() {
	logDebug "ping()..."
	return sendCommands([basicGetCmd()])
}


def on() {
	logDebug "on()..."
	return getChildSwitchCmds(0xFF, null)
}

def off() {
	logDebug "off()..."
	return getChildSwitchCmds(0x00, null)
}


def childUpdated(dni) {
	logDebug "childUpdated(${dni})"
	def child = findChildByDeviceNetworkId(dni)
	def endPoint = getEndPoint(dni)
	def endPointName = getEndPointName(endPoint)	
	def nameAttr = "${endPointName}Name"
	
	if (child && "${child.displayName}" != "${device.currentValue(nameAttr)}") {
		sendEvent(name: nameAttr, value: child.displayName, displayed: false)
	}
}


def leftOn() { childOn(getChildDeviceNetworkId(1)) }
def rightOn() { childOn(getChildDeviceNetworkId(2)) }

def childOn(dni) {
	logDebug "childOn(${dni})..."
	sendCommands(getChildSwitchCmds(0xFF, dni))
}


def leftOff() { childOff(getChildDeviceNetworkId(1)) }
def rightOff() { childOff(getChildDeviceNetworkId(2)) }

def childOff(dni) {
	logDebug "childOff(${dni})..."
	sendCommands(getChildSwitchCmds(0x00, dni))
}

private getChildSwitchCmds(value, dni) {
	def endPoint = getEndPoint(dni)	
	return delayBetween([
		switchBinarySetCmd(value, endPoint)
		// switchBinaryGetCmd(endPoint)
	], 500)
}


def refresh() {
	logDebug "refresh()..."
	def cmds = getRefreshCmds()
	
	childDevices.each {
		def dni = it.deviceNetworkId
		def endPoint = getEndPoint(dni)
		def endPointName = getEndPointName(endPoint)		
		
		cmds << "delay 250"
		if (endPointName == "usb") {
			cmds << switchBinaryGetCmd(endPoint)
		}
		else {
			cmds += getRefreshCmds(dni)
			if (!device.currentValue("${endPointName}Name")) {
				childUpdated(dni)
			}
		}
	}
	sendCommands(cmds)
	return []
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
		meterGetCmd(meterPower, endPoint),
		meterGetCmd(meterVoltage, endPoint),
		meterGetCmd(meterCurrent, endPoint)
	], 250)
}


def reset() {
	logDebug "reset()..."
	
	runIn(5, refresh)
	
	def cmds = getResetCmds()	
	childDevices.each { child ->	
		if (!"${child.deviceNetworkId}".endsWith("USB")) {
			cmds << "delay 500"
			cmds += getResetCmds(child.deviceNetworkId)
		}
	}
	return cmds
}

def childReset(dni) {
	logDebug "childReset($dni)"
	
	def cmds = getResetCmds(dni)
	cmds << "delay 1000"
	cmds += getRefreshCmds(dni)
	sendCommands(cmds)
}

private getResetCmds(dni=null) {
	def endPoint = getEndPoint(dni)
	def child = findChildByDeviceNetworkId(dni)
		
	["power", "voltage", "current"].each {
		executeSendEvent(child, createEventMap("${it}Low", getAttrVal(it), false))
		executeSendEvent(child, createEventMap("${it}High", getAttrVal(it), false))
	}
	executeSendEvent(child, createEventMap("energyTime", new Date().time, false))
	sendEnergyEvents(child, 0)
	
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
	return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endPoint)).encapsulate(cmd))
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
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
		if (!"${description}".contains("command: 5E02")) {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)		
			}
			else {
				log.warn "Unable to parse: $description"
			}
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
	logTrace "SwitchBinaryReport: ${cmd} ({getEndPointName(endPoint)})"
	
	def value = (cmd.value == 0xFF) ? "on" : "off"
	
	executeSendEvent(findChildByEndPoint(endPoint), createEventMap("switch", value))
	
	sendEvent(name: "${getEndPointName(endPoint)}Switch", value:value, displayed: false)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=0) {
	logTrace "BasicReport: ${cmd} (${getEndPointName(endPoint)})"
	
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
			sendMeterEvents(child, meterPower, val)
			sendEvent(name: "${getEndPointName(endPoint)}Power", value: val, unit:"w", displayed: false)
			break
		case meterVoltage.scale:
			sendMeterEvents(child, meterVoltage, val)
			break
		case meterCurrent.scale:
			sendMeterEvents(child, meterCurrent, val)
			break
		default:
			logDebug "Unknown Meter Scale: $cmd"
	}
	
	// runIn(2, updateSecondaryStatus)
	return []
}

private sendMeterEvents(child, meter, value) {
	def highLowNames = [] 
	
	executeSendEvent(child, createEventMap(meter.name, value, meter.displayed, meter.unit))
	
	def highName = "${meter.name}High"
	if (getAttrVal(highName, child) == null || value > getAttrVal(highName, child)) {
		highLowNames << highName
	}

	def lowName = "${meter.name}Low"
	if (getAttrVal(lowName, child) == null || value < getAttrVal(lowName, child)) {
		highLowNames << lowName
	}
	
	highLowNames.each {
		executeSendEvent(child, createEventMap("$it", value, false, meterPower.unit))
	}	
}



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


// def updateSecondaryStatus() {
	// (0..5).each { endPoint ->	
		// def child = findChildByEndPoint(endPoint)
		// def power = getAttrVal("power", child) ?: 0
		// def energy = getAttrVal("energy", child) ?: 0
		// def duration = getAttrVal("energyDuration", child) ?: ""
		// // def active = getAttrVal("acceleration", child) ?: "inactive"
		
		// if (duration) {
			// duration = " - ${duration}"
		// }
		
		// def status = ""
		
		// // status = settings?.displayAcceleration ? "${active.toUpperCase()} / " : ""
		
		// status =  "${status}${power} ${meterPower.unit} / ${energy} ${meterEnergy.unit}${duration}"
		
		// if (getAttrVal("secondaryStatus", child) != "${status}") {
			// executeSendEvent(child, createEventMap("secondaryStatus", status, false))
		// }
	// }
// }


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


// Meters
private getMeterEnergy() { 
	return getMeterMap("energy", 0, "kWh", null, settings?.displayEnergy != false) 
}

private getMeterPower() { 
	return getMeterMap("power", 2, "W", 2000, settings?.displayPower != false)
}

private getMeterVoltage() { 
	return getMeterMap("voltage", 4, "V", 150, settings?.displayVoltage != false) 
}

private getMeterCurrent() { 
	return getMeterMap("current", 5, "A", 18, settings?.displayCurrent != false)
}

private getMeterMap(name, scale, unit, limit, displayed) {
	return [name:name, scale:scale, unit:unit, limit: limit, displayed:displayed]
}



// Configuration Parameters
private getConfigParams() {
	return [
		powerFailureRecoveryParam,
		overloadProtectionParam,
		manualControlParam,
		ledIndicatorModeParam,
		powerReportingThresholdParam,
		powerReportingFrequencyParam,
		energyReportingFrequencyParam,
		voltageReportingFrequencyParam,
		ampsReportingFrequencyParam,		
		leftAutoOffEnabledParam,
		leftAutoOffIntervalParam,
		rightAutoOffEnabledParam,
		rightAutoOffIntervalParam,
		leftAutoOnEnabledParam,
		leftAutoOnIntervalParam,
		rightAutoOnEnabledParam,
		rightAutoOnIntervalParam
	]
}

private getPowerFailureRecoveryParam() {
	return getParam(1, "On/Off Status Recovery After Power Failure", 1, 0, [0:"Restore Outlets States From Before Power Failure", 1:"Turn Outlets On", 2:"Turn Outlets Off"])
}

private getPowerReportingThresholdParam() {
	return getParam(2, "Power Reporting Threshold", 4, 5, powerReportingThresholdOptions) 
}

private getPowerReportingFrequencyParam() {
	return getParam(3, "Power Reporting Frequency", 4, 30, frequencyOptions)
}

private getEnergyReportingFrequencyParam() {
	return getParam(4, "Energy Reporting Frequency", 4, 300, frequencyOptions) 
}

private getVoltageReportingFrequencyParam() {
	return getParam(5, "Voltage Reporting Frequency", 4, 300, frequencyOptions) 
}

private getAmpsReportingFrequencyParam() {
	return getParam(6, "Electrical Current Reporting Frequency", 4, 300, frequencyOptions) 
}

private getOverloadProtectionParam() {
	return getParam(7, "Overload Protection", 1, 10, overloadOptions) 
}

private getLeftAutoOffEnabledParam() {
	return getParam(8, "Left Outlet Auto Turn-Off Timer Enabled", 1, 0, enabledOptions)
}

private getLeftAutoOffIntervalParam() {
	return getParam(9, "Left Outlet Auto Turn-Off After", 4, 60, autoOnOffIntervalOptions)
}

private getRightAutoOffEnabledParam() {
	return getParam(12, "Right Outlet Auto Turn-Off Timer Enabled", 1, 0, enabledOptions)
}

private getRightAutoOffIntervalParam() {
	return getParam(13, "Right Outlet Auto Turn-Off After", 4, 60, autoOnOffIntervalOptions)
}

private getLeftAutoOnEnabledParam() {
	return getParam(10, "Left Outlet Auto Turn-On Timer Enabled", 1, 0, enabledOptions)
}

private getLeftAutoOnIntervalParam() {
	return getParam(11, "Left Outlet Auto Turn-On After", 4, 60, autoOnOffIntervalOptions)
}

private getRightAutoOnEnabledParam() {
	return getParam(14, "Right Outlet Auto Turn-On Timer Enabled", 1, 0, enabledOptions)
}

private getRightAutoOnIntervalParam() {
	return getParam(15, "Right Outlet Auto Turn-On After", 4, 60, autoOnOffIntervalOptions)
}

private getManualControlParam() {
	return getParam(16, "Manual Control", 1, 1, enabledOptions)
}

private getLedIndicatorModeParam() {
	return getParam(17, "LED Indicator Mode", 1, 1, [0:"Always On", 1:"On When Switch On", 2:"LED On for 5 Seconds", 3:"LED Always Off"])
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
	def options = [:]
	(1..10).each {
		options["${it}"] = "${it} A"
	}	
	return options
}

private getPowerReportingThresholdOptions() {
	def options = [0:"Disabled"]
	[1,2,3,4,5,10,25,50,75,100,150,200,250,300,400,500,750,1000,1250,1500,1750,2000,2500,3000,3500,4000,4500,5000].each {
		options["${it}"] = "${it} W"
	}
	return options
}

private getFrequencyOptions() {
	def options = [:]
	options = getTimeOptionsRange(options, "Second", 1, [30,45])
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

private getTimeOptionsRange(options, name, multiplier, range) {	
	range?.each {
		options["${(it * multiplier)}"] = "${it} ${name}${it == 1 ? '' : 's'}"
	}
	return options
}

private getEnabledOptions() {
	return [0:"Disabled", 1:"Enabled"]
}


// Settings

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
			logDebug "${evt.descriptionText}"
			sendEvent(evt)
		}
	}
}

private createEventMap(name, value, displayed=null, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		// isStateChange: true,
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
	return safeToInt((1..3).find {
		"${childDeviceNetworkId}".endsWith("-${getEndPointName(it)?.toUpperCase()}")
	})
}

private getChildDeviceNetworkId(endPoint) {
	return "${device.deviceNetworkId}-${getEndPointName(endPoint).toUpperCase()}"
}

private getEndPointName(endPoint) {
	switch (endPoint) {
		case 1:
			return "left"
			break
		case 2:
			return "right"
			break
		case 3:
			return "usb"
			break
		default:
			return ""
	}
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