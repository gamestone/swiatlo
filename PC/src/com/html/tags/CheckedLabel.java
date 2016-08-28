package com.html.tags;

import com.html.core.*;

public class CheckedLabel extends Element<CheckedLabel> {

    public final Input input;
    public final Tag label;

    public CheckedLabel(Node parent, boolean radio, String caption) {
        super("label", parent);
        style.display("block");
        input = input(radio ? InputType.radio : InputType.checkbox);
        label = span();
        label.text(caption);
    }
}
