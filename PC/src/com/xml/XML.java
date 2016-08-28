package com.xml;

import mlogger.Log;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.*;

public class XML extends XNode {
    
    public Document document;
    public boolean caseSensitive = true;
    public List<String> ignoredDTD = new LinkedList<>();
    public boolean changeReturnsToSpaces = false; // zamien entery na spacje (dla html-a)
    public boolean validateDTD = false;
    public String customDTDvalidator = null; // plik DTD
    public EntityResolver entityResolver = null; // wymagany jesli aktwna jest flaga customDTDvalidator
    public boolean useNameSpaces = true; //wlacza obsluge przestrzeni nazw podczas wyszukiwania elementow 
    public XmlEventListenner eventListenner = null;
    
    public XML(String xml) throws XmlException {
        super(null);
        load(xml);
    }
    
    public XML load(String xml) throws XmlException {
        try {
            document = builder().parse(new InputSource(new StringReader(xml)));
            node = document.getDocumentElement();
            processNode(this);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XmlException(ex);
        }
        return this;
    }
    
    public XML(File xml) throws XmlException {
        super(null);
        load(xml);
    }
    
    public XML load(File xml) throws XmlException {
        try {
            document = builder().parse(xml);
            node = document.getDocumentElement();
            processNode(node);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XmlException(ex);
        }
        return this;
    }
    
    public XML() {
        super(null);
        Document doc = null;
        try {
            doc = builder().parse(new InputSource(new StringReader("<xml/>")));
            node = doc.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Log.error(ex);
        }
        document = doc;
        processNode(node);
    }
    
    public XML(byte[] xml) throws XmlException {
        super(null);
        load(xml);
    }
    
    public XML load(byte[] xml) throws XmlException {
        try {
            document = builder().parse(new ByteArrayInputStream(xml));
            node = document.getDocumentElement();
            processNode(node);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XmlException(ex);
        }
        return this;
    }
    
    public XML(InputStream xml) throws XmlException {
        super(null);
        load(xml);
    }
    
    public XML load(InputStream xml) throws XmlException {
        try {
            document = builder().parse(xml);
            node = document.getDocumentElement();
            processNode(node);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XmlException(ex);
        }
        return this;
    }

    /**
     * Zwraca XML w formie tekstu ze źródłowego parsera DOM
     *
     * @return
     */
    public XML getSourceXML(Writer writer) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // deklaracja xmla
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return this;
        } catch (IllegalArgumentException | TransformerException ex) {
            Log.error(ex);
            return null;
        }
        
    }
    
    public String getSourceXML() {
        StringWriter writer = new StringWriter();
        getSourceXML(writer);
        return writer.toString();
    }
    
    private void processNode(Node node) {
        if (node == document.getDocumentElement())
            node.setUserData("#xnode", this, null);
        
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            XElement xnd = XElement.get(childNodes.item(i));
            if (xnd instanceof XNode)
                processNode(xnd);
        }
    }
    
    private DocumentBuilder builder()
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
    
    @Override
    public String toString() {
        try {
            return getXML();
        } catch (IOException e) {
            Log.warning(e);
            return e.toString();
        }
    }
    
    public String getXML() throws IOException {
        StringWriter writer = new StringWriter();
        getXML(writer);
        return writer.toString();
    }
    
    public void getXML(File file) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));) {
            getXML(out);
        }
    }
    
    public void getXML(OutputStream out) throws IOException {
        getXML(new OutputStreamWriter(out, Charset.forName("UTF-8")));
    }
    
    public void getXML(Writer writer) throws IOException {
        getXML(writer, this);
    }

    public void getXML(Writer writer, Node node) throws IOException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new IOException(ex);
        }
    }
    
    public static class XmlEventListenner {
        
        public HashMap extra = new HashMap();
        
        public boolean beforeWriteNode(Appendable writer, Node nd, int lineNumber) {
            return false;
        }
        
        public boolean beforeWriteTextNode(Appendable writer, Node node, int lineNumber) {
            return false;
        }
        
        public boolean beforeWriteAttribute(Appendable writer, Node attribute, int lineNumber) {
            return false;
        }
        
        public void afterWriteNode(Appendable writer, Node nd, int lineNumber) {
        }
        
        public boolean onCustomEscapeWrite(Appendable writer, Node nd, String s, int lineNumber) {
            // tu mozna podstawic wlasne escapowanie
            return false;
        }
    }
    
    static void checkName(String s) throws XmlException {
        if (s == null || s.trim().isEmpty()
                || !(s.charAt(0) == '_'
                || (s.charAt(0) >= 'a' && s.charAt(0) <= 'z')
                || (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z')))
            throw new XmlException("Nieprawidłowa nazwa \"" + s + "\"");
    }
    
    boolean compare(String name1, String name2, boolean regEx) {
        if (name1 == null || name2 == null)
            return false;
        
        if (regEx)
            return name1.matches(name2);
        
        if (!caseSensitive) {
            name1 = name1.toLowerCase();
            name2 = name2.toLowerCase();
        }
        
        return name2.equals(name1);
    }
    
}
