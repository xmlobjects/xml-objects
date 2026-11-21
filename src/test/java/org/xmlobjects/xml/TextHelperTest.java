/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextHelperTest {

    @Test
    void tokenize() {
        assertArrayEquals(new String[0], TextHelper.tokenize(""));
        assertArrayEquals(new String[0], TextHelper.tokenize(" \t\r\n "));
        assertArrayEquals(new String[]{"a"}, TextHelper.tokenize("a"));
        assertArrayEquals(new String[]{"a"}, TextHelper.tokenize("  a  "));
        assertArrayEquals(new String[]{"a", "bc", "d"}, TextHelper.tokenize("a bc\t\r\n  d"));
        assertArrayEquals(new String[]{"1.5", "2.5", "3.5"}, TextHelper.tokenize("\n  1.5 2.5\n  3.5\n"));
    }

    @Test
    void doubleList() {
        assertEquals(List.of(1.5, -2.0, 3.25e6), TextContent.of(" 1.5  -2\n3.25E6 ").getAsDoubleList());
    }
}
