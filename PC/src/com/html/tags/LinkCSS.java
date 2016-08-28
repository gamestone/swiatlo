package com.html.tags;

import com.html.core.Element;
import com.html.core.Interfaces.INonContentTag;
import com.html.core.Node;

public class LinkCSS extends Element<LinkCSS> implements INonContentTag {

    public String type;
    public String rel;
    public String href;

    public LinkCSS(Node parent, String cssFile) {
        super("link", parent);
        href = cssFile;
        rel = "stylesheet";
        type = "text/css";
    }
}
