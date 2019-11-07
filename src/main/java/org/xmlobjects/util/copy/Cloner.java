package org.xmlobjects.util.copy;

import java.lang.reflect.Constructor;
import java.util.Map;

public interface Cloner<T> {
    T copy(T src, T dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) throws Exception;

    @SuppressWarnings("unchecked")
    default <E> E deepCopy(E value, CopyBuilder builder) {
        return value instanceof Copyable ?
                (E) ((Copyable) value).deepCopy(builder) :
                builder.deepCopy(value);
    }

    @SuppressWarnings("unchecked")
    default T newInstance(T object) throws Exception {
        Constructor constructor = object.getClass().getDeclaredConstructor();
        if (!constructor.isAccessible())
            constructor.setAccessible(true);

        return (T) constructor.newInstance();
    }
}
