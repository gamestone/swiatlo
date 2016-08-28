package com.html.tags;

import com.html.core.Node;

public class Applet extends TObject {

    public Applet(Node parent, String name, String code) {
        // "application/x-java-applet;version=1.7"
        super(parent, "application/x-java-applet");
        code(code);
        name(name);
        param("mayscript", "true");
    }

    public Applet code(String code) {
        param("code", code);
        return this;
    }

    public Applet codebase(String codebase) {
        param("codebase", codebase);
        return this;
    }

    public Applet archive(String archive) {
        param("archive", archive);
        return this;
    }
}
