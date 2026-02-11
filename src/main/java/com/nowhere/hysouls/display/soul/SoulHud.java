package com.nowhere.hysouls.display.soul;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.nowhere.hysouls.display.soul.config.HudPositionConfig;
import com.nowhere.hysouls.display.humanity.config.HumanityHudPositionConfig;

public class SoulHud extends CustomUIHud {
    private int cachedCount = 0;
    private int lastSentCount = -1;
    private boolean visible = true;
    private HudPositionConfig positionConfig;

    private int cachedHumanityCount = 0;
    private int lastSentHumanityCount = -1;
    private boolean humanityVisible = true;
    private HumanityHudPositionConfig humanityPositionConfig;

    public SoulHud(PlayerRef playerRef) {
        super(playerRef);
    }

    protected void build(UICommandBuilder builder) {
        builder.append("Pages/com.nowhere_souldisplay.ui");
        builder.set("#SoulCount.Text", String.format("%d", this.cachedCount));
        if (this.positionConfig != null) {
            builder.setObject("#SoulHUD.Anchor", buildAnchor(this.positionConfig));
        }

        builder.append("Pages/com.nowhere_humanitydisplay.ui");
        builder.set("#HumanityCount.Text", String.format("%02d", this.cachedHumanityCount));
        if (this.humanityPositionConfig != null) {
            builder.setObject("#HumanityHUD.Anchor", buildHumanityAnchor(this.humanityPositionConfig));
        }
    }

    public void setPositionConfig(HudPositionConfig config) {
        this.positionConfig = config;
    }

    public void applyPosition(HudPositionConfig config) {
        this.positionConfig = config;
        UICommandBuilder builder = new UICommandBuilder();
        builder.setObject("#SoulHUD.Anchor", buildAnchor(config));
        this.update(false, builder);
    }

    private static Anchor buildAnchor(HudPositionConfig config) {
        Anchor anchor = new Anchor();
        anchor.setBottom(Value.of(config.bottom));
        anchor.setWidth(Value.of(config.width));
        anchor.setHeight(Value.of(config.height));
        if ("left".equals(config.side)) {
            anchor.setLeft(Value.of(config.offset));
        } else {
            anchor.setRight(Value.of(config.offset));
        }
        return anchor;
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

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#SoulHUD.Visible", visible);
        this.update(false, builder);
    }

    // Humanity display methods

    public void setHumanityPositionConfig(HumanityHudPositionConfig config) {
        this.humanityPositionConfig = config;
    }

    public void updateHumanityCount(int count) {
        this.cachedHumanityCount = count;
        if (this.lastSentHumanityCount != count) {
            this.lastSentHumanityCount = count;
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#HumanityCount.Text", String.format("%02d", count));
            this.update(false, builder);
        }
    }

    public boolean isHumanityVisible() {
        return this.humanityVisible;
    }

    public void setHumanityVisible(boolean visible) {
        if (this.humanityVisible == visible) return;
        this.humanityVisible = visible;
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#HumanityHUD.Visible", visible);
        this.update(false, builder);
    }

    public void applyHumanityPosition(HumanityHudPositionConfig config) {
        this.humanityPositionConfig = config;
        UICommandBuilder builder = new UICommandBuilder();
        builder.setObject("#HumanityHUD.Anchor", buildHumanityAnchor(config));
        this.update(false, builder);
    }

    private static Anchor buildHumanityAnchor(HumanityHudPositionConfig config) {
        Anchor anchor = new Anchor();
        anchor.setBottom(Value.of(config.bottom));
        anchor.setWidth(Value.of(config.width));
        anchor.setHeight(Value.of(config.height));
        if ("left".equals(config.side)) {
            anchor.setLeft(Value.of(config.offset));
        } else {
            anchor.setRight(Value.of(config.offset));
        }
        return anchor;
    }
}
