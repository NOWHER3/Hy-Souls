package com.nowhere.hysouls.warp.marker;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.util.PositionUtil;
import com.nowhere.hysouls.warp.UserWarpsConfig;
import com.nowhere.hysouls.warp.WarpConfigManager;
import com.nowhere.hysouls.warp.WarpModel;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/**
 * Provides bonfire map markers for the world map.
 * Iterates each player's bonfire warps (warps with block positions) and adds markers
 * via the MarkersCollector. When a bonfire is broken and its warp deleted, the marker
 * automatically disappears on the next update cycle.
 */
public class BonfireMarkerProvider implements WorldMapManager.MarkerProvider {

    @SuppressWarnings("removal")
    @Override
    public void update(@Nonnull World world,
                       @Nonnull Player player,
                       @Nonnull MarkersCollector collector) {
        UUID playerId = player.getUuid();

        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(playerId);
        if (userConfig == null || userConfig.warps == null || userConfig.warps.isEmpty()) {
            return;
        }

        UUID currentWorldUuid = world.getWorldConfig().getUuid();

        for (Map.Entry<String, WarpModel> entry : userConfig.warps.entrySet()) {
            String warpName = entry.getKey();
            WarpModel warp = entry.getValue();

            if (!warp.hasBlockPosition()) {
                continue;
            }

            // Only show markers for bonfires in the current world
            if (!warp.worldUuid.equals(currentWorldUuid)) {
                continue;
            }

            Vector3d position = new Vector3d(warp.blockX + 0.5, warp.blockY, warp.blockZ + 0.5);

            if (!collector.isInViewDistance(position)) {
                continue;
            }

            FormattedMessage displayName = new FormattedMessage();
            displayName.rawText = "Bonfire";

            MapMarker marker = new MapMarker(
                    "bonfire_" + warpName,
                    displayName,
                    null,
                    "Warp.png",
                    PositionUtil.toTransformPacket(new Transform(position)),
                    null,
                    null
            );

            collector.add(marker);
        }
    }
}
