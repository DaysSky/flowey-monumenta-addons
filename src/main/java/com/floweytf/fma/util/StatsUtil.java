package com.floweytf.fma.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static com.floweytf.fma.util.ChatUtil.send;
import static com.floweytf.fma.util.FormatUtil.numeric;
import static com.floweytf.fma.util.FormatUtil.timestamp;

public class StatsUtil {
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Time {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Detail {
    }

    public static int logTime(String translation, long startTime, long deltaBegin, long... entries) {
        if (entries.length == 0)
            throw new IllegalArgumentException();

        List<Component> tooltipLines = new ArrayList<>();

        var base = deltaBegin;
        for (int i = 0; i < entries.length - 1; i++) {
            tooltipLines.add(Component.translatable(
                translation + "." + i,
                FormatUtil.timestamp(entries[i] - base)
            ));
            base = entries[i];
        }

        final var last = entries[entries.length - 1];

        final var text = Component.translatable(
            translation,
            FormatUtil.timestamp(last - deltaBegin),
            FormatUtil.timestampAlt(last - startTime)
        );

        if (entries.length == 1) {
            send(text);
        } else {
            final var event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, FormatUtil.buildTooltip(tooltipLines));
            send(text.withStyle(style -> style.withHoverEvent(event)));
        }

        return (int) (last - deltaBegin);
    }

    public static <T> void dumpStats(String translationRoot, T object) {
        try {
            List<MutableComponent> tooltipLines = new ArrayList<>();
            List<MutableComponent> regularLines = new ArrayList<>();

            for (final var part : object.getClass().getFields()) {
                boolean isTimestamp = part.getAnnotationsByType(Time.class).length != 0;
                boolean isDetail = part.getAnnotationsByType(Detail.class).length != 0;

                final var value = part.get(object);
                MutableComponent text;

                if (part.getType() == int.class) {
                    if (isTimestamp) {
                        text = Component.translatable(
                            translationRoot + "." + part.getName(),
                            timestamp((int) value)
                        );
                    } else {
                        text = Component.translatable(
                            translationRoot + "." + part.getName(),
                            numeric(value.toString())
                        );
                    }
                } else {
                    throw new IllegalStateException("idk");
                }

                (isDetail ? tooltipLines : regularLines).add(text);
            }

            final var hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, FormatUtil.buildTooltip(tooltipLines));
            send(Component.translatable(translationRoot + "." + "title").withStyle(ChatFormatting.UNDERLINE));
            for (final var regularLine : regularLines) {
                send(regularLine.withStyle(style -> style.withHoverEvent(hover)));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
