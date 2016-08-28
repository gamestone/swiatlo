package com.html.tags;

import com.html.core.*;
import java.util.*;

public class Tbody extends Element<Tbody> {

    public Table parentTable;

    public Tbody(Table parent) {
        super("tbody", parent);
        parentTable = parent;
    }

    public Tr tr() {
        return new Tr(this);
    }

    /**
     Dodaje nowy wiersz i wpisuje zawartosci komórek
     */
    public Tr addRowCells(Object... items) {
        Tr tr = tr();

        if (items != null)
            for (Object o : items)
                tr.td().setText(o);
        return tr;
    }

    public int getRowsCount() {
        int cnt = 0;
        for (Node nd : tagList)
            if (nd instanceof Tr)
                ++cnt;
        return cnt;

    }

    public void sortRows(final Comparator<Tr> comparator) {
        Collections.sort(tagList, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (!(o1 instanceof Tr) || !(o2 instanceof Tr))
                    return 0;
                return comparator.compare((Tr) o1, (Tr) o2);
            }
        });
    }
}
