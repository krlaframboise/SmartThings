/**
 *  Child Switch v1.0 (CHILD DEVICE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (12/28/2019)
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
		name: "Child Switch", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch"
	) {
		capability "Actuator"
		capability "Switch"		
		capability "Outlet"
		capability "Light"
	}
	
	simulator { }	

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
		main(["switch"])
		details(["switch"])
	}
	
	preferences { }
}


def installed() { 

}

def updated() {	

}

def on() {
	parent.childOn(device.deviceNetworkId)	
}

def off() {
	parent.childOff(device.deviceNetworkId)	
}