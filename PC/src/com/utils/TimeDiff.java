package com.utils;

import static com.utils.TimeDiff.TimeUnit.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 Konwertuje czas w milisekundach na postac dni, hh:mm:ss.SSS
 */
public class TimeDiff {

    public static enum TimeUnit {

        nanosecond(0),
        microsecond(1),
        millisecond(2),
        second(3),
        minute(4),
        hour(5),
        day(6),
        year(7);

        public String getFullName(long value) {

            switch (this) {
                case nanosecond:
                    return value == 1 ? "nanosekunda" : value < 5 ? "nanosekundy" : "nanosekund";
                case microsecond:
                    return value == 1 ? "mikrosekunda" : value < 5 ? "mikrosekundy" : "mikrosekund";
                case millisecond:
                    return value == 1 ? "milisekunda" : value < 5 ? "milisekundy" : "milisekund";
                case second:
                    return value == 1 ? "sekunda" : value < 5 ? "sekundy" : "sekund";
                case minute:
                    return value == 1 ? "minuta" : value < 5 ? "minuty" : "minut";
                case hour:
                    return value == 1 ? "godzina" : value < 5 ? "godziny" : "godzin";
                case day:
                    return value == 1 ? "dzień" : "dni";
                //   case week:
                //        return value == 1 ? "tydzień" : value < 5 ? "tygodnie" : "tygodni";
                case year:
                    return value == 1 ? "rok" : value < 5 ? "lata" : "lat";
            }
            return "";

        }

        private TimeUnit(int weight) {
            this.weight = weight;
        }

        public final int weight;
    }

    public class TimePair {

        public final long value;
        public final TimeUnit unit;

        public TimePair(long value, TimeUnit unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    public final long time;
    public final String sMilliseconds;
    public final int iMilliseconds;
    public final String sSeconds;
    public final int iSeconds;
    public final String sMinutes;
    public final int iMinutes;
    public final String sHours;
    public final int iHours;
    public final String sDays;
    public final int iDays;

    @Override
    public String toString() {
        return toString(true);
    }

    public TimeDiff roundTo(TimeUnit unit) {
        return this;
    }

    private long getVal(TimePair[] parts, TimeUnit unit) {
        for (TimePair pair : parts)
            if (pair.unit == unit)
                return pair.value;

        return 0;
    }

    /**
     Format 00:05:30.000
     @param nanoTime
     @param inclMilliseconds
     @return 
     */
    public String toStringFrmt(long nanoTime, boolean inclMilliseconds) {

        TimePair[] parts = parse(nanoTime, true);

        return String.format("%02d:%02d:%02d" + (inclMilliseconds ? ".%03d" : ""),
                getVal(parts, hour),
                getVal(parts, minute),
                getVal(parts, second),
                getVal(parts, millisecond));

    }

    public String toStringFrmtDyn(long nanoTime) {

        double ms = nanoTime % (1000 * 1000 * 1000) / (1000d * 1000d * 1000d);

        String ss = (ms > 0 ? new DecimalFormat("#.########").format(ms) : "").replace(",", ".");

        if (ss.indexOf(".") == 1)
            ss = ss.substring(1);

        while (!ss.isEmpty() && ss.length() < 4)
            ss += "0";

        return toStringFrmt(nanoTime, false) + ss;

    }

    public String toStringFull(long nanoTime) {

        TimePair[] parts = parse(nanoTime, false);

        long rest = 0;

        TimeUnit round = nanoTime < 1000 ? nanosecond
                         : nanoTime < 1000 * 1000 ? microsecond
                           : nanoTime < 1000 * 1000 * 1000 ? millisecond
                             : second;

        for (TimePair tp : parts)
            if (tp.unit.weight == round.weight - 1)
                rest = tp.value;

        Strings list = new Strings();

        for (int i = parts.length - 1; i >= 0; i--) {
            TimePair tp = parts[i];
            if (tp.unit.weight >= round.weight)
                list.add(tp.value + (tp.unit == round && rest > 0 ? "." + rest : "") + " " + tp.unit.getFullName(tp.value));
        }

        return list.toString(" ");
    }

    private final long us = 1000;
    private final long ms = 1000 * us;
    private final long s = 1000 * ms;
    private final long m = 60 * s;
    private final long h = 60 * m;
    private final long d = 24 * h;
    private final long w = 7 * d;
    private final long y = 365 * d;

    public TimePair[] parse(long nanoTime, boolean includeEmpty) {
        List<TimePair> list = new LinkedList<>();

        long val = nanoTime;

        val = nanoTime % 1000;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, nanosecond));
        nanoTime /= 1000;

        val = nanoTime % 1000;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, microsecond));
        nanoTime /= 1000;

        val = nanoTime % 1000;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, millisecond));
        nanoTime /= 1000;

        val = nanoTime % 60;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, second));
        nanoTime /= 60;

        val = nanoTime % 60;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, minute));
        nanoTime /= 60;

        val = nanoTime % 24;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, hour));
        nanoTime /= 24;

        val = nanoTime % 365;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, day));
        nanoTime /= 365;

        val = nanoTime;
        if (includeEmpty || val > 0)
            list.add(new TimePair(val, year));

        TimePair[] parts = new TimePair[list.size()];
        list.toArray(parts);
        return parts;
    }

    public TimeDiff(long msTime) {
        time = msTime;
        msTime = Math.abs(msTime);

        iMilliseconds = (int) msTime % 1000;
        msTime /= 1000;
        iSeconds = (int) msTime % 60;
        msTime /= 60;
        iMinutes = (int) msTime % 60;
        msTime /= 60;
        iHours = (int) msTime % 24;
        msTime /= 24;
        iDays = (int) msTime;

        sDays = Integer.toString(iDays);
        sHours = (iHours < 10 ? "0" : "") + iHours;
        sMinutes = (iMinutes < 10 ? "0" : "") + iMinutes;
        sSeconds = (iSeconds < 10 ? "0" : "") + iSeconds;
        sMilliseconds = (iMilliseconds < 10 ? "00"
                         : iMilliseconds < 100 ? "0" : "") + iMilliseconds;
    }

    public String getWithUnit() {
        long t = Math.abs(time);
        return (time < 0 ? "-" : "")
                + (t >= 1000 * 60 * 60 * 24 ? sDays + " dni " : "")
                + (t >= 1000 * 60 * 60 ? sHours + ":" : "")
                + (t >= 1000 * 60 ? sMinutes + ":" : "")
                + (t >= 1000 ? sSeconds + "." : "")
                + (t > 1000 ? sMilliseconds : iMilliseconds)
                + " "
                + (t >= 1000 * 60 * 60 ? "godz"
                   : t >= 1000 * 60 ? "min"
                     : t >= 1000 ? "s" : "ms");
    }

    public String toString(boolean inclMilliseconds) {
        return (time < 0 ? "-" : "")
                + (iDays > 0
                   ? sDays + " dni " : "") + sHours
                + ":" + sMinutes + ":" + sSeconds
                + (inclMilliseconds ? "." + sMilliseconds : "");
    }

    public String getDynamic(boolean inclMilliseconds) {
        // zwraca datę w formacie mm:ss lub mm.ss.zzz
        // jeśli przekroczone będą dane wartości wtedy dodana zostanie 
        // sekcja godzin i dni
        long t = Math.abs(time);
        return (time < 0 ? "-" : "")
                + (t >= 1000 * 60 * 60 * 24 ? sDays + " dni " : "")
                + (t >= 1000 * 60 * 60 ? sHours + ":" : "")
                + sMinutes + ":" + sSeconds
                + (inclMilliseconds ? "." + sMilliseconds : "");
    }
}
