package com.html.tags;

import com.html.core.Element;

public class Th extends Element<Th> {

    public Table parentTable;
    public final Tr tr;

    public Th colspan(int colspan) {
        attr("colspan", colspan);
        return this;
    }

    public Th rowspan(int rowspan) {
        attr("rowspan", rowspan);
        return this;
    }

    public Th(Tr parent) {
        super("th", parent);
        parentTable = parent.table;
        this.tr = parent;
    }
}
