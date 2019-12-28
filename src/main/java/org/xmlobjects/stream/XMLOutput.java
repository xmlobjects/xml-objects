package org.xmlobjects.stream;

import org.xml.sax.ContentHandler;

public interface XMLOutput<T extends XMLOutput<?>> extends ContentHandler, AutoCloseable {
    String getPrefix(String namespaceURI);
    T usePrefix(String prefix, String namespaceURI);
    String getNamespaceURI(String prefix);
    T useDefaultNamespace(String namespaceURI);
    String getIndentString();
    T useIndentString(String indent);
    boolean isWriteXMLDeclaration();
    T writeXMLDeclaration(boolean writeXMLDeclaration);
    String[] getHeaderComment();
    T useHeaderComment(String... headerComment);
    String getSchemaLocation(String namespaceURI);
    T useSchemaLocation(String namespaceURI, String schemaLocation);
    void flush() throws Exception;
}
