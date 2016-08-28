package driver.protocol;

import driver.protocol.ProtocolException;
import com.hashes.Hex;
import driver.Main;
import driver.Terminal;
import driver.protocol.Command;
import mlogger.Log;
import mlogger.LogKind;

/**
 * Miłosz Ziernik 2014/07/13
 */
public class Message {

    static long lastReceivedTime = System.currentTimeMillis();
    static Message currentMessage;

    public synchronized static void add(byte b) {
        
            
        long now = System.currentTimeMillis();
        // jesli przerwa w odbieraniu byla wieksza niz 100 ms to wyzeruj bufor
        if (now - lastReceivedTime > (Main.clietMode ? 3000 : 100))
            currentMessage = null;

        lastReceivedTime = now;

        // odczekaj 100ms po uruchomieniu uslugi aby
        // pominac wszystkie smieci
        if (now - Main.start < 100)
            return;

        if (currentMessage == null)
            currentMessage = new Message();

        try {
            currentMessage.collect(b & 0xFF);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public byte params;
    public int bCommand;
    public int bTerminal;
    public int value;

    private final StringBuilder stringBuilder = new StringBuilder();

    private int byteIndex = 0;
    private int crc;

    private void collect(int b) throws ProtocolException {

        ++byteIndex;
        if (byteIndex < 5)
            crc ^= b;

        if (bCommand == Command.textMessage.id && bTerminal > 0) {
            if (b == 0) {
                process();
                return;
            }
            stringBuilder.append((char) b);
            return;
        }

        switch (byteIndex) {
            case 1:
                bCommand = (byte) (b >> 4);
                params = (byte) (b & 0x0F);
                return;

            case 2:
                bTerminal = b;
                return;

            case 3:
                value |= b << 8;
                return;
            case 4:
                value |= b;
                return;
            case 5:
                currentMessage = null;
                if (crc != b)
                    throw new ProtocolException("Nieprawidłowa suma kontrolna");
                process();
                return;
        }
    }
//50 07 00 C0 97

    public static class MsgData {

        public final Terminal terminal;
        public final Command command;
        public final byte params;
        public final int value;

        public MsgData(final Terminal terminal, final Command command,
                final byte params, int value) {
            this.terminal = terminal;
            this.command = command;
            this.params = params;
            this.value = value;
        }

    }

    void process() throws ProtocolException {

        Command command = Command.get(bCommand);
        Terminal terminal = Terminal.get(bTerminal);

        if (command == Command.textMessage) {
            Log.info("Terminal " + (terminal != null ? terminal.id : "?"),
                    stringBuilder.toString());
            currentMessage = null;
            return;
        }

        if (command == Command.error) {
            new Log(LogKind.error)
                    .tag("ERR")
                    .value("Terminal " + terminal.id + ": "
                            + Command.errorToString(value) + (params > 0
                            ? " (" + params + ")" : ""))
                    .attribute("Terminal", terminal.id)
                    .send();

            return;
        }

        Log.event(
                "command", terminal + ", " + command.name() + ": "
                + params + " (0x" + Hex.toString(value, 2, "") + ")");

        terminal.processResponse(command, params, value);

        /*    Thread thread = new Thread(new Runnable() {
         @Override
         public void run() {
     
         }
         });
         thread.setName("Command " + terminal.id + ": " + command.name());
         thread.setPriority(5);
         thread.start();
         */
    }
}
