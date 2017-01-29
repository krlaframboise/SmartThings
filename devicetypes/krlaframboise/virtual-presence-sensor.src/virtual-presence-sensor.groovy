/**
 *  Virtual Presence Sensor v 1.2
 *
 *  Capabilities:
 *    Presence Sensor
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.2 (04/20/2016)
 *      -	Added departed and arrived commands because that's
 *        what the simulated presence sensor DH uses.
 *
 *    1.1 (03/06/2016)
 *      -	Made force commands always change state.
 *
 *  Licensed under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a 
 *  copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
metadata {
	definition (name: "Virtual Presence Sensor", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Presence Sensor"
		capability "Sensor"
		capability "Actuator"
		capability "Media Controller"
		
		command "forcePresent"
		command "arrived"
		command "forceNotPresent"
		command "departed"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles(scale: 2) {
	multiAttributeTile(name:"presence", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("presence", key: "PRIMARY_CONTROL") {
				attributeState( "present", 
					label:'Present', 
					backgroundColor:"#99c2ff", 
					labelIcon:"", 
					action: "departed")
				attributeState("not present", 
					label:'Not Present', 
					backgroundColor: "#cccccc", 
					labelIcon:"", 
					action: "arrived")			
			}
		}				
		main "presence"
		details "presence"
	}
}

def forcePresent() {
	arrived()
}

def arrived() {
	sendEvent(name: "presence", value: "present", isStateChange: true)
}

def forceNotPresent() {
	departed()
}

def departed() {
	sendEvent(name: "presence", value: "not present", isStateChange: true)
}

def parse(String description) {

}