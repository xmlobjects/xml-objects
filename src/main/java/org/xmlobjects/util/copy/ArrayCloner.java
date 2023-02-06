/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2023 Claus Nagel <claus.nagel@gmail.com>
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

import java.lang.reflect.Array;

public class ArrayCloner extends AbstractCloner<Object[]> {

    ArrayCloner(CopyBuilder builder) {
        super(builder);
    }

    @Override
    public Object[] copy(Object[] src, Object[] dest, boolean shallowCopy) {
        if (shallowCopy || src.getClass().getComponentType().isPrimitive())
            System.arraycopy(src, 0, dest, 0, src.length);
        else {
            for (int i = 0; i < src.length; i++)
                dest[i] = deepCopy(src[i]);
        }

        return dest;
    }

    @Override
    public Object[] newInstance(Object[] object, boolean shallowCopy) {
        return (Object[]) Array.newInstance(object.getClass().getComponentType(), object.length);
    }
}
