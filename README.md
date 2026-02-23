
![Cover](https://github.com/user-attachments/assets/e9aeae9f-9896-4449-86e3-dc494267a4d3)

# Hy-Souls

A Dark Souls-inspired mod for Hytale that brings the punishing-but-rewarding Souls gameplay loop to the world of Hytale.

**Version 0.0.8**

## Features

### Souls & Humanity
- Earn **Souls** by defeating enemies and finding consumable soul items in chests
- Spend souls at bonfires to craft gear, trade for metal bars, and buy consumables
- **Humanity** drops as a rare reward from enemies -- used to reverse hollowing and kindle bonfires

### Bonfires
- Craftable rest points (4 Wood Trunk + 1 Iron Sword + 20 Soul Essence)
- Resting recharges Estus, restores health/stamina, and sets your respawn point
- Craft armor, exchange souls for metal bars, and purchase items
- **Kindle** bonfires to increase Estus capacity (5 → 10 → 15 → 20)
- **Upgrade** bonfires through 3 tiers to unlock higher-value bar recipes

### Death & Hollowing
- Death clears all souls and humanity, drops items at your death location
- Dying turns you **hollow**, changing your appearance
- Reverse hollowing at a bonfire using humanity

### Items
- **Estus Flask** -- Rechargeable healing (Instant Heal III + Health Regen III), refills at bonfires
- **Homeward Bone** -- Consumable teleport back to your last bonfire
- **Solaire Armor Set** -- Full armor set craftable at bonfires
- **Metal Bars** -- 8 types (Copper through Mithril) traded for souls at bonfires
- **Consumable Soul Items** -- 10 tiers of soul items found in chests (200 to 20,000 souls)

### Soul Drops

| Enemy Type | Souls |
|---|---|
| Dragons, Void | 1,500 |
| Golems, Undead | 600 |
| Intelligent (Trorks, Goblins, etc.) | 300 |
| Spirits, Beasts | 150 |
| Wildlife | 50 |
| Livestock | 30 |
| Aquatic | 25 |
| Critters | 20 |
| Flying Critters | 15 |
| Passive | 10 |

### Chest Loot
- World-generated chests are populated with soul items based on tiered rarity
- Common, Rare, Epic, and Legendary tiers with weighted random selection
- Fully configurable by server owners

### HUD
- On-screen Soul and Humanity counters
- Toggleable and repositionable via commands
- Auto-hides when the map is open
- Requires [MultipleHUD](https://hytale.com) (optional)

### Commands

| Command | Description |
|---|---|
| `/soulcount` | Check your soul total |
| `/humanitycount` | Check your humanity total |
| `/togglesoulhud` | Toggle soul HUD |
| `/togglehumanityhud` | Toggle humanity HUD |
| `/soulhudpos <side> <offset> <bottom>` | Reposition soul HUD |
| `/humanityhudpos <side> <offset> <bottom>` | Reposition humanity HUD |
| `/estusslot <1-9>` | Set Estus hotbar slot |
| `/bmenu` | Open bonfire menu |

## Installation

1. Place `Hy-Souls.jar` in your server's `mods/` folder
2. Start the server -- config files are created automatically in `mods/Hysouls/`
3. See the [Wiki](https://nowher3.github.io/Hy-Souls/) for full documentation

## Server Configuration

All configs are in `mods/Hysouls/server/`:

- `soul_drop_config.json` -- Customize soul/humanity drops per enemy type
- `chest_loot_config.json` -- Configure chest loot tiers and item weights
- `warpconfig.json` -- Set warp warmup and cooldown timers

See the [Server Guide](https://nowher3.github.io/Hy-Souls/server-guide/installation) for details.

## Recommended Mods

- HyDifficulty
- Perfect Dodges
- Perfect Parries
- More Stamina!

## WIP Features

- Level Up system
- Weapons
- Bosses

## Feedback

Please report any issues or suggestions in the [Issues](https://github.com/NOWHER3/Hy-Souls/issues) tab.
