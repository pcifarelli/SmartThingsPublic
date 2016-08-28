/**
 *  Multichannel Lights Switch2 Endpoint
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
    name: "Multichannel Lights Switch2 Endpoint",
    namespace: "pcifarelli",
    author: "Paul Cifarelli",
    description: "Associates Virtual Switch with Multichannel to control one endpoint.  ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn on when switch is activated:") {
        input "theswitch", "capability.switch", required: true, title: "Which Switch?"
    }
    section("Turn on this light") {
        input "switchtocontrol", "capability.switch", required: true, multiple: false, title: "Which Switch?"
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
    // subscribe
	subscribe(theswitch, "switch", vswActiveDetectedHandler)
	subscribe(switchtocontrol, "switch2", swActiveDetectedHandler)
}

// implement event handlers
def vswActiveDetectedHandler(evt) {
    log.debug "vswActiveDetectedHandler called: $evt $evt.value " + switchtocontrol.currentSwitch2
     if("on" == evt.value && switchtocontrol.currentSwitch2 == "off" ) {
         switchtocontrol.on2()
     } else if ("off" == evt.value && switchtocontrol.currentSwitch2 == "on" ) {
         switchtocontrol.off2()
     }
}

def swActiveDetectedHandler(evt) {
    log.debug "swActiveDetectedHandler called: $evt $evt.value " + theswitch.currentSwitch
     if("on" == evt.value && theswitch.currentSwitch == "off" ) {
         theswitch.on()
     } else if ("off" == evt.value && theswitch.currentSwitch == "on") {
         theswitch.off()
     }
}