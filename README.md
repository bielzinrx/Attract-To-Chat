#  Attract to Chat

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge&logo=minecraft" alt="Minecraft 1.20.1"/>
  <img src="https://img.shields.io/badge/Forge-47.4.0+-orange?style=for-the-badge" alt="Forge"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License"/>
</p>

<p align="center">
  <img src="https://media.forgecdn.net/attachments/description/null/description_6350cc0a-6b1d-426b-afe3-ce097e3105e8.png" alt="Attract to Chat Mod"/>
</p>

**Attract to Chat** is an immersive gameplay mod for Minecraft that makes mobs react to player chat messages!  
When you type a message in chat, nearby mobs will "hear" it and walk toward the exact location where the message was sent.

This adds a unique mechanic to the game, perfect for creative traps, roleplay scenarios, or just a new challenge when exploring!

---

## • Official Download Sources
 
- [📥 CurseForge Page](https://www.curseforge.com/minecraft/mc-mods/attract-to-chat)
- [📥 Modrinth Page](https://modrinth.com/mod/attract-to-chat)
- 📥 Planet Minecraft Page (Coming Soon...)

---

## • Main Features

### Mob Attraction by Chat
- Specific mobs will navigate to your position when you send a chat message
- Works with both vanilla and modded mobs

### CAPS LOCK = LOUDER!
- **More uppercase letters = Greater range**
- **More uppercase letters = Mobs chase longer**
- **More uppercase letters = Mobs move faster**
- But be careful... shouting too much has consequences!

### Vocal Fatigue System
- Spamming CAPS LOCK can cause **Vocal Fatigue** effect
- While fatigued, you cannot speak or use voice commands
- Trying to speak while fatigued makes it worse!

### Healing System
| Item | Effect |
|------|--------|
| 🍯 **Honey Bottle** | Strong relief (60s) |
| 🥛 **Milk Bucket** | Complete cure |
| 🍲 **Soups/Stews** | Moderate relief (15s) |
| 💧 **Water Bottle** | Light relief (10s) |
| ☠️ **Poison/Harming** | Makes it WORSE! (+60s) |

### Visual Feedback
- Particles appear above mobs when they hear you
- Action bar messages for warnings (less intrusive than chat)
- Sound effects for healing and damage

---

## • Configuration

**Config Path:**  
`config/attracttochat-common.toml`

The config auto-reloads when saved - no restart needed!

### Example Configuration
```toml
[general]
hearingRange = 30.0        # Base range in blocks
capsRangeBonus = 5.0       # Extra blocks per CAPS letter
scanCooldownTicks = 20     # Cooldown between messages (20 ticks = 1 sec)
forgetTargetAfterSeconds = 5

enabledEntities = [
    "minecraft:zombie",
    "minecraft:skeleton",
    "minecraft:creeper",
    "minecraft:spider",
    "minecraft:enderman",
    "minecraft:husk",
    "minecraft:drowned",
    "minecraft:stray",
    "minecraft:phantom"
]

[visual]
showAttractionParticles = true
showAttractionFeedback = true

[vocal_fatigue]
fatigueChanceMultiplier = 1.5
fatigueDurationBase = 30
fatiguePenalty = 10

[healing]
honeyRelief = 60
waterRelief = 10
stewRelief = 15
poisonWorsen = 60
```

---

## • Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/attracthelp` | Everyone | Shows help and information |
| `/attractstats` | Everyone | Shows your session statistics |
| `/attractlist` | OP (level 2) | Lists enabled mobs and active goals |
| `/attractreload` | OP (level 2) | Reloads goals from config |
| `/attractdebug [on\|off]` | OP (level 2) | Toggles debug mode |

---

## • Supported Languages

- 🇺🇸 English (en_us)
- 🇧🇷 Portuguese - Brazil (pt_br)
- 🇪🇸 Spanish (es_es)

---

## • Compatibility

| | |
|---|---|
| **Minecraft** | 1.20.1 |
| **Mod Loader** | Forge 47.4.0+ |
| **Java** | 17+ |
| **Side** | Server (clients don't need it) |

Works in both **singleplayer** and **multiplayer**!

---

## • Notes

- This mod affects mob pathfinding; large groups of mobs reacting at once may impact performance on weaker servers
- To disable mob reactions, clear the entity list or set `hearingRange = 0` in the config
- Use `/attractdebug on` to see detailed information about mob behavior

---

## • Changelog

### v2.0.0
-  **NEW:** Vocal Fatigue System - Shouting (CAPS LOCK) can strain your voice!
-  **NEW:** CAPS LOCK Boost - More uppercase = Greater range + Faster mobs + Longer chase
-  **NEW:** Healing System - Honey, Milk, Water, and Soups heal your throat
-  **NEW:** `/attracthelp` command for help
-  **NEW:** `/attractstats` command for session statistics
-  **NEW:** `/attractdebug` command for debug mode
-  **NEW:** Visual particles when mobs are attracted (configurable)
-  **NEW:** Action bar messages (less intrusive)
-  **NEW:** Sound feedback for throat warnings
-  **NEW:** Automatic cleanup of dead mobs
-  **NEW:** 3 Languages: English, Portuguese, Spanish
-  **FIXED:** `scanCooldownTicks` now works correctly
-  **FIXED:** `forgetTargetAfterSeconds` now controls mob pursuit time
-  **IMPROVED:** Better formatted messages with colors
-  **IMPROVED:** Thread-safe mob data storage

---

## • License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ by <b>Bielzinrx and Theus452</b>
</p>
