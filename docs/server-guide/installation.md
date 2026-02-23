---
title: Installation
parent: Server Guide
nav_order: 1
---

# Installation

How to install Hy-Souls on your Hytale server.

---

## Requirements

- A Hytale server
- **MultipleHUD** mod (optional, for HUD display support)

---

## Steps

1. Download the latest `Hy-Souls.jar` from the releases page
2. Place it in your server's `mods/` directory
3. Start the server -- the mod will create its configuration files automatically

On first run, the following directories are created under `mods/Hysouls/`:

```
mods/Hysouls/
├── data/          # Per-player gameplay data (auto-managed)
├── user/          # Per-player UI preferences (auto-managed)
└── server/        # Server-wide configuration files
    ├── soul_drop_config.json
    ├── chest_loot_config.json
    └── warpconfig.json
```

---

## Configuration Files

After first launch, you can customize the mod by editing the files in `mods/Hysouls/server/`:

- [Soul Drops Config](soul-drops-config) -- Customize how many souls each enemy type drops
- [Chest Loot Config](chest-loot-config) -- Configure what soul items appear in chests and how often
- [NPC Groups](npc-groups) -- Define which NPCs belong to which category

---

## Permissions

Most commands require no special permissions. The following warp commands need permission nodes:

| Permission | Commands |
|---|---|
| `soul.command.warp` | `/warp`, `/warps` |
| `soul.command.setwarp` | `/setwarp` |
| `soul.command.delwarp` | `/delwarp` |
| `soul.warps.bypass.cooldown` | Skip warp cooldown timer |
| `soul.warps.bypass.warmup` | Skip warp warmup countdown |

---

## Warp Configuration

The file `mods/Hysouls/server/warpconfig.json` controls warp teleportation timing:

- **Warmup** -- How many seconds players must stand still before teleporting
- **Cooldown** -- How many seconds between warp uses
