package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Tr extends Element<Tr> {

    public final Table table;

    public Tr(Tbody parent) {
        super("tr", parent);
        table = parent.parentTable;
    }

    public Tr(Thead parent) {
        super("tr", parent);
        table = parent.parentTable;
    }

    public Tr(Tfoot parent) {
        super("tr", parent);
        table = parent.parentTable;
    }

    public Th th() {
        return new Th(this);
    }

    public Td td() {
        return new Td(this);
    }

    public Td td(String id) {
        Td td = new Td(this);
        td.id(id);
        return td;
    }

    public Tr setCells(Object... cells) {
        if (cells == null)
            return this;
        if (parentNode() != null && parentNode().getClass() == Thead.class)
            for (Object s : cells)
                th().text(s);
        else
            for (Object s : cells)
                td().text(s);
        return this;
    }

    public int getCellCount() {
        int cnt = 0;
        for (Node tag : tags())
            if (tag.getName() != null
                    && (tag.getName().equals("td") || tag.getName().equals("th")))
                ++cnt;
        return cnt;
    }

    public Td getCell(int cellIndex) {
        int cnt = -1;
        for (Node tag : tags())
            if (tag instanceof Td) {
                ++cnt;
                if (cnt >= cellIndex)
                    return (Td) tag;
            }
        return null;
    }
}
