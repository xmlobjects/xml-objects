package org.xmlobjects.util.copy;

import java.lang.reflect.Array;
import java.util.Map;

public class ArrayCloner implements Cloner<Object[]> {

    ArrayCloner() {
    }

    @Override
    public Object[] copy(Object[] src, Object[] dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) {
        Class<?> componentType = src.getClass().getComponentType();

        if (dest == null)
            dest = (Object[]) Array.newInstance(componentType, src.length);

        if (shallowCopy || componentType.isPrimitive())
            System.arraycopy(src, 0, dest, 0, src.length);
        else {
            for (int i = 0; i < src.length; i++)
                dest[i] = deepCopy(src[i], builder);
        }

        return dest;
    }
}
