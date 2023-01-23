/*
 * @author Olivier Le Diouris
 */
"use strict";

// TODO Move to ES6

let displayBSP, displayLog, displayTWD, displayTWS, thermometer, athermometer, displayHDG, rose,
		displayBaro, displayHum, displayDate, displayTime, displayOverview,
		jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG,
		jumboCSP, jumboVMG,
		displayAW, displayCurrent, displayCurrent2,
		twdEvolution, twsEvolution,

		worldMap,

		currentDirEvolution, currentSpeedEvolution,
		currentDirEvolution30s, currentSpeedEvolution30s,
		currentDirEvolution1m, currentSpeedEvolution1m,
		currentDirEvolution10m, currentSpeedEvolution10m;

let jumboList = [];

let editing = false;

let init = function () {
//displayBSP = new AnalogDisplay('bspCanvas', 100, 15, 5, 1);
	displayBSP = new AnalogDisplay('bspCanvas', 100, 15, 5, 1, true, 40);
	displayBSP.setDigits(5);
	displayBSP.setWithMinMax(true);

	displayLog = new NumericDisplay('logCanvas', 60, 5);

	displayHDG = new Direction('hdgCanvas', 100, 45, 5, true);
	displayHDG.setLabel('HDG');
	displayTWD = new Direction('twdCanvas', 100, 45, 5, true);
	displayTWD.setLabel('TWD');
	displayTWS = new AnalogDisplay('twsCanvas', 100, 50, 10, 1, true, 40);
	displayTWS.setLabel('TWS');
	displayTWS.setWithMinMax(true);
	thermometer = new Thermometer('tmpCanvas', 200);
	athermometer = new Thermometer('atmpCanvas', 200);
	rose = new CompassRose('roseCanvas', 400, 50);
	displayDate = new DateDisplay('dateCanvas', 60);
	displayTime = new TimeDisplay('timeCanvas', 60);
	displayBaro = new AnalogDisplay('baroCanvas', 100, 1040, 10, 1, true, 40, 980);
	displayBaro.setLabel('PRMSL');
	displayHum = new AnalogDisplay('humCanvas', 100, 100, 10, 1, true, 40);
	displayHum.setLabel('HUM');

	displayOverview = new BoatOverview('overviewCanvas');
	worldMap = new WorldMap('mapCanvas', 'GLOBE');

	jumboBSP = new JumboDisplay('jumboBSPCanvas', 'BSP', 120, 60, "0.00");
	jumboHDG = new JumboDisplay('jumboHDGCanvas', 'HDG', 120, 60, "000");
	jumboTWD = new JumboDisplay('jumboTWDCanvas', 'TWD', 120, 60, "000", 'cyan');
	jumboLWY = new JumboDisplay('jumboLWYCanvas', 'LWY', 120, 60, "000", 'red');
	jumboAWA = new JumboDisplay('jumboAWACanvas', 'AWA', 120, 60, "000");
	jumboTWA = new JumboDisplay('jumboTWACanvas', 'TWA', 120, 60, "000", 'cyan');
	jumboAWS = new JumboDisplay('jumboAWSCanvas', 'AWS', 120, 60, "00.0");
	jumboTWS = new JumboDisplay('jumboTWSCanvas', 'TWS', 120, 60, "00.0", 'cyan');
	jumboCOG = new JumboDisplay('jumboCOGCanvas', 'COG', 120, 60, "000");
	jumboCDR = new JumboDisplay('jumboCDRCanvas', 'CDR', 120, 60, "000", 'cyan');
	jumboSOG = new JumboDisplay('jumboSOGCanvas', 'SOG', 120, 60, "0.00");
	jumboCSP = new JumboDisplay('jumboCSPCanvas', 'CSP', 120, 60, "00.0", 'cyan');
	jumboVMG = new JumboDisplay('jumboVMGCanvas', 'VMG', 120, 60, "0.00", 'yellow');

	jumboList = [jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG];

	displayAW = new AWDisplay('awDisplayCanvas', 80, 45, 5);
	displayAW.setLabel("AW");
	displayCurrent  = new CurrentDisplay('currentDisplayCanvas',  80, 45, 5);
	displayCurrent2 = new CurrentDisplay('currentDisplayCanvas2', 60, 45, 5);

	twdEvolution = new DirectionEvolution('twdEvolutionCanvas', "TWD");
	twsEvolution = new SpeedEvolution('twsEvolutionCanvas', 60, true, "TWS");

	twdEvolution.setMaxBuffLength(1200);
	twsEvolution.setMaxBuffLength(1200);

	currentDirEvolution = new DirectionEvolution('currentDirEvolutionCanvas');
	currentSpeedEvolution = new SpeedEvolution('currentSpeedEvolutionCanvas', 5, false, undefined, 1);

	currentDirEvolution30s = new DirectionEvolution('currentDirEvolutionCanvas30s');
	currentSpeedEvolution30s = new SpeedEvolution('currentSpeedEvolutionCanvas30s', 5, false, undefined, 1);
	currentDirEvolution1m = new DirectionEvolution('currentDirEvolutionCanvas1m');
	currentSpeedEvolution1m = new SpeedEvolution('currentSpeedEvolutionCanvas1m', 5, false, undefined, 1);
	currentDirEvolution10m = new DirectionEvolution('currentDirEvolutionCanvas10m');
	currentSpeedEvolution10m = new SpeedEvolution('currentSpeedEvolutionCanvas10m', 5, false, undefined, 1);

	currentDirEvolution.setMaxBuffLength(1200);
	currentDirEvolution30s.setMaxBuffLength(1200);
	currentDirEvolution1m.setMaxBuffLength(1200);
	currentDirEvolution10m.setMaxBuffLength(1200);
	currentSpeedEvolution.setMaxBuffLength(1200);
	currentSpeedEvolution30s.setMaxBuffLength(1200);
	currentSpeedEvolution1m.setMaxBuffLength(1200);
	currentSpeedEvolution10m.setMaxBuffLength(1200);
};

/**
 *
 * @param len can be undefined
 */
let updateTWBufferLength = function(txt) {
	let len;
	if (txt.value.length === 0) {
		len = undefined;
	}
	try {
		len = parseInt(txt.value);
	} catch (err) {
		len = undefined;
		console.log(err);
	}
	twdEvolution.setMaxBuffLength(len);
	twsEvolution.setMaxBuffLength(len);
};

/**
 *
 * @param len can be undefined
 */
let updateCurrentBufferLength = function(txt) {
	let len;
	if (txt.value.length === 0) {
		len = undefined;
	}
	try {
		len = parseInt(txt.value);
	} catch (err) {
		len = undefined;
		console.log(err);
	}
	currentDirEvolution.setMaxBuffLength(len);
	currentDirEvolution30s.setMaxBuffLength(len);
	currentDirEvolution1m.setMaxBuffLength(len);
	currentDirEvolution10m.setMaxBuffLength(len);
	currentSpeedEvolution.setMaxBuffLength(len);
	currentSpeedEvolution30s.setMaxBuffLength(len);
	currentSpeedEvolution1m.setMaxBuffLength(len);
	currentSpeedEvolution10m.setMaxBuffLength(len);
};

let changeBorder = function (b) {
	displayBSP.setBorder(b);
	displayHDG.setBorder(b);
	displayTWD.setBorder(b);
	displayTWS.setBorder(b);
	displayBaro.setBorder(b);
	displayBaro.repaint();
	displayHum.setBorder(b);
	displayHum.repaint();
	displayAW.setBorder(b);
	displayCurrent.setBorder(b);
	displayCurrent2.setBorder(b);
};

let cssSelectManager = function() {
	let x = document.getElementById("css-select").value;
	let cssName = x;
	changeTheme(cssName);
};

const TOTAL_WIDTH = 1200;

let resizeDisplays = function (width) {
	if (displayBSP !== undefined && displayTWS !== undefined) { // TODO Other displays
		displayBSP.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayTWS.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayHDG.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayTWD.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		thermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		athermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		rose.setDisplaySize(400 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayBaro.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayHum.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
		displayOverview.drawGraph();
		twdEvolution.drawGraph();
		twsEvolution.drawGraph();
		currentDirEvolution.drawGraph();
		currentSpeedEvolution.drawGraph();
		currentDirEvolution30s.drawGraph();
		currentSpeedEvolution30s.drawGraph();
		currentDirEvolution1m.drawGraph();
		currentSpeedEvolution1m.drawGraph();
		currentDirEvolution10m.drawGraph();
		currentSpeedEvolution10m.drawGraph();

		let jumboFactor = width / TOTAL_WIDTH;
		for (let i = 0; i < jumboList.length; i++) {
			if (jumboList[i] !== undefined)
				jumboList[i].setDisplaySize(120 * jumboFactor, 60 * jumboFactor);
		}
	}
};

let lpad = function (str, pad, len) {
	while (str.length < len)
		str = pad + str;
	return str;
};

var DEFAULT_TIMEOUT = 60000;

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

let getSunMoonGP = function(from, when) {
	let url = "/astro/positions-in-the-sky";
	// Add date
	url += ("?at=" + when);
	if (from !== undefined) {
		url += ("&fromL=" + from.latitude);
		url += ("&fromG=" + from.longitude);
	}
	// Wandering bodies
	url += ("&wandering=true");
	// return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getAstroData = function(from, when, callback) {
	let getData = getSunMoonGP(from, when);
	getData.then((value) => {
		// console.log("Done:", value);
		let json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json);
		} else {
			console.log(JSON.stringify(json, null, 2));
		}
	}, (error, errmess) => {
		let message;
		if (errmess) {
			let mess = JSON.parse(errmess);
			if (mess.message) {
				message = mess.message;
			}
		}
		displayErr("Failed to get Astro Data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
	});
};
