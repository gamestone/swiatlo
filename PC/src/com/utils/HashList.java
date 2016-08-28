package com.utils;

import java.util.*;

/**
 * Miłosz Ziernik
 * 2013/12/09 

 Szybka synchroniczna kolekcja
 */
public final class HashList<T> extends LinkedHashSet<T> implements Collection<T> {

    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public void insert(T element) {
        synchronized (lock) {
            HashList<T> lst = (HashList<T>) clone();
            clear();
            add(element);
            addAll(lst);
        }
    }

    @Override
    public boolean add(T e) {
        synchronized (lock) {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        synchronized (lock) {
            return super.addAll(c);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (lock) {
            return super.remove(o);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            super.clear();
        }
    }

    @Override
    public Iterator<T> iterator() {
        synchronized (lock) {
            return super.iterator();
        }
    }

    @Override
    public Object clone() {
        synchronized (lock) {
            return super.clone();
        }
    }

    @SuppressWarnings("unchecked")
    public final HashList<T> getCopy() {
        synchronized (lock) {
            return (HashList<T>) clone();
        }
    }

    public final void reverse() {
        synchronized (lock) {
            LinkedList<T> list = new LinkedList<>(this);
            Iterator<T> itr = list.descendingIterator();
            clear();
            while (itr.hasNext())
                add(itr.next());
        }
    }

    @SuppressWarnings("unchecked")
    public final void insertAll(Collection<T> elements) {
        synchronized (lock) {
            HashList<T> lst = getCopy();
            super.clear();
            super.addAll(elements);
            super.addAll(lst);
        }
    }

    public final T first() {
        if (!iterator().hasNext())
            return null;
        synchronized (lock) {
            return iterator().next();
        }
    }

    public final T last() {
        synchronized (lock) {
            T last = null;
            for (T t : this)
                last = t;
            return last;
        }
    }

    public void sort(Comparator<? super T> c) {
        LinkedList<T> lst = new LinkedList<>();

        synchronized (lock) {
            lst.addAll(this);
        }
        Collections.sort(lst, c);
        synchronized (lock) {
            this.clear();
            this.addAll(lst);
        }
    }

}
