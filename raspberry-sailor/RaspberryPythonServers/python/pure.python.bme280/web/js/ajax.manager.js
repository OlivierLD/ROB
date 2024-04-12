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

function getTheData() {

	let url = '/bme280/data',
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

function showCustomAlert(message) {
	let dialog = document.getElementById('custom-alert');

	if (dialog) {
		let content = document.getElementById('custom-alert-content');
		if (content) {
			content.innerHTML = message;
			dialog.show();
		} else {
			console.log(message);
		}
	} else {
		console.log(message);
	}
}

function closeCustomAlert() {
	let dialog = document.getElementById('custom-alert');
	if (dialog) {
		dialog.close();
	}
}

function fetchData(errCallback) {
	// Display popup, fetching data
	showCustomAlert('Fetching data, please wait...');
	let before = (new Date()).getTime();
	let getData = getTheData();
	getData.then((value) => {
		let after = (new Date()).getTime();
		console.log(`Done in ${after - before} ms.`);
		// Close popup
		closeCustomAlert();
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
		// Close popup
		closeCustomAlert();
		if (errCallback) {
			errCallback(error, message);
		} else {
			console.debug("Failed to get data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
		}
	});
}

const EVENT_FULL     = 'full';
const EVENT_AT       = 'at';
const EVENT_PRMSL    = 'prmsl';
const EVENT_HUM      = 'hum';  // Relative
const EVENT_DEW      = 'dew';
const EVENT_AH       = 'ah';   // Absolute

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
		if (json["instant"]["temperature"]) {
			try {
				let airTemp = json["instant"]["temperature"];
				events.publish(EVENT_AT, airTemp);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
			}
		} else {
			console.debug("No Air Temp.");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		if (json["instant"]["pressure"]) {
			try {
				let baro = json["instant"]["pressure"];
				if (baro != 0) {
					events.publish(EVENT_PRMSL, baro);
				}
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
			}
		} else {
			console.debug("No Baro.");
		}
		if (json["instant"]["humidity"]) {
			try {
				let hum = json["instant"]["humidity"];
				if (hum > 0) {
					events.publish(EVENT_HUM, hum);
				}
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
			}
		} else {
			console.debug("No HUM");
		}
		if (json["instant"]["dew-point"]) {
			try {
				let dew = json["instant"]["dew-point"];
				events.publish(EVENT_DEW, dew);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
			}
		} else {
			console.debug("No dewpoint");
		}
		if (json["instant"]["abs-hum"]) {
			try {
				let ah = json["instant"]["abs-hum"];
				events.publish(EVENT_AH, ah);
			} catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "ah");
			}
		} else {
			console.debug("No Absolute Humidity");
		}

		if (errMess && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
