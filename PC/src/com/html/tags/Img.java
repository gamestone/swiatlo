package com.html.tags;

import com.html.core.Element;
import com.html.core.Interfaces.INonContentTag;
import com.html.core.Node;

public class Img extends Element<Img> implements INonContentTag {

    public Img alt(String alt) {
        return attr("alt", alt);
    }

    public Img src(String src) {
        return attr("src", src);
    }

    public Img(Node parent, String src) {
        super("img", parent);
        attr("src", src);
    }
}
