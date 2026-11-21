/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parsing doubles via the bundled FastDoubleParser must give exactly the same results as the previous
 * implementation based on {@link Double#parseDouble(String)} with xs:double INF handling.
 */
class DoubleParsingTest {

    private static Double reference(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return switch (value) {
                case "INF", "+INF" -> Double.POSITIVE_INFINITY;
                case "-INF" -> Double.NEGATIVE_INFINITY;
                default -> null;
            };
        }
    }

    private static void assertSameAsReference(String value) {
        Double expected = reference(value);
        Double single = TextContent.of(value).getAsDouble();
        List<Double> list = TextContent.of(value).getAsDoubleList();

        assertEquals(expected == null ? null : Double.doubleToRawLongBits(expected),
                single == null ? null : Double.doubleToRawLongBits(single), "getAsDouble(\"" + value + "\")");
        assertEquals(expected == null ? null : Double.doubleToRawLongBits(expected),
                list == null ? null : Double.doubleToRawLongBits(list.get(0)), "getAsDoubleList(\"" + value + "\")");
    }

    @Test
    void edgeCases() {
        for (String value : List.of(
                "0", "-0", "0.0", "-0.0", "+1", ".5", "5.", "1e5", "1E-5", "1e+5", "1.5e308", "1e309", "-1e309",
                "1e-400", "4.9e-324", "2.2250738585072014E-308", "1.7976931348623157E308", "9007199254740993",
                "0.1", "0.30000000000000004", "123456789012345678901234567890", "1.2910969084704E-7",
                "0x1p3", "0x1.8p1", "-0x1P-2", "1d", "1D", "1f", "1F", "2.5d",
                "NaN", "-NaN", "Infinity", "-Infinity", "+Infinity", "INF", "+INF", "-INF", "inf", "infinity",
                "", ".", "e5", "1e", "1..2", "1e5.5", "--1", "+-1", "0x", "0xg", "1_000", "１", "abc")) {
            assertSameAsReference(value);
        }
    }

    @Test
    void randomValues() {
        Random random = new Random(42);
        for (int i = 0; i < 1_000_000; i++) {
            double value = switch (i % 4) {
                case 0 -> random.nextDouble() * Math.pow(10, random.nextInt(40) - 20);
                case 1 -> Double.longBitsToDouble(random.nextLong());
                case 2 -> random.nextInt(1_000_000) / 1000.0;
                default -> (random.nextDouble() - 0.5) * 1e7;
            };

            String text = i % 3 == 0 ? String.format("%.15g", value) : Double.toString(value);
            assertSameAsReference(text);
        }
    }

    @Test
    void listsWithMixedContent() {
        assertEquals(List.of(1.0, Double.POSITIVE_INFINITY, -0.5, 3.0),
                TextContent.of("\n  1 INF\t-.5   0x1.8p1\n").getAsDoubleList());
        assertEquals(null, TextContent.of("1 2 x 3").getAsDoubleList());

        List<Double> expected = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Random random = new Random(7);
        for (int i = 0; i < 10_000; i++) {
            double value = random.nextDouble() * 1e6;
            expected.add(value);
            text.append(value).append(i % 7 == 0 ? "\n    " : " ");
        }

        assertEquals(expected, TextContent.of(text.toString()).getAsDoubleList());
    }
}
