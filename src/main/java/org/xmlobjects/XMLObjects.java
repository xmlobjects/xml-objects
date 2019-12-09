package org.xmlobjects;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.xmlobjects.annotation.XMLElement;
import org.xmlobjects.annotation.XMLElements;
import org.xmlobjects.builder.ObjectBuildException;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.serializer.ObjectSerializeException;
import org.xmlobjects.serializer.ObjectSerializer;
import org.xmlobjects.stream.EventType;
import org.xmlobjects.stream.XMLReadException;
import org.xmlobjects.stream.XMLReader;
import org.xmlobjects.stream.XMLWriteException;
import org.xmlobjects.stream.XMLWriter;
import org.xmlobjects.xml.Namespaces;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class XMLObjects {
    private final ConcurrentHashMap<String, Map<String, BuilderInfo>> builders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, ObjectSerializer<?>>> serializers = new ConcurrentHashMap<>();

    private XMLObjects() {
        // just to thwart instantiation
    }

    public static XMLObjects newInstance() throws XMLObjectsException {
        return newInstance(Thread.currentThread().getContextClassLoader());
    }

    public static XMLObjects newInstance(ClassLoader classLoader) throws XMLObjectsException {
        XMLObjects context = new XMLObjects();
        context.loadBuilders(classLoader);
        context.loadSerializers(classLoader);

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

    public XMLObjects registerSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI) throws XMLObjectsException {
        registerSerializer(serializer, objectType, namespaceURI, false);
        return this;
    }

    public ObjectSerializer<?> getSerializer(Class<?> objectType, String namespaceURI) {
        return serializers.getOrDefault(objectType.getName(), Collections.emptyMap()).get(namespaceURI);
    }

    public ObjectSerializer<?> getSerializer(Class<?> objectType) {
        return getSerializer(objectType, XMLConstants.NULL_NS_URI);
    }

    public ObjectSerializer<?> getSerializer(Class<?> objectType, Namespaces namespaces) {
        Map<String, ObjectSerializer<?>> map = serializers.getOrDefault(objectType.getName(), Collections.emptyMap());
        for (Map.Entry<String, ObjectSerializer<?>> entry : map.entrySet()) {
            if (namespaces.contains(entry.getKey()))
                return entry.getValue();
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
    private void loadBuilders(ClassLoader classLoader) throws XMLObjectsException {
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
                registerBuilder(builder, element.namespaceURI(), element.name(), true);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerBuilder(builder, element.namespaceURI(), element.name(), true);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadSerializers(ClassLoader classLoader) throws XMLObjectsException {
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

            Class<?> objectType = getObjectType(serializer);
            if (objectType == null)
                throw new XMLObjectsException("Failed to retrieve object type of serializer " + type.getName() + ".");

            if (isSetElement) {
                XMLElement element = type.getAnnotation(XMLElement.class);
                registerSerializer(serializer, objectType, element.namespaceURI(), true);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerSerializer(serializer, objectType, element.namespaceURI(), true);
            }
        }
    }

    private void registerBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName, boolean failOnDuplicates) throws XMLObjectsException {
        BuilderInfo info = new BuilderInfo(builder, getObjectType(builder));
        BuilderInfo current = builders.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, info);
        if (current != null && current.builder != builder && failOnDuplicates)
            throw new XMLObjectsException("Two builders are registered for the XML element '" +
                    new QName(namespaceURI, localName) + "': " +
                    builder.getClass().getName() + " and " + current.builder.getClass().getName() + ".");
    }

    private void registerSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI, boolean failOnDuplicates) throws XMLObjectsException {
        ObjectSerializer<?> current = serializers.computeIfAbsent(objectType.getName(), v -> new HashMap<>()).put(namespaceURI, serializer);
        if (current != null && current != serializer && failOnDuplicates)
            throw new XMLObjectsException("Two serializers are registered for the object type '" +
                    objectType.getName() + "': " +
                    serializer.getClass().getName() + " and " + current.getClass().getName() + ".");
    }

    private Class<?> getObjectType(ObjectBuilder<?> builder) {
        try {
            return builder.getClass().getMethod("createObject", QName.class).getReturnType();
        } catch (NoSuchMethodException e) {
            return Object.class;
        }
    }

    private Class<?> getObjectType(ObjectSerializer<?> serializer) throws XMLObjectsException {
        Class<?> clazz = serializer.getClass();
        Class<?> objectType = null;
        boolean hasCreateElementMethod = false;

        try {
            do {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals("createElement") && !method.isSynthetic()) {
                        Type[] parameters = method.getGenericParameterTypes();
                        if (parameters.length == 2
                                && parameters[0] instanceof Class<?>
                                && parameters[1] == Namespaces.class
                                && !Modifier.isAbstract(((Class<?>) parameters[0]).getModifiers())) {
                            objectType = (Class<?>) parameters[0];
                            hasCreateElementMethod = true;
                            break;
                        }
                    }
                }
            } while (objectType == null && (clazz = clazz.getSuperclass()) != Object.class);
        } catch (Exception e) {
            throw new XMLObjectsException("Failed to retrieve object type of serializer " + serializer.getClass().getName() + ".", e);
        }

        if (!hasCreateElementMethod)
            throw new XMLObjectsException("The serializer " + serializer.getClass().getName() + " does not implement the createElement method.");

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
