/*
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;

function initAjax(forwardErrors) {
	if (forwardErrors) {
		forwardAjaxErrors = forwardErrors;
	}
	let interval = setInterval(() => {
		fetchNMEA();
	}, 1000);
}

function getNMEAData() {

	let url = '/mux/cache',
		xhr = new XMLHttpRequest(),
		verb = 'GET',
		data = null,
		happyCode = 200,
		TIMEOUT = 10000;

	let promise = new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();

		let req = verb + " " + url;
		if (data) {
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

		let requestTimer = setTimeout(() => {
			xhr.abort();
			let mess = {code: 408, message: `Timeout (${TIMEOUT}ms) for ${verb} ${url}`};
			reject(mess);
		}, TIMEOUT);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
	return promise;
}

function fetchNMEA() {
	let getData = getNMEAData();
	getData.then((value) => {
		console.log("Done:", value);
		let json = JSON.parse(value);
		onMessage(json);
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		console.debug("Failed to get nmea data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});
}

function onMessage(json) {
	try {
		let errMess = "";

		try {
			let latitude = json.Position.lat;
//          console.log("latitude:" + latitude)
			let longitude = json.Position.lng;
//          console.log("Pt:" + latitude + ", " + longitude);
			events.publish('pos', {
				'lat': latitude,
				'lng': longitude
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Displays
		try {
			let bsp = json.BSP.speed;
			events.publish('bsp', bsp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
			let log = json.Log.distance;
			events.publish('log', log);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
		}
		try {
			// let gpsDate = json["GPS Date & Time"].date;
			let gdt = json["GPS Date & Time"];
			let gpsDate = new Date(gdt.fmtDate.year,
			                       gdt.fmtDate.month - 1,
			                       gdt.fmtDate.day,
			                       gdt.fmtDate.hour,
			                       gdt.fmtDate.min,
			                       gdt.fmtDate.sec).getTime();
			events.publish('gps-time', gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
			let hdg = json["HDG true"].angle;
			events.publish('hdg', hdg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			let twd = json.TWD.angle;
			events.publish('twd', twd);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			let twa = json.TWA.angle;
			events.publish('twa', twa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			let tws = json.TWS.speed;
			events.publish('tws', tws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			let waterTemp = json["Water Temperature"].temperature;
			events.publish('wt', waterTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			let airTemp = json["Air Temperature"].temperature;
			events.publish('at', airTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Relative_Humidity, Barometric_Pressure
		try {
			let baro = json["Barometric Pressure"].value;
			if (baro != 0) {
				events.publish('prmsl', baro);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish('hum', hum);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			let aws = json.AWS.speed;
			events.publish('aws', aws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			let awa = json.AWA.angle;
			events.publish('awa', awa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			let cdr = json.CDR.angle;
			events.publish('cdr', cdr);
			events.publish('cdr-30000', cdr);
			events.publish('cdr-60000', cdr);
			events.publish('cdr-600000', cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
			let cog = json.COG.angle;
			events.publish('cog', cog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			let cmg = json.CMG.angle;
			events.publish('cmg', cmg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			let leeway = json.Leeway.angle;
			events.publish('leeway', leeway);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			let csp = json.CSP.speed;
			events.publish('csp', csp);
			events.publish('csp-30000', csp);
			events.publish('csp-60000', csp);
			events.publish('csp-600000', csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}
		try {
			let sog = json.SOG.speed;
			events.publish('sog', sog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
			let to_wp = json["To Waypoint"];
			let b2wp = json["Bearing to WP"].angle;
			events.publish('wp', {
				'to_wp': to_wp,
				'b2wp': b2wp
			});
		} catch (err) {
		}

		try {
			events.publish('vmg', {
				'onwind': json["VMG on Wind"],
				'onwp': json["VMG to Waypoint"]
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		try {
			let prate = json.prate;
			events.publish('prate', prate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "prate");
		}
		try {
			let dew = json.dewpoint;
			events.publish('dew', dew);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
		}

		if (errMess && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
