/**
 *  ThingShield Timer 1.2
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
metadata {
	definition (name: "ThingShield Timer", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Button"
		capability "Refresh"
		 
		command "pushButtonIn", ["number", "number"]
	}

	// simulator metadata
	simulator {
	}
	
	preferences {		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}	

	// UI tile definitions
	tiles(scale: 2) {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		valueTile("power", "device.power", label: 'Power', wordWrap: true, width: 2, height: 2) {
			state "power", label:'DATE\n(POWER)\n${currentValue}',  backgroundColor: "#000000"
		}
		valueTile("energy", "device.energy", label: 'Energy', wordWrap: true, width: 2, height: 2) {
			state "energy", label:'TIME\n(ENERGY)\n${currentValue}',  backgroundColor: "#000000"
		}
		standardTile("refresh", "device.refresh", label: 'Refresh', width: 2, height: 2) {
			state "default", label:'', action: "refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch","power", "energy", "refresh"])
	}
}

def updated() {	
	state.lastPush = null
	state.debugOutput = debugOutput
	
	if (!state.debugOutput) { state.debugOutput = false }
	
	if (!state.scheduledButtons) {
		state.scheduledButtons = []
	}
	
	if (canReport()) {
		reportCurrentDateTimeAsPower()
		reportCurrentTimeAsEnergy()
	}
	writeToDebugLog "Updated"
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text	
	if (value == "tick") {
		if (canPush()) {
			writeToDebugLog "Arduino push ($value)"
			push()
		} 
		if (canReport()) {
			reportCurrentDateTimeAsPower()
			reportCurrentTimeAsEnergy()
		}
		pushScheduledButtons()
	} else {
		writeToDebugLog "Parse returned $value"
	}	
}

private canPush() {
	def lastPush = state.lastPush
	def minimumTime = (60 * 1000) 
	def currentTime = new Date().time
	def result = true	
	
	if (lastPush != null) {
		result = (currentTime - lastPush) >= minimumTime
	}	
	if (result) {
		state.lastPush = currentTime
	}	
	return result
}

def on() {
	writeToDebugLog "On command executed"
	push()
}

def off() {
	writeToDebugLog "Off command exectured"
	push()
}

def push() {
	sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def reportCurrentTimeAsEnergy() {
	def formattedTime = getFormattedCurrentTime()
	sendEvent(name: "energy", value: "$formattedTime", displayed: false, isStateChange: true)
}

def reportCurrentDateTimeAsPower() {
	def formattedDate = getFormattedCurrentDate()
	def formattedTime = getFormattedCurrentTime()
	sendEvent(name: "power", value: "$formattedDate$formattedTime", displayed: false, isStateChange: true)
}

private canReport() {
	def currentReport = "${getOnHourTime()}"
	def onHourTime = Date.parse("E MMM dd H:m:s z yyyy", currentReport)

	def result = true
	if (state.lastReport != null) {		
		def lastReport = Date.parse("E MMM dd H:m:s z yyyy", state.lastReport)
		result = (lastReport.time < onHourTime.time)		
	}		
	if (result) {	
		state.lastReport = currentReport
	}		
	return result
}

def getOnHourTime() {
	def cal = getCurrentCalendar()
	cal.set(Calendar.MINUTE, 0)
	cal.set(Calendar.SECOND, 0)	
	return cal.time
}

def getFormattedCurrentTime() {
  def cal = getCurrentCalendar()
	def h = formatTwoDigit(cal.get(Calendar.HOUR_OF_DAY))
	def min = formatTwoDigit(cal.get(Calendar.MINUTE))
	return "$h$min"
}

def getFormattedCurrentDate() {
  def cal = getCurrentCalendar()
	def y = cal.get(Calendar.YEAR)
	def m = formatTwoDigit(cal.get(Calendar.MONTH) + 1)
	def d = formatTwoDigit(cal.get(Calendar.DATE))
	return "$y$m$d"
}

def getCurrentCalendar() {
	def cal = Calendar.getInstance()
	cal.setTime(getCurrentTime())
	return cal
}

def getCurrentTime() {
	def tz = TimeZone.getTimeZone("America/New_York")    
    def currentUTC = new Date().time
    return new Date(currentUTC + tz.getOffset(currentUTC))
}

def formatTwoDigit(num) {
	return String.format("%02d", num)
}


def pushButtonIn(btn, seconds) {
	setButtonPushTime(btn, addToCurrentTime(seconds))
}

def addToCurrentTime(seconds) {
	return (new Date().time) + (seconds * 1000)
}

def setButtonPushTime(btn, time) {
	writeToDebugLog("scheduledButtons: ${state.scheduledButtons}")
	writeToDebugLog("btn: $btn - time: $time")
	def updated = false
	for (map in state.scheduledButtons) {
		if (map.buttonNumber == btn) {
			map.pushTime = time
			updated = true
		}
	}
	if (!updated) {
		state.scheduledButtons += [buttonNumber: btn, pushTime: time]
	}
	//def item = state.scheduledButtons.find{k -> k.buttonNumber == btn}
	//if (!item) {
	//	state.scheduledButtons << [buttonNumber: btn, pushTime: time]		
	//}
	//else {
	//	item.pushTime = time	
	//}
}

def pushScheduledButtons() {
	for (item in state.scheduledButtons) {
			if (item.pushTime != null && timeElapsed(item.pushTime)) {
				item.pushTime = null
				pushButton(item.buttonNumber)
			}
	}	
}

def timeElapsed(time) {
	return (new Date().time > time)
}

def pushButton(buttonNum) {
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNum], descriptionText: "$device.displayName button $buttonNum was pushed", isStateChange: true)
}

def refresh() {
	for (item in state.scheduledButtons) {
		if (item.pushTime != null) {
			def scheduleTime = (item.pushTime - (new Date().time)) / 1000
			writeToDebugLog("Push button ${item.buttonNumber} in ${scheduleTime} Seconds")
		}
	}
}

def writeToDebugLog(msg) {
	if (state.debugOutput) {
		log.debug msg
	}
}
