package com.floweytf.fma.util;

import com.floweytf.fma.FMAClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Objects;

import static com.floweytf.fma.util.FormatUtil.*;

public class ChatUtil {
    public static void send(Component... message) {
        final var config = FMAClient.CONFIG.get().chatAppearance;
        FMAClient.player().sendSystemMessage(join(
            withColor("[", config.bracketColor),
            withColor(config.tagText, config.tagColor).withStyle(ChatFormatting.BOLD),
            withColor("] ", config.bracketColor),
            colored(config.textColor).append(join(message))
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

    public static void sendCommand(String command) {
        if (command.startsWith("/")) {
            FMAClient.LOGGER.warn("leading /");
        }
        FMAClient.LOGGER.debug("running command as client: {}", command);
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand(command);
    }
}
