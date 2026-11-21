/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import org.xmlobjects.XMLObjects;

import javax.xml.XMLConstants;
import java.util.*;

public class NamespaceSupport {
    private Context current = new Context();
    private Map<String, String> prefixes;
    private boolean nextContext = true;
    private int prefixCounter = 1;

    public NamespaceSupport() {
        current.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
        current.put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
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
        if (current == null) {
            throw new EmptyStackException();
        }
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
            if (current.prefixes != null) {
                current.prefixes.remove(current.namespaceURIs.get(prefix));
                current.namespaceURIs.remove(current.prefixes.get(namespaceURI));
            }

            current.put(namespaceURI, prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        for (Context context = current; context != null; context = context.previous) {
            if (context.prefixes != null) {
                String prefix = context.prefixes.get(namespaceURI);
                if (prefix != null) {
                    return prefix;
                }
            }
        }

        return null;
    }

    public Set<String> getPrefixes(String namespaceURI) {
        Context context = current;
        Set<String> prefixes = new LinkedHashSet<>();

        while (context != null) {
            String prefix = context.prefixes != null ? context.prefixes.get(namespaceURI) : null;
            if (prefix != null) {
                prefixes.add(prefix);
            }

            context = context.previous;
        }

        return prefixes;
    }

    public String getNamespaceURI(String prefix) {
        for (Context context = current; context != null; context = context.previous) {
            if (context.namespaceURIs != null) {
                String namespaceURI = context.namespaceURIs.get(prefix);
                if (namespaceURI != null) {
                    return namespaceURI;
                }
            }
        }

        return null;
    }

    public Map<String, String> getCurrentContext() {
        return current.prefixes != null ? Collections.unmodifiableMap(current.prefixes) : Collections.emptyMap();
    }

    public String createPrefixFromQName(String qName, String namespaceURI) {
        String prefix = null;
        if (!qName.isEmpty()) {
            int index = qName.indexOf(':');
            if (index != -1) {
                String candidate = qName.substring(0, index);
                String previous = getNamespaceURI(candidate);
                if (previous == null) {
                    prefix = candidate;
                } else if (previous.equals(namespaceURI)) {
                    return candidate;
                }
            }
        }

        return prefix != null ? prefix : createPrefix(namespaceURI);
    }

    public String createPrefix(String namespaceURI) {
        String prefix = prefixes != null ? prefixes.get(namespaceURI) : null;
        while (prefix == null || getNamespaceURI(prefix) != null) {
            prefix = "ns" + prefixCounter++;
        }

        return prefix;
    }

    private static class Context {
        // created lazily, since most elements do not declare namespaces
        private Map<String, String> prefixes;
        private Map<String, String> namespaceURIs;
        private Context previous;

        Context() {
        }

        Context(Context previous) {
            this.previous = previous;
        }

        void put(String namespaceURI, String prefix) {
            if (prefixes == null) {
                prefixes = new HashMap<>();
                namespaceURIs = new HashMap<>();
            }

            prefixes.put(namespaceURI, prefix);
            namespaceURIs.put(prefix, namespaceURI);
        }
    }
}
