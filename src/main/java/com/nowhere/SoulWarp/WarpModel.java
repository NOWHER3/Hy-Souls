package com.nowhere.SoulWarp;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class WarpModel {
    @SerializedName("Name")
    public String name;
    @SerializedName("WorldUuid")
    public UUID worldUuid;
    @SerializedName("X")
    public double x;
    @SerializedName("Y")
    public double y;
    @SerializedName("Z")
    public double z;
    @SerializedName("Yaw")
    public float yaw;
    @SerializedName("Pitch")
    public float pitch;

    // Block position fields for tracking placed blocks (like bonfires)
    @SerializedName("BlockX")
    public Integer blockX;
    @SerializedName("BlockY")
    public Integer blockY;
    @SerializedName("BlockZ")
    public Integer blockZ;

    public WarpModel() {
    }

    public WarpModel(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.worldUuid = worldUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public WarpModel(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch,
                     int blockX, int blockY, int blockZ) {
        this(name, worldUuid, x, y, z, yaw, pitch);
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public boolean hasBlockPosition() {
        return blockX != null && blockY != null && blockZ != null;
    }

    public boolean matchesBlockPosition(int x, int y, int z) {
        return hasBlockPosition() && blockX == x && blockY == y && blockZ == z;
    }
}