---
title: Chest Loot Config
parent: Server Guide
nav_order: 3
---

# Chest Loot Configuration

Control what soul items appear in world-generated chests and how often.

**File location:** `mods/Hysouls/server/chest_loot_config.json`

---

## Structure

```json
{
  "enabled": true,
  "tiers": {
    "tierName": {
      "baseChance": 1.0,
      "tags": ["keyword1", "keyword2"],
      "itemWeights": {
        "ItemId": 10,
        "OtherItemId": 5
      }
    }
  },
  "chestBlockTypeMappings": {
    "BlockTypeId": "tierName"
  }
}
```

### Fields

| Field | Description |
|---|---|
| `enabled` | Set to `false` to disable all chest loot generation |
| `tiers` | Map of loot tiers with their rules |
| `baseChance` | Probability that a chest gets any loot at all (`0.0` to `1.0`) |
| `tags` | Keywords matched against the chest's block type name (case-insensitive) |
| `itemWeights` | Weighted random selection -- higher weight = more likely to be chosen |
| `chestBlockTypeMappings` | Direct block type to tier mapping (fallback if no tag matches) |

---

## How Tier Matching Works

When the system encounters a chest, it checks the chest's block type name against each tier's `tags` list (case-insensitive substring match). The first tier with a matching tag is used.

If no tag matches, the system falls back to `chestBlockTypeMappings` for an exact block type ID match.

---

## Default Tiers

### Common

**Tags:** village, kweebec, outpost, camp, cabin, house, small, chest

Chests found in villages, camps, and general world structures. Contains lower-value soul items.

### Rare

**Tags:** temple, emerald, ruins, fortress, tower, shrine, crypt, rare, uncommon, medium

Chests in temples, ruins, and fortified structures. Contains mid-value soul items.

### Epic

**Tags:** epic, boss, dungeon, vault, dragon, elite

Chests in boss rooms, dungeons, and vaults. Contains high-value soul items.

### Legendary

**Tags:** legendary, legend, treasure

The rarest chests. Contains the most valuable soul items.

---

## Weighted Item Selection

Within each tier, items are selected randomly based on their weights. Higher weight = more likely.

**Example:** If a tier has:
```json
"itemWeights": {
  "Ingredient_Hysouls_Soul_Essence_Hard": 10,
  "Ingredient_Hysouls_Soul_Essence_Hard_2": 5,
  "Ingredient_Hysouls_Soul_Essence_Hard_3": 1
}
```

- Soul of a Skeleton Undead: 10/16 chance (62.5%)
- Large Soul of a Skeleton Undead: 5/16 chance (31.25%)
- Soul of a nameless Goblin: 1/16 chance (6.25%)

---

## Customization Examples

### Lower the chance of chests having loot

```json
"common": {
  "baseChance": 0.5,
  "tags": ["village", "camp", "chest"],
  "itemWeights": { ... }
}
```

This makes only 50% of common chests contain soul items.

### Add a custom tier for a specific block type

```json
"chestBlockTypeMappings": {
  "Chest_MyCustomChest": "epic"
}
```

### Disable chest loot entirely

```json
{
  "enabled": false
}
```

---

## Important Notes

- Only **world-generated chests** are populated. Player-placed chests are never given loot.
- Each chest is only populated once. Reopening or reloading the chunk won't add more items.
- Changes to this config take effect on server restart and only apply to newly discovered chests.
