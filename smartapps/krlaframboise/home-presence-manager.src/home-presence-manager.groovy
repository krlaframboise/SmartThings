/**
 *  Home Presence Manager v 1.2.4 testing
 *
 *  Copyright 2016 Kevin LaFramboise
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
definition(
    name: "Home Presence Manager",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Uses motion sensors, contact sensors and virtual presence sensors to keep track of the room you're in so that you can make the lights stay on until you exit instead of relying on inactivity timeouts",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
		iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@3x.png"
)

preferences {
	page(name:"mainPage")
  page(name:"optionsPage")	
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install: true){
		section("Rooms") {
			paragraph "This is the main page"
			href(
				name: "optionsLink", 
				title: "Options",
				description: "",
				page: "optionsPage",
				required: false
			)	
		}
	}
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {		
		section("Options") {
			input "debugLogEnabled", "bool",
				title: "Debug Logging Enabled?",
				defaultValue: true,
				required: false							
		}
	}
}

def installed() {
	log.debug "Installed"
}

def updated() {	
	log.debug "Updated"
	unschedule()
	unsubscribe()
}
