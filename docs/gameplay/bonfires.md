---
title: Bonfires
parent: Gameplay
nav_order: 3
---

# Bonfires

Bonfires are the central mechanic of Hy-Souls. They serve as rest points, respawn locations, and crafting stations.

---

## Crafting a Bonfire

Build a Bonfire at a Workbench with:

| Material | Amount |
|---|---|
| Wood Trunk | 4 |
| Iron Sword | 1 |
| Soul Essence | 20 |

Place the Bonfire block in the world -- it emits fire particles and light.

---

## Resting at a Bonfire

Right-click a placed Bonfire to rest. Resting automatically:

- **Recharges your Estus Flask** to full capacity
- **Restores your health and stamina**
- **Sets the bonfire as your respawn point** -- you'll return here when you die
- **Opens the Bonfire Menu**

---

## Bonfire Menu

When you rest at a bonfire, a menu opens with these options:

| Option | Description |
|---|---|
| **Level Up** | Coming soon |
| **Reverse Hollowing** | Restores your human form if you're hollow (costs Humanity) |
| **Kindle** | Increases Estus capacity at this bonfire (costs Humanity) |
| **Craft** | Opens the crafting interface with your souls as virtual currency |
| **Leave** | Closes the menu |

You can also open the bonfire menu directly with the `/bmenu` command.

---

## Crafting at a Bonfire

The bonfire crafting menu has three tabs:

### Soul Essence Exchange

Trade your souls for metal bars. Higher-tier bars require upgrading your bonfire first.

| Bar | Soul Cost | Craft Time | Required Tier |
|---|---|---|---|
| Copper Bar | 200 | 3s | Tier 1 |
| Iron Bar | 300 | 4s | Tier 1 |
| Cobalt Bar | 800 | 6s | Tier 2 |
| Thorium Bar | 1,000 | 7s | Tier 2 |
| Silver Bar | 1,200 | 8s | Tier 2 |
| Gold Bar | 3,000 | 10s | Tier 3 |
| Adamantite Bar | 4,000 | 12s | Tier 3 |
| Mithril Bar | 5,000 | 15s | Tier 3 |

### Armor (Solaire Set)

Craft the Solaire armor set using souls and materials:

| Piece | Materials | Stats |
|---|---|---|
| Sol Head | 1 Red Feathers + 9 Iron Bar + 10 Souls | +9 HP, 5% resist |
| Sol Chest | 6 Linen Bolt + 10 Souls + 20 Fibre + 6 Light Leather | +17 HP, 9% resist |
| Sol Legs | 8 Iron Bar + 10 Souls | +13 HP, 7% resist |
| Sol Hands | 5 Iron Bar + 10 Souls | +7 HP, 4% resist |

### Merchant

Purchase consumable items:

| Item | Soul Cost | Craft Time |
|---|---|---|
| Homeward Bone | 150 | 2s |

---

## Bonfire Tiers

Upgrade your bonfire to unlock higher-tier crafting recipes:

| Upgrade | Soul Cost | Craft Time | Unlocks |
|---|---|---|---|
| Tier 1 to Tier 2 | 1,500 | 8s | Silver, Thorium, Cobalt bars |
| Tier 2 to Tier 3 | 5,000 | 15s | Gold, Adamantite, Mithril bars |

---

## Kindling

Kindling increases how many Estus charges you receive when resting at that specific bonfire. Each kindle level costs **1 Humanity** and you must be in **human form** (not hollow).

| Kindle Level | Estus Charges |
|---|---|
| 0 (default) | 5 |
| 1 | 10 |
| 2 | 15 |
| 3 (max) | 20 |

Kindling is tracked **per player, per bonfire** -- kindling one bonfire doesn't affect others. After kindling, your Estus is immediately recharged to the new capacity.
