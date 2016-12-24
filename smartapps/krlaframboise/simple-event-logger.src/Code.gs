/**
 *  Simple Event Logger - Google Script Code v 0.0.3
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://github.com/krlaframboise/SmartThings/tree/master/smartapps/krlaframboise/simple-event-logger.src#simple-event-logger
 *
 *  Changelog:
 *
 *    0.0.3 (12/24/2016)
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
function doGet(e) {
	return ContentService.createTextOutput("SUCCESS");
}

function doPost(e) {
	var result = new Object();
	if (e && e.contentLength > 0) {
		
		var data = JSON.parse(e.postData.contents);
		if (data) {	
			result.eventsLogged = 0;
			
			try {
				var sheet = SpreadsheetApp.getActiveSheet();
				
				result.totalEventsLogged = sheet.getLastRow() - 1;
				
				for each (event in data.events) {
					logEvent(sheet, event);
					result.eventsLogged++;
				}
				
				result.totalEventsLogged = sheet.getLastRow() - 1;
				result.success = true;
			}
			catch(e) {
				result.error = e.message;
				result.success = false;
			}
			
			sendPostback(data.postBackUrl, result);
		}
	}
	
	return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);	
}


function logEvent(sheet, event) {
	if (sheet.getLastRow() == 0) {
		sheet.appendRow([
			"Date/Time",
			"Device",
			"Event Name",
			"Event Value",
			"Description"
		]);
	}
  
	sheet.appendRow([
		event.time,
		event.device,
		event.name,
		event.value,
		event.desc
	]);
}


function sendPostback(url, result) {
	var options = {
			'method': 'post',
			'headers': {"Content-Type": "application/json"},
			'payload': JSON.stringify(result)
	};
		
	var response = UrlFetchApp.fetch(url, options);	
}
