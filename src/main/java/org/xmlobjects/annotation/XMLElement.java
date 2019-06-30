package org.xmlobjects.annotation;

import javax.xml.XMLConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface XMLElement {
    String name();
    String namespaceURI() default XMLConstants.NULL_NS_URI;
}
