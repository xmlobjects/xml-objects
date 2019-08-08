package org.xmlobjects.builder;

import org.atteo.classindex.IndexSubclasses;
import org.xmlobjects.stream.XMLReadException;
import org.xmlobjects.stream.XMLReader;
import org.xmlobjects.xml.Attributes;

import javax.xml.namespace.QName;

@IndexSubclasses
public interface ObjectBuilder<T> {
    T createObject(QName name) throws ObjectBuildException;
    default void initializeObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException { }
    default void buildChildObject(T object, QName name, Attributes attributes, XMLReader reader) throws ObjectBuildException, XMLReadException { }
}
