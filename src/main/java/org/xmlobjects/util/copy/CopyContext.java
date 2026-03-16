/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CopyContext {
    private static final Object NULL_CLONE = new Object();
    private final Map<Object, Object> clones = new IdentityHashMap<>();
    private boolean initial = true;

    CopyContext() {
    }

    boolean isInitial() {
        boolean result = initial;
        initial = false;
        return result;
    }

    Object getClone(Object src) {
        return clones.get(src);
    }

    void addClone(Object src, Object dest) {
        clones.put(src, dest != null ? dest : NULL_CLONE);
    }

    void addCloneIfAbsent(Object src, Object dest) {
        clones.putIfAbsent(src, dest != null ? dest : NULL_CLONE);
    }

    boolean isNullClone(Object src) {
        return src == null || src == NULL_CLONE;
    }

    public <T> CopyContext withClone(T src, Supplier<T> supplier) {
        if (src != null) {
            addClone(src, supplier.get());
        }

        return this;
    }

    public CopyContext withSelfCopy(Object src) {
        if (src != null) {
            addClone(src, src);
        }

        return this;
    }

    void clear() {
        clones.clear();
        initial = true;
    }
}
