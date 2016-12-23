<h1>Simple Event Logger<br><br></li>

<h2>Google Sheets Setup<br><br></li>

<ol>
<li>Go to <a target="_blank" href="http://docs.google.com/spreadsheets">http://docs.google.com/spreadsheets</a><br><br></li>
<li>Log in with your Google account.<br><br></li>
<li>Start a new Blank spreadsheet by clicking the green plus<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-home.png?raw=true" /><br><br></li>
<li>You should see "Untitled spreadsheet" in the top left corner, but if you don't, the top menu is collapsed so you'll need to click the 2 downarrows on the right side of the screen.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-expand-menu.png?raw=true" /><br><br></li>
<li>If you click the text "Untitled spreadsheet" in the top left corner, you can rename it to anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-title.png?raw=true" /><br><br></li>
<li>Open the Script Editor which is located in the top "Tools" menu.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-script-editor-menu.png?raw=true" /><br><br></li>
<li>Delete the existing code that's shown and copy and paste all the code from file <a target="_blank" href="https://raw.githubusercontent.com/krlaframboise/SmartThings/master/smartapps/krlaframboise/simple-event-logger.src/Code.gs">code.gs</a>.<br><br></li>
<li>Go into the "Publish" menu and click "Deploy as web app".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-.png?raw=true" /><br><br></li>
<li>Enter a title for the project, you can name it anything you want.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-project-name.png?raw=true" /><br><br></li>
<li>Change the "Who has access to the app" field to "Anyone, even anonymous".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-web-app-access.png?raw=true" /><br><br></li>
<li>Click the "Deploy" button<br><br></li>
<li>Click "Review Permissions" button on the "Authorization Required" popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-auth.png?raw=true" /><br><br></li>
<li>Click the "Allow" button on the permissions screen popup.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-allow.png?raw=true" /><br><br></li>
<li>Copy the "Current Web App Url" and click "OK".<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-url.png?raw=true" /><br><br></li>
<li>Navigate to the Web App Url you copied and you should see the message "SUCCESS" which indicates that the Web App has been configured properly.<br><img src="https://github.com/krlaframboise/Resources/blob/master/simple-event-logger/sheets-success.png?raw=true" /><br><br></li>
<li>That completes the Google Sheets installation, but you should paste the Web App Url somewhere that you can access from your mobile device because you'll need to paste it into the SmartApp's settings.<br><br></li>
</ul>
