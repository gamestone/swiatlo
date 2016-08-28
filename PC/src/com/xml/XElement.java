package com.xml;

import org.w3c.dom.*;

public class XElement implements Node {

    protected Node node;

    public XElement(Node node) {
        this.node = node;
        if (node != null)
            node.setUserData("#xnode", this, null);
    }

    @Override
    public String toString() {
        return node.getNodeName();
    }

    static XElement get(Node source) {
        XElement xnode = (XElement) source.getUserData("#xnode");
        if (xnode != null)
            return xnode;

        switch (source.getNodeType()) {
            case Element.ELEMENT_NODE:
                return new XNode(source);
            case Element.TEXT_NODE:
                return new XText(source);
            case Element.COMMENT_NODE:
                return new XComment(source);
            case Element.CDATA_SECTION_NODE:
                return new XCData(source);
        }

        throw new RuntimeException("Nieznany typ gałęzi: " + source.getNodeType());
    }

    public String name() {
        String name = node.getNodeName();
        return name != null ? name : "";
    }

    @Override
    public String getNodeName() {
        return node.getNodeName();
    }

    @Override
    public String getNodeValue() throws DOMException {
        return node.getNodeValue();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        node.setNodeValue(nodeValue);
    }

    @Override
    public short getNodeType() {
        return node.getNodeType();
    }

    @Override
    public Node getParentNode() {
        return node.getParentNode();
    }

    @Override
    public NodeList getChildNodes() {
        return node.getChildNodes();
    }

    @Override
    public Node getFirstChild() {
        return node.getFirstChild();
    }

    @Override
    public Node getLastChild() {
        return node.getLastChild();
    }

    @Override
    public Node getPreviousSibling() {
        return node.getPreviousSibling();
    }

    @Override
    public Node getNextSibling() {
        return node.getNextSibling();
    }

    @Override
    public NamedNodeMap getAttributes() {
        return node.getAttributes();
    }

    @Override
    public Document getOwnerDocument() {
        return node.getOwnerDocument();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return node.insertBefore(newChild, refChild);
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return node.replaceChild(newChild, oldChild);
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return node.removeChild(oldChild);
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return node.appendChild(newChild);
    }

    @Override
    public boolean hasChildNodes() {
        return node.hasChildNodes();
    }

    @Override
    public Node cloneNode(boolean deep) {
        return node.cloneNode(deep);
    }

    @Override
    public void normalize() {
        node.normalize();
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return node.isSupported(feature, version);
    }

    @Override
    public String getNamespaceURI() {
        return node.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return node.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        node.setPrefix(prefix);
    }

    @Override
    public String getLocalName() {
        return node.getLocalName();
    }

    @Override
    public boolean hasAttributes() {
        return node.hasAttributes();
    }

    @Override
    public String getBaseURI() {
        return node.getBaseURI();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return node.compareDocumentPosition(other);
    }

    @Override
    public String getTextContent() throws DOMException {
        return node.getTextContent();
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        node.setTextContent(textContent);
    }

    @Override
    public boolean isSameNode(Node other) {
        return node.isSameNode(other);
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return node.lookupPrefix(namespaceURI);
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return node.isDefaultNamespace(namespaceURI);
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return node.lookupNamespaceURI(prefix);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return node.isEqualNode(arg);
    }

    @Override
    public Object getFeature(String feature, String version) {
        return node.getFeature(feature, version);
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return node.setUserData(key, data, handler);
    }

    @Override
    public Object getUserData(String key) {
        return node.getUserData(key);
    }

}
