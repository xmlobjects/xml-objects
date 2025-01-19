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

import javax.xml.XMLConstants;
import java.util.*;
import java.util.function.Predicate;

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

    public Set<String> get() {
        return namespaces;
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

    public boolean containsAll(Collection<String> namespaceURIs) {
        return namespaces.containsAll(namespaceURIs);
    }

    public boolean containsAll(String... namespaceURIs) {
        return containsAll(Arrays.asList(namespaceURIs));
    }

    public Namespaces remove(String namespaceURI) {
        namespaces.remove(namespaceURI);
        return this;
    }

    public Namespaces removeIf(Predicate<? super String> filter) {
        namespaces.removeIf(filter);
        return this;
    }

    public Namespaces removeAll(Collection<String> namespaceURIs) {
        namespaces.removeAll(namespaceURIs);
        return this;
    }

    public Namespaces removeAll(String... namespaceURIs) {
        return removeAll(Arrays.asList(namespaceURIs));
    }

    public Namespaces copy() {
        return new Namespaces(new HashSet<>(namespaces));
    }
}
