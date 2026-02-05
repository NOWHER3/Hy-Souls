package com.nowhere.SoulWarp;

import com.google.gson.annotations.SerializedName;

public class SoulWarpsConfig {
    @SerializedName("Warmup")
    public int warmup = 3;
    @SerializedName("Cooldown")
    public int cooldown = 5;
}