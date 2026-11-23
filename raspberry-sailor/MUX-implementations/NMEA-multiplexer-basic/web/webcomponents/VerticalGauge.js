/**
 * @file VerticalGauge.js
 * @author Olivier LeDiouris
 * @version 1.0.0
 * @date Oct-2026
 *
 * Vertical Gauge WebComponent.
 */

const verticalGaugeVerbose = false;
const VERTICAL_GAUGE_TAG_NAME = 'vertical-gauge';

const verticalGaugeDefaultColorConfig = {
	bgColor: 'white',
	displayBackgroundGradient: {
		from: 'white', // 'black',
		to: 'white'  // 'gray'
	},
	gridColor: 'darkgray',
	displayColor: 'black',
	valueColor: 'black',
	valueFrameColor: 'orange',
	valueNbDecimal: 2,
	labelFont: 'Arial',
	valueFont: 'Courier New'
};

/* global HTMLElement */
class VerticalGauge extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",     // Integer. Canvas width
			"height",    // Integer. Canvas height
			"value",     // Float. Numeric value to display
			"label",     // String, like ALT (meters)
			"majortick", // Float. Major tick interval
			"minortick", // Float. Minor tick interval
			"fork"       // Float. Visible range
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a WebComponent Skeleton, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value = 0;
		this._width = 250;
		this._height = 100;
		this._label = "ALT (meters)";

        this._majortick = 5;   // Major tick interval
        this._minortick = 1;   // Minor tick interval
        this._fork = 10;       // Visible range


		this._previousClassName = "";
		this.verticalGaugeColorConfig = verticalGaugeDefaultColorConfig;

		if (verticalGaugeVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (verticalGaugeVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (verticalGaugeVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (verticalGaugeVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = parseFloat(newVal);
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "label":
				this._label = newVal;
				break;
			// case "from":
			// 	this._from = parseFloat(newVal);
			// 	break;
			// case "to":
			// 	this._to = parseFloat(newVal);
			// 	break;
			case "majortick":
				this._majortick = parseFloat(newVal);
				break;
			case "minortick":
				this._minortick = parseFloat(newVal);
				break;
			case "fork":
				this._fork = parseFloat(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (verticalGaugeVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (verticalGaugeVerbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint(); // Done in attributeChangedCallback
	}

	set width(val) {
		this.setAttribute("width", val);
	}

	set height(val) {
		this.setAttribute("height", val);
	}

	set label(val) {
		this.setAttribute("label", val);
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get value() {
		return this._value;
	}

	get width() {
		return this._width;
	}

	get height() {
		return this._height;
	}

	get label() {
		return this._label;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = verticalGaugeDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(VERTICAL_GAUGE_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--display-background-gradient-from':
											colorConfig.displayBackgroundGradient.from = value;
											break;
										case '--display-background-gradient-to':
											colorConfig.displayBackgroundGradient.to = value;
											break;
										case '--grid-color':
											colorConfig.gridColor = value;
											break;
										case '--display-color':
											colorConfig.displayColor = value;
											break;
										case '--value-frame-color':
											colorConfig.valueFrameColor = value;
											break;
										case '--value-color':
											colorConfig.valueColor = value;
											break;
										case '--value-nb-decimal':
											colorConfig.valueNbDecimal = value;
											break;
										case '--label-font':
											colorConfig.labelFont = value;
											break;
										case '--value-font':
											colorConfig.valueFont = value;
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
		this.drawVerticalGauge();
	}

	drawVerticalGauge() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.verticalGaugeColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		let scale = 1.0 * this.width / 200.0;

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		let grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, this.verticalGaugeColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.verticalGaugeColorConfig.displayBackgroundGradient.to); // 1  End
		context.fillStyle = grd;

		// Background
		VerticalGauge.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		// Major and minor ticks
		/*
			width="200"
			height="400"
			value="2.345"
			from="-10"
			to="50"
			majortick="5"
			minortick="1"
			fork="10"
		*/

		let bigTickLength = this.width / 5;
		let smallTickLength = this.width / 10;

		// Start in the middle. For a value like 2.345, what is the closest ?
		// Take the int part => 2.345 -> 2
		// for 2.345, tickOrd = 400 / 2
		// min = 2.345 - (fork / 2) -> ord = 0. Which is 2.345 - 5 = -2.655 -> ord = 0
		// max = 2.345 + (fork / 2) -> ord = 400. Which is 2.345 + 5 = 7.345 -> ord = 400
		// ord(value) = (value - min) * (height / fork)

		let intPart = Math.trunc(this.value);
		let minVisibleValue = this.value - (this._fork / 2.0);
		let maxVisibleValue = this.value + (this._fork / 2.0);
		let closestIntTickOrd = (intPart - minVisibleValue) * (this.height / this._fork);
		// Down
		let valueForTick = intPart;
		while (valueForTick >= minVisibleValue) {
			let ord = this.height - ((valueForTick - minVisibleValue) * (this.height / this._fork));
			if (valueForTick % this._majortick === 0) {
				// Major tick
				context.strokeStyle = this.verticalGaugeColorConfig.gridColor;
				context.beginPath();
				context.moveTo(this.width - bigTickLength, ord);
				context.lineTo(this.width, ord);
				context.stroke();
				// Label of the tick
				context.fillStyle = this.verticalGaugeColorConfig.displayColor;
				context.font = "bold " + Math.round(scale * 16) + "px " + this.verticalGaugeColorConfig.labelFont;
				let strVal = valueForTick.toFixed(0); // this.verticalGaugeColorConfig.valueNbDecimal);
				let metrics = context.measureText(strVal);
				let len = metrics.width;
				context.fillText(strVal, this.width - bigTickLength - len - 5, ord + 5);
			} else {
				// Minor tick
				context.strokeStyle = this.verticalGaugeColorConfig.gridColor;
				context.beginPath();
				context.moveTo(this.width - smallTickLength, ord);
				context.lineTo(this.width, ord);
				context.stroke();
			}
			valueForTick -= this._minortick;
		}
		// Up
		valueForTick = intPart + this._minortick;
		while (valueForTick <= maxVisibleValue) {
			let ord = this.height - ((valueForTick - minVisibleValue) * (this.height / this._fork));
			if (valueForTick % this._majortick === 0) {
				// Major tick
				context.strokeStyle = this.verticalGaugeColorConfig.gridColor;
				context.beginPath();
				context.moveTo(this.width - bigTickLength, ord);
				context.lineTo(this.width, ord);
				context.stroke();
				// Label of the tick
				context.fillStyle = this.verticalGaugeColorConfig.displayColor;
				context.font = "bold " + Math.round(scale * 16) + "px " + this.verticalGaugeColorConfig.labelFont;
				let strVal = valueForTick.toFixed(0); // this.verticalGaugeColorConfig.valueNbDecimal);
				let metrics = context.measureText(strVal);
				let len = metrics.width;
				context.fillText(strVal, this.width - bigTickLength - len - 5, ord + 5);
			} else {
				// Minor tick
				context.strokeStyle = this.verticalGaugeColorConfig.gridColor;
				context.beginPath();
				context.moveTo(this.width - smallTickLength, ord);
				context.lineTo(this.width, ord);
				context.stroke();
			}
			valueForTick += this._minortick;
		}

		// Label and Co
		context.fillStyle = this.verticalGaugeColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.verticalGaugeColorConfig.labelFont;
		context.fillText(this.label, 5, 18);
		// Value
		context.strokeStyle = this.verticalGaugeColorConfig.valueFrameColor;
		context.lineWidth = 2;
		context.beginPath();
		context.rect(0, (this.height / 2) - (scale * 20), this.width - smallTickLength, scale * 40);
		context.stroke();

		context.fillStyle = 'rgba(128, 128, 128, 0.75)'; // this.verticalGaugeColorConfig.bgColor;
		context.fillRect(2, (this.height / 2) - (scale * 20), this.width - smallTickLength - 2, scale * 40);

		context.fillStyle = this.verticalGaugeColorConfig.valueColor;

		context.font = "bold " + Math.round(scale * 40) + "px " + this.verticalGaugeColorConfig.valueFont;
		let strVal = this._value.toFixed(this.verticalGaugeColorConfig.valueNbDecimal) + " >";
		let metrics = context.measureText(strVal);
		let len = metrics.width;
		let lineHeight = metrics.actualBoundingBoxAscent + metrics.actualBoundingBoxDescent;

		if (verticalGaugeVerbose) {
			console.log(`>>> VerticalGauge: value=${this._value}, strVal=${strVal}, len=${len}, canvas.width=${this.canvas.width}, canvas.height=${this.canvas.height}, scale=${scale}`);
		}
		context.fillText(strVal,
			             this.canvas.width - len - smallTickLength - 2,
						 (this.canvas.height / 2) + (lineHeight/*60*/ * scale / 2));
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
}

// Associate the tag and the class
window.customElements.define(VERTICAL_GAUGE_TAG_NAME, VerticalGauge);