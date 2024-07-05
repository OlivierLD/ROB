/*
 * Haversine and friends
 * NodeJS compatible
 */

const KM_EQUATORIAL_EARTH_RADIUS   = 6378.1370;       // km per radian. Pi * 6378.1370 = 20037.5083 km
const NM_EQUATORIAL_EARTH_RADIUS   = 3443.9184665227; // nm per radian. (Pi * 3443.9184..) / 60 ~= 180
const MILE_EQUATORIAL_EARTH_RADIUS = 3964.0379117464; // statute mile per radian

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	};
}

let decToSex = (val, ns_ew) => {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	let i = intValue;
	dec *= 60;
	let s = i + "°" + dec.toFixed(2) + "'";

	if (val < 0) {
		s += (ns_ew === 'NS' ? 'S' : 'W');
	} else {
		s += (ns_ew === 'NS' ? 'N' : 'E');
	}
	return s;
};

// aka deadReckoning. { lat: degrees, lng: degrees }, nm, degrees. Result in degrees.
let haversineInv = (from, dist, heading) => {
	let distRatio = dist / NM_EQUATORIAL_EARTH_RADIUS; // THE key.
	let distRatioSine = Math.sin(distRatio);
	let distRatioCosine = Math.cos(distRatio);

	let startLatRad = Math.toRadians(from.lat);
	let startLonRad = Math.toRadians(from.lng);

	let startLatCos = Math.cos(startLatRad);
	let startLatSin = Math.sin(startLatRad);

	let angleRadHeading = Math.toRadians(heading);
	let endLatRads = Math.asin((startLatSin * distRatioCosine) + (startLatCos * distRatioSine * Math.cos(angleRadHeading)));

	let endLonRads = startLonRad + Math.atan2(Math.sin(angleRadHeading) * distRatioSine * startLatCos, distRatioCosine - startLatSin * Math.sin(endLatRads));

	return { lat: Math.toDegrees(endLatRads), lng: Math.toDegrees(endLonRads) };
};

// aka deadReckoning. { lat: radians, lng: radians }, nm, degrees. Result in radians.
let haversineInvRad = (from, dist, heading) => {
	let distRatio = dist / NM_EQUATORIAL_EARTH_RADIUS; // THE key.
	let distRatioSine = Math.sin(distRatio);
	let distRatioCosine = Math.cos(distRatio);

	let startLatRad = from.lat;
	let startLonRad = from.lng;

	let startLatCos = Math.cos(startLatRad);
	let startLatSin = Math.sin(startLatRad);

	let angleRadHeading = Math.toRadians(heading);
	let endLatRads = Math.asin((startLatSin * distRatioCosine) + (startLatCos * distRatioSine * Math.cos(angleRadHeading)));

	let endLonRads = startLonRad + Math.atan2(Math.sin(angleRadHeading) * distRatioSine * startLatCos, distRatioCosine - startLatSin * Math.sin(endLatRads));

	return { lat: endLatRads, lng: endLonRads };
};

// tests
let fromLat = 47.677667;
let fromLng = -3.135667;
let dist = 10;
let cog = 90;

console.log("Let's go...\n");

let greatCirclePoint = haversineInv({ lat: fromLat, lng: fromLng }, dist, cog);
console.log(`From ${decToSex(fromLat, 'NS') + ' / ' + decToSex(fromLng, 'EW')}. New pos, ${dist} nm in the ${cog}: ${greatCirclePoint.lat + ' / ' + greatCirclePoint.lng} => ${decToSex(greatCirclePoint.lat, 'NS') + ' / ' + decToSex(greatCirclePoint.lng, 'EW')}`);

cog = 45;
greatCirclePoint = haversineInv({ lat: fromLat, lng: fromLng }, dist, cog);
console.log(`From ${decToSex(fromLat, 'NS') + ' / ' + decToSex(fromLng, 'EW')}. New pos, ${dist} nm in the ${cog}: ${greatCirclePoint.lat + ' / ' + greatCirclePoint.lng} => ${decToSex(greatCirclePoint.lat, 'NS') + ' / ' + decToSex(greatCirclePoint.lng, 'EW')}`);

cog = 45;
greatCirclePoint = haversineInvRad({ lat: Math.toRadians(fromLat), lng: Math.toRadians(fromLng) }, dist, cog);
console.log(`Radians: New pos, ${dist} nm in the ${cog}: ${greatCirclePoint.lat + ' / ' + greatCirclePoint.lng}`);
console.log(`Degrees: New pos, ${dist} nm in the ${cog}: ${
	Math.toDegrees(greatCirclePoint.lat) + ' / ' + Math.toDegrees(greatCirclePoint.lng)
} => ${
	decToSex(Math.toDegrees(greatCirclePoint.lat), 'NS') + ' / ' + decToSex(Math.toDegrees(greatCirclePoint.lng), 'EW')
}`);

fromLat = 0;
fromLng = 0;
dist = 5400;
cog = 0;
greatCirclePoint = haversineInv({ lat: fromLat, lng: fromLng }, dist, cog);
console.log(`From ${decToSex(fromLat, 'NS') + ' / ' + decToSex(fromLng, 'EW')}. New pos, ${dist} nm in the ${cog}: ${greatCirclePoint.lat + ' / ' + greatCirclePoint.lng} => ${decToSex(greatCirclePoint.lat, 'NS') + ' / ' + decToSex(greatCirclePoint.lng, 'EW')}`);

console.log("\nEnd of test.");
