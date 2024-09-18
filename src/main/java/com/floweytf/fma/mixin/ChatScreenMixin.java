package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @WrapOperation(
        method = "handleChatInput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"
        )
    )
    private void handleFMACommands(ClientPacketListener instance, String string, Operation<Void> original) {
        if (FMAClient.COMMANDS.parseAccepts(string)) {
            FMAClient.COMMANDS.run(string);
        } else {
            original.call(instance, string);
        }
    }
}
