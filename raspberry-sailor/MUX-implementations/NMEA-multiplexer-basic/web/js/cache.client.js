"use strict";

/**
 * Uses ES6 Promises
 */
function cacheClient(dataManager, bp, errorManager) {

	let onMessage = dataManager; // Client function
	let onError = errorManager;  // Client function
	let betweenPing = 1000;
	if (bp) {
		betweenPing = bp;
	}

	function getNMEAData() {

		let url = '/mux/cache',
			xhr = new XMLHttpRequest(),
			verb = 'GET',
			TIMEOUT = 10000,
			happyCode = 200,
			data = null;

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

			let requestTimer = setTimeout(() => {
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

// Executed at startup
	(() => {
		// Long poll
		setInterval(() => {
			fetchData();
		}, betweenPing);
	})();

	function fetchData() {
		let getData = getNMEAData();
		getData.then((value) => {
			//  console.log("Done:", value);
			let json = JSON.parse(value);
			onMessage(json);
		}, (error, errmess) => {
			let message;
			if (errmess) {
				try {
					let mess = JSON.parse(errmess);
					if (mess.message) {
						message = mess.message;
						if (onError) {
						    onError(message);
						} else {
						    onError(mess);
						}
					}
				} catch (err) {
					console.log(err);
				}
			}
			console.log("Failed to get nmea data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
		});
	}
};
