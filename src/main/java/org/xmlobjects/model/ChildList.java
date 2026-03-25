/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.model;

import org.xmlobjects.copy.CopyContext;
import org.xmlobjects.copy.CopyMode;
import org.xmlobjects.copy.Copyable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.UnaryOperator;

public class ChildList<T extends Child> extends ArrayList<T> implements Copyable<ChildList<T>> {
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

    @Override
    public T set(int index, T element) {
        applyParent(element);
        return super.set(index, element);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        super.replaceAll(operator);
        applyParent(this);
    }

    private void applyParent(T child) {
        if (child != null) {
            child.setParent(parent);
        }
    }

    private void applyParent(Collection<? extends T> c) {
        for (T child : c) {
            if (child != null) {
                child.setParent(parent);
            }
        }
    }

    @Override
    public ChildList<T> newInstance(CopyMode mode, CopyContext context) {
        return new ChildList<>(size(), switch (mode) {
            case SHALLOW -> getParent();
            case DEEP -> context.deepCopy(getParent());
        });
    }

    @Override
    public void shallowCopyTo(ChildList<T> dest, CopyContext context) {
        dest.addAll(this);
    }

    @Override
    public void deepCopyTo(ChildList<T> dest, CopyContext context) {
        for (T element : this) {
            dest.add(context.deepCopy(element));
        }
    }
}
