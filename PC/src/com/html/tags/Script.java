package com.html.tags;

import com.html.core.Element;
import com.html.core.Node;

public class Script extends Element<Script> {

    public String type;
    public String src;
    String script_id;

    public Script(Node parent) {
        super("script", parent);
        type = "text/javascript";
        text("");
    }

    public Script(Node parent, String jsFile) {
        super("script", parent);
        src = jsFile;
        type = "text/javascript";
        text("");
    }
}
