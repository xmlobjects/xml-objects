/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2023 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmlobjects.util.xml;

import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.util.xml.ArrayBuffer.ArrayBufferIterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SAXStreamReader implements XMLStreamReader {
    private final ArrayBufferIterator<Byte> events;
    private final ArrayBufferIterator<String> strings;
    private final ArrayBufferIterator<char[]> characters;

    private final ArrayBuffer<String> elements = new ArrayBuffer<>(String.class, ArrayBuffer.DEFAULT_BUFFER_SIZE);
    private final NamespaceSupport prefixMapping = new NamespaceSupport();
    private final AttributesImpl attributes = new AttributesImpl();
    private final Namespaces namespaces = new Namespaces();

    private String localName;
    private String namespaceURI;
    private String prefix;
    private char[] chars;
    private int eventType = XMLStreamConstants.START_DOCUMENT;

    SAXStreamReader(ArrayBufferIterator<Byte> events, ArrayBufferIterator<String> strings, ArrayBufferIterator<char[]> characters) {
        this.events = events;
        this.strings = strings;
        this.characters = characters;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public int next() throws XMLStreamException {
        switch (events.next()) {
            case SAXBuffer.START_ELEMENT:
                eventType = XMLStreamConstants.START_ELEMENT;
                processStartElement();
                break;
            case SAXBuffer.END_ELEMENT:
                eventType = XMLStreamConstants.END_ELEMENT;
                processEndElement();
                break;
            case SAXBuffer.CHARACTERS:
                eventType = XMLStreamConstants.CHARACTERS;
                chars = characters.next();
                break;
            case SAXBuffer.START_DOCUMENT:
                eventType = XMLStreamConstants.START_DOCUMENT;
                break;
            case SAXBuffer.END_DOCUMENT:
                eventType = XMLStreamConstants.END_DOCUMENT;
                break;
            case SAXBuffer.NAMESPACE_PREFIX_MAPPING:
                eventType = XMLStreamConstants.NAMESPACE;
                processNamespace();
                break;
            default:
                throw new XMLStreamException("The SAX buffer is in an invalid state.");
        }

        return eventType;
    }

    private void processNamespace() {
        String prefix = strings.next();
        String uri = strings.next();
        if (prefix == null) {
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
        }

        namespaces.add(prefix, uri);
        prefixMapping.pushContext();
        prefixMapping.declarePrefix(prefix, uri);
    }

    private void processStartElement() {
        prefixMapping.pushContext();

        namespaceURI = strings.next();
        localName = strings.next();
        String qName = strings.next();

        prefix = prefixMapping.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = prefixMapping.createPrefixFromQName(qName, namespaceURI);
            prefixMapping.declarePrefix(prefix, namespaceURI);
        }

        attributes.clear();
        while (events.hasNext() && events.peek() == SAXBuffer.ATTRIBUTE) {
            events.next();
            String attributeURI = strings.next();
            String attributeLocalName = strings.next();
            String attributeQName = strings.next();
            String attributeType = strings.next();
            String attributeValue = strings.next();

            attributes.addAttribute(attributeURI != null ? attributeURI : XMLConstants.NULL_NS_URI,
                    attributeLocalName,
                    !attributeQName.isEmpty() ? attributeQName : attributeLocalName,
                    attributeType,
                    attributeValue);
        }

        elements.push(namespaceURI);
        elements.push(localName);
        prefixMapping.requireNextContext();
    }

    private void processEndElement() {
        localName = elements.pop();
        namespaceURI = elements.pop();
        namespaces.clear();
        prefixMapping.popContext();
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (type != eventType) {
            throw new XMLStreamException("The specified event type " + type +
                    " does not match the current parser event.");
        }

        if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
            throw new XMLStreamException("The specified namespace URI " + namespaceURI +
                    " does not match the current namespace URI.");
        }

        if (localName != null && !localName.equals(getLocalName())) {
            throw new XMLStreamException("The local name " + localName + " does not match the current local name.");
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if (eventType != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException("Illegal to call getElementText when event is not START_ELEMENT.");
        }

        StringBuilder content = new StringBuilder();

        next();
        while (eventType != XMLStreamConstants.END_ELEMENT) {
            if (eventType != XMLStreamConstants.CHARACTERS) {
                throw new XMLStreamException("Element is not text-only.");
            }

            content.append(getText());
            next();
        }

        return content.toString();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        next();
        while (eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) {
            next();
        }

        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Expected START_ELEMENT or END_ELEMENT.");
        }

        return eventType;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return events.hasNext();
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix must not be null.");
        }

        return prefixMapping.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
        return eventType == XMLStreamConstants.START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return eventType == XMLStreamConstants.END_ELEMENT;
    }

    @Override
    public boolean isCharacters() {
        return eventType == XMLStreamConstants.CHARACTERS;
    }

    @Override
    public boolean isWhiteSpace() {
        if (eventType == XMLStreamConstants.CHARACTERS) {
            for (char ch : chars) {
                if (!Character.isWhitespace(ch)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public int getAttributeCount() {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeCount when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return attributes.getLength();
    }

    @Override
    public QName getAttributeName(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeName when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return new QName(attributes.getURI(index), attributes.getLocalName(index));
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeNamespace when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return attributes.getURI(index);
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeLocalName when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return attributes.getLocalName(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributePrefix when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return prefixMapping.getPrefix(attributes.getURI(index));
    }

    @Override
    public String getAttributeType(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeType when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return attributes.getType(index);
    }

    @Override
    public String getAttributeValue(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call getAttributeValue when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return attributes.getValue(index);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Illegal to call isAttributeSpecified when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return true;
    }

    @Override
    public int getNamespaceCount() {
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT
                && eventType != XMLStreamConstants.NAMESPACE) {
            throw new IllegalStateException("Illegal to call getNamespaceCount when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return namespaces.size();
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT
                && eventType != XMLStreamConstants.NAMESPACE) {
            throw new IllegalStateException("Illegal to call getNamespacePrefix when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return namespaces.getPrefix(index);
    }

    @Override
    public String getNamespaceURI(int index) {
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT
                && eventType != XMLStreamConstants.NAMESPACE) {
            throw new IllegalStateException("Illegal to call getNamespaceURI when event is neither " +
                    "START_ELEMENT nor ATTRIBUTE.");
        }

        return namespaces.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                String namespaceURI = prefixMapping.getNamespaceURI(prefix);
                return namespaceURI != null ? namespaceURI : XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                if (namespaceURI == null) {
                    throw new IllegalArgumentException("Namespace URI must not be null.");
                }

                return prefixMapping.getPrefix(namespaceURI);
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                if (namespaceURI == null) {
                    throw new IllegalArgumentException("Namespace URI must not be null.");
                }

                return prefixMapping.getPrefixes(namespaceURI).iterator();
            }
        };
    }

    @Override
    public int getEventType() {
        return eventType;
    }

    @Override
    public String getText() {
        if (eventType != XMLStreamConstants.CHARACTERS) {
            throw new IllegalStateException("Illegal to call getText when event is not CHARACTERS.");
        }

        return String.valueOf(chars);
    }

    @Override
    public char[] getTextCharacters() {
        if (eventType != XMLStreamConstants.CHARACTERS) {
            throw new IllegalStateException("Illegal to call getTextCharacters when event is not CHARACTERS.");
        }

        return chars;
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if (target == null) {
            throw new NullPointerException("Target char array must not be null.");
        }

        if (targetStart < 0 || length < 0 || sourceStart < 0
                || targetStart >= target.length
                || (targetStart + length) > target.length) {
            throw new IndexOutOfBoundsException();
        }

        int available = chars.length - sourceStart;
        if (available < 0) {
            throw new IndexOutOfBoundsException("sourceStart is greater than number of characters associated " +
                    "with this event.");
        }

        int copied = Math.min(available, length);
        System.arraycopy(chars, sourceStart, target, targetStart, copied);

        return copied;
    }

    @Override
    public int getTextStart() {
        if (eventType != XMLStreamConstants.CHARACTERS) {
            throw new IllegalStateException("Illegal to call getTextStart when event is not CHARACTERS.");
        }

        return 0;
    }

    @Override
    public int getTextLength() {
        if (eventType != XMLStreamConstants.CHARACTERS) {
            throw new IllegalStateException("Illegal to call getTextLength when event is not CHARACTERS.");
        }

        return chars.length;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public boolean hasText() {
        return eventType == XMLStreamConstants.CHARACTERS;
    }

    @Override
    public Location getLocation() {
        return new Location() {
            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public int getColumnNumber() {
                return -1;
            }

            @Override
            public int getCharacterOffset() {
                return -1;
            }

            @Override
            public String getPublicId() {
                return null;
            }

            @Override
            public String getSystemId() {
                return null;
            }
        };
    }

    @Override
    public QName getName() {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new IllegalStateException("Illegal to call getName when event is neither " +
                    "START_ELEMENT nor END_ELEMENT.");
        }

        return new QName(namespaceURI, localName);
    }

    @Override
    public String getLocalName() {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new IllegalStateException("Illegal to call getLocalName when event is neither " +
                    "START_ELEMENT nor END_ELEMENT.");
        }

        return localName;
    }

    @Override
    public boolean hasName() {
        return eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT;
    }

    @Override
    public String getNamespaceURI() {
        if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
            return namespaceURI;
        }

        return null;
    }

    @Override
    public String getPrefix() {
        if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
            return prefix;
        }

        return null;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean standaloneSet() {
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return null;
    }

    @Override
    public String getPITarget() {
        return null;
    }

    @Override
    public String getPIData() {
        return null;
    }

    private static class Namespaces {
        private final List<String> prefixes = new ArrayList<>();
        private final List<String> namespaceURIs = new ArrayList<>();

        void add(String prefix, String namespaceURI) {
            prefixes.add(prefix);
            namespaceURIs.add(namespaceURI);
        }

        String getPrefix(int index) {
            return prefixes.get(index);
        }

        String getNamespaceURI(int index) {
            return namespaceURIs.get(index);
        }

        int size() {
            return prefixes.size();
        }

        void clear() {
            prefixes.clear();
            namespaceURIs.clear();
        }
    }
}
