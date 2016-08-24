/**
 *  Simple Alarm v1.0 [Alpha]
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *
 *  Changelog:
 *
 *    1.0.0_2016-08-22: Example UI
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
    name: "Simple Alarm",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Watches alert and security devices and performs actions when they're set off.",
    category: "Safety & Security",
		iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-alarm/app-SimpleAlarm.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-alarm/app-SimpleAlarm@2x.png")

 preferences {
	page(name:"mainPage", title: "Simple Alarm")
	page(name:"changeSecurityModePage", title: "Change Security Mode")
  page(name:"devicesPage", title: "Devices")	
	page(name:"zoneGroupsPage", title: "Zone Groups")
	page(name:"editZoneGroupPage", title: "Zone Group Details")
	page(name:"zonesPage", title: "Zones")
	page(name:"editZonePage", title: "Zone Details")
	page(name:"refreshZonesPage", title: "Refresh Zones")
	page(name:"alertNotificationsPage", title: "Alert Notifications")
	page(name:"securityNotificationsPage", title: "Security Notifications")
	page(name:"securityModeNotificationsPage", title: "Security Mode Notifications")
	page(name:"armDisarmPage", title: "Arming/Disarming Options")
	page(name:"securityModeArmDisarmPage")
	page(name:"advancedOptionsPage", title: "Advanced Options")
	page(name:"securityModeAdvancedOptionsPage")
}

def mainPage() {	
	dynamicPage(name:"mainPage", uninstall:true, install:true) {		
		if (state.installed) {
			
			def config = getConfigSummary()
			state.configSummary = configSummary
			def unconfiguredDesc = "(unconfigured) "
			
			section ("Security Modes") {
				if (config.hasAllRequirements) {
					getSecurityModes(true).each {
						if (it.name != state.securityMode) {
							def imageName = (it.name == state.securityMode?.name) ? "selected.png" : "unselected.png"							
							getPageLink("${it.id}ChangeSecurityModeLink",
								"${it.name}",
								"changeSecurityModePage",
								[securityMode: it],
								"",
								"$imageName")
						}						
					}
				}
				else {
					paragraph "Security Mode can't be changed until \"Zones\" and \"Notifications\" have been configured."
				}
			}
			section("Settings") {			
				getPageLink("devicesLink",
					"Choose Devices",
					"devicesPage",
					null,
					config.hasAlertOrSecurityDevices ? "" : unconfiguredDesc)
				if (config.hasAlertOrSecurityDevices) {
					getPageLink("zoneGroupsLink",
						"Zone Groups",
						"zoneGroupsPage")
					getPageLink("zonesLink",
						"Zones",
						"zonesPage",
						null,
						config.hasZones ? "" : unconfiguredDesc)
				}
				else {
					paragraph "Zones can't be setup until at least one \"Alert Device to Monitor\" or \"Security Device to Monitor\" has been chosen."
				}
				getPageLink("alertNotificationsLink",
					"Alert Notifications",
					"alertNotificationsPage",
					null,
					config.hasConfiguredAlertNotifications ? "" : (config.hasConfiguredSecurityNotifications ? "(not set)" : "(unconfigured)"))
				getPageLink("securityNotificationsLink",
					"Security Notifications",
					"securityNotificationsPage",
					null,
					config.hasConfiguredSecurityNotifications ? "" : (config.hasConfiguredAlertNotifications ? "(not set)" : "(unconfigured)"))
				if (config.hasAllRequirements) {
					getPageLink("armDisarmLink",
						"Arming/Disarming",
						"armDisarmPage")				
					getPageLink("advancedOptionsLink",
						"Advanced Options",
						"advancedOptionsPage")
				}
				else {
					paragraph "Arming/Disarming and Advanced Options can't be setup until \"Zones\" and \"Notifications\" have been configured."
				}
			}
			section("Logging Options") {
				input "logging", "enum",
					title: "Types of messages to log:",
					multiple: true,
					required: false,
					defaultValue: ["debug", "info"],
					options: ["debug", "info", "trace"]
			}
		}
		else {
			section() {
				state.installed = true
				paragraph image: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-alarm/app-SimpleAlarm@2x.png", "Installation Complete.\n\nPlease tap Done, exit the Marketplace, and open Simple Alarm from your installed SmartApp list."
			}
		}
	}
}

def getConfigSummary() {
	def config = [:]

	config.hasAlertDevices = hasAlertDevices()
	config.hasSecurityDevices = hasSecurityDevices()
	config.hasZones = hasZones()
	
	config.hasAlertOrSecurityDevices = (config.hasAlertDevices || config.hasSecurityDevices)
	
	config.hasConfiguredAlertNotifications = hasConfiguredNotifications("Alert", null)
	
	config.hasConfiguredSecurityNotifications = hasConfiguredNotifications("Security", null)
	
	config.hasConfiguredAlertOrSecurityNotifications = config.hasConfiguredAlertNotifications || config.hasConfiguredSecurityNotifications
	
	//config.hasNotificationDevices = hasNotificationDevices()
	//config.hasRequiredDevices = config.hasAlertOrSecurityDevices && config.hasNotificationDevices
		
	config.hasAllRequirements = config.hasAlertOrSecurityDevices && config.hasConfiguredAlertOrSecurityNotifications && config.hasZones
	
	//logTrace "<--- DEVICES --->\n  hasAlertDevices: ${config.hasAlertDevices}\n  hasSecurityDevices: ${config.hasSecurityDevices}\n  hasAlertOrSecurityDevices: ${config.hasAlertOrSecurityDevices}\n  hasNotificationDevices: ${config.hasNotificationDevices}\n  hasZones: ${config.hasZones}\n  hasRequiredDevices: ${config.hasRequiredDevices}\n  hasAllRequirements: ${config.hasAllRequirements}"
	
	//logTrace "<--- NOTIFICATIONS --->\n  hasConfiguredAlertNotifications: ${config.hasConfiguredAlertNotifications}\n  hasConfiguredSecurityNotifications: ${config.hasConfiguredSecurityNotifications}"
	
	return config
}

def changeSecurityModePage(params) {
	dynamicPage(name:"changeSecurityModePage") {
		section() {
			changeSecurityMode(params.securityMode)
			paragraph "Security Mode Changed to ${state.securityMode?.name}"
		}
	}
}

private changeSecurityMode(newSecurityMode) {
	state.securityMode = newSecurityMode
	initialize()
}

def devicesPage() {
	dynamicPage(name:"devicesPage") {
		section("Alert Devices to Monitor") {
			getAlertDeviceTypes().each {
				input "${it.prefName}", "${it.prefType}",
					title: "${it.name}:",
					multiple: true,
					required: false
			}			
		}
		section("Security Devices to Monitor") {
			getSecurityDeviceTypes().each {
				input "${it.prefName}", "${it.prefType}",
					title: "${it.name}:",
					multiple: true,
					required: false
			}
		}
		section("Arming/Disarming Trigger Devices") {
			getArmDisarmDeviceTypes().each {
				input "${it.prefName}", "${it.prefType}",
					title: "${it.name}:",
					multiple: true,
					required: false
			}
		}
		section ("Notification Devices") {
			getNotificationDeviceTypes().each {
				input "${it.prefName}", "${it.prefType}",
					title: "${it.name}:",
					multiple: true,
					required: false
			}
		}
		section ("SMS Phone Numbers") {
			getSMSNotificationTypes().each {
				input "${it.prefName}", "${it.prefType}", 
					title: "${it.name}:",
					required: false	
			}			
		}
	}
}

def zoneGroupsPage() {
	dynamicPage(name:"zoneGroupsPage") {
		section() {
			getPageLink("addZoneGroupLink",
				"Add New Zone Group",
				"editZoneGroupPage",
				[zoneGroup: null])
			getZoneGroups().each {				
				getPageLink("${it.settingName}Link",
					"${it.name}",
					"editZoneGroupPage",
					[zoneGroup: it])
			}
		}
	}
}  

def editZoneGroupPage(params) {
	dynamicPage(name:"editZoneGroupPage") {
		section ("Edit Zone Group") {
			def zoneGroup = params?.zoneGroup ?: getFirstEmptyZoneGroup()
			if (zoneGroup) {
				input "${zoneGroup.settingName}", "text",
					title: "Zone Group Name:",
					required: false
				if (zoneGroup.name) {
					paragraph "You can remove this Zone Group by clearing the Zone Group Name field."
				}
			}
			else {
				paragraph "You can only have 25 Zone Groups"
			}
		}
	}
}

def zonesPage() {
	dynamicPage(name:"zonesPage") {
		def hasZones = false
		
		section() {			
			getPageLink("addZoneLink",
				"Add New Zone",
				"editZonePage",
				[zone: null])		
			getZones().each {				
				hasZones = true
				getPageLink("${it.settingName}Link",
					"(${it.status}) ${it.displayName}",
					"editZonePage",
					[zone: it])
			}
		}
		
		if (hasZones) {
			section("Zone Changes") {
				paragraph "Zone changes that effect the Armed state won't get applied until you exit the application.  To apply these changes immediately, tap the Refresh Zones button below."
				getPageLink("$refreshZonesLink",
					"Refresh Zones",
					"refreshZonesPage")
			}
		}
	}
}

def refreshZonesPage(params) {
	dynamicPage(name:"refreshZonesPage") {
		section() {
			initialize()
			paragraph "Zones refreshed successfully"
		}
	}
}

def editZonePage(params) {
	dynamicPage(name:"editZonePage") {
		def zone = params?.zone ?: getFirstEmptyZone()
		if (zone) {
			section() {
				input "${zone.settingName}", "text",
					title: "Zone Name:",
					required: false
				input "${zone.settingName}Group", "enum",
					title: "Zone Group:",
					required: false,
					options: getZoneGroupNames()
			}
			section ("Alert Settings") {
				input "${zone.settingName}AlertDevices", "enum",
					title: "Alert Devices:",
					multiple: true,
					required: false,
					options: getAlertDeviceNames()
				
				getAlertDeviceTypes().each {
					def attr = it.alarmAttr
					if (getAlertDevices().find { it.hasAttribute(attr) }) {
						input "${zone.settingName}${it.prefName}Message", "text",
							title: "${it.shortName} Notification Message:",
							required: false
					}
				}
			}
			section ("Security Settings") {
				input "${zone.settingName}SecurityDevices", "enum",
					title: "Security Devices:",
					multiple: true,
					required: false,
					options: getSecurityDeviceNames()

				getSecurityDeviceTypes().each {
					def attr = it.alarmAttr
					if (getSecurityDevices().find { it.hasAttribute(attr) }) {
						input "${zone.settingName}${it.prefName}Message", "text",
							title: "${it.shortName} Notification Message:",
							required: false
					}
				}

				input "${zone.settingName}EnabledSecurityModes", "enum",
					title: "Enabled Security Modes:",
					multiple: true,
					required: false,
					defaultValue: getSecurityModeNames(false),
					options: getSecurityModeNames(false)
			}
			if (zone.name) {
				section() {
					paragraph "You can Delete this Zone by clearing the Zone Name field and tapping Done."
				}
			}
		}
		else {
			section() {
				paragraph "You can only have 100 Zones"
			}
		}
	}
}

def alertNotificationsPage() {
	dynamicPage(name:"alertNotificationsPage") {
		section() {
			paragraph "Setup Alert Notifications for each Security Mode."
			if (!state.configSummary?.hasAlertDevices) {
				paragraph "**** WARNING ****\nThese notifications won't get executed because no Alert Devices are being monitored."
			}
			
			getSecurityModes(true).each {
				getPageLink("${it.id}AlertNotificationsLink",
					"${it.name}",
					"securityModeNotificationsPage",
					[securityMode: it, notificationType: "Alert"],
					hasConfiguredNotifications("Alert", it.id) ? "" : "(not set)")
			}
		}		
	}
} 

def securityNotificationsPage() {
	dynamicPage(name:"securityNotificationsPage") {
		section() {
			paragraph "Setup Security Notifications for each Security Mode."
			if (!state.configSummary?.hasSecurityDevices) {
				paragraph "**** WARNING ****\nThese notifications won't get executed because no Security Devices are being monitored."
			}
						
			getSecurityModes(false).each {
				getPageLink("${it.id}SecurityNotificationsLink",
					"${it.name}",
					"securityModeNotificationsPage",
					[securityMode: it, notificationType: "Security"],
					hasConfiguredNotifications("Security", it.id) ? "" : "(not set)")
			}
		}		
	}
} 

def securityModeNotificationsPage(params) {
	dynamicPage(name:"securityModeNotificationsPage") {
		if (params?.securityMode) {
			state.params.securityMode = params.securityMode
			state.params.notificationType = params.notificationType
		}
		
		def id = state.params?.securityMode?.id
		def name = state.params?.securityMode?.name
		def type = state?.params?.notificationType
		
		section("$name - $type Notifications") {
			paragraph "Setup $type Notifications for Security Mode $name"
		}
		
		section ("Push Notifications") {		
			input "${id}${type}SendPush", "bool",
				title: "Send Push Notifications?",
				defaultValue: false,
				required: false
		}
		section ("SMS Notifications") {
			def smsOptions = getSMSNotificationPhoneNumbers()
			if (smsOptions) {
				input "${id}${type}SendSMS", "enum",
					title: "Send SMS Messages:",
					multiple: true,
					required: false,
					options: smsOptions
			}
			else {
				paragraph "You can't use SMS Notifications until you enter at least one \"SMS Phone Number\" into the \"SMS Phone Numbers\" section of the \"Choose Devices\" page."
			}
		}
		section ("Alarm Notifications") {
			def alarmOptions = getNotificationDeviceNames(null, "Alarm")
			if (alarmOptions) {
				input "${id}${type}Siren", "enum",
					title: "Turn on Siren:",
					multiple: true,
					required: false,
					options: alarmOptions
				input "${id}${type}Strobe", "enum",
					title: "Turn on Strobe:",
					multiple: true,
					required: false,
					options: alarmOptions
				input "${id}${type}SirenStrobe", "enum",
					title: "Turn on Siren & Strobe:",
					multiple: true,
					required: false,				
					options: alarmOptions			
				input "${id}${type}AlarmTurnOffAfter", "number",
					title: "Turn Off Alarm After: (seconds)",
					required: false
			}
			else {
				paragraph getNotificationNoDeviceMessage("Alarm Notifications", "Alarm", null)
			}
		}
		section ("Switch Notifications") {
			def switchOptions = getNotificationDeviceNames(null, "Switch")
			if (switchOptions) {
				input "${id}${type}SwitchOn", "enum",
					title: "Turn On:",
					multiple: true,
					required: false,
					options: switchOptions
				input "${id}${type}SwitchOff", "enum",
					title: "Turn Off:",
					multiple: true,
					required: false,
					options: switchOptions
			}
			else {
				paragraph getNotificationNoDeviceMessage("Switch Notifications", "Switch", null)
			}
		}
		section ("Audio Notifications") {
			def speakOptions = getNotificationDeviceNames("speak", null)
			if (speakOptions) {
				input "${id}${type}Speak", "enum",
					title: "Speak Zone Message:",
					multiple: true,
					required: false,
					options: speakOptions
			}
			else {
				paragraph getNotificationNoDeviceMessage("Speak Zone Message", "Speech Synthesis", null)
			}
			def playTextOptions = getNotificationDeviceNames("playText", null)
			if (playTextOptions) {
				input "${id}${type}PlayText", "enum",
					title: "Play Zone Message as Text:",
					multiple: true,
					required: false,
					options: playTextOptions
			}
			else {
				paragraph getNotificationNoDeviceMessage("Play Zone Message as Text", "Audio Notification or Music Player", "playText")
			}
			def playTrackOptions = getNotificationDeviceNames("playTrack", null)
			if (playTrackOptions) {
				input "${id}${type}PlayTrack", "enum",
					title: "Play Zone Message as Track:",
					multiple: true,
					required: false,
					options: playTrackOptions
			}
			else {			
				paragraph getNotificationNoDeviceMessage("Play Zone Message as Track", "Audio Notification or Music Player", "playTrack")
			}
			if (playTextOptions || playTrackOptions) {
				input "${id}${type}Volume", "number",
					title: "Zone Message Volume:",
					required: false
			}
		}
		section ("Photo Notifications") {
			def photoOptions = getNotificationDeviceNames(null, "Image Capture")
			if (photoOptions) {
				input "${id}${type}TakePhoto", "enum",
					title: "Take Photo:",
					multiple: true,
					required: false,
					options: photoOptions
			}
			else {
				paragraph getNotificationNoDeviceMessage("Photo Notifications", "Image Capture", null)
			}
		}
	}
}

private getNotificationNoDeviceMessage(fieldName, deviceType, cmd=null) {
	def supportsCmd = cmd ? ", that supports the \"$cmd\" command," : ""
	return "You can't use ${fieldName} until you select at least one \"${deviceType}\" device${supportsCmd} from the \"Notification Devices\" section of the \"Choose Devices\" page."
}

def armDisarmPage() {
	dynamicPage(name:"armDisarmPage") {
		section() {
			paragraph "Specify triggers that activate the different Security Modes."
			getSecurityModes(true).each {
				getPageLink("${it.id}ArmDisarmLink",
					"${it.name}",
					"securityModeArmDisarmPage",
					[securityMode: it])
			}
		}
	}
}

def securityModeArmDisarmPage(params) {
	dynamicPage(name:"securityModeArmDisarmPage") {
		if (params?.securityMode) {
			state.params.securityMode = params.securityMode
		}
		
		def id = state.params?.securityMode?.id
		def name = state.params?.securityMode?.name

		section("${name} - Arming/Disarming Options") {
			paragraph "Specify triggers that will cause the Security Mode to change to ${name}."
		}
		
		section ("Location Mode") {
			input "${id}ArmDisarmModes", "mode",
				title: "When Mode changes to",
				multiple: true,
				required: false,
				submitOnChange: true
		}
		
		section ("Smart Home Monitor") {
			input "${id}ArmDisarmAlarmSystemStatuses", "enum",
				title: "When Smart Home Monitor changes to",
				multiple: true,
				required: false,
				options: getAlarmSystemStatuses().collect{ it.name },
				submitOnChange: true
		}
		
		if (hasArmDisarmDevices()) {
		
			section ("Switches") {			
				input "${id}ArmDisarmSwitchesOn", "enum",
					title: "When Switch Turns On",
					multiple: true,
					required: false,
					options: getArmDisarmDeviceNames("Switch")
				input "${id}ArmDisarmSwitchesOff", "enum",
					title: "When Switch Turns Off",
					multiple: true,
					required: false,
					options: getArmDisarmDeviceNames("Switch")
			}
			
			// section ("Buttons") {				
				// input "${id}ArmDisarmButtons", "enum",
					// title: "When Button",
					// multiple: true,
					// required: false,
					// options: getArmDisarmDeviceNames("Button"),
					// submitOnChange: true
				
				// if (settings?."${id}ArmDisarmButtons") {
					// input "${id}ArmDisarmButtonNumber", "number",
						// title: "#",
						// required: true
					// input "${id}ArmDisarmButtonAction", "enum",
						// title: "is",
						// required:true,
						// options: ["pushed", "held"]
				// }
			// }
			
			section ("Presence Sensors") {
				input "${id}ArmDisarmPresenceSensorsPresent", "enum",
					title: "When Presence changes to Present:",
					multiple: true,
					required: false,
					options: getArmDisarmDeviceNames("Presence Sensor")
				input "${id}ArmDisarmPresenceSensorsNotpresent", "enum",
					title: "When Presence changes to Not Present:",
					multiple: true,
					required: false,
					options: getArmDisarmDeviceNames("Presence Sensor")
			}
		}
		else {
			section() {
				paragraph "You can't use devices to trigger Arming/Disarming until you select at least one \"Arming/Disarming Trigger Device\" from the \"Choose Devices\" page."
			}
		}
	}
}

def advancedOptionsPage() {
	dynamicPage(name:"advancedOptionsPage") {
		section() {
			paragraph "Exclude Devices, configure Entry/Exit Delays, and Beeping Options for each Security Mode."
			getSecurityModes(true).each {
				getPageLink("${it.id}advancedOptionsLink",
					"${it.name}",
					"securityModeAdvancedOptionsPage",
					[securityMode: it])
			}			
		}		
	}
}

def securityModeAdvancedOptionsPage(params) {
	dynamicPage(name:"securityModeAdvancedOptionsPage") {
		if (params?.securityMode) {
			state.params.securityMode = params.securityMode
		}
		def id = state.params?.securityMode?.id
		def name = state.params?.securityMode?.name
	
	section("${name} - Advanced Options") {
			paragraph "Configure Advanced Options for Security Mode ${name}."
		}
		section ("Exclude Devices") {			
			input "${id}ExcludedDevices", "enum",
				title: "Don't Monitor These Security Devices:",
				multiple: true,
				required: false,
				options: getSecurityDeviceNames(),
				submitOnChange: true
		}
		section ("Entry/Exit Delay") {
			input "${id}EntryExitDevices", "enum",
				title: "Use Delay for these Devices:",
				multiple: true,
				required: false,
				options: getSecurityDeviceNames(),
				submitOnChange: true
			if (settings?."${id}EntryExitDevices") {
				input "${id}EntryExitDelay", "number",
					title: "Delay Length (seconds):",
					required: true
			}
		}
		section ("Beep Options") {
			def beepDeviceNames = getNotificationDeviceNames("beep", null)
			if (beepDeviceNames) {
				input "${id}EntryExitBeepingEnabled", "bool",
					title: "Beep during entry/exit delay?",
					defaultValue: false,
					required: false,
					submitOnChange: true
				input "${id}ConfirmationBeepEnabled", "bool",
					title: "Beep when Security Mode changes?",
					defaultValue: false,
					required: false,
					submitOnChange: true
				if (settings?."${id}ConfirmationBeepEnabled" || settings?."${id}EntryExitBeepingEnabled") {
					input "${id}BeepDevices", "enum",
						title: "Beep Devices:",
						multiple: true,
						required: true,
						options: beepDeviceNames
				}
				if (settings?."${id}EntryExitBeepingEnabled") {
					input "${id}EntryExitBeepFrequency", "number",
						title: "Entry/Exit Beep Frequency (seconds):",
						required: true
				}
			}
			else {
				paragraph "None of the selected Notification Devices support the 'beep' command"
			}
		}
	}
}

private getPageLink(linkName, linkText, pageName, args=null, description="", imageName="") {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "$description",
		page: "$pageName",
		required: false
	]	
	if (args) {
		map.params = args
	}
	if (imageName) {
		map.image = "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-alarm/${imageName}"
	}
	href(map)
}

def installed() {
	state.securityMode = [id: "disarm", name: "Disarmed"]
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
	
	logDebug "State Used: ${(state.toString().length() / 100000)*100}%"
}

def initialize() {
	state.params = [:]
	armZones()
	initializeMonitoredSecurityDevices()
	initializeArmDisarmTriggers()
	
}

def armZones() {
	logDebug "${state?.securityMode?.name} - Arming/Disarming Zones"
	getZones(false).each {
		if (state.securityMode != "Disarmed" && state.securityMode?.name in settings."${it.settingName}EnabledSecurityModes") {
			it.armed = true
		}
		else {
			it.armed = false
		}
		state."${it.armedStateName}" = it.armed			
		logTrace("Zone ${it.name} ${it.armed ? 'Armed' : 'Disarmed'}")
	}
}

private initializeArmDisarmTriggers() {
	if (getSecurityModeSettings(null, "ArmDisarmAlarmSystemStatuses")) {
		logTrace "Subscribing to SHM Status"
		subscribe(location, "alarmSystemStatus", armDisarmAlarmSystemStatusChangedHandler)
	}
	
	if (getSecurityModeSettings(null, "ArmDisarmModes")) {
		logTrace "Subscribing to Mode Changes"
		subscribe(location, "mode", armDisarmModeChangedHandler)
	}
	
	getArmDisarmDevices().each { device ->
		getArmDisarmDeviceTypes().each { type ->
			def canSubscribe = false
			
			type.attrValues.each { attrValue ->				
				def settingName = "${type.prefName?.capitalize()}${attrValue?.replace(' ', '')?.capitalize()}"
				//log.info "${device.displayName}\n${type}\n$attrValue\n${type.prefName}${attrValue.replace(' ', '').capitalize()}"				
				//log.warn "getSecurityModeSettings(null, $settingName) = ${getSecurityModeSettings(null, settingName)}"
				
				if (device.displayName in getSecurityModeSettings(null, "$settingName")) {
					canSubscribe = true
				}
			}
			
			if (canSubscribe) {
				logTrace "Subscribing to ${type.attrName} for [${device.displayName}]"
				subscribe(device, "${type.attrName}", armDisarmDeviceHandler)
			}
			
		}
	}
}

private getSecurityModeSettings(securityModeId, partialSettingName) {
	def result = []	
	getSecurityModes().each { 
		if (!securityModeId || it.id == securityModeId) {			
			def settingName = "${it.id}${partialSettingName}"
			if (settings[settingName]) {
				result += settings[settingName]
			}
		}
	}
	return result
}

private initializeMonitoredSecurityDevices() {
	getArmedSecurityDevices().each { device ->
		getSecurityDeviceTypes().each { type ->
			if (device.hasAttribute("${type.alarmAttr}")) {
				logTrace "${device.displayName}: Subscribing to ${type.alarmAttr}.${type.alarmValue} Event"
				subscribe(device, "${type.alarmAttr}.${type.alarmValue}", "security${type.shortName}Handler")				
			}
		}
	}
}

private getArmedSecurityDevices() {
	def devices = []
	getZones().each { zone ->
		if (zone.armed) {
			def zoneSecurityDevices = settings["${zone.settingName}SecurityDevices"]
		
			if (zoneSecurityDevices) {			
				getAllSecurityDevices().each { device ->				
					if (device.displayName in zoneSecurityDevices) {					
						devices << device
					}
				}			
			}
		}
	}
	return devices
}

private getAllSecurityDevices() {
	def devices = []	
	getSecurityDeviceTypes().each { deviceType ->
		def settingValue = settings[deviceType.prefName]
		if (settingValue) {
			if (deviceType.multiple) {
				devices += settingValue
			}
			else {
				devices << settingValue
			}
		}
	}
	return devices.flatten()
}

def securityContactHandler(evt) {
	logInfo "${evt.displayName}: Contact is ${evt.value}"
	handleSecurityNotifications("Security", evt)
}

def securityMotionHandler(evt) {
	logInfo "${evt.displayName}: Motion is ${evt.value}"
	handleSecurityNotifications("Security", evt)
}

def armDisarmAlarmSystemStatusChangedHandler(evt) {	
	def status = getAlarmSystemStatuses().find { it.id == evt.value }
	def newSecurityMode = getSecurityModes().find {
		(status?.name in settings["${it.id}ArmDisarmAlarmSystemStatuses"])
	}
	if (newSecurityMode) {
		logInfo "Changing Security Mode to ${newSecurityMode.name} because SHM changed to ${status?.name}."
		changeSecurityMode(newSecurityMode)
	}
	else {
		logTrace "SHM changed to ${status?.name}"
	}
}

def armDisarmModeChangedHandler(evt) {
	def newSecurityMode = getSecurityModes().find {
		(evt.value in settings["${it.id}ArmDisarmModes"])
	}
	if (newSecurityMode) {
		logInfo "Changing Security Mode to ${newSecurityMode.name} because Location Mode changed to ${evt.value}."
		changeSecurityMode(newSecurityMode)
	}
	else {
		logTrace "Location Mode changed to ${evt.value}"
	}
}

def armDisarmDeviceHandler(evt) {
	log.info "Arm/Disarm ${evt.displayName}: ${evt.name} is ${evt.value}"
	
	
}

private handleSecurityNotifications(notificationType, evt) {	
	def currentZone = findZoneByDevice(notificationType, evt?.device?.displayName)
				
	log.info "$notificationType Event in Zone ${currentZone?.displayName}"
	
	def currentDeviceType = getDeviceType(notificationType, evt.name, evt.value)
	def namePrefix = "${state.securityMode.id}${notificationType}"	
	def alarmAutoOffSeconds = settings["${namePrefix}AlarmTurnOffAfter"]
	def volume = settings["${namePrefix}Volume"] ?: null
	def message = "${currentZone.displayName}: ${evt.device?.displayName} - ${evt.name} is ${evt.value}"
		
	def zoneMessage = settings["${currentZone.settingName}${currentDeviceType?.prefName}Message"]
		
	getNotificationSettingNames(notificationType, state.securityMode?.id).each { prefName ->
		def prefValue = settings[prefName]	
		
		if (prefValue) {
			if (prefName.endsWith("SendPush")) {
				logTrace "Sending Push message \"$message\""
				sendPush(message)
			}
			else if (prefName.endsWith("SendSMS")) {
				prefValue.each { phone ->
					logTrace "Sending SMS message \"$message\" to $phone"
					sendSms(phone, message)
				}
			}
			else if (!prefName.endsWith("Volume") && !prefName.endsWith("AlarmTurnOffAfter")) {
				def devices = findNotificationDevices(prefValue)
				if (devices) {
					switch (prefName.replace(namePrefix, "")) {				
						case { it in ["Siren", "Strobe", "SirenAndStrobe"] }:
							if (alarmAutoOffSeconds) {
								logTrace "Scheduling Alarm to turn off in ${alarmAutoOffSeconds} seconds."
								runIn(alarmAutoOffSeconds, turnOffAlarm)
							}
						case "Siren":
							logTrace "Executing siren() on $prefValue"
							devices*.siren()
							break
						case "Strobe":
							logTrace "Executing strobe(): $prefValue"
							devices*.strobe()
							break
						case "SirenStrobe":
							logTrace "Executing both on $prefValue"
							devices*.both()
							break
						case "SwitchOn":
							logTrace "Turning on $prefValue"
							devices*.on()
							break
						case "SwitchOff":
							logTrace "Turning off $prefValue"
							devices*.off()
							break
						case "Speak":
							logTrace "Speak \"${zoneMessage}\" on $prefValue"
							devices*.speak(zoneMessage)
							break
						case "PlayText":
							logTrace "Playing Text \"${zoneMessage}\" on ${prefValue} at volume ${volume}"
							devices*.playText(zoneMessage, volume)
							break
						case "PlayTrack":
							logTrace "Playing Track \"${zoneMessage}\" on ${prefValue} at volume ${volume}"
							devices*.playTrack(zoneMessage, volume)
							break				
						case "TakePhoto":
							logTrace "Taking Photo with ${prefValue}"
							devices*.take()
							break
						default:
							logDebug "Unknown Notification - $prefName: $prefValue"
					}
				}
				else {
					logDebug "Unable to find devices for $prefName: $prefValue"
				}
			}
		}
	}
}

private findZoneByDevice(notificationType, deviceDisplayName) {
	getZones().find { zone ->
		if (zone.armed) {	
			def zoneDeviceNames = settings["${zone.settingName}${notificationType}Devices"]
			return (deviceDisplayName in zoneDeviceNames)				
		}
		else {
			return false
		}
	}
}

private getDeviceType(notificationType, eventName, eventVal) {
	def deviceTypes = (notificationType == "Security") ? getSecurityDeviceTypes() : getAlertDeviceTypes()
	
	return deviceTypes.find {		
		it.alarmAttr == eventName && it.alarmValue == eventVal	
	}
}

private findNotificationDevices(deviceNameList) {
	def devices = []	
	getNotificationDevices().each { device ->		
		if (device.displayName in deviceNameList) {
			devices << device
		}
	}
	return devices
}
	
def turnOffAlarm() {
	logTrace "Turning Off Alarms"
}

private getFirstEmptyZoneGroup() {
	def firstZoneGroup = null
	getZoneGroups(true).sort{ it.id }.each {		
		if (!it.name && !firstZoneGroup) {
			firstZoneGroup = it
		}
	}
	return firstZoneGroup
}

private getZoneGroupNames() {
	getZoneGroups().collect { it.name }	
}

private getZoneGroups(includeEmpty) {
	def zoneGroups = []	
	for (int i = 0; i < 25; i++) {
		def zoneGroup = [id: i, settingName: "zoneGroup$i", name: settings["zoneGroup$i"] ?: ""]
		if (includeEmpty || zoneGroup.name) {
			zoneGroups << zoneGroup
		}
	}
	return zoneGroups.sort { it.name }
}

private getFirstEmptyZone() {
	def firstZone = null
	getZones(true).sort{ it.id }.each {		
		if (!it.name && !firstZone) {
			firstZone = it
		}
	}
	return firstZone
}

private hasZones(includeEmpty=false) {
	return getZones(includeEmpty) ? true : false
}

private getZoneNames() {
	getZones().collect { it.name }		
}

private getZones(includeEmpty=false) {
	def zones = []	
	for (int i = 0; i < 100; i++) {
		
		def zone = [id: i, settingName: "zone$i", name: settings."zone$i" ?: ""]
				
		if (includeEmpty || zone.name) {		
			def displayName = settings."${zone.settingName}Group" ?: ""
			zone.displayName = displayName ? "${displayName} > ${zone.name}" : "${zone.name}"
			
			zone.armedStateName = "${zone.settingName}Armed"
			zone.armed = state."${zone.armedStateName}" ?: false
			zone.status = zone.armed ? "Armed" : "Disarmed"
					
			zones << zone
		}
	}
	return zones.sort { it.displayName }
}

private hasAlertDevices() {
	return getAlertDevices() ? true : false
}

private getAlertDeviceNames() {
	return getAlertDevices().collect { it.displayName }.sort()
}

private getAlertDevices() {
	def devices = []
	getAlertDeviceTypes().each {
		if (settings[it.prefName]) {
			devices += settings[it.prefName]
		}
	}	
	return devices.unique()
}

private getAlertDeviceTypes() {
	return [
		[name: "Carbon Monoxide Detectors", shortName: "Carbon Monoxide", prefName: "alertCarbonMonoxideDetector", prefType: "capability.carbonMonoxideDetector", alarmAttr: "carbonMonoxide", alarmValue: "detected"],
		[name: "Smoke Detectors", shortName: "Smoke", prefName: "alertSmokeDetector", prefType: "capability.smokeDetector", alarmAttr: "smoke", alarmValue: "detected"],
		[name: "Water Sensors", shortName: "Water", prefName: "alertWaterSensors", prefType: "capability.waterSensor", alarmAttr: "water", alarmValue: "wet"]
	]	
}

private hasSecurityDevices() {
	return getSecurityDevices() ? true : false
}

private getSecurityDeviceNames() {
	return getSecurityDevices().collect { it.displayName }.sort()
}

private getSecurityDevices() {
	def devices = []	
	getSecurityDeviceTypes().each {
		if (settings[it.prefName]) {
			devices += settings[it.prefName]
		}
	}	
	return devices.unique()
}

private getSecurityDeviceTypes() {
	return [
		[name: "Contact Sensors", shortName: "Contact", prefName: "securityContactSensors", prefType: "capability.contactSensor", alarmAttr: "contact", alarmValue: "open"],
		[name: "Motion Sensors", shortName: "Motion", prefName: "securityMotionSensors", prefType: "capability.motionSensor", alarmAttr: "motion", alarmValue: "active"]
	]
}

private hasArmDisarmDevices(capabilityName=null) {
	def items
	if (capabilityName) {
		items = getArmDisarmDeviceNames(capabilityName)
	}
	else {
		items = getArmDisarmDevices()
	}
	return items ? true : false
}

private getArmDisarmDeviceNames(capabilityName=null) {
	def names = []
	getArmDisarmDevices().each { 
		if (!capabilityName || it.hasCapability(capabilityName)) {
			names << it.displayName
		}
	}
	return names.sort()
}

private getArmDisarmDevices() {
	def devices = []	
	getArmDisarmDeviceTypes().each {
		if (settings[it.prefName]) {			
			devices += settings[it.prefName]
		}
	}	
	return devices.unique()
}

private getArmDisarmDeviceTypes() {
	return [
		[name: "Switches", shortName: "Switch", prefName: "armDisarmSwitches", prefType: "capability.switch", attrName: "switch", attrValues: ["on", "off"]],
		[name: "Buttons", shortName: "Button", prefName: "armDisarmButtons", prefType: "capability.button", attrName: "button", attrValues: ["pushed", "held"]],
		[name: "Presence Sensors", shortName: "PresenceSensor", prefName: "armDisarmPresenceSensors", prefType: "capability.presenceSensor", attrName: "presence", attrValues: ["present", "not present"]]
	]
}

private hasConfiguredNotifications(notificationType, securityModeId) {	
	return getSecurityModeNotificationDeviceNames(notificationType, securityModeId) ? true : false
}

private getSecurityModeNotificationDeviceNames(notificationType, securityModeId) {
	def names = []
	getSecurityModes().each { secMode ->		
		if (!securityModeId || securityModeId == secMode?.id) {
			// def configName = "has${secMod?.id}${notificationType}Notifications"
			// state.configSummary."$configName" = false
			
			getNotificationSettingNames(notificationType, secMode?.id).each {				
				if (settings["$it"]) {
					//state.configSummary."$configName" = true
					//logTrace "${it}: ${settings[it]}"
					names += settings["$it"]
				}
			}
		}
	}
	return names
}

private getNotificationSettingNames(notificationType, securityModeId) {
	//log.debug "getNotificationSettingNames($notificationType, $securityModeId)"
	
	def prefix = "${securityModeId}${notificationType}"
	
	return ["${prefix}SendPush", "${prefix}SendSMS", "${prefix}Siren", "${prefix}Strobe", "${prefix}SirenStrobe", "${prefix}AlarmTurnOffAfter", "${prefix}SwitchOn", "${prefix}SwitchOff", "${prefix}Speak", "${prefix}PlayText", "${prefix}PlayTrack", "${prefix}Volume", "${prefix}TakePhoto"]	
}

private getNotificationDeviceNames(cmd, capability) {
	def names = []
	getNotificationDevices().each { 	
		if ((!cmd || it.hasCommand(cmd)) && (!capability || it.hasCapability(capability))) {
			names << it.displayName
		}	
	}
	return names.sort()
}

private getNotificationDevices() {
	def devices = []
	getNotificationDeviceTypes().each {
		if (settings[it.prefName]) {
			devices += settings[it.prefName]
		}
	}
	return devices.unique()	
}

private getNotificationDeviceTypes() {
	return [
		[name: "Alarm", prefName: "notificationAlarms", prefType: "capability.alarm"],
		[name: "Audio Notification", prefName: "notificationAudioNotificationGenerators", prefType: "capability.audioNotification"],
		[name: "Image Capture", prefName: "notifictionImageCapture", prefType: "capability.imageCapture"],		
		[name: "Music Player", prefName: "notificationMusicPlayers", prefType: "capability.musicPlayer"],
		[name: "Speech Synthesis", prefName: "notificationSpeechSynthesizers", prefType: "capability.speechSynthesis"],
		[name: "Switch", prefName: "notificationSwitches", prefType: "capability.switch"],
		[name: "Tone", prefName: "notificationToneGenerators", prefType: "capability.tone"]		
	]
}

private getSMSNotificationPhoneNumbers() {
	def phoneNumbers = []
	getSMSNotificationTypes().each {
		if (settings[it.prefName]) {
			phoneNumbers += settings[it.prefName]
		}
	}
	return phoneNumbers.unique()	
}

private getSMSNotificationTypes() {
	return [
		[name: "SMS Phone Number 1", prefName: "notificationPhone1", prefType: "phone"],
		[name: "SMS Phone Number 2", prefName: "notificationPhone2", prefType: "phone"],
		[name: "SMS Phone Number 3", prefName: "notificationPhone3", prefType: "phone"]
	]
}

private getSecurityModeNames(includeDisarmed=true) {
	return getSecurityModes(includeDisarmed).collect { it.name }
}

private getSecurityModes(includeDisarmed=true) {
	def items = [
		[id: "away", name: "Armed (Away)"],
		[id: "sleep", name: "Armed (Sleep)"],
		[id: "stay", name: "Armed (Stay)"]
	]
	if (includeDisarmed) {
		items << [id: "disarmed", name: "Disarmed"]
	}
	return items
}

private getAlarmSystemStatuses() {
	return [
		[id: "away", name: "Armed (away)"],
		[id: "stay", name: "Armed (stay)"],
		[id: "off", name: "Disarmed"]
	]
}

// // private alertCapabilities() {
	// // [
		// // [
			// // name: "Carbon Monoxide Detector",
			// // prefType: "carbonMonoxideDetector",
			// // attributeName: "carbonMonoxide",
			// // activeState: "detected"
		// // ],
		// // [
			// // name: "Smoke Detector",
			// // prefType: "smokeDetector",
			// // attributeName: "smoke",
			// // activeState: "detected",
			// // imageOnly: true
		// // ],
		// // [
			// // name: "Water Sensor",
			// // prefType: "waterSensor",
			// // attributeName: "water",
			// // activeState: "wet"
		// // ]
	// // ]
// // }

// // private securityCapabilities() {
	// // [
		// // [
			// // name: "Contact Sensor",
			// // prefType: "contactSensor",
			// // attributeName: "contact",
			// // activeState: "open"
		// // ],	
		// // [
			// // name: "Motion Sensor", 
			// // prefType: "motionSensor",
			// // attributeName: "motion",
			// // activeState: "active"
		// // ]
	// // ]
// // }

// // private notificationCapabilities() {
	// // [
		// // [
			// // name: "Alarm",
			// // activeState: "off",
			// // imageOnly: true
		// // ]
	// // ]
// // }



// def displaySettingsPage() {
	// dynamicPage(name:"displaySettingsPage") {
		// section ("Display Options") {
			// paragraph "All the capabilities supported by the selected devices are shown on the main screen by default, but this field allows you to limit the list to specific capabilities." 
			// input "enabledCapabilities", "enum",
				// title: "Display Which Capabilities?",
				// multiple: true,
				// options: getCapabilitySettingNames(false),
				// required: false
		// }		
		// section ("Device Capability Exclusions") {
			// paragraph "The capability pages display all the devices that support the capability by default, but these fields allow you to exclude devices from each page."
			// input "lastEventExcludedDevices",
				// "enum",
				// title: "Exclude these devices from the Last Events page:",
				// multiple: true,
				// required: false,
				// options:getExcludedDeviceOptions(null)
			// capabilitySettings().each {
				// input "${getPrefName(it)}ExcludedDevices",
					// "enum",
					// title: "Exclude these devices from the ${getPluralName(it)} page:",
					// multiple: true,
					// required: false,
					// options: getDisplayExcludedDeviceOptions(it)
			// }	
		// }
	// }
// }

// private getDisplayExcludedDeviceOptions(cap) {
	// def devices = []	
	// getDevicesByCapability(getCapabilityName(cap)).each { 
		// if (deviceMatchesSharedCapability(it, cap)) {
			// devices << it.displayName
		// }
	// }	
	// return devices?.sort()
// }

// // Page for defining thresholds used for icons and notifications
// def thresholdsPage() {
	// dynamicPage(name:"thresholdsPage") {		
		// section() {
			// paragraph "The thresholds specified on this page are used to determine icons in the SmartApp and when to send notifications."			
		// }
		// section("Battery Thresholds") {
			// input "lowBatteryThreshold", "number",
				// title: "Enter Low Battery %:",
				// multiple: false,
				// defaultValue: 25			
		// }
		// section("Temperature Thresholds") {
			// input "lowTempThreshold", "number",
				// title: "Enter Low Temperature:",
				// required: false,
				// defaultValue: 63
			// input "highTempThreshold", "number",
				// title: "Enter High Temperature:",
				// required: false,
				// defaultValue: 73			
		// }
		// section("Last Event Thresholds") {
			// input "lastEventThreshold", "number",
				// title: "Last event should be within:",
				// required: false,
				// defaultValue: 7
			// input "lastEventThresholdUnit", "enum",
				// title: "Choose unit of time:",
				// required: false,
				// defaultValue: "days",
				// options: ["seconds", "minutes", "hours", "days"]			
		// }
	// }
// }

// // Page for SMS and Push notification settings
// def notificationsPage() {
	// dynamicPage(name:"notificationsPage") {
		// section ("Notification Settings") {
			// paragraph "When notifications are enabled, notifications will be sent when the device value goes above or below the threshold specified in the Threshold Settings."				
			
			// input "sendPush", "bool",
				// title: "Send Push Notifications?", 
				// required: false
			// input("recipients", "contact", title: "Send notifications to") {
				// input "phone", "phone", 
					// title: "Send text message to",
					// description: "Phone Number", 
					// required: false
      // }
			// mode title: "Only send Notifications for specific mode(s)",
				// required: false
			// input "maxNotifications", "number",
				// title: "Enter maximum number of notifications to receive within 5 minutes:",
				// required: false
		// }
		// section ("Battery Notifications") {			
			// input "batteryNotificationsEnabled", "bool",
				// title: "Send battery notifications?",
				// defaultValue: false,
				// required: false
			// input "batteryNotificationsRepeat", "number",
				// title: "Send repeat notifications every: (hours)",
				// defaultValue: 0,
				// required: false
			// input "batteryNotificationsExcluded", "enum",
				// title: "Exclude these devices from battery notifications:",
				// multiple: true,
				// required: false,
				// options: getExcludedDeviceOptions("Battery")
		// }
		// section ("Temperature Notifications") {
			// input "temperatureNotificationsEnabled", "bool",
				// title: "Send Temperature Notifications?",
				// defaultValue: false,
				// required: false
			// input "temperatureNotificationsRepeat", "number",
				// title: "Send repeat notifications every: (hours)",
				// defaultValue: 0,
				// required: false
			// input "temperatureNotificationsExcluded", "enum",
				// title: "Exclude these devices from temperature notifications:",
				// multiple: true,
				// required: false,
				// options: getExcludedDeviceOptions("Temperature Measurement")
		// }
		// section ("Last Event Notifications") {
			// input "lastEventNotificationsEnabled", "bool",
				// title: "Send Last Event notification?",
				// defaultValue: false,
				// required: false
			// input "lastEventNotificationsRepeat", "number",
				// title: "Send repeat notifications every: (hours)",
				// defaultValue: 0,
				// required: false
			// input "lastEventNotificationsExcluded", "enum",
				// title: "Exclude these devices from last event notifications:",
				// multiple: true,
				// required: false,
				// options: getExcludedDeviceOptions(null)
		// }
	// }
// }

// // Page for Polling settings
// def pollingPage() {
	// dynamicPage(name:"pollingPage") {
		// section ("Polling Settings") {
			// paragraph "If you enable the polling feature, the devices that support the Polling Capability will be polled at a regular interval."
			// paragraph "Polling your devices too frequently can cause them to stop responding or miss other commands that get sent to it."
			// input "pollingEnabled", "bool",
				// title: "Polling Enabled",
				// defaultValue: false,
				// required: false
			// input "pollingInterval", "number",
				// title: "How often should the devices be polled? (minutes)\n(Must be between 5 and ${6 * 24 * 60})",
				// defaultValue: (4 * 60),
				// range: "5..${6 * 24 * 60}",
				// required: false
		// }
		// section("Polling Restrictions") {
			// input "pollingExcluded", "enum",
				// title: "Exclude these devices from Polling",
				// multiple: true,
				// required: false,
				// options: getExcludedDeviceOptions("Polling")
		// }
	// }
// }

// private getExcludedDeviceOptions(capabilityName) {
	// if (capabilityName) {
		// getDevicesByCapability(capabilityName).collect { it.displayName }?.sort()
	// }
	// else {
		// getAllDevices().collect { it.displayName }?.sort()
	// }
// }

// // Page for misc preferences.
// def otherSettingsPage() {
	// dynamicPage(name:"otherSettingsPage") {		
		// section ("Other Settings") {
			// label(name: "label",
				// title: "Assign a name",
				// required: false)
			// input "iconsEnabled", "bool",
				// title: "Display Device State Icons?",
				// defaultValue: true,
				// required: false
			// input "condensedViewEnabled", "bool",
				// title: "Condensed View Enabled?",
				// defaultValue: false,
				// required: false				
		// }
		// section ("Sorting") {
			// input "batterySortByValue", "bool",
				// title: "Sort by Battery Value?",
				// defaultValue: false,
				// required: false
			// input "tempSortByValue", "bool",
				// title: "Sort by Temperature Value?",
				// defaultValue: false,
				// required: false
			// input "lastEventSortByValue", "bool",
				// title: "Sort by Last Event Value?",
				// defaultValue: false,
				// required: false			
		// }	
		// section ("Last Event Accuracy") {
			// input "lastEventAccuracy", "number",
				// title: "Accuracy Level (1-25)\n(Setting this to a higher number will improve the accuracy for devices that generate a lot of events, but if you're seeing timeout errors in Live Logging, you should set this to a lower number.)",
				// defaultValue: 15,
				// range: "1..25",
				// required: false		
			// input "lastEventByStateEnabled", "bool",
				// title: "Advanced Last Event Check Enabled?\n(When enabled, the devices events and state changes are used to determine the most recent activity.)",
				// defaultValue: true,
				// required: false
		// }
		// section ("Logging") {
			// input "logging", "enum",
				// title: "Types of messages to log:",
				// multiple: true,
				// required: false,
				// defaultValue: ["debug", "info"],
				// options: ["debug", "info", "trace"]
		// }
		// section ("Resources") {			
			// paragraph "If you want to be able to use different icons, fork krlaframboise's GitHub Resources repository and change this url to the forked path.  If you do change this setting, make sure that the new location contains all the Required Files."
			// href "", title: "View Required Resource List", 
				// style: "external", 
				// url: 			"http://htmlpreview.github.com/?https://github.com/krlaframboise/Resources/blob/master/simple-device-viewer/required-resources.html"
			// input "resourcesUrl", "text",
				// title: "Resources Url:",
				// required: false,
				// defaultValue: getResourcesUrl()
		// }
		// section ("Scheduling") {
			// paragraph "Leave this field empty unless you're using an external timer to turn on a switch at regular intervals.  If you select a switch, the application will check to see if notifications need to be sent when its turned on instead of using SmartThings scheduler to check every 5 minutes."

			// input "timerSwitch", "capability.switch",
				// title: "Select timer switch:",
				// required: false
		// }		
	// }
// }

// def dashboardSettingsPage() {
	// dynamicPage(name:"dashboardSettingsPage") {
		// section ("Dashboard Settings") {
			// if (state.endpoint) {
				// log.info "Dashboard Url: ${api_dashboardUrl()}"
				// input "dashboardRefreshInterval", "number", 
					// title: "Dashboard Refresh Interval: (seconds)",
					// defaultValue: 300,
					// required: false
				// input "dashboardDefaultView", "enum",
					// title: "Default View:",
					// required: false,
					// options: getCapabilitySettingNames(true)
				// input "dashboardMenuPosition", "enum", 
					// title: "Menu Position:", 
					// defaultValue: "Top of Page",
					// required: false,
					// options: ["Top of Page", "Bottom of Page"]
				// input "customCSS", "text",
					// title:"Enter CSS rules that should be appended to the dashboard's CSS file.",
					// required: false
				// getPageLink("disableDashboardPageLink",
					// "Disable Dashboard",
					// "disableDashboardPage")
			// }
			// else {
				// getPageLink("enableDashboardPageLink",
					// "Enable Dashboard",
					// "enableDashboardPage")
			// }
		// }
	// }
// }



// private disableDashboardPage() {	
	// dynamicPage(name: "disableDashboardPage", title: "") {
		// section() {
			// if (state.endpoint) {
				// try {
					// revokeAccessToken()
				// }
				// catch (e) {
					// logDebug "Unable to revoke access token: $e"
				// }
				// state.endpoint = null
			// }	
			// paragraph "The Dashboard has been disabled! Tap Done to continue"	
		// }
	// }
// }

// private enableDashboardPage() {
	// dynamicPage(name: "enableDashboardPage", title: "") {
		// section() {
			// if (initializeAppEndpoint()) {
				// paragraph "The Dashboard is now enabled. Tap Done to continue"
			// } 
			// else {
				// paragraph "Please go to your SmartThings IDE, select the My SmartApps section, click the 'Edit Properties' button of the Simple Device Viewer app, open the OAuth section and click the 'Enable OAuth in Smart App' button. Click the Update button to finish.\n\nOnce finished, tap Done and try again.", title: "Please enable OAuth for Simple Device Viewer", required: true, state: null
			// }
		// }
	// }
// }



// // Lists all devices and their last event times.
// def lastEventPage() {
	// dynamicPage(name:"lastEventPage") {		
		// section ("Time Since Last Event") {
			// href(
				// name: "refreshLastEventLink", 
				// title: "Refresh Data",
				// description: "${getRefreshLastEventLinkDescription()}",
				// page: "refreshLastEventPage",
				// required: false
			// )
			// getParagraphs(getAllDeviceLastEventListItems()?.unique())			
		// }		
	// }
// }

// private getRefreshLastEventLinkDescription() {
	// def stateRefreshed = (state.stateCachedTime) ? getTimeSinceLastActivity(new Date().time - state.stateCachedTime) : "?"
	// def eventsRefreshed = (state.eventCachedTime) ? getTimeSinceLastActivity(new Date().time - state.eventCachedTime) : "?"
	// return "Events refreshed ${eventsRefreshed.toLowerCase()} ago.\nState refreshed ${stateRefreshed.toLowerCase()} ago."
// }

// def refreshLastEventPage() {
	// dynamicPage(name:"refreshLastEventPage") {		
		// section () {
			// refreshDeviceEventCache()
			// refreshDeviceStateCache()
			// paragraph "The last event times have been refreshed."
		// }		
	// }
// }

// // Lists all devices supporting switch capability as links that can be used to toggle their state
// def toggleSwitchPage(params) {
	// dynamicPage(name:"toggleSwitchPage") {		
		// section () {
			// paragraph "Wait a few seconds before pressing Done to ensure that the previous page refreshes correctly."
			// if (params.deviceId) {
				// def device = params.deviceId ? getAllDevices().find { it.id == params.deviceId } : null
				// def newState = device?.currentSwitch == "off" ? "on" : "off"
				// paragraph toggleSwitch(device, newState)
			// }
			// else {
				// getDevicesByCapability("Switch").each {
					// paragraph toggleSwitch(it, "off")
				// }
			// }			
		// }		
	// }
// }

// private toggleSwitch(device, newState) {
	// if (device) {	
		// if (newState == "on") {
			// device.on()
		// }
		// else {
			// device.off()
		// }		
		// return "Turned ${device.displayName} ${newState.toUpperCase()}"
	// }
// }

// // Lists all devices and all the state of all their capabilities
// def capabilityPage(params) {
	// dynamicPage(name:"capabilityPage") {	
		// def capSetting = params.capabilitySetting ? params.capabilitySetting : state.lastCapabilitySetting
		
		// if (capSetting) {
			// state.lastCapabilitySetting = capSetting
			// section("${getPluralName(capSetting)}") {
				// if (capSetting.name in ["Switch","Light"]) {
					// href(
						// name: "allOffSwitchLink", 
						// title: "Turn Off All ${getPluralName(capSetting)}",
						// description: "",
						// page: "toggleSwitchPage",
						// required: false
					// )
					// getSwitchToggleLinks(getDeviceCapabilityListItems(capSetting))
				// }
				// else {				
					// getParagraphs(getDeviceCapabilityListItems(capSetting))
				// }
			// }
		// }
		// else {
			// section("All Selected Capabilities") {
				// getParagraphs(getAllDevices().collect { 
					// getDeviceAllCapabilitiesListItem(it) 
				// })
			// }
		// }			
	// }
// }

// private getSwitchToggleLinks(listItems) {
	// listItems.sort { it.sortValue }	
	// return listItems.unique().each {
		// href(
			// image: it.image ? it.image : "",
			// name: "switchLink${it.deviceId}", 
			// title: "${it.title}",
			// description: "",
			// page: "toggleSwitchPage", 
			// required: false,
			// params: [deviceId: it.deviceId]
		// )
	// }
// }

// private getParagraphs(listItems) {
	// listItems.sort { it.sortValue }
	// if (!condensedViewEnabled) {
		// return listItems.unique().each { 
			// it.image = it.image ? it.image : ""
			// paragraph image: "${it.image}",	"${it.title}"
		// }
	// }
	// else {
		// def content = null
		// listItems.unique().each { 
			// content = content ? content.concat("\n${it.title}") : "${it.title}"
		// }
		// if (content) {
			// paragraph "$content"
		// }
	// }
// }

// private getCapabilityPageLink(cap) {
	// return href(
		// name: cap ? "${getPrefName(cap)}Link" : "allDevicesLink", 
		// title: cap ? "${getPluralName(cap)}" : "All Devices - States",
		// description: "",
		// page: "capabilityPage",
		// required: false,
		// params: [capabilitySetting: cap]
	// )	
// }

// // Checks if any devices have the specificed capability
// private devicesHaveCapability(name) {	
	// return getAllDevices().find { it.hasCapability(name) } ? true : false
// }

// private getDevicesByCapability(name, excludeList=null) {
	// removeExcludedDevices(getAllDevices()
		// .findAll { it.hasCapability(name.toString()) }
		// .sort() { it.displayName.toLowerCase() }, excludeList)	
// }

// private getDeviceAllCapabilitiesListItem(device) {
	// def listItem = [
		// sortValue: device.displayName
	// ]	
	// getSelectedCapabilitySettings().each {
		// //if (device.hasCapability(getCapabilityName(it))) {
			// listItem.status = (listItem.status ? "${listItem.status}, " : "").concat(getDeviceCapabilityStatusItem(device, it).status)
		// //}
	// }
	// listItem.title = getDeviceStatusTitle(device, listItem.status)
	// return listItem
// }

// private getDeviceCapabilityListItems(cap) {
	// def items = []
	// getDevicesByCapability(getCapabilityName(cap), settings["${getPrefName(cap)}ExcludedDevices"])?.each { 
		// if (deviceMatchesSharedCapability(it, cap)) {
			// items << getDeviceCapabilityListItem(it, cap)
		// }
	// }
	// return items
// }

// private deviceMatchesSharedCapability(device, cap) {
	// if (cap.name in ["Switch", "Light"]) {
		// def isLight = (lightDevices?.find { it.id == device.id }) ? true : false				
		// return ((cap.name == "Light") == isLight)
	// }
	// else {
		// return true
	// }
// }

// private getDeviceCapabilityListItem(device, cap) {
	// def listItem = getDeviceCapabilityStatusItem(device, cap)
	// listItem.deviceId = "${device.id}"
	// if (listItem.image && cap.imageOnly && !condensedViewEnabled) {
		// listItem.title = "${device.displayName}"
	// }
	// else {
		// listItem.title = "${getDeviceStatusTitle(device, listItem.status)}"
	// }
	// listItem
// }

// private getCapabilitySettingByPrefName(prefName) {
	// capabilitySettings().find { getPrefName(it) == prefName }
// }

// private getCapabilitySettingByPluralName(pluralName) {
	// capabilitySettings().find { getPluralName(it)?.toLowerCase() == pluralName?.toLowerCase()}
// }

// private getCapabilitySettingByName(name) {
	// capabilitySettings().find { it.name == name }
// }

// private getAllDeviceLastEventListItems() {
	// removeExcludedDevices(getAllDevices(), lastEventExcludedDevices)?.collect {
		// getDeviceLastEventListItem(it)		
	// }
// }

// private getDeviceLastEventListItem(device) {
	// def now = new Date().time
	// def lastActivity = getDeviceLastActivity(device)
	// def lastEventTime = lastActivity?.time ?: 0
	
	// def listItem = [
		// value: lastEventTime ? now - lastEventTime : Long.MAX_VALUE,
		// status: lastEventTime ? "${getTimeSinceLastActivity(now - lastEventTime)}" : "N/A",
		// deviceId: device.deviceNetworkId
	// ]
	
	// listItem.title = getDeviceStatusTitle(device, listItem.status)
	// listItem.sortValue = settings.lastEventSortByValue ? listItem.value : device.displayName
	// listItem.image = getLastEventImage(lastEventTime)
	// return listItem
// }

// private getDeviceLastActivity(device) {
	// def activity = getDeviceCache(device.deviceNetworkId)?.activity
	// if (activity?.size()) {
		// return activity.sort { it.time }.last()
	// }
// }

// /*There's currently a bug that limits the number
// of events returned to 50 so this method loops
// through the list until it finds one that has 
// a source containing "DEVICE".*/
// private getDeviceLastDeviceEvent(device) {
	// def totalLoops = safeToInteger(settings.lastEventAccuracy, 5)
	// def startDate = new Date() - 7
	// def endDate = new Date()
	// def lastEvent
	
	// for (int index= 0; index < totalLoops; index++) {
		// def events = device.eventsBetween(startDate, endDate, [max:50]).flatten()
		
		// if (events) {			
			// lastEvent = events?.find { "${it.source}".startsWith("DEVICE") }
		
			// if (lastEvent?.date?.time) {
				// // Found an event with the correct source so stop checking.
				// index = totalLoops
			// }
			// else {
				// // Haven't found an event with the correct so move the
				// // end date so the next 50 events will be retrieved.
				// endDate = events.last()?.date
			// }
		// }
		// else {
			// // Checked all the events so stop checking.
			// index = totalLoops
		// }
	// }
	// if (lastEvent) {		
		// return [
			// name: lastEvent.name,
			// value: lastEvent.value,			
			// time: lastEvent.date.time,
			// type: "event"
		// ]
	// }
// }

// private getDeviceLastStateChange(device) {
	// if (settings.lastEventByStateEnabled != false) {
		
		// def lastState
		// device.supportedAttributes.each {
			// def attributeState = device.currentState("$it")
			// if (attributeState) {
				// if (!lastState || lastState.date.time < attributeState.date.time) {
					// lastState = attributeState
				// }
			// }
		// }
		
		// if (lastState) {
			// return [
				// name: lastState.name,
				// value: lastState.value,
				// time: lastState.date?.time,
				// type: "state"
			// ]				
		// }		
	// }	
// }

// private getTimeSinceLastActivity(ms) {
	// if (ms < msSecond()) {
		// return "$ms MS"
	// }
	// else if (ms < msMinute()) {
		// return "${calculateTimeSince(ms, msSecond())} SECS"
	// }
	// else if (ms < msHour()) {
		// return "${calculateTimeSince(ms, msMinute())} MINS"
	// }
	// else if (ms < msDay()) {
		// return "${calculateTimeSince(ms, msHour())} HRS"
	// }
	// else {
		// return "${calculateTimeSince(ms, msDay())} DAYS"
	// }		
// }

// private calculateTimeSince(ms, divisor) {
	// return "${((float)(ms / divisor)).round()}"
// }

// private String getDeviceStatusTitle(device, status) {
	// if (!status || status == "null") {
		// status = "N/A"
	// }
	// if (state.refreshingDashboard) {
		// return device.displayName
	// }
	// else {
		// return "${status?.toUpperCase()} -- ${device.displayName}"
	// }	
// }

// private getDeviceCapabilityStatusItem(device, cap) {
	// try {
		// return getCapabilityStatusItem(cap, device.displayName, device.currentValue(getAttributeName(cap)).toString())		
	// }
	// catch (e) {
		// log.error "Device: ${device?.displayName} - Capability: $cap - Error: $e"
		// return [
			// image: "",
			// sortValue: device?.displayName,
			// value: "",
			// status: "N/A"
		// ]
	// }
// }

// private getCapabilityStatusItem(cap, sortValue, value) {
	// def item = [
		// image: "",
		// sortValue: sortValue,
		// value: value
	// ]
	// item.status = item.value
	// if ("${item.status}" != "null") {
	
		// if (item.status == getActiveState(cap) && !state.refreshingDashboard) {
			// item.status = "*${item.status}"
		// }
		
		// switch (cap.name) {
			// case "Battery":			
				// item.status = "${item.status}%"
				// item.image = getBatteryImage(item.value)
				// if (batterySortByValue) {
					// item.sortValue = safeToInteger(item.value)
				// }				
				// break
			// case "Temperature Measurement":
				// item.status = "${item.status}°${location.temperatureScale}"
				// item.image = getTemperatureImage(item.value)
				// if (tempSortByValue) {
					// item.sortValue = safeToInteger(item.value)
				// }
				// break
			// case "Alarm":
				// item.image = getAlarmImage(item.value)
				// break
			// case "Contact Sensor":
				// item.image = getContactImage(item.value)
				// break
			// case "Lock":
				// item.image = getLockImage(item.value)
				// break
			// case "Motion Sensor":
				// item.image = getMotionImage(item.value)
				// break
			// case "Presence Sensor":
				// item.image = getPresenceImage(item.value)
				// break
			// case ["Smoke Detector", "Carbon Monoxide Detector"]:
				// item.image = getSmokeCO2Image(item.value)
				// break
			// case "Switch":
				// item.image = getSwitchImage(item.value)
				// break
			// case "Light":
				// item.image = getLightImage(item.value)
				// break
			// case "Water Sensor":
				// item.image = getWaterImage(item.value)
				// break
		// }
	// }
	// else {
		// item.status = "N/A"
	// }
	// return item
// }

// private getSelectedCapabilitySettings() {	
	// if (!settings.enabledCapabilities) {
		// return capabilitySettings().findAll { devicesHaveCapability(getCapabilityName(it)) }
	// }
	// else {
		// return capabilitySettings().findAll {	(getPluralName(it) in settings.enabledCapabilities) && devicesHaveCapability(getCapabilityName(it)) }
	// }
// }

// private getAllDNIs() {
	// return getAllDevices().collect { it.deviceNetworkId }
// }

// private getAllDevices() {
	// def devices = []
	// getDeviceInputs().each { deviceInput ->
		// settings["$deviceInput"].each { device ->
			// if (!devices.find { it.deviceNetworkId == device.deviceNetworkId }) {
				// devices << device
			// }
		// }
	// }
	// return devices
// }

// private getDeviceInputs() {
	// def deviceInputs = capabilitySettings().collect {
		// "${getPrefName(it)}Devices" 
	// }
	// deviceInputs << "sensors"
	// deviceInputs << "actuators"
	// return deviceInputs
// }

// private boolean isDevice(obj) {
	// try {
		// if (obj?.id) {
			// // This isn't a device if the following line throws an exception.
			// obj.hasCapability("") 
			// return true
		// }
		// else {
			// return false
		// }
	// }
	// catch (e) {
		// return false
	// }
// }

// private String getLastEventImage(lastEventTime) {
	// def status = lastEventIsOld(lastEventTime) ? "warning" : "ok"
	// return getImagePath("${status}.png")
// }

// private boolean lastEventIsOld(lastEventTime) {	
	// try {
		// if (!lastEventTime) {
			// return true
		// }
		// else {
			// return ((new Date().time - getLastEventThresholdMS()) > lastEventTime)
		// }
	// }
	// catch (e) {
		// return true
	// }
// }

// private String getPresenceImage(currentState) {
	// def status = (currentState == "present") ? "present" : "not-present"
	// return getImagePath("${status}.png")
// }

// private String getContactImage(currentState) {
	// return  getImagePath("${currentState}.png")	
// }

// private String getLockImage(currentState) {
	// return  getImagePath("${currentState}.png")	
// }

// private String getMotionImage(currentState) {
	// def status = (currentState == "active") ? "motion" : "no-motion"
	// return  getImagePath("${status}.png")	
// }

// private String getSwitchImage(currentState) {
	// return  getImagePath("switch-${currentState}.png")	
// }

// private String getLightImage(currentState) {
	// return  getImagePath("light-${currentState}.png")
// }

// private String getAlarmImage(currentState) {
	// return  getImagePath("alarm-${currentState}.png")	
// }

// private String getWaterImage(currentState) {
	// return  getImagePath("${currentState}.png")	
// }

// private String getSmokeCO2Image(currentState) {
	// def status = (currentState == "detected") ? "detected" : "clear"
	// return getImagePath("smoke-${status}.png")	
// }

// private String getBatteryImage(batteryLevel) {
	// def status = batteryIsLow(batteryLevel) ? "low" : "normal"
	// return  getImagePath("${status}-battery.png")	
// }

// private String getTemperatureImage(tempVal) {		
	// def status = "normal"
	// if (tempIsHigh(tempVal)) {
		// status = "high"
	// }
	// else if (tempIsLow(tempVal)) {
		// status = "low"
	// }	
	// return getImagePath("${status}-temp.png")
// }

// private String getImagePath(imageName) {
	// if (iconsAreEnabled()) {
		// if (state.refreshingDashboard) {
			// return imageName
		// }
		// else {
			// return "${getResourcesUrl()}/$imageName"
		// }
	// }
// }

// private boolean iconsAreEnabled() {
	// return (iconsEnabled || iconsEnabled == null || state.refreshingDashboard)
// }

// private getResourcesUrl() {
	// def url = "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer"

	// if (settings?.resourcesUrl) {
		// url = settings.resourcesUrl
	// }
	
	// return url
// }

// // Revokes the dashboard access token, if applicable.
// def uninstalled() {
	// if (state.endpoint) {
		// try {
			// logDebug "Revoking dashboard access token"
			// revokeAccessToken()
		// }
		// catch (e) {
			// log.warn "Unable to revoke dashboard access token: $e"
		// }
	// }
// }

// // Resets subscriptions, scheduling and ensures all settings are initialized.
// def updated() {
	// unsubscribe()
	// unschedule()
	// state.refreshingDashboard = false
	
	// if (state.capabilitySettings) {
		// cleanState()
	// }
	
	// initialize()
	
	// logDebug "State Used: ${(state.toString().length() / 100000)*100}%"
// }

// private initialize() {
	// if (!state.sentNotifications) {
		// state.sentNotifications = []
	// }
	
	// if (settings.timerSwitch) {
		// subscribe(timerSwitch, "switch.on", timerSwitchEventHandler)
	// }
	// else {		
		// runEvery5Minutes(performScheduledTasks)
	// }
	
	// initializeDevicesCache()
// }

// // Starting with version 1.9, the capabilitySettings are
// // no longer stored in state so this cleans up the old data.
// private cleanState() {
	// def sentNotifications = state.sentNotifications
	// def devicesCache = state.devicesCache
	// state.clear()
	// state.sentNotifications = sentNotifications
	// state.devicesCache = devicesCache
// }

// // Remove cached data for devices no longer selected and
// // add cached data for newly selected devices.
// void initializeDevicesCache() {
	// def dnis = getAllDNIs()
	
	// state.devicesCache?.removeAll { cache ->
		// !dnis?.find { dni -> cache.dni == dni }
	// }	
// }

// // Used to generate notifications when external timer is being used instead of relying on SmartThings scheduler. 
// def timerSwitchEventHandler(evt) {
	// performScheduledTasks()	
// }

// def performScheduledTasks() {
	// if (canCheckDevices(state.lastDeviceCheck)) {
		// runIn(45, checkDevices)
	// }
	// if (canPollDevices(state.lastDevicePoll)) {
		// runIn(20, refreshDeviceActivityCache)
		// pollDevices()
	// }
	// else {
		// refreshDeviceActivityCache()
	// }
// }

// void pollDevices() {
	// logDebug "Polling Devices"
	// state.lastDevicePoll = new Date().time	
	// getDevicesByCapability("Polling", pollingExcluded)*.poll()
// }

// private canPollDevices(lastPoll) {
	// return settings.pollingEnabled &&
		// timeElapsed((lastPoll ?: 0) + msMinute(safeToInteger(settings.pollingInterval, 5)), true)
// }

// void refreshDeviceActivityCache() {
	// runIn(25, refreshDeviceStateCache)
	// refreshDeviceEventCache()		
// }

// void refreshDeviceStateCache() {
	// refreshDeviceActivityTypeCache("state")
// }

// void refreshDeviceEventCache() {
	// refreshDeviceActivityTypeCache("event")	
// }

// void refreshDeviceActivityTypeCache(activityType) {
	// def cachedTime = new Date().time
	
	// getAllDevices().each { device ->		
		// def lastActivity 
		// if (activityType == "event") {
			// lastActivity = getDeviceLastDeviceEvent(device)
		// }
		// else {
			// lastActivity = getDeviceLastStateChange(device)		
		// }
		
		// if (lastActivity) {
			// lastActivity.cachedTime = cachedTime
			// saveLastActivityToDeviceCache(device.deviceNetworkId, lastActivity)
		// }		
	// }
	// state."${activityType}CachedTime" = cachedTime
// }

// void saveLastActivityToDeviceCache(dni, lastActivity) {
	// def found = false
	// def activity = getDeviceCache(dni).activity.collect {
		// if (it.type == lastActivity.type) {
			// found = true
			// return (it.time < lastActivity.time) ? lastActivity : it
		// }
		// else {
			// return it
		// }
	// }

	// if (!found) {
		// activity << lastActivity
	// }
		
	// getDeviceCache(dni).activity = activity
// }

// private getDeviceCache(dni) {
	// if (!state.devicesCache) {
		// state.devicesCache = []
	// }
	
	// def deviceCache = state.devicesCache.find { cache -> "$dni" == "${cache.dni}" }
	// if (!deviceCache) {
		// deviceCache = [dni: "$dni", activity: [ ]]
		// state.devicesCache << deviceCache
	// }
	// return deviceCache
// }


// // Generates notifications if device attributes fall outside of specified thresholds and ensures that notifications are spaced at least 5 minutes apart.
// def checkDevices() {
	// logDebug "Checking Device Thresholds"
	
	// state.lastDeviceCheck = new Date().time
	// state.currentCheckSent = 0
		
	// if (settings.batteryNotificationsEnabled) {
		// checkBatteries()
	// }			
	// if (settings.temperatureNotificationsEnabled) {
		// checkTemperatures()
	// }			
	// if (settings.lastEventNotificationsEnabled) {
		// checkLastEvents()
	// }	
// }

// private canCheckDevices(lastCheck) {	
	// return (settings.batteryNotificationsEnabled ||
		// settings.temperatureNotificationsEnabled ||
		// settings.lastEventNotificationsEnabled) &&
		// timeElapsed((lastCheck ?: 0) + msMinute(5), true)
// }

// private checkTemperatures() {
	// logDebug "Checking Temperatures"
	// def cap = getCapabilitySettingByName("Temperature Measurement")
	
	// getDevicesByCapability("Temperature Measurement", temperatureNotificationsExcluded)?.each {	
		// def item = getDeviceCapabilityStatusItem(it, cap)
		
		// def message = null
		// if (tempIsHigh(item.value)) {
			// message = "High Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		// }
		// else if (tempIsLow(item.value)) {			
			// message = "Low Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		// }
		
		// handleDeviceNotification(it, message, "temperature", temperatureNotificationsRepeat)
	// }
// }

// private boolean tempIsHigh(val) {
	// isAboveThreshold(val, highTempThreshold, 73)
// }

// private boolean tempIsLow(val) {
	// isBelowThreshold(val, lowTempThreshold, 63)
// }

// private checkBatteries() {
	// logDebug "Checking Batteries"
	// def cap = getCapabilitySettingByName("Battery")

	// getDevicesByCapability("Battery", batteryNotificationsExcluded)?.each {
		// def item = getDeviceCapabilityStatusItem(it, cap)
		
		// def message = batteryIsLow(item.value) ? "Low Battery Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		// handleDeviceNotification(it, message, "battery", batteryNotificationsRepeat)
	// }
// }

// private boolean batteryIsLow(batteryLevel) {
	// isBelowThreshold(batteryLevel, lowBatteryThreshold, 25)
// }

// private boolean isAboveThreshold(val, threshold, int defaultThreshold) {
	// safeToInteger(val) > safeToInteger(threshold, defaultThreshold)	
// }

// private boolean isBelowThreshold(val, threshold, int defaultThreshold) {
	// safeToInteger(val) < safeToInteger(threshold,defaultThreshold)	
// }

// private int safeToInteger(val, defaultVal=0) {
	// try {
		// if (val) {
			// return val.toFloat().round().toInteger()
		// }
		// else if (defaultVal != 0){
			// return safeToInteger(defaultVal, 0)
		// }
		// else {
			// return defaultVal
		// }
	// }
	// catch (e) {
		// logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		// return 0
	// }
// }

// private checkLastEvents() {
	// logDebug "Checking Last Events"
	// removeExcludedDevices(getAllDevices(), lastEventNotificationsExcluded)?.each {
		
		// def item = getDeviceLastEventListItem(it)
		// def message = item.value > getLastEventThresholdMS() ? "Last Event Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		// handleDeviceNotification(it, message, "lastEvent", lastEventNotificationsRepeat)
	// }
// }

// private long getLastEventThresholdMS() {
	// def threshold = lastEventThreshold ? lastEventThreshold : 7
	// def unitMS
	// switch (lastEventThresholdUnit) {
		// case "seconds":
			// unitMS = msSecond()
			// break
		// case "minutes":
			// unitMS = msMinute()
			// break
		// case "hours":
			// unitMS = msHour()
			// break
		// default:
			// unitMS = msDay()
	// }
	// return (threshold * unitMS)
// }

// private long msSecond(multiplier=1) {
	// return (1000 * multiplier)
// }

// private long msMinute(multiplier=1) {
	// return (msSecond(60) * multiplier)
// }

// private long msHour(multiplier=1) {
	// return (msMinute(60) * multiplier)
// }

// private long msDay(multiplier=1) {
	// return (msHour(24) * multiplier)
// }


// private removeExcludedDevices(deviceList, excludeList) {
	// if (excludeList) {
		// def result = []
		// deviceList.each {
			// def displayName = "${it.displayName}"
			// if (!excludeList.find { it == "$displayName" }) {
				// result << it
			// }
		// }
		// return result
	// }
	// else {
		// return deviceList
	// }
// }

// private handleDeviceNotification(device, message, notificationType, notificationRepeat) {
	// def id = "$notificationType${device.id}"
	// def lastSentMap = state.sentNotifications.find { it.id == id }
	// def lastSent = lastSentMap?.lastSent
	// def repeatMS = notificationRepeat ? msHour(notificationRepeat) : 0	
	// def unknownStatus = message?.contains("- N/A --") ? true : false
			
	// if (message && !unknownStatus) {
		// if (canSendNotification(lastSent, repeatMS)){
			// if (lastSent) {
				// lastSentMap.lastSent = new Date().time
			// }
			// else {
				// state.sentNotifications << [id: "$id", lastSent: new Date().time]				
			// }			
			// sendNotificationMessage(message)
		// }
	// }
	// else if (unknownStatus) {
		// // Do nothing because occassionally null is returned for
		// // battery or last event when it really has a value causing
		// // false notifications to be sent out.
	// }
	// else if (lastSent) {
		// state.sentNotifications.remove(lastSentMap)
	// }
// }

// private boolean canSendNotification(lastSent, repeatMS) {	
	// def sendLimitExceeded = state.currentCheckSent >= (maxNotifications ? maxNotifications : 1000)
	
	// if (!lastSent && !sendLimitExceeded) {
		// return true
	// }
	// else {
		// return (!sendLimitExceeded && repeatMS > 0 && timeElapsed(lastSent + repeatMS))
	// }
// }

// private sendNotificationMessage(message) {	
	// if (sendPush || recipients || phone) {
		// state.currentCheckSent = state.currentCheckSent + 1
		// logInfo "Sending $message"
		// if (sendPush) {
			// sendPush(message)
		// }
		// if (location.contactBookEnabled && recipients) {
			// sendNotificationToContacts(message, recipients)
		// } else {
			// if (phone && sendPush) {
				// sendSmsMessage(phone, message)
			// }
			// else if (phone) {
				// sendSms(phone, message)
			// }
		// }
	// }
	// else {
		// logInfo "Could not send message because notifications have not been configured.\nMessage: $message"
	// }
// }

// private boolean timeElapsed(timeValue, nullResult=false) {
	// if (timeValue != null) {
		// def currentTime = new Date().time
		// return (timeValue <= currentTime)
	// } else {
		// return nullResult
	// }
// }

// private getCapabilitySettingNames(includeEvents) {
	// def items = []
	// if (includeEvents) {
		// items << "Events"
	// }
	// items += capabilitySettings().collect { getPluralName(it) }?.unique()
	// return items.sort()
// }

// private getCapabilityName(capabilitySetting) {
	// capabilitySetting.capabilityName ?: capabilitySetting.name
// }

// private String getAttributeName(capabilitySetting) {
	// capabilitySetting.attributeName ?: capabilitySetting.name.toLowerCase()
// }

// private String getActiveState(capabilitySetting) {
	// capabilitySetting.activeState ?: capabilitySetting.name.toLowerCase()
// }

// private String getPrefName(capabilitySetting) {
	// capabilitySetting.prefName ?: getPrefType(capabilitySetting)
// }

// private String getPrefType(capabilitySetting) {
	// capabilitySetting.prefType ?: capabilitySetting.name.toLowerCase()
// }

// private String getPluralName(capabilitySetting) {
	// capabilitySetting.pluralName ?: "${capabilitySetting.name}s"
// }


// private capabilitySettings() {
	// [	
		// [
			// name: "Alarm",
			// activeState: "off",
			// imageOnly: true
		// ],
		// [
			// name: "Battery",
			// pluralName: "Batteries"
		// ],
		// [
			// name: "Carbon Monoxide Detector",
			// prefType: "carbonMonoxideDetector",
			// attributeName: "carbonMonoxide",
			// activeState: "detected",
			// imageOnly: true
		// ],
		// [
			// name: "Contact Sensor",
			// prefType: "contactSensor",
			// attributeName: "contact",
			// activeState: "open",
			// imageOnly: true
		// ],		
		// [
			// name: "Light",
			// prefName: "light",
			// prefType: "switch",
			// capabilityName: "Switch",
			// attributeName: "switch",
			// activeState: "on",		
			// imageOnly: true
		// ],
		// [
			// name: "Lock",
			// activeState: "locked",
			// imageOnly: true
		// ],		
		// [
			// name: "Motion Sensor", 
			// prefType: "motionSensor",
			// attributeName: "motion",
			// activeState: "active",
			// imageOnly: true
		// ],
		// [
			// name: "Presence Sensor",
			// prefType: "presenceSensor",
			// attributeName: "presence",
			// activeState: "present",
			// imageOnly: true
		// ],
		// [
			// name: "Smoke Detector",
			// prefType: "smokeDetector",
			// attributeName: "smoke",
			// activeState: "detected",
			// imageOnly: true
		// ],
		// [
			// name: "Switch",
			// pluralName: "Switches",		
			// activeState: "on",
			// imageOnly: true
		// ],		
		// [
			// name: "Temperature Measurement",
			// pluralName: "Temperature Sensors",
			// prefType: "temperatureMeasurement",
			// attributeName: "temperature"
		// ],		
		// [
			// name: "Water Sensor",
			// prefType: "waterSensor",
			// attributeName: "water",
			// activeState: "wet",
			// imageOnly: true
		// ]
	// ]
// }



// /********************************************
// *    Dashboard
// ********************************************/
// private initializeAppEndpoint() {	
	// if (!state.endpoint) {
		// try {
			// def accessToken = createAccessToken()
			// if (accessToken) {
				// state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")
			// }
		// } 
		// catch(e) {
			// state.endpoint = null
		// }
	// }
	// logDebug "Dashboard Url: ${api_dashboardUrl()}"	
	// return state.endpoint
// }

// mappings {
	// path("/dashboard") {action: [GET: "api_dashboard"]}
	// path("/dashboard/:capability") {action: [GET: "api_dashboard"]}	
	// path("/dashboard/:capability/:cmd") {action: [GET: "api_dashboard"]}
	// path("/dashboard/:capability/:cmd/:deviceId") {action: [GET: "api_dashboard"]}	
// }

// private api_dashboardUrl(capName=null) {	
	// def pageName
	// capName = capName ?: api_getDefaultCapabilityName()
	// if (capName?.toLowerCase() == "events") {
		// pageName = "events"
	// }
	// else {		
		// def cap = getCapabilitySettingByPluralName(capName)
		// pageName = (cap ? getPluralName(cap)?.toLowerCase()?.replace(" ", "-") : "") ?: "lights"		
	// }
	// return "${state.endpoint}dashboard/${pageName}"
// }

// private api_getDefaultCapabilityName() {
	// if (settings?.dashboardDefaultView) {
		// return settings.dashboardDefaultView
	// }
	// else {
		// return "events"
	// }	
// }

// def api_dashboard() {
	// def cap
	// def currentUrl
	// def menu = ""
	// def header = ""
	// def footer = ""
	// def refreshInterval = 300
	// def html = ""

	// try {
		// state.refreshingDashboard = true
		// header = api_getPageHeader()		
			
		// if (params.capability == "events") {
			// currentUrl = api_dashboardUrl("events")
			// header = api_getPageHeader("Events")
		// }
		// else if (params.capability) {			
			// cap = params.capability ? getCapabilitySettingByPluralName(params.capability?.replace("-", " ")) : null
		
			// currentUrl = api_dashboardUrl(getPluralName(cap))
			// header = api_getPageHeader("${getPluralName(cap)}")
		// }	
		
		// if (!params.capability && state.normalRefreshInterval) {
			// currentUrl = api_dashboardUrl(null)
			// state.normalRefreshInterval = false	// Prevents fast refresh loop
			// refreshInterval = 0			
		// }
		// else {
			// refreshInterval = api_getRefreshInterval(params.cmd)
		// }
		
		// menu = api_getMenuHtml(currentUrl)
		// footer = api_getPageFooter(null, currentUrl)
		
		// if (params.capability == "events") {
			// html = api_getItemsHtml(getAllDeviceLastEventListItems()?.unique())
		// }
		// else if (cap) {
			// html = api_getCapabilityHtml(cap, currentUrl, params.deviceId, params.cmd)
		// }
		
		// html = "<section>$html</section>"
	// }
	// catch(e) {
		// log.error "Unable to load dashboard:\n$e"
		// html = api_getPageErrorHtml(e)
	// }
	// state.refreshingDashboard = false
	// return api_renderHtmlPage(api_getPageBody(header, html, menu, footer), currentUrl, refreshInterval)
// }

// private api_getRefreshInterval(cmd) {
	// if (api_isToggleSwitchCmd(cmd) && state.normalRefreshInterval) {
		// state.normalRefreshInterval = false // Prevents fast refresh loop
		// return 3
	// }
	// else {
		// state.normalRefreshInterval = true
		// return settings.dashboardRefreshInterval ?: 300
	// }
// }

// private api_getCapabilityHtml(cap, currentUrl, deviceId, cmd) {	
	// def html = ""
	// if (api_isToggleSwitchCmd(cmd)) {		
		// if (deviceId) {
			// html = "<h1>${api_toggleSwitch(cap, deviceId, cmd)}</h1>"
		// }
		// else {
			// html = api_toggleSwitches(cap, cmd)
		// }
	
		// html = "<div class=\"command-results\">$html</div>"		
	// }			
	
	// if (cap.name in ["Switch","Light", "Alarm"]) {
		// html += api_getToggleItemsHtml(currentUrl, getDeviceCapabilityListItems(cap))
	// }
	// else {
		// html += api_getItemsHtml(getDeviceCapabilityListItems(cap))
	// }
	// return html
// }

// private api_isToggleSwitchCmd(cmd) {
	// return (cmd in ["on", "off", "toggle"])
// }

// private api_getMenuHtml(currentUrl) {
	// def className = api_menuAtTop() ? "top" : "bottom"
	// def html = "<nav class=\"$className\">"
	
	// html += api_getMenuItemHtml("Refresh", "refresh", currentUrl)
	
	// html += api_getMenuItemHtml("Events", "warning", api_dashboardUrl("events"))
	
	// getSelectedCapabilitySettings().each {
		// html += api_getMenuItemHtml(getPluralName(it), getPrefName(it), api_dashboardUrl(getPluralName(it)))
	// }
	
	// html += "</nav>"
	// return html
// }

// private api_getMenuItemHtml(linkText, className, url) {
	// return "<div class=\"menu-item\"><a href=\"$url\" ${api_getWaitOnClickAttr()} class=\"item-image $className\"><span>${linkText}</span></a></div>"
// }

// private api_toggleSwitches(cap, cmd) {
	// def html = ""	
	
	// getDeviceCapabilityListItems(cap).each {
		// html += "<li>${api_toggleSwitch(cap, it.deviceId, cmd)}</li>"
	// }
	
	// if (html) {
		// return "<h1>The following changes were made:</h1><ul>$html</ul>"
	// }
	// else {
		// return "<h1>No Changes Were Made</h1>"
	// }	
// }

// private api_toggleSwitch(cap, deviceId, cmd) {
	// def device = deviceId ? getAllDevices().find { it.id == deviceId } : null
		
	// if (device) {
		// def newState = api_getNewSwitchState(device, cmd)
		// if (newState) {
			// return toggleSwitch(device, newState)
		// }		
		// else {
			// return "Unable to determine new switch state for ${device.displayName}"
		// }
	// }
	// else {
		// return "Unable to find a device with id ${deviceId}"
	// }		
// }

// private api_getNewSwitchState(device, cmd) {
	// if (cmd in ["on", "off"]) {
		// return cmd
	// }
	// else if (cmd == "toggle") {
		// return device?.currentSwitch == "off" ? "on" : "off"
	// }
	// else {
		// return ""
	// }
// }

// private api_getToggleItemsHtml(currentUrl, listItems) {
	// def html = ""
			
	// listItems.unique().each {		
		// html += api_getItemHtml(it.title, it.image, "${currentUrl}/toggle/${it.deviceId}", it.deviceId, it.status)
	// }
	
	// def pluralName
	// def imageName	
	// if (listItems) {
		// pluralName = listItems[0] ? getPluralName(listItems[0]) : ""
		// imageName = listItems[0]?.image?.replace(".png","")
		// imageName = imageName?.replace("-on", "")?.replace("-off", "")
	// }	
	
	// if (imageName in ["light", "switch"]) {
		// html += api_getItemHtml("Turn All Off", "${imageName}-off all-command", "${currentUrl}/off", "", "")	
		
		// html += api_getItemHtml("Turn All On", "${imageName}-on all-command", "${currentUrl}/on", "", "")
	// }
	// return html
// }

// private api_getItemsHtml(listItems) {
	// def html = ""		
	
	// listItems.sort { it.sortValue }
	// listItems.unique().each {				
		// html += api_getItemHtml(it.title, it.image, null, it.deviceId, it.status)
	// }
	// return html
// }

// private api_getItemHtml(text, imageName, url, deviceId, status) {
	// def imageClass = imageName ? imageName?.replace(".png", "") : ""
	// def deviceClass = deviceId ? deviceId?.replace(" ", "-") : "none"
	// def html 
	
	// if (url) {
		// html = "<a class=\"item-text\" href=\"$url\" ${api_getWaitOnClickAttr()}><span class=\"label\">$text</span></a>"
	// }
	// else {
		// html = "<div class=\"item-text\"><span class=\"label\">$text</span></div>"		
	// }
	
	// html = "<div class=\"item-image-text\"><div class=\"item-image $imageClass\"><span class=\"item-status\">$status</span></div>$html</div>"
	
	// return "<div class=\"device-item device-id-$deviceClass\">$html</div>"
// }

// private api_getWaitOnClickAttr() {
	// return "onclick=\"displayWaitMsg(this)\""
// }

// private api_getPageBody(header, content, menu, footer) {
	// if (api_menuAtTop()) {
		// return "$header$menu$content$footer"
	// }
	// else {
		// return "$header$content$menu$footer"		
	// }
// }

// private api_menuAtTop() {
	// return (settings.dashboardMenuPosition != "Bottom of Page")
// }

// private api_getPageHeader(html=null) {
	// def header = "Simple Device Viewer"
	// header += html ? " - $html" : ""
	// return "<header>$header</header>"
// }

// private api_getPageFooter(html, currentUrl) {
	// html = html ?: ""
	// return "<footer>$html<textarea class=\"dashboard-url\" rows=\"2\">${currentUrl}</textarea></footer>"
// }

// private api_getPageErrorHtml(e) {
	// return "<div class=\"error-message\"><h1>Unable to Load Dashboard</h1><h2>Error Message:</h2><p>$e</p><p><a href=\"${api_dashboardUrl()}\">Back to Default Dashboard</a></p></div>"		
// }

// private api_renderHtmlPage(html, url, refreshInterval) {
	// render contentType: "text/html", 
		// data: "<!DOCTYPE html><html lang=\"en\"><head><title>Simple Device Viewer - Dashboard</title><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><meta http-equiv=\"refresh\" content=\"${refreshInterval}; URL=${url}\">${api_getCSS()}</head><body>${html}${api_getJS()}</body></html>"
// }

// private api_getJS() {	
	// return "<script>function displayWaitMsg(link) { link.className += \" wait\"; }</script>"
// }

// private api_getCSS() {
	// // return "<link rel=\"stylesheet\" href=\"${getResourcesUrl()}/dashboard.css\">"
	
	// def css = "body {	font-size: 100%;	text-align:center;	font-family:Helvetica,arial,sans-serif;	margin:0 0 10px 0;	background-color: #000000;}header, nav, section, footer {	display: block;	text-align:center;}header {	margin: 0 0 0 0;	padding: 4px 0 4px 0;	width: 100%;		font-weight: bold;	font-size: 100%;	background-color:#808080;	color:#ffffff;}nav.top{	padding-top: 0;}nav.bottom{	padding: 4px 4px 4px 4px;}section {	padding: 10px 20px 40px 20px;}.command-results {	background-color: #d6e9c6;	margin: 0 20px 20px 20px;	padding: 10px 20px 10px 20px;	border-radius: 100px;}.command-results h1 {	margin: 0 0 0 0;}.command-results ul {	list-style: none;}.command-results li {	line-height: 1.5;	font-size: 120%;}.dashboard-url {	display:block;	width:100%;	font-size: 80%;}.device-id-none{	background-color: #d6e9c6 !important;}.refresh {	background-image: url('refresh.png');}.alarm, .alarm-both {	background-image: url('alarm-both.png');}.alarm-siren {	background-image: url('alarm-siren.png');}.alarm-strobe {	background-image: url('alarm-strobe.png');}.alarm-off {	background-image: url('alarm-off.png');}.battery, .normal-battery {	background-image: url('normal-battery.png');}.low-battery {	background-image: url('low-battery.png');}.open {	background-image: url('open.png');}.contactSensor, .closed {	background-image: url('closed.png');}.light, .light-on {	background-image: url('light-on.png');}.light-off {	background-image: url('light-off.png');}.lock, .locked{	background-image: url('locked.png');}.unlocked {	background-image: url('unlocked.png');}.motionSensor, .motion {	background-image: url('motion.png');}.no-motion {	background-image: url('no-motion.png');}.presenceSensor, .present {	background-image: url('present.png');}.not-present {	background-image: url('not-present.png');}.smokeDetector, .smoke-detected {	background-image: url('smoke-detected.png');}.smoke-clear {	background-image: url('smoke-clear.png');}.switch, .switch-on {	background-image: url('switch-on.png');}.switch-off {	background-image: url('switch-off.png');}.temperatureMeasurement, .normal-temp {	background-image: url('normal-temp.png');}.low-temp {	background-image: url('low-temp.png');}.high-temp {	background-image: url('high-temp.png');}.waterSensor, .dry {	background-image: url('dry.png');}.wet {	background-image: url('wet.png');}.ok {	background-image: url('ok.png');}.warning {	background-image: url('warning.png');}.device-item {	width: 200px;	display: inline-block;	background-color: #ffffff;	margin: 2px 2px 2px 2px;	padding: 4px 4px 4px 4px;	border-radius: 5px;}.item-image-text {	position: relative;	height: 75px;	width:100%;	display: table;}.item-image {	display: table-cell;	position: relative;	width: 35%;	border: 1px solid #cccccc;	border-radius: 5px;	background-repeat:no-repeat;	background-size:auto 70%;	background-position: center bottom;}.item-status {	width: 100%;	font-size:75%;	display:inline-block;}.item-text {	display: table-cell;	width: 65%;	position: relative;	vertical-align: middle;}a.item-text {	color:#000000;}.item-text.wait, .menu-item a.wait{	color:#ffffff;	background-image:url('wait.gif');	background-repeat:no-repeat;	background-position: center bottom;}.item-text.wait{	background-size:auto 100%;}.label {	display:inline-block;	vertical-align: middle;	line-height:1.4;	font-weight: bold;	padding-left:4px;}.menu-item {	display: inline-block;	background-color:#808080;	padding:4px 4px 4px 4px;	border:1px solid #000000;	border-radius: 5px;	font-weight:bold;}.menu-item .item-image{	display:table-cell;	background-size:auto 45%;	height:50px;	width:75px;	border:0;	border-radius:0;}.menu-item .item-image.switch,.menu-item .item-image.light,.menu-item .item-image.battery,.menu-item .item-image.alarm,.menu-item .item-image.refresh {	background-size:auto 60%;}.menu-item a, .menu-item a:link, .menu-item a:hover, .menu-item a:active,.menu-item a:visited {	color: #ffffff;		text-decoration:none;}.menu-item:hover, .menu-item:hover a, .menu-item a:hover { 	background-color:#ffffff;	color:#000000 !important;}.menu-item span {	width: 100%;	font-size:75%;	display:inline-block;}@media (max-width: 639px){	.device-item {		width:125px;	}	.item-image-text {		height: 65px;	}	.item-image {		background-size: auto 60%;	}	.item-text .label {		font-size: 80%;		line-height: 1.2;	}}"
	
	// css = css.replace("url('", "url('${getResourcesUrl()}/")
	
	// if (settings?.customCSS) {
		// css += settings.customCSS
	// }
	// return "<style>$css</style>"
// }

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

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}