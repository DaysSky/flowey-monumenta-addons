package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;

public class Utils {
    public static Component join(Component... components) {
        var inst = Component.empty();
        for (final var component : components) {
            inst.append(component);
        }
        return inst;
    }

    public static void send(Component... message) {
        player().sendSystemMessage(
            Component.empty()
                .append("[")
                .append(Component.literal("MAID").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .append("] ")
                .append(Utils.join(message))
        );
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

    public static <T> Supplier<T> lazy(Supplier<T> eager) {
        return new Supplier<>() {
            private T inst = null;

            @Override
            public T get() {
                if (inst == null) {
                    inst = eager.get();
                }
                return inst;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> T c(Object o) {
        return (T) o;
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
            Utils.send(Component.translatable(translationRoot + "." + "title"));
            for (final var part : object.getClass().getRecordComponents()) {
                boolean hasTimestamp = part.getAnnotationsByType(Timestamp.class).length != 0;
                final var value = part.getAccessor().invoke(object);

                if (part.getType() == int.class) {
                    if (hasTimestamp) {
                        Utils.send(Component.translatable(
                            translationRoot + "." + part.getName(),
                            Component.literal(Utils.timestamp((Integer) value)).withStyle(ChatFormatting.DARK_GREEN)
                        ));
                    } else {
                        Utils.send(Component.translatable(
                            translationRoot + "." + part.getName(),
                            Component.literal(value.toString()).withStyle(ChatFormatting.DARK_GREEN)
                        ));
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
