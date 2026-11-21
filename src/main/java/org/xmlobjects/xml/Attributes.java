/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Attributes {
    private static final Attributes EMPTY = new Attributes(0);

    // elements usually have only a few attributes, so they are stored in compact parallel arrays
    private String[] namespaceURIs;
    private String[] localNames;
    private TextContent[] values;
    private int size;

    /**
     * Receives the namespace URI, local name and value of each attribute.
     *
     * @param <E> the type of exception the consumer may throw
     */
    @FunctionalInterface
    public interface Consumer<E extends Exception> {
        void accept(String namespaceURI, String localName, TextContent value) throws E;
    }

    public static Attributes empty() {
        return EMPTY;
    }

    public Attributes() {
        this(4);
    }

    private Attributes(int capacity) {
        namespaceURIs = new String[capacity];
        localNames = new String[capacity];
        values = new TextContent[capacity];
    }

    public void add(String namespaceURI, String localName, TextContent value) {
        if (isImmutable()) {
            throw new UnsupportedOperationException("Attributes.empty() is immutable.");
        }

        namespaceURI = ensureNonNull(namespaceURI);
        int index = indexOf(namespaceURI, localName);
        if (index >= 0) {
            values[index] = value;
        } else {
            if (size == values.length) {
                int capacity = size * 2;
                namespaceURIs = Arrays.copyOf(namespaceURIs, capacity);
                localNames = Arrays.copyOf(localNames, capacity);
                values = Arrays.copyOf(values, capacity);
            }

            namespaceURIs[size] = namespaceURI;
            localNames[size] = localName;
            values[size++] = value;
        }
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
        if (isImmutable()) {
            throw new UnsupportedOperationException("Attributes.empty() is immutable.");
        }

        attributes.forEach((localName, value) -> add(namespaceURI, localName, value));
    }

    public boolean contains(String localName) {
        return contains(XMLConstants.NULL_NS_URI, localName);
    }

    public boolean contains(QName name) {
        return contains(name.getNamespaceURI(), name.getLocalPart());
    }

    public boolean contains(String namespaceURI, String localName) {
        return indexOf(ensureNonNull(namespaceURI), localName) >= 0;
    }

    /**
     * Returns the attributes as an unmodifiable map from namespace URIs to maps from local names to values.
     * Use {@link #forEach(Consumer)} to iterate the attributes without creating maps.
     */
    public Map<String, Map<String, TextContent>> get() {
        Map<String, Map<String, TextContent>> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            map.computeIfAbsent(namespaceURIs[i], v -> new LinkedHashMap<>()).put(localNames[i], values[i]);
        }

        map.replaceAll((namespaceURI, values) -> Collections.unmodifiableMap(values));
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the attributes of the given namespace as an unmodifiable map from local names to values.
     */
    public Map<String, TextContent> get(String namespaceURI) {
        namespaceURI = ensureNonNull(namespaceURI);
        Map<String, TextContent> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            if (namespaceURIs[i].equals(namespaceURI)) {
                map.put(localNames[i], values[i]);
            }
        }

        return Collections.unmodifiableMap(map);
    }

    public TextContent getValue(String localName) {
        return getValue(XMLConstants.NULL_NS_URI, localName);
    }

    public TextContent getValue(String namespaceURI, String localName) {
        int index = indexOf(ensureNonNull(namespaceURI), localName);
        return index >= 0 ? values[index] : TextContent.absent();
    }

    public TextContent getValue(QName name) {
        return getValue(name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Passes each attribute to the given consumer, in the order the attributes were added.
     *
     * @param action the consumer
     * @param <E>    the type of exception the consumer may throw
     * @throws E if the consumer throws it
     */
    public <E extends Exception> void forEach(Consumer<E> action) throws E {
        for (int i = 0; i < size; i++) {
            action.accept(namespaceURIs[i], localNames[i], values[i]);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isImmutable() {
        return this == EMPTY;
    }

    private int indexOf(String namespaceURI, String localName) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(localNames[i], localName) && namespaceURIs[i].equals(namespaceURI)) {
                return i;
            }
        }

        return -1;
    }

    private String ensureNonNull(String namespaceURI) {
        return namespaceURI != null ? namespaceURI : XMLConstants.NULL_NS_URI;
    }
}
