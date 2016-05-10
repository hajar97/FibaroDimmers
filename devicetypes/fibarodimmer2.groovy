/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Dimmer 2 UK 1-Way Switch (with bypass + buttons)", namespace: "smartthings", author: "Rajiv, Elnar Hajiyev") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
        capability "Switch Level"
        capability "Button"

		command "reset"
        command "configureAfterSecure"

        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x26, 0x5A, 0x59, 0x85, 0x73, 0x98, 0x7A, 0x56, 0x70, 0x31, 0x32, 0x8E, 0x60, 0x75, 0x71, 0x27, 0x22, 0xEF, 0x2B"
	}

	// simulator metadata
	simulator {
    	status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

        ["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

	// tile definitions

	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configureAfterSecure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch","power","energy"])
		details(["switch","power","energy","configureAfterSecure","refresh","reset"])
	}
}

def parse(String description) {
	log.trace(description)
    log.debug("RAW command: $description")
	def result = null

    if (description != "updated") {
		def cmd = zwave.parse(description.replace("98C1", "9881"), [0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
    }
    log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	log.debug( "Dimming Duration: $cmd.dimmingDuration")
    log.debug( "Button code: $cmd.sceneId")

    if ( cmd.sceneId == 10 ) {
        Integer button = 1
        sendEvent(name: "button", value: "tappedOnS1", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was tapped on", isStateChange: true)
        log.debug( "Button $button was tapped on" )
    }
    else if ( cmd.sceneId == 11 ) {
        Integer button = 1
        sendEvent(name: "button", value: "tappedOffS1", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was tapped off", isStateChange: true)
        log.debug( "Button $button was tapped off" )
    }
    else if ( cmd.sceneId == 14 ) {
        Integer button = 1
        sendEvent(name: "button", value: "doubleTappedS1", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was double tapped", isStateChange: true)
        log.debug( "Button $button was double tapped" )
    }
    else if ( cmd.sceneId == 20 ) {
        Integer button = 2
        sendEvent(name: "button", value: "tappedOnS2", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was tapped on", isStateChange: true)
        log.debug( "Button $button was tapped on" )
    }
    else if ( cmd.sceneId == 21 ) {
        Integer button = 2
        sendEvent(name: "button", value: "tappedOffS2", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was tapped off", isStateChange: true)
        log.debug( "Button $button was tapped off" )
    }
    else if ( cmd.sceneId == 24 ) {
        Integer button = 2
        sendEvent(name: "button", value: "doubleTappedS2", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was double tapped", isStateChange: true)
        log.debug( "Button $button was double tapped" )
    }
    else if ( cmd.sceneId == 25 ) {
        Integer button = 2
        sendEvent(name: "button", value: "tripleTappedS2", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was triple tapped", isStateChange: true)
        log.debug( "Button $button was triple tapped" )
    }
}

// Devices that support the Security command class can send messages in an encrypted form;
// they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.trace(cmd)
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.trace(cmd)
	//dimmerEvents(cmd)
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	log.trace(cmd)
	dimmerEvents(cmd)
}


def dimmerEvents(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.trace(cmd)
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	log.debug "No handler for $cmd"
	// Handles all Z-Wave commands we aren't interested in
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}


def on() {
	log.trace("on")
	secureSequence([
			zwave.basicV1.basicSet(value: 0xFF),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def off() {
	log.trace("off")
	secureSequence([
			zwave.basicV1.basicSet(value: 0x00),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def poll() {
	log.trace("poll")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def refresh() {
	log.trace("trace")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def reset() {
	log.trace("reset")
	return secureSequence([
    	zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterReset(),
		zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2)
	])
}

def setLevel(level) {
	log.trace("setlevel")
	if(level > 99) level = 99
	secureSequence([
		zwave.basicV1.basicSet(value: level),
		zwave.switchMultilevelV1.switchMultilevelGet()
	], 5000)
}

def configureAfterSecure() {
    log.debug "configureAfterSecure()"
        secureSequence([
   		//zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: 1),	// Minimum brightness level (1-98)
   		//zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: 99),	// Maximum brightness level (2-99)
		zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: 2),	// Force auto-calibration of the load with FIBARO Bypass 2
        zwave.configurationV1.configurationSet(parameterNumber: 19, size: 1, scaledConfigurationValue: 99),	// Forced switch on brightness level
        zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: 1),	// Enable Dimmer to work with Toggle Switch
        zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: 0),
        zwave.configurationV1.configurationSet(parameterNumber: 22, size: 1, scaledConfigurationValue: 0),  // Device changes status on switch status change
		zwave.configurationV1.configurationSet(parameterNumber: 26, size: 1, scaledConfigurationValue: 0),	// S2 can control dimmer as well
        zwave.configurationV1.configurationSet(parameterNumber: 28, size: 1, scaledConfigurationValue: 1),	// Scene activation functionality
        zwave.configurationV1.configurationSet(parameterNumber: 29, size: 1, scaledConfigurationValue: 1),  // S1 operates as S2, S2 operates as S1, needed to stop lights from switching on with double tap
        zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: 2),	// 0=forced leading edge control, 1=forced trailing edge control, 2=control mode selected automatically (based on auto-calibration)
		zwave.configurationV1.configurationSet(parameterNumber: 32, size: 1, scaledConfigurationValue: 0),	// On/Off Mode (0=Dimmer,1=On/Off,2=Auto)
		zwave.configurationV1.configurationSet(parameterNumber: 35, size: 1, scaledConfigurationValue: 0),	// Auto Calibration (0=No after Power On, 1=On First Power On, 2=On Each Power On)
        zwave.configurationV1.configurationSet(parameterNumber: 39, size: 2, scaledConfigurationValue: 350),// Power limit - OVERLOAD 1-350
        zwave.configurationV1.configurationSet(parameterNumber: 54, size: 1, scaledConfigurationValue: 1),	// 0=Self-measurement inactive, 1=Self-measurement active


		// Register for Group 1
        zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId]),
        // Register for Group 2
        zwave.associationV2.associationSet(groupingIdentifier:2, nodeId: [zwaveHubNodeId]),
        // Register for Group 3
        zwave.associationV2.associationSet(groupingIdentifier:3, nodeId: [zwaveHubNodeId]),
        // Register for Group 4
        zwave.associationV2.associationSet(groupingIdentifier:4, nodeId: [zwaveHubNodeId]),
        // Register for Group 5
        zwave.associationV2.associationSet(groupingIdentifier:5, nodeId: [zwaveHubNodeId]),
	])
}

def configure() {
	// Wait until after the secure exchange for this
    log.debug "configure()"
}

def updated() {
	log.debug "updated()"
	response(["delay 2000"] + configureAfterSecure() + refresh())
}

private secure(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	log.debug "$commands"
	delayBetween(commands.collect{ secure(it) }, delay)
}