package com.json;

import com.google.gson.*;
import com.utils.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import mlogger.Log;

/**
 * Miłosz Ziernik 2014/04/15
 */
public abstract class JElement {

    String name;
    String comment;
    boolean uncommented;
    public final Map<String, Object> extra = new LinkedHashMap<String, Object>();
    JCollection parent;

    public abstract boolean remove();

    public abstract void move(JCollection destination);

    public abstract void moveChildren(JCollection destination);

    public JCollection getParrent() {
        return parent;
    }

    public Strings getPath() {
        return new Strings();
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return -1;
    }

    public JObject asObject() {
        return (JObject) this;
    }

    public boolean isObject() {
        return this instanceof JObject;
    }

    public boolean isArray() {
        return this instanceof JArray;
    }

    public JArray asArray() {
        return (JArray) this;
    }

    public boolean isValue() {
        return this instanceof JValue;
    }

    public JValue asValue() {
        return (JValue) this;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            print(sw);
        } catch (IOException e) {
            onException(e);
        }
        return sw.toString();
    }

    public void save(OutputStream out) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        print(writer);
        writer.flush();
    }

    public void save(File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            save(out);
        } finally {
            out.close();
        }
    }

    void onException(Exception e) {
        Log.warning(e);
    }

    protected abstract void print(Appendable writer, String intent) throws IOException;

    public void print(Appendable writer) throws IOException {
        print(writer, "");
    }
    /*
     public void print(File file) throws IOException {
     try (BufferedWriter wr = getWriter(file);) {
     print(wr, "", 0, 1, true);
     }
     }
     */

    JOptions getOptions() {
        return this instanceof JCollection ? ((JCollection) this).options
                : parent != null ? parent.options : new JOptions(null);
    }

    public int getLevel() {
        int level = 0;
        JElement el = parent;
        while (el != null) {
            ++level;
            el = el.parent;
        }
        return level;
    }

    protected void escape(String string, Appendable w, boolean escapeUnicode) throws IOException {
        if (string == null || string.length() == 0)
            return;
        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.append('\\');
                    w.append(c);
                    break;
                case '/':
                    if (b == '<')
                        w.append('\\');
                    w.append(c);
                    break;
                case '\b':
                    w.append("\\b");
                    break;
                case '\t':
                    w.append("\\t");
                    break;
                case '\n':
                    w.append("\\n");
                    break;
                case '\f':
                    w.append("\\f");
                    break;
                case '\r':
                    w.append("\\r");
                    break;
                default:
                    if (c < 32 || (escapeUnicode && c > 127)) {
                        w.append("\\u");
                        hhhh = Integer.toHexString(c);
                        w.append("0000", 0, 4 - hhhh.length());
                        w.append(hhhh);
                    }
                    else
                        w.append(c);
            }
        }
    }

    public JElement setComment(String commnet) {
        this.comment = commnet;
        return this;
    }

    public JElement setUncommented(boolean uncommented) {
        this.uncommented = uncommented;
        return this;
    }
}
