package com.utils;

/**
 * Miłosz Ziernik
 * 2013/11/09 
 */
public class Triple<First, Second, Third> {

    public First first;
    public Second second;
    public Third third;

    public Triple(First first, Second second, Third third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public int hashCode() {
        return 31 * hashcode(first) + hashcode(second) + hashcode(third);
    }

    private static int hashcode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Triple))
            return false;
        if (this == obj)
            return true;
        return equal(first, ((Triple) obj).first)
                && equal(second, ((Triple) obj).second)
                && equal(third, ((Triple) obj).third);
    }

    private boolean equal(Object o1, Object o2) {
        return o1 == null ? o2 == null : (o1 == o2 || o1.equals(o2));
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ')';
    }
}
