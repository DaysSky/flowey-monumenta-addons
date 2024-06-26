package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class Utils {
    public static MutableComponent join(Component... components) {
        var inst = Component.empty();
        for (final var component : components) {
            inst.append(component);
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

    /*#b7bdf8*/
    public static void send(Component... message) {
        player().sendSystemMessage(join(
            withColor("[", FMAClient.CONFIG.bracketColor),
            withColor(FMAClient.CONFIG.tagText, FMAClient.CONFIG.tagColor).withStyle(ChatFormatting.BOLD),
            withColor("] ", FMAClient.CONFIG.bracketColor),
            colored(FMAClient.CONFIG.textColor).append(join(message))
        ));
    }

    public static void send(String message) {
        send(Component.literal(message));
    }

    public static void sendWarn(Component message) {
        send(Component.empty()
            .append(Component.literal("WARN").withStyle(ChatFormatting.GOLD))
            .append(" ")
            .append(message)
        );
    }

    public static void sendWarn(String message) {
        sendWarn(Component.literal(message));
    }

    public static void sendDebug(String message) {
        sendWarn("(debug/possible bug) " + message);
    }

    public static String timestamp(int ticks) {
        int second = ticks / 1000;
        int min = second / 60;
        int hr = min / 60;

        return String.format("%d:%02d:%02d.%03d",
            hr,
            min % 60,
            second % 60,
            ticks % 1000
        );
    }

    public static MutableComponent timestampComponent(int ticks) {
        return withColor(timestamp(ticks), FMAClient.CONFIG.numericColor);
    }

    public static Player player() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static void sendCommand(String command) {
        if (command.startsWith("/")) {
            FMAClient.LOGGER.warn("leading /");
        }
        FMAClient.LOGGER.info("running command as client: {}", command);
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand(command);
    }

    public static int computeEntityHealthColor(LivingEntity entity) {
        var hp = entity.getHealth();
        if (FMAClient.CONFIG.countAbsorptionAsHp) {
            hp += entity.getAbsorptionAmount();
        }

        final var ratio = hp / entity.getMaxHealth();

        if (ratio > FMAClient.CONFIG.goodHpPercent / 100) {
            return FMAClient.CONFIG.goodHpColor;
        }

        if (ratio > FMAClient.CONFIG.mediumHpPercent / 100) {
            return FMAClient.CONFIG.mediumHpColor;
        }

        if (ratio > FMAClient.CONFIG.lowHpPercent / 100) {
            return FMAClient.CONFIG.lowHpColor;
        }
        return FMAClient.CONFIG.criticalHpColor;
    }

    public static Component formatEnable(boolean flag) {
        return flag ? Component.literal("enabled").withStyle(ChatFormatting.GREEN) :
            Component.literal("disabled").withStyle(ChatFormatting.RED);
    }

    public static <T extends Record> void dumpStats(String translationRoot, T object) {
        try {
            send(Component.translatable(translationRoot + "." + "title").withStyle(ChatFormatting.UNDERLINE));
            for (final var part : object.getClass().getRecordComponents()) {
                boolean hasTimestamp = part.getAnnotationsByType(Timestamp.class).length != 0;
                final var value = part.getAccessor().invoke(object);

                if (part.getType() == int.class) {
                    if (hasTimestamp) {
                        send(
                            Component.translatable(
                                translationRoot + "." + part.getName(),
                                timestampComponent((int) value)
                            )
                        );
                    } else {
                        send(
                            Component.translatable(
                                translationRoot + "." + part.getName(),
                                withColor(value.toString(), FMAClient.CONFIG.numericColor)
                            )
                        );
                    }
                } else {
                    throw new IllegalStateException("idk");
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
