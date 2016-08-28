package driver.protocol;

import api.WsServer;
import com.hashes.Hex;
import com.json.JObject;
import com.threads.QueueThread;
import com.utils.Timestamp;
import driver.Terminal;
import driver.channels.Channel;
import driver.channels.Group;
import driver.channels.PwmChannel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import jssc.*;
import mlogger.Log;

public class Protocol implements SerialPortEventListener, Runnable {

    private final SerialPort serial;
    private final BusWriter writer;
    public boolean pirState;
    public boolean batState;
    public boolean busPower = true;
    public boolean pirPower;
    public boolean batPower;

    private final Thread collector = new Thread(this);
    private final LinkedList<Packet> queue = new LinkedList<>();

    public static LinkedList<String> getPorts() {
        LinkedList<String> ports = new LinkedList<>();
        ports.addAll(Arrays.asList(SerialPortList.getPortNames()));
        return ports;
    }

    public Protocol(String comPortName) throws Exception {

        Log.debug("Otwieram port " + comPortName);
        serial = new SerialPort(comPortName);

        if (serial.openPort()) {
            Log.debug("Połączono");
        }

        collector.setName("Serial collector");
        collector.start();

        serial.setParams(SerialPort.BAUDRATE_9600, 8, 1, SerialPort.PARITY_NONE);
        serial.addEventListener(this);
        writer = new BusWriter(serial);
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {

        if (spe.getEventType() != SerialPortEvent.RXCHAR)
            System.out.println("Zdarzenie " + spe.getEventType());

        if (spe.getEventType() == SerialPortEvent.RXCHAR) {
            try {

                Packet pck = new Packet(serial.readBytes(), serial.isCTS());

                if (pck.data == null || pck.data.length == 0)
                    return;

                synchronized (queue) {
                    queue.add(pck);
                    queue.notify();
                }

            } catch (SerialPortException ex) {
                Log.error(ex);
            }
        }
    }

    @Override
    public void run() {
        while (true)
            try {
                Packet pck = null;

                while (pck == null)
                    synchronized (queue) {
                        pck = queue.pollFirst();
                        if (pck == null)
                            queue.wait();
                    }

                byte[] bytes = pck.data;
                boolean helper = pck.helper;

                Log.event("serial rec", Hex.toString(bytes, " "));

                if (!helper)
                    for (byte b : bytes)
                        Message.add(b);

                if (helper) {
                    if (bytes.length != 1)
                        throw new Error("Nieprawidłowa ilość bajtów.\n"
                                + "Oczekiwano 1, odebrano " + bytes.length);

                    int v = bytes[0] & 0xFF;

                    pirState = (v & 0x01) == 0;
                    batState = (v & 0x02) == 0;

                    Log.info("status", "PIR: " + pirState + ", BAT: " + batState);

                }

            } catch (Throwable e) {
                Log.error(e);
            }

    }

    /*
    public void pirPwmWrite(int value) throws SerialPortException {
        serial.setDTR(true);
        serial.setRTS(false);
        serial.writeByte((byte) value);
    }

    public void writePowerState() throws SerialPortException {
        int v = 0;
        if (busPower)
            v |= 0x01;
        if (pirPower)
            v |= 0x02;
        if (batPower)
            v |= 0x04;
        serial.setDTR(false);
        serial.setRTS(true);
        serial.writeByte((byte) v);
    }
     */
 /*
     #define CMD_SET_PWM 1
     #define CMD_GET_PWM 2
     #define CMD_SET_GROUPS 3
     #define CMD_GET_GROUPS 4
     #define CMD_SWITCHES_STATE 5
     #define CMD_ERROR_CODE 6
     #define CMD_GET_ID 7
     #define CMD_SET_ID_ACK 8
     #define CMD_TEXT 9
     */
    public void setGroupsPwmValue(int delay, int value, Group... groups)
            throws ProtocolException {

        if (groups == null || groups.length == 0)
            return;

        int val = 0;

        for (Group group : groups)
            val |= (1 << (group.id));

        byte[] array = ByteBuffer.allocate(4).putInt(val).array();

        if (WsServer.hasClients()) {
            JObject json = new JObject();
            json.put("action", "ChannelValue");
            for (Channel cha : groups)
                json.put(cha.key, value);
            WsServer.boadcast(json);
        }

        send(Command.setPwmGroups, delay, array[2] & 0xFF, array[3] & 0xFF, value);
    }

    public void setId(int id) throws ProtocolException {
        checkParam("id", id, 0, 255);
        send(Command.setId, 1, id, 0xAB, 0xA29D);
    }

    public void setTerminalPwmValue(Terminal terminal, int params, int value,
            PwmChannel... channels)
            throws ProtocolException {

        if (channels == null || channels.length == 0 || terminal == null)
            return;

        int chnls = 0;

        for (PwmChannel cha : channels)
            chnls |= (1 << (cha.id - 1));

        if (value < 0)
            value = 0;
        if (value > 4095)
            value = 4095;

        if (WsServer.hasClients()) {
            JObject json = new JObject();
            json.put("action", "ChannelValue");
            for (Channel cha : channels)
                json.put(cha.key, value);
            WsServer.boadcast(json);
        }

        send(Command.setPwm, params, terminal.id, chnls, value);
    }

    public void sendHello() throws ProtocolException {
        Log.event("Say Hello Request");
        send(Command.hello, 15, 0, 0, 0xEEEE);
    }

    public void send(Command command, int params, Terminal terminal, int data, int value)
            throws ProtocolException {

        if (command == null || terminal == null)
            return;

        send(command, params, terminal.id, data, value);
    }

    public void send(Command command, int params, int byte1, int byte2, int value) throws ProtocolException {

        if (command == null)
            return;

        checkParam("params", params, 0, 15);
        checkParam("byte1", byte1, 0, 255);
        checkParam("byte2", byte2, 0, 255);
        checkParam("value", value, 0, 65535);

        byte[] buff = new byte[6];
        buff[0] = (byte) ((command.id << 4) | params & 0x0F);
        buff[1] = (byte) byte1;
        buff[2] = (byte) byte2;
        buff[3] = (byte) (value >> 8);
        buff[4] = (byte) value;
        buff[5] = 0;
        for (int i = 0; i < 5; i++)
            buff[5] ^= buff[i];

        String s = (buff[0] & 0xFF) + " " + value;

        s += " (";
        for (int i = 0; i < 6; i++)
            s += " " + Hex.toString(buff[i]);

        s += " )";

        // Log.debug("Send", s);
        Timestamp ts = new Timestamp();
        //   System.out.println("xx " + (System.currentTimeMillis() - lastSend));
        //  lastSend = System.currentTimeMillis();

        writer.add(buff);

    }

    public static void checkParam(String name, int value, int min, int max) throws ProtocolException {
        if (value < min || value > max)
            throw new ProtocolException("Parametr '" + name + "' musi zawierać się "
                    + "w zakresie " + min + "..." + max + ", obecnie: " + value);
    }

}

class BusWriter extends QueueThread<byte[]> {

    final SerialPort serial;

    public BusWriter(SerialPort serial) {
        this.serial = serial;
    }

    private long lastWrited;

    @Override
    protected void processQueue(LinkedList<byte[]> items) throws Throwable {

        // agregacja na podstawie 3 pierwszych bajtów
        // jeśli 3 pierwsze bajty powtarzają się w wielu żądaniach, to wyślij tylko ostatni
        LinkedHashMap<String, byte[]> aggregated = new LinkedHashMap<>();

        for (byte[] data : items) {
            String key = Hex.toString(Arrays.copyOf(data, 3));
            if (aggregated.containsKey(key))
                aggregated.replace(key, data);
            else
                aggregated.put(key, data);
        }

        for (byte[] data : aggregated.values())
            try {

                long now = System.currentTimeMillis();

                if (now - lastWrited >= 0 && now - lastWrited < 50)
                    try {
                        Thread.sleep(50 - (now - lastWrited));
                    } catch (InterruptedException ex) {
                        return;
                    }

                lastWrited = now;

                Log.event("serial send", Hex.toString(data, " "));
                serial.setDTR(true);
                serial.setRTS(true);
                serial.writeBytes(data);
            } catch (SerialPortException ex) {
                throw new IOException(ex);
            }
    }

}

class Packet {

    byte[] data;
    boolean helper;

    public Packet(byte[] data, boolean helper) {
        this.data = data;
        this.helper = helper;
    }

}
