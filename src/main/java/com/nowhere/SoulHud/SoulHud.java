package com.nowhere.SoulHud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class SoulHud extends CustomUIHud {
    private int cachedCount = 0;
    private int lastSentCount = -1;

    public SoulHud(PlayerRef playerRef) {
        super(playerRef);
    }

    protected void build(UICommandBuilder builder) {
        builder.append("Pages/com.nowhere_souldisplay.ui");
        builder.set("#SoulCount.Text", String.format("%d", this.cachedCount));
    }

    public void updateSoulCount(int arrowCount) {
        this.cachedCount = arrowCount;
        if (this.lastSentCount != arrowCount) {
            this.lastSentCount = arrowCount;
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#SoulCount.Text", String.format("%d", arrowCount));
            this.update(false, builder);
        }
    }
}