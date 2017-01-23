/**
 *  Simple Event Logger - Google Script Code v 1.2
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://github.com/krlaframboise/SmartThings/tree/master/smartapps/krlaframboise/simple-event-logger.src#simple-event-logger
 *
 *  Changelog:
 *
 *    1.2 (01/22/2017)
 *      - Added archive functional for out of space and event limit.
 *      - Changed Maximum rows to 500000 because the sheet becomes very slow once you reach that size.
 *
 *    1.1 (01/02/2017)
 *      - Fixed Log Size calculation and added option to delete extra columns which will drastically increase the amount of clolumns that can be stored.
 *
 *    1.0.0 (12/26/2016)
 *      - Initial Release
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
 
var getVersion = function() { return "01.02.00"; }
 
function doGet(e) {
	var output = "Version " + getVersion()
	return ContentService.createTextOutput(output);
}

function doPost(e) {
	var result = new Object();
	result.version = getVersion();
	result.eventsLogged = 0;
	
	if (e && e.contentLength > 0) {		
		var data = JSON.parse(e.postData.contents);
		if (data) {	
			var sheet = SpreadsheetApp.getActiveSheet();
			
			if (needToArchive(sheet, data.archiveOptions, data.events.length)) {
				result = archiveSheet(sheet, result);
			}
			else {
				result = logEvents(sheet, data, result);
			}
			
			result.freeSpace = calculateAvailableLogSpace(sheet);
			sendPostback(data.postBackUrl, result);
		}
	}
	
	return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);	
}

var logEvents = function(sheet, data, result) {
	try {
		result.totalEventsLogged = sheet.getLastRow() - 1;
		
		if (data.deleteExtraColumns) {
			deleteExtraColumns(sheet);
		}
		
		for each (event in data.events) {
			logEvent(sheet, data.logDesc, event);
			result.eventsLogged++;
		}
		
		result.totalEventsLogged = sheet.getLastRow() - 1;
		result.success = true;
	}
	catch(e) {
		if (e.message.contains("above the limit")) {
			result.logIsFull = true
		}
		result.error = e.message;
		result.success = false;
	}
	return result;
}

var logEvent = function(sheet, logDesc, event) {
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

var getHeader = function(logDesc) {
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

var deleteExtraColumns = function(sheet) {
	try {
	  if (sheet.getMaxColumns() > 5) {
      sheet.deleteColumns(6, (sheet.getMaxColumns() - 5));
    }
	}
	catch (e) {
	
	}
}

var calculateAvailableLogSpace = function(sheet) {
	var cellsUsed = (sheet.getMaxRows() * sheet.getMaxColumns());
	var spaceUsed = (cellsUsed / getLogCapacity()) * 100;
	return (100 - spaceUsed).toFixed(2) + "%";
}

var sendPostback = function(url, result) {
	var options = {
			'method': 'post',
			'headers': {"Content-Type": "application/json"},
			'payload': JSON.stringify(result)
	};
		
	var response = UrlFetchApp.fetch(url, options);	
}

var getLogCapacity = function() { return 500000; }

var needToArchive = function(sheet, archiveOptions, newEvents) {
	switch (archiveOptions.type) {
		case "Out of Space":
			return (archiveOptions.logIsFull || ((sheet.getMaxRows() + newEvents) >= (getLogCapacity() / sheet.getMaxColumns())));
		case "Events":
			return (archiveOptions.logIsFull || ((sheet.getLastRow() + newEvents) >= archiveOptions.interval));
		// case "Days":
			// return (getDaysSince(sheet.getRange(2, 1).value) >= archiveOptions.interval);
		default:
			return false;
	}
}

// var getDaysSince = function(firstDT) {
	// var dayMS = 1000 * 60 * 60 * 24;
	// var currentDT = new Date();
	// var currentDate = Date.UTC(currentDT.getFullYear(), currentDT.getMonth(), currentDT.getDate());
	// var firstDate = Date.UTC(firstDT.getFullYear(), firstDT.getMonth(), firstDT.getDate());
	// var diffMS = Math.abs(currentDate - firstDate);
	// return Math.floor(diffMS / dayMS); 	
// }

var archiveSheet = function(sheet, result) {	
	try {
		var archiveSheet = createArchiveSheet(sheet);
		if (archiveSheet) {
			if (verifyArchiveSheet(sheet, archiveSheet)) {
				clearSheet(sheet);
				result.eventsArchived = true;
				result.success = true;
			}
			else {
				result.success = false;
				result.error = "The number of rows and columsn in thea archive Sheet do not match the original sheet so the original file was not cleared.";				
			}
		}
		else {
			result.success = false;
			result.error = "Unable to create archive file.";			
		}
		result.totalEventsLogged = sheet.getLastRow() - 1;		
	}
	catch(e) {
		result.error = e.message;
		result.success = false;
	}
	return result;
}

var createArchiveSheet = function(sheet) {
	var archiveSheetName = getArchiveSheetName(SpreadsheetApp.getActive().getName(), sheet);
	var archiveFile = DriveApp.getFileById(SpreadsheetApp.getActive().getId()).makeCopy(archiveSheetName);
	return SpreadsheetApp.open(archiveFile);
}


var getArchiveSheetName = function(name, sheet) {
	var firstDate = sheet.getRange("A2").getValue();
	var lastDate = sheet.getRange(sheet.getLastRow(), 1).getValue();
	return name + "_" + getFormattedDate(firstDate) + "_" + getFormattedDate(lastDate);
}

var getFormattedDate = function(dt) {
	var yyyy = dt.getFullYear().toString(); 
	var mm = (dt.getMonth()+1).toString(); 
	var dd = dt.getDate().toString(); 
	return yyyy + "-" + (mm[1] ? mm : ("0" + mm[0])) + "-" + (dd[1] ? dd : ("0" + dd[0])); 
}

var verifyArchiveSheet = function(sheet, archiveSheet) {
	return (sheet.getLastRow() == archiveSheet.getLastRow() && sheet.getLastColumn() == archiveSheet.getLastColumn());
}

var clearSheet = function(sheet) {
	if (sheet.getMaxColumns() > 6) {
		Logger.log("Deleting Columns");
		sheet.deleteColumns(6, (sheet.getMaxColumns() - 5));
	}
	if (sheet.getMaxRows() > 2) {
		sheet.deleteRows(2, (sheet.getMaxRows() - 1));
	}
	sheet.getRange(2, 1, 1, sheet.getLastColumn()).clearContent();
}  
