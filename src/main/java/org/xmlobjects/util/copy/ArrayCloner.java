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
