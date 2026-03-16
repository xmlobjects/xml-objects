/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.lang.reflect.Constructor;

public abstract class AbstractCloner<T> {
    private CopyBuilder builder;

    public AbstractCloner() {
    }

    AbstractCloner(CopyBuilder builder) {
        this.builder = builder;
    }

    public abstract T copy(T src, T dest, boolean shallowCopy) throws Exception;

    final void setCopyBuilder(CopyBuilder builder) {
        this.builder = builder;
    }

    @SuppressWarnings("unchecked")
    public final <E> E deepCopy(E value) {
        return value instanceof Copyable copyable ?
                (E) copyable.deepCopy(builder, builder.getContext()) :
                builder.deepCopy(value);
    }

    @SuppressWarnings("unchecked")
    public T newInstance(T object, boolean shallowCopy) throws Exception {
        Constructor<?> constructor = object.getClass().getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }
}
