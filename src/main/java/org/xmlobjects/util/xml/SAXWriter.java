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

package org.xmlobjects.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xmlobjects.stream.XMLOutput;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SAXWriter extends XMLOutput<SAXWriter> {
    private Writer writer;
    private String encoding;
    private CharsetEncoder encoder;

    private boolean escapeCharacters = true;
    private boolean writeEncoding = false;
    private int depth = 0;
    private XMLEvents lastEvent;

    private enum XMLEvents {
        START_DOCUMENT,
        START_ELEMENT,
        END_ELEMENT,
        CHARACTERS,
        PROCESSING_INSTRUCTION
    }

    public SAXWriter(StreamResult streamResult) throws IOException {
        setOutput(streamResult);
    }

    public SAXWriter(StreamResult streamResult, String encoding) throws IOException {
        setOutput(streamResult, encoding);
    }

    public SAXWriter(OutputStream outputStream) throws IOException {
        this(outputStream, null);
    }

    public SAXWriter(OutputStream outputStream, String encoding) throws IOException {
        setOutput(outputStream, encoding);
    }

    public SAXWriter(Writer writer) {
        setOutput(writer);
    }

    private void setOutput(StreamResult streamResult) throws IOException {
        setOutput(streamResult, null);
    }

    private void setOutput(OutputStream outputStream) throws IOException {
        setOutput(outputStream, null);
    }

    private void setOutput(StreamResult streamResult, String encoding) throws IOException {
        if (streamResult.getOutputStream() != null)
            setOutput(streamResult.getOutputStream(), encoding);
        else if (streamResult.getWriter() != null)
            setOutput(streamResult.getWriter());
        else if (streamResult.getSystemId() != null)
            setOutput(new FileOutputStream(streamResult.getSystemId()), encoding);
    }

    private void setOutput(Writer writer) {
        if (writer instanceof OutputStreamWriter) {
            this.writer = new BufferedWriter(writer);
            String encoding = ((OutputStreamWriter) writer).getEncoding();
            if (encoding != null)
                setEncoding(encoding);
        } else
            this.writer = writer;
    }

    private void setOutput(OutputStream outputStream, String encoding) throws IOException {
        if (encoding == null)
            encoding = System.getProperty("file.encoding", "UTF-8");

        writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
        setEncoding(encoding);
    }

    @Override
    public void flush() throws IOException {
        if (writer != null)
            writer.flush();
    }

    @Override
    public void close() throws IOException {
        if (writer != null)
            writer.close();
    }

    public boolean isEscapeCharacters() {
        return escapeCharacters;
    }

    public SAXWriter escapeCharacters(boolean escapeCharacters) {
        this.escapeCharacters = escapeCharacters;
        return this;
    }

    private void setEncoding(String name) {
        Charset charset = Charset.forName(name);
        encoding = charset.name();
        writeEncoding = true;

        if (!encoding.equalsIgnoreCase("UTF-8"))
            encoder = charset.newEncoder();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            if (length > 0) {
                if (lastEvent == XMLEvents.START_ELEMENT)
                    writer.write('>');

                writeTextContent(ch, start, length, escapeCharacters);
                lastEvent = XMLEvents.CHARACTERS;
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            depth--;

            if (lastEvent == XMLEvents.START_ELEMENT)
                writer.write("/>");
            else {
                if (lastEvent == XMLEvents.END_ELEMENT)
                    writeIndent();

                writer.write("</");

                if (!localName.isEmpty())
                    writeQName(prefixMapping.getPrefix(namespaceURI), localName);
                else
                    writer.write(qName);

                writer.write('>');
            }

            lastEvent = XMLEvents.END_ELEMENT;
            prefixMapping.popContext();
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            if (lastEvent != XMLEvents.END_ELEMENT) {
                if (length > 0 && lastEvent == XMLEvents.START_ELEMENT)
                    writer.write('>');

                writeTextContent(ch, start, length, escapeCharacters);
                lastEvent = XMLEvents.CHARACTERS;
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            if (lastEvent == XMLEvents.START_ELEMENT) {
                writer.write('>');
                writeIndent();
            }

            if (target == null || data == null)
                throw new SAXException("PI target cannot be null.");

            writer.write("<?");
            writer.write(target);
            writer.write(' ');
            writer.write(data);
            writer.write("?>");
            writeIndent();

            lastEvent = XMLEvents.PROCESSING_INSTRUCTION;
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            if (depth == 0 && lastEvent != XMLEvents.START_DOCUMENT) {
                if (writeXMLDeclaration) {
                    if (encoding == null && writer instanceof OutputStreamWriter) {
                        encoding = ((OutputStreamWriter) writer).getEncoding();
                        if (encoding != null)
                            encoding = Charset.forName(encoding).name();
                    }

                    writer.write("<?xml");
                    writer.write(" version=\"1.0\"");

                    if (writeEncoding && encoding != null) {
                        writer.write(" encoding=");
                        writer.write('"');
                        writer.write(encoding);
                        writer.write('"');
                    }

                    writer.write(" standalone=\"yes\"");
                    writer.write("?>");
                    writeIndent();
                }

                if (headerComment != null)
                    writeHeader(headerComment);
            }

            lastEvent = XMLEvents.START_DOCUMENT;
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            prefixMapping.pushContext();

            if (depth > 0) {
                if (lastEvent == XMLEvents.START_ELEMENT)
                    writer.write('>');

                writeIndent();
            }

            writer.write('<');

            if (!localName.isEmpty()) {
                String prefix = prefixMapping.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = prefixMapping.createPrefixFromQName(qName, namespaceURI);
                    prefixMapping.declarePrefix(prefix, namespaceURI);
                }

                writeQName(prefix, localName);
            } else
                writer.write(qName);

            writeAttributes(atts);
            writeNamespaces(prefixMapping.getCurrentContext());

            if (depth == 0)
                writeSchemaLocations();

            lastEvent = XMLEvents.START_ELEMENT;
            prefixMapping.requireNextContext();
            depth++;
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        String previous = prefixMapping.getPrefix(namespaceURI);
        if (previous == null || !namespaceURI.equals(prefixMapping.getNamespaceURI(previous))) {
            prefixMapping.pushContext();
            prefixMapping.declarePrefix(prefix, namespaceURI);
        }
    }

    private void writeAttributes(Attributes atts) throws SAXException {
        if (atts.getLength() > 0) {
            try {
                Map<String, String> prefixes = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    String localName = atts.getLocalName(i);
                    String namespaceURI = atts.getURI(i);
                    String prefix = null;

                    if (namespaceURI != null && !namespaceURI.isEmpty()) {
                        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
                            continue;

                        prefix = prefixMapping.getPrefix(namespaceURI);
                        if (prefix == null) {
                            prefix = prefixMapping.createPrefixFromQName(atts.getQName(i), namespaceURI);
                            prefixMapping.declarePrefix(prefix, namespaceURI);
                        } else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                            if (prefixes == null) {
                                prefixes = new HashMap<>();
                            }

                            String name = atts.getQName(i);
                            prefix = prefixes.computeIfAbsent(namespaceURI,
                                    v -> prefixMapping.createPrefixFromQName(name, namespaceURI));
                        }
                    }

                    writer.write(' ');
                    writeQName(prefix, localName);
                    writer.write("=\"");
                    writeAttributeContent(atts.getValue(i));
                    writer.write('"');
                }

                if (prefixes != null) {
                    writeNamespaces(prefixes);
                }
            } catch (IOException e) {
                throw new SAXException("Caused by:", e);
            }
        }
    }

    private void writeQName(String prefix, String localName) throws SAXException {
        try {
            if (prefix != null && !prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                writer.write(prefix);
                writer.write(':');
            }

            writer.write(localName);
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeNamespaces(Map<String, String> prefixes) throws SAXException {
        try {
            for (Map.Entry<String, String> entry : prefixes.entrySet()) {
                String namespaceURI = entry.getKey();
                String prefix = entry.getValue();

                writer.write(' ');
                writer.write(XMLConstants.XMLNS_ATTRIBUTE);

                if (!prefix.isEmpty()) {
                    writer.write(':');
                    writer.write(prefix);
                }

                writer.write("=\"");
                writeAttributeContent(namespaceURI);
                writer.write('"');
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeSchemaLocations() throws SAXException {
        if (!schemaLocations.isEmpty()) {
            try {
                String prefix = prefixMapping.getPrefix(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

                writer.write(' ');
                writeQName(prefix, "schemaLocation");
                writer.write("=\"");

                Iterator<Map.Entry<String, String>> iter = schemaLocations.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    writeAttributeContent(entry.getKey());
                    writer.write(' ');
                    writeAttributeContent(entry.getValue());

                    if (iter.hasNext())
                        writer.write(' ');
                }

                writer.write('"');
            } catch (IOException e) {
                throw new SAXException("Caused by:", e);
            }
        }
    }

    private void writeHeader(String... data) throws SAXException {
        try {
            if (lastEvent == XMLEvents.START_ELEMENT) {
                writer.write('>');
                writeIndent();
            }

            if (data == null)
                throw new SAXException("Comment target cannot be null.");

            for (String line : data) {
                if (line == null)
                    continue;

                writer.write("<!--");
                writer.write(' ');
                writer.write(line);
                writer.write(' ');
                writer.write("-->");
                writeIndent();
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeIndent() throws SAXException {
        if (indent == null)
            return;

        if (lastEvent == XMLEvents.CHARACTERS)
            return;

        try {
            writer.write("\n");
            for (int i = 0; i < depth; i++)
                writer.write(indent);
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeTextContent(char[] content, int start, int length, boolean escapeCharacters) throws IOException {
        int pos = start;
        final int end = start + length;

        for (int i = start; i < end; i++) {
            char ch = content[i];

            if (encoder != null && !encoder.canEncode(ch)) {
                writer.write(content, pos, i - pos);

                writer.write("&#x");
                writer.write(Integer.toHexString(ch));
                writer.write(';');
                pos = i + 1;
            }

            else if (escapeCharacters) {
                switch (ch) {
                    case '<':
                        writer.write(content, pos, i - pos);
                        writer.write("&lt;");
                        pos = i + 1;

                        break;

                    case '&':
                        writer.write(content, pos, i - pos);
                        writer.write("&amp;");
                        pos = i + 1;

                        break;
                }
            }
        }

        writer.write(content, pos, end - pos);
    }

    private void writeAttributeContent(String content) throws IOException {
        int pos = 0;
        final int end = content.length();

        for (int i = 0; i < end; i++) {
            char ch = content.charAt(i);

            if (encoder != null && !encoder.canEncode(ch)) {
                writer.write(content, pos, i - pos);

                writer.write("&#x");
                writer.write(Integer.toHexString(ch));
                writer.write(';');
                pos = i + 1;
            }

            else {
                switch (ch) {
                    case '<':
                        writer.write(content, pos, i - pos);
                        writer.write("&lt;");
                        pos = i + 1;

                        break;

                    case '&':
                        writer.write(content, pos, i - pos);
                        writer.write("&amp;");
                        pos = i + 1;

                        break;

                    case '"':
                        writer.write(content, pos, i - pos);
                        writer.write("&quot;");
                        pos = i + 1;

                        break;
                }
            }
        }

        writer.write(content, pos, end - pos);
    }
}
