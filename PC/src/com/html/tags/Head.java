package com.html.tags;

import com.html.core.StyleBuilder;
import com.html.core.*;
import com.html.core.Interfaces.INonContentTag;

public class Head extends Element<Head> {

    public static class Meta extends Element<Meta> implements INonContentTag {

        public Meta(Head parent) {
            super("meta", parent);
        }

        public void addContent(String name, String content) {
            attr("name", name);
            attr("content", content);
        }

        public void addEquiv(String httpEquiv, String content) {
            attr("http-equiv", httpEquiv);
            attr("content", content);
        }
    }

    public HeadStyle headStyle = null;

    public Head(Node parent) {
        super("head", parent);
    }

    public Meta meta() {
        return new Meta(this);
    }

    public StyleBuilder styles(String selector) {
        if (headStyle == null)
            headStyle = new HeadStyle(this);

        StyleBuilder sb = new StyleBuilder(builder.html, this);
        headStyle.hstyles.put(selector, sb);
        return sb;
    }

    public Node addMeta(String httpEquiv, String content) {

        Node meta = tag("meta");
        meta.attr("http-equiv", httpEquiv);
        meta.attr("content", content);
        meta.builder.shortTag = true;
        return meta;
    }

    public Script linkJavaScript(String srcFile) {
        String s = srcFile;
        if (s == null)
            s = "";
        s = s.toLowerCase();
        return linkJavaScript(srcFile, !s.startsWith("http://")
                && !s.startsWith("https://"));
    }

    public Script linkJavaScript(String srcFile, boolean relativePath) {

   //     if (relativePath)
        //         srcFile = MainServlet.getRelativeURL(RequestWrapper.getInstance(), srcFile);
        Script scr = null;
        for (Node tag : tags()) {
            if (tag instanceof Script) {
                Script ss = (Script) tag;

                if (ss.src != null && ss.src.equalsIgnoreCase(srcFile)) {
                    scr = ss;
                    break;
                }
            }
        }

        return scr != null ? scr : new Script(this, srcFile);
    }

    public LinkCSS linkCSS(String srcFile) {
        return linkCSS(srcFile, true);
    }

    public LinkCSS linkCSS(String srcFile, boolean relativePath) {

      //  if (relativePath)
        //       srcFile = MainServlet.getRelativeURL(RequestWrapper.getInstance(), srcFile);
        LinkCSS css = null;
        for (Node tag : tags()) {
            if (tag instanceof LinkCSS) {
                LinkCSS ss = (LinkCSS) tag;

                if (ss.href != null && ss.href.equalsIgnoreCase(srcFile)) {
                    css = ss;
                    break;
                }
            }
        }
        return css != null ? css : new LinkCSS(this, srcFile);
    }

    public void setTitle(String title) {
        setTag("title").text(title);
    }

    public void setIcon(String href) {
        Node ico = null;

        //   href = MainServlet.getRelativeURL(RequestWrapper.getInstance(), href);
        for (int i = 0; i < tags().size(); i++) {
            Node tag = tags().get(i);
            if (tag.getName().equalsIgnoreCase("link")) {
                String rel = tag.attributes.get("rel");
                if (rel != null && rel.equalsIgnoreCase("shortcut icon")) {
                    ico = tag;
                    break;
                }
            }

            if (ico == null)
                ico = new Tag("link", this);

            ico.builder.shortTag = true;
            ico.attr("rel", "shortcut icon");
            ico.attr("href", href);
        }
    }
}
