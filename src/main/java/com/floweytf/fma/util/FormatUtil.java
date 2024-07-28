package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

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

    public static MutableComponent join(MutableComponent root, Component delimiter, List<Component> components) {
        for (int i = 0; i < components.size(); i++) {
            root.append(components.get(i));
            if (i + 1 != components.size()) {
                root.append(delimiter);
            }
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

    public static String fmtDouble(double d) {
        return fmtDouble(d, true);
    }

    public static String fmtDouble(double d, boolean showPlus) {
        String prefix = (showPlus && d > 0) ? "+" : "";
        if (d == (int) d) {
            return prefix + (int) d;
        } else {
            return prefix + d;
        }
    }

    private static final ResourceLocation SPACING = new ResourceLocation("fma:formatting/spacing");

    public static Component pad(int pixels) {
        var res = Component.empty();

        while (pixels > 0) {
            for (int i = 9; i >= 0; i--) {
                final var spacing = 1 << i;
                if (pixels >= spacing) {
                    pixels -= spacing;
                    res =
                        res.append(Component.literal(Character.toString('0' + i))).withStyle(style -> style.withFont(SPACING));
                    break;
                }
            }
        }

        return res;
    }

    public static List<Component> tabulate(Font font, List<? extends List<Component>> table) {
        // we need to iterate by columns...
        final var cols = table.get(0).size();

        final var components = new MutableComponent[table.size()];

        for (int i = 0; i < components.length; i++) {
            components[i] = Component.empty();
        }

        for (int j = 0; j < cols; j++) {
            final var width = new int[table.size()];
            var maxWidth = 0;
            for (int i = 0; i < table.size(); i++) {
                maxWidth = Math.max(maxWidth, width[i] = font.width(table.get(i).get(j)));
            }

            if (j + 1 == cols) {
                for (int i = 0; i < table.size(); i++) {
                    components[i] = components[i].append(table.get(i).get(j));
                }
            } else {
                for (int i = 0; i < table.size(); i++) {
                    components[i] = components[i].append(table.get(i).get(j))
                        .append(pad(maxWidth - width[i]))
                        .append(" ");
                }
            }
        }

        return List.of(components);
    }
}
