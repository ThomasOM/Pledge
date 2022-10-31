package dev.thomazz.pledge.util;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class ReflectionUtil {
    public static Field getFieldByClassNames(Class<?> clazz, String... simpleNames)  throws NoSuchFieldException {
        for (String name : simpleNames) {
            for (Field field : clazz.getDeclaredFields()) {
                String typeSimpleName = field.getType().getSimpleName();
                if (name.equals(typeSimpleName)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }

        throw new NoSuchFieldException("Could not find field in class " + clazz.getName() + " with names " + Arrays.toString(simpleNames));
    }

    public static Field getFieldByType(Class<?> clazz, Class<?> type) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> foundType = field.getType();
            if (type.isAssignableFrom(foundType)) {
                return field;
            }
        }

        throw new NoSuchFieldException("Could not find field in class " + clazz.getName() + " with type " + type.getName());
    }
}
