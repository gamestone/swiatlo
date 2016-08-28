package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;
import java.util.List;

public class Ul extends Element<Ul> {

    public String type;

    public Ul(Node parent) {
        super("ul", parent);
    }

    public Tag li() {
        return new Tag("li", this);
    }

    public Tag li(String text) {
        return new Tag("li", this).text(text);
    }

    public void setItems(String... items) {
        if (items == null)
            return;
        for (String s : items)
            li().text(s);
    }

    public Ul addIems(List<? extends Object> list) {
        if (list == null)
            return this;

        for (Object o : list)
            if (o != null)
                li().text(o);
        return this;
    }
}
