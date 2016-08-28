
if (!window.Utils)
    Utils = {};

var service = new function () {
    this._url = "";
    this._httpsUrl = "";
    this.logsEnabled = false;
    this.path = document.URL; // context path
    this.loaded = false; // czy dokument jest załadowany
    this.unloading = false;
    this.standby = false;
    this.username = "";
    this.requestId = "";
    var stby = function (data) {
        ajax.post("/", {
            silent: true,
            skipLog: true,
            contentType: "application/javascript",
            post: data,
            headers: {
                "standby-request-id": service.requestId
            }
        }, function (response) {
            var cancel = false;
            var result;
            try {
                if (!response.error) {
                    var json = response.responseText;
                    if (!json)
                        return;
                    json = JSON.parse(json);
                    cancel = json.cancel;
                    if (json.data)
                        result = eval(json.data);
                }
            } finally {
                if (!cancel)
                    setTimeout(function () {
                        stby(result);
                    }, response.error ? 1000 : 1);
            }
        });


    };


    addEvent(window, "load", function () {
        service.loaded = true;
        if (!window.$svropt)
            return;

        var flags = $svropt.flags || "";

        service._url = $svropt.url;
        service._httpsUrl = $svropt.https;
        service.debugMode = flags.contains("D");
        service.standby = flags.contains("S");
        service.username = $svropt.user;
        service.requestId = $svropt.reqId;
        service.logsEnabled = flags.contains("L");
        if ($svropt.keepAlive)
            service.keepAlive($svropt.keepAlive);

        if (service.standby)
            stby();
    });

    addEvent(window, "beforeunload", function () {
        service.unloading = true;
    });

    if (this.path.indexOf("?") > 0)
        this.path = this.path.substring(0, this.path.indexOf("?"));

    this.url = function (url) {
        if (url.startsWith("?")
                || url.indexOf('http://') === 0
                || url.indexOf('https://') === 0)
            return url;
        return this._url + url;
    };

    var keepAliveTimeout;
    // podtrzymanie sesji
    this.keepAlive = function (time) {
        // time - czas w sekundach

        if (!time || isNaN(time) || time < 0)
            time = 300;

        clearTimeout(keepAliveTimeout);
        keepAliveTimeout = window.setTimeout(function () {
            ajax.post(location.href, {
                headers: {"ajax-keep-alive": "NOP"},
                silent: true,
                skipLog: true,
                busy: null,
                onError: function () {
                }
            }, function () {
                service.keepAlive(time);
            });

        }, time * 1000);
    };

    this.log = function (tag, log) {
        sendLog("debug", tag, log);
    };

    this.logWarning = function (tag, log) {
        sendLog("warning", tag, log);
    };

    this.logError = function (tag, log) {
        sendLog("error", tag, log);
    };

    function sendLog(kind, tag, val) {
        if (!service.logsEnabled || !service.loaded)
            return;
        if (val === undefined) {
            val = tag;
            tag = "";
        }

        var d = new Date();
        function add(val, len) {
            val = val.toString();
            while (val.length < len) {
                val = "0" + val;
            }
            return val;
        }

        d = add(d.getFullYear(), 4) + "-" +
                add(d.getMonth() + 1, 2) + "-"
                + add(d.getDate(), 2) + " "
                + add(d.getHours(), 2) + ":"
                + add(d.getMinutes(), 2) + ":"
                + add(d.getSeconds(), 2) + "."
                + add(d.getMilliseconds(), 3);
        ajax.post("$?addLog", {
            silent: true,
            skipLog: true,
            knd: kind,
            dte: d,
            tag: tag,
            val: val
        });
    }

    this.layer = function (url, post) {
// jesli warstwa tworzona jest w juz instniejacej
        if (document.layer === undefined)
            return new Layer(this.url(url), post);
        else
            return new top.Layer(this.url(url), post);
    };

    this.error = function (source) {
        if (!service.loaded)
            return;
        var err = (source instanceof EError) ? source : new EError(source);
        if (err.id && err.id !== "" && typeof Layer !== 'undefined') {
            var layer = this.layer("$?error=" + err.id);
            layer.onerror = function (e) {
                service.errorInternal(err.message);
            };
        } else
            this.errorInternal(source);
    };

    this.errorInternal = function (message, extraHtml) {
        var err = (message instanceof EError) ? message : new EError(message);
        if (typeof top.Layer === 'undefined') {
            alert(err.message);
            return;
        }
        new top.Layer(null, null).message("com/res/error.png",
                "Błąd", err.message, extraHtml);
    };

    this.info = function (message, extraHtml) {
        if (typeof Layer === 'undefined') {
            alert(message);
            return;
        }
        new top.Layer(null, null).message("com/res/info.png",
                "Informacja", message, extraHtml);
    };

    this.warning = function (message, extraHtml) {
        if (typeof Layer === 'undefined') {
            alert(message);
            return;
        }
        new top.Layer(null, null).message("com/res/warning.png",
                "Ostrzeżenie", message, extraHtml);
    };

    this.logout = function (reloadPage) {
        ajax.get("$logout", {}, function (http) {
            if (reloadPage === undefined || reloadPage === true)
                window.location.reload();
        });
    };

    this.debugWindow = function (popup) {
        if (popup)
            showPopupWindow(this.url("$status"), 1024, 600);
        else
            load(this.url("$status"), true);
    };

    this.logsWindow = function (popup) {
        if (popup)
            showPopupWindow(this.url("$logs"), 1024, 600);
        else
            load(this.url("$logs"), true);
    };

    this.userFiles = function (popup) {
        if (popup)
            showPopupWindow(this.url("$userFiles"), 1024, 600);
        else
            load(this.url("$userFiles"), true);
    };

    this.popupWindow = function (url, w, h) {
        showPopupWindow(this.url(url), w, h);
    };

    this.page = function (url, newWindow) {
        if (newWindow)
            window.open(this.url(url), '_blank');
        else
            window.location.href = this.url(url);
    };

    this.downloadAsZip = function (cacheIds, includeContentSize, fileName) {
        var url = new UrlBuilder();
        for (var i = 0; i < cacheIds.length; i++)
            if (cacheIds[i] && cacheIds[i].length === 26)
                url.add("cid", cacheIds[i]);
        if (url.items.length === 0)
            return;
        window.location.href = this.url("$?getZip&")
                + (includeContentSize ? "contentSize&" : "")
                + (fileName ? +"&fileName=" + escapeUrl(fileName) : "")
                + url();
    };

    // sprawdza czy istnieje plik, następnie pobiera lub zwraca błąd 
    this.download = function (cacheId) {
        ajax.get("$?checkCache=" + escapeUrl(cacheId), {},
                function (http) {
                    if (!http.error && http.responseText === "OK" + cacheId)
                        window.location = service.url("$?get=" + escapeUrl(cacheId));
                });
    };

    this.get = this.download;

    this.postAndDownload = function (url, post) {
        // wykonaj i pobierz jako cache data
        ajax.post(url, {
            busy: "Proszę czekać...",
            post: post
        }, function (http) {
            if (http.status !== 200)
                return;
            var cid = http.getResponseHeader("Cached-File-Id");
            if (cid)
                service.download(cid);
        }, false);
    };

    this.load = function (url, newWindow) {
        return load(this.url(url), newWindow);
    };
    // powtwierdzenie, wykonanie żądania i przeładowanie strony

    this.confirmAndReload = function (message, url, post, expectedResponse) {
        if (confirm(message)) {
            this.postAndReload(url, post, expectedResponse);
        }
    };

    this.postAndReload = function (url, post, expectedResponse) {
        if (!post)
            post = "";
        return ajax.post(url, {
            post: post,
            reload: true
        }, function () {
            if (this.error)
                return;
            if (expectedResponse && this.responseText !== expectedResponse) {
                service.error("Nieprawidłowa odpowiedź");
                return false;
            }
            return true;
        }, false);
    };

    this.submitAndReload = function (formId, onClick) {
        if (onClick)
            if (this.window[onClick](this) === false)
                return;
        var frm = $id(formId);
        var post = $(frm).serialize();
        this.postAndReload(frm.action, post);
    };

    this.removeFile = function (id, sync) {
        return sync ? ajax.getSync("$?remove=" + escape(id))
                === "remove ACK " + id
                : ajax.get("$?remove=" + id);
    };

    this.previewFile = function (id, name, newWindow) {
        ajax.get("$?checkCache=" + escape(id), {}, function (http) {
            if (!http.error) {
                load(service.url("$preview/"
                        + escape(id) + "/" + escapeUrl(name)), newWindow);
            }
        }
        );
    };

    this.previewAsHex = function (id, newWindow) {
        load(this.url("$hex?") + id, newWindow);
    };

    this.previewAsText = function (id, newWindow) {
        load(this.url("$text?") + id, newWindow);
    };

    this.ping = function () {
        var tt = new Date().getTime();
        var s = http.getSync("$?pingTest");
        if (s !== "PingACK")
            return "";
        return new Date().getTime() - tt;
    };

    this.logAjaxError = function (http) {
        if (!(http instanceof XMLHttpRequest)
                || !this.reportJsErrors
                || http.status === 0)
            return;
        var url = http.url;
        if (!url && http.responseXML)
            url = http.responseXML.URL;
        ajax.post("$?ajaxError", {
            msg: http.status + ", " + http.statusText + (url ? ", " + url : "")
        });
    };
    /*
     * obiekt będzie serializowany i zapisywany w konfiguracji
     */

    this.setConfig = function (objectName) {
        // objectName: nazwa zmiennej

        // metody obiektu:
        //  - onBeforeSave

        var opt = window.localStorage.getItem(objectName);

        var object = window[objectName];
        if (!object)
            throw "Nie znaleziono obiektu \"" + objectName + "\"";

        if (opt)
            try {
                opt = window.JSON.parse(opt);

                for (var s in object) {
                    if (opt[s] !== undefined)
                        object[s] = opt[s];
                }
            } catch (e) {
                service.logError("JS config", e);
            }

        addEvent(window, "beforeunload", function () {
            var object = window[objectName];
            if (!object)
                return;
            if (object.onBeforeSave && typeof object.onBeforeSave === "function")
                object.onBeforeSave();

            var field = false;
            var obj = {};
            for (var s in object) {
                if (typeof obj[s] !== "function") {
                    field = true;
                    obj[s] = object[s];
                }
            }
            if (field)
                window.localStorage.setItem(objectName, window.JSON.stringify(obj));
        }, false);
    };

    this.uicallback = function (sender, id) {
        ajax.post(id, {}, function (resp) {
            if (resp.error)
                return;

            var json = JSON.parse(resp.responseText);

            if (json.data)
                eval(json.data);

        });
    };
};

function GrayOut(data) {

    if (!data)
        data = {};

    var wnd = window.layer ? top : window;

    this.tag = getVal(data.parent, wnd.document.body).tag("div");
    var color = getVal(data.color, "#222");
    var time = getVal(data.time, 300); // czas animacji
    var delay = getVal(data.delay, 1);
    var opacity = getVal(data.opacity, 0.2);
    var zIndex = getVal(data.zIndex, null);

    var tr = "opacity " + time + "ms ease-in-out";
    this.tag.transitionTime = time;
    this.tag.id = "GrayOut_" + randomId(5);

    addEvent(this.tag, "mousedown", function () {
        return false;
    });
    addEvent(this.tag, "contextmenu", function () {
        return false;
    });
    var s = this.tag.style;
    s.backgroundColor = color;
    s.opacity = 0;
    s.position = "fixed";
    s.top = 0;
    s.left = 0;
    s.right = 0;
    s.bottom = 0;
    s.webkitUserSelect = "none";
    s.MozUserSelect = "none";
    s.userSelect = "none";
    s.webkitTransition = tr;
    s.MozTransition = tr;
    s.oTransition = tr;
    s.transition = tr;
    if (zIndex)
        s.zIndex = zIndex;

    var fadeInTimeout = window.setTimeout(function (grayout, opacity) {
        grayout.tag.style.opacity = opacity;
    }, delay, this, opacity);
    this.hide = function (force) {
        clearTimeout(fadeInTimeout);
        if (force) {
            this.tag.remove();
            return;
        }
        var grayout = this;
        this.tag.onTransitionEnd(function () {
            grayout.tag.remove();
        });
        setTimeout(function () {
            grayout.tag.remove();
        }, this.tag.transitionTime + 10);
        this.tag.style.opacity = 0;
    };
}

function Busy(data) {
    var busy = this;
    var lblText;
    if (typeof data === "string") {
        lblText = data;
        data = {};
    }

    if (!data)
        data = {};

    var wnd = window.layer ? top : window;
    var tag = getVal(data.parent, wnd.document.body).tag("div");
    var time = getVal(data.time, 700);
    var zIndex = getVal(data.zIndex, null);


    var delay = getVal(data.delay, 100); // opoznienie
    if (data.grayout === undefined)
        data.grayout = {
            parent: tag,
            color: "#222",
            opacity: 0.2,
            time: time,
            delay: delay
        };
    this.tag = tag;
    this.tag.id = "Busy_" + randomId(10);

    if (data.grayout)
        this.grayout = new GrayOut(data.grayout);

    var matrix = [
        "M2301 1783l0 0c-109,63 -251,25 -314,-84l-574 -994c-63,-109 -25,-251 84,"
                + "-314l0 0c109,-63 251,-25 314,84l574 994c63,109 25,251 -84,314z",
        "M1782 2303l0 0c-63,109 -204,147 -314,84l-994 -574c-109,-63 -147,-204 -84,"
                + "-314l0 0c63,-109 204,-147 314,-84l994 574c109,63 147,204 84,314z",
        "M0 2970l0 0c0,-126 103,-230 230,-230l1148 0c126,0 230,103 230,230l0 0c0,"
                + "126 -103,230 -230,230l-1148 0c-126,0 -230,-103 -230,-230z",
        "M1788 3649l0 0c-63,-109 -204,-147 -314,-84l-994 574c-109,63 -147,204 -84,"
                + "314l0 0c63,109 204,147 314,84l994 -574c109,-63 147,-204 84,-314z",
        "M2286 4150l0 0c-109,-63 -251,-25 -314,84l-574 994c-63,109 -25,251 84,314l0"
                + " 0c109,63 251,25 314,-84l574 -994c63,-109 25,-251 -84,-314z",
        "M2970 4334l0 0c126,0 230,103 230,230l0 1148c0,126 -103,230 -230,230l0"
                + " 0c-126,0 -230,-103 -230,-230l0 -1148c0,-126 103,-230 230,-230z",
        "M3647 4154l0 0c109,-63 251,-25 314,84l574 994c63,109 25,251 -84,314l0 "
                + "0c-109,63 -251,25 -314,-84l-574 -994c-63,-109 -25,-251 84,-314z",
        "M4150 3654l0 0c63,-109 204,-147 314,-84l994 574c109,63 147,204 84,314l0 "
                + "0c-63,109 -204,147 -314,84l-994 -574c-109,-63 -147,-204 -84,-314z",
        "M4334 2970l0 0c0,-126 103,-230 230,-230l1148 0c126,0 230,103 230,230l0 "
                + "0c0,126 -103,230 -230,230l-1148 0c-126,0 -230,-103 -230,-230z",
        "M4149 2285l0 0c63,109 204,147 314,84l994 -574c109,-63 147,-204 84,-314l0 "
                + "0c-63,-109 -204,-147 -314,-84l-994 574c-109,63 -147,204 -84,314z",
        "M3654 1791l0 0c109,63 251,25 314,-84l574 -994c63,-109 25,-251 -84,-314l0 "
                + "0c-109,-63 -251,-25 -314,84l-574 994c-63,109 -25,251 84,314z",
        "M2970 0l0 0c126,0 230,103 230,230l0 1148c0,126 -103,230 -230,230l0 0c-126,"
                + "0 -230,-103 -230,-230l0 -1148c0,-126 103,-230 230,-230z"];

    var tr = "opacity " + time + "ms ease-in-out";
    var s = this.tag.style;
    s.opacity = "0";
    s.position = "fixed";
    s.left = "50%";
    s.top = "50%";
    s.textAlign = "center";
    s.webkitUserSelect = "none";
    s.MozUserSelect = "none";
    s.userSelect = "none";
    s.webkitTransition = tr;
    s.MozTransition = tr;
    s.oTransition = tr;
    s.transition = tr;
    if (zIndex)
        s.zIndex = zIndex;

    var svg = this.svg = this.tag.tagNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("width", "52px");
    svg.setAttribute("height", "52px");
    svg.setAttribute("viewBox", "0 0 5941 5941");
    var paths = [];
    for (var i = 0; i < matrix.length; i++) {
        var p = svg.tagNS("http://www.w3.org/2000/svg", "path");
        paths.push(p);
        p.setAttribute("fill", "#CCCCCC");
        p.setAttribute("fill-opacity", "0.8");
        p.setAttribute("d", matrix[i]);
    }

    var draw = function () {
        paths.unshift(paths.pop());
        for (var i = 0; i < paths.length; i++) {
            var s = (i + 2).toString(16);
            paths[i].setAttribute("fill", i < 10 ? "#" + s + s + s : "#bbb");
        }

        window.setTimeout(function () {
            draw();
        }, 50);
    };
    this.lbl = this.tag.tag("label");
    if (lblText)
        this.lbl.text(lblText);
    var s = this.lbl.style;
    s.display = "none";
    s.marginTop = "8px";
    s.font = "11pt Verdana";
    s.color = "#000";
    s.textShadow = "0 0 3px #888";
    window.setTimeout(function () {
        busy.tag.style.opacity = "1";
        draw();
    }, delay);
    var label = typeof data === "string" ? data : data.label;
    if (label)
        this.setLabel(label);
    var delayTimeout = window.setTimeout(function (busy) {
        busy.tag.style.display = "block";
        busy.tag.style.marginLeft =
                Math.round(-busy.tag.clientWidth / 2) + "px";
        busy.tag.style.marginTop =
                Math.round(-busy.tag.clientHeight / 2) + "px";
    }, delay, this);
    this.setLabel = function (label) {
        this.lbl.text(label);
        this.lbl.style.display = "block";
        this.tag.style.marginLeft =
                Math.round(-this.tag.clientWidth / 2) + "px";
        this.tag.style.marginTop =
                Math.round(-this.tag.clientHeight / 2) + "px";
    };
    this.hide = function (force) {
        clearTimeout(delayTimeout);
        this.tag.remove();
        if (this.grayout)
            this.grayout.hide(force);
    };
}

var ajax = new function () {

    var _data = {
        busy: {}, // wyswietl animacje oczekiwania        
        params: {},
        post: undefined, // dane, które będą bezpośrednio przekazane 
        // zigonorowane zostana pozostale parametry, nalezy zdefiniowac contentType
        skipLog: false,
        contentType: null,
        reload: false, // przeładuj po odebranu pozytywnej odpowiedzi
        method: "POST",
        silent: false, // tryb cichy: nie pokazuj komunikatow, dzialanie w tle
        onError: null,
        onAbort: null, // zdarzenie anulowania
        onProgress: null,
        window: null, // jeśli wywołujemy metodę w ramce, 
        //czy wykonywać przekierowania oraz inne elementy w stosunku do okna glownego
        error: null, // EError
        form: null,
        headers: {}
    };

    /*
     * http.error - flaga bledu
     * http.busy - obiekt Busy   
     */

    var send = function (url, postMethod, async, data, onResponse) {
        var ajax = createHttpRequestObject();
        if (!ajax)
            return;
        if (typeof data === "string") {
            url = data;
            data = {};
        }

        if (!data)
            data = {};


        data.window = getVal(data.window, window);

        ajax.data = data;
        ajax.onResponse = onResponse;

        if (data.busy === undefined && !data.silent && data.busy !== false)
            data.busy = {};

        if (data.busy)
            ajax.busy = new Busy(data.busy);

        ajax.onerror = function (e) {
            this.error = true;
            if (this.data && this.data.silent)
                return;
            data.window.service.error(e);
        };
        ajax.ontimeout = function () {
            this.error = true;
            data.window.service.error("Prezekroczono limit czasu odpowiedzi serwera");
        };
        ajax.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.busy)
                    this.busy.hide();

                this.headers = {};
                this.contentType = null;

                var hdrs = this.getAllResponseHeaders().split("\n");
                for (var i = 0; i < hdrs.length; i++) {
                    var arr = hdrs[i].split(":");
                    if (arr.length === 2)
                        this.headers[arr[0].trim()] = arr[1].trim();
                }

                for (var hdr in this.headers)
                    if (hdr.same("content-type"))
                        this.contentType = this.headers[hdr];

                // przytnij content-type jesli zawiera np ";charset=utf8"
                if (this.contentType && this.contentType.contains(";"))
                    this.contentType = this.contentType.substring(0,
                            this.contentType.indexOf(";")).trim();


                var newPage = this.getResponseHeader("ajax-new-page");
                if (newPage)
                    this.data.window.load(decodeURIComponent(newPage), true);
                var redir = this.getResponseHeader("ajax-redirect-page");
                if (redir) {
                    this.data.window.load(decodeURIComponent(redir), false);
                    return;
                }

                if (!this.error) {
                    // aby nie wywolywal wielokrotnie zdarzenia obslugi bledow
                    if (this.status < 200 || this.status > 299)
                        this.error = new EError(this);

                    if (this.error && this.data.onError)
                        this.data.onError(this);

                    if (!this.aborted && this.error && !this.data.silent)
                        this.data.window.service.error(this.error);
                }

                if (this.onResponse && typeof this.onResponse === "function")
                    if (this.onResponse(this) === false)
                        return;

                if (!this.error && (this.data.reload || this.getResponseHeader("ajax-reload-page")))
                    this.data.window.location.reload();
            }
        };
        var _abort = XMLHttpRequest.prototype;
        ajax.abort = function () {
            ajax.aborted = true;
            _abort.abort.call(this);
        };
        ajax.onabort = function () {
            this.aborted = true;
            if (this.data.onAbort)
                this.data.onAbort(this);
        };
        if (ajax.upload && ajax.data.onProgress) {
            ajax.upload.prevTimeStamp = new Date().getTime();
            ajax.upload.prevPos = 0;
            ajax.upload.loaded = 0;
            ajax.upload.speed = 0;
            ajax.upload.data = ajax.data;
            ajax.upload.onloadend = function () {

            };
            ajax.upload.addEventListener("progress", function (e) {
                try {
                    e.timeDiff = new Date().getTime() - this.prevTimeStamp;
                    e.dataDiff = e.loaded - this.prevPos;
                    if (e.timeDiff > 500) {
                        this.speed = (1000 * e.dataDiff / e.timeDiff);
                        this.prevTimeStamp = new Date().getTime();
                        this.prevPos = e.loaded;
                    }
                    e.speed = this.speed;
                    e.diff = e.loaded - this.loaded;
                    this.loaded = e.loaded;
                    if (this.data.onProgress)
                        this.data.onProgress(e);
                } catch (e) {
                    if (this.data.window.onerror)
                        this.data.window.onerror(e.message);
                    else
                        console.error(e.message);
                }
            }, false);
        }

        ajax.open(postMethod ? "POST" : "GET", service.url(url), async);
        ajax.setRequestHeader("ajax-request", "true");
        if (data.skipLog)
            ajax.setRequestHeader("ajax-skip-request-log", "true");

        var ct = data.contentType;

        if (data.headers)
            for (var hdr in data.headers) {
                if (!ct && hdr && hdr.same("content-type"))
                    ct = data.headers[hdr];
                else
                    ajax.setRequestHeader(hdr, escapeUrl(data.headers[hdr]));
            }

        var post = data.post;

        var params = new UrlBuilder();

        for (var item in data) {
            // dodaj niestandardowe obiekty zdefiniowane w obiekcie data jako parametry
            var ok = true;
            for (var it in _data)
                if (it === item)
                    ok = false;
            if (!ok)
                continue;
            params.add(item, data[item]);
        }


        if (data.params)
            params.add(data.params);

        if (data.form)
            serializeForm(data.form, params);

        if (!post && postMethod) {
            if (!ct || data.form)
                ct = "application/x-www-form-urlencoded; charset=UTF-8";

            post = params.toString();
        } else
            ajax.setRequestHeader("Ajax-Params", params.toString());

        if (postMethod)
            ajax.setRequestHeader("Content-Type", ct);

        if (service.requestId)
            ajax.setRequestHeader("Parent-Request-Id", service.requestId);

        this.error = null;
        ajax.send(postMethod ? post : null);
        return ajax;
    };

    this.getSync = function (url, data, onResponse) {
        return send(url, false, false, data, onResponse).responseText;
    };

    this.getAsync = function (url, data, onResponse) {
        return send(url, false, true, data, onResponse);
    };

    this.postSync = function (url, data, onResponse) {
        return send(url, true, false, data, onResponse).responseText;
    };

    this.postAsync = function (url, data, onResponse) {
        return send(url, true, true, data, onResponse);
    };

    this.get = function (url, data, onResponse) {
        return this.getAsync(url, data, onResponse);
    };

    this.post = function (url, data, onResponse) {
        return this.postAsync(url, data, onResponse);
    };
};

function JsonSocket(url, connectWhenLoaded) {
    if (connectWhenLoaded === null || connectWhenLoaded === undefined)
        connectWhenLoaded = true;
    this.socket;
    this.onmessage;
    this.onerror;
    this.onopen;
    this.onclose;
    this.url;
    this.closedManually = false;
    this.error = null;
    this.wasConnected = false;
    this.autoReconnect = false; // automatycznie proboj wznowic polaczenie po utracie
    var retryCounter = 5;
    var ws;
    var self = this;
    var responses = [];
    this.url = url;
    this.errorBox; // okno błędu

    this.close = function () {
        this.closedManually = true;
        this.socket.close();
    };

    this._initWS = function () {
        var pre = service._url.replace("https://", "wss://").replace("http://", "ws://");
        if (pre.indexOf("?") > 0)
            pre = pre.substring(0, pre.indexOf("?"));
        if (pre.indexOf("#") > 0)
            pre = pre.substring(0, pre.indexOf("#"));

        ws = self.socket = window['MozWebSocket']
                ? new MozWebSocket(pre + this.url)
                : new WebSocket(pre + this.url);

        ws.onopen = function () {
            if (self.errorBox) {
                self.errorBox.close();
                self.errorBox = null;
            }

            if (self.onopen)
                self.onopen();
            retryCounter = 5;
            self.wasConnected = true;
        };

        ws.onclose = function () {
            if (service.unloading)
                return;
            if (self.onclose)
                self.onclose();

            if (!self.wasConnected)
                ws.onerror(null);

            if (!self.closedManually
                    && self.autoReconnect
                    && retryCounter > 0)
                setTimeout(function () {
                    self._initWS();
                    --retryCounter;
                }, 1000);
        };
        ws.onerror = function (e) {
            if (service.unloading)
                return;

            self.error = true;
            if (self.onerror)
                if (self.onerror(e) === true)
                    return;
            self.errorBox = new CenterBox({
                text: typeof e === "string" ? e :
                        (self.wasConnected ? "Utracono połączenie" : "Błąd połączenia")
                        + " WebSocket\n\n"
                        + this.url + (self.autoReconnect && retryCounter > 0
                                ? "\n\nPróba ponowienia: "
                                + (6 - retryCounter) : ""),
                details: self.wasConnected ? null : "* Przyczyną problemu może "
                        + "być zablokowanie połączenia przez\nprogram antywirusowy, "
                        + "firewall lub serwer pośredniczący\n\n"
                        + "Rozwiązaniem może być przełączenie w tryb szyfrowany (HTTPS)",
                error: true
            });
        };
        ws.onmessage = function (msg) {
            var data = JSON.parse(msg.data);
            if (data.exception) {
                ws.onerror(data.exception);
                return;
            }

            if (!data.data)
                return;
            if (self.onmessage)
                self.onmessage(data.data, msg);
            for (var i = 0; i < responses.length; i++)
                if (responses[i].uid === data.uid) {
                    if (responses[i].response)
                        responses[i].response(data.data, msg);
                    responses.splice(i, 1);
                    break;
                }
        };
    };
    if (connectWhenLoaded && !service.loaded)
        window.addEventListener("load", function () {
            self._initWS();
        });
    else
        this._initWS();
    this.send = function (action, data, onResponse) {

        var obj = {
            action: action,
            data: data,
            uid: randomId(20)
        };
        // zamykanie polaczenia
        if (this.socket.readyState > 1)
            return;
        if (this.socket.readyState === 0) {
            setTimeout(function (socket) {
                socket.send(action, data);
            }, 1, this);
            return;
        }

        this.socket.send(JSON.stringify(obj));
        responses.push({uid: obj.uid, response: onResponse});
    };
}

function SVG(parent, width, height, viewBox) {
    var svg = parent.tagNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("width", width);
    svg.setAttribute("height", height);
    svg.setAttribute("viewBox", viewBox);
    svg.tstyles = svg.tag("style");
    svg.tstyles.setAttribute("type", "text/css");
    svg.styles = function (selector) {
        if (!this.tstyles.innerHTML)
            this.tstyles.innerHTML = "";
        this.tstyles.innerHTML += selector + "\n";
    };
    svg.tag = function (name) {
        var tag = this.tagNS("http://www.w3.org/2000/svg", name);
        tag.attr = function (name, value) {
            this.setAttribute(name, value);
            return this;
        };
        return tag;
    };

    svg.polygon = function (points) {
        return this.tag("polygon").attr("points", points);
    };

    svg.path = function (points) {
        return this.tag("path").attr("d", points);
    };

    return svg;
}

/*  okno komunikatu na środku ekranu */
function CenterBox(data) {
    if (!data)
        return;
    var self = this;
    if (typeof data === "string")
        data = {
            text: data
        };

    var time = getVal(data.delay, 0); // czas wyswietlania komunikatu
    var error = getVal(data.error, false);
    var grayout = getVal(data.grayout, false);
    var closeable = getVal(data.closeable, true);
    var zIndex = getVal(data.zIndex, null);

    var box = $id("$_center_box");
    if (!box)
        box = document.body.tag("div");
    box.innerHTML = "";
    box.setAttribute("id", "$_center_box");

    box.tag("div").text(data.text);

    if (data.details) {
        box.tag("div")
                .text(data.details)
                .css({
                    marginTop: "20px",
                    fontSize: "8pt",
                    fontStyle: "italic",
                    color: "#333"
                });

    }

    box.css({
        font: "10pt Verdana",
        padding: "25px 30px",
        position: "fixed",
        minWidth: "30%",
        textAlign: "center",
        color: "#000",
        border: "1px solid " + (error ? "#a00" : "#060"),
        opacity: 0.9,
        boxShadow: "0 0 4px " + (error ? "#a00" : "#060"),
        textShadow: "0, 0, 2px, #fff",
        zIndex: zIndex,
        transition: "opacity .3s ease-in-out",
        borderRadius: "4px"
    });

    if (error)
        gradient(box, "#faa", "#f66");
    else
        gradient(box, "#efe", "#8f8");

    if (closeable) {
        var btn = Utils.closeButton(box, 16, 16);
        btn.style.position = "absolute";
        btn.style.right = "4px";
        btn.style.top = "4px";
        btn.style.cursor = "pointer";
        btn.onclick = function () {
            self.close();
        };
    }

    var setPos = function () {
        box.css({
            left: (window.innerWidth / 2 - box.offsetWidth / 2) + "px",
            top: (window.innerHeight / 2 - box.offsetHeight / 2) + "px"
        });
    };

    window.addEventListener("resize", function () {
        setPos();
    });

    setPos();


    if (time > 0)
        window.setTimeout(function () {
            self.close();
        }, time, this);

    this.close = function () {
        box.style.opacity = 0;
        window.setTimeout(function () {
            box.remove();
        }, 500);
    };
}


Utils.closeButton = function (parent, width, height) {
    /*
     * <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 260 260">
     * <path fill="#000000" d="M6 198l68 -68 -68 -68c-8,-8 -8,-21 0,-29l26 -26c8,-8 21,-8 29,0l68 68 68 -68c8,-8 21,-8 29,0l26 26c8,8 8,21 0,29l-68 68 68 68c8,8 8,21 0,29l-26 26c-8,8 -21,8 -29,0l-68 -68 -68 68c-8,8 -21,8 -29,0l-26 -26c-8,-8 -8,-21 0,-29z"/></svg>
     
     
     * 
     */

    width = getVal(width, 16);
    height = getVal(height, 16);

    var pre = parent.tag("div");

    var div = pre.tag("div");



    var svg = new SVG(div, (width - 5) + "px", (height - 5) + "px", "0 0 260 260");

    var p = svg.path("M6 198l68 -68 -68 -68c-8,-8 -8,-21 0,-29l26 -26c8,-8 21,-8 29,0l68 "
            + "68 68 -68c8,-8 21,-8 29,0l26 26c8,8 8,21 0,29l-68 68 68 68c8,8 "
            + "8,21 0,29l-26 26c-8,8 -21,8 -29,0l-68 -68 -68 68c-8,8 -21,8 "
            + "-29,0l-26 -26c-8,-8 -8,-21 0,-29z");


    div.onmouseover = function () {
        div.css({backgroundColor: "rgba(240,140,140,1)"});
    };

    div.onmouseout = function () {
        div.css({backgroundColor: "rgba(220,100,100,1)"});
        pre.css({padding: "1px"});
    };

    pre.onmousedown = function () {
        pre.css({padding: "2px 0 2px 0"});
    };

    pre.onmouseup = function () {
        pre.css({padding: "1px"});
    };

    pre.css({
        display: "inline-block",
        padding: "1px",
        margin: 0,
        lineHeight: 0
    });

    div.css({
        display: "inline-block",
        padding: "2px 6px",
        margin: 0,
        backgroundColor: "rgba(220,100,100,1)",
        opacity: 1,
        border: "1px solid #444",
        boxShadow: "1px 1px 2px rgba(0,0,0,0.4)",
        borderRadius: "2px",
        lineHeight: 0
    });

    p.css({
        fill: "#FFFFFF",
        pointerEvents: "all",
        stroke: "black",
        strokeWidth: "20"
    });



    return pre;
};