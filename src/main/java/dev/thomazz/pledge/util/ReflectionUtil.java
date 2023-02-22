package dev.thomazz.pledge.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

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

    public Method searchInterfaceMethod(Class<?> clazz, String name, Class<?>... args) {
        Queue<Class<?>> classes = new ArrayDeque<>();
        classes.add(clazz);

        while ((clazz = classes.poll()) != null) {
            try {
                return clazz.getDeclaredMethod(name, args);
            } catch (Exception ex) {
                classes.addAll(Arrays.asList(clazz.getInterfaces()));
            }
        }

        throw new IllegalArgumentException("Could not get method: " + name);
    }
}
