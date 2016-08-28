package com.html.tags;

import com.html.core.*;

public class Fieldset extends Element<Fieldset> {

    public Tag legend;

    public Fieldset(Node parent, String legend) {
        super("fieldset", parent);
        this.legend = new Tag("legend", this);
        this.legend.text(legend);
    }
}
