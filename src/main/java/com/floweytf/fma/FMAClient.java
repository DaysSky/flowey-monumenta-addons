package com.floweytf.fma;

import com.floweytf.fma.chat.ChatChannelManager;
import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.features.*;
import com.floweytf.fma.gamestate.GameState;
import com.floweytf.fma.util.TickScheduler;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FMAClient implements ClientModInitializer {
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
