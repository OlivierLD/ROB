/*
 * @author Olivier Le Diouris
 *
 * Shows how to use the WorldMap object
 * and the REST APIs of the img service.
 * 
 * Displays faxes.
 */
"use strict";

let worldMap;
let currentDate;

if (typeof(errManager) !== 'function') {
	console.log(`ww.js: errManager was not defined.`);
	let errManager = function(mess) {
		let content = document.getElementById("error").innerHTML;
		if (content !== undefined) {
			console.log(`Displaying errors in their own div.`);
			document.getElementById("error").innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
			let div = document.getElementById("error");
			div.scrollTop = div.scrollHeight;
		} else {
			console.log(`Logging error: ${mess}`);
		}
	};
}

var DEFAULT_TIMEOUT = 300000; // 120000: 2 minutes, 300000: 5 minutes
var WW_VERBOSE = false;

/*
 * Demo features
 * TODO Get it from another channel, like a GPS...
 */

//let position = { // SF
//	lat:   37.7489,
//	lng: -122.5070
//};
//let position = { // Vannes
//	lat: 47.661667,
//	lng: -2.758167
//};
let position = { // Belz, default
	lat: 47.661667,
	lng: -3.135667
};

const MINUTE = 60000; // in ms.

let getCurrentUTCDate = function() {
	let date = new Date();
	let offset = date.getTimezoneOffset() * MINUTE; // in millisecs

	return new Date().getTime() + offset; // - (6 * 3600 * 1000);
};

let init = function () {
	worldMap = new WorldMap('mapCanvas', 'MERCATOR');

	worldMap.setNorth(75);
	worldMap.setSouth(-75);
	worldMap.setWest(-179);
	worldMap.setEast(179); // Recalculated, anyway.

	worldMap.setUserPosition({ latitude: position.lat, longitude: position.lng }); // TODO gridSquare & GoogleCodePlus ?

};

// let DEFAULT_TIMEOUT = 60000;

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
			let mess = {code: 408, message: `Timeout (${timeout})`};
			reject(mess);
		}, timeout);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				let len = xhr.getResponseHeader("Content-Length");
				// console.log(`Content Length: ${ Intl.NumberFormat().format(len) }`);
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
}

let requestCompositeFaxes = function(requestPayload) {
	let url = "/img/download-and-transform";
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, requestPayload, false);
};

let setWWVerbose = function(value) {
    WW_VERBOSE = value;
}

let getCompositeFaxes = function(options, compositeData, callback) {
	if (WW_VERBOSE) {
		console.log(`getCompositeFaxes, starting`);
	}
	let getData = requestCompositeFaxes(options);
	getData.then((value) => {
		if (WW_VERBOSE) {
			console.log("getCompositeFaxes Done:", JSON.stringify(value));
		}
		let json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json, compositeData);
		} else {
			console.log(JSON.stringify(json, null, 2));
			console.log(JSON.stringify(compositeData, null, 2));
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("getCompositeFaxes: Failed to get Composite Data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});
};

let crawlComposites = function(filter) {
	let url = "/ww/composite-hierarchy";
	if (filter !== undefined && filter.length > 0) {
		url += ("?filter=" + filter);
	}
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, undefined, false);
};

let getExistingComposites = function(callback, filter) {
	if (true || WW_VERBOSE) {
		console.log(`getExistingComposites, starting`);
	}
	let getData = crawlComposites(filter);
	getData.then((value) => {
		if (WW_VERBOSE) {
			console.log("getExistingComposites: Done:", JSON.stringify(value));
		}
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
		errManager("getExistingComposites: Failed to get Composite Data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});

};

// Get position from cache ?
let getPositionFromNMEACache = function() {
	let url = "/mux/cache";
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getGPSPosition = function() {
	if (WW_VERBOSE) {
		console.log(`getGPSPosition, starting`);
	}
	let posPromise = getPositionFromNMEACache();
	posPromise.then(cache => {
		let cacheValues = JSON.parse(cache);
		if (WW_VERBOSE) {
			console.log(`Pos from Cache ${JSON.stringify(cacheValues)}`);
		}
		if (cacheValues.Position) {
			worldMap.setUserPosition({ latitude: cacheValues.Position.lat, longitude: cacheValues.Position.lng });  // TODO gridSquare & GoogleCodePlus ?
			redraw();
			// console.log(`Position from Data Cache: ${JSON.stringify(cacheValues.Position)}`);
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("getGPSPosition: Failed to get Data Cache..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
		// debugger;
		// error.stack;

	});
};

let gribData;
let gribFileLocation;
let polarFile = "./sample.data/polars/CheoyLee42.polar-coeff";

// Callback for GRIBs
let renderGRIBData = function(canvas, context) {
	if (document.getElementById("grib-checkbox")) {
		document.getElementById("grib-checkbox").disabled = false;
		document.getElementById("grib-checkbox").checked = true;
	}

//console.log("Now drawing GRIB");
	if (gribData !== undefined) {
		let date = document.getElementById("grib-dates").value;
		let type = document.getElementById("grib-types").value;
		let colorOption = document.getElementById("wind-color-checkbox");
		let windColorOption = true;
		if (colorOption) {
			windColorOption = colorOption.checked;
		}
		drawGrib(canvas, context, gribData, date, type, windColorOption); // Defined in grib.routing.js
	}
};

// Routing features

let routingPromise = function(payload) {
	let url = "/grib/routing";
	return getPromise(url, DEFAULT_TIMEOUT * 100, 'POST', 200, payload, false);
};

let getBestRoute = function(payload, callback) {

	console.log("From", payload.fromL, payload.fromG, "To", payload.toL, payload.toG,
			"Starting", payload.startTime, "GRIB", payload.gribName,
			"Polars", payload.polarFile);

	let getData = routingPromise(payload);
	getData.then((value) => {
		// console.log("----");
		// console.log("Routing done:", value);
		// console.log("----");
		let json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json);
		} else {
			console.log(JSON.stringify(json, null, 2));
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = typeof(errmess) === 'string' ? JSON.parse(errmess) : errmess;
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("getBestRoute: Failed to get best route..." + (error ? JSON.stringify(error) : ' - ') + ', ' + (message ? message : ' - '));
	});
};