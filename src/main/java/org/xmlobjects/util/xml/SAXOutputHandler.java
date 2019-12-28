package org.xmlobjects.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlobjects.stream.XMLOutput;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SAXOutputHandler implements XMLOutput<SAXOutputHandler> {
    private final ContentHandler parent;
    private final Map<String, String> prefixes = new HashMap<>();
    private final Map<String, String> schemaLocations = new HashMap<>();

    private int depth;

    public SAXOutputHandler(ContentHandler parent) {
        this.parent = Objects.requireNonNull(parent, "Parent must not be null.");
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            if (entry.getValue().equals(namespaceURI))
                return entry.getKey();
        }

        return null;
    }

    @Override
    public SAXOutputHandler withPrefix(String prefix, String namespaceURI) {
        if (prefix != null
                && namespaceURI != null
                && !XMLConstants.XML_NS_PREFIX.equals(prefix)
                && !XMLConstants.XML_NS_URI.equals(namespaceURI))
            prefixes.put(prefix, namespaceURI);

        return this;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixes.get(prefix);
    }

    @Override
    public SAXOutputHandler withDefaultNamespace(String namespaceURI) {
        withPrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
        return this;
    }

    @Override
    public String getIndentString() {
        return null;
    }

    @Override
    public SAXOutputHandler withIndentString(String indent) {
        return this;
    }

    @Override
    public boolean isWriteXMLDeclaration() {
        return false;
    }

    @Override
    public SAXOutputHandler writeXMLDeclaration(boolean writeXMLDeclaration) {
        return this;
    }

    @Override
    public String[] getHeaderComment() {
        return null;
    }

    @Override
    public SAXOutputHandler withHeaderComment(String... headerComment) {
        return this;
    }

    @Override
    public String getSchemaLocation(String namespaceURI) {
        return schemaLocations.get(namespaceURI);
    }

    @Override
    public SAXOutputHandler withSchemaLocation(String namespaceURI, String schemaLocation) {
        if (namespaceURI != null && schemaLocation != null)
            schemaLocations.put(namespaceURI, schemaLocation);

        return this;
    }

    @Override
    public void flush() throws Exception {
        // nothing to do
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        parent.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        parent.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        parent.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        parent.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        parent.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (depth == 0) {
            if (!schemaLocations.isEmpty()) {
                String namespaceURI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
                String prefix = getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = "xsi";
                    withPrefix(prefix, namespaceURI);
                }

                atts = new AttributesImpl(atts);
                ((AttributesImpl) atts).addAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation",
                        prefix + ":schemaLocation", "CDATA", schemaLocations.entrySet().stream()
                                .map(e -> e.getKey() + " " + e.getValue())
                                .collect(Collectors.joining(" ")));
            }

            for (Map.Entry<String, String> entry : prefixes.entrySet())
                parent.startPrefixMapping(entry.getKey(), entry.getValue());
        }

        parent.startElement(uri, localName, qName, atts);
        depth++;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        depth--;
        parent.endElement(uri, localName, qName);

        if (depth == 0) {
            for (String prefix : prefixes.keySet())
                parent.endPrefixMapping(prefix);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        parent.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        parent.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        parent.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        parent.skippedEntity(name);
    }
}
