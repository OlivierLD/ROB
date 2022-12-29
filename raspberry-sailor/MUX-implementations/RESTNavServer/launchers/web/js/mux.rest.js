let errManager = {
    display: alert
};
let RESTPayload = {};
let storedHistory = "";
let storedHistoryOut = "";
let storedElapsed = "";

const DEBUG = false;

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
            let mess = {code: 408, message: 'Timeout'};
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

let enableLogging = (b) => {
    return getPromise('/mux/mux-process/' + (b === true ? 'on' : 'off'), DEFAULT_TIMEOUT, 'PUT', 200, null, false);
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

let deleteLogFile = (logFile) => {
    return getPromise('/mux/log-files/' + logFile, DEFAULT_TIMEOUT, 'DELETE', 200, null, false);
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

let getChannels = () => {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'GET', 200);
};

let getForwarders = () => {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'GET', 200);
};

let getComputers = () => {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'GET', 200);
};

let addForwarder = (forwarder) => {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'POST', 200, forwarder);
};

let addChannel = (channel) => {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'POST', 200, channel);
};

let addComputer = (computer) => {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'POST', 200, computer);
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
    return getPromise('/mux/events/change-speed-unit', DEFAULT_TIMEOUT, 'POST', 200, {"speed-unit": speedUnit}, false);
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
    let str = (arr !== undefined) ? arr.toString() : "";
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
                    html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>Name: " + json[i].file + "<br>Between reads: " + json[i].pause + " ms" + "<br>Loop: " + json[i].loop + "</td><td valign='top'>" + buildList(json[i].deviceFilters) + "</td><td valign='top'>" + buildList(json[i].sentenceFilters) + "</td><td align='center' valign='top'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td valign='top'><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td><b>rnd</b></td><td></td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bmp180':
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bme280':
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'lsm303':
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
                    html += ("<tr><td><b>zda</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'htu21df':
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
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
                    html += ("<tr><td><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                        "Port: " + json[i].port + "<br>" +
                        "Name: " + json[i].bindingName + "<br>" +
                        "Address: " + json[i].serverAddress +
                        "</td><td valign='top'><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td></td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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
        html += "<tr><th>Type</th><th>Parameters</th><th>verb.</th></tr>";
        for (let i = 0; i < json.length; i++) {
            let type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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

let buildTable = (channels, forwarders, computers) => {
    let html = "<table width='100%'>" +
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

let generateDiagram = () => {

    let nbPromises = 0;
    let channelTable = "";
    let forwarderTable = "";
    let computerTable = "";

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
                    html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>File: " + json[i].file + "<br>Between reads: " + json[i].pause + " ms" + "<br>Loop: " + json[i].loop +
                        "</td><td valign='top'>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td valign='top'>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                        "</td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td><b>rnd</b></td><td></td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'bmp180':
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'bme280':
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'lsm303':
                    html += ("<tr><td><b>lsm303</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        ((json[i].headingOffset !== undefined && json[i].headingOffset !== 0) ? ("<td>Heading Offset: " + json[i].headingOffset + "</td>") : "") +
                        ((json[i].readFrequency !== undefined && json[i].readFrequency !== 0) ? ("<td>Read Frequency: " + json[i].readFrequency + " ms</td>") : "") +
                        ((json[i].dampingSize !== undefined && json[i].dampingSize !== 0) ? ("<td>Damping Size: " + json[i].dampingSize + " elmts</td>") : "") +
                        "</td></tr>");
                    break;
                case 'zda':
                    html += ("<tr><td><b>zda</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                case 'htu21df':
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls +
                        "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                        "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                        "</td></tr>");
                    break;
            }
        }
        html += "</table>";
        channelTable = html;
        nbPromises += 1;
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
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
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
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
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
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
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
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
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
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
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
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
        if (nbPromises === 3) {
            document.getElementById("diagram").innerHTML = (buildTable(channelTable, forwarderTable, computerTable));
            document.getElementById("diagram").style.display = 'block';
            document.getElementById("lists").style.display = 'none';
        }
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? JSON.stringify(error) : ' - ') + ', ' + (message !== undefined ? message : ' - '));
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

let manageComputerVerbose = (cb, computer) => {
    console.log('Clicked checkbox on', computer, ' checked:', cb.checked);
    // PUT on the channel.
    computer.verbose = cb.checked;
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

    elmt.classList.toggle('visible-div');

    let newH = '100%';
    let newV = 'visible';
    let newO = '1';

    elmt.style.height = newH;
    elmt.style.visibility = newV;
    elmt.style.opacity = newO;
};

let hideDiv = (divId) => {
    let elmt = document.getElementById(divId);

    elmt.classList.toggle('visible-div');

    let newH = '0';
    let newV = 'hidden';
    let newO = '0';
    elmt.style.height = newH;
    elmt.style.visibility = newV;
    elmt.style.opacity = newO;
};

let showDivs = (channels, forwarders, computers) => {
//  console.log("Displaying divs: channels " + (channels === true ? 'X' : 'O') + " forwarders " + (forwarders === true ? 'X' : 'O') + " computers " + (computers === true ? 'X' : 'O'));
//   document.getElementById("add-channel").style.display = ((channels === true ? 'inline' : 'none'));
    if (channels === true) {
        showDiv("add-channel");
    } else {
        hideDiv("add-channel");
    }
//  document.getElementById("add-forwarder").style.display = ((forwarders === true ? 'inline' : 'none'));
    if (forwarders === true) {
        showDiv("add-forwarder");
    } else {
        hideDiv("add-forwarder");
    }
//  document.getElementById("add-computer").style.display = ((computers === true ? 'inline' : 'none'));
    if (computers === true) {
        showDiv("add-computer");
    } else {
        hideDiv("add-computer");
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
