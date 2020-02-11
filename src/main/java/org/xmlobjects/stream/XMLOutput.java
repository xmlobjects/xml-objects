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
