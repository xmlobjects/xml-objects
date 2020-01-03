package org.xmlobjects.model;

public interface Child {
    Child getParent();
    void setParent(Child parent);

    default <T extends Child> T getParent(Class<T> type) {
        Child parent = this;
        while ((parent = parent.getParent()) != null) {
            if (type.isInstance(parent))
                return type.cast(parent);
        }

        return null;
    }
}
