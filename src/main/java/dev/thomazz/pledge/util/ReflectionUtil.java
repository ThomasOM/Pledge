package dev.thomazz.pledge.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;

@UtilityClass
public class ReflectionUtil {
    public Field getFieldByClassNames(Class<?> clazz, String... simpleNames)  throws NoSuchFieldException {
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

    public Field getFieldByType(Class<?> clazz, Class<?> type) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> foundType = field.getType();
            if (type.isAssignableFrom(foundType)) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Could not find field in class " + clazz.getName() + " with type " + type.getName());
    }

    public Object getNonNullFieldByType(Object instance, Class<?> type) throws ReflectiveOperationException {
        final Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> foundType = field.getType();
            if (type.isAssignableFrom(foundType)) {
                field.setAccessible(true);
                final Object o = field.get(instance);
                if (o != null) return o;
            }
        }

        throw new NoSuchFieldException("Could not find non-null field in class " + clazz.getName() + " with type " + type.getName());
    }
}
