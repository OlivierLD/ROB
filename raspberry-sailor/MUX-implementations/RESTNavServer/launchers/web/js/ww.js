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

var ONE_MINUTE = 60000; // in ms.
var DEFAULT_TIMEOUT = 5 * ONE_MINUTE; // 120000: 2 minutes, 300000: 5 minutes
var WW_VERBOSE = false;

/*
 * Demo features
 * Will get the position from the cache...
 * Default position can be set when starting the NavServer,
 * with -Ddefault.mux.latitude=47.677667 -Ddefault.mux.longitude=-3.135667
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
let getPositionFromNMEACache = function(verbose=false) {
	let url = "/mux/cache";
	if (verbose) {
		console.log(`getPositionFromNMEACache, starting, GET ${url}`);
	}
	return getPromise(url, 30 * ONE_MINUTE, 'GET', 200, null, false);
};

let getGPSPosition = function() {
	if (WW_VERBOSE) {
		console.log(`getGPSPosition, starting`);
	}
	let posPromise = getPositionFromNMEACache(WW_VERBOSE);
	posPromise.then(cache => {
		let cacheValues = JSON.parse(cache);
		if (WW_VERBOSE) {
			console.log(`Pos from Cache ${JSON.stringify(cacheValues)}`);
		}
		if (cacheValues.Position) {
			if (WW_VERBOSE) {
				console.log(`${(new Date())}: Position from Data Cache: ${JSON.stringify(cacheValues.Position)}`);
			}
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

	let withGRIB = true;
	if (document.getElementById("with-grib") && !document.getElementById("with-grib").checked) {
		withGRIB = false;
	}
	if (!withGRIB) {
		return;
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

let pushedCompositeName;

function resetCompositeName() { // Called from the pageXOffset.
	// 1 - Buid pushedCompositeName from compositeData.key and current date, for example: "ATL-0002-FINE-2_2026-02-22T18-37-49Z"
	let date = new Date();
	let dateString = date.toISOString(); // .replace(/:/g, '-');
	pushedCompositeName = `${dateString.substring(0, dateString.lastIndexOf(':'))}`;
	console.log(`- Pushed composite name is now: ${pushedCompositeName}`);
}

// That is the function to push the composite data to the server, and make it available for download and remote display.
async function pushCompositeData(compositeData, requestData) {
	console.log("function pushCompositeData : Composite data ready to be pushed to the server...");
	resetCompositeName();

	// Will use a form like in programmatic.upload.html
/*
compositeData example:
{
    "key": "ATL-0002-FINE-2",
    "name": "North Atlantic, current analysis (fine, 2-day GRIB), v2",
    "comment": "GRIB and Faxes",
    "map": {
        "projection": "MERCATOR",
        "north": 65.5,
        "south": 10,
        "east": 28.2,
        "west": -101.8
    },
    "canvas": {
        "w": 900,
        "h": 600
    },
    "faxData": [
        {
            "faxUrl": "https://tgftp.nws.noaa.gov/fax/PYAA12.gif",
            "name": "North-West Atl surface analysis",
            "transp": "WHITE",
            "rotation": 0,
            "tx": {
                "from": "BLACK",
                "to": "RED"
            },
            "effect": "BLUR",
            "zoom": 0.32291855775920775,
            "location": {
                "x": 13,
                "y": 8
            }
        },
        {
            "faxUrl": "https://tgftp.nws.noaa.gov/fax/PYAA11.gif",
            "name": "North-East Atl surface analysis",
            "transp": "WHITE",
            "rotation": 0,
            "tx": {
                "from": "BLACK",
                "to": "RED"
            },
            "effect": "BLUR",
            "zoom": 0.32291855775920775,
            "location": {
                "x": 401,
                "y": 8
            }
        },
        {
            "faxUrl": "https://tgftp.nws.noaa.gov/fax/PPAA10.gif",
            "name": "North Atl 500mb analysis",
            "transp": "WHITE",
            "tx": {
                "from": "BLACK",
                "to": "BLUE"
            },
            "effect": "BLUR",
            "zoom": 0.49330500605497085,
            "location": {
                "x": 26,
                "y": 27
            }
        },
        {
            "faxUrl": "https://tgftp.nws.noaa.gov/fax/PJAA99.gif",
            "name": "North Atl Sea state",
            "transp": "WHITE",
            "tx": {
                "from": "BLACK",
                "to": "DARKGREEN"
            },
            "effect": "BLUR",
            "zoom": 0.4928121938611098,
            "location": {
                "x": 26,
                "y": 27
            }
        }
    ],
    "gribRequest": "GFS:65N,10N,100W,10E|1,1|0,3..48|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN"
}
*/
    // console.log(compositeData);
/*
requestData example:
[
    {
        "url": "https://tgftp.nws.noaa.gov/fax/PYAA12.gif",
        "name": "North-West Atl surface analysis",
        "storage": "web/2026/02/22/ATL-0002-FINE-2_183749/ATL-0002-FINE-2_0.png",
        "returned": "web/2026/02/22/ATL-0002-FINE-2_183749/_ATL-0002-FINE-2_0.png",
        "transparent": "WHITE",
        "imgType": "png",
        "tx": "BLUR",
        "from": "BLACK",
        "to": "RED",
        "rotation": 0
    },
    {
        "url": "https://tgftp.nws.noaa.gov/fax/PYAA11.gif",
        "name": "North-East Atl surface analysis",
        "storage": "web/2026/02/22/ATL-0002-FINE-2_183749/ATL-0002-FINE-2_1.png",
        "returned": "web/2026/02/22/ATL-0002-FINE-2_183749/_ATL-0002-FINE-2_1.png",
        "transparent": "WHITE",
        "imgType": "png",
        "tx": "BLUR",
        "from": "BLACK",
        "to": "RED",
        "rotation": 0
    },
    {
        "url": "https://tgftp.nws.noaa.gov/fax/PPAA10.gif",
        "name": "North Atl 500mb analysis",
        "storage": "web/2026/02/22/ATL-0002-FINE-2_183749/ATL-0002-FINE-2_2.png",
        "returned": "web/2026/02/22/ATL-0002-FINE-2_183749/_ATL-0002-FINE-2_2.png",
        "transparent": "WHITE",
        "imgType": "png",
        "tx": "BLUR",
        "from": "BLACK",
        "to": "BLUE"
    },
    {
        "url": "https://tgftp.nws.noaa.gov/fax/PJAA99.gif",
        "name": "North Atl Sea state",
        "storage": "web/2026/02/22/ATL-0002-FINE-2_183749/ATL-0002-FINE-2_3.png",
        "returned": "web/2026/02/22/ATL-0002-FINE-2_183749/_ATL-0002-FINE-2_3.png",
        "transparent": "WHITE",
        "imgType": "png",
        "tx": "BLUR",
        "from": "BLACK",
        "to": "DARKGREEN"
    }
]
*/
	// console.log(requestData);
	// Compose new compositeData with the returned file locations
	let newCompositeData = compositeData;
	if (newCompositeData.faxData) {
		for (let i=0; i<newCompositeData.faxData.length; i++) {
			newCompositeData.faxData[i].faxUrl = requestData[i].returned.substring(requestData[i].returned.lastIndexOf('/') + 1); // Just the file name
		}
	}
	console.log(`==> New composite data to push, compositeData.json`);
	proceedJSON('compositeData.json', newCompositeData, pushedCompositeName);
	console.log(`Done with compositeData.json upload`);

	let faxList = [];
	requestData.forEach(data => {
		// let prefix = document.location.origin + document.location.pathname.substring(0, document.location.pathname.lastIndexOf('/'));
		// faxList.push({ file: prefix + "/../" + data.returned, mimeType: "image/png" });
		let fileLocation = data.url;
		if (fileLocation.startsWith('file://'))	 {
			fileLocation = document.location.origin + data.url.substring(data.url.indexOf('/web')); // Wow !
		} else {
			// fileLocation = document.location.origin + "/" + data.returned;
			fileLocation = "../../" + data.returned;
		}
		faxList.push({ file: fileLocation, mimeType: "image/png" }); // TODO Make sure the type is right
		console.log(`- Fax to upload: ${fileLocation}`);
	});

	// Upload faxes.
	if (faxList.length > 0) {
		console.log(`==> Uploading faxes for ${pushedCompositeName}...`);
		proceed(faxList, pushedCompositeName);
		console.log("Done with faxes upload");
	} else {
		console.log(`No faxes to upload for ${pushedCompositeName}.`);
	}
}

// Push GRIB Data on its composite folder. Should be done last, compositeName should have been set before.
async function pushCompositeGRIBData(gribDataJSON) {
	// Will use a form like in programmatic.upload.html
	if (gribDataJSON) {
		console.log(`==> Pushing GRIB data to the server..., for ${pushedCompositeName}`);
		proceedJSON('GRIB.json', gribDataJSON, pushedCompositeName);
		console.log("Done with GRIB upload");
	}
}

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