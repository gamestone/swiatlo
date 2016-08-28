package com.json;

import com.json.exceptions.JException;
import com.json.exceptions.JNotFound;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Miłosz Ziernik 2014/04/15
 */
public class JArray extends JCollection {

    private final ArrayList<JElement> items = new ArrayList<>();

    public static JArray parse(String source) {
        return JSON.parse(source).asArray();
    }

    public static JArray parse(InputStream source) {
        return JSON.parse(source).asArray();
    }

    public static JArray parse(Reader source) {
        return JSON.parse(source).asArray();
    }

    @Override
    JElement addElement(String name, JElement el, boolean insert) {
        if (el == null && !options.acceptNulls())
            return null;

        if (insert)
            items.add(0, el);
        else
            items.add(el);
        if (el != null)
            el.parent = this;
        return el;
    }

    public JArray array() {
        JArray arr = new JArray();
        items.add(arr);
        arr.parent = this;
        return arr;
    }

    public JObject object() {
        JObject obj = new JObject();
        items.add(obj);
        obj.parent = this;
        return obj;
    }

    @Override
    public Iterator<JElement> iterator() {
        List<JElement> list = new LinkedList<>();
        list.addAll(items);
        return list.iterator();
    }

    /**
    Dodaje element i zwraca siebie
    @param object
    @return 
    */
    public JArray _add(Object object) {
        addElement(null, JSON.serialize(object), false);
        return this;
    }

    public JElement add(Object object) {
        return addElement(null, JSON.serialize(object), false);
    }

    public JElement insert(Object object) {
        return addElement(null, JSON.serialize(object), true);
    }

    public JArray addAll(Object... object) {
        if (object != null)
            for (Object o : object)
                add(o);
        return this;
    }

    public JElement getElement(JElement def, int index) {
        return index >= 0 && index < items.size() ? items.get(index) : def;
    }

    public JElement getElement(int index) throws JNotFound {
        if (index >= 0 && index < items.size())
            return items.get(index);
        throw new JNotFound(this, index);
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
    public void sort() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sort(Comparator<JElement> comparator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void join(JCollection second, boolean left) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove() {
        return parent != null ? parent.doRemove(this) : null;
    }

    @Override
    boolean doRemove(Object obj) {
        return items.remove(obj);
    }

    @Override
    public void move(JCollection destination) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveChildren(JCollection destination) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected boolean hasOnlyValues() {

        int length = 0;

        // jeśli tablica zawiera tylko wartości to nie dodawaj entera
        for (JElement el : items) {

            if (el.isArray() || el.isObject())
                return false;

            if (el.isValue() && el.asValue().isString()) {
                if (el.uncommented || (el.comment != null && !el.comment.isEmpty()))
                    return false;
                length += el.asValue().asString().length();
                if (length > 80)
                    return false;
                // jeśli łączna długość stringów w tablicy przekroczy 80 znaków to rozdziel na linie
            }

        }
        return true;
    }

    @Override
    public void invert() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deserialize(Object destination
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
