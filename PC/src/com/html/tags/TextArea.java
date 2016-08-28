package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class TextArea extends Element<TextArea> {

    public String rows;
    public String cols;

    public TextArea autocomplete(boolean state) {
        attr("autocomplete", state ? "on" : "off");
        return this;
    }

    public TextArea required(boolean required) {
        return attr("required", required ? "true" : null);
    }

    public TextArea(Node parent) {
        super("textarea", parent);
    }
}
