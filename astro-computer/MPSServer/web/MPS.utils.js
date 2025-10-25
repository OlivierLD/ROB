/*
 * Main MPS features.
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
	getRefraction,
	haversineInv,
	haversineNm
} from './utils.js';
// import * as CelestialComputer from './lib/celestial-computer.min.js';
// let CelestialComputer = require('./longterm.almanac.js');

import {
	decToSex,
	calculateGreatCircleInDegrees
} from './webcomponents/utilities/Utilities.js';

class MPSToolBox {

	static rhoE = 635677; // Earth radius, in 100s of km. It's 6356.77 km.
    static earthRadiusNM = (rhoE / 100) / 1.852; // Earth radius, in nm.
	static CRITICAL_DIST = 5.0;

	constructor() {
		console.log("New.Target:", new.target);
		console.log(`Target/constructor: new.target === MPSToolBox : ${(new.target === MPSToolBox)}, new.target === this : ${(new.target === this)}`);
	};

	static welcome() {
		// console.log("Interesting...");
		console.log(`${MPSToolBox.name} is now loaded.`);
	};

	test() { // No need to declare it as 'function'
		// console.log("Height is " ,this. _height);
		// console.log("Width is " , this._width);
		console.log("Akeu Coucou!");
	};

	static parseDuration(duration) {
		// "2023-03-19T08:42:33"
		//  |    |  |. |  |. |
		//  |    |  |  |  |  17
		//  |    |  |  |  14
		//  |    |  |  11
		//  |    |  8
		//  |    5
		//  0
		let year = parseInt(duration.substring(0, 4));
		let month = parseInt(duration.substring(5, 7));
		let day = parseInt(duration.substring(8, 10));
		let hours = parseInt(duration.substring(11, 13));
		let minutes = parseInt(duration.substring(14, 16));
		let seconds = parseInt(duration.substring(17));

		return { year: year, month: month, day: day, hours: hours, minutes: minutes, seconds: seconds };
	};

	static getBodyList() {
		let bodies = [ "Sun", "Moon", "Venus", "Mars", "Jupiter", "Saturn" ];
		let starCatalog = CelestialComputer.getStars();
		starCatalog.forEach(star => {
			bodies.push(star.name);
		});
		return bodies;
	};

	static getAltNZ(payload) {
		let userLat = payload.pos.latitude;
		let userLng = payload.pos.longitude;
		let bodyGha = payload.pg.gha;
		let bodyD = payload.pg.d;
		let sru = CelestialComputer.sightReduction(userLat, userLng, bodyGha, bodyD);
		return { alt: sru.alt, z: sru.Z };
	}

	static getBodyData(body, utcdate) {
		let duration = MPSToolBox.parseDuration(utcdate);
		let year = duration.year;
		let month = duration.month;
		let day = duration.day;
		let hour = duration.hours;
		let minute = duration.minutes;
		let second = duration.seconds;

		let delta_t = CelestialComputer.calculateDeltaT(year, month); // Recompute for current date (year and month). More accurate ;)
		let noPlanets = false;
		let calcResult = CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t, noPlanets); // Implicit stars and constellations

		/*
		{
			"hp": 0.0024522676044500826,
			"sd": 0.26759944976784544,
			"gha": 232.50245191671962,
			"d": -10.040683405388103
		}
		 */
		let bodyData = {};
		switch (body) {
			case "Sun":
				bodyData.gha = calcResult.sun.GHA.raw;
				bodyData.d = calcResult.sun.DEC.raw;
				bodyData.hp = calcResult.sun.HP.raw / 3600;
				bodyData.sd = calcResult.sun.SD.raw / 3600;
				break;
			case "Moon":
				bodyData.gha = calcResult.moon.GHA.raw;
				bodyData.d = calcResult.moon.DEC.raw;
				bodyData.hp = calcResult.moon.HP.raw / 3600;
				bodyData.sd = calcResult.moon.SD.raw / 3600;
				break;
			case "Venus":
				bodyData.gha = calcResult.venus.GHA.raw;
				bodyData.d = calcResult.venus.DEC.raw;
				bodyData.hp = calcResult.venus.HP.raw / 3600;
				bodyData.sd = calcResult.venus.SD.raw / 3600;
				break;
			case "Mars":
				bodyData.gha = calcResult.mars.GHA.raw;
				bodyData.d = calcResult.mars.DEC.raw;
				bodyData.hp = calcResult.mars.HP.raw / 3600;
				bodyData.sd = calcResult.mars.SD.raw / 3600;
				break;
			case "Jupiter":
				bodyData.gha = calcResult.jupiter.GHA.raw;
				bodyData.d = calcResult.jupiter.DEC.raw;
				bodyData.hp = calcResult.jupiter.HP.raw / 3600;
				bodyData.sd = calcResult.jupiter.SD.raw / 3600;
				break;
			case "Saturn":
				bodyData.gha = calcResult.saturn.GHA.raw;
				bodyData.d = calcResult.saturn.DEC.raw;
				bodyData.hp = calcResult.saturn.HP.raw / 3600;
				bodyData.sd = calcResult.saturn.SD.raw / 3600;
				break;
			default: // Stars
				let theStar = calcResult.stars.filter(star => star.name == body);
				bodyData.gha = theStar[0].gha;
				bodyData.d = theStar[0].decl;
				bodyData.hp = 0;
				bodyData.sd = 0;
				break;
		}
		return bodyData;
	}

	static calculateDR(gha, dec, lat, lng) {
		let AHL = gha + lng;
		if (AHL < 0.0) {
			AHL = 360 + AHL;
		}
		let sinL = Math.sin(Math.toRadians(lat));
		let sinD = Math.sin(Math.toRadians(dec));
		let cosL = Math.cos(Math.toRadians(lat));
		let cosD = Math.cos(Math.toRadians(dec));
		let cosAHL = Math.cos(Math.toRadians(AHL));
		let sinHe = sinL * sinD + cosL * cosD * cosAHL;
		let He = Math.toDegrees(Math.asin(sinHe));
		let P = AHL >= 180 ? 360 - AHL : AHL;
		let sinP = Math.sin(Math.toRadians(P));
		let cosP = Math.cos(Math.toRadians(P));
		let tanD = Math.tan(Math.toRadians(dec));
		let tanZ = sinP / (cosL * tanD - sinL * cosP);
		let Z = Math.toDegrees(Math.atan(tanZ));
		if (AHL < 180) {
			if (Z < 0.0) {
				Z = 180 - Z;
			} else {
				Z = 360 - Z;
			}
		} else if (Z < 0.0) {
			Z = 180 + Z;
	//  } else {
	//    Z = Z;
		}
		return { dHe: He, dZ: Z };
	}

	static intersectionDelegation(coneBody1, coneBody2, loop, zStep, verbose) {
        let result = [];

        let smallest = Number.MAX_VALUE;
        let closestPointBody1 = null;
        let closestPointBody2 = null;
        let closestPointZBody1 = null;
        let closestPointZBody2 = null;

        let smallestSecond = Number.MAX_VALUE;
        let closestPointBody1Second = null;
        let closestPointBody2Second = null;
        let closestPointZBody1Second = null;
        let closestPointZBody2Second = null;

        const DIST_MIN = 30.0; // Was 3.0 TODO Fix that 3... Relate to first firstZStep ?

        coneBody1.circle.forEach(conePointBody1 => {
			coneBody2.circle.forEach(conePointBody2 => {
                let dist = haversineNm(conePointBody1.point, conePointBody2.point);
                // For some tests..., to find the 2 intersections
                if (loop == 0 && dist < DIST_MIN) {
                    if (verbose) {
                        console.log("Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                dist, zStep / 10,
                                conePointBody1.point,
                                conePointBody1.z,
                                conePointBody2.point,
                                conePointBody2.z);
                    }
                    // For loop 0, we'll need 2 smallest dist, identified by their Z
                    if (closestPointZBody1 != null && closestPointZBody2 != null) {
                        if (verbose) {
                            console.log("DeltaZ_1 %.04f, DeltaZ_2 %.04f, compare to %.04f\n",
                                    Math.abs(conePointBody1.z - closestPointZBody1),
                                    Math.abs(conePointBody2.z - closestPointZBody2),
                                    (5 * zStep));
                        }
                        if (Math.abs(conePointBody1.z - closestPointZBody1) > (5 * zStep) &&
                                Math.abs(conePointBody2.z - closestPointZBody2) > (5 * zStep)) {
                            if (dist < smallestSecond) {
                                smallestSecond = dist;
                                closestPointBody1Second = conePointBody1.point;
                                closestPointBody2Second = conePointBody2.point;
                                closestPointZBody1Second = conePointBody1.z;
                                closestPointZBody2Second = conePointBody2.z;
                                if (verbose) {
                                    console.log("2nd Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                            dist, zStep / 10,
                                            conePointBody1.point,
                                            conePointBody1.z,
                                            conePointBody2.point,
                                            conePointBody2.z);
                                    console.log("-- (1st : between %s (Z=%.02f) and %s (Z=%.02f))\n",
                                            closestPointBody1.toString(),
                                            closestPointZBody1,
                                            closestPointBody2.toString(),
                                            closestPointZBody2);
                                }
                            }
                        }
                    }
                }
                if ((loop != 0) || (loop == 0 && closestPointZBody1Second == null && closestPointZBody2Second == null)) {
                    if (dist < smallest) {
                        smallest = dist;
                        closestPointBody1 = conePointBody1.point;
                        closestPointBody2 = conePointBody2.point;
                        closestPointZBody1 = conePointBody1.z;
                        closestPointZBody2 = conePointBody2.z;
                        if (verbose && dist < DIST_MIN && loop == 0) {
                            console.log("1st Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                    dist, zStep / 10,
                                    conePointBody1.point,
                                    conePointBody1.z,
                                    conePointBody2.point,
                                    conePointBody2.z);
                        }
                    }
                }

			});
		});
        // End of loop #n
        if (verbose) {
            console.log("Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                    loop + 1,
                    smallest,
                    closestPointZBody1,
                    closestPointBody1.toString(),
                    closestPointZBody2,
                    closestPointBody2.toString());
            if (loop == 0) {
                console.log("=> 2nd Intersection: Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                        loop + 1,
                        smallestSecond,
                        closestPointZBody1Second,
                        closestPointBody1Second.toString(),
                        closestPointZBody2Second,
                        closestPointBody2Second.toString());
            }
        }

        if (closestPointBody1 != null && closestPointBody2 != null) {
            // result = new ArrayList<>();
            result.push({ point: closestPointBody1, z: closestPointZBody1 });
            result.push({ point: closestPointBody2, z: closestPointZBody2 });

            if (loop == 0 && closestPointBody1Second != null && closestPointBody2Second != null) {
                result.push({ point: closestPointBody1Second, z: closestPointZBody1Second });
                result.push({ point: closestPointBody2Second, z: closestPointZBody2Second });
            }
        }

        return result;
	}

	static calculateCone(obsAlt, gha, d, bodyName, fromZ, toZ, zStep, verbose) {
        let distInNM = (90.0 - obsAlt) * 60.0;

        // Find MS, distance from observer to summit.
        let MS = earthRadiusNM * (1 / Math.tan(Math.toRadians(obsAlt)));
        if (verbose) {
            console.log("MS (obs to summit), in nautical miles: %.02f'\n", MS);
        }
        let coneDiameter = earthRadiusNM * Math.cos(Math.toRadians(obsAlt));
        if (verbose) {
            console.log("Cone radius, in nautical miles: %.02f'\n", coneDiameter);
        }

        let earthCenterToConeSummit = Math.sqrt((MS * MS) + (earthRadiusNM * earthRadiusNM));

        // Find all the points seeing the body at the same altitude
        if (verbose) {
            console.log("---- The Circle, Cone base ----");
        }

        let cd = {}; // new ConeDefinition();
        cd.circle = [];
        cd.bodyName = bodyName;
        cd.obsAlt = obsAlt;
        cd.pg = { latitude: d, longitude: ghaToLongitude(gha) };
        cd.earthCenterToConeSummit = earthCenterToConeSummit;
        cd.observationTime = "- dummy -"; // SDF_UTC.format(calculationTime);

        for (let z=fromZ; (zStep > 0 && z < toZ) || (zStep < 0 && z > toZ); z += zStep) { // The steps and interval here !
            let hdg = z;
            // console.log("Calculating cone point for Z=%.4f (zStep = %.4f)\n", hdg, zStep);
            let bodyPos = { latitude: d, longitude: ghaToLongitude(gha) };
            let drGC = haversineInv(bodyPos, distInNM, hdg); // THE dr to use

            // altitude tests, reverse
            if (verbose) {
                // for 20-AUG-2025 10:40:31, GHA: 339°17.40', D: N 12°16.80', Obs Alt: 49°22.51'
                let cdr = MPSToolBox.calculateDR(gha, d, drGC.latitude, drGC.longitude);
                let he = cdr.dHe;
                console.log("GHA: %s, D: %s \n", decToSex(gha), decToSex(d, "NS"));
                console.log("For obsAlt=%f (%s), he (from circle)=%f (%s)\n", obsAlt, decToSex(obsAlt), he, decToSex(he));
            }
            cd.circle.push( { point: drGC, z: hdg } ); // new ConePoint(drGC, hdg));
        }
        return cd;
	}

	static resolve2Cones(dateOne, altOne, ghaOne, declOne,
                         dateTwo, altTwo, ghaTwo, declTwo,
                         firstZStep, nbLoops, reverse, verbose) {
        let result = [];

        // double smallest = Double.MAX_VALUE;
        let /*GeoPoint*/ closestPointBody1 = null;
        let /*GeoPoint*/ closestPointBody2 = null;
        let /*Double*/ closestPointZBody1 = null;
        let /*Double*/ closestPointZBody2 = null;

        // double smallestSecond = Double.MAX_VALUE;
        let /*GeoPoint*/ closestPointBody1Second = null;
        let /*GeoPoint*/ closestPointBody2Second = null;
        let /*Double*/ closestPointZBody1Second = null;
        let /*Double*/ closestPointZBody2Second = null;

        let fromZ = 0;
        let toZ = 360;
        let zStep = firstZStep * 10; // because divided by 10, even when starting the first loop.

        if (reverse) {
            fromZ = 360;
            toZ = 0;
            zStep *= -1;
        }

        for (let loop=0; loop<nbLoops; loop++) {

            let coneBody1 = MPSToolBox.calculateCone(/*dateOne, */ altOne, ghaOne, declOne, "Body 1",
                    closestPointZBody1 == null ? fromZ : closestPointZBody1 - zStep,
                    closestPointZBody1 == null ? toZ : closestPointZBody1 + zStep,
                    zStep / 10, verbose);
            let coneBody2 = MPSToolBox.calculateCone(/*dateTwo, */ altTwo, ghaTwo, declTwo, "Body 2",
                    closestPointZBody2 == null ? fromZ : closestPointZBody2 - zStep,
                    closestPointZBody2 == null ? toZ : closestPointZBody2 + zStep,
                    zStep / 10, verbose);

            // Now, find the intersection of the two cones...
            let geoPointsFirst = MPSToolBox.intersectionDelegation(coneBody1, coneBody2, loop, zStep / 10, verbose);
            closestPointBody1 = geoPointsFirst[0].point;
            closestPointZBody1 = geoPointsFirst[0].z;
            closestPointBody2 = geoPointsFirst[1].point;
            closestPointZBody2 = geoPointsFirst[1].z;

            if (loop == 0) { // Populate second ones
                if (geoPointsFirst.length == 4) {
                    closestPointBody1Second = geoPointsFirst[2].point;
                    closestPointZBody1Second = geoPointsFirst[2].z;
                    closestPointBody2Second = geoPointsFirst[3].point;
                    closestPointZBody2Second = geoPointsFirst[3].z;
                } else {
                    console.error("Ooops !!! Second intersection was not found ! Only %d point(s) available.\n", geoPointsFirst.length);
                    throw new Exception(`Ooops !!! Second intersection was not found ! Only ${geoPointsFirst.length} point(s) available.`);
                }
            }

            // 2nd intersection ?
            if (loop > 0) { // Deal with 2nd intersection
                if (verbose) {
                    console.log("Dealing with second Intersection...");
                }
                let coneBody1Second = MPSToolBox.calculateCone(/*firstTime,*/ altOne, ghaOne, declOne, "Body 1",
                        closestPointZBody1Second == null ? fromZ : closestPointZBody1Second - zStep,
                        closestPointZBody1Second == null ? toZ : closestPointZBody1Second + zStep,
                        zStep / 10, verbose);
                let coneBody2Second = MPSToolBox.calculateCone(/*secondTime,*/ altTwo, ghaTwo, declTwo, "Body 2",
                        closestPointZBody2Second == null ? fromZ : closestPointZBody2Second - zStep,
                        closestPointZBody2Second == null ? toZ : closestPointZBody2Second + zStep,
                        zStep / 10, verbose);
                let /*List<ConePoint>*/ geoPointsSecond = MPSToolBox.intersectionDelegation(coneBody1Second, coneBody2Second, loop, zStep / 10, verbose);
                closestPointBody1Second = geoPointsSecond[0].point;
                closestPointZBody1Second = geoPointsSecond[0].z;
                closestPointBody2Second = geoPointsSecond[1].point;
                closestPointZBody2Second = geoPointsSecond[1].z;
            }
            zStep /= 10.0; // For the next loop
        }

        result = []; // new ArrayList<>();
        result.push(closestPointBody1);
        result.push(closestPointBody2);
        result.push(closestPointBody1Second);
        result.push(closestPointBody2Second);

        return result;
	}

/*
  conesIntersectionList is like:
	[
		{
			"bodyOneName": "Sun",
			"bodyTwoName": "Mars",
			"coneOneIntersectionOne": {
				"latitude": 47.6778576795039,
				"longitude": -3.136156308084257
			},
			"coneOneIntersectionTwo": {
				"latitude": 47.67785454351779,
				"longitude": -3.136101675325957
			},
			"coneTwoIntersectionOne": {
				"latitude": -54.33358750551935,
				"longitude": -69.976136930494
			},
			"coneTwoIntersectionTwo": {
				"latitude": -54.333559732393326,
				"longitude": -69.97616847657926
			}
		},
		{
			"bodyOneName": "Sun",
			"bodyTwoName": "Aldebaran",
			"coneOneIntersectionOne": {
				"latitude": 47.67781254977794,
				"longitude": -3.135907656229469
			},
			"coneOneIntersectionTwo": {
				"latitude": 47.6778162434296,
				"longitude": -3.1359545808846647
			},
			"coneTwoIntersectionOne": {
				"latitude": -67.03115515350157,
				"longitude": -50.056840765414535
			},
			"coneTwoIntersectionTwo": {
				"latitude": -67.03111602270741,
				"longitude": -50.05685098588281
			}
		},
		{
			"bodyOneName": "Mars",
			"bodyTwoName": "Sun",
			"coneOneIntersectionOne": {
				"latitude": -54.333559732393326,
				"longitude": -69.97616847657926
			},
			"coneOneIntersectionTwo": {
				"latitude": -54.33358750551935,
				"longitude": -69.976136930494
			},
			"coneTwoIntersectionOne": {
				"latitude": 47.67785454351779,
				"longitude": -3.136101675325957
			},
			"coneTwoIntersectionTwo": {
				"latitude": 47.6778576795039,
				"longitude": -3.136156308084257
			}
		},
		{
			"bodyOneName": "Mars",
			"bodyTwoName": "Aldebaran",
			"coneOneIntersectionOne": {
				"latitude": -83.98892352963195,
				"longitude": -18.88258553903621
			},
			"coneOneIntersectionTwo": {
				"latitude": -83.988952913968,
				"longitude": -18.882437480118863
			},
			"coneTwoIntersectionOne": {
				"latitude": 47.67787976550857,
				"longitude": -3.135832343932849
			},
			"coneTwoIntersectionTwo": {
				"latitude": 47.67786232973659,
				"longitude": -3.1358337830104213
			}
		},
		{
			"bodyOneName": "Aldebaran",
			"bodyTwoName": "Sun",
			"coneOneIntersectionOne": {
				"latitude": 47.6778162434296,
				"longitude": -3.1359545808846647
			},
			"coneOneIntersectionTwo": {
				"latitude": 47.67781254977794,
				"longitude": -3.135907656229469
			},
			"coneTwoIntersectionOne": {
				"latitude": -67.03111602270741,
				"longitude": -50.05685098588281
			},
			"coneTwoIntersectionTwo": {
				"latitude": -67.03115515350157,
				"longitude": -50.056840765414535
			}
		},
		{
			"bodyOneName": "Aldebaran",
			"bodyTwoName": "Mars",
			"coneOneIntersectionOne": {
				"latitude": 47.67786232973659,
				"longitude": -3.1358337830104213
			},
			"coneOneIntersectionTwo": {
				"latitude": 47.67787976550857,
				"longitude": -3.135832343932849
			},
			"coneTwoIntersectionOne": {
				"latitude": -83.988952913968,
				"longitude": -18.882437480118863
			},
			"coneTwoIntersectionTwo": {
				"latitude": -83.98892352963195,
				"longitude": -18.88258553903621
			}
		}
	]
*/
	static processIntersectionsList(conesIntersectionList, verbose) {
        if (verbose) {
            console.log("We have %d intersections to process.\n", conesIntersectionList.length);
        }
        if (conesIntersectionList.length > 1) {
            if (verbose) {
                conesIntersectionList.forEach(ci => {
					console.log("- Intersection between %s and %s\n", ci.bodyOneName, ci.bodyTwoName);
				});
            }
            let candidates = []; // new ArrayList<>();

            // Iterate on the reference as well...
            for (let ref = 0; ref < conesIntersectionList.length; ref++) {

                if (verbose) {
                    console.log("-- Ref: %d\n", ref);
                }
                let referenceIntersection = conesIntersectionList[ref];

                /*
                   Need to manage:
                        cone 1 - intersection 1 with cone 1 - intersection 1
                        cone 1 - intersection 1 with cone 1 - intersection 2
                        cone 1 - intersection 2 with cone 1 - intersection 1
                        cone 1 - intersection 2 with cone 1 - intersection 2

                        cone 2 - intersection 1 with cone 1 - intersection 1
                        cone 2 - intersection 1 with cone 1 - intersection 2
                        cone 2 - intersection 2 with cone 1 - intersection 1
                        cone 2 - intersection 2 with cone 1 - intersection 2

                        cone 1 - intersection 1 with cone 2 - intersection 1
                        cone 1 - intersection 1 with cone 2 - intersection 2
                        cone 1 - intersection 2 with cone 2 - intersection 1
                        cone 1 - intersection 2 with cone 2 - intersection 2

                        cone 2 - intersection 1 with cone 2 - intersection 1
                        cone 2 - intersection 1 with cone 2 - intersection 2
                        cone 2 - intersection 2 with cone 2 - intersection 1
                        cone 2 - intersection 2 with cone 2 - intersection 2

                        Good luck.
                 */

                for (let i = 0; i < conesIntersectionList.length; i++) {
                    if (i != ref) {
                        if (verbose) {
                            console.log("---- i: %d\n", i);
                        }
                        let thatOne = conesIntersectionList[i];

                        // Cartesian product !

                        // Cone 1 - Cone 1
                        let oneOneDistOneOne = haversineNm(referenceIntersection.coneOneIntersectionOne, thatOne.coneOneIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionOne, thatOne.coneOneIntersectionOne, oneOneDistOneOne);
                        }
                        if (oneOneDistOneOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionOne);
                        }
                        let oneOneDistOneTwo = haversineNm(referenceIntersection.coneOneIntersectionOne, thatOne.coneOneIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionOne, thatOne.coneOneIntersectionTwo, oneOneDistOneTwo);
                        }
                        if (oneOneDistOneTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionTwo);
                        }
                        let oneOneDistTwoOne = haversineNm(referenceIntersection.coneOneIntersectionTwo, thatOne.coneOneIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionTwo, thatOne.coneOneIntersectionOne, oneOneDistTwoOne);
                        }
                        if (oneOneDistTwoOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionOne);
                        }
                        let oneOneDistTwoTwo = haversineNm(referenceIntersection.coneOneIntersectionTwo, thatOne.coneOneIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionTwo, thatOne.coneOneIntersectionTwo, oneOneDistTwoTwo);
                        }
                        if (oneOneDistTwoTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionTwo);
                        }

                        // Cone 1 - Cone 2
                        let oneTwoDistOneOne = haversineNm(referenceIntersection.coneOneIntersectionOne, thatOne.coneTwoIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionOne, thatOne.coneTwoIntersectionOne, oneTwoDistOneOne);
                        }
                        if (oneTwoDistOneOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionOne);
                        }
                        let oneTwoDistOneTwo = haversineNm(referenceIntersection.coneOneIntersectionOne, thatOne.coneTwoIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionOne, thatOne.coneTwoIntersectionTwo, oneTwoDistOneTwo);
                        }
                        if (oneTwoDistOneTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionTwo);
                        }
                        let oneTwoDistTwoOne = haversineNm(referenceIntersection.coneOneIntersectionTwo, thatOne.coneTwoIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionTwo, thatOne.coneTwoIntersectionOne, oneTwoDistTwoOne);
                        }
                        if (oneTwoDistTwoOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionOne);
                        }
                        let oneTwoDistTwoTwo = haversineNm(referenceIntersection.coneOneIntersectionTwo, thatOne.coneTwoIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionTwo, thatOne.coneTwoIntersectionTwo, oneTwoDistTwoTwo);
                        }
                        if (oneTwoDistTwoTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionTwo);
                        }

                        // Cone 2 - Cone 1
                        let twoOneDistOneOne = haversineNm(referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionOne, twoOneDistOneOne);
                        }
                        if (twoOneDistOneOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionOne);
                        }
                        let twoOneDistOneTwo = haversineNm(referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionTwo, twoOneDistOneTwo);
                        }
                        if (twoOneDistOneTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionTwo);
                        }
                        let twoOneDistTwoOne = haversineNm(referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionOne, thatOne.coneOneIntersectionOne, oneOneDistTwoOne);
                        }
                        if (twoOneDistTwoOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionOne);
                        }
                        let twoOneDistTwoTwo = haversineNm(referenceIntersection.coneTwoIntersectionTwo, thatOne.coneOneIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionTwo, thatOne.coneOneIntersectionTwo, twoOneDistTwoTwo);
                        }
                        if (twoOneDistTwoTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneOneIntersectionTwo);
                        }

                        // Cone 2 - Cone 2
                        let twoTwoDistOneOne = haversineNm(referenceIntersection.coneTwoIntersectionOne, thatOne.coneTwoIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionOne, thatOne.coneTwoIntersectionOne, twoTwoDistOneOne);
                        }
                        if (twoTwoDistOneOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionOne);
                        }
                        let twoTwoDistOneTwo = haversineNm(referenceIntersection.coneTwoIntersectionOne, thatOne.coneTwoIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionOne, thatOne.coneTwoIntersectionTwo, twoTwoDistOneTwo);
                        }
                        if (twoTwoDistOneTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionTwo);
                        }
                        let twoTwoDistTwoOne = haversineNm(referenceIntersection.coneTwoIntersectionTwo, thatOne.coneTwoIntersectionOne);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneTwoIntersectionTwo, thatOne.coneTwoIntersectionOne, twoTwoDistTwoOne);
                        }
                        if (twoTwoDistTwoOne < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionOne);
                        }
                        let twoTwoDistTwoTwo = haversineNm(referenceIntersection.coneTwoIntersectionTwo, thatOne.coneTwoIntersectionTwo);
                        if (verbose) {
                            console.log("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.coneOneIntersectionTwo, thatOne.coneTwoIntersectionTwo, twoTwoDistTwoTwo);
                        }
                        if (twoTwoDistTwoTwo < MPSToolBox.CRITICAL_DIST) {
                            candidates.push(thatOne.coneTwoIntersectionTwo);
                        }
                    }
                }
            }
            // The result...
            if (verbose) {
                console.log("-----------------------------");
                console.log("%d candidate(s):\n", candidates.length);
                candidates.forEach(pt => {
                    console.log("\u2022 %s\n", pt);
                });
                console.log("-----------------------------");
            }
            // Create lists of points, put together by their distance to each other
            let pointMap = new Map();
            let ref = null;
            candidates.forEach(pt => {
                if (ref != null) {
                    // Calculate distance with ref
                    let nm = haversineNm(ref, pt);
                    if (nm < MPSToolBox.CRITICAL_DIST) {
                        pointMap.get(ref).push(pt); // Gonfled
                    } else {
                        // See distance with other lists (ref)
                        if (verbose) {
                            console.log("%s too far from %s (%f)\n", pt, ref, nm);
                        }
						let keys = pointMap.keys();
                        let found = false;
                        // for (int k=0; k<keys.length; k++) {
						for (let i=0; i<keys.length; i++) {
							let key = keys[i];
							let dist = haversineNm(key, pt);
                            if (dist < MPSToolBox.CRITICAL_DIST) {
                                ref = key;
                                pointMap.get(key).push(pt);
                                found = true;
                                break;
                            }
						}
                        if (!found) {
                            pointMap.set(pt, [ pt ]);
                            ref = pt;
                        }
                    }
                } else {
                    ref = pt;
                    pointMap.set(pt, [ pt ]);
                }
            });
            if (verbose) {
                console.log("PointMap has %d entries\n", pointMap.set().length);
            }
            // We will do the average of the biggest list
            let restricted = [];
            let maxCard = -1;
            pointMap.keys().forEach(key => {
				if (key) { // Mmh ?
					let list = pointMap.get(key);
					if (!list) {
						console.log("WTF?");
					}
					if (list && list.length > maxCard) {
						maxCard = list.length;
						restricted = list;
					}
				}
			});

            // An average ?
            candidates = restricted;
            if (verbose) {
                console.log("Working on %d positions\n", candidates.length);
            }

			let latitudeSum = 0.0;
            let longitudeSum = 0.0;
            candidates.forEach(pt => {
            	latitudeSum += pt.latitude;
            	longitudeSum += pt.longitude;
			});

			let averageLat = latitudeSum / candidates.length;
			let averageLng = longitudeSum / candidates.length;

			let avgPoint = { latitude: averageLat, longitude: averageLng };

            if (verbose) {
                console.log("=> Average: %s\n", JSON.stringify(avgPoint, null, 2));
            }
			// return { latitude: 47, longitude: -3 };
            return avgPoint;
        } else {
            throw new NotEnoughIntersectionsException(String.format("Not enough intersections to process. Need at least 2, got %d", conesIntersectionList.size()));
        }
	}

	static computeConesIntersection(originalListBodyData, verbose) { // Returns a GeoPoint

        // Compute GHA and Decl
        let listBodyData = [];
        originalListBodyData.forEach(bd => {
            try {
				let oneBD = MPSToolBox.getBodyData(bd.bodyName, bd.date);
				let bodyData = {
					bodyName: bd.bodyName,
					date: bd.date,
					gha: oneBD.gha,
					decl: oneBD.d,
					obsAlt: bd.obsAlt
				};
                listBodyData.push(bodyData);
            } catch (ex) {
                throw ex;
            }
        });

        let conesIntersectionList = [];
        /// Cones and Co here. All permutations.
        let nbProcess = 0;
        for (let i=0; i<listBodyData.length; i++) {
            for (let j=0; j<listBodyData.length; j++) {
                if (i != j) {
                    if (verbose) {
                        console.log("[%d, %d], %s and %s\n", i, j, listBodyData[i].bodyName, listBodyData[j].bodyName);
                    }
                    let bodyOne = listBodyData[i].bodyName;
                    let altOne = listBodyData[i].obsAlt; // saturnObsAlt;
                    let ghaOne = listBodyData[i].gha;    // saturnGHA;
                    let declOne = listBodyData[i].decl;  // saturnDecl;
                    let dateOne = MPSToolBox.parseDuration(listBodyData[i].date); // date.getTime();

                    let bodyTwo = listBodyData[j].bodyName;
                    let altTwo = listBodyData[j].obsAlt; // jupiterObsAlt;
                    let ghaTwo = listBodyData[j].gha;    // jupiterGHA;
                    let declTwo = listBodyData[j].decl;  // jupiterDecl;
                    let dateTwo = MPSToolBox.parseDuration(listBodyData[j].date); // date.getTime();

                    let nbIter = 4;        // Hard-coded for now.
                    let reverse = false;   // Hard-coded for now.

                    if (verbose) {
                        console.log("------------------------------------------------");
                        console.log("Starting resolve process with:\n" +
                                        "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                                        "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                                JSON.stringify(dateOne, null, 2),
                                decToSex(altOne),
                                decToSex(ghaOne),
                                decToSex(declOne, "NS"),
                                JSON.stringify(dateTwo, null, 2),
                                decToSex(altTwo),
                                decToSex(ghaTwo),
                                decToSex(declTwo, "NS"));
                        console.log("------------------------------------------------");
                    }
                    // Ephemeris and Altitudes OK, let's proceed.
                    let firstZStep = 0.1;  // More than 0.1 not good enough...

                    // Now, find the intersection(s) of the two cones...
                    let closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                                                            dateTwo, altTwo, ghaTwo, declTwo,
                                                            firstZStep, nbIter, reverse, verbose);

                    if (/*closests != null ||*/ closests.length >= 4) {
                        let d1 = haversineNm(closests[0], closests[1]);
                        let d2 = haversineNm(closests[2], closests[3]);
                        if (verbose) {
                            console.log("%d - %s & %s\n", ++nbProcess, bodyOne, bodyTwo);
                            console.log("After %d iterations:\n", nbIter);
                            console.log("1st position between %s (%s) and %s (%s), dist %.02f nm.\n", closests[0], closests[0].toString(), closests[1], closests[1].toString(), d1);
                            console.log("2nd position between %s (%s) and %s (%s), dist %.02f nm.\n", closests[2], closests[2].toString(), closests[2], closests[2].toString(), d2);
                        }
                        // For later
                        conesIntersectionList.push({ bodyOneName: bodyOne,
							                         bodyTwoName: bodyTwo,
                                                     coneOneIntersectionOne: closests[0],
													 coneOneIntersectionTwo: closests[1],
                                                     coneTwoIntersectionOne: closests[2],
													 coneTwoIntersectionTwo: closests[3] });
                    } else {
                        console.log("Oops ! Not found...");
                    }
                }
            }
        }
        if (verbose) {
            console.log("End of permutations, %d intersections\n", conesIntersectionList.length);
        }

        // Now process all intersections...
        if (verbose) {
            console.log("-----------------------------");
        }

        try {
            let /*GeoPoint*/ avgPoint = MPSToolBox.processIntersectionsList(conesIntersectionList, verbose);
            if (false && verbose) {
                console.log("Found (avg) intersection at %s\n", avgPoint);
            }
            return avgPoint;
        } catch (mei) {
            // mei.printStackTrace();
            throw mei;
        }
    }
}

export { MPSToolBox };

// To be accessed from a non-module emvironment
window.MPSToolBox = MPSToolBox;
