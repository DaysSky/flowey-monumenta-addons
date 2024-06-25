package com.floweytf.fma.util;

public class Stopwatch {
    private long timer;

    public Stopwatch() {
        reset();
    }

    public int reset() {
        final var curr = System.currentTimeMillis();
        final var ret = curr - timer;
        timer = curr;
        return (int) ret;
    }

    public int time() {
        return (int) (System.currentTimeMillis() - timer);
    }
}
