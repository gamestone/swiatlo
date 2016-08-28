package com.html.tags;

import com.html.core.Element;

public class Tfoot extends Element<Tfoot> {

    public Table parentTable;

    public Tfoot(Table parent) {
        super("tfoot", parent);
        parentTable = parent;
    }

    public Tr tr() {
        return new Tr(this);
    }
}
