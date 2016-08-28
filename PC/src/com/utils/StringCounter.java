package com.utils;

import java.util.*;

public class StringCounter {

    public boolean trim = true;
    public boolean nulls = false;
    public Boolean changeCase; // true -> upper, false - lower
    public String emptyReplacment = null;
    public final Map<String, Integer> map = new HashMap<>();

    private String name(String item) {
        if (!nulls && item == null)
            item = "";
        if (trim && item != null)
            item = item.trim();
        if (emptyReplacment != null && item != null && item.isEmpty())
            item = emptyReplacment;
        if (item != null && changeCase != null && changeCase == true)
            item = item.toUpperCase();
        if (item != null && changeCase != null && changeCase == false)
            item = item.toLowerCase();
        return item;
    }

    public final int get(final String item) {
        Integer val = map.get(name(item));
        if (val == null)
            val = 0;
        return val;
    }

    public final void remove(final String item) {
        map.remove(name(item));
    }

    public final void incr(final String item) {
        map.put(item, get(name(item)) + 1);
    }

    public final void decr(final String item) {
        map.put(item, get(name(item)) - 1);
    }
}
