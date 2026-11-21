/**
 * Oho ! For the console menu (top one)
 * @author Olivier Le Diouris
 */
"use strict";

const NO_DIALOG_MESSAGE = "Dialog tag not supported.";
const DEFAULT_LANG = "FR";
let currentLang = DEFAULT_LANG; // Init value

let openNav = () => {
    // console.log("Opening Hamburger Menu");
    // alert("Opening Hamburger Menu");
    // let newWidth = getComputedStyle(document.documentElement).width;
    // console.log(`Setting new Width to ${newWidth}`);
	// document.getElementById("side-nav").style.width = newWidth; // getComputedStyle(document.documentElement).getPropertyValue('--expanded-nav-width'); // "450px";
    document.getElementById("side-nav").style.display = 'inline-block';
    // debugger;
};

let closeNav = () => {
	// document.getElementById("side-nav").style.width = "0";
    document.getElementById("side-nav").style.display = 'none';
};

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

const DYN_EXTENSION = 'ocss'; // 'html'; // 'ocss'; // '.something'; // '.astro'; // '.html'; ... etc.

let clack = (origin, docToLoad) => {
    let originId = '';
    if (typeof(origin) === 'string') {
        originId = origin.replace('_', '');
    } else {
        console.log(`clack: Click on ${origin.innerText}, id ${origin.id}`);
        originId = origin.id.replace('_', '');
    }
    currentContext = originId;

    // if (originId === '11') {
    //     makeCode(document.location.href);
    //     return;
    // } else {
    //     try {
    //         document.getElementById("qrcode").style.display = 'none'; // Improve this ?..
    //     } catch (err) {
    //         console.log(JSON.stringify(err));
    //     }
    // }

    let contentName = docToLoad;
	let contentPlaceHolder = document.getElementById("current-content");

    console.log(`Fetching ${contentName}...`);
	fetch(contentName)
            .then(response => {  // Warning... the NOT_FOUND error lands here, apparently.
                console.log(`Data Response: ${response.status} - ${response.statusText}`);
				if (response.status !== 200) { // There is a problem...
					contentPlaceHolder.innerHTML = generateFetchMessage(contentName, response); // `Fetching ${contentName}...<br/> Data Response: ${response.status} - ${response.statusText}<br/><b>En d&eacute;veloppement...<br/>Disponible prochainement.</b>`;
				} else {
					response.text().then(doc => {
						console.log(`Code data loaded, length: ${doc.length}.`);
						// Some specific cases here
						/* if (origin.id === "1") { // Move this higher. No need to load 1_xx.html ?..
							document.location.reload();
						} else */
                        if (false && originId === "23") { // Not used, an example of a dialog. Content inserted in the <dialog>
							document.getElementById("dialog-content").innerHTML = doc;
							showAboutDialog();
                        } else if (originId === "10") { // The finder
                            document.getElementById("dialog-content").innerHTML = doc; // '<div>Akeu Coucou</div>';
							showAboutDialog();
						} else {
							contentPlaceHolder.innerHTML = doc;
                            if (false && originId === "22") {  // Not used.
                                showSlides(currentSlide);
                            } else if (originId === "21" || originId === "22" || originId === "23") { // Menu 2, One page only, with anchors.
                                // let nbTry = 0;
                                let scrollToAnchor = () => {
                                    const overflow = document.getElementById('action-container-2');
                                    let hashtag = (originId === "21") ? '01' : ((originId === "22") ? '02' : '03');
                                    const anchor = document.querySelector(`a[name='${hashtag}']`);
                                    anchor.scrollIntoView();

                                    /*
                                    const rectOverflow = overflow.getBoundingClientRect();
                                    const rectAnchor = anchor.getBoundingClientRect();

                                    let scroll_top = rectAnchor.top - rectOverflow.top;
                                    console.log(`rectAnchor.top: ${rectAnchor.top}, rectOverflow.top: ${rectOverflow.top} => ${scroll_top}`);
                                    // Set the scroll position of the overflow container
                                    overflow.scrollTop = scroll_top; // .toFixed(0);  // If remains to zero, check div's height
                                    console.log(`>>> Origin: ${originId}: scrolltop: ${overflow.scrollTop} vs ${scroll_top}`);
                                    */
                                    // 2e couche
                                    if (originId === "21") {
                                        window.scrollTo(0, 0); // Scroll on top, if invoked from a button at the bottom of the page
                                    }
                                    window.scrollBy(0, -92); // 92: menu thickness
                                };
                                window.setTimeout(scrollToAnchor, 100);
                                if (false) {
                                    // 2e couche
                                    if (originId === "21") {
                                        window.scrollTo(0, 0); // Scroll on top, if invoked from a button at the bottom of the page
                                    }
                                }
                                fillOutTheTeam(); // For the random order

                                // console.log("Now scrolling.")
                            // } else if (originId === "31" || originId === "32" || originId === "33") {
                            //     const overflow = document.getElementById('action-container');
                            //     let hashtag = (originId === "31") ? '01' : ((originId === "32") ? '02' : '03');
                            //     const anchor = document.querySelector(`a[name='${hashtag}']`);

                            //     const rectOverflow = overflow.getBoundingClientRect();
                            //     const rectAnchor = anchor.getBoundingClientRect();

                            //     // window.setTimeout(() => {
                            //         // Set the scroll position of the overflow container
                            //         overflow.scrollTop = rectAnchor.top - rectOverflow.top;
                            //         console.log("Now Scrolling !");
                            //     //}, 1000);
                            //     // console.log("Scrolling !");
                            } else {
                                if (originId === "4") {
                                    window.setTimeout(() => {
                                        fillOutFleet(null); // Populate default (full) boat list
                                    }, 500);
                                } else if (originId === "62") {
                                    window.setTimeout(() => {
                                        // debugger;
                                        fillOutActu(null); // Populate default (full) news list
                                    }, 500);
                                } else if (originId === "68") {
                                    window.setTimeout(() => {
                                        fillOutNextEvents(null);
                                    }, 500);
                                } else if (originId === "33") { // Partager, PCC
                                    window.setTimeout(() => {
                                        fillOutFleet(CLUB, "share-container", false); // Populate PCC boat list
                                    }, 500);
                                } // 21, fill out team ?

                                window.scrollTo(0, 0); // Scroll on top, if invoked from a button at the bottom of the page
                            }
						}
					});
				}
            },
            (error, errmess) => {
                console.log("Ooch");
                let message;
                if (errmess) {
                    let mess = JSON.parse(errmess);
                    if (mess.message) {
                        message = mess.message;
                    }
                }
                console.debug("Failed to get code data..." +
                    (error ? (typeof(error) === 'object' ? JSON.stringify(error, null, 2) : error) : ' - ') +
                    ', ' +
                    (message ? message : ' - ')
                );
				// Plus tard...
				contentPlaceHolder.innerHTML = generateFetchErrorMessage(contentName, error, errmess);
            });

    // Scroll somewhere in the loaded data ?
    if (true) {
        console.log(`clack: Checking for 'where' parameter...`);
        let where = getQueryParameterByName('where'); // An anchor ?
        if (where) {
            window.setTimeout(() => {
                // debugger;
                console.log(`Scrolling to ${where}...`);
                scrollToGivenAnchor(where);
            }, 3000); // Wait a bit for the page to load. TODO Try async ?...
            // scrollToGivenAnchor(where);
        } else {
            console.log("No 'where' QS parameter.");
        }
    }

}

let specialClack = (clackName) => {
    let originId = '';
    if (typeof(clackName) === 'string') {
        originId = clackName.replace('_', '');
    } else {
        console.log(`clack: Click on ${clackName.innerText}, id ${clackName.id}`);
        originId = clackName.id.replace('_', '');
    }
    currentContext = originId;

	let contentName = `/${originId}_${currentLang}.html`; // ${DYN_EXTENSION}`; // From the origin !!

    let contentPlaceHolder = document.getElementById("current-content");

    // TODO Set the content
    let dynamicContentContainer = DIALOG_OPTION ? document.getElementById("dialog-tx-content") : document.getElementById("info-tx");
    let dialogTitle = document.querySelectorAll('.dialog-title'); // dialog-title
    if (dialogTitle && dialogTitle.length > 0) {
        dialogTitle[dialogTitle.length - 1].innerHTML = name; // Can be several dialogs... take the last.
    }

    console.log(`onclick, loading ${contentName}`);
    fetch(contentName)
        .then(response => {  // Warning... the NOT_FOUND error lands here, apparently.
            console.log(`Data Response: ${response.status} - ${response.statusText}`);
            if (response.status !== 200) { // There is a problem...
                dynamicContentContainer.innerHTML = generateFetchMessage(contentName, response); // `Fetching ${contentName}...<br/> Data Response: ${response.status} - ${response.statusText}<br/><b>En d&eacute;veloppement...<br/>Disponible prochainement.</b>`;
            } else {
                response.text().then(doc => {
                    console.log(`${contentName} code data loaded, length: ${doc.length}.`);
                    dynamicContentContainer.innerHTML = doc;
                });
            }
        },
        (error, errmess) => {
            console.log("Ooch");
            let message;
            if (errmess) {
                let mess = JSON.parse(errmess);
                if (mess.message) {
                    message = mess.message;
                }
            }
            console.debug("Failed to get code data..." + (error ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message ? message : ' - '));
            // Plus tard...
            dynamicContentContainer.innerHTML = generateFetchErrorMessage(contentName, error, errmess); // `<b>${contentName} ${currentLang === 'FR' ? ' introuvable...<br/>Bient&ocirc;t dispo !' : ' not found...<br/>Avai;able soon!'}</b>`;
        });

    // dynamicContentContainer.innerHTML = content;
    if (DIALOG_OPTION) {
        showInfoTxDialog();
    } else {
        dynamicContentContainer.style.display = 'block';
    }
}

let updateMenu = () => { // Multilang aspect, in index.html.

    // Tooltips
    document.querySelectorAll("#_1-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Retour accueil" : "Back to Home Page"));
    document.querySelectorAll(".this-tooltiptext").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Besoin d'aide&nbsp;?" : "Need Help?"));
    document.querySelectorAll("#_7").forEach(elmt =>  elmt.title = (currentLang === "FR" ? "Espace Membres" : "Member Space"));
    document.querySelectorAll("#_7-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Espace Membres" : "Member Space"));
    document.querySelectorAll("#_8").forEach(elmt =>  elmt.title = (currentLang === "FR" ? "La boutique" : "The store"));
    document.querySelectorAll("#_8-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "La boutique" : "The store"));
    document.querySelectorAll("#_9").forEach(elmt =>  elmt.title = (currentLang === "FR" ? "Outils de Navigation" : "Navigation tools"));
    document.querySelectorAll("#_9-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Outils de Navigation" : "Navigation tools"));
    document.querySelectorAll("#_9bis").forEach(elmt =>  elmt.title = (currentLang === "FR" ? "La caisse &agrave; glingues" : "Tool Box"));
    document.querySelectorAll("#_9bis-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "La caisse &agrave; glingues" : "Tool Box"));
    document.querySelectorAll("#_10").forEach(elmt =>  elmt.title = (currentLang === "FR" ? "Besoin d'aide ?" : "Need Help?"));
    document.querySelectorAll("#_10-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Besoin d'aide ?" : "Need Help?"));
    document.querySelectorAll("#_lang-flag-label").forEach(elmt =>  elmt.innerHTML = (currentLang === "FR" ? "Switch to English" : "En fran&ccedil;ais"));

    // Others
    document.querySelectorAll("#home-label").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Accueil" : "Home"));

    // "_11", Qr Code, no update needed.

	document.querySelectorAll("#_2").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Qui sommes-nous&nbsp;?&nbsp;" : "Who we are&nbsp;"));
	document.querySelectorAll("#_21").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Passe-Coque, c'est quoi&nbsp;?" : "Passe-Coque, what's that?"));
	document.querySelectorAll("#_22").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Notre &eacute;quipe" : "Our team"));
	document.querySelectorAll("#_23").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Cr&eacute;er du lien" : "Connecting people"));

	document.querySelectorAll("#_3").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Nos actions&nbsp;" : "Our actions&nbsp;"));
	document.querySelectorAll("#_31").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Transmettre - <i>Les projets</i>" : "Transmitting - <i>The projects</i>"));
	document.querySelectorAll("#_32").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "R&eacute;nover - <i>Le chantier</i>" : "Refitting - <i>The shipyard</i>"));
	document.querySelectorAll("#_33").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Partager - <i>Le Club</i>" : "Sharing - <i>The Club</i>"));
	// document.querySelectorAll("#_34").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Formations" : "Trainings"));
	// document.querySelectorAll("#_35").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Partenaires" : "Partners"));

	document.querySelectorAll("#_4").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "La flotte&nbsp;" : "The fleet&nbsp;"));
	// document.querySelectorAll("#_41").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Les bateaux" : "The boats"));     // Unused
	// document.querySelectorAll("#_42").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Les projets" : "The projects"));  // Unused

	document.querySelectorAll("#_5").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Nous soutenir&nbsp;" : "Support us&nbsp;"));
	document.querySelectorAll("#_51").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Adh&eacute;rer" : "Join us"));
	document.querySelectorAll("#_52").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Faire un don" : "Make a donation"));
	document.querySelectorAll("#_53").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Investir dans l'Eco-Village" : "Invest in the Eco-Village"));

	document.querySelectorAll("#_6").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "En savoir plus&nbsp;" : "Know more&nbsp;"));
	document.querySelectorAll("#_61").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Contact" : "Contact"));
	document.querySelectorAll("#_62").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Actualit&eacute;" : "News"));
	document.querySelectorAll("#_63").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "On parle de nous / Presse" : "We're in the news"));
	document.querySelectorAll("#_64").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "La boutique" : "The store"));
	document.querySelectorAll("#_65").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Partenaires" : "Partners"));
	// document.querySelectorAll("#_66").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Charte PCC" : "PCC Chart"));
	document.querySelectorAll("#_67").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Espace Membres" : "Members Space"));
	document.querySelectorAll("#_68").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Vie de l'asso" : "Life of the asso"));
	document.querySelectorAll("#_69").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Outils de communication" : "Communication material"));
	// document.querySelectorAll("#_7").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? "Espace Membres" : "Members Space"));
	document.querySelectorAll("#ms_7").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? " Espace Membres" : " Members Space"));
	document.querySelectorAll("#bs_8").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? " La boutique" : " The shop"));
	document.querySelectorAll("#bs_9").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? " Navigation" : " Navigation"));
	document.querySelectorAll("#bs_9bis").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? " Bo&icirc;te &agrave; outils" : " Tool Box"));
    // No 9, Navigation works for both languages
	document.querySelectorAll("#bs_10").forEach(elmt => elmt.innerHTML = (currentLang === "FR" ? " Recherche..." : " Search..."));
};

let switchLanguage = () => {
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
	// Le reste...
	updateMenu();

    // Update currently displayed content
    let newId = `_${currentContext}`;
    let el = document.createElement("div");
	el.id = newId;
	clack(el, 'struct');
    // clack(el);
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

// Dynamic translation, for the actu section.
// We assume all data are already in French.
let translate = (actuId) => {
    console.log(`${actuId} : ${(currentLang === 'FR') ? "En français" : "I'll speak english"}`);

    switch (actuId) {
        case 'cc-2023': // Carter Cup 2023
            if (currentLang === 'EN') {  // Then translate
                let dateField = document.getElementById('cc-2023').querySelector('h2');
                if (dateField) {
                    dateField.innerText = "Carter Cup, August 2023";
                }
                let contentField01 = document.getElementById('cc-2023').querySelector('#content-01');
                if (contentField01) {
                    contentField01.innerHTML = 'Jimmy, on board Pordin-Nancq, was a winner!';
                }
                // etc...
            }
            break;
        case 'fam-2023': // Festival dea Aventuries de la Mer 2023
            if (currentLang === 'EN') {  // Then translate
                let dateField = document.getElementById('fam-2023').querySelector('h2');
                if (dateField) {
                    dateField.innerText = "Festival des Aventuriers de la Mer, October 2023";
                }
                let contentField01 = document.getElementById('fam-2023').querySelector('#content-01');
                if (contentField01) {
                    contentField01.innerHTML = 'Passe-Coque wins the "Audelor et R&eacute;gion Bretagne" prize, and a 5000€ check, for the creation of the boat-club of the association.';
                }
                // etc...
            }
            break;
        case 'early-2024':
            if (currentLang === 'EN') {
                let dateField = document.getElementById('early-2024').querySelector('h2');
                if (dateField) {
                    dateField.innerText = "Mid-january 2024";
                }
                let contentField01 = document.getElementById('early-2024').querySelector('div');
                if (contentField01) {
                    contentField01.innerHTML = 'Release of the new web site.';
                }
            }
            break;
        default:
            break;
    }
};

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


// let mouseOnTop = (el) => {
//     console.log(`Mouse on ${el.id}`);
//     // el.style.cursor = 'pointer';
// }

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
