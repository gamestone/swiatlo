package com.utils;

import com.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Miłosz Ziernik
 * 2013/11/05 
 */
public final class Timestamp {

    public final class Diff {

        public final long nano;
        public final Double milliseconds;
        public final String unit;
        public final double trimmed;

        private Diff(final long nano) {
            this.nano = nano;
            milliseconds = (double) Math.round(100 * nano / 1000000d) / 100d;

            String s = "ns";
            Double diff = (double) (System.nanoTime() - time);

            if (diff >= 1000 * 1000 * 1000) {
                diff /= 1000000000d;
                s = "s";
            } else
                if (diff >= 1000 * 1000) {
                    diff /= 1000000d;
                    s = "ms";
                } else
                    if (diff >= 1000) {
                        diff /= 1000d;
                        s = "us";
                    }

            unit = s;
            trimmed = diff;
        }

        @Override
        public String toString() {
            return Utils.formatFloat(trimmed) + " " + unit;
        }
    }

    private final long time = System.nanoTime();

    public final Diff diff() {
        return new Diff(System.nanoTime() - time);
    }

    public final long getNanoTime() {
        return time;
    }

    public final void consoleDiff(Object... elements) {

        System.out.print(diff().toString());

        Strings lst = new Strings().addAll(elements);

        if (!lst.isEmpty())
            System.out.print(" " + lst.toString());

        System.out.println("");
    }

    /**
     przykład 21:07:09.595 0.37 ms
     */
    public final void consoleTimeDiff(Object... elements) {

        String ms = new SimpleDateFormat("SSS").format(new Date());

        while (ms.length() < 3)
            ms = "0" + ms;
        StringBuilder sb = new StringBuilder();

        sb.append("\033[").append(Integer.toString(30 + 4)).append("m");
        sb.append(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        sb.append(".").append(ms);

        sb.append("\033[0m");
        sb.append(" ");
        sb.append(diff().toString());
        Strings lst = new Strings().addAll(elements);

        if (!lst.isEmpty())
            sb.append(" ").append(lst.toString());

        sb.append("");
        System.out.println(sb);
    }

    /*
     Czas wykonania danej metody
     */
    public void consoleDiffMethod(Object... objects) {
        consoleTimeDiff(new Strings()
                .addAll(objects)
                .add(Utils.getCurrentMethodName(1)));
    }
}
