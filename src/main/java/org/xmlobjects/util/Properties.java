/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2025 Claus Nagel <claus.nagel@gmail.com>
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
