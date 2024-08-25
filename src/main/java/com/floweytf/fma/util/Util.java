package com.floweytf.fma.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;

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

    // Technically key isn't necessarily K
    public static <K, V> Optional<V> get(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    public static int colorRange(int val, int max) {
        return Mth.hsvToRgb(val / (max * 3.0f), 1.0f, 1.0f);
    }

    public static int colorRange(float val, float max) {
        return Mth.hsvToRgb(val / (max * 3.0f), 1.0f, 1.0f);
    }

    public static int colorRange(float val) {
        return Mth.hsvToRgb(val / 3.0f, 1.0f, 1.0f);
    }

    public static int colorRange(double val) {
        return Mth.hsvToRgb((float) (val / 3.0), 1.0f, 1.0f);
    }

    public static List<String> match(Pattern pattern, String string, int group) {
        final var matcher = pattern.matcher(string);
        final var result = new ArrayList<String>();
        while (matcher.find()) {
            result.add(matcher.group(group));
        }

        return result;
    }
}
