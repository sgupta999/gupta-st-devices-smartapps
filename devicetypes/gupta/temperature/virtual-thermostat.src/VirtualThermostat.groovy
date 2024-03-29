/**
 *  Virutal Thermostat Device - Thermostat Interface to Control Heating and Cooling device(s) in 
 *							conjunction with any temperature and humidity sensor(s)
 *
 *  Author
 *	 - sandeep gupta
 *
 *  Copyright 2019
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
 

import groovy.transform.Field

// enummaps
@Field final Map      MODE = [
    OFF:   "off",
    HEAT:  "heat",
    AUTO:  "auto",
    COOL:  "cool",
    EHEAT: "emergency heat",
	OVERRIDE: "override",
	RESTORE:  "restore"
]

@Field final Map      OP_STATE = [
    COOLING:   "cooling",
    HEATING:   "heating",
    FAN:       "fan only",
    PEND_COOL: "pending cool",
    PEND_HEAT: "pending heat",
    VENT_ECO:  "vent economizer",
    IDLE:      "idle"
]

@Field final Map SETPOINT_TYPE = [
    COOLING: "cooling",
    HEATING: "heating",
	EMERGENCY: "emergency"
]

@Field final List HEAT_ONLY_MODES = [MODE.HEAT, MODE.EHEAT]
@Field final List COOL_ONLY_MODES = [MODE.COOL]
@Field final List DUAL_SETPOINT_MODES = [MODE.AUTO]
@Field final List RUNNING_OP_STATES = [OP_STATE.HEATING, OP_STATE.COOLING]

// config - TODO: move these to a pref page
@Field List SUPPORTED_MODES = [MODE.OFF, MODE.HEAT, MODE.AUTO, MODE.COOL, MODE.EHEAT, MODE.OVERRIDE]
@Field final Float    DEFAULT_THRESHOLD_DEGREES = defaultVariance()

@Field final Integer  MIN_SETPOINT = lowRange()
@Field final Integer  MAX_SETPOINT = highRange()
@Field final Integer  AUTO_MODE_SETPOINT_SPREAD = setPointSpread() // In auto mode, heat & cool setpoints must be this far apart
// end config

// derivatives
@Field final IntRange FULL_SETPOINT_RANGE = (MIN_SETPOINT..MAX_SETPOINT)
@Field final IntRange HEATING_SETPOINT_RANGE = (MIN_SETPOINT..(MAX_SETPOINT - AUTO_MODE_SETPOINT_SPREAD))
@Field final String   HEATING_SETPOINT_RANGE_STRING = "${MIN_SETPOINT}..${MAX_SETPOINT - AUTO_MODE_SETPOINT_SPREAD}"
@Field final IntRange COOLING_SETPOINT_RANGE = ((MIN_SETPOINT + AUTO_MODE_SETPOINT_SPREAD)..MAX_SETPOINT)
@Field final String   COOLING_SETPOINT_RANGE_STRING = "${MIN_SETPOINT + AUTO_MODE_SETPOINT_SPREAD}..${MAX_SETPOINT}"

// defaults
@Field final String   DEFAULT_TEMPERATURE_SCALE = "F"
@Field final String   DEFAULT_MODE = MODE.OFF
@Field final String   DEFAULT_OP_STATE = OP_STATE.IDLE
@Field final String   DEFAULT_PREVIOUS_STATE = OP_STATE.HEATING
@Field final String   DEFAULT_SETPOINT_TYPE = SETPOINT_TYPE.HEATING
@Field final Integer  DEFAULT_TEMPERATURE = defaultTemp()
@Field final Integer  DEFAULT_HEATING_SETPOINT = defaultHSP()
@Field final Integer  DEFAULT_COOLING_SETPOINT = defaultCSP()
@Field final Integer  DEFAULT_EMERGENCY_SETPOINT = defaultESP()
@Field final Integer  DEFAULT_THERMOSTAT_SETPOINT = DEFAULT_HEATING_SETPOINT
@Field final Integer  DEFAULT_HUMIDITY = 52

def unitString() {  return shouldReportInCentigrade() ? "°C": "°F" }
def defaultTemp() { return shouldReportInCentigrade() ? 22 : 72 }
def defaultCSP() { return shouldReportInCentigrade() ? 27 : 80 }
def defaultHSP() { return shouldReportInCentigrade() ? 20 : 68 }
def defaultESP() { return shouldReportInCentigrade() ? 10 : 50 }
def defaultVariance() { return shouldReportInCentigrade() ? 0.5 : 1 }
def setPointSpread() { return shouldReportInCentigrade() ? 2 : 4 }
def lowRange() { return shouldReportInCentigrade() ? 2 : 35 }
def highRange() { return shouldReportInCentigrade() ? 37 : 99 }
def getRange() { return "${lowRange()}..${highRange()}" }
def getNumRange() { return (lowRange()..highRange())  }
def shouldReportInCentigrade() {return getTempScale() == "C"}

def getTempColors() {
	def colorMap
     //getTemperatureScale() == "C"   wantMetric()		
	if(shouldReportInCentigrade()) {
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 33, color: "#d04e00"],
			[value: 36, color: "#bc2323"]
			]
	} else {
		colorMap = [
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 92, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
	}
}


metadata {
    // Automatically generated. Make future change here.
    definition (name: "Virtual Thermostat", 
				namespace: "gupta/temperature", author: "Sandeep Gupta",
				description: "Thermostat Interface to Control Heating and Cooling device(s) in conjunction with any temperature and humidity sensor(s).",
				category: "Green Living",
				iconUrl:  "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/thermostat.jpg") {
				
        capability "Sensor"
        capability "Actuator"
        capability "Health Check"
		capability "Temperature Measurement"
        capability "Thermostat"
        capability "Relative Humidity Measurement"
        capability "Configuration"
        capability "Refresh"

        command "tempUp"
        command "tempDown"
        command "heatUp"
        command "heatDown"
        command "coolUp"
        command "coolDown"
        command "setpointUp"
        command "setpointDown"
		command "cycleModeHeat"
		command "cycleModeCool"
		command "cycleModeAuto"
		command "cycleModeOff"        

        command "setTemperature", ["number"]
        command "setHumidity", ["number"]

        command "markDeviceOnline"
        command "markDeviceOffline"
		
		attribute "emergencySetpoint", "number"
		attribute "variance", "number"
		attribute "tempScale", "string"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}${unit}', unit: unitString(), defaultState: true, 
					backgroundColors: getTempColors())
			}
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "setpointUp")
                attributeState("VALUE_DOWN", action: "setpointDown")
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("humidity", label: '${currentValue}%', unit: "%", defaultState: true)
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle",backgroundColor: "#649A7B")
                attributeState("heating", backgroundColor: "#E86D13")
                attributeState("cooling", backgroundColor: "#00A0DC")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off",  label: '${name}')
                attributeState("heat", label: '${name}')
                attributeState("cool", label: '${name}')
                attributeState("auto", label: '${name}')
                attributeState("emergency heat", label: 'e-heat')
                attributeState("override", label: 'EXT-override')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label: '${currentValue}', unit: unitString(), defaultState: true)
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label: '${currentValue}', unit: unitString(), defaultState: true)
            }
        }

		standardTile("heat", "device.thermostatMode", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "heat", 	label:'HEAT', action: "cycleModeHeat", nextState: "updating", icon: "st.thermostat.heat", backgroundColor:"#C35403", defaultState: true
            state "off",	label:'',     action: "cycleModeHeat", nextState: "updating", icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "cool",	label:'',     action: "cycleModeHeat", nextState: "updating", icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "auto",	label:'',     action: "cycleModeHeat", nextState: "updating", icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "emergency heat" ,label:'e-heat', icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "override",label:'', backgroundColor: "#CA3D3D", icon: "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/blocked.png"
            state "updating",label: "Working"
        }
		
        standardTile("cool", "device.thermostatMode", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "cool", label:'COOL',      action: "cycleModeCool",  nextState: "updating",   icon: "st.thermostat.cool", backgroundColor:"#008CC1", defaultState: true
            state "off",label:'',            action: "cycleModeCool", nextState: "updating",   icon: "st.thermostat.cool", backgroundColor: "#CCCCCC"
            state "heat",label:'',           action: "cycleModeCool", nextState: "updating",  icon: "st.thermostat.cool", backgroundColor: "#CCCCCC"
            state "auto",label:'',           action: "cycleModeCool", nextState: "updating", icon: "st.thermostat.cool", backgroundColor: "#CCCCCC"
            state "emergency heat" ,label:'e-heat', icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "override",label:'', backgroundColor: "#CA3D3D", icon: "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/blocked.png"
            state "updating", label: "Working"
        }
        standardTile("auto", "device.thermostatMode", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "auto", label:'AUTO',       action: "cycleModeAuto", nextState: "updating", icon: "st.thermostat.auto", backgroundColor:"#407031", defaultState: true
            state "off",label:'',             action: "cycleModeAuto", nextState: "updating", icon: "st.thermostat.auto", backgroundColor: "#CCCCCC"
            state "heat",label:'',            action: "cycleModeAuto", nextState: "updating", icon: "st.thermostat.auto", backgroundColor: "#CCCCCC"
            state "cool",label:'',            action: "cycleModeAuto", nextState: "updating", icon: "st.thermostat.auto", backgroundColor: "#CCCCCC"
            state "emergency heat" ,label:'e-heat', icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "override",label:'', backgroundColor: "#CA3D3D", icon: "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/blocked.png"
            state "updating", label: "Working"
        }
        standardTile("off", "device.thermostatMode", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "off", label:'OFF',         action: "cycleModeOff", nextState: "updating", icon: "st.thermostat.heating-cooling-off", backgroundColor:"#CA3D3D", defaultState: true
            state "auto",label:'',            action: "cycleModeOff", nextState: "updating", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#CCCCCC"
            state "heat",label:'',            action: "cycleModeOff", nextState: "updating", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#CCCCCC"
            state "cool",label:'',            action: "cycleModeOff", nextState: "updating", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#CCCCCC"
            state "emergency heat" ,label:'e-heat', icon: "st.thermostat.heat", backgroundColor: "#CCCCCC"
            state "override",label:'', action: "cycleModeOff", nextState: "updating", icon: "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/override.png", backgroundColor: "#CA3D3D"
            
            state "updating", label: "Working"
        }
		
		standardTile("heatDown", "device.temperature", width:1, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", label: "HSP", action:"heatDown", icon:"st.thermostat.thermostat-down"
		}
				
		controlTile("heatSlider", "device.heatingSetpoint", "slider", width: 1, height: 1, decoration: "flat", range: HEATING_SETPOINT_RANGE_STRING) {
            state "heat", label: "HEAT", action: "setHeatingSetpoint", backgroundColor:"#E86D13"
        }
		
		standardTile("heatUp", "device.temperature", width:1, height:1, decoration: "flat") {
			state "default", label: "HSP", action:"heatUp", icon:"st.thermostat.thermostat-up"
		}
		
		standardTile("coolDown", "device.temperature", width:1, height:1, decoration: "flat") {
			state "default", label: "CSP", action:"coolDown", icon:"st.thermostat.thermostat-down"
		}	
		controlTile("coolSlider", "device.coolingSetpoint", "slider", width: 1, height: 1, range: COOLING_SETPOINT_RANGE_STRING) {
            state "cool", label: "COOL", action: "setCoolingSetpoint", backgroundColor: "#00A0DC"
        }
		standardTile("coolUp", "device.temperature", width:1, height:1, decoration: "flat") {
			state "default", label: "CSP", action:"coolUp", icon:"st.thermostat.thermostat-up"
		}
		
		valueTile("humSensor1", "device.humidity1",  width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}%', defaultState: true, unit: "%", backgroundColors:[
				[value: 0, color: "#FFFCDF"],
				[value: 4, color: "#FDF789"],
				[value: 20, color: "#A5CF63"],
				[value: 23, color: "#6FBD7F"],
				[value: 56, color: "#4CA98C"],
				[value: 59, color: "#0072BB"],
				[value: 76, color: "#085396"]                                    
			]
		}
        valueTile("humSensor2", "device.humidity2",  width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}%', defaultState: true, unit: "%", backgroundColors:[
				[value: 0, color: "#FFFCDF"],
				[value: 4, color: "#FDF789"],
				[value: 20, color: "#A5CF63"],
				[value: 23, color: "#6FBD7F"],
				[value: 56, color: "#4CA98C"],
				[value: 59, color: "#0072BB"],
				[value: 76, color: "#085396"]                                    
			]
		}
        valueTile("hum1", "device.hum1",  width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}', defaultState: true
		}
        valueTile("hum2", "device.hum2",  width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}', defaultState: true
		}        
        
        valueTile("tempSensor1", "device.temp1",  width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'${currentValue}${unit}', defaultState: true, unit: unitString(), backgroundColors: getTempColors()
		}
        valueTile("tempSensor2", "device.temp2",  width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'${currentValue}${unit}', defaultState: true, unit: unitString(), backgroundColors: getTempColors()
		}
        valueTile("name1", "device.name1",  width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'${currentValue}', defaultState: true
		}
        valueTile("name2", "device.name2",  width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'${currentValue}', defaultState: true
		}
        
        valueTile("todayTimeLabel", "timelabel", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
            state "default", label:'Time On Today (HH:MM)', defaultState: true
        }
        valueTile("yesterdayTimeLabel", "timelabel2", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
            state "default", label:'Time On Yesterday (HH:MM)', defaultState: true
        }
        
        valueTile("todayTime", "device.timeOnToday", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
            state "default", label:'${currentValue}', defaultState: true
        }
        valueTile("yesterdayTime", "device.timeOnYesterday", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
            state "default", label:'${currentValue}', defaultState: true
        }	
		
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 1, height:1, decoration: "flat") {
			state "idle", label:'${currentValue}', backgroundColor:"#649A7B"
			state "heating", label:'${currentValue}', backgroundColor:"#E86D13"
			state "cooling", label:'${currentValue}', backgroundColor:"#00A0DC"
		}
		standardTile("refresh", "device.switch", width:1, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh", icon:"st.secondary.refresh"
		}	
		

        standardTile("deviceHealthControl", "device.healthStatus", decoration: "flat", width: 1, height: 1, inactiveLabel: false) {
            state "online",  label: "ONLINE", backgroundColor: "#00A0DC", action: "markDeviceOffline", icon: "st.Health & Wellness.health9", nextState: "goingOffline", defaultState: true
            state "offline", label: "OFFLINE", backgroundColor: "#E86D13", action: "markDeviceOnline", icon: "st.Health & Wellness.health9", nextState: "goingOnline"
            state "goingOnline", label: "Going ONLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
            state "goingOffline", label: "Going OFFLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
        }
		
		main "temperature"
		
        details(["thermostatMulti",
            "heatUp", "todayTimeLabel","heat","cool","yesterdayTimeLabel","coolUp",		
			"heatSlider","todayTime", "auto", "off","yesterdayTime","coolSlider", 
			"heatDown", "name1","name2", "hum1", "hum2","coolDown",	
			"refresh","tempSensor1", "tempSensor2","humSensor1", "humSensor2","thermostatOperatingState"   
            //"deviceHealthControl", "tempDown","roomTemp", "tempUp", "humiditySlider", "reset",          		
        ])
    }
}

def installed() {
    log.trace "Executing 'installed'"
    configure()
    done()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
    done()
}

def configure() {
    log.trace "Executing 'configure'"
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    markDeviceOnline()
    initialize()
    done()
}

def markDeviceOnline() {
    setDeviceHealth("online")
}

def markDeviceOffline() {
    setDeviceHealth("offline")
}

private setDeviceHealth(String healthState) {
    log.debug("healthStatus: ${device.currentValue('healthStatus')}; DeviceWatch-DeviceStatus: ${device.currentValue('DeviceWatch-DeviceStatus')}")
    // ensure healthState is valid
    List validHealthStates = ["online", "offline"]
    healthState = validHealthStates.contains(healthState) ? healthState : device.currentValue("healthStatus")
    // set the healthState
    sendEvent(name: "DeviceWatch-DeviceStatus", value: healthState)
    sendEvent(name: "healthStatus", value: healthState)
}

private initialize() {
    log.trace "Executing 'initialize'"	
    sendEvent(name: "tempScale", value: DEFAULT_TEMPERATURE_SCALE)
    sendEvent(name: "temperature", value: DEFAULT_TEMPERATURE, unit: unitString())
    sendEvent(name: "humidity", value: DEFAULT_HUMIDITY, unit: "%")
    sendEvent(name: "heatingSetpoint", value: DEFAULT_HEATING_SETPOINT, unit: unitString())
    sendEvent(name: "heatingSetpointMin", value: HEATING_SETPOINT_RANGE.getFrom(), unit: unitString())
    sendEvent(name: "heatingSetpointMax", value: HEATING_SETPOINT_RANGE.getTo(), unit: unitString())
    sendEvent(name: "thermostatSetpoint", value: DEFAULT_THERMOSTAT_SETPOINT, unit: unitString())
    sendEvent(name: "coolingSetpoint", value: DEFAULT_COOLING_SETPOINT, unit: unitString())
    sendEvent(name: "emergencySetpoint", value: DEFAULT_EMERGENCY_SETPOINT_SETPOINT, unit: unitString())
    sendEvent(name: "coolingSetpointMin", value: COOLING_SETPOINT_RANGE.getFrom(), unit: unitString())
    sendEvent(name: "coolingSetpointMax", value: COOLING_SETPOINT_RANGE.getTo(), unit: unitString())
    sendEvent(name: "thermostatMode", value: DEFAULT_MODE)	
    sendEvent(name: "thermostatOperatingState", value: DEFAULT_OP_STATE)
    sendEvent(name: "variance", value: DEFAULT_THRESHOLD_DEGREES)
    state.lastOperatingState = DEFAULT_OP_STATE
    state.lastUserSetpointMode = DEFAULT_PREVIOUS_STATE
    unschedule()
}


def refresh() {
    log.trace "Executing refresh"
    sendEvent(name: "thermostatMode", value: getThermostatMode())
    sendEvent(name: "thermostatOperatingState", value: getOperatingState())
    sendEvent(name: "thermostatSetpoint", value: getThermostatSetpoint(), unit: unitString())
    sendEvent(name: "coolingSetpoint", value: getCoolingSetpoint(), unit: unitString())
    sendEvent(name: "heatingSetpoint", value: getHeatingSetpoint(), unit: unitString())
    sendEvent(name: "temperature", value: getTemperature(), unit: unitString())
    sendEvent(name: "humidity", value: getHumidity(), unit: "%")
    done()
}

// Thermostat mode
private String getThermostatMode() {
    return device.currentValue("thermostatMode") ?: DEFAULT_MODE
}

def setThermostatMode(String value) {
    log.trace "Executing 'setThermostatMode' $value"
    if (value in SUPPORTED_MODES) {
        proposeSetpoints(getHeatingSetpoint(), getCoolingSetpoint(), state.lastUserSetpointMode)
        sendEvent(name: "thermostatMode", value: value)
        evaluateOperatingState()
    } else {
        log.warn "'$value' is not a supported mode. Please set one of ${SUPPORTED_MODES.join(', ')}"
    }
    done()
}

def cycleModeHeat(){
	getThermostatMode() == MODE.HEAT ? setThermostatMode(MODE.OFF) : setThermostatMode(MODE.HEAT)
}

def cycleModeCool(){
	getThermostatMode() == MODE.COOL ? setThermostatMode(MODE.OFF) : setThermostatMode(MODE.COOL)
}

def cycleModeAuto(){
	getThermostatMode() == MODE.AUTO ? setThermostatMode(MODE.OFF) : setThermostatMode(MODE.AUTO)
}

def cycleModeOff () {
	getThermostatMode() == MODE.OVERRIDE ? changeState(MODE.RESTORE) : setThermostatMode(MODE.OFF)
}

private Boolean isThermostatOff() {
    return getThermostatMode() == MODE.OFF
}

// operating state
private String getOperatingState() {
    String operatingState = device.currentValue("thermostatOperatingState")?:OP_STATE.IDLE
    return operatingState
}

private setOperatingState(String operatingState) {
    if (operatingState in OP_STATE.values()) {
        sendEvent(name: "thermostatOperatingState", value: operatingState)
        if (operatingState != OP_STATE.IDLaE) {
            state.lastOperatingState = operatingState
        }
    } else {
        log.warn "'$operatingState' is not a supported operating state. Please set one of ${OP_STATE.values().join(', ')}"
    }
}

private String getTempScale(){
    return device?.currentState("tempScale") ?: DEFAULT_TEMPERATURE_SCALE
}

def setTempScale(val) {
	log.trace "Executing 'setTempScale to: $val"
	val = ((val.toLowerCase() == "c") || (val.toLowerCase() == "centigrade") || (val.toLowerCase() == "celcius") ) ? "C" : "F"
    sendEvent(name: "tempScale", value: val)
}

def setVariance(var){
	log.trace "Executing 'setVariance' $var"
    sendEvent(name: "variance", value: variance)
}

private Float getVariance(){
	def var = device.currentState("variance")
    return var ? var.getFloatValue() : DEFAULT_THRESHOLD_DEGREES
}

// setpoint
private Integer getEmergencySetpoint() {
    def es = device.currentState("emergencySetpoint")
    return es ? es.getIntegerValue() : DEFAULT_EMERGENCY_SETPOINT
}


def setEmergencySetpoint(Double degreesF) {
	log.trace "Executing 'setEmergencySetpoint' $degreesF"
	sendEvent(name: "emergencySetpoint", value: degreesF as Integer, unit: unitString(), displayed: false)
}


private Integer getThermostatSetpoint() {
    def ts = device.currentState("thermostatSetpoint")
    return ts ? ts.getIntegerValue() : DEFAULT_THERMOSTAT_SETPOINT
}

private Integer getHeatingSetpoint() {
    def hs = device.currentState("heatingSetpoint")
    return hs ? hs.getIntegerValue() : DEFAULT_HEATING_SETPOINT
}

def setHeatingSetpoint(Double degreesF) {
    log.trace "Executing 'setHeatingSetpoint' $degreesF"
    state.lastUserSetpointMode = SETPOINT_TYPE.HEATING
    setHeatingSetpointInternal(degreesF)
    done()
}

private setHeatingSetpointInternal(Double degreesF) {
    log.debug "setHeatingSetpointInternal($degreesF)"
    proposeHeatSetpoint(degreesF as Integer)
    evaluateOperatingState(heatingSetpoint: degreesF)
}

private heatUp() {
    log.trace "Executing 'heatUp'"
    def newHsp = getHeatingSetpoint() + 1
    if (getThermostatMode() in HEAT_ONLY_MODES + DUAL_SETPOINT_MODES) {
        setHeatingSetpoint(newHsp)
    }
    done()
}

private heatDown() {
    log.trace "Executing 'heatDown'"
    def newHsp = getHeatingSetpoint() - 1
    if (getThermostatMode() in HEAT_ONLY_MODES + DUAL_SETPOINT_MODES) {
        setHeatingSetpoint(newHsp)
    }
    done()
}

private Integer getCoolingSetpoint() {
    def cs = device.currentState("coolingSetpoint")
    return cs ? cs.getIntegerValue() : DEFAULT_COOLING_SETPOINT
}

def setCoolingSetpoint(Double degreesF) {
    log.trace "Executing 'setCoolingSetpoint' $degreesF"
    state.lastUserSetpointMode = SETPOINT_TYPE.COOLING
    setCoolingSetpointInternal(degreesF)
    done()
}

private setCoolingSetpointInternal(Double degreesF) {
    log.debug "setCoolingSetpointInternal($degreesF)"
    proposeCoolSetpoint(degreesF as Integer)
    evaluateOperatingState(coolingSetpoint: degreesF)
}

private coolUp() {
    log.trace "Executing 'coolUp'"
    def newCsp = getCoolingSetpoint() + 1
    if (getThermostatMode() in COOL_ONLY_MODES + DUAL_SETPOINT_MODES) {
        setCoolingSetpoint(newCsp)
    }
    done()
}

private coolDown() {
    log.trace "Executing 'coolDown'"
    def newCsp = getCoolingSetpoint() - 1
    if (getThermostatMode() in COOL_ONLY_MODES + DUAL_SETPOINT_MODES) {
        setCoolingSetpoint(newCsp)
    }
    done()
}

// for the setpoint up/down buttons on the multi-attribute thermostat tile.
private setpointUp() {
    log.trace "Executing 'setpointUp'"
    String mode = getThermostatMode()
    if (mode in COOL_ONLY_MODES) {
        coolUp()
    } else if (mode in HEAT_ONLY_MODES + DUAL_SETPOINT_MODES) {
        heatUp()
    }
    done()
}

private setpointDown() {
    log.trace "Executing 'setpointDown'"
    String mode = getThermostatMode()
    if (mode in COOL_ONLY_MODES + DUAL_SETPOINT_MODES) {
        coolDown()
    } else if (mode in HEAT_ONLY_MODES) {
        heatDown()
    }
    done()
}

// simulated temperature
private Integer getTemperature() {
    def ts = device.currentState("temperature")
    Integer currentTemp = DEFAULT_TEMPERATURE
    try {
        currentTemp = ts.integerValue
    } catch (all) {
        log.warn "Encountered an error getting Integer value of temperature state. Value is '$ts.stringValue'. Reverting to default of $DEFAULT_TEMPERATURE"
        setTemperature(DEFAULT_TEMPERATURE)
    }
    return currentTemp
}

// changes the "room" temperature for the simulation
def setTemperature(newTemp) {
    sendEvent(name:"temperature", value: newTemp)
    evaluateOperatingState(temperature: newTemp)
}

def setHumidity(Integer humidityValue) {
    log.trace "Executing 'setHumidityPercent' to $humidityValue"
    Integer curHum = device.currentValue("humidity") as Integer
    if (humidityValue != null) {
        Integer hum = boundInt(humidityValue, (0..100))
        if (hum != humidityValue) {
            log.warn "Corrrected humidity value to $hum"
            humidityValue = hum
        }
        sendEvent(name: "humidity", value: humidityValue, unit: "%")
    } else {
        log.warn "Could not set measured huimidity to $humidityValue%"
    }
}

private getHumidity() {
    def hp = device.currentState("humidity")
    return hp ? hp.getIntegerValue() : DEFAULT_HUMIDITY
}

/**
 * Ensure an integer value is within the provided range, or set it to either extent if it is outside the range.
 * @param Number value         The integer to evaluate
 * @param IntRange theRange     The range within which the value must fall
 * @return Integer
 */
private Integer boundInt(Number value, IntRange theRange) {
    value = Math.max(theRange.getFrom(), Math.min(theRange.getTo(), value))
    return value.toInteger()
}

private proposeHeatSetpoint(Integer heatSetpoint) {
    proposeSetpoints(heatSetpoint, null)
}

private proposeCoolSetpoint(Integer coolSetpoint) {
    proposeSetpoints(null, coolSetpoint)
}

private proposeSetpoints(Integer heatSetpoint, Integer coolSetpoint, String prioritySetpointType=null) {
    Integer newHeatSetpoint;
    Integer newCoolSetpoint;	

    String mode = getThermostatMode()
    Integer proposedHeatSetpoint = heatSetpoint?:getHeatingSetpoint()
    Integer proposedCoolSetpoint = coolSetpoint?:getCoolingSetpoint()
    if (coolSetpoint == null) {
        prioritySetpointType = SETPOINT_TYPE.HEATING
    } else if (heatSetpoint == null) {
        prioritySetpointType = SETPOINT_TYPE.COOLING
    } else if (prioritySetpointType == null) {
        prioritySetpointType = DEFAULT_SETPOINT_TYPE
    } else {
        // we use what was passed as the arg.
    }

    if (mode in HEAT_ONLY_MODES) {
        newHeatSetpoint = boundInt(proposedHeatSetpoint, FULL_SETPOINT_RANGE)
        if (newHeatSetpoint != proposedHeatSetpoint) {
            log.warn "proposed heat setpoint $proposedHeatSetpoint is out of bounds. Modifying..."
        }
    } else if (mode in COOL_ONLY_MODES) {
        newCoolSetpoint = boundInt(proposedCoolSetpoint, FULL_SETPOINT_RANGE)
        if (newCoolSetpoint != proposedCoolSetpoint) {
            log.warn "proposed cool setpoint $proposedCoolSetpoint is out of bounds. Modifying..."
        }
    } else if (mode in DUAL_SETPOINT_MODES) {
        if (prioritySetpointType == SETPOINT_TYPE.HEATING) {
            newHeatSetpoint = boundInt(proposedHeatSetpoint, HEATING_SETPOINT_RANGE)
            IntRange customCoolingSetpointRange = ((newHeatSetpoint + AUTO_MODE_SETPOINT_SPREAD)..COOLING_SETPOINT_RANGE.getTo())
            newCoolSetpoint = boundInt(proposedCoolSetpoint, customCoolingSetpointRange)
        } else if (prioritySetpointType == SETPOINT_TYPE.COOLING) {
            newCoolSetpoint = boundInt(proposedCoolSetpoint, COOLING_SETPOINT_RANGE)
            IntRange customHeatingSetpointRange = (HEATING_SETPOINT_RANGE.getFrom()..(newCoolSetpoint - AUTO_MODE_SETPOINT_SPREAD))
            newHeatSetpoint = boundInt(proposedHeatSetpoint, customHeatingSetpointRange)
        }
    } else if (mode == MODE.OFF) {
        log.debug "Thermostat is off - no setpoints will be modified"
    } else {
        log.debug "Thermostat mode: $mode no setpoints will be modified"
    }

    if (newHeatSetpoint != null) {
        log.info "set heating setpoint of $newHeatSetpoint"
        sendEvent(name: "heatingSetpoint", value: newHeatSetpoint, unit: unitString())
    }
    if (newCoolSetpoint != null) {
        log.info "set cooling setpoint of $newCoolSetpoint"
        sendEvent(name: "coolingSetpoint", value: newCoolSetpoint, unit: unitString())
    }
	
	sendEvent(name: "coolingSetpoint", value: getCoolingSetpoint(), unit: unitString(), isStateChange: 'true', displayed: 'false')
	sendEvent(name: "heatingSetpoint", value: getHeatingSetpoint(), unit: unitString(), isStateChange: 'true', displayed: 'false')
}


def changeState(modein){
	def mode, opstate
	if (modein == MODE.OVERRIDE) {
		state.lastMode = getThermostatMode()
		state.lastState = getOperatingState()
		mode = modein
		opstate = OP_STATE.IDLE
	}else if ((modein == MODE.RESTORE) && (getThermostatMode() == MODE.OVERRIDE)) {
		mode = (![MODE.OVERRIDE, null].contains(state.lastMode)) ? state.lastMode?: DEFAULT_MODE : DEFAULT_MODE
		opstate = state.lastState?: DEFAULT_OP_STATE
	} else if (getThermostatMode() != MODE.OVERRIDE){
		return;
	}else {
		log.warn "Unknown/unhandled Thermostat mode: $modein"
		return;
	}
	setOperatingState(opstate)
	setThermostatMode(mode)
}

// sets the thermostat setpoint and operating state and starts the "HVAC" or lets it end.
private evaluateOperatingState(Map overrides) {
    // check for override values, otherwise use current state values
    Integer currentTemp = overrides.find { key, value ->
            "$key".toLowerCase().startsWith("curr")|"$key".toLowerCase().startsWith("temp")
        }?.value?:getTemperature() as Integer
	String tsMode = getThermostatMode()
		
	if ((currentTemp < getEmergencySetpoint()) && (tsMode != MODE.EHEAT)){
		setThermostatMode(MODE.EHEAT)
		return;
	}
	
    Integer heatingSetpoint = overrides.find { key, value -> "$key".toLowerCase().startsWith("heat") }?.value?:getHeatingSetpoint() as Integer
    Integer coolingSetpoint = overrides.find { key, value -> "$key".toLowerCase().startsWith("cool") }?.value?:getCoolingSetpoint() as Integer

    
    String currentOperatingState = getOperatingState()

    log.debug "evaluate current temp: $currentTemp, heating setpoint: $heatingSetpoint, cooling setpoint: $coolingSetpoint"
    log.debug "mode: $tsMode, operating state: $currentOperatingState"

    Boolean isHeating = false
    Boolean isCooling = false
    Boolean isIdle = false
    if (tsMode in HEAT_ONLY_MODES + DUAL_SETPOINT_MODES) {
        if (heatingSetpoint - currentTemp >= getVariance()) {
            isHeating = true
            setOperatingState(OP_STATE.HEATING)
        }
        sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
    }
    if (tsMode in COOL_ONLY_MODES + DUAL_SETPOINT_MODES && !isHeating) {
        if (currentTemp - coolingSetpoint >= getVariance()) {
            isCooling = true
            setOperatingState(OP_STATE.COOLING)
        }
        sendEvent(name: "thermostatSetpoint", value: coolingSetpoint)
    }
    else {
        sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
    }
    if (!isHeating && !isCooling) {
        setOperatingState(OP_STATE.IDLE)
    }
}

def setTemperatureSensor(temp, id, name) {
	switch(id) {
      case 0:
        sendEvent(name:"name1", value: name, displayed: false)
      	sendEvent(name:"temp1", value: temp, unit: unitString(), displayed: false)
      	break
      case 1:
        sendEvent(name:"name2", value: name, displayed: false)
      	sendEvent(name:"temp2", value: temp, unit: unitString(), displayed: false)
      	break
    }
}

def setHumiditySensor(humidity, id, name) {
	switch(id) {
      case 0:
        sendEvent(name:"hum1", value: name, displayed: false)
      	sendEvent(name:"humidity1", value: humidity, unit: "%", displayed: false)
      	break
      case 1:
        sendEvent(name:"hum2", value: name, displayed: false)
      	sendEvent(name:"humidity2", value: humidity, unit: "%", displayed: false)
      	break
    }
}
def clearSensorData() {
	sendEvent(name:"name1", value: null, displayed: false)
    sendEvent(name:"temp1", value: null, unit: unitString(), displayed: false)
    sendEvent(name:"name2", value: null, displayed: false)
    sendEvent(name:"temp2", value: null, unit: unitString(), displayed: false)
	sendEvent(name:"hum1", value: null, displayed: false)
    sendEvent(name:"humidity1", value: null, unit: "%", displayed: false)
    sendEvent(name:"hum2", value: null, displayed: false)
    sendEvent(name:"humidity2", value: null, unit: "%", displayed: false)
}

def setTimings(int today, int  yesterday) {
    String todayFormatted = new GregorianCalendar( 0, 0, 0, 0, 0, today, 0 ).time.format( 'HH:mm' )
    String yesterdayFormatted = new GregorianCalendar( 0, 0, 0, 0, 0, yesterday, 0 ).time.format( 'HH:mm' )
    sendEvent(name:"timeOnToday", value: todayFormatted, displayed: true)
    sendEvent(name:"timeOnYesterday", value: yesterdayFormatted, displayed: true)
}

/**
 * Just mark the end of the execution in the log
 */
private void done() {
    log.trace "---- DONE ----"
}
