package com.utils.date.intf;

public enum TimeUnit {

    nanosecond(0, "ns"),
    microsecond(1, "µs"),
    millisecond(2, "ms"),
    second(3, "s"),
    minute(4, "m"),
    hour(5, "h"),
    day(6, "d"),
    year(7, "y");

    public String getFullName(long value) {

        switch (this) {
            case nanosecond:
                return value == 1 ? "nanosekunda"
                       : value > 1 && value < 5 ? "nanosekundy" : "nanosekund";
            case microsecond:
                return value == 1 ? "mikrosekunda"
                       : value > 1 && value < 5 ? "mikrosekundy" : "mikrosekund";
            case millisecond:
                return value == 1 ? "milisekunda"
                       : value > 1 && value < 5 ? "milisekundy" : "milisekund";
            case second:
                return value == 1 ? "sekunda"
                       : value > 1 && value < 5 ? "sekundy" : "sekund";
            case minute:
                return value == 1 ? "minuta"
                       : value > 1 && value < 5 ? "minuty" : "minut";
            case hour:
                return value == 1 ? "godzina"
                       : value > 1 && value < 5 ? "godziny" : "godzin";
            case day:
                return value == 1 ? "dzień" : "dni";
                //   case week:
            //        return value == 1 ? "tydzień" : value < 5 ? "tygodnie" : "tygodni";
            case year:
                return value == 1 ? "rok"
                       : value > 1 && value < 5 ? "lata" : "lat";
        }
        return "";

    }

    private TimeUnit(int weight, String shortUnit) {
        this.weight = weight;
        this.shortUnit = shortUnit;
    }

    public final int weight;
    public final String shortUnit;
}
