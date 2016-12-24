<h1>Simple Event Logger</h1>

<p><img align="right" src="https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@3x.png" alt="Simple Event Logger Logo" />The Simple Event Logger is a fully customizable SmartApp that allows you to accurately log all device activity to a Google Sheets Spreadsheet.  Each event is stored on a separate row so that you have their exact time and details.</p>

<p>Google Sheets has an easy to use filter feature which allows you to do things like view all events for a specific device(s), in a specified date/range and/or specific types of events like temperature.</p>

<p>Since all of your data will be stored in one spreadsheet,advanced users can easily generate pivot tables and graphs for any information they need.  It also eliminates the need to update the code in multiple spreadsheets every time a new version is released.<br></p>

<hr>

<h2>Features</h2>

<!--<ul>
	<li><a href="#description">Description</a></li>
	<li><a href="#installation">Installation</a>
		<ul>
			<li><a href="#installgoogle">Google Sheets Web App Installation</a></li>
			<li><a href="installSmartApp">SmartApp Installation</a></li>
		</ul>
	</li>
</ul>-->

<ul>
	<li>This SmartApp supports almost every capability and attribute that's listed in the SmartThings documentation.<br><br></li>	
	<li>It uses my "simple" approach of selecting all the devices you want to use once and presenting the narrowed list of devices for other settings.<br><br></li>
	<li>It combines all the attributes supported by the selected devices into one setting and allows you to choose which ones to log.<br><br></li>
	<li>Each attribute being logged has an exclusion list so you have complete control over which devices log what.<br><br></li>
	<li>You can set the run interval to 5 Min, 10 Min, 15 Min, 30 Min, 1 Hour, 3 Hour<br><br></li>
	<li>When it runs, it retrieves up to 50 events for each device since the last time it ran.  The number can be set to as low as 1 event.<br><br></li>
	<li>It uses the device's event log to get the new events instead of subscribing to each device and queuing them.<br><br></li>
	<li>It logs the event time, device, event name, event value, event description, but you can disable the description logging if you don't need it.<br><br></li>
	<li>It only logs the events with the Source DEVICE, but I'm considering making that adjustable and possibly adding the ability to log Location events instead of just device events.<br><br></li>
	<li>When it executes, it retrieves the events that have occurred since the last time it executed and posts them to the Google Sheets Web App in a single batch.<br>
	<ul>
		<li>This SmartApp posts the data so that it's transmitted securely.  If the data is being sent as key/value pairs in the url, it's getting stored in web server logs as clear text.</li>
		<li>Posting all the information in a single batch reduces network traffic and should prevent the SmartApp from reaching the 20 second execution limit and timing out.</li>
		</ul><br>
	</li>
	<li>Once the Google Web App has finished logging all the data that was posted to it, it posts a response back to the SmartApp confirming that everyone was logged.<br><br></li>
	<li>The SmartApp shows the result of the last run, the total # of events logged, and the percentage of free space in the log.  The log should be able to hold between 800,000 and 1,000,000 events depending on whether or not the description field is being logged.<br><br></li>
	<li>It writes a debug messages to Live Logging showing the number events it found and the number of events that the Google web app said it logged.<br><br></li>
	<li>You can choose which types of log entries to show in Live Logging (debug, info, trace)<br><br><br></li>
</ul>

<hr>

<h2>Google Sheets Setup</h2>

<ol>
<li>Go to <a target="_blank" href="http://docs.google.com/spreadsheets">http://docs.google.com/spreadsheets</a><br><br></li>
<li>Log in with your Google account.<br><br></li>
<li>Start a new Blank spreadsheet by clicking the green plus<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-home.png?raw=true" /><br><br></li>
<li>You should see "Untitled spreadsheet" in the top left corner, but if you don't, the top menu is collapsed so you'll need to click the 2 downarrows on the right side of the screen.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-expand-menu.png?raw=true" /><br><br></li>
<li>If you click the text "Untitled spreadsheet" in the top left corner, you can rename it to anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-title.png?raw=true" /><br><br></li>
<li>Open the Script Editor which is located in the top "Tools" menu.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-script-editor-menu.png?raw=true" /><br><br></li>
<li>Delete the existing code that's shown and copy and paste all the code from file <a target="_blank" href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/Code.gs">code.gs</a>.<br><br></li>
<li>Go into the "Publish" menu and click "Deploy as web app".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-publish-menu.png?raw=true" /><br><br></li>
<li>Enter a title for the project, you can name it anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-project-name.png?raw=true" /><br><br></li>
<li>Change the "Who has access to the app" field to "Anyone, even anonymous" and click "Deploy".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-web-app-access.png?raw=true" /><br><br></li>
<li>Click "Review Permissions" button on the "Authorization Required" popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-auth.png?raw=true" /><br><br></li>
<li>Click the "Allow" button on the permissions screen popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-allow.png?raw=true" /><br><br></li>
<li>Copy the "Current Web App Url" and click "OK".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-url.png?raw=true" /><br><br></li>
<li>Navigate to the Web App Url you copied and you should see the message "SUCCESS" which indicates that the Web App has been configured properly.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-success.png?raw=true" /><br><br></li>
<li>That completes the Google Sheets installation, but you should paste the Web App Url somewhere that you can access from your mobile device because you'll need to paste it into the SmartApp's settings.<br><br></li>
</ol>

<hr>

<h2>SmartApp Setup</h2>
<ol>
<li>Either copy and paste the <a href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/simple-event-logger.groovy" target="_blank">Simple Event Logger code</a> into a new SmartApp or link to my repository using krlaframboise, SmartThings and master.<br><br></li>
<li>Enable OAuth in the SmartApp Settings<br><br></li>
<li>Install the SmartApp through the Mobile App<br><br></li>
<li>Select all the devices you want to log events for.  The devices will appear in multiple fields, but you only need to select a device once<br><br></li>
<li>Then select all the events that you'd like to log.  Only events that are supported by at least one of the selected devices will appear in the list.<br><br></li>
<li>For each event you selected, choose the devices that it should ignore.  You can skip this section if you want all the selected devices to log all the selected events.<br><br></li>
<li>Paste the Web App Url you previously copied into the "Google Web App Url" field and fill in the rest of the settings.<br><br></li>
<li>Once you've tapped "Done" you should see entries in Live Logging at the interval you specified.<br><br></li>
<li>You can see the events that it logged by going back to the google sheets page.<br><br></li>
</ol>
