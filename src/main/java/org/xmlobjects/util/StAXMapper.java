package org.xmlobjects.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class StAXMapper {
    private final ContentHandler handler;

    public StAXMapper(ContentHandler buffer) {
        this.handler = buffer;
    }

    public void mapEvent(XMLStreamReader reader) throws SAXException {
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
    }

    private void handleStartElement(XMLStreamReader reader) throws SAXException {
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
        return prefix != null ? prefix + ":" + localName : localName;
    }
}
