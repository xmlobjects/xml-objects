package org.xmlobjects;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.xmlobjects.annotation.XMLElement;
import org.xmlobjects.annotation.XMLElements;
import org.xmlobjects.builder.ObjectBuilder;
import org.xmlobjects.serializer.ObjectSerializer;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMLObjectContext {
    public static final DatatypeFactory XML_TYPE_FACTORY;

    private final ConcurrentHashMap<String, Map<String, ObjectBuilder<?>>> builders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, ObjectSerializer<?>>> serializers = new ConcurrentHashMap<>();

    static {
        try {
            XML_TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to initialize datatype factory.", e);
        }
    }

    private XMLObjectContext() {
        // just to thwart instantiation
    }

    public static XMLObjectContext newInstance() throws XMLObjectException {
        return newInstance(Thread.currentThread().getContextClassLoader());
    }

    public static XMLObjectContext newInstance(ClassLoader classLoader) throws XMLObjectException {
        XMLObjectContext context = new XMLObjectContext();
        context.loadBuilders(classLoader);
        context.loadObjectSerializers(classLoader);

        return context;
    }

    public XMLObjectContext registerBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName) throws XMLObjectException {
        registerBuilder(builder,namespaceURI,localName, false);
        return this;
    }

    public ObjectBuilder<?> getBuilder(String namespaceURI, String localName) {
        return builders.getOrDefault(namespaceURI, Collections.emptyMap()).get(localName);
    }

    public ObjectBuilder<?> getBuilder(String localName) {
        return getBuilder(XMLConstants.NULL_NS_URI, localName);
    }

    public ObjectBuilder<?> getBuilder(QName name) {
        return getBuilder(name.getNamespaceURI(), name.getLocalPart());
    }

    public XMLObjectContext registerSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI) throws XMLObjectException {
        registerSerializer(serializer,objectType,namespaceURI,false);
        return this;
    }

    public ObjectSerializer<?> getSerializer(Class<?> objectType, String namespaceURI) {
        return serializers.getOrDefault(objectType.getName(), Collections.emptyMap()).get(namespaceURI);
    }

    public ObjectSerializer<?> getSerializer(Class<?> objectType) {
        return getSerializer(objectType, XMLConstants.NULL_NS_URI);
    }

    private void loadBuilders(ClassLoader classLoader) throws XMLObjectException {
        for (Class<? extends ObjectBuilder> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectBuilder.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectException("The builder " + type.getName() + " uses both @XMLElement and @XMLElements.");

            ObjectBuilder<?> builder;
            try {
                builder = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectException("The builder " + type.getName() + " lacks a default constructor.", e);
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

    private void loadObjectSerializers(ClassLoader classLoader) throws XMLObjectException {
        for (Class<? extends ObjectSerializer> type : ClassFilter.only()
                .withoutModifiers(Modifier.ABSTRACT)
                .satisfying(c -> c.isAnnotationPresent(XMLElement.class) || c.isAnnotationPresent(XMLElements.class))
                .from(ClassIndex.getSubclasses(ObjectSerializer.class, classLoader))) {

            boolean isSetElement = type.isAnnotationPresent(XMLElement.class);
            boolean isSetElements = type.isAnnotationPresent(XMLElements.class);

            if (isSetElement && isSetElements)
                throw new XMLObjectException("The serializer " + type.getName() + " uses both @XMLElement and @XMLElements.");

            ObjectSerializer<?> serializer;
            try {
                serializer = type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new XMLObjectException("The serializer " + type.getName() + " lacks a default constructor.", e);
            }

            Class<?> objectType = getObjectType(serializer);
            if (objectType == null)
                throw new XMLObjectException("Failed to retrieve object type of serializer " + type.getName() + ".");

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

    private void registerBuilder(ObjectBuilder<?> builder, String namespaceURI, String localName, boolean failOnDuplicates) throws XMLObjectException {
        ObjectBuilder<?> current = builders.computeIfAbsent(namespaceURI, v -> new HashMap<>()).put(localName, builder);
        if (current != null && failOnDuplicates)
            throw new XMLObjectException("Two builders are registered for the same XML element '" +
                    new QName(namespaceURI, localName) + "': " +
                    builder.getClass().getName() + " and " + current.getClass().getName() + ".");
    }

    private void registerSerializer(ObjectSerializer<?> serializer, Class<?> objectType, String namespaceURI, boolean failOnDuplicates) throws XMLObjectException {
        ObjectSerializer<?> current = serializers.computeIfAbsent(objectType.getName(), v -> new HashMap<>()).put(namespaceURI, serializer);
        if (current != null && failOnDuplicates)
            throw new XMLObjectException("Two serializers are registered for the same object type '" +
                    objectType.getName() + "': " +
                    serializer.getClass().getName() + " and " + current.getClass().getName() + ".");
    }

    private static Class<?> getObjectType(ObjectSerializer<?> parent) throws XMLObjectException {
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
            throw new XMLObjectException("Failed to retrieve object type of serializer " + parent.getClass().getName() + ".", e);
        }
    }

}
