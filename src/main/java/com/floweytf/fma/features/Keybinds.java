package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.ChatUtil;
import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    private static long meowMsNext = System.currentTimeMillis();

    public static void init() {
        KeyBindingHelper.registerKeyBinding(new AmecsKeyBinding(
            "key.fma.meow",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.fma",
            new KeyModifiers(false, false, false)
        ) {
            @Override
            public void onPressed() {
                final var current = System.currentTimeMillis();

                if (meowMsNext > current) {
                    return;
                }

                meowMsNext = current + 3000;

                ChatUtil.sendCommand(String.format(
                    "chat say %s %s",
                    FMAClient.config().chat.meowingChannel,
                    FMAClient.config().chat.meowingText
                ));
            }
        });

        KeyBindingHelper.registerKeyBinding(new AmecsKeyBinding(
            "key.fma.playerstats",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            "category.fma",
            new KeyModifiers(false, false, false)
        ) {
            @Override
            public void onPressed() {
                final var entity = Minecraft.getInstance().crosshairPickEntity;
                if (entity instanceof Player player) {
                    ChatUtil.sendCommand("ps " + player.getScoreboardName());
                }
            }
        });
    }
}
