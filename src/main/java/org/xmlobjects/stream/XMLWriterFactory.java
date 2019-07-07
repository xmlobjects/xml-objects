package org.xmlobjects.stream;

import org.xmlobjects.XMLObjects;
import org.xmlobjects.util.SAXWriter;

import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class XMLWriterFactory {
    private final XMLObjects xmlObjects;

    private XMLWriterFactory(XMLObjects xmlObjects) {
        this.xmlObjects = Objects.requireNonNull(xmlObjects, "XML objects must not be null.");
    }

    public static XMLWriterFactory newInstance(XMLObjects xmlObjects) {
        return new XMLWriterFactory(xmlObjects);
    }

    public XMLWriter createWriter(File file) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(new OutputStreamWriter(new FileOutputStream(file))));
        } catch (FileNotFoundException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(File file, String encoding) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(new OutputStreamWriter(new FileOutputStream(file), encoding)));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Path path) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(new OutputStreamWriter(Files.newOutputStream(path))));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Path path, String encoding) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(new OutputStreamWriter(Files.newOutputStream(path), encoding)));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(StreamResult result) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(result));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(StreamResult result, String encoding) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(result, encoding));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(OutputStream stream) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(stream));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(OutputStream stream, String encoding) throws XMLWriteException {
        try {
            return new XMLWriter(xmlObjects, new SAXWriter(stream, encoding));
        } catch (IOException e) {
            throw new XMLWriteException("Caused by:", e);
        }
    }

    public XMLWriter createWriter(Writer writer) {
        return new XMLWriter(xmlObjects, new SAXWriter(writer));
    }

    public XMLWriter createWriter(SAXWriter saxWriter) {
        return new XMLWriter(xmlObjects, saxWriter);
    }
}
