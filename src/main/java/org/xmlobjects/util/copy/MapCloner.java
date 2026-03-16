/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("rawtypes")
public class MapCloner<T extends Map> extends AbstractCloner<T> {

    protected MapCloner(CopyBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, boolean shallowCopy) {
        if (shallowCopy) {
            dest.putAll(src);
        } else {
            for (Object object : src.entrySet()) {
                Map.Entry entry = (Map.Entry) object;
                Object key = entry.getKey();
                Object value = entry.getValue();
                dest.put(deepCopy(key), deepCopy(value));
            }
        }

        return dest;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T newInstance(T object, boolean shallowCopy) {
        if (object instanceof SortedMap sortedMap) {
            return (T) new TreeMap<>(sortedMap.comparator());
        }

        try {
            return super.newInstance(object, shallowCopy);
        } catch (Exception e) {
            return (T) new HashMap<>();
        }
    }
}
