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

import java.util.*;

@SuppressWarnings("rawtypes")
public class CollectionCloner<T extends Collection> extends AbstractCloner<T> {

    CollectionCloner(CopyBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, boolean shallowCopy) {
        if (shallowCopy)
            dest.addAll(src);
        else {
            for (Object value : src)
                dest.add(deepCopy(value));
        }

        return dest;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T newInstance(T object, boolean shallowCopy) {
        try {
            return super.newInstance(object, shallowCopy);
        } catch (Throwable e) {
            //
        }

        if (object instanceof List)
            return (T) new ArrayList<>(object.size());
        else if (object instanceof Set)
            return (T) new HashSet<>(object.size());
        else if (object instanceof Deque)
            return (T) new ArrayDeque<>(object.size());
        else
            throw new CopyException("Failed to create an instance of " + object.getClass() + ".");
    }
}
