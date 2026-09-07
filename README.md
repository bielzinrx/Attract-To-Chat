<p align="center">
  <img src="https://i.imgur.com/3U9J050.png" alt="Attract to Chat" width="100%">
</p>

<p align="center">
  <strong>Every message you type makes a sound.<br>Mobs hear it — and come looking.</strong>
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
  <strong>Minecraft 1.19.2 · Java 17 · Server-side with optional client installation</strong><br>
  <em>The Fabric version requires Fabric API 0.76.0 or newer.</em>
</p>

---

## ◈ Messages Become Sound

Type a message. Nearby mobs hear it, walk to where you said it and start investigating the area.

![Chat attraction demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/chat_attraction_bri8qk.gif)

Three rules make it feel alive:

* **CAPS and `!!!` shout** — louder messages travel farther and mobs react faster.
* **Walls muffle** — caves and houses are safer places to talk than open fields.
* **Mobs stay mobs** — fighting mobs keep their real targets, villagers still flee zombies, sleeping villagers can wake up. Vanilla AI is untouched.

---

## ◈ More Than Just Danger

Tired of struggling to move villagers with boats and minecarts?

Use chat to call them toward farms, bases and trading halls. And while you are at it:

* lure hostile mobs away from a friend in trouble;
* fake a distraction — and slip away while they investigate;
* turn chat itself into a stealth mechanic on survival and horror servers;
* run "Don't Scream" style challenges;
* attract compatible creatures added by other mods;
* and yes: punish the player who never stops shouting.

![CAPS loudness demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/caps_dnnpi4.gif)

---

## ◈ Troll Mode

**The admin's favorite button.**

Secretly mark a player and every message they type becomes irresistible — mobs hear them from farther away and react faster. They will never know why the zombies keep finding them.

![Troll Mode demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/troll_opvctf.gif)

---

## ◈ Make It Yours

Adjust how intense the mod feels without touching the core mechanic — everything applies live, no restart needed.

**Presets**<br>
Swap the whole feel with one command: **Safe**, **Casual**, **Chaos** or **Silent** — or save your own custom configurations.

**Vocal Fatigue**<br>
Shout too much and you go hoarse: a 30-second mute plus curious mobs coming to investigate. Milk clears it instantly, honey helps and death resets it. OFF by default.

**Anti-Spam**<br>
Rapid-fire messages stop attracting mobs. Chat itself is never cancelled or hidden — it is only ignored for attraction. Troll Mode players have a bypass. OFF by default.

Use `/atc help` to view the available commands.

---

## ◈ Quick Start

1. Drop the JAR in your server's `mods` folder — Fabric servers also need Fabric API.
2. Restart the server.
3. Type in chat. Watch what happens.

That's it — players join without installing anything. The mod runs entirely on the server.

| Loader | Attract to Chat | Additional dependency |
|:--|:--|:--|
| **Forge 1.19.2** | **2.1.1** | None |
| **Fabric 1.19.2** | **2.1.1** | [Fabric API 0.76.0 or newer](https://modrinth.com/mod/fabric-api/versions?g=1.19.2) |

Do not mix Forge and Fabric files.

**Optional client install** unlocks personal investigation-path particle controls:

```text
/atc client particles enable
/atc client particles disable
```

Particles are disabled by default.

---

## ◈ For Server Admins

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

## ◈ FAQ

### Do players need Attract to Chat installed?

No. The core gameplay mechanics run on the server.

### What does the optional client installation add?

Personal investigation-path particle controls.

### Does Attract to Chat replace Minecraft's mob AI?

No. It adds an investigation goal alongside vanilla AI — fighting mobs keep their real targets, villagers still flee zombies, sleeping villagers can wake up, and flying, aquatic and jumping mobs use movement that fits them. Invalid or unreachable destinations are simply ignored.

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
  <a href="https://url-shortener.curseforge.com/zFhxc"><img src="https://img.shields.io/badge/BisectHosting-Get_25%25_OFF-FF6C2F?style=flat-square" alt="BisectHosting 25% Off"></a>
  <a href="https://ko-fi.com/bielzinrx"><img src="https://img.shields.io/badge/Ko--fi-Support_the_Project-FF5E5B?style=flat-square&amp;logo=kofi&amp;logoColor=white" alt="Support the project on Ko-fi"></a>
</p>

<p align="center">
  <a href="https://github.com/bielzinrx/Attract-To-Chat/issues"><img src="https://img.shields.io/badge/GitHub-Report_a_Bug-181717?style=flat-square&amp;logo=github&amp;logoColor=white" alt="Report a Bug"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat"><img src="https://img.shields.io/badge/GitHub-Source_Code-181717?style=flat-square&amp;logo=github&amp;logoColor=white" alt="Source Code"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/attract-to-chat"><img src="https://img.shields.io/badge/CurseForge-Project_Page-f16436?style=flat-square&amp;logo=curseforge&amp;logoColor=white" alt="CurseForge"></a>
  <a href="https://www.planetminecraft.com/mod/attract-to-chat-mob-attraction-by-chat-messages/"><img src="https://cdn.jsdelivr.net/gh/VoxelForge-oss/voxicons@main/badges-248/badges/planet-minecraft.png" width="78" height="20" alt="Planet Minecraft"></a>
</p>

<p align="center">
  Created by <strong>bielzinrx</strong> · Contributor <strong>Theus452</strong> · Tester <strong>kots_luffyzin</strong>
</p>
