package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SplitTiming {
    private int value = -1;
    private final String translationKey;
    private final Stopwatch splitStopwatch;
    private final Stopwatch totalStopwatch;

    public SplitTiming(Stopwatch splitStopwatch, Stopwatch totalStopwatch, String translationKey) {
        this.translationKey = translationKey;
        this.splitStopwatch = splitStopwatch;
        this.totalStopwatch = totalStopwatch;
    }

    public void done() {
        if (value != -1) {
            return;
        }

        value = splitStopwatch.reset();

        if (!FMAClient.CONFIG.enableTimers) {
            return;
        }

        Utils.send(Component.translatable(
            translationKey,
            Component.literal(Utils.timestamp(value)).withStyle(ChatFormatting.DARK_GREEN),
            Component.literal(Utils.timestamp(totalStopwatch.time())).withStyle(ChatFormatting.GRAY)
        ));
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return "SplitTiming[value = %d, translationKey = '%s']"
            .formatted(value, translationKey);
    }
}
