/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2022 Claus Nagel <claus.nagel@gmail.com>
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlobjects.util.xml.ArrayBuffer.ArrayBufferIterator;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;

public class SAXBuffer extends DefaultHandler {
    public static final int DEFAULT_BUFFER_SIZE = 512;

    static final byte UNDEFINED = 0;
    static final byte START_DOCUMENT = 1;
    static final byte END_DOCUMENT = 2;
    static final byte START_ELEMENT = 3;
    static final byte END_ELEMENT = 4;
    static final byte CHARACTERS = 5;
    static final byte NAMESPACE_PREFIX_MAPPING = 6;
    static final byte ATTRIBUTE = 7;

    private final String END_PREFIX_MAPPING = "END_PREFIX_MAPPING";
    private final ArrayBuffer<Byte> events;
    private final ArrayBuffer<String> strings;
    private final ArrayBuffer<char[]> characters;

    private byte lastElement = UNDEFINED;
    private boolean assumeMixedContent = true;

    public SAXBuffer(int bufferSize) {
        events = new ArrayBuffer<>(Byte.class, bufferSize);
        strings = new ArrayBuffer<>(String.class, bufferSize);
        characters = new ArrayBuffer<>(char[].class, bufferSize);
    }

    public SAXBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public boolean isAssumeMixedContent() {
        return assumeMixedContent;
    }

    public SAXBuffer assumeMixedContent(boolean assumeMixedContent) {
        this.assumeMixedContent = assumeMixedContent;
        return this;
    }

    public void removeTrailingCharacters() {
        while (!events.isEmpty() && events.peek() == CHARACTERS) {
            events.pop();
            characters.pop();
        }
    }

    public void trimToSize() {
        events.trimToSize();
        strings.trimToSize();
        characters.trimToSize();
    }

    public void reset() {
        events.clear();
        strings.clear();
        characters.clear();
        lastElement = UNDEFINED;
    }

    public void addStartDocument() {
        events.push(START_DOCUMENT);
    }

    public void addEndDocument() {
        events.push(END_DOCUMENT);
    }

    public void addNamespacePrefixMapping(String prefix, String uri) {
        events.push(NAMESPACE_PREFIX_MAPPING);
        strings.push(prefix);
        strings.push(uri);
    }

    public void addStartElement(String uri, String localName, String qName) {
        if (!assumeMixedContent && lastElement == START_ELEMENT)
            removeTrailingCharacters();

        events.push(START_ELEMENT);
        strings.push(uri);
        strings.push(localName);
        strings.push(qName.length() != localName.length() ? qName : "");
        lastElement = START_ELEMENT;
    }

    public void addAttribute(String uri, String localName, String qName, String type, String value) {
        if (lastElement == START_ELEMENT) {
            events.push(ATTRIBUTE);
            strings.push(uri);
            strings.push(localName);
            strings.push(qName.length() != localName.length() ? qName : "");
            strings.push(type);
            strings.push(value);
        }
    }

    public void addEndElement() {
        events.push(END_ELEMENT);
        lastElement = END_ELEMENT;
    }

    public void addCharacters(char[] chars) {
        if (assumeMixedContent || lastElement == START_ELEMENT) {
            events.push(CHARACTERS);
            characters.push(chars);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        addStartDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        addEndDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        addNamespacePrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        addStartElement(uri, localName, qName);
        for (int i = 0; i < attributes.getLength(); i++) {
            addAttribute(attributes.getURI(i),
                    attributes.getLocalName(i),
                    attributes.getQName(i),
                    attributes.getType(i),
                    attributes.getValue(i));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        addEndElement();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        char[] chars = new char[length];
        System.arraycopy(ch, start, chars, 0, length);
        addCharacters(chars);
    }

    public XMLStreamReader toXMLStreamReader(boolean release) {
        XMLStreamReader reader = new SAXStreamReader(events.iterator(release),
                strings.iterator(release),
                characters.iterator(release));

        if (release)
            lastElement = UNDEFINED;

        return reader;
    }

    public void send(ContentHandler handler, boolean release) throws SAXException {
        ArrayBufferIterator<Byte> events = this.events.iterator(release);
        ArrayBufferIterator<String> strings = this.strings.iterator(release);
        ArrayBufferIterator<char[]> characters = this.characters.iterator(release);

        AttributesImpl attributes = new AttributesImpl();
        ArrayBuffer<String> util = new ArrayBuffer<>(String.class, DEFAULT_BUFFER_SIZE);

        if (release)
            lastElement = UNDEFINED;

        while (events.hasNext()) {
            switch (events.next()) {
                case START_ELEMENT:
                    sendStartElement(handler, events, strings, util, attributes);
                    break;
                case END_ELEMENT:
                    sendEndElement(handler, util);
                    break;
                case CHARACTERS:
                    char[] ch = characters.next();
                    handler.characters(ch, 0, ch.length);
                    break;
                case NAMESPACE_PREFIX_MAPPING:
                    sendStartPrefixMapping(handler, strings, util);
                    break;
                case START_DOCUMENT:
                    handler.startDocument();
                    break;
                case END_DOCUMENT:
                    handler.endDocument();
                    break;
            }
        }
    }

    private void sendStartElement(ContentHandler handler, ArrayBufferIterator<Byte> events, ArrayBufferIterator<String> strings, ArrayBuffer<String> util, AttributesImpl attributes) throws SAXException {
        String uri = strings.next();
        String localName = strings.next();
        String qName = strings.next();
        if (qName.isEmpty())
            qName = localName;

        while (events.hasNext() && events.peek() == ATTRIBUTE) {
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

        handler.startElement(uri, localName, qName, attributes);

        util.push(uri);
        util.push(localName);
        util.push(qName);
        attributes.clear();
    }

    private void sendEndElement(ContentHandler handler, ArrayBuffer<String> util) throws SAXException {
        String qName = util.pop();
        String localName = util.pop();
        String uri = util.pop();

        handler.endElement(uri, localName, qName);

        while (END_PREFIX_MAPPING.equals(util.peek())) {
            util.pop();
            String prefix = util.pop();
            handler.endPrefixMapping(prefix);
        }
    }

    private void sendStartPrefixMapping(ContentHandler handler, ArrayBufferIterator<String> strings, ArrayBuffer<String> util) throws SAXException {
        String prefix = strings.next();
        String uri = strings.next();
        if (prefix == null)
            prefix = XMLConstants.DEFAULT_NS_PREFIX;

        handler.startPrefixMapping(prefix, uri);

        util.push(prefix);
        util.push(END_PREFIX_MAPPING);
    }
}
