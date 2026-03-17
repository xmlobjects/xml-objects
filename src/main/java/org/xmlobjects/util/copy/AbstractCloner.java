/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.lang.reflect.Constructor;

public abstract class AbstractCloner<T> {
    private CopyBuilder builder;

    protected AbstractCloner() {
    }

    AbstractCloner(CopyBuilder builder) {
        this.builder = builder;
    }

    protected abstract T copy(T src, T dest, boolean shallowCopy) throws Exception;

    final void setCopyBuilder(CopyBuilder builder) {
        this.builder = builder;
    }

    @SuppressWarnings("unchecked")
    protected final <E> E deepCopy(E value) {
        return value instanceof Copyable copyable ?
                (E) copyable.deepCopy(builder, builder.getContext()) :
                builder.deepCopy(value);
    }

    @SuppressWarnings("unchecked")
    protected T newInstance(T object, boolean shallowCopy) throws Exception {
        Constructor<?> constructor = object.getClass().getDeclaredConstructor();
        if (constructor.trySetAccessible()) {
            return (T) constructor.newInstance();
        }

        throw new CopyException("Cannot access the default constructor of " + object.getClass() + ".");
    }
}
