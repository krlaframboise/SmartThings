/**
 *  GoControl Contact Sensor v1.3
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    n/a
 *
 *  Changelog:
 *
 *    1.3 (05/2/2016)
 *      -  Initial releasese.
 *
 *89
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
		name: "GoControl Contact Sensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"

		fingerprint deviceId: "0x2001", 
			inClusters: "0x71,0x85,0x80,0x72,0x30,0x86,0x84"
	}

	// simulator metadata
	simulator {
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(
			name:"contact", 
			type: "generic", 
			width: 6, 
			height: 4, 
			canChangeIcon: true
		){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "closed", 
					label:'closed', 
					icon:"st.contact.contact.closed", 
					backgroundColor:"#79b821"
				attributeState "open", 
					label:'open', 
					icon:"st.contact.contact.open", 
					backgroundColor:"#ffa81e"
			}
		}
		
		valueTile("battery", "device.battery", 
			inactiveLabel: false, 
			decoration: "flat", 
			width: 2, 
			height: 2
		){
			state "battery", 
			label:'${currentValue}% battery', 
			unit:""
		}
		
		valueTile("tampering", "device.tamper", 
			decoration: "flat", 
			width: 2, 
			height: 2
		){
			state "default", 
				label:'', 
				icon: ""
			state "detected",
				label:'Tampering Detected',
				backgroundColor:"#FF0000",
				icon: ""
		}
		standardTile("refresh", "device.refresh", 
			width: 2, 
			height: 2
		) {
			state "default", 
				label:'Reset', 
				action: "refresh", 
				icon:""
		}
		
		main("contact")
		details(["contact", "battery", "tampering", "refresh"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, displayed:true)
	} 
	else {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	logDebug "BasicReport: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	logDebug "WakeUpNotification"
	
	def result = []
	
	if (!state.lastbat || ((new Date().time) - state.lastbat > (4*60*1000))) {
		result << response(zwave.batteryV1.batteryGet().format())
		result << response("delay 3000")  
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {	
	def map = [ 
		name: "battery", 
		value: 1,
		unit: "%", 
		isStateChange: true, 
		displayed: false
	]
	if (cmd.batteryLevel == 0xFF) {
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = new Date().time	
	createEvent(map)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "VersionReport: $cmd"	
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{	
	def contactVal = (cmd.value == 255) ? "open" : "closed"	
	
	createEvent(name: "contact", 
		value: contactVal, 
		isStateChange: true, 
		displayed: true, 
		descriptionText: "Contact is $contactVal")		
}

// Sets tamper attribute to detected when the device is opened.
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []	
	if (!state.tamperingActive && (cmd.event == 3)) {
		state.tamperingActive = true	
		result << createEvent(getTamperEventMap("detected"))
	}
	return result
}

// Resets the tamper attribute to clear.
def refresh() {
	if (state.tamperingActive || (device.currentValue("tamper") == "detected")) {
		state.tamperingActive = false
		sendEvent(getTamperEventMap("clear"))		
	}	
}

def getTamperEventMap(val) {
	[
		name: "tamper", 
		value: val, 
		isStateChange: true, 
		displayed: true,
		descriptionText: "Tampering is $val"
	]
}

def logDebug(msg) {
	log.debug "$msg"
}