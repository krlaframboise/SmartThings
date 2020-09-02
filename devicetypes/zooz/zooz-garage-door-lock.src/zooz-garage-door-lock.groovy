/*
 *  Zooz Garage Door Lock v1.0	(Device Handler)
 *
 *
 *  Changelog:
 *
 *    1.0 (08/10/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Zooz
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
		name: "Zooz Garage Door Lock",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)"
	) {
		capability "Actuator"
		capability "Lock"
	}

	simulator { }

	tiles(scale: 2) {
		
		multiAttributeTile(name:"main", type: "generic", width: 6, height: 4){
			tileAttribute("device.lock", key: "PRIMARY_CONTROL") {
				attributeState("locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00a0dc")
				attributeState("unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#e86d13")
			}
		}
		standardTile("lock", "device.lock", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked"
		}
		main(["main"])
		details(["main"])
	}

	preferences {
		input "debugLoggingEnabled", "enum",
			title: "Debug Logging:",
			required: false,
			defaultValue: 1,
			options: [0:"Disabled", 1:"Enabled [DEFAULT]"]
	}
}


def installed() {
	initialize()
}

def updated() {
	logDebug "updated()..."
	initialize()
}

void initialize() {
	if (!device.currentValue("lock")) {
		sendEvent(name: "lock", value: "unlocked")
	}
}


def parse(String description) {
	logDebug "parse(description)..."
}


def unlock() {
	logDebug "unlock()..."
	parent.childOpen(device.deviceNetworkId)
}


def lock() {
	logDebug "lock()..."
	parent.childClose(device.deviceNetworkId)
}


void logDebug(msg) {
	if ((settings?.debugLoggingEnabled == null) || (settings?.debugLoggingEnabled == 1)) {
		log.debug "$msg"
	}
}