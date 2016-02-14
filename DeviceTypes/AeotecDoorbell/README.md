**Aeotec Doorbell v1.0**

	* Pressing the Physical Doorbell Button executes the
	  "on()" command, but due to the way the hardware functions,
	  the sound will play before the events are raised in ST.

-------------------------------------------------------

**COMMANDS**

	on()
		- Rings Doorbell
		- Sets Attributes:
			status: doorbell
			switch: on			
	
	off()
		- Stops anything that's playing
		- Sets Attributes:
			status: off
			switch: off
			alarm: off
			
	siren(), strobe(), both()
		- Plays alarm track.
		- Sets Attributes:
			status: alarm
			alarm: both
			
	beep()
		- Plays beep track
		- Sets Attribute:
			status: beep
	
	playTrack(track)  [This is a Custom Command]
		- Plays specified track
		- Sets Attribute:
			status: play

-------------------------------------------------------

**PREFERENCES**

	- Enable debug logging?
	  (Enables the output in the live logging)
 
	- Use Secure Commands?
	  (This feature is untested, but it should allow
	  you to still use the device if you were unable
	  to connect using secure inclusion.)
 
-------------------------------------------------------

**IMPORTANT INFORMATION**

	* There is sometimes a 2-3 second delay before the
	  track starts to play.  I'm still researching this
	  problem to see if I can find a solution.
	  
	* When the device turns on, it automatically plays
	  track 1.  Some users have reported the doorbell
	  randomly ringing without the button being pushed,
	  but using a couple of seconds of silence as the
	  first track appears to eliminate that problem.
	  
	* The doorbell button doesn't work if you're holding
	  it, but it works most of the time if you have it on
	  a hard surface.
	  
	* The track numbers get assigned to the files based on
	  the order that the files are copied onto the drive.
	  
	* If the ends of your sound files are getting cut off,
	  add about 1/2 second of silence to the end of them.	  
	  (I've been using Audacity http://audacityteam.org
	  to add the silence. There's an option for "Silence" in
	  the "Generate" menu that allows you to specify the
	  amount of time you want to add. Before you add the
	  silence you should click the end of the track because
	  that's where you want the silence added. Afterwards you
	  can use the export option in the File menu to save your
	  changes. If I remember correctly, it made me install
	  some other component in order to export mp3s, but it
	  provided instructions.)

	* The device stops playing after about 25 seconds whether
	  it's one long track or a short track repeating.  Because
	  of this, the siren and switch will revert back to off
	  as soon as the track finishes.
	  
-------------------------------------------------------

**RULE MACHINE EXAMPLES**

	* Detect when doorbell button is pressed:
		Trigger or Condition > Capability Switch > on

	* Play Doorbell Track:
		Action > Capability Switch > on

	* Play Alarm Track:
		Action > Capability Alarm > siren, strobe, or both

	* If you want to use the playTrack or beep commands,
	  you need to setup Custom Commands in Rule Machine and
	  you can do that from Expert Features > Custom Commands
	  screen. Once you've setup the commands, you can use
	  them for the Action of rules.

	* Play Track by Number
		[select device] > New Custom Command > playTrack >
		Parameter > Number > [track number] > Done > Save Command

	* Beep
		[select device] > New Custom Command > beep >
		[clear parameters] > Done > Save Command

