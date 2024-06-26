package com.floweytf.fma;

import ch.njol.minecraft.config.fma.Config;
import com.floweytf.fma.chat.ChatChannelManager;
import com.floweytf.fma.gamestate.GameState;
import com.floweytf.fma.registry.Commands;
import com.floweytf.fma.registry.Keybinds;
import com.floweytf.fma.util.TickScheduler;
import com.floweytf.fma.util.Utils;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class FMAClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final TickScheduler SCHEDULER = new TickScheduler();
    public static final LeaderboardUtils LEADERBOARD = new LeaderboardUtils();
    public static final GUIManager GUI = new GUIManager();
    public final static GameState GAME_STATE = new GameState();
    public static FMAConfig CONFIG = new FMAConfig();
    public static final String OPTIONS_FILE_NAME = "fma.json";
    public static boolean entityDebug = false;
    public static boolean blockDebug = false;

    public static final List<DebugInfoExporter> DEBUG = List.of(
        SCHEDULER,
        LEADERBOARD,
        GAME_STATE
    );

    private void loadConfig() {
        try {
            CONFIG = Config.readJsonFile(FMAConfig.class, OPTIONS_FILE_NAME);
        } catch (FileNotFoundException e) {
            try {
                Config.writeJsonFile(CONFIG, OPTIONS_FILE_NAME);
            } catch (IOException ignored) {
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.error(e);
        }

        if (CONFIG == null) {
            CONFIG = new FMAConfig();
        }
    }

    public static void saveConfig() {
        Minecraft.getInstance().execute(() -> {
            try {
                Config.writeJsonFile(CONFIG, OPTIONS_FILE_NAME);
            } catch (IOException ex) {
                // ignore
            }
        });
    }

    @Override
    public void onInitializeClient() {
        loadConfig();
        Commands.load();
        Keybinds.init();

        HudRenderCallback.EVENT.register(GUI::render);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entityDebug && !player.isSpectator()) {
                dumpEntityInfo(entity);
            }

            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (blockDebug && !player.isSpectator()) {
                Utils.send(Component.literal("Block Info Dump").withStyle(ChatFormatting.BOLD));
                Utils.send("x = %d, y = %d, z = %d" .formatted(pos.getX(), pos.getY(), pos.getZ()));
                Utils.send("direction = " + direction);
                Utils.send("blockState = " + world.getBlockState(pos));
            }

            return InteractionResult.PASS;
        });
    }

    public static void dumpEntityInfo(Entity entity) {
        Utils.send(Component.literal("Entity Info Dump").withStyle(ChatFormatting.BOLD));
        Utils.send(Component.literal("getName = "), entity.getName());
        Utils.send("type = " + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
        if (entity.getCustomName() != null) {
            Utils.send(Component.literal("getCustomName = "), entity.getCustomName());
        }
        Utils.send("slots = {");
        for (final var armorSlot : entity.getArmorSlots()) {
            Utils.send("  " + armorSlot.toString() + armorSlot.getTag());
        }
        Utils.send("}");
        Utils.send("x = %f, y = %f, z = %f" .formatted(entity.getX(), entity.getY(), entity.getZ()));
        Utils.send("tags = " + entity.getTags());
    }

    public static void runDebug() {
        Utils.send(Component.literal("Debug State Dump").withStyle(ChatFormatting.BOLD));
        ChatChannelManager.getInstance().exportDebugInfo();
        for (var exporter : DEBUG) {
            exporter.exportDebugInfo();
        }
    }
}
