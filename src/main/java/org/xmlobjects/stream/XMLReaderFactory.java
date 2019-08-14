package org.xmlobjects.stream;

import org.xmlobjects.XMLObjects;
import org.xmlobjects.schema.AbstractSchemaHandler;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class XMLReaderFactory {
    private final XMLObjects xmlObjects;
    private final XMLInputFactory xmlInputFactory;

    private AbstractSchemaHandler schemaHandler;
    private boolean createDOMAsFallback;

    private XMLReaderFactory(XMLObjects xmlObjects) {
        this.xmlObjects = Objects.requireNonNull(xmlObjects, "XML objects must not be null.");
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    }

    public static XMLReaderFactory newInstance(XMLObjects xmlObjects) throws XMLReadException {
        try {
            return new XMLReaderFactory(xmlObjects);
        } catch (Throwable e) {
            throw new XMLReadException("Failed to initialize XML reader factory.", e);
        }
    }

    public AbstractSchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public XMLReaderFactory withSchemaHandler(AbstractSchemaHandler schemaHandler) {
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

    public XMLReporter getReporter() {
        return xmlInputFactory.getXMLReporter();
    }

    public XMLReaderFactory withReporter(XMLReporter reporter) {
        xmlInputFactory.setProperty(XMLInputFactory.REPORTER, reporter);
        return this;
    }

    public XMLResolver getResolver() {
        return xmlInputFactory.getXMLResolver();
    }

    public XMLReaderFactory withResolver(XMLResolver resolver) {
        xmlInputFactory.setProperty(XMLInputFactory.RESOLVER, resolver);
        return this;
    }

    public XMLReader createReader(File file) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(new BufferedReader(new FileReader(file))), file.toURI().normalize());
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Path path) throws XMLReadException {
        try {
            return createReader(xmlInputFactory.createXMLStreamReader(Files.newBufferedReader(path)), path.toUri().normalize());
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
        return new XMLReader(xmlObjects, reader, baseURI)
                .withSchemaHandler(schemaHandler)
                .createDOMAsFallback(createDOMAsFallback);
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
            return new URI(systemId).normalize();
        } catch (Exception e) {
            return URI.create("");
        }
    }
}
