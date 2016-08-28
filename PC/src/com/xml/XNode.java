package com.xml;

import mlogger.Log;
import com.utils.*;
import com.utils.VParser.*;
import com.utils.VParser.EmptyValueException;
import com.utils.VParser.NullValueException;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class XNode extends XElement implements Iterable<XNode> {

    public Boolean shortTag = null;
    public boolean multiLineAttributes = false;

    public XNode(Node node) {
        super(node);
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(this);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            return ex.toString();
        }
        return writer.toString();
    }

    public String getName() {
        return getNodeName();
    }

    public XML xml() {
        Node nd = this;
        while (nd != null && nd.getParentNode() != null
                && nd.getParentNode().getNodeType() != Element.DOCUMENT_NODE)
            nd = nd.getParentNode();

        if (nd instanceof XML)
            return (XML) nd;

        if (nd != null)
            return (XML) XElement.get(nd);

        return null;
    }

    public Strings path() {
        Strings list = new Strings();
        list.setSeparator("/");
        Node nd = this;
        while (nd != null && nd.getNodeType() != Element.DOCUMENT_NODE) {
            list.insert(nd.getNodeName());
            nd = nd.getParentNode();
        }
        return list;
    }

    private List<XNode> nodes(final boolean regEx, final boolean canCreate,
            final String... names) {
        final List<XNode> nodes = new LinkedList<>();
        if (names == null || names.length == 0)
            return nodes;

        final XML xml = xml();

        new Object() {
            void match(XNode elm, int level) {
                if (level >= names.length) {
                    nodes.add(elm);
                    return;
                }
                for (XNode nd : elm.nodes())
                    if (xml.compare(names[level], nd.name(), regEx))
                        match(nd, level + 1);
            }
        }.match(this, 0);

        if (canCreate && nodes.isEmpty())
            new Object() {
                void match(XNode elm, int level) {
                    if (level >= names.length) {
                        nodes.add(elm);
                        return;
                    }
                    for (XNode nd : elm.nodes())
                        if (xml.compare(names[level], nd.name(), regEx)) {
                            match(nd, level + 1);
                            return;
                        }
                    match(new XNode(elm.appendChild(
                            elm.getOwnerDocument().createElement(
                                    names[level]))), level + 1);
                }
            }.match(this, 0);

        return nodes;
    }

    public final List<XNode> nodes(final String... names) {
        return nodes(false, false, names);
    }

    public final List<XNode> nodesRegEx(final String... names) {
        return nodes(true, false, names);
    }

    public boolean has(String... names) {
        return !nodes(false, false, names).isEmpty();
    }

    public XNode node(String... names) {
        List<XNode> nodes = nodes(false, false, names);
        if (!nodes.isEmpty())
            return nodes.get(0);
        return null;
    }

    public XNode nodeF(String... names) throws XmlException {
        List<XNode> nodes = nodes(false, false, names);
        if (!nodes.isEmpty())
            return nodes.get(0);

        throw new XmlException("Nie znaleziono gałęzi "
                + path().addAll(names));
    }

    public XNode nodeC(String... names) {
        return nodes(false, true, names).get(0);
    }

    public XNode nodeAdd(String... names) {
        if (names == null || names.length == 0)
            return this;

        Strings list = new Strings(names);
        String last = list.last(true);

        XNode xnode = this;
        if (!list.isEmpty())
            xnode = nodeC(list.getArray());

        return new XNode(xnode.appendChild(xnode.getOwnerDocument().createElement(last)));
    }

    public XNode nodeRegEx(String... names) throws XmlException {
        List<XNode> nodes = nodes(true, false, names);
        if (!nodes.isEmpty())
            return nodes.get(0);

        throw new XmlException("Nie znaleziono gałęzi "
                + path().addAll(names).toString("/"));
    }

    public final Map<String, String> attributes() {
        Map<String, String> map = new LinkedHashMap<>();
        NamedNodeMap attrs = getAttributes();
        for (int i = 0; i < attrs.getLength(); i++)
            map.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
        return map;
    }

    public final List<Node> getAllSourceElements() {
        List<Node> nodes = new LinkedList<>();
        NodeList childNodes = getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
            nodes.add(childNodes.item(i));
        return nodes;
    }

    protected List<Node> getSourceNodes() {
        List<Node> nodes = new LinkedList<>();
        for (Node nd : getAllSourceElements())
            if (nd.getNodeType() == Element.ELEMENT_NODE)
                nodes.add(nd);
        return nodes;
    }

    public List<XNode> nodes() {
        LinkedList<XNode> list = new LinkedList<>();
        for (Node el : getSourceNodes())
            list.add((XNode) XElement.get(el));
        return list;
    }

    public XNode attr(String name, Object value) {
        asElement().setAttribute(name, value != null ? value.toString() : null);
        return this;
    }

    /**
     Bieżąca gałąź jako Element
     @return 
     */
    public Element asElement() {
        return (Element) node;
    }

    public List<Node> getNodes(int... types) {
        List<Node> list = new LinkedList<>();
        NodeList nodes = getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (types == null || types.length == 0) {
                list.add(nodes.item(i));
                continue;
            }
            boolean ok = false;
            short type = nodes.item(i).getNodeType();
            for (int j = 0; j < types.length; j++) {
                ok |= types[j] == type;
                if (ok)
                    list.add(nodes.item(i));
            }
        }
        return list;
    }

    public XNode removeAttr(String name) {
        asElement().removeAttribute(name);
        return this;
    }

    public boolean hasAttr(String name) {
        return name == null ? false : asElement().hasAttribute(name);
    }

    public XNode addValue(Object value) {
        appendChild(getOwnerDocument()
                .createTextNode(value != null ? value.toString() : null));
        return this;
    }

    // <editor-fold desc="================================ Atrybuty =================================" defaultstate="collapsed">
    private VParser getAttrib(String name) {
        VParser parser = new VParser(
                asElement().hasAttribute(name)
                ? asElement().getAttribute(name) : null,
                path().add(name).toString("/"));

        parser.eEmptyValue = "Wartość atrybutu %s nie może być pusta";
        parser.eNullValue = "Nie znaleziono atrybutu %s";

        return parser;
    }

    public String attrStr(String name) {
        VParser val = getAttrib(name);
        if (val.value == null)
            throw new NullValueException(val.valueName, val.eNullValue);
        return val.value;
    }

    public String attrStr(String name, String def) {
        return asElement().hasAttribute(name) ? asElement().getAttribute(name) : def;
    }

    public boolean attrBool(String name) {
        return getAttrib(name).getBool();
    }

    public Boolean attrBool(String name, Boolean def) {
        return getAttrib(name).getBool(def);
    }

    public short attrShort(String name) {
        return getAttrib(name).getShort();
    }

    public Short attrShort(String name, Short def) {
        return getAttrib(name).getShort(def);
    }

    public int attrInt(String name) {
        return getAttrib(name).getInt();
    }

    public Integer attrInt(String name, Integer def) {
        return getAttrib(name).getInt(def);
    }

    public long attrLong(String name) {
        return getAttrib(name).getLong();
    }

    public Long attrLong(String name, Long def) {
        return getAttrib(name).getLong(def);
    }

    public float attrFloat(String name) {
        return getAttrib(name).getFloat();
    }

    public Float attrFloat(String name, Float def) {
        return getAttrib(name).getFloat(def);
    }

    public Double attrDouble(String name) {
        return getAttrib(name).getDouble();
    }

    public Double attrDouble(String name, Double def) {
        return getAttrib(name).getDouble(def);
    }

    public TDate attrDate(String name) {
        return getAttrib(name).getDate();
    }

    public TDate attrDate(String name, TDate def) {
        return getAttrib(name).getDate(def);
    }

    // </editor-fold>
    // <editor-fold desc="================================ Wartości =================================" defaultstate="collapsed">
    public Strings getValues() {
        Strings values = new Strings();
        for (Node nd : getAllSourceElements())
            if (nd.getNodeType() == Element.TEXT_NODE)
                values.add(nd.getNodeValue());
        return values;
    }

    private VParser getVal() {
        Strings text = getValues();
        VParser parser = new VParser(text.isEmpty() ? null : text.toString(" "),
                path().toString("/"));

        parser.eEmptyValue = "Wartość %s nie może być pusta";
        parser.eNullValue = "Nie znaleziono wartości %s";

        return parser;
    }

    public String getStr() {
        VParser val = getVal();
        if (val.value == null)
            throw new EmptyValueException(val.valueName, val.eEmptyValue);
        return val.value;
    }

    public String getStr(String def) {
        return getTextContent() != null ? getTextContent() : def;
    }

    public Boolean getBool(Boolean def) {
        return getVal().getBool(def);
    }

    public boolean getBool() {
        return getVal().getBool();
    }

    public short getShort() {
        return getVal().getShort();
    }

    public Short getShort(Short def) {
        return getVal().getShort(def);
    }

    public int getInt() {
        return getVal().getInt();
    }

    public Integer getInt(Integer def) {
        return getVal().getInt(def);
    }

    public long getLong() {
        return getVal().getLong();
    }

    public Long getLong(Long def) {
        return getVal().getLong(def);
    }

    public float getFloat() {
        return getVal().getFloat();
    }

    public Float getFloat(Float def) {
        return getVal().getFloat(def);
    }

    public Double getDouble() {
        return getVal().getDouble();
    }

    public Double getDouble(Double def) {
        return getVal().getDouble(def);
    }

    public TDate getDate() {
        return getVal().getDate();
    }

    public TDate getDate(TDate def) {
        return getVal().getDate(def);
    }

    public String getText() {
        return getValues().toString();
    }

    public XNode setText(Object value) {
        List<Node> nodes = getNodes(Element.TEXT_NODE);
        for (Node nd : nodes)
            removeChild(nd);
        if (value == null)
            return this;
        appendChild(getOwnerDocument().createTextNode(value.toString()));
        return this;
    }
    // </editor-fold>

    /**
     Dodaje komnetarz przed bieżącą gałęzią
     @param name
     @return 
     */
    public XNode comment(String name) {
        getParentNode().insertBefore(getOwnerDocument()
                .createComment(name), this);
        return this;
    }

    /**
     Dodaje komentarz jako podrzędną gałąź
     @param name
     @param first
     @return 
     */
    public XNode comment(String name, boolean first) {
        Comment comm = getOwnerDocument().createComment(name);
        if (!first || getChildNodes().getLength() == 0)
            appendChild(comm);
        else
            insertBefore(comm, getFirstChild());

        return this;
    }

    public XNode cdata(String name) {
        appendChild(getOwnerDocument().createCDATASection(name));
        return this;
    }

    public static interface EnumNodesVisitor {

        public boolean onVisitNode(XNode node);
    }

    /**
     Wyszukuje galezie z uwzglednieniem wyrazen regularnych
     @param names
     @return 
     */
    /*
     public List<XmlNode> find(String... regExNames) {

     }

     public List<XmlNode> findByAttribute(String regExNames, String regExValues) {

     }
     */
    public void visit(final EnumNodesVisitor visitor) {
        new Object() {
            void visit(XNode node) {
                for (XNode nd : node.nodes())
                    if (!visitor.onVisitNode(nd))
                        return;
                    else
                        visit(nd);
            }
        }.visit(this);
    }

    /*
     @Override
     public String toString() {
     StringWriter sw = new StringWriter();
     try {
     new XmlWriter(sw, getXml()).enumWrite(node, "");
     } catch (Exception e) {
     Log.warning(e);
     throw new RuntimeException(e);
     }
     return sw.toString();
     }
     */
    @Override
    public Iterator<XNode> iterator() {
        return nodes().iterator();
    }

}
