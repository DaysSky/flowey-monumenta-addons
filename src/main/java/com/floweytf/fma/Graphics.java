package com.floweytf.fma;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Graphics {
    public static final Map<String, ResourceLocation> RARITY_TO_TEXTURE = Map.of(
        "artifact", new ResourceLocation("fma:textures/inventory_overlay/artifact.png"),
        "epic", new ResourceLocation("fma:textures/inventory_overlay/epic.png"),
        "legendary", new ResourceLocation("fma:textures/inventory_overlay/legendary.png"),
        "rare", new ResourceLocation("fma:textures/inventory_overlay/rare.png"),
        "unique", new ResourceLocation("fma:textures/inventory_overlay/unique.png")
    );

    public static final List<ResourceLocation> CHARM_RARITY_TO_TEXTURE = List.of(
        new ResourceLocation("fma:textures/inventory_overlay/charm_common.png"),
        new ResourceLocation("fma:textures/inventory_overlay/charm_uncommon.png"),
        new ResourceLocation("fma:textures/inventory_overlay/charm_rare.png"),
        new ResourceLocation("fma:textures/inventory_overlay/charm_epic.png"),
        new ResourceLocation("fma:textures/inventory_overlay/charm_legendary.png")
    );

    public static void renderTexture(PoseStack poseStack, int x, int y, float uOff, float vOff, int w, int h, int texW,
                                     int texH, ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(poseStack, x, y, uOff, vOff, w, h, texW, texH);
    }

    public static void fill(PoseStack stack, int sX, int sY, int eX, int eY, int color) {
        GuiComponent.fill(stack, sX, sY, eX, eY, color);
    }

    public static void drawString(PoseStack poseStack, Font font, Component text, int x, int y, int color) {
        GuiComponent.drawString(poseStack, font, text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, Font font, Component text, int x, int y, int z, int color) {
        poseStack.pushPose();
        poseStack.translate(0, 0, z);
        GuiComponent.drawString(poseStack, font, text, x, y, color);
        poseStack.popPose();
    }
}
