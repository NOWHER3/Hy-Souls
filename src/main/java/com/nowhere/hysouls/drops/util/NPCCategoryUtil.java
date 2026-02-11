package com.nowhere.hysouls.drops.util;

import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.nowhere.hysouls.drops.SoulDropConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NPCCategoryUtil {
    private static final Map<Integer, String> categoryCache = new ConcurrentHashMap<>();
    private static TagSetPlugin.TagSetLookup tagSetLookup;

    public static void initialize() {
        try {
            tagSetLookup = TagSetPlugin.get(NPCGroup.class);
            System.out.println("[SoulDrops] NPCCategoryUtil initialized");
        } catch (Exception e) {
            System.err.println("[SoulDrops] Failed to initialize TagSetPlugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Resolves the drop category for an NPC by checking its roleIndex against
     * the NPCGroup tags declared in each config category entry.
     *
     * Categories are checked in config-defined order (first match wins).
     * If a category has a "tags" array, those NPCGroup names are checked.
     * Otherwise the category key itself is used as the NPCGroup tag name.
     */
    public static String getCategory(int roleIndex, Map<String, SoulDropConfig.CategoryDrops> categories) {
        String cached = categoryCache.get(roleIndex);
        if (cached != null) {
            return cached;
        }

        String category = resolveCategory(roleIndex, categories);
        categoryCache.put(roleIndex, category);
        return category;
    }

    private static String resolveCategory(int roleIndex, Map<String, SoulDropConfig.CategoryDrops> categories) {
        if (tagSetLookup == null || categories == null) {
            return "default";
        }

        for (Map.Entry<String, SoulDropConfig.CategoryDrops> entry : categories.entrySet()) {
            String categoryName = entry.getKey();
            if ("default".equals(categoryName)) {
                continue;
            }

            SoulDropConfig.CategoryDrops categoryDrops = entry.getValue();
            String[] tags = categoryDrops.getTags();

            if (tags != null && tags.length > 0) {
                for (String tag : tags) {
                    if (roleInGroup(tag, roleIndex)) {
                        System.out.println("[SoulDrops] RoleIndex " + roleIndex +
                                " matched tag '" + tag + "' -> category: " + categoryName);
                        return categoryName;
                    }
                }
            } else {
                if (roleInGroup(categoryName, roleIndex)) {
                    System.out.println("[SoulDrops] RoleIndex " + roleIndex +
                            " matched tag '" + categoryName + "' -> category: " + categoryName);
                    return categoryName;
                }
            }
        }

        System.out.println("[SoulDrops] No category match for roleIndex " + roleIndex + ", using default");
        return "default";
    }

    private static boolean roleInGroup(String groupName, int roleIndex) {
        int groupIndex = NPCGroup.getAssetMap().getIndex(groupName);
        if (groupIndex == Integer.MIN_VALUE) {
            return false;
        }
        try {
            return tagSetLookup.tagInSet(groupIndex, roleIndex);
        } catch (Exception e) {
            return false;
        }
    }

    public static void clearCache() {
        categoryCache.clear();
    }
}
