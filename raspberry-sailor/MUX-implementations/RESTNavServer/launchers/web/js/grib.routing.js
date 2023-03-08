"use strict";

/*
 For debugging,
 set DEBUG to true.
*/
const DEBUG = false;
const VERBOSE = false;
var DEFAULT_TIMEOUT = 60000;

const BEAUFORT_SCALE = [
	0, 1, 4, 7, 11, 16, 22, 28, 34, 41, 48, 56, 64
//  |  |  |  |   |   |   |   |   |   |   |   |   |
//	|  |  |  |   |   |   |   |   |   |   |   |   12
//	|  |  |  |   |   |   |   |   |   |   |   11
//	|  |  |  |   |   |   |   |   |   |   10
//	|  |  |  |   |   |   |   |   |   9
//	|  |  |  |   |   |   |   |   8
//	|  |  |  |   |   |   |   7
//	|  |  |  |   |   |   6
//	|  |  |  |   |   5
//	|  |  |  |   4
//	|  |  |  3
//	|  |  2
//	|  1
//  0
];

let getBeaufortScale = function(tws) {
	let currentForce = 12;
	for (let i=0; i<BEAUFORT_SCALE.length;i++) {
		if (BEAUFORT_SCALE[i] > tws) {
			currentForce = i - 1;
			// console.log(`TWS ${this._value} => Force ${currentForce}`);
			break;
		}
	}
	return currentForce;
}


// let errManager = console.log;
let errManager = function(mess) {
	if (document.getElementById("error")) {
		let content = document.getElementById("error").innerHTML;
		if (content !== undefined) {
			document.getElementById("error").innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
			let div = document.getElementById("error");
			div.scrollTop = div.scrollHeight;
		} else {
			console.log(mess);
		}
	} else {
		console.log(mess);
	}
};

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
			let mess = {code: 408, message: `Timeout (${timeout}ms) for ${verb} ${url}`};
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

let getGRIB = function(request) {
	let url = "/grib/get-data";
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, request, false);
};

let requestGRIB = function(gribRequest, cb) {
	let getData = getGRIB(gribRequest);
	getData.then((value) => {
		// console.log("Done:", value);
		let json = JSON.parse(value);
		// Do something smart here.
		if (cb !== undefined) {
			cb(json);
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("Failed to get the GRIB..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});

};

let getSpeed = function(x, y) {
	let tws = Math.sqrt((x * x) + (y * y));
	tws *= 3.600; // m/s to km/h
	tws /= 1.852; // km/h to knots
	return tws;
};

/**
 * Get wind direction from ugrd, vgrd.
 * TODO: use Math.atan2
 * 
 * @param x ugrd
 * @param y vgrd
 * @returns {number} Direction in degrees [0..360]
 */
let getDir = function(x, y) {
	let dir = 0.0;
	if (y !== 0) {
		dir = toDegrees(Math.atan(x / y));
	}
	if (x <= 0 || y <= 0) {
		if (x > 0 && y < 0) {
			dir += 180;
		} else if (x < 0 && y > 0) {
			dir += 360;
		} else if (x < 0 && y < 0) {
			dir += 180;
		} else if (x === 0) {
			if (y > 0) {
				dir = 0.0;
			} else {
				dir = 180;
			}
		} else if (y === 0) {
			if (x > 0) {
				dir = 90;
			} else {
				dir = 270;
			}
		}
	}
	dir += 180;
	while (dir >= 360) {
		dir -= 360;
	}
	return dir;
};

let toRadians = function (deg) {
	return deg * (Math.PI / 180);
};

let toDegrees = function (rad) {
	return rad * (180 / Math.PI);
};

let GRIBTypes = {
	surfaceWind: 'SFC_WIND'
};

let ajustedLongitude = function(leftBoundary, eastIncrease) {
	let lng = leftBoundary + eastIncrease;
	lng = lng % 360;
	if (lng > 180) {
		lng -= 360;
	}
	return lng;
};

const ARROW_LENGTH = 20;
const WIND_ARROW_TRANSPARENCY = 0.3;

let drawWindArrow = function(context, at, twd, tws) {

	context.lineWidth = 1;

	let roundTWS = Math.round(tws);
	let dTWD = Math.toRadians(twd);

	context.strokeStyle = 'rgba(0, 0, 255, ' + WIND_ARROW_TRANSPARENCY.toString() + ')';

	let x = at.x;
	let y = at.y;

	// Arrow
	let featherX = ARROW_LENGTH * Math.sin(dTWD);
	let featherY = ARROW_LENGTH * Math.cos(dTWD);
	context.beginPath();
	context.moveTo(x, y);
	context.lineTo(x + featherX, y - featherY);
	context.closePath();
	context.stroke();

	// Feathers
	let origin = ARROW_LENGTH;
	while (roundTWS >= 50) {
		roundTWS -= 50;
		let featherStartX = x + (origin * Math.sin(dTWD));
		let featherStartY = y - (origin * Math.cos(dTWD));
		let featherEndX = featherStartX + (10 * Math.sin(dTWD + Math.toRadians(60)));
		let featherEndY = featherStartY - (10 * Math.cos(dTWD + Math.toRadians(60)));
		let featherStartX2 = x + ((origin - 5) * Math.sin(dTWD));
		let featherStartY2 = y - ((origin - 5) * Math.cos(dTWD));
		origin -= 5;

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.lineTo(featherStartX2, featherStartY2);
		context.closePath();
		context.fill();
	}
	while (roundTWS >= 10) {
		roundTWS -= 10;
		let featherStartX = x + (origin * Math.sin(dTWD));
		let featherStartY = y - (origin * Math.cos(dTWD));
		let featherEndX = featherStartX + (7 * Math.sin(dTWD + Math.toRadians(60)));
		let featherEndY = featherStartY - (7 * Math.cos(dTWD + Math.toRadians(60)));

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.closePath();
		context.stroke();
		origin -= 3;
	}
	if (roundTWS >= 5) {
		let featherStartX = x + (origin * Math.sin(dTWD));
		let featherStartY = y - (origin * Math.cos(dTWD));
		let featherEndX = featherStartX + (4 * Math.sin(dTWD + Math.toRadians(60)));
		let featherEndY = featherStartY - (4 * Math.cos(dTWD + Math.toRadians(60)));

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.closePath();
		context.stroke();
	}
};

const BLUE_BASED = 1;
const BEAUFORT_BASED = 2; // Based on the colors used for the Beaufort WebComponent.
const WIND_BG_COLOR_OPTION = BEAUFORT_BASED;

// Duplicated from Beaufort.js. TODO: SOmething nicer
// See https://htmlcolorcodes.com/
const SCALE_COLORS = [
	{ force:  0, color: 'rgb(232, 246, 243)' },
	{ force:  1, color: 'rgb(212, 230, 241)' },
	{ force:  2, color: 'rgb(133, 193, 233)' },
	{ force:  3, color: 'rgb( 93, 173, 226)' },
	{ force:  4, color: 'rgb( 26, 188, 156)' },
	{ force:  5, color: 'rgb( 29, 131,  72)' },
	{ force:  6, color: 'rgb(241, 196,  15)' },
	{ force:  7, color: 'rgb(231,  76,  69)' },
	{ force:  8, color: 'rgb(192,  57,  43)' },
	{ force:  9, color: 'rgb(123,  36,  28)' },
	{ force: 10, color: 'rgb(118,  68, 138)' },
	{ force: 11, color: 'rgb( 91,  44, 111)' },
	{ force: 12, color: 'rgb( 33,  47,  61)' }
];

let getBGColor = function(value, type) {
	let color = 'white';
	switch (type) {
		case 'wind': // blue, [0..80]
			if (WIND_BG_COLOR_OPTION === BLUE_BASED) {
				color = 'rgba(0, 0, 255, ' + Math.min((value / 80), 1) + ')';
			} else { // BEAUFORT
				let force = getBeaufortScale(value);
				let beaufortColor = SCALE_COLORS[force].color; //'rgba(1,2,3, 0.5)';
				let transp = 0.75;
				let transpColor = beaufortColor.replace(/rgb/i, "rgba");
				transpColor = transpColor.replace(/\)/i,`, ${transp})`);
				color = transpColor;
				// console.log(`Force ${force}, color is ${color}`);	
			}
			break;
		case 'prmsl': // red, 101300, [95000..104000], inverted
			color = 'rgba(255, 0, 0,' + (1 - Math.min((value - 95000) / (104000 - 95000), 1)) + ')';
			break;
		case 'hgt': // blue, 5640, [4700..6000], inverted
			color = 'rgba(0, 0, 255, ' + (1 - Math.min((value - 4700) / (6000 - 4700), 1)) + ')';
			break;
		case 'prate': // black, [0..30]. Unit is Kg x m-2 x s-1, which is 1mm.s-1. Turned into mm/h
			let max = 30;
			let mm_per_hour = value * 3600;
			let transp = 	Math.min(((mm_per_hour) / max), 1);
			let blue = Math.max(255 - (mm_per_hour * (255 / max)), 0).toFixed(0);
			// if (mm_per_hour > 20) {
			// 	console.log(`>> Value: ${mm_per_hour} => Blue: ${blue}`);
			// }
			color = `rgba(0, 0, ${blue}, ${transp.toFixed(2)})`; // max 30 mm/h
			break;
		case 'tmp': // blue, to red, [233..323] (Celsius [-40..50]). [-40..0] -> blue. [0..50] -> red
			if (value <= 273) { // lower than 0 C
				color = 'rgba(0, 0, 255,' + (1 - Math.min((value - 233) / (273 - 233), 1)) + ')'; // Blue
			} else {
				color = 'rgba(255, 0, 0,' + Math.min((value - 273) / (323 - 273), 1) + ')'; // Red
			}
			break;
		case 'htsgw': // green, [0..15]
			color = 'rgba(0, 100, 0,' + Math.min((value) / 15, 1) + ')';
			break;
		default:
			break;
	}
	return color;
};

let routingResult = undefined;
let plotBestRoute = function(canvas, context) {
	// console.log("Plotting the best computed route: ", routingResult);
	let waypoints, isochrons;
	//if (routingResult.waypoints) { // This is for dev...
	//  	waypoints = routingResult.waypoints;
	//} else {
		waypoints = JSON.parse(routingResult.bestRoutes.JSON).waypoints;
		// Isochrons are in routingResult.isochronals
		isochrons = routingResult.isochronals;
	//}
	context.save();

	// On option: draw isochrones here
	if (isochrons) {
		context.strokeStyle = 'blue';
		context.lineWidth = 0.5;
		isochrons.forEach(isochron => {  // For each isochron

			context.beginPath();
			isochron.forEach((point, idx) => { // For each point in one isochron
				let canvasPt = worldMap.getCanvasLocation(canvas, point.position.latitude, point.position.longitude);
				if (idx === 0) {
					context.moveTo(canvasPt.x, canvasPt.y);
				} else {
					context.lineTo(canvasPt.x, canvasPt.y);
				}
				// Ancestor ?
				if (point.ancestor) {
					// let ancestorPt = worldMap.getCanvasLocation(canvas, point.ancestor.position.latitude, point.ancestor.position.longitude);
					let ancestorPt = worldMap.getCanvasLocation(canvas, point.ancestor.latitude, point.ancestor.longitude);
					context.moveTo(ancestorPt.x, ancestorPt.y);
					context.lineTo(canvasPt.x, canvasPt.y);
				}
			});
			context.stroke();
			context.closePath();
		});
	}
	context.restore();
	context.save();

	context.strokeStyle = 'orange';
	context.lineWidth = 3;
	context.beginPath();
	for (let i=0; i<waypoints.length; i++) {
//	console.log("Plot", waypoints[i].position.latitude + " / " + waypoints[i].position.longitude);
		let canvasPt = worldMap.getCanvasLocation(canvas, waypoints[i].position.latitude, waypoints[i].position.longitude);
		// console.log();
		if (i === 0) {
			context.moveTo(canvasPt.x, canvasPt.y); 
		} else {
			context.lineTo(canvasPt.x, canvasPt.y);
		}
	}
	context.stroke();
	context.closePath();
	context.restore();
};

// Invoked by the callback
let drawGrib = function(canvas, context, gribData, date, type) {
	let oneDateGRIB = gribData[0]; // Default

	// Look for the right date
	for (let i=0; i<gribData.length; i++) {
		if (gribData[i].gribDate.formattedUTCDate === date) {
			oneDateGRIB = gribData[i];
			break;
		}
	}

	// Base this on the type.
	let data = {}; // ugrd, vgrd
	// Look for the right data
	switch (type) {
		case 'wind': // Hybrid type
			for (let i = 0; i < oneDateGRIB.typedData.length; i++) {
				if (oneDateGRIB.typedData[i].gribType.type === 'ugrd') {
					data.x = oneDateGRIB.typedData[i].data;
				} else if (oneDateGRIB.typedData[i].gribType.type === 'vgrd') {
					data.y = oneDateGRIB.typedData[i].data;
				}
			}
			break;
		case 'hgt': // 500mb, gpm
		case 'tmp': // Air temp, K
		case 'prmsl': // Atm Press, Pa
		case 'htsgw': // Wave Height, m
		case 'prate': // Precipitation rate, kg/m^2/s
			for (let i = 0; i < oneDateGRIB.typedData.length; i++) {
				if (oneDateGRIB.typedData[i].gribType.type === type) {
					data.x = oneDateGRIB.typedData[i].data;
				}
			}
			break;
		default:
			break;
	}
	if (VERBOSE) {
		console.log(">> Type %s, Date: %s", type, date);
		console.log("   Dim (W x H) : %d x %d", data.x[0].length, data.x.length);
	}

	let maxTWS = 0;

	for (let hGRIB=0; hGRIB<oneDateGRIB.gribDate.height; hGRIB++) {
		// Actual width... Waves Height has a different lng step.
		let stepX = oneDateGRIB.gribDate.stepx;
		let width = oneDateGRIB.gribDate.width;

		// Find the typedData
		let typedData;
		for (let t=0; t<oneDateGRIB.typedData.length; t++) {
			if (type === oneDateGRIB.typedData[t].gribType.type) {
				typedData = oneDateGRIB.typedData[t];
				break;
			}
		}

		if (typedData !== undefined && typedData.data[0].length !== oneDateGRIB.gribDate.width) {
			width = typedData.data[0].length;
			stepX *= (oneDateGRIB.gribDate.width / typedData.data[0].length);
		}

		for (let wGRIB=0; wGRIB<width; wGRIB++) {
			// Evaluate the cell (lat/lng): [0][0] is bottom left (SW).
			// 1. Cell BG
			let bottomLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB)));
			let bottomRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB) + (stepX)));
			let topLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB)));
			let topRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB) + (stepX)));

			let gribValue;
			if (type === 'wind') {
				gribValue = getSpeed(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
			} else {
				if (data.x) {
					gribValue = data.x[hGRIB][wGRIB];
				}
			}

			// BG Color
			context.fillStyle = getBGColor(gribValue, type);
			if (VERBOSE && type === 'htsgw' && gribValue > 4) {
				console.log(">> Cell (X, Y) (%d, %d): %s => %f", wGRIB, hGRIB, type, gribValue);
			}
			context.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
//		context.stroke();

			if (type === 'wind') {
				// Center of the cell
				let lng = ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB) + (oneDateGRIB.gribDate.stepx / 2));
				let lat  = oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy / 2));
				// data
				let dir = getDir(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
				let speed = gribValue;
		    // console.log("%f / %f (cell %d, %d), dir %s, speed %f kn (u: %f, v: %f)", lat, lng, hGRIB, wGRIB, dir.toFixed(0), speed, data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);

				let canvasPt = worldMap.getCanvasLocation(canvas, lat, lng);
				drawWindArrow(context, canvasPt, dir, speed);

				maxTWS = Math.max(maxTWS, speed);
			}

			// DEBUG, print cell coordinates IN the cell.
			if (DEBUG) {
				let label = "h:" + hGRIB;
				context.fillStyle = 'black';
				context.font = "8px Courier";
				context.fillText(label, topLeft.x + 1, topLeft.y + 9);
				label = "w:" + wGRIB;
				context.fillText(label, topLeft.x + 1, topLeft.y + 18);
			}
		}
	}
	// console.log("Max TWS: %d kn", maxTWS);
	try {
		document.getElementById('max-wind').innerText = `Max GRIB TWS: ${maxTWS.toFixed(2)} kn (Force ${ getBeaufortScale(maxTWS) })`;
	} catch (err) {}
	// Is there a route to draw here?
	if (routingResult !== undefined) {
		plotBestRoute(canvas, context); // TODO Option/checkbox to show/hide routing, isochrons
	}
};

// For tests
/*
let gribData = [
	{
		"gribDate": {
			"date": "Nov 30, 2017, 12:00:00 PM",
			"epoch": 1512072000000,
			"formattedUTCDate": "2017-11-30 12:00:00 UTC",
			"height": 56,
			"width": 61,
			"stepx": 2,
			"stepy": 2,
			"top": 65,
			"bottom": -45,
			"left": 130,
			"right": -110
		},
		"typedData": [
			{
				"gribType": {
					"type": "hgt",
					"desc": "Geopotential height",
					"unit": "gpm",
					"min": 4919.866,
					"max": 5899.366
				},
				"data": [
					[
						5480.386,
						5474.5264,
						5472.706,
...
						5073.241
					]
				]
			}
		]
	}
];
drawGrib(null, null, gribData, null, null);
*/
