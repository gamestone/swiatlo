package com.html.core;

import com.html.*;
import com.html.core.HtmlBuilder.ScriptBuilder;
import com.html.core.Interfaces.ITagListenner;
import com.html.tags.*;
import com.html.tags.Tag;

import com.utils.*;
import com.xml.*;
import java.io.*;
import java.util.*;

public abstract class Node {

    public class Builder {

        public final HtmlBuilder html;
        public boolean breakWords = false;
        public boolean skipTag = false;
        public boolean singleLine = false;
        public Boolean shortTag = null;
        public boolean singleLineChild = false;
        public boolean serviceJsRequired = false;
        public final LinkedList<ITagListenner> listenners = new LinkedList<>();

        private Builder(HtmlBuilder html) {
            this.html = html;
        }
    }

    public final static String nbsp = Character.toString((char) 160);
    public final static String shy = Character.toString((char) 173); // SoftHypen - miekki enter
    //-----------------------------
    public final Map<String, String> attributes = new LinkedHashMap<>();
    String Name;
    String Text;

    String InnerHtml;
    String Comment;
    Node Parent;

    public final Map<Object, Object> extra = new HashMap<>();

    //   private final List<Attribute> attribList = new ArrayList<>();
    protected final LinkedList<Node> tagList = new LinkedList<>();

    public final Builder builder;
    //----------------------------------------------
    public final StyleBuilder style;

    @Override
    public String toString() {
        return Name;
    }

    public Node(String name, Node parent) {

        HtmlBuilder html;
        if (this instanceof HtmlBuilder)
            html = (HtmlBuilder) this;
        else {
            if (parent == null)
                throw new UnsupportedOperationException("Brak rodzica tagu " + name);
            html = parent.builder.html;
            parent.tagList.add(this);
        }

        builder = new Builder(html);
        this.Name = name;
        this.Parent = parent;
        style = new StyleBuilder(html, this);
    }

    @SuppressWarnings("unchecked")
    public Node(String name, Node parent, int index) {
        if (parent == null)
            throw new UnsupportedOperationException("Brak rodzica tagu " + name);
        this.Name = name;
        this.Parent = parent;
        builder = new Builder(parent.builder.html);
        parent.tagList.add(index, this);
        style = new StyleBuilder(builder.html, this);
    }

    public Node attr(String name, Object value) {
        if (name != null)
            if (value == null)
                attributes.remove(name);
            else
                attributes.put(name, value.toString());
        return this;
    }

    /**
     Zwraca serię twardych spacji - mogą służyć jako tabulatory
     */
    public static String nbsp(int count) {
        String tab = "";
        for (int i = 0; i < count; i++)
            tab += (char) 160;
        return tab;
    }

    public void addListenner(ITagListenner listenner) {
        builder.listenners.add(listenner);
    }

    public void sort(final Comparator<Node> comparator) {
        Collections.sort(tagList, comparator);
    }

    public boolean isEmpty() {
        boolean b = (Text != null && Text.isEmpty())
                || (InnerHtml != null && InnerHtml.isEmpty());
        return tagList.isEmpty() && !b;
    }

    public LinkedList<Node> tags() {
        LinkedList<Node> list = new LinkedList<>();
        list.addAll(tagList);
        return list;
    }

    public Node[] getTagsByName(String name) {
        if (name == null || name.equals(""))
            return null;

        ArrayList<Node> lst = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            Node tt = tagList.get(i);
            if (tt != null && tt.Name != null && tt.Name.equalsIgnoreCase(name))
                lst.add(tt);
        }
        Node[] result = new Node[lst.size()];
        lst.toArray(result);
        return result;
    }

    /**
     Dodaj galaz jako dziecko
     */
    @SuppressWarnings("unchecked")
    public Node addTag(Node tag) {
        if (tag.Parent != null)
            tag.Parent.tagList.remove(tag);
        tagList.add(tag);
        tag.Parent = this;
        return tag;
    }

    public Node addTag(int index, Node tag) {
        tagList.add(index, tag);
        if (tag.Parent != null)
            tag.Parent.tagList.remove(tag);
        tag.Parent = this;
        return tag;
    }

    public Tag tag(String name) {
        return new Tag(name, this);
    }

    public TagNC tagNC(String name) {
        return new TagNC(name, this);
    }

    public Tag tag(int index, String name) {
        return new Tag(name, this, index);
    }

    public Tag tag(int index, String name, String text) {
        Tag tt = new Tag(name, this, index);
        tt.text(text);
        return tt;
    }

    public String getName() {
        return Name;
    }

    public String getId() {
        String id = attributes.get("id");
        return id == null ? "" : id;
    }

    public String getText() {
        return Text == null ? "" : Text;
    }

    public String getInnerHtml() {
        return InnerHtml == null ? "" : InnerHtml;
    }

    public Node parentNode() {
        return Parent;
    }

    /**
     Jesli tag nie istnieje to utworz, w przeciwnym razie zwroc pierwszy
     napotkany
     */
    public Tag setTag(String name) {
        Tag result = null;
        Node[] tt = getTagsByName(name);

        if (tt.length > 0)
            result = (Tag) tt[0];
        else
            result = new Tag(name, this);
        return result;
    }

    public String getTagsPath() {
        String result = "";

        Node td = this;
        while (td != null) {
            if (!result.equals(""))
                result = "/" + result;

            result = td.Name.replace(" ", "") + result;
            td = td.Parent;
        }

        return result;
    }

    public void clear() {
        attributes.clear();
        tagList.clear();
    }

    /**
     Przenosi biezaca galaz na lub przed inna
     */
    private boolean moveTo(Node reference, boolean after) {
        if (reference == null || reference.Parent == null)
            return false;

        if (Parent != null)
            Parent.tagList.remove(this);

        int idx = reference.Parent.tagList.indexOf(reference);

        if (after)
            idx++;

        reference.Parent.tagList.add(idx, this);
        this.Parent = reference.Parent;
        return true;
    }

    public boolean moveAfter(Node tag) {
        return moveTo(tag, true);
    }

    public boolean moveBefore(Node tag) {
        return moveTo(tag, false);
    }

    //**************************************************************************
    public Script script(String body) {

        if (body != null && !body.isEmpty())
            for (Node nd : tagList)
                if (nd instanceof Script && body.equals(nd.InnerHtml))
                    return (Script) nd;

        Script script = new Script(this);
        script.innerHtml(body);
        return script;
    }

    public ScriptBuilder scriptBuilder() {
        return new ScriptBuilder(this);
    }

    public Script scriptFunction(String name, String params, String body) {
        if (body == null)
            return null;

        Script script = new Script(this);
        script.innerHtml("\nfunction " + name
                + "(" + (params != null ? params : "") + "){\n  "
                + body.replace("\n", "  \n") + "\n}");
        return script;
    }

    public Tag div() {
        return new Tag("div", this);
    }

    public Select select() {
        return new Select(this);
    }

    public Tag span() {
        return new Tag("span", this);
    }

    public Iframe iframe(String src) {
        return new Iframe(this, src);
    }

    public Applet applet(String name, String code) {
        return new Applet(this, name, code);
    }

    public TObject object(String type) {
        return new TObject(this, type);
    }

    public TagNC br() {
        TagNC br = new TagNC("br", this);
        return br;
    }

    public TagNC hr() {
        return new TagNC("hr", this);
    }

    public Tag h1() {
        return new Tag("h1", this);
    }

    public Tag h2() {
        return new Tag("h2", this);
    }

    public Tag h3() {
        return new Tag("h3", this);
    }

    public Tag h4() {
        return new Tag("h4", this);
    }

    public Tag h5() {
        return new Tag("h5", this);
    }

    public Tag left() {
        return new Tag("left", this);
    }

    public Tag center() {
        return new Tag("center", this);
    }

    public Tag button() {
        return new Tag("button", this);
    }

    public Tag button(Object text) {
        return new Tag("button", this).text(text);
    }

    public Input input(InputType type) {
        return new Input(this, type);
    }

    public Input inputHidden() {
        return input(InputType.hidden);
    }

    public Input inputButton() {
        return input(InputType.button);
    }

    public Input inputCheckBox(/*String id, String name*/) {
        return input(InputType.checkbox);
    }

    public Input inputRadio(/*String id, String name, String value*/) {
        return input(InputType.radio);
    }

    public Input inputEdit(/*String id, String name*/) {
        return input(InputType.text);
    }

    public Input inputNumber(/*String id, String name*/) {
        return input(InputType.number);
    }

    public Tag right() {
        return new Tag("right", this);
    }

    public Tag b() {
        return new Tag("b", this);
    }

    public Tag code() {
        return new Tag("code", this);
    }

    public Tag p() {
        return new Tag("p", this);
    }

    public Tag i() {
        return new Tag("i", this);
    }

    public Tag u() {
        return new Tag("u", this);
    }

    public Input input() {
        return new Input(this, InputType.hidden);
    }

    public Tag label() {
        return new Tag("label", this);
    }

    public Tag label(Object text) {
        return new Tag("label", this).text(text);
    }

    public Fieldset fieldset(String title) {
        return new Fieldset(this, title);
    }

    public TextArea textArea() {
        return new TextArea(this);
    }

    public Form form() {
        return new Form(this);
    }

    public Table table() {
        return new Table(this);
    }

    public A a() {
        return new A(this);
    }

    public Img img(String src) {
        return new Img(this, src);
    }

    public Ul ul() {
        return new Ul(this);
    }

    public Canvas canvas() {
        return new Canvas(this);
    }
    //**************************************************************************    

    public LinkedList<Tag> textToDivs(String text) {
        return textToTags(text, "div", null, false);
    }

    public LinkedList<Tag> textToSpans(String text) {
        return textToTags(text, "span", null, true);
    }

    public List<Tag> textToListItems(String text) {
        return textToTags(text, "li", null, false);
    }

    /**
     Konwertuje tekst do postaci listy tagów z uwzględnieniem enterów i wcięć
     */
    public LinkedList<Tag> textToTags(String text, String tagName, Integer spaceMargin, boolean inclBr) {

        // jesli spaceMargin == null, spacje zamienione beda na twarde
        LinkedList<Tag> lst = new LinkedList<>();
        if (text == null)
            return lst;

        String[] ss = text.split("\\n");
        int lineNr = 0;
        for (String st : ss) {

            if (st.trim().isEmpty()) {
                lst.add(tagNC("br"));
                continue;
            }

            Tag tag = new Tag(tagName, this);
            lst.add(tag);

            int cnt = 0;
            for (int i = 0; i < st.length(); i++) {

                boolean stop = false;

                switch (st.charAt(i)) {
                    case ' ':
                        cnt += 1;
                        break;
                    case '\t':
                        ++cnt;
                        cnt += 2;
                        break;
                    default:
                        stop = true;
                }
                if (stop)
                    break;

            }

            String space = "";

            if (cnt > 0 && spaceMargin != null && spaceMargin > 0) {
                cnt *= spaceMargin;
                tag.style.paddingLeft(cnt + "px");
            }
            if (cnt > 0 && spaceMargin == null)
                for (int i = 0; i < cnt; i++)
                    space += Char.nbsp;

            tag.text(space + st.trim());

            ++lineNr;

            if (inclBr && lineNr < ss.length)
                lst.add(tagNC("br"));

        }

        return lst;
    }

    public void remove() {
        Parent.tagList.remove(this);
    }

    public Iframe ifame(String src) {
        return new Iframe(this, src);
    }

    public CheckedLabel checkedLabel(boolean radio, String caption) {
        return new CheckedLabel(this, radio, caption);
    }

}
