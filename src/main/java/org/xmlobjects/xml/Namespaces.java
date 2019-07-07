package org.xmlobjects.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Namespaces {
    private final Set<String> namespaces;

    private Namespaces(Set<String> namespaces) {
        this.namespaces = Objects.requireNonNull(namespaces, "Namespace URIs must not be null.");
    }

    public static Namespaces of(Collection<String> namespaceURIs) {
        return new Namespaces(new HashSet<>(namespaceURIs));
    }

    public static Namespaces of(String... namespaceURIs) {
        return of(Arrays.asList(namespaceURIs));
    }

    public boolean contains(String namespaceURI) {
        return namespaces.contains(namespaceURI);
    }
}
