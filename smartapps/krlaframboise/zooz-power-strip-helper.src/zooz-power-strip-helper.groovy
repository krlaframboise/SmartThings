/**
 *  Zooz Power Strip Helper - v 1.0.1
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-power-strip/68860?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.0.1 (01/16/2017)
 *      - Bug fix for when virtual devices no longer exist.
 *      - Added icon.
 *
 *    1.0.0 (01/14/2017)
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
    name: "Zooz Power Strip Helper",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Allows you to control the Zooz Power Strip as 5 separate devices.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/zooz-power-strip-helper/app-ZoozPowerStripHelper.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/zooz-power-strip-helper/app-ZoozPowerStripHelper@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/zooz-power-strip-helper/app-ZoozPowerStripHelper@3x.png")
		
preferences {
	page(name: "mainPage")
	page(name: "createDevicesPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		section("About") {
			paragraph "This SmartApp automatically generates virtual devices for the Zooz Power Strip's 5 outlets.  All you have to do is choose your Zooz Power Strip and tap 'Create Virtual Devices'."
			paragraph "Once the virtual devices have been created, you can open them from the 'Things' tab, rename them, and use them like any other switch."
		}
		section("Zooz Power Strip") {
			input "zoozPS", "capability.switch",
				title: "Select Zooz Power Strip",
				submitOnChange: true			
		}
		if (settings?.zoozPS) {
			section ("Outlets") {
				def deviceNames = ""
				getChildDevices()?.sort { it.displayName }?.each {
					deviceNames += "${it.displayName}\n"
				}
				if (deviceNames) {
					paragraph "${deviceNames}"
				}
				if (getChildDevices()?.size() != 5) {
					getPageLink("createDevicesPageLink", "Create Virtual Devices", "createDevicesPage")
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

def createDevicesPage() {
	dynamicPage(name:"createDevicesPage") {
		section("Create Virtual Devices") {
			def msg = ""
			(1..5).each { ch ->				
				def chDevice = [
					ch: "${ch}",
					dni: "${dniFromCH(ch)}",
					label: "Zooz Power Strip - CH${ch}"
				]				
			
				if (!getChildDevice("${chDevice.dni}")) {
					msg += createVirtualDevice(chDevice)
				}
				else {
					msg += "${chDevice.label} Already Exists\n"
				}				
			}
			paragraph "${msg}"
		}
		initialize()
	}
}

private createVirtualDevice(chDevice) {	
	def msg = ""
	try {
		addChildDevice(
			"krlaframboise", 
			"Virtual Switch", 
			"${chDevice.dni}", 
			settings.hostHub?.id, 
			[
				"name": "Zooz Power Strip - Outlet",
				label: chDevice.label,
				completedSetup: true
			]
		)		
		msg = "Created Virtual Device: ${chDevice.label}\n"
		logDebug "${msg}"
		
		syncVirtualSwitch(chDevice)
	}
	catch(e) {
		msg = "Unable to Create Virtual Device: ${chDevice.label}\nError: ${e}\n"
		logWarn "${msg}"		
	}
	return msg
}

private syncVirtualSwitch(chDevice) {
	def actualSwitch = zoozPS."currentCh${chDevice?.ch}Switch"
	def vDevice = getChildDevice(chDevice?.dni)
	if (vDevice && vDevice?.currentSwiitch != actualSwitch) {
		logDebug "Turning Virtual ${vDevice?.displayName} ${actualSwitch}"
		vDevice?."${actualSwitch}"()
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
	
	unschedule()
	unsubscribe()
	initialize()
}

private initialize() {
	logTrace "Executing initialize()"
	if (settings?.zoozPS) {
		
		getChildDevices()?.each {
			def ch = chFromDNI(it.deviceNetworkId)
			subscribe(it, "switch", chSwitchHandler)
		}
		
		(1..5).each { ch ->
			subscribe(settings?.zoozPS, "ch${ch}Switch", mainSwitchHandler)
		}
	}
}

def chSwitchHandler(evt) {
	logTrace "chSwitchHandler: ${evt.displayName}, ${evt.name}, ${evt.value}"
	def ch = chFromDNI(evt?.device?.deviceNetworkId) 
	if (ch) {
		logDebug "Turning ${evt.value} CH${ch}"
		settings?.zoozPS?."ch${ch}${evt.value.capitalize()}"()
	}
}

def mainSwitchHandler(evt) {
	(1..5).each { ch ->
		if (evt.name == "ch${ch}Switch") {
			
			def device = getChildDevice(dniFromCH("${ch}"))
			if (device && device?.currentSwitch != evt.value) {
				logDebug "Turning ${evt.value} ${device.displayName}"
				device."${evt.value}"()
			}
			
		}
	}
}

private chFromDNI(dni) {	
	return dni?.replace("${settings?.zoozPS?.deviceNetworkId}CH", "")
}

private dniFromCH(ch) {
	return "${settings?.zoozPS?.deviceNetworkId}CH${ch}"
}

def uninstalled() {
	logTrace "Executing uninstalled()"
	removeAllOutlets(getChildDevices())
}

private removeAllOutlets(devices) {
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
