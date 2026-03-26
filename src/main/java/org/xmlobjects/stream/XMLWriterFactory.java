/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.xml.sax.ContentHandler;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.xml.SAXOutputHandler;
import org.xmlobjects.util.xml.SAXWriter;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class XMLWriterFactory {
    private final XMLObjects xmlObjects;
    private final Properties properties = new Properties();

    private SAXTransformerFactory transformerFactory;

    private XMLWriterFactory(XMLObjects xmlObjects) {
        this.xmlObjects = Objects.requireNonNull(xmlObjects, "XML objects must not be null.");
    }

    public static XMLWriterFactory newInstance(XMLObjects xmlObjects) {
        return new XMLWriterFactory(xmlObjects);
    }

    public Properties getProperties() {
        return properties;
    }

    public XMLWriterFactory withProperty(String name, Object value) {
        properties.set(name, value);
        return this;
    }

    public XMLWriter createWriter(File file) throws XMLWriteException {
        return createWriter(file, StandardCharsets.UTF_8.name());
    }

    public XMLWriter createWriter(File file, String encoding) throws XMLWriteException {
        try {
            return createWriter(new SAXWriter(new OutputStreamWriter(
                    new FileOutputStream(file), getEncoding(encoding))));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Path path) throws XMLWriteException {
        return createWriter(path, StandardCharsets.UTF_8.name());
    }

    public XMLWriter createWriter(Path path, String encoding) throws XMLWriteException {
        try {
            return createWriter(new SAXWriter(
                    new OutputStreamWriter(Files.newOutputStream(path), getEncoding(encoding))));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(StreamResult result) throws XMLWriteException {
        return createWriter(result, StandardCharsets.UTF_8.name());
    }

    public XMLWriter createWriter(StreamResult result, String encoding) throws XMLWriteException {
        try {
            return createWriter(new SAXWriter(result, getEncoding(encoding)));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(DOMResult result) throws XMLWriteException {
        try {
            if (transformerFactory == null) {
                transformerFactory = (SAXTransformerFactory) TransformerFactory.newDefaultInstance();
            }

            TransformerHandler handler = transformerFactory.newTransformerHandler();
            handler.setResult(result);

            return createWriter(handler);
        } catch (Exception e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(OutputStream stream) throws XMLWriteException {
        return createWriter(stream, StandardCharsets.UTF_8.name());
    }

    public XMLWriter createWriter(OutputStream stream, String encoding) throws XMLWriteException {
        try {
            return createWriter(new SAXWriter(stream, getEncoding(encoding)));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Writer writer) {
        return createWriter(new SAXWriter(writer));
    }

    public XMLWriter createWriter(SAXWriter saxWriter) {
        XMLWriter xmlWriter = new XMLWriter(xmlObjects, saxWriter);
        xmlWriter.setProperties(properties);
        return xmlWriter;
    }

    public XMLWriter createWriter(ContentHandler contentHandler) {
        XMLWriter xmlWriter = new XMLWriter(xmlObjects, new SAXOutputHandler(contentHandler));
        xmlWriter.setProperties(properties);
        return xmlWriter;
    }

    private String getEncoding(String encoding) {
        return encoding != null ? encoding : StandardCharsets.UTF_8.name();
    }
}
