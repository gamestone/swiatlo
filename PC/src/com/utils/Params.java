package com.utils;

import mlogger.Log;
import com.utils.Params.Param;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class Params implements Iterable<Param> {

    public static class Param implements Comparable<Param> {

        public final Params parent;
        public final String name;
        public final Object value;

        private Param(Params parent, String name, Object value) {
            this.parent = parent;
            this.name = name;
            this.value = value;
        }

        public void remove() {
            synchronized (parent.list) {
                parent.list.remove(this);
            }
        }

        @Override
        public int compareTo(Param o) {
            return this.name.compareTo(o.name);
        }

        public String getValue() {
            if (value == null)
                return null;
            return value.toString();
        }
    }

    private final LinkedList<Param> list = new LinkedList<>();
    public boolean caseSensitive = true;
    public boolean unique = false;
    public boolean allowNulls = true;

    public Params add(String name, Object value) {
        if (name == null || (!allowNulls && value == null))
            return this;
        synchronized (list) {
            if (unique) {
                Param p = getParam(name);
                if (p != null)
                    p.remove();
            }
            list.add(new Param(this, name, value));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Param> get() {
        return ((LinkedList<Param>) list.clone());
    }

    public Param getParam(String name) {
        synchronized (list) {
            for (Param p : list)
                if ((caseSensitive && p.name.equals(name))
                        || (!caseSensitive && p.name.equalsIgnoreCase(name)))
                    return p;
            return null;
        }
    }

    public String get(String name) {
        return get(name, null);
    }

    public String get(String name, String def) {
        Param param = getParam(name);
        String val = param != null ? param.getValue() : null;
        return val != null ? val : def;
    }

    public LinkedList<Param> getGroup(String name) {
        LinkedList<Param> lst = new LinkedList<>();
        synchronized (list) {
            for (Param p : list)
                if ((caseSensitive && p.name.equals(name))
                        || (!caseSensitive && p.name.equalsIgnoreCase(name)))
                    lst.add(p);
        }
        return lst;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Param> iterator() {
        return ((List) list.clone()).iterator();
    }

    public static Params fromURI(String query) {
        Params params = new Params();
        if (query == null)
            return params;
        String[] posts = query.split("&");
        for (String s : posts) {
            String s1;
            String s2;
            if (s.contains("=")) {
                s1 = s.substring(0, s.indexOf("="));
                s2 = s.substring(s.indexOf("=") + 1, s.length());
            }
            else {
                s1 = s;
                s2 = "";
            }
            params.add(s1, s2);
        }
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        synchronized (list) {
            for (Param p : list) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(p.name).append(" = ");
                if (p.value == null)
                    sb.append("null");
                else
                    if (p.value instanceof String)
                        sb.append("\"").append(p.value.toString()).append("\"");
                    else
                        sb.append(p.value.toString());
            }
        }
        return sb.toString();
    }

    public String toURI() {
        StringBuilder sb = new StringBuilder();

        synchronized (list) {
            for (Param p : list) {
                if (sb.length() > 0)
                    sb.append("&");
                try {
                    sb.append(URLEncoder.encode(p.name, "UTF-8")
                            .replaceAll("\\+", "%20"));
                } catch (UnsupportedEncodingException ex) {
                }
                String val = p.getValue();
                if (val == null)
                    continue;
                try {
                    sb.append("=").append(URLEncoder.encode(val, "UTF-8")
                            .replaceAll("\\+", "%20"));
                } catch (UnsupportedEncodingException ex) {
                }
            }
        }
        return sb.toString();
    }
}
