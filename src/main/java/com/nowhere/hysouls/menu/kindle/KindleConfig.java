package com.nowhere.hysouls.menu.kindle;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-player configuration for bonfire kindling levels.
 * Maps bonfire locations to their kindle level (0-3).
 */
public class KindleConfig {
    /**
     * Map of bonfire location keys to kindle levels.
     * Key format: "x_y_z" (bonfire block coordinates)
     * Value: 0 (5 estus), 1 (10 estus), 2 (15 estus), 3 (20 estus)
     */
    @SerializedName("Kindling")
    public Map<String, Integer> kindling = new HashMap<>();
}
