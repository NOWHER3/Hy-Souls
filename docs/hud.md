---
title: HUD Customization
nav_order: 5
---

# HUD Customization

Hy-Souls adds two on-screen counters to your HUD: one for **Souls** and one for **Humanity**. Both can be toggled on/off and repositioned.

---

## Toggling HUD Elements

| Command | Effect |
|---|---|
| `/togglesoulhud` | Show or hide the soul counter |
| `/togglehumanityhud` | Show or hide the humanity counter |

Your toggle preferences are saved and persist across sessions.

---

## Repositioning

Both HUD elements can be moved to any corner of the screen:

```
/soulhudpos <side> <offset> <bottom>
/humanityhudpos <side> <offset> <bottom>
```

**Parameters:**

| Parameter | Values | Description |
|---|---|---|
| `side` | `left` or `right` | Which side of the screen |
| `offset` | Any number | Pixel distance from the side edge |
| `bottom` | Any number | Pixel distance from the bottom edge |

**Examples:**

| Command | Result |
|---|---|
| `/soulhudpos left 10 10` | Bottom-left corner |
| `/soulhudpos right 20 50` | Right side, slightly raised |
| `/humanityhudpos left 10 40` | Bottom-left, above the soul counter |

Position preferences are saved per-player and persist across sessions.

---

## Notes

- The soul HUD automatically hides when the in-game map is open and reappears when the map is closed.
- HUD display requires the **MultipleHUD** mod to be installed on the server (optional dependency). Without it, the HUD elements will not appear, but all other Hy-Souls features work normally.
