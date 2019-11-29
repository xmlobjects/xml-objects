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
    public T copy(T src, T dest, boolean shallowCopy) throws Exception {
        if (shallowCopy) {
            for (Field field : fields)
                field.set(dest, field.get(src));
        } else {
            for (Field field : fields) {
                Object value = field.get(src);
                field.set(dest, deepCopy(value));
            }
        }

        return dest;
    }
}
