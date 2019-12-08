package org.xmlobjects.stream;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.xml.SAXWriter;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.ElementContent;
import org.xmlobjects.xml.Namespaces;
import org.xmlobjects.xml.TextContent;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class XMLWriter implements AutoCloseable {
    private final XMLObjects xmlObjects;
    private final SAXWriter saxWriter;

    private final Map<String, ObjectSerializer<?>> serializerCache = new HashMap<>();
    private Properties properties;
    private Transformer transformer;

    private final Deque<QName> elements = new ArrayDeque<>();
    private EventType lastEvent;

    XMLWriter(XMLObjects xmlObjects, SAXWriter saxWriter) {
        this.xmlObjects = xmlObjects;
        this.saxWriter = saxWriter;
    }

    public XMLObjects getXMLObjects() {
        return xmlObjects;
    }

    public SAXWriter getSAXWriter() {
        return saxWriter;
    }

    public Properties getProperties() {
        if (properties == null)
            properties = new Properties();

        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = new Properties(properties);
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

            serializerCache.clear();
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

    public <T> void writeObject(T object, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writeElement(null, object, namespaces);
    }

    public <T> void writeObjectUsingSerializer(T object, Class<? extends ObjectSerializer<T>> type, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writeElementUsingSerializer(null, object, type, namespaces);
    }

    public <T> void writeObjectUsingSerializer(T object, ObjectSerializer<T> serializer, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writeElementUsingSerializer(null, object, serializer, namespaces);
    }

    @SuppressWarnings("unchecked")
    public <T> void writeElement(Element element, T object, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        if (object != null) {
            ObjectSerializer<T> serializer = (ObjectSerializer<T>) xmlObjects.getSerializer(object.getClass(), namespaces);
            if (serializer != null)
                writeElementUsingSerializer(element, object, serializer, namespaces);
        }
    }

    public <T> void writeElementUsingSerializer(Element element, T object, Class<? extends ObjectSerializer<T>> type, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writeElementUsingSerializer(element, object, getOrCreateSerializer(type), namespaces);
    }

    public <T> void writeElementUsingSerializer(Element element, T object, ObjectSerializer<T> serializer, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        if (object != null) {
            if (element == null)
                element = serializer.createElement(object, namespaces);

            if (element != null) {
                serializer.initializeElement(element, object, namespaces, this);
                writeStartElement(element);
            }

            serializer.writeChildElements(object, namespaces, this);

            if (element != null)
                writeEndElement();
        }
    }

    public void writeElement(Element element) throws XMLWriteException {
        writeStartElement(element);
        writeEndElement();
    }

    public void writeDOMElement(org.w3c.dom.Element element) throws XMLWriteException {
        if (element == null)
            return;

        try {
            if (transformer == null)
                transformer = TransformerFactory.newInstance().newTransformer();

            DOMSource source = new DOMSource(element);
            SAXResult result = new SAXResult(saxWriter);
            transformer.transform(source, result);
            transformer.reset();
        } catch (TransformerConfigurationException e) {
            throw new XMLWriteException("Failed to initialize DOM transformer.", e);
        } catch (TransformerException e) {
            throw new XMLWriteException("Failed to write DOM element as XML content.", e);
        }
    }

    public void writeStartElement(Element element) throws XMLWriteException {
        if (element == null)
            throw new XMLWriteException("Illegal to call writeStartElement with a null element.");

        writeStartElement(element.getName(), element.getAttributes());
        if (element.hasContent()) {
            for (ElementContent content : element.getContent()) {
                if (content.isSetElement())
                    writeElement(content.getElement());
                else if (content.isSetTextContent() && content.getTextContent().isPresent())
                    writeCharacters(content.getTextContent().get());
            }
        }
    }

    private void writeStartElement(QName name, Attributes attributes) throws XMLWriteException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<QName, TextContent> entry : attributes.toMap().entrySet()) {
                    if (entry.getValue().isPresent())
                        attrs.addAttribute(entry.getKey().getNamespaceURI(), entry.getKey().getLocalPart(), null, "CDATA", entry.getValue().get());
                }
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

    public void writeCharacters(String text, boolean escapeCharacters) throws XMLWriteException {
        saxWriter.escapeCharacters(escapeCharacters);
        writeCharacters(text);
        saxWriter.escapeCharacters(true);
    }

    public void writeCharacters(String text, int start, int length) throws XMLWriteException {
        try {
            char[] characters = text.toCharArray();
            saxWriter.characters(characters, start, length);
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeCharacters(String text, int start, int length, boolean escapeCharacters) throws XMLWriteException {
        saxWriter.escapeCharacters(escapeCharacters);
        writeCharacters(text, start, length);
        saxWriter.escapeCharacters(true);
    }

    public void writeMixedContent(String mixedContent) throws XMLWriteException {
        writeCharacters(mixedContent, false);
    }

    public <T> ObjectSerializer<T> getOrCreateSerializer(Class<? extends ObjectSerializer<T>> type) throws ObjectSerializeException {
        ObjectSerializer<T> serializer;

        // get serializer from cache or create a new instance
        ObjectSerializer<?> cachedSerializer = serializerCache.get(type.getName());
        if (cachedSerializer != null && type.isAssignableFrom(cachedSerializer.getClass()))
            serializer = type.cast(cachedSerializer);
        else {
            try {
                serializer = type.getDeclaredConstructor().newInstance();
                serializerCache.put(type.getName(), serializer);
            } catch (Exception e) {
                throw new ObjectSerializeException("The serializer " + type.getName() + " lacks a default constructor.");
            }
        }

        return serializer;
    }
}
