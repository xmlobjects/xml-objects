package org.xmlobjects.util.copy;

import java.util.Collection;
import java.util.Map;

public class CollectionCloner<T extends Collection> implements Cloner<T> {

    CollectionCloner() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) throws Exception {
        if (dest == null)
            dest = newInstance(src);

        clones.put(src, dest);

        if (shallowCopy)
            dest.addAll(src);
        else {
            for (Object value : src)
                dest.add(deepCopy(value, builder));
        }

        return dest;
    }
}
