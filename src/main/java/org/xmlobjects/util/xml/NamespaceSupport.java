/*
 * citygml4j - The Open Source Java API for CityGML
 * https://github.com/citygml4j
 *
 * Copyright 2013-2020 Claus Nagel <claus.nagel@gmail.com>
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

import javax.xml.XMLConstants;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

public class NamespaceSupport {
    private Context current;

    public NamespaceSupport() {
        current = new Context();
    }

    public void declarePrefix(String prefix, String namespaceURI) {
        if (prefix != null
                && namespaceURI != null
                && !XMLConstants.XML_NS_PREFIX.equals(prefix)
                && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)
                && !XMLConstants.XML_NS_URI.equals(namespaceURI)) {
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

    public void pushContext() {
        current = new Context(current);
    }

    public void popContext() {
        current = current.previous;
        if (current == null)
            throw new EmptyStackException();
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
