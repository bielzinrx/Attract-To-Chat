# Modrinth - Main Page Description

Copy this for your Modrinth mod description:

---

# 🔊 Attract to Chat

**Attract to Chat** is a gameplay mod that makes mobs react to player chat messages!

When you type a message in the chat, nearby mobs will "hear" it and walk toward the exact location where the message was sent. This adds an immersive mechanic to the game, perfect for creative traps, roleplay scenarios, or just a new challenge when exploring.

![Attract to Chat Banner](https://cdn.modrinth.com/data/cached_images/8ddfffe956c06a25fc31e2d2f06e6c0cc56d09d8.png)

---

## 🎯 Main Features

### 📢 Mob Attraction by Chat
Specific mobs will navigate to your position when you send a chat message. Works with both vanilla and modded mobs using `modid:entity_name` format!

### 🔊 CAPS LOCK = LOUDER!

| Uppercase | Effect |
|-----------|--------|
| Few letters | Normal range and speed |
| Many letters | **Greater range** + **Faster mobs** + **Longer pursuit** |
| Too many | Risk of **Vocal Fatigue**! |

### 😫 Vocal Fatigue System
Shouting too much (CAPS LOCK) can strain your voice!

- The **Vocal Fatigue** debuff prevents you from speaking
- Trying to speak while fatigued **extends the duration**
- At extreme levels, you'll also get **Nausea**

### 💊 Healing System
Different items heal different amounts:

| Item | Effect |
|------|--------|
| 🍯 **Honey Bottle** | Strong relief (60s) |
| 🥛 **Milk Bucket** | **Complete cure** |
| 🍲 **Soups/Stews** | Moderate relief (15s) |
| 💧 **Water Bottle** | Light relief (10s) |
| ☠️ **Poison/Harming** | Makes it **WORSE!** (+60s) |

### ✨ Visual & Audio Feedback
- **Particles** appear above mobs when they hear you
- **Action bar messages** for warnings (less intrusive)
- **Sound effects** for healing and damage

---

## ⚙️ Configuration

**Config Path:** `config/attracttochat-common.toml`

```toml
[general]
hearingRange = 30.0        # Base range in blocks
capsRangeBonus = 5.0       # Extra blocks per CAPS letter
scanCooldownTicks = 20     # Cooldown between messages
forgetTargetAfterSeconds = 5

enabledEntities = [
    "minecraft:zombie",
    "minecraft:skeleton",
    "minecraft:creeper"
]

[vocal_fatigue]
fatigueChanceMultiplier = 1.5
fatigueDurationBase = 30

[healing]
honeyRelief = 60
milkCure = true
waterRelief = 10
stewRelief = 15
```

---

## 📜 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/attracthelp` | Everyone | Shows help and tips |
| `/attractstats` | Everyone | View your session statistics |
| `/attractlist` | OP (level 2) | Lists enabled mobs and active goals |
| `/attractreload` | OP (level 2) | Reloads goals from config |
| `/attractdebug [on\|off]` | OP (level 2) | Toggle debug mode |

---

## 🌐 Languages Supported
- 🇺🇸 English (en_us)
- 🇧🇷 Portuguese - Brazil (pt_br)
- 🇪🇸 Spanish (es_es)

---

## 📊 Compatibility

| | |
|---|---|
| **Minecraft** | 1.20.1 |
| **Mod Loader** | Forge 47.4.0+ |
| **Java** | 17+ |
| **Side** | Server (clients don't need it) |

Works in both **singleplayer** and **multiplayer**!

---

## ⚠️ Notes

- This mod affects mob pathfinding; large groups reacting at once may impact performance
- To disable mob reactions, clear the entity list or set `hearingRange = 0`
- Use `/attractdebug on` to see detailed information about mob behavior

---

## 🔗 Links

- [GitHub Repository](https://github.com/bielzinrx/Attract-To-Chat/)
- [CurseForge Page](https://www.curseforge.com/minecraft/mc-mods/attract-to-chat)
- [Issue Tracker](https://github.com/bielzinrx/Attract-To-Chat/issues)
