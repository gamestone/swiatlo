/**
 * Zwraca nazwę klasy obiektu
 * @param {type} object
 * @returns {undefined}
 */
function className(object) {
    if (typeof object !== "undefined"
            && object !== null
            && object.constructor
            && object.constructor.name)
        return object.constructor.name;
    return typeof object;
}

function getVal(object, def) {
    return object === null || object === undefined ? def : object;
}

HTMLInputElement.prototype.submit = function (busy) {
    if (busy === undefined)
        busy = true;

    if (window.Busy && busy)
        new Busy({});
    var frm = this.form;
    setTimeout(function () {
        frm.submit();
    }, 1);

};

String.prototype.replaceAll = function (src, dst) {
    var temp = this + ""; // aby nie tworzyl tablicy znakow tylko string
    var index = temp.indexOf(src);
    while (index !== -1) {
        temp = temp.replace(src, dst);
        index = temp.indexOf(src);
    }
    return temp;
};

String.prototype.contains = function (str) {
    if (!(typeof str === "string"))
        return false;
    return this.toLowerCase().indexOf(str.toLowerCase()) >= 0;
};

String.prototype.endsWith = function (suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function (suffix) {
    return this.indexOf(suffix) === 0;
};

/*
 * Zwraca true jeśli przycięty tekst jest identyczny bez uwzlędnienia wielkości znaków
 */
String.prototype.same = function (text) {
    if ((typeof text === 'undefined') || (text === null))
        return false;
    return this.trim().toLowerCase() === text.trim().toLowerCase();
};

Number.prototype.round = function (places, truncate) {
    var factor = Math.pow(10, places);
    return Math[Boolean(truncate) ? 'floor' : 'round'](this * factor) / factor;
};

Node.prototype.getParent = function (tagNameOrLevel) {

    if (tagNameOrLevel > 0) {
        var nd = this;
        for (var i = 0; i < tagNameOrLevel; i++) {
            if (!nd)
                return null;
            nd = nd.parentNode;
        }
        if (!nd)
            return null;

        return nd;
    }

    if (!tagNameOrLevel)
        return this;

    var nd = this;
    while (nd) {
        if (nd.nodeName.toLowerCase() === tagNameOrLevel.toLowerCase())
            return nd;
        nd = nd.parentNode;
    }
    return null;
};

Node.prototype.tag = function (tagName) {
    if (!tagName)
        return null;
    var t = document.createElement(tagName);
    this.appendChild(t);
    return t;
};

Node.prototype.tagNS = function (namespace, tagName) {
    var t = document.createElementNS(namespace, tagName);
    this.appendChild(t);
    return t;
};

Node.prototype.insertAfter = function (newNode) {
    this.parentNode.insertBefore(newNode, this.nextSibling);
};

Node.prototype.addTagFirst = function (tagName) {
// dodaj nowy tag jako pierwsze dziecko
    var tag = this.children[0];
    if (!tag)
        return this.tag(tagName);
    var tt = document.createElement(tagName);
    this.insertBefore(tt, tag);
    return tt;
};

Node.prototype.text = function (str) {
    this.innerHTML = escapeHTML(str, true);
    return this;
};

Node.prototype.linkify = function (str) {
    this.innerHTML = linkify(escapeHTML(str, true));
    return this;
};

Node.prototype.setId = function (id) {
    this.id = id;
    return this;
};

Node.prototype.cls = function (className) {
    this.className = className;
    return this;
};

Node.prototype.getInnerText = function () {
    return this.innerHTML.replace(/\&lt;br\&gt;/gi, "\n")
            .replace(/(&lt;([^&gt;]+)&gt;)/gi, "");
};

Node.prototype.remove = function () {
    this.parentNode.removeChild(this);
};

Node.prototype.onTransitionEnd = function (proc) {
    this.addEventListener('transitionend', proc, false);
    this.addEventListener('webkitTransitionEnd', proc, false);
    this.addEventListener('msTransitionEnd', proc, false);
    this.addEventListener('oTransitionEnd', proc, false);
};

/* 
 * pobiera pozycję absolutną obiektu w stosunku do okna nadrzędnego 
 * z uwzględnieniem wszystkich pośrednich obiektów iframe oraz zoom-u
 * includeScroll == false, gdy position = fixed
 */

Node.prototype.getAbsolutePos = function (includeScroll) {

    var x = 0;
    var y = 0;

    if (includeScroll === null || includeScroll === undefined)
        includeScroll = true;


    var getParentFrame = function (wnd) {
        var frames = wnd.parent.document.getElementsByTagName("IFRAME");
        for (var i = 0; i < frames.length; i++)
            if (frames[i].contentWindow === wnd)
                return frames[i];
        return null;
    };

    var wnd = this.ownerDocument.defaultView;
    var frame = this;

    while (frame) {
        var tag = frame;

        var rect = tag.getBoundingClientRect();
        var scrollTop = 0;
        var scrollLeft = 0;

        var zoom = 1;
        while (tag) {
            var st = getComputedStyle(tag);
            if (st)
                zoom *= parseFloat(st.zoom);
            tag = tag.parentNode;

            if (!includeScroll && tag && (tag.scrollTop || tag.scrollLeft)) {
                scrollTop += tag.scrollTop;
                scrollLeft += tag.scrollLeft;
            }
        }
        y += (rect.top + scrollTop) * zoom;
        x += (rect.left + scrollLeft) * zoom;


        frame = getParentFrame(wnd);
        wnd = wnd.parent;
    }


    return {
        x: x,
        y: y
    };

};


Element.prototype.forEach = function (func, elementsOnly) {
    if (!func)
        return;
    for (var i = 0; i < this.childNodes.length; i++)
        if (!elementsOnly || this.childNodes[i].nodeType === 1)
            func(this.childNodes[i]);
};


Array.prototype.clear = function () {
    this.length = 0;
};

Array.prototype.isEmpty = function () {
    return this.length === 0;
};

Array.prototype.contains = function (element) {
    return this.indexOf(element) >= 0;
};

Array.prototype.insert = function (index, item) {
    this.splice(index, 0, item);
};

Array.prototype.remove = function (obj) {
    for (var i = this.length - 1; i >= 0; i--)
        if (this[i] === obj)
            this.splice(i, 1);
};

/** Ogranicz liczbę lementów tablicy  */
Array.prototype.limit = function (itemsCount, fromBegin) {
    if (!this)
        return this;
    if (itemsCount > 0 && itemsCount <= this.length)
        this.splice(fromBegin ? 0 : this.length - itemsCount, this.length - itemsCount);
    return this;
};

Array.prototype.removeObj = function (obj) {
    var idx = this.indexOf(obj);
    if (idx < 0)
        return false;
    this.splice(idx, 1);
    return true;
};

Array.prototype.moveItem = function (index, newIndex) {
    if (index < 0 || index >= this.length || newIndex < 0 || newIndex >= this.length)
        return;
    if (newIndex >= this.length) {
        var k = newIndex - this.length;
        while ((k--) + 1) {
            this.push(undefined);
        }
    }
    this.splice(newIndex, 0, this.splice(index, 1)[0]);
};

Array.prototype.first = function () {
    if (this.length > 0)
        return this[0];
    return undefined;
};

Array.prototype.last = function () {
    if (this.length > 0)
        return this[this.length - 1];
    return undefined;
};

Element.prototype.setText = function (text) {
    this.textContent = text;
    return this;
};

Element.prototype.attr = function (data) {
    if (!data || (typeof data !== "object"))
        return this;

    for (var name in data) {
        var val = data[name];
        if (typeof val === "function")
            continue;

        if (val === undefined)
            this.removeAttribute(name);
        else
            this.setAttribute(name, val);
    }
    return this;
};

Element.prototype.css = function (data) {
    if (!data || (typeof data !== "object"))
        return this;

    for (var name in data) {
        var val = data[name];
        if (typeof val === "function")
            continue;

        if (this.style[name] === undefined)
            throw "Nieznany selektor \"" + name + "\"";

        this.style[name] = val;
    }
    return this;
};

function addEvent(elem, type, eventHandler) {
    if (elem === null || elem === undefined)
        return;
    if (elem.addEventListener) {
        elem.addEventListener(type, eventHandler, false);
    } else if (elem.attachEvent) {
        elem.attachEvent("on" + type, eventHandler);
    } else {
        elem["on" + type] = eventHandler;
    }
}

function onLoad(eventHandler) {
    if (eventHandler)
        addEvent(window, "load", eventHandler);
}

function alertError(source) {
    alert(new EError(source).message);
}

function EError(source) {
    this.ext = false;
    this.msg1 = "";
    this.cls = "";
    this.details = "";
    this.callStack = "";
    this.isEmpty = false;
    this.id = null;
    this.message = null; // sformatowana postać tekstowa

    var type = typeof source;
    if (typeof source === "string") {
        this.msg1 = source;
        this.ext = false;
        this.message = source;
        return;
    }
    if (source instanceof XMLHttpRequest) {
        // zakladamy ze jest to XMLHttpRequest
        this.message = "";

        this.ext = source.status !== 0 && source.statusText;
        if (!this.ext) {
            this.message = "Brak odpowiedzi serwera";
            return;
        }

        var ss = getHeader(source, "Error-Message");
        var cls = this.cls = getHeader(source, "Error-Class");
        var cs = getHeader(source, "Error-CallStack");
        var ct = getHeader(source, "Content-Type");
        this.details = getHeader(source, "Error-Details");
        this.id = getHeader(source, "Error-Id");
        if (ct)
            ct = ct.toLowerCase();

        if (source.status === 0 && !source.statusText) {
            this.isEmpty = true;
            return;
        }

        var msg = "Błąd " + source.status + ": " + source.statusText;

        if (ct && ct.indexOf("text/plain") >= 0 && source.responseText)
            msg = "Błąd " + source.status + ": " + source.responseText;

        if (ss)
            msg = (cls ? cls + ":\n" : "") + ss;

        if (!ss && cls)
            msg = cls;

        if (cs) {

            while (cs.indexOf("[$br]") > 0) {
                cs = cs.replace("[$br]", "\n");
            }
            while (cs.indexOf("[!*]") >= 0) {
                cs = cs.replace("[!*]", "• ");
            }
            this.callStack = cs;
        }

        this.message = msg;
    }

    if (source.message !== undefined) {
        this.msg1 = source.message;
        this.ext = false;
        this.message = source.message;
        return;
    }
}


function escapeUrl(url) {
    var s = encodeURIComponent(url);
    s = s.replaceAll("!", "%21");
    s = s.replaceAll("(", "%28");
    s = s.replaceAll(")", "%29");
    s = s.replaceAll("~", "%7E");
    s = s.replaceAll("'", "%27");
    s = s.replaceAll("+", "%2B");
    return s;
}

// konwertuje obiekt na url/uri
function encodeUri(data) {
    if (data === undefined || data === null)
        return "";
    if (typeof data === "object") {
        var ub = new UrlBuilder();
        for (var name in data) {
            var val = data[name];
            if (val instanceof  Array) {
                for (var i = 0; i < val.length; i++)
                    ub.add(name, val[i]);
            } else
                ub.add(name, val);
        }
        return ub.toString();
    }
    data.builder = ub;
    return data;
}

function showPopupWindow(url, w, h) {
    var sw = screen.availWidth;
    var sh = screen.availHeight;
    var dw = screen.width - sw;
    var dh = screen.height - sh;

    if (!w)
        w = sw / 2;
    if (!h)
        h = sh / 2;

    if (typeof (w) === "string" && w.indexOf("%" > 0)) {
        w = w.substring(0, w.indexOf("%"));
        w = Math.round(parseInt(w.trim()) * sw / 100);
    }

    if (typeof (h) === "string" && h.indexOf("%" > 0)) {
        h = h.substring(0, h.indexOf("%"));
        h = Math.round(parseInt(h.trim()) * sh / 100);
    }

    if (w > sw)
        w = sw;
    if (h > sh)
        h = sh;
    // zakladamy ze pasek jest na dole
    var l = sw / 2 - w / 2 - dw;
    var t = sh / 2 - h / 2 - dh;
    if (l < 0)
        l = 0;
    if (t < 0)
        t = 0;
    var newWin = window.open(url, "", "scrollbars=1,width=" + w + ",height=" + h
            + ",left=" + l + ",top=" + t);
    return newWin && newWin.top;
}

function escapeHTML(str, replaceBR) {
    if (str === null || str === undefined)
        return "";

    str = "" + str;

    var lst = str.toString().split('&').join('&amp;').split('<').join('&lt;')
            .split('"').join('&quot;').split('\'').join('&#39;');

    if (!replaceBR)
        return lst;

    lst = lst.split("\n");

    for (var i = 0; i < lst.length; i++) {
        var space = "";

        for (var j = 0; j < lst[i].length; j++)
            if (lst[i][j] === " ")
                space += "&#160;";
            else
            if (lst[i][j] === "\t")
                space += "&#160;&#160;";
            else
                break;

        lst[i] = space + lst[i].trim();
    }
    return lst.join("<br/>");
}
/*
 function getInnerText(innerHtml) {
 return innerHtml.replace(/\&lt;br\&gt;/gi, "\n")
 .replace(/(&lt;([^&gt;]+)&gt;)/gi, "");
 }
 */
/*
 function getParentTag(base, tag) {
 var nd = base;
 while (nd) {
 if (nd.nodeName.toLowerCase() === tag.toLowerCase())
 return nd;
 nd = nd.parentNode;
 }
 return null;
 }
 */

/*
 
 function deleteNode(nd) {
 if (!nd)
 return;
 nd.parentNode.removeChild(nd);
 }
 */

/*
 // Zwrca wskaźnik do funkcji na podstawie nazwy
 function strFunc(str) {
 if (str.indexOf("(") > 0) {
 str = str.substring(0, str.indexOf("("));
 }
 var f = window[str];
 if (debugMode && !f)
 alert("Nie znaleziono funkcji \"" + str + "\"!");
 return f;
 }
 */

// przyklad wywolania: tag.onclick = "load('url', event)"
function load(url, newWindow) {
// otwiera url w nowym oknie lub biezacym, 
// w nowym zwraca boolean czy udalo sie otworzyc (blokowanie popup)
// newWindow moze byc typu boolean lub event

    if ((newWindow === undefined || newWindow === null)
            && window.event instanceof MouseEvent) {
        newWindow = window.event.button !== 0 || window.event.ctrlKey;
    }

    if (newWindow instanceof MouseEvent) {
        newWindow = newWindow.button !== 0 || newWindow.ctrlKey;
    }

    if (!newWindow && service && service.busyBeforeUnload)
        new Busy({time: 1500, grayout: {opacity: 0}});

    if (newWindow === true)
        window.open(url, '_blank');
    else
        window.location.href = url;
}


function navigate(url) {
    window.location.href = url;
}
// ----------------------------- przerobic na prototype dla selecta
function selectedValue(cbb) {
    if (!cbb || cbb.selectedIndex === -1)
        return "";
    return cbb.options[cbb.selectedIndex].value;
}

function formatFloat(value, power) {
// formatuje wartosc do okreslonej ilosci miejsc po przecinku
    value = Math.round(value * Math.pow(10, power));
    return value / Math.pow(10, power);
}

function $id(id) {
    return document.getElementById(id);
}

function gradient(obj, from, to) {
    var ua = navigator.userAgent.toLowerCase();
    var s = "linear-gradient(" + from + "," + to + ")";
    if (ua.indexOf("webkit") >= 0)
        s = "-webkit-linear-gradient(" + from + "," + to + ")";
    else
    if (ua.indexOf("opera") >= 0)
        s = "-o-linear-gradient(" + from + "," + to + ")";
    else
    if (ua.indexOf("msie") >= 0)
        s = "-ms-filter: \"progid:DXImageTransform.Microsoft.gradient" +
                "(startColorstr=" + from + ", endColorstr=" + to + ")";
    else
    if (ua.indexOf("mozilla") >= 0)
        s = "-moz-linear-gradient(top," + from + "," + to + ")";
    obj.style.backgroundImage = s;
}


function getCookie(c_name) {
    var i, x, y, ARRcookies = document.cookie.split(";");
    for (i = 0; i < ARRcookies.length; i++)
    {
        x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
        y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
        x = x.replace(/^\s+|\s+$/g, "");
        if (x === c_name)
        {
            return unescape(y);
        }
    }
    return null;
}

function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdays = parseInt(exdays);
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escapeUrl(value) + ((exdays === null)
            ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value;
}

function setCookieHttpOnly(c_name, value, exdays) {
    var exdate = new Date();
    exdays = parseInt(exdays);
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escapeUrl(value) + ((exdays === null)
            ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value + "; HttpOnly";
}

function getHeader(resp, name) {
    if (!resp || !name)
        return null;
    var s = resp.getResponseHeader(name);
    if (!s)
        return null;
    return decodeURIComponent(s);
}

function randomUid() {
    var uid = "";
    for (var i = 0; i < 4; i++) {
        uid += (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }
    return uid;
}

function randomId(length) {
    var chars = "abcdefghijklmnopqrstuwvxyzABCDEFGHIJKLMNOPQRSTUWVXYZ1234567890";
    if (!length)
        length = 10;
    var id = "";
    for (var i = 0; i < length; i++)
        id += chars[Math.floor(Math.random() * (chars.length - (i === 0 ? 10 : 0))) ];
    return id;
}
/*
 function httpGetSync(url, skipLog) {
 var http = createHttpRequestObject();
 http.url = url;
 http.open("GET", url, false);
 if (skipLog)
 http.setRequestHeader("ajax-skip-request-log", "true");
 http.send(null);
 if (http.readyState === 4 && http.status === 200) {
 return http.responseText;
 }
 return null;
 }
 
 function httpGetAsync(url, onResponse, skipLog) {
 var http = createHttpRequestObject();
 http.open("get", url, true);
 http.url = url;
 http.onResponse = onResponse;
 if (skipLog)
 http.setRequestHeader("ajax-skip-request-log", "true");
 http.send(null);
 return http;
 }
 */


/*
 function httpPostSync(url, post, skipLog) {
 var http = createHttpRequestObject();
 http.open("POST", url, false);
 http.url = url;
 if (skipLog)
 http.setRequestHeader("ajax-skip-request-log", "true");
 http.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
 http.send(encodeUri(post));
 return http.responseText;
 }
 
 
 function httpPostAsync(url, post, onResponse, skipLog) {
 var http = createHttpRequestObject();
 http.url = url;
 http.open("POST", url, true);
 http.onResponse = onResponse;
 if (skipLog)
 http.setRequestHeader("ajax-skip-request-log", "true");
 http.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
 http.send(encodeUri(post));
 return http;
 }
 */
function createHttpRequestObject() {
    var req;
    try {
        req = new XMLHttpRequest();
    } catch (e) {
        try {
            req = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            try {
                req = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {
            }
        }
    }
    if (req) {
        req.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status !== 200
                        && typeof service !== 'undefined'
                        && this.url.indexOf("$?ajaxError") === -1)
                    service.logAjaxError(this);
                if (this.onResponse)
                    this.onResponse(this);
            }
        };
    }
    return req;
}

/*
 
 
 function grayOut(vis) {
 var zindex = 10;
 var opacity = 30;
 var opaque = (opacity / 100);
 var bgcolor = '#000000';
 var dark = document.getElementById('darkenScreenObject');
 if (!dark) {
 var tbody = document.getElementsByTagName("body")[0];
 var tnode = document.createElement('div');
 tnode.style.position = 'absolute';
 tnode.style.top = '0px';
 tnode.style.left = '0px';
 tnode.style.overflow = 'hidden';
 tnode.style.display = 'none';
 tnode.id = 'darkenScreenObject';
 tbody.appendChild(tnode);
 dark = tnode;
 }
 if (vis) {
 if (document.body && (document.body.scrollWidth || document.body.scrollHeight)) {
 var pageWidth = document.body.scrollWidth + 'px';
 var pageHeight = document.body.scrollHeight + 'px';
 } else if (document.body.offsetWidth) {
 var pageWidth = document.body.offsetWidth + 'px';
 var pageHeight = document.body.offsetHeight + 'px';
 } else {
 var pageWidth = '100%';
 var pageHeight = '100%';
 }
 dark.style.opacity = opaque;
 dark.style.MozOpacity = opaque;
 dark.style.filter = 'alpha(opacity=' + opacity + ')';
 dark.style.zIndex = zindex;
 dark.style.backgroundColor = bgcolor;
 dark.style.width = pageWidth;
 dark.style.height = pageHeight;
 dark.style.display = 'block';
 darkness = true;
 } else {
 dark.style.display = 'none';
 darkness = false;
 }
 }
 */

//==============================================================================
var js_errors = new Array();
function JsError(msg, file, line) {
    this.cnt = 1;
    this.msg = msg;
    this.file = file;
    this.filename = file;
    this.line = line;
    if (file && file.indexOf("/") >= 0)
        this.filename = file.substring(file.lastIndexOf("/") + 1);
}

//------------------------------------------------------------------------------
window.onerror = function (msg, file, line) {
    var err = null;
    if (js_errors.length > 0) {
        var er = js_errors[js_errors.length - 1];
        if (msg === er.msg && line === er.line && er.file === file) {
            err = er;
            ++err.cnt;
        }
    }

    if (err === null) {
        err = new JsError(msg, file, line);
        js_errors.push(err);
    }

    if (!err.msg && !err.file)
        return;
    if (service && service.logsEnabled === true) {
        var stack;
        try {
            // wygeneruj wyjatek aby pobrac stack trace
            throw "";
        } catch (e) {
            stack = e.stack || e.stacktrace || e.message || arguments.callee.caller;
        }

        try {
            ajax.post("$?jsError", {
                msg: err.msg,
                file: err.filename,
                line: err.line,
                stack: stack,
                skipLog: true
            }
            );
        } catch (e) {
            console.error(e);
        }
    }

    if (service && service.debugMode === false)
        return;
    var tag = top.$id("_err-dlg_");
    if (!tag) {
        var body = top.document.body;
        if (!body)
            return;
        tag = body.tag("div");
        tag.setAttribute("width", "100%");
        tag.id = "_err-dlg_";
        var close = tag.tag("div");
        tag.tag("div");
        var s = tag.style;
        s.position = "fixed";
        s.margin = "6px";
        s.padding = "8px 64px";
        s.top = "0";
        s.opacity = "0.9";
        s.border = "2px solid #DD3C10";
        s.boxShadow = "2px 2px 3px #666666";
        s.backgroundColor = "#FFEBE8";
        s.fontFamily = "Verdana";
        s.color = "black";
        s.fontSize = "10pt";
        s.fontWeight = "bold";
        s.zIndex = "1000";
        s.textAlign = "center";
        s.borderRadius = "0 0 8px 8px";
        close.style.position = "absolute";
        close.style.top = "0";
        close.style.right = "4px";
        close.style.cursor = "pointer";
        close.style.color = "#d66";
        close.style.fontSize = "12pt";
        close.innerHTML = "x";
        close.onmouseover = function () {
            close.style.color = "blue";
        };
        close.onmouseout = function () {
            close.style.color = "#d66";
        };
        close.onclick = function () {
            js_errors = new Array();
            top.$id("_err-dlg_").style.display = "none";
        };
    }

    tag.style.display = "block";
    var ss = "";
    for (var i = 0; i < js_errors.length; i++) {
        err = js_errors[i];
        var mm = err.msg;
        if (mm.indexOf("Uncaught exception: ") === 0)
            mm = mm.substring("Uncaught exception: ".length, mm.length);
        ss += "<div>" + (err.cnt > 1 ? "[" + err.cnt + "x] " : "") + escapeHTML(mm) + "</div>\n";
        if (err.filename && err.filename !== "null")
            ss += "<div style='font-weight: normal; font-size: 8pt'>" + escapeHTML(err.filename)
                    + ", line: " + err.line + "</div>\n";
        if (i < js_errors.length - 1)
            ss += "<br/>\n";
    }

    tag.getElementsByTagName("div")[1].innerHTML = ss;
    tag.style.marginLeft = Math.round(-tag.clientWidth / 2) + "px";
    tag.style.left = "50%";
};



/*
 function ajaxPostForm(form, onDone){
 if (!form || form.nodeName !== "FORM") {
 return ;
 }
 
 httpPostAsync(form.action, serializeForm(form), function(http){
 if (http.status == 200){
 onDone();
 return;
 }        
 }, false);
 }
 
 
 function ajaxPostFormAndReload(form){
 if (!form || form.nodeName !== "FORM") {
 return ;
 }
 
 httpPostAsync(form.action, serializeForm(form), function(http){
 if (http.status == 200){
 location.reload(true);
 return;
 }        
 }, false);
 }
 */

function serializeForm(form, builder) {
    if (typeof form === 'string')
        form = $id(form);

    if (!form || !form.elements)
        return;

    if (!builder instanceof UrlBuilder)
        builder = new UrlBuilder();

    var i, j, first;

    var elems = form.elements;
    for (i = 0; i < elems.length; i += 1, first = false) {
        if (elems[i].name.length > 0) { /* don't include unnamed elements */
            switch (elems[i].type) {
                case 'select-one':
                    first = true;
                case 'select-multiple':
                    for (j = 0; j < elems[i].options.length; j += 1)
                        if (elems[i].options[j].selected) {
                            builder.add(elems[i].name, elems[i].options[j].value);
                            if (first)
                                break; /* stop searching for select-one */
                        }
                    break;
                case 'checkbox':
                case 'radio':
                    if (!elems[i].checked)
                        break; /* else continue */
                default:
                    builder.add(elems[i].name, elems[i].value);
                    break;
            }
        }
    }

    return builder.toString();
}


function updateCssRule(selector, style, value) {
    for (var i = 0; i < document.styleSheets.length; i++) {
        var sheet = document.styleSheets[i];
        for (var j = 0; j < sheet.cssRules.length; j++) {
//console.log(sheet.cssRules[j].selectorText);                       
            var sel = sheet.cssRules[j].selectorText;
            if (!sel)
                continue;
            /*   if (sel.substring(0, 1) === "."
             || sel.substring(0, 1) === "#"
             || sel.substring(0, 1) === "@")
             sel = sel.substr(1);
             */

            if (sel.toLowerCase() !== selector.toLowerCase())
                continue;
            var rule = sheet.cssRules[j].cssText;
            var v1 = rule.indexOf(style);
            var v2 = 0;
            if (v1 < 0) {
                v1 = rule.indexOf("{") + 1;
                v2 = v1;
            } else
                v2 = rule.indexOf(";", v1);
            if (v2 < 0)
                v2 = rule.indexOf("}", v1);
            rule = rule.substring(0, v1) + style + ": " + value
                    + (v1 === v2 ? "; " : "") + rule.substring(v2, rule.length);
            sheet.deleteRule(j);
            sheet.insertRule(rule, j);
        }
    }
    return null;
}

function UrlBuilder(baseUrl) {

    // parametr typu null : tylko nazwa
    // undefined zostanie zignorowany
    this.preUrl = "";
    this.items = new Array();


    this.add = function (nameOrObject, value) {
        // value moze byc niezdefiniowany

        if (!nameOrObject)
            return;

        if (typeof nameOrObject === "object") {
            for (var item in nameOrObject)
                this.add(item, nameOrObject[item]);
            return;
        }

        if (value instanceof Array) {
            for (var i = 0; i < value.length; i++)
                this.add(nameOrObject, value[i]);
            return;
        }

        if (value && typeof value === "object") {
            this.add(value);
            return;
        }

        if (value === undefined)
            return;

        this.items.push(escapeUrl(nameOrObject)
                + (value === null ? "" : "=" + escapeUrl(value)));

        return this;

    };

    this.toString = function () {
        var url = this.preUrl;
        for (var i = 0; i < this.items.length; i++) {
            if (url !== "")
                url += "&";
            url += this.items[i];
        }
        return url;
    };

    if (baseUrl) {
        var builder = this;

        var url = this.preUrl = "" + baseUrl;

        if (!url.contains("?")) {
            this.preUrl += "?";
            return;
        }

        this.preUrl = url.substring(0, url.indexOf("?") + 1);

        var arr = url.substring(url.indexOf("?") + 1, url.length).split("&");
        arr.forEach(function (s) {
            if (s.trim())
                builder.items.push(s);
        });
    }
}

function setSelectable(selector, state) {
    updateCssRule(selector, "-webkit-user-select", state ? "text" : "none");
    updateCssRule(selector, "-khtml-user-select", state ? "text" : "none");
    updateCssRule(selector, "-moz-user-select", state ? "text" : "none");
    updateCssRule(selector, "-o-user-selectt", state ? "text" : "none");
    updateCssRule(selector, "user-select", state ? "text" : "none");
}


function formatFileSize(size) {
    if (size >= 0x40000000)
        return (size / 0x40000000).round(2, true) + " GB";
    if (size >= 0x100000)
        return (size / 0x100000).round(2, true) + " MB";
    if (size >= 0x400)
        return (size / 0x400).round(2, true) + " KB";
    return size + " B";
}

function trimFileName(name, length) {
    if (!name || !length)
        return name;

    name = name.trim();
    if (name.length <= length)
        return name;


    if (name.indexOf("." > 0)) {
        var ext = name.substring(name.lastIndexOf("."), name.length);
        name = name.substring(0, name.length - ext.length);

        name = name.substring(0, length - ext.length - 1).trim();
        return name + "\u2026" + ext;
    }

    return name.substring(0, length - 1).trim() + "\u2026";

}

function isFontInstalled(name) {
    name = name.replace(/['"<>]/g, '');
    var body = document.body,
            test = document.createElement('div'),
            installed = false,
            template =
            '<b style="display:inline !important; width:auto !important; font:normal 10px/1 \'X\',sans-serif !important">ii</b>' +
            '<b style="display:inline !important; width:auto !important; font:normal 10px/1 \'X\',monospace !important">ii</b>',
            ab;
    if (name) {
        test.innerHTML = template.replace(/X/g, name);
        test.style.cssText = 'position: absolute; visibility: hidden; display: block !important';
        body.insertBefore(test, body.firstChild);
        ab = test.getElementsByTagName('b');
        installed = ab[0].offsetWidth === ab[1].offsetWidth;
        body.removeChild(test);
    }
    return installed;
}

/*
 Odnajduje w tekscie linki i formatuje je do postaci odnosnikow
 */
function linkify(text) {
    if (!(typeof text === "string"))
        return text;

    var urlRegex = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~$_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
    return text.replace(urlRegex, function (url) {
        return '<a href="' + url + '" target="_blank">' + url + '</a>';
    });
}