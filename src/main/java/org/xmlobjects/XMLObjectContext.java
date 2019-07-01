package org.xmlobjects;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.xmlobjects.annotation.XMLElement;
import org.xmlobjects.annotation.XMLElements;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.serializer.ObjectSerializer;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMLObjectContext {
    private final ConcurrentHashMap<String, Map<String, ObjectBuilder<?>>> builders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, ObjectSerializer<?>>> serializers = new ConcurrentHashMap<>();

    private XMLObjectContext() {
        // just to thwart instantiation
    }

    public static XMLObjectContext newInstance() throws XMLObjectException {
        return newInstance(Thread.currentThread().getContextClassLoader());
    }

    public static XMLObjectContext newInstance(ClassLoader classLoader) throws XMLObjectException {
        XMLObjectContext context = new XMLObjectContext();
        context.loadObjectBuilders(classLoader);
        context.loadObjectSerializers(classLoader);

        return context;
    }

    private void loadObjectBuilders(ClassLoader classLoader) throws XMLObjectException {
        for (Class<? extends ObjectBuilder> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectBuilder.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectException("The builder " + type.getTypeName() + " uses both @XMLElement and @XMLElements");

            ObjectBuilder<?> builder;
            try {
                builder = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectException("The builder " + type.getTypeName() + " lacks a default constructor", e);
            }

            if (isSetElement) {
                XMLElement element = type.getAnnotation(XMLElement.class);
                registerObjectBuilder(builder, element.namespaceURI(), element.name(), true);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerObjectBuilder(builder, element.namespaceURI(), element.name(), true);
            }
        }
    }

    private void loadObjectSerializers(ClassLoader classLoader) throws XMLObjectException {
        for (Class<? extends ObjectSerializer> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectSerializer.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectException("The serializer " + type.getTypeName() + " uses both @XMLElement and @XMLElements");

            ObjectSerializer<?> serializer;
            try {
                serializer = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectException("The serializer " + type.getTypeName() + " lacks a default constructor", e);
            }

            Class<?> objectType = getSerializedObjectType(serializer);
            if (objectType == null)
                throw new XMLObjectException("Failed to retrieve object type of serializer " + type.getTypeName());

            if (isSetElement) {
                XMLElement element = type.getAnnotation(XMLElement.class);
                registerObjectSerializer(serializer, objectType, element.namespaceURI(), true);
            } else if (isSetElements) {
                XMLElements elements = type.getAnnotation(XMLElements.class);
                for (XMLElement element : elements.value())
                    registerObjectSerializer(serializer, objectType, element.namespaceURI(), true);
            }
        }
    }

    private void registerObjectBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName, boolean failOnDuplicates) throws XMLObjectException {
        ObjectBuilder<?> current = builders.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, builder);
        if (current != null && failOnDuplicates)
            throw new XMLObjectException("Two builders are registered for the same XML element '" +
                    new QName(namespaceURI, localName) + "': " +
                    builder.getClass().getTypeName() + " and " + current.getClass().getTypeName());
    }

    private void registerObjectSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI, boolean failOnDuplicates) throws XMLObjectException {
        ObjectSerializer<?> current = serializers.computeIfAbsent(objectType.getName(), v -> new HashMap<>()).put(namespaceURI, serializer);
        if (current != null && failOnDuplicates)
            throw new XMLObjectException("Two serializers are registered for the same object type '" +
                    objectType.getTypeName() + "': " +
                    serializer.getClass().getTypeName() + " and " + current.getClass().getTypeName());
    }

    private static Class<?> getSerializedObjectType(ObjectSerializer<?> parent) throws XMLObjectException {
        try {
            Class<?> clazz = parent.getClass();
            Class<?>  objectType = null;

            do {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals("createElement") && !method.isSynthetic()) {
                        Type[] parameters = method.getGenericParameterTypes();
                        if (parameters.length > 0
                                && parameters[0] instanceof Class<?>
                                && !Modifier.isAbstract(((Class<?>) parameters[0]).getModifiers())) {
                            objectType = (Class<?>) parameters[0];
                            break;
                        }
                    }
                }
            } while (objectType == null && (clazz = clazz.getSuperclass()) != null);

            return objectType;
        } catch (Exception e) {
            throw new XMLObjectException("Failed to retrieve object type of serializer " + parent.getClass().getTypeName(), e);
        }
    }

}
