/**
 *  Copyright 2016 SmartThings
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
 *  Fibaro Reduce Lights Brightness At Night
 *
 *  Author: Elnar Hajiyev
 */
definition(
    name: "Fibaro Reduce Lights Brightness At Night",
    namespace: "My Apps",
    author: "Elnar Hajiyev",
    description: "Set any dimmable Fibaro light to reduced brightenss during night hours. Helps to stop blinding people when they go to the toilet at night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance@2x.png"
)

preferences {
    section("Control this light brightness...") {
        input "fibaroDimmer1Devices", "device.FibaroDimmer1", multiple: true, required: false
        input "fibaroDimmer2Devices", "device.FibaroDimmer2", multiple: true, required: false
	}
    section("From this time...") {
		input "sunsetOffset", "number", title: "This many minutes after sunset...", range: "-1140..1140", defaultValue: 0, required: true
        input "startTime", "time", title: "or starting exactly from this time. If this parameter is set then sunset setting will be ignored.", required: false
    }
    section("Until this time...") {
	    input "sunriseOffset", "number", title: "This many minutes after sunrise...", range: "-1140..1140", defaultValue: 0, required: true
        input "endTime", "time", title: "or starting exactly from this time. If this parameter is set then sunrise setting will be ignored.", required: false
	}
    section("Set this reduced brightness level...") {
    	input "reducedBrightness", "number", title: "Brightness level", description: "1-100%", range: "1..100", required: true
    }
    section("Set this normal brightness level...") {
    	input "normalBrightness", "number", title: "Brightness level", description: "1-100%", range: "1..100", required: true
    }
}

def installed()
{
	initialize()
}

def updated()
{
	initialize()
}

private initialize() {
	unschedule()
    unsubscribe()

    if(fibaroDimmer1Devices || fibaroDimmer2Devices) {
		if (startTime) {
			log.debug "scheduling reduced brightness $reducedBrightness to set at $startTime"
			schedule(startTime, "reduceBrightness")
		}
    	else {
    		log.debug "scheduling reduced brightness $reducedBrightness to set at $sunsetOffset mins after sunset"
    		subscribe(location, "sunsetTime", sunsetHandler)
    	    scheduleReduceBrightness(location.currentValue("sunsetTime"))
    	}
   	 	if (endTime) {
			log.debug "scheduling normal brightness $normalBrightness to restore at $endTime"
			schedule(endTime, "restoreBrightness")
		}
    	else {
    		log.debug "scheduling normal brightness $normalBrightness to set at $sunriseOffset mins after sunrise"
   		    subscribe(location, "sunriseTime", sunriseHandler)
   		    scheduleRestoreBrightness(location.currentValue("sunriseTime"))
    	}
    }
}

def sunsetHandler(evt) {
    scheduleReduceBrightness(evt.value)
}

private scheduleReduceBrightness(sunsetString) {
	//get the Date value for the string
    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)

    //calculate the offset
    def timeAfterSunset = new Date(sunsetTime.time + (sunsetOffset * 60 * 1000))

    log.debug "Scheduling for: $timeAfterSunset (sunset is $sunsetString)"

    //schedule this to run one time
    runOnce(timeAfterSunset, reduceBrightness)
}

def sunriseHandler(evt) {
    scheduleRestoreBrightness(evt.value)
}

private scheduleRestoreBrightness(sunriseString) {
	//get the Date value for the string
    def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)

    //calculate the offset
    def timeAfterSunrise = new Date(sunriseTime.time + (sunriseOffset * 60 * 1000))

    log.debug "Scheduling for: $timeAfterSunrise (sunrise is $sunriseString)"

    //schedule this to run one time
    runOnce(timeAfterSunrise, restoreBrightness)
}

private reduceBrightness() {
	if(fibaroDimmer2Devices) {
		log.debug "reducing brightness of Fibaro Dimmer 2 devices"
    	fibaroDimmer2Devices*.changeSingleParamAfterSecure(19,1,reducedBrightness)
    }
    if(fibaroDimmer1Devices) {
		log.debug "reducing brightness of Fibaro Dimmer 1 devices"
    	fibaroDimmer1Devices*.changeSingleParam(6,1,reducedBrightness)
    }
}

private restoreBrightness() {
	if(fibaroDimmer2Devices) {
		log.debug "restoring brightness of Fibaro Dimmer 2 devices"
		fibaroDimmer2Devices*.changeSingleParamAfterSecure(19,1,normalBrightness)
    }
    if(fibaroDimmer1Devices) {
		log.debug "restoring brightness of Fibaro Dimmer 1 devices"
    	fibaroDimmer1Devices*.changeSingleParam(6,1,normalBrightness)
    }
}