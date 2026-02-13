package com.nowhere.hysouls.config;

import com.google.gson.annotations.SerializedName;
import com.nowhere.hysouls.warp.WarpModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-player gameplay data (persistent game state).
 * Stored in data/ folder.
 */
public class PlayerData {

    @SerializedName("Souls")
    public int souls = 0;

    @SerializedName("Humanity")
    public int humanity = 0;

    @SerializedName("IsHollow")
    public boolean isHollow = false;

    @SerializedName("Kindling")
    public Map<String, Integer> kindling = new HashMap<>();

    @SerializedName("Warps")
    public Map<String, WarpModel> warps = new HashMap<>();
}
