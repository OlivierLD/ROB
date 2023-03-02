const rangeVerbose = false; // For dev/debug
const COLORED_RANGE_TAG_NAME = 'colored-scale';

const SCALE_VALUES = [0,1,2,3,4,5,6,7,8,9,10,11,12];
// See https://htmlcolorcodes.com/
const SCALE_COLORS = [
	{ index:  0, color: 'rgb(232, 246, 243)' },
	{ index:  1, color: 'rgb(212, 230, 241)' },
	{ index:  2, color: 'rgb(133, 193, 233)' },
	{ index:  3, color: 'rgb( 93, 173, 226)' },
	{ index:  4, color: 'rgb( 26, 188, 156)' },
	{ index:  5, color: 'rgb( 29, 131,  72)' },
	{ index:  6, color: 'rgb(241, 196,  15)' },
	{ index:  7, color: 'rgb(231,  76,  69)' },
	{ index:  8, color: 'rgb(192,  57,  43)' },
	{ index:  9, color: 'rgb(123,  36,  28)' },
	{ index: 10, color: 'rgb(118,  68, 138)' },
	{ index: 11, color: 'rgb( 91,  44, 111)' },
	{ index: 12, color: 'rgb( 33,  47,  61)' }
];

const coloredRangeColorConfigDefault = {
	bgColor: 'black',
	borderColor: 'white',
	withGradient: true,
	displayBackgroundGradient: {
		from: 'black',
		to: 'LightGrey'
	},
	withDisplayShadow: true,
	shadowColor: 'rgba(0, 0, 0, 0.75)',
	valueColor: 'LightRed',
	valueOutlineColor: 'black',
	valueNbDecimal: 2,
	font: 'Arial' /* 'Source Code Pro' */
};

/* global HTMLElement */
class ColoredScale extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"orientation",  // String, vertical or horizontal (case insensitive)
			"value",        // Float. True Wind Spped in knots, [0...[
			"unit",         // String. m, sm, ', Celsius, etc.
			"scale"         // Integer. default 1. Value multiplier. If scale = 2 and value = 3, then the step is 1
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Colored Scale, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       =   0;
		this._width       =  50;
		this._height      = 150;
		this._orientation = "VERTICAL";
		this._unit        = "m";
		this._scale       =   1;

		this._previousClassName = "";
		this.scaleColorConfig = coloredRangeColorConfigDefault; // Init

		if (rangeVerbose) {
			console.log("Value in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (rangeVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (rangeVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (rangeVerbose) {
			console.log("attributeChangedCallback invoked on '" + attrName + "' from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = parseFloat(newVal);
				break;
			case "unit":
				this._unit = newVal;
				break;
			case "scale":
				this._scale = parseInt(newVal);
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "orientation":
				this._orientation = newVal.toUpperCase();
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (rangeVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (rangeVerbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint();
	}
	set width(val) {
		this.setAttribute("width", val);
	}
	set unit(val) {
		this.setAttribute("unit", val);
	}
	set scale(val) {
		this.setAttribute("scale", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set orientation(val) {
		this.setAttribute("orientation", val);
	}
	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get value() {
		return this._value;
	}
	get unit() {
		return this._unit;
	}
	get scale() {
		return this._scale;
	}
	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get orientation() {
		return this._orientation;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	getColorConfig(cssClassNames) {
		let colorConfig = coloredRangeColorConfigDefault;
		let classes = cssClassNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
	      // console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r=0; document.styleSheets[s].cssRules !== null && r<document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(COLORED_RANGE_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--with-gradient':
											colorConfig.withGradient = (value === 'true');
											break;
										case '--display-background-gradient-from':
											colorConfig.displayBackgroundGradient.from = value;
											break;
										case '--display-background-gradient-to':
											colorConfig.displayBackgroundGradient.to = value;
											break;
										case '--with-display-shadow':
											colorConfig.withDisplayShadow = (value === 'true');
											break;
										case '--shadow-color':
											colorConfig.shadowColor = value;
											break;
										case '--outline-color':
											colorConfig.outlineColor = value;
											break;
										case '--value-color':
											colorConfig.valueColor = value;
											break;
										case '--value-outline-color':
											colorConfig.valueOutlineColor = value;
											break;
										case '--value-nb-decimal':
											colorConfig.valueNbDecimal = value;
											break;
										case '--font':
											colorConfig.font = value;
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

	repaint() {
		this.drawColoredScale(this._value);
	}

	drawColoredScale(value) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
	//	console.log("Reloading CSS");
			try {
				this.scaleColorConfig = this.getColorConfig(currentStyle);
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

		// Find current scale degree
		let currentStep = 12;
		for (let i=0; i<SCALE_VALUES.length;i++) {
			if ((SCALE_VALUES[i] * this._scale) > value) {
				currentStep = i - 1;
				// console.log(`Value ${value} => Step ${currentStep}`);
				break;
			}
		}
		let labelFontSize = 20; // Math.round(Math.min(oneElementHeight, oneElementWidth)) / 2;
		// 14: 13 scale degrees + room for the label.
		let oneElementWidth = (this._orientation == "VERTICAL") ? this._width : this._width / 13; 
		let oneElementHeight = (this._orientation == "VERTICAL") ? this._height / 14  : this._height - labelFontSize; // 20: label font size, hard-coded...

		// let labelFontSize = Math.round(Math.min(oneElementHeight, oneElementWidth)) / 2;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		if (this.scaleColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, this.height);
			grd.addColorStop(0, this.scaleColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.scaleColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;

		} else {
			//context.fillStyle = "#ffffff";
			context.fillStyle = this.scaleColorConfig.bgColor;
			//context.fillStyle = "transparent";
		}
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		// Scale Elements
		let padding = 2;
		context.lineWidth = 1;
		if (this._orientation == "VERTICAL") {
			for (let i = 0; i < SCALE_VALUES.length; i++) {
				let topLeftX = padding;
				let topLeftY = this._height - ((i + 1) * oneElementHeight) + padding;
				let rectWidth = this._width - (2 * padding);
				let rectHeight = oneElementHeight - (2 * padding);
				context.beginPath();
				context.strokeStyle = this.scaleColorConfig.outlineColor;
				context.roundRect(topLeftX, topLeftY, rectWidth, rectHeight, 5);
				if (currentStep > (i - 1)) {
					context.fillStyle = SCALE_COLORS[i].color;
					context.fill();
					// Scale value
					let scale = (i * this._scale).toFixed(0);
					let fontSize = Math.round(oneElementHeight);
					context.font = `bold ${fontSize}px ${this.scaleColorConfig.font}`;
					let metrics = context.measureText(scale);
					let len = metrics.width;
					let xPos = (this.canvas.width / 2) - (len / 2);
					let yPos = topLeftY + (rectHeight / 2) + (fontSize / 2) - (2 * padding);
					context.fillStyle = this.scaleColorConfig.valueColor;
					context.fillText(scale, xPos, yPos);
					context.strokeStyle = this.scaleColorConfig.valueOutlineColor;
					context.strokeText(scale, xPos, yPos); // Outlined
				} else {
					context.fillStyle = null;
				}
				context.stroke();
				context.closePath();
			}
		} else { // HORIZONTAL
			for (let i = 0; i < SCALE_VALUES.length; i++) {
				let topLeftX = (i * oneElementWidth) + padding;
				let topLeftY = (2 * padding) + labelFontSize;
				let rectWidth = oneElementWidth - (2 * padding);
				let rectHeight = oneElementHeight - (2 * padding);
				context.beginPath();
				context.strokeStyle = this.scaleColorConfig.outlineColor;
				context.roundRect(topLeftX, topLeftY, rectWidth, rectHeight, 5);
				if (currentStep > (i - 1)) {
					context.fillStyle = SCALE_COLORS[i].color;
					context.fill();
					// Scale value
					let scale = (i * this._scale).toFixed(0);
					let fontSize = Math.round(oneElementWidth);
					context.font = `bold ${fontSize}px ${this.scaleColorConfig.font}`;
					let metrics = context.measureText(scale);
					let len = metrics.width;
					let xPos = topLeftX + (rectWidth / 2) - (len / 2);
					let yPos = topLeftY + (rectHeight / 2) + (fontSize / 2) - (2 * padding);
					context.fillStyle = this.scaleColorConfig.valueColor;
					context.fillText(scale, xPos, yPos);
					context.strokeStyle = this.scaleColorConfig.valueOutlineColor;
					context.strokeText(scale, xPos, yPos); // Outlined
				} else {
					context.fillStyle = null;
				}
				context.stroke();
				context.closePath();
			}
		}

		// Scale Value
		let text = this._value.toFixed(this.scaleColorConfig.valueNbDecimal) + ` ${this._unit}`;
		context.font = `bold ${labelFontSize}px ${this.scaleColorConfig.font}`;
		let metrics = context.measureText(text);
		let len = metrics.width;
		context.lineWidth = 1;

		let xPos = padding; // (this.canvas.width) - (len);
		let yPos = labelFontSize; // + padding;
		// Horizontal and Vertical, same coordinates (top-left)
		context.beginPath();
		context.fillStyle = this.scaleColorConfig.valueColor;
		context.fillText(text, xPos, yPos);
		context.strokeStyle = this.scaleColorConfig.valueOutlineColor;
		context.strokeText(text, xPos, yPos); // Outlined
		context.closePath();
	}
}

// Associate the tag and the class
window.customElements.define(COLORED_RANGE_TAG_NAME, ColoredScale);
