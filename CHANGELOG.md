# Changelog

All notable changes to Attract to Chat will be documented in this file.

## [2.0.0] - 2025

### 🆕 Added
- **Vocal Fatigue System** - Shouting (CAPS LOCK) can strain your voice!
- **CAPS LOCK Boost** - More uppercase letters = Greater range + Faster mobs + Longer chase time
- **Healing System** - Different items heal your throat:
  - 🍯 Honey Bottle - Strong relief (60s)
  - 🥛 Milk Bucket - Complete cure
  - 🍲 Soups/Stews - Moderate relief (15s)
  - 💧 Water Bottle - Light relief (10s)
  - ☠️ Poison/Harming potions - Makes it WORSE!
- **New Commands:**
  - `/attracthelp` - Shows help and information
  - `/attractstats` - View your session statistics
  - `/attractdebug [on|off]` - Toggle debug mode (OP)
- **Visual Feedback:**
  - Particles appear above mobs when attracted
  - Action bar messages (less intrusive than chat)
  - Sound effects for warnings and healing
- **Multi-language Support:** English, Portuguese (Brazil), Spanish
- **Automatic cleanup** of dead mobs for better performance
- **Thread-safe** mob data storage

### ✅ Fixed
- `scanCooldownTicks` now works correctly
- `forgetTargetAfterSeconds` now properly controls mob pursuit time

### 🎨 Improved
- Better formatted messages with colors
- More configuration options
- Cleaner code structure

---

## [1.0.1] - 2024

### 🆕 Added
- English and Portuguese tips in config file

---

## [1.0.0] - 2024

### 🆕 Added
- Initial release
- Mobs react to player chat messages
- Customizable detection range
- Cooldown between scans
- Forget time configuration
- Entity filtering (choose which mobs react)
