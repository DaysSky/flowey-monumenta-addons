package com.floweytf.fma.compat;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class WaypointHandler {
    public record Waypoint(BlockPos pos, String name, String symbol, int color, boolean isTemp) {
    }

    @Nullable
    private static WaypointHandler INSTANCE = null;

    public static void loadImpl() {
        if (FabricLoader.getInstance().isModLoaded("xaerominimapfair")) {
            INSTANCE = new XaeroWaypointHandler();
        }
    }

    @Nullable
    public static WaypointHandler getInstance() {
        return INSTANCE;
    }

    public static void withInstance(Consumer<WaypointHandler> consumer) {
        if (INSTANCE != null) {
            consumer.accept(INSTANCE);
        }
    }

    public abstract void addWaypoint(Waypoint waypoint);

    public abstract Stream<Waypoint> getWaypoints();
}
