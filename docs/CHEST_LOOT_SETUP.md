# Chest Loot System Setup Guide

**Status: ✅ Complete and Working** (v0.0.7+)

## Overview

The Chest Loot System automatically populates dungeon and prefab chests with soul items when chunks are loaded. This system uses runtime detection and population, requiring **no prefab modifications**.

## Key Features

- ✅ **Zero Prefab Modifications**: Works with all existing dungeons and structures automatically
- ✅ **Configurable Loot Tiers**: Define different loot quality levels (common, uncommon, rare, epic, legendary)
- ✅ **Weighted Random Selection**: Control drop rates using weight-based probability
- ✅ **Chest Type Mapping**: Different chest blocks can have different loot tiers
- ✅ **One-Time Population**: Chests are populated once when chunks first load, preserving player modifications

## How It Works

1. **Chunk Loading**: When a chunk is loaded by the server, the ChestLoot system scans it
2. **Chest Detection**: The system finds all ItemContainerState blocks (chests) in the chunk
3. **Tier Determination**: Each chest's block type is mapped to a loot tier via configuration
4. **Chance Roll**: The system rolls against the tier's base chance (e.g., 15% for common, 100% for legendary)
5. **Item Selection**: If successful, a soul item is selected using weighted random selection
6. **Population**: The selected soul item is added to the chest's inventory
7. **Tracking**: The chunk is marked as processed to prevent repopulation

## Configuration

### Config File Location

```
mods/Hysouls/server/chest_loot_config.json
```

The config file is automatically created with default values on first run.

### Config Structure

```json
{
  "enabled": true,
  "tiers": {
    "common": {
      "baseChance": 0.15,
      "itemWeights": {
        "Ingredient_Hysouls_Soul_Essence_Hard_2": 50.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_3": 30.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_4": 15.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_5": 5.0
      }
    },
    "rare": {
      "baseChance": 0.50,
      "itemWeights": {
        "Ingredient_Hysouls_Soul_Essence_Hard_5": 20.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_6": 30.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_7": 30.0,
        "Ingredient_Hysouls_Soul_Essence_Hard_8": 20.0
      }
    }
  },
  "chestBlockTypeMappings": {
    "Chest": "common",
    "Chest_Rare": "rare",
    "Chest_Epic": "epic",
    "Chest_Legendary": "legendary"
  }
}
```

### Configuration Options

#### Global Settings

- **`enabled`** (boolean): Master toggle for the entire system
  - `true`: System actively populates chests
  - `false`: System is disabled (no chest population)

#### Tier Configuration

Each tier has two settings:

- **`baseChance`** (0.0 - 1.0): Probability that a chest of this tier receives soul loot
  - `0.15` = 15% chance
  - `0.50` = 50% chance
  - `1.0` = 100% chance (always drops)

- **`itemWeights`** (map of item ID → weight): Weighted random selection of which soul item drops
  - Higher weight = more likely to be selected
  - Weights are relative (50.0 is twice as likely as 25.0)
  - Total weight is calculated automatically

#### Chest Block Type Mappings

Maps Hytale chest block IDs to loot tiers:

```json
"chestBlockTypeMappings": {
  "Chest": "common",
  "Chest_Rare": "rare"
}
```

- **Left side**: Hytale block type ID (from prefabs/structures)
- **Right side**: Tier name (must match a key in `tiers`)
- Unmapped chest types default to "common" tier

### Default Tiers

The system comes with 5 pre-configured tiers:

| Tier | Base Chance | Soul Items | Description |
|------|-------------|------------|-------------|
| **Common** | 15% | Hard 2-5 (400-1600 souls) | Low-value souls, weighted toward tier 2 |
| **Uncommon** | 30% | Hard 3-6 (800-3200 souls) | Mid-low value souls, balanced distribution |
| **Rare** | 50% | Hard 5-8 (1600-12800 souls) | Mid-high value souls, balanced distribution |
| **Epic** | 75% | Hard 7-10 (6400-51200 souls) | High-value souls, weighted toward tier 9 |
| **Legendary** | 100% | Hard 8-10 (12800-51200 souls) | Guaranteed max-tier souls |

## For Server Admins

### Customizing Loot Tables

**To adjust drop rates:**
1. Open `chest_loot_config.json`
2. Modify `baseChance` values for tiers (0.0 = never, 1.0 = always)
3. Save and restart the server

**To change which souls drop:**
1. Edit the `itemWeights` for a tier
2. Add/remove soul item IDs
3. Adjust weights to control probability
4. Save and restart the server

**To create new tiers:**
1. Add a new tier to the `tiers` object
2. Set `baseChance` and `itemWeights`
3. Map chest block types to the new tier in `chestBlockTypeMappings`
4. Save and restart the server

**To map custom chest types:**
1. Identify the chest block type ID (check prefab files or logs)
2. Add mapping in `chestBlockTypeMappings`
3. Save and restart the server

### Disabling the System

Set `"enabled": false` in the config and restart.

### Performance Considerations

- The system checks for new chunks every 100 ticks (~5 seconds)
- Each chunk is only processed once (tracked in memory)
- Minimal performance impact after initial chunk scanning
- No persistent storage needed (tracking is per-server-session)

## For Prefab Creators

### Using the System in Prefabs

**Good news**: You don't need to do anything special! Just place chests in your prefabs as normal.

The system automatically:
- Detects all chests when the prefab generates
- Determines appropriate loot tier based on chest block type
- Populates them according to configuration

### Best Practices

1. **Use Different Chest Types**: If available, use different chest block types (Chest, Chest_Rare, etc.) to create progression
2. **Place Strategically**: Put higher-tier chests in harder-to-reach or more dangerous locations
3. **Don't Pre-Fill**: The system adds soul items to empty slots - you can pre-fill chests with other loot if needed

### Chest Type Recommendations

- **Early Dungeons**: Use `Chest` (common tier, 15% chance)
- **Mid-Game Dungeons**: Use `Chest_Rare` (rare tier, 50% chance)
- **End-Game Dungeons**: Use `Chest_Epic` or `Chest_Legendary` (75-100% chance)
- **Hidden Rooms/Secrets**: Use higher-tier chests as rewards for exploration

## Technical Details

### System Architecture

**Component**: `com.nowhere.hysouls.loot.ChestLootSystem`
- Extends `EntityTickingSystem<EntityStore>`
- Ticks every 100 game ticks (~5 seconds)
- Queries for `BlockChunk` components to find loaded chunks

**Chest Detection**:
- Uses `BlockStateModule.get().getComponentType(ItemContainerState.class)` to query chests
- Filters by chunk reference to process only chests in the current chunk
- Validates block type contains "chest" (case-insensitive)

**Weighted Random Selection**:
- Calculates total weight from all items in tier
- Rolls random value in range [0, totalWeight)
- Selects item where cumulative weight exceeds roll
- Same algorithm used by `SoulDropsSystem` for consistency

**Chunk Tracking**:
- Processed chunks stored in `ConcurrentHashMap.newKeySet()`
- Chunk ID generated from coordinates: `ChunkUtil.coordToLong(x, z)`
- Tracking is in-memory only (resets on server restart)
- **Design**: Old chunks don't repopulate on restart (chests save their contents)

### Item Population

When a chest passes the chance roll:
1. Weighted item is selected from tier's `itemWeights`
2. `ItemStack` created with quantity 1
3. Item added to chest's `ItemContainer` via `addItemStack()`
4. Chest state marked for save automatically

### Error Handling

- Invalid item IDs: Logged as error, chest skipped
- Missing tier config: Logged as error, chest skipped
- Invalid chest reference: Silently skipped
- Container add failure: Logged as error with exception

### Compatibility

- **Hytale Server**: Requires server with ECS support and BlockStateModule
- **Other Plugins**: Compatible with any plugins that don't modify chest population
- **Vanilla Chests**: Works with all standard Hytale chest types
- **Custom Chests**: Works with modded chest blocks if they extend `ItemContainerState`

### Limitations

1. **No Retroactive Population**: Chests in already-generated chunks won't be populated
   - **Reason**: Chunks save their state; repopulating would override player changes
   - **Workaround**: Delete world region files to force chunk regeneration (not recommended)

2. **One-Time Population**: Each chest is populated exactly once
   - **Reason**: Prevents exploits (break chunk, reload, get new loot)
   - **Design**: Players can empty chests and they stay empty

3. **Per-Session Tracking**: Chunk tracking resets on server restart
   - **Reason**: Simplifies implementation, avoids disk I/O
   - **Impact**: None (chunks persist their contents)

4. **Block Type Mapping**: Requires knowing chest block IDs to customize
   - **Workaround**: Check logs for block IDs when chests are processed

## Troubleshooting

### Chests Aren't Being Populated

**Check 1**: Is the system enabled?
- Verify `"enabled": true` in `chest_loot_config.json`

**Check 2**: Are you in a new area?
- The system only populates chests in newly-loaded chunks
- Already-generated chunks won't be retroactively populated

**Check 3**: Did you pass the chance roll?
- Common chests only have 15% chance to get loot
- Try checking multiple chests or using a higher-tier chest

**Check 4**: Check server logs
- Look for `[ChestLoot]` messages
- Errors will indicate configuration problems

### All Chests Have The Same Loot

**Cause**: All chests are using the same block type
- Check `chestBlockTypeMappings` to see which tier is used
- Prefabs may only use the standard "Chest" block

**Solution**:
- Either map "Chest" to different tiers based on location (requires code changes)
- Or use prefabs with different chest block types

### Weights Aren't Working as Expected

**Remember**: Weights are **relative**, not percentages
- Weights of [50, 30, 20] = 50%, 30%, 20% distribution
- Weights of [100, 50, 50] = 50%, 25%, 25% distribution

**To get 80% chance for one item:**
- Use weights like `{"item1": 80.0, "item2": 20.0}`

### Config Changes Not Taking Effect

**Solution**: Restart the server
- Config is loaded once at startup
- Use `/restart` or stop/start the server
- (Future: Could add `/hysouls reload` command)

## Examples

### Example 1: High-Value Dungeon

For an end-game dungeon, increase rare chest chances:

```json
"rare": {
  "baseChance": 0.80,
  "itemWeights": {
    "Ingredient_Hysouls_Soul_Essence_Hard_7": 10.0,
    "Ingredient_Hysouls_Soul_Essence_Hard_8": 30.0,
    "Ingredient_Hysouls_Soul_Essence_Hard_9": 40.0,
    "Ingredient_Hysouls_Soul_Essence_Hard_10": 20.0
  }
}
```

### Example 2: Guaranteed Low-Tier Loot

For starter area chests, guarantee a small reward:

```json
"common": {
  "baseChance": 1.0,
  "itemWeights": {
    "Ingredient_Hysouls_Soul_Essence_Hard_2": 100.0
  }
}
```

### Example 3: Balanced Mid-Game

For balanced progression:

```json
"uncommon": {
  "baseChance": 0.40,
  "itemWeights": {
    "Ingredient_Hysouls_Soul_Essence_Hard_4": 40.0,
    "Ingredient_Hysouls_Soul_Essence_Hard_5": 40.0,
    "Ingredient_Hysouls_Soul_Essence_Hard_6": 20.0
  }
}
```

## Future Enhancements

Possible future additions:

- **Biome-Based Loot**: Different loot tables per biome
- **Location-Based Rules**: Tier based on distance from spawn
- **Multiple Items**: Allow chests to receive multiple soul items
- **Reload Command**: `/hysouls reload` to refresh config without restart
- **Debug Command**: `/hysouls chestloot debug` to see tier and roll results
- **Prefab Tags**: Allow prefabs to specify tier via NBT tags

## Support

For issues or questions:
1. Check server logs for `[ChestLoot]` messages
2. Verify config syntax (use a JSON validator)
3. Report bugs at: https://github.com/anthropics/claude-code/issues
