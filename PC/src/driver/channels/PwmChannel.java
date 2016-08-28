package driver.channels;

import driver.Location;
import driver.Main;
import driver.protocol.ProtocolException;
import driver.Terminal;

/**
 * Miłosz Ziernik 2014/07/13
 */
public class PwmChannel extends Channel {

    public final Terminal terminal;
    public Location location;

    public PwmChannel(Terminal terminal, int id) {
        super(id, "T" + terminal.id + "." + id);
        this.terminal = terminal;
    }

    @Override
    public String toString() {
        return "PWM " + terminal.id + "." + id;
    }

    @Override
    public void doSetValue(int value, int delay) throws ProtocolException {

        if (value < 0)
            value = 0;
        if (value > 4095)
            value = 4095;

        if (delay < 0)
            delay = 0;

        if (delay > 7)
            delay = 7;

        if (Main.protocol != null)
            Main.protocol.setTerminalPwmValue(terminal, delay, value, this);
    }

}
