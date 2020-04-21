/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2020 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects.util.xml;

import org.xmlobjects.XMLObjects;

import javax.xml.XMLConstants;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NamespaceSupport {
    private Context current = new Context();
    private Map<String, String> prefixes;
    private boolean nextContext = true;
    private int prefixCounter = 1;

    public NamespaceSupport() {
        current.prefixes.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
        current.namespaceURIs.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        current.prefixes.put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
        current.namespaceURIs.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        current.prefixes.put(XMLConstants.NULL_NS_URI, XMLConstants.DEFAULT_NS_PREFIX);
    }

    public void createInternalPrefixes(XMLObjects xmlObjects) {
        prefixes = new HashMap<>();
        xmlObjects.getSerializableNamespaces().stream()
                .sorted()
                .forEach(n -> prefixes.put(n, "ns" + prefixCounter++));
    }

    public boolean requiresNextContext() {
        return nextContext;
    }

    public void requireNextContext() {
        nextContext = true;
    }

    public void pushContext() {
        if (nextContext) {
            current = new Context(current);
            nextContext = false;
        }
    }

    public void popContext() {
        current = current.previous;
        if (current == null)
            throw new EmptyStackException();
    }

    public void declarePrefix(String prefix, String namespaceURI) {
        if (prefix != null
                && namespaceURI != null
                && !XMLConstants.XML_NS_PREFIX.equals(prefix)
                && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)
                && !XMLConstants.XML_NS_URI.equals(namespaceURI)
                && !XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)
                && !XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
            // we only support one prefix per namespace URI and context
            // so, we first delete a previous mapping
            current.prefixes.remove(current.namespaceURIs.get(prefix));
            current.namespaceURIs.remove(current.prefixes.get(namespaceURI));
            current.prefixes.put(namespaceURI, prefix);
            current.namespaceURIs.put(prefix, namespaceURI);
        }
    }

    public String getPrefix(String namespaceURI) {
        Context context = current;
        String prefix = null;

        while (context != null && (prefix = context.prefixes.get(namespaceURI)) == null)
            context = context.previous;

        return prefix;
    }

    public Set<String> getPrefixes(String namespaceURI) {
        Context context = current;
        Set<String> prefixes = new LinkedHashSet<>();

        while (context != null) {
            String prefix = context.prefixes.get(namespaceURI);
            if (prefix != null)
                prefixes.add(prefix);

            context = context.previous;
        }

        return prefixes;
    }

    public String getNamespaceURI(String prefix) {
        Context context = current;
        String namespaceURI = null;

        while (context != null && (namespaceURI = context.namespaceURIs.get(prefix)) == null)
            context = context.previous;

        return namespaceURI;
    }

    public Map<String, String> getCurrentContext() {
        return new HashMap<>(current.namespaceURIs);
    }

    public String createPrefixFromQName(String qName, String namespaceURI) {
        String prefix = null;
        if (!qName.isEmpty()) {
            int index = qName.indexOf(':');
            if (index != -1) {
                String candidate = qName.substring(0, index);
                String previous = getNamespaceURI(candidate);
                if (previous == null)
                    prefix = candidate;
                else if (previous.equals(namespaceURI))
                    return candidate;
            }
        }

        return prefix != null ? prefix : createPrefix(namespaceURI);
    }

    public String createPrefix(String namespaceURI) {
        String prefix = prefixes != null ? prefixes.get(namespaceURI) : null;
        while (prefix == null || getNamespaceURI(prefix) != null)
            prefix = "ns" + prefixCounter++;

        return prefix;
    }

    private static class Context {
        private final Map<String, String> prefixes = new HashMap<>();
        private final Map<String, String> namespaceURIs = new HashMap<>();
        private Context previous;

        Context() {
        }

        Context(Context previous) {
            this.previous = previous;
        }
    }
}
