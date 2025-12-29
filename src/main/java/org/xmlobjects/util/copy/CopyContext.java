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

package org.xmlobjects.util.copy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class CopyContext {
    private static final Object NULL_CLONE = new Object();
    private final Map<Object, Object> clones = new IdentityHashMap<>();
    private final AtomicBoolean initial = new AtomicBoolean(true);

    CopyContext() {
    }

    boolean isInitial() {
        return initial.getAndSet(false);
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
        initial.set(true);
    }
}
