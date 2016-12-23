/**
 *  Simple Event Logger - Google Script Code v 0.0.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    N/A
 *
 *  Changelog:
 *
 *    0.0.0 (12/23/2016)
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
	var sheet = SpreadsheetApp.getActiveSheet();		
	var result = new Object();
	result.success = false;
		
	if (e && e.contentLength > 0) {

		var data = JSON.parse(e.postData.contents);
		if (data) {		
		
			result.eventCount = 0;
			
			for each (event in data.events) {
				logEvent(sheet, event);
				result.eventCount++;
			}
	
			result.endTime = data.time;		
			result.success = true;
			
			var responseUrl = getSmartAppResponseUrl(sheet, data.appId, data.token, result);
			var response = UrlFetchApp.fetch(responseUrl);
		}
	}
	
	return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
	
}

function getSmartAppResponseUrl(sheet, appId, token, result) {
	var total = sheet.getLastRow();
	if (total > 0) {
		total = (total - 1);
	}
	
	return "https://graph.api.smartthings.com/api/token/" + token + "/smartapps/installations/" + appId + "/logging-result/" + result.success.toString() + '§' + result.endTime.toString() + '§' + result.eventCount.toString() + '§' + total.toString();
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

