package driver.channels;

import driver.Location;
import driver.Main;
import driver.Terminal;
import driver.protocol.ProtocolException;
import java.util.*;

public class Group extends Channel {

    public final static Set<Group> all = new LinkedHashSet<>();

    public final PwmChannel[] channels;
    public final String name;
    public final boolean isTerinal;
    public final Location location;

    public Group(int id, Location location, String name, PwmChannel... channels) {
        super(id, "G" + id);
        this.name = name;
        this.location = location;

        Terminal t = channels[0].terminal;
        for (PwmChannel c : channels)
            if (!Objects.equals(t, c.terminal))
                t = null;

        this.isTerinal = t != null;
        if (!isTerinal && (id < 1 || id > 16))
            throw new RuntimeException();

        this.channels = channels;

        for (Group g : all)
            if (g.id == id)
                throw new RuntimeException("Zduplikowany identyfikaotr grupy (" + id + ")");

        all.add(this);
    }

    @Override
    protected void doSetValue(int value, int delay) throws ProtocolException {
        if (Main.protocol != null)
            if (isTerinal)
                Main.protocol.setTerminalPwmValue(channels[0].terminal, delay, value, channels);
            else
                Main.protocol.setGroupsPwmValue(delay, value, this);
    }

    public void testAnimation() throws ProtocolException, InterruptedException {
        for (PwmChannel ch : channels) {
            ch.setValue(0, 0);
            Thread.sleep(10);
        }

        for (PwmChannel ch : channels) {
            ch.setValue(30, 3);
            Thread.sleep(300);
        }

        Thread.sleep(1000);

        //        send(setPwm, false, id, 63, 0, 0);
        for (PwmChannel ch : channels) {
            ch.setValue(4095, 1);
            Thread.sleep(200);
        }
        //  Thread.sleep(200);

        for (PwmChannel ch : channels) {
            ch.setValue(0, 2);
            Thread.sleep(200);
        }
    }

    public String getName() {
        return location.name + " " + name;
    }
}
