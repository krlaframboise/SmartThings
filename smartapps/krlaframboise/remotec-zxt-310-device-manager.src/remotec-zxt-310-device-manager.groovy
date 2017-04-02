/**
 *  Remotec ZXT-310 Device Manager v1.0
 *
 *		(You have to install the device handlers Remotec ZXT-310 IR Extender and Remotec ZXT-310 Device in order to use this SmartApp)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  
 *
 *  Changelog:
 *
 *    1.0.0 (04/02/2017)
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
    name: "Remotec ZXT-310 Device Manager",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Allows you to use the Remotec ZXT-310 IR Extender End Points as separate devices.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/unknown/zwave/remote-controller.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/unknown/zwave/remote-controller@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/unknown/zwave/remote-controller@3x.png")
		
preferences {
	page(name: "mainPage")
	page(name: "createDevicesPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		section("About") {
			paragraph "This SmartApp automatically generates virtual devices for the Remotec ZXT-310 IR Extender's 6 end points.  All you have to do is choose your Remotec ZXT-310 IR Extender and tap 'Create Devices'."
			paragraph "Once the devices have been created you can open them from the 'Things' tab and update their settings."
			paragraph "These devices allow you to use the buttons, but they must be learned using the Remotec ZXT-310 IR Extender device."
		}
		section("Remotec ZXT-310 IR Extender") {
			input "irExtender", "capability.button",
				title: "Remotec ZXT-310 IR Extender",
				submitOnChange: true,
				required: true
		}
		if (irExtender) {
			section ("Remotec ZXT-310 Devices") {
				def deviceNames = ""
				getChildDevices()?.collect { getChildDisplayName(it) }?.sort { it }?.each {
					deviceNames += "${it}\n"
				}
				if (deviceNames) {
					paragraph "${deviceNames}"
				}
				if (getChildDevices()?.size() != 6) {
					getPageLink("createDevicesPageLink", "Create Devices", "createDevicesPage")
				}
			}
		}
		section("Live Logging Options") {
			input "logging", "enum",
				title: "Types of messages to write to Live Logging:",
				multiple: true,
				required: false,
				defaultValue: ["debug", "info"],
				options: ["debug", "info", "trace"]
		}
	}
}

private getChildDisplayName(device) {
	def ep = epFromDNI(device.deviceNetworkId)
	return "EP${ep}: ${device.displayName}"
}

def createDevicesPage() {
	dynamicPage(name:"createDevicesPage") {
		section("Create Devices") {
			def msg = ""
			(1..6).each { ep ->
				def epDevice = [
					ep: "${ep}",
					dni: "${dniFromEP(ep)}",
					label: "Remotec ZXT-310:EP${ep}"
				]
				if (!getChildDevice("${epDevice.dni}")) {
					msg += createVirtualDevice(epDevice)
				}
				else {
					msg += "${epDevice.label} Already Exists\n"
				}				
			}
			paragraph "${msg}"
		}
		initialize()
	}
}

private createVirtualDevice(epDevice) {	
	def msg = ""
	try {
		addChildDevice(
			"krlaframboise", 
			"Remotec ZXT-310 Device", 
			"${epDevice.dni}", 
			irExtender.hub.id, 
			[
				"name": "Remotec ZXT-310",
				label: epDevice.label,
				completedSetup: true
			]
		)		
		msg = "Created Device: ${epDevice.label}\n"
		logDebug "${msg}"

		refreshChildData(epDevice.dni)
	}
	catch(e) {
		msg = "Unable to Create Device: ${epDevice.label}\nError: ${e}\n"
		logWarn "${msg}"		
	}
	return msg
}

def refreshChildData(dni) {
	logDebug "refreshChildData(${dni})"
	def ep = epFromDNI(dni)
	def data = irExtender?.currentValue("ep${ep}Data")	
	if (data) {
		getChildDevice(dni)?.refreshData(data)
	}	
}

private getPageLink(linkName, linkText, pageName, args=null,desc="",image=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "$desc",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	if (image) {
		map.image = image
	}
	href(map)
}


def installed() {	
	logTrace "Executing installed()"
	initialize()
}

def updated() {
	logTrace "Executing updated()"
	
	unsubscribe()
	initialize()
}

private initialize() {
	logTrace "Executing initialize()"
	if (irExtender) {
		
		getChildDevices()?.each {
			subscribe(it, "button.pushed", childButtonPushedHandler)
		}
		
		(1..6).each { ep ->
			subscribe(irExtender, "ep${ep}Data", epDataChangedHandler)
		}		
	}
}

def childButtonPushedHandler(evt) {
	irExtender.pushButton(evt.data)	
}

def epDataChangedHandler(evt) {
	def ep = evt.name?.replace("ep", "")?.replace("Data", "")
	def dni = dniFromEP(ep)
	if (dni) {		
		getChildDevice(dni)?.refreshData(evt.value)		
	}	
}

private epFromDNI(dni) {	
	return dni?.replace("${irExtender?.deviceNetworkId}-EP", "")
}

private dniFromEP(ep) {
	return "${irExtender?.deviceNetworkId}-EP${ep}"
}

def uninstalled() {
	logTrace "Executing uninstalled()"
	removeAllDevices(getChildDevices())
}

private removeAllDevices(devices) {
	devices?.each {
		logDebug "Removing ${it.displayName}"
		deleteChildDevice(it.deviceNetworkId)
	}
}

def childUninstalled() {
	// Required to prevent warning on uninstall.
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private logWarn(msg) {
	log.warn msg
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}
