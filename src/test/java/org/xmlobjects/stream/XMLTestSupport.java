/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.xmlobjects.util.xml.SecureXMLProcessors;
import org.xmlobjects.xml.Element;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple tree model used to compare written and re-parsed XML independently of builders and serializers.
 */
final class XMLTestSupport {

    record Node(QName name, Map<QName, String> attributes, String text, List<Node> children) {
        Node(QName name) {
            this(name, new LinkedHashMap<>(), null, new ArrayList<>());
        }

        Node attribute(QName name, String value) {
            attributes.put(name, value);
            return this;
        }

        Node text(String text) {
            return new Node(name, attributes, text, children);
        }

        Node child(Node child) {
            children.add(child);
            return this;
        }
    }

    private XMLTestSupport() {
    }

    static void write(XMLWriter writer, Node node) throws XMLWriteException {
        Element element = Element.of(node.name().getNamespaceURI(), node.name().getLocalPart());
        node.attributes().forEach((name, value) ->
                element.addAttribute(name.getNamespaceURI(), name.getLocalPart(), value));

        writer.writeStartElement(element);
        if (node.text() != null) {
            writer.writeCharacters(node.text());
        }

        for (Node child : node.children()) {
            write(writer, child);
        }

        writer.writeEndElement();
    }

    static Node parse(byte[] xml) throws XMLStreamException {
        XMLInputFactory factory = SecureXMLProcessors.newDefaultXMLInputFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));
        try {
            reader.nextTag();
            return parse(reader);
        } finally {
            reader.close();
        }
    }

    private static Node parse(XMLStreamReader reader) throws XMLStreamException {
        Node node = new Node(reader.getName());
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            node.attribute(reader.getAttributeName(i), reader.getAttributeValue(i));
        }

        StringBuilder text = new StringBuilder();
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT -> node.child(parse(reader));
                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE ->
                        text.append(reader.getText());
                case XMLStreamConstants.END_ELEMENT -> {
                    // whitespace between child elements (indentation) is not part of the model
                    return node.children().isEmpty() && !text.isEmpty() ? node.text(text.toString()) : node;
                }
                default -> {
                }
            }
        }
    }
}
