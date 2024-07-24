package com.floweytf.fma.util;

import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {
    public static long now() {
        return System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public static <T> T c(Object o) {
        return (T) o;
    }

    public static <T, C extends Collection<T>> Collector<T, ?, C> colAppend(C collection) {
        return Collectors.toCollection(() -> collection);
    }
}
