/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributesTest {
    private static final String GML = "http://www.opengis.net/gml";
    private static final String XLINK = "http://www.w3.org/1999/xlink";

    @Test
    void addAndLookup() {
        Attributes attributes = new Attributes();
        assertTrue(attributes.isEmpty());

        attributes.add(GML, "id", "B1");
        attributes.add("srsDimension", "3");
        attributes.add(null, "uom", "m");
        attributes.add(new QName(XLINK, "href"), "#a");
        for (int i = 0; i < 10; i++) {
            attributes.add("a" + i, String.valueOf(i));
        }

        // replaces the existing value
        attributes.add(GML, "id", "B2");

        assertFalse(attributes.isEmpty());
        assertEquals("B2", attributes.getValue(GML, "id").get());
        assertEquals("3", attributes.getValue("srsDimension").get());
        assertEquals("m", attributes.getValue("", "uom").get());
        assertEquals("m", attributes.getValue(null, "uom").get());
        assertEquals("#a", attributes.getValue(new QName(XLINK, "href")).get());
        assertEquals("9", attributes.getValue("a9").get());
        assertTrue(attributes.getValue(GML, "name").isAbsent());
        assertTrue(attributes.getValue(XLINK, "id").isAbsent());
        assertTrue(attributes.contains(GML, "id"));
        assertTrue(attributes.contains("srsDimension"));
        assertFalse(attributes.contains("id"));
    }

    @Test
    void forEachKeepsInsertionOrder() {
        Attributes attributes = new Attributes();
        attributes.add(GML, "id", "B1");
        attributes.add("srsDimension", "3");
        attributes.add(XLINK, "href", "#a");
        attributes.add(GML, "id", "B2");

        List<String> visited = new ArrayList<>();
        attributes.forEach((namespaceURI, localName, value) -> visited.add(namespaceURI + "|" + localName + "=" + value.get()));
        assertEquals(List.of(GML + "|id=B2", "|srsDimension=3", XLINK + "|href=#a"), visited);
    }

    @Test
    void getReturnsLiveMaps() {
        Attributes attributes = new Attributes();
        attributes.add(GML, "id", "B1");
        attributes.add("srsDimension", "3");

        Map<String, Map<String, TextContent>> map = attributes.get();
        assertEquals(Map.of(GML, Map.of("id", attributes.getValue(GML, "id")),
                "", Map.of("srsDimension", attributes.getValue("srsDimension"))), map);

        // changes through the returned maps are visible, and the object keeps working afterwards
        map.get(GML).put("name", TextContent.of("x"));
        map.remove("");
        assertEquals("x", attributes.getValue(GML, "name").get());
        assertFalse(attributes.contains("srsDimension"));

        attributes.add(XLINK, "href", "#a");
        assertEquals("#a", attributes.get(XLINK).get("href").get());
        attributes.get(XLINK).put("title", TextContent.of("t"));
        assertEquals("t", attributes.getValue(XLINK, "title").get());

        List<String> visited = new ArrayList<>();
        attributes.forEach((namespaceURI, localName, value) -> visited.add(localName));
        assertEquals(4, visited.size());
        assertTrue(visited.containsAll(List.of("id", "name", "href", "title")));
    }

    @Test
    void addAll() {
        Attributes attributes = new Attributes();
        attributes.addAll(GML, Map.of("id", TextContent.of("B1"), "name", TextContent.of("n")));
        assertEquals("B1", attributes.getValue(GML, "id").get());
        assertEquals("n", attributes.getValue(GML, "name").get());
    }

    @Test
    void empty() {
        Attributes empty = Attributes.empty();
        assertTrue(empty.isEmpty());
        assertTrue(empty.isImmutable());
        assertEquals(Map.of(), empty.get());
        assertEquals(Map.of(), empty.get(GML));
        assertTrue(empty.getValue(GML, "id").isAbsent());
        assertThrows(UnsupportedOperationException.class, () -> empty.add("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> empty.addAll(GML, Map.of()));
        assertThrows(UnsupportedOperationException.class, () -> empty.get().put(GML, Map.of()));
        empty.forEach((namespaceURI, localName, value) -> fail());
        assertFalse(new Attributes().isImmutable());
    }
}
