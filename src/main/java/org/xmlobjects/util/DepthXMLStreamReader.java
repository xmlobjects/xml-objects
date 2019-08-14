package org.xmlobjects.util;

import org.xmlobjects.schema.AbstractSchemaHandler;
import org.xmlobjects.schema.SchemaHandlerException;
import org.xmlobjects.xml.Namespaces;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Objects;

public class DepthXMLStreamReader implements XMLStreamReader {
    private final XMLStreamReader reader;
    private final URI baseURI;
    private final Namespaces namespaces;

    private AbstractSchemaHandler schemaHandler;
    private int depth;

    public DepthXMLStreamReader(XMLStreamReader reader, URI baseURI) {
        this.reader = Objects.requireNonNull(reader, "XML stream reader must not be null.");
        this.baseURI = Objects.requireNonNull(baseURI, "The base URI must not be null.");
        namespaces = Namespaces.newInstance();
    }

    public DepthXMLStreamReader(XMLStreamReader reader) {
        this(reader, URI.create(""));
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public Namespaces getNamespaces() {
        return namespaces;
    }

    public AbstractSchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public void setSchemaHandler(AbstractSchemaHandler schemaHandler) {
        this.schemaHandler = schemaHandler;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return reader.getProperty(name);
    }

    @Override
    public int next() throws XMLStreamException {
        int event = reader.next();
        if (event == START_ELEMENT) {
            for (int i = 0; i < reader.getNamespaceCount(); i++)
                namespaces.add(reader.getNamespaceURI(i));

            if (schemaHandler != null) {
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(reader.getAttributeNamespace(i))) {
                        try {
                            switch (reader.getAttributeLocalName(i)) {
                                case "schemaLocation":
                                    String[] schemaLocations = reader.getAttributeValue(i).split("\\s+");
                                    if (schemaLocations.length % 2 == 0) {
                                        for (int j = 0; j < schemaLocations.length; j += 2)
                                            schemaHandler.parseSchema(schemaLocations[j], baseURI.resolve(schemaLocations[j + 1]).toString());
                                    }
                                    break;
                                case "noNamespaceSchemaLocation":
                                    schemaHandler.parseSchema(XMLConstants.NULL_NS_URI, reader.getAttributeValue(i));
                                    break;
                            }
                        } catch (SchemaHandlerException e) {
                            throw new XMLStreamException("Caused by: ", e);
                        }
                    }
                }
            }

            depth++;
        } else if (event == END_ELEMENT)
            depth--;

        return event;
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        reader.require(type,namespaceURI,localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        String text = reader.getElementText();
        while (reader.getEventType() != XMLStreamConstants.END_ELEMENT)
            reader.next();

        depth--;
        return text;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int event = next();
        while ((event == XMLStreamConstants.CHARACTERS && isWhiteSpace())
                || (event == XMLStreamConstants.CDATA && isWhiteSpace())
                || event == XMLStreamConstants.SPACE
                || event == XMLStreamConstants.PROCESSING_INSTRUCTION
                || event == XMLStreamConstants.COMMENT)
            event = next();

        if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT)
            throw new XMLStreamException("Expected start or end tag.", getLocation());

        return event;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }

    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return reader.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
        return reader.isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return reader.isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return reader.isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return reader.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    @Override
    public QName getAttributeName(int index) {
        return reader.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
        return reader.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(int index) {
        return reader.getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        return reader.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
        return reader.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(int index) {
        return reader.getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return reader.isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        return reader.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI(int index) {
        return reader.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }

    @Override
    public int getEventType() {
        return reader.getEventType();
    }

    @Override
    public String getText() {
        return reader.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return reader.getTextCharacters(sourceStart,target,targetStart,length);
    }

    @Override
    public int getTextStart() {
        return reader.getTextStart();
    }

    @Override
    public int getTextLength() {
        return reader.getTextLength();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public boolean hasText() {
        return reader.hasText();
    }

    @Override
    public Location getLocation() {
        return reader.getLocation();
    }

    @Override
    public QName getName() {
        return reader.getName();
    }

    @Override
    public String getLocalName() {
        return reader.getLocalName();
    }

    @Override
    public boolean hasName() {
        return reader.hasName();
    }

    @Override
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return reader.getPrefix();
    }

    @Override
    public String getVersion() {
        return reader.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    @Override
    public String getPITarget() {
        return reader.getPITarget();
    }

    @Override
    public String getPIData() {
        return reader.getPIData();
    }
}
