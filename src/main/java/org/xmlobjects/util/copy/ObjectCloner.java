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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectCloner<T> extends AbstractCloner<T> {
    private final List<Field> fields = new ArrayList<>();

    ObjectCloner(Class<?> type, CopyBuilder builder) {
        super(builder);

        do {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }

                field.setAccessible(true);
                this.fields.add(field);
            }
        } while ((type = type.getSuperclass()) != Object.class && type != null);
    }

    @Override
    public T copy(T src, T dest, boolean shallowCopy) throws Exception {
        if (shallowCopy) {
            for (Field field : fields) {
                field.set(dest, field.get(src));
            }
        } else {
            for (Field field : fields) {
                Object value = field.get(src);
                field.set(dest, deepCopy(value));
            }
        }

        return dest;
    }
}
