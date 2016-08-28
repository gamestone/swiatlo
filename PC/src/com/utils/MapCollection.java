package com.utils;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapCollection<Key, Item> implements Iterable<Entry<Key, Collection<Item>>> {

    private final Map<Key, Collection<Item>> map;
    private final Class<? extends Collection> itemClass;

    public MapCollection(Class<? extends Map> mapClass, Class<? extends Collection> cls) {
        this.itemClass = cls;
        try {
            map = mapClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    public MapCollection<Key, Item> add(Key key, Item item) {
        Collection<Item> list = map.get(key);
        if (list == null) {
            list = new LinkedList<>();
            map.put(key, list);
        }
        list.add(item);
        return this;
    }

    public Collection<Item> getList(Key key) {
        return map.get(key);
    }

    public Map<Key, Collection<Item>> getMap() {
        return map;
    }

    public MapCollection<Key, Item> remove(Key key) {
        map.remove(key);
        return this;
    }

    public Collection<Item> get(Key key) {
        Collection<Item> list = map.get(key);

        if (list == null)
            try {
                list = itemClass.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        return list;
    }

    @Override
    public Iterator<Entry<Key, Collection<Item>>> iterator() {
        return map.entrySet().iterator();
    }

    public void addAll(MapCollection<Key, Item> second) {
        for (Entry<Key, Collection<Item>> en : second)
            for (Item it : en.getValue())
                add(en.getKey(), it);
    }

    public Pairs<Key, Item> getAll() {
        Pairs<Key, Item> pairs = new Pairs<>();

        for (Entry<Key, Collection<Item>> en : this)
            for (Item it : en.getValue())
                pairs.add(en.getKey(), it);

        return pairs;
    }

}
