package com.utils;

import java.util.HashMap;

public class Counter<T> extends HashMap<T, Integer> {

    @Override
    public Integer get(Object key) {
        Integer val = super.get(key);
        if (val == null)
            val = 0;
        return val;
    }

    public Integer inc(T object) {
        return put(object, get(object) + 1);
    }

}
