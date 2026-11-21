/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import org.junit.jupiter.api.Test;
import org.xmlobjects.schema.SchemaHandler;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers review item C5: handling of xsi:schemaLocation and xsi:noNamespaceSchemaLocation.
 */
class DepthXMLStreamReaderTest {
    private static final URI BASE = URI.create("http://example.org/data/city.gml");

    @Test
    void schemaLocations() throws Exception {
        List<String> parsed = parse("""
                <root xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="
                          http://example.org/a  a.xsd
                          http://example.org/b  ..\\schemas\\b.xsd
                      ">
                  <child xsi:noNamespaceSchemaLocation="  c.xsd "/>
                </root>""");

        assertEquals(List.of(
                "http://example.org/a -> http://example.org/data/a.xsd",
                "http://example.org/b -> http://example.org/schemas/b.xsd",
                " -> http://example.org/data/c.xsd"), parsed);
    }

    @Test
    void invalidSchemaLocation() {
        assertThrows(XMLStreamException.class, () -> parse("""
                <root xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:noNamespaceSchemaLocation="http://exa mple.org/%%"/>"""));
    }

    private List<String> parse(String xml) throws XMLStreamException {
        List<String> parsed = new ArrayList<>();
        XMLStreamReader stream = SecureXMLProcessors.newDefaultXMLInputFactory().createXMLStreamReader(new StringReader(xml));
        DepthXMLStreamReader reader = new DepthXMLStreamReader(stream, BASE);
        reader.setSchemaHandler(new SchemaHandler() {
            @Override
            public void parseSchema(String namespaceURI, String schemaLocation) {
                parsed.add(namespaceURI + " -> " + schemaLocation);
            }
        });

        while (reader.hasNext()) {
            reader.next();
        }

        return parsed;
    }
}
