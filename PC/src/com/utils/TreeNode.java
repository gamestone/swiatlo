package com.utils;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;

/**
 Klasa reprezentująca strukturę obiektów w formie drzewa. W momencie
 modyfikacji wartości wyliczany jest hasz bieżącej gałęzi oraz wszystkich
 nadrzędnych
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {

    public final TreeNode<T> parent;
    public final String key;
    public final LinkedList<TreeNode<T>> items = new LinkedList<>();
    private T value;

    public TreeNode(final T value) {
        this.value = value;
        this.key = null;
        parent = null;
    }

    public T getValue() {
        return value;
    }

    public TreeNode<T> setValue(T value) {
        if (value == null && this.value == null
                || (value != null && value.equals(this.value))
                || (this.value != null && this.value.equals(value)))
            return this;
        this.value = value;
        return this;
    }

    private TreeNode(final TreeNode<T> parent, final String key, final T value) {
        this.key = key;
        this.value = value;
        this.parent = parent;
        if (parent != null)
            parent.items.add(this);
    }

    public TreeNode<T> add(final String key, final T value) {
        if (key != null)
            for (TreeNode<T> tn : items)
                if (key.equals(tn.key))
                    return tn;
        return new TreeNode<>(this, key, value);
    }

    public TreeNode<T> add(final T value) {
        return new TreeNode<>(this, null, value);
    }

    public void remove() {
        if (parent != null)
            parent.items.remove(this);
    }

    public LinkedList<TreeNode<T>> getNodes(final boolean includeSubNodes) {
        final LinkedList<TreeNode<T>> lst = new LinkedList<>();

        new Object() {
            void visit(TreeNode<T> node) {
                for (TreeNode<T> tn : node.items) {
                    lst.add(tn);
                    if (includeSubNodes)
                        visit(tn);
                }
            }
        }.visit(this);
        return lst;
    }

    public LinkedList<T> getValues(final boolean includeSubNodes) {
        final LinkedList<T> lst = new LinkedList<>();
        for (TreeNode<T> tn : getNodes(true))
            lst.add(tn.value);
        return lst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value != null ? value.toString() : "<null>");

        for (TreeNode<T> tn : getNodes(true)) {
            sb.append("\n");
            for (int i = 0; i < tn.level(); i++)
                sb.append("  ");
            sb.append(tn.value != null ? tn.value.toString() : "<null>");
        }
        return sb.toString();
    }

    public TreeNode<T> getNodeByValue(T value) {
        for (TreeNode<T> tn : items)
            if (tn.value != null && tn.value.equals(value))
                return tn;
        return null;
    }

    /**
     Pobiera hasz jako MD5 bieżącej gałęzi i wszystkich dzieci
     */
    public UUID getHash() {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
        new Object() {
            void visit(TreeNode<T> node) {
                md.update((byte) level());
                md.update(ByteBuffer.allocate(4).putInt(node.hashCode()));
                md.update((byte) 111);
                md.update(ByteBuffer.allocate(4).putInt(
                        node.value != null ? node.value.hashCode() : 0));
                md.update((byte) 222);
                for (TreeNode<T> tn : node)
                    visit(tn);
                md.update((byte) 255);
            }
        }.visit(this);

        return UUID.nameUUIDFromBytes(md.digest());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode) {
            String kk = ((TreeNode) obj).key;
            obj = ((TreeNode) obj).value;
            if (key != null && !key.equals(kk))
                return false;
        }

        return ((value == null && obj == null)
                || (value != null && value.equals(obj)));
    }

    public TreeNode<T> getByKey(final String key) {
        if (key != null)
            for (TreeNode<T> tn : items)
                if (key.equals(tn.key))
                    return tn;
        return null;
    }

    public List<Pair<TreeNode<T>, TreeNode<T>>> diff(final TreeNode<T> tree) {
        final List<Pair<TreeNode<T>, TreeNode<T>>> diff = new LinkedList<>();
        new Object() {
            void visit(TreeNode<T> base, TreeNode<T> comp) {
                /* 
                 if (!base.equals(comp)) {
                 diff.add(new Pair<>(base, comp));
                 }
                 */

                for (TreeNode<T> node : base.items) {
                    TreeNode<T> tn = comp.getByKey(node.key);

                    // zmodyfikowany
                    if (tn != null && !node.equals(tn)) {
                        diff.add(new Pair<>(node, tn));
                        visit(node, tn);
                        continue;
                    }

                    boolean found = false;
                    for (TreeNode<T> tnC : comp.items)
                        if (node.equals(tnC)) {
                            found = true;
                            visit(node, tnC);
                            break;
                        }
                    if (!found)
                        diff.add(new Pair<>(node, (TreeNode<T>) null));
                }

                for (TreeNode<T> tnC : comp.items) {
                    if (base.getByKey(tnC.key) != null)
                        continue;

                    boolean found = false;
                    for (TreeNode<T> tnB : base.items)
                        if (tnB.equals(tnC)) {
                            found = true;
                            break;
                        }
                    if (!found)
                        diff.add(new Pair<>((TreeNode<T>) null, tnC));
                }

            }
        }.visit(this, tree);
        return diff;
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return items.iterator();
    }

    public int level() {
        TreeNode tn = this;
        int level = 0;
        while (tn != null && tn.parent != null) {
            tn = tn.parent;
            ++level;
        }
        return level;
    }
}
