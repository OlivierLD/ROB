/*
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;

function initAjax(forwardErrors) {
	if (forwardErrors !== undefined) {
		forwardAjaxErrors = forwardErrors;
	}
	let interval = setInterval(function () {
		fetch();
	}, 1000);
}

const FETCH_TIMEOUT = 15000;

function getNMEAData() {

	let url = '/mux/cache',
			xhr = new XMLHttpRequest(),
			verb = 'GET',
			data = null,
			happyCode = 200,
			TIMEOUT = FETCH_TIMEOUT;

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
}

const FETCH_VERBOSE = false;

function fetch() {
	try {
		let getData = getNMEAData();
		getData.then((value) => {
			if (FETCH_VERBOSE) {
				console.log("Done:", value);
			}
			let json = JSON.parse(value);
			onMessage(json);
		}, (error, errmess) => {
			let message;
			if (errmess !== undefined) {
				let mess = JSON.parse(errmess);
				if (mess.message !== undefined) {
					message = mess.message;
				}
			}
			console.debug("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
		});
	} catch (err) {
		console.log(`Oops: ${err}`);
	}
}

// Topics
const FULL = 'full';
const POS = 'pos';
const BSP = 'bsp';
const LOG = 'log';
const GPS_TIME = 'gps-time';
const HDG = 'hdg';
const TWD = 'twd';
const TWA = 'twa';
const TWS = 'tws';
const WT = 'wt';
const AT = 'at';
const PRMSL = 'prmsl';
const HUM = 'hum';
const AWS = 'aws';
const AWA = 'awa';
const CDR = 'cdr';
const CDR_30000 = 'cdr-30000';
const CDR_60000 = 'cdr-60000';
const CDR_600000 = 'cdr-600000';
const COG = 'cog';
const CMG = 'cmg';
const LEEWAY = 'leeway';
const CSP = 'csp';
const CSP_30000 = 'csp-30000';
const CSP_60000 = 'csp-60000';
const CSP_600000 = 'csp-600000';
const SOG = 'sog';
const WP = 'wp';
const VMG = 'vmg';
const PRATE = 'prate';
const DEW = 'dew';
const AIS = 'ais';
const MARKERS = 'markers';
const BORDERS = 'borders';
const BORDERS_THREATS = 'borders-threats';
const TRUE_HDG = 'true-hdg';

function onMessage(json) {
	try {
		let errMess = "";

		try {
			events.publish(FULL, json);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "FULL");
		}
		try {
			let latitude = json.Position.lat;
//          console.log("latitude:" + latitude)
			let longitude = json.Position.lng;
//          console.log("Pt:" + latitude + ", " + longitude);
			events.publish(POS, {
				'lat': latitude,
				'lng': longitude
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Displays
		try {
			let bsp = json.BSP.speed;
			events.publish(BSP, bsp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
			let log = json.Log.distance;
			events.publish(LOG, log);
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
			events.publish(GPS_TIME, gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
			let hdg = json["HDG true"].angle;
			events.publish(HDG, hdg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			let twd = json.TWD.angle;
			events.publish(TWD, twd);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			let twa = json.TWA.angle;
			events.publish(TWA, twa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			let tws = json.TWS.speed;
			events.publish(TWS, tws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			let waterTemp = json["Water Temperature"].temperature;
			events.publish(WT, waterTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			let airTemp = json["Air Temperature"].temperature;
			events.publish(AT, airTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Relative_Humidity, Barometric_Pressure
		try {
			let baro = json["Barometric Pressure"].value;
			if (baro != 0) {
				events.publish(PRMSL, baro);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish(HUM, hum);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			let aws = json.AWS.speed;
			events.publish(AWS, aws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			let awa = json.AWA.angle;
			events.publish(AWA, awa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			let cdr = json.CDR.angle;
			events.publish(CDR, cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}
		try {
		    let cdr = json["Current calculated with damping"]["30000"].direction.angle;
			events.publish(CDR_30000, cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR-30000");
		}
		try {
		    let cdr = json["Current calculated with damping"]["60000"].direction.angle;
			events.publish(CDR_60000, cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR-60000");
		}
		try {
		    let cdr = json["Current calculated with damping"]["600000"].direction.angle;
			events.publish(CDR_600000, cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR-600000");
		}

		try {
			let cog = json.COG.angle;
			events.publish(COG, cog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			let cmg = json.CMG.angle;
			events.publish(CMG, cmg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			let leeway = json.Leeway.angle;
			events.publish(LEEWAY, leeway);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			let csp = json.CSP.speed;
			events.publish(CSP, csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}
		try {
		    let csp = json["Current calculated with damping"]["30000"].speed.speed;
			events.publish(CSP_30000, csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP-30000");
		}
		try {
		    let csp = json["Current calculated with damping"]["60000"].speed.speed;
			events.publish(CSP_60000, csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP-60000");
		}
		try {
		    let csp = json["Current calculated with damping"]["600000"].speed.speed;
			events.publish(CPS_600000, csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP-600000");
		}
		try {
			let sog = json.SOG.speed;
			events.publish(SOG, sog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
			let to_wp = json["To Waypoint"];
			let b2wp = json["Bearing to WP"].angle;
			events.publish(WP, {
				'to_wp': to_wp,
				'b2wp': b2wp
			});
		} catch (err) {
		}

		try {
			events.publish(VMG, {
				'onwind': json["VMG on Wind"],
				'onwp': json["VMG to Waypoint"]
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		try {
			let prate = json.prate;
			events.publish(PRATE, prate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "prate");
		}
		try {
			let dew = json.dewpoint;
			events.publish(DEW, dew);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
		}

		try {
			let ais = json.ais;
			if (ais) {
				events.publish(AIS, ais);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "ais (" + err + ")");
		}
		// Markers
		try {
			let markers = json['markers-data'];
			if (markers) {
				events.publish(MARKERS, markers);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Markers (" + err + ")");
		}

		// Borders
		try {
			let borders = json['borders-data'];
			if (borders) {
				events.publish(BORDERS, borders);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Borders (" + err + ")");
		}

		try {
			let borderThreats = json['borders-threats'];
			if (borderThreats) {
				events.publish(BORDERS_THREATS, borderThreats);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Borders threats (" + err + ")");
		}

		try {
		    let hdg = json["HDG true"].angle;
		    if (hdg) {
                events.publish(TRUE_HDG, hdg);
		    }
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "True Heading (" + err + ")");
		}

		try {
		    let bsp = json["BSP"].speed;
		    if (bsp) {
                events.publish(BSP, bsp);
		    }
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "True Heading (" + err + ")");
		}

		if (errMess !== undefined && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
