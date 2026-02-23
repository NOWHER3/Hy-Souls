---
title: Commands
nav_order: 4
---

# Commands

All commands available to players in Hy-Souls.

---

## Soul Commands

| Command | Description |
|---|---|
| `/soulcount` | Display your current soul total |

---

## Humanity Commands

| Command | Description |
|---|---|
| `/humanitycount` | Display your current humanity total |
| `/humanitycount set <amount>` | Set your humanity to a specific amount |
| `/humanitycount add <amount>` | Add humanity to your total |

---

## HUD Commands

| Command | Description |
|---|---|
| `/togglesoulhud` | Show or hide the soul counter HUD |
| `/soulhudpos <side> <offset> <bottom>` | Reposition the soul HUD |
| `/togglehumanityhud` | Show or hide the humanity counter HUD |
| `/humanityhudpos <side> <offset> <bottom>` | Reposition the humanity HUD |

**Position arguments:**
- `side` -- `left` or `right` side of the screen
- `offset` -- pixel distance from the chosen side edge
- `bottom` -- pixel distance from the bottom edge

Example: `/soulhudpos right 20 50` places the soul counter 20px from the right edge and 50px from the bottom.

See [HUD Customization](hud) for more details.

---

## Item Commands

| Command | Description |
|---|---|
| `/estusslot <1-9>` | Set which hotbar slot your Estus Flask goes to |

---

## Warp Commands

These commands require permissions from the server.

| Command | Permission | Description |
|---|---|---|
| `/warp <name>` | `soul.command.warp` | Teleport to a named warp |
| `/setwarp <name>` | `soul.command.setwarp` | Create a warp at your current location |
| `/delwarp <name>` | `soul.command.delwarp` | Delete a named warp |
| `/warps` | `soul.command.warp` | List all your warps |

**Warp behavior:**
- If the server has a warmup timer configured, you'll see a countdown. **Don't move** during the countdown or the teleport is cancelled.
- If the server has a cooldown configured, you'll need to wait between warps.
- Cross-world teleportation is not supported.

---

## Utility Commands

| Command | Description |
|---|---|
| `/bmenu` | Open the bonfire menu directly (without interacting with a bonfire block) |
