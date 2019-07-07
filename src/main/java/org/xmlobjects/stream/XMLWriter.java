package org.xmlobjects.stream;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.util.SAXWriter;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.ElementContent;
import org.xmlobjects.xml.Namespaces;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class XMLWriter implements AutoCloseable {
    private final XMLObjects xmlObjects;
    private final SAXWriter saxWriter;

    private final Deque<QName> elements = new ArrayDeque<>();
    private EventType lastEvent;

    XMLWriter(XMLObjects xmlObjects, SAXWriter saxWriter) {
        this.xmlObjects = xmlObjects;
        this.saxWriter = saxWriter;
    }

    public SAXWriter getSAXWriter() {
        return saxWriter;
    }

    public void flush() throws XMLWriteException {
        try {
            saxWriter.flush();
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    @Override
    public void close() throws XMLWriteException {
        try {
            if (lastEvent != EventType.END_DOCUMENT)
                writeEndDocument();

            saxWriter.close();
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public boolean isEscapeCharacters() {
        return saxWriter.isEscapeCharacters();
    }

    public XMLWriter escapeCharacters(boolean escapeCharacters) {
        saxWriter.escapeCharacters(escapeCharacters);
        return this;
    }

    public boolean isWriteReportedNamespaces() {
        return saxWriter.isWriteReportedNamespaces();
    }

    public XMLWriter writeReportedNamespaces(boolean writeReportedNamespaces) {
        saxWriter.writeReportedNamespaces(writeReportedNamespaces);
        return this;
    }

    public XMLWriter usePrefix(String prefix, String namespaceURI) {
        saxWriter.usePrefix(prefix,namespaceURI);
        return this;
    }

    public String getPrefix(String namespaceURI) {
        return saxWriter.getPrefix(namespaceURI);
    }

    public String getNamespaceURI(String prefix) {
        return saxWriter.getNamespaceURI(prefix);
    }

    public XMLWriter useDefaultNamespace(String namespaceURI) {
        saxWriter.useDefaultNamespace(namespaceURI);
        return this;
    }

    public String getIndentString() {
        return saxWriter.getIndentString();
    }

    public XMLWriter useIndentString(String indent) {
        saxWriter.useIndentString(indent);
        return this;
    }

    public boolean isWriteEncoding() {
        return saxWriter.isWriteEncoding();
    }

    public XMLWriter writeEncoding(boolean writeEncoding) {
        saxWriter.writeEncoding(writeEncoding);
        return this;
    }

    public boolean isWriteXMLDeclaration() {
        return saxWriter.isWriteXMLDeclaration();
    }

    public XMLWriter writeXMLDeclaration(boolean writeXMLDeclaration) {
        saxWriter.writeXMLDeclaration(writeXMLDeclaration);
        return this;
    }

    public String[] getHeaderComment() {
        return saxWriter.getHeaderComment();
    }

    public XMLWriter useHeaderComment(String... headerComment) {
        saxWriter.useHeaderComment(headerComment);
        return this;
    }

    public String getSchemaLocation(String namespaceURI) {
        return saxWriter.getSchemaLocation(namespaceURI);
    }

    public XMLWriter useSchemaLocation(String namespaceURI, String schemaLocation) {
        saxWriter.useHeaderComment(namespaceURI, schemaLocation);
        return this;
    }

    public void writeStartDocument() throws XMLWriteException {
        try {
            saxWriter.startDocument();
            lastEvent = EventType.START_DOCUMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndDocument() throws XMLWriteException {
        while (!elements.isEmpty())
            writeEndElement();

        saxWriter.endDocument();
        lastEvent = EventType.END_DOCUMENT;
    }

    @SuppressWarnings("unchecked")
    public <T> void writeElement(T object, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        ObjectSerializer<T> serializer = (ObjectSerializer<T>) xmlObjects.getSerializer(object.getClass(), namespaces);
        if (serializer != null) {
            Element element = serializer.createElement(object, namespaces);
            if (element == null)
                throw new ObjectSerializeException("The serializer " + serializer.getClass().getName() + " created a null value.");

            serializer.serializeElement(element, object, namespaces, this);
        }
    }

    public void writeElement(Element element) throws XMLWriteException {
        writeStartElement(element);
        writeEndElement();
    }

    public void writeStartElement(Element element) throws XMLWriteException {
        writeStartElement(element.getName(), element.getAttributes());
        if (element.hasContent()) {
            for (ElementContent content : element.getContent()) {
                if (content.isSetElement())
                    writeElement(content.getElement());
                else
                    writeCharacters(content.getText());
            }
        }
    }

    private void writeStartElement(QName name, Attributes attributes) throws XMLWriteException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            if (attributes != null && !attributes.isEmpty()) {
                attributes.toMap()
                        .forEach((k, v) -> attrs.addAttribute(k.getNamespaceURI(), k.getLocalPart(), null, "CDATA", v.get()));
            }

            saxWriter.startElement(name.getNamespaceURI(), name.getLocalPart(), null, attrs);
            elements.push(name);
            lastEvent = EventType.START_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndElement() throws XMLWriteException {
        try {
            QName name = elements.pop();
            saxWriter.endElement(name.getNamespaceURI(), name.getLocalPart(), null);
            lastEvent = EventType.END_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeCharacters(String text) throws XMLWriteException {
        try {
            char[] characters = text.toCharArray();
            saxWriter.characters(characters, 0, characters.length);
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeCharacters(String text, int start, int len) throws XMLWriteException {
        try {
            char[] characters = text.toCharArray();
            saxWriter.characters(characters, start, len);
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }
}
