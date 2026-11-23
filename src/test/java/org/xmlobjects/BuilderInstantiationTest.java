/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects;

import org.junit.jupiter.api.Test;
import org.xmlobjects.annotation.XMLElement;
import org.xmlobjects.builder.ObjectBuilder;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuilderInstantiationTest {

    @XMLElement(name = "NoDefault")
    public static class NoDefaultConstructor implements ObjectBuilder<Object> {
        public NoDefaultConstructor(String value) {
        }

        @Override
        public Object createObject(QName name, Object parent) {
            return null;
        }
    }

    @XMLElement(name = "Private")
    public static class PrivateConstructor implements ObjectBuilder<Object> {
        private PrivateConstructor() {
        }

        @Override
        public Object createObject(QName name, Object parent) {
            return null;
        }
    }

    @XMLElement(name = "Failing")
    public static class FailingConstructor implements ObjectBuilder<Object> {
        public FailingConstructor() {
            throw new IllegalStateException("failed");
        }

        @Override
        public Object createObject(QName name, Object parent) {
            return null;
        }
    }

    @Test
    void missingDefaultConstructor() throws Exception {
        XMLObjectsException e = load(NoDefaultConstructor.class);
        assertTrue(e.getMessage().contains("lacks a default constructor"), e.getMessage());
        assertInstanceOf(NoSuchMethodException.class, e.getCause());
    }

    @Test
    void inaccessibleConstructor() throws Exception {
        XMLObjectsException e = load(PrivateConstructor.class);
        assertTrue(e.getMessage().contains("export its package to org.xmlobjects"), e.getMessage());
        assertInstanceOf(IllegalAccessException.class, e.getCause());
    }

    @Test
    void failingConstructor() throws Exception {
        XMLObjectsException e = load(FailingConstructor.class);
        assertTrue(e.getMessage().startsWith("Failed to create the builder"), e.getMessage());
        assertFalse(e.getMessage().contains("lacks a default constructor"), e.getMessage());
    }

    private XMLObjectsException load(Class<?> builder) throws IOException, XMLObjectsException {
        // serves a class index that only lists the given builder
        Path index = Files.createTempFile("index", ".txt");
        Files.writeString(index, builder.getName() + "\n");
        try {
            URL url = index.toUri().toURL();
            ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
                @Override
                public Enumeration<URL> getResources(String name) throws IOException {
                    return name.equals("META-INF/services/" + ObjectBuilder.class.getName()) ?
                            Collections.enumeration(List.of(url)) :
                            Collections.emptyEnumeration();
                }
            };

            XMLObjects xmlObjects = XMLObjects.newInstance(new ClassLoader(null) {
            });
            return assertThrows(XMLObjectsException.class, () -> xmlObjects.loadBuilders(loader, true));
        } finally {
            Files.delete(index);
        }
    }
}
