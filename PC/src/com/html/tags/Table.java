package com.html.tags;

import com.html.core.*;
import java.util.*;

public class Table extends Element<Table> {

    public String rules; //all, groups, rows, cols
    public final Thead thead;
    public final Tbody tbody;
    public Tfoot tfoot;

    public Tbody addBody() {
        return (Tbody) addTag(new Tbody(this));
    }

    public Tfoot tfoot() {
        if (tfoot == null)
            tfoot = new Tfoot(this);
        return tfoot;
    }

    public Table(Node parent) {
        super("table", parent);
        this.thead = new Thead(this);
        this.tbody = new Tbody(this);
    }

    public Table(Node parent, String id, String name) {
        super("table", parent);
        this.thead = new Thead(this);
        this.tbody = new Tbody(this);
        id(id);
        name(name);
    }

    public Table(Node parent, String id) {
        super("table", parent);
        this.thead = new Thead(this);
        this.tbody = new Tbody(this);
        id(id);
    }

    public Tr tbodyTr() {
        return tbody.tr();
    }

    public Tr theadTr() {
        return thead.tr();
    }

    public Col col() {
        return new Col(this);
    }

    public void sortRows(final int cellIndex, final Comparator<Td> comparator) {
        sortRows(new Comparator<Tr>() {
            @Override
            public int compare(Tr o1, Tr o2) {
                Td td1 = o1.getCell(cellIndex);
                Td td2 = o2.getCell(cellIndex);
                return comparator.compare(td1, td2);
            }
        });
    }

    public void sortRows(final Comparator<Tr> comparator) {
        List<Tr> list = new LinkedList<>();
        for (Node nd : tbody.tags())
            if (nd instanceof Tr)
                list.add((Tr) nd);

        Collections.sort(list, comparator);

        for (Tr tr : list)
            tbody.addTag(tr);
    }

    public Set<Tr> getRows() {
        Set<Tr> rows = new LinkedHashSet<>();
        for (Node nd : tbody.tags())
            if (nd instanceof Tr)
                rows.add((Tr) nd);
        return rows;
    }

}
