/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.api.Test;
import org.xmlobjects.XMLObjects;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers review item R1: {@link XMLReader#getTextContent()} for single and multiple text events.
 */
class XMLReaderTest {

    @Test
    void textContent() throws Exception {
        String xml = "<root>" +
                "<single>one event</single>" +
                "<empty/>" +
                "<multi>a<!-- comment -->b<![CDATA[<c>]]>d&amp;e</multi>" +
                "<long>" + "1.5 2.5 3.5 ".repeat(20_000) + "</long>" +
                "<afterLong>x</afterLong>" +
                "<mixed>before<child/>after</mixed>" +
                "</root>";

        XMLReaderFactory factory = XMLReaderFactory.newInstance(XMLObjects.newInstance());
        try (XMLReader reader = factory.createReader(new StringReader(xml))) {
            reader.nextTag();
            assertEquals("one event", nextText(reader));
            assertEquals("", nextText(reader));
            assertEquals("ab<c>d&e", nextText(reader));
            assertEquals("1.5 2.5 3.5 ".repeat(20_000), nextText(reader));
            assertEquals("x", nextText(reader));
            // text up to the first child element
            assertEquals("before", nextText(reader));
        }
    }

    private String nextText(XMLReader reader) throws XMLReadException {
        EventType event;
        while ((event = reader.nextTag()) != EventType.START_ELEMENT) {
            assertEquals(EventType.END_ELEMENT, event);
        }

        return reader.getTextContent().get();
    }
}
