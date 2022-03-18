package dev.thomazz.pledge.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class ReflectionUtil {
    public static Field get(Class<?> oClass, Class<?> type, int index) throws NoSuchFieldException {
        int i = 0;
        for (Field field : oClass.getDeclaredFields()) {
            if (field.getType() == type) {
                if (i == index) {
                    field.setAccessible(true);
                    return field;
                }
                i++;
            }
        }

        throw new NoSuchFieldException("Could not find field of class " + type.getName() + " with index " + index);
    }

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
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Could not find field in class " + clazz.getName() + " with type " + type.getName());
    }

    public static Method getMethodByNames(Class<?> clazz, String... names) throws NoSuchMethodException {
        for (String name : names) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (name.equals(method.getName())) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        throw new NoSuchMethodException("Could not find method in class " + clazz.getName() + " with names " + Arrays.toString(names));
    }

    public static Class<?> getSuperClassByName(Class<?> clazz, String simpleName) {
        if (!clazz.getSimpleName().equals(simpleName)) {
            Class<?> superClazz;
            while((superClazz = clazz.getSuperclass()) != null) {
                if (superClazz.getSimpleName().equals(simpleName)) {
                    break;
                }
            }

            return superClazz;
        } else {
            return clazz;
        }
    }

    public static void removeFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
