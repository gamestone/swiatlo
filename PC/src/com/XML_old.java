package com;

import mlogger.Log;
import java.io.*;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 *
 * @author Miłosz Ziernik
 */
@Deprecated
public final class XML_old {

    public static abstract class XmlEnumNode {

        public abstract boolean enumNode(XmlNode node, int level, XmlNode root) throws XmlException;
    }

    public static class XmlEventListenner {

        public HashMap extra = new HashMap();

        public boolean beforeWriteNode(Writer writer, XmlNode nd, int lineNumber) {
            return false;
        }

        public boolean beforeWriteTextNode(Writer writer, XML_old.XmlTextNode node, int lineNumber) {
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
    public XML_old.XmlEventListenner eventListenner = null;
    public String xmlHeader = xmlHeader_;
    public String customDTDvalidator = null; // plik DTD
    public EntityResolver entityResolver = null; // wymagany jesli aktwna jest flaga customDTDvalidator
    public boolean useNameSpaces = true; //wlacza obsluge przestrzeni nazw podczas wyszukiwania elementow 
    private final List<XmlCommentNode> rootComments = new LinkedList<>();

    public static class XmlBaseNode {

        public HashMap<String, Object> extra = null;
        public String name;
        public XmlNode parent;
        public XML_old xml;
        public Boolean shortTag = null; // null - auto, 1 - wymus krotki, 0 - wymus dlugi

        public int index() {
            if (parent == null)
                return -1;
            return parent.nodes.indexOf(this);
        }

        public boolean isTextNode() {
            return (this instanceof XML_old.XmlTextNode);
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

        public List<XmlBaseNode> nodes = new LinkedList<>();
        public List<XmlAttribute> attributes = new LinkedList<>();
        public boolean multiLineAttributes = false;
        private List<XmlNode> findList;

        @Override
        public String toString() {
            try {
                return getXml();
            } catch (IOException ex) {
                return ex.toString();
            }
        }

        public HashMap<String, String> getDeclaredNamespaces() {
            HashMap<String, String> ns = new HashMap<>();
            for (XmlAttribute a : attributes)
                if (a.name != null && a.name.toLowerCase().startsWith("xmlns:"))
                    ns.put(a.value, a.name.substring(a.name.indexOf(":") + 1));
            return ns;
        }

        private boolean compare(String s1, String s2) {
            return xml.caseSensitive ? (s1 != null && s2 != null && s1.equals(s2))
                    : (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
        }

        public XmlNode(XmlNode parent, String name) throws XmlException {
            super(parent, name);
            XML_old.checkName(name);
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
            XmlAttribute attr = getAttribute(name, false);
            if (attr != null)
                attr.delete();
        }

        public String getPath() {
            String s = "";
            XmlNode nd = this;

            while (nd != null) {
                if (!s.isEmpty())
                    s = xml.pathSeparator + s;
                s = nd.name + s;
                nd = nd.parent;
            }
            return s;
        }

        private void enumNodePath(List<XmlNode> list, XmlNode node, String[] path, int level) {

            boolean found = false;
            for (XmlBaseNode xn : node.nodes) {

                if (!(xn instanceof XmlNode))
                    continue;

                XmlNode nd = (XmlNode) xn;

                if (compare(xn.name, path[level])) {
                    if (level >= path.length - 1) {
                        list.add(nd);
                        found = true;
                        continue;
                    }
                    enumNodePath(list, nd, path, level + 1);
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

                    if (compare(xn.name, ns + ":" + path[level])) {
                        if (level >= path.length - 1) {
                            list.add(nd);
                            found = true;
                            continue;
                        }
                        enumNodePath(list, nd, path, level + 1);
                    }
                }
            }
        }

        public XmlNode node(String nodePath, boolean raiseIfNotExists)
                throws XmlException {
            XmlNode[] nn = getNodes(nodePath, raiseIfNotExists);

            if (nn.length > 0)
                return nn[0];
            else {
                if (raiseIfNotExists)
                    throw new XmlException("Nie znaleziono gałęzi \""
                            + getPath() + xml.pathSeparator + nodePath + "\"!");
                return null;
            }
        }

        public XmlNode[] getNodes() throws XmlException {
            return getNodes(null, false);
        }

        public XmlNode[] getNodes(String nodePath, boolean raiseIfNotExists)
                throws XmlException {
            if (nodePath == null || nodePath.trim().isEmpty()) {
                List<XmlNode> list = new LinkedList<>();
                for (XmlBaseNode bn : nodes)
                    if (bn instanceof XmlNode)
                        list.add((XmlNode) bn);
                XmlNode[] result = new XmlNode[list.size()];
                list.toArray(result);
                return result;
            }

            String[] path = nodePath.split(xml.pathSeparator);
            XmlNode[] result;
            List<XmlNode> list = new LinkedList<>();

            if (path.length == 0)
                list.add(this);
            else {

                enumNodePath(list, this, path, 0);

                if (list.isEmpty())
                    if (raiseIfNotExists)
                        throw new XmlException("Nie znaleziono gałęzi \""
                                + getPath() + xml.pathSeparator + nodePath + "\"!");
            }

            result = new XmlNode[list.size()];
            list.toArray(result);
            return result;
        }

        public XmlNode[] getNodes(String pathName) throws XmlException {
            return getNodes(pathName, false);
        }

        public String[] getInnerTexts() {
            List<String> result = new LinkedList<>();

            for (int i = 0; i < nodes.size(); i++)
                if (nodes.get(i) instanceof XML_old.XmlTextNode)
                    result.add(((XML_old.XmlTextNode) nodes.get(i)).innerText);

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

        public String getInnerText(String nodePath) throws XmlException {
            return node(nodePath, true).getInnerText();
        }

        public String getInnerText(String nodePath, String defaultValue) {
            XmlNode nn = null;
            try {
                nn = node(nodePath, false);
            } catch (XmlException ex) {
            }
            return nn != null && nn.getInnerText() != null ? nn.getInnerText() : defaultValue;
        }

        public String getInnerText() {
            String[] ss = getInnerTexts();
            String res = null;
            for (String s : ss) {
                if (res == null)
                    res = "";
                res += s;
            }
            return res;
        }

        public XML_old.XmlTextNode[] getTextNodes() throws XmlException {
            List<XML_old.XmlTextNode> result = new LinkedList<>();

            for (int i = 0; i < nodes.size(); i++)
                if (nodes.get(i) instanceof XML_old.XmlTextNode)
                    result.add((XML_old.XmlTextNode) nodes.get(i));

            XML_old.XmlTextNode[] array = new XML_old.XmlTextNode[result.size()];
            result.toArray(array);
            return array;
        }

        /**
         * Metoda zwraca tablice galezi od zadeklarowanej nazwie
         */
        public XmlNode node(String name) throws XmlException {
            return node(name, true);
        }

        //----------------------------------------------------------------------
        /**
         * Metoda zwraca atrybut na podstawie sciezki nodePath Separatorem jest
         * wartosc zmiennej pathSeparator Jesli ktorys z elementow sciezki
         * bedzie pusty, zostanie pominiety Jesli nie znaleziono galezi lub
         * atrybutu zwrocony zostanie wyjatek albo null
         */
        public XmlAttribute getAttribute(String nodePath, boolean raiseIfNotExists) throws XmlException {

            String sAttr = nodePath;
            String sNodes = "";

            if (nodePath.indexOf(xml.pathSeparator) > 0) {
                sAttr = nodePath.substring(
                        nodePath.lastIndexOf(xml.pathSeparator) + 1,
                        nodePath.length());
                sNodes = nodePath.substring(0, nodePath.lastIndexOf(xml.pathSeparator));
            }

            XmlNode nd = this;

            if (!sNodes.isEmpty())
                nd = node(sNodes, raiseIfNotExists);

            XmlAttribute attr = null;

            if (nd != null) {
                for (XmlAttribute xa : nd.attributes)
                    if (compare(xa.name, sAttr)) {
                        attr = xa;
                        break;
                    }

                if (attr == null && xml.useNameSpaces && !nd.getNameSpace().isEmpty()) {
                    String ns = nd.getNameSpace();
                    for (XmlAttribute xa : nd.attributes)
                        if (compare(xa.name, ns + ":" + sAttr)) {
                            attr = xa;
                            break;
                        }
                }
            }

            if (attr == null)
                if (raiseIfNotExists)
                    throw new XmlException("Nie znaleziono atrybutu \""
                            + nd.getPath() + xml.pathSeparator + nodePath + "\"!");
                else
                    return null;

            return attr;
        }

        public String getStr(String pathName) throws XmlException {
            return getAttribute(pathName, true).value;
        }

        public String getStr(String pathName, String defValue) {
            XmlAttribute a = null;
            try {
                a = getAttribute(pathName, false);
            } catch (XmlException ex) {
            }
            return a == null ? defValue : a.value;
        }

        public Boolean getBool(String pathName) throws XmlException {
            String s = getStr(pathName);
            return s.equals("1") || s.equalsIgnoreCase("true");
        }

        public Boolean getBool(String pathName, boolean defValue) {
            return Utils.strBool(getStr(pathName, ""), defValue);
        }

        public int getInt(String pathName) throws XmlException {
            return Integer.parseInt(getStr(pathName));
        }

        public int getInt(String pathName, int defValue) {
            String s = getStr(pathName, null);
            return s == null || s.isEmpty() ? defValue : Integer.parseInt(s);
        }

        public double getDouble(String pathName) throws XmlException {
            return Double.parseDouble(getStr(pathName));
        }

        public double getDouble(String pathName, double defValue) {
            String s = getStr(pathName, null);
            return s == null || s.isEmpty() ? defValue : Double.parseDouble(s);
        }

        public XmlAttribute setInt(String name, int value) throws XmlException {
            return setStr(name, Integer.toString(value));
        }

        public XmlAttribute setDouble(String name, double value) throws XmlException {
            return setStr(name, Double.toString(value));
        }

        public XmlAttribute setBool(String name, boolean value) throws XmlException {
            return setStr(name, Boolean.toString(value));
        }

        public XmlAttribute setStr(String name, String value) throws XmlException {
            XmlAttribute attr = null;

            for (int j = 0; j < attributes.size(); j++)
                if (compare(attributes.get(j).name, name)) {
                    attr = attributes.get(j);
                    attr.value = value;
                    break;
                }

            if (attr == null)
                attr = new XmlAttribute(this, -1, name, value);

            return attr;
        }

        public XmlNode addNode(String nodePath) throws XmlException {
            String[] nn = nodePath.split(xml.pathSeparator);

            XmlNode base = this;

            for (String s : nn)
                if (!s.trim().isEmpty())
                    base = new XmlNode(base, s);
            return base;
        }

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
         * Znajdź gałąź, jeśli nie istnieje to utwórz
         */
        public XmlNode openNode(String nodePath) throws XmlException {
            XmlNode node = node(nodePath, false);
            return node != null ? node : new XmlNode(this, nodePath);
        }

        private void enumFind(XmlNode node, String name) {

            for (int i = 0; i < node.nodes.size(); i++) {
                if (!(node.nodes.get(i) instanceof XmlNode))
                    continue;
                XmlNode nn = (XmlNode) node.nodes.get(i);
                if (compare(nn.name, name))
                    findList.add(nn);
                enumFind(nn, name);
            }
        }

        /**
         * Znajdz galaz na podtsawie nazyw przeszukujac drzewo
         */
        public XmlNode[] findNodes(String name) {
            findList = new LinkedList<>();
            enumFind(this, name);

            XmlNode[] result = new XmlNode[findList.size()];
            findList.toArray(result);
            findList = null;
            return result;
        }

        public void enumNodes(XML_old.XmlEnumNode proc) throws XmlException {
            internalEnumNode(this, 0, proc, this);
        }

        private boolean internalEnumNode(XmlNode nd, int level, XML_old.XmlEnumNode proc, XmlNode nroot) throws XmlException {
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

        public void addClassFields(Object obj) throws XmlException {
            final String[] types = {"boolean", "byte", "int", "short",
                "integer", "long", "double", "float", "string", "date"};

            multiLineAttributes = true;
            Field[] fileds = obj.getClass().getFields();

            for (Field f : fileds)
                try {
                    Object fobj = f.get(obj);
                    if (fobj == null)
                        continue;

                    String typeName = f.getType().getSimpleName();

                    boolean ok = false;
                    for (String s : types)
                        if (s.equalsIgnoreCase(typeName)) {
                            ok = true;
                            break;
                        }
                    if (!ok)
                        continue;

                    String value = fobj.toString();

                    if (f.getType() == Date.class)
                        value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(f.get(obj));

                    setStr(f.getName(), value);

                } catch (IllegalAccessException | IllegalArgumentException ex) {
                }
        }

        public void loadClassFields(Object obj) {
            final String[] types = {"boolean", "byte", "int", "short",
                "integer", "long", "double", "float", "string", "date"};

            Field[] fileds = obj.getClass().getFields();

            for (Field f : fileds)
                try {

                    String typeName = f.getType().getSimpleName();

                    boolean ok = false;
                    for (String s : types)
                        if (s.equalsIgnoreCase(typeName)) {
                            ok = true;
                            break;
                        }
                    if (!ok)
                        continue;

                    String value = getStr(f.getName(), null);

                    //     f.get(obj).toString();
                    if (value == null)
                        continue;

                    switch (typeName) {

                        case "boolean": {
                            f.setBoolean(obj, Boolean.parseBoolean(value));
                            break;
                        }
                        case "Boolean": {
                            f.set(obj, Boolean.valueOf(Boolean.parseBoolean(value)));
                            break;
                        }

                        case "byte": {
                            f.setByte(obj, Byte.parseByte(value));
                            break;
                        }

                        case "Byte": {
                            f.set(obj, Byte.valueOf(Byte.parseByte(value)));
                            break;
                        }

                        case "short": {
                            f.setShort(obj, Short.parseShort(value));
                            break;
                        }
                        case "Short": {
                            f.set(obj, Short.valueOf(Short.parseShort(value)));
                            break;
                        }

                        case "int": {
                            f.setInt(obj, Integer.parseInt(value));
                            break;
                        }
                        case "Integer": {
                            f.set(obj, Integer.valueOf(Integer.parseInt(value)));
                            break;
                        }

                        case "long": {
                            f.setLong(obj, Long.parseLong(value));
                            break;
                        }
                        case "Long": {
                            f.set(obj, Long.valueOf(Long.parseLong(value)));
                            break;
                        }

                        case "float": {
                            f.setFloat(obj, Float.parseFloat(value));
                            break;
                        }

                        case "Float": {
                            f.set(obj, Float.valueOf(Float.parseFloat(value)));
                            break;
                        }

                        case "double": {
                            f.setDouble(obj, Double.parseDouble(value));
                            break;
                        }
                        case "Double": {
                            f.set(obj, Double.valueOf(Double.parseDouble(value)));
                            break;
                        }

                        case "Date": {
                            f.set(obj, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(value));
                            break;
                        }

                        case "String": {
                            f.set(obj, value);
                            break;
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException | ParseException ex) {
                    Log.warning(ex);
                }
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

        public XML_old xml;
        public String name;
        public String value;
        public XmlNode parent;

        @Override
        public String toString() {
            return name + " = " + value;
        }

        public XmlAttribute(XmlNode parent, int index, String name, String value) throws XmlException {
            XML_old.checkName(name);
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

    public XML_old(String sxml) throws XmlException {
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

    public XML_old(File file) throws XmlException {
        load(file);
    }

    public void load(File file) throws XmlException {
        try {
            document = Builder().parse(file);
            parse();
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public XML_old(byte[] bxml) throws XmlException {
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

    public XML_old(InputStream is) throws XmlException {
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

    public XML_old() throws XmlException {
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
            if (bn instanceof XML_old.XmlTextNode)
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

    public XmlNode node(String nodePath, boolean raiseIfNotExists) throws XmlException {
        return root.node(nodePath, raiseIfNotExists);
    }

    public XmlNode node(String nodePath) throws XmlException {
        return root.node(nodePath);
    }

    /**
     * Dodaj nową gałąź
     */
    public XmlNode addNode(String nodePath) throws XmlException {
        return root.addNode(nodePath);
    }

    /**
     * Znajdź gałąź, jeśli nie istnieje to utwórz
     */
    public XmlNode openNode(String nodePath) throws XmlException {
        return root.openNode(nodePath);
    }

    public XmlNode[] getNodes(String pathName) throws XmlException {
        return root.getNodes(pathName);
    }

    public XmlNode[] getNodes() throws XmlException {
        return root.getNodes();
    }

    public String getStr(String pathName) throws XmlException {
        return root.getStr(pathName);
    }

    public String getStr(String pathName, String defValue) throws XmlException {
        return root.getStr(pathName, defValue);
    }

    public Boolean getBool(String pathName) throws XmlException {
        return root.getBool(pathName);
    }

    public Boolean getBool(String pathName, boolean defValue) throws XmlException {
        return root.getBool(pathName, defValue);
    }

    public int getInt(String pathName) throws XmlException {
        return root.getInt(pathName);
    }

    public int getInt(String pathName, int defValue) throws XmlException {
        return root.getInt(pathName, defValue);
    }

    public double getDouble(String pathName) throws XmlException {
        return root.getDouble(pathName);
    }

    public double getDouble(String pathName, double defValue) throws XmlException {
        return root.getDouble(pathName, defValue);
    }

    public XmlAttribute setInt(String name, int value) throws XmlException {
        return root.setInt(name, value);
    }

    public XmlAttribute setDouble(String name, double value) throws XmlException {
        return root.setDouble(name, value);
    }

    public XmlAttribute setBool(String name, boolean value) throws XmlException {
        return root.setBool(name, value);
    }

    public XmlAttribute setStr(String name, String value) throws XmlException {
        return root.setStr(name, value);
    }
}
