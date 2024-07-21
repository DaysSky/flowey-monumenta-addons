package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import java.time.Duration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class FormatUtil {
    public static MutableComponent join(Component... components) {
        return join(Component.empty(), List.of(components));
    }

    public static MutableComponent join(MutableComponent root, Iterable<Component> components) {
        for (final var component : components) {
            root.append(component);
        }
        return root;
    }

    public static MutableComponent buildTooltip(List<? extends Component> components) {
        var inst = FormatUtil.colored(FMAClient.appearance().textColor);
        for (int i = 0; i < components.size(); i++) {
            var component = components.get(i);
            inst.append(component);
            if (i + 1 != components.size()) {
                inst.append(Component.literal("\n"));
            }
        }
        return inst;
    }

    public static MutableComponent withColor(MutableComponent component, int color) {
        return component.withStyle(s -> s.withColor(color));
    }

    public static MutableComponent withColor(String component, int color) {
        return withColor(Component.literal(component), color);
    }

    public static MutableComponent colored(int color) {
        return withColor(Component.empty(), color);
    }

    private static String formatTimestamp(long ticks) {
        long second = ticks / 1000;
        long min = second / 60;
        long hr = min / 60;

        if (hr == 0) {
            return String.format("%d:%02d.%03d",
                min % 60,
                second % 60,
                ticks % 1000
            );
        } else {
            return String.format("%d:%02d:%02d.%03d",
                hr,
                min % 60,
                second % 60,
                ticks % 1000
            );
        }
    }

    public static MutableComponent timestamp(long ticks) {
        return withColor(formatTimestamp(ticks), FMAClient.appearance().numericColor);
    }

    public static MutableComponent timestampAlt(long ticks) {
        return withColor(formatTimestamp(ticks), FMAClient.appearance().detailColor);
    }

    public static MutableComponent numeric(Object value) {
        return withColor(String.valueOf(value), FMAClient.appearance().numericColor);
    }

    public static MutableComponent altText(String text) {
        return withColor(text, FMAClient.appearance().altTextColor);
    }

    public static MutableComponent playerNameText(String text) {
        return withColor(text, FMAClient.appearance().playerNameColor);
    }
}
