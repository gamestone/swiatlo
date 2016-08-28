package driver.protocol;

import api.WsServer;
import com.json.JArray;
import com.json.JObject;
import com.threads.TThread;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import mlogger.Log;

public class PIR extends TThread {

    boolean pirEnabled;
    long powerOnTS;
    public final Helper helper;
    long flushTS;
    boolean bright;
    private final Object flushNotify = new Object();

    final static int POWER_ON_DELAY = 15000;
    final static int BRIGHT_TIME = 25000;

    public void flush() {
        if (!(helper.pirPower && System.currentTimeMillis() - powerOnTS > POWER_ON_DELAY))
            return;

        flushTS = System.currentTimeMillis();
        synchronized (flushNotify) {
            flushNotify.notifyAll();
        }
    }

    public PIR(Helper helper) {
        this.helper = helper;
    }

    static int getSeconds(double day, double hMin, double hMax) {

        hMin *= 60 * 60;
        hMax *= 60 * 60;

        day += 10;
        if (day > 365)
            day = day - 365;
        day = 182 - Math.abs(day - 182);

        double d = (hMax - hMin) / hMax;

        // day =  day / 182.5d
        day = (1 - Math.cos((day / 182.5d) * Math.PI)) / 2d;

        return (int) (hMin + hMax * d * day);
    }

    private final static double WINTER_ON = 14.5; // +1 UTC
    private final static double WINTER_OFF = 20; // +1  UTC
    private final static double SUMMER_ON = 19; // +2 UTC
    private final static double SUMMER_OFF = 21.5; // +2 UTC

    boolean shouldBeActive() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int zoneOffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 1000;

        int seconds = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
                + cal.get(Calendar.MINUTE) * 60
                + cal.get(Calendar.SECOND);

        seconds -= zoneOffset;

        int day = cal.get(Calendar.DAY_OF_YEAR);

        return (seconds >= getSeconds(day, WINTER_ON, SUMMER_ON))
                && (seconds <= getSeconds(day, WINTER_OFF, SUMMER_OFF));

//        day = 182 - day;
//        if (day < 0)
//            day *= -1;
//
//        return (minutes >= getSeconds(day, 3.3, 5))
//                && (minutes <= getSeconds(day, 6, 7.5));
    }

    @Override
    protected void execute() throws Throwable {

        // getSchedule();
        while (!isInterrupted())
            try {

                long now = System.currentTimeMillis();
                boolean canBright = shouldBeActive();

                if (canBright && !helper.pirPower) {
                    // włączenie modułu
                    Log.info("Włączam moduł PIR");
                    helper.pirPower = true;
                    helper.writePowerState();
                    helper.pwmWrite(100);
                    powerOnTS = now;
                    notifyChanges();
                }

                if (!canBright && helper.pirPower) {
                    // wylaczenie modułu
                    Log.info("Wyłączam moduł PIR");
                    helper.pirPower = false;
                    helper.writePowerState();
                    helper.pwmWrite(0);
                    powerOnTS = 0;
                    notifyChanges();
                }
 
                if (flushTS > 0 && !bright) {
                    // wlaczenie
                    Log.info("Włączam zewnętrzne oświetlenie");

                    for (int i = helper.pwmValue; i <= 255; i++) {
                        helper.pwmWrite(i);
                        Thread.sleep(5);
                    }
                    bright = true;
                    notifyChanges();
                }

                if (bright && flushTS > 0 && now - flushTS > BRIGHT_TIME) {
                    bright = false;
                    flushTS = 0;

                    // wylaczenie
                    Log.info("Wyłączam zewnętrzne oświetlenie");
                    for (int i = helper.pwmValue; i > 0; i--) {
                        helper.pwmWrite(i);
                        Thread.sleep(15);
                        if (flushTS > 0)
                            break;
                    }

                    notifyChanges();
                    // jesli podczas sciemniania cos sie mienilo,
                    // to pomin sleepa 5000
                    if (flushTS > 0)
                        continue;
                }

                synchronized (flushNotify) {
                    flushNotify.wait(5000);
                }

            } catch (Throwable e) {
                Log.error(e);
            }

    }

    private void notifyChanges() {
        if (!WsServer.hasClients())
            return;

        JObject json = new JObject();
        json.object("PIR")
                .put("power", helper.pirPower)
                .put("state", helper.pirState)
                .put("value", helper.pwmValue);

        WsServer.boadcast(json);
    }

    public static JArray getSchedule() {
        JArray main = new JArray();

        for (int day = 1; day < 366; day++) {

            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.DAY_OF_YEAR, day);

            Date time = cal.getTime();

            int zoneOffsetHours = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 60 * 1000);

            int on = getSeconds(day, WINTER_ON, SUMMER_ON);
            int off = getSeconds(day, WINTER_OFF, SUMMER_OFF);

            JArray arr = main.array();
            arr.add(day);

            arr.add(new SimpleDateFormat("dd MMM").format(time));
            arr.add(zoneOffsetHours);

            Calendar con = Calendar.getInstance();
            con.set(Calendar.HOUR_OF_DAY, zoneOffsetHours);
            con.set(Calendar.MINUTE, 0);
            con.set(Calendar.SECOND, on);

            Calendar coff = Calendar.getInstance();
            coff.set(Calendar.HOUR_OF_DAY, zoneOffsetHours);
            coff.set(Calendar.MINUTE, 0);
            coff.set(Calendar.SECOND, off);

            arr.add(new SimpleDateFormat("HH:mm:ss").format(con.getTime()));

            String line = String.format("%03d", day)
                    + " (" + new SimpleDateFormat("dd MMM").format(time) + "): ";
            line += new SimpleDateFormat("HH:mm:ss").format(con.getTime());
            line += " - ";

            // cal.set(Calendar.HOUR_OF_DAY, zoneOffsetHours);
            line += new SimpleDateFormat("HH:mm:ss").format(coff.getTime());

            arr.add(new SimpleDateFormat("HH:mm:ss").format(coff.getTime()));

            line += " (+" + zoneOffsetHours + "h UTC)";

          //  System.out.println(line);
        }

        return main;
    }

}
