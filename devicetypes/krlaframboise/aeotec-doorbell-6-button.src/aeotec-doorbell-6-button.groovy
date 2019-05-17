/**
 *  Aeotec Doorbell 6 Button (CHILD DEVICE) v1.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (05/16/2019)
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
		name: "Aeotec Doorbell 6 Button", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "oic.d.switch",
		vid:"generic-switch"
	) {
		capability "Sensor"
		capability "Button"
		capability "Battery"
		capability "Switch"
		capability "Refresh"
		
		attribute "firmwareVersion", "string"
		attribute "lastPushed", "string"
	}
	
	simulator { }	

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}			
			tileAttribute ("device.lastPushed", key: "SECONDARY_CONTROL") {
				attributeState "lastPushed", label:'Last Pushed: ${currentValue}'
			}
		}		
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:2, height: 2) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		main("switch")
		details(["switch", "refresh", "battery","firmwareVersion"])
	}
	
	preferences { 	
		getOptionsInput("tone", "Sound", defaultTone, setDefaultOption(toneOptions, defaultTone))
		
		getOptionsInput("volume", "Volume", defaultVolume, setDefaultOption(volumeOptions, defaultVolume))
		
		getOptionsInput("lightEffect", "Light Effect", defaultLightEffect, setDefaultOption(lightEffectOptions, defaultLightEffect))
		
		getOptionsInput("repeat", "Repeat", defaultRepeat, setDefaultOption(getRepeatOptions(false), defaultRepeat))

		getOptionsInput("repeatDelay", "Repeat Delay", defaultRepeatDelay, setDefaultOption(repeatDelayOptions, defaultRepeatDelay))
		
		getOptionsInput("toneIntercept", "Tone Intercept Length", defaultToneIntercept, setDefaultOption(toneInterceptOptions, defaultToneIntercept))
		
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}

private getOptionsInput(name, title, defaultVal, options) {
	input "${name}", "enum",
		title: "${title}:",
		required: false,
		defaultValue: defaultValue,
		displayDuringSetup: true,
		options: options
}


private getVolumeSetting() {
	return safeToInt(settings?.volume, defaultVolume)	
}

private getToneSetting() {
	return safeToInt(settings?.tone, defaultTone)
}

private getLightEffectSetting() {
	return safeToInt(settings?.lightEffect, defaultLightEffect)
}

private getRepeatSetting() {
	return safeToInt(settings?.repeat, defaultRepeat)
}

private getRepeatDelaySetting() {
	return safeToInt(settings?.repeatDelay, defaultRepeatDelay)
}

private getToneInterceptSetting() {
	return safeToInt(settings?.toneIntercept, defaultToneIntercept)
}

private getDefaultVolume() { return 2 }
private getDefaultTone() { return 1 }
private getDefaultLightEffect() { return 3 }
private getDefaultRepeat() { return 1 }
private getDefaultRepeatDelay() { return 0 }
private getDefaultToneIntercept() { return 0 }


def installed() { 
	logDebug "installed()..."
	sendEvent(getEventMap("numberOfButtons", 1))
	sendEvent(getEventMap("switch", "off"))
}


def updated() {	
	logDebug "updated()..."
	def groupSettings = [
		"tone": toneSetting,
		"volume": volumeSetting,
		"lightEffect": lightEffectSetting,
		"repeat": repeatSetting,
		"repeatDelay": repeatDelaySetting,
		"toneIntercept": toneInterceptSetting,
		"childName": device.displayName
	]
	parent?.childUpdated(buttonNumber, groupSettings)
}


def refresh() {
	logDebug "refresh()..."
	parent?.childRefresh(buttonNumber)
}


def on() {
	logDebug "on()..."
	parent?.childOn(buttonNumber)	
}


def off() {
	logDebug "off()..."
	parent?.childOff(buttonNumber)
}


def getEventMap(name, value, displayed=false, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} - ${name} ${value}"
	]
	
	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}	
	
	if (displayed) {
		logDebug "${name} ${value}" + (unit ? "${unit}" : "")
	}
	return eventMap
}


def setDefaultOption(options, defaultVal) {
	return options?.collect { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
}


def getVolumeOptions() {
	def options = ["0":"Mute", "1":"1 - Low"]	

	(2..6).each {
		options["${it}"] = "${it}"
	}
	
	options["7"] = "7 - High"
	return options
}

def getToneOptions() {
	def options = [:]
	
	(1..30).each {
		options["${it}"] = "Tone #${it}"
	}
	
	return options
}

def getLightEffectOptions() {
	[
		0:"Off",
		1:"On",
		2:"Slow Pulse",
		3:"Pulse",
		4:"Fast Pulse",
		5:"Flash",
		6:"Strobe"
	]	
}

def getRepeatOptions(includeUnlimited) {
	def options = [:]	
	if (includeUnlimited) {
		options["0"] = "Unlimited"
	}
	(1..30).each {
		options["${it}"] = "${it}"
	}	
	return options
}

private getRepeatDelayOptions() {
	def options = [
		0:"No Delay",
		1:"1 Second"
	]
	(2..14).each {
		options["${it}"] = "${it} Seconds"
	}
	return options
}

private getToneInterceptOptions() {
	def options = [
		0:"Play Entire Tone",
		1:"1 Second"
	]
	(2..15).each {
		options["${it}"] = "${it} Seconds"
	}
	(4..50).each {
		options["${it * 5}"] = "${it * 5} Seconds"
	}
	return options
}


private getButtonNumber() {
	return safeToInt(getDataValue("buttonNumber"))
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


def logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}