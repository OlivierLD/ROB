/*
 * @author Olivier Le Diouris
 * This is NOT a Web Component.
 */
"use strict";

/**
 * Draw a map of the current path (stored positions)
 * 
 * @param {string} cName Canvas Name. Mandatory prm.
 * @param {number} width Canvas width, default 400
 * @param {number} height Canvas height, default 400
 * @param {color} bgColor Background color, default black
 * @param {color} fgColor Foreground (track) color, default red
 * @param {color} gridColor Grid color, default green
 * @param {color} textColor Text color, default white
 * @param {number} buffSize Max number of points in the track, default 400
 */
function TrackMap(cName, width, height, bgColor, fgColor, gridColor, textColor, buffSize) {

	this.bg = (bgColor || 'black');
	this.fg = (fgColor || 'red');   // Track
	this.gc = (gridColor || 'green');
	this.tc = (textColor || 'white');

	this.w = (width || 400);
	this.h = (height || 400);

	this.bufferSize = (buffSize || 400);

	this.posBuffer = [];
	this.markers = [];
	this.lastCog = 0;
	this.lastSog = 0;

	let markerRadius = 5;

	let canvasName = cName;
	let canvas = document.getElementById(canvasName);
	let context = canvas.getContext('2d');

	this.resetMarkers = function() {
		this.markers = [];
	};

	this.setMarkerList = function(list) {
		this.markers = list;
		this.repaint();
	};

	/**
	 * Add point to tail of the buffer, drop the head if needed.
	 *
	 * @param point { lat: x, lng: y }
	 */
	this.addPoint = function(point) {
		this.posBuffer.push(point);
		while (this.posBuffer.length > this.bufferSize) {
			this.posBuffer.splice(0, 1);
		}
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
		let text = 'THE TRACK';
		let len = 0;
		context.font = "bold " + (fontSize * 1.5) + "px Arial"; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = 'rgba(128, 128, 128, 0.75)'; // this.tc; 
		context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) + ((fontSize * 1.5) / 2));
		// context.fillText(text, (this.w - len - 2), fontSize); // (this.h / 2) - (fontSize) - 2);

		context.fillStyle = this.tc; 
		context.font = "bold " + fontSize + "px Arial"; // "bold 40px Arial"
		if (this.lastCog !== undefined) {
			text = 'COG ' + this.lastCog.toFixed(0) + String.fromCharCode(176);
			let metrics = context.measureText(text);
			len = metrics.width;
			// context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) + (fontSize) - 2);
			context.fillText(text, 0, this.h - fontSize);
		}

		if (this.lastSog !== undefined) {
			text = 'SOG ' + this.lastSog.toFixed(1) + ' kts';
			let metrics = context.measureText(text);
			len = metrics.width;
			// context.fillText(text, (this.w / 2) - (len / 2), (this.h / 2) + (2 * fontSize) - 2);
			context.fillText(text, (this.w / 2), this.h - fontSize);
		}

		context.closePath();

		// Draw the map here
		// 1 - Find the min and max, for latitude and longitude
		let minLat = 100, maxLat = -100, minLng = 200, maxLng = -200;
		if (this.posBuffer.length > 1) {
			this.posBuffer.forEach( pos => {
				minLat = Math.min(minLat, pos.lat);
				maxLat = Math.max(maxLat, pos.lat);
				minLng = Math.min(minLng, pos.lng);
				maxLng = Math.max(maxLng, pos.lng);
			});
		} else {
			return;
		}

		let deltaLat = Math.abs(maxLat - minLat);
		let deltaLng = Math.abs(maxLng - minLng);

		let delta = Math.max(deltaLat, deltaLng);
		if (delta === 0) {
			return; // Make sure it's not zero...
		}

		let mapCenter = {
			lat: minLat + ((maxLat - minLat) / 2),
			lng: minLng + ((maxLng - minLng) / 2)
		};

		// Draw track here (square projection, that will do for now)
		context.beginPath();
		context.lineWidth = 3;
		let sizeFactor = 1;
		// 1 - find the right sizeFactor, for all the track points to fit in the circle.
		this.posBuffer.forEach((pos, idx) => {
			let x = (this.w / 2) + ((pos.lng - mapCenter.lng) * (this.w / delta));
			let y = (this.h / 2) - ((pos.lat - mapCenter.lat) * (this.h / delta));
			let dx = Math.abs((this.w / 2) - x);
			let dy = Math.abs((this.h / 2) - y);
			let distToCenter = Math.sqrt((dx * dx) + (dy * dy));
			sizeFactor = Math.min(sizeFactor, (this.w / 2) / distToCenter);
		});
		sizeFactor *= 0.95; // Not too close to the borders.
		// console.log('Factor:' + sizeFactor);
		// 2 - Apply it.
		var canvasX, canvasY;
		this.posBuffer.forEach((pos, idx) => {
			canvasX = (this.w / 2) + (((pos.lng - mapCenter.lng) * (this.w / delta)) * sizeFactor);
			canvasY = (this.h / 2) - (((pos.lat - mapCenter.lat) * (this.h / delta)) * sizeFactor);
			if (pos === 0) {
				context.moveTo(canvasX, canvasY);
			} else {
				context.lineTo(canvasX, canvasY);
			}
		});
		context.strokeStyle = this.fg;
		context.stroke();
		context.closePath();
		// Dot on last pos
		if (canvasX !== undefined && canvasY !== undefined) {
			context.beginPath();
			let radius = 6;
			if (true || this.lastCog === undefined) { // Draw a dot
				context.arc(canvasX, canvasY, radius, 0, 2 * Math.PI, false);
			} else { // Draw an arrow
				// console.log(`Last COG: ${this.lastCog}, on x: ${Math.sin(Math.toRadians(this.lastCog))}, on y: ${Math.cos(Math.toRadians(this.lastCog))}`);
				context.moveTo(canvasX, canvasY);
				context.lineTo(canvasX - ((2 * radius) * Math.sin(Math.toRadians(this.lastCog))), canvasY - (radius * Math.cos(Math.toRadians(this.lastCog))));
				context.lineTo(canvasX - ((2 * radius) * Math.sin(Math.toRadians(this.lastCog))), canvasY + (radius * Math.cos(Math.toRadians(this.lastCog))));
			}
			context.closePath();
			context.fillStyle = this.fg;
			context.fill();
			// Crosshair ?
			if (true) {
				context.lineWidth = 1;
				context.strokeStyle = 'cyan';
				context.beginPath();
				context.moveTo(canvasX - 10, canvasY);
				context.lineTo(canvasX + 10, canvasY);
				context.closePath();
				context.stroke();

				context.beginPath();
				context.moveTo(canvasX, canvasY - 10);
				context.lineTo(canvasX, canvasY + 10);
				context.closePath();
				context.stroke();
			}

		}

		// Markers, if any !
		this.markers.forEach(marker => {
			context.beginPath();
			context.lineWidth = 3;

			canvasX = (this.w / 2) + (((marker.longitude - mapCenter.lng) * (this.w / delta)) * sizeFactor);
			canvasY = (this.h / 2) - (((marker.latitude - mapCenter.lat) * (this.h / delta)) * sizeFactor);
			// console.log(`Plotting marker ${marker.label} (${canvasX}, ${canvasY})`);
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
