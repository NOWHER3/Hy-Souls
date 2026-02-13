package com.nowhere.hysouls.config;

import com.google.gson.annotations.SerializedName;

/**
 * Per-player user preferences and settings.
 * Stored in user/ folder.
 */
public class UserPreferences {

    @SerializedName("EstusSlot")
    public int estusSlot = 1;

    @SerializedName("SoulHud")
    public SoulHudConfig soulHud = new SoulHudConfig();

    @SerializedName("HumanityHud")
    public HumanityHudConfig humanityHud = new HumanityHudConfig();

    public static class SoulHudConfig {
        @SerializedName("Side")
        public String side = "right";

        @SerializedName("Offset")
        public int offset = 450;

        @SerializedName("Bottom")
        public int bottom = 38;

        @SerializedName("Width")
        public int width = 150;

        @SerializedName("Height")
        public int height = 32;
    }

    public static class HumanityHudConfig {
        @SerializedName("Side")
        public String side = "left";

        @SerializedName("Offset")
        public int offset = 425;

        @SerializedName("Bottom")
        public int bottom = 38;

        @SerializedName("Width")
        public int width = 75;

        @SerializedName("Height")
        public int height = 75;
    }
}
