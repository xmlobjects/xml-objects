package org.xmlobjects.model;

import org.xmlobjects.util.copy.Copyable;

public interface Child extends Copyable {
    Child getParent();
    void setParent(Child parent);
}
