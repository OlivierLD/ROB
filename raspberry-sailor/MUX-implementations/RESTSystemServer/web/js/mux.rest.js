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

const DEFAULT_TIMEOUT = 10000; // 10s

let protocolTestFunc = () => {
    let url = document.location.origin.replace('http', 'mux') + '/this-is-a-test';
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

let terminate = () => {
    return getPromise('/system/stop-all', DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

let systemDate = () => {
    return getPromise('/system/system-date', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let updateSystemDate = (newFmtDate) => {
    return getPromise('/system/system-date', DEFAULT_TIMEOUT, 'POST', 201, newFmtDate, false);
};

let muxStatus = () => {
    return getPromise('/system/mux-stat', DEFAULT_TIMEOUT, 'GET', 201, null, false);
}

let startMux = () => {
    //                                     The script has a sleep...
    return getPromise('/system/start-mux', 2 * DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 201 || ret === 202); }, null, false);
};

let stopMux = () => {
    return getPromise('/system/stop-mux', DEFAULT_TIMEOUT, 'POST', (ret) => { return (ret === 201 || ret === 202); }, null, false);
}

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
