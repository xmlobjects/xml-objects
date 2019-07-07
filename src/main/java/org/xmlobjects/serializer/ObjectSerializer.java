package org.xmlobjects.serializer;

import org.atteo.classindex.IndexSubclasses;
import org.xmlobjects.stream.XMLWriteException;
import org.xmlobjects.stream.XMLWriter;
import org.xmlobjects.xml.Element;
import org.xmlobjects.xml.Namespaces;

@IndexSubclasses
public interface ObjectSerializer<T> {
    Element createElement(T object, Namespaces namespaces) throws ObjectSerializeException;
    void serializeElement(Element element, T object, Namespaces namespaces, XMLWriter writer) throws ObjectSerializeException, XMLWriteException;
}
