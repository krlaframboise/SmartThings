/**
 *  Zooz Power Strip USB VER 2.0 (CHILD DEVICE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    2.0.0 (10/16/2018)
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
		name: "Zooz Power Strip USB VER 2.0", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		vid:"generic-switch"
	) {
		capability "Sensor"
		capability "Switch"		
		capability "Outlet"
	}
	
	simulator { }	

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', icon: "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/usb.png", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', icon: "https://raw.githubusercontent.com/krlaframboise/Resources/master/Zooz/usb.png", backgroundColor: "#ffffff"
			}			
		}
	}
	
	preferences { }
}


def installed() { }


def updated() {	}


def on() { }

def off() { }