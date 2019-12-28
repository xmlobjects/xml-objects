package org.xmlobjects.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SAXWriter implements ContentHandler, AutoCloseable {
    private final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final NamespaceSupport prefixMapping = new NamespaceSupport();
    private final NamespaceContext namespaceContext = new NamespaceContext();
    private final Map<String, String> schemaLocations = new HashMap<>();

    private Writer writer;
    private String encoding;
    private CharsetEncoder encoder;

    private boolean writeReportedNamespaces = false;
    private boolean needNamespaceContext = true;
    private boolean escapeCharacters = true;
    private boolean writeEncoding = false;
    private boolean writeXMLDeclaration = true;
    private String indentString;
    private String[] headerComment;

    private int depth = 0;
    private int prefixCounter = 1;
    private XMLEvents lastEvent;

    private enum XMLEvents {
        START_ELEMENT,
        END_ELEMENT,
        CHARACTERS,
        PROCESSING_INSTRUCTION,
        COMMENT
    }

    public SAXWriter() {
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

    public void setOutput(StreamResult streamResult) throws IOException {
        setOutput(streamResult, null);
    }

    public void setOutput(OutputStream outputStream) throws IOException {
        setOutput(outputStream, null);
    }

    public void setOutput(StreamResult streamResult, String encoding) throws IOException {
        if (streamResult.getOutputStream() != null)
            setOutput(streamResult.getOutputStream(), encoding);
        else if (streamResult.getWriter() != null)
            setOutput(streamResult.getWriter());
        else if (streamResult.getSystemId() != null)
            setOutput(new FileOutputStream(streamResult.getSystemId()), encoding);
    }

    public void setOutput(Writer writer) {
        if (writer instanceof OutputStreamWriter) {
            this.writer = new BufferedWriter(writer);
            String encoding = ((OutputStreamWriter) writer).getEncoding();
            if (encoding != null)
                setEncoding(encoding);
        } else
            this.writer = writer;
    }

    public void setOutput(OutputStream outputStream, String encoding) throws IOException {
        if (encoding == null)
            encoding = System.getProperty("file.encoding", "UTF-8");

        writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
        setEncoding(encoding);
    }

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

    public boolean isWriteReportedNamespaces() {
        return writeReportedNamespaces;
    }

    public SAXWriter writeReportedNamespaces(boolean writeReportedNamespaces) {
        this.writeReportedNamespaces = writeReportedNamespaces;
        return this;
    }

    public String getPrefix(String namespaceURI) {
        String prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == null)
            prefix = getReportedPrefix(namespaceURI);

        return prefix;
    }

    public SAXWriter usePrefix(String prefix, String namespaceURI) {
        if (prefix != null && namespaceURI != null) {
            if (namespaceContext.containsPrefix(prefix))
                namespaceContext.removePrefix(prefix);

            namespaceContext.declarePrefix(prefix, namespaceURI);
        }

        return this;
    }

    public String getNamespaceURI(String prefix) {
        String namespaceURI = namespaceContext.getNamespaceURI(prefix);
        if (namespaceURI == null)
            namespaceURI = getReportedURI(prefix);

        return namespaceURI;
    }

    public SAXWriter useDefaultNamespace(String namespaceURI) {
        if (namespaceURI != null)
            namespaceContext.declarePrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);

        return this;
    }

    public String getIndentString() {
        return indentString;
    }

    public SAXWriter useIndentString(String indent) {
        if (indent != null)
            this.indentString = indent;

        return this;
    }

    public boolean isWriteEncoding() {
        return writeEncoding;
    }

    public SAXWriter writeEncoding(boolean writeEncoding) {
        this.writeEncoding = writeEncoding;
        return this;
    }

    public boolean isWriteXMLDeclaration() {
        return writeXMLDeclaration;
    }

    public SAXWriter writeXMLDeclaration(boolean writeXMLDeclaration) {
        this.writeXMLDeclaration = writeXMLDeclaration;
        return this;
    }

    public String[] getHeaderComment() {
        return headerComment;
    }

    public SAXWriter useHeaderComment(String... headerMessage) {
        if (headerMessage != null)
            this.headerComment = headerMessage;

        return this;
    }

    public String getSchemaLocation(String namespaceURI) {
        return schemaLocations.get(namespaceURI);
    }

    public SAXWriter useSchemaLocation(String namespaceURI, String schemaLocation) {
        if (namespaceURI != null && schemaLocation != null)
            schemaLocations.put(namespaceURI, schemaLocation);

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
    public void endDocument() throws SAXException {
        // nothing to do
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
                    writeQName(namespaceContext.getPrefix(namespaceURI), localName);
                else
                    writer.write(qName);

                writer.write('>');
            }

            lastEvent = XMLEvents.END_ELEMENT;
            namespaceContext.popContext();
            prefixMapping.popContext();
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // nothing to do
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
    public void setDocumentLocator(Locator locator) {
        // nothing to do
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // nothing to do
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            if (depth == 0) {
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

                    lastEvent = XMLEvents.PROCESSING_INSTRUCTION;
                }

                if (headerComment != null) {
                    writeHeader(headerComment);

                    lastEvent = XMLEvents.COMMENT;
                }
            }

        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if (needNamespaceContext)
                prefixMapping.pushContext();

            if (depth > 0) {
                if (lastEvent == XMLEvents.START_ELEMENT)
                    writer.write('>');

                writeIndent();
            }

            writer.write('<');

            boolean writeLocalNS = false;
            String prefix = null;

            if (!localName.isEmpty()) {
                prefix = namespaceContext.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = getReportedPrefix(namespaceURI);
                    if (prefix == null) {
                        prefix = getOrCreatePrefix(qName);
                        prefixMapping.declarePrefix(prefix, namespaceURI);
                    }

                    writeLocalNS = true;
                }

                writeQName(prefix, localName);
            } else
                writer.write(qName);

            if (depth == 0) {
                writeDeclaredNamespaces();
                writeSchemaLocations();
            }

            if (writeLocalNS) {
                namespaceContext.declarePrefix(prefix, namespaceURI);
                writeNamespace(prefix, namespaceURI);
            }

            if (writeReportedNamespaces && depth > 0)
                writeReportedNamespaces();

            writeAttributes(atts);

            lastEvent = XMLEvents.START_ELEMENT;
            needNamespaceContext = true;
            depth++;
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        if (needNamespaceContext) {
            prefixMapping.pushContext();
            needNamespaceContext = false;
        }

        if (getReportedPrefix(namespaceURI) == null)
            prefixMapping.declarePrefix(prefix, namespaceURI);
    }

    private String getReportedPrefix(String namespaceURI) {
        String prefix = prefixMapping.getPrefix(namespaceURI);
        if (prefix == null && namespaceURI.equals(prefixMapping.getURI(XMLConstants.DEFAULT_NS_PREFIX)))
            prefix = XMLConstants.DEFAULT_NS_PREFIX;

        return prefix;
    }

    private String getReportedURI(String prefix) {
        return prefixMapping.getURI(prefix);
    }

    private String getOrCreatePrefix(String qName) {
        if (!qName.isEmpty()) {
            int index = qName.indexOf(':');
            if (index != -1)
                return qName.substring(0, index);
        }

        return "ns" + prefixCounter++;
    }

    private void writeAttributes(Attributes atts) throws SAXException {
        try {
            for (int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                String namespaceURI = atts.getURI(i);
                String prefix = null;

                if (namespaceURI != null && namespaceURI.length() > 0) {
                    if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
                        continue;

                    prefix = namespaceContext.getPrefix(namespaceURI);
                    if (prefix == null) {
                        prefix = getReportedPrefix(namespaceURI);
                        if (prefix == null) {
                            prefix = getOrCreatePrefix(atts.getQName(i));
                            prefixMapping.declarePrefix(prefix, namespaceURI);
                        }

                        namespaceContext.declarePrefix(prefix, namespaceURI);
                        writeNamespace(prefix, namespaceURI);
                    }
                }

                writer.write(' ');
                writeQName(prefix, localName);
                writer.write("=\"");
                writeAttributeContent(atts.getValue(i));
                writer.write('"');
            }
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
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

    private void writeDeclaredNamespaces() throws SAXException {
        for (Entry<String, String> entry : namespaceContext.getRootNamespaceContext().entrySet())
            writeNamespace(entry.getValue(), entry.getKey());
    }

    private void writeReportedNamespaces() throws SAXException {
        for (Enumeration<?> e = prefixMapping.getDeclaredPrefixes(); e.hasMoreElements(); ) {
            String prefix = e.nextElement().toString();
            String namespaceURI = prefixMapping.getURI(prefix);

            // skip if the namespace URI is already defined locally
            if (namespaceContext.getPrefix(namespaceURI) != null)
                continue;

            // change prefix if it is already in use
            if (namespaceContext.containsPrefix(prefix))
                prefix += prefixCounter++;

            namespaceContext.declarePrefix(prefix, namespaceURI);
            writeNamespace(prefix, namespaceURI);
        }
    }

    private void writeNamespace(String prefix, String namespaceURI) throws SAXException {
        if (prefix.equals(XMLConstants.XML_NS_PREFIX) && namespaceURI.equals(XMLConstants.XML_NS_URI))
            return;

        try {
            writer.write(' ');
            writer.write(XMLConstants.XMLNS_ATTRIBUTE);

            if (prefix.length() > 0) {
                writer.write(':');
                writer.write(prefix);
            }

            writer.write("=\"");
            writeAttributeContent(namespaceURI);
            writer.write('"');
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeSchemaLocations() throws SAXException {
        if (!schemaLocations.isEmpty()) {
            try {
                String namespaceURI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
                String prefix = namespaceContext.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = "xsi";
                    namespaceContext.declarePrefix(prefix, namespaceURI);
                    writeNamespace(prefix, namespaceURI);
                }

                writer.write(' ');
                writeQName(prefix, "schemaLocation");
                writer.write("=\"");

                Iterator<Entry<String, String>> iter = schemaLocations.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter.next();
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

            lastEvent = XMLEvents.COMMENT;
        } catch (IOException e) {
            throw new SAXException("Caused by:", e);
        }
    }

    private void writeIndent() throws SAXException {
        if (indentString == null || indentString.length() == 0)
            return;

        if (lastEvent == XMLEvents.CHARACTERS)
            return;

        try {
            writer.write(LINE_SEPARATOR);
            for (int i = 0; i < depth; i++)
                writer.write(indentString);
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

    private class NamespaceContext {
        private final Deque<NamespaceMap> contexts = new ArrayDeque<>();

        private void pushContext() {
            if (contexts.isEmpty() || contexts.getFirst().level != depth)
                contexts.push(new NamespaceMap(depth));
        }

        private void popContext() {
            if (!contexts.isEmpty() && contexts.getFirst().level == depth)
                contexts.pop();
        }

        private void declarePrefix(String prefix, String namespaceURI) {
            pushContext();
            contexts.getFirst().namespaces.put(namespaceURI, prefix);
        }

        private String getPrefix(String namespaceURI) {
            for (NamespaceMap context : contexts) {
                String prefix = context.namespaces.get(namespaceURI);
                if (prefix != null) {

                    // make sure the prefix has not been redefined in a more recent context
                    if (context != contexts.getFirst()) {
                        for (NamespaceMap recent : contexts) {
                            if (recent == context)
                                break;

                            if (recent.namespaces.containsValue(prefix))
                                return null;
                        }
                    }

                    return prefix;
                }
            }

            return null;
        }

        private boolean containsPrefix(String prefix) {
            for (NamespaceMap context : contexts) {
                if (context.namespaces.containsValue(prefix))
                    return true;
            }

            return false;
        }

        private void removePrefix(String prefix) {
            for (NamespaceMap context : contexts)
                context.namespaces.values().removeIf(v -> v.equals(prefix));
        }

        private String getNamespaceURI(String prefix) {
            for (NamespaceMap context : contexts) {
                for (Entry<String, String> entry : context.namespaces.entrySet()) {
                    if (entry.getValue().equals(prefix))
                        return entry.getKey();
                }
            }

            return null;
        }

        private Map<String, String> getRootNamespaceContext() {
            return !contexts.isEmpty() ? contexts.getLast().namespaces : Collections.emptyMap();
        }
    }

    private static class NamespaceMap {
        private final Map<String, String> namespaces;
        private final int level;

        private NamespaceMap(int level) {
            this.level = level;
            namespaces = new HashMap<>();
        }
    }
}
