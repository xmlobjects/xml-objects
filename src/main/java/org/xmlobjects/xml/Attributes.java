/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Attributes {
    private final Map<String, Map<String, TextContent>> attributes = new HashMap<>();

    public void add(String namespaceURI, String localName, TextContent value) {
        attributes.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, value);
    }

    public void add(String namespaceURI, String localName, String value) {
        add(namespaceURI, localName, TextContent.of(value));
    }

    public void add(String localName, TextContent value) {
        add(XMLConstants.NULL_NS_URI, localName, value);
    }

    public void add(String localName, String value) {
        add(localName, TextContent.of(value));
    }

    public void add(QName name, TextContent value) {
        add(name.getNamespaceURI(), name.getLocalPart(), value);
    }

    public void add(QName name, String value) {
        add(name, TextContent.of(value));
    }

    public void addAll(String namespaceURI, Map<String, TextContent> attributes) {
        this.attributes.computeIfAbsent(namespaceURI, v -> new HashMap<>()).putAll(attributes);
    }

    public boolean contains(String localName) {
        return contains(XMLConstants.NULL_NS_URI, localName);
    }

    public boolean contains(QName name) {
        return contains(name.getNamespaceURI(), name.getLocalPart());
    }

    public boolean contains(String namespaceURI, String localName) {
        return get(namespaceURI).containsKey(localName);
    }

    public Map<String, Map<String, TextContent>> get() {
        return attributes;
    }

    public Map<String, TextContent> get(String namespaceURI) {
        return attributes.getOrDefault(namespaceURI, Collections.emptyMap());
    }

    public TextContent getValue(String localName) {
        return getValue(XMLConstants.NULL_NS_URI, localName);
    }

    public TextContent getValue(String namespaceURI, String localName) {
        return get(namespaceURI).getOrDefault(localName, TextContent.absent());
    }

    public TextContent getValue(QName name) {
        return getValue(name.getNamespaceURI(), name.getLocalPart());
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public Attributes copy() {
        Attributes copy = new Attributes();
        copy.attributes.putAll(attributes);
        return copy;
    }
}
