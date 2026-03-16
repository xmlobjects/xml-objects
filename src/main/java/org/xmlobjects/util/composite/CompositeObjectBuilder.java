/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.composite;

import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.stream.XMLReadException;
import org.xmlobjects.stream.XMLReader;
import org.xmlobjects.xml.Attributes;

import javax.xml.namespace.QName;
import java.util.Objects;

public abstract class CompositeObjectBuilder<T> implements ObjectBuilder<T> {
    private final Class<? extends ObjectBuilder<T>> builder;

    @SuppressWarnings("unchecked")
    public <S extends ObjectBuilder<? super T>> CompositeObjectBuilder(Class<S> adapter) {
        this.builder = Objects.requireNonNull((Class<? extends ObjectBuilder<T>>) adapter, "Object builder must not be null.");
    }

    @Override
    public void initializeObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException {
        reader.getOrCreateBuilder(builder).initializeObject(object, name, attributes, reader);
    }

    @Override
    public void buildChildObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException {
        reader.getOrCreateBuilder(builder).buildChildObject(object, name, attributes, reader);
    }
}
