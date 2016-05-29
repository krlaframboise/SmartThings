/**
 *  CoRE - Community's own Rule Engine
 *
 *  Copyright 2016 Adrian Caramaliu <adrian(a sign goes here)caramaliu.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Version history
 *	 5/28/2016 >>> v0.0.052.20160528 - Alpha test version - Fixed a bug where last executed task was not correctly removed
 *	 5/28/2016 >>> v0.0.051.20160528 - Alpha test version - More fixes for casting and variable condition description
 *	 5/27/2016 >>> v0.0.050.20160527 - Alpha test version - Load Attribute from variable done and partially tested. Missing: color support - this is a complex data type...
 *	 5/27/2016 >>> v0.0.04f.20160527 - Alpha test version - We have an official icon! Also, fixed a problem with time scheduling for "not in between", fixed a potential problem with casting null values.
 *	 5/27/2016 >>> v0.0.04e.20160527 - Alpha test version - Implemented saveAttribute, introduced "aggregated" commands, these only run once, even when used on a list of devices
 *	 5/27/2016 >>> v0.0.04d.20160527 - Alpha test version - Fixed a bug (for good?) with item in list for is_one_of. Types enum, mode, and other special types need not be casted.
 *	 5/26/2016 >>> v0.0.04c.20160526 - Alpha test version - Fixed a bug with item in list for is_one_of.
 *	 5/26/2016 >>> v0.0.04b.20160526 - Alpha test version - Fixed a bug introduced by the simulator
 *	 5/26/2016 >>> v0.0.04a.20160526 - Alpha test version - First attempt at simulations :)
 *	 5/26/2016 >>> v0.0.049.20160526 - Alpha test version - Fixed a problem with the new casting function. There are several special data types, namely mode, alarmSystemStatus, etc. that act as strings.
 *	 5/26/2016 >>> v0.0.048.20160526 - Alpha test version - Conditions for capability Variable should now work. Triggers are only available for @ (global) variables, but the mechanism for subscribing to changes is not yet here. So triggers don't yet work.
 *	 5/26/2016 >>> v0.0.047.20160526 - Alpha test version - Fixed a problem with casting enums... they are now handled as strings
 *	 5/26/2016 >>> v0.0.046.20160526 - Alpha test version - Pretty major changes at conditions UI and logic. Added the ability to compare against another device/attribute pair (can choose any attribute of that device). Added the toggleLevel command and fixed some bugs with setting SHM status.
 *	 5/25/2016 >>> v0.0.045.20160525 - Alpha test version - Caching attributes - attempt to speed up some things
 *	 5/25/2016 >>> v0.0.044.20160525 - Alpha test version - Fixed the command description for custom commands - temporary until custom commands are complete
 *	 5/25/2016 >>> v0.0.043.20160525 - Alpha test version - Fixed toggle virtual command, modified the Flash virtual command to turn the switch back on if it started on, added the Cancel Pending Tasks virtual command, added action advanced options Task Override Scope and Task Cancellation Policy. Global scope does not yet work, CoRE would have to "spread the word" to all child pistons. Improved the device condition evaluation to speed up things - took common code outside of the device loop.
 *	 5/24/2016 >>> v0.0.042.20160524 - Alpha test version - Execute Routine action is now available. Routine execution trigger not yet ready.
 *	 5/24/2016 >>> v0.0.041.20160524 - Alpha test version - Extended "is one of" and "is not one of" to attributes that have only two values. It was previously available only to those having three or more values.
 *	 5/24/2016 >>> v0.0.040.20160524 - Alpha test version - Multi sub-device support (read buttons in conditions, or multi-switch in actions). TODO: buttons & triggers - the button state does not change, so detecting a change is tricky, or rather, simpler, but different.
 *	 5/23/2016 >>> v0.0.03f.20160523 - Alpha test version - Added matching and non-matching device list variables and renamed "With (devices)" to "Using (devices)"
 *	 5/23/2016 >>> v0.0.03e.20160523 - Alpha test version - Set Variable fixes for time variables, fixed variables not being parsed in sendNotification
 *	 5/23/2016 >>> v0.0.03d.20160523 - Alpha test version - Set Variable done, testing in progress
 *	 5/23/2016 >>> v0.0.03c.20160523 - Alpha test version - Fixed a problem where dry evaluation of a condition may fail when changing the capability or the attribute or the comparison in the UI
 *	 5/22/2016 >>> v0.0.03b.20160522 - Alpha test version - Changed hue to angle, 0-360. Fixed a problem with mode and SHM alarm triggers. Added two predefined commands: quickSetCool and quickSetHeat
 *	 5/20/2016 >>> v0.0.03a.20160520 - Alpha test version - Color support. Added all 140 standard CSS colors, plus 4 white light colors. Partially tested with Osram (Green is green and Crimson is crimson)
 *	 5/20/2016 >>> v0.0.039.20160520 - Alpha test version - Fixed an error where an incomplete (during building) time condition would fail due to next event calculation introduced in v0.0.036.20160520
 *	 5/20/2016 >>> v0.0.038.20160520 - Alpha test version - Displaying individual actions (when true) in the main piston page and showing action restrictions as per https://github.com/ady624/CoRE/issues/7
 *	 5/20/2016 >>> v0.0.037.20160520 - Alpha test version - Modified event duplication detection to only ignore identical events received within one second of each other, or events generated before the last event was generated. Also enabled {$variable} support in notification messages. "Time is {$now}" will return the time... :)
 *	 5/20/2016 >>> v0.0.036.20160520 - Alpha test version - Optimized time condition evaluation for next event time - limitations: events only happen at designated times, with no regards to any existing time restrictions (i.e. will trigger an event on Fri even if Fri is restricted - the evaluation at that time, however, will take restrictions into account)
 *	 5/19/2016 >>> v0.0.035.20160519 - Alpha test version - Fixed a problem where custom time in a "between" condition would reset the offset for sunrise/sunset
 *	 5/19/2016 >>> v0.0.034.20160519 - Alpha test version - Notification support. Push/SMS/Notification. Coming soon: variable support in message.
 *	 5/19/2016 >>> v0.0.033.20160519 - Alpha test version - Location Mode and SHM status now trigger events
 *	 5/19/2016 >>> v0.0.032.20160519 - Alpha test version - Added delayed turn on, off and toggle
 *	 5/19/2016 >>> v0.0.031.20160519 - Alpha test version - Minor fixes, removed [] from no-parameter device commands
 *	 5/19/2016 >>> v0.0.030.20160519 - Alpha test version - Implemented the Toggle and Flash virtual commands, ability to control any device using Capability selection, and unified the way location control works. Minor fixes, spelling and others
 *	 5/18/2016 >>> v0.0.02f.20160518 - Alpha test version - Minor bug fixes - including time condition scheduling, unscheduling of all event tasks prior to rescheduling (deleting a time trigger left the schedule behind)
 *	 5/18/2016 >>> v0.0.02e.20160518 - Alpha test version - Broken debugging mode down into several levels, info, trace, debug, warn and error. Fixed display of device names where device has no label.
 *	 5/18/2016 >>> v0.0.02d.20160518 - Alpha test version - Time conditions now act as triggers if no triggers are involved in any of the condition sets - this is to mimic the way device act as triggers when the same applies
 *	 5/17/2016 >>> v0.0.02c.20160517 - Alpha test version - Fixed time not between
 *	 5/17/2016 >>> v0.0.02b.20160517 - Alpha test version - Individual actions...
 *	 5/17/2016 >>> v0.0.02a.20160517 - Alpha test version - Fixed a problem with time subscriptions subscribe() failing
 *	 5/17/2016 >>> v0.0.029.20160517 - Alpha test version - Fixed a problem with time between - comparing only one variable, not both
 *	 5/17/2016 >>> v0.0.028.20160517 - Alpha test version - Fixed circulateFan misspelled, fixed is_one_of missing, progress to detecting location mode and alarm system
 *	 5/17/2016 >>> v0.0.027.20160517 - Alpha test version - More minor bugs with triggers
 *	 5/17/2016 >>> v0.0.026.20160517 - Alpha test version - Minor bug with trigger support
 *	 5/17/2016 >>> v0.0.025.20160517 - Alpha test version - Live condition evaluation and fixed minor bugs
 *	 5/17/2016 >>> v0.0.024.20160517 - Alpha test version - Added three more piston modes. We now have Simple, Latching, And-If, Or-If, Then-If, and Else-If
 *	 5/17/2016 >>> v0.0.023.20160517 - Alpha test version - Change SHM state now functional
 *	 5/17/2016 >>> v0.0.022.20160517 - Alpha test version - Change location mode now functional, fixes for Android (removed ranges on Parent ID)
 *	 5/16/2016 >>> v0.0.021.20160516 - Alpha test version - More bug fixes
 *	 5/16/2016 >>> v0.0.020.20160516 - Alpha test version - More float vs int problems fixed with Android
 *	 5/16/2016 >>> v0.0.01f.20160516 - Alpha test version - Fixed a problem with $previousStateDuration not being available on first run
 *	 5/16/2016 >>> v0.0.01e.20160516 - Alpha test version - Fixed the action ID being considered float under Android. Forcefully casting to int.
 *	 5/16/2016 >>> v0.0.01c.20160516 - Alpha test version - Fixed the time condition evaluation returning false. Result was not correctly initialized to true
 *	 5/16/2016 >>> v0.0.01b.20160516 - Alpha test version - Added the $random and $randomLevel "random" variables. Also initializing the system store correctly. Added submitOnChange for pageActionDevices
 *	 5/16/2016 >>> v0.0.01a.20160516 - Alpha test version - Simple actions are now executed. Capture/Restore states, attributes, or variables not completed yet.
 *	 5/14/2016 >>> v0.0.019.20160514 - Alpha test version - Bug fixes - event cache not properly initialized leading to impossibility to install a new piston, more action UI progress
 *	 5/13/2016 >>> v0.0.017.20160513 - Alpha test version - Variable support improved - full list of variables during config
 *	 5/13/2016 >>> v0.0.016.20160513 - Alpha test version - Minor fixes, bringing missing methods back from the dead
 *	 5/13/2016 >>> v0.0.015.20160513 - Alpha test version - Merged CoRE and CoRE Piston into one single its-own-parent-and-child app, action UI progress
 * 
 *******************************************************************************************************************************************************************
 * PLEASE MAKE SURE TO UNINSTALL ANY PREVIOUS VERSION UP UNTIL HERE - CHILD NAME HAS CHANGED
 *******************************************************************************************************************************************************************
 * 
 *  CoRE version history before merge
 *	 5/11/2016 >>> v0.0.005.20160511 - Alpha test version - Changed name from "Rule Engine" to "CoRE" and added an easter egg
 *	 5/10/2016 >>> v0.0.004.20160510 - Alpha test version - Added runtime statistics - even more stats for nerds
 *	 5/10/2016 >>> v0.0.003.20160510 - Alpha test version - Added runtime statistics - stats for nerds
 *	 5/10/2016 >>> v0.0.001.20160510 - Alpha test version - Minor improvements for global variable list
 *	 5/10/2016 >>> v0.0.000.20160510 - Alpha test version - Initial build
 * 
 *  CoRE Piston version history before merge
 *   5/12/2016 >>> v0.0.014.20160512 - Alpha test version - Piston's scheduler, "Timing belt" is now operational.
 *   5/11/2016 >>> v0.0.013.20160511 - Alpha test version - Name changed to CoRE Piston, child of CoRE, time triggers operational - can figure out next schedule time
 *   5/10/2016 >>> v0.0.012.20160510 - Alpha test version - Added runtime statistics - even more stats for nerds
 *   5/10/2016 >>> v0.0.011.20160510 - Alpha test version - Added runtime statistics - stats for nerds
 *   5/10/2016 >>> v0.0.010.20160510 - Alpha test version - Minor improvements for global variable list
 *   5/10/2016 >>> v0.0.00f.20160510 - Alpha test version - Local and global (@) variables support, added offsets for device and variable comparison
 *   5/10/2016 >>> v0.0.00e.20160510 - Alpha test version - Time condition evaluation done, filters pending
 *   5/10/2016 >>> v0.0.00d.20160510 - Alpha test version - Date & Time condition UI tweaks, time interpretation complete
 *   5/07/2016 >>> v0.0.00c.20160507 - Alpha test version - Date & Time condition UI tweaks
 *   5/07/2016 >>> v0.0.00b.20160507 - Alpha test version - Date & Time condition UI finalized, pending community comments
 *   5/07/2016 >>> v0.0.00a.20160507 - Alpha test version - Name changed to Piston, child app of Rule Engine, courtesy of @bridaus and @JDRoberts (reinforcement of the word Rule)
 *   5/06/2016 >>> v0.0.009.20160506 - Alpha test version - Partial support for Location Mode, Smart Home Monitor and Date & Time - work in progress
 *   5/06/2016 >>> v0.0.008.20160506 - Alpha test version - Minor improvements
 *   5/05/2016 >>> v0.0.007.20160505 - Alpha test version - All conditions implemented, simple triggers implemented. History-based triggers ("...stays...") not working yet
 *   5/05/2016 >>> v0.0.006.20160505 - Alpha test version - Simple conditions implemented. All "is" type conditions should work
 *   5/04/2016 >>> v0.0.005.20160504 - Alpha test version - added full list of standard capabilities, attributes and commands, improved condition UI
 *   5/02/2016 >>> v0.0.004.20160502 - Alpha test version - changed license from Apache to GPLv3
 *   5/02/2016 >>> v0.0.003.20160502 - Alpha test version - added mode - simple, latching or else-if
 *   5/02/2016 >>> v0.0.002.20160502 - Alpha test version - added latching rules
 *   4/29/2016 >>> v0.0.001.20160429 - Alpha test version - added condition naming
 *   4/29/2016 >>> v0.0.000.20160429 - Alpha test version
 *
 */


/******************************************************************************/
/*** CoRE VERSION															***/
/******************************************************************************/

def version() {
	return "v0.0.052.20160528"
}


/******************************************************************************/
/*** CoRE DEFINITION														***/
/******************************************************************************/

definition(
    name: "CoRE${parent ? " - Piston" : ""}",
    namespace: "ady624",
    author: "Adrian Caramaliu",
    description: "CoRE - Community's own Rule Engine",
    singleInstance: true,
    category: "Convenience",
    iconUrl: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE.png",
    iconX2Url: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE@2x.png",
    iconX3Url: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE@2x.png"
)

preferences {
	//common pages
	page(name: "pageMain")
  
    //CoRE pages
    page(name: "pageStatistics")
    page(name: "pageChart")
    page(name: "pageGlobalVariables")
    
    //Piston pages
    page(name: "pageIf")
    page(name: "pageIfOther")
    page(name: "pageThen")
    page(name: "pageElse")
    page(name: "pageCondition")
    page(name: "pageConditionGroupL1")
    page(name: "pageConditionGroupL2")
    page(name: "pageConditionGroupL3")
    page(name: "pageConditionVsTrigger")
    page(name: "pageActionGroup")
    page(name: "pageAction")
    page(name: "pageActionDevices")
    page(name: "pageVariables")  
    page(name: "pageSetVariable")
		page(name: "pageSummary")
    page(name: "pageSimulate")
}


/******************************************************************************/
/*** CoRE CONSTANTS															***/
/******************************************************************************/

private triggerPrefix() {
    return "● "
}

private conditionPrefix() {
	return "◦ "
}

private virtualCommandPrefix() {
    return "● "
}

private customAttributePrefix() {
	return "⌂ "
}

private customCommandPrefix() {
	return "⌂ "
}

private customCommandSuffix() {
	return "(..)"
}

/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/


/******************************************************************************/
/*** COMMON PAGES															***/
/******************************************************************************/
def pageMain() {
	dev()
	if (!parent) {
		pageMainCoRE()
    } else {
		pageMainCoREPiston()
    }
}


/******************************************************************************/
/*** CoRE PAGES																***/
/******************************************************************************/
private pageMainCoRE() {
    //CoRE main page
    dynamicPage(name: "pageMain", title: "CoRE Pistons", install: true, uninstall: true) {
        section {
            app( name: "pistons", title: "Add a CoRE piston...", appName: "CoRE", namespace: "ady624", multiple: true)
        }

        section(title:"Application Info") {
            paragraph version(), title: "Version"
            href "pageGlobalVariables", title: "Global Variables"
            href "pageStatistics", title: "Runtime Statistics"
        }

        section(title: "Advanced options", hideable: !settings.expertMode && !settings.debugging, hidden: true) {
            input "expertMode", "bool", title: "Expert Mode", defaultValue: false, submitOnChange: true
            input "debugging", "bool", title: "Enable debugging", defaultValue: false, submitOnChange: true
            def debugging = settings.debugging
            if (debugging) {
	            input "log#info", "bool", title: "Log info messages", defaultValue: true
	            input "log#trace", "bool", title: "Log trace messages", defaultValue: true
	            input "log#debug", "bool", title: "Log debug messages", defaultValue: false
	            input "log#warn", "bool", title: "Log warning messages", defaultValue: true
	            input "log#error", "bool", title: "Log error messages", defaultValue: true
            }
        }

    }
}

def pageGlobalVariables() {
	dynamicPage(name: "pageGlobalVariables", title: "Global Variables", install: false, uninstall: false) {
    	section() {
        	def cnt = 0
            for (def variable in state.store.sort{ it.key }) {
            	def value = getVariable(variable.key, true)
                paragraph "$value", title: "${variable.key}"
                cnt++
            }
            if (!cnt) {
            	paragraph "No global variables yet"
            }
        }
	}
}

def pageStatistics() {
	dynamicPage(name: "pageStatistics", title: "", install: false, uninstall: false) {
        def apps = getChildApps()
    	section(title: "CoRE") {
            paragraph mem(), title: "Memory Usage"
            paragraph "${apps.size}", title: "Running pistons"
            paragraph "0", title: "Paused pistons"
            paragraph "${apps.size}", title: "Total pistons"
        }
        
        updateChart("delay", null)
        section(title: "Event delay (15 minute average, last 2h)") {
        	def text = ""
            def chart = state.charts["delay"]
            def totalAvg = 0
            for (def i = 0; i < 8; i++) {
            	def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
                def time = chart["$i"].q
                def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
            	def avg = Math.ceil(value / 1)
                totalAvg += avg
                if (avg > 10) {
                	avg = 10
                }
                 def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
            	text += "$hour\t${graph}\n"
            }
            totalAvg = totalAvg / 8
            href "pageChart", params: [chart: "delay", title: "Event delay"], title: "", description: text, required: true, state: totalAvg < 5 ? "complete" : null
        }

        updateChart("exec", null)
        section(title: "Execution time (15 minute average, last 2h)") {
        	def text = ""
            def chart = state.charts["exec"]
            def totalAvg = 0
            for (def i = 0; i < 8; i++) {
            	def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
                def time = chart["$i"].q
                def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
            	def avg = Math.ceil(value / 1)
                totalAvg += avg
                if (avg > 10) {
                	avg = 10
                }
                def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
            	text += "$hour\t${graph}\n"
            }
            totalAvg = totalAvg / 8
            href "pageChart", params: [chart: "exec", title: "Execution time"], title: "", description: text, required: true, state: totalAvg < 5 ? "complete" : null
        }

		def i = 0
        if (apps && apps.size()) {
            for (app in apps) {
                def mode = app.getMode()
                def version = app.version()
                def currentState = app.getCurrentState()
                def stateSince = app.getCurrentStateSince()
                def runStats = app.getRunStats()
                def conditionStats = app.getConditionStats()
                def subscribedDevices = app.getDeviceSubscriptionCount()
                stateSince = stateSince ? formatLocalTime(stateSince) : null
                def description = "Piston mode: ${mode ? mode : "unknown"}"
                description += "\nPiston version: $version"
                description += "\nSubscribed devices: $subscribedDevices"
                description += "\nCondition count: ${conditionStats.conditions}"
                description += "\nTrigger count: ${conditionStats.triggers}"
                description += "\n\nCurrent state: ${currentState == null ? "unknown" : currentState}"
                description += "\nSince: " + (stateSince ?  stateSince : "(never run)")
                description += "\n\nMemory usage: " + app.mem()
                if (runStats) {
	                def executionSince = runStats.executionSince ? formatLocalTime(runStats.executionSince) : null
                    description += "\n\nEvaluated: ${runStats.executionCount} time${runStats.executionCount == 1 ? "" : "s"}"
                    description += "\nSince: " + (executionSince ?  executionSince : "(unknown)")
                    description += "\n\nTotal evaluation time: ${Math.round(runStats.executionTime / 1000)}s"
                    description += "\nLast evaluation time: ${runStats.lastExecutionTime}ms"
                    if (runStats.executionCount > 0) {
                        description += "\nMin evaluation time: ${runStats.minExecutionTime}ms"
                        description += "\nAvg evaluation time: ${Math.round(runStats.executionTime / runStats.executionCount)}ms"
                        description += "\nMax evaluation time: ${runStats.maxExecutionTime}ms"
                    }
                    if (runStats.eventDelay) {
                        description += "\n\nLast event delay: ${runStats.lastEventDelay}ms"
                        if (runStats.executionCount > 0) {
                            description += "\nMin event delay time: ${runStats.minEventDelay}ms"
                            description += "\nAvg event delay time: ${Math.round(runStats.eventDelay / runStats.executionCount)}ms"
                            description += "\nMax event delay time: ${runStats.maxEventDelay}ms"
                        }
					}                    
                }
                section(title: i++ == 0 ? "Pistons" : "") {
                    paragraph description, title: app.label ? app.label : app.name, required: currentState != null, state: currentState ? "complete" : null
                }
            }
        } else {
        	section() {
            	paragraph "No pistons running"
            }
        }
    }
}

def pageChart(params) {
	def chartName = params?.chart
    def chartTitle = params?.title
	dynamicPage(name: "pageChart", title: "", install: false, uninstall: false) {
    	if (chartName) {
            updateChart(chartName, null)
            section(title: "$chartTitle (15 minute average, last 24h)\nData is calculated across all pistons") {
                def text = ""
                def chart = state.charts[chartName]
				def totalAvg = 0
                for (def i = 0; i < 96; i++) {
                    def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
                    def time = chart["$i"].q
                    def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
                    def avg = Math.ceil(value / 1)
	                totalAvg += avg
                    if (avg > 10) {
                        avg = 10
                    }
                    def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
                    text += "$hour\t${graph}\n"
                }
				totalAvg = totalAvg / 96
                paragraph text, required: true, state: totalAvg < 5 ? "complete" : null
            }
		}
    }
}




/******************************************************************************/
/*** CoRE PISTON PAGES														***/
/******************************************************************************/

private pageMainCoREPiston() {
    //CoRE Piston main page
    //dev()
		state.pistonDescription = ""
    state.run = "config"
    configApp()
    cleanUpConditions(true)
    dynamicPage(name: "pageMain", title: "", uninstall: true, install: true) {
    	def currentState = state.currentState
    	section() {
        	def enabled = settings["enabled"] != false
            def pistonModes = ["Simple", "Latching", "And-If", "Or-If"]
            if (!getConditionTriggerCount(state.config.app.otherConditions)) {
            	pistonModes += ["Then-If", "Else-If"]
            }
            if (listActions(-1).size()) {
            	pistonModes.remove("Simple")
            }
        	input "enabled", "bool", description: enabled ? "Current state: ${currentState == null ? "unknown" : currentState}\nCPU: ${cpu()}\t\tMEM: ${mem(false)}" : "", title: "Status: ${enabled ? "RUNNING" : "PAUSED"}", submitOnChange: true, required: false, state: "complete", defaultValue: true
            input "mode", "enum", title: "Piston Mode", required: true, state: null, options: pistonModes, defaultValue: "Simple", submitOnChange: true
            switch (settings.mode) {
                case "Latching":
                paragraph "A latching Piston - also known as a bi-stable Piston - uses one set of conditions to achieve a 'true' state and a second set of conditions to revert back to its 'false' state"
                break
                case "Else-If":
                paragraph "An Else-If Piston executes a set of actions if an initial condition set evaluates to true, otherwise executes a second set of actions if a second condition set evaluates to true"
                break
            }
        }
        section() {
						state.pistonDescription = "${state.pistonDescription}\nMODE: ${settings.mode}\nIF"
            href "pageIf", title: "If...", description: (state.config.app.conditions.children.size() ? "Tap here to add more conditions" : "Tap here to add a condition")
            buildIfContent()
        }

        section() {
					state.pistonDescription = "${state.pistonDescription}\nTHEN"
        	def actions = listActions(0)
            def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
            href "pageActionGroup", params:[conditionId: 0], title: "Then...", description: desc, state: null, submitOnChange: false
            if (actions.size()) {
                for (action in actions) {
                    href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true										
										state.pistonDescription = "${state.pistonDescription}\n${getActionDescription(action)}"
                }
            }
        }

        def title = ""
        switch (settings.mode) {
            case "Latching":
            title = "But if..."
            break
            case "And-If":
            title = "And if..."
            break
            case "Or-If":
            title = "Or if..."
            break
            case "Then-If":
            title = "Then if..."
            break
            case "Else-If":
            title = "Else if..."
            break                        
        }
		if (title) {
					state.pistonDescription = "${state.pistonDescription}\n" + "$title".replace("...","").toUpperCase()
            section() {
                href "pageIfOther", title: title, description: (state.config.app.otherConditions.children.size() ? "Tap here to add more conditions" : "Tap here to add a condition")
                buildIfOtherContent()
            }
            section() {
								state.pistonDescription = "${state.pistonDescription}\nTHEN"
                def actions = listActions(-1)
                def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
                href "pageActionGroup", params:[conditionId: -1], title: "Then...", description: desc, state: null, submitOnChange: false
                if (actions.size()) {
                    for (action in actions) {
                        href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
												state.pistonDescription = "${state.pistonDescription}\n${getActionDescription(action)}"
                    }
                }
            }
        }

		if (settings.mode != "Latching") {
            section() {
								state.pistonDescription = "${state.pistonDescription}\nELSE"
                def actions = listActions(-2)
                def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
                href "pageActionGroup", params:[conditionId: -2], title: "Else...", description: desc, state: null, submitOnChange: false
                if (actions.size()) {
                    for (action in actions) {
                        href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
												state.pistonDescription = "${state.pistonDescription}\n${getActionDescription(action)}"
                    }
                }
            }
        }
				
				section() {
					href "pageSummary", title: "View Piston Summary", description: "Allows you to see the entire piston in a condensed view."
				}

        section() {
            href "pageSimulate", title: "Simulate", description: "Allows you to test the actions manually", state: complete
        }

        section(title:"Application Info") {
            label name: "name", title: "Name", required: true, state: (name ? "complete" : null), defaultValue: parent.generatePistonName()
            input "description", "string", title: "Description", required: false, state: (description ? "complete" : null), capitalization: "sentences"
            paragraph version(), title: "Version"
            paragraph mem(), title: "Memory Usage"
            href "pageVariables", title: "Local Variables"
        }
        
        section(title: "Advanced options", hideable: !settings.debugging, hidden: true) {
            input "debugging", "bool", title: "Enable debugging", defaultValue: false, submitOnChange: true
            def debugging = settings.debugging
            if (debugging) {
	            input "log#info", "bool", title: "Log info messages", defaultValue: true
	            input "log#trace", "bool", title: "Log trace messages", defaultValue: true
	            input "log#debug", "bool", title: "Log debug messages", defaultValue: false
	            input "log#warn", "bool", title: "Log warning messages", defaultValue: true
	            input "log#error", "bool", title: "Log error messages", defaultValue: true
            }
        }
    }
}

private pageSummary() {
	dynamicPage(name: "pageSummary", title: "", uninstall: false, install: false) {
		section("Piston Summary") {
			paragraph "${state.pistonDescription}"					
		}
	}
}

def pageIf(params) {
    state.run = "config"
	cleanUpConditions(false)
	def condition = state.config.app.conditions
    dynamicPage(name: "pageIf", title: "Main Condition Group", uninstall: false, install: false) {
    	getConditionGroupPageContent(params, condition)
    }
}

def pageIfOther(params) {
    state.run = "config"
    cleanUpConditions(false)
	def condition = state.config.app.otherConditions
    dynamicPage(name: "pageIfOther", title: "Main Group", uninstall: false, install: false) {
    	getConditionGroupPageContent(params, condition)
    }
}
def pageConditionGroupL1(params) {
	pageConditionGroup(params, 1)
}

def pageConditionGroupL2(params) {
	pageConditionGroup(params, 2)
}

def pageConditionGroupL3(params) {
	pageConditionGroup(params, 3)
}

//helper function for condition group paging
def pageConditionGroup(params, level) {
    state.run = "config"
    cleanUpConditions(false)
    def condition = null    
    if (params?.command == "add") {
        condition = createCondition(params?.parentConditionId, true)
    } else {
		condition = getCondition(params?.conditionId ? (int) params?.conditionId : state.config["conditionGroupIdL$level"])
    }
    if (condition) {
    	def id = (int) condition.id
        state.config["conditionGroupIdL$level"] = id
        def pid = (int) condition.parentId
    	dynamicPage(name: "pageConditionGroupL$level", title: "Group $id (level $level)", uninstall: false, install: false) {
	    	getConditionGroupPageContent(params, condition)
	    }
    }
}

private getConditionGroupPageContent(params, condition) {	
	try {
        if (condition) {
            def id = (int) condition.id
            def pid = (int) condition.parentId ? (int) condition.parentId : (int)condition.id
            def nextLevel = (int) (condition.level ? condition.level : 0) + 1
            def cnt = 0
            section() {
                if (settings["condNegate$id"]) {
                    paragraph "NOT ("
                }
                for (c in condition.children) {
                    if (cnt > 0) {
                        if (cnt == 1) {
                            input "condGrouping$id", "enum", title: "", description: "Choose the logical operation to be applied between all conditions in this group", options: ["AND", "OR", "XOR"], defaultValue: "AND", required: true, submitOnChange: true
                        } else {
                            paragraph settings["condGrouping$id"], state: "complete"
                        }
                    }
                    def cid = c?.id
                    def conditionType = (c.trg ? "trigger" : "condition")
                    if (c.children != null) {
                        href "pageConditionGroupL${nextLevel}", /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/folder.png",*/ params: ["conditionId": cid], title: "Group #$cid", description: getConditionDescription(cid), state: "complete", required: false, submitOnChange: false
                    } else {
                        href "pageCondition", /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/${conditionType}.png",*/ params: ["conditionId": cid], title: (c.trg ? "Trigger" : "Condition") + " #$cid", description: getConditionDescription(cid), state: "complete", required: false, submitOnChange: false
                    }
                    //when true - individual actions
                    def actions = listActions(c.id)
                    def sz = actions.size() - 1
                    def i = 0
                    def tab = "  "
                    for (action in actions) {
                        href "pageAction", params: ["actionId": action.id], title: "", description: (i == 0 ? "${tab}╠═(when true)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : ""), state: null, required: false, submitOnChange: false
                        i = i + 1
                    }

                    cnt++
                }
                if (settings["condNegate$id"]) {
                    paragraph ")", state: "complete"
                }
            }
            section() {
                href "pageCondition", params:["command": "add", "parentConditionId": id], title: "Add a condition", description: "A condition watches the state of one or multiple similar devices", state: "complete", submitOnChange: true
                if (nextLevel <= 3) {
                    href "pageConditionGroupL${nextLevel}", params:["command": "add", "parentConditionId": id], title: "Add a group", description: "A group is a container for multiple conditions and/or triggers, allowing for more complex logical operations, such as evaluating [A AND (B OR C)]", state: "complete", submitOnChange: true
                }
            }

            if (condition.children.size()) {
                section(title: "Group Overview") {
                    def value = evaluateCondition(condition)
                    paragraph getConditionDescription(id), required: true, state: ( value ? "complete" : null ) 
                    paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )                
                }       
            }

            if (id > 0) {
                def actions = listActions(id)
                if (actions.size() || state.config.expertMode) {
                    section(title: "Individual actions") {
                        def desc = actions.size() ? "" : "Tap to select actions"
                        href "pageActionGroup", params:[conditionId: id], title: "When true, do...", description: desc, state: null, submitOnChange: false
                        if (actions.size()) {
                            for (action in actions) {
                                href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
                            }
                        }
                    }
                }
            }

            section(title: "Advanced options") {
                input "condNegate$id", "bool", title: "Negate Group", description: "Apply a logical NOT to the whole group", defaultValue: false, state: null, submitOnChange: true
            }
            if (state.config.expertMode) {
            	section("Set variables") {
                    input "condVarD$id", "string", title: "Save last evaluation date", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                    input "condVarS$id", "string", title: "Save last evaluation result", description: "Enter a variable name to store the truth result in", required: false, capitalization: "none"
                    //input "condVarM$id", "string", title: "Save matching device list", description: "Enter a variable name to store the list of devices that match the condition", required: false, capitalization: "none"
                    //input "condVarN$id", "string", title: "Save non-matching device list", description: "Enter a variable name to store the list of devices that do not match the condition", required: false, capitalization: "none"
                }
                section("Set variables on true") {
                    input "condVarT$id", "string", title: "Save event date on true", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                    input "condVarV$id", "string", title: "Save event value on true", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
                }
                section("Set variables on false") {
                    input "condVarF$id", "string", title: "Save event date on false", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                    input "condVarW$id", "string", title: "Save event value on false", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
                }
            }

            if (id > 0) {
                section(title: "Required data - do not change", hideable: true, hidden: true) {            
                    input "condParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change", range: "$pid..${pid+1}", defaultValue: pid
                }
            }
        } 
    } catch(e) {
    	debug "ERROR: Error while executing getConditionGroupPageContent: $e", null, "error"
    }
}

def pageCondition(params) {
	try {
        state.run = "config"
        //get the current edited condition
        def condition = null    
        if (params?.command == "add") {
            condition = createCondition(params?.parentConditionId, false)
        } else {   	
            condition = getCondition(params?.conditionId ? params?.conditionId : state.config.conditionId)
        }
        if (condition) {
            updateCondition(condition)
            def id = (int) condition.id
            state.config.conditionId = id
            def pid = (int) condition.parentId
            def overrideAttributeType = null
            def showDateTimeFilter = false
            def showDateTimeRepeat = false
            def showParameters = false
            def recurring = false
            def trigger = false

            def branchId = getConditionMasterId(condition.id)
            def supportsTriggers = (branchId == 0) || (settings.mode in ["Latching", "And-If", "Or-If"])
            dynamicPage(name: "pageCondition", title: (condition.trg ? "Trigger" : "Condition") + " #$id", uninstall: false, install: false) {
                section() {
                    if (!settings["condDevices$id"] || (settings["condDevices$id"].size() == 0)) {
                        //only display capability selection if no devices already selected
                        input "condCap$id", "enum", title: "Capability", options: listCapabilities(true, false), submitOnChange: true, required: false
                    }
                    if (settings["condCap$id"]) {
                    	//define variables
                    	def devices
                        def attribute
                        def attr
                        def comparison
                        def allowDeviceComparisons = true
                        
                        def capability = getCapabilityByDisplay(settings["condCap$id"])
                        if (capability) {
                            if (capability.virtualDevice) {
                                attribute = capability.attribute
                                attr = getAttributeByName(attribute)
                                if (attribute == "time") {
                                    //Date & Time support
                                    comparison = cleanUpComparison(settings["condComp$id"])
                                    input "condComp$id", "enum", title: "Comparison", options: listComparisonOptions(attribute, supportsTriggers), required: true, multiple: false, submitOnChange: true
                                    if (comparison) {
                                        def comp = getComparisonOption(attribute, comparison)
                                        if (attr && comp) {
                                            //we have a valid comparison object
                                            trigger = (comp.trigger == comparison)
                                            //if no parameters, show the filters
                                            def varList = listVariables(true)
                                            showDateTimeFilter = comp.parameters == 0
                                            for (def i = 1; i <= comp.parameters; i++) {
                                                input "condValue$id#$i", "enum", title: (comp.parameters == 1 ? "Value" : (i == 1 ? "Time" : "And")), options: timeComparisonOptionValues(trigger), required: true, multiple: false, submitOnChange: true
                                                def value = settings["condValue$id#$i"]
                                                if (value) {
                                                    showDateTimeFilter = true
                                                    if (value.contains("custom")) {
                                                        //using a time offset
                                                        input "condTime$id#$i", "time", title: "Custom time", required: true, multiple: false, submitOnChange: true
                                                    }
                                                    if (value.contains("variable")) {
                                                        //using a time offset
                                                        input "condVar$id#$i", "enum", options: varList, title: "Variable", required: true, multiple: false, submitOnChange: true
                                                    }
                                                    if (comparison.contains("around") || !(value.contains('every') || value.contains('custom'))) {
                                                        //using a time offset
                                                        input "condOffset$id#$i", "number", title: (comparison.contains("around") ? "Give or take minutes" : "Offset (+/- minutes)"), range: (comparison.contains("around") ? "1..360" : "-360..360"), required: true, multiple: false, defaultValue: (comparison.contains("around") ?  5 : 0), submitOnChange: true
                                                    }

                                                    if (value.contains("minute")) {
                                                        recurring = true
                                                    }

                                                    if (value.contains("number")) {                                              
                                                        //using a time offset
                                                        input "condEvery$id", "number", title: value.replace("every n", "N"), range: "1..*", required: true, multiple: false, defaultValue: 5, submitOnChange: true
                                                        recurring = true
                                                    }

                                                    if (value.contains("hour")) {
                                                        //using a time offset
                                                        input "condMinute$id", "enum", title: "At this minute", options: timeMinuteOfHourOptions(), required: true, multiple: false, submitOnChange: true
                                                        recurring = true
                                                    }

                                                }
                                            }
                                            if (trigger && !recurring) {
                                                showDateTimeRepeat = true
                                            }
                                        }
                                    }

                                } else {
                                    //Location Mode, Smart Home Monitor support
                                    comparison = cleanUpComparison(settings["condComp$id"])
                                    if (attribute == "variable") {
                                        def dataType = settings["condDataType$id"]
                                        overrideAttributeType = dataType ? dataType : "string"                                        
                                        input "condDataType$id", "enum", title: "Data Type", options: ["boolean", "string", "number", "decimal"], required: true, multiple: false, submitOnChange: true
                                        input "condVar$id", "enum", title: "Variable name", options: listVariables(true, overrideAttributeType) , required: true, multiple: false, submitOnChange: true
                                        def variable = settings["condVar$id"]
                                        if (!"$variable".startsWith("@")) supportsTriggers = false
                                    } else {
                                    	//do not allow device comparisons for location related capabilities, except variables
                                    	allowDeviceComparisons = false
                                    }
                                    input "condComp$id", "enum", title: "Comparison", options: listComparisonOptions(attribute, supportsTriggers, overrideAttributeType), required: true, multiple: false, submitOnChange: true
                                    if (comparison) { 
                                        //Value
                                        showParameters = true
                                    }
                                }
                            } else {                        
                                //physical device support
                                devices = settings["condDevices$id"]
                                input "condDevices$id", "capability.${capability.name}", title: "${capability.display} list", required: false, state: (devices ? "complete" : null), multiple: capability.multiple, submitOnChange: true
                                if (devices && devices.size()) {
                                    if (!condition.trg && (devices.size() > 1)) {
                                        input "condMode$id", "enum", title: "Evaluation mode", options: ["Any", "All"], required: true, multiple: false, defaultValue: "All", submitOnChange: true
                                    }
                                    def evalMode = (settings["condMode$id"] == "All" && !condition.trg) ? "All" : "Any"

                                    //Attribute
                                    attribute = cleanUpAttribute(settings["condAttr$id"])
                                    if (attribute == null) {
                                        attribute = capability.attribute
                                    }
                                    //display the Attribute only in expert mode or in basic mode if it differs from the default capability attribute
                                    if ((attribute != capability.attribute) || state.config.expertMode) {
                                        input "condAttr$id", "enum", title: "Attribute", options: listCommonDeviceAttributes(devices), required: true, multiple: false, defaultValue: capability.attribute, submitOnChange: true
                                    }
                                    if (capability.count) {
                                        def subDevices = capability.count && (attribute == capability.attribute) ? listCommonDeviceSubDevices(devices, capability.count, "") : []
                                        if (subDevices.size()) {
                                            input "condSubDev$id", "enum", title: "${capability.display}(s)", options: subDevices, defaultValue: subDevices.size() ? subDevices[0] : null, required: true, multiple: true, submitOnChange: true                                        
                                        }
                                    }
                                    if (attribute) {                              
                                        //Condition
                                        attr = getAttributeByName(attribute)
                                        comparison = cleanUpComparison(settings["condComp$id"])
                                        input "condComp$id", "enum", title: "Comparison", options: listComparisonOptions(attribute, supportsTriggers, attr.momentary ? "momentary" : null), required: true, multiple: false, submitOnChange: true                                
                                        if (comparison) {                                	
                                            //Value
                                            showParameters = true
                                            /*
                                            def comp = getComparisonOption(attribute, comparison)
                                            if (attr && comp) {
                                                trigger = (comp.trigger == comparison)                                            
                                                def extraComparisons = !comparison.contains("one of")
                                                def varList = (extraComparisons ? listVariables(true) : [])
                                                if (comp.parameters >= 1) {
                                                    def value1 = settings["condValue$id#1"]
                                                    def device1 = settings["condDev$id#1"]
                                                    def variable1 = settings["condVar$id#1"]
                                                    if (!extraComparisons || ((device1 == null) && (variable1 == null))) {
                                                        input "condValue$id#1", attr.type, title: (comp.parameters == 1 ? "Value" : "From value"), options: attr.options, range: attr.range, required: true, multiple: comp.multiple, submitOnChange: true
                                                    }
                                                    if (extraComparisons) {
                                                        if ((value1 == null) && (variable1 == null)) {
                                                            input "condDev$id#1", "capability.${capability.name}", title: (device1 == null ? "... or choose a device to compare ..." : (comp.parameters == 1 ? "Device" : "From")), required: true, multiple: comp.multiple, submitOnChange: true
                                                        }
                                                        if ((value1 == null) && (device1 == null)) {
                                                            input "condVar$id#1", "enum", options: varList, title: (variable1 == null ? "... or choose a variable to compare ..." : (comp.parameters == 1 ? "Variable" : "From")), required: true, multiple: false, submitOnChange: true, capitalization: "none"
                                                        }
                                                        if (((variable1 != null) || (device1 != null)) && ((attr.type == "number") || (attr.type == "decimal"))) {
                                                            input "condOffset$id#1", attr.type, range: "*..*", title: "Offset (+/-" + (attr.unit ? " ${attr.unit})" : ")"), required: true, multiple: false, defaultValue: 0, submitOnChange: true
                                                        }
                                                    }
                                                }
                                                if (comp.parameters == 2) {
                                                    def value2 = settings["condValue$id#2"]
                                                    def device2 = settings["condDev$id#2"]
                                                    def variable2 = settings["condVar$id#2"]
                                                    if (!extraComparisons || ((device2 == null) && (variable2 == null))) {
                                                        input "condValue$id#2", attr.type, title: "Through value", options: attr.options, range: attr.range, required: true, multiple: false, submitOnChange: true
                                                    }
                                                    if (extraComparisons) {
                                                        if ((value2 == null) && (variable2 == null)) {
                                                            input "condDev$id#2", "capability.${capability.name}", title: (device2 == null ? "... or choose a device to compare ..." : "Through device"), required: true, multiple: false, submitOnChange: true
                                                        }
                                                        if ((value2 == null) && (device2 == null)) {
                                                            input "condVar$id#2", "enum", options: varList, title: (variable2 == null ? "... or choose a variable to compare ..." : "Through variable"), required: true, multiple: false, submitOnChange: true, capitalization: "none"
                                                        }
                                                        if (((variable2 != null) || (device2 != null)) && ((attr.type == "number") || (attr.type == "decimal"))) {
                                                            input "condOffset$id#1", attr.type, range: "*..*", title: "Offset (+/-" + (attr.unit ? " ${attr.unit})" : ")"), required: true, multiple: false, defaultValue: 0, submitOnChange: true
                                                        }
                                                    }
                                                }

                                                if (comp.timed) {
                                                    if (comparison.contains("change")) {
                                                        input "condTime$id", "enum", title: "In the last (minutes)", options: timeOptions(true), required: true, multiple: false, submitOnChange: true
                                                    } else {
                                                        input "condFor$id", "enum", title: "Time restriction", options: ["for at least", "for less than"], required: true, multiple: false, submitOnChange: true
                                                        input "condTime$id", "enum", title: "Interval", options: timeOptions(), required: true, multiple: false, submitOnChange: true
                                                    }
                                                }
                                            }
                                        */
                                        }
                                        //input "condDevice$id", "", title: title, required: true, multiple: false, submitOnChange: true
                                    }
                                }
                            }
                        }

                        if (showParameters) {
                        	//build the parameters inputs for all physical capabilities and variables
                            def comp = getComparisonOption(attribute, comparison, overrideAttributeType)
                            if (attr && comp) {
                                trigger = (comp.trigger == comparison)
                                def extraComparisons = !comparison.contains("one of")
                                def varList = (extraComparisons ? listVariables(true, overrideAttributeType) : [])
                                def type = overrideAttributeType ? overrideAttributeType : (attr.type == "routine" ? "enum" : attr.type)

                                for (def i = 1; i <= comp.parameters; i++) {
                                    //input "condValue$id#1", type, title: "Value", options: attr.options, range: attr.range, required: true, multiple: comp.multiple, submitOnChange: true
                                    def value = settings["condValue$id#$i"]
                                    def device = settings["condDev$id#$i"]
                                    def variable = settings["condVar$id#$i"]
                                    if (!extraComparisons || ((device == null) && (variable == null))) {
                                        input "condValue$id#$i", type == "boolean" ? "enum" : type, title: (comp.parameters == 1 ? "Value" : "${i == 1 ? "From" : "To"} value"), options: type == "boolean" ? ["true", "false"] : attr.options, range: attr.range, required: true, multiple: type == "boolean" ? false : comp.multiple, submitOnChange: true
                                    }
                                    if (extraComparisons) {
                                        if ((value == null) && (variable == null) && (allowDeviceComparisons)) {
                                            input "condDev$id#$i", "capability.${type == "boolean" ? "switch" : "sensor"}", title: (device == null ? "... or choose a device to compare ..." : (comp.parameters == 1 ? "Device value" : "${i == 1 ? "From" : "To"} device value")), required: true, multiple: false, submitOnChange: true
                                            if (device) {
                                                input "condAttr$id#$i", "enum", title: "Attribute", options: listCommonDeviceAttributes([device]), required: true, multiple: false, submitOnChange: true, defaultValue: attribute
                                            }
                                        }
                                        if ((value == null) && (device == null)) {
                                            input "condVar$id#$i", "enum", options: varList, title: (variable == null ? "... or choose a variable to compare ..." : (comp.parameters == 1 ? "Variable value" : "${i == 1 ? "From" : "To"} variable value")), required: true, multiple: comp.multiple, submitOnChange: true, capitalization: "none"
                                        }
                                        if (((variable != null) || (device != null)) && ((type == "number") || (type == "decimal"))) {
                                            input "condOffset$id#$i", type, range: "*..*", title: "Offset (+/-" + (attr.unit ? " ${attr.unit})" : ")"), required: true, multiple: false, defaultValue: 0, submitOnChange: true
                                        }
                                    }
                                }

                                if (comp.timed) {
                                    if (comparison.contains("change")) {
                                        input "condTime$id", "enum", title: "In the last (minutes)", options: timeOptions(true), required: true, multiple: false, submitOnChange: true
                                    } else {
                                        input "condFor$id", "enum", title: "Time restriction", options: ["for at least", "for less than"], required: true, multiple: false, submitOnChange: true
                                        input "condTime$id", "enum", title: "Interval", options: timeOptions(), required: true, multiple: false, submitOnChange: true
                                    }
                                }

                            }
                        }
                    }
                }
                
                if (showDateTimeRepeat) {
                    section(title: "Repeat this trigger...") {
                        input "condRepeat$id", "enum", title: "Repeat", options: timeRepeatOptions(), required: true, multiple: false, defaultValue: "every day", submitOnChange: true
                        def repeat = settings["condRepeat$id"]
                        if (repeat) {
                            def incremental = repeat.contains("number")
                            if (incremental) {
                                //using a time offset
                                input "condRepeatEvery$id", "number", title: repeat.replace("every n", "N"), range: "1..*", required: true, multiple: false, defaultValue: 2, submitOnChange: true
                                recurring = true
                            }
                            def monthOfYear = null
                            if (repeat.contains("week")) {
                                input "condRepeatDayOfWeek$id", "enum", title: "Day of the week", options: timeDayOfWeekOptions(), required: true, multiple: false, submitOnChange: true
                            }
                            if (repeat.contains("month") || repeat.contains("year")) {
                                //oh-oh, monthly
                                input "condRepeatDay$id", "enum", title: "On", options: timeDayOfMonthOptions(), required: true, multiple: false, submitOnChange: true
                                def dayOfMonth = settings["condRepeatDay$id"]
                                def certainDay = false
                                def dayOfWeek = null
                                if (dayOfMonth) {
                                    if (dayOfMonth.contains("week")) {
                                        certainDay = true
                                        input "condRepeatDayOfWeek$id", "enum", title: "Day of the week", options: timeDayOfWeekOptions(), required: true, multiple: false, submitOnChange: true
                                        dayOfWeek = settings["condDOWOM$id"]
                                    }
                                }
                                if (repeat.contains("year")) {// && (dayOfMonth) && (!certainDay || dayOfWeek)) {
                                    //oh-oh, yearly
                                    input "condRepeatMonth$id", "enum", title: "Of", options: timeMonthOfYearOptions(), required: true, multiple: false, submitOnChange: true
                                    monthOfYear = settings["condRepeatMonth$id"]
                                }
                            }
                        }
                    }
                }

                section(title: (condition.trg ? "Trigger" : "Condition") + " Overview") {
                    def value = evaluateCondition(condition)
                    paragraph getConditionDescription(id), required: true, state: ( value ? "complete" : null )
                    paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )
                    if (condition.attr == "time") {
                        def v = ""
                        def nextTime = null
                        for (def i = 0; i < (condition.trg ? 3 : 1); i++) {
                            nextTime = condition.trg ? getNextTimeTriggerTime(condition, nextTime) : getNextTimeConditionTime(condition, nextTime)
                            if (nextTime) {
                                v = v + ( v ? "\n" : "") + formatLocalTime(nextTime)
                            } else {
                                break
                            }
                        }
                        paragraph v ? v : "(not happening any time soon)", title: "Next scheduled event${i ? "s" : ""}", required: true, state: ( v ? "complete" : null )
                    }
                }

                if (showDateTimeFilter) {
                    section(title: "Date & Time Filters", hideable: !state.config.expertMode, hidden: !(state.config.expertMode || settings["condMOH$id"] || settings["condHOD$id"] || settings["condDOW$id"] || settings["condDOM$id"] || settings["condMOY$id"] || settings["condY$id"])) {
                        paragraph "But only on these..."
                        input "condMOH$id", "enum", title: "Minute of the hour", description: 'Any minute of the hour', options: timeMinuteOfHourOptions(), required: false, multiple: true, submitOnChange: true
                        input "condHOD$id", "enum", title: "Hour of the day", description: 'Any hour of the day', options: timeHourOfDayOptions(), required: false, multiple: true, submitOnChange: true
                        input "condDOW$id", "enum", title: "Day of the week", description: 'Any day of the week', options: timeDayOfWeekOptions(), required: false, multiple: true, submitOnChange: true
                        input "condDOM$id", "enum", title: "Day of the month", description: 'Any day of the month', options: timeDayOfMonthOptions2(), required: false, multiple: true, submitOnChange: true
                        input "condWOM$id", "enum", title: "Week of the month", description: 'Any week of the month', options: timeWeekOfMonthOptions(), required: false, multiple: true, submitOnChange: true
                        input "condMOY$id", "enum", title: "Month of the year", description: 'Any month of the year', options: timeMonthOfYearOptions(), required: false, multiple: true, submitOnChange: true
                        input "condY$id", "enum", title: "Year", description: 'Any year', options: timeYearOptions(), required: false, multiple: true, submitOnChange: true
                    }
                }

                if (id > 0) {
                    def actions = listActions(id)
                    if (actions.size() || state.config.expertMode) {
                        section(title: "Individual actions") {
                            def desc = actions.size() ? "" : "Tap to select actions"
                            href "pageActionGroup", params:[conditionId: id], title: "When true, do...", description: desc, state: null, submitOnChange: false
                            if (actions.size()) {
                                for (action in actions) {
                                    href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
                                }
                            }
                        }
                    }
                }

                section(title: "Advanced options") {
                    input "condNegate$id", "bool", title: "Negate ${condition.trg ? "trigger" : "condition"}", description: "Apply a logical NOT to the ${condition.trg ? "trigger" : "condition"}", defaultValue: false, state: null, submitOnChange: true
                }
                if (state.config.expertMode) {
                    section("Set variables") {
                        input "condVarD$id", "string", title: "Save last evaluation date", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                        input "condVarS$id", "string", title: "Save last evaluation result", description: "Enter a variable name to store the truth result in", required: false, capitalization: "none"
                        input "condVarM$id", "string", title: "Save matching device list", description: "Enter a variable name to store the list of devices that match the condition", required: false, capitalization: "none"
                        input "condVarN$id", "string", title: "Save non-matching device list", description: "Enter a variable name to store the list of devices that do not match the condition", required: false, capitalization: "none"
                    }
                    section("Set variables on true") {
                        input "condVarT$id", "string", title: "Save event date on true", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                        input "condVarV$id", "string", title: "Save event value on true", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
                    }
                    section("Set variables on false") {
                        input "condVarF$id", "string", title: "Save event date on false", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
                        input "condVarW$id", "string", title: "Save event value on false", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
                    }
                }

                section() {
                    paragraph "NOTE: To delete this condition, simply remove all devices from the list above and tap Done"
                }

                section(title: "Required data - do not change", hideable: true, hidden: true) {            
                    input "condParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change condParent$id", range: "$pid..${pid+1}", defaultValue: pid
                }
            }
        }
    } catch(e) {
    	debug "ERROR: Error while executing pageCondition: $e", null, "error"
    }
}

def pageConditionVsTrigger() {
	state.run = "config"
	dynamicPage(name: "pageConditionVsTrigger", title: "Conditions versus Trigers", uninstall: false, install: false) {
    	section() {
			paragraph "All Pistons are event-driven. This means that an action is taken whenever something happens while the Piston is watching over. To do so, the Piston subscribes to events from all the devices you use while building your 'If...' and - in case of latching Pistons - your 'But if...' statements as well. Since a Piston subscribes to multiple device events, it is evaluated every time such an event occurs. Depending on your conditions, a device event may not necessarily make any change to the evaluated state of the Piston (think OR), but the Piston is evaluated either way, making it possible to execute actions even if the Piston's status didn't change. More about this under the 'Then...' or 'Else...' sections of the Piston." 
paragraph "Events tell Pistons something has changed. Depending on the logic you are trying to implement, sometimes you need to check that the state of a device is within a certain range, and sometimes you need to react to a device state reaching a certain value, list or range.\n\nLet's start with an example. Say you have a temperature sensor and you want to monitor its temperature. You want to be alerted if the temperature is over 100°F. Now, assume the temperature starts at 99°F and increases steadily at a rate of one degree Fahrenheit per minute.", title: "State vs. State Change"
            paragraph "If you use a condition, the Piston will be evaluated every one minute, as the temperature changes. The first evaluation will result in a false condition as the temperature reaches 100°F. Remember, our condition is for the temperature to be OVER 100°F. The next minute, your temperature is reported at 101°F which will cause the Piston to evaluate true this time. Your 'Then...' actions will now have a chance at execution. The next minute, as the temperature reaches 102°F, the Piston will again evaluate true and proceed to executing your 'Then...' actions. This will happen for as long as the temperature remains over 100°F and will possibly execute your actions every time a new temperature is read that matches that condition. You could use this to pass the information along to another service (think IFTTT) or display it on some sort of screen. But not for turning on a thermostat - you don't neet to turn the thermostat on every one minute, it's very likely already on from your last execution.", title: "Using a Condition"
            paragraph "If you use a trigger, the Piston will now be on the lookout for a certain state change that 'triggers' our evaluation to become true. You will no longer look for a temperature over 100°F, but instead you will be looking for when the temperature exceeds 100°F. This means your actions will only be executed when the temperature actually transitioned from below or equal to 100°F to over 100°F. This means your actions will only execute once and for the Piston to fire your actions again, the temperature would have to first drop at or below 100°F and then raise again to exceed your set threshold of 100°F. Now, this you could use to control a thermostat, right?", title: "Using a Trigger"
		}
    }
}

def pageVariables() {
	state.run = "config"
	dynamicPage(name: "pageVariables", title: "", install: false, uninstall: false) {
    	section("Local Variables") {
        	def cnt = 0
            for (def variable in state.store.sort{ it.key }) {
            	def value = getVariable(variable.key, true)
                paragraph "$value", title: "${variable.key}"
                cnt++
            }
            if (!cnt) {
            	paragraph "No local variables yet"
            }
        }
    	section("System Variables") {
            for (def variable in state.systemStore.sort{ it.key }) {
            	def value = getVariable(variable.key, true)
                paragraph "$value", title: "${variable.key}"
            }
        }
	}
}

def pageActionGroup(params) {
	state.run = "config"
	def conditionId = params?.conditionId != null ? (int) params?.conditionId : (int) state.config.conditionId
    state.config.conditionId = (int) conditionId
	def value = conditionId < -1 ? false : true
    def block = conditionId > 0 ? "WHEN TRUE, DO ..." : "IF"
    if (conditionId < 0) {
    	switch (settings.mode) {
        	case "Simple":
            	block = ""
            	value = false
                break
        	case "And-If":
            	block = "AND IF"
                break
        	case "Or-If":
            	block = "OR IF"
                break
        	case "Then-If":
            	block = "THEN IF"
                break
        	case "Else-If":
            	block = "ELSE IF"
                break
			case "Latching":
            	block = "BUT IF"
                break
        }
    }
    
    switch (conditionId) {
    	case 0:
        	block = "IF (condition) THEN ..."
        	break
    	case -1:
        	block = "IF (condition) $block (condition) THEN ..."
        	break
    	case -2:
        	block = "IF (condition) ${block ? "$block (condition) " : ""}ELSE ..."
        	break
    }
    
    cleanUpActions()
	dynamicPage(name: "pageActionGroup", title: "$block", uninstall: false, install: false) {
	    def actions = listActions(conditionId)
        if (actions.size()) {
            section() {
                for(def action in actions) {
                    href "pageAction", params:[actionId: action.id], title: "Action #${action.id}", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
                }
            }
        }
        
        section() {
			href "pageAction", params:[command: "add", conditionId: conditionId], title: "Add an action", required: !actions.size(), state: (actions.size() ? null : "complete"), submitOnChange: true
		}
        
    }
}

def pageAction(params) {
	state.run = "config"
   	//this page has a dual purpose, either action wizard or task manager
    //if no devices have been previously selected, the page acts as a wizard, guiding the use through the selection of devices
    //if at least one device has been previously selected, the page will guide the user through setting up tasks for selected devices
    def action = null
    if (params?.command == "add") {
        action = createAction(params?.conditionId)
    } else {   	
		action = getAction(params?.actionId ? params?.actionId : state.config.actionId)
    }
    if (action) {
    	updateAction(action)
    	def id = action.id
        state.config.actionId = id
        def pid = action.pid
    
        dynamicPage(name: "pageAction", title: "Action #$id", uninstall: false, install: false) {
            def devices = []
            def usedCapabilities = []
            //did we get any devices? search all capabilities
            for(def capability in capabilities()) {
                if (capability.devices) {
                    //only if the capability published any devices - it wouldn't be here otherwise
                    def dev = settings["actDev$id#${capability.name}"]
                    if (dev && dev.size()) {
                        devices = devices + dev
                        //add to used capabilities - needed later
                        if (!(capability.name in usedCapabilities)) {
                            usedCapabilities.push(capability.name)
                        }
                    }
                }
            }
            def locationAction = !!settings["actDev$id#location"]
            def deviceAction = !!devices.size()
            def actionUsed = deviceAction || locationAction
            if (!actionUsed) {
            	//category selection page
                for(def category in listCommandCategories()) {
                    section(title: category) {
                        def options = []
                        for(def command in listCategoryCommands(category)) {
                            def option = getCommandGroupName(command)
                            if (option && !(option in options)) {
                                options.push option
                                if (option.contains("location mode")) {
                                	def controlLocation = settings["actDev$id#location"]
                                	input "actDev$id#location", "bool", title: option, defaultValue: false, submitOnChange: true
                                } else {
                                	href "pageActionDevices", params:[actionId: id, command: command], title: option, submitOnChange: true
                                }
                            }
                        }
                    }
                }
                section(title: "All devices") {
					href "pageActionDevices", params:[actionId: id, command: ""], title: "Control any device", submitOnChange: true
                }
            } else {
            	//actual action page
                if (true || deviceAction) {
                    section() {
                        def names=[]
                        if (deviceAction) {
                            for(device in devices) {
                                def label = getDeviceLabel(device)
                                if (!(label in names)) {
                                	names.push(label)
                                }
                            }
                            href "pageActionDevices", title: "Using...", params:[actionId: id, capabilities: usedCapabilities], description: "${buildNameList(names, "and")}", state: "complete", submitOnChange: true
                        } else {
                        	names.push "location"
                            input "actDev$id#location", "bool", title: "Using location...", state: "complete", defaultValue: true, submitOnChange: true
                        }
                    }
                    def prefix = "actTask$id#"
                    def tasks = settings.findAll{it.key.startsWith(prefix)}
                    def maxId = 1
                    def ids = []
                    //we need to get a list of all existing ids that are used
                    for (task in tasks) {
                        if (task.value) {
                            def tid = task.key.replace(prefix, "")
                            if (tid.isInteger()) {
                                tid = tid.toInteger()
                                maxId = tid >= maxId ? tid + 1 : maxId
                                ids.push(tid)
                            }
                        }
                    }
                    //sort the ids, we really want to have these in the proper order
                    ids = ids.sort()
                    def availableCommands = (deviceAction ? listCommonDeviceCommands(devices, usedCapabilities) : [])
                    for (vcmd in virtualCommands().sort { it.display }) {
                        if ((!(vcmd.display in availableCommands)) && (vcmd.location || deviceAction)) {
                            def ok = true
                        	if (vcmd.requires && vcmd.requires.size()) {
                            	//we have requirements, let's make sure they're fulfilled
                                for (device in devices) {
                                	for (cmd in vcmd.requires) {
                                    	if (!device.hasCommand(cmd)) {
                                        	ok = false
                                            break
                                        }
                                    }
                                    if (!ok) break
                                }
                            }
                            //single device support - some virtual commands require only one device, can't handle more at a time
                            if (ok && (!vcmd.singleDevice || (devices.size() == 1))) {
                                availableCommands.push(virtualCommandPrefix() + vcmd.display)
                            }
                        }
                    }
                    def idx = 0
                    if (ids.size()) {
						for (tid in ids) {
	                    	section(title: idx == 0 ? "First," : "And then") {
                                //display each 
                                input "$prefix$tid", "enum", options: availableCommands, title: "", required: true, state: "complete", submitOnChange: true
                                //parameters
                                def cmd = settings["$prefix$tid"]
                                def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
                                def custom = (cmd && cmd.startsWith(customCommandPrefix()))
                                cmd = cleanUpCommand(cmd)
                                def command = null
                                if (virtual) {
                                    //dealing with a virtual command
                                    command = getVirtualCommandByDisplay(cmd)
                                } else {
                                    command = getCommandByDisplay(cmd)
                                }
                                if (command) {
                                    if (command.parameters) {
                                        def i = 0
                                        for (def parameter in command.parameters) {
                                            def param = parseCommandParameter(parameter)
                                            if (param) {
                                            	if ((command.parameters.size() == 1) && (param.type == "var")) {
                                                	def task = getActionTask(action, tid)
                                                    def desc = getTaskDescription(task)
                                                    desc = "$desc".tokenize("=")
                                                    def title = desc.size() == 2 ? desc[0].trim() : "Set variable..."
                                                    def description = desc.size() == 2 ? desc[1].trim() : null
                                                    href "pageSetVariable", params: [actionId: id, taskId: tid], title: title, description: description, required: true, state: description ? "complete" : null, submitOnChange: true
                                                    if (description) {
                                                    	def value = task_vcmd_setVariable(null, task, true)
                                                    	paragraph "Current evaluation: " + value
                                                    }
													break
                                                }
                                                if (param.type == "attribute") {
                                                    input "actParam$id#$tid-$i", "devices", options: listCommonDeviceAttributes(devices), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
                                                } else if (param.type == "attributes") {
                                                    input "actParam$id#$tid-$i", "devices", options: listCommonDeviceAttributes(devices), title: param.title, required: param.required, submitOnChange: param.last, multiple: true
                                                } else if (param.type == "variable") {
                                                    input "actParam$id#$tid-$i", "enum", options: listVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
                                                } else if (param.type == "variables") {
                                                    input "actParam$id#$tid-$i", "enum", options:  listVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: true
                                                } else if (param.type == "routine") {
                                                	def routines = location.helloHome?.getPhrases()*.label
                                                    input "actParam$id#$tid-$i", "enum", options: routines, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
                                                } else if (param.type == "aggregation") {
                                                	def aggregationOptions = ["First", "Last", "Min", "Avg", "Max", "Sum", "Count", "Boolean And", "Boolean Or", "Boolean True Count", "Boolean False Count"]
                                                    input "actParam$id#$tid-$i", "enum", options: aggregationOptions, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
                                                } else if (param.type == "dataType") {
                                                	def dataTypeOptions = ["boolean", "decimal", "number", "string"]
                                                    input "actParam$id#$tid-$i", "enum", options: dataTypeOptions, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
                                                } else {
                                                    input "actParam$id#$tid-$i", param.type, range: param.range, options: param.options, title: param.title, required: param.required, submitOnChange: param.last || (i == command.varEntry), capitalization: "none"
                                                }
                                                if (param.last && settings["actParam$id#$tid-$i"]) {
                                                    //this is the last parameter, if filled in
                                                    break
                                                }
                                            } else {
                                                paragraph "Invalid parameter definition for $parameter"
                                            }
                                            i += 1
                                        }
                                    }
                                }
                                idx += 1
                            }
                        }
                    }
                    section() {
                        input "$prefix$maxId", "enum", options: availableCommands, title: "Add a task", required: !ids.size(), submitOnChange: true
                    }
				}
            }
                       
            if (actionUsed) {
            	section(title: "Action restrictions") {
                	if (action.pid < 1) {
                    	//this option is only available for the three master action groups
                		input "actRStateChange$id", "bool", title: "Execute on piston status change only", required: false
                    }
                	input "actRMode$id", "mode", title: "Execute in these modes only", description: "Any location mode", required: false, multiple: true
                	input "actRAlarm$id", "enum", options: getAlarmSystemStatusOptions(), title: "Execute during these alarm states only", description: "Any alarm state", required: false, multiple: true
                }

            	section(title: "Advanced options") {
                	paragraph "When an action schedules tasks for a certain device or devices, these new tasks may cause a conflict with pending future scheduled tasks for the same device or devices. The task override scope defines how these conflicts are handled. Depending on your choice, the following pending tasks are cancelled:\n ● None - no pending task is cancelled\n ● Action - only tasks scheduled by the same action are cancelled (default)\n ● Local - only local tasks (scheduled by the same piston) are cancelled\n ● Global - all global tasks (scheduled by any piston in the CoRE) are cancelled"
                	input "actTOS$id", "enum", title: "Task override scope", options:["None", "Action", "Local", "Global"], defaultValue: "Action", required: true
                	input "actTCP$id", "enum", title: "Task cancellation policy", options:["None", "Cancel on piston state change"], defaultValue: "None", required: true
                }

                if (id) {
                    section(title: "Required data - do not change", hideable: true, hidden: true) {            
                        input "actParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change", range: "$pid..${pid+1}", defaultValue: pid
                    }
                }
            }
        }
    }
}

def pageActionDevices(params) {
	state.run = "config"
    def actionId = params?.actionId
    if (!actionId) return
    //convert this to an int - Android thinks this is a float
    actionId = (int) actionId
	def command = params?.command
	def caps = params?.capabilities
    def capabilities = capabilities().findAll{ it.devices }
	if (caps && caps.size()) {
    	capabilities = []
    	//we don't have a list of capabilities to filter by, let's figure things out by using the command
        for(def cap in caps) {
        	def capability = getCapabilityByName(cap)
            if (capability && !(capability in capabilities)) capabilities.push(capability)
        }
    } else {
    	if (command) capabilities = listCommandCapabilities(command)
    }

    if (!capabilities) return
	dynamicPage(name: "pageActionDevices", title: "", uninstall: false, install: false) {
        caps = [:]
        //we got a list of capabilities to display
        def used = []
        for(def capability in capabilities.sort{ it.devices.toLowerCase() }) {
            //go through each and look for "devices" - the user-friendly name of what kind of devices the capability stands for
            if (capability.devices) {
            	if (!(capability.devices in used)) {
                	used.push capability.devices
                	def cap = caps[capability.name] ? caps[capability.name] : []
                	if (!(capability.devices in cap)) cap.push(capability.devices)
                	caps[capability.name] = cap
                }
            }
        }
        if (caps.size()) {
            section() {
            	paragraph "Please select devices from the list${caps.size() > 1 ? "s" : ""} below. When done, please tap the Done to continue"
            }
            for(cap in caps) {
                section() {
                    input "actDev$actionId#${cap.key}", "capability.${cap.key}", title: "Select ${buildNameList(cap.value, "or")}", multiple: true, required: false
                }
            }
        }
    }
}





private pageSetVariable(params) {
	state.run = "config"
    def aid = params?.actionId ? (int) params?.actionId : (int) state.actionId
    def tid = params?.taskId ? (int) params?.taskId : (int) state.taskId
    state.actionId = aid
    state.taskId = tid
    if (!aid) return
    if (!tid) return
	dynamicPage(name: "pageSetVariable", title: "", uninstall: false, install: false) {
        section("Variable") {
            input "actParam$aid#$tid-0", "text", title: "Variable name", required: true, submitOnChange: true, capitalization: "none"
            input "actParam$aid#$tid-1", "enum", title: "Variable data type", options: ["boolean", "decimal", "number", "string", "time"], required: true, submitOnChange: true
            input "actParam$aid#$tid-2", "bool", title: "I know algebra ☺", options: ["boolean", "decimal", "number", "string", "time"], required: true, submitOnChange: true
            //input "actParam$aid#$tid-3", "text", title: "Formula", required: true, submitOnChange: true
        }
        def algebra = settings["actParam$aid#$tid-2"]
        def dataType = settings["actParam$aid#$tid-1"]
        if (algebra) {
        	section() {
                paragraph "Well, too bad. The algebra section is not yet complete..."
            }
        } else {
        	def i = 1
            def operation = ""
            while (dataType) {
                def a1 = i * 4
                def a2 = a1 + 1
                def a3 = a2 + 1
                def op = a3 + 1
                def secondaryDataType = (i == 1 ? dataType : (dataType == "time" ? "decimal" : dataType))
                section(formatOrdinalNumberName(i).capitalize() + " operand") {
                    def val = settings["actParam$aid#$tid-$a1"] != null
                    def var = settings["actParam$aid#$tid-$a2"]
                    if (val || (val == 0) || !var) {
                    	def inputType = secondaryDataType == "boolean" ? "enum" : secondaryDataType
                        input "actParam$aid#$tid-$a1", inputType, range: (i == 1 ? "*..*" : "0..*"), title: "Value", options: ["false", "true"], required: true, submitOnChange: true, capitalization: "none"
                    }
                    if (var || !val) {
                        input "actParam$aid#$tid-$a2", "enum", options: listVariables(true, secondaryDataType), title: (var ? "Variable value" : "...or variable value...") + (var ? "\n[${getVariable(var, true)}]" : ""), required: true, submitOnChange: true
                    }
                    if ((dataType == "time") && (i > 1) && !(operation.contains("*") || operation.contains("÷"))) {
                        input "actParam$aid#$tid-$a3", "enum", options: ["seconds", "minutes", "hours", "days", "weeks", "months", "years"], title: "Time unit", required: true, submitOnChange: true, defaultValue: "minutes"
                    }
                }

            	operation = settings["actParam$aid#$tid-$op"]
                if (operation) operation = "$operation"
                section(title: operation ? "" : "Add operation") {
                	def opts = []
                    switch (dataType) {
                    	case "boolean":
                        	opts += ["AND", "OR"]
                            break
                    	case "string":
                        	opts += ["+ (concatenate)"]
                            break
                    	case "number":
                    	case "decimal":
                    	case "time":
                        	opts += ["+ (add)", "- (subtract)", "* (multiply)", "÷ (divide)"]
                            break
                    }
                    input "actParam$aid#$tid-$op", "enum", title: "Operation", options: opts, required: false, submitOnChange: true
                }
                i += 1
                if (!operation || i > 10) break
            }
        }
    }
}

def pageSimulate() {
	state.run = "config"
	dynamicPage(name: "pageSimulate", title: "", uninstall: false, install: false) {
		section("") {
        	paragraph "Preparing to simulate piston..."
            paragraph "Current piston state is: ${state.currentState}"
        }
        state.sim = [
        	evals: [],
            cmds: []
        ]
        def error
        def perf = now()
        try {
        	broadcastEvent([name: "time", date: new Date(), deviceId: "time", conditionId: null], true, false)
        	processTasks()
        } catch(all) {
        	error = all
        }
        perf = now() - perf
        
        def evals = state.sim.evals
        def cmds = state.sim.cmds
        state.sim = null
        
		section("") {
        	paragraph "Simulation ended in ${perf}ms.", state: "complete"
            paragraph "New piston state is: ${state.currentState}"
            if (error) {
            	paragraph error, required: true, state: null
            }
        }
        section("Evaluations performed") {
            if (evals.size()) {
	            for(msg in evals) {
	                paragraph msg, state: "complete"
	            }
            } else {
            	paragraph "No evaluations have been performed."
            }
        }
        section("Commands executed") {
            if (cmds.size()) {
		    	for(msg in cmds) {
                    paragraph msg, state: "complete"
                }
            } else {
            	paragraph "No commands have been executed."
            }
        }

        section("Scheduled ST job") {
        	def time = getVariable("\$nextScheduledTime")
            paragraph time ? formatLocalTime(time) : "No ST job has been scheduled.", state: time ? "complete" : null
        }

		def tasks = atomicState.tasks
        tasks = tasks ? tasks : [:]
		section("Pending tasks") {
        	if (!tasks.size()) {
            	paragraph "No tasks are currently scheduled."
			} else {
                for(task in tasks.sort { it.value.time } ) {
                    def time = formatLocalTime(task.value.time)
                    if (task.value.type == "evt") {                    
                        paragraph "EVENT - $time\n$task.value"
                    } else {
                        paragraph "COMMAND - $time\n$task.value"
                    }
                }
	        }
        }

    }
}

private buildIfContent() {
	buildIfContent(state.config.app.conditions.id, 0)
}

private buildIfOtherContent() {
	buildIfContent(state.config.app.otherConditions.id, 0)
}

private buildIfContent(id, level) {
	def condition = getCondition(id)
    if (!condition) {
    	return null
    }
    def conditionGroup = (condition.children != null)
    def conditionType = (condition.trg ? "trigger" : "condition")
    level = (level ? level : 0)
    def pre = ""
    def preNot = ""
    def tab = ""
    def aft = ""
    switch (level) {
    	case 1:
        	pre = " ┌ ("
        	preNot = " ┌ NOT ("
        	tab = " │   "
        	aft = " └ )"
	        break;
        case 2:
    	    pre = " │ ┌ ["
    	    preNot = " │ ┌ NOT ["
        	tab = " │ │   "
       		aft = " │ └ ]"
        	break;
        case 3:
	        pre = " │ │ ┌ <"
	        preNot = " │ │ ┌ NOT {"
        	tab = " │ │ │   "
    	    aft = " │ │ └ >"
        	break;
    }
	if (!conditionGroup) {
		href "pageCondition", /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/${conditionType}.png",*/ params: ["conditionId": id], title: "", description: tab + getConditionDescription(id).trim(), state: "complete", required: false, submitOnChange: false
		state.pistonDescription = "${state.pistonDescription}\n${tab + getConditionDescription(id).trim()}"
    } else {
    
        def grouping = settings["condGrouping$id"]
        def negate = settings["condNegate$id"]
    
    	if (pre) {
			href "pageConditionGroupL${level}", /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/folder.png",*/ params: ["conditionId": id], title: "", description: (negate? preNot : pre), state: "complete", required: true, submitOnChange: false
				state.pistonDescription = "${state.pistonDescription}\n${(negate? preNot : pre)}"
        }
        
        def cnt = 0
        for (child in condition.children) {
        	buildIfContent(child.id, level + (child.children == null ? 0 : 1))
            cnt++            
            if (cnt < condition.children.size()) {
            	def page = (level ? "pageConditionGroupL${level}" : (id == 0 ? "pageIf" : "pageIfOther"))
            	href page, /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/" + (level ? "folder.png" : "blank.png"),*/ params: ["conditionId": id], title: "", description: tab + grouping, state: "complete", required: true, submitOnChange: false
							state.pistonDescription = "${state.pistonDescription}\n${tab + grouping}"
            }
        }
        
        if (aft) {
			href "pageConditionGroupL${level}", /*image: "https://raw.githubusercontent.com/ady624/SmartThingers/master/resources/images/folder.png",*/ params: ["conditionId": id], title: "", description: aft, state: "complete", required: true, submitOnChange: false
			state.pistonDescription = "${state.pistonDescription}\n${aft}"
        }
    }
    if (condition.id > 0) {
	    //when true - individual actions
        def actions = listActions(id)
        def sz = actions.size() - 1
        def i = 0
        for (action in actions) {
             href "pageAction", params: ["actionId": action.id], title: "", description: (i == 0 ? "${tab}╠═(when true)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : ""), state: null, required: false, submitOnChange: false
            i = i + 1
						state.pistonDescription = "${state.pistonDescription}\n" + (i == 0 ? "${tab}╠═(when true)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : "")
        }
    } else {
		def value = evaluateCondition(condition)
		paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )                
    }
}


/********** COMMON INITIALIZATION METHODS **********/
def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	if (parent) {
    	initializeCoREPiston()
    } else {
    	initializeCoRE()
    }
}



/******************************************************************************/
/*** 																		***/
/*** COMMON PUBLISHED METHODS												***/
/*** 																		***/
/******************************************************************************/

def mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

def cpu() {
    if (state.lastExecutionTime == null) {
    	return "N/A"
    } else {
    	def cpu = Math.round(state.lastExecutionTime / 20000)
        if (cpu > 100) {
        	cpu = 100
        }
        return "$cpu%"
    }
}

def getVariable(name, forDisplay) {
	def value = getVariable(name)
    if (forDisplay) {
        if ((value instanceof Long) && (value >= 999999999999)) return formatLocalTime(value)
    }
    return value
}

def getVariable(name) {
    name = sanitizeVariableName(name)
    if (name == "\$now") return now()
    if (name == "\$hour24") return adjustTime().hours
    if (name == "\$hour") {
    	def h = adjustTime().hours
    	return (h == 0 ? 12 : (h > 12 ? h - 12 : h))
    }
    if (name == "\$meridian") {
    	def h = adjustTime().hours
    	return ( h < 12 ? "AM" : "PM")
    }
    if (name == "\$meridianWithDots") {
    	def h = adjustTime().hours
    	return ( h <12 ? "A.M." : "P.M.")
    }
    if (name == "\$minute") return adjustTime().minutes
    if (name == "\$second") return adjustTime().seconds
    if (name == "\$day") return adjustTime().date
    if (name == "\$dayOfWeek") return getDayOfWeekNumber()
    if (name == "\$dayOfWeekName") return getDayOfWeekName()
    if (name == "\$month") return adjustTime().month + 1
    if (name == "\$monthName") return getMonthName()
    if (name == "\$year") return adjustTime().year + 1900
    if (name == "\$now") return now()
    if (name == "\$random") return Math.random()
    if (name == "\$randomColor") return getColorByName("Random").rgb
    if (name == "\$randomColorName") return getColorByName("Random").name
    if (name == "\$randomLevel") return (int)Math.round(100 * Math.random())
    if (name == "\$currentStateDuration") {
    	try {
        	return state.systemStore["\$currentStateSince"] ? now() - (new Date(state.systemStore["\$currentStateSince"])).time : null
        } catch(all) {
        	return null
        }
    }
	if (!name) {
    	return null
    }
    if (parent && name.startsWith("@")) {
    	return parent.getVariable(name)
    } else {
    	if (name.startsWith("\$")) {
			return state.systemStore[name]
        } else {
			return state.store[name]
    	}
    }
}

def setVariable(name, value, system = false) {
    name = sanitizeVariableName(name)
	if (!name) {
    	return
    }
    if (parent && name.startsWith("@")) {
    	parent.setVariable(name, value)
    } else {
    	if (name.startsWith("\$")) {
        	if (system) {
				state.systemStore[name] = value
            }
        } else {
	    	debug "Storing variable $name with value $value"
			state.store[name] = value
    	}
    }
    //TODO: date&time triggers based on variables being changed need to be reevaluated
}

private testDataType(value, dataType) {
	if (!dataType || !value) return true
	switch (dataType) {
    	case "bool":
    	case "boolean":
        case "string":
        	return true
        case "time":
        	return (value instanceof Long) && (value > 999999999999)
        case "number":
        case "decimal":
        	return !((value instanceof Long) && (value > 999999999999)) && ("$value".isInteger() || "$value".isFloat())
    }
    return false
}

def listVariables(config = false, dataType = null, listLocal = true, listGlobal = true, listSystem = true, listState = false) {
	def result = []
    def parentResult = null
    def systemResult = []
    if (listLocal) {
        for (variable in state.store) {
        	if (!dataType || testDataType(variable.value, dataType)) {
	            result.push(variable.key)
            }
        }
    }
    if (listSystem) {
        for (variable in state.systemStore) {
        	if (!dataType || testDataType(variable.value, dataType)) {
	            systemResult.push(variable.key)
            }
        }
    }
    if (listGlobal) {
        if (parent) {
            parentResult = parent.listVariables(config, dataType, listLocal, listGlobal, false, listState)
        }
    }
    if (parent && config) {
    	//look for variables set during conditions
        def list = settings.findAll{it.key.startsWith("condVar") && !it.key.contains("#")}
        for (it in list) {
        	def var = sanitizeVariableName(it.value)
            if (var) {
            	if (var.startsWith("@")) {
                	//global
                    if (listGlobal && !(var in parentResult)) {
                        if (!dataType || testDataType(it.value, dataType)) {
                            parentResult.push(var)
                        }
                    }
                } else {
                	//local
                    if (listLocal && !(var in result)) {
                        if (!dataType || testDataType(it.value, dataType)) {
                            result.push(var)
                        }
                    }
                }
            }
        }
        //look for tasks that set variables...
        list = settings.findAll{it.key.startsWith("actTask")}
        for (it in list) {
        	if (it.value) {
            	def virtualCommand = getVirtualCommandByDisplay(cleanUpCommand(it.value))
                if (virtualCommand && (virtualCommand.varEntry != null)) {
                	def var = sanitizeVariableName(settings[it.key.replace("actTask", "actParam") + "-${virtualCommand.varEntry}"])
                    if (var) {
                        if (var.startsWith("@")) {
                            //global
                            if (!(var in parentResult)) {
                                parentResult.push(var)
                            }
                        } else {
                            //local
                            if (!(var in result)) {
                                result.push(var)
                            }
                        }
                    }
				}
            }
        }
    }
    return result.sort() + (parentResult ? parentResult.sort() : []) + systemResult.sort()
}













/******************************************************************************/
/***																		***/
/*** CoRE CODE																***/
/***																		***/
/******************************************************************************/


/******************************************************************************/
/*** CoRE INITIALIZATION METHODS											***/
/******************************************************************************/

def initializeCoRE() {
    state.store = state.store ? state.store : [:]
    state.systemStore = state.systemStore ? state.systemStore : initialSystemStore()
}

def childUninstalled() {}

/******************************************************************************/
/*** CoRE PUBLISHED METHODS													***/
/******************************************************************************/

def expertMode() {
	return !!settings["expertMode"]
}

def updateChart(name, value) {
	def charts = atomicState.charts
    charts = charts ? charts : [:]
    def modified = false
    def lastQuarter = getPreviousQuarterHour()
    def chart = charts[name]
    if (!chart) {
    	//create a log with that name
        chart = [:]
        //create the log for the last 96 quarter-hours
        def quarter = lastQuarter
        for (def i = 0; i < 96; i++) {
        	chart["$i"] = [q: quarter, t: 0, c: 0]
            //chart["q$i"].q = quarter
            //chart["q$i"].t = 0
            //chart["q$i"].c = 0
            quarter = quarter - 900000
        }
        charts[name] = chart
        modified = true
    }
    if (lastQuarter != chart["0"].q) {
    	//we need to advance the log
        def steps = Math.floor((lastQuarter - chart["0"].q) / 900000).toInteger()
        if (steps != 0) {
        	modified = true
        	//we need to shift the log, we're in a different current quarter
            if ((steps < 1) || (steps > 95)) {
            	//in case of weird things, we reset the whole log
                steps = 96
            }
            if (steps < 96) {
                //reset the log as it seems we have a problem
                for (def i = 95; i >= steps; i--) {
                    chart["$i"] = chart["${i-steps}"]
                    //chart["q$i"].q = chart["q${i-steps}"].q
                    //chart["q$i"].c = chart["q${i-steps}"].c
                    //chart["q$i"].t = chart["q${i-steps}"].t
                }
            }
            //reset the new quarters
            def quarter = lastQuarter
            for (def i = 0; i < steps; i++) {
                chart["$i"] = [q: quarter, t: 0, c:0]
                //chart["q$i"].t = 0
                //chart["q$i"].c = 0
                quarter = quarter - 900000
            }        	
        }
    }
    if (value) {
    	modified = true
        chart["0"].t = chart["0"].t + value
        chart["0"].c = chart["0"].c + 1
    }
    if (modified) {
    	charts[name] = chart
	    //state.charts = charts
        //atomicState.charts = charts
        atomicState.charts = charts
        state.charts = charts
    }
   	return null
}

def generatePistonName() {
	if (parent) {
    	return null
    }
    def apps = getChildApps()
	def i = 1
    while (true) {
    	def name = i == 5 ? "Mambo No. 5" : "CoRE Piston #$i"
        def found = false
        for (app in apps) {
        	if (app.label == name) {
                found = true
            	break
            }
        }
        if (found) {
           	i++
        	continue
        }
        return name
    }
}








/******************************************************************************/
/***																		***/
/*** CoRE PISTON CODE														***/
/***																		***/
/******************************************************************************/


/******************************************************************************/
/*** CoRE PISTON INITIALIZATION METHODS										***/
/******************************************************************************/

def initializeCoREPiston() {
	// TODO: subscribe to attributes, devices, locations, etc.
    //move app to production
	state.run = "config"
    state.temp = null
    state.debugLevel = 0
	debug "Initializing app...", 1
    cleanUpConditions(true)
    state.app = state.config ? state.config.app : state.app
    //save misc
    state.app.mode = settings.mode
    
	state.run = "app"
    
    state.cache = [:]
    state.tasks = state.tasks ? state.tasks : [:]
    state.store = state.store ? state.store : [:]
    state.systemStore = state.systemStore ? state.systemStore : initialSystemStore()
    for (var in initialSystemStore()) {
    	if (!state.containsKey(var.key)) {
        	state.systemStore[var.key] = null
        }
    }
    
    subscribeToAll(state.app)
  
    subscribe(app, appHandler)
    
    state.remove("config")
    //uncomment next line to clear system store
    //state.systemStore = [:]
    setVariable("\$lastInitialized", now(), true)
    setVariable("\$now", null, true)
    setVariable("\$currentStateDuration", null, true)
    setVariable("\$currentState", state.currentState, true)
    setVariable("\$currentStateSince", state.currentStateSince, true)
    processTasks()
	debug "Done", -1

	//we need to finalize to write atomic state
	//save all atomic states to state
    //to avoid race conditions
	//state.cache = atomicState.cache
    //state.tasks = atomicState.tasks
}

/* prepare configuration version of app */
private configApp() {
	//TODO: rebuild (object-oriented) app object from settings
	//prepare stores    
    state.temp = [:]
    state.store = state.store ? state.store : [:]
    state.systemStore = state.systemStore ? state.systemStore : initialSystemStore()
    
	if (!state.config) {
    	//initiate config app, since we have no running version yet (not yet installed)
        state.config = [:]
        state.config.conditionId = 0
    	state.config.app = state.app ? state.app : null
        if (!state.config.app) {
        	state.config.app = [:]
            //create the root condition
            state.config.app.conditions = createCondition(true)
            state.config.app.conditions.id = 0
            state.config.app.otherConditions = createCondition(true)
            state.config.app.otherConditions.id = -1
            state.config.app.actions = []
        }
    }
    //get expert savvy
    state.config.expertMode = parent.expertMode()
	state.config.app.mode = settings.mode
}
private subscribeToAll(app) {
	debug "Initializing subscriptions...", 1
	//we have to maintain two separate logic threads for the latching mode
    //to do so, we first simulate
    state.deviceSubscriptions = 0
	def hasTriggers = getConditionHasTriggers(app.conditions)
   	def hasLatchingTriggers = false
    
   	if (settings.mode in ["Latching", "And-If", "Or-If"]) {
    	//we really get the count
    	hasLatchingTriggers = getConditionHasTriggers(app.otherConditions)
		//simulate subscribing to both lists
		def subscriptions = subscribeToDevices(app.conditions, hasTriggers, null, null, null, null)
		def latchingSubscriptions = subscribeToDevices(app.otherConditions, hasLatchingTriggers, null, null, null, null)
        //we now have the two lists that we'd be subscribing to, let's figure out the common elements
        def commonSubscriptions = [:]
        for (subscription in subscriptions) {
        	if (latchingSubscriptions.containsKey(subscription.key)) {
            	//found a common subscription, save it
                commonSubscriptions[subscription.key] = true
            }
        }
        //perform subscriptions
		subscribeToDevices(app.conditions, false, bothDeviceHandler, null, commonSubscriptions, null)
		subscribeToDevices(app.conditions, hasTriggers, deviceHandler, null, null, commonSubscriptions)
		subscribeToDevices(app.otherConditions, hasLatchingTriggers, latchingDeviceHandler, null, null, commonSubscriptions)       
    } else {
    	//simple IF case, no worries here
    	subscribeToDevices(app.conditions, hasTriggers, deviceHandler, null, null, null)
    }
	debug "Finished subscribing", -1
}

private subscribeToDevices(condition, triggersOnly, handler, subscriptions, onlySubscriptions, excludeSubscriptions) {
	if (subscriptions == null) {
    	subscriptions = [:]
    }
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                subscribeToDevices(child, triggersOnly, handler, subscriptions, onlySubscriptions, excludeSubscriptions)
            }
        } else {
        	if (condition.trg || !triggersOnly) {
            	//get the details
                def capability = getCapabilityByDisplay(condition.cap)
            	def devices = capability.virtualDevice ? (capability.attribute == "time" ? [] : [capability.virtualDevice]) : settings["condDevices${condition.id}"]
                def attribute = capability.virtualDevice ? capability.attribute : condition.attr
                if (devices) {
                	for (device in devices) {
                    	def subscription = "${device.id}-${attribute}"
                        if ((excludeSubscriptions == null) || !(excludeSubscriptions[subscription])) {
                        	//if we're provided with an exclusion list, we don't subscribe to those devices/attributes events
                            if ((onlySubscriptions == null) || onlySubscriptions[subscription]) {
                            	//if we're provided with a restriction list, we use it
                                if (!subscriptions[subscription]) {
                                    subscriptions[subscription] = true //[deviceId: device.id, attribute: attribute]
                                    if (handler) {
	                                    //we only subscribe to the device if we're provided a handler (not simulating)
                                        debug "Subscribing to events from $device for attribute $attribute, handler is $handler", null, "trace"
                                        subscribe(device, attribute, handler)
                                        state.deviceSubscriptions = state.deviceSubscriptions ? state.deviceSubscriptions + 1 : 1
                                        //initialize the cache for the device - this will allow the triggers to work properly on first firing
                                        state.cache[device.id + "-" + attribute] = [v: device.currentValue(attribute), t: now()]
                                        
                                    }
                                }
                            }
						}
                    }
                } else {
                	return
                }
            }
        }
    }
    return subscriptions
}













/******************************************************************************/
/*** CoRE PISTON CONFIGURATION METHODS										***/
/******************************************************************************/

//creates a condition (grouped or not)
private createCondition(group) {
    def condition = [:]
    //give the new condition an id
    condition.id = (int) getNextConditionId()
    //initiate the condition type
    if (group) {
    	//initiate children
        condition.children = []
        condition.actions = []
    } else {
    	condition.type = null
    }
    return condition
}

//creates a condition and adds it to a parent
private createCondition(parentConditionId, group) {	
    def parent = getCondition(parentConditionId)
    if (parent) {
		def condition = createCondition(group)
    	//preserve the parentId so we can rebuild the app from settings
    	condition.parentId = parent ? (int) parent.id : null
        //calculate depth for new condition
        condition.level = (parent.level ? parent.level : 0) + 1
   		//add the new condition to its parent, if any
        //set the parent for upwards traversal
   		parent.children.push(condition)
    	//return the newly created condition
    	return condition
	}
    return null
}

//deletes a condition
private deleteCondition(conditionId) {
	def condition = getCondition(conditionId)
    if (condition) {
    	def parent = getCondition(condition.parentId)
        if (parent) {
			parent.children.remove(condition);
        }
    }
}

private updateCondition(condition) {
	condition.cap = settings["condCap${condition.id}"]
	condition.dev = []
    condition.sdev = settings["condSubDev${condition.id}"]
    condition.attr = cleanUpAttribute(settings["condAttr${condition.id}"])
    switch (condition.cap) {
    	case "Date & Time":
        	condition.attr = "time"
            condition.dev.push "time"
        	break
        case "Mode":
        case "Location Mode":
        	condition.attr = "mode"
            condition.dev.push "location"
            break
        case "Smart Home Monitor":
        	condition.attr = "alarmSystemStatus"
            condition.dev.push "location"
            break
        case "Routine":
        	condition.attr = "routineExecuted"
            condition.dev.push "location"
            break
        case "Variable":
        	condition.attr = "variable"
            condition.dev.push "location"
            break
    }
    if (!condition.attr) {
	    def cap = getCapabilityByDisplay(condition.cap)
        if (cap && cap.attribute) {
    		condition.attr = cap.attribute
            if (cap.virtualDevice) condition.dev.push(cap.virtualDevice)
        }
    }
    for (device in settings["condDevices${condition.id}"])
    {
        //save the list of device IDs - we can't have the actual device objects in the state
        condition.dev.push(device.id)
    }
    condition.comp = cleanUpComparison(settings["condComp${condition.id}"])
    condition.var = settings["condVar${condition.id}"]
    condition.dt = settings["condDataType${condition.id}"]
    condition.trg = !!isComparisonOptionTrigger(condition.attr, condition.comp)
	condition.mode = condition.trg ? "Any" : (settings["condMode${condition.id}"] ? settings["condMode${condition.id}"] : "Any")
    condition.val1 = settings["condValue${condition.id}#1"]
    condition.dev1 = settings["condDev${condition.id}#1"] ? getDeviceLabel(settings["condDev${condition.id}#1"]) : null
    condition.attr1 = settings["condAttr${condition.id}#1"] ? getDeviceLabel(settings["condAttr${condition.id}#1"]) : null
    condition.var1 = settings["condVar${condition.id}#1"]
    condition.val2 = settings["condValue${condition.id}#2"]
    condition.dev2 = settings["condDev${condition.id}#2"] ? getDeviceLabel(settings["condDev${condition.id}#2"]) : null
    condition.attr2 = settings["condAttr${condition.id}#2"] ? getDeviceLabel(settings["condAttr${condition.id}#2"]) : null
    condition.var2 = settings["condVar${condition.id}#2"]
    condition.for = settings["condFor${condition.id}"]
    condition.fort = settings["condTime${condition.id}"]
    condition.t1 = settings["condTime${condition.id}#1"]
    condition.t2 = settings["condTime${condition.id}#2"]
    condition.o1 = settings["condOffset${condition.id}#1"]
    condition.o2 = settings["condOffset${condition.id}#2"]
    condition.e = settings["condEvery${condition.id}"]
    condition.e = condition.e ? condition.e : 5
    condition.m = settings["condMinute${condition.id}"]
    
	//time repeat
    condition.r = settings["condRepeat${condition.id}"]
    condition.re = settings["condRepeatEvery${condition.id}"]
    condition.re = condition.re ? condition.re : 2
    condition.rd = settings["condRepeatDay${condition.id}"]
    condition.rdw = settings["condRepeatDayOfWeek${condition.id}"]
    condition.rm = settings["condRepeatMonth${condition.id}"]
    
    //time filters
    condition.fmh = settings["condMOH${condition.id}"]
    condition.fhd = settings["condHOD${condition.id}"]
    condition.fdw = settings["condDOW${condition.id}"]
    condition.fdm = settings["condDOM${condition.id}"]
    condition.fwm = settings["condWOM${condition.id}"]
    condition.fmy = settings["condMOY${condition.id}"]
    condition.fy = settings["condY${condition.id}"]

	condition.grp = settings["condGrouping${condition.id}"]
    condition.grp = condition.grp && condition.grp.size() ? condition.grp : "AND"
    condition.not = !!settings["condNegate${condition.id}"]
    
    //variables
    condition.vd = settings["condVarD${condition.id}"]
    condition.vs = settings["condVarS${condition.id}"]
    condition.vm = settings["condVarM${condition.id}"]
    condition.vn = settings["condVarN${condition.id}"]
    condition.vt = settings["condVarT${condition.id}"]
    condition.vv = settings["condVarV${condition.id}"]
    condition.vf = settings["condVarF${condition.id}"]
    condition.vw = settings["condVarW${condition.id}"]
    
    condition = cleanUpMap(condition)
    return null
}

//used to get the next id for a condition, action, etc - looks into settings to make sure we're not reusing a previously used id
private getNextConditionId() {
	def nextId = getLastConditionId(state.config.app.conditions) + 1
	def otherNextId = getLastConditionId(state.config.app.otherConditions) + 1
    nextId = nextId > otherNextId ? nextId : otherNextId
    while (settings.findAll { it.key == "condParent" + nextId }) {
    	nextId++
    }
    return (int) nextId
}

//helper function for getNextId
private getLastConditionId(parent) {
	if (!parent) {
    	return -1
    }
	def lastId = parent?.id    
    for (child in parent.children) {
        def childLastId = getLastConditionId(child)
        lastId = lastId > childLastId ? lastId : childLastId
    }
    return lastId
}


//creates a condition (grouped or not)
private createAction(parentId) {
    def action = [:]
    //give the new condition an id
    action.id = (int) getNextActionId()
    action.pid = (int) parentId
    state.config.app.actions.push(action)
    return action
}

private getNextActionId() {
	def nextId = 1
    for(action in state.config.app.actions) {
    	if (action.id > nextId) {
        	nextId = action.id + 1
        }
    }
    while (settings.findAll { it.key == "actParent" + nextId }) {
    	nextId++
    }
    return (int) nextId
}

private updateAction(action) {
	if (!action) return null
    def id = action.id
    def devices = []
    def usedCapabilities = []
    //did we get any devices? search all capabilities
    for(def capability in capabilities()) {
        if (capability.devices) {
            //only if the capability published any devices - it wouldn't be here otherwise
            def dev = settings["actDev$id#${capability.name}"]
            if (dev && dev.size()) {
                devices = devices + dev
                //add to used capabilities - needed later
                if (!(capability.name in usedCapabilities)) {
                    usedCapabilities.push(capability.name)
                }
            }
        }
    }
    action.d = []
    for(device in devices) {
    	if (!(device.id in action.d)) {
    		action.d.push(device.id)
        }
    }
    action.l = settings["actDev$id#location"]
    
    //restrictions
    action.rc = settings["actRStateChange$id"]
    action.ra = settings["actRAlarm$id"]
    action.rm = settings["actRMode$id"]
    
    action.tos = settings["actTOS$id"]
    action.tcp = settings["actTCP$id"]
    
    //look for tasks
    action.t = []
    def prefix = "actTask$id#"
    def tasks = settings.findAll{it.key.startsWith(prefix)}
    def ids = []
    //we need to get a list of all existing ids that are used
    for (item in tasks) {
        if (item.value) {
            def tid = item.key.replace(prefix, "")
            if (tid.isInteger()) {
                tid = tid.toInteger()
                def task = [ i: tid + 0 ]
                //get task data
                //get command
                def cmd = settings["$prefix$tid"]
                task.c = cmd
                task.p = []
                def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
                def custom = (cmd && cmd.startsWith(customCommandPrefix()))
                cmd = cleanUpCommand(cmd)
                def command = null
                if (virtual) {
                    //dealing with a virtual command
                    command = getVirtualCommandByDisplay(cmd)
                } else {
                    command = getCommandByDisplay(cmd)
                }
                if (command) {
                	if (command.name == "setVariable") {
                    	//setVariable is different, we've got a variable number of parameters...
                        //variable name
                        task.p.push([i: 0, t: "variable", d: settings["actParam$id#$tid-0"], v: 1])
                        //data type
                        def dataType = settings["actParam$id#$tid-1"]
                        task.p.push([i: 1, t: "text", d: dataType])
                        //algebra
                        task.p.push([i: 2, t: "bool", d: !!settings["actParam$id#$tid-2"]])
                        //formula
                        task.p.push([i: 3, t: "text", d: settings["actParam$id#$tid-3"]])
                        def i = 4
                        while (true) {
                        	//value
	                        task.p.push([i: i, t: dataType, d: settings["actParam$id#$tid-$i"]])
                            //variable name
	                        task.p.push([i: i + 1, t: "text", d: settings["actParam$id#$tid-${i + 1}"]])
                            //variable name
	                        task.p.push([i: i + 2, t: "text", d: settings["actParam$id#$tid-${i + 2}"]])
                            //next operation
                            def operation = settings["actParam$id#$tid-${i + 3}"]
                            if (!operation) break
	                        task.p.push([i: i + 3, t: "text", d: operation])
                            if (dataType == "time") dataType = "decimal"
                            i = i + 4
                        }
                    } else if (command.parameters) {
                        def i = 0
                        for (def parameter in command.parameters) {
                            def param = parseCommandParameter(parameter)
                            if (param) {
                            	def type = param.type
                                def data = settings["actParam$id#$tid-$i"]
                                def var = (command.varEntry == i)
                                if (var) {
                                    task.p.push([i: i, t: type, d: data, v: 1])
                                } else {
                                    task.p.push([i: i, t: type, d: data])
                                }
                            }
                            i++
                        }
                    }
                }
                action.t.push(task)
            }
        }
    }
    //clean up for memory optimization
    action = cleanUpMap(action)    
}

private cleanUpActions() {
	for(action in state.config.app.actions) {
    	updateAction(action)
    }
   	def dirty = true
    while (dirty) {
    	dirty = false
		for(action in state.config.app.actions) {
			if (!((action.d && action.d.size()) || action.l)) {
            	state.config.app.actions.remove(action)
                dirty = true
                break
            }
    	}
    }
}

private listActionDevices(actionId) {
    def devices = []
    //did we get any devices? search all capabilities
    for(def capability in capabilities()) {
        if (capability.devices) {
            //only if the capability published any devices - it wouldn't be here otherwise
            def dev = settings["actDev$actionId#${capability.name}"]
            for (d in dev) {
            	if (!(d in devices)) {
                	devices.push(d)
                }
            }
        }
    }
	return devices
}
private getActionDescription(action) {
	if (!action) return null
    def devices = (action.l ? ["location"] : listActionDevices(action.id))
    def result = ""
    if (action.rc) {
    	result += "® If piston state changes...\n"
    }
    if (action.rm) {
    	result += "® If mode is ${buildNameList(action.rm, "or")}...\n"
    }
    if (action.ra) {
    	result += "® If alarm is ${buildNameList(action.ra, "or")}...\n"
    }
    result += (result ? "\n" : "") + "Using " + buildDeviceNameList(devices, "and")+ "..."
    for (task in action.t.sort{it.i}) {
    	def t = cleanUpCommand(task.c)
        if (task.p && task.p.size()) {
        	t += " ["
            def i = 0
            for(param in task.p.sort{ it.i }) {
            	t += (i > 0 ? ", " : "") + (param.v ? "{${param.d}}" : "${param.d}")
                i++
            }
        	t += "]"
        }
        result += "\n ► " + getTaskDescription(task)
    }
    return result
}

private getTaskDescription(task) {
	if (!task) return "[ERROR]"
    def virtual = (task.c && task.c.startsWith(virtualCommandPrefix()))
    def custom = (task.c && task.c.startsWith(customCommandPrefix()))
	def command = cleanUpCommand(task.c)
    
    if (custom) {
		return task.c
    }
    
    def cmd = (virtual ? getVirtualCommandByDisplay(command) : getCommandByDisplay(command))    
    if (!cmd) return "[ERROR]"
    
    if (cmd.name == "setVariable") {
    	if (task.p.size() < 7) return "[ERROR]"
        def name = task.p[0].d
        def dataType = task.p[1].d
        def algebra = !!task.p[2].d
        if (!name || !dataType) return "[ERROR]"
        def result = "Set $dataType variable {$name} = "
        if (algebra) {
        	return result + "<complex algebra not ready yet>"
        } else {
            def i = 4
            def grouping = false
            def groupingUnit = ""
            while (true) {
            	def value = task.p[i].d
               	def variable = value ? (dataType == "string" ? "\"$value\"" : "$value") : "${task.p[i + 1].d}"
                def unit = (dataType == "time" ? task.p[i + 2].d : null)
                def operation = task.p.size() > i + 3 ? "${task.p[i + 3].d} ".tokenize(" ")[0] : null                
                def needsGrouping = (operation == "*") || (operation == "÷") || (operation == "AND")
                if (needsGrouping) {
                    //these operations require grouping i.e. (a * b * c) seconds
                    if (!grouping) {
                        grouping = true
                        groupingUnit = unit
                        result += "("
                    }
                }
                //add the value/variable 
                result += variable + (!grouping && unit ? " $unit" : "")
                if (grouping && !needsGrouping) {
                    //these operations do NOT require grouping
                    //ungroup
                    grouping = false
                    result += ")${groupingUnit ? " $groupingUnit" : ""}"
                }
                if (!operation) break
                result += " $operation "
				i += 4                
            }        	
        }        
        return result
    	//special case for setVariable as the number of parameters is variable
    
    
    } else if (cmd.name == "setColor") {
    	def result = "Set color to "
        if (task.p[0].d) return result + "\"${task.p[0].d}\""
        if (task.p[1].d) return result + "RGB(${task.p[1].d})"
        return result + "HSL(${task.p[2].d}°, ${task.p[3].d}%, ${task.p[4].d}%)"
	} else {
    	return formatMessage(cmd.description ? cmd.description : cmd.display, task.p)
    }
}















/******************************************************************************/
/*** ENTRY AND EXIT POINT HANDLERS											***/
/******************************************************************************/

def appHandler() {
}

def deviceHandler(evt) {
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
    def perf = now()
	debug "Received a primary block device event", 1
    broadcastEvent(evt, true, false)
    //process tasks
    processTasks()
    perf = now() - perf
    debug "Done in ${perf}ms", -1
    exitPoint(perf)
}

def latchingDeviceHandler(evt) {
	entryPoint()
    
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
    def perf = now()
	debug "Received a secondary block device event", 1
    broadcastEvent(evt, false, true)
    //process tasks
    processTasks()
    perf = now() - perf
    debug "Done in ${perf}ms", -1
    exitPoint(perf)
}

def bothDeviceHandler(evt) {
	entryPoint()
    
	//executes whenever a common use device has an event
	//broadcast to both IF blocks
    def perf = now()
	debug "Received a dual block device event", 1
    broadcastEvent(evt, true, true)
    //process tasks
    processTasks()
    perf = now() - perf
    debug "Done in ${perf}ms", -1
    exitPoint(perf)
}

def timeHandler() {
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
    def perf = now()
    debug "Received a time event", 1
    processTasks()
    perf = now() - perf
    debug "Done in ${perf}ms", -1
    exitPoint(perf)
}

def recoveryHandler() {
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
    def perf = now()
    debug "CAUTION: Received a recovery event", 1, "warn"
    processTasks()
    perf = now() - perf
    debug "Done in ${perf}ms", -1
    exitPoint(perf)
}
private entryPoint() {
	//initialize whenever app runs
    //use the "app" version throughout
    state.run = "app"
    state.sim = null
	state.debugLevel = 0
	state.tasker = state.tasker ? state.tasker : []
}

private exitPoint(milliseconds) {
	def runStats = atomicState.runStats
	if (runStats == null) {
    	runStats = [:]
    }
    runStats.executionSince = runStats.executionSince ? runStats.executionSince : now()
    runStats.executionCount = runStats.executionCount ? runStats.executionCount + 1 : 1
    runStats.executionTime = runStats.executionTime ? runStats.executionTime + milliseconds : milliseconds
    runStats.minExecutionTime = runStats.minExecutionTime && runStats.minExecutionTime < milliseconds ? runStats.minExecutionTime : milliseconds
    runStats.maxExecutionTime = runStats.maxExecutionTime && runStats.maxExecutionTime > milliseconds ? runStats.maxExecutionTime : milliseconds
    runStats.lastExecutionTime = milliseconds
    
    def lastEvent = state.lastEvent
    if (lastEvent && lastEvent.delay) {
        runStats.eventDelay = runStats.eventDelay ? runStats.eventDelay + lastEvent.delay : lastEvent.delay
        runStats.minEventDelay = runStats.minEventDelay && runStats.minEventDelay < lastEvent.delay ? runStats.minEventDelay : lastEvent.delay
        runStats.maxEventDelay = runStats.maxEventDelay && runStats.maxEventDelay > lastEvent.delay ? runStats.maxEventDelay : lastEvent.delay
        runStats.lastEventDelay = lastEvent.delay
    }
    setVariable("\$previousEventExecutionTime", milliseconds, true)
    state.lastExecutionTime = milliseconds
	parent.updateChart("exec", milliseconds)
    atomicState.runStats = runStats
   


	//save all atomic states to state
    //to avoid race conditions
	state.cache = atomicState.cache
    state.tasks = atomicState.tasks
    state.runStats = atomicState.runStats   
    state.temp = null
    state.sim = null
}











/******************************************************************************/
/*** EVENT MANAGEMENT FUNCTIONS												***/
/******************************************************************************/

private broadcastEvent(evt, primary, secondary) {
	//filter duplicate events and broadcast event to proper IF blocks
    def perf = now()
    def delay = perf - evt.date.getTime()
	debug "Processing event ${evt.name}${evt.device ? " for device ${evt.device}" : ""}${evt.deviceId ? " with id ${evt.deviceId}" : ""}${evt.value ? ", value ${evt.value}" : ""}, generated on ${evt.date}, about ${delay}ms ago", 1, "trace"
    //save previous event
	setVariable("\$previousEventReceived", getVariable("\$currentEventReceived"), true)
    setVariable("\$previousEventDevice", getVariable("\$currentEventDevice"), true)
    setVariable("\$previousEventDeviceIndex", getVariable("\$currentEventDeviceIndex"), true)
    setVariable("\$previousEventAttribute", getVariable("\$currentEventAttribute"), true)
    setVariable("\$previousEventValue", getVariable("\$currentEventValue"), true)
    setVariable("\$previousEventDate", getVariable("\$currentEventDate"), true)
    setVariable("\$previousEventDelay", getVariable("\$currentEventDelay"), true)        
    def lastEvent = [
    	event: [
        	device: evt.device ? "${evt.device}" : evt.deviceId,
            name: evt.name,
            value: evt.value,
            date: evt.date
        ],
        delay: delay
    ]
    state.lastEvent = lastEvent
    setVariable("\$currentEventReceived", perf, true)
    setVariable("\$currentEventDevice", lastEvent.event.device, true)
    setVariable("\$currentEventDeviceIndex", 0, true)
    setVariable("\$currentEventAttribute", lastEvent.event.name, true)
    setVariable("\$currentEventValue", lastEvent.event.value, true)
    setVariable("\$currentEventDate", lastEvent.event.date && lastEvent.event.date instanceof Date ? lastEvent.event.date.time : null, true)
    setVariable("\$currentEventDelay", lastEvent.delay, true)    
    try {
	    parent.updateChart("delay", delay)
    } catch(e) {
    	debug "ERROR: Could not update delay chart: $e", null, "error"
    }
    if (evt.deviceId != "time") {
    	def cache = atomicState.cache
        cache = cache ? cache : [:]
    	def cachedValue = cache[evt.deviceId + '-' + evt.name]
    	def eventTime = evt.date.getTime()
		cache[evt.deviceId + '-' + evt.name] = [o: cachedValue ? cachedValue.v : null, v: evt.value, t: eventTime ]
    	atomicState.cache = cache
        state.cache = cache
		if (cachedValue) {
	    	if ((cachedValue.v == evt.value) && (!evt.data) && (/*(cachedValue.v instanceof String) || */(eventTime < cachedValue.t) || (cachedValue.t + 1000 > eventTime))) {
	        	//duplicate event
	    		debug "WARNING: Received duplicate event for device ${evt.device}, attribute ${evt.name}='${evt.value}', ignoring...", null, "warn"
	            evt = null
	        }
	    }
	}
    try {
        if (evt) {  
            //broadcast to primary IF block
            def result1 = null
            def result2 = null
            //some piston modes require evaluation of secondary conditions regardless of eligibility - we use force then
            def force = false
            if (mode in ["And-If", "Or-If"]) {
            	//these two modes always evaluate both blocks
            	primary = true
                secondary = true
                force = true
            }
            
            if (primary) {
                result1 = evaluateConditionSet(evt, true, force)
                state.lastPrimaryEvaluationResult = result1
                state.lastPrimaryEvaluationDate = now()
                def msg = "Primary IF block evaluation result is $result1"
                if (state.sim) state.sim.evals.push(msg)
                debug msg
                
                switch (mode) {
                	case "Then-If":
                    	//execute the secondary branch if the primary one is true
                    	secondary = result1
                        force = true
                		break
                	case "Else-If":
                    	//execute the second branch if the primary one is false
                    	secondary = !result1
                        force = true
                		break
                }                
            }
            
            //broadcast to secondary IF block
            if (secondary) {
                result2 = evaluateConditionSet(evt, false, force)
                state.lastSecondaryEvaluationResult = result2
                state.lastSecondaryEvaluationDate = now()
                def msg = "Secondary IF block evaluation result is $result2"
                if (state.sim) state.sim.evals.push(msg)
                debug msg
            }
            def currentState = state.currentState
            def currentStateSince = state.currentStateSince
            def mode = state.app.mode
            
            def stateMsg = null

            switch (mode) {
                case "Latching":
                    if (currentState in [null, false]) {
                        if (result1) {
                            //flip on
                            state.currentState = true
                            state.currentStateSince = now()
                            stateMsg = "♦ Latching Piston changed state to true ♦"
                        }
                    }
                    if (currentState in [null, true]) {
                        if (result2) {
                            //flip off
                            state.currentState = false
                            state.currentStateSince = now()
                            stateMsg = "♦ Latching Piston changed state to false ♦"
                        }
                    }
                    break
                case "Simple":
                	result2 = !result1
                    if (currentState != result1) {
                        state.currentState = result1
                        state.currentStateSince = now()
                        stateMsg = "♦ Simple Piston changed state to $result1 ♦"
                    }
                    break
                case "And-If":
                	def newState = result1 && result2
                    if (currentState != newState) {
                        state.currentState = newState
                        state.currentStateSince = now()
                        stateMsg = "♦ And-If Piston changed state to $result1 ♦"
                    }
                    break
                case "Or-If":
                	def newState = result1 || result2
                    if (currentState != newState) {
                        state.currentState = newState
                        state.currentStateSince = now()
                        stateMsg = "♦ Or-If Piston changed state to $result1 ♦"
                    }
                    break
                case "Then-If":
                	def newState = result1 && result2
                    if (currentState != newState) {
                        state.currentState = newState
                        state.currentStateSince = now()
                        stateMsg = "♦ Then-If Piston changed state to $result1 ♦"
                    }
                    break
                case "Else-If":
                	def newState = result1 || result2
                    if (currentState != newState) {
                        state.currentState = newState
                        state.currentStateSince = now()
                        stateMsg = "♦ Else-If Piston changed state to $result1 ♦"
                    }
                    break
            }
            if (stateMsg) {
            	if (state.sim) state.sim.evals.push stateMsg
				debug stateMsg, null, "info"
			}
            def stateChanged = false
            if (currentState != state.currentState) {
            	stateChanged = true
            	//we have a state change
	            setVariable("\$previousState", currentState, true)
	            setVariable("\$previousStateSince", currentStateSince, true)
	            setVariable("\$previousStateDuration", state.currentStateSince && currentStateSince ? state.currentStateSince - currentStateSince : null, true)
	            setVariable("\$currentState", state.currentState, true)
	            setVariable("\$currentStateSince", state.currentStateSince, true)
                //new state
                currentState = state.currentState
                //resume all tasks that are waiting for a state change
                cancelTasks(currentState)
                resumeTasks(currentState)
            }
            //execute the DO EVERY TIME actions
            if (result1) scheduleActions(0, stateChanged)
            if (result2) scheduleActions(-1, stateChanged)
            if ((mode != "Latching") && (!currentState)) {
            	//execute the else branch
            	scheduleActions(-2, stateChanged)
            }
        }
	} catch(e) {
    	debug "ERROR: An error occurred while processing event $evt: $e", null, "error"
    }
    perf = now() - perf
    if (evt) debug "Event processing took ${perf}ms", -1, "trace"
}

private checkEventEligibility(condition, evt) {
	//we have a quad-state result
    // -2 means we're using triggers and the event does not match any of the used triggers
    // -1 means we're using conditions only and the event does not match any of the used conditions
    // 1 means we're using conditions only and the event does match at least one of the used conditions
    // 2 means we're using triggers and the event does match at least one of the used triggers
    // any positive value means the event is eligible for evaluation
	def result = -1 //assuming conditions only, no match
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                def v = checkEventEligibility(child, evt)
                switch (v) {
                	case -2:
                    	result = v
                    	break
                    case -1:
                    	break
                    case  1:
                    	if (result == -1) {
                        	result = v
                        }
                    	break
                    case  2:
	                	//if we already found a matching trigger, we're out
    	            	return v
                }
            }
        } else {
        	if (condition.trg) {
            	if (result < 2) {
                	//if we haven't already found a trigger
                	result = -2 // we are using triggers
                }
            }
            for (deviceId in condition.dev) {
                if ((evt.deviceId ? evt.deviceId : "location" == deviceId) && (evt.name == condition.attr)) {
                	if (condition.trg) {
                    	//we found a trigger that matches the event, exit immediately
                    	return 2
                    } else {
                    	if (result == -1) {
                        	//we found a condition that matches the event, still looking for triggers though
                        	result = 1
                        }
                    }
                }
            }
        }
    }
    return result	
}



/******************************************************************************/
/*** CONDITION EVALUATION FUNCTIONS											***/
/******************************************************************************/

private evaluateConditionSet(evt, primary, force = false) {
	//executes whenever a device in the primary or secondary if block has an event
    def perf = now()
    def pushNote = null
    
    //debug "Event received by the ${primary ? "primary" : "secondary"} IF block evaluation for device ${evt.device}, attribute ${evt.name}='${evt.value}', isStateChange=${evt.isStateChange()}, currentValue=${evt.device.currentValue(evt.name)}, determining eligibility"
    //check for triggers - if the primary IF block has triggers and the event is not related to any trigger
    //then we don't want to evaluate anything, as only triggers should be executed
    //this check ensures that an event that is used in both blocks, but as different types, one as a trigger
    //and one as a condition do not interfere with each other
    def app = state.run == "config" ? state.config.app : state.app
    def eligibilityStatus = force || !!(state.sim) ? 1 : checkEventEligibility(primary ? app.conditions: app.otherConditions , evt)
    def evaluation = null
    if (!force) {
    	debug "Event eligibility for the ${primary ? "primary" : "secondary"} IF block is $eligibilityStatus  - ${eligibilityStatus > 0 ? "ELIGIBLE" : "INELIGIBLE"} (" + (eligibilityStatus == 2 ? "triggers required, event is a trigger" : (eligibilityStatus == 1 ? "triggers not required, event is a condition" : (eligibilityStatus == -2 ? "triggers required, but event is a condition" : "something is messed up"))) + ")"
    }
    if (eligibilityStatus > 0) {
        evaluation = evaluateCondition(primary ? app.conditions: app.otherConditions, evt)
        //log.info "${primary ? "PRIMARY" : "SECONDARY"} EVALUATION IS $evaluation\n${getConditionDescription(primary ? 0 : -1)}\n"
        //pushNote = "${evt.device}.${evt.name} >>> ${evt.value}\n${primary ? "primary" : "secondary"} evaluation result: $evaluation\n\n${getConditionDescription(primary ? 0 : -1)}\n\nEvent received after ${perf - evt.date.getTime()}ms\n"
    } else {
    	//ignore the event
    }
    perf = now() - perf
	//if (pushNote) {
    	//sendPush(pushNote + "Event processed in ${perf}ms")
    //}
    return evaluation
}

private evaluateCondition(condition, evt = null) {
	try {
        //evaluates a condition
        def perf = now()  
        def result = false

        if (condition.children == null) {
            //we evaluate a real condition here
            //several types of conditions, device, mode, SMH, time, etc.
            if (condition.attr == "time") {
                result = evaluateTimeCondition(condition, evt)
            } else {
                result = evaluateDeviceCondition(condition, evt)
            }       
        } else {
            //we evaluate a group
            result = (condition.grp == "AND") && (condition.children.size()) //we need to start with a true when doing AND or with a false when doing OR/XOR
            for (child in condition.children.sort { it.id }) {
                //evaluate the child
                def subResult = evaluateCondition(child, evt)
                //apply it to the composite result
                switch (condition.grp) {
                    case "AND":
                    result = result && subResult
                    break
                    case "OR":
                    result = result || subResult
                    break
                    case "XOR":
                    result = result ^ subResult
                    break
                }
            }
        }

        //apply the NOT, if needed
        result = condition.not ? !result : result

        //store variables (only if evt is available, i.e. not simulating)
        if (evt) {
            if (condition.vd) setVariable(condition.vd, now())
            if (condition.vs) setVariable(condition.vs, result)
            if (condition.vt && result) setVariable(condition.vt, evt.date.getTime())
            if (condition.vv && result) setVariable(condition.vv, evt.value)
            if (condition.vf && !result) setVariable(condition.vf, evt.date.getTime())        
            if (condition.vw && !result) setVariable(condition.vw, evt.value)

            if (result) {
                scheduleActions(condition.id)
            }
        }        

        perf = now() - perf
        return result
    } catch(e) {
    	debug "ERROR: Error evaluating condition: $e", null, "error"
    }
    return false
}

private evaluateDeviceCondition(condition, evt) {
	//evaluates a condition   
    //we need true when dealing with All
    def mode = condition.mode == "All" ? "All" : "Any"
    def result =  mode == "All" ? true : false
    def currentValue = null
    
    //get list of devices
    def devices = settings["condDevices${condition.id}"]
    def eventDeviceId = evt ? evt.deviceId : null
    def virtualCurrentValue = null
    switch (condition.cap) {
    	case "Mode":
    	case "Location Mode":
        	devices = [location]
            virtualCurrentValue = location.mode
            eventDeviceId = location.id
            break
        case "Smart Home Monitor":
        	devices = [location]
            virtualCurrentValue = getAlarmSystemStatus()
            eventDeviceId = location.id
        	break    	
        case "Routine":
        	devices = [location]
            virtualCurrentValue = evt ? evt.displayName : "<<<unknown routine>>>"
            eventDeviceId = location.id
        	break    	
        case "Variable":
        	devices = [location]
            virtualCurrentValue = getVariable(condition.var)
            eventDeviceId = location.id
        	break    	
    }
    
    if (!devices) {
        //something went wrong
        return false    	
    }
    
	def attr = getAttributeByName(condition.attr)
    //get capability if the attribute suggests one
    def capability = attr && attr.capability ? getCapabilityByName(attr.capability) : null
    
    def hasSubDevices = false
    def matchesSubDevice = false
    if (evt && evt.jsonData && capability && capability.count && capability.data) {
        //at this point we won't evaluate this condition unless we have the right sub device below
        hasSubDevices = true
        setVariable("\$currentEventDeviceIndex", cast(evt.jsonData[capability.data], "number"), true)
        def subDeviceId = "#${evt.jsonData[capability.data]}".trim()
        def subDevices = condition.sdev ? condition.sdev : []
        if (subDevices && subDevices.size()) {
            //are we expecting that button?
            //subDeviceId in subDevices didn't seem to work?!
            for(subDevice in subDevices) {
                if (subDevice == subDeviceId) {
                    matchesSubDevice = true
                    break
                }
            }
        }
    }
    
    //is this a momentary event?
    def momentary = attr ? !!attr.momentary : false
    //if we're dealing with a momentary capability, we can only expect one of the devices to be true at any time
    if (momentary) {
    	mode = "Any"
    }
    
    //matching devices list
    def vm = []
    //non-matching devices list
    def vn = []
    //the real deal goes here
    for (device in devices) {
        def comp = getComparisonOption(condition.attr, condition.comp, (condition.attr == "variable" ? condition.dt : null))
        if (comp) {
            //if event is about the same device/attribute, use the event's value as the current value, otherwise, fetch the current value from the device
            def deviceResult = false
            def ownsEvent = evt && (eventDeviceId == device.id) && (evt.name == condition.attr)
            def oldValue = null
            def oldValueSince = null
            if (evt) {
                def cache = atomicState.cache
                cache = cache ? cache : [:]
                def cachedValue = cache[device.id + "-" + condition.attr]
                if (cachedValue) {
                    oldValue = cachedValue.o
                    oldValueSince = cachedValue.t
                }
            }
			def type = attr.name == "variable" ? (condition.dt ? condition.dt : attr.type) : attr.type
            //if we're dealing with an owned event, use that event's value
            //if we're dealing with a virtual device, get the virtual value
            currentValue = cast(evt && ownsEvent ? evt.value : (virtualCurrentValue ? virtualCurrentValue : device.currentValue(condition.attr)), type)
			def value1
            def offset1
			def value2
            def offset2
            
			if (comp.parameters > 0) {
            	value1 = cast(condition.var1 ? getVariable(condition.var1) : (condition.dev1 && settings["condDev${condition.id}#1"] ? settings["condDev${condition.id}#1"].currentValue(condition.attr1 ? condition.attr1 : condition.attr) : condition.val1), type)
            	offset1 = cast(condition.var1 || condition.dev1 ? condition.o1 : 0, type)
                if (comp.parameters > 1) {
                    value2 = cast(condition.var2 ? getVariable(condition.var2) : (condition.dev2 && settings["condDev${condition.id}#2"] ? settings["condDev${condition.id}#2"].currentValue(condition.attr2 ? condition.attr2 : condition.attr) : condition.val2), type)
                    offset2 = cast(condition.var1 || condition.dev1 ? condition.o2 : 0, type)
                }
            }
            
            switch (type) {
            	case "number":
                case "decimal":
                    if (comp.parameters > 0) {
                        value1 += cast(condition.var1 || condition.dev1 ? condition.o1 : 0, type)
                        if (comp.parameters > 1) {
                            value2 += cast(condition.var1 || condition.dev1 ? condition.o2 : 0, type)
                        }
                    }
                    break
            }
            /*
            //casting
            if (attr) {
                switch (attr.type) {
                    case "number":
                    if (oldValue instanceof String) oldValue = oldValue.isInteger() ? oldValue.toInteger() : 0
                    if (currentValue instanceof String) currentValue = currentValue.isInteger() ? currentValue.toInteger() : 0
                    if (value1 instanceof String) value1 = value1.isInteger() ? value1.toInteger() : 0
                    if (value2 instanceof String) value2 = value2.isInteger() ? value2.toInteger() : 0
                    value1 = o1 ? value1 + o1 : value1
                    value2 = o2 ? value2 + o2 : value2
                    break
                    case "decimal":
                    if (oldValue instanceof String) oldValue = oldValue.isFloat() ? oldValue.isFloat() : 0
                    if (currentValue instanceof String) currentValue = currentValue.isFloat() ? currentValue.toFloat() : 0
                    if (value1 instanceof String) value1 = value1.isFloat() ? value1.toFloat() : 0
                    if (value2 instanceof String) value2 = value2.isFloat() ? value1.toFloat() : 0
                    value1 = o1 ? value1 + o1 : value1
                    value2 = o2 ? value2 + o2 : value2
                    break
                }
            }
            */
            if (condition.trg && !ownsEvent) {
                //all triggers should own the event, otherwise be false
                deviceResult = false
            } else {          
                def function = "eval_" + (condition.trg ? "trg" : "cond") + "_" + condition.comp.replace(" ", "_")
                //if we have a momentary capability and the event is not owned, there's no need to evaluate the function
                //also, if there are subdevices and the one we're looking for does not match, no need to evaluate the function either
                if ((momentary && !ownsEvent) || (hasSubDevices && !matchesSubDevice)) {
                    deviceResult = false
                    def msg = "${deviceResult ? "♣" : "♠"} Evaluation for ${momentary ? "momentary " : ""}$device's ${condition.attr} [$currentValue] ${condition.comp} '$value1${comp.parameters == 2 ? " - $value2" : ""}' returned $deviceResult"
                    if (state.sim) state.sim.evals.push(msg)
					debug msg
                } else {
                    deviceResult = "$function"(condition, device, condition.attr, oldValue, oldValueSince, currentValue, value1, value2, ownsEvent ? evt : null, evt, momentary)
                    def msg = "${deviceResult ? "♣" : "♠"} Function $function for $device's ${condition.attr} [$currentValue] ${condition.comp} '$value1${comp.parameters == 2 ? " - $value2" : ""}' returned $deviceResult"
                    if (state.sim) state.sim.evals.push(msg)
                    debug msg
                }

                if (deviceResult) {
                    if (condition.vm) vm.push "$device"
                } else {
                    if (condition.vn) vn.push "$device"
                }
            }

            //compound the result, depending on mode
            def finalResult = false
            switch (mode) {
                case "All":
                result = result && deviceResult
                finalResult = !result
                break
                case "Any":
                result = result || deviceResult
                finalResult = result
                break
            }
            //optimize the loop to exit when we find a result that's going to be the final one (AND encountered a false, or OR encountered a true)
            if (finalResult && !condition.vm && !condition.vn) break
        }
    }

	if (evt) {
    	if (condition.vm) setVariable(condition.vm, buildNameList(vm, "and"))
    	if (condition.vn) setVariable(condition.vn, buildNameList(vn, "and"))
    }
    return  result
}

private evaluateTimeCondition(condition, evt = null, unixTime = null, getNextEventTime = false) {
    //we sometimes optimize this and sent the comparison text and object
    //no condition? not time condition? false!
    if (!condition || (condition.attr != "time")) {
        return false
    }
    //get UTC now if no unixTime is provided
    unixTime = unixTime ? unixTime : now()
    //convert that to location's timezone, for comparison
    def attr = getAttributeByName(condition.attr)
    def comparison = cleanUpComparison(condition.comp)
    def comp = getComparisonOption(condition.attr, comparison)    
    //if we can't find the attribute (can't be...) or the comparison object, or we're dealing with a trigger, exit stage false
    if (!attr || !comp) {
        return false
    }

    if (comp.trigger == comparison) {
    	if (evt) {
            //trigger
            if (evt && (evt.deviceId == "time") && (evt.conditionId == condition.id)) {
                condition.lt = evt.date.time
                //we have a time event returning as a result of a trigger, assume true
                return true
            } else {


                if (comparison.contains("stay")) {
                    //we have a stay condition
                }
            }
        }
        return false
    }

	def time = adjustTime(unixTime)

	//check comparison
    def result = true
    if (comparison.contains("any")) {
    	//we match any time
    } else {
        //convert times to number of minutes since midnight for easy comparison
        def m = time ? time.hours * 60 + time.minutes : 0
        def m1 = null
        def m2 = null
       	//go through each parameter
        def o1 = condition.o1 ? condition.o1 : 0
        def o2 = condition.o2 ? condition.o2 : 0
        for (def i = 1; i <= comp.parameters; i++) {
        	def val = i == 1 ? condition.val1 : condition.val2
            def t = null
            def v = 0
            switch (val) {
            	case "custom time":
                	t = (i == 1 ? (condition.t1 ? adjustTime(condition.t1) : null) : (condition.t2 ? adjustTime(condition.t2) : null))
                    v = t ? t.getHours() * 60 + t.getMinutes() : null
                    if (!comparison.contains("around")) {
                        switch (i) {
                        	case 1:
                            	o1 = 0
                                break
                            case 2:
                            	o2 = 0
                                break
                        }
                    }
                   	break
				case "midnight":
                	v = (i == 1 ? 0 : 1440)
                    break
                case "sunrise":
                	t = getSunrise()
                    v = t ? t.hours * 60 + t.minutes : null
                	break
				case "noon":
                	v = 12 * 60 //noon is 720 minutes away from midnight
                    break
                case "sunset":
                	t = getSunset()
                    v = t ? t.hours * 60 + t.minutes : null
                	break
            }
            if (i == 1) {
            	m1 = v
            } else {
            	m2 = v
            }
        }
        
        def rightNow = time.time
        def lastMidnight =  rightNow - rightNow.mod(86400000)
        def nextMidnight =  lastMidnight + 86400000
        
        //we need to ensure we have a full condition
        if (getNextEventTime) {
        	if ((m1 == null) || ((comp.parameters == 2) && (m2 == null))) {
            	return null
            }
        }
        
        switch (comparison) {
        	case { comparison.contains("before") }:
                if ((m1 == null) || (m >= addOffsetToMinutes(m1, o1))) {
                    //m before m1?
                    result = false
                }
                if (getNextEventTime) {
                	if (result) {
                    	//we're looking for the next time when time is not before given amount, that's exactly the time we're looking at
                        return convertDateToUnixTime(lastMidnight + addOffsetToMinutes(m1, o1) * 60000)
                    } else {
                    	//the next time time is before a certain time is... next midnight...
                        return convertDateToUnixTime(nextMidnight)
                    }
                }
                if (!result) return false
                break
        	case { comparison.contains("after") }:
        		if ((m1 == null) || (m < addOffsetToMinutes(m1, o1))) {
        			//m after m1?
	        		result = false
	            }
                if (getNextEventTime) {
                	if (result) {
                    	//we're looking for the next time when time is not after given amount, next midnight
                        return convertDateToUnixTime(nextMidnight)
                    } else {
                    	//the next time time is before a certain time is... next midnight...
                        return convertDateToUnixTime(lastMidnight + addOffsetToMinutes(m1, o1) * 60000)
                    }
                }
                if (!result) return result               
                break
            case { comparison.contains("around") }:
                //if no offset, we can't really match anything
                def a1 = addOffsetToMinutes(m1, -o1)
                def a2 = addOffsetToMinutes(m1, +o1)
                if (a1 < a2 ? (m < a1) || (m >= a2) : (m >= a2) && (m < a1)) {                
                    result = false
                }
                if (getNextEventTime) {
                	if (result) {
                    	//we're in between the +/- time, the a2 is the next time we are looking for
                        return convertDateToUnixTime(lastMidnight + a2 * 60000)
                    } else {
                    	//return a1 time either today or tomorrow
                    	return convertDateToUnixTime((a1 < m ? nextMidnight : lastMidnight) + a1 * 60000)
                    }
                }
                if (!result) return result               
                break
            case { comparison.contains("between") }:
                def a1 = addOffsetToMinutes(m1, o1)
                def a2 = addOffsetToMinutes(m2, o2)
                def eval = (a1 < a2 ? (m < a1) || (m >= a2) : (m >= a2) && (m < a1))
                if (getNextEventTime) {
                	if (!eval) {
                    	//we're in between the a1 and a2
                        if (a1 < a2) {
                        	//normal range, a2 is our time
                            return convertDateToUnixTime(lastMidnight + a2 * 60000)
                        } else {
                        	//reverse range, we exit the interval at every a2, today or tomorrow
                            return convertDateToUnixTime((a2 < m ? nextMidnight : lastMidnight) + a2 * 60000)
                        }
                    } else {
                    	//we're not in between the a1 and a2
                        if (a1 < a2) {
                        	//normal range, a1 is our time, either today or tomorrow
	                    	return convertDateToUnixTime((a1 < m ? nextMidnight : lastMidnight) + a1 * 60000)
                        } else {
                        	//reverse range, a1 is our time
                            return convertDateToUnixTime(lastMidnight + a1 * 60000)
                        }
                    }
                }                
                if (comparison.contains("not")) {
                    eval = !eval
                }
                if (eval) {
                    result = false
                }
                if (!result) return result
                break
        }
    }

	return result && testDateTimeFilters(condition, time)
}

private testDateTimeFilters(condition, now) {
	//if we made it this far, let's check on filters
    if (condition.fmh || condition.fhd || condition.fdw || condition.fdm || condition.fwm || condition.fmy || condition.fy) {
    	//check minute filter
        if (condition.fmh) {
        	def m = now.minutes.toString().padLeft(2, "0")
            if (!(m in condition.fmh)) {
            	return false
            }
		}

		//check hour filter
        if (condition.fhd) {
        	def h = formatHour(now.hours)
            if (!(h in condition.fhd)) {
            	return false
            }
		}
        
        if (condition.fdw) {
            def dow = getDayOfWeekName(now)
            if (!(dow in condition.fdw)) {
            	return false
            }
		}
        
        if (condition.fwm) {
        	def weekNo = "the ${formatOrdinalNumberName(getWeekOfMonth(now))} week"
            def lastWeekNo = "the ${formatOrdinalNumberName(getWeekOfMonth(now, reverse))} week"
            if (!((weekNo in condition.fwm) || (lastWeekNo in condition.fwm))) {
            	return false
            }
		}
		if (condition.fdm) {
        	def dayNo = "the " + formatOrdinalNumber(getDayOfMonth(now))
            def lastDayNo = "the " + formatOrdinalNumberName(getDayOfMonth(now, true)) + " day of the month"
            if (!((dayNo in condition.fdm) || (lastDayNo in condition.fdm))) {
            	return false
            }
		}

		if (condition.fmy) {
            if (!(getMonthName(now) in condition.fmy)) {
            	return false
            }
		}
        
        if (condition.fy) {
        	def year = now.year + 1900
            def yearOddEven = year.mod(2)
            def odd = "odd years" in condition.fy
            def even = "even years" in condition.fy
            def leap = "leap years" in condition.fy
            if (!(((yearOddEven == 0) && even) || ((yearOddEven == 1) && odd) || ((year.mod(4) == 0) && leap) || ("$year" in condition.fy))) {
            	return false
            }
        }
    }
    return true
}

/* low-level evaluation functions */
private eval_cond_is(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return eval_cond_is_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_is_not(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return eval_cond_is_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_is_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def v = "$currentValue".trim()
    for(def value in value1) {
        if ("$value".trim() == v)
        return true
    }
    return false
}

private eval_cond_is_not_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_is_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue == value1
}

private eval_cond_is_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue != value1
}

private eval_cond_is_less_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue < value1
}

private eval_cond_is_less_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue <= value1
}

private eval_cond_is_greater_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue > value1
}

private eval_cond_is_greater_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return currentValue >= value1
}

private eval_cond_is_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	try {
   		return Math.round(currentValue).mod(2) == 0
    } catch(all) {}
    return false
}

private eval_cond_is_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	try {
   		return Math.round(currentValue).mod(2) == 1
    } catch(all) {}
    return false
}

private eval_cond_is_inside_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	if (value1 < value2) {
		return (currentValue >= value1) && (currentValue <= value2)
    } else {
		return (currentValue >= value2) && (currentValue <= value1)
    }
}

private eval_cond_is_outside_of_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	if (value1 < value2) {
		return (currentValue < value1) || (currentValue > value2)
	} else {
		return (currentValue < value2) || (currentValue > value1)
    }
}

private listPreviousStates(device, attribute, currentValue, minutes, excludeLast) {
//	def events = device.eventsSince(new Date(now() - minutes * 60000));
    def result = []
	if (!(device instanceof physicalgraph.app.DeviceWrapper)) return result
	def events = device.events([all: true, max: 100]).findAll{it.name == attribute}
    //if we got any events, let's go through them       
	//if we need to exclude last event, we start at the second event, as the first one is the event that triggered this function. The attribute's value has to be different from the current one to qualify for quiet
    def value = currentValue
    def thresholdTime = now() - minutes * 60000
    def endTime = now()
    for(def i = 0; i < events.size(); i++) {
    	def startTime = events[i].date.getTime()
    	def duration = endTime - startTime
        if ((duration >= 1000) && ((i > 0) || !excludeLast)) {
	        result.push([value: events[i].value, startTime: startTime, duration: duration])
        }
        if (startTime < thresholdTime)
	        break
        endTime = startTime
    }
    return result
}

private eval_cond_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	def minutes = timeToMinutes(condition.fort)
	def events = device.eventsSince(new Date(now() - minutes * 60000)).findAll{it.name == attribute}
    return (events.size() > 0)
}

private eval_cond_did_not_change(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_was(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return eval_cond_was_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_was_not(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	eval_cond_was_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_cond_was_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value == value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value != value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_less_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value < value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_less_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value <= value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_greater_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value > value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_greater_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value >= value1) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value.isInteger() ? state.value.toInteger().mod(2) == 0 : false) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (state.value.isInteger() ? state.value.toInteger().mod(2) == 1 : false) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_inside_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (value1 < value2 ? (state.value >= value1) && (state.value <= value2) : (state.value >= value2) && (state.value <= value1)) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_outside_of_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
    def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
    def thresholdTime = time * 60000
    def stableTime = 0
    for (state in states) {
    	if (value1 < value2 ? (state.value < value1) || (state.value > value2) : (state.value < value2) || (state.value > value1)) {
        	stableTime += state.duration
        } else {
        	break
        }
    }
    return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

/* triggers */
private eval_trg_changes(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return momentary || (oldValue != currentValue)
}

private eval_trg_changes_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return (momentary || !eval_cond_is_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary)) &&
    		eval_cond_is_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_changes_to_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return (momentary || !eval_cond_is_one_of(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary)) &&
    		eval_cond_is_one_of(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_changes_away_from(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return (momentary || !eval_cond_is_not_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary)) &&
    		eval_cond_is_not_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_changes_away_from_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return (momentary || !eval_cond_is_not_one_of(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary)) &&
    		eval_cond_is_not_one_of(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_drops_below(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_less_than(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_less_than(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_drops_to_or_below(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_less_than_or_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_less_than_or_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_raises_above(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_greater_than(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_greater_than(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_raises_to_or_above(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_greater_than_or_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_greater_than_or_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_changes_to_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_even(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_even(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_changes_to_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_odd(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_odd(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_enters_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_inside_range(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_inside_range(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_exits_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return !eval_cond_is_outside_of_range(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary) &&
    		eval_cond_is_outside_of_range(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary)
}

private eval_trg_executed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	return (evt && evt.displayName && evt.displayName == value1)
}

/*
private eval_trg_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	if (!oldValueSince) return false
    def time = timeToMinutes(condition.fort)
    def thresholdTime = time * 60000
	def stableTime = now() - oldValueSince
    return (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_trg_did_not_change(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary) {
	if (!oldValueSince) return false
	return !eval_trg_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary)
}
*/



/******************************************************************************/
/*** SCHEDULER FUNCTIONS - TIMING BELT										***/
/******************************************************************************/

private scheduleTimeTriggers() {
	debug "Rescheduling time triggers", null, "trace"
    //remove all pending events
    unscheduleTask("evt", null, null)
    def app = state.run == "config" ? state.config.app : state.app
    if (getTriggerCount(app) > 0) {
        withEachTrigger(app.conditions, "scheduleTimeTrigger")
        if (app.mode in ["Latching", "And-If", "Or-If"]) {
            withEachTrigger(app.otherConditions, "scheduleTimeTrigger")
        }
    } else {
    	//we're not using triggers, let's mess up with time conditions
        withEachCondition(app.conditions, "scheduleTimeTrigger")
        if (app.mode in ["Latching", "And-If", "Or-If"]) {
            withEachCondition(app.otherConditions, "scheduleTimeTrigger")
        }
    }
}

private scheduleTimeTrigger(condition) {
	if (!condition || !(condition.attr) || (condition.attr != "time")) {
    	return
    }
    def time = condition.trg ? getNextTimeTriggerTime(condition, condition.lt) : getNextTimeConditionTime(condition, condition.lt)
    condition.nt = time
    scheduleTask("evt", condition.id, null, null, time)
}

private scheduleActions(conditionId, stateChanged = false) {
	debug "Scheduling actions for condition #${conditionId}. State did${stateChanged ? "" : " NOT"} change."
	def actions = listActions(conditionId).sort{ it.id }
    for (action in actions) {
    	//restrict on state changed
    	if (action.rc && !stateChanged) continue
    	if (action.rm && action.rm.size() && !(location.mode in action.rm)) continue
    	if (action.ra && action.ra.size() && !(getAlarmSystemStatus() in action.ra)) continue
		//we survived all restrictions, pfew
		scheduleAction(action)
    }
}

private scheduleAction(action) {
	if (!action) return null
    def deviceIds = action.l ? ["location"] : (action.d ? action.d : [])
    def tos = action.tos ? action.tos : "Action"
    if (tos != "None") {
    	def aid = (tos == "Action") ? action.id : null
        unscheduleTask("cmd", action.id, null)
        for (deviceId in deviceIds) {
            //remove all tasks for all involved devices
            unscheduleTask("cmd", aid, deviceId)
        }
        if (tos == "Global") {
        	debug "WARNING: Task override policy for Global is not yet implemented", null, "warn"
        }
    }
    def time = now()
    def waitFor = null
    def waitSince = null
    if (action.t && action.t.size() && deviceIds.size() ) {
    	for (task in action.t.sort{ it.i }) {
        	def cmd = task.c
            def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
            def custom = (cmd && cmd.startsWith(customCommandPrefix()))
            cmd = cleanUpCommand(cmd)
            def command = null
            if (virtual) {
                //dealing with a virtual command
                command = getVirtualCommandByDisplay(cmd)
                if (command && command.immediate) {
               		def function = "cmd_${command.name}".replace(" ", "_").replace("(", "_").replace(")", "_").replace("&", "_")
					def result = "$function"(action, task, time)
                	time = (result && result.time) ? result.time : time
                    if (result && result.waitFor) {
                    	waitFor = result.waitFor
                        waitSince = time
                    }
                    command = null
                }
            } else {
                command = getCommandByDisplay(cmd)
            }
            if (command) {
                for (deviceId in deviceIds) {
                    def data = task.p && task.p.size() ? [p: task.p] : null
                    if (waitFor) {
                        data = data ? data : [:]
                        data.w = waitFor //what to wait for
                        data.o = time - waitSince //delay after state change
                    }
                    if (action.tcp && action.tcp != "None") {
                        data = data ? data : [:]
                        data.c = true
                    }
					if (command.aggregated) {
                    	//an aggregated command schedules one command task for the whole group
                    	deviceId = null
					}
                    scheduleTask("cmd", action.id, deviceId, task.i, time, data)
                    //an aggregated command schedules one command task for the whole group, so there's only one scheduled task, exit
                    if (command.aggregated) break
                }
            }           
        }
    }
}

private cmd_wait(action, task, time) {
	def result = [:]
    if (task && task.p && task.p.size() == 2) {
        def unit = 60000
        switch (task.p[1].d) {
        	case "seconds":
            	unit = 1000
                break
        	case "minutes":
            	unit = 60000
                break
        	case "hours":
            	unit = 3600000
                break
        }
       	def offset = task.p[0].d * unit
       	result.time = time + offset
    }
    return result
}

private cmd_waitRandom(action, task, time) {
	def result = [:]
    if (task && task.p && task.p.size() == 3) {
        def unit = 60000
        switch (task.p[2].d) {
        	case "seconds":
            	unit = 1000
                break
        	case "minutes":
            	unit = 60000
                break
        	case "hours":
            	unit = 3600000
                break
        }
    	def min = task.p[0].d * unit
        def max = task.p[1].d * unit
        if (min > max) {
        	//swap the numbers
        	def x = min
            min = max
            max = x
        }
       	def offset = (long)(min + Math.round(Math.random() * (max - min)))
       	result.time = time + offset
    }
    return result
}

private cmd_waitState(action, task, time) {
	def result = [:]
    if (task && task.p && task.p.size() == 1) {
        def state = "${task.p[0].d}"
        if (state.contains("any")) {
        	result.waitFor = "a"
        }
        if (state.contains("true")) {
        	result.waitFor = "t"
        }
        if (state.contains("false")) {
        	result.waitFor = "f"
        }
    }
    return result
}


private scheduleTask(task, ownerId, deviceId, taskId, unixTime, data = null) {
	if (!unixTime) return false
	if (!state.tasker) {
    	state.tasker = []
        state.taskerIdx = 0
    }
    //get next index for task ordering
    def idx = state.taskerIdx
    state.taskerIdx = idx + 1
    state.tasker.push([idx: idx, add: task, ownerId: ownerId, deviceId: deviceId, taskId: taskId, data: data, time: unixTime, created: now()])
    return true
}

private unscheduleTask(task, ownerId, deviceId) {
	if (!state.tasker) {
    	state.tasker = []
        state.taskerIdx = 0
    }
    def idx = state.taskerIdx
    state.taskerIdx = idx + 1
	state.tasker.push([idx: idx, del: task, ownerId: ownerId, deviceId: deviceId, created: now()])
}

private getNextTimeConditionTime(condition, startTime = null) {
def perf = now()

	//no condition? not time condition? false!
	if (!condition || (condition.attr != "time")) {
    	return null
    }
	//get UTC now if no unixTime is provided
	def unixTime = startTime ? startTime : now()
    //remove the seconds...
    unixTime = unixTime - unixTime.mod(60000)
    //we give it up to 25 hours to find the next time when the condition state would change
    //optimized procedure - limitations : this will only trigger on strict condition times, without actually accounting for time restrictions...
    return evaluateTimeCondition(condition, null, unixTime, true)
}

private getNextTimeTriggerTime(condition, startTime = null) {
	//no condition? not time condition? false!
	if (!condition || (condition.attr != "time")) {
    	return null
    }
	//get UTC now if no unixTime is provided
	def unixTime = startTime ? startTime : now()
    //convert that to location's timezone, for comparison
	def now = adjustTime(unixTime)
	def attr = getAttributeByName(condition.attr)
    def comparison = cleanUpComparison(condition.comp)
    def comp = getComparisonOption(condition.attr, comparison)    
    //if we can't find the attribute (can't be...) or the comparison object, or we're not dealing with a trigger, exit stage null
    if (!attr || !comp || comp.trigger != comparison) {
    	return null
    }
    
    def repeat = (condition.val1 && condition.val1.contains("every") ? condition.val1 : condition.r)
    if (!repeat) {
    	return null
	}
    def interval = (repeat.contains("number") ? (condition.val1 && condition.val1.contains("every") ? condition.e : condition.re) : 1)
    if (!interval) {
    	return null
	}
    repeat = repeat.replace("every ", "").replace("number of ", "").replace("s", "")
	//do the work
    def maxCycles = null
	while ((maxCycles == null) || (maxCycles > 0)) {
        def cycles = null
	    def repeatCycle = false
        if (repeat == "minute") {
            //increment minutes
            now = new Date(now.time + interval * 60000)
            cycles = 1500 //up to 25 hours
        } else if (repeat == "hour") {
            //increment hours
            def m = now.minutes
            def rm = (condition.m ? condition.m : "0").toInteger()
            now = new Date(now.time + (m < rm ? interval - 1 : interval) * 3600000)
            now = new Date(now.year, now.month, now.date, now.hours, rm, 0)
            cycles = 744
        } else {
            //we're repeating at a granularity larger or equal to a day
            //we need the time of the day at which things happen
            def h = 0
            def m = 0
            def offset = 0
            def customTime = null
            switch (condition.val1) {
                case "custom time":
                    if (!condition.t1) {
                        return null
                    }
                    customTime = adjustTime(condition.t1)
                    break
                case "sunrise":
                    customTime = getSunrise()
                    offset = condition.o1 ? condition.o1 : 0
                    break
                case "sunset":
                    customTime = getSunset()
                    offset = condition.o1 ? condition.o1 : 0
                    break
                case "noon":
                    h = 12
                    offset = condition.o1 ? condition.o1 : 0
                    break
                case "midnight":
                    offset = condition.o1 ? condition.o1 : 0
                    break
            }
            
            if (customTime) {
                h = customTime.hours
                m = customTime.minutes
            }
            //we now have the time of the day
            //let's figure out the next day

            //we need a - one day offset if now is before the required time
            //since today could still be a candidate
            now = (now.hours * 60 - h * 60 + now.minutes - m - offset < 0) ? now - 1 : now
            now = new Date(now.year, now.month, now.date, h, m, 0)
            
            //apply the offset
            if (offset) {
            	now = new Date(now.time + offset * 60000)
            }

            switch (repeat) {
                case "day":
                    now = now + interval
                    cycles = 1095
                    break
                case "week":
                    def dow = now.day
                    def rdow = getDayOfWeekNumber(condition.rdw)
                    if (rdow == null) {
                        return null
                    }
                    now = now + (rdow <= dow ? rdow + 7 - dow : rdow - dow) + (interval - 1) * 7            	
                    cycles = 520
                    break
                case "month":
                	def day = condition.rd
                    if (!day) {
                    	return null
                    }
                    if (day.contains("week")) {
                        def rdow = getDayOfWeekNumber(condition.rdw)
                        if (rdow == null) {
                            return null
                        }
                    	//we're using Nth week day of month
                        def week = 1
						if (day.contains("first")) {
                        	week = 1
                        } else if (day.contains("second")) {
                        	week = 2
                        } else if (day.contains("third")) {
                        	week = 3
                        } else if (day.contains("fourth")) {
                        	week = 4
                        } else if (day.contains("fifth")) {
                        	week = 5
                        }
                        if (day.contains("last")) {
                            week = -week
                        }
                        def intervalOffset = 0
                        def d = getDayInWeekOfMonth(now, week, rdow)
                        //get a possible date this month
                        if (d && (new Date(now.year, now.month, d, now.hours, now.minutes, 0) > now)) {
                            //at this point, the next month is this month (lol), we need to remove one from the interval
                            intervalOffset = 1
                        }

                        //get the day of the next required month
                        d = getDayInWeekOfMonth(new Date(now.year, now.month + interval - intervalOffset, 1, now.hours, now.minutes, 0), week, rdow)
                        if (d) {
	                        now = new Date(now.year, now.month + interval - intervalOffset, d, now.hours, now.minutes, 0)
                        } else {
                        	now = new Date(now.year, now.month + interval - intervalOffset, 1, now.hours, now.minutes, 0)
                            repeatCycle = true
                        }
                    } else {
                    	//we're specifying a day
	                    def d = 1
                        if (day.contains("last")) {
                        	//going backwards
                            if (day.contains("third")) {
                            	d = -2
                            } else if (day.contains("third")) {
                            	d = -1
                            } else {
                            	d = 0
                            }
                        	def intervalOffset = 0                            
                            //get the last day of this month
                            def dd = (new Date(now.year, now.month + 1, d)).date
                            if (new Date(now.year, now.month, dd, now.hours, now.minutes, 0) > now) {
                                //at this point, the next month is this month (lol), we need to remove one from the interval
                                intervalOffset = 1
                            }
                            //get the day of the next required month
                            d = (new Date(now.year, now.month + interval - intervalOffset + 1, d)).date
                            now = new Date(now.year, now.month + interval - intervalOffset, d, now.hours, now.minutes, 0)
                        } else {
                        	//the day is in the string
                        	day = day.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "")
                            if (!day.isInteger()) {
                            	//error
                            	return null
                            }                            
                            d = day.toInteger()
							now = new Date(now.year, now.month + interval - (d > now.date ? 1 : 0), d, now.hours, now.minutes, 0)
							if (d > now.date) {
                            	//we went overboard, this month does not have so many days, repeat the cycle to move on to the next month that does
                                repeatCycle = true
                            }
                        }
                    }
                    cycles = 36
                    break
                case "year":
                	def day = condition.rd
                    if (!day) {
                    	return null
                    }
                    if (!condition.rm) {
                        return null
                    }
                    def mo = getMonthNumber(condition.rm)
                    if (mo == null) {
                    	return null
                    }
                    mo--                   
                    if (day.contains("week")) {
                        def rdow = getDayOfWeekNumber(condition.rdw)
                        if (rdow == null) {
                            return null
                        }
                    	//we're using Nth week day of month
                        def week = 1
						if (day.contains("first")) {
                        	week = 1
                        } else if (day.contains("second")) {
                        	week = 2
                        } else if (day.contains("third")) {
                        	week = 3
                        } else if (day.contains("fourth")) {
                        	week = 4
                        } else if (day.contains("fifth")) {
                        	week = 5
                        }
                        if (day.contains("last")) {
                            week = -week
                        }
                        def intervalOffset = 0
                        def d = getDayInWeekOfMonth(new Date(now.year, mo, now.date, now.hours, now.minutes, 0), week, rdow)
                        //get a possible date this year
                        if (d && (new Date(now.year, mo, d, now.hours, now.minutes, 0) > now)) {
                            //at this point, the next month is this month (lol), we need to remove one from the interval
                            intervalOffset = 1
                        }

                        //get the day of the next required month
                        d = getDayInWeekOfMonth(new Date(now.year + interval - intervalOffset, mo, 1, now.hours, now.minutes, 0), week, rdow)
                        if (d) {
	                        now = new Date(now.year + interval - intervalOffset, mo, d, now.hours, now.minutes, 0)
                        } else {
                        	now = new Date(now.year + interval - intervalOffset, mo, 1, now.hours, now.minutes, 0)
                            repeatCycle = true
                        }
                    } else {
                    	//we're specifying a day
	                    def d = 1
                        if (day.contains("last")) {
                        	//going backwards
                            if (day.contains("third")) {
                            	d = -2
                            } else if (day.contains("third")) {
                            	d = -1
                            } else {
                            	d = 0
                            }
                        	def intervalOffset = 0                            
                            //get the last day of specified month
                            def dd = (new Date(now.year, mo + 1, d)).date
                            if (new Date(now.year, mo, dd, now.hours, now.minutes, 0) > now) {
                                //at this point, the next month is this month (lol), we need to remove one from the interval
                                intervalOffset = 1
                            }
                            //get the day of the next required month
                            d = (new Date(now.year + interval - intervalOffset, mo + 1, d)).date
                            now = new Date(now.year + interval - intervalOffset, mo, d, now.hours, now.minutes, 0)
                        } else {
                        	//the day is in the string
                        	day = day.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "")
                            if (!day.isInteger()) {
                            	//error
                            	return null
                            }                            
                            d = day.toInteger()
							now = new Date(now.year + interval - ((d > now.date) && (now.month == mo) ? 1 : 0), mo, d, now.hours, now.minutes, 0)
							if (d > now.date) {
                            	//we went overboard, this month does not have so many days, repeat the cycle to move on to the next month that does
                                if (d > 29) {
                                	//no year ever will have this day on the selected month
                                    return null
                                }
                                repeatCycle = true
                            }
                        }
                    }
                    cycles = 10
                    break
            }
        }
		//check if we have to repeat or exit
		if ((!repeatCycle) && testDateTimeFilters(condition, now)) {
            //make it UTC Unix Time
            def result = convertDateToUnixTime(now)
            //we only provide a time in the future
            //if we weren't, we'd be hogging everyone trying to keep up
            if (result >= (new Date()).time) {
            	return result
            }
        }       
        maxCycles = (maxCycles == null ? cycles : maxCycles) - 1
	}
}

def keepAlive() {
	state.run = "app"
    processTasks()
}

private processTasks() {
	//pfew, off to process tasks
    //first, we make a variable to help us pick up where we left off
    def tasks = null
    def perf = now()
    debug "Processing tasks", 1, "trace"
    
    try {

        def safetyNet = false

        //let's give now() a 2s bump up so that if anything is due within 2s, we do it now rather than scheduling ST
        def threshold = 2000

        //we're off to process any pending immediate EVENTS ONLY
        //we loop a seemingly infinite loop
        //no worries, we'll break out of it, maybe :)
        while (true) {
            //we need to read the list every time we get here because the loop itself takes time.
            //we always need to work with a fresh list.
            tasks = tasks ? tasks : atomicState.tasks
            tasks = tasks ? tasks : [:]
            for (item in tasks.findAll{it.value.type == "evt"}.sort{ it.value.time }) {
                def task = item.value
                if (task.time <= now() + threshold) {
                    //remove from tasks
                    tasks.remove(item.key)
                    atomicState.tasks = tasks
                    state.tasks = tasks
                    //throw away the task list as this procedure below may take time, making our list stale
                    //not to worry, we'll read it again on our next iteration
                    tasks = null
                    //since we may timeout here, install the safety net
                    if (!safetyNet) {
                        safetyNet = true
                        debug "Installing ST safety net", null, "trace"
                        runIn(90, recoveryHandler)
                    }
                    //trigger an event
                    if (getCondition(task.ownerId, true)) {
                        //look for condition in primary block
                        debug "Broadcasting time event for primary IF block, condition #${task.ownerId}, task = $task", null, "trace"
                        broadcastEvent([name: "time", date: new Date(task.time), deviceId: "time", conditionId: task.ownerId], true, false)
                    } else if (getCondition(task.ownerId, false)) {
                        //look for condition in secondary block
                        debug "Broadcasting time event for secondary IF block, condition #${task.ownerId}", null, "trace"
                        broadcastEvent([name: "time", date: new Date(task.time), deviceId: "time", conditionId: task.ownerId], false, true)
                    } else {
                        debug "ERROR: Time event cannot be processed because condition #${task.ownerId} does not exist", null, "error"
                    }
                    //continue the loop
                    break
                }
            }
            //well, if we got here, it means there's nothing to do anymore
            if (tasks != null) break
        }

        //okay, now let's give the time triggers a chance to readjust
        scheduleTimeTriggers()

        //read the tasks
        tasks = atomicState.tasks
        tasks = tasks ? tasks : [:]
        def idx = 1
        //find the last index
        for(task in tasks) {
            if ((task.value.idx) && (task.value.idx >= idx)) {
                idx = task.value.idx + 1
            }
        }
        
        //then if there's any pending tasks in the tasker, we look them up too and merge them to the task list
        if (state.tasker && state.tasker.size()) {
            for (task in state.tasker.sort{ it.idx }) {
                if (task.add) {
                    def t = cleanUpMap([type: task.add, idx: idx, ownerId: task.ownerId, deviceId: task.deviceId, taskId: task.taskId, time: task.time, created: task.created, data: task.data])
                    def n = "${task.add}:${task.ownerId}${task.deviceId ? ":${task.deviceId}" : ""}${task.taskId ? "#${task.taskId}" : ""}"
                    idx++
                    tasks[n] = t
                } else if (task.del) {
                    //delete a task
                    def dirty = true
                    while (dirty) {
                        dirty = false
                        for (it in tasks) {
                        	if ((it.value.type == task.del) && (!task.ownerId || (it.value.ownerId == task.ownerId)) && (!task.deviceId || (task.deviceId == it.value.deviceId)) && (!task.taskId || (task.taskId == it.value.taskId))) {
                            	tasks.remove(it.key)
    	                        dirty = true
                                break
	                        }
	                    }
	                }
                }
            }
            //we save the tasks list atomically, ouch
            //this is to avoid spending too much time with the tasks list on our hands and having other instances
            //running and modifying the old list that we picked up above
            state.tasksProcessed = now()
            atomicState.tasks = tasks
            state.tasks = tasks
            state.tasker = null
        }

        //time to see if there is any ST schedule needed for the future
        def nextTime = null
        def immediateTasks = 0
        def thresholdTime = now() + threshold
        for (item in tasks) {
            def task = item.value
            //if a command task is waiting, we ignore it
            if (!task.data || !task.data.w) {
            	//if a task is already due, we keep track of it
            	if (task.time <= thresholdTime) {
	                immediateTasks++
	            } else {
	                //we try to get the nearest time in the future
	                nextTime = (nextTime == null) || (nextTime > task.time) ? task.time : nextTime
    	        }
            }
        }
        //if we found a time that's after 
        if (nextTime) {
            def seconds = Math.round((nextTime - now()) / 1000)
            runIn(seconds, timeHandler)
            state.nextScheduledTime = nextTime
            setVariable("\$nextScheduledTime", nextTime, true)
            debug "Scheduling ST to run in ${seconds}s, at ${formatLocalTime(nextTime)}", null, "info"
        } else {
            setVariable("\$nextScheduledTime", null, true)
        }

        //we're done with the scheduling, let's do some real work, if we have any
        if (immediateTasks) {
            if (!safetyNet) {
                //setup a safety net ST schedule to resume the process if we fail
                safetyNet = true
                debug "Installing ST safety net", null, "trace"
                runIn(90, recoveryHandler)
            }

            debug "Found $immediateTasks task${immediateTasks > 1 ? "s" : ""} due at this time"
            //we loop a seemingly infinite loop
            //no worries, we'll break out of it, maybe :)
            def found = true
            while (found) {
                found = false
                //we need to read the list every time we get here because the loop itself takes time.
                //we always need to work with a fresh list. Using a ? would not read the list the first time around (optimal, right?)
                tasks = tasks ? tasks : atomicState.tasks
                tasks = tasks ? tasks : [:]
                def firstTask = tasks.sort{ it.value.time }.find{ (it.value.type == "cmd") && (!it.value.data || !it.value.data.w) && (it.value.time <= (now() + threshold)) }
                if (firstTask) {
                    def firstSubTask = tasks.sort{ it.value.idx }.find{ (it.value.type == "cmd") && (!it.value.data || !it.value.data.w) && (it.value.time == firstTask.value.time) }
                    if (firstSubTask) {
                        def task = firstSubTask.value
                        //remove from tasks
                        tasks = atomicState.tasks
                        tasks.remove(firstSubTask.key)
                        atomicState.tasks = tasks
                        state.tasks = tasks
                        //throw away the task list as this procedure below may take time, making our list stale
                        //not to worry, we'll read it again on our next iteration
                        tasks = null
                        //do some work
                        if (settings.enabled && (task.type == "cmd")) {
                            debug "Processing command task $task"
                            try {
                            	processCommandTask(task)
							} catch (e) {
                            	debug "ERROR: Error while processing command task: $e", null, "error"
                            }
                        }
                        //repeat the while since we just modified the task
                        found = true
                    }
                }
            }
        }
        //would you look at that, we finished!
        //remove the safety net, wasn't worth the investment
        debug "Removing any existing ST safety nets", null, "trace"
        unschedule(recoveryHandler)
    } catch (e) {
    	debug "ERROR: Error while executing processTasks: $e", null, "error"
    }
	//end of processTasks
	perf = now() - perf
    debug "Task processing took ${perf}ms", -1, "trace"
}

private cancelTasks(state) {
	def tasks = tasks ? tasks : atomicState.tasks
	tasks = tasks ? tasks : [:]
	//debug "Resuming tasks on piston state change, resumable states are $resumableStates", null, "trace"
    while (true) {
    	def item = tasks.find{ (it.value.type == "cmd") && (it.value.data && it.value.data.c)}
        if (item) {
        	tasks.remove(item.key)
        } else {
        	break
        }
    }
    atomicState.tasks = tasks
}

private resumeTasks(state) {
	def tasks = tasks ? tasks : atomicState.tasks
	tasks = tasks ? tasks : [:]
    def resumableStates = ["a", (state ? "t" : "f")]
	//debug "Resuming tasks on piston state change, resumable states are $resumableStates", null, "trace"
    def time = now()
    def list = tasks.findAll{ (it.value.type == "cmd") && (it.value.data && (it.value.data.w in resumableStates))}
    //todo: support for multiple wait for state commands during same action
    if (list.size()) {
        for (item in list) {
            tasks[item.key].time = time + (tasks[item.key].data.o ? tasks[item.key].data.o  : 0)
            tasks[item.key].data.w = null
            tasks[item.key].data.o = null
        }
		atomicState.tasks = tasks
    }
}

//the heavy lifting of commands
//this executes each and every single command we have to give
private processCommandTask(task) {
    def action = getAction(task.ownerId)
	if (!action) return false
	if (!action.t) return false
    def devices = listActionDevices(action.id)
    def device = devices.find{ it.id == task.deviceId }
    def t = action.t.find{ it.i == task.taskId }
    if (!t) return false
    //found the actual task, let's figure out what command we're running
    def cmd = t.c
    def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
    def custom = (cmd && cmd.startsWith(customCommandPrefix()))
    cmd = cleanUpCommand(cmd)
    def command = null
    if (virtual) {
        //dealing with a virtual command
        command = getVirtualCommandByDisplay(cmd)
        if (command && !command.immediate) {
        	//we can't run immediate tasks here
            //execute the virtual task
            def cn = command.name
            def suffix = ""
            if (cn.contains("#")) {
            	//multi command
                def parts = cn.tokenize("#")
                if (parts.size() == 2) {
                	cn = parts[0]
                    suffix = parts[1]
				}
            }
            def msg = "Executing virtual command ${cn}"
            if (state.sim) state.sim.cmds.push(msg)
            debug msg, null, "info"
            def function = "task_vcmd_${cn}".replace(" ", "_").replace("(", "_").replace(")", "_").replace("&", "_").replace("#", "_")
            return "$function"(command.aggregated ? devices : device, task, suffix)
        }
    } else {
        command = getCommandByDisplay(cmd)
        if (command) {
            if (device.hasCommand(command.name)) {
                def requiredParams = command.parameters ? command.parameters.size() : 0
                def availableParams = t.p ? t.p.size() : 0
                if (requiredParams == availableParams) {
                    def params = []
                    t.p.sort{ it.i }.findAll() {
                        params.push(it.d instanceof String ? formatMessage(it.d) : it.d)
                    }
                    if (params.size()) {
                    	if ((command.name == "setColor") && (params.size() == 5)) {
                        	//using a little bit of a hack here
                            //we should have 5 parameters:
                            //color name
                            //color rgb
                            //hue
                            //saturation
                            //lightness
                            def name = params[0]
                            def hex = params[1]
                            def hue = params[2] instanceof Integer ? params[2] / 3.6 : 0
                            def saturation = params[3]
                            def lightness = params[4]
                            def p = [:]
                            if (name) {
                            	def color = getColorByName(name)
                                p.hue = color.h / 3.6
                                p.saturation = color.s
                                //ST wrongly calls this level - it's lightness
                                p.level = color.l
                            } else if (hex) {
                            	p.hex = hex
                            } else {
                            	p.hue = hue
                                p.saturation = saturation
                                p.level - lightness
                            }
                            def msg = "Executing with parameters: [${device}].${command.name}($p)"
				            if (state.sim) state.sim.cmds.push(msg)
							debug msg, null, "info"
                        	device."${command.name}"(p)
                        } else {
                        	def msg = "Executing with parameters: [${device}].${command.name}($params)" 
				            if (state.sim) state.sim.cmds.push(msg)
                        	debug msg, null, "info"
                        	device."${command.name}"(params as Object[])
                        }
                        return true
                    } else {
                    	def msg = "Executing: [${device}].${command.name}()"
                        if (state.sim) state.sim.cmds.push(msg)
                        debug msg, null, "info"
                        device."${command.name}"()
                        return true
                    }
                }
            }
        }
    }
	return false
}

private task_vcmd_toggle(device, task, suffix = "") {
    if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix")) {
    	//we need a device that has both on and off commands
    	return false
    }
    if (device.currentValue("switch") == "on") {
    	device."off$suffix"()
    } else {
    	device."on$suffix"()
    }
    return true
}

private task_vcmd_toggleLevel(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || !device.hasCommand("setLevel") || (params.size() != 1)) {
    	//we need a device that has both on and off commands
    	return false
    }
    def level = params[0].d
    if (device.currentValue("switch") == "on") {
    	device."off$suffix"()
    } else {
    	device.setLevel(level)
    	device."on$suffix"()
    }
    return true
}

private task_vcmd_delayedToggle(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || (params.size() != 1)) {
    	//we need a device that has both on and off commands
    	return false
    }
    def delay = params[0].d
    if (device.currentValue("switch") == "on") {
    	device."off$suffix"([delay: delay])
    } else {
    	device."on$suffix"([delay: delay])
    }
    return true
}

private task_vcmd_delayedOn(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || !device.hasCommand("on$suffix") || (params.size() != 1)) {
    	//we need a device that has both on and off commands
    	return false
    }
    def delay = params[0].d
   	device."on$suffix"([delay: delay])
    return true
}

private task_vcmd_delayedOff(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || !device.hasCommand("off$suffix") || (params.size() != 1)) {
    	//we need a device that has both on and off commands
    	return false
    }
    def delay = params[0].d
   	device."off$suffix"([delay: delay])
    return true
}

private task_vcmd_flash(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || (params.size() != 3)) {
    	//we need a device that has both on and off commands
        //we also need three parameters
        //p[0] represents the on interval
        //p[1] represents the off interval
        //p[2] represents the number of flashes
    	return false
    }
    def onInterval = params[0].d
    def offInterval = params[1].d
    def flashes = params[2].d
    def delay = 0
    def originalState = device.currentValue("switch")
    for (def i = 0; i < flashes; i++) {
    	device."on$suffix"([delay: delay])
        delay = delay + onInterval
    	device."off$suffix"([delay: delay])
        delay = delay + offInterval
    }
    if (originalState == "on") {
    	device."on$suffix"([delay: delay])
    }
    return true
}

private task_vcmd_setLocationMode(devices, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 1) {
    	return false
    }
    def mode = params[0].d
    if (location.mode != mode) {
        location.setMode(mode)
        return true
    } else {
    	debug "Not changing location mode because location is already in the $mode mode"
    }
    return false
}

private task_vcmd_setAlarmSystemStatus(devices, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 1) {
    	return false
    }
    def status = params[0].d
    if (getAlarmSystemStatus() != status) {
        setAlarmSystemStatus(status)
        return true
    } else {
    	debug "WARNING: Not changing SHM's status because it already is $status", null, "warn"
    }
    return false
}

private task_vcmd_sendNotification(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 1) {
    	return false
    }
    def message = formatMessage(params[0].d)
    sendNotificationEvent(message)
}

private task_vcmd_sendPushNotification(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 2) {
    	return false
    }
    def message = formatMessage(params[0].d)
    def saveNotification = !!params[1].d
    if (saveNotification) {
    	sendPush(message)
    } else {
		sendPushMessage(message)
	}
}

private task_vcmd_sendSMSNotification(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 3) {
    	return false
    }
    def message = formatMessage(params[0].d)
    def phones = "${params[1].d}".replace(" ", "").replace("-", "").replace("(", "").replace(")", "").tokenize(",;*|").unique()
    def saveNotification = !!params[2].d
    for(def phone in phones) {
        if (saveNotification) {
            sendSms(phone, message)
        } else {
            sendSmsMessage(phone, message)
        }
        //we only need one notification
        saveNotification = false
    }
}


private task_vcmd_executeRoutine(devices, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (params.size() != 1) {
    	return false
    }
    def routine = formatMessage(params[0].d)
	location.helloHome?.execute(routine)
    return true
}

private task_vcmd_cancelPendingTasks(device, task, suffix = "") {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || (params.size() != 1)) {
    	return false
    }
	unscheduleTask("cmd", null, device.id)
    if (params[0].d == "Global") {
    	debug "WARNING: Global cancellation not yet implemented", null, "warn"
    }
    return true
}

private task_vcmd_loadAttribute(device, task, simulate = false) {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!device || (params.size() != 4)) {
    	return false
    }
	def attribute = cleanUpAttribute(params[0].d)
    def variable = params[1].d
    def allowTranslations = !!params[2].d
    def negateTranslations = !!params[3].d   
    //work, work, work
    //get the real value
    def value = getVariable(variable)
    def commands = commands().findAll{ (it.attribute == attribute) && it.value }
    log.trace "Possible commands are: ${commands*.name}"
    //oh boy, we can pick and choose...
    for (command in commands) {
    	if (command.value.startsWith("*")) {
        	if (command.parameters && (command.parameters.size() == 1)) {
                def parts = command.value.tokenize(":")
                def v = value
                if (parts.size() == 2) {
                    v = cast(v, parts[1])
                }
                if (device.hasCommand(command.name)) {
                    log.trace "Executing [${getDeviceLabel(device)}].$command($v)"
                    device."${command.name}"(v)
                    return true
                }
            }        	
        } else {
            if ((command.value == value) && (!command.parameters)) {
                //found an exact match, let's do it
                if (device.hasCommand(command.name)) {
                    log.trace "Executing [${getDeviceLabel(device)}].$command()"
                    device."${command.name}"()
                    return true
                }
            }
        }
    }
    //boolean stuff goes here
    if (!allowTranslations) return false
    def v = cast(value, "boolean")
    if (negateTranslations) v = !v
    for (command in commands) {
    	if (!command.value.startsWith("*")) {
            if ((cast(command.value, "boolean") == v) && (!command.parameters)) {
                //found an exact match, let's do it
                if (device.hasCommand(command.name)) {
                    log.trace "Executing [${getDeviceLabel(device)}].$command() (boolean translation)"
                    device."${command.name}"()
                    return true
                }
            }
        }
    }
    return false
}

private task_vcmd_saveAttribute(devices, task, simulate = false) {
    def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
    if (!devices || (params.size() != 4)) {
    	return false
    }
	def attribute = cleanUpAttribute(params[0].d)
    def aggregation = params[1].d
    def dataType = params[2].d
    def variable = params[3].d    
    //work, work, work
    def result
    def attr = getAttributeByName(attribute)
    if (attr) {
    	def type = attr.type
        result = cast("", attr.type)
        def values = []
        for (device in devices) {
            values.push cast(device.currentValue(attribute), type)
        }        
        if (values.size()) {
            switch (aggregation) {
				case "First":
                    result = null
                    for(value in values) {
                        result = value
                        break
                    }            	
                    break
				case "Last":
                    result = null
                    for(value in values) {
                        result = value
                    }            	
                    break
				case "Min":
                    result = null
                    for(value in values) {
                        if ((result == null) || (value < result)) result = value
                    }            	
                    break
                case "Max":
                    result = null
                    for(value in values) {
                        if ((result == null) || (value > result)) result = value
                    }            	
                    break
                case "Avg":
                    result = null
                    if (attr.type in ["number", "decimal"]) {
                        for(value in values) {
                        	result = result == null ? value : result + value
                        }
                        result = cast(result / values.size(), attr.type)
                    } else {
                        //average will act differently on strings and booleans
                        //we look for the value that is used most and we consider that the average
                        def map = [:]
                        for (value in values) {
                            map[value] = map[value] ? map[value] + 1 : 1
                        }
                        for (item in map.sort { - it.value }) {
	                        result = cast(item.key, attr.type)
                            break
                        }
                    }
                    break                    
                case "Sum":
                    result = null
                    if (attr.type in ["number", "decimal"]) {
                        for(value in values) {
                        	result = result == null ? value : result + value
                        }
                    } else {
                        //sum will act differently on strings and booleans
                        result = buildNameList(values, "")
                    }
                    break
                case "Count":
                    result = (int) values.size()
                    break
				case "Boolean And":
                	result = true
                    for (value in values) {
                    	result = result && cast(value, "boolean")
                        if (!result) break
                    }
                    break
                case "Boolean Or":
                	result = false
                    for (value in values) {
                    	result = result || cast(value, "boolean")
                        if (result) break
                    }
                    break
                case "Boolean True Count":
                	result = (int) 0
                    for (value in values) {
                    	if (cast(value, "boolean")) result += 1
                    }
                    break
                case "Boolean True Count":
                	result = (int) 0
                    for (value in values) {
                    	if (!cast(value, "boolean")) result += 1
                    }
                    break                    
            }
        }
    }
    
   
    if (dataType) {
    	//if user wants a certain data type, we comply
    	result = cast(result, dataType)
    }
    setVariable(variable, result)
    return true
}


private task_vcmd_setVariable(devices, task, simulate = false) {
    def params = simulate ? ((task && task.p && task.p.size()) ? task.p : []) : ((task && task.data && task.data.p && task.data.p.size()) ? task.data.p : [])
	//we need at least 7 params
	if (params.size() < 7) {
    	return simulate ? null : false
    }
	def name = params[0].d
    def dataType = params[1].d
    if (!name || !dataType) return simulate ? null : false
    def result = ""
    switch (dataType) {
    	case "time":
        	result = adjustTime()
            break
		case "number":
        case "decimal":
        	result = 0
            break
    }
    def immediate = !!params[2].d
    if (algebra) {
    	//no complex algebra yet :(
        return simulate ? null : false
    } else {
    	try {
            def i = 4
            def grouping = false
            def groupingUnit = ""
            def groupingIndex = null
            def groupingResult = null
            def groupingOperation = null
            def previousOperation = null
            def operation = null
            def subDataType = dataType
            def idx = 0
            while (true) {
                def value = params[i].d
                def variable = params[i + 1].d
                if (!value) {
                    //we get the value of the variable
                    if (subDataType in ["time"]) {
                    	value = adjustTime(getVariable(variable)).time
                    } else {
	                    value = cast(getVariable(variable, dataType in ["string", "text"]), subDataType)
                    }
                } else {
                	value = cast(value, subDataType)
                }
                if (i == 4) {
                    //initial values
                    result = cast(value, dataType)
                }
                def unit = (dataType == "time" ? params[i + 2].d : null)
                previousOperation = operation
                operation = params.size() > i + 3 ? "${params[i + 3].d} ".tokenize(" ")[0] : null                
                def needsGrouping = (operation == "*") || (operation == "÷") || (operation == "AND")
                def skip = idx == 0
                if (needsGrouping) {
                    //these operations require grouping i.e. (a * b * c) seconds
                    if (!grouping) {
                        grouping = true
                        groupingIndex = idx
                        groupingUnit = unit
                        groupingOperation = previousOperation
                        groupingResult = value
                        skip = true
                    }
                }
                //add the value/variable
                subDataType = subDataType == "time" ? "long" : subDataType
                if (!skip) {
                    def operand1 = grouping ? groupingResult : result
                    def operand2 = value
                    if (groupingUnit ? groupingUnit : unit) {
                        switch (unit) {
                            case "seconds":
                            operand2 = operand2 * 1000
                            break
                            case "minutes":
                            operand2 = operand2 * 60000
                            break
                            case "hours":
                            operand2 = operand2 * 3600000
                            break
                            case "days":
                            operand2 = operand2 * 86400000
                            break
                            case "weeks":
                            operand2 = operand2 * 604800000
                            break
                            case "months":
                            operand2 = operand2 * 2592000000
                            break
                            case "years":
                            operand2 = operand2 * 31536000000
                            break
                        }
                    }
                    //reset the group unit - we only apply it once
                    groupingUnit = null
                    def res = null
                    switch (previousOperation) {
                        case "AND":
                        res = cast(operand1 && operand2, subDataType)
                        break
                        case "OR":
                        res = cast(operand1 || operand2, subDataType)
                        break
                        case "+":
                        res = cast(operand1 + operand2, subDataType)
                        break
                        case "-":
                        res = cast(operand1 - operand2, subDataType)
                        break
                        case "*":
                        res = cast(operand1 * operand2, subDataType)
                        break
                        case "÷":
                        if (!operand2) return null
                        res = cast(operand1 / operand2, subDataType)
                        break
                    }
                    if (grouping) {
                        groupingResult = res
                    } else {
                        result = res
                    }
                }
                skip = false
                if (grouping && !needsGrouping) {
                    //these operations do NOT require grouping
                    //ungroup
                    if (!groupingOperation) {
                        result = groupingResult
                    } else {
                        def operand1 = result
                        def operand2 = groupingResult

                        switch (groupingOperation) {
                            case "AND":
                            result = cast(operand1 && operand2, subDataType)
                            break
                            case "OR":
                            result = cast(operand1 || operand2, subDataType)
                            break
                            case "+":
                            result = cast(operand1 + operand2, subDataType)
                            break
                            case "-":
                            result = cast(operand1 - operand2, subDataType)
                            break
                            case "*":
                            result = cast(operand1 * operand2, subDataType)
                            break
                            case "÷":
                            if (!operand2) return null
                            result = cast(operand1 / operand2, subDataType)
                            break
                        }                
                    }
                    grouping = false
                }
                if (!operation) break
                i += 4                
                idx += 1
            }
        } catch (e) {
        	return simulate ? null : false
        }
    }
    if (dataType in ["string", "text"]) {
    	result = formatMessage(result)
    } else if (dataType in ["time"]) {
		result = simulate ? formatLocalTime(convertTimeToUnixTime(result)) : convertTimeToUnixTime(result)
	}
    if (!simulate) {
    	setVariable(name, result)
    } else {
	    return result
    }
    return true
}


private cast(value, dataType) {
	def trueStrings = ["1", "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
    def falseStrings = ["0", "false", "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping"]
	switch (dataType) {
        case "string":
        case "text":
			return value ? "$value" : ""
        case "number":
        	if (value == null) return (int) 0
        	if (value instanceof String) {
            	if (value.isInteger())
                	return value.toInteger()
            	if (value.isFloat())
                	return (int) Math.round(value.toFloat())
                if (value in trueStrings)
                	return (int) 1
            }
            def result = (int) 0
            try {
            	result = (int) value
            } catch(all) {
            }
            return result ? result : (int) 0
        case "long":
        	if (value == null) return (long) 0
        	if (value instanceof String) {
            	if (value.isInteger())
                	return (long) value.toInteger()
            	if (value.isFloat())
                	return (long) Math.round(value.toFloat())
                if (value in trueStrings)
                	return (long) 1
            }
            def result = (long) 0
            try {
            	result = (long) value
            } catch(all) {
            }
            return result ? result : (long) 0
        case "decimal":
        	if (value == null) return (float) 0
        	if (value instanceof String) {
            	if (value.isFloat())
                	return (float) value.toFloat()
            	if (value.isInteger())
                	return (float) value.toInteger()
                if (value in trueStrings)
                	return (float) 1
            }
            def result = (float) 0
            try {
            	result = (float) value
            } catch(all) {
            }
            return result ? result : (float) 0
        case "boolean":
        	if (value instanceof String) {
            	if (!value || (value in falseStrings))
                	return false
                return true
            }
            return !!value
		case "time":
			return value instanceof String ? adjustTime(value).time : cast(value, "long")
    }
    //anything else...
    return value
}





/******************************************************************************/
/*** CoRE PISTON PUBLISHED METHODS											***/
/******************************************************************************/

def getLastPrimaryEvaluationDate() {
	return state.lastPrimaryEvaluationDate
}

def getLastPrimaryEvaluationResult() {
	return state.lastPrimaryEvaluationResult
}

def getLastSecondaryEvaluationDate() {
	return state.lastSecondaryEvaluationDate
}

def getLastSecondaryEvaluationResult() {
	return state.lastSecondaryEvaluationResult
}

def getCurrentState() {
	return state.currentState
}

def getMode() {
	return state.app  ? state.app.mode : null
}

def getDeviceSubscriptionCount() {
	return state.deviceSubscriptions ? state.deviceSubscriptions : 0
}

def getCurrentStateSince() {
	return state.currentStateSince
}

def getRunStats() {
	return state.runStats
}

def resetRunStats() {
    atomicState.runStats = null
	state.runStats = null
}

def getConditionStats() {
	return [
    	conditions: getConditionCount(state.app),
        triggers: getTriggerCount(state.app)
    ]
}





/******************************************************************************/
/***																		***/
/*** UTILITIES																***/
/***																		***/
/******************************************************************************/

/******************************************************************************/
/*** DEBUG FUNCTIONS														***/
/******************************************************************************/

private debug(message, shift = null, cmd = null) {
	def debugging = settings.debugging
	if (!debugging) {
    	return
    }
    cmd = cmd ? cmd : "debug"
    if (!settings["log#$cmd"]) {
    	return
    }
    //mode is
    // 0 - initialize level, level set to 1
    // 1 - start of routine, level up
    // -1 - end of routine, level down
    // anything else - nothing happens
    def maxLevel = 4
    def level = state.debugLevel ? state.debugLevel : 0
    def levelDelta = 0
    def prefix = "║"
    def pad = "░"
    switch (shift) {
    	case 0:
        	level = 0
            prefix = ""
            break
        case 1: 
        	level += 1
            prefix = "╚"
            pad = "═"
            break
        case -1:
        	levelDelta = -(level > 0 ? 1 : 0)
            pad = "═"
            prefix = "╔"
        break
    }
    
    if (level > 0) {
    	prefix = prefix.padLeft(level, "║").padRight(maxLevel, pad)
    }

    level += levelDelta
    state.debugLevel = level
    
    if (debugging) {
    	prefix += " "
    } else {
    	prefix = ""
    }

	if (cmd == "info") {
		log.info "$prefix$message"
    } else if (cmd == "trace") {
		log.trace "$prefix$message"
    } else if (cmd == "warn") {
		log.warn "$prefix$message"
    } else if (cmd == "error") {
		log.error "$prefix$message"
    } else {
		log.debug "$prefix$message"
    }
}


/******************************************************************************/
/*** DATE & TIME FUNCTIONS													***/
/******************************************************************************/
private getPreviousQuarterHour(unixTime = now()) {
	return unixTime - unixTime.mod(900000)
}

//adjusts the time to local timezone
private adjustTime(time = null) {
	if (time instanceof String) {
    	//get UTC time
    	time = timeToday(time, location.timeZone).getTime()
    }
    if (time instanceof Date) {
    	//get unix time
    	time = time.getTime()
    }
    if (!time) {
    	time = now()
    }
    if (time) {
    	return new Date(time + location.timeZone.getOffset(time))
    }
    return null
}

private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm a z") {
	if (time instanceof Long) {
    	time = new Date(time)
    }
	if (time instanceof String) {
    	//get UTC time
    	time = timeToday(time, location.timeZone)
    }   
    if (!(time instanceof Date)) {
    	return null
    }
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}

private convertDateToUnixTime(date) {
	if (!date) {
    	return null
    }
	if (!(date instanceof Date)) {
    	date = new Date(date)
    }
	return date.time - location.timeZone.getOffset(date.time)
}

private convertTimeToUnixTime(time) {
	if (!time) {
    	return null
    }
	return time - location.timeZone.getOffset(time)
}

private formatTime(time) {
	//we accept both a Date or a settings' Time    
    return formatLocalTime(time, "h:mm a z")
}

private formatHour(h) {
	return (h == 0 ? "midnight" : (h < 12 ? "${h} AM" : (h == 12 ? "noon" : "${h-12} PM"))).toString()
}

private formatDayOfMonth(dom, dow) {
	if (dom) {
    	if (dom.contains("week")) {
        	//relative day of week
            return dom.replace("week", dow)
        } else {
        	//dealing with a certain day of the month
            if (dom.contains("last")) {
            	//relative day value
                return dom
            } else {           	
            	//absolute day value
                def day = dom.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "").toInteger()
                return "on the ${formatOrdinalNumber(day)}"
            }
        }
    }
    return "[ERROR]"
}

//return the number of occurrences of same day of week up until the date or from the end of the month if backwards, i.e. last Sunday is -1, second-last Sunday is -2
private getWeekOfMonth(date = null, backwards = false) {
	if (!date) {
    	date = adjustTime(now())
    }
	def day = date.date
    if (backwards) {
        def month = date.month
        def year = date.year
        def lastDayOfMonth = (new Date(year, month + 1, 0)).date
        return -(1 + Math.floor((lastDayOfMonth - day) / 7))
    } else {
		return 1 + Math.floor((day - 1) / 7) //1 based
    }
}

//returns the number of day in a month, 1 based, or -1 based if backwards (last day of the month)
private getDayOfMonth(date = null, backwards = false) {
	if (!date) {
    	date = adjustTime(now())
    }
	def day = date.date
    if (backwards) {
        def month = date.month
        def year = date.year
        def lastDayOfMonth = (new Date(year, month + 1, 0)).date
        return day - lastDayOfMonth - 1
    } else {
		return day
    }
}

//for a given month, returns the Nth instance of a certain day of the week within that month. week ranges from 1 through 5 and -1 through -5
private getDayInWeekOfMonth(date, week, dow) {
	if (!date || (dow == null)) {
    	return null
    }
    def lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
	if (week > 0) {
    	//going forward
        def firstDayOfMonthDOW = (new Date(date.year, date.month, 1)).day
        //find the first matching day
        def firstMatch = 1 + dow - firstDayOfMonthDOW + (dow < firstDayOfMonthDOW ? 7 : 0)
        def result = firstMatch + 7 * (week - 1)
        return result <= lastDayOfMonth ? result : null
    }
    if (week < 0) {
    	//going backwards
        def lastDayOfMonthDOW = (new Date(date.year, date.month + 1, 0)).day
        //find the first matching day
        def firstMatch = lastDayOfMonth + dow - lastDayOfMonthDOW - (dow > lastDayOfMonthDOW ? 7 : 0)
        def result = firstMatch + 7 * (week + 1)
        return result >= 1 ? result : null
    }
    return null
}

private getDayOfWeekName(date = null) {
	if (!date) {
    	date = adjustTime(now())
    }
    switch (date.day) {
    	case 0: return "Sunday"
    	case 1: return "Monday"
    	case 2: return "Tuesday"
    	case 3: return "Wednesday"
    	case 4: return "Thursday"
    	case 5: return "Friday"
    	case 6: return "Saturday"
    }
    return null
}

private getDayOfWeekNumber(date = null) {
	if (!date) {
    	date = adjustTime(now())
    }
    if (date instanceof Date) {
    	return date.day
    }
    switch (date) {
    	case "Sunday": return 0
    	case "Monday": return 1
    	case "Tuesday": return 2
    	case "Wednesday": return 3
    	case "Thursday": return 4
    	case "Friday": return 5
    	case "Saturday": return 6
    }
    return null
}

private getMonthName(date = null) {
	if (!date) {
    	date = adjustTime(now())
    }
    def month = date.month + 1
    switch (month) {
    	case  1: return "January"
    	case  2: return "February"
    	case  3: return "March"
    	case  4: return "April"
    	case  5: return "May"
    	case  6: return "June"
    	case  7: return "July"
    	case  8: return "August"
    	case  9: return "September"
    	case 10: return "October"
    	case 11: return "November"
    	case 12: return "December"
    }
    return null
}

private getMonthNumber(date = null) {
	if (!date) {
    	date = adjustTime(now())
    }
    if (date instanceof Date) {
    	return date.month + 1
    }
    switch (date) {
    	case "January": return 1
    	case "February": return 2
    	case "March": return 3
    	case "April": return 4
    	case "May": return 5
    	case "June": return 6
    	case "July": return 7
        case "August": return 8
    	case "September": return 9
    	case "October": return 10
    	case "November": return 11
    	case "December": return 12
    }
    return null
}
private getSunrise() {
	if (!(state.sunrise instanceof Date)) {
    	def sunTimes = getSunriseAndSunset()
        state.sunrise = adjustTime(sunTimes.sunrise)
        state.sunset = adjustTime(sunTimes.sunset)
    }
    return state.sunrise
}

private getSunset() {
	if (!(state.sunset instanceof Date)) {
    	def sunTimes = getSunriseAndSunset()
        state.sunrise = adjustTime(sunTimes.sunrise)
        state.sunset = adjustTime(sunTimes.sunset)
    }
    return state.sunset
}

private addOffsetToMinutes(minutes, offset) {
	if (minutes == null) {
    	return null
    }
	if (offset == null) {
    	return minutes
    }
	minutes = minutes + offset
    while (minutes >= 1440) {
    	minutes -= 1440
    }
    while (minutes < 0) {
    	minutes += 1440
    }
    return minutes
}

private timeComparisonOptionValues(trigger) {
   	return ["custom time", "midnight", "sunrise", "noon", "sunset", "time of variable", "date and time of variable"] + (trigger ? ["every minute", "every number of minutes", "every hour", "every number of hours"] : [])
}

private timeOptions(trigger = false) {
	def result = ["1 minute"]
    for (def i =2; i <= (trigger ? 360 : 60); i++) {
    	result.push("$i minutes")
    }
	return result
}

private timeRepeatOptions() {
	return ["every day", "every number of days", "every week", "every number of weeks", "every month", "every number of months", "every year", "every number of years"]
}

private timeMinuteOfHourOptions() {
	def result = []
    for (def i =0; i <= 59; i++) {
    	result.push("$i".padLeft(2, "0"))
    }
	return result
}

private timeHourOfDayOptions() {
	def result = []
    for (def i =0; i <= 23; i++) {
    	result.push(formatHour(i))
    }
	return result
}

private timeDayOfMonthOptions() {
	def result = []
    for (def i =1; i <= 31; i++) {
    	result.push("on the ${formatOrdinalNumber(i)}")
    }
	return result + ["on the last day", "on the second-last day", "on the third-last day", "on the first week", "on the second week", "on the third week", "on the fourth week", "on the fifth week", "on the last week", "on the second-last week", "on the third-last week"]
}

private timeDayOfMonthOptions2() {
	def result = []
    for (def i =1; i <= 31; i++) {
    	result.push("the ${formatOrdinalNumber(i)}")
    }
	return result + ["the last day of the month", "the second-last day of the month", "the third-last day of the month"]
}

private timeDayOfWeekOptions() {
	return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
}

private timeWeekOfMonthOptions() {
	return ["the first week", "the second week", "the third week", "the fourth week", "the fifth week", "the last week", "the second-last week"]
}

private timeMonthOfYearOptions() {
	return ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
}

private timeYearOptions() {
	def result = ["even years", "odd years", "leap years"]
    def year = 1900 + (new Date()).getYear()
    for (def i = year; i <= 2099; i++) {
    	result.push("$i")
    }
    for (def i = 2016; i < year; i++) {
    	result.push("$i")
    }
    return result
}

private timeToMinutes(time) {
	if (!(time instanceof String)) return 0
	def value = time.replace(" minutes", "").replace(" minute", "")
    if (value.isInteger()) {
    	return value.toInteger()
    }
    return 0
}





/******************************************************************************/
/*** NUMBER FUNCTIONS														***/
/******************************************************************************/

private formatOrdinalNumber(number) {
	def hm = number.mod(100)
    if ((hm < 10) || (hm > 20)) {
        switch (number.mod(10)) {
            case 1:
                return "${number}st"
            case 2:
                return "${number}nd"
            case 3:
                return "${number}rd"
        }
    }
	return "${number}th"
}

private formatOrdinalNumberName(number) {
	def prefix = ""
    if ((number >= 100) || (number <= -100)) {
    	return "NOT_IMPLEMENTED"
    }
    if (number < -1) {
    	return formatOrdinalNumberName(-number) + "-last"
    }
    if (number >= 20) {
    	def tens = Math.floor(number / 10)
        switch (tens) {
        	case 2:
            	prefix = "twenty"
                break
        	case 3:
            	prefix = "thirty"
                break
        	case 4:
            	prefix = "fourty"
                break
        	case 5:
            	prefix = "fifty"
                break
        	case 6:
            	prefix = "sixty"
                break
        	case 7:
            	prefix = "seventy"
                break
        	case 8:
            	prefix = "eighty"
                break
        	case 9:
            	prefix = "ninety"
                break
        }
        if (prefix) {
        	if (number.mod(10) > 0) {
            	prefix = prefix + "-"
            }
            number = number - tens * 10
        }
    }
	switch (number) {
    	case -1: return "${prefix}last"
        case 0: return prefix
    	case 1: return "${prefix}first"
    	case 2: return "${prefix}second"
    	case 3: return "${prefix}third"
    	case 4: return "${prefix}fourth"
    	case 5: return "${prefix}fifth"
    	case 6: return "${prefix}sixth"
    	case 7: return "${prefix}seventh"
    	case 8: return "${prefix}eighth"
    	case 9: return "${prefix}nineth"
    	case 10: return "${prefix}tenth"
    	case 11: return "${prefix}eleventh"
    	case 12: return "${prefix}twelveth"
    	case 13: return "${prefix}thirteenth"
    	case 14: return "${prefix}fourteenth"
    	case 15: return "${prefix}fifteenth"
    	case 16: return "${prefix}sixteenth"
    	case 17: return "${prefix}seventeenth"
    	case 18: return "${prefix}eighteenth"
    	case 19: return "${prefix}nineteenth"
    }
}









/******************************************************************************/
/*** CONDITION FUNCTIONS													***/
/******************************************************************************/

//finds and returns the condition object for the given condition Id
private _traverseConditions(parent, conditionId) {
    if (parent.id == conditionId) {
        return parent
    }
    for (condition in parent.children) {
        def result = _traverseConditions(condition, conditionId)
        if (result) {
            return result
        }
    }
    return null
}

//returns a condition based on its ID
private getCondition(conditionId, primary = null) {
	def result = null
    def parent = (state.run == "config" ? state.config : state)
    if (parent && (primary in [null, true]) && parent.app && parent.app.conditions) {
    	result =_traverseConditions(parent.app.conditions, conditionId)
    }
    if (!result && parent && (primary in [null, false]) && parent.app && parent.app.otherConditions) {
    	result = _traverseConditions(parent.app.otherConditions, conditionId)
    }
	return result
}

private getConditionMasterId(conditionId) {
	if (conditionId <= 0) return conditionId
    def condition = getCondition(conditionId)
	if (condition && (condition.parentId != null)) return getConditionMasterId(condition.parentId)
    return condition.id
}

//optimized version that returns true if any trigger is detected
private getConditionHasTriggers(condition) {
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                if (getConditionHasTriggers(child)) {
                	//if we detect a trigger we exit immediately
                	return true
                }
            }
        } else {
        	return !!condition.trg
        }
    }
    return false
}

private getConditionTriggerCount(condition) {
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                result += getConditionTriggerCount(child)
            }
        } else {
        	if (condition.trg) {
            	def devices = settings["condDevices${condition.id}"]
                if (devices) {
                	return devices.size()
                } else {
                	return 1
                }
            }
        }
    }
    return result
}

private withEachCondition(condition, callback) {
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                withEachCondition(child, callback)
            }
        } else {
           	"$callback"(condition)
        }
    }
    return result
}

private withEachTrigger(condition, callback) {
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                withEachTrigger(child, callback)
            }
        } else {
        	if (condition.trg) {
            	"$callback"(condition)
            }
        }
    }
    return result
}

private getTriggerCount(app) {
	return getConditionTriggerCount(app.conditions) + (settings.mode in ["Latching", "And-If", "Or-If"] ? getConditionTriggerCount(app.otherConditions) : 0)
}

private getConditionConditionCount(condition) {
	def result = 0
    if (condition) {
        if (condition.children != null) {
            //we're dealing with a group
            for (child in condition.children) {
                result += getConditionConditionCount(child)
            }
        } else {
        	if (!condition.trg) {
            	def devices = settings["condDevices${condition.id}"]
                if (devices) {
                	return devices.size()
                } else {
                	return 1
                }
            }
        }
    }
    return result
}

private getConditionCount(app) {
	return getConditionConditionCount(app.conditions) + (settings.mode != "Simple" ? getConditionConditionCount(app.otherConditions) : 0)
}

//cleans up conditions - this may be replaced by a complete rebuild of the app object from the settings
private cleanUpConditions(deleteGroups) {
	//go through each condition in the state config and delete it if no associated settings exist
    if (!state.config || !state.config.app) return
    _cleanUpCondition(state.config.app.conditions, deleteGroups)
    _cleanUpCondition(state.config.app.otherConditions, deleteGroups)
    cleanUpActions()
}

//helper function for _cleanUpConditions
private _cleanUpCondition(condition, deleteGroups) {
	def result = false

	if (condition.children) {
    	//we cannot use a for each due to concurrent modifications
        //we're using a while instead
        def deleted = true
        while (deleted) {
        	deleted = false
			for (def child in condition.children) {
            	deleted = _cleanUpCondition(child, deleteGroups)
	    		result = result || deleted
                if (deleted) {
                	break
                }
            }
		}
    }

	//if non-root condition
	if (condition.id > 0) {
    	if (condition.children == null) {
        	//if regular condition
        	if ((condition.cap != "Mode") && (condition.cap != "Location Mode") && (condition.cap != "Smart Home Monitor") && (condition.cap != "Date & Time") && (condition.cap != "Routine") && (condition.cap != "Variable") && settings["condDevices${condition.id}"] == null) {
	        	deleteCondition(condition.id);
	            return true
	        //} else {
            //	updateCondition(condition)
            }
        } else {
        	//if condition group
        	if (deleteGroups && (condition.children.size() == 0)) {
	        	deleteCondition(condition.id);
	            return true
	        }
        }
    }
    updateCondition(condition)
    return result
}

private getConditionDescription(id, level = 0) {
	def condition = getCondition(id)
    def pre = ""
    def preNot = ""
    def tab = ""
    def aft = ""
    def conditionGroup = (condition.children != null)
    switch (level) {
    	case 1:
        	pre = " ┌ ("
        	preNot = " ┌ NOT ("
        	tab = " │   "
        	aft = " └ )"
	        break;
        case 2:
    	    pre = " │ ┌ ["
    	    preNot = " │ ┌ NOT ["
        	tab = " │ │   "
       		aft = " │ └ ]"
        	break;
        case 3:
	        pre = " │ │ ┌ <"
	        preNot = " │ │ ┌ NOT {"
        	tab = " │ │ │   "
    	    aft = " │ │ └ >"
        	break;
    }
    if (!conditionGroup) {
    	//single condition
        if (condition.attr == "time") {
        	return getTimeConditionDescription(condition)
        }
        def capability = getCapabilityByDisplay(condition.cap)
        def virtualDevice = capability ? capability.virtualDevice : null
		def devices = virtualDevice ? null : settings["condDevices$id"]
        if (virtualDevice || (devices && devices.size())) {
            def evaluation = (virtualDevice ? "" : (devices.size() > 1 ? (condition.mode == "All" ? "Each of " : "Any of ") : ""))
            def deviceList = (virtualDevice ? (capability.virtualDeviceName ? capability.virtualDeviceName : virtualDevice.name) : buildDeviceNameList(devices, "or")) + " "
	        def attribute = condition.attr + " "
            def attr = getAttributeByName(condition.attr)
            def unit = (attr && attr.unit ? attr.unit : "")
            def comparison = cleanUpComparison(condition.comp)
            def comp = getComparisonOption(condition.attr, comparison)
            def subDevices = capability.count && attr && (attr.name == capability.attribute) ? buildNameList(condition.sdev, "or") + " " : ""
            def values = " [ERROR]"
            def time = ""
            if (comp) {
            	switch (comp.parameters) {
                	case 0:
                    	values = ""
                        break
                    case 1:
                    	def o1 = condition.o1 ? (condition.o1 < 0 ? " - " : " + ") + condition.o1.abs() : ""
                    	values = " ${(condition.var1 ? "{" + condition.var1 + o1 + "}$unit" : (condition.dev1 ? "{[" + condition.dev1 + "'s ${condition.attr1 ? condition.attr1 : attr.name}]" + o1 + "}$unit" : condition.val1 + unit))}"
                        break
                    case 2:
                    	def o1 = condition.o1 ? (condition.o1 < 0 ? " - " : " + ") + condition.o1.abs() : ""
                    	def o2 = condition.o2 ? (condition.o2 < 0 ? " - " : " + ") + condition.o2.abs() : ""
                    	values = " ${(condition.var1 ? "{" + condition.var1 + o1 + "}$unit" : (condition.dev1 ? "{[" + condition.dev1 + "'s ${condition.attr1 ? condition.attr1 : attr.name}]" + o1 + "}$unit" : condition.val1 + unit)) + " - " + (condition.var2 ? "{" + condition.var2 + o2 + "}$unit" : (condition.dev2 ? "{[" + condition.dev2 + "'s ${condition.attr2 ? condition.attr2 : attr.name}]" + o2 + "}$unit" : condition.val2 + unit))}"
                        break                       
            	}
                if (comp.timed) {
                   	time = " for [ERROR]"
                	if (comparison.contains("change")) {
                    	time = " in the last " + (condition.fort ? condition.fort : "[ERROR]")
                    } else if (condition.for && condition.fort) {
                		time = " " + condition.for + " " + condition.fort
                    }
                }
            }
            if (virtualDevice) {
            	attribute = ""
            }
            return tab + (condition.not ? "!" : "") + (condition.trg ? triggerPrefix() : conditionPrefix()) + evaluation + deviceList + attribute + subDevices + comparison + values + time
        }
        return "Sorry, incomplete rule"
	} else {
    	//condition group
        def grouping = condition.grp
        def negate = condition.not
        def result = (negate ? preNot : pre) + "\n"
        def cnt = 1
        for (child in condition.children) {
        	result += getConditionDescription(child.id, level + (child.children == null ? 0 : 1)) + "\n" + (cnt < condition.children.size() ? tab + grouping + "\n" : "")
            cnt++
        }
        result += aft
        return result
    }
}


private getTimeConditionDescription(condition) {
    if (condition.attr != "time") {
    	return "[ERROR]"
    }
    def attr = getAttributeByName(condition.attr)
    def comparison = cleanUpComparison(condition.comp)
    def comp = getComparisonOption(condition.attr, comparison)
    def result = (condition.trg ? triggerPrefix() + "Trigger " : conditionPrefix() + "Time ") + comparison
    def val1 = condition.val1 ? condition.val1 : ""
    def val2 = condition.val2 ? condition.val2 : ""
    if (attr && comp) {
    	//is the condition a trigger?
    	def trigger = (comp.trigger == comparison)
        def repeating = trigger
    	for (def i = 1; i <= comp.parameters; i++) {
        	def val = (i == 1 ? val1 : val2)
            def recurring = false
            def preciseTime = false
            
            if (val.contains("custom")) {
                //custom time
                val = formatTime(i == 1 ? condition.t1 : condition.t2)
                preciseTime = true
                //def hour = condition.t1.getHour()
                //def minute = condition.t2.getMinute()
            } else if (val.contains("every")) {
            	recurring = true
                repeating = false
                //take out the "happens at" and replace it with "happens "... every [something]
                result = result.replace("happens at", "happens")
            	if (val.contains("number")) {
                	//multiple minutes or hours
                	val = "every ${condition.e} ${val.contains("minute") ? "minutes" : "hours"}"
                } else {
                	//one minute or one hour
                    //no change to val
				}
            } else {
                //simple, no change to val
            }
            
            if (comparison.contains("around")) {
            	def range = i == 1 ? condition.o1 : condition.o2
            	val += " ± $range minute${range > 1 ? "s" : ""}"
            } else {
            	if ((!preciseTime) && (!recurring)) {
	            	def offset = i == 1 ? condition.o1 : condition.o2
                    if (offset == null) { 
                    	offset = 0
                    }
                    def after = offset >= 0
                    offset = offset.abs()
                    if (offset != 0) {
                        result = result.replace("happens at", "happens")
                        val = "${offset} minute${offset > 1 ? "s" : ""} ${after ? "after" : "before"} $val"
                    }
                }
            }
            
            if (i == 1) {
            	val1 = val
           	} else {
            	val2 = val
            }
            
		}
        
        switch (comp.parameters) {
        	case 1:
            	result += " $val1"
                break
            case 2:
            	result += " $val1 and $val2"
                break
        }
        
        //repeat options
        if (repeating) {
            def repeat = condition.r
            if (repeat) {
            	if (repeat.contains("day")) {
                	//every day
                    //every N days
                	if (repeat.contains("number")) {
	                    result += ", ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("days", condition.re > 2 ? "days" : "day")}"
	                } else {
	                    result += ", $repeat"
	                }
				}
            	if (repeat.contains("week")) {
                	//every day
                    //every N days
                    def dow = condition.rdw ? condition.rdw : "[ERROR]"
                	if (repeat.contains("number")) {
	                    result += ", ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("weeks", condition.re > 2 ? "weeks" : "week").replace("week", "${dow}")}"
	                } else {
	                    result += ", every $dow"
	                }
				}
                if (repeat.contains("month")) {
                	//every Nth of the month
                    //every Nth of every N months
                    //every first/second/last [dayofweek] of the month
                    //every first/second/last [dayofweek] of every N months
                	if (repeat.contains("number")) {
	                    result += ", " + formatDayOfMonth(condition.rd, condition.rdw) + " of ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("months", condition.re > 2 ? "months" : "month")}"
	                } else {
	                    result += ", " + formatDayOfMonth(condition.rd, condition.rdw).replace("the", "every")
	                }
                }
                if (repeat.contains("year")) {
                	//oh boy, we got years too!
                    def month = condition.rm ? condition.rm : "[ERROR]"
                	if (repeat.contains("number")) {                    
	                    result += ", " + formatDayOfMonth(condition.rd, condition.rdw) + " of ${month} of ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("years", condition.re > 2 ? "years" : "year")}"
	                } else {
	                    result += ", " + formatDayOfMonth(condition.rd, condition.rdw).replace("the", "every") + " of ${month}"
	                }                    
                }
            } else {
                result += " [REPEAT INCOMPLETE]"
            }
        }
        
        //filters
		if (condition.fmh || condition.fhd || condition.fdw || condition.fdm || condition.fwm || condition.fmy || condition.fy) {
        	//we have some filters
            /*
                condition.fmh = settings["condMOH${condition.id}"]
                condition.fhd = settings["condHOD${condition.id}"]
                condition.fdw = settings["condDOW${condition.id}"]
                condition.fdm = settings["condDOM${condition.id}"]
                condition.fmy = settings["condMOY${condition.id}"]
                condition.fy = settings["condY${condition.id}"]
			*/
            result += ", but only if"
            def i = 0
            if (condition.fmh) {
            	result += "${i > 0 ? ", and" : ""} the minute is ${buildNameList(condition.fmh, "or")}"
                i++
            }
            if (condition.fhd) {
            	result += "${i > 0 ? ", and" : ""} the hour is ${buildNameList(condition.fhd, "or")}"
                i++
            }
            if (condition.fdw) {
            	result += "${i > 0 ? ", and" : ""} the day of the week is ${buildNameList(condition.fdw, "or")}"
                i++
            }
            if (condition.fwm) {
            	result += "${i > 0 ? ", and" : ""} the week is ${buildNameList(condition.fwm, "or")} of the month"
                i++
            }
            if (condition.fdm) {
            	result += "${i > 0 ? ", and" : ""} the day is ${buildNameList(condition.fdm, "or")} of the month"
                i++
            }
            if (condition.fmy) {
            	result += "${i > 0 ? ", and" : ""} the month is ${buildNameList(condition.fmy, "or")}"
                i++
            }
            if (condition.fy) {
            	def odd = "odd years" in condition.fy
            	def even = "even years" in condition.fy
               	def leap = "leap years" in condition.fy
                def list = []
                //if we have both odd and even selected, that would match all years, so get out                
                if (!(even && odd)) {
                    if (odd || even || leap) {   
                        if (odd) list.push("odd")
                        if (even) list.push("even")
                        if (leap) list.push("leap")
                    }
				}
                for(year in condition.fy) {
                	if (!year.contains("year")) {
                    	list.push(year)
                    }
                }
                if (list.size()) {
                	result += "${i > 0 ? ", and" : ""} the year is ${buildNameList(list, "or")}"
                }
            }
            
        }
        
        
    }
	return result
}





/******************************************************************************/
/*** ACTION FUNCTIONS														***/
/******************************************************************************/

def getAction(actionId) {
    def parent = (state.run == "config" ? state.config : state)
	for(action in parent.app.actions) {
    	if (action.id == actionId) {
        	return action
        }
    }
    return null
}

def listActions(conditionId) {
	def result = []
    def parent = (state.run == "config" ? state.config : state)

	for(action in parent.app.actions) {
    	if ((action.pid == conditionId)) {
        	result.push(action)
        }
    }    
    return result
}

def getActionTask(action, taskId) {
    if (!action) return null
    if (!(taskId instanceof Integer)) return null
    for (task in action.t) {
    	if (task.i == taskId) {
        	return task
        }
    }
    return null
}

/******************************************************************************/
/*** OTHER FUNCTIONS														***/
/******************************************************************************/

private sanitizeVariableName(name) {
	name = name ? "$name".trim().replace(" ", "_") : null
}

def dummy() {
	//evaluates a condition
    def perf = now()
	debug "Entering dummy()", 1
    //using this for development
   
//    scheduleTimeTriggers()
    
//    publishTasks()
    
    perf = now() - perf
    debug "Exiting dummy() after ${perf}ms", -1

}

private cleanUpMap(map) {
	def dirty = true
    while (dirty) {
    	dirty = false
        //we need to break the loop every time we removed an item
        for(item in map) {
        	if (item.value == null) {
            	map.remove(item.key)
                dirty = true
                break
            }
        }
    }
	return map
}

private cleanUpAttribute(attribute) {
	if (attribute) {
		return attribute.replace(customAttributePrefix(), "")
    }
    return null
}

private cleanUpCommand(command) {
	if (command) {
		return command.replace(customCommandPrefix(), "").replace(virtualCommandPrefix(), "").replace(customCommandSuffix(), "")
    }
    return null
}

private cleanUpComparison(comparison) {
	if (comparison) {
		return comparison.replace(triggerPrefix(), "").replace(conditionPrefix(), "")
    }
    return null
}

private buildDeviceNameList(devices, suffix) {
	def cnt = 1
    def result = ""
	for (device in devices) {
    	def label = getDeviceLabel(device)
        result += "$label" + (cnt < devices.size() ? (cnt == devices.size() - 1 ? " $suffix " : ", ") : "")
        cnt++
    }
    return result;
}

private buildNameList(list, suffix) {
	def cnt = 1
    def result = ""
	for (item in list) {
        result += item + (cnt < list.size() ? (cnt == list.size() - 1 ? "${list.size() > 2 ? "," : ""} $suffix " : ", ") : "")
        cnt++
    }
    return result;
}

private getDeviceLabel(device) {
	return device instanceof String ? device : (device ? ( device.label ? device.label : (device.name ? device.name : "$device")) : "Unknown device")
}

private getAlarmSystemStatus() {
	switch (location.currentState("alarmSystemStatus")?.value) {
    	case "off":
        	return getAlarmSystemStatusOptions()[0]
    	case "stay":
        	return getAlarmSystemStatusOptions()[1]
    	case "away":
        	return getAlarmSystemStatusOptions()[2]
    }
    return null
}

private setAlarmSystemStatus(status) {
	def value = null
    def options = getAlarmSystemStatusOptions()
	switch (status) {
    	case options[0]:
        	value = "off"
            break
    	case options[1]:
        	value = "stay"
            break
    	case options[2]:
        	value = "away"
            break
    }
    if (value && (value != location.currentState("alarmSystemStatus")?.value)) {
		sendLocationEvent(name: 'alarmSystemStatus', value: value)
        return true
    }
    debug "WARNING: Could not set SHM status to '$status' because that status does not exist.", null, "warn"
    return false
}

private formatMessage(message, params = null) {
	if (!message) {
    	return message
    }
    def variables = message.findAll(/\{([^\{\}]*)?\}*/)
    def varMap = [:]
    for (variable in variables) {
        if (!(variable in varMap)) {
        	def var = variable.replace("{", "").replace("}", "")
            def idx = var.isInteger() ? var.toInteger() : null
            def value = ""
            if (params && (idx >= 0) && (idx < params.size())) {
            	value = "${params[idx].d ? params[idx].d : params[idx]}"            	
            } else {
	            value = getVariable(var, true)
            }
           	varMap[variable] = value
        }
    }
    for(var in varMap) {
    	if (var.value) {
			message = message.replace(var.key, "${var.value}")
        }
    }
    return message
}






/******************************************************************************/
/*** DATABASE FUNCTIONS														***/
/******************************************************************************/
//returns a list of all available capabilities
private listCapabilities(requireAttributes, requireCommands) {
    def result = []
    for (capability in capabilities()) {
    	if ((requireAttributes && capability.attribute) || (requireCommands && capability.commands) || !(requireAttributes || requireCommands)) {
	    	result.push(capability.display)
		}
    }
    return result
}

//returns a list of all available attributes
private listAttributes() {
    def result = []
    for (attribute in attributes()) {
    	result.push(attribute.name)
    }
    return result.sort()
}

//returns a list of possible comparison options for a selected attribute
private listComparisonOptions(attributeName, allowTriggers, overrideAttributeType = null) {
    def conditions = []
    def triggers = []
    def attribute = getAttributeByName(attributeName)
    def allowTimedComparisons = !(attributeName in ["mode", "alarmSystemStatus", "routineExecuted", "variable"])
    if (attribute) {
    	def optionCount = attribute.options ? attribute.options.size() : 0
        def attributeType = overrideAttributeType ? overrideAttributeType : attribute.type
        for (comparison in comparisons()) {
            if (comparison.type == attributeType) {
                for (option in comparison.options) {
                    if (option.condition && (!option.minOptions || option.minOptions <= optionCount) && (allowTimedComparisons || !option.timed)) {                    
                        conditions.push(conditionPrefix() + option.condition)
                    }
                    if (allowTriggers && option.trigger && (!option.minOptions || option.minOptions <= optionCount) && (allowTimedComparisons || !option.timed)) {
                        triggers.push(triggerPrefix() + option.trigger)
                    }
                }
            }
        }
    }    
    return conditions.sort() + triggers.sort()
}

//returns the comparison option object for the given attribute and selected comparison
private getComparisonOption(attributeName, comparisonOption, overrideAttributeType = null) {	
    def attribute = getAttributeByName(attributeName)
    if (attribute && comparisonOption) {
		def attributeType = overrideAttributeType ? overrideAttributeType : (attributeName == "variable" ? "variable" : attribute.type)
        for (comparison in comparisons()) {
            if (comparison.type == attributeType) {
                for (option in comparison.options) {
                    if (option.condition == comparisonOption) {
                        return option
                    }
                    if (option.trigger == comparisonOption) {
                    	return option
                    }
                }
            }
        }
    }
    return null	
}

//returns true if the comparisonOption selected for the given attribute is a trigger-type condition
private isComparisonOptionTrigger(attributeName, comparisonOption) {
    def attribute = getAttributeByName(attributeName)
    if (attribute) {
		def attributeType = attribute.type
        for (comparison in comparisons()) {
            if (comparison.type == attributeType) {
                for (option in comparison.options) {
                    if (option.condition == comparisonOption) {
                        return false
                    }
                    if (option.trigger == comparisonOption) {
                    	return true
                    }
                }
            }
        }
    }
    return false	
}

//returns the list of attributes that exist for all devices in the provided list
private listCommonDeviceAttributes(devices) {
	def list = [:]
    def customList = [:]
    //build the list of standard attributes
	for (attribute in attributes()) {
    	if (attribute.name.contains("*")) {
        	for (def i = 1; i <= 32; i++) {
            	list[attribute.name.replace("*", "$i")] = 0
            }
        } else {
    		list[attribute.name] = 0
        }
    }
	//get supported attributes
    for (device in devices) {
    	def attrs = device.supportedAttributes
        for (attr in attrs) {        	
        	if (list.containsKey(attr.name)) {
            	//if attribute exists in standard list, increment its usage count
	       		list[attr.name] = list[attr.name] + 1
            } else {
            	//otherwise increment the usage count in the custom list
	       		customList[attr.name] = customList[attr.name] ? customList[attr.name] + 1 : 1
            }
        }
    }
    def result = []
    //get all common attributes from the standard list
    for (item in list) {
    	//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
    	if (item.value >= devices.size()) {
        	result.push(item.key)
        }
    }
    //get all common attributes from the custom list
    for (item in customList) {
    	//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
    	if (item.value >= devices.size()) {
        	result.push(customAttributePrefix() + item.key)
        }
    }
    //return the sorted list
    return result.sort()
}


private listCommonDeviceSubDevices(devices, countAttribute, prefix = "") {
	def result = []
    def subDeviceCount = null
    def hasMainSubDevice = false
    //get supported attributes
    for (device in devices) {
    	def cnt = 4
        switch (device.name) {
        	case "Aeon Minimote":
            case "Aeon Key Fob":
        	case "Simulated Minimote":
            	cnt = 4
                break        	
        }
        /*
        if (countAttribute.contains("*")) {
        	//we're looking for a repeated attribute, like switch1, switch2, switch3, switch4, etc.
            def attrPrefix = countAttribute.tokenize("*")[0]
            hasMainSubDevice = device.hasAttribute(attrPrefix)
            def attrs = device.supportedAttributes
            cnt = 0
            for (def attr in attrs) {
            	if (attr.startsWith(attrPrefix)) {
                	def c = cast(attr.replace(attrPrefix, ""), "number")
                    cnt = cnt < c ? c : cnt
                }
            }
        } else {
        */
        if (device.hasAttribute(countAttribute)) {
            def c = cast(device.currentValue(countAttribute), "number")
            cnt = c ? c : cnt
        }
        if (cnt instanceof String) {
        	cnt = cnt.isInteger() ? cnt.toInteger() : 0
        }
        if (cnt instanceof Integer) {
            subDeviceCount = (subDeviceCount == null) || (cnt < subDeviceCount) ? (int) cnt : subDeviceCount
        }
    }
    if (subDeviceCount >= 2) {
    	if (hasMainSubDevice) {
        	result.push "Main ${prefix.toLowerCase()}"
        }
    	for(def i = 1; i <= subDeviceCount; i++) {
        	result.push "$prefix #$i".trim()
        }
    }
    //return the sorted list
    return result
}

private listCommonDeviceCommands(devices, capabilities) {
	def list = [:]
    def customList = [:]
    //build the list of standard attributes
	for (command in commands()) {
    	list[command.name] = 0
    }
	//get supported attributes
    for (device in devices) {
    	def cmds = device.supportedCommands
        for (cmd in cmds) {
        	def found = false
            for (capability in capabilities) {
            	def name = capability + "." + cmd.name
                if (list.containsKey(name)) {
                    //if attribute exists in standard list, increment its usage count
                    list[name] = list[name] + 1
                    found = true
                } else {
                	name = name.replaceAll("[\\d]", "") + "*"
                    if (list.containsKey(name)) {
	                    list[name] = list[name] + 1
    	                found = true
                    }
                }
            }
        	if (!found && list.containsKey(cmd.name)) {
            	//if attribute exists in standard list, increment its usage count
	       		list[cmd.name] = list[cmd.name] + 1
                found = true
            }
            if (!found) {
            	//otherwise increment the usage count in the custom list
	       		customList[cmd.name] = customList[cmd.name] ? customList[cmd.name] + 1 : 1
            }
        }
    }
    
    def result = []
    //get all common attributes from the standard list
    for (item in list) {
    	//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
    	if (item.value >= devices.size()) {
        	def command = getCommandByName(item.key)
            if (command && command.display) {
        		result.push(command.display)
            }
        }
    }
    //get all common attributes from the custom list
    for (item in customList) {
    	//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
    	if (item.value >= devices.size()) {
        	result.push(customCommandPrefix() + item.key + customCommandSuffix())
        }
    }
    //return the sorted list
    return result.sort()
}

private getCapabilityByName(name) {
    for (capability in capabilities()) {
    	if (capability.name == name) {
        	return capability
        }
    }
    return null
}

private getCapabilityByDisplay(display) {
    for (capability in capabilities()) {
    	if (capability.display == display) {
        	return capability
        }
    }
    return null
}

private getAttributeByName(name) {
	def name2 = name instanceof String ? name.replaceAll("[\\d]", "").trim() + "*" : null
    for (attribute in attributes()) {
    	if ((attribute.name == name) || (name2 && (attribute.name == name2))) {
        	return attribute
        }
    }
    return [ name: name, type: "text", range: null, unit: null, options: null]
}

//returns all available command categories
private listCommandCategories() {
	def categories = []
	for(def command in commands()) {
    	if (command.category && !(command.category in categories)) {
        	categories.push(command.category)
        }
    }
    return categories
}

//returns all available commands in a category
private listCategoryCommands(category) {
	def result = []
    for(def command in commands()) {
    	if ((command.category == category) && !(command.name in result)) {
        	result.push(command)
        }
    }
    return result
}

//gets a category and command and returns the user friendly display name
private getCommand(category, name) {
    for(def command in commands()) {
    	if ((command.category == category) && (command.name == name)) {
        	return command
        }
    }
    return null
}

private getCommandByName(name) {
    for(def command in commands()) {
    	if (command.name == name) {
        	return command
        }
    }
    return null
}

private getVirtualCommandByName(name) {
    for(def command in virtualCommands()) {
    	if (command.name == name) {
        	return command
        }
    }
    return null
}

private getCommandByDisplay(display) {
    for(def command in commands()) {
    	if (command.display == display) {
        	return command
        }
    }
    return null
}

private getVirtualCommandByDisplay(display) {
    for(def command in virtualCommands()) {
    	if (command.display == display) {
        	return command
        }
    }
    return null
}

//gets a category and command and returns the user friendly display name
private getCommandGroupName(category, name) {
    def command = getCommand(category, name)
    return getCommandGroupName(command)
}

private getCommandGroupName(command) {
	if (!command) {
    	return null
    }
    if (!command.group) {
    	return null
    }
    if (command.group.contains("[devices]")) {
        def list = []
        for (capability in listCommandCapabilities(command)) {
        	if ((capability.devices) && !(capability.devices in list)){
            	list.push(capability.devices)
            }
        }
        return command.group.replace("[devices]", buildNameList(list, "or"))
	} else {
    	return command.group
    }
}


//gets a category and command and returns the user friendly display name
private listCommandCapabilities(command) {
	//first off, find all commands that are capability-custom (i.e. name is of format <capability>.<name>)
    //we need to exclude these capabilities
    //if our name is of form <capability>.<name>
    if (command.name.contains(".")) {
    	//easy, we only have one capability
        def cap = getCapabilityByName(command.name.tokenize(".")[0])
        if (!cap) {
        	return []
        }
        return [cap]
    }
    def excludeList = []
    for(def c in commands()) {
    	if (c.name.endsWith(".${command.name}")) {
        	//get the capability and add it to an exclude list
        	excludeList.push(c.name.tokenize(".")[0])
        }
    }
    //now get the capability names
    def result = []
    for(def c in capabilities()) {
    	if (!(c.name in excludeList) && c.commands && (command.name in c.commands) && !(c in result)) {
        	result.push(c)
        }
    }
    return result
}

private parseCommandParameter(parameter) {
	if (!parameter) {
    	return null
    }
    
    def required = !(parameter && parameter.startsWith("?"))
    if (!required) {
    	parameter = parameter.substring(1)
    }

    def last = (parameter && parameter.startsWith("*"))
    if (last) {
    	parameter = parameter.substring(1)
    }

    //split by :
	def tokens = parameter.tokenize(":")
    if (tokens.size() < 2) {
    	return [title: tokens[0], type: "text", required: required, last: last]
    }
    def title = ""
    def dataType = ""
    if (tokens.size() == 2) {
    	title = tokens[0]
        dataType = tokens[1]
	} else {
    	//title contains at least one :, so we rebuild it
    	for(def i=0; i < tokens.size() - 1; i++) {
        	title += (title ? ":" : "") + tokens[i]
        }
        dataType = tokens[tokens.size() - 1]
    }

    if (dataType in ["attribute", "attributes", "variable", "variables", "routine", "aggregation", "dataType"]) {
    	//special case handled internally
        return [title: title, type: dataType, required: required, last: last]
    }
    
  
    //at this point, let's check if we're dealing with a custom data type
    //we accept
    // string
    // text
    // number
    // number[range]
    // decimal
    // decimal[range]
    // enum[comma separated list, double quotes not required]
    
    tokens = dataType.tokenize("[]")
    if (tokens.size()) {
    	dataType = tokens[0]
        switch (tokens.size()) {
            case 1:
            	switch (dataType) {
                	case "string":
                	case "text":
                    	return [title: title, type: "text", required: required, last: last]
                	case "bool":
                	case "email":
                	case "time":
                	case "phone":
                	case "contact":
                	case "number":
                	case "decimal":
                    case "var":
                    	return [title: title, type: dataType, required: required, last: last]
                	case "color":
                    	return [title: title, type: "enum", options: colorOptions(), required: required, last: last]
                }
                break
            case 2:
            	switch (dataType) {
                	case "string":
                	case "text":
                    	return [title: title, type: "text", required: required, last: last]
                	case "bool":
                	case "email":
                	case "time":
                	case "phone":
                	case "contact":
                	case "number":
                	case "decimal":
                    	return [title: title, type: dataType, range: tokens[1], required: required, last: last]
                	case "enum":
                    	return [title: title, type: dataType, options: tokens[1].tokenize(","), required: required, last: last]
                }
                break
        }
	}
    
    //check to see if dataType is an attribute, we use the attribute declaration then
    def attr = getAttributeByName(dataType)
    if (attr) {
    	return [title: title + (attr.unit ? " (${attr.unit})" : ""), type: attr.type, range: attr.range, options: attr.options, required: required, last: last]
    }
    
    //give up
    return null
}






/******************************************************************************/
/*** DATABASE																***/
/******************************************************************************/

private capabilities() {
	return [
    	[ name: "accelerationSensor",				display: "Acceleration Sensor",				attribute: "acceleration",				commands: null,																		multiple: true,			devices: "acceleration sensors",	],
    	[ name: "alarm",							display: "Alarm",							attribute: "alarm",						commands: ["off", "strobe", "siren", "both"],										multiple: true,			devices: "sirens",			],
    	[ name: "doorControl",						display: "Automatic Door",					attribute: "door",						commands: ["open", "close"],														multiple: true,			devices: "doors",			],
    	[ name: "garageDoorControl",				display: "Automatic Garage Door",			attribute: "door",						commands: ["open", "close"],														multiple: true,			devices: "garage doors",		],
        [ name: "battery",							display: "Battery",							attribute: "battery",					commands: null,																		multiple: true,			devices: "battery powered devices",	],
    	[ name: "beacon",							display: "Beacon",							attribute: "presence",					commands: null,																		multiple: true,			devices: "beacons",	],
    	[ name: "switch",							display: "Bulb",							attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "lights", 			],
        [ name: "button",							display: "Button",							attribute: "button",					commands: null,																		multiple: true,			devices: "buttons",			count: "numberOfButtons", data: "buttonNumber", momentary: true],
        [ name: "imageCapture",						display: "Camera",							attribute: "image",						commands: ["take"],																	multiple: true,			devices: "cameras",			],
    	[ name: "carbonDioxideMeasurement",			display: "Carbon Dioxide Measurement",		attribute: "carbonDioxide",				commands: null,																		multiple: true,			devices: "carbon dioxide sensors",	],
        [ name: "carbonMonoxideDetector",			display: "Carbon Monoxide Detector",		attribute: "carbonMonoxide",			commands: null,																		multiple: true,			devices: "carbon monoxide detectors",	],
    	[ name: "colorControl",						display: "Color Control",					attribute: "color",						commands: ["setColor", "setHue", "setSaturation"],									multiple: true,			devices: "RGB/W lights"		],
        [ name: "colorTemperature",					display: "Color Temperature",				attribute: "colorTemperature",			commands: ["setColorTemperature"],													multiple: true,			devices: "RGB/W lights",	],
    	[ name: "configure",						display: "Configure",						attribute: null,						commands: ["configure"],															multiple: true,			devices: "configurable devices",	],
    	[ name: "consumable",						display: "Consumable",						attribute: "consumable",				commands: ["setConsumableStatus"],													multiple: true,			devices: "consumables",	],
		[ name: "contactSensor",					display: "Contact Sensor",					attribute: "contact",					commands: null,																		multiple: true,			devices: "contact sensors",	],
    	[ name: "dateAndTime",						display: "Date & Time",						attribute: "time",						commands: null, /* wish we could control time */									multiple: true,			, virtualDevice: [id: "time", name: "time"],		virtualDeviceName: "Date & Time"	],
    	[ name: "switchLevel",						display: "Dimmable Light",					attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmable lights",	],
    	[ name: "switchLevel",						display: "Dimmer",							attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmers",			],
    	[ name: "energyMeter",						display: "Energy Meter",					attribute: "energy",					commands: null,																		multiple: true,			devices: "energy meters"],
        [ name: "illuminanceMeasurement",			display: "Illuminance Measurement",			attribute: "illuminance",				commands: null,																		multiple: true,			devices: "illuminance sensors",	],
        [ name: "imageCapture",						display: "Image Capture",					attribute: "image",						commands: ["take"],																	multiple: true,			devices: "cameras"],
    	[ name: "waterSensor",						display: "Leak Sensor",						attribute: "water",						commands: null,																		multiple: true,			devices: "leak sensors",	],
    	[ name: "switch",							display: "Light bulb",						attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "lights", 			],
        [ name: "locationMode",						display: "Location Mode",					attribute: "mode",						commands: ["setMode"],																multiple: false,		devices: "location", virtualDevice: location	],
        [ name: "lock",								display: "Lock",							attribute: "lock",						commands: ["lock", "unlock"],														multiple: true,			devices: "electronic locks", ],
    	[ name: "mediaController",					display: "Media Controller",				attribute: "currentActivity",			commands: ["startActivity", "getAllActivities", "getCurrentActivity"],				multiple: true,			devices: "media controllers"],
        [ name: "locationMode",						display: "Mode",							attribute: "mode",						commands: ["setMode"],																multiple: false,		devices: "location", virtualDevice: location	],
    	[ name: "momentary",						display: "Momentary",						attribute: null,						commands: ["push"],																	multiple: true,			devices: "momentary switches"],
    	[ name: "motionSensor",						display: "Motion Sensor",					attribute: "motion",					commands: null,																		multiple: true,			devices: "motion sensors",	],
    	[ name: "musicPlayer",						display: "Music Player",					attribute: "status",					commands: ["play", "pause", "stop", "nextTrack", "playTrack", "setLevel", "playText", "mute", "previousTrack", "unmute", "setTrack", "resumeTrack", "restoreTrack"],	multiple: true,			devices: "music players", ],
    	[ name: "notification",						display: "Notification",					attribute: null,						commands: ["deviceNotification"],													multiple: true,			devices: "notification devices",	],
    	[ name: "pHMeasurement",					display: "pH Measurement",					attribute: "pH",						commands: null,																		multiple: true,			devices: "pH sensors",	],
    	[ name: "switch",							display: "Outlet",							attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "outlets",			],
    	[ name: "polling",							display: "Polling",							attribute: null,						commands: ["poll"],																	multiple: true,			devices: "pollable devices",	],
        [ name: "powerMeter",						display: "Power Meter",						attribute: "power",						commands: null,																		multiple: true,			devices: "power meters",	],
        [ name: "power",							display: "Power",							attribute: "powerSource",				commands: null,																		multiple: true,			devices: "powered devices",	],
    	[ name: "presenceSensor",					display: "Presence Sensor",					attribute: "presence",					commands: null,																		multiple: true,			devices: "presence sensors",	],
    	[ name: "refresh",							display: "Refresh",							attribute: null,						commands: ["refresh"],																multiple: true,			devices: "refreshable devices",	],
    	[ name: "relativeHumidityMeasurement",		display: "Relative Humidity Measurement",	attribute: "humidity",					commands: null,																		multiple: true,			devices: "humidity sensors",	],
    	[ name: "relaySwitch",						display: "Relay Switch",					attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "relays",			],
    	[ name: "routine",							display: "Routine",							attribute: "routineExecuted",			commands: ["executeRoutine"],														multiple: true,			virtualDevice: location,	virtualDeviceName: "Routine"	],
    	[ name: "shockSensor",						display: "Shock Sensor",					attribute: "shock",						commands: null,																		multiple: true,			devices: "shock sensors",	],
    	[ name: "signalStrength",					display: "Signal Strength",					attribute: "lqi",						commands: null,																		multiple: true,			devices: "wireless devices",	],
    	[ name: "alarm",							display: "Siren",							attribute: "alarm",						commands: ["off", "strobe", "siren", "both"],										multiple: true,			devices: "sirens",			],
    	[ name: "sleepSensor",						display: "Sleep Sensor",					attribute: "sleeping",					commands: null,																		multiple: true,			devices: "sleep sensors",	],
    	[ name: "smartHomeMonitor",					display: "Smart Home Monitor",				attribute: "alarmSystemStatus",			commands: ["setAlarmSystemStatus"],																		multiple: true,			, virtualDevice: location,	virtualDeviceName: "Smart Home Monitor"	],
    	[ name: "smokeDetector",					display: "Smoke Detector",					attribute: "smoke",						commands: null,																		multiple: true,			devices: "smoke detectors",	],
        [ name: "soundSensor",						display: "Sound Sensor",					attribute: "sound",						commands: null,																		multiple: true,			devices: "sound sensors",	],
    	[ name: "speechSynthesis",					display: "Speech Synthesis",				attribute: null,						commands: ["speak"],																multiple: true,			devices: "speech synthesizers", ],
        [ name: "stepSensor",						display: "Step Sensor",						attribute: "steps",						commands: null,																		multiple: true,			devices: "step sensors",	],
    	[ name: "switch",							display: "Switch",							attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "switches",			],
    	[ name: "switchLevel",						display: "Switch Level",					attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmers" ],
        [ name: "soundPressureLevel",				display: "Sound Pressure Level",			attribute: "soundPressureLevel",		commands: null,																		multiple: true,			devices: "sound pressure sensors",	],
    	[ name: "consumable",						display: "Stock Management",				attribute: "consumable",				commands: null,																		multiple: true,			devices: "consumables",	],
    	[ name: "tamperAlert",						display: "Tamper Alert",					attribute: "tamper",					commands: null,																		multiple: true,			devices: "tamper sensors",	],
    	[ name: "temperatureMeasurement",			display: "Temperature Measurement",			attribute: "temperature",				commands: null,																		multiple: true,			devices: "temperature sensors",	],
        [ name: "thermostat",						display: "Thermostat",						attribute: "temperature",				commands: ["setHeatingSetpoint", "setCoolingSetpoint", "off", "heat", "emergencyHeat", "cool", "setThermostatMode", "fanOn", "fanAuto", "fanCirculate", "setThermostatFanMode", "auto"],	multiple: true,		devices: "thermostats"	],
        [ name: "thermostatCoolingSetpoint",		display: "Thermostat Cooling Setpoint",		attribute: "coolingSetpoint",			commands: ["setCoolingSetpoint"],													multiple: true,			],
    	[ name: "thermostatFanMode",				display: "Thermostat Fan Mode",				attribute: "thermostatFanMode",			commands: ["fanOn", "fanAuto", "fanCirculate", "setThermostatFanMode"],				multiple: true,			devices: "fans",	],
    	[ name: "thermostatHeatingSetpoint",		display: "Thermostat Heating Setpoint",		attribute: "heatingSetpoint",			commands: ["setHeatingSetpoint"],													multiple: true,			],
    	[ name: "thermostatMode",					display: "Thermostat Mode",					attribute: "thermostatMode",			commands: ["off", "heat", "emergencyHeat", "cool", "auto", "setThermostatMode"],	multiple: true,			],
    	[ name: "thermostatOperatingState",			display: "Thermostat Operating State",		attribute: "thermostatOperatingState",	commands: null,																		multiple: true,			],
    	[ name: "thermostatSetpoint",				display: "Thermostat Setpoint",				attribute: "thermostatSetpoint",		commands: null,																		multiple: true,			],
    	[ name: "threeAxis",						display: "Three Axis Sensor",				attribute: "threeAxis",					commands: null,																		multiple: true,			devices: "three axis sensors",	],
    	[ name: "timedSession",						display: "Timed Session",					attribute: "sessionStatus",				commands: ["setTimeRemaining", "start", "stop", "pause", "cancel"],					multiple: true,			devices: "timed sessions"],
    	[ name: "tone",								display: "Tone Generator",					attribute: null,						commands: ["beep"],																	multiple: true,			devices: "tone generators",	],
    	[ name: "touchSensor",						display: "Touch Sensor",					attribute: "touch",						commands: null,																		multiple: true,			],
    	[ name: "valve",							display: "Valve",							attribute: "contact",					commands: ["open", "close"],														multiple: true,			devices: "valves",			],
    	[ name: "variable",							display: "Variable",						attribute: "variable",					commands: ["setVariable"],															multiple: true,			virtualDevice: location,	virtualDeviceName: "Variable"	],
    	[ name: "voltageMeasurement",				display: "Voltage Measurement",				attribute: "voltage",					commands: null,																		multiple: true,			devices: "volt meters",	],
    	[ name: "waterSensor",						display: "Water Sensor",					attribute: "water",						commands: null,																		multiple: true,			devices: "leak sensors",	],
        [ name: "windowShade",						display: "Window Shade",					attribute: "windowShade",				commands: ["open", "close", "presetPosition"],										multiple: true,			devices: "window shades",	],
    ]
}

private commands() {
	def tempUnit = "°" + location.temperatureScale
	return [
        [ name: "locationMode.setMode",						category: "Location",					group: "Control location mode, Smart Home Monitor and more",		display: "Set location mode",			parameters: [], ],
        [ name: "smartHomeMonitor.setAlarmSystemStatus",	category: "Location",					group: "Control location mode, Smart Home Monitor and more",		display: "Set Smart Home Monitor status",parameters: [], ],
    	[ name: "on",										category: "Convenience",				group: "Control [devices]",			display: "Turn on", 					parameters: [], 	attribute: "switch",	value: "on",	],
        [ name: "on1",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #1", 					parameters: [], 	attribute: "switch1",	value: "on",	],
    	[ name: "on2",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #2", 					parameters: [], 	attribute: "switch2",	value: "on",	],
    	[ name: "on3",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #3", 					parameters: [], 	attribute: "switch3",	value: "on",	],
    	[ name: "on4",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #4", 					parameters: [], 	attribute: "switch4",	value: "on",	],
    	[ name: "on5",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #5", 					parameters: [], 	attribute: "switch5",	value: "on",	],
    	[ name: "on6",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #6", 					parameters: [], 	attribute: "switch6",	value: "on",	],
    	[ name: "on7",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #7", 					parameters: [], 	attribute: "switch7",	value: "on",	],
    	[ name: "on8",										category: "Convenience",				group: "Control [devices]",			display: "Turn on #8", 					parameters: [], 	attribute: "switch8",	value: "on",	],
    	[ name: "off",										category: "Convenience",				group: "Control [devices]",			display: "Turn off",					parameters: [], 	attribute: "switch",	value: "off",	],
    	[ name: "off1",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #1",					parameters: [], 	attribute: "switch1",	value: "off",	],
    	[ name: "off2",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #2",					parameters: [], 	attribute: "switch2",	value: "off",	],
    	[ name: "off3",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #3",					parameters: [], 	attribute: "switch3",	value: "off",	],
    	[ name: "off4",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #4",					parameters: [], 	attribute: "switch4",	value: "off",	],
    	[ name: "off5",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #5",					parameters: [], 	attribute: "switch5",	value: "off",	],
    	[ name: "off6",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #6",					parameters: [], 	attribute: "switch6",	value: "off",	],
    	[ name: "off7",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #7",					parameters: [], 	attribute: "switch7",	value: "off",	],
    	[ name: "off8",										category: "Convenience",				group: "Control [devices]",			display: "Turn off #8",					parameters: [], 	attribute: "switch8",	value: "off",	],
    	[ name: "toggle",									category: "Convenience",				group: null,						display: "Toggle",						parameters: [], 	],
    	[ name: "toggle1",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle2",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle3",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle4",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle5",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle6",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle7",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "toggle8",									category: "Convenience",				group: null,						display: "Toggle #1",						parameters: [], ],
    	[ name: "setLevel",									category: "Convenience",				group: "Control [devices]",			display: "Set level",					parameters: ["Level:level"], description: "Set level to {0}%",		attribute: "level",		value: "*|number",	],
    	[ name: "setColor",									category: "Convenience",				group: "Control [devices]",			display: "Set color",					parameters: ["?*Color:color","?*RGB:text","Hue:hue","Saturation:saturation","Lightness:level"], 	attribute: "color",		value: "*|color",	],
    	[ name: "setHue",									category: "Convenience",				group: "Control [devices]",			display: "Set hue",						parameters: ["Hue:hue"], description: "Set hue to {0}°",	attribute: "hue",		value: "*|number",	],
    	[ name: "setSaturation",							category: "Convenience",				group: "Control [devices]",			display: "Set saturation",				parameters: ["Saturation:saturation"], description: "Set saturation to {0}%",	attribute: "saturation",		value: "*|number",	],
    	[ name: "setColorTemperature",						category: "Convenience",				group: "Control [devices]",			display: "Set color temperature",		parameters: ["Color Temperature:colorTemperature"], description: "Set color temperature to {0}°K",	attribute: "colorTemperature",		value: "*|number",	],
    	[ name: "open",										category: "Convenience",				group: "Control [devices]",			display: "Open",						parameters: [], attribute: "door",		value: "open",	],
    	[ name: "close",									category: "Convenience",				group: "Control [devices]",			display: "Close",						parameters: [], attribute: "door",		value: "close",	],
    	[ name: "windowShade.open",							category: "Convenience",				group: "Control [devices]",			display: "Open fully",					parameters: [], ],
    	[ name: "windowShade.close",						category: "Convenience",				group: "Control [devices]",			display: "Close fully",					parameters: [], ],
    	[ name: "windowShade.presetPosition",				category: "Convenience",				group: "Control [devices]",			display: "Move to preset position",		parameters: [], ],
		[ name: "lock",										category: "Safety and Security",		group: "Control [devices]",			display: "Lock",						parameters: [], attribute: "lock",		value: "locked",	],
    	[ name: "unlock",									category: "Safety and Security",		group: "Control [devices]",			display: "Unlock",						parameters: [], attribute: "lock",		value: "unlocked",	],
    	[ name: "take",										category: "Safety and Security",		group: "Control [devices]",			display: "Take a picture",				parameters: [], ],
    	[ name: "alarm.off",								category: "Safety and Security",		group: "Control [devices]",			display: "Stop",						parameters: [], attribute: "alarm",		value: "stop",	],
    	[ name: "alarm.strobe",								category: "Safety and Security",		group: "Control [devices]",			display: "Strobe",						parameters: [], attribute: "alarm",		value: "strobe",	],
    	[ name: "alarm.siren",								category: "Safety and Security",		group: "Control [devices]",			display: "Siren",						parameters: [], attribute: "alarm",		value: "siren",	],
    	[ name: "alarm.both",								category: "Safety and Security",		group: "Control [devices]",			display: "Strobe and Siren",			parameters: [], attribute: "alarm",		value: "both",	],
    	[ name: "thermostat.off",							category: "Comfort",					group: "Control [devices]",			display: "Set to Off",					parameters: [], attribute: "thermostatMode",	value: "off",	],
    	[ name: "thermostat.heat",							category: "Comfort",					group: "Control [devices]",			display: "Set to Heat",					parameters: [], attribute: "thermostatMode",	value: "heat",	],
    	[ name: "thermostat.cool",							category: "Comfort",					group: "Control [devices]",			display: "Set to Cool",					parameters: [], attribute: "thermostatMode",	value: "cool",	],
    	[ name: "thermostat.auto",							category: "Comfort",					group: "Control [devices]",			display: "Set to Auto",					parameters: [], attribute: "thermostatMode",	value: "auto",	],
    	[ name: "thermostat.emergencyHeat",					category: "Comfort",					group: "Control [devices]",			display: "Set to Emergency Heat",		parameters: [], attribute: "thermostatMode",	value: "emergencyHeat",	],
    	[ name: "thermostat.quickSetHeat",					category: "Comfort",					group: "Control [devices]",			display: "Quick set heating point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set quick heating point at {0}$tempUnit",	],
    	[ name: "thermostat.quickSetCool",					category: "Comfort",					group: "Control [devices]",			display: "Quick set cooling point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set quick cooling point at {0}$tempUnit",	],
    	[ name: "thermostat.setHeatingSetpoint",			category: "Comfort",					group: "Control [devices]",			display: "Set heating point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set heating point at {0}$tempUnit",	attribute: "thermostatHeatingSetpoint",	value: "*|decimal",	],
    	[ name: "thermostat.setCoolingSetpoint",			category: "Comfort",					group: "Control [devices]",			display: "Set cooling point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set cooling point at {0}$tempUnit",	attribute: "thermostatCoolingSetpoint",	value: "*|decimal",	],
    	[ name: "thermostat.setThermostatMode",				category: "Comfort",					group: "Control [devices]",			display: "Set thermostat mode",			parameters: ["Mode:thermostatMode"], description: "Set thermostat mode to {0}",	attribute: "thermostatMode",	value: "*|string",	],
		[ name: "fanOn",									category: "Comfort",					group: "Control [devices]",			display: "Set fan to On",				parameters: [], ],
    	[ name: "fanCirculate",								category: "Comfort",					group: "Control [devices]",			display: "Set fan to Circulate",		parameters: [], ],
    	[ name: "fanAuto",									category: "Comfort",					group: "Control [devices]",			display: "Set fan to Auto",				parameters: [], ],
    	[ name: "setThermostatFanMode",						category: "Comfort",					group: "Control [devices]",			display: "Set fan mode",				parameters: ["Fan mode:thermostatFanMode"], description: "Set fan mode to {0}",	],
    	[ name: "play",										category: "Entertainment",				group: "Control [devices]",			display: "Play",						parameters: [], ],
    	[ name: "pause",									category: "Entertainment",				group: "Control [devices]",			display: "Pause",						parameters: [], ],
    	[ name: "stop",										category: "Entertainment",				group: "Control [devices]",			display: "Stop",						parameters: [], ],
    	[ name: "nextTrack",								category: "Entertainment",				group: "Control [devices]",			display: "Next track",					parameters: [], ],
    	[ name: "previousTrack",							category: "Entertainment",				group: "Control [devices]",			display: "Previous track",				parameters: [], ],
    	[ name: "mute",										category: "Entertainment",				group: "Control [devices]",			display: "Mute",						parameters: [], ],
    	[ name: "unmute",									category: "Entertainment",				group: "Control [devices]",			display: "Unmute",						parameters: [], ],
		[ name: "musicPlayer.setLevel",						category: "Entertainment",				group: "Control [devices]",			display: "Set volume",					parameters: ["Level:level"], description: "Set volume to {0}%",	],
    	[ name: "playText",									category: "Entertainment",				group: "Control [devices]",			display: "Speak text",					parameters: ["Text:string"], description: "Speak text \"{0}\"", ],
    	[ name: "playTrack",								category: "Entertainment",				group: "Control [devices]",			display: "Play track",					parameters: [], ],
    	[ name: "setTrack",									category: "Entertainment",				group: "Control [devices]",			display: "Set track",					parameters: [], ],
    	[ name: "resumeTrack",								category: "Entertainment",				group: "Control [devices]",			display: "Resume track",				parameters: [], ],
    	[ name: "restoreTrack",								category: "Entertainment",				group: "Control [devices]",			display: "Restore track",				parameters: [], ],
    	[ name: "speak",									category: "Entertainment",				group: "Control [devices]",			display: "Speak",						parameters: ["Message:string"], description: "Speak \"{0}\"", ],
    	[ name: "startActivity",							category: "Entertainment",				group: "Control [devices]",			display: "Start activity",				parameters: [], ],
    	[ name: "getCurrentActivity",						category: "Entertainment",				group: "Control [devices]",			display: "Get current activity",		parameters: [], ],
    	[ name: "getAllActivities",							category: "Entertainment",				group: "Control [devices]",			display: "Get all activities",			parameters: [], ],
    	[ name: "push",										category: "Other",						group: "Control [devices]",			display: "Push",						parameters: [], ],
    	[ name: "beep",										category: "Other",						group: "Control [devices]",			display: "Beep",						parameters: [], ],
    	[ name: "timedSession.setTimeRemaining",			category: "Other",						group: "Control [devices]",			display: "Set remaining time",			parameters: ["Remaining time [s]:number"], description: "Set remaining time to {0}s",	],
    	[ name: "timedSession.start",						category: "Other",						group: "Control [devices]",			display: "Start timed session",			parameters: [], ],
    	[ name: "timedSession.stop",						category: "Other",						group: "Control [devices]",			display: "Stop timed session",			parameters: [], ],
    	[ name: "timedSession.pause",						category: "Other",						group: "Control [devices]",			display: "Pause timed session",			parameters: [], ],
    	[ name: "timedSession.cancel",						category: "Other",						group: "Control [devices]",			display: "Cancel timed session",		parameters: [], ],
    	[ name: "setConsumableStatus",						category: "Other",						group: "Control [devices]",			display: "Set consumable status",		parameters: ["Status:consumable"], description: "Set consumable status to {0}",	],
		[ name: "configure",								category: null,							group: null,						display: "Configure",					parameters: [], ],
    	[ name: "poll",										category: null,							group: null,						display: "Poll",						parameters: [], ],
    	[ name: "refresh",									category: null,							group: null,						display: "Refresh",						parameters: [], ],
    ]
}

private virtualCommands() {
	return [
    	[ name: "wait",					requires: [],			 			display: "Wait",							parameters: ["Time:number[1..1440]","Unit:enum[seconds,minutes,hours]"],													immediate: true,	location: true,	description: "Wait {0} {1}",	],
    	[ name: "waitRandom",			requires: [],			 			display: "Wait (random)",					parameters: ["At least (minutes):number[1..1440]","At most (minutes):number[1..1440]","Unit:enum[seconds,minutes,hours]"],	immediate: true,	location: true,	description: "Wait {0}-{1} {2}",	],
    	[ name: "waitState",			requires: [],			 			display: "Wait for piston state change",	parameters: ["Change to:enum[any,false,true]"],															immediate: true,	location: true,						description: "Wait for {0} state"],
    	[ name: "toggle",				requires: ["on", "off"], 			display: "Toggle",																																															],
    	[ name: "toggle#1",				requires: ["on1", "off1"], 			display: "Toggle #1",																																															],
    	[ name: "toggle#2",				requires: ["on2", "off2"], 			display: "Toggle #2",																																															],
    	[ name: "toggle#3",				requires: ["on3", "off3"], 			display: "Toggle #3",																																															],
    	[ name: "toggle#4",				requires: ["on4", "off4"], 			display: "Toggle #4",																																															],
    	[ name: "toggle#5",				requires: ["on5", "off5"], 			display: "Toggle #5",																																															],
    	[ name: "toggle#6",				requires: ["on6", "off6"], 			display: "Toggle #6",																																															],
    	[ name: "toggle#7",				requires: ["on7", "off7"], 			display: "Toggle #7",																																															],
    	[ name: "toggle#8",				requires: ["on8", "off8"], 			display: "Toggle #8",																																															],
    	[ name: "toggleLevel",			requires: ["on", "off", "setLevel"],display: "Toggle level",					parameters: ["Level:level"],																																	description: "Toggle level between 0% and {0}%",	],
    	[ name: "delayedOn",			requires: ["on"], 					display: "Turn on (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on after {0}ms",	],
    	[ name: "delayedOn#1",			requires: ["on1"], 					display: "Turn on #1 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #1 after {0}ms",	],
    	[ name: "delayedOn#2",			requires: ["on2"], 					display: "Turn on #2 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #2 after {0}ms",	],
    	[ name: "delayedOn#3",			requires: ["on3"], 					display: "Turn on #3 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #3 after {0}ms",	],
    	[ name: "delayedOn#4",			requires: ["on4"], 					display: "Turn on #4 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #4 after {0}ms",	],
    	[ name: "delayedOn#5",			requires: ["on5"], 					display: "Turn on #5 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #5 after {0}ms",	],
    	[ name: "delayedOn#6",			requires: ["on6"], 					display: "Turn on #6 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #6 after {0}ms",	],
    	[ name: "delayedOn#7",			requires: ["on7"], 					display: "Turn on #7 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #7 after {0}ms",	],
    	[ name: "delayedOn#8",			requires: ["on8"], 					display: "Turn on #8 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #8 after {0}ms",	],
    	[ name: "delayedOff",			requires: ["off"], 					display: "Turn off (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off after {0}ms",	],
    	[ name: "delayedOff#1",			requires: ["off1"],					display: "Turn off #1 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #1 after {0}ms",	],
    	[ name: "delayedOff#2",			requires: ["off2"],					display: "Turn off #2 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #2 after {0}ms",	],
    	[ name: "delayedOff#3",			requires: ["off3"],					display: "Turn off #3 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #3 after {0}ms",	],
    	[ name: "delayedOff#4",			requires: ["off4"],					display: "Turn off #4 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #4 after {0}ms",	],
    	[ name: "delayedOff#5",			requires: ["off5"],					display: "Turn off #5 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #5 after {0}ms",	],
    	[ name: "delayedOff#6",			requires: ["off7"],					display: "Turn off #6 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #6 after {0}ms",	],
    	[ name: "delayedOff#7",			requires: ["off7"],					display: "Turn off #7 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #7 after {0}ms",	],
    	[ name: "delayedOff#8",			requires: ["off8"],					display: "Turn off #8 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #8 after {0}ms",	],
    	[ name: "delayedToggle",		requires: ["on", "off"], 			display: "Toggle (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle after {0}ms",	],
    	[ name: "delayedToggle#1",		requires: ["on1", "off1"], 			display: "Toggle #1 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #1 after {0}ms",	],
    	[ name: "delayedToggle#2",		requires: ["on2", "off2"], 			display: "Toggle #2 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #2 after {0}ms",	],
    	[ name: "delayedToggle#3",		requires: ["on3", "off3"], 			display: "Toggle #3 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #3 after {0}ms",	],
    	[ name: "delayedToggle#4",		requires: ["on4", "off4"], 			display: "Toggle #4 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #4 after {0}ms",	],
    	[ name: "delayedToggle#5",		requires: ["on5", "off5"], 			display: "Toggle #5 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #5 after {0}ms",	],
    	[ name: "delayedToggle#6",		requires: ["on6", "off6"], 			display: "Toggle #6 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #6 after {0}ms",	],
    	[ name: "delayedToggle#7",		requires: ["on7", "off7"], 			display: "Toggle #7 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #7 after {0}ms",	],
    	[ name: "delayedToggle#8",		requires: ["on8", "off8"], 			display: "Toggle #8 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #8 after {0}ms",	],
    	[ name: "flash",				requires: ["on", "off"], 			display: "Flash",							parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#1",				requires: ["on1", "off1"], 			display: "Flash #1",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #1 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#2",				requires: ["on2", "off2"], 			display: "Flash #2",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #2 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#3",				requires: ["on3", "off3"], 			display: "Flash #3",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #3 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#4",				requires: ["on4", "off4"], 			display: "Flash #4",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #4 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#5",				requires: ["on5", "off5"], 			display: "Flash #5",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #5 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#6",				requires: ["on6", "off6"], 			display: "Flash #6",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #6 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#7",				requires: ["on7", "off7"], 			display: "Flash #7",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #7 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "flash#8",				requires: ["on8", "off8"], 			display: "Flash #8",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #8 {0}ms/{1}ms for {2} time(s)",	],
    	[ name: "setVariable",			requires: [],			 			display: "Set variable", 					parameters: ["Variable:var"],																				varEntry: 0, 						location: true,															aggregated: true,	],
    	[ name: "saveAttribute",		requires: [],			 			display: "Save attribute to variable", 		parameters: ["Attribute:attribute","Aggregation:aggregation","?Convert to data type:dataType","Save to variable:string"],					varEntry: 3,		description: "Save attribute '{0}' to variable {3}",	aggregated: true,	],
    	[ name: "saveState",			requires: [],			 			display: "Save state to variable",			parameters: ["Attributes:attributes","Save to state variable...:string"],												varEntry: 1,																				aggregated: true,	],
        [ name: "saveStateLocally",		requires: [],			 			display: "Save state",																																																											aggregated: true,	],
    	[ name: "saveStateGlobally",	requires: [],			 			display: "Save state (global)",																																																									aggregated: true,	],
    	[ name: "loadAttribute",		requires: [],			 			display: "Load attribute from variable",	parameters: ["Attribute:attribute","Load from variable...:variable","Allow translations:bool","Negate translation:bool"],																								],
    	[ name: "loadState",			requires: [],			 			display: "Load state from variable",		parameters: ["Load from state variable...:stateVariable","Attributes:attributes"],																								],
    	[ name: "loadStateLocally",		requires: [],			 			display: "Load state",						parameters: ["Attributes:attributes"],																															],
    	[ name: "loadStateGlobally",	requires: [],			 			display: "Load state (global)",				parameters: ["Attributes:attributes"],																															],
    	[ name: "setLocationMode",		requires: [],			 			display: "Set location mode",				parameters: ["Mode:mode"],																														location: true,	description: "Set location mode to \"{0}\"",		aggregated: true,	],
    	[ name: "setAlarmSystemStatus",	requires: [],			 			display: "Set Smart Home Monitor status",	parameters: ["Status:alarmSystemStatus"],																										location: true,	description: "Set SHM alarm to \"{0}\"",			aggregated: true,	],
    	[ name: "sendNotification",		requires: [],			 			display: "Send notification",				parameters: ["Message:text"],																													location: true,	description: "Send notification \"{0}\"",	],
    	//[ name: "sendNotificationToContacts",requires: [],		 			display: "Send notification to contacts",	parameters: ["Message:text","Contacts:contact","Save notification:bool"],																		location: true,	],
    	[ name: "sendPushNotification",	requires: [],			 			display: "Send Push notification",			parameters: ["Message:text","Save notification:bool"],																							location: true,	description: "Send Push notification \"{0}\"",	],
    	[ name: "sendSMSNotification",	requires: [],			 			display: "Send SMS notification",			parameters: ["Message:text","Phone number:phone","Save notification:bool"],																		location: true, description: "Send SMS notification \"{0}\" to {1}",	],
    	[ name: "executeRoutine",		requires: [],			 			display: "Execute routine",					parameters: ["Routine:routine"],																		location: true, 										description: "Execute routine \"{0}\"",				aggregated: true,	],
        [ name: "cancelPendingTasks",	requires: [],			 			display: "Cancel pending tasks",			parameters: ["Scope:enum[Local,Global]"],																														description: "Cancel all pending {0} tasks",		],
    ]
}

private attributes() {
	if (state.temp && state.temp.attributes) return state.temp.attributes
	def tempUnit = "°" + location.temperatureScale
    state.temp = state.temp ? state.temp : [:]
	state.temp.attributes = [
    	[ name: "acceleration",				type: "enum",				range: null,			unit: null,		options: ["active", "inactive"],																			],
    	[ name: "alarm",					type: "enum",				range: null,			unit: null,		options: ["off", "strobe", "siren", "both"],																],
    	[ name: "battery",					type: "number",				range: "0..100",		unit: "%",		options: null,																								],
    	[ name: "beacon",					type: "enum",				range: null,			unit: null,		options: ["present", "not present"],																		],
        [ name: "button",					type: "enum",				range: null,			unit: null,		options: ["held", "pushed"],																				capability: "button",	momentary: true], //default capability so that we can figure out multi sub devices
    	[ name: "carbonDioxide",			type: "decimal",			range: "0..*",			unit: null,		options: null,																								],
    	[ name: "carbonMonoxide",			type: "enum",				range: null,			unit: null,		options: ["clear", "detected", "tested"],																	],
    	[ name: "color",					type: "color",				range: null,			unit: "#RRGGBB",options: null,																								],
    	[ name: "hue",						type: "number",				range: "0..360",		unit: "°",		options: null,																								],
    	[ name: "saturation",				type: "number",				range: "0..100",		unit: "%",		options: null,																								],
    	[ name: "hex",						type: "hexcolor",			range: null,			unit: null,		options: null,																								],
    	[ name: "saturation",				type: "number",				range: "0..100",		unit: "%",		options: null,																								],
    	[ name: "level",					type: "number",				range: "0..100",		unit: "%",		options: null,																								],
    	[ name: "switch",					type: "enum",				range: null,			unit: null,		options: ["on", "off"],																						],
    	[ name: "switch*",					type: "enum",				range: null,			unit: null,		options: ["on", "off"],																						],
    	[ name: "colorTemperature",			type: "number",				range: "2000..7000",	unit: "°K",		options: null,																								],
    	[ name: "consumable",				type: "enum",				range: null,			unit: null,		options: ["missing", "good", "replace", "maintenance_required", "order"],									],
    	[ name: "contact",					type: "enum",				range: null,			unit: null,		options: ["open", "closed"],																				],
    	[ name: "door",						type: "enum",				range: null,			unit: null,		options: ["unknown", "closed", "open", "closing", "opening"],												],
    	[ name: "energy",					type: "decimal",			range: "0..*",			unit: "kWh",	options: null,																								],
    	[ name: "energy*",					type: "decimal",			range: "0..*",			unit: "kWh",	options: null,																								],
    	[ name: "illuminance",				type: "number",				range: "0..*",			unit: "lux",	options: null,																								],
    	[ name: "image",					type: "image",				range: null,			unit: null,		options: null,																								],
    	[ name: "lock",						type: "enum",				range: null,			unit: null,		options: ["locked", "unlocked"],																			],
    	[ name: "activities",				type: "string",				range: null,			unit: null,		options: null,																								],
    	[ name: "currentActivity",			type: "string",				range: null,			unit: null,		options: null,																								],
    	[ name: "motion",					type: "enum",				range: null,			unit: null,		options: ["active", "inactive"],																			],
    	[ name: "status",					type: "string",				range: null,			unit: null,		options: null,																								],								
    	[ name: "mute",						type: "enum",				range: null,			unit: null,		options: ["muted", "unmuted"],																				],
    	[ name: "pH",						type: "decimal",			range: "0..14",			unit: null,		options: null,																								],
    	[ name: "power",					type: "decimal",			range: "0..*",			unit: "W",		options: null,																								],
    	[ name: "power*",					type: "decimal",			range: "0..*",			unit: "W",		options: null,																								],
    	[ name: "presence",					type: "enum",				range: null,			unit: null,		options: ["present", "not present"],																		],
    	[ name: "humidity",					type: "number",				range: "0..100",		unit: "%",		options: null,																								],
        [ name: "shock",					type: "enum",				range: null,			unit: null,		options: ["detected", "clear"],																				],
    	[ name: "lqi",						type: "number",				range: "0..255",		unit: null,		options: null,																								],
    	[ name: "rssi",						type: "number",				range: "0..100",		unit: "%",		options: null,																								],
    	[ name: "sleeping",					type: "enum",				range: null,			unit: null,		options: ["sleeping", "not sleeping"],																		],
    	[ name: "smoke",					type: "enum",				range: null,			unit: null,		options: ["clear", "detected", "tested"],																	],
    	[ name: "sound",					type: "enum",				range: null,			unit: null,		options: ["detected", "not detected"],																		],
        [ name: "steps",					type: "number",				range: "0..*",			unit: null,		options: null,																								],
    	[ name: "goal",						type: "number",				range: "0..*",			unit: null,		options: null,																								],
    	[ name: "soundPressureLevel",		type: "number",				range: "0..*",			unit: null,		options: null,																								],
    	[ name: "tamper",					type: "enum",				range: null,			unit: null,		options: ["clear", "detected"],																				],
    	[ name: "temperature",				type: "decimal",			range: "*..*",			unit: tempUnit,	options: null,																								],
    	[ name: "thermostatMode",			type: "enum",				range: null,			unit: null,		options: ["off", "auto", "cool", "heat", "emergency heat"],													],
    	[ name: "thermostatFanMode",		type: "enum",				range: null,			unit: null,		options: ["auto", "on", "circulate"],																		],
    	[ name: "thermostatOperatingState",	type: "enum",				range: null,			unit: null,		options: ["idle", "pending cool", "cooling", "pending heat", "heating", "fan only", "vent economizer"],		],
        [ name: "coolingSetpoint",			type: "decimal",			range: "-127..127",		unit: tempUnit,	options: null,																								],
        [ name: "heatingSetpoint",			type: "decimal",			range: "-127..127",		unit: tempUnit,	options: null,																								],
        [ name: "thermostatSetpoint",		type: "decimal",			range: "-127..127",		unit: tempUnit,	options: null,																								],
        [ name: "sessionStatus",			type: "enum",				range: null,			unit: null,		options: ["paused", "stopped", "running", "canceled"],														],
    	[ name: "threeAxis",				type: "threeAxis",			range: "0..1024",		unit: null,		options: null,																								],
    	[ name: "touch",					type: "enum",				range: null,			unit: null,		options: ["touched"],																						],
    	[ name: "valve",					type: "enum",				range: null,			unit: null,		options: ["open", "closed"],																				],
        [ name: "voltage",					type: "decimal",			range: "*..*",			unit: "V",		options: null,																								],
    	[ name: "water",					type: "enum",				range: null,			unit: null,		options: ["dry", "wet"],																					],
    	[ name: "windowShade",				type: "enum",				range: null,			unit: null,		options: ["unknown", "open", "closed", "opening", "closing", "partially open"],								],
    	[ name: "mode",						type: "mode",				range: null,			unit: null,		options: state.run == "config" ? getLocationModeOptions() : [],																					],
    	[ name: "alarmSystemStatus",		type: "enum",				range: null,			unit: null,		options: state.run == "config" ? getAlarmSystemStatusOptions() : [],																		],
    	[ name: "routineExecuted",			type: "routine",			range: null,			unit: null,		options: state.run == "config" ? location.helloHome?.getPhrases()*.label : [],															],
    	[ name: "variable",					type: "enum",				range: null,			unit: null,		options: state.run == "config" ? listVariables(true, null, true, true, true, false) : [],												],
    	[ name: "time",						type: "time",				range: null,			unit: null,		options: null,																								],
    ]
    return state.temp.attributes
}

private comparisons() {
	def optionsEnum = [
        [ condition: "is", trigger: "changes to", parameters: 1, timed: false],
        [ condition: "is not", trigger: "changes away from", parameters: 1, timed: false],
        [ condition: "is one of", trigger: "changes to one of", parameters: 1, timed: false, multiple: true, minOptions: 2],
        [ condition: "is not one of", trigger: "changes away from one of", parameters: 1, timed: false, multiple: true, minOptions: 2],
        [ condition: "was", trigger: "stays", parameters: 1, timed: true],
        [ condition: "was not", trigger: "stays away from", parameters: 1, timed: true],
        [ trigger: "changes", parameters: 0, timed: false],
        [ condition: "changed", parameters: 0, timed: true],
        [ condition: "did not change", parameters: 0, timed: true],
    ]
    
    def optionsMomentary = [
        [ condition: "is", trigger: "changes to", parameters: 1, timed: false],
	]
    
	def optionsBool = [
        [ condition: "is", parameters: 1, timed: false],
        [ condition: "is not", parameters: 1, timed: false],
        [ condition: "is true", parameters: 0, timed: false],
        [ condition: "is false", parameters: 0, timed: false],
    ]
	def optionsRoutine = [
        [ trigger: "executed", parameters: 1, timed: false],
    ]
    def optionsNumber = [
        [ condition: "is equal to", trigger: "changes to", parameters: 1, timed: false],
        [ condition: "is not equal to", trigger: "changes away from", parameters: 1, timed: false],
        [ condition: "is less than", trigger: "drops below", parameters: 1, timed: false],
        [ condition: "is less than or equal to", trigger: "drops to or below", parameters: 1, timed: false],
        [ condition: "is greater than", trigger: "raises above", parameters: 1, timed: false],
        [ condition: "is greater than or equal to", trigger: "raises to or above", parameters: 1, timed: false],
        [ condition: "is inside range", trigger: "enters range", parameters: 2, timed: false],
        [ condition: "is outside of range", trigger: "exits range", parameters: 2, timed: false],
        [ condition: "is even", trigger: "changes to an even value", parameters: 0, timed: false],
        [ condition: "is odd", trigger: "changes to an odd value", parameters: 0, timed: false],
        [ condition: "was equal to", trigger: "stays equal to", parameters: 1, timed: true],
        [ condition: "was not equal to", trigger: "stays not equal to", parameters: 1, timed: true],
        [ condition: "was less than", trigger: "stays less than", parameters: 1, timed: true],
        [ condition: "was less than or equal to", trigger: "stays less than or equal to", parameters: 1, timed: true],
        [ condition: "was greater than", trigger: "stays greater than", parameters: 1, timed: true],
        [ condition: "was greater than or equal to", trigger: "stays greater than or equal to", parameters: 1, timed: true],
        [ condition: "was inside range",trigger: "stays inside range",  parameters: 2, timed: true],
        [ condition: "was outside range", trigger: "stays outside range", parameters: 2, timed: true],
        [ condition: "was even", trigger: "stays even", parameters: 0, timed: true],
        [ condition: "was odd", trigger: "stays odd", parameters: 0, timed: true],
        [ trigger: "changes", parameters: 0, timed: false],
        [ condition: "changed", parameters: 0, timed: true],
        [ condition: "did not change", parameters: 0, timed: true],
    ]
    def optionsTime = [
        [ trigger: "happens at", parameters: 1],
        [ condition: "is any time of day", parameters: 0],
        [ condition: "is around", parameters: 1],
        [ condition: "is before", parameters: 1],
        [ condition: "is after", parameters: 1],
        [ condition: "is between", parameters: 2],
        [ condition: "is not between", parameters: 2],
	]
	return [
    	[ type: "bool",					options: optionsBool,		],
    	[ type: "boolean",				options: optionsBool,		],
    	[ type: "string",				options: optionsEnum,		],
    	[ type: "text",					options: optionsEnum,		],
    	[ type: "enum",					options: optionsEnum,		],
    	[ type: "mode",					options: optionsEnum,		],
    	[ type: "alarmSystemStatus",	options: optionsEnum,		],
    	[ type: "routine",				options: optionsRoutine,	],
    	[ type: "number",				options: optionsNumber,		],
    	[ type: "variable",				options: optionsNumber,		],
    	[ type: "decimal",				options: optionsNumber		],
    	[ type: "time",					options: optionsTime,		],        
    	[ type: "momentary",			options: optionsMomentary,	],        
    ]
}

private getLocationModeOptions() {
	def result = []
    for (mode in location.modes) {
    	if (mode) result.push("$mode")
    }
    return result
}
private getAlarmSystemStatusOptions() {
	return ["Disarmed", "Armed/Stay", "Armed/Away"]
}

private initialSystemStore() {
	return [
        "\$currentEventAttribute": null,
        "\$currentEventDate": null,
        "\$currentEventDelay": 0,
        "\$currentEventDevice": null,
		"\$currentEventDeviceIndex": 0,
        "\$currentEventReceived": null,
        "\$currentEventValue": null,
        "\$currentState": null,
        "\$currentState": null,
        "\$currentStateDuration": 0,
        "\$currentStateSince": null,
        "\$currentStateSince": null,
        "\$nextScheduledTime": null,
        "\$now": 999999999999,
        "\$hour": 0,
        "\$hour24": 0,
        "\$minute": 0,
        "\$second": 0,
        "\$meridian": "",
        "\$meridianWithDots": "",
        "\$day": 0,
        "\$dayOfWeek": 0,
        "\$dayOfWeekName": "",
        "\$month": 0,
        "\$monthName": "",
        "\$year": 0,
        "\$meridianWithDots": "",
        "\$previousEventAttribute": null,
        "\$previousEventDate": null,
        "\$previousEventDelay": 0,
        "\$previousEventDevice": null,
		"\$previousEventDeviceIndex": 0,
        "\$previousEventExecutionTime": 0,
        "\$previousEventReceived": null,
        "\$previousEventValue": null,
        "\$previousState": null,
        "\$previousStateDuration": 0,
        "\$previousStateSince": null,
        "\$random": 0,
        "\$randomColor": "#FFFFFF",
        "\$randomColorName": "White",
        "\$randomLevel": 0,
	]
}


private colors() {
	return [
        [ name: "Random",					rgb: "#000000",		h: 0,		s: 0,		l: 0,	],
        [ name: "Soft White",				rgb: "#B6DA7C",		h: 83,		s: 56,		l: 67,	],
        [ name: "Warm White",				rgb: "#DAF17E",		h: 72,		s: 80,		l: 72,	],
        [ name: "Daylight White",			rgb: "#CEF4FD",		h: 191,		s: 91,		l: 90,	],
        [ name: "Cool White",				rgb: "#F3F6F7",		h: 187,		s: 19,		l: 96,	],
        [ name: "White",					rgb: "#FFFFFF",		h: 0,		s: 100,		l: 100,	],
        [ name: "Alice Blue",				rgb: "#F0F8FF",		h: 208,		s: 100,		l: 97,	],
        [ name: "Antique White",			rgb: "#FAEBD7",		h: 34,		s: 78,		l: 91,	],
        [ name: "Aqua",						rgb: "#00FFFF",		h: 180,		s: 100,		l: 50,	],
        [ name: "Aquamarine",				rgb: "#7FFFD4",		h: 160,		s: 100,		l: 75,	],
        [ name: "Azure",					rgb: "#F0FFFF",		h: 180,		s: 100,		l: 97,	],
        [ name: "Beige",					rgb: "#F5F5DC",		h: 60,		s: 56,		l: 91,	],
        [ name: "Bisque",					rgb: "#FFE4C4",		h: 33,		s: 100,		l: 88,	],
        [ name: "Blanched Almond",			rgb: "#FFEBCD",		h: 36,		s: 100,		l: 90,	],
        [ name: "Blue",						rgb: "#0000FF",		h: 240,		s: 100,		l: 50,	],
        [ name: "Blue Violet",				rgb: "#8A2BE2",		h: 271,		s: 76,		l: 53,	],
        [ name: "Brown",					rgb: "#A52A2A",		h: 0,		s: 59,		l: 41,	],
        [ name: "Burly Wood",				rgb: "#DEB887",		h: 34,		s: 57,		l: 70,	],
        [ name: "Cadet Blue",				rgb: "#5F9EA0",		h: 182,		s: 25,		l: 50,	],
        [ name: "Chartreuse",				rgb: "#7FFF00",		h: 90,		s: 100,		l: 50,	],
        [ name: "Chocolate",				rgb: "#D2691E",		h: 25,		s: 75,		l: 47,	],
        [ name: "Coral",					rgb: "#FF7F50",		h: 16,		s: 100,		l: 66,	],
        [ name: "Corn Flower Blue",			rgb: "#6495ED",		h: 219,		s: 79,		l: 66,	],
        [ name: "Corn Silk",				rgb: "#FFF8DC",		h: 48,		s: 100,		l: 93,	],
        [ name: "Crimson",					rgb: "#DC143C",		h: 348,		s: 83,		l: 58,	],
        [ name: "Cyan",						rgb: "#00FFFF",		h: 180,		s: 100,		l: 50,	],
        [ name: "Dark Blue",				rgb: "#00008B",		h: 240,		s: 100,		l: 27,	],
        [ name: "Dark Cyan",				rgb: "#008B8B",		h: 180,		s: 100,		l: 27,	],
        [ name: "Dark Golden Rod",			rgb: "#B8860B",		h: 43,		s: 89,		l: 38,	],
        [ name: "Dark Gray",				rgb: "#A9A9A9",		h: 0,		s: 0,		l: 66,	],
        [ name: "Dark Green",				rgb: "#006400",		h: 120,		s: 100,		l: 20,	],
        [ name: "Dark Khaki",				rgb: "#BDB76B",		h: 56,		s: 38,		l: 58,	],
        [ name: "Dark Magenta",				rgb: "#8B008B",		h: 300,		s: 100,		l: 27,	],
        [ name: "Dark Olive Green",			rgb: "#556B2F",		h: 82,		s: 39,		l: 30,	],
        [ name: "Dark Orange",				rgb: "#FF8C00",		h: 33,		s: 100,		l: 50,	],
        [ name: "Dark Orchid",				rgb: "#9932CC",		h: 280,		s: 61,		l: 50,	],
        [ name: "Dark Red",					rgb: "#8B0000",		h: 0,		s: 100,		l: 27,	],
        [ name: "Dark Salmon",				rgb: "#E9967A",		h: 15,		s: 72,		l: 70,	],
        [ name: "Dark Sea Green",			rgb: "#8FBC8F",		h: 120,		s: 25,		l: 65,	],
        [ name: "Dark Slate Blue",			rgb: "#483D8B",		h: 248,		s: 39,		l: 39,	],
        [ name: "Dark Slate Gray",			rgb: "#2F4F4F",		h: 180,		s: 25,		l: 25,	],
        [ name: "Dark Turquoise",			rgb: "#00CED1",		h: 181,		s: 100,		l: 41,	],
        [ name: "Dark Violet",				rgb: "#9400D3",		h: 282,		s: 100,		l: 41,	],
        [ name: "Deep Pink",				rgb: "#FF1493",		h: 328,		s: 100,		l: 54,	],
        [ name: "Deep Sky Blue",			rgb: "#00BFFF",		h: 195,		s: 100,		l: 50,	],
        [ name: "Dim Gray",					rgb: "#696969",		h: 0,		s: 0,		l: 41,	],
        [ name: "Dodger Blue",				rgb: "#1E90FF",		h: 210,		s: 100,		l: 56,	],
        [ name: "Fire Brick",				rgb: "#B22222",		h: 0,		s: 68,		l: 42,	],
        [ name: "Floral White",				rgb: "#FFFAF0",		h: 40,		s: 100,		l: 97,	],
        [ name: "Forest Green",				rgb: "#228B22",		h: 120,		s: 61,		l: 34,	],
        [ name: "Fuchsia",					rgb: "#FF00FF",		h: 300,		s: 100,		l: 50,	],
        [ name: "Gainsboro",				rgb: "#DCDCDC",		h: 0,		s: 0,		l: 86,	],
        [ name: "Ghost White",				rgb: "#F8F8FF",		h: 240,		s: 100,		l: 99,	],
        [ name: "Gold",						rgb: "#FFD700",		h: 51,		s: 100,		l: 50,	],
        [ name: "Golden Rod",				rgb: "#DAA520",		h: 43,		s: 74,		l: 49,	],
        [ name: "Gray",						rgb: "#808080",		h: 0,		s: 0,		l: 50,	],
        [ name: "Green",					rgb: "#008000",		h: 120,		s: 100,		l: 25,	],
        [ name: "Green Yellow",				rgb: "#ADFF2F",		h: 84,		s: 100,		l: 59,	],
        [ name: "Honeydew",					rgb: "#F0FFF0",		h: 120,		s: 100,		l: 97,	],
        [ name: "Hot Pink",					rgb: "#FF69B4",		h: 330,		s: 100,		l: 71,	],
        [ name: "Indian Red",				rgb: "#CD5C5C",		h: 0,		s: 53,		l: 58,	],
        [ name: "Indigo",					rgb: "#4B0082",		h: 275,		s: 100,		l: 25,	],
        [ name: "Ivory",					rgb: "#FFFFF0",		h: 60,		s: 100,		l: 97,	],
        [ name: "Khaki",					rgb: "#F0E68C",		h: 54,		s: 77,		l: 75,	],
        [ name: "Lavender",					rgb: "#E6E6FA",		h: 240,		s: 67,		l: 94,	],
        [ name: "Lavender Blush",			rgb: "#FFF0F5",		h: 340,		s: 100,		l: 97,	],
        [ name: "Lawn Green",				rgb: "#7CFC00",		h: 90,		s: 100,		l: 49,	],
        [ name: "Lemon Chiffon",			rgb: "#FFFACD",		h: 54,		s: 100,		l: 90,	],
        [ name: "Light Blue",				rgb: "#ADD8E6",		h: 195,		s: 53,		l: 79,	],
        [ name: "Light Coral",				rgb: "#F08080",		h: 0,		s: 79,		l: 72,	],
        [ name: "Light Cyan",				rgb: "#E0FFFF",		h: 180,		s: 100,		l: 94,	],
        [ name: "Light Golden Rod Yellow",	rgb: "#FAFAD2",		h: 60,		s: 80,		l: 90,	],
        [ name: "Light Gray",				rgb: "#D3D3D3",		h: 0,		s: 0,		l: 83,	],
        [ name: "Light Green",				rgb: "#90EE90",		h: 120,		s: 73,		l: 75,	],
        [ name: "Light Pink",				rgb: "#FFB6C1",		h: 351,		s: 100,		l: 86,	],
        [ name: "Light Salmon",				rgb: "#FFA07A",		h: 17,		s: 100,		l: 74,	],
        [ name: "Light Sea Green",			rgb: "#20B2AA",		h: 177,		s: 70,		l: 41,	],
        [ name: "Light Sky Blue",			rgb: "#87CEFA",		h: 203,		s: 92,		l: 75,	],
        [ name: "Light Slate Gray",			rgb: "#778899",		h: 210,		s: 14,		l: 53,	],
        [ name: "Light Steel Blue",			rgb: "#B0C4DE",		h: 214,		s: 41,		l: 78,	],
        [ name: "Light Yellow",				rgb: "#FFFFE0",		h: 60,		s: 100,		l: 94,	],
        [ name: "Lime",						rgb: "#00FF00",		h: 120,		s: 100,		l: 50,	],
        [ name: "Lime Green",				rgb: "#32CD32",		h: 120,		s: 61,		l: 50,	],
        [ name: "Linen",					rgb: "#FAF0E6",		h: 30,		s: 67,		l: 94,	],
        [ name: "Maroon",					rgb: "#800000",		h: 0,		s: 100,		l: 25,	],
        [ name: "Medium Aquamarine",		rgb: "#66CDAA",		h: 160,		s: 51,		l: 60,	],
        [ name: "Medium Blue",				rgb: "#0000CD",		h: 240,		s: 100,		l: 40,	],
        [ name: "Medium Orchid",			rgb: "#BA55D3",		h: 288,		s: 59,		l: 58,	],
        [ name: "Medium Purple",			rgb: "#9370DB",		h: 260,		s: 60,		l: 65,	],
        [ name: "Medium Sea Green",			rgb: "#3CB371",		h: 147,		s: 50,		l: 47,	],
        [ name: "Medium Slate Blue",		rgb: "#7B68EE",		h: 249,		s: 80,		l: 67,	],
        [ name: "Medium Spring Green",		rgb: "#00FA9A",		h: 157,		s: 100,		l: 49,	],
        [ name: "Medium Turquoise",			rgb: "#48D1CC",		h: 178,		s: 60,		l: 55,	],
        [ name: "Medium Violet Red",		rgb: "#C71585",		h: 322,		s: 81,		l: 43,	],
        [ name: "Midnight Blue",			rgb: "#191970",		h: 240,		s: 64,		l: 27,	],
        [ name: "Mint Cream",				rgb: "#F5FFFA",		h: 150,		s: 100,		l: 98,	],
        [ name: "Misty Rose",				rgb: "#FFE4E1",		h: 6,		s: 100,		l: 94,	],
        [ name: "Moccasin",					rgb: "#FFE4B5",		h: 38,		s: 100,		l: 85,	],
        [ name: "Navajo White",				rgb: "#FFDEAD",		h: 36,		s: 100,		l: 84,	],
        [ name: "Navy",						rgb: "#000080",		h: 240,		s: 100,		l: 25,	],
        [ name: "Old Lace",					rgb: "#FDF5E6",		h: 39,		s: 85,		l: 95,	],
        [ name: "Olive",					rgb: "#808000",		h: 60,		s: 100,		l: 25,	],
        [ name: "Olive Drab",				rgb: "#6B8E23",		h: 80,		s: 60,		l: 35,	],
        [ name: "Orange",					rgb: "#FFA500",		h: 39,		s: 100,		l: 50,	],
        [ name: "Orange Red",				rgb: "#FF4500",		h: 16,		s: 100,		l: 50,	],
        [ name: "Orchid",					rgb: "#DA70D6",		h: 302,		s: 59,		l: 65,	],
        [ name: "Pale Golden Rod",			rgb: "#EEE8AA",		h: 55,		s: 67,		l: 80,	],
        [ name: "Pale Green",				rgb: "#98FB98",		h: 120,		s: 93,		l: 79,	],
        [ name: "Pale Turquoise",			rgb: "#AFEEEE",		h: 180,		s: 65,		l: 81,	],
        [ name: "Pale Violet Red",			rgb: "#DB7093",		h: 340,		s: 60,		l: 65,	],
        [ name: "Papaya Whip",				rgb: "#FFEFD5",		h: 37,		s: 100,		l: 92,	],
        [ name: "Peach Puff",				rgb: "#FFDAB9",		h: 28,		s: 100,		l: 86,	],
        [ name: "Peru",						rgb: "#CD853F",		h: 30,		s: 59,		l: 53,	],
        [ name: "Pink",						rgb: "#FFC0CB",		h: 350,		s: 100,		l: 88,	],
        [ name: "Plum",						rgb: "#DDA0DD",		h: 300,		s: 47,		l: 75,	],
        [ name: "Powder Blue",				rgb: "#B0E0E6",		h: 187,		s: 52,		l: 80,	],
        [ name: "Purple",					rgb: "#800080",		h: 300,		s: 100,		l: 25,	],
        [ name: "Red",						rgb: "#FF0000",		h: 0,		s: 100,		l: 50,	],
        [ name: "Rosy Brown",				rgb: "#BC8F8F",		h: 0,		s: 25,		l: 65,	],
        [ name: "Royal Blue",				rgb: "#4169E1",		h: 225,		s: 73,		l: 57,	],
        [ name: "Saddle Brown",				rgb: "#8B4513",		h: 25,		s: 76,		l: 31,	],
        [ name: "Salmon",					rgb: "#FA8072",		h: 6,		s: 93,		l: 71,	],
        [ name: "Sandy Brown",				rgb: "#F4A460",		h: 28,		s: 87,		l: 67,	],
        [ name: "Sea Green",				rgb: "#2E8B57",		h: 146,		s: 50,		l: 36,	],
        [ name: "Sea Shell",				rgb: "#FFF5EE",		h: 25,		s: 100,		l: 97,	],
        [ name: "Sienna",					rgb: "#A0522D",		h: 19,		s: 56,		l: 40,	],
        [ name: "Silver",					rgb: "#C0C0C0",		h: 0,		s: 0,		l: 75,	],
        [ name: "Sky Blue",					rgb: "#87CEEB",		h: 197,		s: 71,		l: 73,	],
        [ name: "Slate Blue",				rgb: "#6A5ACD",		h: 248,		s: 53,		l: 58,	],
        [ name: "Slate Gray",				rgb: "#708090",		h: 210,		s: 13,		l: 50,	],
        [ name: "Snow",						rgb: "#FFFAFA",		h: 0,		s: 100,		l: 99,	],
        [ name: "Spring Green",				rgb: "#00FF7F",		h: 150,		s: 100,		l: 50,	],
        [ name: "Steel Blue",				rgb: "#4682B4",		h: 207,		s: 44,		l: 49,	],
        [ name: "Tan",						rgb: "#D2B48C",		h: 34,		s: 44,		l: 69,	],
        [ name: "Teal",						rgb: "#008080",		h: 180,		s: 100,		l: 25,	],
        [ name: "Thistle",					rgb: "#D8BFD8",		h: 300,		s: 24,		l: 80,	],
        [ name: "Tomato",					rgb: "#FF6347",		h: 9,		s: 100,		l: 64,	],
        [ name: "Turquoise",				rgb: "#40E0D0",		h: 174,		s: 72,		l: 56,	],
        [ name: "Violet",					rgb: "#EE82EE",		h: 300,		s: 76,		l: 72,	],
        [ name: "Wheat",					rgb: "#F5DEB3",		h: 39,		s: 77,		l: 83,	],
        [ name: "White Smoke",				rgb: "#F5F5F5",		h: 0,		s: 0,		l: 96,	],
        [ name: "Yellow",					rgb: "#FFFF00",		h: 60,		s: 100,		l: 50,	],
        [ name: "Yellow Green",				rgb: "#9ACD32",		h: 80,		s: 61,		l: 50,	],
    ]    
}

private colorOptions() {
    return colors()*.name
}

private getColorByName(name) {
	if (name == "Random") {
    	//randomize the color
        def idx = 6 + Math.round(Math.random() * (colors().size() - 7)) as Integer
        return colors()[idx]
    }
    for (color in colors()) {
    	if (color.name == name) {
        	return color
        }
    }
    return [ name: "White", rgb: "#FFFFFF", h: 0, s: 100, l: 100, ]
}






/******************************************************************************/
/*** DEVELOPMENT AREA														***/
/*** Write code here and then move it to its proper location				***/
/******************************************************************************/

private dev() {
}
