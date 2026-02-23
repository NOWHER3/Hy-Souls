package com.nowhere.hysouls.display.soul;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Ticking system that hides the soul/humanity HUD when the world map is open.
 * Uses reflection to read WorldMapTracker.clientHasWorldMapVisible since
 * the field has no public getter.
 */
public class MapHudVisibilitySystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Field MAP_VISIBLE_FIELD;

    static {
        Field field = null;
        try {
            field = WorldMapTracker.class.getDeclaredField("clientHasWorldMapVisible");
            field.setAccessible(true);
        } catch (Exception e) {
            // Logged at runtime if null
        }
        MAP_VISIBLE_FIELD = field;
    }

    private final Query<EntityStore> query = Player.getComponentType();
    private boolean loggedReflectionError = false;

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (MAP_VISIBLE_FIELD == null) {
            if (!loggedReflectionError) {
                LOGGER.at(Level.WARNING).log("MapHudVisibilitySystem: WorldMapTracker.clientHasWorldMapVisible field not found");
                loggedReflectionError = true;
            }
            return;
        }

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null || !playerRef.isValid()) return;

        try {
            boolean mapVisible = MAP_VISIBLE_FIELD.getBoolean(player.getWorldMapTracker());
            SoulHud hud = Main.get().getHudManager().getHud(playerRef.getUuid());
            if (hud != null) {
                hud.setMapOpen(mapVisible);
            }
        } catch (IllegalAccessException e) {
            if (!loggedReflectionError) {
                LOGGER.at(Level.WARNING).log("MapHudVisibilitySystem: Failed to read map visibility: " + e.getMessage());
                loggedReflectionError = true;
            }
        }
    }
}
