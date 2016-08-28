package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Style extends Element<Style> {

    public String rel;
    public String type;
    public String href;

    public Style(Node parent) {
        super("style", parent);
        rel = "stylesheet";
        type = "text/css";
        text("");
    }
}
