/*
 * @author Olivier Le Diouris
 *
 * Shows how to use the WorldMap object
 * and the REST APIs of the Astro service.
 * Displays a GLOBE
 */
"use strict";

let worldMap;
let currentDate;

let errManager = function(mess) {
	console.log(mess);
};

let init = function () {
	worldMap = new WorldMap('mapCanvas', 'GLOBE');

	// For Mercator
	// worldMap.setNorth(75);
	// worldMap.setSouth(-75);
	// worldMap.setWest(127.5);
};

var DEFAULT_TIMEOUT = 60000;

function getPromise(url,                          // full api path
					timeout,                      // After that, fail.
					verb,                         // GET, PUT, DELETE, POST, etc
					happyCode,                    // if met, resolve, otherwise fail.
					data,                         // payload, when needed (PUT, POST...)
					show) {                       // Show the traffic [true]|false

	if (show === undefined) {
		show = true;
	}
	if (show === true) {
		document.body.style.cursor = 'wait';
	}
	let xhr = new XMLHttpRequest();

	return new Promise(function (resolve, reject) {
		let xhr = new XMLHttpRequest();

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		xhr.setRequestHeader("Content-type", "application/json");
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(function () {
			xhr.abort();
			let mess = {code: 408, message: 'Timeout'};
			reject(mess);
		}, timeout);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
}

let getSkyGP = function(when) {
	let url = "/astro/positions-in-the-sky";
	// Add date
	url += ("?at=" + when);
	url += ("&fromL=" + position.lat);
	url += ("&fromG=" + position.lng);
	// Wandering bodies
	if (document.getElementById("WWB").checked) { // to minimize the size of the payload
		url += ("&wandering=true");
	}
	// Stars
	if (document.getElementById("WS").checked) { // to minimize the size of the payload
		url += ("&stars=true");
	}
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getAstroData = function(when, callback) {
	let getData = getSkyGP(when);
	getData.then((value) => {
		// console.log("Done:", value);
		let json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json);
		} else {
			console.log(JSON.stringify(json, null, 2));
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("Failed to get Astro Data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});
};

let getQueryParameterByName = function(name, url) {
	if (!url) url = window.location.href;
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
	if (!results) return null;
	if (!results[2]) return '';
	return decodeURIComponent(results[2].replace(/\+/g, " "));
};

/*
 *  Demo features
 */

let position = {
	lat: 37.7489,
	lng: -122.5070
};

const MINUTE = 60000; // in ms.

let getCurrentUTCDate = function() {
	let date = new Date();
	let offset = date.getTimezoneOffset() * MINUTE; // in millisecs

	return new Date().getTime() + offset; // - (6 * 3600 * 1000);
};

let initAjax = function () {

	currentDate = getCurrentUTCDate();
	console.log("Starting (now) at " + new Date(currentDate).format("Y-M-d H:i:s UTC"));

	let interval = setInterval(function () {
		tickClock();
	}, 100);

	let intervalGPS = setInterval(function () {
		tickGPS();
	}, 1000) // 1 sec;

};

let tickClock = function () {

	let moveFast = true, erratic = false;

	let mf = getQueryParameterByName("move-fast");
	moveFast = (mf === "true");

	if (moveFast) {
		// Changed position
		position.lng += 1;
		if (position.lng > 360) position.lng -= 360;
		if (position.lng > 180) position.lng -= 360;

		if (erratic) {
			let plus = (Math.random() > 0.5);
			position.lat += (Math.random() * (plus ? 1 : -1));
			if (position.lat > 90) position.lat = 180 - position.lat;
			if (position.lat < -90) position.lat = -180 + position.lat;
		}
	}
	let json = {
		Position: {
			lat: position.lat,
			lng: position.lng
		}
	};
	onMessage(json); // Position
};

let tickGPS = function () {

	let moveFast = true;
	let mf = getQueryParameterByName("move-fast");
	moveFast = (mf === "true");

	let json = {
		GPS: new Date(currentDate)
	};
	onMessage(json); // Date

	if (moveFast) {
		currentDate += (1 * MINUTE);
	} else {
		currentDate = getCurrentUTCDate();
	}

	let mess = "Time is now " + new Date(currentDate).format("Y-M-d H:i:s UTC");
	let dateField = document.getElementById("current-date");
	if (dateField !== undefined) {
		dateField.innerText = mess;
	} else {
		console.log(mess);
	}
};

let onMessage = function (json) {
	try {
		let errMess = "";

		try {
			if (json.Position !== undefined) {
				let latitude = json.Position.lat;
	//    console.log("latitude:" + latitude)
				let longitude = json.Position.lng;
	//    console.log("Pt:" + latitude + ", " + longitude);
				events.publish('pos', {
					'lat': latitude,
					'lng': longitude
				});
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		try {
		  if (json.GPS !== undefined) {
				let gpsDate = json.GPS;
				events.publish('gps-time', gpsDate);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}
		if (errMess !== undefined)
			displayErr(errMess);
	}
	catch (err) {
		displayErr(err);
	}
};
