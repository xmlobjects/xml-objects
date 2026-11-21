/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

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
import org.xmlobjects.util.xml.SAXFilter;
import org.xmlobjects.util.xml.SAXWriter;
import org.xmlobjects.util.xml.SecureXMLProcessors;
import org.xmlobjects.xml.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

public class XMLWriter implements AutoCloseable {
    private final XMLObjects xmlObjects;
    private final XMLOutput<?> output;
    private final Map<Class<?>, ObjectSerializer<?>> serializerCache = new IdentityHashMap<>();
    private final Deque<QName> elements = new ArrayDeque<>();
    private final AttributesImpl attrs = new AttributesImpl();
    private char[] buffer;

    private Properties properties;
    private Transformer transformer;
    private SAXParser parser;
    private boolean prologWritten;
    private EventType lastEvent;

    XMLWriter(XMLObjects xmlObjects, XMLOutput<?> output) {
        this.xmlObjects = xmlObjects;
        this.output = output;
        output.getPrefixMapping().createInternalPrefixes(xmlObjects);
    }

    public XMLObjects getXMLObjects() {
        return xmlObjects;
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
            if (lastEvent != EventType.END_DOCUMENT) {
                finishDocument(prologWritten);
            }

            output.close();
        } catch (Exception e) {
            throw new XMLWriteException("Caused by:", e);
        } finally {
            serializerCache.clear();
        }
    }

    public String getPrefix(String namespaceURI) {
        return output.getPrefix(namespaceURI);
    }

    public XMLWriter withPrefix(String prefix, String namespaceURI) {
        output.withPrefix(prefix, namespaceURI);
        return this;
    }

    public String getNamespaceURI(String prefix) {
        return output.getNamespaceURI(prefix);
    }

    public XMLWriter withDefaultNamespace(String namespaceURI) {
        output.withDefaultNamespace(namespaceURI);
        return this;
    }

    public String getIndent() {
        return output.getIndent();
    }

    public XMLWriter withIndent(String indent) {
        output.withIndent(indent);
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
            prologWritten = true;
            lastEvent = EventType.START_DOCUMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndDocument() throws XMLWriteException {
        finishDocument(true);
    }

    private void finishDocument(boolean writeEndDocument) throws XMLWriteException {
        while (!elements.isEmpty()) {
            writeEndElement();
        }

        if (writeEndDocument) {
            try {
                output.endDocument();
                lastEvent = EventType.END_DOCUMENT;
            } catch (SAXException e) {
                throw new XMLWriteException("Caused by:", e);
            }
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
            if (serializer != null) {
                writeElementUsingSerializer(element, object, serializer, namespaces);
            }
        }
    }

    public <T> void writeElementUsingSerializer(Element element, T object, Class<? extends ObjectSerializer<T>> type, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writeElementUsingSerializer(element, object, getOrCreateSerializer(type), namespaces);
    }

    public <T> void writeElementUsingSerializer(Element element, T object, ObjectSerializer<T> serializer, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        if (object != null) {
            if (element == null) {
                element = serializer.createElement(object, namespaces);
            }

            if (element != null) {
                serializer.initializeElement(element, object, namespaces, this);
                writeStartElement(element);
            }

            serializer.writeChildElements(object, namespaces, this);

            if (element != null) {
                writeEndElement();
            }
        }
    }

    public void writeElement(Element element) throws XMLWriteException {
        writeStartElement(element);
        writeEndElement();
    }

    public void writeStartElement(Element element) throws XMLWriteException {
        if (element == null) {
            throw new XMLWriteException("Illegal to call writeStartElement with a null element.");
        }

        writeStartElement(element.getName(), element.getAttributes());
        if (element.hasContent()) {
            for (ElementContent content : element.getContent()) {
                if (content.isSetElement()) {
                    writeElement(content.getElement());
                } else if (content.isSetTextContent() && content.getTextContent().isPresent()) {
                    writeTextContent(content.getTextContent());
                }
            }
        }
    }

    private void writeStartElement(QName name, Attributes attributes) throws XMLWriteException {
        try {
            // SAX handlers must not keep a reference to the attributes, so the instance can be reused
            attrs.clear();
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, Map<String, TextContent>> entry : attributes.get().entrySet()) {
                    for (Map.Entry<String, TextContent> attribute : entry.getValue().entrySet()) {
                        if (attribute.getValue().isPresent()) {
                            String namespaceURI = entry.getKey();
                            String localName = attribute.getKey();
                            String qName = getQName(namespaceURI, localName);
                            attrs.addAttribute(namespaceURI, localName, qName, "CDATA", attribute.getValue().get());
                        }
                    }
                }
            }

            String namespaceURI = name.getNamespaceURI();
            String localName = name.getLocalPart();

            // SAXWriter determines prefixes itself, other content handlers need the qualified name
            String qName = output instanceof SAXWriter ? localName : getElementQName(namespaceURI, localName, true);
            output.startElement(namespaceURI, localName, qName, attrs);
            elements.push(name);
            lastEvent = EventType.START_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    private String getQName(String namespaceURI, String localName) throws SAXException {
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            String prefix = output.getPrefix(namespaceURI);
            if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                prefix = output.createPrefix(namespaceURI);
                output.startPrefixMapping(prefix, namespaceURI);
            }

            return prefix + ":" + localName;
        } else {
            return localName;
        }
    }

    private String getElementQName(String namespaceURI, String localName, boolean declare) throws SAXException {
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            // unlike attributes, elements may use the default namespace
            String prefix = output.getPrefix(namespaceURI);
            if (prefix == null && declare) {
                prefix = output.createPrefix(namespaceURI);
                output.startPrefixMapping(prefix, namespaceURI);
            }

            if (prefix != null && !prefix.isEmpty()) {
                return prefix + ":" + localName;
            }
        }

        return localName;
    }

    public void writeEndElement() throws XMLWriteException {
        if (elements.isEmpty()) {
            throw new XMLWriteException("Illegal to call writeEndElement without an open element.");
        }

        try {
            QName name = elements.pop();
            String namespaceURI = name.getNamespaceURI();
            String localName = name.getLocalPart();
            String qName = output instanceof SAXWriter ? "" : getElementQName(namespaceURI, localName, false);
            output.endElement(namespaceURI, localName, qName);
            lastEvent = EventType.END_ELEMENT;
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public void writeEndElements(int count) throws XMLWriteException {
        while (count-- > 0) {
            writeEndElement();
        }
    }

    public void writeCharacters(String text, int start, int length) throws XMLWriteException {
        try {
            if (output instanceof SAXWriter saxWriter) {
                saxWriter.characters(text, start, length);
            } else {
                if (buffer == null || buffer.length < length) {
                    buffer = new char[Math.max(length, 1024)];
                }

                text.getChars(start, start + length, buffer, 0);
                output.characters(buffer, 0, length);
            }
        } catch (SAXException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    private void writeTextContent(TextContent textContent) throws XMLWriteException {
        if (output instanceof SAXWriter saxWriter
                && saxWriter.isEscapeCharacters()
                && textContent.isMarkupFree()) {
            // generated from numbers, booleans, dates etc., so nothing to escape
            saxWriter.escapeCharacters(false);
            try {
                writeCharacters(textContent.get());
            } finally {
                saxWriter.escapeCharacters(true);
            }
        } else {
            writeCharacters(textContent.get());
        }
    }

    public void writeCharacters(String text) throws XMLWriteException {
        writeCharacters(text, 0, text.length());
    }

    public void writeDOMElement(org.w3c.dom.Element element) throws XMLWriteException {
        if (element == null) {
            return;
        }

        try {
            if (transformer == null) {
                transformer = SecureXMLProcessors.newTransformerFactory().newTransformer();
            }

            DOMSource source = new DOMSource(element);
            SAXResult result = new SAXResult(new DOMHandler(output));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new XMLWriteException("Failed to initialize DOM transformer.", e);
        } catch (TransformerException e) {
            throw new XMLWriteException("Failed to write DOM element as XML content.", e);
        } finally {
            if (transformer != null) {
                transformer.reset();
            }
        }
    }

    public void writeMixedContent(String mixedContent) throws XMLWriteException {
        try {
            if (parser == null) {
                SAXParserFactory factory = SecureXMLProcessors.newSAXParserFactory();
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
            if (parser != null) {
                parser.reset();
            }
        }
    }

    public <T> ObjectSerializer<T> getOrCreateSerializer(Class<? extends ObjectSerializer<T>> type) throws ObjectSerializeException {
        ObjectSerializer<?> cachedSerializer = serializerCache.get(type);
        if (cachedSerializer != null) {
            return type.cast(cachedSerializer);
        } else {
            try {
                ObjectSerializer<T> serializer = type.getDeclaredConstructor().newInstance();
                serializerCache.put(type, serializer);
                return serializer;
            } catch (Exception e) {
                throw new ObjectSerializeException("The serializer " + type.getName() + " lacks a default constructor.");
            }
        }
    }

    public ContentHandler getContentHandler() {
        return getContentHandler(false);
    }

    public ContentHandler getContentHandler(boolean writeFragment) {
        return new SAXFilter(output) {
            @Override
            public void startDocument() throws SAXException {
                if (!writeFragment) {
                    super.startDocument();
                    prologWritten = true;
                    lastEvent = EventType.START_DOCUMENT;
                }
            }

            @Override
            public void endDocument() throws SAXException {
                if (!writeFragment) {
                    super.endDocument();
                    lastEvent = EventType.END_DOCUMENT;
                }
            }

            @Override
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException {
                super.startElement(uri, localName, qName, atts);
                elements.push(new QName(uri, localName));
                lastEvent = EventType.START_ELEMENT;
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                elements.pop();
                lastEvent = EventType.END_ELEMENT;
            }
        };
    }

    private static class DOMHandler extends SAXFilter {

        DOMHandler(ContentHandler parent) {
            super(parent);
        }

        @Override
        public void startDocument() {
        }

        @Override
        public void endDocument() {
        }
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
            if (depth++ > 0) {
                super.startElement(uri, localName, qName, attributes);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (--depth > 0) {
                super.endElement(uri, localName, qName);
            }
        }
    }
}
