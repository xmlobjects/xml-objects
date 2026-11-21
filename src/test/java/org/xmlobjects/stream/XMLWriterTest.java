/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.xml.Element;

import javax.xml.transform.dom.DOMResult;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers review item C7.
 */
class XMLWriterTest {
    private static final String BLDG = "http://www.opengis.net/citygml/building/2.0";
    private static final String GML = "http://www.opengis.net/gml";
    private static final String DEFAULT = "http://example.org/default";

    @Test
    void contentHandlerReceivesQualifiedNames() throws Exception {
        DOMResult result = new DOMResult();
        try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(result)) {
            writer.withPrefix("bldg", BLDG).withDefaultNamespace(DEFAULT);
            writer.writeStartDocument();
            writer.writeStartElement(Element.of(BLDG, "Building").addAttribute(GML, "id", "B1"));
            writer.writeElement(Element.of(GML, "name").addTextContent("x"));
            writer.writeElement(Element.of(DEFAULT, "inDefault"));
            writer.writeEndElement();
            writer.writeEndDocument();
        }

        org.w3c.dom.Element building = ((Document) result.getNode()).getDocumentElement();
        assertEquals("bldg:Building", building.getNodeName());
        assertEquals(BLDG, building.getNamespaceURI());

        org.w3c.dom.Element name = (org.w3c.dom.Element) building.getFirstChild();
        assertEquals(GML, name.getNamespaceURI());
        // gml was not declared up front, so a prefix is created; the attribute and element share it
        String gmlPrefix = building.getAttributeNodeNS(GML, "id").getPrefix();
        assertEquals(gmlPrefix + ":name", name.getNodeName());

        org.w3c.dom.Element inDefault = (org.w3c.dom.Element) name.getNextSibling();
        assertEquals("inDefault", inDefault.getNodeName());
        assertEquals(DEFAULT, inDefault.getNamespaceURI());
    }

    @Test
    void writeEndElementWithoutOpenElement() throws Exception {
        try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(new StringWriter())) {
            writer.writeStartDocument();
            assertThrows(XMLWriteException.class, writer::writeEndElement);
        }
    }
}
