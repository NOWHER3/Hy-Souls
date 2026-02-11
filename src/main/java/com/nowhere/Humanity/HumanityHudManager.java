package com.nowhere.Humanity;

import com.nowhere.Main;
import com.nowhere.Humanity.config.HumanityHudConfigManager;
import com.nowhere.Humanity.config.HumanityHudPositionConfig;

import java.util.UUID;

public class HumanityHudManager {

    public void incrementHumanity(UUID playerId) {
        HumanityManager.addHumanity(playerId, 1);
        Main.get().getHudManager().updateHumanityDisplay(playerId);
    }

    public void applyConfig(UUID uuid) {
        Main.get().getHudManager().applyHumanityConfig(uuid);
    }

    public void toggleHud(UUID uuid) {
        Main.get().getHudManager().toggleHumanityHud(uuid);
    }

    public void shutdown() {
        // No-op: SoulHudManager owns the HUD lifecycle
    }
}
