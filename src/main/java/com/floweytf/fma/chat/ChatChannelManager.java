package com.floweytf.fma.chat;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.FMAConfig;
import com.floweytf.fma.debug.DebugInfoExporter;
import com.floweytf.fma.util.ChatUtil;
import com.floweytf.fma.util.FormatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatChannelManager implements DebugInfoExporter {
    private static ChatChannelManager INSTANCE;

    public static ChatChannelManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatChannelManager(FMAClient.CONFIG.getConfig());
            FMAClient.CONFIG.registerSaveListener((configHolder, config) -> {
                INSTANCE.reload(config);
                return InteractionResult.PASS;
            });
        }

        return INSTANCE;
    }

    public static final SystemChatChannel GLOBAL = new SystemChatChannel("g", "global", ChatFormatting.WHITE);

    // cache info
    private Component cachePromptText;
    private int cachePromptWidth;

    // channel info
    @NotNull
    private ChatChannel currentChannel;
    private int builtinIndex = 0;
    private int dmIndex = 0;
    private List<SystemChatChannel> systemChannels = List.of(GLOBAL);
    private final List<DMChatChannel> dmChannels = new ArrayList<>();

    private void setPrompt(Component text) {
        cachePromptText = text;
        final var oldTextWidth = cachePromptWidth;
        cachePromptWidth = Minecraft.getInstance().fontFilterFishy.width(cachePromptText);
        if (Minecraft.getInstance().screen instanceof ChatScreen screen) {
            // apply position delta (better compatibility)
            screen.input.setX(screen.input.getX() - oldTextWidth + cachePromptWidth);
        }
    }

    public void openDm(String playerName) {
        for (int i = 0; i < dmChannels.size(); i++) {
            if (dmChannels.get(i).playerName().equals(playerName)) {
                dmIndex = i;
                setChannel(dmChannels.get(i));
                return;
            }
        }

        dmIndex = dmChannels.size();
        final var channel = new DMChatChannel(playerName);
        dmChannels.add(channel);
        setChannel(channel);
    }

    private void renderPrompt() {
        setPrompt(FormatUtil.join(
            getChannel().getPromptText(),
            Component.literal(" >> ")
        ));
    }

    private void reload(FMAConfig config) {
        final var list = new ArrayList<SystemChatChannel>();
        list.add(GLOBAL);
        config.chatChannels.channels.forEach(ent -> list.add(ent.build()));
        systemChannels = list;
        setChannel(GLOBAL);
        builtinIndex = 0;
    }

    private ChatChannelManager(FMAConfig config) {
        reload(config);
    }

    public boolean isDm() {
        return currentChannel instanceof DMChatChannel;
    }

    public void cycleBuiltins() {
        final var index = systemChannels.indexOf(currentChannel);

        if (index != -1 && isDm()) {
            builtinIndex = index - 1;
        }

        builtinIndex = (builtinIndex + 1) % systemChannels.size();
        setChannel(systemChannels.get(builtinIndex));
    }

    public void cycleDm() {
        if (dmChannels.isEmpty())
            return;

        final var index = dmChannels.indexOf(currentChannel);

        if (index != -1) {
            dmIndex = index;
        }

        dmIndex = (dmIndex + 1) % dmChannels.size();
        setChannel(dmChannels.get(dmIndex));
    }

    public void toggleDmBuiltin() {
        if (isDm()) {
            setChannel(systemChannels.get(builtinIndex));
        } else if (!dmChannels.isEmpty()) {
            setChannel(dmChannels.get(dmIndex));
        }
    }

    public void setChannel(ChatChannel channel) {
        this.currentChannel = channel;
        renderPrompt();
    }

    public List<SystemChatChannel> getSystemChannels() {
        return Collections.unmodifiableList(systemChannels);
    }

    public @NotNull ChatChannel getChannel() {
        return currentChannel;
    }

    public Component promptText() {
        return cachePromptText;
    }

    public int promptTextWidth() {
        return cachePromptWidth;
    }

    @Override
    public void exportDebugInfo() {
        ChatUtil.send(Component.literal("ChatChannelManager").withStyle(ChatFormatting.UNDERLINE));
        ChatUtil.send(FormatUtil.join(Component.literal("cachePromptText = "), cachePromptText));
        ChatUtil.send("cachePromptWidth = " + cachePromptWidth);
        ChatUtil.send("currentChannel = " + currentChannel);
        ChatUtil.send("builtinIndex = " + builtinIndex);
        ChatUtil.send("dmIndex = " + dmIndex);
        ChatUtil.send("currentEnabledChannels = " + systemChannels);
        ChatUtil.send("dmChannels = " + dmChannels);
    }
}
