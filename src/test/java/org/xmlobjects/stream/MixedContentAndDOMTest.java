/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.api.Test;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.xml.Element;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers review item C6: mixed content and DOM elements are read and written with the secure processors.
 */
class MixedContentAndDOMTest {

    @Test
    void roundTrip() throws Exception {
        String xml = "<root>" +
                "<p>a <b>bold</b> &amp; c</p>" +
                "<x:any xmlns:x=\"urn:x\" x:attr=\"v\"><x:y>1</x:y></x:any>" +
                "</root>";

        XMLObjects xmlObjects = XMLObjects.newInstance();
        String mixedContent;
        org.w3c.dom.Element any;
        try (XMLReader reader = XMLReaderFactory.newInstance(xmlObjects).createReader(new StringReader(xml))) {
            reader.nextTag();
            reader.nextTag();
            mixedContent = reader.getMixedContent();
            reader.nextTag();
            any = reader.getDOMElement();
        }

        assertEquals("a <b>bold</b> &amp; c", mixedContent);
        assertEquals("urn:x", any.getNamespaceURI());
        assertEquals("any", any.getLocalName());

        StringWriter out = new StringWriter();
        try (XMLWriter writer = XMLWriterFactory.newInstance(xmlObjects).createWriter(out)) {
            writer.writeXMLDeclaration(false);
            writer.writeStartDocument();
            writer.writeStartElement(Element.of("root"));
            writer.writeStartElement(Element.of("p"));
            writer.writeMixedContent(mixedContent);
            writer.writeEndElement();
            writer.writeDOMElement(any);
            writer.writeEndDocument();
        }

        assertEquals(XMLTestSupport.parse(xml.getBytes()), XMLTestSupport.parse(out.toString().getBytes()));
    }

    @Test
    void doctypeInMixedContentIsRejected() throws Exception {
        try (XMLWriter writer = XMLWriterFactory.newInstance(XMLObjects.newInstance()).createWriter(new StringWriter())) {
            writer.writeStartDocument();
            writer.writeStartElement(Element.of("root"));
            assertThrows(XMLWriteException.class, () -> writer.writeMixedContent(
                    "</dummy><!DOCTYPE d [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><dummy>&e;"));
        }
    }
}
