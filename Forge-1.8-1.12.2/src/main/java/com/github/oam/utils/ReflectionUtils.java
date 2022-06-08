package com.github.oam.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtils {

    public static <T> T getField(final Object ob, final Class<T> fieldClazz, final int index) {
        int i = 0;
        for (Field field : ob.getClass().getDeclaredFields()) {
            if (field.getType().equals(fieldClazz)) {
                if (i == index) {
                    try {
                        field.setAccessible(true);
                        return (T) field.get(ob);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        return null;
                    }
                }
                i++;
            }
        }
        return null;
    }

    public static <T> void setField(final Object ob, final T value, final Class<T> fieldClazz, final int index) {
        int i = 0;
        for (Field field : ob.getClass().getDeclaredFields()) {
            if (field.getType().equals(fieldClazz)) {
                if (i == index) {
                    try {
                        field.setAccessible(true);
                        field.set(ob, value);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    break;
                }
                i++;
            }
        }
    }

    public static Method getMethod(final Object ob, final String... methodNames) {
        final List<String> possibleNames = new ArrayList<>();
        Collections.addAll(possibleNames, methodNames);
        for (Method method : ob.getClass().getMethods()) {
            if (possibleNames.contains(method.getName())) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    public static Class<?> getClass(final String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

}
