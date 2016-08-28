package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class A extends Element<A> {

    public A href(String href) {
        return attr("href", href);
    }

    public A(Node parent) {
        super("a", parent);
    }
}
