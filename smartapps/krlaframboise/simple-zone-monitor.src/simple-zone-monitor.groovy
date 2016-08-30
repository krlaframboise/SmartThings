/**
 *  Simple Zone Monitor v0.0.0 [ALPHA]
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *
 *  Changelog:
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
    name: "Simple Zone Monitor",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Monitors safety/security devices and performs actions when they're triggered.",
    category: "Safety & Security",
		iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-zone-monitor/app-SimpleZoneMonitor.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-zone-monitor/app-SimpleZoneMonitor@2x.png")

 preferences {
	page(name:"mainPage", title: "Simple Zone Monitor")
	page(name:"settingsPage", title: "Settings")
	page(name:"changeStatusPage", title: "Change Monitoring Status")
	page(name:"statusesPage", title: "Monitoring Statuses")
  page(name:"devicesPage", title: "Devices")
	page(name:"zoneGroupsPage", title: "Zone Groups")
	page(name:"editZoneGroupPage", title: "Zone Group Details")
	page(name:"zonesPage", title: "Zones")
	page(name:"editZonePage", title: "Zone Details")
	page(name:"statusZonesPage", title: "Monitoring Status Zones")
	page(name:"editStatusZonesPage", title: "Monitoring Status Zones")
	//page(name:"refreshZonesPage", title: "Refresh Zones")
	page(name:"safetyNotificationsPage", title: "Safety Notifications")
	page(name:"securityNotificationsPage", title: "Security Notifications")
	page(name:"statusNotificationsPage", title: "Monitoring Status Notifications")
	page(name:"armDisarmPage", title: "Arming/Disarming Options")
	page(name:"statusArmDisarmPage")
	page(name:"advancedOptionsPage", title: "Advanced Options")
	page(name:"statusAdvancedOptionsPage")
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		if (state.installed) {

			def config = getConfigSummary()
			if (config.hasAllRequirements) {
				section("Change Monitoring Status") {
					getStatuses(true, true).each {
						if (it.name != state.status) {
							def imageName = (it.name == state.status?.name) ? "selected.png" : "unselected.png"
							getPageLink("${it.id}ChangeStatusLink",
								"${it.name}",
								"changeStatusPage",
								[status: it],
								"",
								"$imageName")
						}
					}
				}

				def armedZones = ""
				getZones(false)?.sort{ it.displayName }?.each {
					if (it.armed) {
						armedZones += "${it.displayName}\n"
					}
				}
				if (armedZones) {
					section("Armed Zones") {
						getParagraph(armedZones ?: "None", "armed.png")
					}
				}
				section("Settings") {
					getPageLink("settingsPageLink",
						"Settings",
						"settingsPage",
						null,
						"",
						"settings.png")
				}
			}
			else {
				section() {
					getWarningParagraph("The application can't Arm Zones while there are unconfigured settings.")					
				}
				getSettingsPageContent(config)
			}
		}
		else {
			section() {
				state.installed = true
				getParagraph("Installation Complete.\n\nPlease tap Done, exit the Marketplace, and open Simple Zone Monitor from your installed SmartApp list.", "app-SimpleZoneMonitor@2x.png")
			}
		}
	}
}

def settingsPage() {
	dynamicPage(name:"settingsPage") {
		getSettingsPageContent(getConfigSummary())
	}
}

private getSettingsPageContent(config) {
	def unconfiguredDesc = "(unconfigured) "
	section("Master Settings") {
		getPageLink("statusLink",
			"Choose Monitoring Statuses",
			"statusesPage",
			null,
			config.hasStatuses ? "" : unconfiguredDesc)
		getPageLink("devicesLink",
			"Choose Devices",
			"devicesPage",
			null,
			config.hasSafetyOrSecurityDevices ? "" : unconfiguredDesc)
	}
	section("Zone Settings") {
		if (config.hasSafetyOrSecurityDevices) {
			getPageLink("zoneGroupsLink",
				"Zone Groups",
				"zoneGroupsPage")
			getPageLink("zonesLink",
				"Zones",
				"zonesPage",
				null,
				config.hasZones ? "" : unconfiguredDesc)
			if (config.hasZones && config.hasStatuses) {
				getPageLink("statusZonesLink",
					"Monitoring Status Zones",
					"statusZonesPage",
					null,
					config.hasStatusZones ? "" : unconfiguredDesc)
			}
			else {
				getWarningParagraph("Monitoring Status Zones can't be setup until Monitoring Statuses have been selected and \"Zones\" have been created.")
			}
		}
		else {
			getWarningParagraph("Zones can't be setup until at least one \"Safety Device to Monitor\" or \"Security Device to Monitor\" has been chosen.")
		}
	}
	section("Notification Settings") {
		if (config.hasStatuses) {
			getPageLink("safetyNotificationsLink",
				"Safety Notifications",
				"safetyNotificationsPage",
				null,
				config.hasConfiguredSafetyNotifications ? "" : (config.hasConfiguredSecurityNotifications ? "(not set)" : "(unconfigured)"))
			getPageLink("securityNotificationsLink",
				"Security Notifications",
				"securityNotificationsPage",
				null,
				config.hasConfiguredSecurityNotifications ? "" : (config.hasConfiguredSafetyNotifications ? "(not set)" : "(unconfigured)"))
		}
		else {
			getWarningParagraph("Notifications can't be configured until there's at least one \"Active Monitoring Status\" has been chosen.")
		}
	}
	section("Arming/Disarming and Advanced Options") {
		if (config.hasAllRequirements) {
			getPageLink("armDisarmLink",
				"Arming/Disarming",
				"armDisarmPage")
			getPageLink("advancedOptionsLink",
				"Delays / Beeping / Device Exclusions",
				"advancedOptionsPage")
		}
		else {
			getWarningParagraph("Arming/Disarming and Advanced Options can't be setup until \"Zones\" and \"Notifications\" have been configured.")
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

def getConfigSummary() {
	def config = [:]

	config.hasSafetyDevices = hasSafetyDevices()
	config.hasSecurityDevices = hasSecurityDevices()
	config.hasZones = hasZones()
	config.hasStatuses = hasStatuses()
	config.hasStatusZones = hasStatusZones()
	
	config.hasSafetyOrSecurityDevices = (config.hasSafetyDevices || config.hasSecurityDevices)

	config.hasConfiguredSafetyNotifications = hasConfiguredNotifications("Safety", null)

	config.hasConfiguredSecurityNotifications = hasConfiguredNotifications("Security", null)

	config.hasConfiguredSafetyOrSecurityNotifications = config.hasConfiguredSafetyNotifications || config.hasConfiguredSecurityNotifications

	config.hasAllRequirements = config.hasSafetyOrSecurityDevices && config.hasConfiguredSafetyOrSecurityNotifications && config.hasStatuses && config.hasZones && config.hasStatusZones && config.hasStatuses

	//logTrace("<--- DEVICES --->\n  hasSafetyDevices: ${config.hasSafetyDevices}\n  hasSecurityDevices: ${config.hasSecurityDevices}\n  hasSafetyOrSecurityDevices: ${config.hasSafetyOrSecurityDevices}\n  hasNotificationDevices: ${config.hasNotificationDevices}\n  hasZones: ${config.hasZones}\n  hasRequiredDevices: ${config.hasRequiredDevices}\n  hasAllRequirements: ${config.hasAllRequirements}")

	//logTrace("<--- NOTIFICATIONS --->\n  hasConfiguredSafetyNotifications: ${config.hasConfiguredSafetyNotifications}\n  hasConfiguredSecurityNotifications: ${config.hasConfiguredSecurityNotifications}")
	state.configSummary = config
	return config
}

def changeStatusPage(params) {
	dynamicPage(name:"changeStatusPage") {
		section() {
			changeStatus(params.status)
			paragraph "Monitoring Status Changed to ${state.status?.name}"
		}
	}
}

private changeStatus(newStatus) {
	logInfo("Changing Monitoring Status to ${newStatus?.name}")
	state.status = newStatus
	initialize()
}

def statusesPage() {
	dynamicPage(name:"statusesPage") {
		section("Choose Monitoring Statuses") {
			getInfoParagraph("The Monitoring Status determines which zones are armed and which notifications to use when an Intrusion or Safety Alert occurs.")
									
			input "selectedStatuses", "enum",
					title: "Only the Monitoring Statuses you select below will be visible throughout the rest of the SmartApp.",
					multiple: true,
					required: true,
					options: getOptionalStatusNames()
		}
		section("Required Monitoring Statuses") {
			getInfoParagraph("Disarmed and Disabled are Monitoring Statuses used throughout the SmartApp, but they're not listed above because they provide additional functionality and can't be hidden.")
			paragraph "Disarmed: Security Devices won't be monitored, but notifications can still be performed for Safety Devices."
			paragraph "Disabled: Security and Safety Devices won't be monitored and the Arm/Disarm Triggers are disabled.  When the Monitoring Status is set to Disabled, the only way to change it is through the SmartApp."
		}
	}
}

def devicesPage() {
	dynamicPage(name:"devicesPage") {
		section("Safety Devices to Monitor") {
			getSafetyDeviceTypes().each {
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
		section("Notification Devices") {
			getNotificationDeviceTypes().each {
				input "${it.prefName}", "${it.prefType}",
					title: "${it.name}:",
					multiple: true,
					required: false
			}
		}
		section("SMS Phone Numbers") {
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
					[zoneGroup: it],
					getZoneGroupSummary(it))
			}
		}
	}
}  

private getZoneGroupSummary(zoneGroup) {
	def summary = ""
	getZones(false)?.sort { it.name }?.each { zone ->
		if (zone?.zoneGroupName == zoneGroup.name) {
			summary += summary ? "\n" : ""
			summary += "${zone.name}"
		}
	}	
	return summary ?: "  > (No Zones)"
}

def editZoneGroupPage(params) {
	dynamicPage(name:"editZoneGroupPage") {
		section("Edit Zone Group") {
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
				getWarningParagraph("You can only have 25 Zone Groups")
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
			getZones(false).each {
				hasZones = true
				getPageLink("${it.settingName}Link",
					"${it.displayName}",
					"editZonePage",
					[zone: it],
					getZoneSummary(it))
			}
		}

		// if (hasZones) {
			// section("Zone Changes") {
				// paragraph "Zone changes that effect the Armed state won't get applied until you exit the application.  To apply these changes immediately, tap the Refresh Zones button below."
				// getPageLink("$refreshZonesLink",
					// "Refresh Zones",
					// "refreshZonesPage")
			// }
		// }
	}
}

private getZoneSummary(zone) {
	def summary = ""	
	if (zone?.status) {
		summary += summary ? "\n" : ""
		summary = "Zone Status: ${zone?.status}"
	}
	settings["${zone.settingName}SafetyDevices"]?.each {
		summary += summary ? "\n" : ""
		summary += "Safety: ${it}"
	}
	settings["${zone.settingName}SecurityDevices"]?.each {
		summary += summary ? "\n" : ""
		summary += "Security: ${it}"
	}	
	return summary ?: "(not set)"
}

// def refreshZonesPage(params) {
	// dynamicPage(name:"refreshZonesPage") {
		// section() {
			// initialize()
			// paragraph "Zones refreshed successfully"
		// }
	// }
// }

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
			section("Safety Settings") {
				input "${zone.settingName}SafetyDevices", "enum",
					title: "Safety Devices:",
					multiple: true,
					required: false,
					options: getSafetyDeviceNames()

				getSafetyDeviceTypes().each {
					def attr = it.alarmAttr
					if (getSafetyDevices().find { it.hasAttribute(attr) }) {
						input "${zone.settingName}${it.prefName}Message", "text",
							title: "${it.shortName} Notification Message:",
							required: false
					}
				}
			}
			section("Security Settings") {
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
			}
			if (zone.name) {
				section() {
					paragraph "You can Delete this Zone by clearing the Zone Name field and tapping Done."
				}
			}
		}
		else {
			section() {
				getWarningParagraph("You can only have 100 Zones")
			}
		}
	}
}

def statusZonesPage() {
	dynamicPage(name:"statusZonesPage") {
		section() {
			getInfoParagraph("Specify zones that are armed for the different Monitoring Statuses.")
			getStatuses(true).each {
				getPageLink("${it.id}StatusZonesLink",
					"${it.name}",
					"editStatusZonesPage",
					[status: it],
					getStatusZonesSummary(it))
			}
		}
	}
}

private getStatusZonesSummary(status) {
		def summary = ""
	
	summary = appendStatusSettingSummary(summary, status.id, "StatusZones", "%")
		
	return summary ?: "(not set)"
}

def editStatusZonesPage(params) {
	dynamicPage(name:"editStatusZonesPage") {
		if (params?.status) {
			state.params.status = params.status
		}

		def id = state.params?.status?.id
		def name = state.params?.status?.name

		section("${name} - Zones") {
			getInfoParagraph("Select the Zones that should be armed while the Monitoring Status is ${name}.")
		}
		
		section() {
			input "${id}StatusZones", "enum",
				title: "Select Zones:",
				multiple: true,
				required: false,
				options: getZoneNames()
		}
	}
}

def safetyNotificationsPage() {
	dynamicPage(name:"safetyNotificationsPage") {
		section() {
			getInfoParagraph("Setup Safety Notifications for each Monitoring Status.")
			if (!state.configSummary?.hasSafetyDevices) {
				getWarningParagraph("These notifications won't get executed because no Safety Devices are being monitored.")
			}

			getStatuses(true).each {
				getPageLink("${it.id}SafetyNotificationsLink",
					"${it.name}",
					"statusNotificationsPage",
					[status: it, notificationType: "Safety"],
					hasConfiguredNotifications("Safety", it.id) ? "" : "(not set)")
			}
		}
	}
} 

def securityNotificationsPage() {
	dynamicPage(name:"securityNotificationsPage") {
		section() {
			getInfoParagraph("Setup Security Notifications for each Monitoring Status.")
			if (!state.configSummary?.hasSecurityDevices) {
				getWarningParagraph("These notifications won't get executed because no Security Devices are being monitored.")
			}

			getStatuses(false).each {
				getPageLink("${it.id}SecurityNotificationsLink",
					"${it.name}",
					"statusNotificationsPage",
					[status: it, notificationType: "Security"],
					hasConfiguredNotifications("Security", it.id) ? "" : "(not set)")
			}
		}
	}
} 

def statusNotificationsPage(params) {
	dynamicPage(name:"statusNotificationsPage") {
		if (params?.status) {
			state.params.status = params.status
			state.params.notificationType = params.notificationType
		}

		def id = state.params?.status?.id
		def name = state.params?.status?.name
		def type = state.params?.notificationType

		section("$name - $type Notifications") {
			getInfoParagraph("Setup $type Notifications for Monitoring Status $name")
		}

		section("Push Notifications") {
			input "${id}${type}SendPush", "bool",
				title: "Send Push Notifications?",
				defaultValue: false,
				required: false
		}
		section("SMS Notifications") {
			def smsOptions = getSMSNotificationPhoneNumbers()
			if (smsOptions) {
				input "${id}${type}SendSMS", "enum",
					title: "Send SMS Messages:",
					multiple: true,
					required: false,
					options: smsOptions
			}
			else {
				getInfoParagraph("You can't use SMS Notifications until you enter at least one \"SMS Phone Number\" into the \"SMS Phone Numbers\" section of the \"Choose Devices\" page.")
			}
		}
		section("Alarm Notifications") {
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
		section("Switch Notifications") {
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
		section("Audio Notifications") {
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
		section("Photo Notifications") {
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
			getInfoParagraph("Specify triggers that activate the different Monitoring Statuses.")
			getStatuses(true).each {
				getPageLink("${it.id}ArmDisarmLink",
					"${it.name}",
					"statusArmDisarmPage",
					[status: it],
					getStatusArmDisarmSummary(it))
			}
		}
	}
}

private getStatusArmDisarmSummary(status) {
	def summary = ""
	def settingValue = getStatusSettings(status.id, "ArmDisarmSmartHomeMonitorStates")
	if (settingValue) {
		summary = "Smart Home Monitor: $settingValue"
	}
	
	settingValue = getStatusSettings(status.id, "ArmDisarmModes")
	if (settingValue) {
		summary += summary ? "\n" : ""
		summary += "Location Mode: $settingValue"
	}
	
	getArmDisarmDeviceTypes().each { type ->			
		type.attrValues.each { attrValue ->
			def settingName = "${type.prefName?.capitalize()}${attrValue?.replace(' ', '')?.capitalize()}"
			settingValue = getStatusSettings(status.id, "$settingName")
			
			if (settingValue) {
				summary += summary ? "\n" : ""
				summary += "${type.name.capitalize()} ${attrValue.capitalize()}: $settingValue"
			}
		}
	}	
	return summary ?: "(not set)"
}

def statusArmDisarmPage(params) {
	dynamicPage(name:"statusArmDisarmPage") {
		if (params?.status) {
			state.params.status = params.status
		}

		def id = state.params?.status?.id
		def name = state.params?.status?.name

		section("${name} - Arming/Disarming Options") {
			getInfoParagraph("Specify triggers that will cause the Monitoring Status to change to ${name}.")
		}

		section("Location Mode") {
			input "${id}ArmDisarmModes", "mode",
				title: "When Mode changes to",
				multiple: true,
				required: false,
				submitOnChange: true
		}

		section("Smart Home Monitor") {
			input "${id}ArmDisarmSmartHomeMonitorStates", "enum",
				title: "When Smart Home Monitor changes to",
				multiple: true,
				required: false,
				options: getSmartHomeMonitorStates().collect{ it.name },
				submitOnChange: true
		}

		if (hasArmDisarmDevices()) {

			section("Switches") {
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

			// section("Buttons") {
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

			section("Presence Sensors") {
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
				getInfoParagraph("You can't use devices to trigger Arming/Disarming until you select at least one \"Arming/Disarming Trigger Device\" from the \"Choose Devices\" page.")
			}
		}
	}
}

def advancedOptionsPage() {
	dynamicPage(name:"advancedOptionsPage") {
		section() {
			getInfoParagraph("Exclude Devices, configure Entry/Exit Delays, and Beeping Options for each Monitoring Status.")
			getStatuses(true).each {				
				getPageLink("${it.id}advancedOptionsLink",
					"${it.name}",
					"statusAdvancedOptionsPage",
					[status: it],
					getStatusAdvancedOptionsSummary(it))
			}
		}
	}
}

private getStatusAdvancedOptionsSummary(status) {
	def summary = ""
	
	summary = appendStatusSettingSummary(summary, status.id, "ExcludedDevices", "Exclude: %")
		
	def entryExitDelay = settings["${status.id}EntryExitDelay"]
	if (entryExitDelay) {
		summary = appendStatusSettingSummary(summary, status.id, "EntryExitDevices", "Delayed ${entryExitDelay}s: %")
		
		summary = appendStatusSettingSummary(summary, status.id, "EntryExitBeepingEnabled", "Entry/Exit Beeping: %")
		
		summary = appendStatusSettingSummary(summary, status.id, "EntryExitBeepFrequency", "Beep Frequency: % Seconds")		
	}
	
	summary = appendStatusSettingSummary(summary, status.id, "ConfirmationBeepEnabled", "Beep on Monitoring Status change: %")
	
	summary = appendStatusSettingSummary(summary, status.id, "BeepDevices", "Play beep on %")
					
	return summary ?: "(not set)"
}

def statusAdvancedOptionsPage(params) {
	dynamicPage(name:"statusAdvancedOptionsPage") {
		if (params?.status) {
			state.params.status = params.status
		}
		def id = state.params?.status?.id
		def name = state.params?.status?.name

	section("${name} - Advanced Options") {
			getInfoParagraph("Configure Advanced Options for Monitoring Status ${name}.")
		}
		section("Exclude Devices") {
			input "${id}ExcludedDevices", "enum",
				title: "Don't Monitor These Security Devices:",
				multiple: true,
				required: false,
				options: getSecurityDeviceNames(),
				submitOnChange: true
		}
		section("Entry/Exit Delay") {
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
		section("Beep Options") {
			def beepDeviceNames = getNotificationDeviceNames("beep", null)
			if (beepDeviceNames) {
				input "${id}EntryExitBeepingEnabled", "bool",
					title: "Beep during entry/exit delay?",
					defaultValue: false,
					required: false,
					submitOnChange: true
				input "${id}ConfirmationBeepEnabled", "bool",
					title: "Beep when Monitoring Status changes?",
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
				getInfoParagraph("None of the selected Notification Devices support the 'beep' command")
			}
		}
	}
}

private getInfoParagraph(txt) {
	getParagraph(txt, "info.png")
}

private getWarningParagraph(txt) {
	getParagraph(txt, "warning.png")
}

private getParagraph(txt, imageName="") {
	if (imageName) {
		paragraph image: getImageUrl(imageName), txt
	}
	else {
		paragraph txt
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
		map.image = getImageUrl(imageName)
	}
	else if (description == "(unconfigured)") {
		map.image = getImageUrl("warning.png")
	}
	href(map)
}

def installed() {
	state.status = [id: "disarm", name: "Disarmed"]
	initialize()
}

def updated() {	
	initialize()

	logDebug("State Used: ${(state.toString().length() / 100000)*100}%")
}

def initialize() {
	unsubscribe()
	unschedule()
	state.params = [:]
	armZones()
	initializeMonitoredSecurityDevices()
	initializeArmDisarmTriggers()
}

def armZones() {
	def statusZoneNames = getStatusSettings(state.status?.id, "StatusZones")
	
	logDebug("Arming/Disarming Zones for Monitoring Status ${state?.status?.name}")
	
	def details = "Zone Status:\n"
	getZones(false).sort { it.displayName }.each { zone ->
		if (state.status?.optional && zone?.name in statusZoneNames) {
			zone.armed = true
		}
		else {
			zone.armed = false
		}
		state."${zone.armedStateName}" = zone.armed
		details += "${zone.displayName}: ${zone.armed ? 'Armed' : 'Disarmed'}\n"		
	}
	logDebug(details)
}

private initializeArmDisarmTriggers() {
	def details = "Arming/Disarming Subscriptions:\n"
	if (getStatusSettings(null, "ArmDisarmSmartHomeMonitorStates")) {
		details += "Smart Home Monitor Changes\n"
		subscribe(location, "alarmSystemStatus", armDisarmSmartHomeMonitorChangedHandler)
	}

	if (getStatusSettings(null, "ArmDisarmModes")) {
		details += "Location Mode Changes\n"
		subscribe(location, "mode", armDisarmModeChangedHandler)
	}

	getArmDisarmDevices().each { device ->
		getArmDisarmDeviceTypes().each { type ->
			def canSubscribe = false

			type.attrValues.each { attrValue ->
				def settingName = "${type.prefName?.capitalize()}${attrValue?.replace(' ', '')?.capitalize()}"
				
				if (device.displayName in getStatusSettings(null, "$settingName")) {
					canSubscribe = true
				}
			}

			if (canSubscribe) {
				details += "${device.displayName}: ${type.attrName?.capitalize()} Event\n"
				subscribe(device, "${type.attrName}", armDisarmDeviceEventHandler)
			}
		}
	}
	logTrace(details)
}

def armDisarmSmartHomeMonitorChangedHandler(evt) {
	def shmState = getSmartHomeMonitorStates().find { it.id == evt.value }
	logInfo("SHM changed to ${shmState?.name}")
	
	def newStatus = getStatuses().find {
		(status?.name in settings["${it.id}ArmDisarmSmartHomeMonitorStates"])
	}
	
	if (newStatus) {
		changeStatus(newStatus)
	}	
}

def armDisarmModeChangedHandler(evt) {
	logInfo("Location Mode changed to ${evt.value}")
	def newStatus = getStatuses().find {
		(evt.value in settings["${it.id}ArmDisarmModes"])
	}
	if (newStatus) {
		changeStatus(newStatus)
	}	
}

def armDisarmDeviceEventHandler(evt) {
	logInfo("${evt.displayName}: ${evt.name} changed to ${evt.value}")
		
	def type = getArmDisarmDeviceTypes().find { it.attrName == evt.name}
	if (type) {
		def newStatus = getStatuses().find {
			(evt.displayName in settings[getStatusSettingName(it.id, type.prefName, evt.value)])
		}
		changeStatus(newStatus)
	}	
}

private getStatusSettingName(statusId, partialSettingName, attrValue) {
	partialSettingName = partialSettingName?.capitalize() ?: ""
	
	if (attrValue != null) {
		partialSettingName = "${partialSettingName}${attrValue?.replace(' ', '')?.capitalize()}"
	}
	
	return "${statusId}${partialSettingName}"
}

private appendStatusSettingSummary(summary, statusId, partialSettingName, summaryLineFormat) {	
	getStatusSettings(statusId, partialSettingName)?.each {
		summary += summary ? "\n" : ""
		summary += summaryLineFormat.replace("%", "${it}")
	}
	return summary
}

private getStatusSettings(statusId, partialSettingName) {
	def result = []
	partialSettingName = partialSettingName?.capitalize() ?: ""
		
	getStatuses().each { 
		if (!statusId || it.id == statusId) {
			def settingName = "${it.id}${partialSettingName}"
			if (settings[settingName]) {
				result += settings[settingName]
			}
		}
	}
	return result
}

private initializeMonitoredSecurityDevices() {
	def details = "Security Device Subscriptions:\n"
	getArmedSecurityDevices()?.sort { it.displayName }?.each { device ->
		getSecurityDeviceTypes().each { type ->
			if (device.hasAttribute("${type.alarmAttr}")) {				
				details += "${device.displayName}: ${type.alarmAttr?.capitalize()} Event\n"
				subscribe(device, "${type.alarmAttr}.${type.alarmValue}", "security${type.shortName}Handler")
			}
		}
	}
	logTrace(details)
}

private getArmedSecurityDevices() {
	def devices = []
	def excludedDevices = settings["${state.status?.id}ExcludedDevices"]
	
	getZones().each { zone ->
		if (zone.armed) {
			def zoneSecurityDevices = settings["${zone.settingName}SecurityDevices"]
			
			if (zoneSecurityDevices) {
				getAllSecurityDevices().each { device ->					
					if (device.displayName in zoneSecurityDevices) {					
						if (device.displayName in excludedDevices) {
							logTrace "Ignoring ${device.displayName} because it's in the ${state.status?.name} Exclude List"
						}
						else {
							devices << device
						}					
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
	def namePrefix = "${state.status.id}${notificationType}"
	def alarmAutoOffSeconds = settings["${namePrefix}AlarmTurnOffAfter"]
	def volume = settings["${namePrefix}Volume"] ?: null
	def message = "${currentZone?.displayName}: ${evt.device?.displayName} - ${evt.name} is ${evt.value}"

	def zoneMessage = settings["${currentZone?.settingName}${currentDeviceType?.prefName}Message"]

	getNotificationSettingNames(notificationType, state.status?.id).each { prefName ->
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
			log.debug "zettingName: ${zone.settingName}${notificationType}Devices"
			def zoneDeviceNames = settings["${zone.settingName}${notificationType}Devices"]
			log.debug "$deviceDisplayName == $zoneDeviceNames"
			return (deviceDisplayName in zoneDeviceNames)
		}
		else {
			return false
		}
	}
}

private getDeviceType(notificationType, eventName, eventVal) {
	def deviceTypes = (notificationType == "Security") ? getSecurityDeviceTypes() : getSafetyDeviceTypes()

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
			zone.zoneGroupName = settings."${zone.settingName}Group" ?: ""
			zone.displayName = zone.zoneGroupName ? "${zone.zoneGroupName} > ${zone.name}" : "${zone.name}"

			zone.armedStateName = "${zone.settingName}Armed"
			zone.armed = state."${zone.armedStateName}" ?: false
			zone.status = zone.armed ? "Armed" : "Disarmed"

			zones << zone
		}
	}
	return zones.sort { it.displayName }
}

private hasSafetyDevices() {
	return getSafetyDevices() ? true : false
}

private getSafetyDeviceNames() {
	return getSafetyDevices().collect { it.displayName }.sort()
}

private getSafetyDevices() {
	def devices = []
	getSafetyDeviceTypes().each {
		if (settings[it.prefName]) {
			devices += settings[it.prefName]
		}
	}
	return devices.unique()
}

private getSafetyDeviceTypes() {
	return [
		[name: "Carbon Monoxide Detectors", shortName: "Carbon Monoxide", prefName: "safetyCarbonMonoxideDetector", prefType: "capability.carbonMonoxideDetector", alarmAttr: "carbonMonoxide", alarmValue: "detected"],
		[name: "Smoke Detectors", shortName: "Smoke", prefName: "safetySmokeDetector", prefType: "capability.smokeDetector", alarmAttr: "smoke", alarmValue: "detected"],
		[name: "Water Sensors", shortName: "Water", prefName: "safetyWaterSensors", prefType: "capability.waterSensor", alarmAttr: "water", alarmValue: "wet"]
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

private hasStatusZones() {
	return getStatusZoneNames(null) ? true : false
}

private getStatusZoneNames(statusId) {
	def names = []
	getStatuses().each { status ->
		if (!statusId || statusId == status?.id) {
			def statusZones = getStatusSettings(status?.id, "StatusZones")
			if (statusZones) {
				names += statusZones
			}
		}
	}
	return names
}

private hasStatuses() {
	return getStatuses() ? true : false
}

private hasConfiguredNotifications(notificationType, statusId) {
	return getStatusNotificationDeviceNames(notificationType, statusId) ? true : false
}

private getStatusNotificationDeviceNames(notificationType, statusId) {
	def names = []
	getStatuses().each { status ->

		if (!statusId || statusId == status?.id) {
			getNotificationSettingNames(notificationType, status?.id).each {
				if (settings["$it"]) {
					names += settings["$it"]
				}
			}
		}
	}
	return names
}

private getNotificationSettingNames(notificationType, statusId) {
	def prefix = "${statusId}${notificationType}"

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

private getStatusNames(includeDisarmed=true, includeDisabled=false) {
	return getStatuses(includeDisarmed, includeDisabled).collect { it.name }
}

private getOptionalStatusNames() {
	getAllStatuses().findAll { it.optional }?.collect { it.name }
}

private getStatuses(includeDisarmed=true, includeDisabled=false) {
	def statuses = []	
	getAllStatuses().each { status ->
		def isAllowed = ((includeDisarmed || status.id != "disarmed") && (includeDisabled || status.id != "disabled"))
		if (isAllowed && (!status.optional || status.name in settings.selectedStatuses)) {
			statuses << status
		}
	}
	return statuses
}

private getAllStatuses() {
	def items = [
		[id: "active", name: "Armed (Active)", optional: true],
		[id: "alone", name: "Armed (Alone)", optional: true],
		[id: "away", name: "Armed (Away)", optional: true],
		[id: "outdoors", name: "Armed (Outdoors)", optional: true],
		[id: "relaxed", name: "Armed (Relaxed)", optional: true],
		[id: "sleep", name: "Armed (Sleep)", optional: true],
		[id: "stay", name: "Armed (Stay)", optional: true],
		[id: "testing", name: "Armed (Testing)", optional: true],
		[id: "trusted", name: "Armed (Trusted Visitor)", optional: true],
		[id: "untrusted", name: "Armed (Untrusted Visitor)", optional: true],
		[id: "disabled", name: "Disabled", optional: false],
		[id: "disarmed", name: "Disarmed", optional: false]
	]
	return items
}

private getSmartHomeMonitorStates() {
	return [
		[id: "away", name: "Armed (away)"],
		[id: "stay", name: "Armed (stay)"],
		[id: "off", name: "Disarmed"]
	]
}

private getImageUrl(imageName) {
		return "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-zone-monitor/${imageName}"
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