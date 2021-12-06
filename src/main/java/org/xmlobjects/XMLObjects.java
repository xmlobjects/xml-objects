/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2021 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.w3c.dom.Element;
import org.xmlobjects.annotation.XMLElement;
import org.xmlobjects.annotation.XMLElements;
import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.stream.*;
import org.xmlobjects.xml.Namespaces;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class XMLObjects {
    private final Map<String, Map<String, BuilderInfo>> builders = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ObjectSerializer<?>>> serializers = new ConcurrentHashMap<>();

    private XMLObjects() {
        // just to thwart instantiation
    }

    public static XMLObjects newInstance() throws XMLObjectsException {
        return newInstance(Thread.currentThread().getContextClassLoader());
    }

    public static XMLObjects newInstance(ClassLoader classLoader) throws XMLObjectsException {
        XMLObjects context = new XMLObjects();
        context.loadBuilders(classLoader, true);
        context.loadSerializers(classLoader, true);

        return context;
    }

    public XMLObjects registerBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName) throws XMLObjectsException {
        registerBuilder(builder, namespaceURI, localName, false);
        return this;
    }

    public ObjectBuilder<?> getBuilder(String namespaceURI, String localName) {
        BuilderInfo info = builders.getOrDefault(namespaceURI, Collections.emptyMap()).get(localName);
        return info != null ? info.builder : null;
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectBuilder<T> getBuilder(String namespaceURI, String localName, Class<T> objectType) {
        Objects.requireNonNull(objectType, "Object type must not be null.");
        BuilderInfo info = builders.getOrDefault(namespaceURI, Collections.emptyMap()).get(localName);
        return info != null && objectType.isAssignableFrom(info.objectType) ? (ObjectBuilder<T>) info.builder : null;
    }

    public ObjectBuilder<?> getBuilder(String localName) {
        return getBuilder(XMLConstants.NULL_NS_URI, localName);
    }

    public <T> ObjectBuilder<T> getBuilder(String localName, Class<T> objectType) {
        return getBuilder(XMLConstants.NULL_NS_URI, localName, objectType);
    }

    public ObjectBuilder<?> getBuilder(QName name) {
        return getBuilder(name.getNamespaceURI(), name.getLocalPart());
    }

    public <T> ObjectBuilder<T> getBuilder(QName name, Class<T> objectType) {
        return getBuilder(name.getNamespaceURI(), name.getLocalPart(), objectType);
    }

    public Class<?> getObjectType(ObjectBuilder<?> builder) {
        for (Map<String, BuilderInfo> infos : builders.values()) {
            for (BuilderInfo info : infos.values()) {
                if (info.builder == builder)
                    return info.objectType;
            }
        }

        return Object.class;
    }

    public Class<?> getObjectType(String namespaceURI, ObjectBuilder<?> builder) {
        for (BuilderInfo info : builders.getOrDefault(namespaceURI, Collections.emptyMap()).values()) {
            if (info.builder == builder)
                return info.objectType;
        }

        return Object.class;
    }

    public <T> XMLObjects registerSerializer(ObjectSerializer<T> serializer, Class<T> objectType, String namespaceURI) throws XMLObjectsException {
        registerSerializer(serializer, objectType, namespaceURI, false);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectSerializer<T> getSerializer(Class<T> objectType, String namespaceURI) {
        ObjectSerializer<?> serializer = serializers.getOrDefault(objectType.getName(), Collections.emptyMap()).get(namespaceURI);
        return serializer != null ? (ObjectSerializer<T>) serializer : null;
    }

    public <T> ObjectSerializer<T> getSerializer(Class<T> objectType) {
        return getSerializer(objectType, XMLConstants.NULL_NS_URI);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectSerializer<T> getSerializer(Class<T> objectType, Namespaces namespaces) {
        Map<String, ObjectSerializer<?>> map = serializers.getOrDefault(objectType.getName(), Collections.emptyMap());
        for (Map.Entry<String, ObjectSerializer<?>> entry : map.entrySet()) {
            if (namespaces.contains(entry.getKey())) {
                return (ObjectSerializer<T>) entry.getValue();
            }
        }

        return null;
    }

    public Set<String> getSerializableNamespaces() {
        return serializers.values().stream().flatMap(map -> map.keySet().stream()).collect(Collectors.toSet());
    }

    public <T> T fromXML(XMLReader reader, Class<T> objectType) throws ObjectBuildException, XMLReadException {
        T object = null;
        int stopAt = 0;

        while (reader.hasNext()) {
            EventType event = reader.nextTag();

            if (event == EventType.START_ELEMENT) {
                ObjectBuilder<T> builder = getBuilder(reader.getName(), objectType);
                if (builder != null) {
                    stopAt = reader.getDepth() - 2;
                    object = reader.getObjectUsingBuilder(builder);
                }
            }

            if (event == EventType.END_ELEMENT) {
                if (reader.getDepth() == stopAt)
                    return object;
                else if (reader.getDepth() < stopAt) {
                    throw new XMLReadException("XML reader is in an illegal state: depth = " + reader.getDepth() +
                            " but expected depth = " + stopAt + ".");
                }
            }
        }

        return object;
    }

    public void toXML(XMLWriter writer, Object object, Namespaces namespaces) throws ObjectSerializeException, XMLWriteException {
        writer.writeStartDocument();
        writer.writeObject(object, namespaces);
        writer.writeEndDocument();
    }

    public void toXML(XMLWriter writer, Object object, Collection<String> namespaceURIs) throws ObjectSerializeException, XMLWriteException {
        toXML(writer, object, Namespaces.of(namespaceURIs));
    }

    public void toXML(XMLWriter writer, Object object, String... namespaceURIs) throws ObjectSerializeException, XMLWriteException {
        toXML(writer, object, Namespaces.of(namespaceURIs));
    }

    public void toXML(XMLWriter writer, Object object) throws ObjectSerializeException, XMLWriteException {
        toXML(writer, object, Namespaces.of(getSerializableNamespaces()));
    }

    @SuppressWarnings("rawtypes")
    public void loadBuilders(ClassLoader classLoader, boolean failOnDuplicates) throws XMLObjectsException {
        for (Class<? extends ObjectBuilder> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectBuilder.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectsException("The builder " + type.getName() + " uses both @XMLElement and @XMLElements.");

            ObjectBuilder<?> builder;
            try {
                builder = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectsException("The builder " + type.getName() + " lacks a default constructor.", e);
            }

            if (isSetElement) {
                XMLElement element = type.getAnnotation(XMLElement.class);
                registerBuilder(builder, element.namespaceURI(), element.name(), failOnDuplicates);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerBuilder(builder, element.namespaceURI(), element.name(), failOnDuplicates);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void loadSerializers(ClassLoader classLoader, boolean failOnDuplicates) throws XMLObjectsException {
        for (Class<? extends ObjectSerializer> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectSerializer.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectsException("The serializer " + type.getName() + " uses both @XMLElement and @XMLElements.");

            ObjectSerializer<?> serializer;
            try {
                serializer = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectsException("The serializer " + type.getName() + " lacks a default constructor.", e);
            }

            Class<?> objectType = findObjectType(serializer);
            if (isSetElement) {
                XMLElement element = type.getAnnotation(XMLElement.class);
                registerSerializer(serializer, objectType, element.namespaceURI(), failOnDuplicates);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerSerializer(serializer, objectType, element.namespaceURI(), failOnDuplicates);
            }
        }
    }

    public void unloadBuilders(String namespaceURI) {
        if (namespaceURI != null)
            builders.remove(namespaceURI);
    }

    public void unloadSerializers(String namespaceURI) {
        if (namespaceURI != null)
            serializers.values().forEach(v -> v.remove(namespaceURI));
    }

    private void registerBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName, boolean failOnDuplicates) throws XMLObjectsException {
        BuilderInfo info = new BuilderInfo(builder, findObjectType(builder));
        BuilderInfo current = builders.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, info);
        if (current != null && current.builder != builder && failOnDuplicates)
            throw new XMLObjectsException("Two builders are registered for the XML element " +
                    new QName(namespaceURI, localName) + ": " +
                    builder.getClass().getName() + " and " + current.builder.getClass().getName() + ".");
    }

    private void registerSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI, boolean failOnDuplicates) throws XMLObjectsException {
        ObjectSerializer<?> current = serializers.computeIfAbsent(objectType.getName(), v -> new HashMap<>()).put(namespaceURI, serializer);
        if (current != null && current != serializer && failOnDuplicates)
            throw new XMLObjectsException("Two serializers are registered for the object type " +
                    objectType.getName() + ": " +
                    serializer.getClass().getName() + " and " + current.getClass().getName() + ".");
    }

    private Class<?> findObjectType(ObjectBuilder<?> builder) throws XMLObjectsException {
        try {
            return builder.getClass().getMethod("createObject", QName.class, Object.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new XMLObjectsException("The builder " + builder.getClass().getName() + " lacks the createObject method.", e);
        }
    }

    private Class<?> findObjectType(ObjectSerializer<?> serializer) throws XMLObjectsException {
        Class<?> clazz = serializer.getClass();
        Class<?> objectType = null;

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isSynthetic() && Modifier.isPublic(method.getModifiers())) {
                Class<?> candidateType = null;
                Type[] parameters;

                switch (method.getName()) {
                    case "createElement":
                        parameters = method.getGenericParameterTypes();
                        if (parameters.length == 2
                                && parameters[0] instanceof Class<?>
                                && parameters[1] == Namespaces.class) {
                            candidateType = (Class<?>) parameters[0];
                        }
                        break;
                    case "initializeElement":
                        parameters = method.getGenericParameterTypes();
                        if (parameters.length == 4
                                && parameters[0] == Element.class
                                && parameters[1] instanceof Class<?>
                                && parameters[2] == Namespaces.class
                                && parameters[3] == XMLWriter.class) {
                            candidateType = (Class<?>) parameters[1];
                        }
                        break;
                    case "writeChildElements":
                        parameters = method.getGenericParameterTypes();
                        if (parameters.length == 3
                                && parameters[0] instanceof Class<?>
                                && parameters[1] == Namespaces.class
                                && parameters[2] == XMLWriter.class) {
                            candidateType = (Class<?>) parameters[0];
                        }
                        break;
                }

                if (candidateType != null) {
                    if (objectType != null && candidateType != objectType)
                        throw new XMLObjectsException("The serializer " + serializer.getClass().getName() +
                                " uses different object types: " +
                                objectType.getName() + " and " + candidateType.getName() + ".");

                    objectType = candidateType;
                }
            }
        }

        if (objectType == null) {
            throw new XMLObjectsException("The serializer " + serializer.getClass().getName() + " must implement " +
                    "at least one of the methods createElement, initializeElement, and writeChildElements.");
        }

        return objectType;
    }

    private static class BuilderInfo {
        final ObjectBuilder<?> builder;
        final Class<?> objectType;

        BuilderInfo(ObjectBuilder<?> builder, Class<?> objectType) {
            this.builder = builder;
            this.objectType = objectType;
        }
    }
}
