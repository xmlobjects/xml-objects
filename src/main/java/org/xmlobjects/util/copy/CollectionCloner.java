package org.xmlobjects.util.copy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class CollectionCloner<T extends Collection> extends AbstractCloner<T> {

    CollectionCloner(CopyBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T src, T dest, boolean shallowCopy) {
        if (shallowCopy)
            dest.addAll(src);
        else {
            for (Object value : src)
                dest.add(deepCopy(value));
        }

        return dest;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T newInstance(T object, boolean shallowCopy) {
        try {
            return super.newInstance(object, shallowCopy);
        } catch (Throwable e) {
            //
        }

        if (object instanceof List)
            return (T) new ArrayList<>(object.size());
        else if (object instanceof Set)
            return (T) new HashSet<>(object.size());
        else if (object instanceof Deque)
            return (T) new ArrayDeque<>(object.size());
        else
            throw new CopyException("Failed to create an instance of " + object.getClass() + ".");
    }
}
