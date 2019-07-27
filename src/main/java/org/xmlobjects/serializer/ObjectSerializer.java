package org.xmlobjects.serializer;

import org.atteo.classindex.IndexSubclasses;
import org.xmlobjects.stream.XMLWriteException;
import org.xmlobjects.stream.XMLWriter;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.Namespaces;

@IndexSubclasses
public interface ObjectSerializer<T> {
    default Element createElement(T object, Namespaces namespaces) throws ObjectSerializeException { return null; }
    default void initializeElement(Element element, T object, Namespaces namespaces, XMLWriter writer) throws ObjectSerializeException, XMLWriteException { }
    default void writeChildElements(T object, Namespaces namespaces, XMLWriter writer) throws ObjectSerializeException, XMLWriteException { }
}
