---
title: Soul Drops Config
parent: Server Guide
nav_order: 2
---

# Soul Drops Configuration

Control how many souls and humanity each enemy type drops when killed.

**File location:** `mods/Hysouls/server/soul_drop_config.json`

---

## Structure

```json
{
  "enabled": true,
  "categories": {
    "CategoryName": {
      "tags": ["Tag1", "Tag2"],
      "drops": {
        "dropName": {
          "itemId": "Ingredient_Hysouls_Soul_Essence",
          "amount": 100,
          "chance": 1.0
        }
      }
    }
  }
}
```

### Fields

| Field | Description |
|---|---|
| `enabled` | Set to `false` to disable all soul drops from NPCs |
| `categories` | Map of category names to their drop rules |
| `tags` | List of NPC group names that match this category (see [NPC Groups](npc-groups)) |
| `drops` | Map of drop entries for this category |
| `itemId` | The item to drop (usually `Ingredient_Hysouls_Soul_Essence` or `Ingredient_Hysouls_Humanity_Essence`) |
| `amount` | How many to drop |
| `chance` | Drop probability from `0.0` (never) to `1.0` (always) |

---

## Default Categories

| Category | Tags | Soul Drop | Humanity Drop |
|---|---|---|---|
| Dragon | Dragons | 1,500 (100%) | 1 (10%) |
| Void | Void | 1,500 (100%) | 1 (10%) |
| Golem | Golems | 600 (100%) | 1 (5%) |
| Undead | Undead, Skeleton, Zombie | 600 (100%) | 1 (5%) |
| Intelligent | Trork, Outlander, Goblin, Scarak, Kweebec, Feran variants | 300 (100%) | 1 (3%) |
| Spirit | Spirits | 150 (100%) | 1 (1%) |
| Beast | Fen_Stalker, Predators, Spiders, Scorpions, etc. | 150 (100%) | 1 (1%) |
| Wildlife | Prey, Deer, Moose, Bison, Ram, Mouflon | 50 (100%) | None |
| Livestock | Pig, Sheep, Goat, Cow, Horse, Camel | 30 (100%) | None |
| Flying_Critter | Birds, Flock, Chicken, Turkey | 15 (100%) | None |
| Aquatic | Aquatic | 25 (100%) | None |
| Critter | Critters, Vermin, Mouse, Rat, Rabbit | 20 (100%) | 1 (10%) |
| Passive | Passive | 10 (100%) | None |
| default | *(fallback for unmatched NPCs)* | 50 (100%) | 1 (2%) |

---

## Customization Examples

### Increase dragon soul drops

```json
"Dragon": {
  "tags": ["Dragons"],
  "drops": {
    "souls": {
      "itemId": "Ingredient_Hysouls_Soul_Essence",
      "amount": 5000,
      "chance": 1.0
    }
  }
}
```

### Disable humanity drops from a category

Remove the humanity entry from the `drops` map, or set its `chance` to `0.0`:

```json
"humanity": {
  "itemId": "Ingredient_Hysouls_Humanity_Essence",
  "amount": 1,
  "chance": 0.0
}
```

### Add a new custom category

Add a new entry to `categories` with the tags matching your NPC groups:

```json
"Boss": {
  "tags": ["BossNPCs"],
  "drops": {
    "souls": {
      "itemId": "Ingredient_Hysouls_Soul_Essence",
      "amount": 10000,
      "chance": 1.0
    },
    "humanity": {
      "itemId": "Ingredient_Hysouls_Humanity_Essence",
      "amount": 3,
      "chance": 1.0
    }
  }
}
```

You'll also need to create a matching [NPC Group](npc-groups) file.

---

## Disabling Soul Drops

Set `enabled` to `false` at the top of the config:

```json
{
  "enabled": false,
  "categories": { ... }
}
```
