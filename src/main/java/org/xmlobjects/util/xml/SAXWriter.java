/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SAXWriter extends XMLOutput<SAXWriter> {
    private OutputBuffer writer;
    private String encoding;
    private CharsetEncoder encoder;

    private static final int BUFFER_SIZE = 4096;
    private static final boolean[] TEXT_ESCAPES = new boolean[128];
    private static final boolean[] ATTRIBUTE_ESCAPES = new boolean[128];

    static {
        for (char ch : new char[]{'<', '>', '&', '\r'}) {
            TEXT_ESCAPES[ch] = ATTRIBUTE_ESCAPES[ch] = true;
        }

        for (char ch : new char[]{'"', '\n', '\t'}) {
            ATTRIBUTE_ESCAPES[ch] = true;
        }
    }

    private final char[] buffer = new char[BUFFER_SIZE];
    private String[] indentStrings = new String[16];
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
        if (streamResult.getOutputStream() != null) {
            setOutput(streamResult.getOutputStream(), encoding);
        } else if (streamResult.getWriter() != null) {
            setOutput(streamResult.getWriter());
        } else if (streamResult.getSystemId() != null) {
            setOutput(new FileOutputStream(streamResult.getSystemId()), encoding);
        }
    }

    private void setOutput(Writer writer) {
        this.writer = new OutputBuffer(writer);
        if (writer instanceof OutputStreamWriter streamWriter) {
            String encoding = streamWriter.getEncoding();
            if (encoding != null) {
                setEncoding(encoding);
            }
        }
    }

    private void setOutput(OutputStream outputStream, String encoding) throws IOException {
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }

        writer = new OutputBuffer(new OutputStreamWriter(outputStream, encoding));
        setEncoding(encoding);
    }

    @Override
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        indentStrings = new String[16];
        if (writer != null) {
            writer.close();
        }
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

        if (!encoding.equalsIgnoreCase("UTF-8")) {
            encoder = charset.newEncoder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            if (length > 0) {
                if (lastEvent == XMLEvents.START_ELEMENT) {
                    writer.write('>');
                }

                writeTextContent(ch, start, length, escapeCharacters);
                lastEvent = XMLEvents.CHARACTERS;
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    public void characters(String text, int start, int length) throws SAXException {
        try {
            if (length > 0) {
                if (lastEvent == XMLEvents.START_ELEMENT) {
                    writer.write('>');
                }

                writeEscaped(text, start, start + length, escapeCharacters, false);
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

            if (lastEvent == XMLEvents.START_ELEMENT) {
                writer.write("/>");
            } else {
                if (lastEvent == XMLEvents.END_ELEMENT) {
                    writeIndent();
                }

                writer.write("</");

                if (!localName.isEmpty()) {
                    writeQName(prefixMapping.getPrefix(namespaceURI), localName);
                } else {
                    writer.write(qName);
                }

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
                if (length > 0 && lastEvent == XMLEvents.START_ELEMENT) {
                    writer.write('>');
                }

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

            if (target == null || data == null) {
                throw new SAXException("PI target cannot be null.");
            }

            writer.write("<?" + target + " " + data + "?>");
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
                    writer.write("<?xml version=\"1.0\"");
                    if (writeEncoding && encoding != null) {
                        writer.write(" encoding=\"" + encoding + "\"");
                    }

                    writer.write(" standalone=\"yes\"?>");
                    writeIndent();
                }

                if (headerComment != null) {
                    writeHeader(headerComment);
                }
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
                if (lastEvent == XMLEvents.START_ELEMENT) {
                    writer.write('>');
                }

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
            } else {
                writer.write(qName);
            }

            writeAttributes(atts);
            writeNamespaces(prefixMapping.getCurrentContext());
            if (depth == 0) {
                writeSchemaLocations();
            }

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
                        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                            continue;
                        }

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
            if (prefix != null && !prefix.isEmpty()) {
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

                writer.write(" " + XMLConstants.XMLNS_ATTRIBUTE);
                if (!prefix.isEmpty()) {
                    writer.write(":" + prefix);
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
                    if (iter.hasNext()) {
                        writer.write(' ');
                    }
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

            if (data == null) {
                throw new SAXException("Comment target cannot be null.");
            }

            for (String line : data) {
                if (line == null) {
                    continue;
                }

                writer.write("<!-- " + line + " -->");
                writeIndent();
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeIndent() throws SAXException {
        if (indent == null) {
            return;
        }

        if (lastEvent == XMLEvents.CHARACTERS) {
            return;
        }

        try {
            if (depth >= indentStrings.length) {
                indentStrings = Arrays.copyOf(indentStrings, Math.max(depth + 1, indentStrings.length * 2));
            }

            String indentString = indentStrings[depth];
            if (indentString == null) {
                indentString = indentStrings[depth] = "\n" + indent.repeat(depth);
            }

            writer.write(indentString);
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeTextContent(char[] content, int start, int length, boolean escapeCharacters) throws IOException {
        writeEscaped(content, start, start + length, escapeCharacters, false);
    }

    private void writeAttributeContent(String content) throws IOException {
        writeEscaped(content, 0, content.length(), true, true);
    }

    private void writeEscaped(String content, int start, int end, boolean escapeMarkup, boolean attribute) throws IOException {
        // copy chunks into a reusable buffer instead of allocating a char array per value
        while (start < end) {
            int chunkEnd = Math.min(end, start + BUFFER_SIZE);
            if (chunkEnd < end && Character.isHighSurrogate(content.charAt(chunkEnd - 1))) {
                // never split a surrogate pair between two chunks
                chunkEnd--;
            }

            content.getChars(start, chunkEnd, buffer, 0);
            writeEscaped(buffer, 0, chunkEnd - start, escapeMarkup, attribute);
            start = chunkEnd;
        }
    }

    private void writeEscaped(char[] content, int start, int end, boolean escapeMarkup, boolean attribute) throws IOException {
        if (!escapeMarkup && encoder == null) {
            // nothing to escape and every character can be encoded
            writer.write(content, start, end - start);
            return;
        }

        boolean[] escapes = attribute ? ATTRIBUTE_ESCAPES : TEXT_ESCAPES;
        int pos = start;
        for (int i = start; i < end; i++) {
            char ch = content[i];
            String replacement = null;
            int consumed = 1;

            if (ch < 128) {
                // fast path: most characters are ASCII and need no escaping
                if (!escapeMarkup || !escapes[ch]) {
                    continue;
                }

                replacement = switch (ch) {
                    case '<' -> "&lt;";
                    case '>' -> "&gt;";
                    case '&' -> "&amp;";
                    case '\r' -> "&#13;";
                    case '"' -> "&quot;";
                    case '\n' -> "&#10;";
                    default -> "&#9;";
                };
            } else if (encoder == null) {
                continue;
            } else if (Character.isHighSurrogate(ch) && i + 1 < end && Character.isLowSurrogate(content[i + 1])) {
                consumed = 2;
                if (!encoder.canEncode(new String(content, i, 2))) {
                    replacement = "&#x" + Integer.toHexString(Character.toCodePoint(ch, content[i + 1])) + ";";
                }
            } else if (!encoder.canEncode(ch)) {
                replacement = "&#x" + Integer.toHexString(ch) + ";";
            }

            if (replacement != null) {
                writer.write(content, pos, i - pos);
                writer.write(replacement);
                pos = i + consumed;
            }

            i += consumed - 1;
        }

        writer.write(content, pos, end - pos);
    }

    /**
     * Unsynchronized replacement for {@link BufferedWriter}: a SAXWriter is used by a single thread,
     * and the lock taken by {@code BufferedWriter} on every small write is measurable.
     */
    private static final class OutputBuffer extends Writer {
        private final Writer out;
        private final char[] buffer = new char[8192];
        private int pos;

        OutputBuffer(Writer out) {
            this.out = out;
        }

        @Override
        public void write(int c) throws IOException {
            if (pos == buffer.length) {
                flushBuffer();
            }

            buffer[pos++] = (char) c;
        }

        @Override
        public void write(char[] chars, int off, int len) throws IOException {
            if (len > buffer.length - pos) {
                flushBuffer();
                if (len >= buffer.length) {
                    out.write(chars, off, len);
                    return;
                }
            }

            System.arraycopy(chars, off, buffer, pos, len);
            pos += len;
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            if (len > buffer.length - pos) {
                flushBuffer();
                if (len >= buffer.length) {
                    out.write(str, off, len);
                    return;
                }
            }

            str.getChars(off, off + len, buffer, pos);
            pos += len;
        }

        @Override
        public void write(String str) throws IOException {
            write(str, 0, str.length());
        }

        private void flushBuffer() throws IOException {
            if (pos > 0) {
                out.write(buffer, 0, pos);
                pos = 0;
            }
        }

        @Override
        public void flush() throws IOException {
            flushBuffer();
            out.flush();
        }

        @Override
        public void close() throws IOException {
            try {
                flushBuffer();
            } finally {
                out.close();
            }
        }
    }

    @Override
    protected SAXWriter self() {
        return this;
    }
}
