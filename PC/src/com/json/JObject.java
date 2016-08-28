package com.json;

import com.json.exceptions.JException;
import com.utils.Strings;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Miłosz Ziernik 2014/04/15
 */
public class JObject extends JCollection {

    final Map<String, JElement> items = new LinkedHashMap<String, JElement>();

    public static JObject parse(String source) {
        return JSON.parse(source).asObject();
    }

    public static JObject parse(InputStream source) {
        return JSON.parse(source).asObject();
    }

    public static JObject parse(Reader source) {
        return JSON.parse(source).asObject();
    }

    @Override
    JElement addElement(String name, JElement el, boolean insert) {
        if (el == null && !options.acceptNulls())
            return null;

        Map<String, JElement> map = new LinkedHashMap<>();
        if (insert) {
            map.putAll(items);
            items.clear();
            map.remove(name);
        }

        if (el != null)
            el.name = name;
        items.put(name, el);

        if (insert)
            items.putAll(map);

        if (el != null)
            el.parent = this;
        return el;
    }

    @Override
    public final Iterator<JElement> iterator() {
        Set<JElement> set = new LinkedHashSet<>();
        set.addAll(items.values());
        return set.iterator();
    }

    public JObject put(String name, Object object) {
         addElement(name, JSON.serialize(object), false);
         return this;
    }

    public JElement add(String name, Object object) {
        return put(name, object);
    }

    public boolean has(String name) {
        return contains(name);
    }

    public boolean contains(String name) {
        return items.containsKey(name);
    }

    /**
     * dodaj element na poczatku listy
     */
    public JElement insert(String name, Object object) {
        return addElement(name, JSON.serialize(object), true);
    }

    public String getStr(String name) throws JException {
        return getElement(name).asValue().asString();
    }

    public String getStr(String name, String def) {
        JValue jval = getValue(name);
        return jval != null && jval.isString() ? jval.asString() : def;
    }

    public int getInt(String name) throws JException {
        return getElement(name).asValue().asNumber().intValue();
    }

    public Integer getInt(String name, Integer def) {
        Number number = getNumber(name, null);
        return number != null ? number.intValue() : def;
    }

    public long getLong(String name) throws JException {
        return getElement(name).asValue().asNumber().longValue();
    }

    public Long getLong(String name, Long def) {
        Number number = getNumber(name, null);
        return number != null ? number.longValue() : def;
    }

    public Number getNumber(String name, Number def) {
        JValue jval = getValue(name);
        return jval != null && jval.isNumber() ? jval.asNumber() : def;
    }

    public Boolean getBool(String name, Boolean def) {
        JValue jval = getValue(name);
        return jval != null && jval.isBoolean() ? jval.asBoolean() : def;
    }

    public JElement getElement(final String... names) throws JException {
        JElement element = getElement(null, names);
        if (element == null)
            throw new JException("Nie znaleziono obiektu \""
                    + new Strings(names).toString("/") + "\"");
        return element;
    }

    public Map<String, JElement> getItems() {
        return Collections.synchronizedMap(items);
    }

    /* public JObject getObject(final JObject def, final String... names) {
     JElement el = getElement(def, names);
     return el != null && el.isObject() ? el.asObject() : def;
     }

     public JArray getArray(final JArray def, final String... names) {
     JElement el = getElement(def, names);
     return el != null && el.isArray() ? el.asArray() : def;
     }
     */
    public JValue getValue(final String name) {
        JElement el = getElement((JElement) null, name);
        return el != null && el.isValue() ? el.asValue() : null;
    }

    public JValue getValueF(final String name) throws JException {
        return getElement(new String[]{name}).asValue();
    }

    public JElement getElement(final JElement def, final String... names) {
        if (names == null || names.length == 0)
            return this;

        return new Object() {

            private JElement visit(JObject obj, int level) {

                JElement el = obj.items.get(names[level]);
                if (el == null)
                    return def;

                if (level == names.length - 1)
                    return el;

                if (!el.isObject())
                    return def;

                return visit(el.asObject(), level + 1);
            }
        }.visit(this, 0);
    }

    /**
     *
     * @param includeNonStringValues konwertuje wartości liczbowe i boolean do
     * tekstu
     * @return Zwraca listę wartości
     */
    public LinkedList<String> getStringValues(boolean includeNonStringValues) {
        LinkedList<String> list = new LinkedList<>();
        for (JElement el : items.values())
            if (el != null && el.isValue()) {
                JValue jval = el.asValue();
                if (jval.isNull() && !options.acceptNulls())
                    continue;
                Object val = jval.value();
                if (includeNonStringValues || jval.isString())
                    list.add(val == null ? null : val.toString());
            }
        return list;
    }

    public LinkedList<Number> getNumberValues(boolean includeNonStringValues) {
        LinkedList<Number> list = new LinkedList<>();
        for (JElement el : items.values())
            if (el != null && el.isValue() && el.asValue().isNumber())
                list.add(el.asValue().asNumber());
        return list;
    }

    /**
     *
     * @param names ścieżka nazw obiektów
     * @return Zwraca istniejący obiekt lub tworzy nowy jeśli nie istnieje
     */
    public JObject object(final String... names) {
        if (names == null || names.length == 0)
            return this;

        return new Object() {

            private JObject visit(JObject obj, int level) {

                JObject jobj = null;
                JElement element = obj.items.get(names[level]);
                if (element != null && element.isObject())
                    jobj = element.asObject();

                if (jobj == null) {
                    jobj = new JObject();
                    jobj.name = names[level];
                    jobj.parent = obj;
                    obj.items.put(names[level], jobj);
                    jobj.parent = obj;
                }

                if (level == names.length - 1)
                    return jobj;

                return visit(jobj, level + 1);

            }
        }.visit(this, 0);
    }

    /**
     * @param names ścieżka nazw obiektów
     * @return Zwraca istniejącą tablicę lub tworzy nową jeśli nie istnieje
     */
    public JArray array(final String... names) {
        if (names == null || names.length == 0)
            return null;
        JObject obj = object(Arrays.copyOfRange(names, 0, names.length - 1));
        String name = names[names.length - 1];
        JElement element = obj.items.get(name);
        if (element != null && element.isArray())
            return element.asArray();

        JArray arr = new JArray();
        arr.parent = obj;
        arr.name = name;
        obj.items.put(name, arr);
        arr.parent = obj;
        return arr;
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean remove() {
        return parent != null ? parent.doRemove(name) : false;
    }

    @Override
    boolean doRemove(Object obj) {
        return items.remove(obj) != null;
    }

    @Override
    public void move(JCollection destination) {
        if (destination == null)
            return;
        remove();
        destination.addElement(this.name, this, false);
    }

    @Override
    public void moveChildren(JCollection destination) {

    }

    @Override
    public void join(JCollection second, boolean left) {
    }

    @Override
    public void sort() {
        Map<String, JElement> map = new TreeMap<>();
        map.putAll(items);
        items.clear();
        items.putAll(map);
    }

    @Override
    public void sort(Comparator<JElement> comparator) {

        List<JElement> list = new LinkedList<>();
        list.addAll(items.values());
        Collections.sort(list, comparator);
        items.clear();
        for (JElement el : list)
            items.put(el.getName(), el);
    }

    @Override
    public void invert() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deserialize(Object destination) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
