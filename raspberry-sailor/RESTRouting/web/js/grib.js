"use strict";

const DEBUG = false;
const DEFAULT_TIMEOUT = 60000; // 1 minute

// var errManager = console.log;
let errManager = (mess) => {
    let errorElement = document.getElementById("error");
    if (errorElement) {
        let content = errorElement.innerHTML;
        if (content !== undefined) {
            errorElement.innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
            errorElement.scrollTop = errorElement.scrollHeight; // Scroll down
        } else {
            console.log(JSON.stringify(mess, null, 2));
        }
    } else {
        alert("Where do you call me from ??! (Check the console)");
        console.log(JSON.stringify(mess, null, 2));
    }
};

let getQueryParameterByName = (name, url) => {
	if (!url) {
	    url = window.location.href;
	}
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
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
    url,                          // full resource path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data = null,                  // payload, when needed (PUT, POST...)
    show = true,                  // Show the traffic [true]|false
    headers = null) => {          // Array of { name: 'Header-Name', value: 'Header-Value' }

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
            let mess = { code: 408, message: `Timeout for ${verb} ${url}` };
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

let getGRIB = (request) => {
	var url = "/grib/get-data";
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, request, false);
};

let requestGRIB = (gribRequest) => {
	let getData = getGRIB(gribRequest);
	getData.then(value => {
		let json = JSON.parse(value);
		// Do something smart here.
		console.log("GRIB Data:", json);
		document.getElementById("result").innerHTML = "<pre>" + JSON.stringify(json, null, 2) + "</pre>";
	}, (error, errmess) => {
		var message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager("Failed to get the GRIB..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};
