package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Ol extends Element<Ol> {
//lista numerowana

    public String type;

    public Ol(Node parent) {
        super("ol", parent);
    }

    public Tag li() {
        return new Tag("li", this);
    }
}
