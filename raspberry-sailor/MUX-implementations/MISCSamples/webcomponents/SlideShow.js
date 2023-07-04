// "use strict";

// import slideShowCSS from './css/SlideShow.css'; TODO See why that thing does not work...

const slideshowVerbose = false;
const SLIDE_SHOW_TAG_NAME = 'slide-show';

/**
 * This is a example, using/requiring nested tags
 * <slide-show>
 *   <slide-show-image></slide-show-image>
 *   <slide-show-image></slide-show-image>
 * </slide-show>
 *
 * Also supports different ratios for the images of the same slideshow.
 */

/* global HTMLElement */

// See https://www.w3schools.com/jsref/prop_node_nodetype.asp
const ELEMENT_TYPE = 1;
const ATTRIBUTE_TYPE = 2;
const TEXT_TYPE = 3;
const COMMENT_TYPE = 8;
const NODE_TYPE = [
	{ type: 'element', val: ELEMENT_TYPE },
	{ type: 'attribute', val:  ATTRIBUTE_TYPE },
	{ type: 'text', val:  TEXT_TYPE },
	{ type: 'comment', val: COMMENT_TYPE }
];

function nodeTypeFromVal(val) {
	let typeName = 'Unknown';
	for (let i=0; i<NODE_TYPE.length; i++) {
		if (NODE_TYPE[i].val === val) {
			typeName = NODE_TYPE[i].type;
			break;
		}
	}
	return typeName;
}

/**
 * Sample Slide Show
 */
class SlideShow extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",
			"height",
			"slideclick"
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.

		// slideShowCSS.use();

		// create and append an element, a div.

		this.slideshowContainer = document.createElement("div");
		this.slideshowContainer.setAttribute('class', 'slideshow-container');
		this._shadowRoot.appendChild(this.slideshowContainer);

		this.dots = document.createElement("div");
		this.dots.setAttribute('class', 'dots');
		this._shadowRoot.appendChild(this.dots);

		this.slideIndex = 1;

		this.widgetColor = 'cyan'; // TODO Make it a CSS rule

		// <link href="./css/SlideShow.css" rel="stylesheet" type="text/css">
		// let link = document.createElement("link");
		// link.setAttribute('rel', 'stylesheet');
		// link.setAttribute('type', 'text/css');
		// link.setAttribute('href', './css/SlideShow.css');
		// this._shadowRoot.appendChild(link);

		// TODO Isn't there a better way to define the styles? (slideShowCSS.use();)
		let cssClasses = document.createElement("style");
		cssClasses.innerHTML =
				'.the-slides {' +
				'   display: none;' +
				'}' +
				'\n' +
				'/* Slideshow container */' +
				'.slideshow-container {' +
				'   max-width: 1000px;' +
				'   position: relative;' +
				'   margin: auto;' +
				'}' +
				'\n' +
				'/* Next & previous buttons */' +
				'.prev, .next {' +
				'   cursor: pointer;' +
				'   position: absolute;' +
				'   top: 50%;' +
				'   width: auto;' +
				'   padding: 8px 16px 8px 12px;' +
				'   margin-top: -22px;' +
				'   color: '  + this.widgetColor + ';' +
				'   font-weight: bold;' +
				'   font-size: 18px;' +
				'   transition: 0.6s ease;' +
				'   border-radius: 24px;' +
				'}' +
				'\n' +
				'/* Position the "next button" to the right */' +
				'.next {' +
				'   right: 0;' +
				'   padding: 8px 12px 8px 16px;' +
				'}' +
				'\n' +
				'/* On hover, add a black background color with a little bit see-through */' +
				'.prev:hover, .next:hover {' +
				'   background-color: rgba(0, 0, 0, 0.6);' +
				'   border: 1px solid ' + this.widgetColor + ';' +
				'}' +
				'\n' +
				'/* Caption text */' +
				'.text {' +
				'   color: ' + this.widgetColor + ';' +
  				'   text-shadow: 1px 1px 2px black, 0 0 25px white, 0 0 5px silver;' +
				'   font-size: 15px;' +
				'   /*padding: 8px 12px;*/' +
				'   position: absolute;' +
				'   bottom: 26px;' +
				'   width: 100%;' +
				'   text-align: center;' +
				'}' +
				'\n' +
				'/* Number text (1/3 etc) */' +
				'.numbertext {' +
				'   color: ' + this.widgetColor + ';' +
				'   font-size: 12px;' +
				'   padding: 8px 12px;' +
				'   position: absolute;' +
				'   top: 0;' +
				'}' +
				'\n' +
				'/* The dots/bullets/indicators */' +
				'.dot {' +
				'   cursor:pointer;' +
				'   height: 13px;' +
				'   width: 13px;' +
				'   margin: 0 2px;' +
				'   background-color: #bbb;' +
				'   border-radius: 50%;' +
				'   display: inline-block;' +
				'   transition: background-color 0.6s ease;' +
				'}' +
				'\n' +
				'.dots {' +
				'   position: relative;' +
				'   text-align: center;' +
				'   bottom: 20px;' +
				'   margin: auto;' +
				'   grid-area: center;' +
				'}' +
				'\n' +
				'.active, .dot:hover {' +
				'   background-color: #717171;' +
				'   height: 11px;' +
				'   width: 11px;' +
				'   border: 1px solid '+ this.widgetColor + ';' +
				'}' +
				'\n' +
				'/* Fading animation */' +
				'.fade {' +
				'   -webkit-animation-name: fade;' +
				'   -webkit-animation-duration: 1.5s;' +
				'   animation-name: fade;' +
				'   animation-duration: 1.5s;' +
				'}' +
				'\n' +
				'@-webkit-keyframes fade {' +
				'   from {opacity: .4}' +
				'   to {opacity: 1}' +
				'}' +
				'\n' +
				'@keyframes fade {' +
				'   from {opacity: .4}' +
				'   to {opacity: 1}' +
				'}';

		this._shadowRoot.appendChild(cssClasses);

		this._connected = false;

		// this.addEventListener('click', e => {
		// 	console.log("Clicked");
		// });

		this._forward = () => {
			this.plusSlides(1);
		};
		this._backward = () => {
			this.plusSlides(-1);
		};

		// Slide click. Do nothing by default.
		this._onclick = src => {};

	}

	// Called whenever the custom element (Web Comp.) is inserted into the DOM.
	connectedCallback() {
		this._connected = true;
		if (slideshowVerbose) {
			console.log("connectedCallback invoked");
		}
		// To dynamically add children
		let observer = new MutationObserver(function(mutations) {
			mutations.forEach(function(mutation) {
				// Detect insertion
				if (mutation.addedNodes.length > 0)
					console.info('Node added: ', mutation.addedNodes[0]);
			})
		});
		observer.observe(this, { childList: true });

		if (slideshowVerbose) {
			console.info("At load time:" + this.childElementCount + " child(ren)");
			if (this.childElementCount > 0) {
				this.childNodes.forEach(childNode => {
					console.info("Node type is " + nodeTypeFromVal(childNode.nodeType));
					if (childNode.nodeType === ELEMENT_TYPE) {
						console.info(">> Node name is " + childNode.nodeName);
					} else if (childNode.nodeType === TEXT_TYPE) {
						console.info(">> Node value is " + childNode.nodeValue);
					} else if (childNode.nodeType === ATTRIBUTE_TYPE) {
						console.info(">> Node value is " + childNode.nodeValue);
					} else if (childNode.nodeType === COMMENT_TYPE) {
						console.info(">> Node value is " + childNode.nodeValue);
					}
				});
			}
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (slideshowVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (slideshowVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "slideclick":
				this._onclick = window[newVal];
				break;
			default:
				break;
		}
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (slideshowVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set slideclick(callback) {
		this._onclick = window[callback];
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
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

	get shadowRoot() {
		return this._shadowRoot;
	}

	repaint() {
		if (this._connected) {
			this.drawComponent();
		}
	}

	plusSlides(n) {
		this.showSlides(this.slideIndex += n);
	}

	currentSlide(n) {
		this.showSlides(this.slideIndex = n);
	}

	showSlides(n) {
		var i;
		let slides = this.slideshowContainer.getElementsByClassName("the-slides");
		let dots = this.dots.getElementsByClassName("dot");
		if (n > slides.length) {
			this.slideIndex = 1
		}
		if (n < 1) {
			this.slideIndex = slides.length
		}
		for (i = 0; i < slides.length; i++) {
			slides[i].style.display = "none";
		}

		for (i = 0; i < dots.length; i++) {
			dots[i].className = dots[i].className.replace(" active", "");
		}
		slides[this.slideIndex - 1].style.display = "block";
		dots[this.slideIndex - 1].className += " active";
	}

	drawComponent() {
		if (this._connected) {
			if (this._width !== undefined) {
				this.slideshowContainer.style.width = this._width + "px";
			}
			if (this._height !== undefined) {
				this.slideshowContainer.style.height = this._height + "px";
			}
			/*
			 * See if Shadow CSS can help... We need several CSS classes here.
			 */

			if (slideshowVerbose) {
				console.info("Drawing Component:" + this.childElementCount + " child(ren) slide(s)");
			}
			let self = this;
			if (this.childElementCount > 0) {
				if (slideshowVerbose) {
					console.log("slideshowContainer has " + this.slideshowContainer.childElementCount + " child nodes");
				}
				if (this.slideshowContainer.childElementCount > 0) {
					// Drop them all
					for (let n=0; n<this.slideshowContainer.childNodes.length; n++) {
						this.slideshowContainer.removeChild(this.slideshowContainer.childNodes[n]);
					}
					// while (this.slideshowContainer.firstChild !== undefined) {
					// 	this.slideshowContainer.removeChild(this.slideshowContainer.firstChild);
					// }
				}
				let idx = 0;
				this.childNodes.forEach(childNode => {
					if (childNode.nodeType === ELEMENT_TYPE) {
//					console.info(">> Node name is " + childNode.nodeName);
						if (childNode.nodeName === 'SLIDE-SHOW-IMAGE') {
							// Compose the slide here, with title, number, image, onclick event. Also add one dot per slide.

							idx += 1;

							let slide = document.createElement("div");
							slide.setAttribute('class', 'the-slides fade');

							let numberDiv = document.createElement('div');
							numberDiv.setAttribute('class', 'numbertext');
							numberDiv.innerText = idx + ' / ' + this.childElementCount;
							slide.appendChild(numberDiv);

							let image = document.createElement("img");
							let src = childNode.getAttribute('src');
							let title = childNode.getAttribute('title');
							let tooltip = childNode.getAttribute('desc');

							// Get the actual size of the image, make sure it fits the slide
							let realImage = new Image();
							realImage.src = src;
							realImage.onload = () => {
								let imageRatio = (realImage.width / realImage.height);
								let slideRatio = (self._width / self._height);
								if (imageRatio !== slideRatio) { // Resize required
									// console.log("Resizing: Real image dimension for " + src + ":" + realImage.width + 'x' + realImage.height +
									// 		", ratio=" + imageRatio + ", compare to " + slideRatio);
									if (imageRatio > slideRatio) { // image wider than slide
										let factor = slideRatio / imageRatio;
										// Keep width, adjust (shrink) height
										image.setAttribute('width', self._width);
										image.setAttribute('height', self._height * factor);
										// Margin top
										let margin = (self._height / 2) - (self._height * factor / 2);
										// console.log('Margin top:' + margin);
										image.style.marginTop = margin.toFixed(0) + 'px';
									} else { // image higher than slide
										let factor = imageRatio / slideRatio;
										// Keep height, adjust (shrink) width
										image.setAttribute('width', self._width * factor);
										image.setAttribute('height', self._height);
										// Margin left
										let margin = (self._width / 2) - (self._width * factor / 2);
										// console.log('Margin top:' + margin);
										image.style.marginLeft = margin.toFixed(0) + 'px';
									}
								} else {
									image.setAttribute('width', self._width);
									image.setAttribute('height', self._height);
								}
							}

							// image.setAttribute('width', self._width);
							// image.setAttribute('height', self._height);
							image.setAttribute('src', src);
							if (tooltip) {
								image.setAttribute('title', tooltip);
							} else {
							  image.setAttribute('title', title);
							}
							slide.appendChild(image);
							// On Slide Click:
							slide.addEventListener('click', () => {
								console.log(`${src} was clicked, invoking user's callback.`);
								self._onclick(src);
							});

							let textDiv = document.createElement('div');
							textDiv.setAttribute('class', 'text');
							textDiv.innerText = title;
							slide.appendChild(textDiv);

							if (slideshowVerbose) {
								console.log('Adding slide-image', slide);
							}
							this.slideshowContainer.appendChild(slide);

							let dot = document.createElement('span');
							dot.setAttribute('class', 'dot');
							dot.setAttribute('title', title);
							let gotoSlide = idx;
							dot.addEventListener('click', () => {
								self.currentSlide(gotoSlide);
							});
							this.dots.appendChild(dot);
						}
					}
				});

				let prev = document.createElement('a');
				prev.setAttribute('class', 'prev');
  			prev.onclick = this._backward;
				prev.innerHTML = '&#10094;';

				let next = document.createElement('a');
				next.setAttribute('class', 'next');
				next.onclick = this._forward;
				next.innerHTML = '&#10095;';

				this.slideshowContainer.appendChild(prev);
				this.slideshowContainer.appendChild(next);

				this.slideIndex = 1;
				this.showSlides(this.slideIndex); // First display, use the first slide.
			}
		}
	}
}
// Associate the tag and the class
window.customElements.define(SLIDE_SHOW_TAG_NAME, SlideShow);
