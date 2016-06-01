/**
 *  Virtual Aeon Labs Multifunction Doorbell
 *
 */
metadata {
	definition (name: "Virtual Aeon Labs Multifunction Doorbell", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"			
		capability "Switch"
		capability "Alarm"	
		capability "Music Player"
		capability "Tone"		
		capability "Battery"
		capability "Button"
		capability "Polling"
		capability "Presence Sensor"
		capability "Refresh"
		
		command "pushButton"
	}

	simulator {
		status "basic report on": zwave.basicV1.basicReport(value:0xFF).incomingMessage()		
		status "basic report off": zwave.basicV1.basicReport(value:0x00).incomingMessage()		
		
		reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
		reply "988100200100,9881002002": "command: 9881, payload: 00200300"
		reply "9881002001FF,delay 3000,988100200100,9881002002": "command: 9881, payload: 00200300"	
	}

	preferences {
		input "bellTrack", "number", title: "Doorbell Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false	
			
		input "toneTrack", "number", title: "Beep Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false
		
		input "alarmTrack", "number", title: "Alarm Track (1-100)", defaultValue: 2, displayDuringSetup: true, required: false
		
		input "soundLevel", "number", title: "Sound Level (1-10)", defaultValue: 10, displayDuringSetup: true,  required: false
		
		input "soundRepeat", "number", title: "Sound Repeat: (1-100)", defaultValue: 1, displayDuringSetup: true, required: false		
				
		input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: true, required: false
		
		input "silentButton", "bool", title: "Enable Silent Button?\n(If you want to use the button for something other than a doorbell, you need to also set the Doorbell Track to a track that doesn't have a corresponding sound file.)", defaultValue: false, required: false
		
		input "forceConfigure", "bool", title: "Force Configuration Refresh? (This only needs to be enabled if your experiencing problems with your settings not getting applied)", defaultValue: true, required: false
	}	
	
	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "bell", label:'Doorbell Ringing!', action: "off", icon:"st.Home.home30", backgroundColor:"#99c2ff"
				attributeState "alarm", label:'Alarm!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"
				attributeState "play", label:'Playing!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#694489"
			}
		}		
		standardTile("playBell", "device.tone", label: 'Doorbell', width: 2, height: 2) {
			state "default", label:'Doorbell', action:"pushButton", icon:"st.Home.home30", backgroundColor: "#99c2ff"
		}
		standardTile("playTone", "device.tone", label: 'Beep', width: 2, height: 2) {
			state "default", label:'Beep', action:"beep", icon:"st.Entertainment.entertainment2", backgroundColor: "#99FF99"
		}
		standardTile("playAlarm", "device.alarm", label: 'Alarm', width: 2, height: 2) {
			state "default", label:'Alarm', action: "both", icon:"st.alarm.alarm.alarm", backgroundColor: "#ff9999"
		}
		valueTile("previous", "device.musicPlayer", label: 'Previous Track', width: 2, height: 2) {
			state "default", label:'<<', action:"previousTrack", backgroundColor: "#694489"
		}
		valueTile("trackDescription", "device.trackDescription", label: 'Play Track', wordWrap: true, width: 2, height: 2) {
			state "trackDescription", label:'PLAY\n${currentValue}', action: "play", backgroundColor: "#694489"
		}		
		valueTile("next", "device.musicPlayer", label: 'Next Track', width: 2, height: 2) {
			state "default", label:'>>', action:"nextTrack", backgroundColor: "#694489"
		}
		standardTile("refresh", "device.refresh", label: 'Refresh', width: 2, height: 2) {
			state "default", label:'', action: "refresh", icon:"st.secondary.refresh"
		}
		valueTile("battery", "device.battery",  width: 2, height: 2) {
			state "battery", label:'BATTERY\n${currentValue}%', unit:"", backgroundColor: "#000000"
		}
		valueTile("presence", "device.presence",  width: 2, height: 2) {
			state "present", label: 'Online', unit: "", backgroundColor: "#00FF00"
			state "not present", label: 'Offline', unit: "", backgroundColor: "#FF0000"
			state "default", label: 'Unknown', unit: "", defaultState: true
		}
		main "status"
		details(["status", "playBell", "playTone", "playAlarm", "previous", "trackDescription", "next", "refresh", "battery", "presence"])
	}
}


def pushButton() {	
	
}

def off() {	

}

def on() {
	both()
}

def strobe() {
	both()
}
def siren() {
	both()
}
def both() {	

}

def beep() {

}


def previousTrack() {	

}

def nextTrack() {

}

def setTrack(track) {	

}

def stop() {
	off()
}

def play() {

}

def playTrack(track) {
}

def setLevel(level) {

}

def refresh() {

}

def poll() {

}


def updated() {
}

def configure() {
}