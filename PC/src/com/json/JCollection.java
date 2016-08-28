package com.json;

import com.google.gson.*;
import java.io.IOException;
import java.util.*;

/**
 * Miłosz Ziernik 2014/04/15
 */
public abstract class JCollection extends JElement
        implements Iterable<JElement> {

    public final JOptions options = new JOptions(this);

    abstract JElement addElement(String name, JElement el, boolean insert);

    public abstract void clear();

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract void sort();

    public abstract void invert();

    public abstract void sort(Comparator<JElement> comparator);

    abstract boolean doRemove(Object obj);

    public abstract void deserialize(Object destination);

    public LinkedList<JObject> getObjects() {
        LinkedList<JObject> list = new LinkedList<>();
        for (JElement el : this)
            if (el.isObject())
                list.add(el.asObject());
        return list;
    }

    public LinkedList<JValue> getValues() {
        LinkedList<JValue> list = new LinkedList<>();
        for (JElement el : this)
            if (el.isValue())
                list.add(el.asValue());
        return list;
    }

    public LinkedList<String> getValuesStr() {
        LinkedList<String> list = new LinkedList<>();
        for (JElement el : this)
            if (el.isValue())
                list.add(el.asValue().asString());
        return list;
    }

    public LinkedList<JArray> getArrays() {
        LinkedList<JArray> list = new LinkedList<>();
        for (JElement el : this)
            if (el.isArray())
                list.add(el.asArray());
        return list;
    }

    public JObject object(final String... names) {
        if (this instanceof JObject)
            return asObject().object(names);

        if (this instanceof JArray)
            return asArray().object();
        return null;
    }

    public JArray array(final String... names) {
        if (this instanceof JObject)
            return asObject().array(names);

        if (this instanceof JArray)
            return asArray().array();
        return null;
    }

    public JCollection value(final String name, final Object value) {
        if (this instanceof JObject)
            asObject().put(name, value);

        if (this instanceof JArray)
            asArray().add(value);

        return this;
    }

    @Override
    protected void print(Appendable writer, String offset) throws IOException {

        char begin = isArray() ? '[' : '{';
        char end = isArray() ? ']' : '}';

        JOptions opt = getOptions();
        String intent = opt.intent();
        boolean compact = intent.isEmpty() && (comment == null || comment.isEmpty());

        Boolean sl = opt.singleLine();

        boolean singleLine = (sl != null && sl == true)
                || (sl == null && isArray() && asArray().hasOnlyValues());

        boolean comm = comment != null && !comment.isEmpty();

        if ((sl == null || Boolean.TRUE.equals(sl)) && comm)
            singleLine = false;

        if (singleLine)
            comm = false;

        writer.append(begin);
        if (isEmpty()) {
            if (!opt.intent().isEmpty())
                writer.append(compact ? "" : " ");
            writer.append(end);
            return;
        }

        String lineBreak = compact || singleLine ? "" : opt.lineBreakChar();

        if (comm) {
            writer.append(" //");
            escape(comment, writer, options.escapeUnicode());
        }

        writer.append(lineBreak);

        String offs = compact ? "" : offset + intent;

        ArrayList<JElement> list = new ArrayList<>();
        for (JElement el : this)
            list.add(el);

        for (int i = 0; i < list.size(); i++) {

            JElement el = list.get(i);
            JElement next = i < list.size() - 1 ? list.get(i + 1) : null;

            //   if (json.writeIntf != null && !json.writeIntf.beforeWriteName(writer, el, name))
            //       continue;
            if (!singleLine && !compact)
                writer.append(offs);

            if (el instanceof JValue && el.uncommented)
                writer.append("//");

            if (el instanceof JCollection && el.uncommented)
                writer.append("/* ");

            if (isObject()) {
                boolean quot = useQuota(el.name);
                if (quot) {
                    writer.append("\"");
                    escape(el.name, writer, opt.escapeUnicode());
                    writer.append("\"");
                }
                else
                    writer.append(el.name);
                writer.append(":");
                if (!compact)
                    writer.append(" ");
            }

            el.print(writer, el instanceof JCollection ? offs : "");

            if (next != null && !next.uncommented)
                writer.append(",");

            if (el instanceof JValue && el.comment != null && !el.comment.isEmpty()) {
                writer.append(" //");
                escape(el.comment, writer, options.escapeUnicode());
            }

            if (el instanceof JCollection && el.uncommented)
                writer.append(" */");

            if (next != null)
                writer.append(compact ? "" : singleLine ? " " : lineBreak);

        }

        if (!singleLine)
            writer.append(lineBreak).append(offset);

        writer.append(end);
    }

    /**
     * Złącz dwie kolekcje
     *
     * @param second Druga kolekcja (musi być tego samego typu: JObject lub
     * JArray
     * @param left jeśli nazwy się pokrywają, zachowaj lewy (oryginalny)
     * element, w przeciwnym razie nadpisz
     */
    public abstract void join(JCollection second, boolean left);

    boolean useQuota(String str) {
        if (options.quotaNames())
            return true;

        for (int i = 0; i < str.length(); i++) {
            boolean ok = true;
            char c = str.charAt(i);
            ok = (c >= 48 && c <= 52) || (c >= 65 && c <= 90)
                    || (c >= 97 && c <= 122) || c == '_' || c == '.' || c == '-';
            if (!ok)
                return true;
            if (i == 0 && c >= 48 && c <= 52)
                return true;
        }
        return false;
    }

    /* String ln() {
     return compactMode != null && compactMode ? "" : "\n";
     }*/
    public JCollection getRoot() {

        JCollection col = this;

        while (col.parent != null)
            col = col.parent;

        return col;
    }
}
