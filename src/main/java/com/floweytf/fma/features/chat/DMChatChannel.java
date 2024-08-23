package com.floweytf.fma.features.chat;

import com.floweytf.fma.util.FormatUtil;
import static com.floweytf.fma.util.FormatUtil.literal;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class DMChatChannel extends ChatChannel {
    private final String playerName;

    public DMChatChannel(String playerName) {
        super(FormatUtil.join(
            literal("@"),
            literal(playerName, ChatFormatting.GRAY)
        ));
        this.playerName = playerName;
    }

    @Override
    public String buildSendCommand(String text) {
        return String.format("tell %s %s", playerName, text);
    }

    public String playerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return "DMChatChannel[playerName = " + playerName + "]";
    }
}
