package com.floweytf.fma.chat;

import com.floweytf.fma.DebugInfoExporter;
import com.floweytf.fma.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ChatChannelManager implements DebugInfoExporter {
    private static ChatChannelManager INSTANCE;

    public static ChatChannelManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatChannelManager();
        }

        return INSTANCE;
    }

    public static final SystemChatChannel GLOBAL = new SystemChatChannel(
        "g",
        ChatFormatting.WHITE,
        Component.literal("g").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("lobal")
    );

    public static final SystemChatChannel WORLD_CHAT = new SystemChatChannel(
        "wc",
        ChatFormatting.BLUE,
        Component.literal("w").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("orld-"),
        Component.literal("c").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("hat")
    );

    public static final SystemChatChannel LOCAL = new SystemChatChannel(
        "l",
        ChatFormatting.YELLOW,
        Component.literal("l").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("ocal")
    );

    public static final SystemChatChannel GUILD = new SystemChatChannel(
        "gc",
        style -> style.withColor(0xbe74fb),
        Component.literal("MAID")
    );

    public static final SystemChatChannel LFG = new SystemChatChannel(
        "lfg",
        ChatFormatting.GOLD,
        Component.literal("l").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("ooking-"),
        Component.literal("f").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("or-"),
        Component.literal("g").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("roup")
    );

    public static final SystemChatChannel TR = new SystemChatChannel(
        "tr",
        ChatFormatting.DARK_GREEN,
        Component.literal("tr").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        Component.literal("ading")
    );

    public static final List<SystemChatChannel> BUILTIN_CHANNELS = List.of(GLOBAL, LOCAL, WORLD_CHAT, GUILD, LFG, TR);

    // cache info
    private Component cachePromptText;
    private int cachePromptWidth;

    // channel info
    @NotNull
    private ChatChannel currentChannel;
    private int builtinIndex = 0;
    private int dmIndex = 0;
    private final BitSet currentEnabledChannels = new BitSet(BUILTIN_CHANNELS.size());
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

    private void renderComponents() {
        var current = Component.empty().append("[");

        for (int i = 0; i < BUILTIN_CHANNELS.size(); i++) {
            if (currentEnabledChannels.get(i)) {
                current = current.append(BUILTIN_CHANNELS.get(i).shorthand());
            } else {
                current = current.append(" ".repeat(BUILTIN_CHANNELS.get(i).command().length()));
            }
        }

        setPrompt(current
            .append("] ")
            .append(getChannel().getPromptText())
            .append(" >> "));
    }

    private ChatChannelManager() {
        for (int i = 0; i < BUILTIN_CHANNELS.size(); i++) {
            currentEnabledChannels.set(i);
        }
        setChannel(GLOBAL);
    }

    public boolean isDm() {
        return currentChannel instanceof DMChatChannel;
    }

    public void cycleBuiltins() {
        final var index = BUILTIN_CHANNELS.indexOf(currentChannel);

        if (index != -1 && isDm()) {
            builtinIndex = index - 1;
        }

        builtinIndex = (builtinIndex + 1) % BUILTIN_CHANNELS.size();
        setChannel(BUILTIN_CHANNELS.get(builtinIndex));
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
            setChannel(BUILTIN_CHANNELS.get(builtinIndex));
        } else if (!dmChannels.isEmpty()) {
            setChannel(dmChannels.get(dmIndex));
        }
    }

    public void setChannel(ChatChannel channel) {
        this.currentChannel = channel;
        renderComponents();
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
        Utils.send(Component.literal("ChatChannelManager").withStyle(ChatFormatting.UNDERLINE));
        Utils.send(Utils.join(
            Component.literal("cachePromptText = "),
            cachePromptText
        ));
        Utils.send("cachePromptWidth = " + cachePromptWidth);
        Utils.send("currentChannel = " + currentChannel);
        Utils.send("builtinIndex = " + builtinIndex);
        Utils.send("dmIndex = " + dmIndex);
        Utils.send("currentEnabledChannels = " + currentEnabledChannels);
        Utils.send("dmChannels = " + dmChannels);
    }
}
