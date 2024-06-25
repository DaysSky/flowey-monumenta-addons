package com.floweytf.fma.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public class SystemChatChannel extends ChatChannel {
    private final String command;
    private final Component shorthand;

    private static Component format(MutableComponent base, Component... parts) {
        for (final var part : parts) {
            base = base.append(part);
        }
        return base;
    }

    protected SystemChatChannel(String command, ChatFormatting color, Component... parts) {
        super(Component.literal("#").append(format(Component.empty().withStyle(color), parts)));
        this.command = command;
        this.shorthand = Component.literal(command).withStyle(color);
    }

    protected SystemChatChannel(String command, UnaryOperator<Style> style, Component... parts) {
        super(Component.literal("#").append(format(Component.empty().withStyle(style), parts)));
        this.command = command;
        this.shorthand = Component.literal(command).withStyle(style);
    }

    public Component shorthand() {
        return shorthand;
    }

    public String command() {
        return command;
    }

    @Override
    public String buildSendCommand(String text) {
        return String.format("%s %s", command, text);
    }

    @Override
    public String toString() {
        return "SystemChatChannel[command = " + command + "]";
    }
}
