package com.floweytf.fma.util;

import java.lang.reflect.Field;

public class Util {
    public static long now() {
        return System.currentTimeMillis();
    }

    public static Field field(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
