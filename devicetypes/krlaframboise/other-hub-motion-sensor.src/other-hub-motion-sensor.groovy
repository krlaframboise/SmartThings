/**
 *  Other Hub Motion Sensor 1.0
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
	definition (name: "Other Hub Motion Sensor", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Refresh"
		capability "Motion Sensor"
		
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}

	tiles {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'MOTION', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#00A0DC")
				attributeState("inactive", label:'NO MOTION',
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#cccccc")
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

void refresh() {
	parent.childRefresh(device.currentValue("deviceId"))
}