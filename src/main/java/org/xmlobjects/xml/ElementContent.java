/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2024 Claus Nagel <claus.nagel@gmail.com>
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

import java.util.Objects;

public class ElementContent {
    private final static ElementContent EMPTY = new ElementContent(null, null);

    private final TextContent textContent;
    private final Element element;

    private ElementContent(TextContent textContent, Element element) {
        this.textContent = textContent;
        this.element = element;
    }

    public static ElementContent of(TextContent textContent) {
        return new ElementContent(Objects.requireNonNull(textContent, "Text content must not be null."), null);
    }

    public static ElementContent of(Element element) {
        return new ElementContent(null, Objects.requireNonNull(element, "XML element must not be null."));
    }

    public static ElementContent empty() {
        return EMPTY;
    }

    public boolean isSetTextContent() {
        return textContent != null;
    }

    public TextContent getTextContent() {
        return textContent;
    }

    public boolean isSetElement() {
        return element != null;
    }

    public Element getElement() {
        return element;
    }
}
