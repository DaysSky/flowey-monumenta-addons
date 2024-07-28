package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.Graphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GUIManager {
    private final Minecraft minecraft = Minecraft.getInstance();

    private void renderChatChannelHud(GuiGraphics graphics) {
        if (!FMAClient.features().enableChatChannels) {
            return;
        }

        final var height = minecraft.getWindow().getGuiScaledHeight();

        Graphics.fill(
            graphics,
            2, height - 14,
            2 + FMAClient.CHAT_CHANNELS.promptTextWidth(), height - 2,
            this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE)
        );

        Graphics.drawString(
            graphics,
            minecraft.fontFilterFishy,
            FMAClient.CHAT_CHANNELS.promptText(),
            4,
            height - 12,
            0xffffffff
        );
    }

    public void render(GuiGraphics graphics, float partialTicks) {
        renderChatChannelHud(graphics);
    }
}
