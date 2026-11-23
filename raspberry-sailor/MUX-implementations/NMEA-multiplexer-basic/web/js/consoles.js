/**
 * Oho ! For the console menu (top one)
 * @author Olivier Le Diouris
 */
"use strict";

const NO_DIALOG_MESSAGE = "Dialog tag not supported.";
const DEFAULT_LANG = "EN";

let currentLang = DEFAULT_LANG; // Init value

let showSection = (id) => {
	//document.getElementById(id).style.display = 'inline-block';
	document.getElementById(id).style.visibility = 'visible';
	document.getElementById(id).style.opacity = 1;
	document.getElementById(id).style.height = 'auto';
};

let hideSection = (id) => {
	//document.getElementById(id).style.display = 'none';
	document.getElementById(id).style.visibility = 'hidden';
	document.getElementById(id).style.opacity = 0;
	document.getElementById(id).style.height = 0;
};

const COLLAPSED_LINE = "&#9658;&nbsp;";
const EXPANDED_LINE = "&#9660;&nbsp;";

let showHideSection = (obj, id) => {
	let innerSpan = obj.querySelector('.expand-collapse');
	if (document.getElementById(id).style.visibility === 'visible') {  // Then hide it
		innerSpan.innerHTML = COLLAPSED_LINE;
		hideSection(id);
	} else { // Show it
		innerSpan.innerHTML = EXPANDED_LINE;
		showSection(id);
	}
};

var currentContext = "1"; // Default, Home page.

// prefer getQueryParameterByName
let getQSPrm = (prm) => {
	let value;
	let loc = document.location.toString();
	if (loc.indexOf("?") > -1) {
		let qs = loc.substring(loc.indexOf("?") + 1);
		let prms = qs.split('&');
		for (let i=0; i<prms.length; i++) {
			let nv = prms[i].split('=');
			if (nv.length === 2) {
				if (nv[0] === prm) {
					return nv[1];
				}
			}
		}
	}
	return value;
};

let clack = (origin, docToLoad) => {
    let originId = '';
    if (typeof(origin) === 'string') {
        originId = origin.replace('_', '');
    } else {
        // console.log(`clack: Click on ${origin.innerText}, id ${origin.id}`);
        originId = origin.id.replace('_', '');
    }
    currentContext = originId;

    let contentName = docToLoad;
	let contentPlaceHolder = document.getElementById("current-content");

    // console.log(`Fetching ${contentName}...`);
    let iframeToFill = document.getElementById("content-frame");
    iframeToFill.src = contentName;
}

let updateMenu = () => {

    document.querySelectorAll(".this-tooltiptext").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Besoin d'aide&nbsp;?" : "Need Help?"));
    document.querySelectorAll("#_lang-flag-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Switch to English" : "En fran&ccedil;ais"));

    document.querySelectorAll("#_1").forEach(elmt =>  {
        elmt.innerHTML = (currentLang === "FR" ? "Effacer" : "Clear")
        elmt.title = (currentLang === "FR" ? "Retour à la page d'accueil" : "Back to home page")
    });

    document.querySelectorAll("#_2").forEach(elmt => elmt.title = (currentLang === "FR" ? "MUX Admin" : "MUX Admin"));
	document.querySelectorAll("#_2").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Admin" : "MUX Admin"));
	document.querySelectorAll("#_21").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Admin" : "MUX Admin"));
	document.querySelectorAll("#_22").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Builder" : "MUX Builder"));
	document.querySelectorAll("#_221").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Builder - graphique" : "MUX Builder - graphical"));
	document.querySelectorAll("#_222").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Builder - texte" : "MUX Builder - text"));
	// document.querySelectorAll("#_23").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "MUX Builder (texte)" : "MUX Builder (text)"));
	document.querySelectorAll("#_24").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Marker Builder" : "Marker Builder"));

	document.querySelectorAll("#_3").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Consoles" : "Consoles"));
	document.querySelectorAll("#_31").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Console compl&egrave;te" : "Full Console"));
	document.querySelectorAll("#_32").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Console Raymarine" : "Raymarine Console"));
	document.querySelectorAll("#_33").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Vu du Cockpit" : "Cockpit View"));
	document.querySelectorAll("#_34").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Simulateur" : "Simulator"));
	document.querySelectorAll("#_35").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Console avec Vent et WebComponents" : "Console with Wind and WebComponents"));
	// document.querySelectorAll("#_36").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Console Leaflet - 1, donn&eacute;es GPS" : "Leaflet Console - 1, GPS Data"));
	document.querySelectorAll("#_37").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Console Leaflet, donn&eacute;es GPS" : "Leaflet Console, GPS Data"));
	document.querySelectorAll("#_38").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Affichage pare-brise" : "Headup Display"));

	document.querySelectorAll("#_4").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Mar&eacute;es" : "Tides"));
	document.querySelectorAll("#_41").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Ports, avec Leaflets" : "Leaflet Tide Stations"));
	document.querySelectorAll("#_42").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Courbes" : "Tide Curves"));
	document.querySelectorAll("#_43").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Publication d'almanachs, mar&eacute;e ou Soleil" : "Tide (or Sun) Almanacs Publication"));

	document.querySelectorAll("#_5").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Almanachs" : "Almanacs"));
	document.querySelectorAll("#_51").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Publication d'almanachs nautiques" : "Nautical Almanacs Publication"));
	document.querySelectorAll("#_52").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Calendrier lunaire" : "Moon Calendar"));
	document.querySelectorAll("#_53").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Publication d'almanachs, mar&eacute;e ou Soleil" : "Tide (or Sun) Almanacs Publication"));

	document.querySelectorAll("#_6").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Chartless, et vues de l'espace" : "Chartless and Space Views"));
	document.querySelectorAll("#_61").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Chartless, avec carte du monde" : "Chartless with World Data"));
	document.querySelectorAll("#_62").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Chartless, avec carte du monde et mar&eacute;es" : "Chartless with World and Tide Data"));
	document.querySelectorAll("#_63").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Vue de l'espace" : "Space View"));

    document.querySelectorAll("#_7").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Assistant m&eacute;t&eacute;o" : "Weather Wizard"));
    document.querySelectorAll("#_71").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "D&eacute;finition des Composites" : "Basic Workbench"));
    document.querySelectorAll("#_72").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Op&eacute;ration" : "Operation, at work"));
    document.querySelectorAll("#_73").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Fax de surface, Atlantique Nord" : "North Atlantic Surface Faxes"));
    document.querySelectorAll("#_74").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Fax Atlantique Nord, surface et 500mb" : "North Atlantic Surface and 500mb Faxes"));
};

let switchLanguage = (langToUse) => {

    console.log(`In switchLanguage (start), currentLang is set to ${currentLang}`);

    if (langToUse) {
        currentLang = langToUse;
        if (langToUse === "FR") { // Then switch to EN
            document.querySelectorAll("#lang-flag").forEach(flagElement => {
                flagElement.src = "us_uk_flag.png"; // "./usa.gif";
                flagElement.alt = "US Flag";
                flagElement.title = "Switch to English";
            });
        } else {
            document.querySelectorAll("#lang-flag").forEach(flagElement => {
                flagElement.src = "./france.gif";
                flagElement.alt = "Drapeau français";
                flagElement.title = "En français";
            });
        }
    } else { // Flip language
        if (currentLang === "FR") { // Then switch to EN
            document.querySelectorAll("#lang-flag").forEach(flagElement => {
                flagElement.src = "./france.gif";
                flagElement.alt = "Drapeau français";
                flagElement.title = "En français";
            });
            currentLang = "EN";
        } else {
            document.querySelectorAll("#lang-flag").forEach(flagElement => {
                flagElement.src = "us_uk_flag.png"; // "./usa.gif";
                flagElement.alt = "US Flag";
                flagElement.title = "Switch to English";
            });
            currentLang = "FR";
        }
    }
	// Le reste...
	updateMenu();

    // Update currently displayed content
    let newId = `_${currentContext}`;
    let el = document.createElement("div");
	el.id = newId;
	// clack(el, 'struct');
    // clack(el);

    console.log(`In switchLanguage (end), currentLang is set to ${currentLang}`);

};

let generateFetchMessage = (contentName, response) => {
    let mess = (currentLang === 'FR') ? 'Cette page est en cours de d&eacute;veloppement...<br/>Disponible prochainement.' :
                                        'This page is being developped...<br/>Available soon.';
    let message = `<div style='margin: 10px;'><div style='display: block; padding: 5px; border: 1px solid silver; margin: 5px; line-height: 1.0;'>Error Message (=> admin)<br/> Fetching ${contentName}...<br/>Data Response: ${response.status} - ${response.statusText}</div>` +
    `<div style="width: 100%; text-align: center;"><img src="/images/the.shipyard.jpg" width="100%"></div>` +
    `<div style='border: 3px solid orange; border-radius: 10px; text-align: center; display: grid; grid-template-columns: auto auto auto;'>` +
    `<div style="display: flex; align-items: center; margin: auto;"><img src="/images/construction.cone.png" height="52"></div> <div><b>${mess}</b></div> <div style="display: flex; align-items: center; margin: auto;"><img src="/images/construction.cone.png" height="52"></div></div></div>`;
    return message;
};

let generateFetchErrorMessage = (contentName, error, errmess) => {
    let message;
    if (errmess) {
        let mess = JSON.parse(errmess);
        if (mess.message) {
            message = mess.message;
        }
    }
    let text = (currentLang === 'FR') ? 'Erreur de chargement...<br/>Est-ce que le serveur est en route ?<br/>Voyez votre administrateur.' :
                                        'Load error...<br/>Is the server running?<br/>See your admin.';
    let content = `<div style='margin: 10px;'><pre>Fetch Error for ${contentName}: ${(error ? (typeof(error) === 'object' ? JSON.stringify(error, null, 2) : error) : ' - ') + ', ' + (message ? message : ' - ')} </pre><div style='border: 3px solid red; border-radius: 10px; text-align: center;'><b>${text}</b></div></div>`;
    return content;
};

let closeDiv = (el) => {
    el.style.display = 'none';
}

function expandClick(origin) {
    console.log("Click happened");
    let divToShow = origin.parentNode.querySelector("div");
    if (divToShow.style.display === 'none') {
        divToShow.style.display = 'block';
        origin.innerHTML = '&#9660;';
    } else {
        divToShow.style.display = 'none';
        origin.innerHTML = '&#9658;';
    }
}

function decToSex(val, ns_ew) {
    let absVal = Math.abs(val);
    let intValue = Math.floor(absVal);
    let dec = absVal - intValue;
    let i = intValue;
    dec *= 60;
    let min = dec.toFixed(4);
    while (min.length < 7) {
        min = '0' + min;
    }
    let s = i + "°" + min + "'";

    if (val < 0) {
        s += (ns_ew === 'NS' ? 'S' : 'W');
    } else {
        s += (ns_ew === 'NS' ? 'N' : 'E');
    }
    return s;
}

/**
 * Turn "N 47 40.66" into 47.677667
 * Warning: No deg sign, no min sign.
 */
function sexToDec(val, lat_lng) {
    let sign = val.substring(0, 1);
    let fullValue = val.substring(val.indexOf(' ') + 1);
    let degs = fullValue.substring(0, fullValue.indexOf(' '));
    let mins = fullValue.substring(fullValue.indexOf(' ') + 1);

    let deg = parseInt(degs);
    let min = parseFloat(mins);

    let decValue = deg + (min / 60.0);
    if (sign === 'W' || sign === 'S') {
        decValue *= -1;
    }

    return decValue;
}

let scrollHere = (parent) => {
    console.log('Scroll Here invoked'); // scroll-margin-top may not be appropriate here
    let href = parent.href.substring(parent.href.indexOf("#") + 1);
    // Now look for the tab-section to scroll in
    let ref = parent.parentNode;
    while (ref !== undefined && !ref.classList.contains('tab-section')) {
        console.log(`Classlist of ${ref.nodeName}: ${ref.classList}`);
        ref = ref.parentNode;
    }
    let currentDialog = document.getElementById('info-tx-dialog');
    // currentDialog.scrollTo(0, -60);
    let anchor = document.querySelectorAll(`a[name="${href}"]`);
    anchor[0].scrollIntoView();
    // currentDialog.style.marginTop = '280px';
    currentDialog.scrollIntoView();
    // document.getElementById('current-content').scrollTo(0, -60);
    // debugger;
    return event.preventDefault();
}

let scrollToGivenAnchor = (hashtag) => {
    const anchor = document.querySelector(`a[name='${hashtag}']`);
    if (anchor) {
        anchor.scrollIntoView();
    } else {
        console.log(`scrollToGivenAnchor: Anchor ${hashtag} not found.`);
    }
    // 2e couche
    window.scrollBy(0, -92); // 92: menu thickness
};

// Left-right swipe gesture management on the slides
let xDown = null;
let yDown = null;

function handleTouchStart(evt) {
    xDown = evt.touches[0].clientX;
    yDown = evt.touches[0].clientY;
}

function handleTouchMove(evt) {
    if (!xDown || !yDown) {
        return;
    }
    let xUp = evt.touches[0].clientX;
    let yUp = evt.touches[0].clientY;
    let xDiff = xDown - xUp;
    let yDiff = yDown - yUp;

    // most significant
    if (Math.abs(xDiff) > Math.abs(yDiff)) { // Left-right
        if (xDiff > 0) {
        /* left swipe */
        plusSlides(1);
        } else {
        /* right swipe */
        plusSlides(-1);
        }
    } else { // Up-Down, not needed here (yet...)
        if (yDiff > 0) {
        /* up swipe */
        } else {
        /* down swipe */
        }
    }
    /* reset values */
    xDown = null;
    yDown = null;
}

function manageClick(cb) {
    auto = cb.checked;
    if (auto) {
        showSlides(slideIndex);
    }
}

let getQueryParameterByName = (name, url) => {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) {
        return null;
    }
    if (!results[2]) {
        return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, " "));
};

let qrcode;

let makeCode = (url, location) => {
	if (url === undefined) {
		url = document.location.href;
	}
    console.log(`QR Code for ${url}`);
	let docLoc = url;
	let toDisplay = docLoc; // .substring(0, docLoc.lastIndexOf('/')) + "/" + url; // document.location + url;

  	qrcode.makeCode(toDisplay);
    // qrcode.style.display = 'block';
    if (location) {
        // let content = document.getElementById("qrcode").innerHTML;
        document.getElementById(location).appendChild(document.getElementById("qrcode"));
        document.getElementById(location).firstChild.style.display = 'block';
    } else {
        document.getElementById("qrcode").style.display = 'block'; // Show it ! (Hidden otherwise)
    }
};

const DIALOG_OPTION = true;

// Copy the content of a field
let copyToClipboard = (fieldId) => {
    let value = document.getElementById(fieldId).innerHTML;
    let codeContent = value.replaceAll("<br>", "\n");
    codeContent = content.replaceAll("<br/>", "\n");
    // console.log(codeContent);
    let codeHolder = document.createElement("textarea"); // To keep the format, 'input' would not.
    codeHolder.value = codeContent;
    document.body.appendChild(codeHolder);
    codeHolder.select();
    document.execCommand("copy");
    document.body.removeChild(codeHolder);
    // customAlert(`Value ${value} copied to clipboard`);
};

// Copy a literal value
let copyTextToClipboard = (content) => {
    let codeContent = content.replaceAll("<br>", "\n");
    codeContent = content.replaceAll("<br/>", "\n");
    // console.log(codeContent);
    let codeHolder = document.createElement("textarea"); // To keep the format, 'input' would not.
    codeHolder.value = codeContent;
    document.body.appendChild(codeHolder);
    codeHolder.select();
    document.execCommand("copy");
    document.body.removeChild(codeHolder);
    // customAlert(`Value ${content} copied to clipboard`);
};

let keepScrolling = true;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
};


// Jump to somewhere else in the same site...
let jumpTo = (page, extraPrm) => {
    let origin = window.location.origin;
    let newUrl = origin + `?lang=${currentLang}&nav-to=${page}&${extraPrm}`
    console.log(`Going to ${newUrl}`);
    // debugger;
    if (false) {
        window.open(newUrl); // New window
    } else {
        window.location.assign(newUrl); // Same window
    }
};
