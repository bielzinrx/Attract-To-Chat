# Attract to Chat

**Attract to Chat** is a gameplay mod for Minecraft that makes mobs react to player chat messages.  
When you type a message in the chat, nearby mobs listed in the configuration will “hear” it and walk toward the exact location where the message was sent.

This adds an immersive and fun mechanic to the game, allowing for creative traps, roleplay scenarios, or just a new challenge when exploring.

🔗 Official Download Sources

- [GitHub Repository](https://github.com/bielzinrx/Attract-To-Chat/)  
- [📥 CurseForge Page](https://www.curseforge.com/minecraft/mc-mods/attract-to-chat)
- [📥 Modrinth Page](https://modrinth.com/mod/attract-to-chat)
---

## Main Features

- **Mob Attraction by Chat** – Specific mobs will navigate to the position where you sent a chat message.  
- **Customizable Detection Range** – Set how far mobs can “hear” you (in blocks).  
- **Cooldown Between Scans** – Control how often mobs scan for messages to react to.  
- **Forget Time** – Mobs will forget your message location after a set number of seconds.  
- **Entity Filtering** – Choose exactly which mobs can be attracted, including mobs from other mods using `modid:entity_name`.

---

## ⚙️ Configuration

**Server-Side Config Path:**  
/YOUR_WORLD/serverconfig/attracttochat-server.toml

**Client-Side (Per-World) Config Path:**  
/saves/YOUR_WORLD/serverconfig/attracttochat-server.toml

You can edit these files to:
- Change the maximum hearing range.
- Adjust the scan cooldown in ticks (**20 ticks = 1 second**).
- Set the time in seconds for mobs to forget the message location.
- Add or remove entity IDs from the attraction list.

---

### Default Configuration Example
```toml
[general]
hearingRange = 30.0
scanCooldownTicks = 20
forgetTargetAfterSeconds = 5
enabledEntities = ["minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"]
```
Compatibility
Minecraft: 1.20.1 (Forge)

Works in both singleplayer and multiplayer.

Only the server needs to have the mod installed for it to work (clients do not require it unless customizing per-world configs).

⚠️ Notes
This mod affects mob pathfinding; large groups of mobs reacting at once may impact performance on weaker servers.

To disable mob reactions, clear the entity list or set the hearing range to 0 in the config file.
