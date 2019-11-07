package org.xmlobjects.util.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectCloner<T> implements Cloner<T> {
    private List<Field> fields = new ArrayList<>();

    ObjectCloner(Class<?> type) {
        do {
            try {
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers))
                        continue;

                    if (!field.isAccessible())
                        field.setAccessible(true);

                    this.fields.add(field);
                }
            } catch (Throwable e) {
                //
            }
        } while ((type = type.getSuperclass()) != Object.class && type != null);
    }

    @Override
    public T copy(T src, T dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) throws Exception {
        if (dest == null)
            dest = newInstance(src);

        clones.put(src, dest);

        if (shallowCopy) {
            for (Field field : fields)
                field.set(dest, field.get(src));
        } else {
            for (Field field : fields) {
                Object value = field.get(src);
                field.set(dest, deepCopy(value, builder));
            }
        }

        return dest;
    }
}
