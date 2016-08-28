package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class TObject extends Element<TObject> {

    public TObject(Node parent, String type) {
        super("object", parent);
        attr("type", type);
    }

    public TObject width(int width) {
        attr("width", width);
        return this;
    }

    public TObject height(int height) {
        attr("height", height);
        return this;
    }

    public TObject align(String align) {
        attr("align", align);
        return this;
    }

    public TObject alt(String alt) {
        attr("alt", alt);
        return this;
    }

    public TObject param(String name, String value) {
        tag("param")
                .attr("name", name)
                .attr("value", value).builder.shortTag = true;
        return this;
    }
}
