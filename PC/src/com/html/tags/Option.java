package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Option extends Element<Option> {

    public Option(Node parent, String text, String value) {
        super("option", parent);
        text(text);
        value(value);
    }

    public Option selected(boolean selected) {
        attr("selected", selected ? "selected" : null);
        return this;
    }
}
