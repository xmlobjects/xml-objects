/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2021 Claus Nagel <claus.nagel@gmail.com>
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
import org.xmlobjects.XMLObjects;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.xml.SAXOutputHandler;
import org.xmlobjects.util.xml.SAXWriter;

import javax.xml.transform.TransformerConfigurationException;
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
            return createWriter(new SAXWriter(new OutputStreamWriter(new FileOutputStream(file), getEncoding(encoding))));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Path path) throws XMLWriteException {
        return createWriter(path, StandardCharsets.UTF_8.name());
    }

    public XMLWriter createWriter(Path path, String encoding) throws XMLWriteException {
        try {
            return createWriter(new SAXWriter(new OutputStreamWriter(Files.newOutputStream(path), getEncoding(encoding))));
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
            if (transformerFactory == null)
                transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

            TransformerHandler handler = transformerFactory.newTransformerHandler();
            handler.setResult(result);

            return createWriter(handler);
        } catch (TransformerConfigurationException e) {
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
