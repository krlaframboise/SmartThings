<a name="ReadMeAnchor"></a>

<h1>Kevin LaFramboise's Device Type Handlers</h1>

<p><strong>I'm posting this code on GitHub so that anyone can use it, but this is a private repository so pull requests will be ignored.  If you find a problem or want something added, please post a message on the corresponding topic in the SmartThings forum.</strong></p> 

<p>If you like my Device Type Handlers and would like to make a donation, please go to https://www.paypal.me/krlaframboise</p>

<hr />

<ul>
		<li><a href="#aeotec-doorbell">Aeotec Doorbell GEN5</a></li>
		<li><a href="#aeotec-doorbell-6">Aeotec Doorbell 6</a></li>
		<li><a href="#aeotec-led-bulb-6-multi-white">Aeotec LED Bulb 6 Multi-White</a></li>
		<li><a href="#aeotec-nanomote">Aeotec NanoMote One</a></li>
		<li><a href="#aeotec-nanomote">Aeotec NanoMote Quad</a></li>
		<li><a href="#aeon-labs-multifunction-siren">Aeotec Siren GEN5</a></li>
		<li><a href="#aeotec-siren-6">Aeotec Siren 6</a></li>
		<li><a href="#aeotec-trisensor">Aeotec TriSensor</a></li>
		<li><a href="#besense-motion-sensor-zwave-plus">BeSense 360 Ceiling Sensor</a></li>
		<li><a href="#besense-doorwindow-sensor-zwave-plus">BeSense Door/Window Sensor</a></li>
		<li><a href="#besense-motion-sensor-zwave-plus">BeSense PIR Wall Sensor</a></li>
		<li><a href="#dome-door-sensor">Dome Door Sensor</a></li>
		<li><a href="#dome-doorwindow-sensor-pro">Dome Door/Window Sensor Pro</a></li>
		<li><a href="#dome-leak-sensor">Dome Leak Sensor</a></li>
		<li><a href="#dome-motion-sensor">Dome Motion Sensor</a></li>
		<li><a href="#dome-mouser">Dome Mouser</a></li>
		<li><a href="#dome-on-off-plug">Dome On Off Plug</a></li>
		<li><a href="#dome-siren">Dome Siren</a></li>
		<li><a href="#dome-water-shut-off">Dome Water Shut-Off</a></li>
		<li><a href="#ecolink-motion-sensor">Ecolink Motion Sensor</a></li>
		<li><a href="#ecolink-wireless-switch">Ecolink Motorized Double Rocker Switch</a></li>
		<li><a href="#ecolink-wireless-switch">Ecolink Motorized Double Toggle Switch</a></li>
		<li><a href="#ecolink-wireless-switch">Ecolink Motorized Rocker Switch</a></li>
		<li><a href="#ecolink-wireless-switch">Ecolink Motorized Toggle Switch</a></li>
		<li><a href="#ecolink-siren">Ecolink Siren</a></li>
		<li><a href="#eva-logik-in-wall-smart-dimmer">EVA LOGIK In-Wall Smart Dimmer</a></li>
		<li><a href="#eva-logik-in-wall-smart-switch">EVA LOGIK In-Wall Smart Switch</a></li>			
		<li><a href="#everspring-motion-detector">Everspring Motion Detector</a></li>
		<li><a href="#everspring-temperaturehumidity-detector">Everspring Temperature/Humidity Detector</a></li>
		<li><a href="#fibaro-doorwindow-sensor-2">Fibaro Door/Window Sensor 2</a></li>
		<li><a href="#fibaro-motion-sensor-zw5">Fibaro Motion Sensor ZW5</a></li>
		<li><a href="#fibaro-swipe">Fibaro Swipe</a></li>
		<li><a href="#gocontrollinear-doorwindow-sensor">GoControl/Linear Door/Window Sensor</a></li>
		<li><a href="#gocontrollinear-multifunction-contact-sensor">GoControl/Linear Multifunction Contact Sensor</a></li>
		<li><a href="#gocontrollinear-motion-sensor">GoControl/Linear Motion Sensor</a></li>
		<li><a href="#gocontrollinear-multifunction-siren">GoControl/Linear/Vision Multifunction Siren</a></li>
		<li><a href="#aeotec-nanomote">Hank Scene Controller</a></li>
		<li><a href="#aeotec-nanomote">Hank Four-Key Scene Controller</a></li>
		<li><a href="#hank-rgbw-led-bulb">Hank RGBW LED Bulb</a></li>
		<li><a href="#leaksmart-water-valve">LeakSmart Water Valve</a></li>
		<li><a href="#eva-logik-in-wall-smart-dimmer">MINOSTON In-Wall Smart Dimmer</a></li>
		<li><a href="#eva-logik-in-wall-smart-switch">MINOSTON In-Wall Smart Switch</a></li>			
		<li><a href="#visionmonoprice-shock-sensor">Monoprice Shock Sensor</a></li>
		<li><a href="#monoprice-z-wave-plus-doorwindow-sensor">Monoprice Z-Wave Plus Door/Window Sensor</a></li>
		<li><a href="#neo-coolcam-door-sensor">Neo Coolcam Door Sensor</a></li>
		<li><a href="#neo-coolcam-light-switch-2ch">Neo Coolcam Light Switch 2CH</a></li>
		<li><a href="#neo-coolcam-motion-sensor">Neo Coolcam Motion Sensor</a></li>
		<li><a href="#neo-coolcam-power-plug">Neo Coolcam Power Plug</a></li>
		<li><a href="#neo-coolcam-siren">Neo Coolcam Siren</a></li>
		<li><a href="#qubino-roller-shade-controller">Qubino Shutter Module</a></li>
		<li><a href="#remotec-zxt-310-ir-extender">Remotec ZXT-310 IR Extender</a></li>
		<li><a href="#strips-multi-sensor">Strips Comfort by Sensative</a></li>
		<li><a href="#strips-multi-sensor">Strips Drip by Sensative</a></li>
		<li><a href="#vision-recessed-door-sensor">Vision Recessed Door Sensor</a></li>
		<li><a href="#visionmonoprice-shock-sensor">Vision Shock Sensor</a></li>
		<li><a href="#wireless-smoke-detector-sensor">Wireless Smoke Detector Sensor</a></li>
		<li><a href="#zipato-multisound-siren">Zipato Multisound Siren</a></li>
		<li><a href="#zooz-4-in-1-sensor">Zooz 4-in-1 Sensor</a></li>
		<li><a href="#zooz-double-plug">Zooz Double Plug</a></li>			
		<li><a href="#zooz-double-switch">Zooz Double Switch</a></li>			
		<li><a href="#zooz-motion-sensor-zse18">Zooz Motion Sensor ZSE18</a></li>
		<li><a href="#zooz-multirelay">Zooz MultiRelay</a></li>
		<li><a href="#zooz-multisiren">Zooz Multisiren</a></li>
		<li><a href="#zooz-outdoor-motion-sensor">Zooz Outdoor Motion Sensor</a></li>
		<li><a href="#zooz-outdoor-motion-sensor-ver-20">Zooz Outdoor Motion Sensor VER 2.0</a></li>
		<li><a href="#zooz-power-switchzooz-smart-plug">Zooz Smart Plug</a></li>
		<li><a href="#zooz-power-strip-ver-20">Zooz Power Strip VER 2.0</a></li>
		<li><a href="#zooz-power-strip">Zooz Power Strip</a></li>
		<li><a href="#zooz-power-switchzooz-smart-plug">Zooz Power Switch</a></li>
		<li><a href="#zooz-rgbw-dimmer">Zooz RGBW Dimmer</a></li>
		<li><a href="#zooz-smart-chime">Zooz Smart Chime</a></li>
		<li><a href="#zooz-power-switchzooz-smart-plug">Zooz Smart Plug</a></li>
		<li><a href="#zooz-smart-plug-ver-20">Zooz Smart Plug VER 2.0</a></li>
		<li><a href="#zooz-water-sensor">Zooz Water Sensor</a></li>
</ul>

<hr />

<h3>Aeon Labs Multifunction Siren</h3>

<ul>
<li>DTH for the Aeon Labs Siren that provides features like beeping, auto off, delayed alarm, beep scheduling for things like beeping during entry and exit.<br /></li>
<li><a href="https://community.smartthings.com/t/release-aeon-labs-multifunction-siren/40652?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeon-labs-multifunction-siren.src">View Aeon Labs Multifunction Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec Doorbell</h3>

<ul>
<li>DTH for the Aeon Labs Aeotec Doorbell that allows you to use the device as a Switch, Alarm, Tone Generator, Music Player, and Audio Notification. Implements custom commands to allow you to play tracks by track number and change the volume on the fly.<br /></li>

<li><a href="https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeotec-doorbell.src">View Aeotec Doorbell - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec Doorbell 6</h3>

<ul>
<li>This is a device handler for the Aeotec Doorbell 6 (ZW162-A)</li>
<li><a href="https://community.smartthings.com/t/release-aeotec-doorbell-6/165030">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-doorbell-6.src/aeotec-doorbell-6.groovy">View Aeotec Doorbell 6 - Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-doorbell-6-button.src/aeotec-doorbell-6-button.groovy">View Aeotec Doorbell 6 Button - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec LED Bulb 6 Multi-White</h3>

<ul>
<li>This is a device handler for the Aeotec LED Bulb 6 Multi-White (ZWA001-A)</li>
<li><a href="https://community.smartthings.com/t/release-aeotec-led-bulb-6-multi-white/142910?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-led-bulb-6-multi-white.src/aeotec-led-bulb-6-multi-white.groovy">View Aeotec LED Bulb 6 Multi-White - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec NanoMote</h3>

<ul>
<li>This is a device handler for:
<ul><li>Aeotec NanoMote One (ZWA003-A)</li>
<li>Aeotec NanoMote Quad (ZWA004-A)</li>
<li>Hank Scene Controller (HKZW-SCN01)</li>
<li>Hank Four-Key Scene Controller (HKZW-SCN04)</li></ul></li>
<li><a href="https://community.smartthings.com/t/release-aeotec-nanomote-one-quad-hank-one-four-button-scene-controller/127563">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-nanomote.src/aeotec-nanomote.groovy">View Aeotec NanoMote - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec Siren 6</h3>

<ul>
<li>This is a device handler for the Aeotec Siren 6 (ZW164-A)</li>
<li><a href="https://community.smartthings.com/t/release-aeotec-siren-6/164654">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-siren-6.src/aeotec-siren-6.groovy">View Aeotec Siren 6 - Device Handler Code</a></li>
</ul>

<hr />

<h3>Aeotec TriSensor</h3>

<ul>
<li>This is a device handler for the Aeotec TriSensor (ZWA005-A)</li>
<li><a href="https://community.smartthings.com/t/release-aeotec-trisensor/140556?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/aeotec-trisensor.src/aeotec-trisensor.groovy">View Aeotec TriSensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>BeSense Motion Sensor ZWave Plus</h3>

<ul>
<li>This is a device handler for:
<ul><li>BeSense 360 Ceiling Sensor (IX32)</li>
<li>BeSense PIR Wall Sensor (IX30)</li></ul></li>
<li>Supports all functionality that the devices offer</li>
<!--<li><a href="">View Documentation in SmartThings Forum</a></li>-->
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/besense-motion-sensor-zwave-plus.src/besense-motion-sensor-zwave-plus.groovy">View BeSense Motion Sensor ZWave Plus - Device Handler Code</a></li>
</ul>

<hr />

<h3>BeSense Door/Window Sensor ZWave Plus</h3>

<ul>
<li>This is the official device handler for the BeSense Door/Window Sensor (IM20)</li>
<li>Supports all functionality that the device offers</li>
<!--<li><a href="">View Documentation in SmartThings Forum</a></li>-->
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/besense-door-window-sensor-zwave-plus.src/besense-door-window-sensor-zwave-plus.groovy">View BeSense Door/Window Sensor ZWave Plus - Device Handler Code</a></li>
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
<h3>Ecolink Wireless Switch</h3>
<ul>
<li>This is a device handler for:<ul>
<li>Ecolink Motorized Double Rocker Switch (DDLS2-ZWAVE5)</li>
<li>Ecolink Motorized Double Toggle Switch (DTLS2-ZWAVE5)</li>
<li>Ecolink Motorized Rocker Switch (DLS-ZWAVE5)</li>
<li>Ecolink Motorized Toggle Switch (TLS-ZWAVE5)</li>
</ul></li>
<li>Reports Switch, Battery, and allows you to change the wakeup interval.</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/ecolink-wireless-switch.src/ecolink-wireless-switch.groovy">View Ecolink Wireless Switch - Device Handler Code</a></li>
</ul>

<hr />
<h3>Ecolink Siren</h3>
<ul>
<li>This is a device handler for the Ecolink Siren (SC-ZWAVE5-ECO)</li>
<li>The device is fully functional if selected as switch:
 <ul><li>Switch On: Siren On</li>
 <li>Switch Off: Turns Everything Off</li>
 <li>Set Level 10%: Chime/Beep</li>
 <li>Set Level 20%: Entry/Continuous Tone</li>
 <li>Set Level 30%: Exit/Repeating Beep</li>
 </ul></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/ecolink-siren.src/ecolink-siren.groovy">View Ecolink Siren - Device Handler Code</a></li>
</ul>


<hr />
<h3>EVA LOGIK In-Wall Smart Dimmer</h3>
<ul>
<li>This is a device handler for:
<ul><li>EVA LOGIK In-Wall Smart Dimmer (ZW31)</li>
<li>MINOSTON In-Wall Smart Dimmer (MS11Z)</li></ul></li>
<li><a href="https://community.smartthings.com/t/release-eva-logik-zw31-minoston-ms11z-in-wall-dimmer/198305">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/eva-logik-in-wall-smart-dimmer.src/eva-logik-in-wall-smart-dimmer.groovy">View EVA LOGIK In-Wall Smart Dimmer - Device Handler Code</a></li>
</ul>


<hr />
<h3>EVA LOGIK In-Wall Smart Switch</h3>
<ul>
<li>This is a device handler for:
<ul><li>EVA LOGIK In-Wall Smart Switch (ZW30)</li>
<li>MINOSTON In-Wall Smart Switch (MS10Z)</li></ul></li>
<li><a href="https://community.smartthings.com/t/release-eva-logik-zw30-minoston-ms10z-in-wall-switch/198306">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/eva-logik-in-wall-smart-switch.src/eva-logik-in-wall-smart-switch.groovy">View EVA LOGIK In-Wall Smart Switch - Device Handler Code</a></li>
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
<h3>Fibaro Door/Window Sensor 2</h3>
<ul>
<li>This is a device handler for the Fibaro Door/Window Sensor 2(FGDW-002)</li>
<li>Reports Contact, Temperature, and Tamper</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/fibaro-door-window-sensor-2.src/fibaro-door-window-sensor-2.groovy">View Fibaro Door/Window Sensor 2 - Device Handler Code</a></li>
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

<h3>Hank RGBW LED Bulb</h3>

<ul>
<li>This is a DTH for the Hank RGBW LED Bulb (Model: HKZW-RGB01)<br /></li>
<li><a href="https://community.smartthings.com/t/release-hank-rgbw-led-bulb/127560">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/hank-rgbw-led-bulb.src/hank-rgbw-led-bulb.groovy">View Hank RGBW LED Bulb - Device Handler Code</a></li>
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

<h3>Neo Coolcam Door Sensor</h3>

<ul>
<li>This is a device handler for the Neo Coolcam Door Sensor (NAS-DS02ZU / NAS-DS02ZE)</li>
<li><a href="https://community.smartthings.com/t/release-neo-coolcam-door-window-sensor/145827">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/neo-coolcam-door-sensor.src/neo-coolcam-door-sensor.groovy">View Neo Coolcam Door Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Neo Coolcam Light Switch 2CH</h3>

<ul>
<li>This is a device handler for the Neo Coolcam Light Switch 2CH (NAS-SC02ZU-2 / NAS-SC02ZE-2)</li>
<li><a href="https://community.smartthings.com/t/release-neo-coolcam-light-switch-2ch/147756?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/neo-coolcam-light-switch-2ch.src/neo-coolcam-light-switch-2ch.groovy">View Neo Coolcam Light Switch 2CH - Device Handler Code</a></li>
</ul>

<hr />

<h3>Neo Coolcam Motion Sensor</h3>

<ul>
<li>This is a device handler for the Neo Coolcam Motion Sensor (NAS-PD01ZU-T / NAS-PD01ZE-T)</li>
<li><a href="https://community.smartthings.com/t/release-neo-coolcam-motion-sensor-nas-pd01zu-t/143096?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/neo-coolcam-motion-sensor.src/neo-coolcam-motion-sensor.groovy">View Neo Coolcam Motion Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Neo Coolcam Power Plug</h3>

<ul>
<li>This is a device handler for the Neo Coolcam Power Plug (NAS-WR02ZU, NAS-WR02ZE)</li>
<li><a href="https://community.smartthings.com/t/release-neo-coolcam-power-plug/144274?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/neo-coolcam-power-plug.src/neo-coolcam-power-plug.groovy">View Neo Coolcam Power Plug - Device Handler Code</a></li>
</ul>

<hr />

<h3>Neo Coolcam Siren</h3>

<ul>
<li>This is a device handler for the Neo Coolcam Siren (NAS-AB02ZU)</li>
<li><a href="https://community.smartthings.com/t/release-neo-coolcam-siren/156537?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/neo-coolcam-siren.src/neo-coolcam-siren.groovy">View Neo Coolcam Siren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Qubino Roller Shade Controller</h3>

<ul>
<li>This is a device handler for the Qubino DC Shutter Module (ZMNHOD3)</li>
<li><a href="https://community.smartthings.com/t/motorized-roller-shades-project/146502?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/qubino-roller-shade-controller.src/qubino-roller-shade-controller.groovy">View Qubino Roller Shade Controller - Device Handler Code</a></li>
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

<h3>Strips Multi-Sensor</h3>

<ul>
<li>This is a device handler for the Strips Comfort by Sensative and Strips Drip by Sensative<br /></li>
<li>Allows you to change the primary and secondary tiles</li>
<li>Supports configuration parameters</li>
<li>Reports Light, Temperature, and Water for both devices</li>
<li><a href="https://community.smartthings.com/t/release-strips-drip-strips-comfort/135276?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/strips-multi-sensor.src/strips-multi-sensor.groovy">View Strips Multi-Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Vision Recessed Door Sensor</h3>

<ul>
<li>This is a device handler for the Vision Recessed Door Sensor (ZD2105US-5).<br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/vision-recessed-door-sensor.src/vision-recessed-door-sensor.groovy">View Vision Recessed Door Sensor - Device Handler Code</a></li>
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

<h3>Zooz 4-in-1 Sensor</h3>

<ul>
<li>This is a device handler for the Zooz 4-in-1 Sensor (ZSE40).</li>
<li><strong>After updating the settings, pressing the button on the bottom with a paperclip will automatically apply them.</strong></li>
<li>If you want to force all the values to refresh, tap the refresh button and then press the button on the bottom with a paperclip.</li>
<li>The device wakes up every 6 hours by default, but there's a setting for <em>Minimum Check-in Interval (Hours)</em> which accepts the range 1 to 167.</li>
<li>There's also a setting for <em>Battery Reporting Interval (Hours)</em> which accepts the same range of values.</li>
<li>When the cover of the device is opened, it raises the "tamper" event with the value "detected".</li>
<li>The setting <em>Automatically Clear Tamper</em> allows you to choose whether it raises the tamper clear event when the device cover is closed or if you have to press the "Refresh" button to clear it.<br></li>
<li><a href="https://community.smartthings.com/t/release-zooz-4-in-1-sensor/82989?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-4-in-1-sensor.src/zooz-4-in-1-sensor.groovy">View Zooz 4-in-1 Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Double Plug</h3>

<ul>
<li>This is a device handler for the Zooz Double Plug (ZEN25)</li>
<li><a href="https://community.smartthings.com/t/release-zooz-double-plug/151104?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-double-plug.src/zooz-double-plug.groovy">View Zooz Double Plug - Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-double-plug-outlet.src/zooz-double-plug-outlet.groovy">View Zooz Double Plug Outlet - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Double Switch</h3>

<ul>
<li>This is a device handler for the Zooz Double Switch (ZEN30)</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-double-switch.src/zooz-double-switch.groovy">View Zooz Double Switch - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Motion Sensor ZSE18</h3>

<ul>
<li>This is a device handler for the Zooz Motion Sensor (ZSE18)<br /></li>
<li>Reports Motion, Acceleration, and Battery</li>
<li><a href="https://community.smartthings.com/t/release-zooz-motion-sensor-zse18/129743?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozMotionSensorZSE" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-motion-sensor-zse18.src/zooz-motion-sensor-zse18.groovy">View Zooz Motion Sensor ZSE18 - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz MultiRelay</h3>

<ul>
<li>This is a device handler for the Zooz MultiRelay (ZEN16)<br /></li>
<li><a href="https://community.smartthings.com/t/release-zooz-multirelay-zen16/181057?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-multirelay.src/zooz-multirelay.groovy">View Zooz MultiRelay - Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/child-switch.src/child-switch.groovy">View Child Switch - Device Handler Code (OPTIONAL)</a></li>
</ul>

<hr />

<h3>Zooz Multisiren</h3>

<ul>
<li>This is a device handler for the Zooz Multisiren (ZSE19)<br /></li>
<li><a href="https://community.smartthings.com/t/release-zooz-s2-multisiren-zse19/142891?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-s2-multisiren.src/zooz-s2-multisiren.groovy">View Zooz Multisiren - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Outdoor Motion Sensor</h3>

<ul>
<li>This is a device handler for the Zooz Outdoor Motion Sensor (ZSE29)<br /></li>
<li>Reports Motion, Battery, and Tamper</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-outdoor-motion-sensor.src/zooz-outdoor-motion-sensor.groovy">View Zooz Outdoor Motion Sensor - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Outdoor Motion Sensor VER 2.0</h3>

<ul>
<li>This is a device handler for the Zooz Outdoor Motion Sensor VER 2.0(ZSE29)<br /></li>
<li>Reports Illuminance, Motion, Battery, and Tamper</li>
<li>Supports Configuration Parameters</li>
<li><a href="https://community.smartthings.com/t/release-zooz-outdoor-motion-sensor-ver-2-0-zse29/180195?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-outdoor-motion-sensor-2-0.src/zooz-outdoor-motion-sensor-2-0.groovy">View Zooz Outdoor Motion Sensor VER 2.0 - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Power Strip VER 2.0</h3>

<ul>
<li>This is a device handler for the Zooz Power Strip VER 2.0 (ZEN20).<br /></li>
<li>A device is created for the Power Strip and each Outlet<br></li>
<li>Power Strip reports combined power/energy<br></li>
<li>Outlets report power/energy<br></li>
<li>A Component Switch is created for each USB Port and they report ON when devices plugged into them are using power. The USB Ports can not be controlled and will not appear in your device list, but they will appear in Smart Apps<br></li>
<li>Keeps history of low and high power values<br></li>
<li>The Power Switch device can turn on/off all outlets at the same time or you can choose a delay to use between them.<br></li>
<li>Enable/Disable Manual Operation<br></li>
<li>Power recovery options<br></li>
<li>LED options<br></li>
<li>Auto on/off intervals for each Outlet<br></li>
<li>Power and Energy reporting intervals<br></li>
<li>Power reporting threshold<br></li>
<li><a href="https://community.smartthings.com/t/release-zooz-power-strip-ver-2-0/138231?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-power-strip-ver-2-0.src/zooz-power-strip-ver-2-0.groovy">View Zooz Power Strip VER 2.0 - Power Strip Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-power-strip-outlet-ver-2-0.src/zooz-power-strip-outlet-ver-2-0.groovy">View Zooz Power Strip VER 2.0 - Outlet Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-power-strip-usb-ver-2-0.src/zooz-power-strip-usb-ver-2-0.groovy">View Zooz Power Strip VER 2.0 - USB Port Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Power Strip</h3>

<ul>
<li>This is a device handler for the OLD Discontinued version of the Zooz Z-Wave Power Strip (ZEN20).<br /></li>
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

<h3>Zooz RGBW Dimmer</h3>

<ul>
<li>This is a device handler for the Zooz RGBW Dimmer (ZEN31)<br /></li>
<li><a href="https://community.smartthings.com/t/release-zooz-rgbw-dimmer-zen31/178616?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-rgbw-dimmer.src/zooz-rgbw-dimmer.groovy">View Zooz RGBW Dimmer - Device Handler Code</a></li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/child-dimmer.src/child-dimmer.groovy">View Child Dimmer - Device Handler Code</a></li>
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

<h3>Zooz Smart Plug VER 2.0</h3>

<ul>
<li>This is a device handler for the Zooz Smart Plug VER 2.0 (ZEN06)</li>
<li><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-smart-plug.src/zooz-smart-plug.groovy">View Zooz Smart Plug VER 2.0 - Device Handler Code</a></li>
</ul>

<hr />

<h3>Zooz Water Sensor</h3>

<ul>
<li>This is a device handler for the Zooz Water Sensor (ZSE30).<br /></li>
<li>Red LED and optional audible alarm when water is detected.</li>
<li>Specify the first alarm beep duration, reminder beep duration, interval between beeps, and the total length of time it should send reminders.</li>
<li><a href="https://community.smartthings.com/t/release-zooz-water-sensor/78223?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
<li><a id="ZoozWaterSensor" href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zooz-water-sensor.src/zooz-water-sensor.groovy">View Zooz Water Sensor - Device Handler Code</a></li>
</ul>
