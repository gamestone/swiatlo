package com;

import com.XmlSerializer.INode;
import mlogger.Log;
import com.utils.VParser.EmptyValueException;
import com.utils.VParser.IncorrectValueException;
import com.utils.VParser.NullValueException;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**

 @author Miłosz Ziernik
 */
public final class Xml2 {

    public static abstract class XmlEnumNode {

        public abstract boolean enumNode(XmlNode node, int level, XmlNode root) throws XmlException;
    }

    public static class XmlEventListenner {

        public HashMap extra = new HashMap();

        public boolean beforeWriteNode(Writer writer, XmlNode nd, int lineNumber) {
            return false;
        }

        public boolean beforeWriteTextNode(Writer writer, XmlTextNode node, int lineNumber) {
            return false;
        }

        public boolean beforeWriteAttribute(Writer writer, XmlAttribute attribute, int lineNumber) {
            return false;
        }

        public void afterWriteNode(Writer writer, XmlNode nd, int lineNumber) {
        }

        public boolean onCustomEscapeWrite(Writer writer, XmlNode nd, String s, int lineNumber) {
            // tu mozna podstawic wlasne escapowanie
            return false;
        }
    }

    public static class XmlException extends Exception {

        public XmlException(String message) {
            super(message);
        }

        public XmlException(Exception e) {
            super("Błąd parsowania XML, " + Exceptions.exceptionToStr(e)
                    + (e instanceof SAXParseException
                       ? "; linia: " + ((SAXParseException) e).getLineNumber()
                    + ", kulumna: " + ((SAXParseException) e).getColumnNumber()
                       : ""));
        }
    }
    public static boolean useShortTags_ = false;
    public static boolean trimInnerText_ = false;
    public static String spaceChar_ = "  ";
    public static String returnChar_ = "\n";
    public static String xmlHeader_ = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    //----------------------
    private Document document = null;
    public XmlNode root;
    private Writer writer;
    public String spaceChar = spaceChar_; //\t";
    public String returnChar = returnChar_; //"\r\n";
    public String pathSeparator = "/";
    public boolean caseSensitive = false;
    public List<String> ignoredDTD = new LinkedList<>();
    public boolean useShortTags = useShortTags_;
    public boolean changeReturnsToSpaces = false; // zamien entery na spacje (dla html-a)
    public boolean validateDTD = false;
    public boolean trimInnerText = trimInnerText_;
    public XmlEventListenner eventListenner = null;
    public String xmlHeader = xmlHeader_;
    public String customDTDvalidator = null; // plik DTD
    public EntityResolver entityResolver = null; // wymagany jesli aktwna jest flaga customDTDvalidator
    public boolean useNameSpaces = true; //wlacza obsluge przestrzeni nazw podczas wyszukiwania elementow 
    private final List<XmlCommentNode> rootComments = new LinkedList<>();

    @Override
    public String toString() {
        try {
            return getXml();
        } catch (IOException ex) {
            return ex.toString();
        }
    }

    public static class XmlBaseNode {

        public final HashMap<String, Object> extra = new HashMap<>();
        public String name;
        public XmlNode parent;
        public Xml2 xml;
        public Boolean shortTag = null; // null - auto, 1 - wymus krotki, 0 - wymus dlugi

        public int index() {
            if (parent == null)
                return -1;
            return parent.nodes.indexOf(this);
        }

        public boolean isTextNode() {
            return (this instanceof XmlTextNode);
        }

        public XmlBaseNode getNext() {
            if (parent == null)
                return null;
            int idx = parent.nodes.indexOf(this);
            if (idx == parent.nodes.size() - 1)
                return null;
            return parent.nodes.get(++idx);
        }

        public XmlBaseNode(XmlNode parent, String name) throws XmlException {
            this.name = name;
            this.parent = parent;
            if (parent != null) {
                xml = parent.xml;
                parent.nodes.add(this);
            }
        }

        public void delete() {
            if (parent == null)
                return;
            parent.nodes.remove(this);
        }
    }

    public static final class XmlTextNode extends XmlBaseNode {

        public String innerText;

        public XmlTextNode(XmlNode parent, String innerText) throws XmlException {
            super(parent, "#Text");
            this.innerText = innerText;
        }
    }

    public static final class XmlCdataNode extends XmlBaseNode {

        public String innerText;

        public XmlCdataNode(XmlNode parent, String innerText) throws XmlException {
            super(parent, "#Text");
            this.innerText = innerText;
        }
    }

    public static final class XmlCommentNode extends XmlBaseNode {

        public String innerText;

        public XmlCommentNode(XmlNode parent, String innerText) throws XmlException {
            super(parent, "#Text");
            this.innerText = innerText;
        }
    }

    public static final class XmlNode extends XmlBaseNode {

        private final List<XmlBaseNode> nodes = new LinkedList<>();
        public final List<XmlAttribute> attributes = new LinkedList<>();
        public boolean multiLineAttributes = false;
        public INode annotation;

        @Override
        public String toString() {
            try {
                return getXml();
            } catch (IOException ex) {
                return ex.toString();
            }
        }

        public List<XmlBaseNode> allNodes() {
            return nodes;
        }

        public HashMap<String, String> getDeclaredNamespaces() {
            HashMap<String, String> ns = new HashMap<>();
            for (XmlAttribute a : attributes)
                if (a.name != null && a.name.toLowerCase().startsWith("xmlns:"))
                    ns.put(a.value != null ? a.value.toString() : null,
                            a.name.substring(a.name.indexOf(":") + 1));
            return ns;
        }

        private boolean compare(String name, String mask, boolean useMask) {

            if (useMask && mask != null && name != null) {
                if (xml.caseSensitive) {
                    mask = mask.toLowerCase();
                    name = name.toLowerCase();
                }
                mask = mask.replaceAll("\\.", "\\\\.");
                mask = mask.replaceAll("\\*", ".*");
                mask = mask.replaceAll("\\?", ".");
                return name.matches(mask);
            }

            return xml.caseSensitive ? (mask != null && name != null && mask.equals(name))
                   : (mask != null && name != null && mask.equalsIgnoreCase(name));
        }

        public XmlNode(XmlNode parent, String name) throws XmlException {
            super(parent, name);
            checkName(name);
        }

        public String getNameSpace() {
            if (name.indexOf(":") > 0)
                return name.substring(0, name.indexOf(":"));
            return "";
        }

        public void clear() {
            nodes.clear();
            attributes.clear();
        }

        public void deleteAtribute(String name) throws XmlException {
            XmlAttribute attr = attribute(false, name);
            if (attr != null)
                attr.delete();
        }

        public String getPath(String... path) {
            String s = "";
            XmlNode nd = this;

            while (nd != null) {
                if (!s.isEmpty())
                    s = xml.pathSeparator + s;
                s = nd.name + s;
                nd = nd.parent;
            }

            if (path != null)
                for (String p : path) {
                    if (!s.isEmpty())
                        s += xml.pathSeparator;
                    s += p;
                }
            return s;
        }

        private void enumGetNodes(List<XmlNode> list, XmlNode node,
                String[] path, int level, boolean canCreate) throws XmlException {

            if (path == null || level >= path.length)
                return;

            boolean found = false;
            for (XmlBaseNode xn : node.nodes) {

                if (!(xn instanceof XmlNode))
                    continue;

                XmlNode nd = (XmlNode) xn;

                if (compare(xn.name, path[level], true)) {
                    if (level >= path.length - 1) {
                        list.add(nd);
                        found = true;
                        continue;
                    }
                    enumGetNodes(list, nd, path, level + 1, canCreate);
                }
            }

            // jesli nie znaleziono galezi na podstawie samej nazwy, sprobuj 
            // poszukac dodajac przestrzen nazw rodzica
            if (!found && xml.useNameSpaces && !node.getNameSpace().isEmpty()) {
                String ns = node.getNameSpace();

                for (XmlBaseNode xn : node.nodes) {

                    if (!(xn instanceof XmlNode))
                        continue;

                    XmlNode nd = (XmlNode) xn;

                    if (compare(xn.name, ns + ":" + path[level], true)) {
                        if (level >= path.length - 1) {
                            list.add(nd);
                            continue;
                        }
                        found = true;
                        enumGetNodes(list, nd, path, level + 1, canCreate);
                    }
                }
            }

            if (!found && canCreate) {
                XmlNode nd = new XmlNode(node, path[level]);
                if (level == path.length - 1)
                    list.add(nd);
                enumGetNodes(list, nd, path, level + 1, canCreate);
            }
        }

        public XmlNode addNode(String... path) throws XmlException {
            if (path == null || path.length == 0)
                return this;

            XmlNode nd = this;
            String[] pp = Arrays.copyOf(path, path.length - 1);
            if (pp.length > 0)
                nd = getNode(false, pp);
            return new XmlNode(nd, path[path.length - 1]);
        }

        public XmlNode node(String... path) throws XmlException {
            return getNode(true, path);
        }

        /**
         Zwraca gałąź o danej ścieżce, jeśli nie istnieje to tworzy
         */
        public XmlNode open(String... path) throws XmlException {
            return getNode(false, path);
        }

        /**
         Zwraca gałąź o danej ścieżce, jeśli nie istnieje zwraca null-a
         */
        public XmlNode nodeNull(String... path) throws XmlException {
            return getNode(null, path);
        }

        /**
         Otwiera gałąź, mustExists:
         true: jesli nie znaleziono, zwraca bląd
         false: jesli nie znalezino, to utworz
         null: jesli nie znaleziono, to zwroc nulla
         */
        public XmlNode getNode(Boolean mustExists, String... nodePath)
                throws XmlException {

            List<XmlNode> lNodes = nodes(mustExists, nodePath);

            if (!lNodes.isEmpty())
                return lNodes.get(0);

            if (lNodes.isEmpty() && mustExists == null)
                return null;

            throw new XmlException("Nie znaleziono gałęzi \"" + getPath(nodePath));

        }

        public List<XmlNode> nodes() throws XmlException {
            return nodes(false);
        }

        /**
         Tworzy listę gałęzi, mustExists:
         true: jesli nie znaleziono, zwraca bląd
         false: jesli nie znalezino, to utworz
         null: jesli nie znaleziono, to zwroc nulla
         */
        public List<XmlNode> nodes(Boolean mustExists, String... nodePath)
                throws XmlException {
            // jesli nie podano sciezki to zwróć bieżąca
            if (nodePath == null || nodePath.length == 0) {
                List<XmlNode> list = new LinkedList<>();
                for (XmlBaseNode bn : nodes)
                    if (bn instanceof XmlNode)
                        list.add((XmlNode) bn);
                return list;
            }

            List<XmlNode> list = new LinkedList<>();

            enumGetNodes(list, this, nodePath, 0, mustExists != null && !mustExists);
            if (list.isEmpty())
                if (mustExists != null && mustExists)
                    throw new XmlException("Nie znaleziono gałęzi \""
                            + getPath(nodePath) + "\"!");

            return list;
        }

        public String[] getInnerTexts() {
            List<String> result = new LinkedList<>();

            for (int i = 0; i < nodes.size(); i++)
                if (nodes.get(i) instanceof XmlTextNode)
                    result.add(((XmlTextNode) nodes.get(i)).innerText);

            String[] array = new String[result.size()];
            result.toArray(array);
            return array;
        }

        public void deleteTextNodes() {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                XmlBaseNode bn = nodes.get(i);
                if (bn instanceof XmlBaseNode)
                    bn.delete();
            }
        }

        public XmlTextNode addInnerText(String text) throws XmlException {
            if (text == null)
                return null;
            return new XmlTextNode(this, text);
        }

        public XmlCommentNode addComment(String text) throws XmlException {
            return new XmlCommentNode(this, text);
        }

        public XmlTextNode setInnerText(String text) throws XmlException {
            deleteTextNodes();
            return addInnerText(text);
        }

        public String getInnerTextF(String... nodePath) throws XmlException {
            return getNode(true, nodePath).getInnerText();
        }

        public String getInnerText(String... nodePath) {
            try {
                return getNode(true, nodePath).getInnerText();
            } catch (XmlException ex) {
                return null;
            }
        }

        public String getInnerText() {
            String[] ss = getInnerTexts();
            StringBuilder sb = new StringBuilder();
            for (String s : ss)
                sb.append(s);
            return sb.toString();
        }

        public List<XmlTextNode> getTextNodes() throws XmlException {
            List<XmlTextNode> result = new LinkedList<>();
            for (XmlBaseNode nd : nodes)
                if (nd instanceof XmlTextNode)
                    result.add((XmlTextNode) nd);
            return result;
        }

        /**
         Otwiera atrybut, mustExists:
         true: jesli nie znaleziono, zwraca bląd
         false: jesli nie znalezino, to utworz
         null: jesli nie znaleziono, to zwroc nulla
         */
        public XmlAttribute attribute(Boolean mustExists, String... path)
                throws XmlException {
            List<XmlAttribute> lAttribs = attributes(mustExists != null && mustExists, path);

            if (!lAttribs.isEmpty())
                return lAttribs.get(0);

            if (lAttribs.isEmpty() && mustExists == null)
                return null;

            if (lAttribs.isEmpty() && mustExists)
                throw new XmlException("Nie znaleziono atrybutu \""
                        + getPath(path) + "\"!");

            return new XmlAttribute(this, -1, name, null);
        }

        public List<XmlAttribute> attributes(boolean mustExists, String... path)
                throws XmlException {

            if (path == null || path.length == 0) {
                if (mustExists && attributes.isEmpty())
                    throw new XmlException("Nie znaleziono atrybutu!");
                return attributes;
            }

            String[] fNodes = null;
            String sAttr = path[path.length - 1];

            if (path.length > 1)
                fNodes = Arrays.copyOf(path, path.length - 1);

            List<XmlAttribute> lAttribs = new LinkedList<>();
            List<XmlNode> lNodes;
            if (fNodes == null || fNodes.length == 0) {
                lNodes = new LinkedList<>();
                lNodes.add(this);
            } else
                lNodes = nodes(true, fNodes);

            for (XmlNode nd : lNodes) {

                XmlAttribute attr = null;

                for (XmlAttribute xa : nd.attributes)
                    if (compare(xa.name, sAttr, true)) {
                        attr = xa;
                        break;
                    }

                if (attr == null && xml.useNameSpaces && !nd.getNameSpace().isEmpty()) {
                    String ns = nd.getNameSpace();
                    for (XmlAttribute xa : nd.attributes)
                        if (compare(xa.name, ns + ":" + sAttr, true)) {
                            attr = xa;
                            break;
                        }
                }

                if (attr != null)
                    lAttribs.add(attr);

            }

            if (lAttribs.isEmpty())
                if (mustExists)
                    throw new XmlException("Nie znaleziono atrybutu \""
                            + getPath(sAttr) + "\"!");
                else
                    return lAttribs;

            return lAttribs;
        }

        public XmlNode attr(String name, Object value) throws XmlException {
            if (value == null)
                return this;
            XmlAttribute attr = null;
            for (XmlAttribute a : attributes)
                if (compare(a.name, name, true)) {
                    attr = a;
                    break;
                }

            if (attr == null)
                attr = new XmlAttribute(this, -1, name, value.toString());

            attr.value = value.toString();

            return this;
        }
        private final StringParser spAttrib = new StringParser();

        {
            spAttrib.nullValue = "Nie znaleziono atrybutu %s";
        }

        private Object getAttr(String... path) {
            try {
                XmlAttribute aa = attribute(false, path);
                return aa != null && aa.value != null ? aa.value : null;
            } catch (Exception e) {
                return null;
            }
        }

        public String attrStr(String... path) {
            return spAttrib.getStr(getPath(path), getAttr(path), true);
        }

        public String attrStrDef(String def, String... path) {
            Object attr = getAttr(path);
            return attr != null ? attr.toString() : def;
        }

        public boolean attrBool(String... path) {
            return spAttrib.getBool(getPath(path), getAttr(path));
        }

        public Boolean attrBoolDef(Boolean def, String... path) {
            return spAttrib.getBool(getPath(path), getAttr(path), def);
        }

        public byte attrByte(String... path) {
            return spAttrib.getByte(getPath(path), getAttr(path));
        }

        public Byte attrByteDef(Byte def, String... path) {
            return spAttrib.getByte(getPath(path), getAttr(path), def);
        }

        public short attrShort(String... path) {
            return spAttrib.getShort(getPath(path), getAttr(path));
        }

        public Short attrShortDef(Short def, String... path) {
            return spAttrib.getShort(getPath(path), getAttr(path), def);
        }

        public int attrInt(String... path) {
            return spAttrib.getInt(getPath(path), getAttr(path));
        }

        public Integer attrIntDef(Integer def, String... path) {
            return spAttrib.getInt(getPath(path), getAttr(path), def);
        }

        public long attrLong(String... path) {
            return spAttrib.getLong(getPath(path), getAttr(path));
        }

        public Long attrLongDef(Long def, String... path) {
            return spAttrib.getLong(getPath(path), getAttr(path), def);
        }

        public float attrFloat(String... path) {
            return spAttrib.getFloat(getPath(path), getAttr(path));
        }

        public Float attrIntDef(Float def, String... path) {
            return spAttrib.getFloat(getPath(path), getAttr(path), def);
        }

        public double attrDouble(String... path) {
            return spAttrib.getDouble(getPath(path), getAttr(path));
        }

        public Double attrDoubleDef(Double def, String... path) {
            return spAttrib.getDouble(getPath(path), getAttr(path), def);
        }

        public Date attrDate(String... path) {
            return spAttrib.getDate(getPath(path), getAttr(path));
        }

        public Date attrDateDef(Date def, String... path) {
            return spAttrib.getDate(getPath(path), getAttr(path), def);
        }
        /*
         public XmlNode addNode(String... nodePath) throws XmlException {
         String[] nn = nodePath.split(xml.pathSeparator);

         XmlNode base = this;

         for (String s : nn) {
         if (!s.trim().isEmpty()) {
         base = new XmlNode(base, s);
         }
         }
         return base;
         }
         */

        public void move(XmlNode newParent, XmlBaseNode destination, boolean insertAfter) {
            if (newParent == null || destination == null || destination.parent == null)
                return;

            int idx = destination.parent.nodes.indexOf(destination);

            if (idx == -1 || idx == nodes.size() - 1)
                newParent.nodes.add(this);
            else
                newParent.nodes.add(insertAfter ? idx + 1 : idx, this);
            if (parent != null)
                parent.nodes.remove(this);
            this.parent = newParent;
        }

        public void move(XmlNode newParent) {
            if (newParent == null)
                return;
            newParent.nodes.add(this);
            if (parent != null)
                parent.nodes.remove(this);
            parent = newParent;
        }

        /**
         Znajdź gałąź, jeśli nie istnieje to utwórz
         */
        private void enumFind(List<XmlNode> lst, XmlNode node, String name) {

            for (int i = 0; i < node.nodes.size(); i++) {
                if (!(node.nodes.get(i) instanceof XmlNode))
                    continue;
                XmlNode nn = (XmlNode) node.nodes.get(i);
                if (compare(nn.name, name, true))
                    lst.add(nn);
                enumFind(lst, nn, name);
            }
        }

        /**
         Znajdz galaz na podtsawie nazyw przeszukujac drzewo
         */
        public List<XmlNode> findNodes(String name) {
            List<XmlNode> lst = new LinkedList<>();
            enumFind(lst, this, name);
            return lst;
        }

        public void enumNodes(XmlEnumNode proc) throws XmlException {
            internalEnumNode(this, 0, proc, this);
        }

        private boolean internalEnumNode(XmlNode nd, int level, XmlEnumNode proc, XmlNode nroot) throws XmlException {
            int i = 0;

            while (i < nd.nodes.size()) {
                int cnt = nd.nodes.size();
                XmlBaseNode bn = nd.nodes.get(i);

                if (bn instanceof XmlNode) {
                    if (!internalEnumNode((XmlNode) bn, level + 1, proc, nroot))
                        return false;

                    if (!proc.enumNode((XmlNode) bn, level, nroot))
                        return false;
                }
                if (nd.nodes.size() == cnt)
                    i++;
            }
            return true;
        }

        public String getXml() throws IOException {
            StringWriter sw = new StringWriter();
            xml.writer = sw;
            xml.lineNumber = 1;
            try {
                xml.enumWrite(this, " ");
            } finally {
                sw.flush();
                sw.close();
            }
            return sw.toString();
        }

        public void getXml(Writer writer, String space) throws IOException {
            xml.writer = writer;
            xml.lineNumber = 1;
            try {
                xml.enumWrite(this, space);
            } finally {
                writer.flush();
            }
        }
    }

    public static final class XmlAttribute {

        public Xml2 xml;
        public String name;
        public String value;
        public XmlNode parent;

        @Override
        public String toString() {
            return name + " = " + value;
        }

        public XmlAttribute(XmlNode parent, int index, String name, String value) throws XmlException {
            checkName(name);
            this.name = name;
            this.value = value;
            this.parent = parent;
            if (parent != null) {
                xml = parent.xml;
                if (index >= 0)
                    parent.attributes.add(index, this);
                else
                    parent.attributes.add(this);
            }
        }

        public String getPath() {
            return parent.getPath() + " / " + name;
        }

        public void delete() {
            if (parent == null)
                return;
            for (int i = 0; i < parent.attributes.size(); i++)
                if (parent.attributes.get(i) == this) {
                    parent.attributes.remove(i);
                    break;
                }
        }
    }

    public DocumentBuilder Builder()
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        dbf.setValidating(validateDTD);
        dbf.setNamespaceAware(false);
        if (!validateDTD) {
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://apache.org/xml/features/validation/schema", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);

            // dbf.setFeature("http://apache.org/xml/features/validation/identity-constraint-checking", false);
            if (entityResolver != null)
                db.setEntityResolver(entityResolver);
            else
                db.setEntityResolver(new EntityResolver() {
                    @Override
                    public InputSource resolveEntity(String publicId, String systemId)
                            throws SAXException, IOException {
                        ignoredDTD.add(publicId + " - " + systemId);
                        return new InputSource(new StringReader(""));
                    }
                });

        }
        return db;
    }

    private static void checkName(String s) throws XmlException {
        if (s == null || s.trim().isEmpty()
                || !(s.charAt(0) == '_'
                || (s.charAt(0) >= 'a' && s.charAt(0) <= 'z')
                || (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z')))
            throw new XmlException("Nieprawidłowa nazwa \"" + s + "\"");
    }

    public Xml2(String sxml) throws XmlException {
        load(sxml);
    }

    public void load(String sxml) throws XmlException {
        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(sxml));
            document = Builder().parse(is);
            parse();
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public Xml2(File file) throws XmlException {
        load(file);
    }

    public Xml2() {
        try {
            load("<xml/>");
        } catch (XmlException xe) {
            Log.error(xe);
        }
    }

    public void load(File file) throws XmlException {
        try {
            document = Builder().parse(file);
            parse();
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public Xml2(byte[] bxml) throws XmlException {
        load(bxml);
    }

    public void load(byte[] bxml) throws XmlException {
        try {
            document = Builder().parse(new ByteArrayInputStream(bxml));
            parse();
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public Xml2(InputStream is) throws XmlException {
        load(is);
    }

    public void load(InputStream is) throws XmlException {
        try {
            document = Builder().parse(is);
            parse();
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    private void enumNode(Node node, XmlNode xnode) throws XmlException {

        NamedNodeMap attribs = node.getAttributes();

        for (int i = 0; i < attribs.getLength(); i++) {
            Node nd = attribs.item(i);
            new XmlAttribute(xnode, -1, nd.getNodeName(), nd.getNodeValue());
        }

        NodeList nList = node.getChildNodes();

        for (int n = 0; n < nList.getLength(); n++) {
            Node nd = nList.item(n);

            switch (nd.getNodeType()) {
                case Element.ELEMENT_NODE:
                    enumNode(nd, new XmlNode(xnode, nd.getNodeName()));
                    break;

                case Element.TEXT_NODE:
                    if (!nd.getNodeValue().trim().isEmpty())
                        new XmlTextNode(xnode, nd.getNodeValue());
                    break;

                case Element.COMMENT_NODE:
                    new XmlCommentNode(xnode, nd.getNodeValue());
                    break;

                case Element.CDATA_SECTION_NODE:
                    new XmlCdataNode(xnode, nd.getNodeValue());
                    break;

            }
        }
    }

    private void parse() throws XmlException {
        Element eroot = document.getDocumentElement();

        NodeList nList = document.getChildNodes();

        for (int n = 0; n < nList.getLength(); n++) {
            Node nd = nList.item(n);
            if (nd.getNodeType() == Element.COMMENT_NODE)
                rootComments.add(new XmlCommentNode(null, nd.getNodeValue()));
        }

        root = new XmlNode(null, eroot.getNodeName());

        root.xml = this;
        enumNode(document.getDocumentElement(), root);
    }

    private void write(String s) throws IOException {
        writer.write(s);
    }

    private void write(char c) throws IOException {
        writer.write(c);
    }
    private int lineNumber = 1;

    public static void escape(Writer writer, String s) throws IOException {
        if (writer == null || s == null)
            return;

        int len = s.length();

        //  znaki zastrzeżone w XMLu 	[#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]        
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if ((c >= 0x0 && c <= 0x8)
                    || c == 0xB
                    || c == 0xC
                    || c == 0xE
                    || (c >= 0xF && c <= 0x1F)
                    || (c >= 0x7F && c <= 0x84)
                    || (c >= 0x86 && c <= 0x97))
                c = '.'; // znak zastępczy dla zastrzeżonych

            switch (c) {
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                case '&':
                    writer.write("&amp;");
                    break;
                case '"':
                    writer.write("&quot;");
                    break;
                case '\'':
                    writer.write("&apos;");
                    break;
                default:
                    writer.write(c);
                    break;
            }
        }
    }

    public static String escape(String s) {
        StringWriter writer = new StringWriter();
        try {
            escape(writer, s);
        } catch (IOException ex) {
        }
        return writer.toString();
    }

    private void writeEscape(String s, XmlNode nd) throws IOException {
        if (eventListenner != null
                && eventListenner.onCustomEscapeWrite(writer, nd, s, lineNumber))
            return;

        if (changeReturnsToSpaces)
            s = s.replace("\r\n", " ").replace("\n", " ");

        escape(writer, s);

    }

    private void writeBreakLine(String space) throws IOException {
        write(returnChar);
        ++lineNumber;
        write(space);
    }

    private void enumWrite(XmlNode node, final String space) throws IOException {
        //    boolean hasAttributes = !node.attributes.isEmpty();
        boolean hasElementNodes = false;
        boolean hasTextNodes = false;
        boolean shortTag = useShortTags;
        if (node.shortTag != null)
            shortTag = node.shortTag;

        if (eventListenner != null
                && eventListenner.beforeWriteNode(writer, node, lineNumber))
            return;

        for (XmlBaseNode bn : node.nodes) {
            if (bn instanceof XmlNode)
                hasElementNodes = true;
            if (bn instanceof XmlTextNode)
                hasTextNodes = true;
            if (hasElementNodes && hasTextNodes)
                break;
        }

        boolean hasNodes = hasElementNodes || hasTextNodes;

        if (node.parent != null)
            writeBreakLine(space);

        write("<" + node.name);

        for (int i = 0; i < node.attributes.size(); i++) {

            XmlAttribute xa = node.attributes.get(i);

            if (eventListenner != null
                    && eventListenner.beforeWriteAttribute(writer, xa, lineNumber))
                continue;

            if (node.multiLineAttributes) {
                writeBreakLine(space);
                write(spaceChar);
            }

            if (!node.multiLineAttributes)
                write(" ");

            write(xa.name + "=\"");
            if (xa.value != null)
                writeEscape(xa.value, node);
            write("\"");
        }

        if (!hasNodes && shortTag) {
            if (node.multiLineAttributes)
                writeBreakLine(space);
            write("/>");
        } else {
            write(">");
            if (node.multiLineAttributes && hasTextNodes) {
                writeBreakLine(space);
                write(spaceChar);
            }
        }

        for (int i = 0; i < node.nodes.size(); i++) {
            XmlBaseNode bn = node.nodes.get(i);

            if (bn instanceof XmlNode)
                enumWrite((XmlNode) bn, space + spaceChar);

            if (bn instanceof XmlCommentNode) {
                XmlCommentNode xtn = (XmlCommentNode) bn;
                writeBreakLine(space);
                write(spaceChar);
                write("<!--");

                // komentarze bez escapowania
                if (trimInnerText)
                    write(xtn.innerText.trim());
                else
                    write(xtn.innerText);
                write("-->");
            }

            if (bn instanceof XmlTextNode) {
                XmlTextNode xtn = (XmlTextNode) bn;
                if (xtn.innerText == null)
                    continue;
                if (eventListenner != null
                        && eventListenner.beforeWriteTextNode(writer, xtn, lineNumber))
                    continue;

                if (trimInnerText)
                    writeEscape(xtn.innerText.trim(), node);
                else
                    writeEscape(xtn.innerText, node);
            }
        }

        if (hasNodes)
            if (hasElementNodes) {
                write(returnChar);
                ++lineNumber;
                write(space);
            }

        if (hasNodes || !shortTag)
            write("</" + node.name + ">");

        if (eventListenner != null)
            eventListenner.afterWriteNode(writer, node, lineNumber);

    }

    public String getXml() throws IOException {
        StringWriter sw = new StringWriter();
        getXml(root, sw, "");
        return sw.toString();
    }

    public void getXml(XmlNode node, Writer writer, String space) throws IOException {
        this.writer = writer;
        lineNumber = 1;
        if (xmlHeader != null && !xmlHeader.isEmpty()) {
            writer.write(xmlHeader);
            writer.write(returnChar);
            ++lineNumber;
        }

        // ------------- zapisz komentarze w glownej galzezi
        if (node == root)
            for (XmlCommentNode nd : rootComments) {
                write("<!--");
                if (trimInnerText)
                    writeEscape(nd.innerText.trim(), node);
                else
                    writeEscape(nd.innerText, node);
                write("-->");
                writeBreakLine(space);
            }

        try {
            enumWrite(node, space);
        } finally {
            writer.flush();
            writer.close();
        }
    }

    public void save(File file) throws IOException {
        getXml(root, new BufferedWriter(new FileWriter(file), 102400), " ");
    }

    public static class StringParser {

        public String nullValue = "Nie znaleziono wartości \"%s\"";
        public String emptyValue = "Wartość \"%s\" nie może być pusta";
        public String incorrectValue = "Nieprawidłowa wartość \"%s\"";

        public String getStr(String valueName, Object value, boolean canBeEmpty) {
            if (value == null)
                throw new NullValueException(valueName, nullValue);

            if (value.toString().trim().isEmpty())
                throw new EmptyValueException(valueName, nullValue);

            return value.toString();
        }

        public Boolean getBool(String valueName, Object value, Boolean def) {
            String val = getStr(valueName, value, true);
            try {
                Boolean res = Utils.strBool(val, def);
                if (res == null)
                    return def;
                return res;
            } catch (Exception e) {
                return def;
            }
        }

        public boolean getBool(String valueName, Object value) {
            String val = getStr(valueName, value, false);

            try {
                Boolean res = Utils.strBool(val, null);
                if (res == null)
                    throw new IncorrectValueException(valueName, incorrectValue, null);
                return res;
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Byte getByte(String valueName, Object value, Byte def) {
            try {
                return Byte.parseByte(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public byte getByte(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Byte.parseByte(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Short getShort(String valueName, Object value, Short def) {
            try {
                return Short.parseShort(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public short getShort(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Short.parseShort(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Integer getInt(String valueName, Object value, Integer def) {
            try {
                return Integer.parseInt(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public int getInt(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Long getLong(String valueName, Object value, Long def) {
            try {
                return Long.parseLong(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public long getLong(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Float getFloat(String valueName, Object value, Float def) {
            try {
                return Float.parseFloat(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public float getFloat(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Float.parseFloat(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Double getDouble(String valueName, Object value, Double def) {
            try {
                return Double.parseDouble(getStr(valueName, value, false));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public double getDouble(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }

        public Date getDate(String valueName, Object value, Date def) {
            try {
                return new SimpleDateFormat().parse(
                        getStr(valueName, value, false));
            } catch (ParseException e) {
                return def;
            }
        }

        public Date getDate(String valueName, Object value) {
            String val = getStr(valueName, value, false);
            try {
                return new SimpleDateFormat().parse(val);
            } catch (ParseException e) {
                throw new IncorrectValueException(valueName, incorrectValue, null);
            }
        }
    }
}
