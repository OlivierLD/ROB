"use strict";

/*
 * Interactive test file for the Celestial Computer.
 *
 * To be used with node.js:
 * $ node interactive.js
 *
 */

import * as CelestialComputer from './longterm.almanac.js';
import {
    sightReduction,
    getGCDistance,
    getGCDistanceDegreesNM,
    calculateGreatCircle,
    getMoonTilt,
    parseDuration
} from './utils.js';

// import * as CelestialComputer from './lib/celestial-computer.min.js';
// let CelestialComputer = require('./longterm.almanac.js');

const DELTA_T = 69.2201; // Will be re-calculated

let decToSex = (val, ns_ew) => {
    let absVal = Math.abs(val);
    let intValue = Math.floor(absVal);
    let dec = absVal - intValue;
    let i = intValue;
    dec *= 60; //    let s = i + "°" + dec.toFixed(2) + "'";
    //    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";

    let s = ""; // sign

    if (ns_ew !== undefined) {
        if (val < 0) {
            s += ns_ew === 'NS' ? 'S' : 'W';
        } else {
            s += ns_ew === 'NS' ? 'N' : 'E';
        }
        s += " ";
    } else {
        if (val < 0) {
            s += '-';
        }
    }

    s += i + "°" + dec.toFixed(2) + "'";
    return s;
};

export function sampleMain(userDataObject) {
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
    return CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t, noPlanets);
}

console.log("+-----------------------------------+");
console.log("| An example of what can be done... |");
console.log("+-----------------------------------+");

console.log("CelestialComputer is loaded and ready to use.");

// Using default import
// import readline from 'readline';

// Using named imports
import { createInterface } from 'readline';

// Dynamic import (Node.js 14+)
// const { createInterface } = await import('readline');

// Create interface
const rl = createInterface({
  input: process.stdin,
  output: process.stdout
});
// console.log("Let's go!...");

rl.question(`Hit [return] when ready... `, name => {
  // console.log(`Hi ${name}!`);
  console.log("Let's go!...\n\n");

  const now = new Date();

  let today = { // Start date
    year: now.getUTCFullYear(),
    month: now.getUTCMonth() + 1, // [1..12]
    day: now.getUTCDate()
  };

  let calculationData = { // input data
    utcyear: today.year,
    utcmonth: today.month,
    utcday: today.day,
    utchour: now.getUTCHours(),
    utcminute: now.getUTCMinutes(),
    utcsecond: now.getUTCSeconds(),
    deltaT: DELTA_T, /// Recalculated
    noPlanets: false
  };

  let before = Date.now();
  let calcResult = sampleMain(calculationData); // in app.js
  let after = Date.now();

  if (false) {
    console.log("Calculation done %04d-%02d-%02d %02d:%02d:%02d UTC :", calculationData.utcyear, calculationData.utcmonth, calculationData.utcday, calculationData.utchour, calculationData.utcminute, calculationData.utcsecond);
    console.log("Result:\n", JSON.stringify(calcResult, null, 2));
  }

  console.log(`- Calculation done in ${after - before} ms.\n`);
  console.log("At %s-%s-%s %s:%s:%s UTC :",
              calculationData.utcyear.toString().padStart(4, '0'),
              calculationData.utcmonth.toString().padStart(2, '0'),
              calculationData.utcday.toString().padStart(2, '0'),
              calculationData.utchour.toString().padStart(2, '0'),
              calculationData.utcminute.toString().padStart(2, '0'),
              calculationData.utcsecond.toString().padStart(2, '0'));
  console.log("-------------------------------------------------");
  console.log(`- Equation of Time: ${calcResult.EOT.fmt}`);
  console.log("-------------------------------------------------");
  console.log(`- Sun GHA: ${calcResult.sun.GHA.fmt},\n` +
              `- Sun DEC: ${decToSex(calcResult.sun.DEC.raw, 'NS')},\n` +
              `- Sun HP: ${calcResult.sun.HP.fmt},\n` +
              `- Sun SD: ${calcResult.sun.SD.fmt}`);
  console.log("-------------------------------------------------");
  console.log(`- Moon GHA: ${calcResult.moon.GHA.fmt},\n` +
              `- Moon DEC: ${decToSex(calcResult.moon.DEC.raw, 'NS')},\n` +
              `- Moon HP: ${calcResult.moon.HP.fmt},\n` +
              `- Moon SD: ${calcResult.moon.SD.fmt}`);
  console.log("-------------------------------------------------");
  console.log(`- Jupiter GHA: ${calcResult.jupiter.GHA.fmt},\n` +
              `- Jupiter DEC: ${decToSex(calcResult.jupiter.DEC.raw, 'NS')},\n` +
              `- Jupiter HP: ${calcResult.jupiter.HP.fmt},\n` +
              `- Jupiter SD: ${calcResult.jupiter.SD.fmt}`);
  console.log("-------------------------------------------------");

  let starData = {};
  let arrayLen = calcResult.stars.length;

  // Looking for Zubenelgenubi
  let starName = "Zubenelgenubi"; // The one we look for...

  for (let i=0; i<arrayLen; i++) {
     if (calcResult.stars[i].name == starName) {
        starData = calcResult.stars[i];
        break;
     }
  }
  if (starData.name !== undefined) {
    console.log(`- ${starName} GHA: ${decToSex(starData.gha)}\n` +
                `- ${starName} DEC: ${decToSex(starData.decl, "NS")}`);
  } else {
    console.log(`Star ${starName} was not found...`);
  }
  console.log("-------------------------------------------------");

  rl.close();
  console.log("Done. Bye now!");
});