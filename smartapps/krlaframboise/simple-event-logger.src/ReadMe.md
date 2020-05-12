<h1 id="simple-event-logger">Simple Event Logger - v1.1.0</h1>
<p>By Kevin LaFramboise (krlaframboise)</p>

<p><img align="right" src="https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@3x.png" alt="Simple Event Logger Logo" />The Simple Event Logger is a fully customizable SmartApp that allows you to accurately log all device activity to a Google Sheets Spreadsheet.  Each event is stored on a separate row so that you have their exact time and details.</p>

<p>Google Sheets has an easy to use filter feature which allows you to do things like view all events for a specific device(s), in a specified date/range and/or specific types of events like temperature.</p>

<p>Since all of your data will be stored in one spreadsheet,advanced users can easily generate pivot tables and graphs for any information they need.  It also eliminates the need to update the code in multiple spreadsheets every time a new version is released.</p>

<hr>

<h2>Menu</h2>

<ul>
	<li><a href="#features">Features</a></li>
	<li>Installation<br>
		<ul>
			<li><a href="#google-sheets-setup">Google Sheets Web App Installation</a></li>
			<li><a href="#smartapp-setup">SmartApp Installation</a></li>			
		</ul>
	</li>
	<li><a href="#creating-filters-to-view-the-logged-events">Creating Filters to View the Logged Events</a></li>
	<li>Code<br>
	<ul><li><a href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/Code.gs">Google Sheets Web App Code</a></li>
	<li><a href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/simple-event-logger.groovy" target="_blank">SmartApp Code</a></li>
	</ul></li>
	<li><a href="#upcoming-features">Upcoming Features</a></li>
</ul>

<hr>

<h2>Features</h2>

<ul>
	<li><p>This SmartApp supports almost every capability and attribute that's listed in the SmartThings documentation.</p></li>	
	<li><p>It uses my "simple" approach of selecting all the devices you want to use once and presenting the narrowed list of devices for other settings.</p></li>
	<li><p>It combines all the attributes supported by the selected devices into one setting and allows you to choose which ones to log.</p></li>
	<li><p>Each attribute being logged has an exclusion list so you have complete control over which devices log what.</p></li>
	<li><p>You can set the run interval to 5 Min, 10 Min, 15 Min, 30 Min, 1 Hour, 3 Hour</p></li>
	<li><p>When it runs, it retrieves up to 50 events for each device since the last time it ran.  The number can be set to as low as 1 event.</p></li>
	<li><p>It uses the device's event log to get the new events instead of subscribing to each device and queuing them.</p></li>
	<li><p>It logs the event time, device, event name, event value, event description, but you can disable the description logging if you don't need it.</p></li>
	<li><p>It only logs the events with the Source DEVICE, but I'm considering making that adjustable and possibly adding the ability to log Location events instead of just device events.</p></li>
	<li><p>When it executes, it retrieves the events that have occurred since the last time it executed and posts them to the Google Sheets Web App in a single batch.</p>
	<ul>
		<li><p>This SmartApp posts the data so that it's transmitted securely.  If the data is being sent as key/value pairs in the url, it's getting stored in web server logs as clear text.</p></li>
		<li><p>Posting all the information in a single batch reduces network traffic and should prevent the SmartApp from reaching the 20 second execution limit and timing out.</p></li>
		</ul></li>
	<li><p>Once the Google Web App has finished logging all the data that was posted to it, it posts a response back to the SmartApp confirming that everyone was logged.</p></li>
	<li><p>The SmartApp shows the result of the last run, the total # of events logged, and the percentage of free space in the log.  The log should be able to hold up to 400,000 events.</p></li>
	<li><p>It writes a debug messages to Live Logging showing the number events it found and the number of events that the Google web app said it logged.</p></li>
	<li><p>You can choose which types of log entries to show in Live Logging (debug, info, trace)</p></li>
</ul>

<hr>

<h2>Google Sheets Setup</h2>

<ol>
<li>Go to <a target="_blank" href="http://docs.google.com/spreadsheets"><b>http://docs.google.com/spreadsheets</b></a><br><br></li>
<li>Log in with your Google account.<br><br></li>
<li>Start a new Blank spreadsheet by clicking the green plus<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-home.png?raw=true" /><br><br></li>
<li>You should see "Untitled spreadsheet" in the top left corner, but if you don't, the top menu is collapsed so you'll need to click the 2 downarrows on the right side of the screen.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-expand-menu.png?raw=true" /><br><br></li>
<li>If you click the text "Untitled spreadsheet" in the top left corner, you can rename it to anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-title.png?raw=true" /><br><br></li>
<li>Open the Script Editor which is located in the top "Tools" menu.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-script-editor-menu.png?raw=true" /><br><br></li>
<li>Delete the existing code that's shown and copy and paste all the code from file <a target="_blank" href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/Code.gs"><b>code.gs</b></a>.<br><br></li>
<li>Go into the "Publish" menu and click "Deploy as web app".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-publish-menu.png?raw=true" /><br><br></li>
<li>Enter a title for the project, you can name it anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-project-name.png?raw=true" /><br><br></li>
<li>Deploy as Web App Options<br>
<ul>
	<li>Change the "Product Version" to "New".  That is the default value the first time you deploy it, but it needs to be changed manually every time you deploy a new version or the changes won't get applied.</li>
	<li>Change the "Who has access to the app" field to "Anyone, even anonymous".<br><em>(This makes the Web App url accessible to anyone that knows the url, but no one will know the url unless you give it out.  Even if someone had the url, the only thing they'd be able to see is the interval version number.)</em></li>
	<li>Click "Deploy"</li>
</ul><br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-web-app-access.png?raw=true" /><br><br></li>
<li>Click "Review Permissions" button on the "Authorization Required" popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-auth.png?raw=true" /><br><br></li>
<li>Click the "Allow" button on the permissions screen popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-allow.png?raw=true" /><br><br></li>
<li>Copy the "Current Web App Url" and click "OK".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-url.png?raw=true" /><br><br></li>
<li>Log out of Google and navigate to the Web App Url you copied<br><ul><li>You should see a message that starts with "Version: " which indicates that the Web App has been configured properly.</li><li><b>Don't copy the url from this page</b> because it's NOT the same as the url you previously copied from the Script Editor Publish screen.</li><li>The url that's needed for the SmartApp starts with "https://script.google.com/macros/s/".</li></ul><br><br></li>
<li>That completes the Google Sheets installation, but you should paste the Web App Url somewhere that you can access from your mobile device because you'll need to paste it into the SmartApp's settings.<br><br></li>
</ol>

<hr>

<h2>SmartApp Setup</h2>
<ol>
<li>Create the SmartApp in the IDE by either copying the <a href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/simple-event-logger.groovy" target="_blank"><b>Simple Event Logger code</b></a> and pasting it into the New SmartApp "From Code" option or by adding my GitHub Repository with the settings krlaframboise, SmartThings, and master.<br><ul><li><a href="http://thingsthataresmart.wiki/index.php?title=Using_Custom_Code#Using_a_Custom_SmartApp" target="_blank">Learn more about using custom SmartApps</a></li><li><a href="http://thingsthataresmart.wiki/index.php?title=Using_Custom_Code#A_Note_on_.22GitHub_Integration.22" target="_blank">Learn more about Github Integration</a></li></ul><br></li>
<li>Enable OAuth in the SmartApp Settings<br><ul><li>Open the SmartApp in the IDE and click the "App Settings" button.</li><li>Click the "OAuth" link and then click the "Enable OAuth in SmartApp" button.</li></ul><br></li>
<li>Install the SmartApp through the Mobile App<br>
<ul><li>Open the SmartThings Mobile App and tap the "Automation" button that's located along the bottom.</li>
<li>Tap the SmartApps tab and then tap "Add a SmartApp".</li>
<li>Tap "My Apps" and then tap "Simple Event Logger".</li>
</ul><br></li>
<li>Select all the devices you want to log events for.  You should see most of your devices in the "Actuators" and "Sensors" fields, but most of them will be in multiple fields.  When you select a device, you're telling the SmartApp that it should log devices.  Which events it should log is specified in a different section of the settings so it doesn't matter which field you select the device from.<br><br></li>
<li>After you've selected the devices you want to log events for, scroll down to the "Choose Events" section and select the events that should be logged for all devices.<br><br></li>
<li>If you want to log an event for some devices and not others you can use to corresponding "Device Exclusion" fields to exclude those devices.  <em>(Due to timeout problems some users were experiencing, this feature has been moved into it's own section and won't be visible until you've completed the installation and re-opened the SmartApp)</em><br><br></li>
<li>Change the "Logging Options" (if needed)<br><ul><li><b>Log Events Every:</b> Determines the schedule interval for posting new events to the google sheet.</li>
<li><b>Maximum number of events to log for each device per section:</b> When the SmartApp Executes it retrieves between (1 and 50) events from the device since it last ran depending on this setting.  Setting this number too high may cause the SmartApp to reach the 20 second execution limit and setting it too low may result in some events not getting logged.</li><li><b>Log Event Description:</b> Determines whether the event's description is logged.  Google Sheets is limited to 2 million cells so it can hold about 400,000 events, but only if columns F-Z have been deleted and no other sheets have been added.</li><li><b>Delete Extra Columns:</b>This automatically deletes columns F-Z if they're empty which allows you to log more events.</li></ul><br><br></li>
<li>Paste the Web App Url you previously copied from the <b>"Google Web App Url"</b> field.<br>
<ul><li>The url should start with "https://script.google.com/macros/s/"</li><li>If your url does not start like that, the SmartApp won't work so you need to go back into the Google Sheets Script Editor and copy the url from the publish screen.</li></ul><br><br></li>
<li>Once you've filled in all the required information and tapped done, it will initialize the logging schedule for the interval specified.  After that scheduled amount of time, you should see new events logged to the spreadsheet and if you open the SmartApp, you will see information about the last time start it executed.<br><br></li>
</ol>

<hr>

<h2>Creating Filters to View the Logged Events</h2>

<p>The data gets logged in a format that's easy to work with, but not very useful to look at, but you can easily create and save filters to see the information you care about.</p>

<p>The following instructions demonstrate how to setup and save a filter that shows your temperature data.</p>

<ol><li>Open the Google Sheet that the data is being logged to<br></li>
<li>Click the down arrow next to the filter icon in the toolbar and then click "Create new filter view".<br>
<img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter1.png?raw=true" /><br><br></li>
<li>Enter a name filter by clicking in the box to the right of "Name:", typing "Temperature" and then pressing enter.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter2.png?raw=true" /><br><br></li>
<li>Click the down arrow to the right of the Date/Time field and then click "Sort Z-A" which will ensure that the most recent data will appear at the top of the page.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter3.png?raw=true" /><br><br></li>
<li>Click the down arrow to the right of the Event Name field, click the "clear" link, select the event name "temperature", and click OK.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter4.png?raw=true" /><br><br></li>
<li>Once you've done that you should see just the event names that you select.  If you were just looking for the events of a specific device, you could click the down arrow next to the Device field and select it.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter5.png?raw=true" /><br><br></li>
<li>Click the x in the top right corner of the black bar to close out of the filter view.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter6.png?raw=true" /><br><br></li>
<li>The next time you want to view your temperatures, click the down arrow next to the filter icon in the toolbar and select the filter "Temperature".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-filter7.png?raw=true" /><br><br></li>
</ol>
<p>If you've had the filtered view open for a while, you probably won't see the recent events, but you can refresh it by closing the filter and spreadsheet and then re-opening.</p>

<hr>

<h2>Upcoming Features</h2>
<ul>
<li><p>Ability to select specific numeric events/attributes and have it store an average of values since the last execution instead of creating a new row for every value.  This should conserve log space because a lot of power devices report a lot of values in a short period of time.</p></li>
<li><p>Optionally send low log space warnings by Push, SMS, and/or Email based on customizable threshold.</p></li>
<li><p>Adding a couple more web app url fields so you can optionally have multiple sheets setup to increase the amount of storage space. When the first one becomes full, it starts logging in the second, and then the 3rd(if applicable). Once it runs out of spreadsheets it can either stop logging or go back to the first book, clear it and start the cycle over.</p></li>
<li><p>Convert the SmartApp into a Parent/Child SmartApp, but <b>only if multiple users request this feature</b>.</p><p>One of my goals in writing this SmartApp was to make it as easy as possible to log everything.  I didn't want to have to setup a different spreadsheet for every event I wanted to monitor because it's a pain to set them all up, configure them and then update them all every time there's a new version of the script code.</p><p>Having the SmartApp only support one spreadsheet should make things easier for most users, but for advanced users that are logging a lot of data, the 400k-500k event log limit might become a problem.  Those users may also experience performance problems while working with the data in Google Sheets or run into problems with the SmartApp reaching the 20 second execution limit before it can send the data to be logged.</p><p>The way the SmartApp is now, they can still use multiple spreadsheets by installing multiple instances of the SmartApp, but it would be easier for them if this was a Parent/Child SmartApp.  I'm hesistant to make this change because it will end up making the initial setup process more confusing for the average user, but I will make the change if enough users request it.</p></li>
</ul>
