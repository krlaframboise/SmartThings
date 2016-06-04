/**
 *  Forcible Mobile Presence v 1.0
 *
 *  Capabilities:
 *    Presence Sensor
 *
 *  Author: Original "Mobile Presence" by SmartThings
 *    Modified by Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (04/30/2016)
 *      -	Initial Release
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
	definition (name: "Forcible Mobile Presence", namespace: "krlaframboise", author: "SmartThings, Kevin LaFramboise") {
		capability "Presence Sensor"
		capability "Sensor"
		
		command "arrived"
		command "departed"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles(scale: 2) {
		standardTile("presence", "device.presence", width: 4, height: 4, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ebeef2")
		}
		standardTile("setArrived", "generic", width: 2, height: 2) {
      state "default", label:'Arrive', 
				action:"arrived"
    }
		standardTile("setDeparted", "generic", width: 2, height: 2) {
      state "default", label:'Depart', 
				action:"departed"
    }
		main "presence"
		details(["presence", "setArrived", "setDeparted"])
	}
}

def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = getState(value)
	def isStateChange = isStateChange(device, name, value)

	def results = [
    	translatable: true,
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	log.debug "Parse returned ${results.descriptionText}"
	return results

}

private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	}
	null
}

private String parseValue(String description) {
	switch(description) {
		case "presence: 1": return "present"
		case "presence: 0": return "not present"
		default: return description
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "{{ linkText }} has arrived"
		case "not present": return "{{ linkText }} has left"
		default: return value
	}
}

private getState(String value) {
	switch(value) {
		case "present": return "arrived"
		case "not present": return "left"
		default: return value
	}
}

def arrived() {
	sendForcedEvent("present")
}

def departed() {
	sendForcedEvent("not present")
}

private sendForcedEvent(newState) {
	def displayState = (newState == "present") ? "Present" : "Not Present"
	sendEvent(name: "presence", value: newState, descriptionText: "${device.displayName} was forced to ${displayState}", isStateChange: true)
}