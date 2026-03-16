/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.composite;

import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.stream.XMLWriteException;
import org.xmlobjects.stream.XMLWriter;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.Namespaces;

import java.util.Objects;

public abstract class CompositeObjectSerializer<T> implements ObjectSerializer<T> {
    private final Class<? extends ObjectSerializer<T>> serializer;

    @SuppressWarnings("unchecked")
    public <S extends ObjectSerializer<? super T>> CompositeObjectSerializer(Class<S> adapter) {
        this.serializer = Objects.requireNonNull((Class<? extends ObjectSerializer<T>>) adapter, "Object serializer must not be null.");
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
