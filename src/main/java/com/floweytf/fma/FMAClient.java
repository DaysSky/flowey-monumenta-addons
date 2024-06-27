package com.floweytf.fma;

import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.features.Commands;
import com.floweytf.fma.features.GUIManager;
import com.floweytf.fma.features.Keybinds;
import com.floweytf.fma.features.LeaderboardUtils;
import com.floweytf.fma.gamestate.GameState;
import com.floweytf.fma.util.TickScheduler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
    public static ConfigHolder<FMAConfig> CONFIG;

    public static Player player() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static ClientLevel level() {
        return (ClientLevel) player().level();
    }

    public static String playerName() {
        return player().getScoreboardName();
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(FMAConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(FMAConfig.class);
        HudRenderCallback.EVENT.register(GUI::render);

        Commands.init();
        Keybinds.init();
        Debug.init();
    }
}
