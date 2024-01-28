/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2024 Claus Nagel <claus.nagel@gmail.com>
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

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class MapCloner<T extends Map> extends AbstractCloner<T> {

    MapCloner(CopyBuilder builder) {
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
        try {
            return super.newInstance(object, shallowCopy);
        } catch (Throwable e) {
            return (T) new HashMap<>();
        }
    }
}
