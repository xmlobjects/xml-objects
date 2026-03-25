# xml-objects – Framework Guide

A deep-dive into the design philosophy, core concepts, and key advantages of the **xml-objects** framework.

---

## Table of Contents

1. [What Problem Does xml-objects Solve?](#1-what-problem-does-xml-objects-solve)
2. [Core Architecture](#2-core-architecture)
3. [A First Example](#3-a-first-example)
4. [Key Advantage 1 — One Builder/Serializer for Multiple Namespaces](#4-key-advantage-1--one-builderserializer-for-multiple-namespaces)
5. [Key Advantage 2 — Apply to Existing Domain Models](#5-key-advantage-2--apply-to-existing-domain-models)
6. [The XMLObjects Registry](#6-the-xmlobjects-registry)
7. [ObjectBuilder — Reading XML](#7-objectbuilder--reading-xml)
8. [ObjectSerializer — Writing XML](#8-objectserializer--writing-xml)
9. [Auto-Registration via Annotations](#9-auto-registration-via-annotations)
10. [The Namespaces Concept](#10-the-namespaces-concept)
11. [Value Model — Element, Attributes, TextContent](#11-value-model--element-attributes-textcontent)
12. [XMLReader and XMLWriter](#12-xmlreader-and-xmlwriter)
13. [Child and ChildList — Parent-Aware Domain Models](#13-child-and-childlist--parent-aware-domain-models)
14. [BuildResult — Handling Unknown Elements](#14-buildresult--handling-unknown-elements)
15. [Properties — Passing Context Through the Object Hierarchy](#15-properties--passing-context-through-the-object-hierarchy)
16. [Thread Safety](#16-thread-safety)
17. [Comparison with JAXB](#17-comparison-with-jaxb)

---

## 1. What Problem Does xml-objects Solve?

Most XML mapping frameworks follow the same paradigm: **annotate your Java classes**, and the framework generates or interprets the XML mapping from those annotations. This couples your domain model tightly to the XML representation.

xml-objects takes the opposite approach. It is a **mapping framework**, not a binding framework. The mapping logic lives entirely outside the domain model in dedicated `ObjectBuilder` and `ObjectSerializer` classes. The domain model classes themselves are not touched, not annotated, and not generated.

This separation enables two capabilities that are unique among Java XML libraries:

- **One builder/serializer can handle multiple XML namespaces** (e.g. different schema versions), without duplicating domain classes.
- **Any existing domain model can be mapped to XML**, even if you have no access to its source code.

---

## 2. Core Architecture

```
┌─────────────────────────────────────────────────────┐
│                   XMLObjects                        │
│  (registry: namespace+localName → builder/serializer│
│             objectType+namespace → serializer)      │
└──────────┬──────────────────────────┬───────────────┘
           │                          │
    ┌──────▼───────┐           ┌──────▼─────────┐
    │ ObjectBuilder│           │ObjectSerializer│
    │  (reads XML) │           │  (writes XML)  │
    └──────┬───────┘           └──────┬─────────┘
           │                          │
    ┌──────▼──────┐            ┌──────▼───────┐
    │  XMLReader  │            │  XMLWriter   │
    │ (StAX-based)│            │ (SAX-based)  │
    └─────────────┘            └──────────────┘
           │                           │
    ┌──────▼───────────────────────────▼──────┐
    │              Domain Model               │
    │   (plain Java objects — no annotations) │
    └─────────────────────────────────────────┘
```

The `XMLObjects` instance is the central registry. It maps:
- `(namespaceURI, localName)` → `ObjectBuilder` (for reading)
- `(objectType, namespaceURI)` → `ObjectSerializer` (for writing)

Builders and serializers are stateless, reusable, and registered once.

---

## 3. A First Example

Consider the following XML document describing a building with a nested address and a text-content description element:

```xml
<Building xmlns="https://example.org/building/1.0"
          name="City Hall" height="45.5" floors="12">
    <description>A historic city hall built in 1901.</description>
    <Address xmlns="https://example.org/address/1.0"
             street="Main Street 1"
             city="Berlin"
             postalCode="10115"/>
</Building>
```

The corresponding domain model is two plain Java classes — no annotations, no base classes, no generated code:

```java
public class Building {
    private String name;
    private double height;
    private int floors;
    private String description;
    private Address address;
    // getters and setters ...
}

public class Address {
    private String street;
    private String city;
    private String postalCode;
    // getters and setters ...
}
```

### Builders (reading XML)

An `ObjectBuilder` is responsible for creating and populating a domain object from XML. First, the simpler `AddressBuilder`:

```java
@XMLElement(name = "Address", namespaceURI = "https://example.org/address/1.0")
public class AddressBuilder implements ObjectBuilder<Address> {

    @Override
    public Address createObject(QName name, Object parent) {
        return new Address();
    }

    @Override
    public void initializeObject(Address object, QName name, Attributes attributes, XMLReader reader) {
        attributes.getValue("street").ifPresent(object::setStreet);
        attributes.getValue("city").ifPresent(object::setCity);
        attributes.getValue("postalCode").ifPresent(object::setPostalCode);
    }
}
```

The `BuildingBuilder` reads its own attributes in `initializeObject`, then delegates each child element to the registry via `reader.getObject()` in `buildChildObject`:

```java
@XMLElement(name = "Building", namespaceURI = "https://example.org/building/1.0")
public class BuildingBuilder implements ObjectBuilder<Building> {

    @Override
    public Building createObject(QName name, Object parent) {
        return new Building();
    }

    @Override
    public void initializeObject(Building object, QName name, Attributes attributes, XMLReader reader) {
        attributes.getValue("name").ifPresent(object::setName);
        attributes.getValue("height").ifDouble(object::setHeight);
        attributes.getValue("floors").ifInteger(object::setFloors);
    }

    @Override
    public void buildChildObject(Building object, QName name, Attributes attributes, XMLReader reader)
            throws ObjectBuildException, XMLReadException {
        if ("https://example.org/building/1.0".equals(name.getNamespaceURI())
                && "description".equals(name.getLocalPart())) {
            reader.getTextContent().ifPresent(object::setDescription);
        } else if ("https://example.org/address/1.0".equals(name.getNamespaceURI())
                && "Address".equals(name.getLocalPart())) {
            // QName already verified above — use getObjectUsingBuilder to skip the registry lookup
            object.setAddress(reader.getObjectUsingBuilder(AddressBuilder.class));
        }
    }
}
```

### Serializers (writing XML)

`ObjectSerializer` mirrors the builder. `AddressSerializer` creates the element and populates its attributes:

```java
@XMLElement(name = "Address", namespaceURI = "https://example.org/address/1.0")
public class AddressSerializer implements ObjectSerializer<Address> {

    @Override
    public Element createElement(Address object, Namespaces namespaces) {
        return Element.of("https://example.org/address/1.0", "Address");
    }

    @Override
    public void initializeElement(Element element, Address object, Namespaces namespaces, XMLWriter writer) {
        element.addAttribute("street",     object.getStreet());
        element.addAttribute("city",       object.getCity());
        element.addAttribute("postalCode", object.getPostalCode());
    }
}
```

`BuildingSerializer` writes its own attributes and then delegates the child `Address` to the writer:

```java
@XMLElement(name = "Building", namespaceURI = "https://example.org/building/1.0")
public class BuildingSerializer implements ObjectSerializer<Building> {

    @Override
    public Element createElement(Building object, Namespaces namespaces) {
        return Element.of("https://example.org/building/1.0", "Building");
    }

    @Override
    public void initializeElement(Element element, Building object, Namespaces namespaces, XMLWriter writer) {
        element.addAttribute("name",   object.getName());
        element.addAttribute("height", TextContent.ofDouble(object.getHeight()));
        element.addAttribute("floors", TextContent.ofInteger(object.getFloors()));
    }

    @Override
    public void writeChildElements(Building object, Namespaces namespaces, XMLWriter writer)
            throws ObjectSerializeException, XMLWriteException {
        if (object.getDescription() != null) {
            writer.writeElement(Element.of("https://example.org/building/1.0", "description")
                    .addTextContent(object.getDescription()));
        }
        if (object.getAddress() != null) {
            // Serializer type is known — skip the registry lookup
            writer.writeObjectUsingSerializer(object.getAddress(), AddressSerializer.class, namespaces);
        }
    }
}
```

### Putting it together

```java
XMLObjects xmlObjects = XMLObjects.newInstance();  // auto-registers all annotated builders/serializers

// Reading
try (XMLReader reader = XMLReaderFactory.newInstance().createReader(xmlObjects, inputStream)) {
    Building building = xmlObjects.fromXML(reader, Building.class);
}

// Writing
try (XMLWriter writer = XMLWriterFactory.newInstance().createWriter(xmlObjects, outputStream)) {
    xmlObjects.toXML(writer, building,
        "https://example.org/building/1.0",
        "https://example.org/address/1.0");
}
```

---

## 4. Key Advantage 1 — One Builder/Serializer for Multiple Namespaces

### The challenge with annotation-based frameworks

In annotation-based frameworks like JAXB, namespace information is baked directly into `@XmlRootElement`, `@XmlElement`, and `@XmlType` on your domain classes. If an XML application evolves and introduces a new schema version with a different namespace URI, your options are typically:

- A separate annotated class per version (e.g. `BuildingV1`, `BuildingV2`), or
- An unmarshal adapter that dispatches manually.

This can become burdensome when you have many element types across several schema versions.

### The xml-objects solution

Suppose the building schema now exists in three versions:

```xml
<!-- Version 1.0 -->
<Building xmlns="https://example.org/building/1.0" name="City Hall" height="45.5" floors="12">
    <Address xmlns="https://example.org/address/1.0" street="Main Street 1" city="Berlin" postalCode="10115"/>
</Building>

<!-- Version 2.0 — same structure, new namespace -->
<Building xmlns="https://example.org/building/2.0" name="City Hall" height="45.5" floors="12">
    <Address xmlns="https://example.org/address/1.0" street="Main Street 1" city="Berlin" postalCode="10115"/>
</Building>

<!-- Version 3.0 -->
<Building xmlns="https://example.org/building/3.0" name="City Hall" height="45.5" floors="12">
    <Address xmlns="https://example.org/address/1.0" street="Main Street 1" city="Berlin" postalCode="10115"/>
</Building>
```

The `Building` domain class does not change. Both the builder and the serializer simply swap `@XMLElement` for `@XMLElements` — all other code stays identical:

```java
@XMLElements({
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/1.0"),
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/2.0"),
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/3.0")
})
public class BuildingBuilder implements ObjectBuilder<Building> {
    // identical to the single-namespace version above
    // The QName parameter in createObject/initializeObject carries the actual
    // namespace URI if version-specific branching is needed:
    //   String version = name.getNamespaceURI();
}

@XMLElements({
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/1.0"),
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/2.0"),
    @XMLElement(name = "Building", namespaceURI = "https://example.org/building/3.0")
})
public class BuildingSerializer implements ObjectSerializer<Building> {
    // identical to the single-namespace version above
}
```

`XMLObjects.newInstance()` picks up both classes automatically via the annotation index — no manual registration needed. The `Namespaces` object passed to `toXML()` at the call site then determines which version is written:

```java
XMLObjects xmlObjects = XMLObjects.newInstance();  // auto-registers all three versions

// Write version 1.0
xmlObjects.toXML(writer, building, "https://example.org/building/1.0", "https://example.org/address/1.0");

// Write version 3.0 — same domain object, same serializer, different namespace at call site
xmlObjects.toXML(writer, building, "https://example.org/building/3.0", "https://example.org/address/1.0");
```

> Manual registration via `xmlObjects.registerSerializer(...)` is also available for dynamic or programmatic setups where annotations are not practical — see [Section 6](#6-the-xmlobjects-registry).

---

## 5. Key Advantage 2 — Apply to Existing Domain Models

### The challenge with annotation-based frameworks

In annotation-based frameworks like JAXB, the domain classes typically need to carry XML mapping annotations (`@XmlRootElement`, `@XmlElement`, `@XmlAttribute`, etc.). If you generate classes from an XSD schema you lose control over your model; if you retrofit your own classes with annotations, the mapping concern moves into the domain layer. Workarounds such as `@XmlJavaTypeAdapter` exist but add indirection and boilerplate.

The hardest case is a domain model from a third-party library whose source code you cannot modify at all.

### The xml-objects solution

Notice that neither `Building` nor `Address` in the example above carry any xml-objects annotations. The mapping logic lives entirely in the builder and serializer classes. This means you can apply the exact same pattern to classes you do not own:

```java
// From an external dependency — source cannot be modified
public final class Address {
    private String street;
    private String city;
    private String postalCode;
    // getters and setters ...
}
```

The `AddressBuilder` and `AddressSerializer` shown in [Section 3](#3-a-first-example) work without any changes — they live in your own codebase and the framework discovers them automatically via the annotation index. The external `Address` class never needs to know that xml-objects exists.

---

## 6. The XMLObjects Registry

`XMLObjects` is the central registry. It is created once and shared across the application.

```java
XMLObjects xmlObjects = XMLObjects.newInstance();
```

This triggers auto-registration of all `ObjectBuilder` and `ObjectSerializer` implementations found on the classpath that carry `@XMLElement` or `@XMLElements` annotations.

You can also register manually for dynamic or programmatic setups:

```java
xmlObjects.registerBuilder(new AddressBuilder(), "https://example.org/address/1.0", "Address");
xmlObjects.registerSerializer(new AddressSerializer(), Address.class, "https://example.org/address/1.0");
```

### Registry structure

| Direction | Key | Value |
|-----------|-----|-------|
| Read | `(namespaceURI, localName)` | `ObjectBuilder<T>` |
| Write | `(objectType.getName(), namespaceURI)` | `ObjectSerializer<T>` |

The builder registry stores one builder per `(namespace, localName)` pair — last write wins. The serializer registry stores one serializer per `(objectType, namespace)` pair — same rule. This is intentional and deterministic: whichever serializer is registered last for a given combination is the one that is used.

---

## 7. ObjectBuilder — Reading XML

`ObjectBuilder<T>` is an interface with three lifecycle methods:

```java
public interface ObjectBuilder<T> {

    // 1. Called first — create the domain object (parent context is available)
    T createObject(QName name, Object parent) throws ObjectBuildException;

    // 2. Called once — read attributes of the current element into the object
    default void initializeObject(T object, QName name, Attributes attributes, XMLReader reader)
            throws ObjectBuildException, XMLReadException { }

    // 3. Called for each child START_ELEMENT — read child elements
    default void buildChildObject(T object, QName name, Attributes attributes, XMLReader reader)
            throws ObjectBuildException, XMLReadException { }
}
```

The `XMLReader` passed to `initializeObject` and `buildChildObject` is positioned at the current `START_ELEMENT`. From within `buildChildObject`, two methods dispatch to a child builder:

| Method | How it finds the builder | When to use |
|--------|--------------------------|-------------|
| `reader.getObject(ChildType.class)` | Registry lookup: resolves `(namespaceURI, localName)` → builder via the global `XMLObjects` map | When you have not yet checked the element name — e.g. a catch-all branch |
| `reader.getObjectUsingBuilder(ChildBuilder.class)` | Session cache lookup: resolves the builder class directly via a session-local `IdentityHashMap` | When the `if`-condition already verified the element name and namespace |

In practice, `buildChildObject` always checks the QName before acting, so `getObjectUsingBuilder` is the natural choice for every known element type — it skips the global registry lookup and the `isAssignableFrom` check:

```java
@Override
public void buildChildObject(Building object, QName name, Attributes attributes, XMLReader reader)
        throws ObjectBuildException, XMLReadException {
    if ("https://example.org/address/1.0".equals(name.getNamespaceURI())
            && "Address".equals(name.getLocalPart())) {
        // QName already confirmed — go directly to the builder
        object.setAddress(reader.getObjectUsingBuilder(AddressBuilder.class));
    } else {
        // Unknown element — let the registry decide, or ignore
        // reader.getObject(Object.class);
    }
}
```

Builder instances are cached in a session-local `IdentityHashMap` for the lifetime of the `XMLReader`, so `getObjectUsingBuilder` never allocates a new builder instance after the first call.

The `parent` parameter in `createObject` gives access to the object currently being built one level up, enabling parent-aware construction.

---

## 8. ObjectSerializer — Writing XML

`ObjectSerializer<T>` mirrors the builder with three lifecycle methods:

```java
public interface ObjectSerializer<T> {

    // 1. Create the XML element descriptor (name + namespace)
    default Element createElement(T object, Namespaces namespaces)
            throws ObjectSerializeException { return null; }

    // 2. Add attributes and text content to the element
    default void initializeElement(Element element, T object, Namespaces namespaces, XMLWriter writer)
            throws ObjectSerializeException, XMLWriteException { }

    // 3. Write nested child elements
    default void writeChildElements(T object, Namespaces namespaces, XMLWriter writer)
            throws ObjectSerializeException, XMLWriteException { }
}
```

The `Namespaces` parameter in all three methods tells the serializer which namespaces are active in the current write context. A serializer that supports multiple schema versions can use it to decide which element name and child structure to emit.

From within `writeChildElements`, two methods dispatch to a child serializer:

| Method | How it finds the serializer | When to use |
|--------|----------------------------|-------------|
| `writer.writeObject(child, namespaces)` | Registry lookup: resolves `(objectType, namespaces)` → serializer via the global `XMLObjects` map | When the serializer for a child is not known in advance — e.g. polymorphic content |
| `writer.writeObjectUsingSerializer(child, ChildSerializer.class, namespaces)` | Session cache lookup: resolves the serializer class directly via a session-local `IdentityHashMap` | When the child type is known — the common case in `writeChildElements` |

Serializer instances are cached in a session-local `IdentityHashMap` for the lifetime of the `XMLWriter`, so `writeObjectUsingSerializer` never allocates a new serializer instance after the first call.

---

## 9. Auto-Registration via Annotations

xml-objects uses [ClassIndex](https://github.com/atteo/classindex) to build a compile-time index of all `ObjectBuilder` and `ObjectSerializer` subclasses. At runtime, `XMLObjects.newInstance()` scans this index and auto-registers every concrete, annotated implementation — no XML configuration file, no Spring context, no classpath scan at startup.

```java
// Registers this builder for two namespace versions automatically
@XMLElements({
    @XMLElement(name = "Road", namespaceURI = "https://example.org/transport/1.0"),
    @XMLElement(name = "Road", namespaceURI = "https://example.org/transport/2.0")
})
public class RoadBuilder implements ObjectBuilder<Road> { ... }
```

Rules:
- Abstract classes are excluded from auto-registration.
- A class must have a **public no-argument constructor** — the framework instantiates it via reflection.
- If both `@XMLElement` and `@XMLElements` are present on the same class, an `XMLObjectsException` is thrown.
- If a duplicate `(namespace, localName)` registration is attempted and `failOnDuplicates` is `true` (the default for auto-registration), an `XMLObjectsException` is thrown.

---

## 10. The Namespaces Concept

`Namespaces` is a simple set of namespace URI strings that controls **which serializers are active during a write operation**.

```java
Namespaces namespaces = Namespaces.of(
    "https://example.org/building/2.0",
    "https://example.org/address/1.0"
);
xmlObjects.toXML(writer, building, namespaces);
```

When `XMLWriter.writeObject(object, namespaces)` is called:
1. The framework looks up `(object.getClass().getName(), ns)` for each `ns` in `namespaces`.
2. The first matching serializer wins.
3. The same `Namespaces` instance is passed down the entire object tree, so child serializers also respect the active namespace set.

This means you can control the output schema version at the call site, with zero changes to any serializer implementation:

```java
// Write in version 1.0
xmlObjects.toXML(writer, building, "https://example.org/building/1.0");

// Write in version 2.0 — same domain object, different serializer
xmlObjects.toXML(writer, building, "https://example.org/building/2.0");
```

---

## 11. Value Model — Element, Attributes, TextContent

### TextContent

`TextContent` is the central value type for XML text. It wraps a `String` and provides type-safe conversion to Java primitives, enums, dates, and more.

The idiomatic pattern is the **`ifX` family of methods**. Each one parses the content to the target type and, if successful, passes the result to the supplied `Consumer` — making method references the natural fit:

```java
// String value
attributes.getValue("name").ifPresent(object::setName);

// Numeric value
attributes.getValue("count").ifInteger(object::setCount);
attributes.getValue("ratio").ifDouble(object::setRatio);

// Boolean
attributes.getValue("visible").ifBoolean(object::setVisible);

// List values (whitespace-separated tokens in XML)
attributes.getValue("coords").ifDoubleList(object::setCoordinates);
```

If the attribute is absent or cannot be parsed to the requested type, the consumer is simply not called — no null check, no try/catch required.

When you do need the value directly (e.g. for conditional logic), the `getAsX()` methods return a boxed type or `null` if absent/unparseable, and `getAsXOrElse(default)` provides a fallback:

```java
int count = attributes.getValue("count").getAsIntegerOrElse(0);
```

`TextContent.absent()` is a sentinel for missing or null values — every `ifX` and `getAsX` method is safe to call on it without null checks.

### Attributes

`Attributes` is a two-level map: `namespaceURI → (localName → TextContent)`. It is created fresh for each `START_ELEMENT` and passed to the builder's lifecycle methods. Its mutable `add()` API is used by the writer side; the reader side only reads from it.

### Element

`Element` is the write-side counterpart: it carries a `QName`, an optional `Attributes`, and a list of child `ElementContent` items (either nested `Element`s or `TextContent` leaf values). It is constructed fluently by serializers:

```java
Element.of("https://example.org/building/1.0", "Building")
    .addAttribute("id",   object.getId())
    .addAttribute("name", object.getName())
    .addChildElement(addressElement);
```

---

## 12. XMLReader and XMLWriter

### XMLReader

`XMLReader` wraps a StAX `XMLStreamReader` and adds:
- Depth tracking via `DepthXMLStreamReader`
- Object dispatch via the `XMLObjects` registry
- Schema validation support via `SchemaHandler`
- DOM fallback for unknown elements via `getDOMElement()`
- Mixed-content reading via `getMixedContent()`
- Base URI tracking for relative URL resolution

Typical usage pattern:

```java
try (XMLReader reader = XMLReaderFactory.newInstance().createReader(xmlObjects, inputStream)) {
    MyObject result = xmlObjects.fromXML(reader, MyObject.class);
}
```

### XMLWriter

`XMLWriter` wraps a SAX `ContentHandler` and adds:
- Object dispatch via the `XMLObjects` registry
- Namespace prefix management
- Indentation and encoding control
- `writeObject()` for recursive serialization

Typical usage pattern:

```java
try (XMLWriter writer = XMLWriterFactory.newInstance().createWriter(xmlObjects, outputStream)) {
    xmlObjects.toXML(writer, myObject, "https://example.org/1.0");
}
```

Both `XMLReader` and `XMLWriter` implement `AutoCloseable` for safe use in try-with-resources.

---

## 13. Child and ChildList — Parent-Aware Domain Models

xml-objects provides optional base types in the `org.xmlobjects.model` package for domain models that need to navigate parent-child relationships at runtime.

### Child

`Child` is a simple interface that adds a bidirectional parent reference to any domain class:

```java
public class Building implements Child {
    private Child parent;
    private Address address;

    @Override public Child getParent() { return parent; }
    @Override public void setParent(Child parent) { this.parent = parent; }
}
```

Once a class implements `Child`, any ancestor can be located by type without storing explicit references:

```java
// Walk up the object tree to find the nearest enclosing Building
Building building = address.getParent(Building.class);
```

### ChildList

`ChildList<T extends Child>` is an `ArrayList` that automatically calls `setParent()` on every element added to it — so child objects always have their parent reference set correctly, even when items are added via `add()`, `addAll()`, or `set()`:

```java
public class Building implements Child {
    private final ChildList<Room> rooms = new ChildList<>(this);

    public ChildList<Room> getRooms() { return rooms; }
}
```

```java
// Parent is set automatically — no manual setParent() call needed
building.getRooms().add(new Room("Conference Room"));
```

Both `Child` and `ChildList` are completely optional. Builders and serializers work identically with plain POJOs. Use them when your domain model benefits from upward navigation.

---

## 14. BuildResult — Handling Unknown Elements

When a builder encounters a child element it does not recognise, the standard behaviour is to ignore it. In cases where unknown content must be preserved (e.g. for round-tripping or extensibility), `getObjectOrDOMElement()` provides a safe fallback that returns either a typed object or a DOM `Element`:

```java
@Override
public void buildChildObject(Building object, QName name, Attributes attributes, XMLReader reader)
        throws ObjectBuildException, XMLReadException {
    if ("https://example.org/address/1.0".equals(name.getNamespaceURI())
            && "Address".equals(name.getLocalPart())) {
        object.setAddress(reader.getObject(Address.class));
    } else {
        // Preserve unrecognised elements as DOM for later processing
        reader.getObjectOrDOMElement(Object.class)
              .ifDOMElement(object::addGenericElement);
    }
}
```

`BuildResult<T>` uses the same `ifX` idiom as `TextContent`:

```java
BuildResult<Address> result = reader.getObjectOrDOMElement(Address.class);
result.ifObject(object::setAddress);          // known element — typed object
result.ifDOMElement(object::addGenericElement); // unknown element — DOM node
```

DOM fallback must be enabled on the factory:

```java
XMLReaderFactory.newInstance(xmlObjects)
    .createDOMAsFallback(true)
    .createReader(inputStream);
```

---

## 15. Properties — Passing Context Through the Object Hierarchy

Both `XMLReader` and `XMLWriter` carry a `Properties` map — a typed key-value store — that is accessible from every builder and serializer during a read or write operation. It is the idiomatic way to pass application-specific context (configuration flags, caches, counters, parent state) through the entire object hierarchy without coupling builders to each other.

Properties are set on the factory and are available throughout the entire read/write session:

```java
XMLReaderFactory.newInstance(xmlObjects)
    .withProperty("app.strictMode", true)
    .withProperty("app.targetVersion", "2.0")
    .createReader(inputStream);
```

Any builder or serializer can read them via the `reader`/`writer` parameter:

```java
@Override
public void initializeObject(Building object, QName name, Attributes attributes, XMLReader reader) {
    boolean strict = reader.getProperties().get("app.strictMode", Boolean.class) == Boolean.TRUE;
    // ...
}
```

Properties can also be written during processing — for example a parent builder can place state that child builders pick up:

```java
// In BuildingBuilder.initializeObject:
reader.getProperties().set("currentBuilding", object);

// In a deeply nested child builder:
Building building = reader.getProperties().get("currentBuilding", Building.class);
```

---

## 16. Thread Safety

`XMLObjects` is **thread-safe for reads** after initial setup. The builder and serializer maps are `ConcurrentHashMap` instances, so concurrent `fromXML`/`toXML` calls on the same `XMLObjects` registry are safe.

`XMLReader` and `XMLWriter` instances are **not thread-safe** — each thread must use its own reader/writer. This is the expected usage pattern since they wrap a single underlying stream.

---

## 17. Comparison with JAXB

JAXB is a mature, well-supported standard with excellent tooling, broad IDE integration, and a large ecosystem. It is a solid choice when you control all domain classes, work with a single schema version, and want out-of-the-box XSD round-tripping.

xml-objects is designed for a different set of requirements: multi-namespace XML applications, existing or third-party domain models, and use cases where the mapping logic must be a separate, explicit artifact.

| Feature | xml-objects | JAXB |
|---------|-------------|------|
| Domain model modification required | **No** | Typically yes; adapters possible but add boilerplate |
| Works with unmodifiable third-party classes | **Directly** | Via `@XmlJavaTypeAdapter`, with extra indirection |
| One mapper for multiple schema versions / namespaces | **Yes** (`@XMLElements`) | Requires separate classes or manual adapters per namespace |
| Mapping logic separated from domain model | **Always** | Mixed into domain classes by default |
| Schema version controlled at call site | **Yes** (via `Namespaces`) | No |
| Streaming (StAX/SAX-based) | **Yes** | Partial |
| XSD-to-class generation / tooling | No | **Yes** |
| Standard (Jakarta EE) | No | **Yes** |
| Auto-registration without XML config | **Yes** (compile-time index) | No |
| Mixed-content support | **Yes** (`getMixedContent()`) | Limited |
| DOM fallback for unknown elements | **Yes** (`createDOMAsFallback`) | No |
| **Startup time** | **Fast** — annotation index built at compile time; no runtime scanning | Slow — `JAXBContext` scans classes and generates accessor bytecode at startup |
| **Runtime throughput** | **No reflection** — builders/serializers are plain classes; session-local cache avoids repeated registry lookups | No reflection after warm-up — modern implementations generate bytecode accessors; comparable once warmed |

### The fundamental difference

JAXB models the relationship as:

```
Java class  ←→  XML schema type
```

Every Java class corresponds to exactly one XML type in one namespace. Changing the namespace means changing the class.

xml-objects models the relationship as:

```
Java class  ←→  Builder/Serializer  ←→  XML schema type(s)
```

The builder/serializer layer decouples the two sides completely. One Java class can be mapped to many XML representations (across versions, profiles, or encodings), and one XML representation can be mapped to many Java classes depending on context. The mapping strategy is a first-class artifact of the application — not an implicit side effect of annotations.

