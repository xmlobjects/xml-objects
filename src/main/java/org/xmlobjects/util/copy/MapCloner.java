package org.xmlobjects.util.copy;

import java.util.Map;

public class MapCloner<T extends Map> implements Cloner<T> {

    MapCloner() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) throws Exception {
        if (dest == null)
            dest = newInstance(src);

        clones.put(src, dest);

        if (shallowCopy)
            dest.putAll(src);
        else {
            for (Object object : src.entrySet()) {
                Map.Entry entry = (Map.Entry) object;
                Object key = entry.getKey();
                Object value = entry.getValue();
                dest.put(deepCopy(key, builder), deepCopy(value, builder));
            }
        }

        return dest;
    }
}
