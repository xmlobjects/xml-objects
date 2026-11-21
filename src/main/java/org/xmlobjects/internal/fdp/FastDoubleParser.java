/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.internal.fdp;

/**
 * Entry point to the bundled parts of FastDoubleParser for internal use by xml-objects.
 * <p>
 * The other classes in this package are taken unchanged, except for the package name, from
 * <a href="https://github.com/wrandelshofer/FastDoubleParser">FastDoubleParser</a> by Werner Randelshofer,
 * release v2.0.1 (commit 2e1235766719b2db4f24c5c28920012850333833), using the Java 21 variant of
 * {@code FastDoubleSwar}. Only the classes required for parsing a {@code double} from a {@link CharSequence}
 * are included. FastDoubleParser is licensed under the MIT License; see {@code META-INF/FastDoubleParser-LICENSE},
 * {@code META-INF/FastDoubleParser-NOTICE} and {@code META-INF/FastDoubleParser-thirdparty-LICENSE}.
 * <p>
 * This class mirrors {@code ch.randelshofer.fastdoubleparser.JavaDoubleParser.parseDouble(CharSequence, int, int)},
 * which is not bundled since it also references the parsers for byte and char arrays.
 * <p>
 * This package is not exported and not part of the public API of xml-objects.
 */
public final class FastDoubleParser {
    private static final JavaDoubleBitsFromCharSequence CHAR_SEQUENCE_PARSER = new JavaDoubleBitsFromCharSequence();

    private FastDoubleParser() {
    }

    /**
     * Parses a Java {@code FloatingPointLiteral} like {@link Double#parseDouble(String)}, but from a section
     * of a {@link CharSequence} and without creating a substring.
     *
     * @param str    the character sequence
     * @param offset the start of the literal
     * @param length the length of the literal
     * @return the parsed value
     * @throws NumberFormatException if the literal cannot be parsed
     */
    public static double parseDouble(CharSequence str, int offset, int length) throws NumberFormatException {
        long bitPattern = CHAR_SEQUENCE_PARSER.parseFloatingPointLiteral(str, offset, length);
        if (bitPattern == AbstractNumberParser.SYNTAX_ERROR_BITS) {
            throw new NumberFormatException(AbstractNumberParser.SYNTAX_ERROR);
        }

        return Double.longBitsToDouble(bitPattern);
    }
}
