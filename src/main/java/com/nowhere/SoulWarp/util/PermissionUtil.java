package com.nowhere.SoulWarp.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import java.util.Collection;
import java.util.UUID;

public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        return PermissionsModule.get().hasPermission(uuid, permission);
    }

    public static boolean hasAnyPermission(UUID uuid, String... permissions) {
        PermissionsModule module = PermissionsModule.get();

        for(String perm : permissions) {
            if (module.hasPermission(uuid, perm)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAnyPermission(UUID uuid, Collection<String> permissions) {
        PermissionsModule module = PermissionsModule.get();

        for(String perm : permissions) {
            if (module.hasPermission(uuid, perm)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAllPermissions(UUID uuid, String... permissions) {
        PermissionsModule module = PermissionsModule.get();

        for(String perm : permissions) {
            if (!module.hasPermission(uuid, perm)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasAllPermissions(UUID uuid, Collection<String> permissions) {
        PermissionsModule module = PermissionsModule.get();

        for(String perm : permissions) {
            if (!module.hasPermission(uuid, perm)) {
                return false;
            }
        }

        return true;
    }
}
