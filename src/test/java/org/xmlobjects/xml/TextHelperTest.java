/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(TextContent.of("1.5 x 2").getAsDoubleList());
        assertNull(TextContent.of(" \n ").getAsDoubleList());
        assertNull(TextContent.absent().getAsDoubleList());
    }

    @Test
    void listGettersWithAndWithoutCachedTokens() {
        TextContent content = TextContent.of(" 1 2\t3 ");
        assertEquals(List.of(1, 2, 3), content.getAsIntegerList());
        assertEquals(List.of("1", "2", "3"), content.getAsList());
        // now parsed from the tokens cached by getAsList()
        assertEquals(List.of(1.0, 2.0, 3.0), content.getAsDoubleList());
        assertEquals(List.of(1, 2, 3), content.getAsIntegerList());

        assertEquals(List.of(true, false, true), TextContent.of("true 0 1").getAsBooleanList());
    }

    @Test
    void dateList() {
        List<OffsetDateTime> dates = TextContent.of(" 2024-01-31Z\n2024-02-29Z ").getAsDateList();
        assertEquals(List.of(
                OffsetDateTime.of(2024, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 29, 0, 0, 0, 0, ZoneOffset.UTC)), dates);
        assertNull(TextContent.of("2024-01-31 2024-13-01").getAsDateList());
    }

    @Test
    void infinity() {
        assertEquals(Double.POSITIVE_INFINITY, TextContent.of("INF").getAsDouble());
        assertEquals(Double.POSITIVE_INFINITY, TextContent.of(" +INF ").getAsDouble());
        assertEquals(Double.NEGATIVE_INFINITY, TextContent.of("-INF").getAsDouble());
        assertTrue(TextContent.of("NaN").getAsDouble().isNaN());
        assertNull(TextContent.of("inf").getAsDouble());
        assertEquals(List.of(1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
                TextContent.of("1 INF -INF").getAsDoubleList());

        assertEquals("INF", TextContent.ofDouble(Double.POSITIVE_INFINITY).get());
        assertEquals("-INF", TextContent.ofDouble(Double.NEGATIVE_INFINITY).get());
        assertEquals("NaN", TextContent.ofDouble(Double.NaN).get());
        assertEquals("1.0 INF -INF NaN", TextContent.ofDoubleList(Arrays.asList(
                1.0, Double.POSITIVE_INFINITY, null, Double.NEGATIVE_INFINITY, Double.NaN)).get());
    }

    @Test
    void doubleFormatUnchanged() {
        Random random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double value = (random.nextDouble() - 0.5) * Math.pow(10, random.nextInt(40) - 20);
            assertEquals(Double.toString(value), TextContent.ofDouble(value).get());
            assertEquals(Double.toString(value), TextContent.ofDoubleList(List.of(value)).get());
            assertEquals(value, TextContent.of(Double.toString(value)).getAsDoubleList().get(0));
        }
    }
}
