package org.xmlobjects.stream;

import org.xmlobjects.XMLObjects;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class XMLReaderFactory {
    private final XMLObjects xmlObjects;
    private final XMLInputFactory xmlInputFactory;

    private boolean createDOMasFallback;

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

    public XMLReaderFactory createDOMasFallback(boolean createDOMasFallback) {
        this.createDOMasFallback = createDOMasFallback;
        return this;
    }

    public boolean isCreateDOMasFallback() {
        return createDOMasFallback;
    }

    public XMLReaderFactory withReporter(XMLReporter reporter) {
        xmlInputFactory.setProperty(XMLInputFactory.REPORTER, reporter);
        return this;
    }

    public XMLReporter getReporter() {
        return xmlInputFactory.getXMLReporter();
    }

    public XMLReaderFactory withResolver(XMLResolver resolver) {
        xmlInputFactory.setProperty(XMLInputFactory.RESOLVER, resolver);
        return this;
    }

    public XMLResolver getResolver() {
        return xmlInputFactory.getXMLResolver();
    }

    public XMLReader createReader(File file) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(
                    new BufferedReader(new FileReader(file))), createDOMasFallback);
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Path path) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(Files.newBufferedReader(path)), createDOMasFallback);
        } catch (XMLStreamException | IOException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(InputStream stream) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(stream), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(InputStream stream, String encoding) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(stream, encoding), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Reader reader) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(reader), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(Source source) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(source), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(String systemId, InputStream stream) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(systemId, stream), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(String systemId, Reader reader) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createXMLStreamReader(systemId, reader), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public XMLReader createReader(XMLStreamReader reader) {
        return new XMLReader(xmlObjects, reader, createDOMasFallback);
    }

    public XMLReader createFilteredReader(XMLReader reader, StreamFilter filter) throws XMLReadException {
        try {
            return new XMLReader(xmlObjects, xmlInputFactory.createFilteredReader(reader.getStreamReader(), filter), createDOMasFallback);
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }
}
