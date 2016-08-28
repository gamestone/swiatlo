package com.utils;

import com.Utils;
import java.text.Collator;
import java.util.*;

/**
 *
 * @author milosz
 * @param <First>
 * @param <Second>
 */
public class Pairs<First, Second> implements Iterable<Pair<First, Second>> {

    private final List<Pair<First, Second>> list = new LinkedList<>();

    public List<Pair<First, Second>> asList() {
        LinkedList<Pair<First, Second>> result = new LinkedList<>();
        result.addAll(list);
        return result;
    }

    public List<First> firstList() {
        List<First> lst = new LinkedList<>();
        for (Pair<First, Second> pair : list)
            lst.add(pair.first);
        return lst;
    }

    public List<Second> secondList() {
        List<Second> lst = new LinkedList<>();
        for (Pair<First, Second> pair : list)
            lst.add(pair.second);
        return lst;
    }

    public void sort(Comparator<Pair<First, Second>> comparator) {
        Collections.sort(list, comparator);
    }

    public void sortFirst() {
        sort(new Comparator<Pair<First, Second>>() {

            @Override
            public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {
                if (o1 == null || o2 == null)
                    return 0;
                return Utils.collator.compare(o1.first, o2.first);
            }
        });
    }

    public void sortFirst(final Comparator<First> comparator) {
        sort(new Comparator<Pair<First, Second>>() {

            @Override
            public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {

                return comparator.compare(o1 != null ? o1.first : null,
                        o2.first != null ? o2.first : null);
            }
        });
    }

    public void sortSecond() {
        sort(new Comparator<Pair<First, Second>>() {

            @Override
            public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {
                if (o1 == null || o2 == null)
                    return 0;
                return Utils.collator.compare(o1.second, o2.second);
            }
        });
    }

    public void sortSecond(final Comparator<Second> comparator) {
        sort(new Comparator<Pair<First, Second>>() {

            @Override
            public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {
                return comparator.compare(o1 != null ? o1.second : null,
                        o2.second != null ? o2.second : null);
            }
        });
    }

    public Pair add(First first, Second second) {
        Pair<First, Second> pair = new Pair<>(first, second);
        list.add(pair);
        return pair;
    }

    public boolean remove(Pair<First, Second> pair) {
        return list.remove(pair);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    @Override
    public Iterator<Pair<First, Second>> iterator() {
        return list.iterator();
    }

}
