/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class Properties implements Serializable {
    private Map<String, Object> properties;

    public Properties() {
    }

    public Properties(Properties other) {
        if (other.properties != null) {
            properties = new HashMap<>();
            properties.putAll(other.properties);
        }
    }

    public Object get(String name) {
        return properties != null ? properties.get(name) : null;
    }

    public <T> T get(String name, Class<T> type) {
        if (properties != null) {
            Object value = properties.get(name);
            return type.isInstance(value) ? type.cast(value) : null;
        } else {
            return null;
        }
    }

    public boolean getAndCompare(String name, Object expectedValue) {
        return Objects.equals(get(name), expectedValue);
    }

    public <T> T getOrDefault(String name, Class<T> type, Supplier<T> supplier) {
        T value = get(name, type);
        return value != null ? value : supplier.get();
    }

    public <T> T getOrSet(String name, Class<T> type, Supplier<T> supplier) {
        T value = get(name, type);
        if (value == null) {
            value = supplier.get();
            set(name, value);
        }

        return value;
    }

    public void set(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }

        properties.put(name, value);
    }

    public void remove(String name) {
        if (properties != null) {
            properties.remove(name);
        }
    }

    public void clear() {
        if (properties != null) {
            properties.clear();
        }
    }
}
