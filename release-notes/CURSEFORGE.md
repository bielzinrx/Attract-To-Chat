# CurseForge - Main Page Description

---

**Attract to Chat** is a gameplay mod that makes mobs react to player chat messages!

When you type a message in the chat, nearby mobs will "hear" it and walk toward the exact location where the message was sent. This adds an immersive mechanic to the game, perfect for creative traps, roleplay scenarios, or just a new challenge when exploring.

---

## 🎯 Main Features

### 📢 Mob Attraction by Chat
Specific mobs will navigate to your position when you send a chat message. Works with both vanilla and modded mobs!

### 🔊 CAPS LOCK = LOUDER!
- **More uppercase letters = Greater hearing range**
- **More uppercase letters = Mobs chase longer**
- **More uppercase letters = Mobs move faster**

But be careful... shouting too much has consequences!

### 😫 Vocal Fatigue System
Spamming CAPS LOCK can cause the **Vocal Fatigue** effect:
- While fatigued, you cannot speak or use voice commands
- Trying to speak while fatigued makes it worse!
- At extreme levels, you'll also get Nausea

### 💊 Healing System

| Item | Effect |
|------|--------|
| 🍯 **Honey Bottle** | Strong relief (60s) |
| 🥛 **Milk Bucket** | Complete cure |
| 🍲 **Soups/Stews** | Moderate relief (15s) |
| 💧 **Water Bottle** | Light relief (10s) |
| ☠️ **Poison/Harming** | Makes it WORSE! |

### ✨ Visual Feedback
- Particles appear above mobs when they hear you
- Action bar messages for warnings (less intrusive than chat)
- Sound effects for healing and damage

---

## ⚙️ Configuration

**Config Path:** `config/attracttochat-common.toml`

You can customize:
- Maximum hearing range
- Scan cooldown (in ticks)
- Forget time (how long mobs pursue)
- Which mobs can be attracted
- Fatigue chance and duration
- Healing amounts
- Visual/audio feedback

---

## 📜 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/attracthelp` | Everyone | Shows help and information |
| `/attractstats` | Everyone | Shows your session statistics |
| `/attractlist` | OP | Lists enabled mobs |
| `/attractreload` | OP | Reloads config |
| `/attractdebug` | OP | Toggle debug mode |

---

## 🌐 Languages
- 🇺🇸 English
- 🇧🇷 Portuguese (Brazil)
- 🇪🇸 Spanish

---

## 📊 Compatibility

- **Minecraft:** 1.20.1
- **Mod Loader:** Forge 47.4.0+
- **Side:** Server-side only (clients don't need it)

Works in both **singleplayer** and **multiplayer**!

---

## ⚠️ Notes
- This mod affects mob pathfinding; large groups may impact performance on weaker servers
- To disable, clear the entity list or set `hearingRange = 0` in config
