/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.stream.XMLOutput;

import javax.xml.XMLConstants;
import java.util.Map;
import java.util.stream.Collectors;

public class SAXOutputHandler extends XMLOutput<SAXOutputHandler> {
    private int depth;

    public SAXOutputHandler(ContentHandler parent) {
        super(parent);
    }

    @Override
    public String getIndent() {
        return null;
    }

    @Override
    public boolean isWriteXMLDeclaration() {
        return false;
    }

    @Override
    public String[] getHeaderComment() {
        return null;
    }

    @Override
    public void flush() throws Exception {
        // nothing to do
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        prefixMapping.pushContext();

        if (depth == 0) {
            if (!schemaLocations.isEmpty()) {
                atts = new AttributesImpl(atts);
                ((AttributesImpl) atts).addAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation",
                        prefixMapping.getPrefix(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI) +
                                ":schemaLocation", "CDATA", schemaLocations.entrySet().stream()
                                .map(e -> e.getKey() + " " + e.getValue())
                                .collect(Collectors.joining(" ")));
            }

            for (Map.Entry<String, String> entry : prefixMapping.getCurrentContext().entrySet()) {
                super.startPrefixMapping(entry.getValue(), entry.getKey());
            }
        }

        super.startElement(uri, localName, qName, atts);
        prefixMapping.requireNextContext();
        depth++;
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        prefixMapping.pushContext();
        prefixMapping.declarePrefix(prefix, namespaceURI);
        super.startPrefixMapping(prefix, namespaceURI);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        depth--;
        super.endElement(uri, localName, qName);

        if (depth == 0) {
            for (Map.Entry<String, String> entry : prefixMapping.getCurrentContext().entrySet()) {
                super.endPrefixMapping(entry.getValue());
            }
        }

        prefixMapping.popContext();
    }

    @Override
    protected SAXOutputHandler self() {
        return this;
    }
}
