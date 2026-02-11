package com.nowhere.hysouls.display.soul.config;

import com.google.gson.annotations.SerializedName;

public class HudPositionConfig {
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
