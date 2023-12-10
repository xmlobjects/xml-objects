/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2023 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects.stream;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlobjects.util.xml.NamespaceSupport;
import org.xmlobjects.util.xml.SAXFilter;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Map;

public abstract class XMLOutput<T extends XMLOutput<?>> extends SAXFilter implements AutoCloseable {
    protected final NamespaceSupport prefixMapping = new NamespaceSupport();
    protected final Map<String, String> schemaLocations = new HashMap<>();
    protected String indent;
    protected boolean writeXMLDeclaration = true;
    protected String[] headerComment;

    protected abstract T self();

    public XMLOutput() {
        super(new DefaultHandler());
    }

    public XMLOutput(ContentHandler parent) {
        super(parent);
    }

    public abstract void flush() throws Exception;

    NamespaceSupport getPrefixMapping() {
        return prefixMapping;
    }

    public String getPrefix(String namespaceURI) {
        return prefixMapping.getPrefix(namespaceURI);
    }

    String createPrefix(String namespaceURI) {
        return prefixMapping.createPrefix(namespaceURI);
    }

    public T withPrefix(String prefix, String namespaceURI) {
        prefixMapping.pushContext();
        prefixMapping.declarePrefix(prefix, namespaceURI);
        return self();
    }

    public String getNamespaceURI(String prefix) {
        return prefixMapping.getNamespaceURI(prefix);
    }

    public T withDefaultNamespace(String namespaceURI) {
        return withPrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
    }

    public String getSchemaLocation(String namespaceURI) {
        return schemaLocations.get(namespaceURI);
    }

    public T withSchemaLocation(String namespaceURI, String schemaLocation) {
        if (namespaceURI != null && schemaLocation != null) {
            schemaLocations.put(namespaceURI, schemaLocation);
            withPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        }

        return self();
    }

    public String getIndent() {
        return indent;
    }

    public T withIndent(String indent) {
        this.indent = indent;
        return self();
    }

    public boolean isWriteXMLDeclaration() {
        return writeXMLDeclaration;
    }

    public T writeXMLDeclaration(boolean writeXMLDeclaration) {
        this.writeXMLDeclaration = writeXMLDeclaration;
        return self();
    }

    public String[] getHeaderComment() {
        return headerComment;
    }

    public T withHeaderComment(String... headerMessage) {
        if (headerMessage != null) {
            this.headerComment = headerMessage;
        }

        return self();
    }
}
