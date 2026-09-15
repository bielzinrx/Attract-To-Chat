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
  <strong>Minecraft 1.19.2 &amp; 1.20.1 · Java 17 · Server-side with optional client installation</strong><br>
  <em>Fabric API is required: 0.76.0+ for Minecraft 1.19.2 and 0.92.7+ for Minecraft 1.20.1.</em>
</p>

***

## ◈ The idea

You're caving. It's been a good run — diamonds, iron, half a stack of lapis. Your friend types a joke in chat. You laugh, type back: _"holy crap this cave is HUGE"_

The typing stops. Somewhere in the dark, something stops too.

In **Attract to Chat**, chat is sound. Every message you type is a voice in the world — and the world listens. Mobs hear it, walk to where you said it and start investigating the area. That is the whole mod. And it quietly changes every conversation your server will ever have.

![Chat attraction demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/chat_attraction_bri8qk.gif)

Three rules keep it feeling alive:

*   **Shouting is loud.** CAPS and `!!!` carry your voice further — and bring company faster.
*   **Walls are quiet.** A cave, a basement, a walled base: safe rooms for quiet people. (Yes — even that cave. Especially that cave.)
*   **Mobs stay mobs.** Fighting mobs keep their real target, villagers still flee zombies, sleeping villagers can wake up. Vanilla AI is never replaced. The world just… pays attention now.

***

## ◈ A tool, not just a trap

Here's the twist: the same rule that gets you killed can work for you.

Tired of dragging villagers around with boats and minecarts? **Talk to them** — call them toward farms, bases and trading halls. And while you are at it:

*   lure hostile mobs away from a friend in trouble;
*   fake a distraction — and slip away while they investigate;
*   turn chat itself into a stealth mechanic on survival and horror servers;
*   run "Don't Scream" style challenges;
*   attract creatures added by other mods;
*   and yes: punish the player who never stops shouting.

![CAPS loudness demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/caps_dnnpi4.gif)

***

## ◈ Troll Mode

**The admin's favorite button.**

Secretly mark a player and every message they type becomes irresistible — mobs hear them from farther away and react faster. No glow. No warning. Just a player slowly realizing that the zombies always know exactly where they are.

They will never know why.

![Troll Mode demonstration](https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/troll_opvctf.gif)

***

## ◈ Make it yours

Adjust how intense the mod feels without touching the core mechanic — everything applies live, no restart needed.

**Presets** — swap the whole feel with one command: **Safe**, **Casual**, **Chaos** or **Silent** — or save your own.

**Vocal fatigue** _(off by default)_ — shout too much and you go hoarse: a short mute plus curious mobs coming to check on you. Milk clears it instantly, honey helps, death resets it.

**Anti-spam** _(off by default)_ — rapid-fire messages stop attracting mobs. Chat is never cancelled or hidden; it just stops being loud.

Use `/atc help` to see everything the mod can do.

***

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
* `enableCapsFeature` — ON by default
* `enableCapsFeature` — ON by default
* `walkieChatCompat` — ON by default: optional Walkie-Chat integration (proximity chat, block stations) — Minecraft 1.20.1
* `walkieChatProximityRange` / `walkieChatProximityCapsBonus` — 15 / 10 blocks: attraction range for Walkie-Chat proximity chat (Minecraft 1.20.1)

**Command reference**

```text
/atc status
/atc walkiechat on|off
/atc config list
/atc config info <option>
/atc config hearingrange <value>
/atc feature caps|fatigue|antispam
/atc entity add|remove
/atc ignore add
/atc preset set <name>
```

***

## ◈ Compatibility

Plays well with others — no setup, no conflicts:

*   [Walkie-Chat](https://www.curseforge.com/minecraft/mc-mods/walkie-chat) _(Minecraft 1.20.1)_ — walkie talkies join the sound world: proximity chat attracts mobs, and hostile mobs that reach a Walkie Block station will smash it. On 1.19.2 both mods run side by side, no conflicts.
*   **Voice-chat mods** — Attract to Chat listens to typed words, not voice. Both run on the same server.
*   **AI mods** — the investigation goal walks beside vanilla AI; mobs in combat keep their real target.
*   **Modded mobs** — creatures added by other mods can hear you too.

***

## ◈ Getting started

1.  Drop the JAR in your server's `mods` folder — Fabric servers also need Fabric API.
2.  Restart the server.
3.  Say something. Watch what answers.

That's it — players join without installing anything.

| Minecraft |Loader |Attract to Chat |Additional dependency      |
| --------- |------ |--------------- |-------------------------- |
| <strong>1.20.1</strong> |Forge  |<strong>2.1.2</strong> |None                       |
| <strong>1.20.1</strong> |Fabric |<strong>2.1.2</strong> |<a href="https://www.curseforge.com/minecraft/mc-mods/fabric-api/files" target="_blank" rel="nofollow">Fabric API 0.92.7 or newer</a> |
| <strong>1.19.2</strong> |Forge  |<strong>2.1.2</strong> |None                       |
| <strong>1.19.2</strong> |Fabric |<strong>2.1.2</strong> |<a href="https://www.curseforge.com/minecraft/mc-mods/fabric-api/files" target="_blank" rel="nofollow">Fabric API 0.76.0 or newer</a> |

Do not mix Forge and Fabric files.

**Optional client install** — adds a personal switch for the investigation particles (`/atc client particles enable|disable`). Nobody sees them until they ask.

***

## ◈ FAQ

**Do players need the mod installed?** No. The magic runs on the server.

**Does it replace Minecraft's mob AI?** No — it adds an investigation goal alongside it. Mobs already in combat keep their real target; villagers still flee zombies.

**Do walls really matter?** Yes. Solid terrain muffles messages.

**Does it work with other mods?** Yes — walkie talkies, voice-chat mods, AI mods and modded creatures all run alongside it.

**Do commands attract mobs?** No — messages starting with `!`, `@`, `#` or `/` are never heard.

**Modpacks?** Yes, please — MIT License.

***

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
