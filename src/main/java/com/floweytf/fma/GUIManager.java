package com.floweytf.fma;

import com.floweytf.fma.chat.ChatChannelManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

public class GUIManager {
    private final Minecraft minecraft = Minecraft.getInstance();

    private void renderChatChannelHud(PoseStack stack, float partialTicks) {
        if (!FMAClient.CONFIG.enableChatChannels) {
            return;
        }

        final var height = minecraft.getWindow().getGuiScaledHeight();

        GuiComponent.fill(
            stack,
            2, height - 14,
            2 + ChatChannelManager.getInstance().promptTextWidth(), height - 2,
            this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE)
        );

        GuiComponent.drawString(
            stack,
            minecraft.fontFilterFishy,
            ChatChannelManager.getInstance().promptText(),
            4,
            height - 12,
            0xffffffff
        );
    }

    public void render(PoseStack stack, float partialTicks) {
        renderChatChannelHud(stack, partialTicks);
    }
}
