/**
 *  Zooz Double Plug Outlet v1.0 (CHILD DEVICE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
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
		name: "Zooz Double Plug Outlet", 
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
		capability "Refresh"		
		
		// attribute "secondaryStatus", "string"
		attribute "energyTime", "number"
		attribute "energyDuration", "string"
		attribute "current", "number"
				
		["power", "voltage", "current"].each {
			attribute "${it}Low", "number"
			attribute "${it}High", "number"
		}
				
		command "reset"
	}
	
	simulator { }	

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			// tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				// attributeState "default", label:'${currentValue}'
			// }
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
	}
	
	preferences { }
}


def installed() { }


def updated() {	
	parent.childUpdated(device.deviceNetworkId)
}


def on() {
	parent.childOn(device.deviceNetworkId)	
}

def off() {
	parent.childOff(device.deviceNetworkId)	
}

def refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

def reset() {
	parent.childReset(device.deviceNetworkId)	
}