/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2021 Claus Nagel <claus.nagel@gmail.com>
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
