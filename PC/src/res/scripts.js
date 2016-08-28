var currDrag;

addEventListener("mouseup", function (e) {
    currDrag = null;
});

var ws = new WebSocket("ws://" + document.location.hostname + ":8887");
//var ws = new WebSocket("ws://192.168.1.10:8887");

ws.onopen = function () {
    ws.send("{action:\"getGroups\"}");

};

ws.onclose = function () {

};

ws.onerror = function (e) {

};


function btnUpdateGroupsClick() {
    ws.send("{action:\"updateGroups\"}");
}

addEventListener("load", function () {
    document.body.tag("button").text("Random").onclick = function () {
        currenrMessage = JSON.stringify({
            action: "setPwm",
            speed: 4,
            channel: "T2All",
            value: Math.round(Math.random() * 4095)
        });
    }

    setTimeout(function () {
        prevRGB = $id("rgb").value;
        checkRGB();
    }, 100);
});

ws.onmessage = function (msg) {

    var json = JSON.parse(msg.data);
    var res = json.result;
    switch (json.request.action) {

        case "getGroups":

            for (var g in res) {
                var group = res[g];
                var sl = $id("slider" + g);
                var pos = Math.sqrt(group.value / 4095);
                sl.style.left = (pos * 100) + "%";
                sl.style.display = "block";
                $id("lbl" + g + "val").text((Math.round((group.value / 4095) * 1000) / 10) + "%");
            }

            break;
    }

};
var currenrMessage;
setInterval(function () {
    if (!currenrMessage)
        return;
    ws.send(currenrMessage);
    currenrMessage = null;
}, 30);
addEventListener("mousemove", function (e) {

    if (!currDrag)
        return;
    var left = e.clientX - currDrag.dragStartX;
    if (left < 0)
        left = 0;
    var max = currDrag.parentNode.offsetWidth - 10;
    if (left > max)
        left = max;
    currDrag.style.left = left + "px";
    var pos = left / max;
    var val = Math.round((pos * pos) * 4095);
    var g = currDrag.getAttribute("channel");
    currenrMessage = JSON.stringify({
        action: "setPwm",
        channel: g,
        value: val
    });
    $id("lbl" + g + "val").text((Math.round((val / 4095) * 1000) / 10) + "%");
});

addEventListener("load", function () {
    var sliders = document.getElementsByClassName("slider");
    for (var i = 0; i < sliders.length; i++) {
        var sdr = sliders[i];
        sdr.onmousedown = function (e) {
            if (e.button !== 0)
                return;
            currDrag = e.currentTarget;
            currDrag.dragStartX = e.clientX - currDrag.offsetLeft;
        };
        sdr.parentNode.onmousemove = function (e) {


        };
    }

    $('#rgb').wheelColorPicker({preview: true});

});

var prevRGB;

function checkRGB() {

    var rgb = $id("rgb").value;

    if (prevRGB !== rgb) {
        ws.send(JSON.stringify({
            action: "setRGB",
            value: rgb
        }));
    }

    prevRGB = rgb;

    setTimeout(checkRGB, 150);

}


/*
 $(function () {
 $('#rgb').wheelColorPicker({ layout: 'block', preview: true, sliders: "wv" });
 });
 
 */