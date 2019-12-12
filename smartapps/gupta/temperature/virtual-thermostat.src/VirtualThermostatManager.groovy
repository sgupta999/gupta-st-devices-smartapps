/**
 *  Virutal Thermostat Manager App to Create Multiple Virutal Thermostat Contollers
 *  that control a Virtual Thermostat Device Interface to Control Heating and Cooling device(s) in 
 *	conjunction with any temperature and humidity sensor(s)
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

definition(
    name: "Virtual Thermostat Manager",
    namespace: "gupta/temperature",
    author: "Sandeep Gupta",
    description: "Thermostat Interface to Control Heating and Cooling device(s) in conjunction with any temperature and humidity sensor(s).",
    category: "Green Living",
    iconUrl: "https://github.com/sgupta999/GuptaSmartthingsRepository/raw/master/icons/thermostat.jpg",
	singleInstance: true
)

preferences {
    page(name: "Install", title: "Thermostat Manager", install: true, uninstall: true) {
        section("Devices") {
        }
        section {
            app(name: "thermostats", appName: "Virtual Thermostat Controller", namespace: "gupta/temperature", title: "New Thermostat", multiple: true)
        }
		remove("Delete Thermostat(s)", "Are you sure?", "This will delete all Thermostat(s). Cannot be undone.")
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
}