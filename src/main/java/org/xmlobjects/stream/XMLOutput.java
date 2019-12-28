package org.xmlobjects.stream;

import org.xml.sax.ContentHandler;

public interface XMLOutput<T extends XMLOutput<?>> extends ContentHandler, AutoCloseable {
    String getPrefix(String namespaceURI);
    T withPrefix(String prefix, String namespaceURI);
    String getNamespaceURI(String prefix);
    T withDefaultNamespace(String namespaceURI);
    String getIndentString();
    T withIndentString(String indent);
    boolean isWriteXMLDeclaration();
    T writeXMLDeclaration(boolean writeXMLDeclaration);
    String[] getHeaderComment();
    T withHeaderComment(String... headerComment);
    String getSchemaLocation(String namespaceURI);
    T withSchemaLocation(String namespaceURI, String schemaLocation);
    void flush() throws Exception;
}
