package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.util.ChatUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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

    @WrapOperation(
        method = "handleChatInput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"
        )
    )
    private void sendWithChatChannel(ClientPacketListener instance, String message, Operation<Void> original) {
        if (!FMAClient.features().enableChatChannels) {
            original.call(instance, message);
            return;
        }

        ChatUtil.sendCommand(FMAClient.CHAT_CHANNELS.getChannel().buildSendCommand(message));
    }

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"
        ),
        index = 1
    )
    private int renderChannelBg(int x) {
        if (!FMAClient.features().enableChatChannels) {
            return x;
        }

        return x + FMAClient.CHAT_CHANNELS.promptTextWidth() + 3;
    }

    @ModifyConstant(
        method = "init",
        constant = @Constant(intValue = 4)
    )
    private int moveEditBox(int original) {
        if (!FMAClient.features().enableChatChannels) {
            return original;
        }

        return original + 3 + FMAClient.CHAT_CHANNELS.promptTextWidth();
    }
}
