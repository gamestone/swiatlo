package driver.channels;

import driver.protocol.ProtocolException;
import java.util.LinkedHashMap;
import java.util.Map;
import mlogger.Log;

/**
 * Miłosz Ziernik 2014/08/03
 */
public abstract class Channel {

    public Thread animation;
    public final int id;
    public int currentValue;
    public int savedValue = 4095;
    public final String key;

    public final static Map<String, Channel> map = new LinkedHashMap<>();

    public Channel(int id, String key) {
        this.id = id;
        this.key = key;

        if (map.containsKey(key))
            throw new RuntimeException("Klucz " + key + " nie jest unikalny");

        map.put(key, this);
    }

    public static Channel get(String key) {
        Channel channel = map.get(key);

        if (channel == null)
            throw new NoSuchFieldError("Nie znaleziono kanału \"" + key + "\"");

        return channel;
    }

    public int getValue() {
        return currentValue;
    }

    public Boolean isOn() {
        return currentValue == 0 ? Boolean.FALSE
                : currentValue == 4095 ? Boolean.TRUE
                        : null;
    }

    public void setState(boolean on) throws ProtocolException {
        setValue(on ? currentValue : 0, 2);
    }

    protected abstract void doSetValue(int value, int speed) throws ProtocolException;

    long prevStep = System.currentTimeMillis();

    public void setValue(int value, int speed) throws ProtocolException {
        if (value < 0)
            value = 0;
        if (value > 4095)
            value = 4095;

        if (value == currentValue)
            return;

        doSetValue(value, speed);
        Log.event("Request", key + ", setValue "
                + value + ", speed " + speed);

        //System.out.println("step " + currentValue + "  " + (System.currentTimeMillis() - prevStep));
        prevStep = System.currentTimeMillis();
        this.currentValue = value;
    }

    public void saveValue() {
        savedValue = currentValue;
    }

    public int loadValue() {
        if (savedValue < 10)
            savedValue = 10; // nie pozwalaj na zpisanie wartosci mniejszej niz 10
        if (savedValue > 4095)
            savedValue = 4095;
        return savedValue;
    }
}
