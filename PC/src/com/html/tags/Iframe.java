package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Iframe extends Element<Iframe> {

    public String src;
    public String frameborder;

    public Iframe(Node parent) {
        super("iframe", parent);
    }

    public Iframe(Node parent, String src) {
        super("iframe", parent);
        this.src = src;
    }
}
