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
			section("Armed Zones") {
				def armedZones = ""
				getZones(false)?.sort{ it.displayName }?.each {
					if (it.armed) {
						armedZones += "${it.displayName}\n"
					}
				}
				paragraph armedZones ?: "None"
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

	config.hasAllRequirements = config.hasAlertOrSecurityDevices && config.hasConfiguredAlertOrSecurityNotifications && config.hasZones

	//logTrace("<--- DEVICES --->\n  hasAlertDevices: ${config.hasAlertDevices}\n  hasSecurityDevices: ${config.hasSecurityDevices}\n  hasAlertOrSecurityDevices: ${config.hasAlertOrSecurityDevices}\n  hasNotificationDevices: ${config.hasNotificationDevices}\n  hasZones: ${config.hasZones}\n  hasRequiredDevices: ${config.hasRequiredDevices}\n  hasAllRequirements: ${config.hasAllRequirements}")

	//logTrace("<--- NOTIFICATIONS --->\n  hasConfiguredAlertNotifications: ${config.hasConfiguredAlertNotifications}\n  hasConfiguredSecurityNotifications: ${config.hasConfiguredSecurityNotifications}")

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
	logInfo("Changing Security Mode to ${newSecurityMode?.name}")
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

	logDebug("State Used: ${(state.toString().length() / 100000)*100}%")
}

def initialize() {
	state.params = [:]
	armZones()
	initializeMonitoredSecurityDevices()
	initializeArmDisarmTriggers()

}

def armZones() {
	logDebug("Arming/Disarming Zones for Security Mode ${state?.securityMode?.name}")
	def status = "Zone Status:\n"
	getZones(false).sort { it.displayName }.each {
		if (state.securityMode != "Disarmed" && state.securityMode?.name in settings."${it.settingName}EnabledSecurityModes") {
			it.armed = true
		}
		else {
			it.armed = false
		}
		state."${it.armedStateName}" = it.armed
		status += "${it.displayName}: ${it.armed ? 'Armed' : 'Disarmed'}\n"		
	}
	logDebug(status)
}

private initializeArmDisarmTriggers() {
	def status = "Arming/Disarming Subscriptions:\n"
	if (getSecurityModeSettings(null, "ArmDisarmAlarmSystemStatuses")) {
		status += "Smart Home Monitor Status\n"
		subscribe(location, "alarmSystemStatus", armDisarmAlarmSystemStatusChangedHandler)
	}

	if (getSecurityModeSettings(null, "ArmDisarmModes")) {
		status += "Location Mode Changes\n"
		subscribe(location, "mode", armDisarmModeChangedHandler)
	}

	getArmDisarmDevices().each { device ->
		getArmDisarmDeviceTypes().each { type ->
			def canSubscribe = false

			type.attrValues.each { attrValue ->
				def settingName = "${type.prefName?.capitalize()}${attrValue?.replace(' ', '')?.capitalize()}"
				
				if (device.displayName in getSecurityModeSettings(null, "$settingName")) {
					canSubscribe = true
				}
			}

			if (canSubscribe) {
				status += "${device.displayName}: ${type.attrName?.capitalize()} Event\n"
				subscribe(device, "${type.attrName}", armDisarmDeviceEventHandler)
			}
		}
	}
	logTrace(status)
}

def armDisarmAlarmSystemStatusChangedHandler(evt) {
	def status = getAlarmSystemStatuses().find { it.id == evt.value }
	logInfo("SHM changed to ${status?.name}")
	
	def newSecurityMode = getSecurityModes().find {
		(status?.name in settings["${it.id}ArmDisarmAlarmSystemStatuses"])
	}
	
	if (newSecurityMode) {
		changeSecurityMode(newSecurityMode)
	}	
}

def armDisarmModeChangedHandler(evt) {
	logInfo("Location Mode changed to ${evt.value}")
	def newSecurityMode = getSecurityModes().find {
		(evt.value in settings["${it.id}ArmDisarmModes"])
	}
	if (newSecurityMode) {
		changeSecurityMode(newSecurityMode)
	}	
}

def armDisarmDeviceEventHandler(evt) {
	logInfo("${evt.displayName}: ${evt.name} changed to ${evt.value}")
		
	def type = getArmDisarmDeviceTypes().find { it.attrName == evt.name}
	if (type) {
		def newSecurityMode = getSecurityModes().find {
			(evt.displayName in settings[getSecurityModeSettingName(it.id, type.prefName, evt.value)])
		}
		changeSecurityMode(newSecurityMode)
	}	
}

private getSecurityModeSettingName(securityModeId, partialSettingName, attrValue) {
	partialSettingName = partialSettingName?.capitalize() ?: ""
	
	if (attrValue != null) {
		partialSettingName = "${partialSettingName}${attrValue?.replace(' ', '')?.capitalize()}"
	}
	
	return "${securityModeId}${partialSettingName}"
}

private getSecurityModeSettings(securityModeId, partialSettingName) {
	def result = []
	partialSettingName = partialSettingName?.capitalize() ?: ""
		
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
	def status = "Security Device Subscriptions:\n"
	getArmedSecurityDevices()?.sort { it.displayName }?.each { device ->
		getSecurityDeviceTypes().each { type ->
			if (device.hasAttribute("${type.alarmAttr}")) {				
				status += "${device.displayName}: ${type.alarmAttr?.capitalize()} Event\n"
				subscribe(device, "${type.alarmAttr}.${type.alarmValue}", "security${type.shortName}Handler")
			}
		}
	}
	logTrace(status)
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
	logInfo("${evt.displayName}: Contact is ${evt.value}")
	handleSecurityNotifications("Security", evt)
}

def securityMotionHandler(evt) {
	logInfo("${evt.displayName}: Motion is ${evt.value}")
	handleSecurityNotifications("Security", evt)
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
				logTrace("Sending Push message \"$message\"")
				sendPush(message)
			}
			else if (prefName.endsWith("SendSMS")) {
				prefValue.each { phone ->
					logTrace("Sending SMS message \"$message\" to $phone")
					sendSms(phone, message)
				}
			}
			else if (!prefName.endsWith("Volume") && !prefName.endsWith("AlarmTurnOffAfter")) {
				def devices = findNotificationDevices(prefValue)
				if (devices) {
					switch (prefName.replace(namePrefix, "")) {
						case { it in ["Siren", "Strobe", "SirenAndStrobe"] }:
							if (alarmAutoOffSeconds) {
								logTrace("Scheduling Alarm to turn off in ${alarmAutoOffSeconds} seconds.")
								runIn(alarmAutoOffSeconds, turnOffAlarm)
							}
						case "Siren":
							logTrace("Executing siren() on $prefValue")
							devices*.siren()
							break
						case "Strobe":
							logTrace("Executing strobe(): $prefValue")
							devices*.strobe()
							break
						case "SirenStrobe":
							logTrace("Executing both on $prefValue")
							devices*.both()
							break
						case "SwitchOn":
							logTrace("Turning on $prefValue")
							devices*.on()
							break
						case "SwitchOff":
							logTrace("Turning off $prefValue")
							devices*.off()
							break
						case "Speak":
							logTrace("Speak \"${zoneMessage}\" on $prefValue")
							devices*.speak(zoneMessage)
							break
						case "PlayText":
							logTrace("Playing Text \"${zoneMessage}\" on ${prefValue} at volume ${volume}")
							devices*.playText(zoneMessage, volume)
							break
						case "PlayTrack":
							logTrace("Playing Track \"${zoneMessage}\" on ${prefValue} at volume ${volume}")
							devices*.playTrack(zoneMessage, volume)
							break
						case "TakePhoto":
							logTrace("Taking Photo with ${prefValue}")
							devices*.take()
							break
						default:
							logDebug("Unknown Notification - $prefName: $prefValue")
					}
				}
				else {
					logDebug("Unable to find devices for $prefName: $prefValue")
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
	logTrace("Turning Off Alarms")
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
			getNotificationSettingNames(notificationType, secMode?.id).each {
				if (settings["$it"]) {
					names += settings["$it"]
				}
			}
		}
	}
	return names
}

private getNotificationSettingNames(notificationType, securityModeId) {
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