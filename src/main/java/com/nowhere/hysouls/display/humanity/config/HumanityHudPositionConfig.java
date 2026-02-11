package com.nowhere.hysouls.display.humanity.config;

import com.google.gson.annotations.SerializedName;

public class HumanityHudPositionConfig {
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
