/**
 *  Other Hub Switch 1.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (09/05/2017)
 *			- Initial Relase
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
 */
metadata {
	definition (name: "Other Hub Switch", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Refresh"
		capability "Switch"
		
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}

	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', 
					action: "switch.on", 
					nextState: "turningOn",
					icon: "st.switches.switch.off", 
					backgroundColor: "#ffffff"
				attributeState "turningOn", label: 'TURNING ON',
					action: "switch.off", 
					nextState: "off", 
					icon: "st.switches.switch.on", 
					backgroundColor: "#00a0dc"				
				attributeState "on", label: 'ON', 
					action: "switch.off",
					nextState: "turningOff",
					icon: "st.switches.switch.on", 
					backgroundColor: "#00a0dc"
				attributeState "turningOff", label: 'TURNING OFF', 
					action: "switch.off", 
					nextState: "off",
					icon: "st.switches.switch.off", 
					backgroundColor: "#ffffff"
			}
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "status", 
					label:'${currentValue}', 
					backgroundColor:"#ffffff"
			}
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh-icon"
		}
	}
}


void on() {
	parent.childOn(device.currentValue("deviceId"))
}

void off() {
	parent.childOff(device.currentValue("deviceId"))
}

void refresh() {
	parent.childRefresh(device.currentValue("deviceId"))
}