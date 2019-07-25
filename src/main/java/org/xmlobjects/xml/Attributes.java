package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public TextContent getValue(String localName) {
        return getValue(XMLConstants.NULL_NS_URI, localName);
    }

    public TextContent getValue(String namespaceURI, String localName) {
        return attributes.getOrDefault(namespaceURI, Collections.emptyMap()).getOrDefault(localName, TextContent.empty());
    }

    public TextContent getValue(QName name) {
        return getValue(name.getNamespaceURI(), name.getLocalPart());
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public Map<QName, TextContent> toMap() {
        return attributes.entrySet().stream().flatMap(namespace -> namespace.getValue().entrySet().stream()
                .map(attribute -> new AbstractMap.SimpleEntry<>(new QName(namespace.getKey(), attribute.getKey()), attribute.getValue())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
