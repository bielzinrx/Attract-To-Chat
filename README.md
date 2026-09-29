<p align="center">
  <img src="https://i.imgur.com/smzhpln.png" alt="Attract to Chat" width="100%">
</p>

**Mobs can't read. But they can hear you type.**

<p align="center">
  <a href="https://github.com/bielzinrx/Attract-To-Chat/releases"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/forge_46h.png" alt="Supports Forge"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat/releases"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/fabric_46h.png" alt="Supports Fabric"></a>
</p>

**Forge · Fabric — server-side only. Players install nothing.**

You're caving. Good run — diamonds, iron, half a stack of lapis. A friend types a joke; you laugh and type back: *"HOLY CRAP THIS CAVE IS HUGE."*

The jokes stop. Two tunnels over, a zombie abandons its minecart heist mid-swing and starts walking toward the message.

![Nearby mobs turn toward the typed message and start walking to it](https://res.cloudinary.com/diexbbgwe/image/upload/v1790383471/core_mechanic_elqags.gif)

In **Attract to Chat**, chat is sound. Every message lands where you were standing when you typed it. Mobs hear it, walk toward that spot, and look around for whoever made the noise. That's the whole mod: one rule, no new blocks, no new items — your server's chat just has consequences now.

---

## ◈ Hearing rules

![A CAPS shout crosses the valley — mobs drop what they were doing and walk toward the typing](https://res.cloudinary.com/diexbbgwe/image/upload/v1790554965/hearing_rules.f300_taz2ed.gif)

* **Caps carry.** `HELP` travels farther than `help`, and `!!!` brings company faster. Shouting has consequences.
* **Walls muffle.** Sound is raycast block by block, mouth to ear: every solid block between you trims 12.5% of a message's reach, and eight of them cut it off entirely. A sealed shelter is a real shelter.
* **Mobs stay mobs.** No teleport-aggro, no stacked odds. A mob mid-fight keeps its real target, villagers still flee zombies, and a villager you've taught to listen wakes for a shout and nothing less. Vanilla AI stays vanilla — the world just... listens now.
* **Prefixes are silent.** Messages starting with `!`, `@`, `#` or `/` are never heard. You cannot lure with commands, and admin chat stays quiet.

---

## ◈ Not just a trap — a tool

The same rule that gets you killed can work for you.

![Villager herding: fifteen minutes pushing with job blocks, ten seconds with one sentence](https://res.cloudinary.com/diexbbgwe/image/upload/v1790554966/villager_utility.f300_uhx59k.gif)

* call villagers to the farm with a sentence. goodbye, boat method. *(one command first: `/atc entity add minecraft:villager` — the default listen-list is hostile mobs only, so villagers stay quiet until you say otherwise. Waking a whole village shouldn't be an accident. And if a villager already shuffles over on your server, someone added it there once: `/atc entity list` shows exactly who hears.)*
* type at the ridge, slip away while they check it out.
* turn chat itself into a game of stealth on survival and challenge servers.
* host a "no talking" night — and find out who breaks first.
* teach mobs from other mods to listen too — `/atc entity add` and they hear you.
* and yes: finally, a reason for the player who never stops shouting to whisper.

---

## ◈ Troll Mode

**The admin's favorite button.**

`/atc trollmode add <player>` secretly marks someone — `add @a` marks the whole server at once. From then on, everything they type carries farther and pulls harder — mobs quietly gravitate toward them. No glow, no warning, no explanation. Just a friend slowly realizing the horde always knows exactly where they are.

It only changes how loud their voice is — never their loot, their health, or their odds in a fight. Everything that happens to them could happen to anyone who shouts too hard.

They will never know why.

Need a mercy button? `/atc trollmode remove <player>` un-marks one player, and `/atc trollmode remove @a` hands anonymity back to everyone at once — both autocomplete.

![On a flat map: the marked player types one word and the horde reorganizes around them](https://res.cloudinary.com/diexbbgwe/image/upload/v1790383470/trollmode_demo_emkcwp.gif)

---

## ◈ Make it yours

Adjust how intense the world feels without touching the core mechanic — everything applies live, no restart.

* **Presets** — swap the whole vibe in one command: **Safe**, **Casual**, **Chaos** or **Silent** (`/atc preset set chaos`), or save and reuse your own. `/atc preset undo` when the chaos went further than planned.
* **Vocal fatigue** *(off by default)* — shout too much and you go hoarse: a short mute, and curious mobs coming to check on you. Milk clears it instantly, honey helps, death resets it.
* **Anti-spam** *(off by default)* — rapid-fire messages stop attracting mobs. Chat is never cancelled or hidden; it just stops being loud.

<details>
<summary><b>◈ Commands — full reference</b> (click to expand)</summary>

**Basics** *(everyone can run these)*

| Command | What it does |
|:--|:--|
| `/atc help [overview\|gameplay\|mobs\|admin\|feature\|config\|walkiechat\|client]` | In-game manual; categories autocomplete as you type. |
| `/atc status` | Live report: range, toggles, cooldowns, mob speed — plus the Walkie-Chat bridge state when the mod is present. |

**Tuning** *(needs op level 2, all applied live — no restart)*

| Command | What it does |
|:--|:--|
| `/atc debug [on\|off]` | Shows what the world heard and who came. Bare command toggles it. |
| `/atc preset set <safe\|casual\|chaos\|silent\|your own>` | The whole balance in one command. |
| `/atc preset undo` | Puts back exactly what the last preset changed. |
| `/atc preset custom save \| update \| delete \| rename <name>` · `/atc preset custom list` | Build and keep your own presets. |
| `/atc preset reset` · `/atc preset status` | Factory defaults / what's active right now. |
| `/atc config list` | Every option with current vs. default value. |
| `/atc config info <option>` | What an option does and its valid range — in game. |
| `/atc config hearingrange [0–500]` | How far a normal voice carries. Bare = current value. |
| `/atc config capsrangebonus [0–100]` | Extra blocks when you SHOUT. |
| `/atc config mobspeed base <0.5–2.0>` · `/atc config mobspeed max <0.5–3.0>` | How eagerly mobs walk toward a voice. |
| `/atc config forgettime <1–300>` *(seconds)* | How long they keep investigating before giving up. |
| `/atc config fatigue threshold <1–100000>` · `/atc config fatigue muteduration <1–3600>` *(seconds)* | When fatigue bites, and how long the hoarse phase lasts. |
| `/atc feature caps [enable\|disable]` | The CAPS mechanic itself; bare = status. |
| `/atc feature fatigue enable\|disable` | Vocal fatigue on/off (clears state on off). |
| `/atc feature antispam enable\|disable\|status` | The burst guard, with live feedback. |
| `/atc feature antispam cooldown <0–60>` *(seconds)* | Rest between scans per player (enforced while anti-spam is on). |
| `/atc feature antispam window <max 0–50> <seconds 1–120>` | Messages per window before the tap quiets. |

**People & creatures** *(op level 2 unless noted)*

| Command | What it does |
|:--|:--|
| `/atc ignore add\|remove <player\|@a>` | Voices the world never hears. `@a` mutes everyone at once and calms current investigations. |
| `/atc trollmode add\|remove <player\|@a>` · `/atc trollmode list` | The secret mark, its audit list — `@a` marks or clears everyone at once. |
| `/atc entity add\|remove <entity id>` · `/atc entity list` | Exactly which mobs — modded ones included — can hear you. Default: the 24 vanilla hostiles; add anyone else by name. |
| `/atc client particles [enable\|disable]` | *(player command, needs the optional client)* your personal particle trail; bare shows status. |

**Walkie-Chat** *(only visible when Walkie-Chat is installed; op level 2)*

| Command | What it does |
|:--|:--|
| `/atc walkiechat compat enable\|disable` | The bridge, live. |
| `/atc walkiechat proximity range <0–500>` · `/atc walkiechat proximity capsbonus <0–100>` | A floor for handheld walkie voices — it applies when Walkie-Chat reports its own proximity range, which not every Walkie-Chat version does. |

Anything with `<arguments>` autocompletes in game — you never need to memorize this table.

</details>

<details>
<summary><b>◈ Every config option</b> (defaults included)</summary>

The file lives at `config/attracttochat-common.json` on the server side. Every value carries a one-line `#` comment written by the mod — edit freely; it all applies live.

| Option | Default | In one line |
|:--|:--|:--|
| `hearingRange` | 30 | How far a normal message travels, in blocks. |
| `capsRangeBonus` | 5 | Extra reach when you SHOUT. |
| `enableCapsFeature` | on | Turn CAPS sensitivity on or off entirely. |
| `mobSpeedBase` / `mobSpeedMax` | 1.2 / 2.0 | How eagerly mobs walk toward a voice. |
| `forgetTargetAfterSeconds` | 20 | How long they keep investigating before giving up. |
| `scanCooldownTicks` | 40 | Rest between attraction scans *per player*. Enforced while anti-spam is on; with anti-spam off, every accepted message runs one scan. |
| `enableVocalFatigue` | off | Shout too much → short mute + curious visitors. |
| `muteDurationTicks` | 600 | How long the hoarse phase lasts (ticks; 20 = 1 s). |
| `traumaThreshold` | 1000 | Fatigue buildup needed before it bites; decays with calm chat. |
| `enableAntiSpam` | off | Rapid-fire messages stop attracting. |
| `antiSpamMaxMessages` / `antiSpamWindowSeconds` | 3 / 8 | The spam line, and how many seconds it covers. |
| `walkieChatCompat` | on | The Walkie-Chat bridge. Does nothing at all unless Walkie-Chat is installed. |
| `walkieChatProximityRange` / `walkieChatProximityCapsBonus` | 15 / 10 | Floor for handheld walkie voices: Walkie-Chat asks ATC for it before reporting its own range. |
| `debugMode` | off | What `/atc debug` flips. |

Nothing here is hidden — `/atc config info <option>` explains every one of them in game too.

</details>

<details>
<summary><b>◈ Compatibility</b> — tested pairings & how it behaves with others</summary>

* **Walkie-Chat** ([by Theus452, Modrinth](https://modrinth.com/mod/walkie-chat)) — Walkie-Talkies talk, mobs listen. A voice on the air is heard where you stand, and a message Walkie-Chat already delivered is never counted twice.
* Talk on a Walkie-Talkie beside a Walkie Block set to the same frequency and the horde comes for
  the relay instead of you. Plain chat always brings them to you — typing next to a block does not
  hand it to the mob.
* A station under attack is chipped down with the game's own cracking progress, one mob at a time,
  so a crowd takes turns: a slime hops on top of it, a creeper finishes it with the last tick of
  its fuse. The wreck drops nothing, and everyone tuned to that channel hears it die over the
  walkie, naming whatever did it.
* The bridge is optional, on by default, and off with one command (`/atc walkiechat compat disable`). `/atc status` always tells you what's live.
* **Voice chat mods** — Attract to Chat listens to typed words, not microphone audio. Same server, no overlap.
* **AI / combat mods** — the investigation is one more entry in the mob's own goal list, claiming only movement and look. It never touches the attack target: a mob fighting you keeps fighting you, and it gives up the moment vanilla would.
* **Mob-adding mods** — by default, only the 24 vanilla hostiles hear you. That is deliberate: a 300-mod pack should not gain new aggro by accident. When you *want* a mob to listen, `/atc entity add <modid:name>` makes it hear, one decision at a time.
* **Minimaps, inventory UIs, shaders, performance mods** — nothing to conflict with: no render hook, no GUI, no keybind, no entity models. The client half is one class that sends a single "I'm here" packet on join.
* **Chat mods that block or cancel messages** — a message cancelled before it reaches chat is not a sound: mobs never heard it, and never will. One deliberate exception: while the Walkie-Chat bridge is on, ATC re-reads cancelled chat itself, because Walkie-Chat cancels every message it relays — the bridge's own checks then decide what actually attracts.

</details>

---

## ◈ For server owners

**Server-side only** — drop it on the server, players install nothing. Vanilla and third-party clients join normally and never see a missing-mod warning; the optional client only *adds* the personal particle trail, and it's opt-in per player.

* `/atc debug` shows you exactly what the mobs heard — and where they went.
* `/atc ignore add <player>` is for the guy who found the loophole.
* Everything the mod says to a player is translated **on the server**, in that player's own language. No client mod needed, no missing translations — built for mixed BR/EU/US servers.

---

## ◈ Start in 3 lines

1. Grab the jar matching your **Minecraft version and loader** from [Releases](https://github.com/bielzinrx/Attract-To-Chat/releases) (or [Modrinth](https://modrinth.com/mod/attract-to-chat) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/attract-to-chat)).
2. Drop it in your **server's** `mods` folder. Fabric versions pair with Fabric API, Forge versions run standalone — either way, never mix loaders.
3. Boot it up. Say something. Watch what answers.

---

<details>
<summary><b>◈ FAQ</b></summary>

**Do players need the mod installed?**

No — the magic runs on the server.

**Solo too?**

Yes. A singleplayer world is a server too; the admin commands just need cheats enabled, and status/help/particles work regardless.

**Does it replace Minecraft's mob AI?**

No — the investigation is one goal among many; mobs in combat keep their real target, and eight solid blocks still keep you safe.

**Do walls really matter?**

Yes — block by block: 12.5% of a message's reach per solid block, and eight of them silence it entirely.

**Does it work with other mods?**

Yes — the Compatibility box above lists what is actually tested, and where behavior could surprise you (new mobs are silent until you add them).

**Can I put it in a modpack?**

Yes, please — MIT, no need to ask.

</details>

---

## ◈ Building from source

Architectury multi-loader project — `./gradlew build` in the branch named after your target Minecraft version. PRs welcome; if it changes gameplay behavior, open an issue first.

---

<p align="center">
  <strong>Say one word in a cave on your server tonight, and count what answers.</strong><br><br>
  <a href="https://ko-fi.com/bielzinrx"><img src="https://img.shields.io/badge/Ko--fi-Buy_me_a_coffee-FF5E5B?style=flat-square&logo=kofi&logoColor=white" alt="Ko-fi"></a>
</p>

<p align="center">
  <strong>MIT — free in any modpack, no need to ask. Credit appreciated, not required.</strong><br>
  Bugs go to the <a href="https://github.com/bielzinrx/Attract-To-Chat/issues">issue tracker</a>  · ideas and questions too.<br><br>
  <a href="https://github.com/bielzinrx/Attract-To-Chat"><img src="https://img.shields.io/badge/GitHub-Source-181717?style=flat-square&logo=github&logoColor=white" alt="Source"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/attract-to-chat"><img src="https://img.shields.io/badge/CurseForge-Downloads-f16436?style=flat-square&logo=curseforge&logoColor=white" alt="CurseForge"></a>
  <a href="https://www.planetminecraft.com/mod/attract-to-chat-mob-attraction-by-chat-messages/"><img src="https://cdn.jsdelivr.net/gh/VoxelForge-oss/voxicons@main/badges-248/badges/planet-minecraft.png" width="78" height="20" alt="Planet Minecraft"></a><br>
  <a href="https://url-shortener.curseforge.com/zFhxc"><img src="https://img.shields.io/badge/BisectHosting-Get_25%25_OFF-FF6C2F?style=flat-square" alt="BisectHosting 25% Off"></a>
</p>

<p align="center"><em>Created by bielzinrx · Walkie-Chat by Theus452 · Tested by kots_luffyzin</em></p>
