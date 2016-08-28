package com.utils;

import java.util.*;

public class PerformanceCounter {

    public class PCEntry {

        public Long size;
        public final long nanoTime = System.nanoTime();
        public final long time = System.currentTimeMillis();
        public String name;
    }
    public int maxEntries = 100000;
    public final LinkedList<PCEntry> entries = new LinkedList<>();

    public PCEntry add(long size) {
        return add(size, null);
    }

    public PCEntry add() {
        return add(null, null);
    }

    public PCEntry add(Long size, String name) {
        PCEntry pce = new PCEntry();
        pce.size = size;
        synchronized (entries) {
            entries.add(pce);
            while (entries.size() > maxEntries)
                entries.pollFirst();
        }
        return pce;
    }

    /**
     Pobierz elementy z danego przedziału czasowego
     */
    public List<PCEntry> getItemsInTimeRange(long startTime, long endTime) {
        List<PCEntry> lst = new ArrayList<>();
        synchronized (entries) {
            for (PCEntry pce : entries)
                if (pce.time >= startTime && pce.time <= endTime)
                    lst.add(pce);
        }
        return lst;
    }

    /**
     Zlicz wpisy w ciągu /timeDiff/ milisekund. Jeśi /timeDif/ <= 0,
     zlicza z całości

     */
    public long getItemsCount(long timeDiff) {
        long startTime = new Date().getTime();
        if (timeDiff > 0)
            startTime -= timeDiff;
        return getItemsInTimeRange(startTime, new Date().getTime()).size();
    }

    /**
     Różnica czasu w milisekundach z dokładnością do nano sekund
     */
    public Double diff(PCEntry entry) {
        if (entry == null)
            return 0d;
        double d = System.nanoTime() - entry.nanoTime;
        return d / 100000d;
    }

    public long getItemsSize(long timeDiff) {
        long startTime = new Date().getTime();
        if (timeDiff > 0)
            startTime -= timeDiff;
        long size = 0;
        for (PCEntry pce : getItemsInTimeRange(startTime, new Date().getTime()))
            size += pce.size;
        return size;
    }
}
