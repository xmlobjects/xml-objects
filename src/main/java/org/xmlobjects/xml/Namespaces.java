package org.xmlobjects.xml;

import javax.xml.XMLConstants;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Namespaces {
    private static final Namespaces EMPTY = new Namespaces(Collections.singleton(XMLConstants.NULL_NS_URI));
    private final Set<String> namespaces;

    private Namespaces(Set<String> namespaces) {
        this.namespaces = Objects.requireNonNull(namespaces, "Namespace URIs must not be null.");
    }

    public static Namespaces newInstance() {
        return new Namespaces(new HashSet<>());
    }

    public static Namespaces of(Collection<String> namespaceURIs) {
        return new Namespaces(new HashSet<>(namespaceURIs));
    }

    public static Namespaces of(String... namespaceURIs) {
        return of(Arrays.asList(namespaceURIs));
    }

    public static Namespaces empty() {
        return EMPTY;
    }

    public Namespaces add(String namespaceURI) {
        namespaces.add(namespaceURI);
        return this;
    }

    public Namespaces addNullNamespace() {
        namespaces.add(XMLConstants.NULL_NS_URI);
        return this;
    }

    public boolean contains(String namespaceURI) {
        return namespaces.contains(namespaceURI);
    }
}
