"use strict";

const DEFAULT_TIMEOUT = 60000;
const DEBUG = false;

// var errManager = console.log;
let errManager = (mess) => {
	let errorElement = document.getElementById("error");
	if (errorElement) {
		let content = errorElement.innerHTML;
		errorElement.innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
		errorElement.scrollTop = errorElement.scrollHeight;
	} else {
		console.log(mess);
	}
};

// var messManager = console.log;
let messManager = (mess) => {
	let messageElement = document.getElementById("messages");
	let content = messageElement.innerHTML;
	messageElement.innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	messageElement.scrollTop = messageElement.scrollHeight;
};

let getQueryParameterByName = (name, url) => {
	if (!url) {
		url = window.location.href;
	}
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)");
	let results = regex.exec(url);
	if (!results) {
		return null;
	}
	if (!results[2]) {
		return '';
	}
	return decodeURIComponent(results[2].replace(/\+/g, " "));
};

/* Uses ES6 Promises */
let getPromise = (
	url,                          // full api path
	timeout,                      // After that, fail.
	verb,                         // GET, PUT, DELETE, POST, etc
	happyCode,                    // if met, resolve, otherwise fail.
	data = null,                  // payload, when needed (PUT, POST...)
	show = false) => {            // Show the traffic [true]|false

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
	}

	let promise = new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		let req = verb + " " + url;
		if (data) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}
		if (DEBUG) {
			console.log("Request:", req);
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

		let requestTimer = setTimeout(() => {
			xhr.abort();
			let mess = {code: 408, message: 'Timeout'};
			reject(mess);
		}, TIMEOUT);

		xhr.onload = () => {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
	return promise;
};

let getCurrentTime = () => {
	let url = "/astro/utc";
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getTideStations = (offset, limit, filter) => {
	let url = "/tide/tide-stations";
	if (! isNaN(parseInt(offset))) {
		url += ("?offset=" + offset);
	}
	if (! isNaN(parseInt(limit))) {
		url += ((url.indexOf("?") > -1 ? "&" : "?") + "limit=" + limit);
	}
	if (filter !== undefined) {
		url += ((url.indexOf("?") > -1 ? "&" : "?") + "filter=" + encodeURIComponent(filter));
	}
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getTideStationsFiltered = filter => {
	let url = "/tide/tide-stations/" + encodeURIComponent(filter);
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

/**
 * POST /astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
 * 		payload { latitude: 37.76661945, longitude: -122.5166988 }
 * @param from
 * @param to
 * @param tz
 * @param pos
 */
let requestDaylightData = (from, to, tz, pos) => {
	let url = "/astro/sun-between-dates?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

let requestSunMoontData = (from, to, tz, pos) => {
	let url = "/astro/sun-moon-dec-alt?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

const DURATION_FMT = "Y-m-dTH:i:s";

// Also encodes parenthesis and other stuff
function fixedEncodeURIComponent(str) {
  return encodeURIComponent(str).replace(/[!'()*]/g, function(c) {
    return '%' + c.charCodeAt(0).toString(16);
  });
}

/**
 *
 * @param station
 * @param at A json Object like { year: 2017, month: 09, day: 06 }. month 09 is Sep.
 * @param tz
 * @param step
 * @param unit
 * @param withDetails
 * @param nbDays
 */
let getTideTable = (station, at, tz, step, unit, withDetails, nbDays) => {
	if (nbDays === undefined) {
		nbDays = 1;
	}
	let url = "/tide/tide-stations/" + fixedEncodeURIComponent(station) + "/wh";
	if (withDetails === true) {
		url += "/details";
	}
	// From and To parameters
	let now = new Date();
	let year = (at !== undefined && at.year !== undefined ? at.year : now.getFullYear());
	let month = (at !== undefined && at.month !== undefined ? at.month - 1 : now.getMonth());
	let day = (at !== undefined && at.day !== undefined ? at.day : now.getDate());
	let from = new Date(year, month, day, 0, 0, 0, 0);
	let to = new Date(from.getTime() + (nbDays * 3600 * 24 * 1000) + 1000); // + (x * 24h) and 1s
	let fromPrm = from.format(DURATION_FMT);
	let toPrm = to.format(DURATION_FMT);
	url += ("?from=" + fromPrm + "&to=" + toPrm);

	let data = null; // Payload
	if (tz.length > 0 || step.length > 0 || unit.length > 0) {
		data = {};
		if (tz.length > 0) {
			data.timezone = tz;
		}
		if (step.length > 0) {
			data.step = parseInt(step);
		}
		if (unit.length > 0) {
			data.unit = unit;
		}
	}
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

let getPublishedDoc = (station, options) => {
	let url = "/tide/publish/" + fixedEncodeURIComponent(station);
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

let getSunDataAtPos = (lat, lng) => {
	let url = "/astro/sun-now";
	let data = {
	    position: {
		    latitude: lat,
		    longitude: lng
		}
	}; // Payload
	// data.latitude = lat;
	// data.longitude = lng;
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

let tideStations = (offset, limit, filter, callback) => {
	let getData = getTideStations(offset, limit, filter);
	getData.then(value => {
		let json = JSON.parse(value);
		// Do something smart
		messManager("Got " + json.length + " stations.");
		if (callback === undefined) {
			json.forEach(function (ts, idx) {
				try {
					json[idx] = decodeURI(decodeURIComponent(ts));
				} catch (err) {
					console.log("Oops:" + ts);
				}
			});
			document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
		} else {
			callback(json);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get the station list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let tideStationsFiltered = filter => {
	let getData = getTideStationsFiltered(filter);
	getData.then(value => {
		let json = JSON.parse(value);
		// Do something smart
		messManager("Got " + json.length + " station(s)");
		json.forEach(function(ts, idx) {
			try {
				ts.fullName = decodeURIComponent(decodeURIComponent(ts.fullName));
				ts.nameParts.forEach(function(np, i) {
					ts.nameParts[i] = decodeURIComponent(decodeURIComponent(np));
				});
			} catch (err) {
				console.log("Oops:" + ts);
			}
		});
		document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get the station list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let getSunData = (from, to, tz, pos, callback) => {
	let getData = requestDaylightData(from, to, tz, pos);
	getData.then(value => {
		let json = JSON.parse(value);
		if (callback === undefined) {
			// Do something smart
			messManager("Got " + json);
			document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
		} else {
			callback(json);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get Sun data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let getSunMoonCurves = (from, to, tz, pos, callback) => {
	let getData = requestSunMoontData(from, to, tz, pos);
	getData.then(value => {
		let json = JSON.parse(value);
		if (callback === undefined) {
			// Do something smart
			messManager("Got " + json);
			document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
		} else {
			callback(json);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get Sun & Moon data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let showTime = () => {
	let getData = getCurrentTime();
	getData.then(value => {
		let json = JSON.parse(value);
		// Do something smart
		document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get Time..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let tideTable = (station, at, tz, step, unit, withDetails, nbDays, callback) => {
	let getData = getTideTable(station, at, tz, step, unit, withDetails, nbDays);
	getData.then(value => {
		if (callback === undefined) {
			try {
				let json = JSON.parse(value);
				// Do something smart
				json.stationName = decodeURIComponent(decodeURIComponent(json.stationName));
				document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
			} catch (err) {
				errManager(err + '\nFor\n' + value);
			}
		} else {
			callback(value);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get the station data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let publishTable = (station, options, callback) => {
	let getData = getPublishedDoc(station, options);
	getData.then(value => {
		if (callback === undefined) {
			try {
				// Do something smart
				document.getElementById("result").innerHTML = ("<pre>" + value + "</pre>");
			} catch (err) {
				errManager(err + '\nFor\n' + value);
			}
		} else {
			callback(value);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed publish station data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

let sunData = (lat, lng, callback) => {
	let getData = getSunDataAtPos(lat, lng);
	getData.then(value => {
		if (callback === undefined) {
			try {
				let json = JSON.parse(value);
				// Do something smart
				let strLat = decToSex(json.lat, "NS");
				let strLng = decToSex(json.lng, "EW");
				let strDecl = decToSex(json.decl, "NS");
				let strGHA = decToSex(json.gha);

				document.getElementById("result").innerHTML = ("<pre>" +
						JSON.stringify(json, null, 2) +
						"<br/>" +
						( strLat + " / " + strLng) +
						"<br/>" +
						new Date(json.epoch) +
						"<br/>" +
						("Dec: " + strDecl) +
						"<br/>" +
						("GHA: " + strGHA) +
						"<br/>" +
						("Meridian Pass. Time: " + hoursDecimalToHMS(json.eot) + " UTC") +
						"<br/>" +
						("Rise: " + hoursDecimalToHMS(json.riseTime) + " UTC") +
						"<br/>" +
						("Set: " + hoursDecimalToHMS(json.setTime) + " UTC") +
						"</pre>");
			} catch (err) {
				errManager(err + '\nFor\n' + value);
			}
		} else {
			callback(value);
		}
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get the station data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};
