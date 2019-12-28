package org.xmlobjects.stream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.xml.SAXBuffer;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.ElementContent;
import org.xmlobjects.xml.Namespaces;
import org.xmlobjects.xml.TextContent;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class XMLWriter implements AutoCloseable {
    private final XMLObjects xmlObjects;
    private final XMLOutput<?> output;

    private final Map<String, ObjectSerializer<?>> serializerCache = new HashMap<>();
    private Properties properties;
    private Transformer transformer;
    private SAXParser parser;

    private final Deque<QName> elements = new ArrayDeque<>();
    private int prefixCounter = 1;
    private EventType lastEvent;

    XMLWriter(XMLObjects xmlObjects, XMLOutput<?> output) {
        this.xmlObjects = xmlObjects;
        this.output = output;
    }

    public XMLObjects getXMLObjects() {
        return xmlObjects;
    }

    public ContentHandler getContentHandler() {
        return output;
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
            output.flush();
        } catch (Exception e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    @Override
    public void close() throws XMLWriteException {
        try {
            if (lastEvent != EventType.END_DOCUMENT)
                writeEndDocument();

            serializerCache.clear();
            output.close();
        } catch (Exception e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public String getPrefix(String namespaceURI) {
        return output.getPrefix(namespaceURI);
    }

    public XMLWriter withPrefix(String prefix, String namespaceURI) {
        output.withPrefix(prefix,namespaceURI);
        return this;
    }

    public String getNamespaceURI(String prefix) {
        return output.getNamespaceURI(prefix);
    }

    public XMLWriter withDefaultNamespace(String namespaceURI) {
        output.withDefaultNamespace(namespaceURI);
        return this;
    }

    public String getIndentString() {
        return output.getIndentString();
    }

    public XMLWriter withIndentString(String indent) {
        output.withIndentString(indent);
        return this;
    }

    public boolean isWriteXMLDeclaration() {
        return output.isWriteXMLDeclaration();
    }

    public XMLWriter writeXMLDeclaration(boolean writeXMLDeclaration) {
        output.writeXMLDeclaration(writeXMLDeclaration);
        return this;
    }

    public String[] getHeaderComment() {
        return output.getHeaderComment();
    }

    public XMLWriter withHeaderComment(String... headerComment) {
        output.withHeaderComment(headerComment);
        return this;
    }

    public String getSchemaLocation(String namespaceURI) {
        return output.getSchemaLocation(namespaceURI);
    }

    public XMLWriter withSchemaLocation(String namespaceURI, String schemaLocation) {
        output.withSchemaLocation(namespaceURI, schemaLocation);
        return this;
    }

    public void writeStartDocument() throws XMLWriteException {
        try {
            output.startDocument();
            lastEvent = EventType.START_DOCUMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndDocument() throws XMLWriteException {
        while (!elements.isEmpty())
            writeEndElement();

        try {
            output.endDocument();
            lastEvent = EventType.END_DOCUMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeObject(Object object, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
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
            SAXResult result = new SAXResult(output);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new XMLWriteException("Failed to initialize DOM transformer.", e);
        } catch (TransformerException e) {
            throw new XMLWriteException("Failed to write DOM element as XML content.", e);
        } finally {
            if (transformer != null)
                transformer.reset();
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
                    if (entry.getValue().isPresent()) {
                        String namespaceURI = entry.getKey().getNamespaceURI();
                        String localName = entry.getKey().getLocalPart();
                        String qName = getQName(namespaceURI, localName);
                        attrs.addAttribute(namespaceURI, localName, qName, "CDATA", entry.getValue().get());
                    }
                }
            }

            output.startElement(name.getNamespaceURI(), name.getLocalPart(), "", attrs);
            elements.push(name);
            lastEvent = EventType.START_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndElement() throws XMLWriteException {
        try {
            QName name = elements.pop();
            output.endElement(name.getNamespaceURI(), name.getLocalPart(), "");
            lastEvent = EventType.END_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeCharacters(String text, int start, int length) throws XMLWriteException {
        try {
            char[] characters = text.toCharArray();
            output.characters(characters, start, length);
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeCharacters(String text) throws XMLWriteException {
        writeCharacters(text, 0, text.length());
    }

    public void writeMixedContent(String mixedContent) throws XMLWriteException {
        try {
            if (parser == null) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newSAXParser();
            }

            SAXBuffer buffer = new MixedContentBuffer();
            parser.getXMLReader().setContentHandler(buffer);
            parser.getXMLReader().parse(new InputSource(new StringReader("<dummy>" + mixedContent + "</dummy>")));
            buffer.send(output, true);
        } catch (ParserConfigurationException e) {
            throw new XMLWriteException("Failed to initialize mixed content parser.", e);
        } catch (SAXException | IOException e) {
            throw new XMLWriteException("Failed to write mixed content.", e);
        } finally {
            if (parser != null)
                parser.reset();
        }
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

    private String getQName(String namespaceURI, String localName) {
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            String prefix = output.getPrefix(namespaceURI);
            if (prefix == null) {
                prefix = "ns" + prefixCounter++;
                output.withPrefix(prefix, namespaceURI);
            }

            return prefix + ":" + localName;
        } else
            return "";
    }

    private static class MixedContentBuffer extends SAXBuffer {
        int depth = 0;

        @Override
        public void startDocument() {
        }

        @Override
        public void endDocument() {
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if (depth++ > 0)
                super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (--depth > 0)
                super.endElement(uri, localName, qName);
        }
    }
}
