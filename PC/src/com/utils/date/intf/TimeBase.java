package com.utils.date.intf;

public enum TimeBase {

    seconds(0),
    minutes(1),
    hours(2),
    days(3),
    years(4);
    public final int weight;

    private TimeBase(int weight) {
        this.weight = weight;
    }

    public static TimeBase get(int weight) {
        for (TimeBase tb : TimeBase.values())
            if (tb.weight == weight)
                return tb;
        return null;
    }

}
