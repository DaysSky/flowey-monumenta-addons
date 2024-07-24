package com.floweytf.fma;

import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.features.*;
import com.floweytf.fma.features.chat.ChatChannelManager;
import com.floweytf.fma.features.cz.CharmItemManager;
import com.floweytf.fma.features.gamestate.GameState;
import com.floweytf.fma.util.TickScheduler;
import com.mojang.blaze3d.font.GlyphInfo;
import java.util.List;
import java.util.Objects;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FMAClient implements ClientModInitializer {
    public static final List<String> SPLASH = List.of(
        "meow :3",
        "better than MME",
        "portal best strike",
        "portal worst strike",
        "/lfg portal 1/4 16ar r9 5pts",
        "diec = tafu confirmed 2024",
        "monumenta sucks, play gtnh instead"
    );

    public static final Logger LOGGER = LogManager.getLogger();
    public static final TickScheduler SCHEDULER = new TickScheduler();
    public static final LeaderboardUtils LEADERBOARD = new LeaderboardUtils();
    public static final GUIManager GUI = new GUIManager();
    public final static GameState GAME_STATE = new GameState();
    public static ChatChannelManager CHAT_CHANNELS;
    public static Commands COMMANDS;
    public static SideBarManager SIDEBAR;
    public final static ModContainer MOD = FabricLoader.getInstance().getModContainer("fma").orElseThrow();

    public static ConfigHolder<FMAConfig> CONFIG;

    public static Player player() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static ClientLevel level() {
        return (ClientLevel) player().level;
    }

    public static String playerName() {
        return player().getScoreboardName();
    }

    @Override
    public void onInitializeClient() {
        CONFIG = FMAConfig.register();

        HudRenderCallback.EVENT.register(GUI::render);

        // stupid ass hack
        ClientLifecycleEvents.CLIENT_STARTED.register(this::initializeAfterMC);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> SIDEBAR.onTick(mc));
        Keybinds.init();
        Debug.init();
        CharmItemManager.init();
        try {
            Class.forName("com.floweytf.fma.features.cz.data.CharmEffectType");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeAfterMC(Minecraft minecraft) {
        reload();
    }

    public static void reload() {
        final var config = CONFIG.get();
        CHAT_CHANNELS = new ChatChannelManager(config);
        COMMANDS = new Commands(config);
        SIDEBAR = new SideBarManager(config);
    }

    public static FMAConfig config() {
        return CONFIG.get();
    }

    public static FMAConfig.Appearance appearance() {
        return CONFIG.get().appearance;
    }

    public static FMAConfig.FeatureToggles features() {
        return CONFIG.get().features;
    }
}
