/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Attributes {
    private static final Attributes EMPTY = new Attributes(true);
    private static final int INITIAL_CAPACITY = 4;

    private final boolean immutable;

    // Elements usually have only a few attributes, so they are stored in compact arrays. The nested maps
    // are only created when requested through get() or get(String), since callers may modify them.
    // From then on, the maps are the only storage.
    private String[] namespaceURIs;
    private String[] localNames;
    private TextContent[] values;
    private int size;
    private Map<String, Map<String, TextContent>> attributes;

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
        this(false);
    }

    private Attributes(boolean immutable) {
        this.immutable = immutable;
    }

    public void add(String namespaceURI, String localName, TextContent value) {
        requireMutable();
        namespaceURI = ensureNonNull(namespaceURI);
        if (attributes != null) {
            attributes.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, value);
        } else {
            int index = indexOf(namespaceURI, localName);
            if (index >= 0) {
                values[index] = value;
            } else {
                if (namespaceURIs == null) {
                    namespaceURIs = new String[INITIAL_CAPACITY];
                    localNames = new String[INITIAL_CAPACITY];
                    values = new TextContent[INITIAL_CAPACITY];
                } else if (size == namespaceURIs.length) {
                    namespaceURIs = Arrays.copyOf(namespaceURIs, size * 2);
                    localNames = Arrays.copyOf(localNames, size * 2);
                    values = Arrays.copyOf(values, size * 2);
                }

                namespaceURIs[size] = namespaceURI;
                localNames[size] = localName;
                values[size++] = value;
            }
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
        requireMutable();
        for (Map.Entry<String, TextContent> entry : attributes.entrySet()) {
            add(namespaceURI, entry.getKey(), entry.getValue());
        }
    }

    public boolean contains(String localName) {
        return contains(XMLConstants.NULL_NS_URI, localName);
    }

    public boolean contains(QName name) {
        return contains(name.getNamespaceURI(), name.getLocalPart());
    }

    public boolean contains(String namespaceURI, String localName) {
        namespaceURI = ensureNonNull(namespaceURI);
        return attributes != null ?
                attributes.getOrDefault(namespaceURI, Collections.emptyMap()).containsKey(localName) :
                indexOf(namespaceURI, localName) >= 0;
    }

    /**
     * Returns the attributes as a map from namespace URIs to maps from local names to values. Changes to the
     * returned maps are reflected in this object. Use {@link #forEach(Consumer)} to iterate the attributes
     * more efficiently.
     */
    public Map<String, Map<String, TextContent>> get() {
        return immutable ? Map.of() : toMap();
    }

    /**
     * Returns the attributes of the given namespace as a map from local names to values. Changes to the
     * returned map are reflected in this object, unless it is empty.
     */
    public Map<String, TextContent> get(String namespaceURI) {
        return immutable ?
                Collections.emptyMap() :
                toMap().getOrDefault(ensureNonNull(namespaceURI), Collections.emptyMap());
    }

    public TextContent getValue(String localName) {
        return getValue(XMLConstants.NULL_NS_URI, localName);
    }

    public TextContent getValue(String namespaceURI, String localName) {
        namespaceURI = ensureNonNull(namespaceURI);
        if (attributes != null) {
            return attributes.getOrDefault(namespaceURI, Collections.emptyMap())
                    .getOrDefault(localName, TextContent.absent());
        } else {
            int index = indexOf(namespaceURI, localName);
            return index >= 0 ? values[index] : TextContent.absent();
        }
    }

    public TextContent getValue(QName name) {
        return getValue(name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Passes each attribute to the given consumer, in the order the attributes were added. Unlike
     * {@link #get()}, this does not create any maps.
     *
     * @param action the consumer
     * @param <E>    the type of exception the consumer may throw
     * @throws E if the consumer throws it
     */
    public <E extends Exception> void forEach(Consumer<E> action) throws E {
        if (attributes != null) {
            for (Map.Entry<String, Map<String, TextContent>> entry : attributes.entrySet()) {
                for (Map.Entry<String, TextContent> attribute : entry.getValue().entrySet()) {
                    action.accept(entry.getKey(), attribute.getKey(), attribute.getValue());
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                action.accept(namespaceURIs[i], localNames[i], values[i]);
            }
        }
    }

    public boolean isEmpty() {
        return attributes != null ? attributes.isEmpty() : size == 0;
    }

    public boolean isImmutable() {
        return immutable;
    }

    private Map<String, Map<String, TextContent>> toMap() {
        if (attributes == null) {
            attributes = new HashMap<>();
            for (int i = 0; i < size; i++) {
                attributes.computeIfAbsent(namespaceURIs[i], v -> new HashMap<>()).put(localNames[i], values[i]);
            }

            namespaceURIs = localNames = null;
            values = null;
            size = 0;
        }

        return attributes;
    }

    private int indexOf(String namespaceURI, String localName) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(localName, localNames[i]) && namespaceURI.equals(namespaceURIs[i])) {
                return i;
            }
        }

        return -1;
    }

    private void requireMutable() {
        if (immutable) {
            throw new UnsupportedOperationException("Attributes.empty() is immutable.");
        }
    }

    private String ensureNonNull(String namespaceURI) {
        return namespaceURI != null ? namespaceURI : XMLConstants.NULL_NS_URI;
    }
}
