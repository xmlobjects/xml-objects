/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects;

import org.junit.jupiter.api.Test;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.xml.Namespaces;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Covers review item C8: serializer lookup must be deterministic if several namespaces match.
 */
class XMLObjectsTest {

    static class Foo {
    }

    @Test
    void firstRegisteredSerializerWins() throws Exception {
        XMLObjects xmlObjects = XMLObjects.newInstance();
        List<String> namespaceURIs = new ArrayList<>();
        List<ObjectSerializer<Foo>> serializers = new ArrayList<>();

        // register different serializers for the same type under many namespaces, in reverse order,
        // so that registration order differs from any hash order
        for (int i = 20; i > 0; i--) {
            String namespaceURI = "http://example.org/ns/" + i;
            ObjectSerializer<Foo> serializer = new ObjectSerializer<>() {
            };

            xmlObjects.registerSerializer(serializer, Foo.class, namespaceURI);
            namespaceURIs.add(namespaceURI);
            serializers.add(serializer);
        }

        assertSame(serializers.get(0), xmlObjects.getSerializer(Foo.class, Namespaces.of(namespaceURIs)));
        assertSame(serializers.get(5), xmlObjects.getSerializer(Foo.class,
                Namespaces.of(namespaceURIs.subList(5, namespaceURIs.size()))));
    }

    @Test
    void sameInstanceForSeveralNamespaces() throws Exception {
        XMLObjects xmlObjects = XMLObjects.newInstance();
        ObjectSerializer<Foo> serializer = new ObjectSerializer<>() {
        };

        xmlObjects.registerSerializer(serializer, Foo.class, "http://example.org/v1");
        xmlObjects.registerSerializer(serializer, Foo.class, "http://example.org/v2");

        assertSame(serializer, xmlObjects.getSerializer(Foo.class, Namespaces.of("http://example.org/v2")));
        assertSame(serializer, xmlObjects.getSerializer(Foo.class,
                Namespaces.of("http://example.org/v1", "http://example.org/v2")));
    }
}
