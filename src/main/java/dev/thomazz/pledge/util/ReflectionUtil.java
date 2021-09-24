package dev.thomazz.pledge.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public final class ReflectionUtil {
    public static <T> T invokeStatic(Class<?> clazz, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = clazz.getDeclaredMethod(method);
        return (T) m.invoke(null);
    }

    public static Field get(Class<?> oClass, Class<?> type, int index) {
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

        return null;
    }

    public static Field getFieldByClassNames(Class<?> clazz, String... simpleNames) {
        for (Field field : clazz.getDeclaredFields()) {
            String typeSimpleName = field.getType().getSimpleName();
            for (String name : simpleNames) {
                if (name.equals(typeSimpleName)) {
                    return field;
                }
            }
        }

        return null;
    }

    public static Field getFieldByType(Class<?> clazz, Class<?> type) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> foundType = field.getType();
            if (type.isAssignableFrom(foundType)) {
                return field;
            }
        }

        return null;
    }

    public static void removeFinalModifier(Field field) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Java 12 compatibility
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field classField : fields) {
                    if ("modifiers".equals(classField.getName())) {
                        classField.setAccessible(true);
                        classField.set(field, modifiers & ~Modifier.FINAL);
                        break;
                    }
                }
            }
        }
    }
}
