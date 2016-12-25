/**
 *  Simple Event Logger - Google Script Code v 0.0.4
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://github.com/krlaframboise/SmartThings/tree/master/smartapps/krlaframboise/simple-event-logger.src#simple-event-logger
 *
 *  Changelog:
 *
 *    0.0.4 (12/25/2016)
 *      - Beta Release
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
function getVersion() { return "00.00.04"; }
 
function doGet(e) {
	var output = "Version " + getVersion()
	return ContentService.createTextOutput(output);
}

function doPost(e) {
	var result = new Object();
	result.version = getVersion();
	
	if (e && e.contentLength > 0) {
		
		var data = JSON.parse(e.postData.contents);
		if (data) {	
			var sheet = SpreadsheetApp.getActiveSheet();
			
			result.eventsLogged = 0;
			
			try {
				result.totalEventsLogged = sheet.getLastRow() - 1;
				
				for each (event in data.events) {
					logEvent(sheet, data.logDesc, event);
					result.eventsLogged++;
				}
				
				result.totalEventsLogged = sheet.getLastRow() - 1;
				result.success = true;
			}
			catch(e) {
				result.error = e.message;
				result.success = false;
			}
			
			result.freeSpace = calculateAvailableLogSpace(sheet);
			
			sendPostback(data.postBackUrl, result);
		}
	}
	
	return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);	
}


function logEvent(sheet, logDesc, event) {
	if (sheet.getLastRow() == 0) {		
		sheet.appendRow(getHeader(logDesc));
	}
  
	var newRow = [
		event.time,
		event.device,
		event.name,
		event.value
	];
	if (logDesc) {
		newRow.push(event.desc);
	}	
	sheet.appendRow(newRow);
}

function getHeader(logDesc) {
	var header = [
			"Date/Time",
			"Device",
			"Event Name",
			"Event Value"
		];
		if (logDesc) {
			header.push("Description");
		}
		return header;
}

function calculateAvailableLogSpace(sheet) {
	var cellsUsed = (sheet.getLastRow() * sheet.getLastColumn());
	var spaceUsed = (cellsUsed / 2000000) * 100;
	return (100 - spaceUsed).toFixed(2) + "%";
}

function sendPostback(url, result) {
	var options = {
			'method': 'post',
			'headers': {"Content-Type": "application/json"},
			'payload': JSON.stringify(result)
	};
		
	var response = UrlFetchApp.fetch(url, options);	
}
