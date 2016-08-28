package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Tag extends Element<Tag> {

    public Tag(String name, Node parent) {
        super(name, parent);
    }

    public Tag(String name, Node parent, int index) {
        super(name, parent, index);
    }

}
