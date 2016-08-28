package driver.protocol;

import jssc.*;
import mlogger.Log;

public class Helper implements SerialPortEventListener {

    private final SerialPort serial;
    private static long lastSend = System.currentTimeMillis();
    private long lastWrited;

    public boolean pirState;
    public boolean batState;
    public boolean pirPower;
    public boolean batPower;
    public int pwmValue;

    public final PIR pir;

    public Helper(String comPortName) throws Exception {

        Log.debug("Otwieram port " + comPortName);
        serial = new SerialPort(comPortName);

        if (serial.openPort())
            Log.debug("Połączono");

        serial.setParams(SerialPort.BAUDRATE_9600, 8, 1, SerialPort.PARITY_NONE);
        serial.addEventListener(this);

        pir = new PIR(this);
        pir.start();

        writePowerState();
        pwmWrite(0);
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {

        if (spe.getEventType() != SerialPortEvent.RXCHAR) {
            System.out.println("Zdarzenie " + spe.getEventType());
        }

        if (spe.getEventType() == SerialPortEvent.RXCHAR) {
            try {

                byte[] bytes = serial.readBytes();
                if (bytes == null || bytes.length == 0) {
                    return;
                }

                int v = bytes[0] & 0xFF;

                pirState = (v & 0x01) != 0;
                batState = (v & 0x02) == 0;

             //   System.out.println("PIR: " + pirState + ", BAT: " + batState);

                Log.info("status", "PIR: " + pirState + ", BAT: " + batState);

                if (pirState)
                    pir.flush();

            } catch (Throwable ex) {
                Log.error(ex);
            }
        }
    }

    public void pwmWrite(int value) throws SerialPortException {
        pwmValue = value;
        serial.writeByte((byte) 1);
        serial.writeByte((byte) value);
    }

    public void writePowerState() throws SerialPortException {
        int v = 0;
        if (pirPower) {
            v |= 0x01;
        }
        if (batPower) {
            v |= 0x02;
        }
        serial.writeByte((byte) 2);
        serial.writeByte((byte) v);
    }

}
