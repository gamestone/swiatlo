package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Canvas extends Element<Canvas> {

    public String width;
    public String height;

    public Canvas(Node parent) {
        super("canvas", parent);
    }
}
