/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.util.*;

@SuppressWarnings("rawtypes")
public class CollectionCloner<T extends Collection> extends AbstractCloner<T> {

    protected CollectionCloner(CopyBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, boolean shallowCopy) {
        if (shallowCopy) {
            dest.addAll(src);
        } else {
            for (Object value : src) {
                dest.add(deepCopy(value));
            }
        }

        return dest;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T newInstance(T object, boolean shallowCopy) {
        if (object instanceof SortedSet<?> sortedSet) {
            return (T) new TreeSet<>(sortedSet.comparator());
        }

        try {
            return super.newInstance(object, shallowCopy);
        } catch (Exception e) {
            //
        }

        if (object instanceof List) {
            return (T) new ArrayList<>(object.size());
        } else if (object instanceof Set) {
            return (T) new HashSet<>(object.size());
        } else if (object instanceof Deque) {
            return (T) new ArrayDeque<>(object.size());
        } else {
            throw new CopyException("Failed to create an instance of " + object.getClass() + ".");
        }
    }
}
