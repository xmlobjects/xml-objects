package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Element {
    private final QName name;
    private Attributes attributes;
    private List<ElementContent> content;

    private Element(String namespaceURI, String localName) {
        this.name = new QName(namespaceURI, localName);
    }

    public static Element of(String namespaceURI, String localName) {
        return new Element(namespaceURI, localName);
    }

    public QName getName() {
        return name;
    }

    public Element addAttribute(String namespaceURI, String localName, String value) {
        if (attributes == null)
            attributes = new Attributes();

        attributes.add(namespaceURI, localName, value);
        return this;
    }

    public Element addAttribute(String name, String value) {
        return addAttribute(XMLConstants.NULL_NS_URI, name, value);
    }

    public Element addAttribute(QName name, String value) {
        return addAttribute(name.getNamespaceURI(), name.getLocalPart(), value);
    }

    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    public Attributes getAttributes() {
        return attributes;
    }

    private Element addContent(ElementContent item) {
        if (content == null)
            content = new ArrayList<>();

        content.add(item);
        return this;
    }

    public Element addChildElement(Element child) {
        return addContent(ElementContent.of(child));
    }

    public Element addText(String text) {
        return addContent(ElementContent.of(text));
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public List<ElementContent> getContent() {
        return content != null ? content : Collections.emptyList();
    }
}
