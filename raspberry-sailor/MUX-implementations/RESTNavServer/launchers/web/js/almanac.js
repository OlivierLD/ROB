"use strict";

var DEFAULT_TIMEOUT = 300000; // 5 minutes // 60000; // 1 minute

// let errManager = console.log;
let errManager = function(mess) {
	let content = document.getElementById("error").innerHTML;
	document.getElementById("error").innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	try {
		flipTab('error-tab');
	} catch (err) {
		console.log(err);
	}
	let div = document.getElementById("error");
	div.scrollTop = div.scrollHeight;
};

// let messManager = console.log;
let messManager = function(mess) {
	let content = document.getElementById("messages").innerHTML;
	document.getElementById("messages").innerHTML = ((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	try {
		flipTab('message-tab');
	} catch (err) {
		console.log(err);
	}
	let div = document.getElementById("messages");
	div.scrollTop = div.scrollHeight;
};

let getQueryParameterByName = function(name, url) {
	if (!url) url = window.location.href;
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
	if (!results) return null;
	if (!results[2]) return '';
	return decodeURIComponent(results[2].replace(/\+/g, " "));
};

function getPromise(url,                          // full api path
					timeout,                      // After that, fail.
					verb,                         // GET, PUT, DELETE, POST, etc
					happyCode,                    // if met, resolve, otherwise fail.
					data,                         // payload, when needed (PUT, POST...) TODO: page fmt
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

const DURATION_FMT = "Y-m-dTH:i:s";

let getPerpetualDoc = function(options) {
	let url = "/astro/publish/perpetual";
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

let getAlmanac = function(options) {
	let url = "/astro/publish/almanac";
	return getPromise(url, 1000 * DEFAULT_TIMEOUT, 'POST', 200, options, false); // TODO Manage page format
};

let getLunar = function(options) {
	let url = "/astro/publish/lunar";
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, options, false); // TODO Manage page format
};

let publishPerpetual = function(options, callback) {
	let getData = getPerpetualDoc(options); // TODO Manage page format
	getData.then((value) => {
		// console.log("Done:", value);
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
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("Failed to get Astro Data..." + (error ? JSON.stringify(error) : ' - ') + ', ' + (message ? message : ' - '));
	});
};

let publishAlmanac = function(options, callback) {
	let getData = getAlmanac(options);
	getData.then((value) => {
		// console.log("Done:", value);
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
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("Failed publishing Almanac..." + (error ? JSON.stringify(error) : ' - ') + ', ' + (message ? message : ' - '));
	});

};

let publishLunar= function(options, callback) {

	console.log(`publishLunar, options: ${JSON.stringify(options)}`);

	let getData = getLunar(options);
	getData.then((value) => {
		// console.log("Done:", value);
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
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		errManager("Failed publishing Lunar..." + (error ? JSON.stringify(error) : ' - ') + ', ' + (message ? message : ' - '));
	});
};
