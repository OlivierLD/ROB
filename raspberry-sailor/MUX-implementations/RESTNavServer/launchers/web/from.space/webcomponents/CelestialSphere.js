"use strict";

/*
 * Inspired by Navigator (www.tecepe.com.br/nav), tab StarFinder, on Android
 * Tag name could be improved...
 */

const celestialSphereVerbose = false;
const CELESTIALSPHERE_TAG_NAME = 'celestial-sphere';

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

import * as Utilities from "./utilities/Utilities.js";
import '../date.proto.js';

/* The map data */
import constellations from "./skymap/stars/constellations.js";
// import * as Utilities from "../utilities/Utilities.js";
// import constellations from "./stars/constellations"; // minifyJs does NOT like the .js extension

const CardinalPoints = [
	"N", "NNE", "NE", "ENE",
	"E", "ESE", "SE", "SSE",
	"S", "SSW", "SW", "WSW",
	"W", "WNW", "NW", "NNW"
];

const celestialSphereDefaultColorConfig = {
	bgColor: 'white',
	ticksColor: 'gray',
	tickLabelsColor: 'black',
	skyBGColor: 'rgb(21, 21, 86)', // 'lightGray',
	headingTicksColor: 'orange',
	skyGridColor: 'cyan',
	declinationCircleColor: 'cyan',
	ariesLabelColor: 'silver',
	cardPointLabelsColor: 'red',
	equatorialGridColor: 'blue',
	constellationLineColor: 'lightGray',
	constellationNameColor: 'orange',
	starColor: 'gold',
	starCircleColor: 'black',
	starNameColor: 'white',
	wanderingBodiesColor: 'orangered', // 'cyan',
	wanderingBodiesNameColor: 'limegreen',
	boatFillColor: 'silver', // unused
	boatOutlineColor: 'rgba(192, 192, 192, 0.75'
};

/* global HTMLElement */
class CelestialSphere extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",                  // Integer. Canvas width
			"height",                 // Integer. Canvas height
			"star-names",             // boolean. Default true (major stars only)
			"stars",                  // boolean. Default true
			"constellation-names",    // boolean. Default false
			"constellations",         // boolean. Default true
			"wandering-bodies",       // boolean. Default false
			"latitude",               // Number [-90..90], default 45 (see below)
			"longitude",              // Number [-180..180], default 3 (see below)
			"heading",                // Boat (true) heading (N, 0 by default)
			"use-heading",            // true or false. Heading North (0 true) if false
			"boat-shape"              // MONO, CATA, TRI, PLANE
			// TODO a zoom factor?
		];
	}

	static dummyDump() {
		console.log('We have %d constellation(s).', constellations.length);
		for (let i=0; i<constellations.length; i++) {
			console.log("- %s: %d star(s)", constellations[i].name, constellations[i].stars.length);
			if (i === 0) {
				console.log(constellations[i]);
			}
		}
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a CelestialSphere or StarFinder, on an HTML5 canvas"); // used with fallback
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// For tests of the import
		//	this.dummyDump();

		// For the boat shapes
		this.WL_RATIO_COEFF = 0.75; // Ratio to apply to (3.5 * Width / Length)
		this.BOAT_LENGTH = 100; // 50;
		this._boatShape = 'MONO'; // MONO, CATA, TRI, PLANE

		// Default values
		this._width       = 500;
		this._height      = 500;

		this._zoom        = 1.0;

		this._heading = 0;
		this._use_heading = false;

		this.majorTicks = 45; // prm ?
		this.minorTicks =  5; // prm ?

		this.LHAAries = 0;

		this.observerLatitude = 45;
		this.observerLongitude = -3;

		this._starNames = true;
		this._withStars = true;
		this._constellationNames = false;
		this._withConstellations = true;
		this._withWanderingBodies = false;
		this._wanderingBodiesData = undefined;

		this._previousClassName = "";
		this.celestialSphereColorConfig = celestialSphereDefaultColorConfig;

		this.doAFter = function(sunPath, context) {
			// Do-nothing by default; Callback, after drawing. Takes 'this' and the context as parameter.
		};
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (celestialSphereVerbose) {
			console.log("connectedCallback invoked");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (celestialSphereVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (celestialSphereVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "stars":
				this._withStars = (newVal === 'true');
				break;
			case "star-names":
				this._starNames = (newVal === 'true');
				break;
			case "constellations":
				this._withConstellations = (newVal === 'true');
				break;
			case "constellation-names":
				this._constellationNames = (newVal === 'true');
				break;
			case "wandering-bodies":
				this._withWanderingBodies = (newVal === 'true');
				break;
			case "latitude":
				this.observerLatitude = parseFloat(newVal);
				break;
			case "longitude":
				this.observerLongitude = parseFloat(newVal);
				break;
			case "heading":
				this._heading = parseInt(newVal);
				break;
			case "use-heading":
				this._use_heading = (newVal === 'true');
				break;
			case "boat-shape":
				switch (newVal) {
					case 'MONO':
					case 'CATA':
					case 'TRI':
					case 'PLANE':
						this._boatShape = newVal;
						break;
					default:
						console.log(`BoatShape unchanged, invalid value [${newVal}].`);
						break;
				}
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (celestialSphereVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set stars(val) {
		this._withStars = val;
	}
	set starNames(val) {
		this._starNames = val;
	}
	set constellations(val) {
		this._withConstellations = val;
	}
	set constellationNames(val) {
		this._constellationNames = val;
	}
	set wanderingBodies(val) {
		this._withWanderingBodies = val;
	}
	set wanderingBodiesData(json) {
		this._wanderingBodiesData = json;
	}
	set latitude(val) {
		this.observerLatitude = val;
	}
	set longitude(val) {
		this.observerLongitude = val;
	}
	set heading(val) {
		this._heading = val;
	}
	set useHeading(val) {
		this._use_heading = val;
	}
	set boatShape(val) {
		this._boatShape = val;
	}
	set zoom(val) {
		this._zoom = val;
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get stars() {
		return this._withStars;
	}
	get starNames() {
		return this._starNames;
	}
	get constellations() {
		return this._withConstellations;
	}
	get constellationNames() {
		return this._constellationNames;
	}
	get wanderingBodies() {
		return this._withWanderingBodies;
	}
	get wanderingBodiesData() {
		return this._wanderingBodiesData;
	}
	get latitude() {
		return this.observerLatitude;
	}
	get longitude() {
		return this.observerLongitude;
	}
	get heading() {
		return this._heading;
	}
	get useHeading() {
		return this._use_heading;
	}
	get boatShape() {
		return this._boatShape;
	}
	get zoom() {
		return this._zoom;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	/*
	 * Component methods
	 */

	static getColorConfig(cssClassNames) {
		let colorConfig = celestialSphereDefaultColorConfig;
		let classes = cssClassNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r=0; document.styleSheets[s].cssRules !== null && r<document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(WORLD_MAP_TAG_NAME) > -1))) { // Cases like "tag-name .className"
							//				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
								if (elem.trim().length > 0) {
									let keyValPair = elem.split(":");
									let key = keyValPair[0].trim();
									let value = keyValPair[1].trim();
									switch (key) {
										case '--background-color':
											colorConfig.bgColor = value;
											break;
										case '--ticks-color':
											colorConfig.ticksColor = value;
											break;
										case '--ticks-labels-color':
											colorConfig.tickLabelsColor = value;
											break;
										case '--sky-background':
											colorConfig.skyBGColor = value;
											break;
										case '--heading-ticks-color':
											colorConfig.headingTicksColor = value;
											break;
										case '--sky-grid-color':
											colorConfig.skyGridColor = value;
											break;
										case '--declination-color':
											colorConfig.declinationCircleColor = value;
											break;
										case '--aries-label-color':
											colorConfig.ariesLabelColor = value;
											break;
										case '--card-points-color':
											colorConfig.cardPointLabelsColor = value;
											break;
										case '--equatorial-grid-color':
											colorConfig.equatorialGridColor = value;
											break;
										case '--constellation-lines-color':
											colorConfig.constellationLineColor = value;
											break;
										case '--constellation-names-color':
											colorConfig.constellationNameColor = value;
											break;
										case '--star-color':
											colorConfig.starColor = value;
											break;
										case '--star-names-color':
											colorConfig.starNameColor = value;
											break;
										case '--star-circle-color':
											colorConfig.starCircleColor = value;
											break;
										case '--wandering-bodies-color':
											colorConfig.wanderingBodiesColor = value;
											break;
										case '--wandering-bodies-labels-color':
											colorConfig.wanderingBodiesNameColor = value;
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

	setDoAfter(func) {
		console.log("Setting doAfter callback function");
		this.doAfter = func;
	}

	repaint() {
		this.drawCelestialSphere();
	}

	getCanvasCenter() {
		let cw = this._width * this._zoom;
		let ch = this._height * this._zoom;
		return { x: cw / 2, y: ch / 2};
	}

	drawBoat(context, trueHeading) {
		let x = [];
		let y = [];// Half, length

		let __zoom = 3;

		let boatLength = this.BOAT_LENGTH * __zoom * this._zoom;

		if (this._boatShape === 'MONO') {
			// Width
			x.push(this.WL_RATIO_COEFF * 0); // Bow
			//     Starboard
			x.push(this.WL_RATIO_COEFF * (   1 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (   2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (   2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( 1.5 * boatLength) / 7); // Transom, starboard
			//     Port
			x.push(this.WL_RATIO_COEFF * (-1.5 * boatLength) / 7); // Transom, port
			x.push(this.WL_RATIO_COEFF * (  -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -1 * boatLength) / 7);

			// Length
			y.push((-4 * boatLength) / 7); // Bow
			//      Starboard
			y.push((-3 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push( (1 * boatLength) / 7);
			y.push( (3 * boatLength) / 7);
			//     Port
			y.push( (3 * boatLength) / 7);
			y.push( (1 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push((-3 * boatLength) / 7);

		} else if (this._boatShape === 'CATA') {
			x.push(this.WL_RATIO_COEFF * 0); // Arm, front, center
			// Starboard
			x.push(this.WL_RATIO_COEFF * (   1 * boatLength) / 7); // Arm starboard, hull side
			x.push(this.WL_RATIO_COEFF * ( 1.5 * boatLength) / 7); // Starboard bow
			x.push(this.WL_RATIO_COEFF * (   2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (   2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (   2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( 1.8 * boatLength) / 7); // Starboard transform, ext
			x.push(this.WL_RATIO_COEFF * ( 1.2 * boatLength) / 7); // Starboard transform, int
			x.push(this.WL_RATIO_COEFF * (   1 * boatLength) / 7); // Arm, back, starboard, hull side
			x.push(this.WL_RATIO_COEFF * (   0 * boatLength) / 7); // Arm, back, starboard, center
			// Port
			x.push(this.WL_RATIO_COEFF * (  -0 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -1 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (-1.2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (-1.8 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (-1.5 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  -1 * boatLength) / 7);

			// Length
			y.push((-1 * boatLength) / 7);
			//   Starboard
			y.push((-1 * boatLength) / 7);
			y.push((-4 * boatLength) / 7); // Bow
			y.push((-1 * boatLength) / 7);
			y.push((0 * boatLength) / 7);
			y.push((1 * boatLength) / 7);
			y.push((3 * boatLength) / 7);
			y.push((3 * boatLength) / 7);
			y.push((1 * boatLength) / 7);
			y.push((1 * boatLength) / 7);
			//    Port
			y.push((1 * boatLength) / 7);
			y.push((1 * boatLength) / 7); // Bow
			y.push((3 * boatLength) / 7);
			y.push((3 * boatLength) / 7);
			y.push((1 * boatLength) / 7);
			y.push((0 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push((-4 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);

		} else if (this._boatShape === 'TRI') {
			// Width
			x.push(this.WL_RATIO_COEFF * 0); // Bow, center hull
			// Starboard
			x.push(this.WL_RATIO_COEFF * (  0.3 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  0.6 * boatLength) / 7); // Arm, front, starboard, inside
			x.push(this.WL_RATIO_COEFF * (  1.6 * boatLength) / 7); // Arm, front, starboard, outside
			x.push(this.WL_RATIO_COEFF * (  1.8 * boatLength) / 7); // Outrigger bow
			x.push(this.WL_RATIO_COEFF * (    2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (    2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  1.9 * boatLength) / 7); // Outrigger transom, ext
			x.push(this.WL_RATIO_COEFF * (  1.7 * boatLength) / 7); // Outrigger transom, int
			x.push(this.WL_RATIO_COEFF * (  1.6 * boatLength) / 7); // Arm, back, starboard, outside
			x.push(this.WL_RATIO_COEFF * (  0.6 * boatLength) / 7); // Arm, back, starboard, inside
			x.push(this.WL_RATIO_COEFF * (  0.3 * boatLength) / 7); // Main hull, transom starboard,
			// Port
			x.push(this.WL_RATIO_COEFF * ( -0.3 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( -0.6 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( -1.6 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( -1.7 * boatLength) / 7); // Outrigger transom, int
			x.push(this.WL_RATIO_COEFF * ( -1.9 * boatLength) / 7); // Outrigger transom, ext
			x.push(this.WL_RATIO_COEFF * (   -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (   -2 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * ( -1.8 * boatLength) / 7); // Outrigger bow
			x.push(this.WL_RATIO_COEFF * ( -1.6 * boatLength) / 7); // Arm, front, starboard, outside
			x.push(this.WL_RATIO_COEFF * ( -0.6 * boatLength) / 7); // Arm, front, starboard, inside
			x.push(this.WL_RATIO_COEFF * ( -0.3 * boatLength) / 7);

			// Length
			y.push((-4 * boatLength) / 7); // Bow
			// Starboard
			y.push((-3 * boatLength) / 7);
			y.push((-1 * boatLength) / 7); // Starboard arm, front
			y.push((-1 * boatLength) / 7); // Starboard arm, front, outrigger
			y.push((-2.6 * boatLength) / 7); // Starboard outrigger bow
			y.push((-1.5 * boatLength) / 7);
			y.push(( 1.5 * boatLength) / 7);
			y.push(( 2.5 * boatLength) / 7); // Starboard transom, ext
			y.push(( 2.5 * boatLength) / 7); // Starboard transom, ext
			y.push(( 1 * boatLength) / 7); // Starboard arm, back, outrigger
			y.push(( 1 * boatLength) / 7); // Starboard arm, hull
			y.push(( 3 * boatLength) / 7);
			// Port
			y.push(( 3 * boatLength) / 7);
			y.push(( 1 * boatLength) / 7);
			y.push(( 1 * boatLength) / 7);
			y.push(( 2.5 * boatLength) / 7);
			y.push(( 2.5 * boatLength) / 7);
			y.push(( 1.5 * boatLength) / 7);
			y.push((-1.5 * boatLength) / 7);
			y.push((-2.6 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push((-3 * boatLength) / 7);
		} else if (this._boatShape === 'PLANE') {
			// Width
			x.push(this.WL_RATIO_COEFF * 0); // Nose
			// Starboard
			x.push(this.WL_RATIO_COEFF * (  0.3 * boatLength) / 7);
			x.push(this.WL_RATIO_COEFF * (  0.6 * boatLength) / 7); // Wing, front, starboard, inside
			x.push(this.WL_RATIO_COEFF * (  4 * boatLength) / 7); // Wing, front, starboard, outside
			x.push(this.WL_RATIO_COEFF * (  4 * boatLength) / 7); // Wing, outside, back
			x.push(this.WL_RATIO_COEFF * (  0.6 * boatLength) / 7); // Wing, back, starboard, inside
			x.push(this.WL_RATIO_COEFF * (  0.3 * boatLength) / 7); // Main hull, transom starboard,
			x.push(this.WL_RATIO_COEFF * (    2 * boatLength) / 7); // Main hull, back wing, front
			x.push(this.WL_RATIO_COEFF * (    2 * boatLength) / 7); // Main hull, back wing, back, ext
			x.push(this.WL_RATIO_COEFF * (  0.1 * boatLength) / 7); // Main hull, back wing, back, int
			// Port
			x.push(this.WL_RATIO_COEFF * ( -0.1 * boatLength) / 7); // Main hull, back wing, back, int
			x.push(this.WL_RATIO_COEFF * (   -2 * boatLength) / 7); // Main hull, back wing, back, ext
			x.push(this.WL_RATIO_COEFF * (   -2 * boatLength) / 7); // Main hull, back wing, front
			x.push(this.WL_RATIO_COEFF * ( -0.3 * boatLength) / 7); // Main hull, transom starboard,
			x.push(this.WL_RATIO_COEFF * ( -0.6 * boatLength) / 7); // Wing, back, starboard, inside
			x.push(this.WL_RATIO_COEFF * ( -4 * boatLength) / 7); // Outrigger bow
			x.push(this.WL_RATIO_COEFF * ( -4 * boatLength) / 7); // Arm, front, starboard, outside
			x.push(this.WL_RATIO_COEFF * ( -0.6 * boatLength) / 7); // Arm, front, starboard, inside
			x.push(this.WL_RATIO_COEFF * ( -0.3 * boatLength) / 7);

			// Length
			y.push((-4 * boatLength) / 7); // Nose
			// Starboard
			y.push((-3 * boatLength) / 7);
			y.push((-2 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push(( 0 * boatLength) / 7);
			y.push((-0.5 * boatLength) / 7);
			y.push(( 1.8 * boatLength) / 7);
			y.push(( 2.5 * boatLength) / 7);
			y.push(( 3 * boatLength) / 7);
			y.push(( 2.8 * boatLength) / 7);
			// Port
			y.push(( 2.8 * boatLength) / 7);
			y.push(( 3 * boatLength) / 7);
			y.push(( 2.5 * boatLength) / 7);
			y.push(( 1.8 * boatLength) / 7);
			y.push((-0.5 * boatLength) / 7);
			y.push(( 0 * boatLength) / 7);
			y.push((-1 * boatLength) / 7);
			y.push((-2 * boatLength) / 7);
			y.push((-3 * boatLength) / 7);
		}
		let xPoints = [];
		let yPoints = [];

		// Rotation matrix:
		// | cos(alpha)  -sin(alpha) |
		// | sin(alpha)   cos(alpha) |
		// The center happens to be the middle of the boat.

		let center = this.getCanvasCenter();
		let ptX = center.x;
		let ptY = center.y;

		for (let i=0; i<x.length; i++) { // Rotation
			let dx = x[i] * Math.cos(Math.toRadians(trueHeading)) + (y[i] * (-Math.sin(Math.toRadians(trueHeading))));
			let dy = x[i] * Math.sin(Math.toRadians(trueHeading)) + (y[i] *   Math.cos(Math.toRadians(trueHeading)));
			xPoints.push(Math.round(ptX + dx));
			yPoints.push(Math.round(ptY + dy));
		}
		context.fillStyle = this.celestialSphereColorConfig.boatFillColor;
		context.beginPath();
		context.moveTo(xPoints[0], yPoints[0]);
		for (let i=1; i<xPoints.length; i++) {
			context.lineTo(xPoints[i], yPoints[i]);
		}
		context.closePath();
		// context.fill();
		context.strokeStyle = this.celestialSphereColorConfig.boatOutlineColor;
		context.lineWidth = 2;
		context.stroke();
	}

	drawCelestialSphere() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.celestialSphereColorConfig = CelestialSphere.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width * this._zoom, this.height * this._zoom);

		let radius = Math.min(this.width, this.height) * 0.99 * this._zoom / 2;

		// Set the canvas size from its container.
		this.canvas.width = this.width * this._zoom;
		this.canvas.height = this.height * this._zoom;

		context.beginPath();
		// White BG
		context.fillStyle = this.celestialSphereColorConfig.bgColor;
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius, 0, 2 * Math.PI, false);
		context.fill();
		context.closePath();
		// 2 circles for LHA / Heading
		context.beginPath();
		context.lineWidth = 1;
		context.strokeStyle = this.celestialSphereColorConfig.ticksColor;
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.98, 0, 2 * Math.PI, false);
		context.stroke();
		context.closePath();

		context.beginPath();
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92, 0, 2 * Math.PI, false); // This one is the "horizon" (pole abaisse)
		context.stroke();
		context.closePath();


		if (true) { // this._type === MapType.STARFINDER_TYPE) { // OPTION StarFinder
			// Major ticks
			context.beginPath();
			for (let i = 0; i < 360; i++) {
				if (i % this.majorTicks === 0) {
					let currentAngle = - Math.toRadians(i - ((this.observerLatitude >= 0 ? 1 : -1) * this.LHAAries));
					let xFrom = (this.canvas.width / 2) - ((radius * 0.98) * Math.cos(currentAngle));
					let yFrom = (this.canvas.height / 2) - ((radius * 0.98) * Math.sin(currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * 0.92) * Math.cos(currentAngle));
					let yTo = (this.canvas.height / 2) - ((radius * 0.92) * Math.sin(currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
			}
			context.lineWidth = 1;
			context.strokeStyle = this.celestialSphereColorConfig.ticksColor;
			context.stroke();
			context.closePath();

			// Minor ticks
			if (this.minorTicks > 0) {
				context.beginPath();
				for (let i = 0; i < 360; i += this.minorTicks) {
					let _currentAngle = - Math.toRadians(i - ((this.observerLatitude >= 0 ? 1 : -1) * this.LHAAries));

					let xFrom = (this.canvas.width / 2) - ((radius * 0.98) * Math.cos(_currentAngle));
					let yFrom = (this.canvas.height / 2) - ((radius * 0.98) * Math.sin(_currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
					let yTo = (this.canvas.height / 2) - ((radius * 0.95) * Math.sin(_currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
				context.lineWidth = 1;
				context.strokeStyle = this.celestialSphereColorConfig.ticksColor;
				context.stroke();
				context.closePath();
			}

			// LHA values
			context.beginPath();
			for (let i = 0; i < 360; i++) {
				if (i % this.majorTicks === 0) {
					context.save();
					context.translate(this.canvas.width / 2, (this.canvas.height / 2)); // canvas.height);
					let __currentAngle = - Math.toRadians(i - ((this.observerLatitude >= 0 ? 1 : -1) * this.LHAAries));
					context.rotate(__currentAngle - Math.PI);
					context.font = "bold " + Math.round(10 * this._zoom) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = this.celestialSphereColorConfig.tickLabelsColor;
					let lha = (this.observerLatitude >= 0 || i === 0 ? i : (360 - i));
					let str = lha.toString() + '°';
					let len = context.measureText(str).width;
					context.fillText(str, -len / 2, (-(radius * 0.98) + 10));
					// context.lineWidth = 1;
					// context.strokeStyle = 'black';
					// context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
					context.restore();
				}
			}
			context.closePath();
		}

		// Full Sphere
		// Sky BG Color
		context.beginPath();
		context.fillStyle = this.celestialSphereColorConfig.skyBGColor;
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92, 0, 2 * Math.PI, false); // This one is the "horizon" (pole abaisse)
		context.fill();
		context.closePath();

		// Also Manage Heading...
		// Compass / Heading
		context.beginPath();
		for (let i = 0; i < 360; i+=11.25) { // N, NNE, NE ,etc
			let currentAngle = Math.toRadians(i /* - (this._use_heading ? this._heading : 0)*/);
			let xFrom = (this.canvas.width / 2) + ((radius * 0.92) * Math.cos(currentAngle));
			let yFrom = (this.canvas.height / 2) + ((radius * 0.92) * Math.sin(currentAngle));
			let xTo = (this.canvas.width / 2) + ((radius * 0.88) * Math.cos(currentAngle));
			let yTo = (this.canvas.height / 2) + ((radius * 0.88) * Math.sin(currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 2;
		context.strokeStyle = this.celestialSphereColorConfig.headingTicksColor;
		context.stroke();
		context.closePath();

		// Compass Values
		context.beginPath();
		for (let i = 0; i < 360; i+=22.5) {
			context.save();
			context.translate(this.canvas.width / 2, (this.canvas.height / 2)); // canvas.height);
			let __currentAngle = /*-*/ Math.toRadians(i /* - (this._use_heading ? this._heading : 0)*/);
			context.rotate(__currentAngle); //  - Math.PI);
			context.font = "bold " + Math.round(12 * this._zoom) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = this.celestialSphereColorConfig.cardPointLabelsColor;
			// let hour = (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE  || i === 0 ? i : (24 - i)); // TODO Fix that
			let label = CardinalPoints[i / 22.5];
			let str = label;
			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .88) + 10));
			// context.lineWidth = 1;
			// context.strokeStyle = 'black';
			// context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();

		// Full Sphere Celestial equator
		context.beginPath();
		context.lineWidth = 2;
		context.strokeStyle = this.celestialSphereColorConfig.skyGridColor;
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92 / 1, 0, 2 * Math.PI, false);
		context.stroke();
		context.closePath();

		// Declinations
		context.save();
		context.beginPath();
		context.setLineDash([5]);
		context.lineWidth = 1; // 0.5;
		context.strokeStyle = this.celestialSphereColorConfig.declinationCircleColor;
		for (let i=10; i<90; i+=10) { // [80..10]
			if (i === 0) {
				continue;
			}
			context.beginPath();
			let r = Math.round((radius * 0.92) * (90 - i) / 90);
			context.arc(this.canvas.width / 2, this.canvas.height / 2, r, 0, 2 * Math.PI, false);
			context.stroke();
			context.closePath();
		}
		context.restore();

		if (this._use_heading) {
			this.drawBoat(context, this._heading);
		}

		if (this._withStars || this._withConstellations) {
			this.drawStars(context, radius * 0.92);
		}

		if (this._withWanderingBodies) {
			this.drawWanderingBodies(context, radius * 0.92);
		}

		// Display LHA Aries as text, and position
		let strAries = Utilities.decToSex(this.LHAAries);
		context.fillStyle = this.celestialSphereColorConfig.ariesLabelColor;
		context.font = "bold 16px Arial"; // "bold 40px Arial"
		context.fillText('LHA Aries: ' + strAries, 10, 18);

		context.fillText('From position ', 10, 18 * 2);
		context.fillText(Utilities.decToSex(this.observerLatitude, "NS"), 10, 18 * 3);
		context.fillText(Utilities.decToSex(this.observerLongitude, "EW"), 10, 18 * 4);

		if (this.useHeading) {
			context.fillText(`HDG: ${this._heading.toFixed(0)}\xba true`, 10, 18 * 5);
		}

		// Zenith info
		let str = "Zenith is in the center.";
		let len = context.measureText(str).width;
		context.fillText(str, (this.canvas.width) - 5 - (len), 18);

		// After (callback), like Plot Stars & Constellations ?
		if (this.doAfter !== undefined) {
			// console.log("Calling doAfter callback function");
			this.doAfter(this, context);
		// } else {
		// 	console.log("No doAfter callback function defined");
		}

	}

	static findStar(starArray, starName) {
		let star = {};
		for (let i=0; i<starArray.length; i++) {
			if (starArray[i].name === starName) {
				return starArray[i];
			}
		}
		return star;
	}

	drawStars(context, radius) {
		for (let i=0; i<constellations.length; i++) {
			// Constellation?
			if (this._withConstellations) {
				let constellation = constellations[i].lines;
				for (let l = 0; l < constellation.length; l++) {
					let starFrom = CelestialSphere.findStar(constellations[i].stars, constellations[i].lines[l].from);
					let starTo = CelestialSphere.findStar(constellations[i].stars, constellations[i].lines[l].to);
					if (starFrom !== undefined && starTo !== undefined) {
						context.beginPath();
						let dec = starFrom.d; // * (this.observerLatitude >= 0 ? 1 : -1);
						let ra = starFrom.ra;
						let lng = (360 - (ra * 360 / 24));
						lng += (/*this._hemisphere * */this.LHAAries);
						if (lng > 180) {
							lng -= 360;
						}
						// Sight Reduction
						let sr1 = sightReduction(this.observerLatitude, this.observerLongitude, lng, dec);
						let p1 = this.plotOnSphere(sr1.alt, sr1.Z /* - (this.useHeading ? this.heading : 0)*/, radius);
						dec = starTo.d; // * (this.observerLatitude >= 0 ? 1 : -1);
						ra = starTo.ra;
						lng = (360 - (ra * 360 / 24));
						lng += (/*this._hemisphere * */this.LHAAries);
						if (lng > 180) {
							lng -= 360;
						}
						// Sight Reduction
						let sr2 = sightReduction(this.observerLatitude, this.observerLongitude, lng, dec);
						let p2 = this.plotOnSphere(sr2.alt, sr2.Z /*- (this.useHeading ? this.heading : 0)*/, radius); // this.plotCoordinates(dec, lng, radius);
						context.strokeStyle = this.celestialSphereColorConfig.constellationLineColor;
						context.lineWidth = 0.5;
						context.moveTo((this.canvas.width / 2) - p1.x, (this.canvas.height / 2) + p1.y);
						context.lineTo((this.canvas.width / 2) - p2.x, (this.canvas.height / 2) + p2.y);

						context.stroke();
						context.closePath();
					}
				}
				if (this._constellationNames) {
					// Calculate the center of the constellation
					let minD = undefined, maxD = undefined, minRA = undefined, maxRA = undefined;
					for (let s = 0; s < constellations[i].stars.length; s++) {
						if (s === 0) {
							minD = constellations[i].stars[s].d;
							maxD = constellations[i].stars[s].d;
							minRA = constellations[i].stars[s].ra;
							maxRA = constellations[i].stars[s].ra;
						} else {
							minD = Math.min(constellations[i].stars[s].d, minD);
							maxD = Math.max(constellations[i].stars[s].d, maxD);
							minRA = Math.min(constellations[i].stars[s].ra, minRA);
							maxRA = Math.max(constellations[i].stars[s].ra, maxRA);
						}
					}
					let centerDec = (this.observerLatitude >= 0 ? 1 : -1) * (maxD + minD) / 2;
					let centerRA = (maxRA + minRA) / 2;
					let lng = (360 - (centerRA * 360 / 24));
					lng += (/*this._hemisphere * */this.LHAAries);
					if (lng > 180) {
						lng -= 360;
					}
        			let sr = sightReduction(this.observerLatitude, this.observerLongitude, lng, centerDec);
					// let p = this.plotCoordinates(centerDec, lng, radius);
					let p = this.plotOnSphere(sr.alt, sr.Z /* - (this.useHeading ? this.heading : 0)*/, radius);

					context.font = "bold " + Math.round(10 * this._zoom) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = this.celestialSphereColorConfig.constellationNameColor;
					let str = constellations[i].name;
					let len = context.measureText(str).width;
					context.fillText(str, (this.canvas.width / 2) - p.x - (len / 2), (this.canvas.height / 2) + p.y - 2);
				}
			}

			// Stars
			if (this._withStars) {
				for (let s = 0; s < constellations[i].stars.length; s++) {
					let dec = constellations[i].stars[s].d; // * (this.observerLatitude >= 0 ? 1 : -1);
					let ra = constellations[i].stars[s].ra;
					let sha = (360 - (ra * 360 / 24)); //
					let gha = sha + (/*this._hemisphere * */this.LHAAries);
					if (gha > 180) {
						gha -= 360;
					}
					// Sight Reduction !
        			let sr = sightReduction(this.observerLatitude, this.observerLongitude, gha, dec);

					if (false) {
						console.log(`${constellations[i].name} - ${constellations[i].stars[s].name} = He: ${sr.alt}, Z: ${sr.Z}`); //  { he: srSun.alt, z: srSun.Z };
						// RA, Dec, SHA (AHso)
						console.log(`\t  RA: ${ra}, Dec: ${decToSex(dec, "NS")}, SHA: ${decToSex(sha)}, GHA: ${decToSex(sha + this.LHAAries)} (${decToSex(gha, "EW")})`);
					}

					if (sr.alt >= 0) {
						// let p = this.plotCoordinates(dec, gha, radius);
						let p = this.plotOnSphere(sr.alt, sr.Z /*- (this.useHeading ? this.heading : 0)*/, radius);
						context.beginPath();
						context.fillStyle = this.celestialSphereColorConfig.starColor;
						const starRadius = 2;
						context.arc((this.canvas.width / 2) - p.x, (this.canvas.height / 2) + p.y, starRadius, 0, 2 * Math.PI, false);
						context.fill();
						context.strokeStyle = this.celestialSphereColorConfig.constellationLineColor;
						context.lineWidth = 0.5;
						context.stroke();

						if (constellations[i].stars[s].name.charAt(0) === constellations[i].stars[s].name.charAt(0).toUpperCase() && this._starNames) { // Star name, starts with uppercase
							context.font = "bold " + Math.round(10 * this._zoom) + "px Arial"; // Like "bold 15px Arial"
							context.fillStyle = this.celestialSphereColorConfig.starNameColor;
							let str = constellations[i].stars[s].name;
							let len = context.measureText(str).width;
							context.fillText(str, (this.canvas.width / 2) - p.x - (len / 2), (this.canvas.height / 2) + p.y - 2);
						}
						context.closePath();
					}
				}
			}
		}
	}

	// AKA AHso
	static findGHAAries(wBodies) {
		let ghaA = undefined;
		for (let i=0; i<wBodies.length; i++) {
			if (wBodies[i].name === "aries") {
				return wBodies[i].gha;
			}
		}
		return ghaA;
	}

	/*
	 * Sun     \u2609
	 * Moon    \u263D, \u263E
	 * Venus   \u2640
	 * Mars    \u2642
	 * Jupiter \u2643
	 * Saturn  \u2644
	 *
	 * Aries (Gamma) \u03b3
	 */
	static findSymbol(bodyName) {
		switch (bodyName.toUpperCase()) {
			case 'ARIES':
				return '\u03b3';
			case 'SUN':
				return '\u2609';
			case 'MOON':
				return '\u263D';
			case 'VENUS':
				return '\u2640';
			case 'MARS':
				return '\u2642';
			case 'JUPITER':
				return '\u2643';
			case 'SATURN':
				return '\u2644';
			default:
				return bodyName;
		}
	}

	drawWanderingBodies(context, radius) {
		if (this._wanderingBodiesData !== undefined) {
			let self = this;
			let ghaAries = CelestialSphere.findGHAAries(this._wanderingBodiesData);
			this._wanderingBodiesData.forEach((body) => {
				let dec = body.decl; // * (this.observerLatitude >= 0 ? 1 : -1);
				let lng = body.gha - ghaAries;
				lng += (/*this._hemisphere * */self.LHAAries);
				if (lng > 180) {
					lng -= 360;
				}
				// Sight Reduction !
				let sr = sightReduction(this.observerLatitude, this.observerLongitude, lng, dec);
				// console.log(`${body.name} = He: ${sr.alt}, Z: ${sr.Z}`); //  { he: srSun.alt, z: srSun.Z };
				if (true || sr.alt >= 0) {
					let p = this.plotOnSphere(sr.alt, sr.Z /*- (this.useHeading ? this.heading : 0)*/, radius);
					context.beginPath();
					context.fillStyle = this.celestialSphereColorConfig.wanderingBodiesColor;
					const bodyRadius = 4;
					context.arc((self.canvas.width / 2) - p.x, (self.canvas.height / 2) + p.y, bodyRadius, 0, 2 * Math.PI, false);
					context.fill();
					context.strokeStyle = this.celestialSphereColorConfig.starCircleColor;
					context.lineWidth = 0.5;
					context.stroke();

					context.font = "bold " + Math.round(30 /*24*/) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = this.celestialSphereColorConfig.wanderingBodiesNameColor;
					let str = CelestialSphere.findSymbol(body.name);
					let len = context.measureText(str).width;
					context.fillText(str, (self.canvas.width / 2) - p.x - (len / 2), (self.canvas.height / 2) + p.y - 4);

					context.closePath();
				}
			});
		} else {
			console.log("No wandering bodies data available");
		}
	}

	plotCoordinates(lat, lng, radius) {
		let r = (((90 - lat) / 180) * radius);
		let xOffset = Math.round(r * Math.sin(Math.toRadians(lng))); // * (this.observerLatitude >= 0 ? 1 : -1);
		let yOffset = Math.round(r * Math.cos(Math.toRadians(lng)));
		return {x: xOffset, y: yOffset};
	}

	// Z: heading should already be taken in account
	plotOnSphere(alt, Z, radius) {
		let r = ((90 - alt) / 90) * radius;
		let xOffset = - Math.round(r * Math.sin(Math.toRadians(Z)));
		let yOffset = - Math.round(r * Math.cos(Math.toRadians(Z)));
		return {x: xOffset, y: yOffset};
	}

}

// Associate the tag and the class
window.customElements.define(CELESTIALSPHERE_TAG_NAME, CelestialSphere);
