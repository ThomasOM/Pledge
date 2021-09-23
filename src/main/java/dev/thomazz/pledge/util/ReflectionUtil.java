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

    public static <T> T get(Object o, String f) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    public static void set(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(o, value);
    }

    public static <T> Field get(Class<?> oClass, Class<?> type, int index) {
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
