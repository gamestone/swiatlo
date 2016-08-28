package com.utils;

import java.text.*;
import java.util.*;

public class TDate extends Date {

    public String defaultFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    public final static String fullInclMs = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String fullExclMs = "yyyy-MM-dd HH:mm:ss";

    private final static Strings parsePatterns = new Strings()
            .add("yyyy-MM-dd HH:mm:ss.SSS")
            .add("yyyy/MM/dd HH:mm:ss.SSS")
            .add("yyyy-MM-dd HH:mm:ss")
            .add("yyyy/MM/dd HH:mm:ss")
            .add("yyyy-MM-dd")
            .add("yyyy/MM/dd")
            .add("yyMMdd");

    public TDate() {
        super();
    }

    public TDate(Date date) {
        super(date.getTime());
    }

    public TDate(long date) {
        super(date);
    }

    /**
     Porównuje biężącą datę z limitem expireMs w milisekundach
     */
    public boolean isExpired(int expireMs) {
        return new Date().getTime() > getTime() + expireMs;
    }

    private static Date autoParse(String date) {
        for (String pattern : parsePatterns)
            try {
                Date result = new SimpleDateFormat(pattern).parse(date);
                return result;
            } catch (Exception e) {
            }
        return null;
    }

    public TDate(String date) throws ParseException {
        super(autoParse(date).getTime());
    }

    public String toString(String format) {
        return new SimpleDateFormat(format).format(this);
    }

    public String toString(boolean includeMilliseconds) {
        return new SimpleDateFormat(includeMilliseconds
                                    ? fullInclMs : fullExclMs).format(this);
    }

    public TimeDiff diff(Date date) {
        return new TimeDiff(getTime() - date.getTime());
    }

    public TimeDiff diff(long date) {
        return new TimeDiff(getTime() - date);
    }

    @Override
    public String toString() {
        return toString(defaultFormat);
    }

    public boolean isSameDay(Date other) {
        if (other == null)
            return false;

        Calendar cal1 = getCalendar();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(other);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getYear() {
        return getCalendar().get(Calendar.YEAR);
    }

    @Override
    public int getMonth() {
        return getCalendar().get(Calendar.MONTH) + 1;
    }

    @Override
    public int getDay() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    public int getWeekOfYear() {
        return getCalendar().get(Calendar.WEEK_OF_YEAR);
    }

    public int getWeekOfMonth() {
        return getCalendar().get(Calendar.WEEK_OF_MONTH);
    }

    public int getDayOfYear() {
        return getCalendar().get(Calendar.DAY_OF_YEAR);
    }

    public int getDayOfWeek() {
        return getCalendar().get(Calendar.DAY_OF_WEEK) - 1;
    }

    public int getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    public int getSecond() {
        return getCalendar().get(Calendar.SECOND);
    }

    public int getMillisecond() {
        return getCalendar().get(Calendar.MILLISECOND);
    }

    public final Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        return cal;
    }

    public final TDate addYears(int years) {
        return add(years, 0, 0, 0, 0, 0, 0, 0);
    }

    public final TDate addMonths(int months) {
        return add(0, months, 0, 0, 0, 0, 0, 0);
    }

    public final TDate addWeeks(int weeks) {
        return add(0, 0, weeks, 0, 0, 0, 0, 0);
    }

    public final TDate addDays(int days) {
        return add(0, 0, 0, days, 0, 0, 0, 0);
    }

    public final TDate addHours(int hours) {
        return add(0, 0, 0, 0, hours, 0, 0, 0);
    }

    public final TDate addMinutes(int minutes) {
        return add(0, 0, 0, 0, 0, minutes, 0, 0);
    }

    public final TDate addSeconds(int seconds) {
        return add(0, 0, 0, 0, 0, 0, seconds, 0);
    }

    public final TDate addMilliseconds(int milliseconds) {
        return add(0, 0, 0, 0, 0, 0, 0, milliseconds);
    }

    public final TDate add(int milliseconds) {
        return add(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public final TDate add(int seconds, int milliseconds) {
        return add(0, 0, 0, 0, 0, 0, seconds, 0);
    }

    public final TDate add(int minutes, int seconds, int milliseconds) {
        return add(0, 0, 0, 0, 0, minutes, seconds, 0);
    }

    public final TDate add(int hours, int minutes, int seconds, int milliseconds) {
        return add(0, 0, 0, 0, hours, minutes, seconds, 0);
    }

    public final TDate add(int days, int hours, int minutes, int seconds, int milliseconds) {
        return add(0, 0, 0, days, hours, minutes, seconds, 0);
    }

    public final TDate add(int weeks, int days, int hours, int minutes, int seconds, int milliseconds) {
        return add(0, 0, weeks, days, hours, minutes, seconds, 0);
    }

    public final TDate add(int months, int weeks, int days, int hours, int minutes, int seconds, int milliseconds) {
        return add(0, months, weeks, days, hours, minutes, seconds, 0);
    }

    public final TDate add(int years, int months, int weeks, int days, int hours, int minutes, int seconds, int milliseconds) {
        Calendar cal = getCalendar();
        cal.add(Calendar.MILLISECOND, milliseconds);
        cal.add(Calendar.SECOND, seconds);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.add(Calendar.WEEK_OF_YEAR, weeks);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.YEAR, years);
        setTime(cal.getTime().getTime());
        return this;
    }

    public final TDate setMilliseconds(Integer milliseconds) {
        return set(0, 0, 0, 0, 0, 0, 0, milliseconds);
    }

    public final TDate set(Integer year, Integer month, Integer week, Integer day,
            Integer hour, Integer minute, Integer second, Integer millisecond) {
        Calendar cal = getCalendar();
        if (millisecond != null)
            cal.set(Calendar.MILLISECOND, millisecond);
        if (second != null)
            cal.set(Calendar.SECOND, second);
        if (minute != null)
            cal.set(Calendar.MINUTE, minute);
        if (hour != null)
            cal.set(Calendar.HOUR_OF_DAY, hour);
        if (day != null)
            cal.set(Calendar.DAY_OF_MONTH, day);
        if (week != null)
            cal.set(Calendar.WEEK_OF_YEAR, week);
        if (month != null)
            cal.set(Calendar.MONTH, month - 1);
        if (year != null)
            cal.set(Calendar.YEAR, year);
        setTime(cal.getTime().getTime());
        return this;
    }

    public final TDate clearMilliseconds() {
        return set(null, null, null, null, null, null, null, 0);
    }

    public final TDate clearTime() {
        return set(null, null, null, null, 0, 0, 0, 0);
    }

    public boolean between(Date from, Date to) {
        return after(from) && before(to);
    }

}
