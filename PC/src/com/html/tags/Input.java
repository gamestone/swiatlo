package com.html.tags;

import com.html.core.Element;
import com.html.core.Interfaces.INonContentTag;
import com.html.core.Node;

public class Input extends Element<Input> implements INonContentTag {

    public String spellcheck;
    public String multiple;
    public String onkeydown;
    public String minlength;  // minimalna liczba znakow
    public String maxlength;  // maksymalna liczba znakow
    public String autocomplete;

    /*
     * button
     checkbox
     color
     date 
     datetime 
     datetime-local 
     email 
     file
     hidden
     image
     month 
     number 
     password
     radio
     range 
     reset
     search
     submit
     tel
     text
     time 
     url
     week
     */
    public Input(Node parent, InputType type) {
        super("input", parent);
        type(type);
        if (type == InputType.file && parent instanceof Form) {
            ((Form) parent).enctype("multipart/form-data");
            name("file");
        }

    }

    public DataList dataList() {
        return new DataList(this, this);
    }

    public Input autocomplete(boolean state) {
        attr("autocomplete", state ? "on" : "off");
        return this;
    }

    public Input required(boolean required) {
        return attr("required", required ? "true" : null);
    }

    public Input checked(boolean checked) {
        attr("checked", checked ? "checked" : null);
        return this;
    }

    public Input size(int size) {
        attr("size", size);
        return this;
    }

    public Input readOnly(boolean readOnly) {
        return attr("readonly", readOnly ? "readonly" : null);
    }

    public Input type(InputType type) {
        attr("type", type != null ? type.name() : null);
        return this;
    }
}
