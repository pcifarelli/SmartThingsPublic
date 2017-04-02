/**
 *  Restore The Lights
 *
 *  Copyright 2016 Paul Cifarelli
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
    name: "Restore The Lights",
    namespace: "pcifarelli",
    author: "Paul Cifarelli",
    description: "Whenever the Mode changes, turn off the selected lights.  When the Mode changes back, restore to previous state. " +
    			 "Great for turning off lights on the way out, and back on when you get Home",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage@2x.png"
)


preferences {
    section("Start Mode") {
        input "smode", "mode", required: true, title: "Which mode do we save the lights from?", multiple: false
    }
    section("Next Mode") {
        input "nmode", "mode", required: true, title: "Which mode, when switched to from the Start Mode, should the lights be turned off?", multiple: false
    }
	section("Which Lights?") {
        input "switches", "capability.switch", required: false, multiple: true, title: "Which lights?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	state.lastMode = location.mode
    log.debug "initialize: lastMode is ${state.lastMode}"
	log.debug "The switches I will save are " + switches.collect{ it.label }
    
    // subscribe
    subscribe(location, modeChangeHandler)
}

def modeChangeHandler(evt) {
    log.debug "modeChangeHandler: lastMode is ${state.lastMode}"
    
	if (state.lastMode == smode) {
		if (evt.value == nmode) {
        	// we made the right mode change to kill the lights
        	log.debug "Previous mode was ${state.lastMode}, new mode is ${location.mode} - Saving light state and killing the lights"
            state.savedSwitches = switches.collect{ device(it) }
            switches.collect{ it.off() }
        }
	}
    
    if (state.lastMode == nmode) {
    	if (evt.value == smode) {
       		// we made the right mode change to restore the lights
        	log.debug "Previous mode was ${state.lastMode}, new mode is ${location.mode} - Restoring the lights"
            state.savedSwitches.collect {
                def sw = getDeviceById(it.id)
               	// why check currentSwitch?  For example, in case you run a vacation program while you were away.
            	if (it.state == "on" && sw.currentSwitch == "off") {
                    log.debug "Turning ${it.label} back on"
                	sw.on()
                } else if (it.state == "off" && sw.currentSwitch == "on") {
                    log.debug "Turning ${it.label} back off"
                	sw.off()
                }
            }
            // we no longer need the saved switches, so clean up
            state.remove("savedSwitches")
        }
    }
    
    // save the new mode as the lastMode
    state.lastMode = location.mode
}

private device(it) {
    //log.debug "id: ${it?.id}   label: ${it?.label}   state: it.currentSwitch"
	it ? [id: it.id, state: it.currentSwitch] : null
}

private getDeviceById(id) {
	def device = switches.find { it.id == id }
}