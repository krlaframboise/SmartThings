<a name="ReadMeAnchor"></a>

<h1>Kevin LaFramboise's Device Type Handlers</h1>

<ul>
	<li><a href="#aeon-labs-aeotec-doorbell">Aeon Labs Aeotec Doorbell</a></li>
	<li><a href="#aeon-labs-multifunction-siren">Aeon Labs Multifunction Siren</a></li>
	<li><a href="#alarm-switch">Alarm Switch</a></li>
	<li><a href="#dome-door-sensor">Dome Door Sensor</a></li>
	<li><a href="#dome-doorwindow-sensor-pro">Dome Door/Window Sensor Pro</a></li>
	<li><a href="#dome-leak-sensor">Dome Leak Sensor</a></li>
	<li><a href="#dome-motion-sensor">Dome Motion Sensor</a></li>
	<li><a href="#dome-mouser">Dome Mouser</a></li>			
	<li><a href="#dome-on-off-plug">Dome On Off Plug</a></li>
	<li><a href="#dome-siren">Dome Siren</a></li>
	<li><a href="#dome-water-shut-off">Dome Water Shut-Off</a></li>
	<li><a href="#ecolink-motion-sensor">Ecolink Motion Sensor</a></li>
	<li><a href="#everspring-motion-detector">Everspring Motion Detector</a></li>
	<li><a href="#everspring-temperaturehumidity-detector">Everspring Temperature/Humidity Detector</a></li>
	<li><a href="#fibaro-motion-sensor-zw5">Fibaro Motion Sensor ZW5</a></li>
	<li><a href="#fibaro-swipe">Fibaro Swipe</a></li>
	<li><a href="#forcible-mobile-presence">Forcible Mobile Presence</a></li>
	<li><a href="#gocontrollinear-doorwindow-sensor">GoControl/Linear Door/Window Sensor</a></li>
	<li><a href="#gocontrollinear-multifunction-contact-sensor">GoControl/Linear Multifunction Contact Sensor</a></li>
	<li><a href="#gocontrollinear-motion-sensor">GoControl/Linear Motion Sensor</a></li>
	<li><a href="#gocontrollinear-multifunction-siren">GoControl/Linear/Vision Multifunction Siren</a></li>
	<li><a href="#leaksmart-water-valve">LeakSmart Water Valve</a></li>
	<li><a href="#zoozmonoprice-4-in-1-multisensor">Monoprice 4-in-1 Motion Sensor with Temperature, Humidity, and Light Sensors</a></li>
	<li><a href="#visionmonoprice-shock-sensor">Monoprice Shock Sensor</a></li>
	<li><a href="#monoprice-z-wave-plus-doorwindow-sensor">Monoprice Z-Wave Plus Door/Window Sensor</a></li>
	<li><a href="#polling-cree-bulb">Polling Cree Bulb</a></li>
	<li><a href="#polling-ge-link-bulb">Polling GE Link Bulb</a></li>
	<li><a href="#remotec-zxt-310-ir-extender">Remotec ZXT-310 IR Extender</a></li>
	<li><a href="#thingshield-timer">ThingShield Timer</a></li>
	<li><a href="#visionmonoprice-shock-sensor">Vision Shock Sensor</a></li>
	<li><a href="#wireless-smoke-detector-sensor">Wireless Smoke Detector Sensor</a></li>
	<li><a href="#zipato-multisound-siren">Zipato Multisound Siren</a></li>
	<li><a href="#zoozmonoprice-4-in-1-multisensor">Zooz 4-in-1 Multisensor</a></li>
	<li><a href="#zooz-power-switchzooz-smart-plug">Zooz Smart Plug</a></li>
	<li><a href="#zooz-power-strip">Zooz Power Strip</a></li>
	<li><a href="#zooz-power-switchzooz-smart-plug">Zooz Power Switch</a></li>
	<li><a href="#zooz-smart-chime">Zooz Smart Chime</a></li>
	<li><a href="#zooz-water-sensor">Zooz Water Sensor</a></li>
</ul>

If you like the SmartApps and Device Handlers I've created and you would like to make a donation, please go to https://www.paypal.me/krlaframboise

<hr />

<h3>Aeon Labs Aeotec Doorbell</h3>

<ul>
<li>DTH for the Aeon Labs Aeotec Doorbell that allows you to use the device as a Switch, Alarm, Tone Generator, Music Player, and Audio Notification. Implements custom commands to allow you to play tracks by track number and change the volume on the fly.<br /></li>

<li><a href="https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeotec-doorbell.src">View Aeon Labs Aeotec Doorbell - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeon Labs Multifunction Siren</h3>

<ul>
<li>DTH for the Aeon Labs Siren that provides features like beeping, auto off, delayed alarm, beep scheduling for things like beeping during entry and exit.<br /></li>
<li><a href="https://community.smartthings.com/t/release-aeon-labs-multifunction-siren/40652?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeon-labs-multifunction-siren.src">View Aeon Labs Multifunction Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Alarm Switch</h3>
<ul>
<li>Allows you to use any device that supports the Switch Capability as an alarm device.<br /></li>
<li>You can set it to automatically turn off after a specified amount of time.<br /></li>
<li>You can choose which alarm event should be raised when the alarm is activiated (strobe, siren, strobe & siren).</li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/alarm-switch.src">View Alarm Switch - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Door Sensor</h3>

<ul>
<li>This is the official device handler for the Dome Door Sensor (DMWD1)</li>
<li>Supports all functionality that the device offers</li>
<li><a href="https://community.smartthings.com/t/release-dome-door-sensor-official/76321?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-door-sensor.src/dome-door-sensor.groovy">View Dome Door Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Door/Window Sensor Pro</h3>

<ul>
<li>This is the official device handler for the Dome Door/Window Sensor Pro (DMDP1)</li>
<li>Supports all functionality that the device offers</li>
<li><a href="https://community.smartthings.com/t/release-dome-door-window-sensor-pro-official/94739?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-door-window-sensor-pro.src/dome-door-window-sensor-pro.groovy">View Dome Door/Window Sensor Pro - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Leak Sensor</h3>

<ul>
<li>This is the official device handler for the Dome Leak Sensor (DMWS1)</li>
<li>Supports all functionality that the device offers</li>
<li><a href="https://community.smartthings.com/t/release-dome-leak-sensor-official/76154?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-leak-sensor.src/dome-leak-sensor.groovy">View Dome Leak Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Motion Sensor</h3>

<ul>
<li>This is the official device handler for the Dome Motion Sensor (DMMS1)</li>
<li><a href="https://community.smartthings.com/t/release-dome-motion-sensor-official/78092?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-motion-sensor.src/dome-motion-sensor.groovy">View Dome Motion Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Mouser</h3>

<ul>
<li>This is the official device handler for the Dome Mouser (DMMZ1)</li>
<li><a href="https://community.smartthings.com/t/release-dome-mouser-official/75732?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-mouser.src/dome-mouser.groovy">View Dome Mouser - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome On Off Plug</h3>

<ul>
<li>This is the official device handler for the Dome On Off Plug (DMOF1)</li>
<li><a href="https://community.smartthings.com/t/release-dome-motion-sensor-official/78092?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-on-off-plug.src/dome-on-off-plug.groovy">View Dome On Off Plug - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Siren</h3>

<ul>
<li>This is the official device handler for the Dome Siren (DMS01)</li>
<li>Supports all functionality that the device offers</li>
<li><a href="https://community.smartthings.com/t/release-dome-siren-official/75499?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-siren.src/dome-siren.groovy">View Dome Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Dome Water Shut-Off</h3>

<ul>
<li>This is the official device handler for the Dome Water Shut-Off (DMWV1)</li>
<li>You can open the valve with either Valve.open or Switch.on</li>
<li>You can close the valve with either Valve.close or Switch.off</li>
<li><a href="https://community.smartthings.com/t/release-dome-water-main-shut-off-official/75500?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/dome-water-shut-off.src/dome-water-shut-off.groovy">View Dome Water Shut-Off - Device Handler Code</a></li>
</ul>


<hr />
<h3>Ecolink Motion Sensor</h3>
<ul>
<li>This is a device handler for the Ecolink Motion Sensor (PIRZWAVE2.5-ECO)</li>
<li>Reports Motion, Battery, Tamper, and allows you to change the wakeup interval.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/ecolink-motion-sensor.src/ecolink-motion-sensor.groovy">View Ecolink Motion Sensor - Device Handler Code</a></li>
</ul>


<hr />
<h3>Everspring Motion Detector</h3>
<ul>
<li>This is a device handler for the Everspring Motion Detector (HSP02)</li>
<li>Reports Motion, Battery, Tamper.</li>
<li>Allows you to set a ambient light percentage and it raises the Contact Open event when the light level drops below that percentage and motion is detected.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/everspring-motion-detector.src/everspring-motion-detector.groovy">View Everspring Motion Detector - Device Handler Code</a></li>
</ul>


<hr />
<h3>Everspring Temperature/Humidity Detector</h3>
<ul>
<li>This is a device handler for the Everspring Temperature/Humidity Detector (ST814-2)</li>
<li>Reports Relative Humidity, Temperature, and Battery.</li>
<li>Allows you to choose whether Humidity or Temperature is displayed as in the Things list.</li>
<li>You can change the reporting interval for Temperature/Humidity or disable it.</li>
<li>You can specify a Temperature threshold and a Humidity threshold for reporting</li>
<li>Supports Temperature and Humidity offsets</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/everspring-temperature-humidity-detector.src/everspring-temperature-humidity-detector.groovy">View Everspring Temperature/Humidity Detector - Device Handler Code</a></li>
</ul>


<hr />
<h3>Fibaro Motion Sensor ZW5</h3>
<ul>
<li>This is a device handler for the Fibaro Motion Sensor ZW5 (FGMS-001)</li>
<li>Reports Motion, Light, Temperature, and Acceleration/Tamper.</li>
<li>It can also report either Earthquake magnitude or Three-Axis x,y,z.
<li>Simplifies all the configuration settings.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/fibaro-motion-sensor-zw5.src/fibaro-motion-sensor-zw5.groovy">View Fibaro Motion Sensor ZW5 - Device Handler Code</a></li>
</ul>


<hr />
<h3>Fibaro Swipe</h3>
<ul>
<li>This is a device handler for the Fibaro Swipe (FGGC-001)</li>
<li>The device supports 16 buttons that are mapped to the gestures and sequences</li>
<li>There's a label setting for each button that gets displayed on the device details screen</li>
<li>Allows you to choose the 2-3 gestures to use for each of the 6 sequences</li>
<li>Creates the button held event when circular gestures start and button pushed event when they stop</li>
<li>Double gestures can be disabled.</li>
<li><a href="https://community.smartthings.com/t/release-fibaro-swipe/88073?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/fibaro-swipe-gesture-controller.src/fibaro-swipe-gesture-controller.groovy">View Fibaro Swipe - Device Handler Code</a></li>
</ul>


<hr />
<h3>Forcible Mobile Presence</h3>
<ul>
<li>Adds the buttons "Arrive" and "Depart" to the normal "Mobile Presence" DTH so you can force the presence state.</li>
<li>The default DTH doesn't appear to run locally so I'm unaware of any downsides to using this one.</li>
<li>Those buttons can really come in handy if you're locked out of your house or your alarm won't disarm because your phone gets stuck on "not present".</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/forcible-mobile-presence.src/forcible-mobile-presence.groovy">View Forcible Mobile Presence - Device Handler Code</a></li>
</ul>

<hr />
<h3>GoControl/Linear Door/Window Sensor</h3>

<ul>
<li>DTH for the GoControl Linear Door/Window Sensor, Model: WADWAZ-1<br /></li>
<li>Automatically sets polling attribute so it can be monitored by SmartApps like the Simple Device Viewer to ensure it's stil online.
Supports the Tamper Alert Capability.<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-contact-sensor.src/gocontrol-contact-sensor.groovy">View GoControl/Linear Door/Window Sensor - Device Handler Code</a></li>
</ul>

<hr />
<h3>GoControl/Linear Multifunction Contact Sensor</h3>

<ul>
<li>Advanced device handler for the GoControl/Linear Contact Sensor (WADWAZ-1) that allows you to use the internal and external sensors as different capabilities.<br /></li>
<li>Supports the Contact Sensor, Water Sensor and Motion Sensor capabilities.<br></li>
<li>Choose which contact (internal/external/main) and which state (open/close) go with each of the motion and water states (wet/dry/active/inactive).<br></li>
<li>Choose which capability to use for the main tile.<br></li>
<li>Choose which capability to use for the secondary status on the main tile.<br></li>
<li>Choose default state to use for the capabilities that are not being used.<br></li>
<li>Has all the features that the basic version has like the ability to decide if the internal, external or a combination of both cause the Contact Capability to change.<br></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-linear-multifunction-contact-sensor/77659?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/gocontrol-multifunction-contact-sensor.src/gocontrol-multifunction-contact-sensor.groovy">View GoControl/Linear Multifunction Contact Sensor - Device Handler Code</a></li>
</ul>

<hr />
<h3>GoControl/Linear Motion Sensor</h3>

<ul>
<li>DTH for the GoControl/Linear Motion Sensor, Model: WAPIRZ-1<br /></li>
<li>Automatically sets polling attribute so it can be monitored by SmartApps like the Simple Device Viewer to ensure it's stil online.<br /></li>
<li>Supports the Tamper Alert Capability.<br /></li>
<li>Provides offset so you can adjust the temperature<br /></li>
<li>Provides threshold so you can prevent it from bouncing back and forth between the same 2 temperatures.<br /></li>
<li>Allows you to set the frequency that it checks the battery<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-motion-sensor.src/gocontrol-motion-sensor.groovy">View GoControl/Linear Motion Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>GoControl/Linear Multifunction Siren</h3>

<ul>
<li>DTH for the GoControl Siren, Linear Siren and possibly some other generic sirens. Models: ZM1601US / WA105DBZ-1<br /></li>

<li>It allows you to make the alarm turn off automatically, switch between siren/strobe/both on the fly, have it automatically turn off after a specified amount of time and it also allows you to make the device beep.  The custom commands can be sent to the device using he speaktext and playtext commands of the Music Player capability.<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-siren-linear-siren/47024?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-multifunction-siren.src/gocontrol-multifunction-siren.groovy">View GoControl/Linear Multifunction Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>LeakSmart Water Valve</h3>

<ul>
<li>DTH for the LeakSmart Water Valve and it polls regularly so you can use a SmartApp like the Simple Device Viewer to monitor it and receive notifications if it stops reporting.<br /></li>
<li><a href="https://community.smartthings.com/t/release-leaksmart-water-valve/48669?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/leaksmart-water-valve.src/leaksmart-water-valve.groovy">View LeakSmart Water Valve - Device Handler Code</a></li>
</ul>

<hr />

<h3>Monoprice Z-Wave Plus Door/Window Sensor</h3>

<ul>
<li>This is a device handler for the Monoprice Z-Wave Plus Door/Window Sensor (Model: P/N 15270)</li>
<li>It has the setting <em>Enable External Sensor</em> which enables the terminals so you can attach an external sensor.</li>
<li>The device wakes up every 6 hours by default, but there's a setting for <em>Minimum Check-in Interval (Hours)</em> which accepts the range 1 to 167.</li>
<li>There's also a setting for <em>Battery Reporting Interval (Hours)</em> which accepts the same range of values.</li>
<li>When the cover of the device is opened, it raises the "tamper" event with the value "detected".</li>
<li>The setting <em>Automatically Clear Tamper</em> allows you to choose whether it raises the tamper clear event when the device cover is closed or if you have to press the "Refresh" button to clear it.<br></li>
<li><a href="https://community.smartthings.com/t/release-monoprice-z-wave-plus-door-window-sensor/70478?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/monoprice-zwave-plus-door-window-sensor.src/monoprice-zwave-plus-door-window-sensor.groovy">View Monoprice Z-Wave Plus Door/Window Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Polling Cree Bulb</h3>
<ul>
<li>DTH for the Cree Connect Bulb that fixes the problem with the official Cree Bulb DTH where it doesn't remember the last level it was set to when it's turned on.<br /></li>
<li>It also reports at a regular interval so it can be monitored to ensure it doesn't drop offline.<br /></li>
<li>Allows you to control the dim rate.</li>
<li>I find this version to work a lot better than the default Cree Bulb DTH and since the default version doesn't run locally, there's really no downside to using my version.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/polling-cree-bulb.src">View Polling Cree Bulb - Device Handler Code</a></li>
</ul>

<hr />

<h3>Polling GE Link Bulb</h3>
<ul>
<li>DTH for the GE Link Bulb that reports at regular intervals so you can monitor it to ensure that it hasn't dropped offline.</li>
<li>The default GE Link Bull DTH responds to poll requests and runs locally so instead of using this one, you might be better off sticking with the default and using a SmartApp like pollster or my Simple Device Viewer to poll it a couple of times a day to make sure it's still online.</li> 
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/polling-ge-link-bulb.src">View Polling GE Link Bulb - Device Handler Code</a></li>
</ul>

<hr />

<h3>Remotec ZXT-310 IR Extender</h3>

<ul>
<li>This is a device handler for the Remotec Z-Wave-to-AV IR Extender (Model: ZXT-310)</li>
<li>The device handler provides 6 sets of 9 buttons which allows you to learn up to 54 IR Codes from other remote controls.</li>
<li>Each set of buttons can be configured to use the internal IR Port or any of the External Ports. The device has 5 external ports and comes with 3 - 6' external cables.</li>
<li>You can specify triggers for the 9 buttons. The options are Switch On, Switch Off, Switch On/Off, and Momentary Switch Push.</li>
<li>You can also push the buttons using any SmartApp that supports the Switch Level capability. Level 10% pushes button 1, 20% pushes button 2, etc.</li>
<li>The triggers and switch levels push the buttons for the active set of buttons, but to switch between the sets of buttons you need to tap the E1-E6 tiles or use a SmartApp like CoRE to execute the custom commands setActiveEP1 - setActiveEP6. Or use the optional SmartApp which will generate a separate virtual device for each set of buttons.</li>
<li>All you have to do to program a button is tap the "Learn" tile, tap the button you want to program, hold down the key on the remote control until the LED flashes twice, and then tap the "Learn" tile again.<br></li>
<li><a href="https://community.smartthings.com/t/release-remotec-zxt-310-z-wave-to-av-ir-extender/83472?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/remotec-zxt-310-ir-extender.src/remotec-zxt-310-ir-extender.groovy">View Remotec ZXT-310 IR Extender - Device Handler Code</a></li>
<li>View optional <a href="https://github.com/krlaframboise/SmartThings/blob/master/smartapps/krlaframboise/remotec-zxt-310-device-manager.src/remotec-zxt-310-device-manager.groovy">SmartApp code</a> and optional <a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/remotec-zxt-310-device.src/remotec-zxt-310-device.groovy">Child Device code</a> that allow you to use this device as 6 devices.</a></li>
</ul>

<hr />

<h3>ThingShield Timer</h3>

<ul>
<li>Allows you to use an external timing source, like the Arduino Uno, with the Arduino ThingShield to push a momentary switch at regular intervals.<br /> </li>
<li>It can also be used for scheduling in SmartApps so that you don't have to rely on SmartThings Scheduler.  You basically tell it to push a certain button number in a certain amount of time and then subscribe to its button pushed event and in the event handler method, you check to see if the button number pushed was the one you scheduled.<br /> </li>
<li>I created this because of all the problems SmartThings had with their scheduler, but the new version of their scheduler has been stable for a while so it's probably no longer needed.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/thingshield-timer.src">View ThingShield Timer - Device Handler Code</a></li>
</ul>

<hr />

<h3>Vision/Monoprice Shock Sensor</h3>

<ul>
<li>This is a device handler for the Vision Shock Sensor (ZS 5101).<br /></li>
<li>It's also a device handler for the Monoprice Shock Sensor (P/N 15269)</li>
<li>Choose between Motion and Acceleration as the capability to use for the primary status shown in the main tile which is activated by vibration.</li>
<li>Primary status automatically resets back to inactive shortly after vibration stops</li>
<li>Choose between None, Motion, Contact, Tamper and Water for the secondary status which is activated by the external sensor and/or tamper switch.</li>
<li>Once the secondary status is activated, the Refresh button needs to be tapped in order to reset it</li>
<li><a href="https://community.smartthings.com/t/release-vision-shock-sensor-zs-5101/81628?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/vision-shock-sensor.src/vision-shock-sensor.groovy">View Vision Shock Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Wireless Smoke Detector Sensor</h3>

<ul>
<li>This is a device handler for the Wireless Smoke Detector Sensor (ZWN-SD).<br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/wireless-smoke-detector-sensor.src/wireless-smoke-detector-sensor.groovy">View Wireless Smoke Detector Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zipato Multisound Siren</h3>

<ul>
<li>This is a device handler for the Zipato Z-Wave Indoor Multi-Sound Siren (PH-PSE02.US).  It's been tested on the US version, but it should work with the EU version.<br /></li>
<li><a href="https://community.smartthings.com/t/release-zipato-phileo-multisound-siren-ph-pse02-us/53748?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zipato-multisound-siren.src/zipato-multisound-siren.groovy">View Zipato Multisound Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz/Monoprice 4-in-1 Multisensor</h3>

<ul>
<li>This is a device handler for the Zooz 4-in-1 Multisensor (ZSE40).</li>
<li>It's also a device handler for the Monoprice 4-in-1 Motion Sensor with Temperature, Humidity, and Light Sensors (P/N 15902)</li>
<li><strong>After updating the settings, pressing the button on the bottom with a paperclip will automatically apply them.</strong></li>
<li>If you want to force all the values to refresh, tap the refresh button and then press the button on the bottom with a paperclip.</li> 
<li>The device wakes up every 6 hours by default, but there's a setting for <em>Minimum Check-in Interval (Hours)</em> which accepts the range 1 to 167.</li>
<li>There's also a setting for <em>Battery Reporting Interval (Hours)</em> which accepts the same range of values.</li>
<li>When the cover of the device is opened, it raises the "tamper" event with the value "detected".</li>
<li>The setting <em>Automatically Clear Tamper</em> allows you to choose whether it raises the tamper clear event when the device cover is closed or if you have to press the "Refresh" button to clear it.<br></li>
<li><a href="https://community.smartthings.com/t/release-zooz-4-in-1-multisensor/82989?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-monoprice-4-in-1-multisensor.src/zooz-monoprice-4-in-1-multisensor.groovy">View Zooz/Monoprice 4-in-1 Multisensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Power Strip</h3>

<ul>
<li>This is a device handler for the Zooz Z-Wave Power Strip (ZEN20).<br /></li>
<li>The 5 outlets can be controlled separately using the custom commands ch1On, ch1Off, ch2On, ch2Off, etc.  This requires the use of a SmartApp that supports custom commands, like CoRE.</li>
<li>Creates Digital events when turned on/off from SmartApp and Physical events when the buttons on the power strip are pushed.</li>
<li>The Main Switch Behavior setting for each outlet determines how it responds to the switch.on/switch.off commands.<br>
<ul>
<li><b>On/Off:</b> Switch.on command turns it on and Switch.off command turns it off.</li>
<li><b>On:</b> Switch.on command turns the outlet on, but Switch.off doesn't turn it off.</li>
<li><b>Off:</b> Switch.off turns the outlet off, but Switch.on doesn't turn it on.</li>
<li><b>None:</b> The outlet ignores the Switch.on and Switch.off commands.</li>
</ul>
</li>
<li>The Main Switch shows "on" when ANY of the outlets with the Main Switch Behavior set to "on" or "on/off" are on.</li>
<li>The Main Switch shows "off" when ALL of the outlets with the Main Switch Behavior set to "off" or "on/off" are off.</li>
<li>Main Switch Delay setting allows the Main Switch to turn the outlets on/off gradually instead of all at once.</li>
<li><a href="https://community.smartthings.com/t/release-zooz-power-strip/68860?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozPowerStrip" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-power-strip.src/zooz-power-strip.groovy">View Zooz Power Strip - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Power Switch/Zooz Smart Plug</h3>

<ul>
<li>This is a device handler for the Zooz Z-Wave Power Switch (ZEN15) and Zooz Z-Wave Smart Plug (ZEN06).<br /></li>
<li>Reports Power, Energy, Voltage, and Current</li>
<li>Tracks high and low values for Power, Voltage, and Current.</li>
<li>Reports Energy Duration and Cost.</li>
<li>Creates Digital events when turned on/off from SmartApp and Physical events when the button is used.</li>
<li>Optionally display Power, Energy, Voltage, and Current events in the Recently tab.</li>
<li><a href="https://community.smartthings.com/t/release-zooz-power-switch-zooz-smart-plug/97220?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozPowerSwitch" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-power-switch.src/zooz-power-switch.groovy">View Zooz Power Switch/Zooz Smart Plug - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Smart Chime</h3>

<ul>
<li>This is a device handler for the Zooz Smart Chime (ZSE33).<br /></li>
<li>Has 10 sounds that can be used as chimes or sirens</li>
<li>Has 3 volume settings that can be set for chime and siren.</li>
<li>Optionally use flashing LED for chime and siren.</li>
<li>Use device as alarm to play the siren sound, switch to play the chime.</li>
<li>Use the customChime command to play a sound by number.</li>
<li><a href="https://community.smartthings.com/t/release-zooz-smart-chime/77152?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozSmartChime" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-smart-chime.src/zooz-smart-chime.groovy">View Zooz Smart Chime - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Water Sensor</h3>

<ul>
<li>This is a device handler for the Zooz Water Sensor (ZSE30).<br /></li>
<li>Red LED and optional audbile alarm when water is detected.</li>
<li>Specify the first alarm beep duration, reminder beep duration, interval between beeps, and the total length of time it should send reminders.</li>
<li><a href="https://community.smartthings.com/t/release-zooz-water-sensor/78223?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozWaterSensor" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-water-sensor.src/zooz-water-sensor.groovy">View Zooz Water Sensor - Device Handler Code</a></li>
</ul>
