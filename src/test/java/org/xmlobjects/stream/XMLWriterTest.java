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
    void markupFreeTextContentIsWrittenWithoutEscaping() throws Exception {
        for (String encoding : new String[]{"UTF-8", "ISO-8859-1"}) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(out, encoding)) {
                writer.writeStartDocument();
                writer.writeStartElement(Element.of("root"));
                writer.writeElement(Element.of("doubles").addTextContent(org.xmlobjects.xml.TextContent.ofDoubleList(
                        java.util.List.of(1.5, -2.0, Double.POSITIVE_INFINITY, 1.0E-7))));
                writer.writeElement(Element.of("integer").addTextContent(org.xmlobjects.xml.TextContent.ofInteger(-42)));
                writer.writeElement(Element.of("date").addTextContent(org.xmlobjects.xml.TextContent.ofDate(
                        java.time.OffsetDateTime.of(2024, 2, 29, 0, 0, 0, 0, java.time.ZoneOffset.UTC))));
                // strings must still be escaped, also when written after markup-free content
                writer.writeElement(Element.of("text").addTextContent("1 < 2 & 3 > 2"));
                writer.writeEndDocument();
            }

            String xml = out.toString(encoding);
            org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<doubles>1.5 -2.0 INF 1.0E-7</doubles>"), xml);
            org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<integer>-42</integer>"), xml);
            org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<date>2024-02-29</date>"), xml);
            org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<text>1 &lt; 2 &amp; 3 &gt; 2</text>"), xml);
        }
    }

    @Test
    void escapeCharactersSettingIsKept() throws Exception {
        StringWriter out = new StringWriter();
        org.xmlobjects.util.xml.SAXWriter saxWriter = new org.xmlobjects.util.xml.SAXWriter(out).escapeCharacters(false);
        try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(saxWriter)) {
            writer.writeStartDocument();
            writer.writeStartElement(Element.of("root"));
            writer.writeElement(Element.of("integer").addTextContent(org.xmlobjects.xml.TextContent.ofInteger(1)));
            writer.writeEndDocument();
        }

        org.junit.jupiter.api.Assertions.assertFalse(saxWriter.isEscapeCharacters());
    }

    @Test
    void writeEndElementWithoutOpenElement() throws Exception {
        try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(new StringWriter())) {
            writer.writeStartDocument();
            assertThrows(XMLWriteException.class, writer::writeEndElement);
        }
    }
}
