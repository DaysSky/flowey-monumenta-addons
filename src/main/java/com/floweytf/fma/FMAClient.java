package com.floweytf.fma;

import com.floweytf.fma.compat.WaypointHandler;
import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.features.Commands;
import com.floweytf.fma.features.Keybinds;
import com.floweytf.fma.features.LeaderboardUtils;
import com.floweytf.fma.features.SideBarManager;
import com.floweytf.fma.features.cz.CharmItemManager;
import com.floweytf.fma.features.gamestate.GameState;
import com.floweytf.fma.util.TickScheduler;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
    public final static GameState GAME_STATE = new GameState();
    public final static ModContainer MOD = FabricLoader.getInstance().getModContainer("fma").orElseThrow();
    public static SideBarManager SIDEBAR;
    public static ConfigHolder<FMAConfig> CONFIG;

    public static Player player() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static void withPlayer(Consumer<Player> consumer) {
        final var player = Minecraft.getInstance().player;
        if (player != null) {
            consumer.accept(player);
        }
    }

    public static ClientLevel level() {
        return (ClientLevel) player().level;
    }

    public static String playerName() {
        return player().getScoreboardName();
    }

    public static void reload() {
        final var config = CONFIG.get();
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
    
    @Override
    public void onInitializeClient() {
        CONFIG = FMAConfig.register();

        // stupid ass hack
        ClientLifecycleEvents.CLIENT_STARTED.register(this::initializeAfterMC);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> SIDEBAR.onTick(mc));
        Keybinds.init();
        Debug.init();
        Commands.init();
        CharmItemManager.init();
        WaypointHandler.loadImpl();

        new VersionChecker(CONFIG.get()).registerEvent();
    }

    private void initializeAfterMC(Minecraft minecraft) {
        reload();
    }
}
