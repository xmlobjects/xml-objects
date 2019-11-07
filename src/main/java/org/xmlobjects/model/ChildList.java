package org.xmlobjects.model;

import java.util.ArrayList;
import java.util.Collection;

public class ChildList<T extends Child> extends ArrayList<T> {
    private Child parent;

    public ChildList(Child parent) {
        this.parent = parent;
    }

    public ChildList(Collection<? extends T> c, Child parent) {
        super(c);
        this.parent = parent;
        applyParent(c);
    }

    public ChildList(int initialCapacity, Child parent) {
        super(initialCapacity);
        this.parent = parent;
    }

    public Child getParent() {
        return parent;
    }

    public void setParent(Child parent) {
        this.parent = parent;
        applyParent(this);
    }

    @Override
    public void add(int index, T element) {
        applyParent(element);
        super.add(index, element);
    }

    @Override
    public boolean add(T o) {
        applyParent(o);
        return super.add(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        applyParent(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        applyParent(c);
        return super.addAll(index, c);
    }

    private void applyParent(T child) {
        if (child != null)
            child.setParent(parent);
    }

    private void applyParent(Collection<? extends T> c) {
        for (T child : c) {
            if (child != null)
                child.setParent(parent);
        }
    }
}
