/*
 *  Child USB Port v1.0
 *
 *  Changelog:
 *
 *    1.0 (09/21/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Kevin LaFramboise
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

metadata {
	definition (
		name: "Child USB Port", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "oic.d.switch",
        mnmn: "SmartThingsCommunity",
		vid: "6be2cfc6-6f75-36c1-b81e-baebb049890c"
	) {
		capability "Switch"
		capability "platemusic11009.usbPort"		
	}
	
	simulator { }	

	tiles(scale: 2) {
		multiAttributeTile(name:"usbPort", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.usbPort", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', backgroundColor: "#ffffff"
			}
		}
		main(["usbPort"])
		details(["usbPort"])
	}
	
	preferences { }
}


def installed() { 
	sendEvent(name:"switch", value:"off")
	sendEvent(name:"usbPort", value:"off")
}

def updated() {	

}

def on() {
	log.debug "The USB Port can't be controlled."
}

def off() {
	log.debug "The USB Port can't be controlled."
}