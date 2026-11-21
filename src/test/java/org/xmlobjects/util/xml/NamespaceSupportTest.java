/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NamespaceSupportTest {
    private static final String A = "http://example.org/a";
    private static final String B = "http://example.org/b";

    @Test
    void lookupsAcrossEmptyAndNonEmptyContexts() {
        NamespaceSupport support = new NamespaceSupport();

        // root element declares a
        support.pushContext();
        support.declarePrefix("a", A);
        support.requireNextContext();
        assertEquals("a", support.getPrefix(A));

        // child without declarations
        support.pushContext();
        support.requireNextContext();
        assertEquals("a", support.getPrefix(A));
        assertEquals(Set.of("a"), support.getPrefixes(A));
        assertEquals(Map.of(), support.getCurrentContext());

        // grandchild redeclares a with another prefix and declares b
        support.pushContext();
        support.declarePrefix("x", A);
        support.declarePrefix("b", B);
        support.requireNextContext();
        assertEquals("x", support.getPrefix(A));
        assertEquals("b", support.getPrefix(B));
        assertEquals(Set.of("x", "a"), support.getPrefixes(A));
        assertEquals(A, support.getNamespaceURI("x"));

        // leaving the grandchild restores the outer mappings
        support.popContext();
        assertEquals("a", support.getPrefix(A));
        assertNull(support.getPrefix(B));
        assertEquals(A, support.getNamespaceURI("a"));
        assertNull(support.getNamespaceURI("b"));

        support.popContext();
        support.popContext();
        assertNull(support.getPrefix(A));
    }
}
