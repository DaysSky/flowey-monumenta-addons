package com.floweytf.fma.features.chat;

import static com.floweytf.fma.util.FormatUtil.literal;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class SystemChatChannel extends ChatChannel {
    private final String channelId;

    public SystemChatChannel(String channelId, String displayName, ChatFormatting color) {
        super(literal("#" + displayName, color));
        this.channelId = channelId;
    }

    public SystemChatChannel(String channelId, String displayName, UnaryOperator<Style> style) {
        super(literal("#" + displayName, style));
        this.channelId = channelId;
    }

    @Override
    public String buildSendCommand(String text) {
        return String.format("chat say %s %s", channelId, text);
    }

    @Override
    public String toString() {
        return "SystemChatChannel[channelId = " + channelId + "]";
    }
}
