package com.html.tags;

import com.html.core.Element;

public class Thead extends Element<Thead> {

    public Table parentTable;

    public Thead(Table parent) {
        super("thead", parent);
        parentTable = parent;
    }

    public Tr tr() {
        return new Tr(this);
    }
}
