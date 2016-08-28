package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Form extends Element<Form> {

    public Form(Node parent) {
        super("form", parent);
        attr("method", "POST");
    }

    public Form target(String target) {
        attr("target", target);
        attr("method", "POST");
        return this;
    }

    public Form method(String method) {
        attr("method", method);
        return this;
    }

    public Form action(String action) {
        attr("action", action);
        return this;
    }

    public Form enctype(String enctype) {
        attr("enctype", enctype);
        //"multipart/form-data"
        return this;
    }

    public Form onSubmit(String onSubmit) {
        attr("onsubmit", onSubmit);
        return this;
    }

    public Input inputSubmit() {
        return input(InputType.submit);
    }

}
