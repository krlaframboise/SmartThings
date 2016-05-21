/**
 *  LeakSmart Water Valve v 1.0 Beta
 *  
 *  Capabilities:
 *      Configuration, Refresh, Switch, Battery, Valve
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *  Url to Documentation:
 *      
 *
 *  Changelog:
 *
 *    1.0 Beta (05/20/2016)
 *      - Testing
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
	definition (name: "LeakSmart Water Valve", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Valve"

		//fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0020,0006,0B02", outClusters: "0003"
		
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0006,0020,0B02", outClusters: "0019"
	}
	
	preferences {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'Closed', 
					action: "valve.open", 
					nextState: "opening", 
					icon:"st.valves.water.closed", 
					backgroundColor:"#e86d13"
				attributeState "opening", 
					label:'Opening', 
					action: "valve.open", 
					icon:"st.valves.water.closed", 
					backgroundColor:"#53a7c0"
				attributeState "open", 
					label:'Open', 
					action: "valve.close", 
					nextState: "closing",
					icon:"st.valves.water.open", 
					backgroundColor:"#53a7c0"
				attributeState "closing", 
					label:'Closing', 
					action: "valve.close", 
					icon:"st.valves.water.open", 
					backgroundColor:"#e86d13"
			}
		}
		standardTile("openValve", "general", width: 2, height: 2, canChangeIcon: true) {
			state "default", 
				label: 'Open', 
				action: "open", 
				icon: ""			
		}
		standardTile("closeValve", "general", width: 2, height: 2, canChangeIcon: true) {
			state "default", 
				label: 'Close', 
				action: "close", 
				icon: ""			
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2, canChangeIcon: true) {
			state "default", 
				label: 'Refresh', 
				action: "refresh.refresh", 
				icon: ""			
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2, canChangeIcon: true) {
			state "default", 
				label: 'Refresh', 
				action: "refresh.refresh", 
				icon: ""			
		}
		standardTile("configure", "device.configuration", width: 2, height: 2, canChangeIcon: true) {
			state "default", 
				label: 'Config', 
				action: "configuration.configure", 
				icon: ""			
		}
		valueTile("battery", "device.battery", width: 2, height: 2, canChangeIcon: true) {
			state "battery", 
				label: 'Battery ${currentValue}%',
				unit: "%"
				icon: ""			
		}
		main "contact"
		details(["contact", "openValve", "closeValve", "refresh", "configure", "battery"])
	}
}

def updated() {
	if (!state.configured) {		
		return response(configure())
	}
}

def parse(String description) {
	log.debug "DESCRIPTION:$description\nEVENT:${zigbee.getEvent(description)}\nMAP:${zigbee.parseDescriptionAsMap(description)}"
	def result = []
	def evt = zigbee.getEvent(description)
	if (evt) {
		//logDebug "name: ${evt.name}, value: ${evt.value}\n$evt"
		if (evt.name == "switch") {
			createEvent([
				name: "contact",
				value: (evt.value == "on") ? "open" : "closed"
			])
		}
		result << createEvent(evt)
	}
	else {
		def map = zigbee.parseDescriptionAsMap(description)
		if (map) {			
			result += handleUnknownDescriptionMap(map)
		}
		else { 
			//logDebug "Unknown Command: $description"
		}
	}
	return result
}

private handleUnknownDescriptionMap(map) {
	logDebug "Unknown Map: $map"
	def result = []	
	return result
}

// Commands to device
def on() {
	logDebug "on()"
	open()
}

def off() {
	logDebug "off()"
	close()
}

def open() {
	logDebug "Opening Valve"
	zigbee.on()	
}

def close() {
	logDebug "Closing Valve"
	zigbee.off()
}

def refresh() {
	logDebug "Refreshing"	
	return zigbee.onOffRefresh() + 
		zigbee.simpleMeteringPowerRefresh() + 
		zigbee.electricMeasurementPowerRefresh() + 
		zigbee.onOffConfig() + 
		zigbee.simpleMeteringPowerConfig() + 
		zigbee.electricMeasurementPowerConfig() +
		configureSwitchReporting() +
		configureBatteryReporting()		
}

def configure() {
	logDebug "Configuring Reporting and Bindings."
	state.configured = true
	return zigbee.onOffConfig() + 
		zigbee.simpleMeteringPowerConfig() + 
		zigbee.electricMeasurementPowerConfig() + 
		configureSwitchReporting() +
		configureBatteryReporting() +
		zigbee.onOffRefresh() + 
		zigbee.simpleMeteringPowerRefresh() + 
		zigbee.electricMeasurementPowerRefresh()
}

private configureSwitchReporting() {
	def interval = 3600 // 1 Hour
	zigbee.configureReporting(0x0006, 0x0000, 0x10, 1, interval, null)
}

private configureBatteryReporting() {
	def interval = 14400 // 4 Hours	
	zigbee.configureReporting(0x0001, 0x0020, 0x20, 30, interval, 0x01)
}

private logDebug(msg) {
	if (settings.debugOutput != false) {
		log.debug "$msg"
	}
}