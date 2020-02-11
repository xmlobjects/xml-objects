/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2020 Claus Nagel <claus.nagel@gmail.com>
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

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class StAXStream2SAX {
    private final ContentHandler handler;

    public StAXStream2SAX(ContentHandler handler) {
        this.handler = handler;
    }

    public void bridgeEvent(XMLStreamReader reader) throws SAXException {
        switch (reader.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                handleStartElement(reader);
                break;
            case XMLStreamConstants.END_ELEMENT:
                handleEndElement(reader);
                break;
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
                handleCharacters(reader);
                break;
        }
    }

    private void handleCharacters(XMLStreamReader reader) throws SAXException {
        handler.characters(reader.getText().toCharArray(), 0, reader.getTextLength());
    }

    private void handleEndElement(XMLStreamReader reader) throws SAXException {
        handler.endElement(getNamespaceURI(reader.getNamespaceURI()),
                reader.getLocalName(),
                getQName(reader.getPrefix(), reader.getLocalName()));

        for (int i = 0; i < reader.getNamespaceCount(); i++)
            handler.endPrefixMapping(reader.getNamespacePrefix(i));
    }

    private void handleStartElement(XMLStreamReader reader) throws SAXException {
        for (int i = 0; i < reader.getNamespaceCount(); i++)
            handler.startPrefixMapping(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));

        handler.startElement(getNamespaceURI(reader.getNamespaceURI()),
                reader.getLocalName(),
                getQName(reader.getPrefix(), reader.getLocalName()),
                getAttributes(reader));
    }

    private Attributes getAttributes(XMLStreamReader reader) {
        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributes.addAttribute(getNamespaceURI(reader.getAttributeNamespace(i)),
                    reader.getAttributeLocalName(i),
                    getQName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i)),
                    reader.getAttributeType(i),
                    reader.getAttributeValue(i));
        }

        return attributes;
    }

    private String getNamespaceURI(String namespaceURI) {
        return namespaceURI != null ? namespaceURI : XMLConstants.NULL_NS_URI;
    }

    private String getQName(String prefix, String localName) {
        return prefix != null && !prefix.isEmpty() ? prefix + ":" + localName : localName;
    }
}
