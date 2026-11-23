/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;

let DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data = null,             // payload, when needed (PUT, POST...)
    show = true,             // Show the traffic [true]|false
    headers = null) {        // Array of { name: '', value: '' }

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
        if (data !== undefined && data !== null) {
            req += ("\n" + JSON.stringify(data, null, 2));
        }

        xhr.open(verb, url, true);
        if (headers === null) {
            xhr.setRequestHeader("Content-type", "application/json");
        } else {
            headers.forEach(header => xhr.setRequestHeader(header.name, header.value));
        }
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
}


/*
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;

function initAjax(forwardErrors) {

    // Example:
    // ISS Position http://api.open-notify.org/iss-now.json
    // ISS Passage time http://api.open-notify.org/iss-pass.json?lat=37.7&lon=-122.5 [ &alt=20&n=5 ]
    // ISS Crew members: http://api.open-notify.org/astros.json
    if (false) { //
        let issInterval = setInterval(() => {
            let issPromise = getISSData();
            issPromise.then(issData => {
                console.log('ISSData:', issData);
            }, (error, message) => {
                console.debug('ISSData error', error, message);
            });
        }, 5000);
    }

	if (forwardErrors !== undefined) {
		forwardAjaxErrors = forwardErrors;
	}
	let interval = setInterval(function () {
		fetchNMEA();
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

function fetchNMEA() {
	try {
		let getData = getNMEAData();
		getData.then((value) => {
			if (FETCH_VERBOSE) {
				console.log("Done:", value); // Value is text data
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
			console.debug("Failed to get nmea data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message) : ' - '));
		});
	} catch (err) {
		console.log(`Oops: ${err}`);
	}
}

function pushNMEAData(nmeaData) {

	let url = '/mux/nmea-sentence',
			xhr = new XMLHttpRequest(),
			verb = 'POST',
			data = nmeaData,
			happyCode = 201,
			TIMEOUT = FETCH_TIMEOUT;

	return new Promise(function (resolve, reject) {
		let xhr = new XMLHttpRequest();

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		xhr.setRequestHeader("Content-type", "text/plain");
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(data); // JSON.stringify(data));
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
