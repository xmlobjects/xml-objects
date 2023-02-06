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

package org.xmlobjects.stream;

import org.xmlobjects.XMLObjects;
import org.xmlobjects.schema.SchemaHandler;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.SystemIDResolver;
import org.xmlobjects.util.xml.SecureXMLProcessors;

import javax.xml.stream.*;
import javax.xml.transform.Source;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class XMLReaderFactory {
    private final XMLObjects xmlObjects;
    private final XMLInputFactory xmlInputFactory;
    private final Properties properties = new Properties();

    private SchemaHandler schemaHandler;
    private boolean createDOMAsFallback;

    private XMLReaderFactory(XMLObjects xmlObjects) {
        this.xmlObjects = Objects.requireNonNull(xmlObjects, "XML objects must not be null.");
        xmlInputFactory = SecureXMLProcessors.newXMLInputFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    }

    public static XMLReaderFactory newInstance(XMLObjects xmlObjects) throws XMLReadException {
        try {
            return new XMLReaderFactory(xmlObjects);
        } catch (Throwable e) {
            throw new XMLReadException("Failed to initialize XML reader factory.", e);
        }
    }

    public SchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public XMLReaderFactory withSchemaHandler(SchemaHandler schemaHandler) {
        this.schemaHandler = schemaHandler;
        return this;
    }

    public boolean isCreateDOMAsFallback() {
        return createDOMAsFallback;
    }

    public XMLReaderFactory createDOMAsFallback(boolean createDOMAsFallback) {
        this.createDOMAsFallback = createDOMAsFallback;
        return this;
    }

    public XMLReporter getXMLReporter() {
        return xmlInputFactory.getXMLReporter();
    }

    public XMLReaderFactory withXMLReporter(XMLReporter reporter) {
        xmlInputFactory.setXMLReporter(reporter);
        return this;
    }

    public XMLResolver getXMLResolver() {
        return xmlInputFactory.getXMLResolver();
    }

    public XMLReaderFactory withXMLResolver(XMLResolver resolver) {
        xmlInputFactory.setXMLResolver(resolver);
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public XMLReaderFactory withProperty(String name, Object value) {
        properties.set(name, value);
        return this;
    }

    public XMLReader createReader(File file) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(new BufferedInputStream(new FileInputStream(file))), file.toURI().normalize());
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(File file, String encoding) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding), file.toURI().normalize());
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Path path) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(new BufferedInputStream(Files.newInputStream(path))), path.toUri().normalize());
        } catch (XMLStreamException | IOException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Path path, String encoding) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(new BufferedInputStream(Files.newInputStream(path)), encoding), path.toUri().normalize());
        } catch (XMLStreamException | IOException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(InputStream stream) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(stream));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(InputStream stream, String encoding) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(stream, encoding));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(String systemId, InputStream stream) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(systemId, stream), createBaseURI(systemId));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(String systemId, InputStream stream, String encoding) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(stream, encoding), createBaseURI(systemId));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Reader reader) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(reader));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(String systemId, Reader reader) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(systemId, reader), createBaseURI(systemId));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Source source) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(source), createBaseURI(source.getSystemId()));
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(XMLStreamReader reader) {
        return createReader(reader, URI.create(""));
    }

    public XMLReader createReader(XMLStreamReader reader, URI baseURI) {
        XMLReader xmlReader = new XMLReader(xmlObjects, reader, baseURI);
        xmlReader.setSchemaHandler(schemaHandler);
        xmlReader.createDOMAsFallback(createDOMAsFallback);
        xmlReader.setProperties(properties);

        return xmlReader;
    }

    public XMLReader createFilteredReader(XMLReader reader, StreamFilter filter) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createFilteredReader(reader.getStreamReader(), filter), reader.getBaseURI());
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    private URI createBaseURI(String systemId) {
        try {
            return new URI(SystemIDResolver.getAbsoluteURI(systemId)).normalize();
        } catch (Exception e) {
            return URI.create("");
        }
    }
}
