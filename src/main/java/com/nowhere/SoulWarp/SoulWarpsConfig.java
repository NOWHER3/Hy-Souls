package com.nowhere.SoulWarp;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class SoulWarpsConfig {
    @SerializedName("Warps")
    public Map<String, WarpModel> warps = new HashMap<>();
    @SerializedName("Warmup")
    public int warmup = 3;
    @SerializedName("Cooldown")
    public int cooldown = 5;
}