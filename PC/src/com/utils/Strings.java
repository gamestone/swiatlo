package com.utils;

import com.FieldUtils;
import com.StringUtils;
import com.Utils;
import java.util.*;

/**
 *
 * @author milosz
 */
public class Strings implements Iterable<String> {

    private final LinkedList<String> list = new LinkedList<>();
    private String separator = ", ";
    private String prefix;
    private String surfix;
    private boolean nonEmpty;
    private boolean escapeJS;
    private boolean allowNulls;
    private boolean trim;
    private boolean unique = true;
    private boolean caseSensitive = true;
    private final List<Pair<String, String>> replace = new LinkedList<>();

    public Strings() {

    }

    public Strings(String... objects) {
        addAll(objects);
    }

    public Strings(Object... objects) {
        addAll(objects);
    }

    public Strings(Collection<Object> collection) {
        addAll(collection);
    }

    public List<String> getList() {
        LinkedList<String> result = new LinkedList<>();
        result.addAll(list);
        return result;
    }

    public String[] getArray() {
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    public Strings replace(String from, Number to) {
        return replace(from, "" + to);
    }

    public Strings setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public Strings replace(String from, String to) {
        replace.add(new Pair<>(from, to));
        return this;
    }

    public Strings escapeJS(boolean escapeJS) {
        this.escapeJS = escapeJS;
        return this;
    }

    public final Strings unique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public final Strings caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public final Strings prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public final Strings surfix(String surfix) {
        this.surfix = surfix;
        return this;
    }

    public final Strings nonEmpty(boolean nonEmpty) {
        this.nonEmpty = nonEmpty;
        return this;
    }

    public final Strings trim(boolean trim) {
        this.trim = trim;
        return this;
    }

    public final Strings allowNulls(boolean allowNulls) {
        this.allowNulls = allowNulls;
        return this;
    }

    public Strings addAll(Object... objects) {
        return add(false, objects);
    }

    public Strings addAll(String... objects) {
        return add(false, objects);
    }

    public Strings addAll(Collection<Object> collection) {
        if (collection != null)
            for (Object s : collection)
                add(false, s);
        return this;
    }

    public Strings add(Object object) {
        return add(false, object);
    }

    public Strings insert(Object object) {
        return add(true, object);
    }

    private Strings add(boolean first, Object object) {

        List<Object> items = new LinkedList<>();

        if (object instanceof Strings)
            items.addAll(((Strings) object).list);
        else {

            if (object != null && object.getClass().isArray()) {
                Object[] arr = (Object[]) object;
                for (Object obj : arr)
                    add(first, obj);
                return this;
            }

            if (object != null && object instanceof Iterable) {
                for (Object obj : (Iterable) object)
                    add(first, obj);
                return this;
            }

            items.add(object);
        }

        for (Object obj : items) {
            String val = object != null ? obj.toString() : null;

            if (!allowNulls && val == null)
                return this;

            if (nonEmpty && (val == null || val.toString().trim().isEmpty()))
                return this;

            if (escapeJS)
                val = StringUtils.escapeJS(object, false);

            if (val == null)
                val = "null";

            for (Pair<String, String> p : replace)
                val = Utils.replace(val, p.first, p.second);

            if (!unique && contains(val))
                return this;

            if (first)
                list.add(0, val);
            else
                list.add(val);
        }
        return this;
    }

    public String get(int index) {
        if (index >= 0 && index < list.size())
            return list.get(index);
        return null;
    }

    public String toString(String separator) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0)
                sb.append(separator);
            if (prefix != null)
                sb.append(prefix);
            if (trim)
                s = s.trim();
            sb.append(s);
            if (surfix != null)
                sb.append(surfix);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(separator);
    }

    @Override
    public Iterator<String> iterator() {
        return list.iterator();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Strings remove(int index) {
        if (index >= 0 && index < list.size())
            list.remove(index);
        return this;
    }

    public String last(boolean remove) {
        return remove ? list.pollLast() : list.peekLast();
    }

    public String first(boolean remove) {
        return remove ? list.pollFirst() : list.peekFirst();
    }

    public Strings remove(String item) {
        list.remove(item);
        return this;
    }

    public boolean has(String text) {
        return contains(text);
    }

    public boolean contains(String text) {
        for (String s : list)
            if ((caseSensitive && s != null && s.equals(text))
                    || (!caseSensitive && s != null && s.equalsIgnoreCase(text)))
                return true;
        return false;

    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

}
