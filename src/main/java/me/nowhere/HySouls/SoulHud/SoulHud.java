package me.nowhere.HySouls.SoulHud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class SoulHud extends CustomUIHud {
    public SoulHud(PlayerRef playerRef) {
        super(playerRef);
    }

    protected void build(UICommandBuilder builder) {
        builder.append("Pages/com.nowhere_souldisplay.ui");
    }

    public void updateSoulCount(int soulCount) {
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#SoulCount.Text", String.format("%d", soulCount));
        this.update(false, builder);
    }
}
