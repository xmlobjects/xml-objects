module org.xmlobjects {
    requires transitive java.xml;
    requires transitive com.sun.xml.xsom;
    requires transitive org.xmlobjects.classindex;

    exports org.xmlobjects;
    exports org.xmlobjects.annotation;
    exports org.xmlobjects.builder;
    exports org.xmlobjects.model;
    exports org.xmlobjects.schema;
    exports org.xmlobjects.serializer;
    exports org.xmlobjects.stream;
    exports org.xmlobjects.util;
    exports org.xmlobjects.util.composite;
    exports org.xmlobjects.util.copy;
    exports org.xmlobjects.util.xml;
    exports org.xmlobjects.xml;
}