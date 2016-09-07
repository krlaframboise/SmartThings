<a name="ReadMeAnchor"></a>
<h1>Welcome to Kevin LaFramboise's SmartThings Repository</h1>

Below you will find a list of the SmartApps and Device Handlers that I've created.  I've included a short description of what they can do and included links to their topics in the SmartThings forum.

I don't think SmartThings developers should charge for the things they create, but donations are always appreciated.  If you would like to make a donation, please go to https://www.paypal.me/krlaframboise

<hr />

<h2>SmartApps</h2>

<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/smartapps/krlaframboise/blink-system-connector.src/blink-system-connector.groovy">Blink System Connector</a></h3>

<ul>
<li>Allows you to use your Blink Cameras with SmartThings.<br /></li>
<li>Provides summary information about the Blink sync module and cameras.<br /></li>

<li>Add/remove and view the status of your Blink Cameras<br /></li>

<li>Manually Arm and Disarm all the cameras.<br /></li>

<li>Automatically Arm/Disarm your cameras when Smart Home Monitor arms and disarms (optional).<br /></li>

<li><a href="https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/smartapps/krlaframboise/home-presence-manager.src/home-presence-manager.groovy">Home Presence Manager</a></h3>
<ul>
<li>Uses Motion Sensors, Contact Sensors and Virtual Presence Sensors to keep track of the room you're in and turn on and off lights as you move throughout the house.<br /></li>

<li><a href="https://community.smartthings.com/t/release-home-presence-manager/48449?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/smartapps/krlaframboise/simple-device-viewer.src/simple-device-viewer.groovy">Simple Device Viewer</a></h3>

<ul>
<li>Allows you to easily see a list of information about your devices like battery percentages, temperatures, how long since last event, switch state, etc.<br /></li>

<li>Receive Push and/or SMS notifications based on temperature, battery level, and/or time since last event.<br /></li>

<li>It can automatically poll the devices at a specified interval.<br /></li>

<li>Turn Off All Lights and/or Switches with a push of a button.<br /></li>

<li><a href="https://community.smartthings.com/t/release-simple-device-viewer/42481?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
</ul>

<hr />

<h2>Device Type Handlers</h2>

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeotec-doorbell.src">Aeon Labs Aeotec Doorbell</a></h3>

<ul>
<li>DTH for the Aeon Labs Aeotec Doorbell that allows you to use the device as a Switch, Alarm, Tone Generator, Music Player, and Audio Notification. Implements custom commands to allow you to play tracks by track number and change the volume on the fly.<br /></li>

<li><a href="https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/aeon-labs-multifunction-siren.src">Aeon Labs Multifunction Siren</a></h3>

<ul>
<li>DTH for the Aeon Labs Siren that provides features like beeping, auto off, delayed alarm, beep scheduling for things like beeping during entry and exit.<br /></li>
<li><a href="https://community.smartthings.com/t/release-aeon-labs-multifunction-siren/40652?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/alarm-switch.src">Alarm Switch</a></h3>
<ul>
<li>Allows you to use any device that supports the Switch Capability as an alarm device.<br /></li>
<li>You can set it to automatically turn off after a specified amount of time.<br /></li>
<li>You can choose which alarm event should be raised when the alarm is activiated (strobe, siren, strobe & siren).</li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/blink-wireless-camera.src">Blink Wireless Camera</a></h3>

<ul>
<li>Allows you to enable/disable cameras independently.<br /></li>

<li>The device's arm/disarm button will arm/disarm all of the cameras.<br /></li>

<li>Displays image from blink homescreen<br /></li>

<li>Allows you to take photos.<br /></li>

<li>Displays time of last 5 motion events<br /></li>

<li>Can display thumbnail associated with each event<br /></li>

<li>Displays and raises events for Temperature, Battery, Motion, WiFI Signal, Sync Module Signal, Camera Status, System Status, and Switch status<br /></li>


<li><a href="https://community.smartthings.com/t/release-blink-camera-device-handler-smartapp/44100?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>


<hr />
<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/forcible-mobile-presence.src/forcible-mobile-presence.groovy">Forcible Mobile Presence</a></h3>
<ul>
<li>Adds the buttons "Arrive" and "Depart" to the normal "Mobile Presence" DTH so you can force the presence state.</li>
<li>The default DTH doesn't appear to run locally so I'm unaware of any downsides to using this one.</li>
<li>Those buttons can really come in handy if you're locked out of your house or your alarm won't disarm because your phone gets stuck on "not present".</li>
</ul>

<hr />
<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-contact-sensor.src/gocontrol-contact-sensor.groovy">GoControl/Linear Door/Window Sensor</a></h3>

<ul>
<li>DTH for the GoControl Linear Door/Window Sensor, Model: WADWAZ-1<br /></li>
<li>Automatically sets polling attribute so it can be monitored by SmartApps like the Simple Device Viewer to ensure it's stil online.
Supports the Tamper Alert Capability.<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />
<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-motion-sensor.src/gocontrol-motion-sensor.groovy">GoControl/Linear Motion Sensor</a></h3>

<ul>
<li>DTH for the GoControl/Linear Motion Sensor, Model: WAPIRZ-1<br /></li>
<li>Automatically sets polling attribute so it can be monitored by SmartApps like the Simple Device Viewer to ensure it's stil online.<br /></li>
<li>Supports the Tamper Alert Capability.<br /></li>
<li>Provides offset so you can adjust the temperature<br /></li>
<li>Provides threshold so you can prevent it from bouncing back and forth between the same 2 temperatures.<br /></li>
<li>Allows you to set the frequency that it checks the battery<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/gocontrol-multifunction-siren.src/gocontrol-multifunction-siren.groovy">GoControl/Linear Multifunction Siren</a></h3>

<ul>
<li>DTH for the GoControl Siren, Linear Siren and possibly some other generic sirens. Models: ZM1601US / WA105DBZ-1<br /></li>

<li>It allows you to make the alarm turn off automatically, switch between siren/strobe/both on the fly, have it automatically turn off after a specified amount of time and it also allows you to make the device beep.  The custom commands can be sent to the device using he speaktext and playtext commands of the Music Player capability.<br /></li>
<li><a href="https://community.smartthings.com/t/release-gocontrol-siren-linear-siren/47024?u=krlaframboise">View Documentation in SmartThings Forum</a></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/leaksmart-water-valve.src/leaksmart-water-valve.groovy">LeakSmart Water Valve</a></h3>

<ul>
<li>DTH foor the LeakSmart Water Valve and it polls regularly so you can use a SmartApp like the Simple Device Viewer to monitor it and receive notifications if it stops reporting.<br /></li>
<li><a href="https://community.smartthings.com/t/release-leaksmart-water-valve/48669?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/polling-cree-bulb.src">Polling Cree Bulb</a></h3>
<ul>
<li>DTH for the Cree Connect Bulb that fixes the problem with the official Cree Bulb DTH where it doesn't remember the last level it was set to when it's turned on.<br /></li>
<li>It also reports at a regular interval so it can be monitored to ensure it doesn't drop offline.<br /></li>
<li>Allows you to control the dim rate.</li>
<li>I find this version to work a lot better than the default Cree Bulb DTH and since the default version doesn't run locally, there's really no downside to using my version.</li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/polling-ge-link-bulb.src">Polling GE Link Bulb</a></h3>
<ul>
<li>DTH for the GE Link Bulb that reports at regular intervals so you can monitor it to ensure that it hasn't dropped offline.</li>
<li>The default GE Link Bull DTH responds to poll requests and runs locally so instead of using this one, you might be better off sticking with the default and using a SmartApp like pollster or my Simple Device Viewer to poll it a couple of times a day to make sure it's still online.</li> 
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/tree/master/devicetypes/krlaframboise/thingshield-timer.src">ThingShield Timer</a></h3>

<ul>
<li>Allows you to use an external timing source, like the Arduino Uno, with the Arduino ThingShield to push a momentary switch at regular intervals.<br /> </li>
<li>It can also be used for scheduling in SmartApps so that you don't have to rely on SmartThings Scheduler.  You basically tell it to push a certain button number in a certain amount of time and then subscribe to its button pushed event and in the event handler method, you check to see if the button number pushed was the one you scheduled.<br /> </li>
<li>I created this because of all the problems SmartThings had with their scheduler, but the new version of their scheduler has been stable for a while so it's probably no longer needed.</li>
</ul>

<hr />

<h3><a href="https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/zipato-multisound-siren.src/zipato-multisound-siren.groovy">Zipato Multisound Siren</a></h3>

<ul>
<li>This is a device handler for the Zipato Z-Wave Indoor Multi-Sound Siren (PH-PSE02.US).  It's been tested on the US version, but it should work with the EU version.<br /></li>
<li><a href="https://community.smartthings.com/t/release-zipato-phileo-multisound-siren-ph-pse02-us/53748?u=krlaframboise">View Documentation in SmartThings Forum</a><br /></li>
</ul>
