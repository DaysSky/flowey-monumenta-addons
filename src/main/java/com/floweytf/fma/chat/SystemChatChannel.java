package com.floweytf.fma.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public class SystemChatChannel extends ChatChannel {
    private final String channelId;

    public SystemChatChannel(String channelId, String displayName, ChatFormatting color) {
        super(Component.literal("#").append(Component.literal(displayName).withStyle(color)));
        this.channelId = channelId;
    }

    public SystemChatChannel(String channelId, String displayName, UnaryOperator<Style> style) {
        super(Component.literal("#").append(Component.literal(displayName).withStyle(style)));
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
