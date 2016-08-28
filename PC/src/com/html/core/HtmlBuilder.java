package com.html.core;

import com.*;
import com.html.core.Interfaces.ITagListenner;
import com.html.tags.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import mlogger.Log;

/**

 @author Miłosz Ziernik
 */
public final class HtmlBuilder extends Element<HtmlBuilder> {

    public final Head head;
    public final Body body;
    public Node meta;
    public boolean addSpecesInSingleLine = true;
    private Writer writer;
    public boolean returnAsXHTML = false;
    //public boolean noChacheHeader = true;
    public boolean compactMode = false;
    //----------------------------------------
    private String lastWrited = "";
    private String currentLine = "";
    final static String instanceUid = Utils.randomId(4);

    @SuppressWarnings("unchecked")
    public HtmlBuilder() {
        super("html", null);
        head = new Head(this);
        body = new Body(this);
        compactMode = false;
        returnAsXHTML = false;

        if (returnAsXHTML) {
            attr("xmlns", "http://www.w3.org/1999/xhtml");
            attr("xml:lang", "pl");
            attr("lang", "pl");
        }

        head.meta().addEquiv("Content-Type", "text/html; charset=UTF-8");

    }

//=========================================================================
    private void write(String str) throws IOException {
        String s = str;
        if (str != null && str.startsWith("\n") && lastWrited.endsWith("\n"))
            s = s.substring(1, s.length());
        lastWrited = s;
        writer.append(s);
    }

    private boolean processTag(Node tag, String space) throws IOException {

        boolean singleLine = tag.builder.singleLine;
        boolean shortTags = tag.builder.shortTag != null ? tag.builder.shortTag : false;

        if (tag instanceof TagNC || tag instanceof TagNC)
            shortTags = true;

        boolean enterAdded = false;

        for (ITagListenner ll : tag.builder.listenners)
            if (!ll.onBeforeBuildTag(tag))
                return false;

        if (tag.Parent != null) {
            singleLine = tag.Parent.builder.singleLine;
            if (tag.Parent.Parent != null && tag.Parent.Parent.builder.singleLineChild)
                singleLine = true;
        }

        if (singleLine)
            tag.builder.singleLine = true;

        if (tag instanceof HeadStyle)
            tag.InnerHtml = ((HeadStyle) tag).write(compactMode ? "" : space + "  ");

        if (tag instanceof Node)
            for (Field field : tag.getClass().getFields()) {
                int mod = field.getModifiers();

                if (Modifier.isAbstract(mod)
                        || Modifier.isStatic(mod)
                        || Modifier.isPrivate(mod))
                    continue;

                if (field.getType() != String.class
                        && field.getType() != StyleBuilder.class)
                    continue;

                String fName = field.getName().replace("_", "");

                if (fName.equals("Name") || fName.equals("Text")
                        || fName.equals("InnerHtml") || fName.equals("Comment"))
                    continue;

                String value = null;

                if (fName.equals("style")) {
                    value = tag.style.write(compactMode ? "" : space);

                    if (value == null)
                        continue;
                }

                if (value == null)
                    try {
                        value = (String) field.get(tag);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                    }

                if (value == null)
                    continue;

                if (!tag.attributes.containsKey(fName))
                    tag.attributes.put(fName, value);
            }

        boolean isEmpty = tag.tags().isEmpty()
                && (tag.Text == null) && (tag.InnerHtml == null);

        if (tag.builder.skipTag)
            return false;

        if (!compactMode && !singleLine) {
            write("\n" + space);
            currentLine = space;
            enterAdded = true;
        }

        if (!compactMode && singleLine && !lastWrited.equals(" ")
                && addSpecesInSingleLine)
            write(" ");
        if (compactMode && tag.Text != null && !lastWrited.endsWith(" "))
            write(" ");
        write("<" + tag.Name.replace(" ", ""));

        for (Entry<String, String> attr : tag.attributes.entrySet()) {
            String name = attr.getKey();
            String value = attr.getValue();

            if (name != null && !name.equals("")
                    && value != null)
                write(" " + name.replace(" ", "") + "=\""
                        + escape(value, false, tag.builder.breakWords) + "\"");
        }

        if (isEmpty && shortTags) {
            write("/>");
            if (compactMode)
                write(" ");
        } else {
            write(">");
            if (tag.Text != null && !tag.Text.equals(""))
                write(escape(tag.Text, true, tag.builder.breakWords));

            if (tag instanceof Script && tag.InnerHtml != null) {
                String s = tag.InnerHtml;
                if (!compactMode && !s.startsWith("\n"))
                    s = "\n" + s;
                if (!compactMode && !s.endsWith("\n"))
                    s += "\n";
                if (compactMode)
                    s = s.trim();
                write(s.replaceAll("\n", "\n" + space + "  "));
            } else
                if (tag.InnerHtml != null)
                    write(tag.InnerHtml);
        }

        if (tag.Comment != null && !tag.Comment.equals("")) {
            write("\n" + space + "  <!-- " + tag.Comment + " -->");
            currentLine = space + "  <!-- " + tag.Comment + " -->";
        }

        for (Node tt : tag.tags())
            if (tt != null && !tt.Name.equals(""))
                processTag(tt, space + "  ");

        if (!compactMode && tag.tags().size() > 0 && !singleLine && currentLine.isEmpty())
            write(space);

        if (!compactMode && tag instanceof HeadStyle)
            write("\n" + space);

        if (!isEmpty || !shortTags) {
            write("</" + tag.Name + ">");
            if (compactMode)
                write(" ");
        }

        if (!compactMode && enterAdded && !singleLine) {
            write("\n");
            currentLine = "";
        }

        if (!compactMode && singleLine && !lastWrited.equals(" ") && addSpecesInSingleLine)
            write(" ");
        return true;

    }

    /**
     Escapuje stringa do postaci HTML-owej zamieniajac entery na BR-ki
     */
    public static String escapeBR(String s) {
        return escape(s, true, false).replace("\r\n", "<br/>").replace("\n", "<br/>");
    }

    public static String escape(String s, boolean innerText, boolean breakWords) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c < 32 && c != 10 && c != 13) {
                sb.append("&#x");
                sb.append(Integer.toString(c, 16));
                sb.append(";");
                continue;
            }

            switch (c) {
                default:
                    sb.append(c);
                    break;
                case '<':
                    sb.append(innerText ? "&lt;" : c);
                    break;
                case '>':
                    sb.append(innerText ? "&gt;" : c);
                    break;

                case '&':
                    sb.append("&amp;");
                    break;

                case '"':
                    sb.append(innerText ? c : "&quot;");
                    break;

                /*  case '\'':
                 sb.append(innerText ? c : "&apos;");
                 break;
                 */
            }

            if (breakWords && ((c >= '!' && c <= '/')
                    || (c >= ':' && c <= '@')
                    || (c >= '[' && c <= '`')
                    || (c >= '{' && c <= '~')))
                sb.append(Node.shy);
        }

        return sb.toString();
    }

    public void saveToFile(File file) throws IOException {
        OutputStreamWriter out
                = new OutputStreamWriter(
                        new BufferedOutputStream(
                                new FileOutputStream(file), 102400),
                        "UTF-8");
        try {
            returnHTML(out, this);
        } finally {
            out.flush();
            out.close();
        }
    }

    public void returnHTML(OutputStream out) throws IOException {
        try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
            returnHTML(writer, this);
        }
    }

    public void returnHTML(Writer writer, Node tag) throws IOException {
        this.writer = writer;
        if (returnAsXHTML && tag == this) {
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" "
                    + "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        }

        if (!returnAsXHTML && tag == this)
            writer.append("<!doctype html>");

        processTag(tag, "");
    }

    public void returnHTML(Writer writer, Node tag, boolean closeStream) throws IOException {
        if (writer == null)
            return;
        this.writer = writer;
        processTag(tag, "");
        if (closeStream)
            writer.close();
    }

    @Override
    public String toString() {
        return toString(this);
    }

    public String toString(Node tag) {
        StringWriter stringWriter = new StringWriter();
        try {
            returnHTML(stringWriter, tag);
        } catch (IOException ex) {
            Log.warning(ex);
        }
        return stringWriter.toString();
    }

    public String TagToString(Node tag) throws IOException {
        writer = new StringWriter();
        processTag(tag, "");
        return writer.toString();
    }

    @Override
    public void clear() {
        body.clear();
        head.clear();
    }

    public void setTitle(String title) {
        head.setTag("title").text(title);

    }
    //==========================================================================

    public static class ScriptBuilder implements ITagListenner {

        private final Node tag;
        public final List<String> lines = new LinkedList<>();

        public ScriptBuilder(Node tag) {
            this.tag = tag;
            tag.addListenner(this);
        }

        public void add(int level, String code) {
            String s = "";
            for (int i = 0; i < level; i++)
                s += "  ";
            lines.add(s + code);
        }

        @Override
        public boolean onBeforeBuildTag(Node node) {
            if (!lines.isEmpty())
                tag.script(Utils.listToString(lines, "\n"));
            return true;
        }

    }
}
