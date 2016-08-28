package driver;

import driver.protocol.ProtocolException;
import com.hashes.Bin;
import driver.channels.*;
import driver.protocol.Command;
import driver.switches.Switch;
import java.util.*;
import mlogger.Log;

import static driver.protocol.Command.switchStates;
import driver.protocol.Protocol;

/**
 * Miłosz Ziernik 2014/07/13
 */
public class Terminal extends Channel implements Iterable<PwmChannel> {

    private final static LinkedList<Terminal> all = new LinkedList<>();
    public final PwmChannel[] channels;
    public Integer revision;

    public final Location location;

    public final PwmChannel C1;
    public final PwmChannel C2;
    public final PwmChannel C3;
    public final PwmChannel C4;
    public final PwmChannel C5;
    public final PwmChannel C6;

    //---------------------------------------------------------
    public final int id;

    public static ArrayList<Terminal> all() {
        ArrayList list = new ArrayList<>();
        list.addAll(all);
        return list;
    }

    public Terminal(Location location, int id) {
        super(id, "T" + id);
        this.id = id;
        this.location = location;

        for (Terminal t : all)
            if (t.id == id)
                throw new RuntimeException("Zduplikowany identyfikaotr terminala (" + id + ")");

        C1 = new PwmChannel(this, 1);
        C2 = new PwmChannel(this, 2);
        C3 = new PwmChannel(this, 3);
        C4 = new PwmChannel(this, 4);
        C5 = new PwmChannel(this, 5);
        C6 = new PwmChannel(this, 6);

        channels = new PwmChannel[]{C1, C2, C3, C4, C5, C6};

        all.add(this);

    }

    @Override
    public String toString() {
        return "Terminal " + id;
    }

    public static Terminal find(int id) {

        for (Terminal t : all) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    public static Terminal get(int id) throws ProtocolException {
        if (id < 1 || id > 32)
            throw new ProtocolException("Nieprawidłowy id " + id);
        Terminal terminal = find(id);
        if (terminal == null)
            throw new ProtocolException("Nie znaleziono terminala " + id);
        return terminal;
    }

    public void send(Command command, int params, int data, int value) throws ProtocolException {
        Main.protocol.send(command, params, this, data, value);
    }

    public void setPwmValue(int params, int value, PwmChannel... channels)
            throws ProtocolException {
        Main.protocol.setTerminalPwmValue(this, params, value, channels);
    }

    public void sendHello() throws ProtocolException {
        Main.protocol.send(Command.hello, 0, id, 0, 0xCCCC);
    }

    public void resetDevice() throws ProtocolException {
        Main.protocol.send(Command.resetDevice, 11, id, 222, 0xEDCB);
    }

    public void writeEepromData(int cell, int value) throws ProtocolException {
        Protocol.checkParam("cell", cell, 0, 15);
        Protocol.checkParam("value", value, 0, 65535);
        Main.protocol.send(Command.eepromData, 1, id, cell, value);
    }

    public void readEepromData(int cell) throws ProtocolException {
        Protocol.checkParam("cell", cell, 0, 15);
        Main.protocol.send(Command.eepromData, 2, id, cell, 0);
    }

    public void setInstanceId(int value) throws ProtocolException {
        Protocol.checkParam("value", value, 0, 65535);
        Main.protocol.send(Command.instanceId, 1, id, 0, value);
    }

    public void getInstanceId() throws ProtocolException {
        Main.protocol.send(Command.instanceId, 2, id, 0, currentValue);
    }

    public void processResponse(Command command, byte params, int value) {

        try {
            switch (command) {

                case hello:
                    revision = Integer.valueOf(params);
                    break;

                case switchStates:
                    for (Switch sw : Switch.all.values())
                        if (sw.terminal == this && (sw.downId == value || sw.upId == value)) {
                            sw.onSwitchDownEvent(value == sw.upId);
                            return;
                        }

                    break;

            }

        } catch (Throwable e) {
            Log.error(e);
        }
    }

    @Override
    public Iterator<PwmChannel> iterator() {
        return Arrays.asList(channels).iterator();
    }

    @Override
    protected void doSetValue(int value, int speed) throws ProtocolException {
        this.setPwmValue(speed, value, channels);
    }

    public void getGroups() throws ProtocolException {
        send(Command.groups, 2, 0, 65535);
    }

    public void updateGroup(int groupNr, int channels) throws ProtocolException {
        Protocol.checkParam("groupNr", groupNr, 0, 15);
        Protocol.checkParam("channels", channels, 0, 63);
        send(Command.groups, 1, channels, groupNr);
    }

    public void updateGroups() throws ProtocolException {
        for (int grId = 0; grId <= 15; grId++) {

            int channels = 0;

            for (Group gr : Group.all)
                if (gr.id == grId)
                    for (PwmChannel ch : gr.channels)
                        if (ch.terminal == this)
                            channels |= (1 << (ch.id - 1));

            Log.info("Terminal " + id + ", aktualizacja grupy " + grId + ": "
                    + Bin.toString(channels));

            updateGroup(grId, channels);

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

}
