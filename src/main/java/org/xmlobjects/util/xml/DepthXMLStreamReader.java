/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import org.xmlobjects.schema.SchemaHandler;
import org.xmlobjects.schema.SchemaHandlerException;
import org.xmlobjects.xml.Namespaces;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import java.net.URI;
import java.util.Objects;

public class DepthXMLStreamReader extends StreamReaderDelegate {
    private final URI baseURI;
    private final Namespaces namespaces;

    private SchemaHandler schemaHandler;
    private int depth;
    private int state;

    public DepthXMLStreamReader(XMLStreamReader reader, URI baseURI) {
        super(Objects.requireNonNull(reader, "XML stream reader must not be null."));
        this.baseURI = Objects.requireNonNull(baseURI, "The base URI must not be null.");
        namespaces = Namespaces.newInstance();
    }

    public DepthXMLStreamReader(XMLStreamReader reader) {
        this(reader, URI.create(""));
    }

    public XMLStreamReader getReader() {
        return getParent();
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public Namespaces getNamespaces() {
        return namespaces;
    }

    public SchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public void setSchemaHandler(SchemaHandler schemaHandler) {
        this.schemaHandler = schemaHandler;
    }

    public int getDepth() {
        return depth;
    }

    public int getState() {
        return state;
    }

    @Override
    public int next() throws XMLStreamException {
        XMLStreamReader reader = getParent();
        int event = reader.next();
        state++;

        if (event == START_ELEMENT) {
            for (int i = 0, count = reader.getNamespaceCount(); i < count; i++) {
                namespaces.add(reader.getNamespaceURI(i));
            }

            if (schemaHandler != null) {
                for (int i = 0, count = reader.getAttributeCount(); i < count; i++) {
                    if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(reader.getAttributeNamespace(i))) {
                        try {
                            switch (reader.getAttributeLocalName(i)) {
                                case "schemaLocation" -> {
                                    String[] schemaLocations = reader.getAttributeValue(i).trim().split("\\s+");
                                    if (schemaLocations.length % 2 == 0) {
                                        for (int j = 0; j < schemaLocations.length; j += 2) {
                                            schemaHandler.parseSchema(schemaLocations[j], resolve(schemaLocations[j + 1]));
                                        }
                                    }
                                }
                                case "noNamespaceSchemaLocation" -> schemaHandler.parseSchema(XMLConstants.NULL_NS_URI,
                                        resolve(reader.getAttributeValue(i).trim()));
                                default -> {
                                }
                            }
                        } catch (SchemaHandlerException e) {
                            throw new XMLStreamException("Caused by: ", e);
                        }
                    }
                }
            }

            depth++;
        } else if (event == END_ELEMENT) {
            depth--;
        }

        return event;
    }

    private String resolve(String schemaLocation) throws XMLStreamException {
        try {
            return baseURI.resolve(schemaLocation.replace('\\', '/')).toString();
        } catch (IllegalArgumentException e) {
            throw new XMLStreamException("Invalid schema location '" + schemaLocation + "'.", getLocation(), e);
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        String text = super.getElementText();
        state++;
        depth--;
        return text;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        // must not delegate, since the parent would bypass next()
        int event = next();
        while ((event == XMLStreamConstants.CHARACTERS && isWhiteSpace())
                || (event == XMLStreamConstants.CDATA && isWhiteSpace())
                || event == XMLStreamConstants.SPACE
                || event == XMLStreamConstants.PROCESSING_INSTRUCTION
                || event == XMLStreamConstants.COMMENT) {
            event = next();
        }

        if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Expected start or end tag.", getLocation());
        }

        return event;
    }
}
