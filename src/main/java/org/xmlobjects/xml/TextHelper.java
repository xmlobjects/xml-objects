/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2025 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmlobjects.xml;

public class TextHelper {

    static String normalize(String value) {
        int length = value.length();
        if (length != 0) {
            char[] chars = value.toCharArray();
            for (int i = 0; i < length; i++) {
                if (Character.isWhitespace(chars[i])) {
                    chars[i] = ' ';
                }
            }

            value = new String(chars);
        }

        return value;
    }

    static String collapse(String value) {
        int length = value.length();
        if (length != 0) {
            int i = 0;
            while (i < length && Character.isWhitespace(value.charAt(i))) {
                i++;
            }

            if (i != length) {
                StringBuilder collapsed = new StringBuilder(length - i);
                char ch = value.charAt(i);
                collapsed.append(ch);

                boolean isWhiteSpace = false;
                for (i += 1; i < length; i++) {
                    ch = value.charAt(i);
                    if (Character.isWhitespace(ch)) {
                        isWhiteSpace = true;
                    } else {
                        if (isWhiteSpace) {
                            collapsed.append(' ');
                            isWhiteSpace = false;
                        }

                        collapsed.append(ch);
                    }
                }

                value = collapsed.toString();
            } else {
                value = "";
            }
        }

        return value;
    }

    static String[] tokenizeContent(String value) {
        int length = value.length();
        String[] tokens = new String[(length / 2) + 1];
        int noOfTokens = 0;
        int current = -1;
        int next;

        do {
            next = nextWhiteSpace(value, current + 1, length);
            if (next != current + 1) {
                tokens[noOfTokens++] = value.substring(current + 1, next);
            }

            current = next;
        } while (next != length);

        String[] tokenizedContent = new String[noOfTokens];
        System.arraycopy(tokens, 0, tokenizedContent, 0, noOfTokens);
        return tokenizedContent;
    }

    static int nextWhiteSpace(String value, int pos, int length) {
        while (pos < length && !Character.isWhitespace(value.charAt(pos))) {
            pos++;
        }

        return pos;
    }
}
