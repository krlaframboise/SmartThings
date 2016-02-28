**Aeon Labs Multifunction Siren v 1.0**

This is a new device handler for the Aeon Labs Siren that provides a lot of new functionality.  I've included some screenshots below, but the full specifications will be posted separately.

**Main Screen**

The main screen allows you to turn on/off the Alarm, play the Beep Tone, play the Beep Tone Schedule, and play the 6 preset Beeps.  To change the Alarm and Beep preferences can be changed from the Settings screen.<br />
<img width="200" src="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/DeviceTypes/AeonLabsMultifunctionSiren/Main.png" />

**Alarm Settings**

You can choose the sound and volume that the default Alarm should use and an optional Duration.

If the specified Duration is greater than 0, the alarm will automatically turn off after that number of seconds.<br />
<img width="200" src="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/DeviceTypes/AeonLabsMultifunctionSiren/AlarmSettings.png" />
 
**Beep Settings**

You can completely customize the default Beep sound and repetition, but it's limited to a maximum play time of 20 seconds.<br />
<img width="200" src="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/DeviceTypes/AeonLabsMultifunctionSiren/BeepSettings.png" />

**Beep Schedule**

The beep command is designed for short sounds that play or repeat for up to 20 seconds, but the Beep Schedule settings allow you to play the beep at set intervals for a specific amount of time.<br />
<img width="200" src="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/DeviceTypes/AeonLabsMultifunctionSiren/BeepScheduleSettings.png" />

**Rule Machine - Custom Commands**

* customAlarm(sound, volume, duration) - Plays specified sound at specified volume for specified duration.  (duration is optional)

* startBeep() - Uses the schedule settings to repeatedly play the default beep.

* customBeep\[1-6\]() - A command for each of the 6 custom beep buttons shown in the app.

* customBeep(sound, volume, repeat, repeatDelay, beepLength) - Plays beep using specified settings.
(repeat, repeatDelay, and beepLength are optional)
