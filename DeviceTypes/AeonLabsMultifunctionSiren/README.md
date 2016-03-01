**Aeon Labs Multifunction Siren**

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
<hr />
This DH supports the Music Player capability which allows you to use custom sirens and beeps with the SmartApps **Smart Home Monitor** and **Notify with Sound**.  

Rule Machine is no longer required to utilize all the functionality of this Device Handler, but if you're using Rule Machine, you can now use all the commands without setting them up as Custom Commands.

Here's an overview of how you can use these features in the 3 SmartApps mentioned.

**Smart Home Monitor**
The Custom Monitoring section of SHM supports Audio Notifications and if you choose "Custom Message" for the Notification, you can specify the command to execute in the "Play this message" field.

**Speaker Notify with Sound**
You can setup a Custom Message action and specify the command to execute in the "Play this message" field.

**Rule Machine**
You can use the "Send or speak a message" action to execute the command.  You do this by entering the command in the "Custom message to send" field, enabling the "Speak this message?" option, and choosing the siren for the "On this music device" field.

**Supported Commands**

You can execute any of these commands by entering them exactly as shown into one of the fields described above:

off
stop
on
play
siren
strobe
both
beep
startBeep
customBeep1
customBeep2
customBeep3
customBeep4
customBeep5
customBeep6

The **customAlarm** command can be used by entering "customAlarm sound, volume, duration".  The command below will play sound 5 at volume 1 for 60 seconds.
    **customAlarm 5, 1, 60**

The **customBeep** command can be used by entering the 5 parameters without any text.  The command below will play a 50 millisecond beep using sound 3 at volume 2 and it will do this 5 times with a 1 second pause between each beep.
    **3, 2, 5, 1000, 50**
    
The **startCustomBeep** command can be used by entering the 7 parameters without any text.  The command below performs the customBeep mentioned above at 10 second intervals for 1 minute.
    **10, 60, 3, 2, 5, 1000, 50**
