package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.chat.ChatChannelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GUIManager {
    private final Minecraft minecraft = Minecraft.getInstance();

    private void renderChatChannelHud(GuiGraphics graphics) {
        if (!FMAClient.CONFIG.get().features.enableChatChannels) {
            return;
        }

        final var height = minecraft.getWindow().getGuiScaledHeight();

        graphics.fill(
            2, height - 14,
            2 + ChatChannelManager.getInstance().promptTextWidth(), height - 2,
            this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE)
        );

        graphics.drawString(
            minecraft.fontFilterFishy,
            ChatChannelManager.getInstance().promptText(),
            4,
            height - 12,
            0xffffffff
        );
    }

    public void render(GuiGraphics graphics, float partialTicks) {
        renderChatChannelHud(graphics);
    }
}
