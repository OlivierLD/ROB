let errManager = {
    display: alert
};
let RESTPayload = {};
let storedHistory = "";
let storedHistoryOut = "";
let storedElapsed = "";

const DEBUG = false;

let lpad = (s, w, len) => {
    let str = s;
    while (str.length < len) {
        str = w + str;
    }
    return str;
};

/* Uses ES6 Promises */
let getPromise = (
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // A code, or a function (callback) returning a boolean. if met, resolve, otherwise fail.
    data = null,                  // payload, when needed (PUT, POST...)
    show = false) => {            // Show the traffic [true]|false

    if (show === true) {
        document.body.style.cursor = 'wait';
    }

    if (DEBUG) {
        console.log(">>> Promise", verb, url);
    }

    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
        let TIMEOUT = timeout;

        let req = verb + " " + url;
        if (data !== undefined && data !== null) {
            req += ("\n" + JSON.stringify(data, null, 2));
        }
        if (DEBUG) {
            console.log("Request:", req);
        }

        xhr.open(verb, url, true);
        xhr.setRequestHeader("Content-type", "application/json");
        try {
            if (data === undefined || data === null) {
                xhr.send();
            } else {
                xhr.send(JSON.stringify(data));
            }
        } catch (err) {
            console.log("Send Error ", err);
        }

        let requestTimer = setTimeout(() => {
            xhr.abort();
            let mess = {code: 408, message: `Timeout (${timeout}ms) for ${verb} ${url}`};
            reject(mess);
        }, TIMEOUT);

        xhr.onload = () => {
            clearTimeout(requestTimer);
            if ((typeof(happyCode) === 'function' && happyCode(xhr.status)) || (typeof(happyCode) === 'number' && xhr.status === happyCode)) {
                resolve(xhr.response);
            } else {
                reject({code: xhr.status, message: xhr.response});
            }
        };
    });
};

const DEFAULT_TIMEOUT = 10000;

let protocolTestFunc = () => {
    let url = document.location.origin.replace('http', 'mux') + '/this-is-a-test';
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

let terminate = () => {
    return getPromise('/mux/terminate', DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

let systemDate = () => {
    return getPromise('/mux/system-date', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let updateSystemDate = (newFmtDate) => {
    return getPromise('/mux/system-date', DEFAULT_TIMEOUT, 'POST', 201, newFmtDate, false);
};

let updateMarkersConfig = (markerList) => {
    return getPromise('/mux/reload', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 201 || ret === 202); }, markerList, false);
};

let updateNextWaypoint = (waypointId) => {
    return getPromise(`/mux/waypoints/${waypointId}`, DEFAULT_TIMEOUT, 'PUT', (ret) => { return (ret === 201 || ret === 202); }, waypointId, false);
};

let enableLogging = (b) => {
    return getPromise('/mux/mux-process/' + (b === true ? 'on' : 'off'), DEFAULT_TIMEOUT, 'PUT', 201, null, false);
};

let getForwarderStatus = () => {
    return getPromise('/mux/mux-process', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getLogFiles = () => {
    return getPromise('/mux/log-files', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

// Should be useless..., invoke it directly (no promise required) to download.
let getLogFile = (fileName) => {
    return getPromise('/mux/log-files/' + fileName, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getLogFileDetails = (fileName) => {
    return getPromise('/mux/log-file-details/' + fileName, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getLogToJSON = (fileName, timeoutFactor = 1) => {
    return getPromise('/mux/log-file-to-json/' + fileName, timeoutFactor * DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let deleteLogFile = (logFile) => {
    return getPromise('/mux/log-files/' + logFile, DEFAULT_TIMEOUT, 'DELETE', (ret) => { return (ret >= 200 || ret < 300); }, null, false);
};

let getSystemTime = () => {
    return getPromise('/mux/system-time?fmt=date', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getVolume = () => {
    return getPromise('/mux/nmea-volume', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getRunData = () => {
    return getPromise('/mux/run-data', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getLastSentence = () => {
    return getPromise('/mux/last-sentence', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getSOGCOG = () => {
    return getPromise('/mux/sog-cog', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getDistance = () => {
    return getPromise('/mux/distance', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getDeltaAlt = () => {
    return getPromise('/mux/delta-alt', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getSerialPorts = () => {
    return getPromise('/mux/serial-ports', DEFAULT_TIMEOUT, 'GET', 200);
};

let getMUXContext = () => {
    return getPromise('/mux/context', DEFAULT_TIMEOUT, 'GET', 200);
};

let getMarkerFiles = () => {
    return getPromise('/mux/marker-files', DEFAULT_TIMEOUT, 'GET', 200);
};

let getChannels = () => {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'GET', 200);
};

let getForwarders = () => {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'GET', 200);
};

let getComputers = () => {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'GET', 200);
};

let getCache = () => {
    return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'GET', 200);
};

let addForwarder = (forwarder) => {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 200 || ret === 201); }, forwarder);
};

let addChannel = (channel) => {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 200 || ret === 201); }, channel);
};

let addComputer = (computer) => {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 200 || ret === 201); }, computer);
};

let updateChannel = (channel) => {
    return getPromise('/mux/channels/' + channel.type, DEFAULT_TIMEOUT, 'PUT', 201, channel);
};

let updateComputer = (computer) => {
    return getPromise('/mux/computers/' + computer.type, DEFAULT_TIMEOUT, 'PUT', 201, computer);
};

let updateMuxVerbose = (value) => {
    return getPromise('/mux/mux-verbose/' + value, DEFAULT_TIMEOUT, 'PUT', (code) => {
        return code === 200 || code === 201;
    });
};

let resetDataCache = () => {
    return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'DELETE', (code) => {
        return code === 202 || code === 200 ||  code === 204;
    });
};

let deleteForwarder = (forwarder) => {
    return getPromise('/mux/forwarders/' + forwarder.type, DEFAULT_TIMEOUT, 'DELETE', 204, forwarder);
};

let deleteComputer = (computer) => {
    return getPromise('/mux/computers/' + computer.type, DEFAULT_TIMEOUT, 'DELETE', 204, computer);
};

let deleteChannel = (channel) => {
    return getPromise('/mux/channels/' + channel.type, DEFAULT_TIMEOUT, 'DELETE', 204, channel);
};

let setSpeedUnit = (speedUnit) => {
    return getPromise('/mux/events/change-speed-unit', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret >= 200 || ret < 300); }, {"speed-unit": speedUnit}, false);
};

let pushData = (flow) => {
    if (false && flowData.length < (INIT_SIZE - 1)) {
        flowData.splice(0, 1);
        flowData.push(new Tuple(flowData.length, flow));
    } else {
        flowData.push(new Tuple(flowData.length, flow));
    }
    document.getElementById("flow").innerText = (flow + " bytes/sec.");
    if (GRAPH_MAX_LEN !== undefined && flowData.length > GRAPH_MAX_LEN) {
        while (flowData.length > GRAPH_MAX_LEN) {
            flowData.splice(0, 1);
        }
    }
};

let protocolTest = () => {
    let postData = protocolTestFunc();
    postData.then((value) => {
        console.log(value);
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get protocol test status..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let forwarderStatus = () => {
    // No REST traffic for this one.
    let getData = getForwarderStatus();
    getData.then((value) => {
        let json = JSON.parse(value); // Like {"processing":false,"started":1501082121336}
        let status = json.processing;
        document.getElementById("forwarders-status").innerText = (status === true ? 'ON' : 'Paused');
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get the forwarders status..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        document.getElementById("forwarders-status").innerText = ('-');
    });
};

let dataVolume = () => {
    // No REST traffic for this one.
    document.getElementById('flow').style.cursor = 'progress';
    let getData = getVolume();
    getData.then((value) => {
        let json = JSON.parse(value); // Like { "nmea-bytes": 13469, "started": 1489004962194 }
        let currentTime = new Date().getTime();
        let elapsed = currentTime - json.started;
        let volume = json["nmea-bytes"];
        let flow = Math.round(volume / (elapsed / 1000));
        pushData(flow);
        document.getElementById('flow').style.cursor = 'auto';
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get the flow status..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        pushData(0);
        document.getElementById('flow').style.cursor = 'auto';
    });
};

let storedNMEA = "";

let stackNMEAData = (sentenceToAdd) => {
    storedNMEA += ((storedNMEA.length > 0 ? "\n" : "") + sentenceToAdd); // TODO Limit the size...
    let content = '<pre>' + storedNMEA + '</pre>';
    document.getElementById("inbound-data-div").innerHTML = content;
    document.getElementById("inbound-data-div").scrollTop = document.getElementById("inbound-data-div").scrollHeight;
};

let lastTimeStamp = 0;

let getLastNMEASentence = () => {
    // No REST traffic for this one.
    let getData = getLastSentence();
    getData.then((value) => {
        let json = JSON.parse(value); // Like { "nmea-bytes": 13469, "started": 1489004962194 }
        let lastString = json["last-data"];
        if (lastString !== null && lastString !== undefined) {
            lastString = lastString.trim();
        }
        let timestamp = json["timestamp"];
        if (timestamp > lastTimeStamp) {
            stackNMEAData(lastString);
            lastTimeStamp = timestamp;
//          console.log(lastString)
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get the last NMEA Data..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let serialPortList = () => {
    let before = new Date().getTime();
    let getData = getSerialPorts();
    getData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<h5>Available Serial Ports</h5>";
        if (json.length > 0) {
            html += "<table>";
            json.forEach((line, idx) => {
                html += ("<tr><td>" + line + "</td></tr>");
            });
            html += "</table>";
        } else {
            html += "<i>No Serial Port available</i>";
        }
        document.getElementById("lists").innerHTML = html;
        document.getElementById("diagram").style.display = 'none';
        document.getElementById("lists").style.display = 'block';
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get serial ports list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let buildList = (arr) => {
    let str = (arr) ? arr.toString() : "";
    return str;
};

let setRESTPayload = (payload, elapsed) => {
    if (typeof payload === 'string') {
        if (payload.length > 0) {
            RESTPayload = JSON.parse(payload);
        } else {
            RESTPayload = {};
        }
    } else {
        RESTPayload = payload;
    }
    if (true || showRESTData) { // Show anyways
        displayRawData(elapsed);
    }
};

let displayRawData = (elapsed) => {
    let stringified = JSON.stringify(RESTPayload, null, 2);
    storedHistory += ((storedHistory.length > 0 ? "\n" : "") + stringified);
    let content = '<pre>' + storedHistory + '</pre>';
    let elapsedContent = "\n";
    if (elapsed !== undefined) {
        elapsedContent = ('in ' + elapsed + " ms.\n");
    }
    document.getElementById("raw-data").innerHTML = content;
    document.getElementById("raw-data").scrollTop = document.getElementById("raw-data").scrollHeight;

    storedElapsed += elapsedContent;
    document.getElementById("rest-elapsed").innerHTML = ('<pre>' + storedElapsed + "</pre>");
    document.getElementById("rest-elapsed").scrollTop = document.getElementById("rest-elapsed").scrollHeight;
};

let displayRawDataOut = () => {
    if (document.getElementById("raw-data-out") !== undefined) {
        document.getElementById("raw-data-out").innerHTML = ('<pre>' + storedHistoryOut + '</pre>');
        if (document.getElementById("raw-data-out") !== undefined) {
            document.getElementById("raw-data-out").scrollTop = document.getElementById("raw-data-out").scrollHeight;
        }
    }
};

let clearRESTData = () => {
    RESTPayload = {};
    storedHistory = "";
    document.getElementById("raw-data").innerHTML = ("");
};

let clearRESTOutData = () => {
    storedHistoryOut = "";
    storedElapsed = "";
    document.getElementById("raw-data-out").innerHTML = "";
    document.getElementById("rest-elapsed").innerHTML = "";
};

let channelList = () => {
    let before = new Date().getTime();
    let getData = getChannels();
    getData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<h5>Reads from</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>Device filters</th><th>Sentence filters</th><th>verb.</th></tr>"
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>Name: " + json[i].file +
                    "<br>Archive ?: " + json[i].zip  +
                    "<br>Path in archive: " + json[i].pathInArchive  +
                    "<br>Between reads: " + json[i].pause + " ms" +
                    "<br>Loop: " + json[i].loop +
                    "</td><td valign='top'>" + buildList(json[i].deviceFilters) +
                    "</td><td valign='top'>" + buildList(json[i].sentenceFilters) +
                    "</td><td align='center' valign='top'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td valign='top'><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td valign='top'><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td valign='top'><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td valign='top'><b>ws</b></td><td> " + json[i].wsUri + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td valign='top'><b>rnd</b></td><td></td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bmp180':  // Obsolete
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bme280':  // Obsolete
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'lsm303':  // Obsolete
                    html += ("<tr><td><b>lsm303</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td>");
                    if (json[i].headingOffset !== undefined) {
                        html += ("<td>Heading Offset: " + json[i].headingOffset + "</td>");
                    }
                    if (json[i].readFrequency !== undefined) {
                        html += ("<td>Read Frequency: " + json[i].readFrequency + " ms</td>");
                    }
                    if (json[i].dampingSize !== undefined) {
                        html += ("<td>Damping Size: " + json[i].dampingSize + " elmts</td>");
                    }
                    html += "</tr>";
                    break;
                case 'zda':
                    html += ("<tr><td valign='top'><b>zda</b></td><td> Prefix: " + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'htu21df':  // Obsolete
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rest':
                    html += ("<tr><td valign='top'><b>rest</b></td><td>" + "Service: " + json[i].verb + " " + json[i].protocol + "://" + json[i].hostname + ":" + json[i].port + json[i].queryPath + (json[i].queryString ? json[i].queryString : "") + "  <br/>" +
                             "JQ syntax: " + json[i].jsonQueryString + "<br/>" +
                             "Frequency: " + json[i].frequency + "ms <br/>" +
                             (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        document.getElementById("lists").innerHTML = html;
        document.getElementById("diagram").style.display = 'none';
        document.getElementById("lists").style.display = 'block';
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get channels list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let forwarderList = () => {
    let before = new Date().getTime();
    let getData = getForwarders();
    getData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<h5>Writes to</h5>" + "<table>";
        html += "<tr><th>Type</th><th>Parameters</th></th></tr>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'file':
					if (json[i].timeBased === true) {
						html += ("<tr><td><b>file</b></td><td>(time based) " + json[i].radix + ", dir " + json[i].dir + ", split every " + json[i].split + ".</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
					} else {
						html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
					}
					if (json[i].filters) {
					    let filterList = json[i].filters.join(", ");
					    html += (`<tr><td></td><td>Filter(s): ${filterList}</td></tr>`);
					}
                    break;
                case 'serial':
                    html += ("<tr><td valign='top'><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td valign='top'><b>tcp</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'rest':
                    /*
                     "port": 8080,
                     "serverName": "192.168.42.6",
                     "verb": "POST",
                     "resource": "/whatever",
                     */
                    html += ("<tr><td valign='top'><b>rest</b></td><td>" + json[i].verb + " http://" + json[i].serverName + ":" + json[i].port + json[i].resource + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td>");
                    break;
                case 'gpsd':
                    html += ("<tr><td valign='top'><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td valign='top'><b>ws</b></td><td>" + json[i].wsUri + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                        "Port: " + json[i].port + "<br>" +
                        "Name: " + json[i].bindingName + "<br>" +
                        "Address: " + json[i].serverAddress +
                        "</td><td valign='top'><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td valign='top'><b>console</b></td><td></td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        document.getElementById("lists").innerHTML = html;
        document.getElementById("diagram").style.display = 'none';
        document.getElementById("lists").style.display = 'block';
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get forwarders list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let computerList = () => {
    let before = new Date().getTime();
    let getData = getComputers();
    getData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<h5>Computes and writes</h5>" + "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>verb.</th><th>act.</th></tr>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td>" +
                                 "<td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td>" +
                                 "<td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td>" +
                                 "<td></td>" + // Active placeholder
                                 "<td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td>" +
                             "</tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td>" +
                                 "<td valign='top'>" + json[i].cls + "</td>" +
                                 "<td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td>" +
                                 "<td valign='top' align='center'><input type='checkbox' title='active' onchange='manageComputerActive(this, " + JSON.stringify(json[i]) + ");'" + (json[i].active === true ? " checked" : "") + "></td>" +
                                 "<td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td>" +
                             "</tr>");
                    break;
            }
        }
        html += "</table>";
        document.getElementById("lists").innerHTML = html;
        document.getElementById("diagram").style.display = 'none';
        document.getElementById("lists").style.display = 'block';
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let buildTable = (context, markerFiles, channels, forwarders, computers) => {
    let html = "<table width='100%'>" +
        // Context and Markers
        "<tr><th  width='45%' colspan='2'>Context</th><th width='55%'>Available Markers Files</th></tr>" +
        "<tr><td colspan='2' style='vertical-align: top;'>" + context + "</td><td width='45%' style='vertical-align: top; overflow-x: scroll;'>" + markerFiles + "</td></tr>" +
        "</table>" +

        "<br/><br/>" +

        "<table width='100%'>" +
        // Data
        "<tr><th width='45%'>Pulled in from</th><th width='10%'></th><th width='45%'>Pushed out to</th></tr>" +
        "<tr><td valign='middle' align='center' rowspan='2' title='Channels'>" + channels + "</td>" +
        //      "<td valign='middle' align='center' rowspan='2'><b><i>MUX</i></b></td>" +
        "<td valign='middle' align='center' rowspan='2'><img src='images/antenna.png' width='32' height='32' alt='MUX' title='MUX'></td>" +
        "<td valign='middle' align='center' title='Forwarders'>" + forwarders + "</td></tr>" +
        "<tr><td valign='middle' align='center' title='Computers'>" + computers + "</td></tr>" +
        "</table>";
    return html;
};

let valueOrText = (value, ifEmpty) => {
    if (value === undefined || value === null || value.trim().length === 0) {
        return "<span style='color: lightgrey;'>" + ifEmpty + "</span>";
    } else {
        return value;
    }
};

let updateMarkerConfig = () => {

    let buttonList_toggle = document.querySelectorAll(".toggle-marker-config");
    buttonList_toggle.forEach(button => {
        button.style.display = 'none';
    });

    let buttonList_01 = document.querySelectorAll(".remove-marker-button");
    buttonList_01.forEach(button => {
        button.style.display = 'inline';
    });

    let buttonList_02 = document.querySelectorAll(".update-marker-button");
    buttonList_02.forEach(button => {
        button.style.display = 'inline';
    });

    let buttonList_03 = document.querySelectorAll(".add-marker-button");
    buttonList_03.forEach(button => {
        button.style.display = 'inline';
    });

    let nextWPselect = document.getElementById('currentWaypoint');
    nextWPselect.disabled = false;
};

let cancelMarkersUpdate = () => {
    let buttonList_toggle = document.querySelectorAll(".toggle-marker-config");
    buttonList_toggle.forEach(button => {
        button.style.display = 'inline';
    });

    let buttonList_01 = document.querySelectorAll(".remove-marker-button");
    buttonList_01.forEach(button => {
        button.style.display = 'none';
    });

    let buttonList_02 = document.querySelectorAll(".update-marker-button");
    buttonList_02.forEach(button => {
        button.style.display = 'none';
    });

    let markerList = document.getElementById('full-markers-list');
    markerList.childNodes.forEach(line => line.style.display = 'block');

    let buttonList_03 = document.querySelectorAll(".add-marker-button");
    buttonList_03.forEach(button => {
        button.style.display = 'none';
    });

    let markerFilesList = document.getElementById('available-markers');
    markerFilesList.childNodes.forEach(line => {
        line.style.display = 'block';
        line.style = '';
    });
    let nextWPselect = document.getElementById('currentWaypoint'); // TODO Reset to previous value
    nextWPselect.disabled = true;
}

let removeMarkerLine = (clickedButton) => {
    let theLine = clickedButton.parentNode;
    console.log(`Clicked: ${theLine.childNodes[0].textContent}`);
    theLine.parentElement.style.display = 'none';
};

let viewMarkers = (clickedButton) => {
    let theLine = clickedButton.parentNode;
    let markerFileName = theLine.childNodes[0].innerText;
    let mess = (`View requested on: ${markerFileName}`);
    console.log(mess);
    // alert(mess);
    // Redirect to another page, with the json equivalent of the yaml.
    let nextURL = `chartless.markers.admin.viewer.html?marker-file=${markerFileName}`;
    window.open(nextURL, "WPViewer");
}

let addMarkerFile = (clickedButton) => {
    let theLine = clickedButton.parentNode;
    console.log(`Clicked: ${theLine.childNodes[0].textContent}`);
    // theLine.style.display = 'none';
    theLine.style = 'color: red;';
    // Add line in markers list?
}

let updateMarkerList = () => { // The final one
    // Build the new list
    let newList = [];
    let markerList = document.getElementById('full-markers-list');
    markerList.childNodes.forEach(line => {
        if (line.style.display !== 'none') {
            newList.push(line.childNodes[0].firstChild.textContent.trim());
        }
    });
    let markerFilesList = document.getElementById('available-markers');
    markerFilesList.childNodes.forEach(line => {
        if (line.firstChild.style.color === 'red') {
            newList.push(line.childNodes[0].firstChild.textContent.trim());
        }
    });

    console.log("New file list:" );
    newList.forEach(el => console.log(`- ${el}`));

    // Update, POST /mux/reload
    let markerUpdater = updateMarkersConfig(newList);
    markerUpdater.then(value => {
        console.log(`After update ! ${value}`);
        // Reload at the end
        generateDiagram();
    }, (error, errMess) => {
        console.log(error);
        console.log(errMess);
        alert("Failed to update markers config");
        generateDiagram();
    });

    // Next Waypoint
    let nextWPselect = document.getElementById('currentWaypoint');
    let selectedValue = nextWPselect.options[nextWPselect.selectedIndex].value;
    console.log(`Selected waypoint: ${selectedValue}`);
    let wpUpdater = updateNextWaypoint(selectedValue); //  === "null" ? null : selectedValue);
    wpUpdater.then(value => {
        console.log(`After waypoint update ! ${value}`);
        // Reload at the end
        generateDiagram();
    }, (error, errMess) => {
        console.log(error);
        console.log(errMess);
        alert("Failed to update next waypoint");
        generateDiagram();
    });

};

let generateDiagram = () => {

    let nbPromises = 0;
    let contextTable = "";
    let markersTable = "";
    let channelTable = "";
    let forwarderTable = "";
    let computerTable = "";

    let getContextPromise = getMUXContext(); // March 2025
    getContextPromise.then( value => {
        let before = new Date().getTime();
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        console.log("Building Context Table");
        let html = "<table width='100%'>";
        // Mux name
        if (json['name']) {
            html += `<tr><td>${json['name']}</td></tr>`;
        }
        // Mux description
        if (json['description']) {
            html += `<tr><td><ul>`;
            json['description'].forEach(desc => {
                html += `<li>${desc}</li>`;
            });
            html += `</ul></td></tr>`;
        }
        // TODO Other stuff here (deviation file, declination, etc)

        // Used Markers and borders, waypoints
        if (json['markers'] || json['markerList']) {
            html += '<tr><td>';
            html += 'Markers and Borders:<br/>';
            html += '<ul id="full-markers-list">';
            if (json['markers']) {
                html += `<li><span>${json['markers']}  <button class='remove-marker-button' style='display: none;' onclick='removeMarkerLine(this);'>Remove</button></span> - <span> </span><button onclick='viewMarkers(this);' style='margin-left: 5px;'>View</button></li>`;
            }
            if (json['markerList']) {
                json['markerList'].forEach(marker => {
                    html += `<li><span>${marker[0]} <button class='remove-marker-button' style='display: none;' onclick='removeMarkerLine(this);'>Remove</button></span> - <span><i>${marker[1]}</i></span><button onclick='viewMarkers(this);' style='margin-left: 5px;'>View</button></li>`;
                })
            }
            // Current waypoint
            // if (json['currentWaypointName']) {
                html += `<li>` +
                            `<span>Current Waypoint: ` + // ${json['currentWaypointName']}</span>&nbsp;&nbsp;` +
                            `<select id="currentWaypoint" onchange="/*updateCurrentWaypoint(this);*/" disabled>` +
                                `<option value="null">None</option>`;
                json['waypointList'].forEach(waypoint => {
                        html += `<option value="${waypoint.id}"${waypoint.id === json['currentWaypointName'] ? " selected" : ""}>${waypoint.id}</option>`;
                });
                html +=     `</select>` +
                        `</li>`;
            // }

            html += '</ul>';
            html += '<button class="toggle-marker-config" onclick="updateMarkerConfig();">Update markers config?</button>';
            html += '<button class="update-marker-button" style="display: none;" onclick="updateMarkerList(); alert(\'Reload charts after markers update to see the updated waypoints.\');">Update</button>';
            html += '<button class="update-marker-button" style="display: none;" onclick="cancelMarkersUpdate();">Cancel</button>';
            html += '</td></tr>';
        }

        html += "</table>";
        contextTable = html;
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }

    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        } else {
            message = 'Failed to get the MUX Context';
        }
        contextTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get MUX Context..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });

    let getMarkerFilesPromise = getMarkerFiles(); // March 2025
    getMarkerFilesPromise.then( value => {
        let before = new Date().getTime();
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        console.log("Building Context Table");
        let html = "<table width='100%'>";
        html += "<tr><td><div style='max-height: 200px; overflow-y: scroll;'>";
        html += "<ul id='available-markers'>";
        json.forEach(fDesc => {
            html += `<li><span>${fDesc[0]} <button class='add-marker-button' style='display: none;' onclick='addMarkerFile(this);'>Add</button></span> - <span><i>${fDesc[1]}</i></span><button onclick='viewMarkers(this);' style='margin-left: 5px;'>View</button></li>`;
        });
        html += "</ul>";
        html == "</div></td></tr>";
        html += "</table>";
        markersTable = html;
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }

    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        } else {
            message = 'Failed to get the MUX Context';
        }
        contextTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get MUX Context..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });


    let getChannelPromise = getChannels();
    getChannelPromise.then((value) => {
        let before = new Date().getTime();
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<table>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>File: " + json[i].file +
                        "<br>Archive ?: " + json[i].zip  +
                        "<br>Path in archive: " + (json[i].pathInArchive ? json[i].pathInArchive : "-")  +
                        "<br>Between reads: " + json[i].pause + " ms" +
                        "<br>Loop: " + json[i].loop +
                        "</td><td valign='top'>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td valign='top'>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td valign='top'><b>serial</b></td><td>" + json[i].port + ":" + json[i].br +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td valign='top'><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td valign='top'><b>ws</b></td><td> " + json[i].wsUri +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td valign='top'><b>rnd</b></td><td></td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'bmp180': // Obsolete
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'bme280': // Obsolete
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'lsm303': // Obsolete
                    html += ("<tr><td><b>lsm303</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        ((json[i].headingOffset !== undefined && json[i].headingOffset !== 0) ? ("<td>Heading Offset: " + json[i].headingOffset + "</td>") : "") +
                        ((json[i].readFrequency !== undefined && json[i].readFrequency !== 0) ? ("<td>Read Frequency: " + json[i].readFrequency + " ms</td>") : "") +
                        ((json[i].dampingSize !== undefined && json[i].dampingSize !== 0) ? ("<td>Damping Size: " + json[i].dampingSize + " elmts</td>") : "") +
                        "</td></tr>");
                    break;
                case 'zda':
                    html += ("<tr><td valign='top'><b>zda</b></td><td>Prefix: " + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'htu21df': // Obsolete
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'rest':
                    html += ("<tr><td valign='top'><b>rest</b></td><td>" + "Service: " + json[i].verb + " " + json[i].protocol + "://" + json[i].hostname + ":" + json[i].port + json[i].queryPath + (json[i].queryString ? json[i].queryString : "") + "  <br/>" +
                            "JQ syntax: " + (json[i].jsonQueryString ? json[i].jsonQueryString : "-") + "<br/>" +
                            "Frequency: " + json[i].frequency + "ms <br/>" +
                            (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                            "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                            "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') + "</td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td>" + json[i].cls +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
            }
        }
        html += "</table>";
        channelTable = html;
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        } else {
            message = 'Failed to get the channels';
        }
        channelTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get channels list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });

    let getForwarderPromise = getForwarders();
    getForwarderPromise.then((value) => {
        let before = new Date().getTime();
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<table>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'file':
					if (json[i].timeBased === true) {
						html += ("<tr><td><b>file</b></td><td>(time based) " + json[i].radix + ", dir " + json[i].dir + ", split every " + json[i].split + ".</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
					} else {
						html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
					}
					if (json[i].filters) {
					    let filterList = json[i].filters.join(", ");
					    html += (`<tr><td></td><td>Filter(s): ${filterList}</td></tr>`);
					}
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                // case 'udp':
                //     html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                //     break;
                case 'rest':
                    /*
                     "port": 8080,
                     "serverName": "192.168.42.6",
                     "verb": "POST",
                     "resource": "/whatever",
                     */
                    html += ("<tr><td><b>rest</b></td><td>" + json[i].verb + " http://" + json[i].serverName + ":" + json[i].port + json[i].resource + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td>");
                    break;
                case 'gpsd':
                    html += ("<tr><td><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                        "Port: " + json[i].port + "<br>" +
                        "Name: " + json[i].bindingName + "<br>" +
                        "Address: " + json[i].serverAddress +
                        "</td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td>" + valueOrText('', 'No parameter') + "</td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td></tr>");
                    break;
            }
        }
        html += "</table>";
        forwarderTable = html;
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        } else {
            message = 'Failed to get the Forwarders';
        }
        forwarderTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get forwarders list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });

    let getComputerPromise = getComputers();
    getComputerPromise.then((value) => {
        let before = new Date().getTime();
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = "<table>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td></tr>");
                    break;
            }
        }
        html += "</table>";
        computerTable = html;
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        computerTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 5) {
            document.getElementById("diagram").innerHTML = (buildTable(contextTable, markersTable, channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let generateCache = () => {
    let before = new Date().getTime();
    let getData = getCache();
    getData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        let json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        let html = `<h5>NMEA Cache at <i>${new Date()}</i></h5>`;
        if (json) {
            html += "<div style='max-height: 150px; border: 1px solid silver; border-radius: 5px; overflow: auto;'>"
            html += "<pre>" + JSON.stringify(json, null, 2) + "</pre>";
            html += "</div>";
        } else {
            html += "<i>No Cache available</i>";
        }
        document.getElementById("lists").innerHTML = html;
        document.getElementById("diagram").style.display = 'none';
        document.getElementById("lists").style.display = 'block';
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to get the cache..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let createChannel = (channel) => {
    let before = new Date().getTime();
    let postData = addChannel(channel);
    postData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to create channel..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let createForwarder = (forwarder) => {
    let before = new Date().getTime();
    let postData = addForwarder(forwarder);
    postData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to create forwarder..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let createComputer = (computer) => {
    let before = new Date().getTime();
    let postData = addComputer(computer);
    postData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to create computer..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let removeChannel = (channel) => {
    let before = new Date().getTime();
    let deleteData = deleteChannel(channel);
    deleteData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to delete channel..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let removeForwarder = (channel) => {
    let before = new Date().getTime();
    let deleteData = deleteForwarder(channel);
    deleteData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to delete forwarder..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let removeComputer = (computer) => {
    let before = new Date().getTime();
    let deleteData = deleteComputer(computer);
    deleteData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to delete computer..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let changeChannel = (channel) => {
    let before = new Date().getTime();
    let putData = updateChannel(channel);
    putData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to update channel..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let changeComputer = (computer) => {
    let before = new Date().getTime();
    let putData = updateComputer(computer);
    putData.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to update computer..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let manageChannelVerbose = (cb, channel) => {
    console.log('Clicked checkbox on', channel, ' checked:', cb.checked);
    // PUT on the channel.
    channel.verbose = cb.checked;
    changeChannel(channel);
};

let manageComputerActive = (cb, computer) => {
    // PUT on the computer.
    computer.active = cb.checked;
    console.log('=> Clicked active checkbox on', computer, ' checked:', cb.checked);
    changeComputer(computer);
};


let manageComputerVerbose = (cb, computer) => {
    // PUT on the computer.
    computer.verbose = cb.checked;
    console.log('Clicked verbose checkbox on', computer, ' checked:', cb.checked);
    changeComputer(computer);
};

let manageMuxVerbose = (cb) => {
    let before = new Date().getTime();
    let updateMux = updateMuxVerbose(cb.checked ? 'on' : 'off');
    updateMux.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to update multiplexer..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let resetCache = () => {
    let before = new Date().getTime();
    let reset = resetDataCache();
    reset.then((value) => {
        let after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    }, (error, errMess) => {
        document.body.style.cursor = 'default';
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager.display("Failed to reset data cache..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let addChannelVisible = false;
let addForwarderVisible = false;
let addComputerVisible = false;

let showAddChannel = () => {
    addChannelVisible = !addChannelVisible;
    addForwarderVisible = false;
    addComputerVisible = false;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

let showAddForwarder = () => {
    addChannelVisible = false;
    addForwarderVisible = !addForwarderVisible;
    addComputerVisible = false;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

let showAddComputer = () => {
    addChannelVisible = false;
    addForwarderVisible = false;
    addComputerVisible = !addComputerVisible;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

let showDiv = (divId) => {
    let elmt = document.getElementById(divId);

    if (!('visible-div' in elmt.classList)) {
        elmt.classList.add('visible-div');
    }

    let newH = '100%';
    let newV = 'visible';
    let newO = '1';

    elmt.style.height = newH;
    elmt.style.visibility = newV;
    elmt.style.opacity = newO;
};

let hideDiv = (divId) => {
    let elmt = document.getElementById(divId);

    if (('visible-div' in elmt.classList)) {
        elmt.classList.remove('visible-div');
    }

    let newH = '0';
    let newV = 'hidden';
    let newO = '0';
    elmt.style.height = newH;
    elmt.style.visibility = newV;
    elmt.style.opacity = newO;
};

let showDivs = (channels, forwarders, computers) => {
    // console.log("Displaying divs: channels " + (channels === true ? 'X' : 'O') + " forwarders " + (forwarders === true ? 'X' : 'O') + " computers " + (computers === true ? 'X' : 'O'));
//   document.getElementById("add-channel").style.display = ((channels === true ? 'inline' : 'none'));
    if (channels === true) {
        showDiv("add-channel");
        document.getElementById("add-channel-icon").src = 'pandown.gif';
    } else {
        hideDiv("add-channel");
        document.getElementById("add-channel-icon").src = 'panright.gif';
    }
//  document.getElementById("add-forwarder").style.display = ((forwarders === true ? 'inline' : 'none'));
    if (forwarders === true) {
        showDiv("add-forwarder");
        document.getElementById("add-fwrd-icon").src = 'pandown.gif';
    } else {
        hideDiv("add-forwarder");
        document.getElementById("add-fwrd-icon").src = 'panright.gif';
    }
//  document.getElementById("add-computer").style.display = ((computers === true ? 'inline' : 'none'));
    if (computers === true) {
        showDiv("add-computer");
        document.getElementById("add-comp-icon").src = 'pandown.gif';
    } else {
        hideDiv("add-computer");
        document.getElementById("add-comp-icon").src = 'panright.gif';
    }
};

let decToSex = (val, ns_ew, withDeg) => {
    let absVal = Math.abs(val);
    let intValue = Math.floor(absVal);
    let dec = absVal - intValue;
    let i = intValue;
    dec *= 60;
//    let s = i + "°" + dec.toFixed(2) + "'";
//    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
    let s = "";
    if (val < 0) {
        s += (ns_ew === 'NS' ? 'S' : 'W');
    } else {
        s += (ns_ew === 'NS' ? 'N' : 'E');
    }
    s += " ";
    let sep = " ";
    if (withDeg === true) {
        sep = "°";
    }
//    s += i + "\"" + dec.toFixed(2) + "'";
    s += i + sep + dec.toFixed(2) + "'";
    return s;
};