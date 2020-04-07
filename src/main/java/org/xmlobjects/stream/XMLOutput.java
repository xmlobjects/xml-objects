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
import org.xml.sax.helpers.DefaultHandler;
import org.xmlobjects.util.xml.SAXFilter;

public abstract class XMLOutput<T extends XMLOutput<?>> extends SAXFilter implements AutoCloseable {

    public XMLOutput() {
        super(new DefaultHandler());
    }

    public XMLOutput(ContentHandler parent) {
        super(parent);
    }

    public abstract String getPrefix(String namespaceURI);
    public abstract T withPrefix(String prefix, String namespaceURI);
    public abstract String getNamespaceURI(String prefix);
    public abstract T withDefaultNamespace(String namespaceURI);
    public abstract String getIndentString();
    public abstract T withIndentString(String indent);
    public abstract boolean isWriteXMLDeclaration();
    public abstract T writeXMLDeclaration(boolean writeXMLDeclaration);
    public abstract String[] getHeaderComment();
    public abstract T withHeaderComment(String... headerComment);
    public abstract String getSchemaLocation(String namespaceURI);
    public abstract T withSchemaLocation(String namespaceURI, String schemaLocation);
    public abstract void flush() throws Exception;

    protected abstract String createPrefix();
}
