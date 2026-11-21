/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.internal;

import org.xmlobjects.xml.TextContent;

import java.util.function.Predicate;

/**
 * Gives classes of xml-objects access to internal state of classes in other packages without making
 * that state part of the public API. This package is not exported.
 */
public final class InternalAccess {
    private static Predicate<TextContent> markupFree = content -> false;

    private InternalAccess() {
    }

    /**
     * Called once by {@link TextContent} during class initialization.
     */
    public static void setMarkupFree(Predicate<TextContent> markupFree) {
        InternalAccess.markupFree = markupFree;
    }

    /**
     * Returns whether the text content is known to contain no characters that must be escaped in XML,
     * because it was created from a number, boolean, date or similar value.
     */
    public static boolean isMarkupFree(TextContent content) {
        return markupFree.test(content);
    }
}
