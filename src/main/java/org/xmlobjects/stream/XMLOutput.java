/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
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
            return withPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        } else {
            return self();
        }
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
