/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2022 Claus Nagel <claus.nagel@gmail.com>
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
        return value instanceof Copyable ?
                (E) ((Copyable) value).deepCopy(builder) :
                builder.deepCopy(value);
    }

    @SuppressWarnings("unchecked")
    public T newInstance(T object, boolean shallowCopy) throws Exception {
        Constructor<?> constructor = object.getClass().getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }
}
