/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
definition(
    name: "Fibaro Dimmer 2 Scenes",
    namespace: "My Apps",
    author: "Elnar Hajiyev",
    description: "Smart app that allows to control switches with changes of Fibaro Dimmer 2 Scenes",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When this button is tapped...") {
		input "buttonDevice", "capability.button", title: "Dimmer?"
	}
	section("Turn on or off all of these switches with S1 tapped on/off") {
		input "switchesTapS1", "capability.switch", multiple: true, required: false
	}
    section("Toggle all of these switches with S1 double tap") {
		input "switchesDoubleTapS1", "capability.switch", multiple: true, required: false
	}
    section("Turn on or off all of these switches with S2 tapped on/off") {
		input "switchesTapS2", "capability.switch", multiple: true, required: false
	}
    section("Toggle all of these switches with S2 double tap") {
		input "switchesDoubleTapS2", "capability.switch", multiple: true, required: false
	}
    section("Toggle all of these switches with S2 triple tap") {
		input "switchesTipleTapS2", "capability.switch", multiple: true, required: false
	}
}

def installed()
{
	subscribe(buttonDevice, "button", buttonHandler, [filterEvents: false])
}

def updated()
{
	unsubscribe()
	subscribe(buttonDevice, "button", buttonHandler, [filterEvents: false])
}

def buttonHandler(evt) {
	log.info evt.value

    if(evt.value == "tappedOnS1") {
    	switchesTapS1.on()
    }
    else if(evt.value == "tappedOffS1") {
    	switchesTapS1.off()
    }
    else if(evt.value == "doubleTappedS1") {
        toggle(switchesDoubleTapS1)
    }
    else if(evt.value == "tappedOnS2") {
    	switchesTapS2.on()
    }
    else if(evt.value == "tappedOffS2") {
    	switchesTapS2.off()
    }
    else if(evt.value == "doubleTappedS2") {
    	toggle(switchesDoubleTapS2)
    }
    else if(evt.value == "tripleTappedS2") {
    	toggle(switchesTipleTapS2)
    }


}

def toggle(devices) {
    log.debug "toggle: $devices = ${devices*.currentValue('switch')}"

    if (devices*.currentValue('switch').contains('on')) {
        devices.off()
    }
    else if (devices*.currentValue('switch').contains('off')) {
        devices.on()
    }
    else {
        devices.on()
    }
}