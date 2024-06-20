/**
 * This is the chartless-map Web Component.
 * Can be used to plot a grid, markers, tracks... without having to have a cartography available. (or
 * a device supporting it).
 * 
 * Supported projection(s):
 * - Mercator
 * 
 * With callbacks.
 */

const chartlessMapVerbose = false;
const CHARTLESS_MAP_TAG_NAME = 'chartless-map';


const chartlessMapDefaultColorConfig = {
	bgColor: 'white',
	gridColor: 'rgba(0, 0, 0, 0.7)',
	fgColor: 'silver',
	markerAndTrackColor: 'blue',
	targetColor: 'green',
	borderColor: 'blue',
	valueFont: 'Courier New'
};

import * as Utilities from "./utilities/Utilities.js";

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

/* global HTMLElement */
class ChartlessMap extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",           // Integer. Canvas width
			"height",          // Integer. Canvas height
			"center-lat",      // Number. Latitude of the center of the chart
			"center-lng",      // Number. Longitude of the center of the chart
			"chart-width",     // Number. Width of the chart in degrees
			"projection"       // String. Default Mercator (TODO: Anaximandre, Lambert)
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a WebComponent ChartlessMap, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._width = 800;
		this._height = 600;
		this._centerLat = 0.0;
		this._centerLng = 0.0;
		this._chartWidth = 10.0;
		this._projection = "MERCATOR";

		this._doBefore = function(graphDisplay, context) {}; // Do-nothing by default
		this._doAfter = function(graphDisplay, context) {};  // Do-nothing by default
		this._mouseMoveFeedback = null;

		this._previousClassName = "";
		this.chartlessMapColorConfig = chartlessMapDefaultColorConfig;

		let instance = this;
		let mouseIsDown = false;
		let lastDraggedPos = null;

		this.canvas.addEventListener('mousedown', function(evt) {
			// console.log("Mouse down");
			mouseIsDown = true;
			let rect = evt.currentTarget.getBoundingClientRect();
			let x = evt.clientX - rect.left;
			let y = evt.clientY - rect.top;
			let pos = instance.canvasToPos(x, y);
			lastDraggedPos = { x: x, y: y, pos: pos };
		});

		this.canvas.addEventListener('mouseup', function(evt) {
			// console.log("Mouse up");
			mouseIsDown = false;
			lastDraggedPos = null;
		});

		// Attach a mousemovelistener to the canvas
		this.canvas.addEventListener('mousemove', function(evt) {

			let rect = evt.currentTarget.getBoundingClientRect();
			let x = evt.clientX - rect.left;
			let y = evt.clientY - rect.top;

			let pos = instance.canvasToPos(x, y);

			if (!mouseIsDown) { // Regular move, mouse up
				if (instance._mouseMoveFeedback !== null) {
					instance._mouseMoveFeedback({ x: Math.round(x), y: Math.round(y), pos: pos });
				} else {
					// console.log(`Mouse Move: ${Math.round(x)} / ${Math.round(y)}: ${JSON.stringify(pos)} => ${ChartlessMap.decToSex(pos.lat, "NS")} / ${ChartlessMap.decToSex(pos.lng, "EW")}`);
					instance.title = `${instance.decToSex(pos.lat, "NS")} / ${instance.decToSex(pos.lng, "EW")}`;
				}
			} else { // Dragging
				// console.log(`Chart being dragged: ${Math.round(x)} / ${Math.round(y)}: ${JSON.stringify(pos)} => ${ChartlessMap.decToSex(pos.lat, "NS")} / ${ChartlessMap.decToSex(pos.lng, "EW")}`);
				let mousePos = instance.canvasToPos(Math.round(x), Math.round(y));
				if (lastDraggedPos !== null) {
					let deltaLat = lastDraggedPos.pos.lat - mousePos.lat;
					let deltaLng = lastDraggedPos.pos.lng - mousePos.lng;
					let newCenterLng = instance.centerLng + deltaLng;
					let newCenterLat = instance.centerLat + deltaLat;
					// console.log(`Bam ! deltaLat=${deltaLat}, deltaLng=${deltaLng} => center ${instance.centerLat}  ${instance.centerLng} becomes ${newCenterLat}  ${newCenterLng}`);
					instance.centerLat = newCenterLat;
					instance.centerLng = newCenterLng;
					lastDraggedPos = { x: x, y: y, pos: mousePos };
				}
			}
		});

		if (chartlessMapVerbose) {
			console.log(`Data in Constructor: ${this._width} x ${this._height}, etc...`);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (chartlessMapVerbose) {
			console.log(`connectedCallback invoked, ${this._width} x ${this._height}, etc...`);
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (chartlessMapVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (chartlessMapVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "height":
				this._height = parseInt(newVal);
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "center-lat":
				this._centerLat = parseFloat(newVal);
				break;
			case "center-lng":
				this._centerLng = parseFloat(newVal);
				break;
			case "chart-width":
				this._chartWidth = parseFloat(newVal);
				break;
			case "projection":
				this._projection = newVal;
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (chartlessMapVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set chartWidth(option) {
		this.setAttribute("chart-width", option);
		if (chartlessMapVerbose) {
			console.log(">> Chart Width:", option);
		}
		// this.repaint(); // Done in attributeChangedCallback
	}

	set width(val) {
		this.setAttribute("width", val);
	}

	set height(val) {
		this.setAttribute("height", val);
	}

	set centerLat(val) {
		this.setAttribute("center-lat", val);
	}

	set centerLng(val) {
		this.setAttribute("center-lng", val);
	}

	set projection(val) {
		this.setAttribute("projection", val);
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get chartWidth() {
		return this._chartWidth;
	}

	get width() {
		return this._width;
	}

	get height() {
		return this._height;
	}

	get centerLat() {
		return this._centerLat;
	}

	get centerLng() {
		return this._centerLng;
	}

	get projection() {
		return this._projection;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	get colorConfig() {
		return this.chartlessMapColorConfig;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = chartlessMapDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(CHARTLESS_MAP_TAG_NAME) > -1))) { // Cases like "tag-name .className"
							//				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
								if (elem.trim().length > 0) {
									let keyValPair = elem.split(":");
									let key = keyValPair[0].trim();
									let value = keyValPair[1].trim();
									switch (key) {
										case '--bg-color':
											colorConfig.bgColor = value;
											break;
										case '--grid-color':
											colorConfig.gridColor = value;
											break;
										case '--fg-color':
											colorConfig.fgColor = value;
											break;
										case '--value-font':
											colorConfig.valueFont = value;
											break;
										case '--marker-and-track-color':
											colorConfig.markerAndTrackColor = value;
											break;
										case '--target-color':
											colorConfig.targetColor = value;
											break;
										case '--border-color':
											colorConfig.borderColor = value;
											break;
										default:
											break;
									}
								}
							});
						}
					}
				} catch (err) {
					// Absorb
				}
			}
		}
		return colorConfig;
	}

	setDoBefore(func) {
		this._doBefore = func;
	}
	setDoAfter(func) {
		this._doAfter = func;
	}
	setMouseMoveFeedback(func) {
		this._mouseMoveFeedback = func;
	}

	repaint() {
		this.drawChartlessMap();
	}

	drawChartlessMap() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.chartlessMapColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		context.fillStyle = this.chartlessMapColorConfig.bgColor;

		// Background
		ChartlessMap.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		this._doBefore(this, context);

		// The Grid. 
		context.font = "10px Arial";
		context.fillStyle = this.chartlessMapColorConfig.fgColor;
		context.strokeStyle = this.chartlessMapColorConfig.gridColor;
		// context.font = "bold " + Math.round(scale * 16) + "px " + this.chartlessMapColorConfig.valueFont;

		// centerLat increasing lat
		let centerIncLat = ChartlessMap.getIncLat(this._centerLat);
		// console.log(`Inc Lat of Lat Center (${this._centerLat}) = ${centerIncLat}`);

		let lngLeft = this._centerLng - (this._chartWidth / 2);
		let lngRight = this._centerLng + (this._chartWidth / 2);
		// console.log(`Left : ${ChartlessMap.decToSex(lngLeft, "EW")}`);
		// console.log(`Right: ${ChartlessMap.decToSex(lngRight, "EW")}`);
		let whRatio = this._width / this._height;
		let incLatTop = centerIncLat + ((this._chartWidth / 2) / whRatio);
		let incLatBottom = centerIncLat - ((this._chartWidth / 2) / whRatio);
		let latTop = ChartlessMap.getInvIncLat(incLatTop);
		let latBottom = ChartlessMap.getInvIncLat(incLatBottom);
		// console.log(`Top   : ${ChartlessMap.decToSex(latTop, "NS")}`);
		// console.log(`Bottom: ${ChartlessMap.decToSex(latBottom, "NS")}`);

		let chartHeight = incLatTop - incLatBottom; // In Increasing Latitude !!
		// 0.5, 1, 5, 10. To be tuned...
		// let gridStep = 0.5;
		// let gridStep = Math.round((this._chartWidth / 4) * 10) / 20;
		let gridStep = Math.round(this._chartWidth / 4) / 2;
		// console.log(`GridStep: ${gridStep}, vs ${Math.round((this._chartWidth / 4) * 10) / 20}`);
		if (gridStep === 0) {
			gridStep = Math.round((this._chartWidth / 4) * 10) / 8; // because we have a (width / 4)
		}

		if (chartlessMapVerbose) {
			console.log(`GridStep: ${gridStep}`);
		}

		let lngDegreesToPixels = this._width / this._chartWidth;
		let firstWestMeridian = parseFloat((lngLeft - gridStep).toFixed(0));
		if (chartlessMapVerbose) {
			console.log(`firstWestMeridian: ${firstWestMeridian}`);
		}
		for (let g=firstWestMeridian; g<lngRight && gridStep>0; g+=gridStep) {
			// console.log(`Between ${ChartlessMap.decToSex(lngLeft, "EW")} (${lngLeft}) and ${ChartlessMap.decToSex(lngRight, "EW")} (${lngRight}), drawing meridian at ${ChartlessMap.decToSex(g, "EW")}`);
			let x = (g - lngLeft) * lngDegreesToPixels;
			// console.log(` ${ChartlessMap.decToSex(g, "EW")} => x: ${x}`);

			context.save();
			context.beginPath();
			context.moveTo(x, 0);
			context.lineTo(x, this._height);

			// Label
			let label = `${ChartlessMap.decToSex(g, "EW")}`;
			var len = context.measureText(label).width;
			context.translate(Math.round(x + 5), 0); // len / 2);
			context.rotate(Math.PI / 2);
			context.fillText(label, 1, 1); //i * xScale, cHeight - (len));
	  
			context.stroke();
			context.closePath();
			context.restore();
		}

		let latDegreesToPixel = this._height / chartHeight;
		let firstSouthParallel = parseFloat((latBottom - gridStep).toFixed(0));
		// console.log(`Between ${ChartlessMap.decToSex(latBottom, "NS")} (${latBottom}) and ${ChartlessMap.decToSex(latTop, "NS")} (${latTop}), starting parallels at ${ChartlessMap.decToSex(firstSouthParallel, "NS")}`);
		for (let l=firstSouthParallel; l<latTop && gridStep>0; l+=gridStep) {
			// console.log(`Draw parallel at ${ChartlessMap.decToSex(l, "NS")}`);
			let y = (ChartlessMap.getIncLat(l) - ChartlessMap.getIncLat(latBottom)) * latDegreesToPixel;
			// console.log(` ${ChartlessMap.decToSex(l, "NS")} => y: ${y}`);

			context.beginPath();
			context.moveTo(0, this._height - y);
			context.lineTo(this._width, this._height - y);

			// Label
			let label = `${ChartlessMap.decToSex(l, "NS")}`;
			let metrics = context.measureText(label);
			let len = metrics.width;
			context.fillText(label, 5, Math.round(this._height - y + 12));

			context.stroke();
			context.closePath();
		}


		// To do in a _doAfter, an example.
		if (false) {
			// let center = ChartlessMap.posToCanvas(this._centerLat, this._centerLng, this._projection, this._centerLat, this._centerLng, this._chartWidth, this._width, this._height);
			let center = this.posToCanvas(this._centerLat, this._centerLng);
			// Plot center
			context.save();

			context.beginPath();
			context.strokeStyle = 'red';
			context.lineWidth = 1;
			console.log(`Plotting chart center on ${center.x} x ${center.y}`);
			context.moveTo(center.x, center.y - 20);
			context.lineTo(center.x, center.y + 20);

			context.moveTo(center.x - 20, center.y);
			context.lineTo(center.x + 20, center.y);
			context.stroke();
			context.closePath();

			context.beginPath();
			context.lineWidth = 4;
			context.arc(center.x, center.y, 10, 0, 2 * Math.PI, false);
			context.stroke();
			context.closePath();
		
			context.restore();
		}

		this._doAfter(this, context);
	}

	static roundRect(ctx, x, y, width, height, radius, fill, stroke) {
		if (fill === undefined) {
			fill = true;
		}
		if (stroke === undefined) {
			stroke = true;
		}
		if (radius === undefined) {
			radius = 5;
		}
		ctx.beginPath();
		ctx.moveTo(x + radius, y);
		ctx.lineTo(x + width - radius, y);
		ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
		ctx.lineTo(x + width, y + height - radius);
		ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
		ctx.lineTo(x + radius, y + height);
		ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
		ctx.lineTo(x, y + radius);
		ctx.quadraticCurveTo(x, y, x + radius, y);
		ctx.closePath();
		if (stroke) {
			ctx.stroke();
		}
		if (fill) {
			ctx.fill();
		}
	}

	static decToSex(val, ns_ew) {
		let absVal = Math.abs(val);
		let intValue = Math.floor(absVal);
		let dec = absVal - intValue;
		let i = intValue;
		dec *= 60;
		// Rounding pb
		if (parseFloat(dec.toFixed(2)) == 60.0) {
			i += 1;
			dec = 0;
		}
//    var s = i + "°" + dec.toFixed(2) + "'";
//    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
		let s = "";
		if (ns_ew !== undefined) {
			if (val < 0) {
				s += (ns_ew === 'NS' ? 'S' : 'W');
			} else {
				s += (ns_ew === 'NS' ? 'N' : 'E');
			}
			s += " ";
		} else {
			if (val < 0) {
				s += '-'
			}
		}
		s += i + "°" + Utilities.lpad(dec.toFixed(2), 5, '0') + "'";

		return s;
	}

	decToSex(val, ns_ew) {
		return ChartlessMap.decToSex(val, ns_ew);
	}

	/**
	 * Latitude to Increasing Latitude
	 * @param {number} lat 
	 * @returns the increasing latitude
	 */
	static getIncLat(lat) {
		let il = Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(lat) / 2)));
		return Math.toDegrees(il);
	}

	/**
	 * Increasing latitude to latitude
	 * @param {number} il 
	 * @returns the correspoonding latitude
	 */
	static getInvIncLat(il) {
		let ret = Math.toRadians(il);
		ret = Math.exp(ret);
		ret = Math.atan(ret);
		ret -= (Math.PI / 4); // 0.78539816339744828D;
		ret *= 2;
		ret = Math.toDegrees(ret);
		return ret;
	}

	static posToCanvas(lat, 
		               lng, 
					   projection, 
					   centerLat, 
					   centerLng, 
					   chartWidth,
					   canvasWidth,
					   canvasHeight) {
		if (projection !== "MERCATOR") {
			console.error(`${projection} not supported yet.`);
			return;
		}
		// centerLat increasing lat
		let centerIncLat = ChartlessMap.getIncLat(centerLat);
		// console.log(`Inc Lat of Lat Center (${centerLat}) = ${centerIncLat}`);

		let lngLeft = centerLng - (chartWidth / 2);
		// let lngRight = centerLng + (chartWidth / 2);
		// console.log(`Left : ${ChartlessMap.decToSex(lngLeft, "EW")}`);
		// console.log(`Right: ${ChartlessMap.decToSex(lngRight, "EW")}`);
		let whRatio = canvasWidth / canvasHeight;
		let incLatTop = centerIncLat + ((chartWidth / 2) / whRatio);
		let incLatBottom = centerIncLat - ((chartWidth / 2) / whRatio);
		// let latTop = ChartlessMap.getInvIncLat(incLatTop);
		let latBottom = ChartlessMap.getInvIncLat(incLatBottom);
		// console.log(`Top   : ${ChartlessMap.decToSex(latTop, "NS")}`);
		// console.log(`Bottom: ${ChartlessMap.decToSex(latBottom, "NS")}`);

		let chartHeight = incLatTop - incLatBottom; // In Increasing Latitude !!

		let lngDegreesToPixels = canvasWidth / chartWidth;
		let x = (lng - lngLeft) * lngDegreesToPixels;

		let latDegreesToPixel = canvasHeight / chartHeight;
		let y = (ChartlessMap.getIncLat(lat) - ChartlessMap.getIncLat(latBottom)) * latDegreesToPixel;

		return { x: Math.round(x), y: Math.round(canvasHeight - y) };
	}

	// Wowow !! THIS is smart (static vs non-static) !
	posToCanvas(lat, lng) {
		return ChartlessMap.posToCanvas(lat, lng, 
										this._projection, 
										this._centerLat, 
										this._centerLng, 
										this._chartWidth,
										this._width,
										this._height);
	}

	static canvasToPos(x, 
					   y, 
					   projection, 
					   centerLat, 
					   centerLng, 
					   chartWidth,
					   canvasWidth,
					   canvasHeight) {
		if (projection !== "MERCATOR") {
			console.error(`${projection} not supported yet.`);
			return;
		}
		// How far from the center
		let deltaX = x - (canvasWidth / 2);
		let deltaY = (canvasHeight / 2) - y; // Inverted, y=0 is the top.

		let deltaLng = deltaX * (chartWidth / canvasWidth);
		let g = centerLng + deltaLng;

		let deltaLat = deltaY * (chartWidth / canvasWidth); // Yes, chartWidth
		let centerIncLat = ChartlessMap.getIncLat(centerLat);
		let newIncLat = centerIncLat + deltaLat;
		let l = ChartlessMap.getInvIncLat(newIncLat);

		return { lat: l, lng: g };
	}

	canvasToPos(x, y) {
		return ChartlessMap.canvasToPos(x, y, 
										this._projection, 
										this._centerLat, 
										this._centerLng, 
										this._chartWidth,
										this._width,
										this._height);
	}

	// Markers (display)  displays
	plotMark(context, marker, markerRadius, beaconHeight, defaultColor, extraData) {
		let lat = marker.latitude;
		let lng = marker.longitude;
		let label = marker.label;
		let markType = marker.type || 'default'; // Can be undefined or null
		
		let canvasCoord = this.posToCanvas(lat, lng);
		switch (markType) {
			case 'red':
				context.save();
				context.strokeStyle = 'red';
				context.fillStyle = 'red';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (Beacon)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Top cylinder
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'green':
				context.save();
				context.strokeStyle = 'green';
				context.fillStyle = 'green';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (Beacon)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Top cone
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom left
				context.lineTo(canvasCoord.x, canvasCoord.y - beaconHeight - 10);					  // Top (center)
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'card-n':
				context.save();
				context.strokeStyle = 'black';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				// Beacon Bottom part
				context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Top part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 1
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom left
				context.lineTo(canvasCoord.x, canvasCoord.y - 20 - 10);						// Top (center)
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 2
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 12); // Bottom left
				context.lineTo(canvasCoord.x, canvasCoord.y - 20 - 20);						// Top (center)
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 12); // Bottom right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'card-s':
				context.save();
				context.strokeStyle = 'black';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				// Beacon Bottom part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Top part
				context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.5), canvasCoord.y - (beaconHeight / 2)); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 1
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x, canvasCoord.y - beaconHeight - 3);						 // Bottom (center)
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top right
				context.closePath();
				context.fill();
				// Top. Cone 2
				context.beginPath();
				context.moveTo(canvasCoord.x, canvasCoord.y - beaconHeight - 12);						// Bottom (center)
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 20); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 20); // Top right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'card-e':
				context.save();
				context.strokeStyle = 'black';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				// Beacon Bottom part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Middle part
				context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Top part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 1 (bottom one)
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x, canvasCoord.y - beaconHeight - 3);						 // Bottom (center)
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 10); // Top right
				context.closePath();
				context.fill();
				// Top. Cone 2 (top one)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 12); // Bottom left
				context.lineTo(canvasCoord.x, canvasCoord.y - 20 - 20);						// Top (center)
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 12); // Bottom right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'card-w':
				context.save();
				context.strokeStyle = 'black';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				// Beacon Bottom part
				context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Middle part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Beacon Top part
				context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 1
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom left
				context.lineTo(canvasCoord.x, canvasCoord.y - beaconHeight - 10);						// Top (center)
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 3); // Bottom right
				context.closePath();
				context.fill();
				// Top. Cone 2
				context.beginPath();
				context.moveTo(canvasCoord.x, canvasCoord.y - beaconHeight - 12);						 // Bottom (center)
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight - 20); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight - 20); // Top right
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'sp':
				context.save();
				context.strokeStyle = 'black';
				context.fillStyle = 'yellow';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				context.fill();
				// Top. Diagonal Cross
				context.lineWidth = 4;
				context.strokeStyle = 'yellow';
				// context.fillStyle = 'yellow';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y - beaconHeight - 2); // Bottom left
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y - beaconHeight - 2 - (markerRadius / 1));	// Top right
				context.closePath();
				context.stroke();

				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y - beaconHeight - 2 - (markerRadius / 1)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y - beaconHeight - 2);	// Bottom right
				context.closePath();
				context.stroke();
				context.restore();
				break;
			case 'is-dng':
				context.save();
				context.strokeStyle = 'black';
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Cone (the beacon body)
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.stroke();
				// Bottom part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2), canvasCoord.y); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2), canvasCoord.y); // Bottom right
				context.closePath();
				context.fill();
				// Middle part
				context.fillStyle = 'red';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.33), canvasCoord.y - (beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Top part
				context.fillStyle = 'black';
				context.beginPath();
				context.moveTo(canvasCoord.x - (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom left
				context.lineTo(canvasCoord.x - (markerRadius / 3), canvasCoord.y - beaconHeight); // Top left
				context.lineTo(canvasCoord.x + (markerRadius / 3), canvasCoord.y - beaconHeight); // Top right
				context.lineTo(canvasCoord.x + (markerRadius / 2.66), canvasCoord.y - (2 * beaconHeight / 3)); // Bottom right
				context.closePath();
				context.fill();
				// Top. sphere 1
				context.fillStyle = 'black';
				context.beginPath();
				context.arc(canvasCoord.x, canvasCoord.y - beaconHeight - (1 * markerRadius / 3), markerRadius / 3, 0, 2 * Math.PI, false);
				context.closePath();
				context.fill();
				// Top. sphere 2
				context.beginPath();
				context.arc(canvasCoord.x, canvasCoord.y - beaconHeight - (3 * (markerRadius / 3)), markerRadius / 3, 0, 2 * Math.PI, false);
				context.closePath();
				context.fill();
				context.restore();
				break;
			case 'default':
			default:
				context.save();
				context.strokeStyle = defaultColor;
				// Crosshair
				context.lineWidth = 0.5;
				context.beginPath();
				context.moveTo(canvasCoord.x - markerRadius, canvasCoord.y);
				context.lineTo(canvasCoord.x + markerRadius, canvasCoord.y);
				context.moveTo(canvasCoord.x, canvasCoord.y - markerRadius);
				context.lineTo(canvasCoord.x, canvasCoord.y + markerRadius);
				context.stroke();
				context.closePath();
				// Circle
				context.beginPath();
				context.strokeStyle = defaultColor;
				context.lineWidth = 2;
				context.arc(canvasCoord.x, canvasCoord.y, markerRadius, 0, 2 * Math.PI, false);
				context.stroke();
				context.restore();
				break;
		}
		// Label
		context.font = "bold 12px Arial";
		context.fillStyle = defaultColor;
		context.fillText(label, canvasCoord.x + markerRadius + 2, canvasCoord.y);
		if (extraData !== null) { // Optional extra lines, near the label
			context.font = "12px Arial";
			extraData.forEach((line, idx) => {
				context.fillText(line, canvasCoord.x + markerRadius + 2, canvasCoord.y + (12 * (idx + (label.trim().length == 0 ? 0 : 1))));
			}); 
		}
		context.closePath();
	}
}

// Associate the tag and the class
window.customElements.define(CHARTLESS_MAP_TAG_NAME, ChartlessMap);
