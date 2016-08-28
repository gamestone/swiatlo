package api;

import com.json.*;
import driver.Location;
import driver.Main;
import driver.RGB;
import driver.Terminal;
import driver.channels.*;
import driver.protocol.PIR;
import driver.switches.Switch;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import http.websocket.WebSocket;
import http.websocket.framing.Framedata;
import http.websocket.handshake.ClientHandshake;
import http.websocket.server.WebSocketServer;
import java.util.LinkedList;
import java.util.List;
import mlogger.Log;

public class WsServer extends WebSocketServer {

    final static List<WebSocket> sockets = new LinkedList<>();

    public WsServer() throws UnknownHostException {
        super(new InetSocketAddress(8187));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (sockets) {
            sockets.add(conn);
        }
        Log.info("WS", "Podłączony " + conn.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (sockets) {
            sockets.remove(conn);
        }
        Log.info("WS", "Rozłączony " + conn.getRemoteSocketAddress());
    }

    public static boolean hasClients() {
        return !sockets.isEmpty();
    }

    public static void boadcast(JObject obj) {
        if (sockets.isEmpty())
            return;
        obj.options.singleLine(true);
        String val = obj.toString();

        synchronized (sockets) {
            for (WebSocket ws : sockets)
                try {
                    ws.send(val);
                } catch (Throwable e) {
                    Log.error(e);
                }
        }

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {

            JObject result = new JObject();
            result.options.singleLine(true);

            JObject obj = JSON.parse(message).asObject();
            result.add("request", obj);
            JObject res = result.object("result");

            switch (obj.getStr("action", "")) {
                case "setPwm":
                    String cha = obj.getStr("channel");
                    if ("pir".equals(cha))
                        Main.helper.pwmWrite(obj.getInt("value") / 16);
                    else
                        Channel.get(cha)
                                .setValue(obj.getInt("value"),
                                        obj.getInt("speed", 0));

                    break;

                case "getPwm":
                    res.put("value", Channel.get(obj.getStr("channel"))
                            .getValue());
                    break;

                case "getAll":
                    //--------------------------------------

                    res.object("miscActions")
                            .put("updateGroups", "Aktualizacja grup");

                    //--------------------------------------
                    JObject jloc = res.object("loc");
                    for (Location l : Location.values())
                        jloc.put("" + l.key, l.name);

                    JObject jterms = res.object("term");

                    for (Terminal t : Terminal.all()) {
                        JObject jterm = jterms.object(t.key);

                        jterm.put("id", t.id)
                                .put("loc", t.location.key)
                                .put("rev", t.revision);

                        JObject jchns = jterm.object("chnls");
                        for (PwmChannel c : t.channels)
                            jchns.object(c.key)
                                    .put("id", c.id)
                                    .put("loc", c.location != null ? c.location.key : null)
                                    .put("cval", c.currentValue)
                                    .put("sval", c.savedValue);

                    }

                    JObject jgroups = res.object("group");
                    for (Group g : Group.all) {
                        JObject og = jgroups.object(g.key);
                        og.put("id", g.id)
                                .put("name", g.name)
                                .put("loc", g.location != null ? g.location.key : null)
                                .put("sval", g.savedValue)
                                .put("cval", g.currentValue);
                        JArray array = og.array("chnls");
                        for (PwmChannel ch : g.channels)
                            array.add(ch.key);
                    }

                    JObject jswtch = res.object("switch");

                    for (Switch sw : Switch.all.values()) {
                        jswtch.object(sw.key)
                                .put("loc", sw.location != null ? sw.location.key : null)
                                .put("term", sw.terminal.key)
                                .put("down", sw.downId)
                                .put("up", sw.upId)
                                .put("name", sw.name)
                                .put("group", sw.group != null ? sw.group.key : null);
                    }
                    
                     JObject jrgb = res.object("rgb");
                     for (RGB rgb : RGB.all.values())
                         jrgb.object(rgb.key)
                                 .put("loc", rgb.location.key)
                                 .put("hv", rgb.hue)
                                 .put("sv", rgb.saturation)
                                 .put("lv", rgb.lightness)
                                 .put("hk", rgb.chaH.key)
                                 .put("sk", rgb.chaS.key)
                                 .put("lk", rgb.chaL.key);
                    
                    if (Main.helper != null)
                        res.object("pir")
                                .put("power", Main.helper.pirPower)
                                .put("state", Main.helper.pirState)
                                .put("value", Main.helper.pwmValue)
                                .put("schedule", PIR.getSchedule());

                    break;

                case "getGroups":
                    for (Group g : Group.all) {
                        JObject og = res.object(g.key);
                        og.put("id", g.id);
                        og.put("name", g.getName());
                        og.put("value", g.getValue());
                        JArray array = og.array("channels");
                        for (PwmChannel ch : g.channels)
                            array.add(ch.key);
                    }
                    break;

                case "updateGroups":

                    for (Terminal t : Terminal.all()) {
                        t.updateGroups();
                        Thread.sleep(200);
                    }

                    result.put("alert", "Zakończono aktualizację");
                    break;

                case "setRGB":

                    int value = Integer.parseInt(obj.getStr("value"), 16);
                    int b = (value) & 0xFF;
                    int g = (value >> 8) & 0xFF;
                    int r = (value >> 16) & 0xFF;

                    RGB.setRGB(r + (g << 8) + (b << 16), true, true);
                    break;

            }

            conn.send(result.toString());

        } catch (Throwable e) {
            Log.warning(e);
            sendError(conn, e);
        }

    }

    private void sendError(WebSocket conn, Throwable e) {
        JObject json = new JObject();
        json.options.singleLine(true);
        json.put("error", e.toString());
        conn.send(json.toString());
    }

    @Override
    public void onFragment(WebSocket conn, Framedata fragment) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.error(ex);
    }

    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
