package org.xmlobjects.stream;

import org.w3c.dom.Node;
import org.xmlobjects.XMLObjectContext;
import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.util.DepthXMLStreamReader;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.TextContent;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class XMLReader implements AutoCloseable {
    private final XMLObjectContext context;
    private final DepthXMLStreamReader reader;

    private Map<String, ObjectBuilder<?>> builderCache = new HashMap<>();
    private Transformer transformer;
    private boolean createDOMasFallback;

    public XMLReader(XMLObjectContext context, XMLStreamReader reader) {
        this.context = Objects.requireNonNull(context, "XML object context must not be null.");
        this.reader = new DepthXMLStreamReader(reader);
    }

    public XMLReader createDOMasFallback(boolean createDOMasFallback) throws XMLReadException {
        this.createDOMasFallback = createDOMasFallback;
        if (createDOMasFallback && transformer == null) {
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new XMLReadException("Failed to create DOM transformer.", e);
            }
        }

        return this;
    }

    public boolean isCreateDOMasFallback() {
        return createDOMasFallback;
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    @Override
    public void close() throws XMLReadException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ParseResult<T> read(Class<T> type) throws ObjectBuildException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new ObjectBuildException("XML reader is in an illegal state: expected start tag.");

        QName name = reader.getName();
        ObjectBuilder<?> builder = context.getBuilder(name);
        if (builder != null) {
            Object object = builder.createObject(name);
            if (object == null)
                throw new ObjectBuildException("The builder " + builder.getClass().getName() + " created a null value.");

            if (type.isAssignableFrom(object.getClass()))
                return ParseResult.of(read(type.cast(object), (ObjectBuilder<T>) builder));
        }

        if (createDOMasFallback) {
            try {
                DOMResult result = new DOMResult();
                transformer.transform(new StAXSource(reader), result);
                Node node = result.getNode();
                transformer.reset();

                return ParseResult.of(node.getFirstChild());
            } catch (TransformerException e) {
                throw new ObjectBuildException("Failed to read XML content as DOM object.", e);
            }
        }

        return ParseResult.empty();
    }

    public <T> T readWithBuilder(Class<ObjectBuilder<T>> type) throws ObjectBuildException {
        ObjectBuilder<T> builder;

        // read builder from cache or create a new instance
        ObjectBuilder<?> cachedBuilder = builderCache.get(type.getName());
        if (cachedBuilder != null && type.isAssignableFrom(cachedBuilder.getClass()))
            builder = type.cast(cachedBuilder);
        else {
            try {
                builder = type.getDeclaredConstructor().newInstance();
                builderCache.put(type.getName(), builder);
            } catch (Exception e) {
                throw new ObjectBuildException("The builder " + type.getName() + " lacks a default constructor.");
            }
        }

        return readWithBuilder(builder);
    }

    public <T> T readWithBuilder(ObjectBuilder<T> builder) throws ObjectBuildException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new ObjectBuildException("XML reader is in an illegal state: expected start tag.");

        T object = builder.createObject(reader.getName());
        if (object == null)
            throw new ObjectBuildException("The builder " + builder.getClass().getName() + " created a null value.");

        return read(object, builder);
    }

    private <T> T read(T object, ObjectBuilder<T> builder) throws ObjectBuildException {
        try {
            int stopAt = reader.getDepth() - 1;
            int childLevel = reader.getDepth() + 1;

            // initialize object
            builder.initializeObject(object, getAttributes(), this);
            reader.next();

            while (true) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && reader.getDepth() == childLevel) {
                    // build nested objects
                    builder.buildNestedObject(object, reader.getName(), getAttributes(), this);
                }

                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getDepth() == stopAt)
                        return object;
                    else if (reader.getDepth() < stopAt)
                        throw new ObjectBuildException("XML reader is in an illegal state: depth = " + reader.getDepth() +
                                " but expected depth = " + stopAt + ".");
                }

                if (reader.hasNext())
                    reader.next();
                else
                    return null;
            }
        } catch (XMLStreamException e) {
            throw new ObjectBuildException("Failed to read XML content.", e);
        }
    }

    private Attributes getAttributes() {
        Attributes attributes = new Attributes();
        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            for (int i = 0; i < reader.getAttributeCount(); i++)
                attributes.add(reader.getAttributeName(i), reader.getAttributeValue(i));
        }

        return attributes;
    }

    public TextContent getTextContent() throws ObjectBuildException {
        try {
            StringBuilder result = new StringBuilder();
            boolean shouldParse = true;

            while (shouldParse && reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.CHARACTERS:
                    case XMLStreamReader.CDATA:
                        result.append(reader.getText());
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                    case XMLStreamReader.END_ELEMENT:
                        shouldParse = false;
                        break;
                }
            }

            return new TextContent(result.toString());
        } catch (XMLStreamException e) {
            throw new ObjectBuildException("Failed to read text content.", e);
        }
    }
}
