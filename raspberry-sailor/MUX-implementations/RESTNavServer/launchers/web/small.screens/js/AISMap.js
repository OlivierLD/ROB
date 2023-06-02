/*
 * @author Olivier Le Diouris
 * This is NOT a Web Component.
 */
"use strict";

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

/**
 * Draw a map of the AIS target in sight (within a radius)
 * 
 * @param {string} cName Canvas Name. Mandatory prm.
 * @param {number} width Canvas width, default 400
 * @param {number} height Canvas height, default 400
 * @param {color} bgColor Background color, default black
 * @param {color} fgColor Foreground (track) color, default red
 * @param {color} gridColor Grid color, default green
 * @param {color} textColor Text color, default white
 * @param {number} radius In-sight radius
 */
function AISMap(cName, width, height, bgColor, fgColor, gridColor, textColor, radius) {

	this.bg = (bgColor || 'black');
	this.fg = (fgColor || 'red');   // Track
	this.gc = (gridColor || 'green');
	this.tc = (textColor || 'white');

	this.w = (width || 400);
	this.h = (height || 400);

	this.radius = (radius || 5);

	this.aisTargets = [];
	this.markers = [];
	this.currentPos = { lat: 0, lng: 0 };
	this.lastCog = 0;
	this.lastSog = 0;

	let canvasName = cName;
	let canvas = document.getElementById(canvasName);
	let context = canvas.getContext('2d');

	this.setCurrentPoint = function(point) {
		this.currentPos = point;
		this.repaint();
	};

	this.resetAISTargets = function() {
		this.aisTargets = [];
	};
	this.resetMarkers = function() {
		this.markers = [];
	};

	function drawArrow(context, canvasX, canvasY, boatLen, heading, color) {
		
		// console.log(`Drawing arrow for heading ${heading}`);

		context.beginPath();
		context.lineWidth = 1.5;
		context.fillStyle = color;

		context.moveTo(canvasX +  ((boatLen / 2) * Math.sin(Math.toRadians(heading))),
		               canvasY - ((boatLen / 2) * Math.cos(Math.toRadians(heading))));

		context.lineTo(canvasX + ((boatLen / 2) * Math.sin(Math.toRadians(heading))),
			           canvasY - ((boatLen / 2) * Math.cos(Math.toRadians(heading))));
		context.lineTo(canvasX - ((boatLen / 2) * Math.sin(Math.toRadians(heading - 10))),
					   canvasY + ((boatLen / 2) * Math.cos(Math.toRadians(heading - 10))));
		context.lineTo(canvasX - ((boatLen * 0.9 / 2) * Math.sin(Math.toRadians(heading - 0))),
					   canvasY + ((boatLen * 0.9 / 2) * Math.cos(Math.toRadians(heading - 0)))); // Transom center
		context.lineTo(canvasX - ((boatLen / 2) * Math.sin(Math.toRadians(heading + 10))),
		               canvasY + ((boatLen / 2) * Math.cos(Math.toRadians(heading + 10))));

		context.closePath();
		context.stroke();
	}

	/*
	 * aisTarget: {
	 * 	 mmsi: string, 
	 *   vesselName: string, 
	 *   lat: number, 
	 *   lng: number, 
	 *   cog: number, 
	 *   sog: number, 
	 *   radius: number,
	 *   threat: boolean
	 * }
	 */
	this.addAISTarget = function(point) {
		this.aisTargets.push(point);
		this.repaint();
	};

	this.setMarkerList = function(list) {
		this.markers = list;
		this.repaint();
	};

	this.setLastCog = function(cog) {
		this.lastCog = cog;
	};

	this.setLastSog = function(sog) {
		this.lastSog = sog;
	};

	this.repaint = function() {

		context.beginPath();
		context.fillStyle = this.bg;
		context.fillRect(0, 0, canvas.width, canvas.height); // cleanup

		context.lineWidth = 2;
		context.arc((canvas.width / 2), (canvas.height / 2), Math.min((canvas.width / 2), (canvas.height / 2)), 0, 2 * Math.PI, false);
		context.strokeStyle = 'silver'; // Hard-coded, not needed on a real watch
		context.stroke();

		// A grid, to show off
		context.lineWidth = 1;
		context.strokeStyle = this.gc;
		let nbLines = 6;
		for (let x=1; x<nbLines; x++) {
			context.moveTo(x * (this.w / nbLines), 0);
			context.lineTo(x * (this.w / nbLines), this.h);
		}
		for (let y=1; y<nbLines; y++) {
			context.moveTo(0, y * (this.h / nbLines));
			context.lineTo(this.w, y * (this.h / nbLines));
		}
		context.stroke();
		context.closePath();

		// A label to show off again
		let fontSize = 12;
		let text = 'AIS Targets';
		let len = 0;
		context.font = "bold " + fontSize + "px Arial"; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = this.tc; 
		context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) - (fontSize) - 2);

		if (this.lastCog !== undefined) {
			text = 'COG ' + this.lastCog.toFixed(0) + String.fromCharCode(176);
			let metrics = context.measureText(text);
			len = metrics.width;
			context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) + (fontSize) - 2);
		}

		if (this.lastSog !== undefined) {
			text = 'SOG ' + this.lastSog.toFixed(1) + ' kts';
			let metrics = context.measureText(text);
			len = metrics.width;
			context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) + (2 * fontSize) - 2);
		}

		context.closePath();

		// Draw the AIS Targets here
		// 1 - Find the min and max, for latitude and longitude
		let mapCenter = this.currentPos;

		let minLat = mapCenter.lat - (radius / 60.0), 
			maxLat = mapCenter.lat + (radius / 60.0), 
			minLng = mapCenter.lng - (radius / 60.0), 
			maxLng = mapCenter.lng + (radius / 60.0);

		let deltaLat = Math.abs(maxLat - minLat);
		let deltaLng = Math.abs(maxLng - minLng);

		let delta = Math.max(deltaLat, deltaLng);
		if (delta === 0) {
			return; // Make sure it's not zero...
		}
		let sizeFactor = 1;
		let boatRadius = 10;
		let markerRadius = 5;

		// Draw current pos & heading
		context.beginPath();
		context.lineWidth = 3;

		let canvasX = (this.w / 2) + (((mapCenter.lng - mapCenter.lng) * (this.w / delta)) * sizeFactor);
		let canvasY = (this.h / 2) - (((mapCenter.lat - mapCenter.lat) * (this.h / delta)) * sizeFactor);

		context.arc(canvasX, canvasY, boatRadius, 0, 2 * Math.PI);

		context.strokeStyle = 'orange'; // this.fg;
		context.stroke();
		context.closePath();
		// Heading 
		drawArrow(context, canvasX, canvasY, 6 * boatRadius, this.lastCog, 'orange');

		this.aisTargets.forEach((target, idx) => {
		   /*
			* aisTarget: {
			* 	 mmsi: string, 
			*   vesselName: string, 
			*   lat: number, 
			*   lng: number, 
			*   cog: number, 
			*   sog: number, 
			*   radius: number,
			*   threat: boolean
			* }
			*/
			let withinSight = true; // TODO Something real..., radius etc.
			if (withinSight) {
				// console.log(`- (AISMap) Plotting AIS Target ${target.vesselName !== null ? target.vesselName : target.mmsi} ${target.threat ? '- Honk!' : ''}`)
				context.beginPath();
				context.lineWidth = 3;

				let canvasX = (this.w / 2) + (((target.lng - mapCenter.lng) * (this.w / delta)) * sizeFactor);
				let canvasY = (this.h / 2) - (((target.lat - mapCenter.lat) * (this.h / delta)) * sizeFactor);

				context.arc(canvasX, canvasY, boatRadius, 0, 2 * Math.PI);

				context.strokeStyle = target.threat ? 'red' : 'green'; // this.fg;
				context.stroke();
				context.closePath();
				drawArrow(context, canvasX, canvasY, 5 * boatRadius, this.lastCog, context.strokeStyle);
			}
		});

		// Markers, if any !
		this.markers.forEach(marker => {
			context.beginPath();
			context.lineWidth = 3;

			let canvasX = (this.w / 2) + (((marker.longitude - mapCenter.lng) * (this.w / delta)) * sizeFactor);
			let canvasY = (this.h / 2) - (((marker.latitude - mapCenter.lat) * (this.h / delta)) * sizeFactor);
			// console.log(`Plotting marker ${marker.label}`);
			context.arc(canvasX, canvasY, markerRadius, 0, 2 * Math.PI);
			context.font = "8px Courier";
			context.fillText(marker.label, canvasX + markerRadius + 1, canvasY - markerRadius);
			context.fillStyle = 'cyan';
			context.strokeStyle = 'cyan'; // this.fg;
			context.stroke();
			context.closePath();
		});
	};
};
