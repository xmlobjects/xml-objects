/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.annotation;

import javax.xml.XMLConstants;
import java.lang.annotation.*;

@Documented
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface XMLElement {
    String name();

    String namespaceURI() default XMLConstants.NULL_NS_URI;
}
