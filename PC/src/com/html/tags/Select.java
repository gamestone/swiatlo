package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;
import java.util.Collection;

public class Select extends Element<Select> {

    public void setSelectedOption(String value) {
        for (Node tag : tags())
            if (tag instanceof Option)
                if (value.equals(tag.attributes.get("value")))
                    ((Option) tag).selected(true);

    }

    public class OptGroup extends Element<OptGroup> {

        public OptGroup(Select parent, String label) {
            super("optgroup ", parent);
            attr("label", label);
        }

        public Option option(String text, String value) {
            return new Option(this, text, value);
        }
    }

    public Select addElements(Collection<String> elements, String selected) {
        for (String s : elements)
            option(s, s).selected(s != null && s.equals(selected));
        return this;
    }

    public Select(Node parent) {
        super("select", parent);
    }

    public Select size(int size) {
        return attr("size", size);
    }

    public Select multiple(boolean multiple) {
        return attr("multiple", multiple ? "multiple" : null);
    }

    public Option option(String text, String value) {
        return new Option(this, text, value);
    }

    public OptGroup group(String name) {
        return new OptGroup(this, name);
    }
}
