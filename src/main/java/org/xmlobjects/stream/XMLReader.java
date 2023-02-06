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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.schema.SchemaHandler;
import org.xmlobjects.util.Properties;
import org.xmlobjects.util.xml.DepthXMLStreamReader;
import org.xmlobjects.util.xml.SAXWriter;
import org.xmlobjects.util.xml.StAXStream2SAX;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.Namespaces;
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
import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class XMLReader implements AutoCloseable {
    private final XMLObjects xmlObjects;
    private final DepthXMLStreamReader reader;

    private final Map<Class<?>, ObjectBuilder<?>> builderCache = new IdentityHashMap<>();
    private WeakReference<?> parent = new WeakReference<>(null);
    private boolean createDOMAsFallback;
    private Properties properties;
    private Transformer transformer;

    XMLReader(XMLObjects xmlObjects, XMLStreamReader reader, URI baseURI) {
        this.xmlObjects = Objects.requireNonNull(xmlObjects, "XML objects must not be null.");
        this.reader = new DepthXMLStreamReader(reader, baseURI);
    }

    XMLReader(XMLObjects xmlObjects, XMLStreamReader reader) {
        this(xmlObjects, reader, URI.create(""));
    }

    public XMLObjects getXMLObjects() {
        return xmlObjects;
    }

    public XMLStreamReader getStreamReader() {
        return reader;
    }

    public Namespaces getNamespaces() {
        return reader.getNamespaces();
    }

    public SchemaHandler getSchemaHandler() {
        return reader.getSchemaHandler();
    }

    void setSchemaHandler(SchemaHandler schemaHandler) {
        reader.setSchemaHandler(schemaHandler);
    }

    public boolean isCreateDOMAsFallback() {
        return createDOMAsFallback;
    }

    void createDOMAsFallback(boolean createDOMAsFallback) {
        this.createDOMAsFallback = createDOMAsFallback;
    }

    public URI getBaseURI() {
        return reader.getBaseURI();
    }

    public Properties getProperties() {
        if (properties == null)
            properties = new Properties();

        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = new Properties(properties);
    }

    @Override
    public void close() throws XMLReadException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        } finally {
            parent.clear();
            builderCache.clear();
        }
    }

    public int getDepth() {
        return reader.getDepth();
    }

    public boolean hasNext() throws XMLReadException {
        try {
            return reader.hasNext();
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public EventType nextTag() throws XMLReadException {
        try {
            do {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        return EventType.START_ELEMENT;
                    case XMLStreamConstants.END_ELEMENT:
                        return EventType.END_ELEMENT;
                }
            } while (reader.hasNext());

            return EventType.END_DOCUMENT;
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public QName getName() throws XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT && reader.getEventType() != XMLStreamConstants.END_ELEMENT)
            throw new XMLReadException("Illegal to call getName when event is neither START_ELEMENT nor END_ELEMENT.");

        return reader.getName();
    }

    public String getPrefix() throws XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT && reader.getEventType() != XMLStreamConstants.END_ELEMENT)
            throw new XMLReadException("Illegal to call getPrefix when event is neither START_ELEMENT nor END_ELEMENT.");

        return reader.getPrefix();
    }

    public <T> T getObject(Class<T> type) throws ObjectBuildException, XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call getObject when event is not START_ELEMENT.");

        QName name = reader.getName();
        ObjectBuilder<T> builder = xmlObjects.getBuilder(name, type);
        if (builder != null) {
            T object = builder.createObject(name, parent.get());
            if (object == null)
                throw new ObjectBuildException("The builder " + builder.getClass().getName() + " created a null value.");

            return processObject(object, name, builder);
        } else
            return null;
    }

    public <T> T getObjectUsingBuilder(Class<? extends ObjectBuilder<T>> type) throws ObjectBuildException, XMLReadException {
        return getObjectUsingBuilder(getOrCreateBuilder(type));
    }

    public <T> T getObjectUsingBuilder(ObjectBuilder<T> builder) throws ObjectBuildException, XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call getObjectUsingBuilder when event is not START_ELEMENT.");

        QName name = reader.getName();
        T object = builder.createObject(name, parent.get());
        if (object == null)
            throw new ObjectBuildException("The builder " + builder.getClass().getName() + " created a null value.");

        return processObject(object, name, builder);
    }

    @SuppressWarnings("unchecked")
    public <T> T fillObject(T object) throws ObjectBuildException, XMLReadException {
        if (object == null)
            throw new ObjectBuildException("Illegal to call fillObject with a null object.");

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call fillObject when event is not START_ELEMENT.");

        QName name = reader.getName();
        ObjectBuilder<T> builder = xmlObjects.getBuilder(name, (Class<T>) object.getClass());
        return builder != null ? processObject(object, name, builder) : null;
    }

    public <T> T fillObjectUsingBuilder(T object, Class<? extends ObjectBuilder<T>> type) throws ObjectBuildException, XMLReadException {
        return fillObjectUsingBuilder(object, getOrCreateBuilder(type));
    }

    public <T> T fillObjectUsingBuilder(T object, ObjectBuilder<T> builder) throws ObjectBuildException, XMLReadException {
        if (object == null)
            throw new ObjectBuildException("Illegal to call fillObjectUsingBuilder with a null object.");

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call fillObjectUsingBuilder when event is not START_ELEMENT.");

        return processObject(object, reader.getName(), builder);
    }

    private <T> T processObject(T object, QName name, ObjectBuilder<T> builder) throws ObjectBuildException, XMLReadException {
        WeakReference<?> previous = parent;
        try {
            parent = new WeakReference<>(object);
            int stopAt = reader.getDepth() - 1;
            int childLevel = reader.getDepth() + 1;

            // initialize object
            builder.initializeObject(object, name, getAttributes(), this);

            while (true) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && reader.getDepth() == childLevel) {
                    // build child object
                    int state = reader.getState();
                    builder.buildChildObject(object, reader.getName(), getAttributes(), this);

                    // continue if the reader is at the next start element
                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && state != reader.getState())
                        continue;
                }

                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getDepth() == stopAt)
                        return object;
                    else if (reader.getDepth() < stopAt) {
                        throw new XMLReadException("Reader is in illegal state (depth = " + stopAt +
                                " but expected depth = " + reader.getDepth() + ").");
                    }
                }

                if (reader.hasNext())
                    reader.next();
                else
                    return null;
            }
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        } finally {
            parent = previous;
        }
    }

    public Element getDOMElement() throws XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call getDOMElement when event is not START_ELEMENT.");

        try {
            if (transformer == null)
                transformer = TransformerFactory.newInstance().newTransformer();

            DOMResult result = new DOMResult();
            transformer.transform(new StAXSource(reader), result);
            Node node = result.getNode();

            if (node.hasChildNodes()) {
                Node child = node.getFirstChild();
                if (child.getNodeType() == Node.ELEMENT_NODE)
                    return (Element) child;
            }

            return null;
        } catch (TransformerConfigurationException e) {
            throw new XMLReadException("Failed to initialize DOM transformer.", e);
        } catch (TransformerException e) {
            throw new XMLReadException("Failed to read XML content as DOM element.", e);
        } finally {
            if (transformer != null)
                transformer.reset();
        }
    }

    public <T> BuildResult<T> getObjectOrDOMElement(Class<T> type) throws ObjectBuildException, XMLReadException {
        T object = getObject(type);
        if (object != null)
            return BuildResult.of(object);
        else if (createDOMAsFallback) {
            Element element = getDOMElement();
            if (element != null)
                return BuildResult.of(element);
        }

        return BuildResult.empty();
    }

    public Attributes getAttributes() throws XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call getAttributes when event is not START_ELEMENT.");

        Attributes attributes = new Attributes();
        for (int i = 0; i < reader.getAttributeCount(); i++)
            attributes.add(reader.getAttributeName(i), reader.getAttributeValue(i));

        return attributes;
    }

    public TextContent getTextContent() throws XMLReadException {
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

            return TextContent.of(result.toString());
        } catch (XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public String getMixedContent() throws XMLReadException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
            throw new XMLReadException("Illegal to call getMixedContent when event is not START_ELEMENT.");

        try (StringWriter writer = new StringWriter()) {
            try (SAXWriter saxWriter = new SAXWriter(writer).writeXMLDeclaration(false)) {
                int stopAt = reader.getDepth() - 1;
                StAXStream2SAX mapper = new StAXStream2SAX(saxWriter);

                // map content of start element to a string representation
                while (reader.next() != XMLStreamConstants.END_ELEMENT || reader.getDepth() > stopAt)
                    mapper.bridgeEvent(reader);
            }

            return writer.toString();
        } catch (IOException | SAXException | XMLStreamException e) {
            throw new XMLReadException("Caused by:", e);
        }
    }

    public <T> ObjectBuilder<T> getOrCreateBuilder(Class<? extends ObjectBuilder<T>> type) throws ObjectBuildException {
        ObjectBuilder<?> cachedBuilder = builderCache.get(type);
        if (cachedBuilder != null && type.isAssignableFrom(cachedBuilder.getClass())) {
            return type.cast(cachedBuilder);
        } else {
            try {
                ObjectBuilder<T> builder = type.getDeclaredConstructor().newInstance();
                builderCache.put(type, builder);
                return builder;
            } catch (Exception e) {
                throw new ObjectBuildException("The builder " + type.getName() + " lacks a default constructor.");
            }
        }
    }
}
