package com.floweytf.fma.compat;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import xaero.common.XaeroMinimapSession;

@SuppressWarnings("deprecation")
public class XaeroWaypointHandler extends WaypointHandler {
    @Override
    public void addWaypoint(Waypoint waypoint) {
        final var session = XaeroMinimapSession.getCurrentSession().getWaypointsManager();

        session.getCurrentWorld()
            .getCurrentSet()
            .getList()
            .add(new xaero.common.minimap.waypoints.Waypoint(waypoint.pos().getX(), waypoint.pos().getY(),
                waypoint.pos().getZ(), waypoint.name(), waypoint.symbol(), waypoint.color(), 0, waypoint.isTemp()));
    }

    @Override
    public Stream<Waypoint> getWaypoints() {
        final var session = XaeroMinimapSession.getCurrentSession().getWaypointsManager();

        return session.getCurrentWorld()
            .getCurrentSet()
            .getList()
            .stream()
            .map(ent -> new Waypoint(
                new BlockPos(ent.getX(), ent.getY(), ent.getZ()),
                ent.getName(), ent.getSymbol(), ent.getColor(), ent.isTemporary()
            ));
    }
}