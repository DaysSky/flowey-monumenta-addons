package com.floweytf.fma.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class HoverControlHandler {
    private ItemStack stackInstance = null;
    private boolean wasControlDown = false;
    private boolean shouldDisplayAdvanced = false;

    private void update(ItemStack stack) {
        if (stackInstance != stack) {
            stackInstance = stack;
            shouldDisplayAdvanced = false;
        }

        final var isControlDown = InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            InputConstants.KEY_LCONTROL
        );

        if (wasControlDown != isControlDown && isControlDown) {
            shouldDisplayAdvanced = !shouldDisplayAdvanced;
        }

        wasControlDown = isControlDown;
    }

    public boolean isEnabled(ItemStack stack) {
        update(stack);
        return shouldDisplayAdvanced || InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            InputConstants.KEY_LSHIFT
        );
    }
}
