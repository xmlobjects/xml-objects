/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.stream.XMLTestSupport.Node;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Covers review items C2 (supplementary characters) and C3 (escaping of attribute and text values).
 */
class EscapingRoundTripTest {
    private static final String NS = "http://example.org/test";

    private static final String ATTRIBUTE_VALUE = "a\nb\tc\r\nd \"q\" 'a' <x> & y";
    private static final String TEXT_VALUE = "line1\r\nline2\rline3 <tag> & ]]> \"q\" \t tab";
    private static final String UNICODE_VALUE = "äöü € 中文 😀 𠀋 end";

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII"})
    void roundTrip(String encoding) throws Exception {
        Node expected = new Node(new QName(NS, "root"))
                .attribute(new QName("plain"), ATTRIBUTE_VALUE)
                .attribute(new QName(NS, "unicode"), UNICODE_VALUE)
                .child(new Node(new QName(NS, "text")).text(TEXT_VALUE))
                .child(new Node(new QName(NS, "unicode")).text(UNICODE_VALUE));

        byte[] xml = write(expected, encoding);

        assertEquals(expected, XMLTestSupport.parse(xml));
        String raw = new String(xml, Charset.forName(encoding)).toLowerCase();
        assertFalse(raw.contains("&#xd83d;"), "surrogate halves must not be written as character references");
        assertFalse(raw.contains("&#xd840;"), "surrogate halves must not be written as character references");
    }

    private byte[] write(Node node, String encoding) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance(XMLObjects.newInstance());
        try (XMLWriter writer = factory.createWriter(out, encoding)) {
            writer.writeStartDocument();
            XMLTestSupport.write(writer, node);
            writer.writeEndDocument();
        }

        return out.toByteArray();
    }
}
