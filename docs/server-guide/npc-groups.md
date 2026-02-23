---
title: NPC Groups
parent: Server Guide
nav_order: 4
---

# NPC Groups

NPC Groups define which NPCs belong to which category for the soul drops system. Each group file maps a group name to a list of NPC role IDs.

**File location:** `Server/NPC/Groups/` (within the mod's resource pack)

---

## How It Works

1. NPC Group files define named groups of NPC roles
2. The [Soul Drops Config](soul-drops-config) references these group names in its `tags` field
3. When an NPC dies, the system checks which groups it belongs to and looks up the matching drop category

---

## Built-in Groups

### Dragons

```json
{
  "IncludeRoles": ["Dragon_Fire", "Dragon_Frost"]
}
```

### Golems

```json
{
  "IncludeRoles": [
    "Golem_Crystal_Earth",
    "Golem_Crystal_Flame",
    "Golem_Crystal_Frost",
    "Golem_Crystal_Sand",
    "Golem_Crystal_Thunder",
    "Golem_Firesteel",
    "Golem_Guardian_Void"
  ]
}
```

### Spirits

```json
{
  "IncludeRoles": [
    "Spirit_Ember",
    "Spirit_Frost",
    "Spirit_Root",
    "Spirit_Thunder"
  ]
}
```

---

## Adding Custom Groups

To add a new NPC group:

1. Create a new JSON file in `Server/NPC/Groups/` (e.g., `BossNPCs.json`)
2. Add the NPC role IDs:

```json
{
  "IncludeRoles": ["Boss_Dragon_King", "Boss_Void_Lord"]
}
```

3. Reference the group name (filename without extension) in your [Soul Drops Config](soul-drops-config) `tags`:

```json
"Boss": {
  "tags": ["BossNPCs"],
  "drops": {
    "souls": {
      "itemId": "Ingredient_Hysouls_Soul_Essence",
      "amount": 10000,
      "chance": 1.0
    }
  }
}
```

---

## Finding NPC Role IDs

NPC role IDs correspond to the NPC definitions in the Hytale server resource files. Check the server's NPC definition files for the exact role IDs available.
