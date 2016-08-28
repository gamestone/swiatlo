/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.html.core;

import com.*;
import com.html.tags.*;

/**
 *
 * @author milosz
 */
public abstract class Element<TTag extends Node> extends Node {

    @SuppressWarnings("unchecked")
    protected final TTag self = (TTag) this;

    public Element(String name, Node parent) {
        super(name, parent);
    }

    public Element(String name, Node parent, int index) {
        super(name, parent, index);
    }

    // wszystkie atrybuty powinny być ustawiane tutaj
    public TTag attr(String name, Object value) {
        super.attr(name, value);
        return self;
    }

    public TTag text(Object innerText) {
        Text = Utils.toString(innerText, true);
        return self;
    }

    public TTag innerHtml(String html) {
        InnerHtml = html;
        return self;
    }

    public TTag editable(boolean editable) {
        return attr("contenteditable", Boolean.toString(editable));
    }

    public TTag cls(String className) {

        String cls = attributes.get("class");
        if (cls != null)
            cls += " ";
        else
            cls = "";

        cls += className;
        return attr("class", cls);
    }

    public TTag onClick(String onClick) {
        return attr("onclick", onClick);
    }

    public TTag onDoubleClick(String onClick) {
        return attr("ondblclick", onClick);
    }

    public TTag onChange(String onChange) {
        return attr("onchange", onChange);
    }

    public TTag onKeyPress(String onkeypress) {
        return attr("onkeypress", onkeypress);
    }

    public TTag onKeyDown(String onkeydown) {
        return attr("onkeydown", onkeydown);
    }

    public TTag id(String id) {
        return attr("id", id);
    }

    public TTag name(String name) {
        return attr("name", name);
    }

    public TTag value(Object value) {
        return attr("value", value);
    }

    public TTag title(String title) {
        return attr("title", title);
    }

    public TTag disabled(boolean disabled) {
        return attr("disabled", disabled ? "disabled" : null);
    }

    public TTag setText(Object o) {
        Text = o != null ? o.toString() : null;
        return self;
    }

    public TTag setDownloadUrl(String mimeType, String fileName, String href) {
        return attr("data-downloadurl", mimeType + ":" + fileName + ":" + href);
    }
}
