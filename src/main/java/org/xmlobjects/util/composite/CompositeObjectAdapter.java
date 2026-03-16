/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.composite;

import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.stream.XMLReadException;
import org.xmlobjects.stream.XMLReader;
import org.xmlobjects.stream.XMLWriteException;
import org.xmlobjects.stream.XMLWriter;
import org.xmlobjects.xml.Attributes;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.Namespaces;

import javax.xml.namespace.QName;
import java.util.Objects;

public abstract class CompositeObjectAdapter<T> implements ObjectBuilder<T>, ObjectSerializer<T> {
    private final Class<? extends ObjectBuilder<T>> builder;
    private final Class<? extends ObjectSerializer<T>> serializer;

    @SuppressWarnings("unchecked")
    public <S extends ObjectBuilder<? super T> & ObjectSerializer<? super T>> CompositeObjectAdapter(Class<S> adapter) {
        this.builder = Objects.requireNonNull((Class<? extends ObjectBuilder<T>>) adapter, "Object adapter must not be null.");
        this.serializer = (Class<? extends ObjectSerializer<T>>) adapter;
    }

    @Override
    public void initializeObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException {
        reader.getOrCreateBuilder(builder).initializeObject(object, name, attributes, reader);
    }

    @Override
    public void buildChildObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException {
        reader.getOrCreateBuilder(builder).buildChildObject(object, name, attributes, reader);
    }

    @Override
    public void initializeElement(Element element, T object, Namespaces namespaces, XMLWriter writer) throws ObjectSerializeException, XMLWriteException {
        writer.getOrCreateSerializer(serializer).initializeElement(element, object, namespaces, writer);
    }

    @Override
    public void writeChildElements(T object, Namespaces namespaces, XMLWriter writer) throws ObjectSerializeException, XMLWriteException {
        writer.getOrCreateSerializer(serializer).writeChildElements(object, namespaces, writer);
    }
}
