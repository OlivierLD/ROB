/*
 * Main Astro features.
 */

import * as CelestialComputer from './longterm.almanac.js';
import {
	// sightReduction,
	getGCDistance,
	getGCDistanceDegreesNM,
	calculateGreatCircle,
	getMoonTilt,
	calcLHA,
	ghaToLongitude,
	getHorizonDip,
	getRefraction
} from './utils.js';
// import * as CelestialComputer from './lib/celestial-computer.min.js';
// let CelestialComputer = require('./longterm.almanac.js');

import {
	decToSex,
	calculateGreatCircleInDegrees
} from './webcomponents/utilities/Utilities.js';

export function astroMain(userDataObject) {
	let year = userDataObject.utcyear;
	let	month = userDataObject.utcmonth;
	if (month < 1 || month > 12) {
		throw new Error("Month out of range! Restart calculation.");
	}
	let day = userDataObject.utcday;
	if (day < 1 || day > 31) {
		throw new Error("Day out of range! Restart calculation.");
	}
	let leap = CelestialComputer.isLeapYear(year);
	if (month === 2 && day > 28 && !leap) {
		throw new Error("February has only 28 days! Restart calculation.");
	}
	if (month === 2 && day > 29 && leap) {
		throw new Error("February has only 29 days in a leap year! Restart calculation.");
	}
	if (month === 4 && day > 30) {
		throw new Error("April has only 30 days! Restart calculation.");
	}
	if (month === 6 && day > 30) {
		throw new Error("June has only 30 days! Restart calculation.");
	}
	if (month === 9 && day > 30) {
		throw new Error("September has only 30 days! Restart calculation.");
	}
	if (month === 11 && day > 30) {
		throw new Error("November has only 30 days! Restart calculation.");
	}
	let hour = userDataObject.utchour;
	let minute = userDataObject.utcminute;
	let second = userDataObject.utcsecond;

	let delta_t = userDataObject.deltaT;

	delta_t = CelestialComputer.calculateDeltaT(year, month); // Recompute for current date (year and month). More accurate ;)
	// console.log("DeltaT is now %f", delta_t);

	let noPlanets = userDataObject.noPlanets || false;
	let calcResult = CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t, noPlanets); // Implicit stars and constellations

	try {
		let solarDate = CelestialComputer.getSolarDateAtPos(userPos.latitude, userPos.longitude, calcResult.EOT.raw, year, month, day, hour, minute, second, delta_t);
		calcResult.solarDate = solarDate; // Adding SolarDate at user's position
	} catch (err) {
		console.log("No user pos...");
	}
	return calcResult;
}

// To facilitate the syntax
window.astroMain = astroMain;
window.CelestialComputer = CelestialComputer;

window.gridSquare = CelestialComputer.gridSquare;
window.sightReduction = CelestialComputer.sightReduction; // The one in utils.js
window.getHorizonDip = getHorizonDip;
window.getRefraction = getRefraction;

window.getGCDistance = getGCDistance;
window.getGCDistanceDegreesNM = getGCDistanceDegreesNM;
window.calculateGreatCircle = calculateGreatCircle;
window.getMoonTilt = getMoonTilt;

window.calcLHA = calcLHA;
window.decToSex = decToSex;
window.calculateGreatCircleInDegrees = calculateGreatCircleInDegrees;

window.ghaToLongitude = ghaToLongitude;

// console.log("SRU Test:" + JSON.stringify(sightReduction(37.5,-122.3, 80, 22)));
