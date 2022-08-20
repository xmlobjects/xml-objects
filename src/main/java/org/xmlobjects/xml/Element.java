/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2022 Claus Nagel <claus.nagel@gmail.com>
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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Element {
    private final QName name;
    private Attributes attributes;
    private List<ElementContent> content;

    private Element(String namespaceURI, String localName) {
        Objects.requireNonNull(namespaceURI, "The namespace URI must not be null.");
        Objects.requireNonNull(localName, "The local name must not be null.");
        this.name = new QName(namespaceURI, localName);
    }

    public static Element of(String namespaceURI, String localName) {
        return new Element(namespaceURI, localName);
    }

    public static Element of(String localName) {
        return new Element(XMLConstants.NULL_NS_URI, localName);
    }

    public QName getName() {
        return name;
    }

    public Element addAttribute(String namespaceURI, String localName, TextContent value) {
        if (value != null && value.isPresent()) {
            if (attributes == null)
                attributes = new Attributes();

            attributes.add(namespaceURI, localName, value);
        }

        return this;
    }

    public Element addAttribute(String namespaceURI, String localName, String value) {
        return addAttribute(namespaceURI, localName, TextContent.of(value));
    }

    public Element addAttribute(String name, TextContent value) {
        return addAttribute(XMLConstants.NULL_NS_URI, name, value);
    }

    public Element addAttribute(String name, String value) {
        return addAttribute(name, TextContent.of(value));
    }

    public Element addAttribute(QName name, TextContent value) {
        return addAttribute(name.getNamespaceURI(), name.getLocalPart(), value);
    }

    public Element addAttribute(QName name, String value) {
        return addAttribute(name, TextContent.of(value));
    }

    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public Element addChildElement(Element child) {
        return addContent(ElementContent.of(child));
    }

    public Element addTextContent(TextContent textContent) {
        return textContent != null && textContent.isPresent() ? addContent(ElementContent.of(textContent)) : this;
    }

    public Element addTextContent(String text) {
        return addTextContent(TextContent.of(text));
    }

    private Element addContent(ElementContent item) {
        if (item != null) {
            if (content == null)
                content = new ArrayList<>();

            content.add(item);
        }

        return this;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public List<ElementContent> getContent() {
        return content != null ? content : Collections.emptyList();
    }
}
