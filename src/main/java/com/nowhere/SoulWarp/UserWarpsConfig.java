package com.nowhere.SoulWarp;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class UserWarpsConfig {
    @SerializedName("Warps")
    public Map<String, WarpModel> warps = new HashMap<>();
}






