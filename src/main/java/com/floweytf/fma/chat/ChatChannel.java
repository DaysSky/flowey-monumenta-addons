package com.floweytf.fma.chat;

import net.minecraft.network.chat.Component;

public abstract class ChatChannel {
    private final Component text;

    protected ChatChannel(Component text) {
        this.text = text;
    }

    public Component getPromptText() {
        return text;
    }

    public abstract String buildSendCommand(String text);
}
