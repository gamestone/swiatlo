package com.utils.date;

import com.utils.*;
import static com.utils.date.intf.TimeUnit.*;
import com.utils.date.intf.*;
import java.text.*;
import java.util.*;

public class TTime {

    /*
    
    
     Jesli nanotime = 0 to toString nic nie zwraca
    
    
     */
    private final long nanoTime;
    private final boolean minus;
    private int precisionMin = 0; // 0..9 // liczba miejsc dziesietnych
    private int precisionMax = 9;
    private TimeBase baseMin = TimeBase.minutes;
    private TimeBase baseMax = TimeBase.years;

    public final static long NS = 1;
    public final static long US = 1000;
    public final static long MS = 1000 * US;
    public final static long S = 1000 * MS;
    public final static long M = 60 * S;
    public final static long H = 60 * M;
    public final static long D = 24 * H;
    public final static long Y = 365 * D;

    public final TimeElement parts;
    public final TimeElement total;

    public TTime precision(int precisionMin, int precisionMax) {
        this.precisionMin = precisionMin > 9 ? 9 : precisionMin < 0 ? 0 : precisionMin;
        this.precisionMax = precisionMax > 9 ? 9 : precisionMax < 0 ? 0 : precisionMax;
        return this;
    }

    public TTime base(TimeBase baseMin, TimeBase baseMax) {
        this.baseMin = baseMin;
        this.baseMax = baseMax;
        return this;
    }

    /**
    
     @param milliseconds czas w milisekundach
     @return Formatuje czas do postaci mm:ss, hh:mm:ss lub dd hh:mm:ss
     */
    public static String formatSecondsMS(long milliseconds) {
        return new TTime(milliseconds * 1000 * 1000)
                .base(TimeBase.minutes, TimeBase.days)
                .precision(0, 0)
                .toStringFrmt();
    }

    public TTime(int hour, int minute, int second) {
        nanoTime = Math.abs(hour) * H + Math.abs(minute) * M + Math.abs(second) * S;
        parts = new TimeElement(true);
        total = new TimeElement(false);
        minus = false;
    }

    public TTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        nanoTime = cal.get(Calendar.HOUR_OF_DAY) * H
                + cal.get(Calendar.MINUTE) * M
                + cal.get(Calendar.SECOND) * S
                + cal.get(Calendar.MILLISECOND) * MS;
        parts = new TimeElement(true);
        total = new TimeElement(false);
        minus = false;
    }

    public TTime(double milliseconds) {
        this.nanoTime = Math.abs(Math.round(milliseconds * 1000d * 1000d));
        parts = new TimeElement(true);
        total = new TimeElement(false);
        minus = milliseconds < 0;
    }

    public TTime(long nanoTime) {
        this.nanoTime = Math.abs(nanoTime);
        parts = new TimeElement(true);
        total = new TimeElement(false);
        minus = nanoTime < 0;
    }

    public TTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        nanoTime = cal.get(Calendar.HOUR_OF_DAY) * H
                + cal.get(Calendar.MINUTE) * M
                + cal.get(Calendar.SECOND) * S
                + cal.get(Calendar.MILLISECOND) * MS;
        parts = new TimeElement(true);
        total = new TimeElement(false);
        minus = false;
    }

    @Override
    public String toString() {
        return toStringFull(false);
    }

    /**
     @param nanoTime
     @param includeMilliseconds
     @return 
     Format 00:05:30.000
  
    
     */
    /**
     mm:ss.zzz milisekyndy opcjonalne
    
    
     nie dynamiczny (HH.mm.ss.SSS) : Część milisekundowa zawsze na 3 znakach (o ile występuje)
     */
    public String toStringFrmt() {
        return toStringFrmt(baseMin, baseMax, precisionMin, precisionMax);
    }

    private String toStringFrmt(TimeBase baseMin, TimeBase baseMax, int precisionMin, int precisionMax) {

        TimeBase base
                = total.years > 0 ? TimeBase.years
                  : total.days > 0 ? TimeBase.days
                    : total.hours > 0 ? TimeBase.hours
                      : total.minutes > 0 ? TimeBase.minutes
                        : TimeBase.seconds;

        if (baseMax == null)
            baseMax = baseMin != null && base.weight > baseMin.weight ? base : baseMin;

        if (baseMin == null)
            baseMin = baseMax != null && base.weight < baseMax.weight ? base : baseMax;

        if (baseMax != null && base.weight > baseMax.weight)
            base = baseMax;

        if (baseMin != null && base.weight < baseMin.weight)
            base = baseMin;

        String result = "";
        switch (base) {
            case seconds:
                result = String.format("%02d", total.seconds);
                break;
            case minutes:
                result = String.format("%02d:%02d", total.minutes, parts.seconds);
                break;
            case hours:
                result = String.format("%02d:%02d:%02d",
                        total.hours, parts.minutes, parts.seconds);
                break;
            case days:
                result = String.format("%d " + TimeUnit.day.getFullName(total.days)
                        + " %02d:%02d:%02d", total.days, parts.hours,
                        parts.minutes, parts.seconds);
                break;

            case years:
                result = String.format("%d "
                        + TimeUnit.year.getFullName(total.years)
                        + " %d " + TimeUnit.day.getFullName(parts.days)
                        + " %02d:%02d:%02d", total.years, parts.days,
                        parts.hours, parts.minutes, parts.seconds);
                break;
        }

        return (minus ? "- " : "") + result + getDynalicMsPart(precisionMin, precisionMax);

    }

    private String getDynalicMsPart(int precisionMin, int precisionMax) {

        if (precisionMax == 0)
            return "";

        double ms = nanoTime % (1000 * 1000 * 1000) / (1000d * 1000d * 1000d);

        String ss = "#.#########";

        precisionMin = precisionMin < precisionMax ? precisionMin : precisionMax;
        precisionMax = precisionMax > precisionMin ? precisionMax : precisionMin;

        if (Math.max(precisionMin, precisionMax) > 0) {
            ss = "#.";
            for (int i = 0; i < Math.max(precisionMin, precisionMax) && i <= 9; i++)
                ss += "#";
        }

        ss = new DecimalFormat(ss).format(ms).replace(",", ".");

        if (ss.indexOf(".") == 1)
            ss = ss.substring(1);
        if (!ss.startsWith("."))
            ss = "." + ss;

        if (!ss.isEmpty())
            while (ss.length() < precisionMin + 1)
                ss += "0";

        return ss;
    }

    public String toStringFull(boolean fullUnitName) {

        TimePair[] pairs = getParts(false);

        long rest = 0;

        TimeUnit round = nanoTime < 1000 ? nanosecond
                         : nanoTime < 1000 * 1000 ? microsecond
                           : nanoTime < 1000 * 1000 * 1000 ? millisecond
                             : second;

        for (TimePair tp : pairs)
            if (tp.unit.weight == round.weight - 1)
                rest = tp.value;

        Strings list = new Strings();

        for (int i = pairs.length - 1; i >= 0; i--) {
            TimePair tp = pairs[i];
            if (tp.unit.weight >= round.weight)
                list.add(tp.value + (tp.unit == round && rest > 0
                                     ? "." + rest : "") + " "
                        + (fullUnitName ? tp.unit.getFullName(tp.value) : tp.unit.shortUnit));
        }

        return (minus ? "- " : "") + list.toString(" ");
    }

    public TimePair[] getParts(boolean includeEmpty) {
        List<TimePair> list = new LinkedList<>();

        if (includeEmpty || parts.nanoseconds > 0)
            list.add(new TimePair(parts.nanoseconds, nanosecond));

        if (includeEmpty || parts.microseconds > 0)
            list.add(new TimePair(parts.microseconds, microsecond));

        if (includeEmpty || parts.milliseconds > 0)
            list.add(new TimePair(parts.milliseconds, millisecond));

        if (includeEmpty || parts.seconds > 0)
            list.add(new TimePair(parts.seconds, second));

        if (includeEmpty || parts.minutes > 0)
            list.add(new TimePair(parts.minutes, minute));

        if (includeEmpty || parts.hours > 0)
            list.add(new TimePair(parts.hours, hour));

        if (includeEmpty || parts.days > 0)
            list.add(new TimePair(parts.days, day));

        if (includeEmpty || parts.years > 0)
            list.add(new TimePair(parts.years, year));

        TimePair[] parts = new TimePair[list.size()];
        list.toArray(parts);
        return parts;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TTime
                && ((TTime) obj).nanoTime == nanoTime;
    }

    @Override
    public int hashCode() {
        return (int) (141 * 7 + this.nanoTime);
    }

    public class TimePair {

        public final long value;
        public final TimeUnit unit;

        public TimePair(long value, TimeUnit unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    public class TimeElement {

        public final long nanoseconds;
        public final long microseconds;
        public final long milliseconds;
        public final long seconds;
        public final long minutes;
        public final long hours;
        public final long days;
        public final long years;

        public TimeElement(boolean part) {
            years = nanoTime / Y;
            days = part ? (nanoTime / D) % 365 : nanoTime / D;
            hours = part ? (nanoTime / H) % 24 : nanoTime / H;
            minutes = part ? (nanoTime / M) % 60 : nanoTime / M;
            seconds = part ? (nanoTime / S) % 60 : nanoTime / S;
            milliseconds = part ? (nanoTime / MS) % 1000 : nanoTime / MS;
            microseconds = part ? (nanoTime / US) % 1000 : nanoTime / US;
            nanoseconds = part ? (nanoTime / NS) % 1000 : nanoTime / NS;
        }

        @Override
        public String toString() {
            return String.format("%dy %dd %dh %dm %ds %dms %dus %dns",
                    years, days, hours, minutes, seconds, milliseconds, microseconds, nanoseconds);
        }
    }

}
