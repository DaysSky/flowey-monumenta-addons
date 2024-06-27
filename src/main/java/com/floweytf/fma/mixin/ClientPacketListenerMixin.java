package com.floweytf.fma.mixin;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.events.ClientReceiveSystemChatEvent;
import com.floweytf.fma.events.ClientSetTitleEvent;
import com.floweytf.fma.events.EventResult;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(
        method = "setTitleText",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setTitle(Lnet/minecraft/network/chat/Component;)V"
        ),
        cancellable = true
    )
    private void onRecvTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        FMAClient.LOGGER.debug("Recv title: {}", packet.getText());
        if (ClientSetTitleEvent.TITLE.invoker().onSetTitle(packet.getText()) != EventResult.CONTINUE) {
            ci.cancel();
        }
    }

    @Inject(
        method = "setSubtitleText",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setSubtitle(Lnet/minecraft/network/chat/Component;)V"
        ),
        cancellable = true
    )
    private void onRecvSubtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        FMAClient.LOGGER.debug("Recv subtitle: {}", packet.getText());
        if (ClientSetTitleEvent.SUBTITLE.invoker().onSetTitle(packet.getText()) != EventResult.CONTINUE) {
            ci.cancel();
        }
    }

    @Inject(
        method = "setActionBarText",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
        ),
        cancellable = true
    )
    private void onRecvActionBarText(ClientboundSetActionBarTextPacket packet, CallbackInfo ci) {
        FMAClient.LOGGER.debug("Recv action bar: {}", packet.getText());
        if (ClientSetTitleEvent.ACTIONBAR.invoker().onSetTitle(packet.getText()) != EventResult.CONTINUE) {
            ci.cancel();
        }
    }

    @Inject(
        method = "handleSystemChat",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage" +
                "(Lnet/minecraft/network/chat/Component;Z)V"
        ),
        cancellable = true
    )
    private void onRecvChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        FMAClient.LOGGER.debug("Recv chat: {}", packet.content());
        if (ClientReceiveSystemChatEvent.EVENT.invoker().onMessage(packet.content()) != EventResult.CONTINUE) {
            ci.cancel();
        }
    }
}
