<p align="center">
  <img src="https://i.imgur.com/3U9J050.png" alt="Attract to Chat" width="100%">
</p>

<p align="center">
  <strong>Turn player messages into sounds that Minecraft mobs can hear and investigate.</strong>
</p>

<p align="center">
  <a href="https://modrinth.com/mod/attract-to-chat/versions">
    <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/forge_46h.png" alt="Supports Forge">
  </a>
  <a href="https://modrinth.com/mod/attract-to-chat/versions">
    <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/fabric_46h.png" alt="Supports Fabric">
  </a>
</p>

<p align="center">
  <strong>Minecraft 1.19.2 &amp; 1.20.1 · Java 17 · Server-side with optional client installation</strong><br>
  <em>Fabric API is required: 0.76.0+ for Minecraft 1.19.2 and 0.92.7+ for Minecraft 1.20.1.</em>
</p>

---

## ◈ Messages Become Sound

**Attract to Chat** makes Minecraft mobs react to player messages.

Send a message and nearby mobs may hear it, travel toward the location where it was sent and investigate the area. Writing in CAPS or using exclamation marks makes the message louder, allowing it to reach mobs from farther away and making them react faster.

![Chat attraction demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/chat_attraction_bri8qk.gif)

---

## ◈ Terrain Muffling

**Walls can muffle chat.**

Solid terrain can reduce how far a message can be heard. Thin barriers may only soften the effect, while thicker structures can make communication much harder for nearby mobs to detect.

This makes caves, houses and other enclosed spaces safer places to talk than open terrain.

---

## ◈ More Than Just Danger

Tired of struggling to move villagers with boats and minecarts?

Use chat to call them toward farms, bases, villages or trading halls.

You can also use messages to:

* lure hostile mobs away from another player;
* create distractions while escaping;
* turn multiplayer chat into a stealth mechanic;
* make survival and horror servers more intense;
* create "Don't Scream" style challenges;
* attract compatible creatures added by other mods;
* punish players who cannot stop shouting.

![CAPS loudness demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/caps_dnnpi4.gif)

---

## ◈ Mobs Still Act Like Mobs

Attract to Chat works alongside Minecraft's existing AI instead of replacing it.

* Fighting mobs keep their real targets.
* Villagers prioritize escaping zombies and other dangers.
* Sleeping villagers can wake after hearing loud messages.
* Flying, aquatic and jumping mobs use compatible movement.
* Invalid or unreachable destinations are ignored.
* Investigations end when a more important behavior takes priority.

Mobs investigate the sound without abandoning their normal behavior.

---

## ◈ Features

* Configurable hearing range
* Louder CAPS and exclamation marks
* Terrain Muffling
* Vanilla and compatible modded mob support
* Villager attraction
* Player ignore rules
* Troll Mode
* Optional Vocal Fatigue
* Optional Anti-Spam
* Built-in and custom presets
* Live in-game configuration
* Command autocomplete
* Optional investigation-path particles

---

## ◈ Troll Mode

Troll Mode allows admins to secretly make selected players much more attractive to mobs.

Affected players can be heard from farther away and trigger faster, more aggressive reactions without changing the experience for everyone else on the server.

![Troll Mode demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/troll_opvctf.gif)

---

## ◈ Gameplay Customization

Server owners can adjust how intense Attract to Chat feels without changing the core mechanic.

**Presets**  
Choose from built-in presets such as **Safe**, **Casual**, **Chaos** and **Silent**, or save your own custom configurations.

**Vocal Fatigue**  
Optional Vocal Fatigue discourages players from constantly shouting. Too many CAPS or `!!!` messages make the player hoarse; crossing the threshold mutes them for 30 seconds and nearby mobs come to investigate. Milk clears it instantly, honey helps and death resets it. OFF by default.

**Anti-Spam**  
Optional Anti-Spam helps control repeated attraction triggers from excessive chat activity. It never cancels or hides chat messages — it only ignores them for attraction. Players in Troll Mode have a bypass.

**In-Game Configuration**  
Hearing, presets, ignored players, supported entities and other settings can be managed directly in-game.

Use `/atc help` to view the available commands.

---

## ◈ Configuration

Every option lives in `config/attracttochat-common.json` and applies live — edit the file or use commands, no restart needed. The file comes commented line by line: a `#` note above each option explains what it does, its unit, default, valid range and a practical example.

**Options at a glance**

* `hearingRange` — 30 blocks (0–500): how far mobs hear normal chat
* `capsRangeBonus` — +5 blocks per CAPS word (0–100)
* `mobSpeedBase` / `mobSpeedMax` — 1.2 / 2.0 investigation speed multipliers
* `trollSpeedMultiplier` — 2.5 for Troll Mode targets
* `forgetTargetAfterSeconds` — 20s (1–300) until a mob gives up searching
* `scanCooldownTicks` — 40 ticks = 2s between attraction scans per player
* `antiSpamMaxMessages` / `antiSpamWindowSeconds` — 3 messages / 8s window
* `traumaThreshold` — 1000 shout trauma before vocal fatigue
* `muteDurationTicks` — 600 ticks = 30s hoarse mute
* `enableVocalFatigue` / `enableAntiSpam` — OFF by default
* `enableCapsFeature` / `showParticles` — ON by default

**Command reference**

```text
/atc status
/atc config list
/atc config info <option>
/atc config hearingrange <value>
/atc feature caps|fatigue|antispam
/atc entity add|remove
/atc ignore add
/atc preset set <name>
```

**Presets** — `safe` 24/4/20t · `casual` 32/6/15t · `chaos` 60/14/5t · `silent` 30/5/20t (no particles)

---

## ◈ Server-Side Friendly

Install Attract to Chat on a **Forge or Fabric server running Minecraft 1.19.2 or 1.20.1** and players can join without installing the mod themselves.

Installing it on the client is optional and unlocks personal investigation-path particle controls:

```text
/atc client particles enable
/atc client particles disable
```

Particles are disabled by default.

---

## ◈ Installation

### Requirements

- Minecraft Java Edition **1.19.2 or 1.20.1**
- **Java 17**
- A Forge or Fabric server matching the downloaded build

| Minecraft | Loader | Attract to Chat | Additional dependency |
|:--|:--|:--|:--|
| **1.20.1** | Forge | **2.1.1** | None |
| **1.20.1** | Fabric | **2.1.1** | [Fabric API 0.92.7 or newer](https://modrinth.com/mod/fabric-api/versions?g=1.20.1) |
| **1.19.2** | Forge | **2.1.1** | None |
| **1.19.2** | Fabric | **2.1.1** | [Fabric API 0.76.0 or newer](https://modrinth.com/mod/fabric-api/versions?g=1.19.2) |

1. **Choose the correct build.** Download the version matching your **Minecraft version and mod loader** from [Modrinth](https://modrinth.com/mod/attract-to-chat/versions), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/attract-to-chat/files) or [GitHub Releases](https://github.com/bielzinrx/Attract-To-Chat/releases).
2. **Install the required files.** Place the Attract to Chat JAR inside the server's `mods` folder. Fabric installations must also include Fabric API.
3. **Start or restart the server.** The mod creates its configuration automatically during startup.
4. **Confirm the installation.** Use `/atc help` in game to view the available commands.

### Optional Client Installation

Players can join a modded server without installing Attract to Chat themselves. Client installation is only needed for personal investigation-path particle controls. The client and server must use the same loader; Fabric clients also require Fabric API.

---

## ◈ FAQ

### Do players need Attract to Chat installed?

No. The core gameplay mechanics run on the server.

### What does the optional client installation add?

Personal investigation-path particle controls.

### Does Attract to Chat replace Minecraft's mob AI?

No. Normal behaviors can still take priority over an investigation.

### Do walls affect hearing?

Yes. Solid terrain can muffle messages and reduce how far mobs can hear them.

### Does it support modded mobs?

Compatible creatures added by other mods can be used with the attraction system.

### Does it work with voice-chat mods like Voiceless Survival?

Attract to Chat reacts to text chat, not voice. The hooks are distinct, so both mods can run on the same server without conflict.

### Does it work with AI mods like Enhanced AI?

Yes. Attract to Chat adds an investigation goal without replacing vanilla mob AI. Mobs already in combat keep their real target.

### Do commands and team chat attract mobs?

No. Messages starting with `!`, `@`, `#` or `/` never attract anything.

### Can I use Attract to Chat in a modpack?

Yes. Attract to Chat is released under the **MIT License**.

---

## ◈ Support the Project

<p align="center">
  <a href="https://url-shortener.curseforge.com/zFhxc"><img src="https://img.shields.io/badge/BisectHosting-Get_25%25_OFF-FF6C2F?style=flat-square&logoColor=white" alt="BisectHosting 25% Off"></a>
  <a href="https://ko-fi.com/bielzinrx"><img src="https://img.shields.io/badge/Ko--fi-Support_the_project-FF5E5B?style=flat-square&logo=kofi&logoColor=white" alt="Ko-fi"></a>
</p>

<p align="center">
  <a href="https://github.com/bielzinrx/Attract-To-Chat/issues"><img src="https://img.shields.io/badge/GitHub-Report_a_Bug-181717?style=flat-square&logo=github&logoColor=white" alt="Report a Bug"></a>
  <a href="https://modrinth.com/mod/attract-to-chat"><img src="https://img.shields.io/badge/Modrinth-Project_Page-1bd96a?style=flat-square&logo=modrinth&logoColor=white" alt="Modrinth"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/attract-to-chat"><img src="https://img.shields.io/badge/CurseForge-Project_Page-f16436?style=flat-square&logo=curseforge&logoColor=white" alt="CurseForge"></a>
  <a href="https://www.planetminecraft.com/mod/attract-to-chat-mob-attraction-by-chat-messages/"><img src="https://raw.githubusercontent.com/VoxelForge-oss/voxicons/main/badges-248/badges/planet-minecraft.png" width="78" height="20" alt="Planet Minecraft"></a>
</p>

<p align="center">
  Created by <strong>bielzinrx</strong> · Contributor <strong>Theus452</strong> · Tester <strong>kots_luffyzin</strong>
</p>
