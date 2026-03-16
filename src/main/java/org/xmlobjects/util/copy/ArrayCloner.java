/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

import java.lang.reflect.Array;

public class ArrayCloner extends AbstractCloner<Object> {

    ArrayCloner(CopyBuilder builder) {
        super(builder);
    }

    @Override
    public Object copy(Object src, Object dest, boolean shallowCopy) {
        if (src instanceof Object[] srcArray && dest instanceof Object[] destArray) {
            if (shallowCopy) {
                System.arraycopy(srcArray, 0, destArray, 0, srcArray.length);
            } else {
                for (int i = 0; i < srcArray.length; i++) {
                    destArray[i] = deepCopy(srcArray[i]);
                }
            }
        } else {
            System.arraycopy(src, 0, dest, 0, Array.getLength(src));
        }

        return dest;
    }

    @Override
    public Object newInstance(Object object, boolean shallowCopy) {
        return Array.newInstance(object.getClass().getComponentType(), Array.getLength(object));
    }
}
