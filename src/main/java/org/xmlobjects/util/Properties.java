package org.xmlobjects.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Properties implements Serializable {
    private Map<String, Object> properties;

    public Object get(String name) {
        return properties != null ? properties.get(name) : null;
    }

    public <T> T get(String name, Class<T> typeOfT) {
        if (properties != null) {
            Object value = properties.get(name);
            return value != null && typeOfT.isAssignableFrom(value.getClass()) ? typeOfT.cast(value) : null;
        } else
            return null;
    }

    public boolean getAndCompare(String name, Object expectedValue) {
        return Objects.equals(get(name), expectedValue);
    }

    public void set(String name, Object value) {
        if (properties == null)
            properties = new HashMap<>();

        properties.put(name, value);
    }

    public void remove(String name) {
        if (properties != null)
            properties.remove(name);
    }

    public void clear() {
        if (properties != null)
            properties.clear();
    }
}
