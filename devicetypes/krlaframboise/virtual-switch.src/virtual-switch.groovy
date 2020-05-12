/**
 *  Virtual Switch - v 1.0.1
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0.1 (10/30/2018)
 *      - Added support for new mobile app.
 *
 *    1.0.0 (01/14/2017)
 *      - Initial Release
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
	definition (name: "Virtual Switch", namespace: "krlaframboise", author: "Kevin LaFramboise", vid:"generic-switch") {
		capability "Actuator"
		capability "Switch"
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "switch.on", 
					icon:"st.Appliances.appliances17", 
					backgroundColor: "#ffffff"
				attributeState "on", 
					label:'On', 
					action: "switch.off", 
					icon:"st.Appliances.appliances17", 
					backgroundColor: "#79b821"
			}
		}
		
		standardTile("switchToggle", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
    main "switch"
		details(["switch","switchToggle"])
	}
}

def parse(String description) { }

def on() {
	sendEvent(name: "switch", value: "on")
}

def off() {
	sendEvent(name: "switch", value: "off")
}