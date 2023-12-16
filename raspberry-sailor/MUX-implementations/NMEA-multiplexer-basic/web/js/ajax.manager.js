/**
 * Uses ES6 Promises.
 * 
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;

function initAjax(forwardErrors=false, ping=1000) {

	forwardAjaxErrors = forwardErrors;
	let interval = setInterval(function () {
		fetchData();
	}, ping);
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
			if (data) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(function () {
			xhr.abort();
			let mess = {code: 408, message: `Timeout (${TIMEOUT}ms) for ${verb} ${url}`};
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
}

function fetchData() {
	let getData = getNMEAData();
	getData.then((value) => {
		// console.log("Done:", value);
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

const EVENT_FULL     = 'full';
const EVENT_POS      = 'pos';
const EVENT_BSP      = 'bsp';
const EVENT_LOG      = 'log';
const EVENT_GPS_TIME = 'gps-time';
const EVENT_GPS_SAT  = 'gps-sat';
const EVENT_HDG      = 'hdg';
const EVENT_TWD      = 'twd';
const EVENT_TWA      = 'twa';
const EVENT_TWS      = 'tws';
const EVENT_WT       = 'wt';
const EVENT_AT       = 'at';
const EVENT_PRMSL    = 'prmsl';
const EVENT_HUM      = 'hum';
const EVENT_AWS      = 'aws';
const EVENT_AWA      = 'awa';
const EVENT_CDR      = 'cdr';
const EVENT_COG      = 'cog';
const EVENT_SOG      = 'sog';
const EVENT_CMG      = 'cmg';
const EVENT_LEEWAY   = 'leeway';
const EVENT_CSP      = 'csg';
const EVENT_WP       = 'wp';
const EVENT_VMG      = 'vmg';
const EVENT_PRATE    = 'prate';
const EVENT_DEW      = 'dew';
const EVENT_AIS      = 'ais';
const EVENT_MARKERS  = 'markers';
const EVENT_BORDERS  = 'borders';
const BORDERS_THREATS = "borders-threats";

function onMessage(json) {
	try {
		let errMess = "";

		try {
			events.publish(EVENT_FULL, json);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "full data");
			console.debug(`onMessage: ${errMess}`);
		}

		// Publishes
		if (json.Position) {
			try {
				let latitude = json.Position.lat;
	//          console.debug("latitude:" + latitude)
				let longitude = json.Position.lng;
	//          console.debug("Pt:" + latitude + ", " + longitude);
				events.publish(EVENT_POS, {
					'lat': latitude,
					'lng': longitude
				});
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
			}
		} else {
			console.debug("No Position");
		}
		if (json.BSP) {
			try {
				let bsp = json.BSP.speed;
				events.publish(EVENT_BSP, bsp);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
			}
		} else {
			console.debug("No BSP");
		}
		if (json.Log) {
			try {
				let log = json.Log.distance;
				events.publish(EVENT_LOG, log);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
			}
		} else {
			console.debug("No LOG");
		}
		if (json["GPS Date & Time"]) {
			try {
				// let gpsDate = json["GPS Date & Time"].date; // Time Zone issue. Use fmtDate
				let gpsDate = new Date(json["GPS Date & Time"].fmtDate.year,
				                       json["GPS Date & Time"].fmtDate.month - 1, // Jan: 0 ... Dec: 11
									   json["GPS Date & Time"].fmtDate.day, 
									   json["GPS Date & Time"].fmtDate.hour, 
									   json["GPS Date & Time"].fmtDate.min, 
									   json["GPS Date & Time"].fmtDate.sec).getTime();
				events.publish(EVENT_GPS_TIME, gpsDate);  
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
			}
		} else {
			console.debug("No GPS Data");
		}

		if (json["HDG true"]) {
			try {
				let hdg = json["HDG true"].angle;
				events.publish(EVENT_HDG, hdg);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
			}
		} else {
			console.debug("No HDG");
		}
		if (json.TWD) {
			try {
				let twd = json.TWD.angle;
				events.publish(EVENT_TWD, twd);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
			}
		} else {
			console.debug("No TWD");
		}
		if (json.TWA) {
			try {
				let twa = json.TWA.angle;
				events.publish(EVENT_TWA, twa);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
			}
		} else {
			console.debug("No TWA");
		}
		if (json.TWS) {
			try {
				let tws = json.TWS.speed;
				events.publish(EVENT_TWS, tws);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
			}
		} else {
			console.debug("No TWS");
		}

		if (json["Water Temperature"]) {
			try {
				let waterTemp = json["Water Temperature"].temperature;
				events.publish(EVENT_WT, waterTemp);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
			}
		} else {
			console.debug("No Water Temp.");
		}

		if (json["Air Temperature"]) {
			try {
				let airTemp = json["Air Temperature"].temperature;
				events.publish(EVENT_AT, airTemp);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
			}
		} else {
			console.debug("No Air Temp.");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		if (json["Barometric Pressure"]) {
			try {
				let baro = json["Barometric Pressure"].value;
				if (baro != 0) {
					events.publish(EVENT_PRMSL, baro);
				}
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
			}
		} else {
			console.debug("No Baro.");
		}
		if (json["Relative Humidity"]) {
			try {
				let hum = json["Relative Humidity"];
				if (hum > 0) {
					events.publish(EVENT_HUM, hum);
				}
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
			}
		} else {
			console.debug("No HUM");
		}
		try {
		    let satData = json["Satellites in view"];
		    if (satData) {
		        events.publish(EVENT_GPS_SAT, satData);
		    }
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Satellites in view");
		}
		if (json.AWS) {
			try {
				let aws = json.AWS.speed;
				events.publish(EVENT_AWS, aws);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
			}
		} else {
			console.debug("No AWS");
		}
		if (json.AWA) {
			try {
				let awa = json.AWA.angle;
				events.publish(EVENT_AWA, awa);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
			}
		} else {
			console.debug("No AWA");
		}
		if (json.CDR) {
			try {
				let cdr = json.CDR.angle;
				events.publish(EVENT_CDR, cdr);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
			}
		} else {
			console.debug("No CDR");
		}
		if (json.COG) {
			try {
				let cog = json.COG.angle;
				events.publish(EVENT_COG, cog);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
			}
		} else {
			console.debug("No COG");
		}
		if (json.CMG) {
			try {
				let cmg = json.CMG.angle;
				events.publish(EVENT_CMG, cmg);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
			}
		} else {
			console.debug("No CMG");
		}
		if (json.Leeway) {
			try {
				let leeway = json.Leeway.angle;
				events.publish(EVENT_LEEWAY, leeway);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
			}
		} else {
			console.debug("No leeway");
		}
		if (json.CSP) {
			try {
				let csp = json.CSP.speed;
				events.publish(EVENT_CSP, csp);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
			}
		} else {
			console.debug("No CSP");
		}
		if (json.SOG) {
			try {
				let sog = json.SOG.speed;
				events.publish(EVENT_SOG, sog);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
			}
		} else {
			console.debug("No SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		if (json["To Waypoint"] && json["Bearing to WP"]) {
			try {
				let to_wp = json["To Waypoint"];
				let b2wp = json["Bearing to WP"].angle;
				events.publish(EVENT_WP, {
					'to_wp': to_wp,
					'b2wp': b2wp
				});
			} catch (err) {
			}
		} else {
			console.debug("No WP info");
		}

		if (json["VMG on Wind"] && json["VMG to Waypoint"]) {
			try {
				events.publish(EVENT_VMG, {
					'onwind': json["VMG on Wind"],
					'onwp': json["VMG to Waypoint"]
				});
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
			}
		} else {
			console.debug("Not enough VMG info");
		}

		if (json.prate) {
			try {
				let prate = json.prate;
				events.publish(EVENT_PRATE, prate);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "prate");
			}
		} else {
			console.debug("No PRATE");
		}
		if (json.dewpoint) {
			try {
				let dew = json.dewpoint;
				events.publish(EVENT_DEW, dew);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
			}
		} else {
			console.debug("No dewpoint");
		}

		try {
			let ais = json.ais;
			if (ais) {
				events.publish(EVENT_AIS, ais);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "ais (" + err + ")");
		}

		// Markers
		try {
			let markers = json['markers-data'];
			if (markers) {
				events.publish(EVENT_MARKERS, markers);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Markers (" + err + ")");
		}

		// Borders
		try {
			let borders = json['borders-data'];
			if (borders) {
				events.publish(EVENT_BORDERS, borders);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Borders (" + err + ")");
		}

		try {
			let borderThreats = json[BORDERS_THREATS];
			if (borderThreats) {
				events.publish(BORDERS_THREATS, borderThreats);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Borders threats (" + err + ")");
		}

		if (errMess && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
