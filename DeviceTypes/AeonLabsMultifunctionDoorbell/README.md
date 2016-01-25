# Aeon Labs Multifunction Doorbell

This is a device handler for the Aeon Labs Doorbell.  It allows you to use the device as a doorbell, siren, and to play music tracks.  If you just want to use the device as a siren or a way to play mp3 files you can use the doorbell button for something else.

<a href="https://github.com/krlaframboise/SmartThings/raw/master/DeviceTypes/AeonLabsMultifunctionDoorbell/Screenshot1.png"><img src="https://github.com/krlaframboise/SmartThings/raw/master/DeviceTypes/AeonLabsMultifunctionDoorbell/Screenshot1_sm.png" /></a><a href="https://github.com/krlaframboise/SmartThings/raw/master/DeviceTypes/AeonLabsMultifunctionDoorbell/Screenshot2.png"><img src="https://github.com/krlaframboise/SmartThings/raw/master/DeviceTypes/AeonLabsMultifunctionDoorbell/Screenshot2_sm.png" /></a>

**COMMANDS:**

* siren, strobe, both, On
    - Plays Alarm Track
    - Sets
        alarm: both
        status: alarm
        switch: on

* beep
    - Plays Beep Track
    - Sets
        status: beep
        
* off, stop  (or pushing main tile when it's playing)
    - Stops anything currently playing
    - Sets
        alarm: off
        switch: off
        status: off
    
* pushButton (and physically button)
    - Play the Doorbell Track
    - Sets
        status: bell (unless the "Silent Bell" setting is enabled)
        button: Pushed 1
        
* playTrack
    - Plays specified track number.
    - Sets
        status: play        

* play
    - Plays the currently saved track
    - Sets
        status: play
        
* setTrack, nextTrack, previousTrack
    - Saves the specified track number and moves it forward or back.
    - Sets
        trackDescription: (changes to currently saved track number)
        
* setLevel
    - Sets the device volume to specified number (1-10)
    
* poll
	- Sends a command to the device and waits up to 15 seconds for a response.  If it receives a response, it sets the presence attribute to "present", otherwise it sets it to "not present".  The presence attribute is from the Beacon capability, NOT the Presence Sensor.
	
* refresh
	- Clears current status.
	- Refreshes Online/Offline Tile by polling.
    
-------------------------------------------------------

**PREFERENCES**  (*** located on the Edit Device screen ***)

*   Doorbell Track

*   Beep Track

*   Alarm Track

*   Sound Level (even on setting 10 it's not very loud)

*   Sound Repeat (how many times that track should play)

*   Enable debug logging (enables the output in the live logging) 

*   Enable Silent Button (This option prevents the status from changing to "bell", but it doesn't prevent the device from playing the Doorbell track.  To make it completely silent, set the Doorbell Track to 100 and remove that file from the device if it exists.) 

*  Force Configuration Refresh (When you first install the device it should save all of the configuration settings and then just save the preferences you change going forward.  If for some reason your settings aren't saving, you can enable this option to force the entire configuration to get sent to the device. 

*If there are additional fields after the Force configuration choice, they're left over from one of the other device handlers and if you clear their values they won't appear next time.*
 
-------------------------------------------------------

**IMPORTANT INFORMATION**

* Be patient with the mobile interface.  If you try to start a track while another is playing nothing will happen, but you can stop the track by clicking the main tile.  Once the main tile goes back to the "off" state you can play another track.

* When the device turns on or has any type of power interruption it will play the first track so you should use a blank sound file as your first track. Example of empty sound file: <a href="https://github.com/krlaframboise/SmartThings/blob/master/DeviceTypes/AeonLabsMultifunctionDoorbell/01-Silence.mp3?raw=true" target="_blank">1-Silent.mp3</a>

* The doorbell button doesn't work reliably if you're holding it, but it works most of the time if you have it on a hard surface.

* The track numbers get assigned to the files based on the order that the files are copied onto the drive.  <a href="https://community.smartthings.com/t/aeon-doorbell-type-beta/28889/262?u=krlaframboise">More Information</a>

* If the ends of your sound files are getting cut off, add about 1/2 second of silence to the end of them.  <a href="https://community.smartthings.com/t/aeon-doorbell-type-beta/28889/261?u=krlaframboise">More Information</a>

* The device stops playing after about 25 seconds whether it's one long track or a short track repeating.  Because of this, the siren and switch will revert back to off as soon as the track finishes.

* The alarm and music player capabilities allow you to use this device with many existing applications, but the most useful is probably Rule Machine.  <a href="https://community.smartthings.com/t/aeon-doorbell-type-beta/28889/356?u=krlaframboise">More Information</a>

* If you're using the button as a trigger in Rule Machine and the action uses the doorbell to play a track, the track may not play unless you delay the action by at least 2 seconds.
