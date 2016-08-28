package com.html.tags;

import com.html.core.Element;

public class Td extends Element<Td> {

    public Table parentTable;
    public final Tr tr;

    public Td colspan(int colspan) {
        attr("colspan", colspan);
        return this;
    }

    public Td rowspan(int rowspan) {
        attr("rowspan", rowspan);
        return this;
    }

    public Td(Tr parent) {
        super("td", parent);
        parentTable = parent.table;
        this.tr = parent;
    }

}
