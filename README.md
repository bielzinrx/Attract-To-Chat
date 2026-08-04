<p align="center">
  <img src="https://i.imgur.com/3U9J050.png" alt="Attract to Chat" width="100%">
</p>

<p align="center">
  <a href="https://modrinth.com/mod/attract-to-chat"><img src="https://img.shields.io/modrinth/dt/attract-to-chat?style=flat-square&logo=modrinth&logoColor=white&label=Modrinth&color=1bd96a" alt="Modrinth Downloads"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/attract-to-chat"><img src="https://img.shields.io/curseforge/dt/1327186?style=flat-square&logo=curseforge&logoColor=white&label=CurseForge&color=f16436" alt="CurseForge Downloads"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat/releases"><img src="https://img.shields.io/github/v/release/bielzinrx/Attract-To-Chat?style=flat-square&label=Latest&color=blue" alt="Latest Release"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat/blob/1.20.1/LICENSE"><img src="https://img.shields.io/github/license/bielzinrx/Attract-To-Chat?style=flat-square&color=lightgrey" alt="License"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Forge%201.20.1-Stable-b07219?style=flat-square&logo=curseforge&logoColor=white" alt="Forge 1.20.1 Stable">
  <img src="https://img.shields.io/badge/Fabric%201.20.1-In%20Development-d6a85f?style=flat-square" alt="Fabric 1.20.1 In Development">
  <img src="https://img.shields.io/badge/Forge%201.19.2-In%20Testing-8a6d4b?style=flat-square&logo=curseforge&logoColor=white" alt="Forge 1.19.2 In Testing">
  <img src="https://img.shields.io/badge/Fabric%201.19.2-In%20Testing-6f7f5d?style=flat-square" alt="Fabric 1.19.2 In Testing">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Side-Server--First-1a6b8a?style=flat-square" alt="Server First">
  <img src="https://img.shields.io/badge/Client-Optional-5965d9?style=flat-square" alt="Optional Client">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="Java 17">
</p>

<p align="center">
  <strong>Current stable release: Minecraft 1.20.1 · Forge</strong><br>
  Fabric 1.20.1 and both Minecraft 1.19.2 builds are undergoing final compatibility testing.
</p>

---

**Attract to Chat** turns Minecraft chat into a gameplay mechanic.

When a player sends a message, configured mobs can go to the exact location where the message was sent. Uppercase letters and exclamation marks make the message louder.

<p align="center">
  <img src="https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/chat_attraction_bri8qk.gif" alt="Chat attraction demonstration" width="80%">
</p>

---

## ◈ How It Works

Each accepted chat message creates a temporary sound source at the sender's position. The mod evaluates:

- the configured base hearing range;
- uppercase letters and exclamation marks;
- whether the mob is already fighting, fleeing, or handling a higher-priority task;
- whether the entity is enabled in the server configuration;
- cooldown, anti-spam, ignore, and player-specific rules.

Messages beginning with `/`, `!`, `@`, or `#` are ignored by the attraction engine, so commands and selected chat formats stay silent.

**CAPS loudness** — Uppercase text increases the effective hearing range and investigation speed. The full CAPS system is optional and can be toggled at runtime.

**Temporary investigation** — Mobs investigate the message location instead of receiving a permanent target. Investigation ends when the destination becomes invalid, the timer expires, or a more important AI behavior takes priority.

<p align="center">
  <img src="https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/caps_dnnpi4.gif" alt="Normal chat versus CAPS loudness" width="80%">
</p>

---

## ◈ AI-Aware Reactions

Attract to Chat cooperates with Minecraft's existing mob AI instead of replacing it.

- Mobs keep real combat targets instead of abandoning a fight for chat noise.
- Villagers prioritize escaping zombies over investigating messages.
- Sleeping villagers can wake when they hear a sufficiently loud message.
- Flying mobs receive air-compatible destinations.
- Slimes use movement behavior suited to their navigation.
- Endermen can react differently to sufficiently loud shouts.
- Unsafe, unloaded, or unreachable destinations are rejected.
- Active investigations are cancelled when a higher-priority survival behavior appears.

<p align="center">
  <img src="https://res.cloudinary.com/diexbbgwe/image/upload/v1785719395/villager_xfwilc.gif" alt="Villager survival priority demonstration" width="80%">
</p>

---

## ◈ Vanilla and Modded Entities

The default configuration includes a curated selection of hostile mobs. Admins can add or remove any compatible mob, including entities added by other mods.

```text
/atc entity add <modid:entity>
/atc entity remove <modid:entity>
/atc entity list
```

Autocomplete reads the server's registry. Non-mob entities without usable pathfinding — projectiles, dropped items, displays, vehicles — are filtered out.

An exclusion-style entity list is also supported: when every configured entry begins with `!`, all compatible mobs are enabled except the listed ones.

---

## ◈ Path Feedback

Optional vanilla particles can display the path a mob is following toward the message source — useful for testing or playing with visible feedback.

Particles are disabled by default. They only become available to a player when:

1. Attract to Chat is installed on the server;
2. the matching Forge 1.20.1 JAR is also installed on that player's client;
3. the player enables particles manually.

```text
/atc client particles enable
/atc client particles disable
```

Clients without Attract to Chat can join normally and never receive these particles. The `/atc client` command only appears after the server confirms that the player has the optional client installation.

<p align="center">
  <img src="https://res.cloudinary.com/diexbbgwe/image/upload/v1785719397/particles_zml8qn.gif" alt="Mob investigation path particles" width="80%">
</p>

---

## ◈ Optional Vocal Fatigue

Adds a consequence for repeatedly sending loud messages. Loud chat accumulates server-side strain; reaching the configured threshold temporarily prevents the player from sending chat. Fatigue and mute time persist through logout and login.

- Death clears the current fatigue state.
- Threshold and mute duration are configurable.
- Disabled by default — no custom status effect required.

```text
/atc feature fatigue enable
/atc feature fatigue disable
/atc feature fatigue status
```

---

## ◈ Optional Anti-Spam

Limits how often chat can trigger new attraction scans without hiding or deleting the player's message.

- Minimum cooldown between accepted scans
- Sliding message window
- Configurable limits and duration
- Runtime status and configuration commands

```text
/atc feature antispam enable
/atc feature antispam disable
/atc feature antispam status
```

---

## ◈ Ignore Rules

Ignored players do not generate attraction events.

```text
/atc ignore add <player>
/atc ignore remove <player>
```

Use `@a` to apply or remove a global rule, including players who join later:

```text
/atc ignore add @a
/atc ignore remove @a
```

Applying an ignore rule also clears any active investigations it affects.

---

## ◈ Troll Mode

An admin-controlled chaos profile for selected players.

```text
/atc trollmode add <player>
/atc trollmode remove <player>
/atc trollmode list
```

For affected players, the mod can use a larger hearing range, faster investigation, direct player pursuit, and different anti-spam behavior. Troll Mode is player-specific and never changes the server globally.

<p align="center">
  <img src="https://res.cloudinary.com/diexbbgwe/image/upload/v1785719396/troll_opvctf.gif" alt="Troll Mode demonstration" width="80%">
</p>

---

## ◈ Presets

Built-in presets for different server styles:

| Preset | Intended use |
| --- | --- |
| `safe` | Lower-risk attraction for relaxed survival. |
| `casual` | Balanced settings for general multiplayer. |
| `chaos` | High range, strong CAPS impact, rapid reactions. |
| `silent` | Normal attraction behavior without server path particles. |

```text
/atc preset set <safe|casual|chaos|silent>
```

**Custom presets** capture the current preset-managed settings and the full enabled-entity list.

```text
/atc preset custom save <name>
/atc preset custom update <name>
/atc preset custom rename <old> <new>
/atc preset custom delete <name>
/atc preset custom list
```

Apply a custom preset the same way as a built-in one: `/atc preset set <name>`.

**Undo and reset**

```text
/atc preset undo    # restores the state recorded before the latest preset application
/atc preset reset   # restores only preset-managed gameplay values
/atc preset status
```

`undo` preserves manual changes made afterward whenever possible. `reset` does not erase ignored players, Troll Mode assignments, or saved custom presets. Preset data and undo state persist across server restarts.

---

## ◈ Server and Client Requirements

Attract to Chat is **server-first**.

- The current stable release supports **Minecraft 1.20.1 with Forge**.
- Install the Forge 1.20.1 build on the server.
- The attraction engine, AI behavior, configuration, fatigue, anti-spam and presets run server-side.
- Clients do not need the mod for core gameplay.
- Installing the same JAR on the client is optional and enables personal particle controls.
- Java 17 is required.

| Loader | Minecraft version | Status |
| --- | :---: | :---: |
| Forge | 1.20.1 | **Stable** |
| Fabric | 1.20.1 | Development and compatibility testing |
| Forge | 1.19.2 | Development and compatibility testing |
| Fabric | 1.19.2 | Development and compatibility testing |

Development source may be visible in the repository before a public build is released. Only files attached to the latest GitHub release, CurseForge project or Modrinth project should be treated as supported downloads.

---

## ◈ Installation

1. Download `Attract-To-Chat-1.20.1-Forge-2.1.0.jar`.
2. Drop the `.jar` into your Forge 1.20.1 server's `mods` folder.
3. Start the server once to generate the configuration.
4. Use `/atc help` in-game, or edit the generated config file directly.

The shared configuration lives at:

```text
config/attracttochat-common.json
```

Command changes are saved atomically. Valid manual edits are detected and applied live while the server is running; invalid edits are rejected without replacing the last valid runtime configuration.

### Optional client installation

Players may install the same Forge 1.20.1 JAR on their clients to unlock personal particle controls.

Core gameplay does not require a client installation.

---

## ◈ Command Overview

All features are organized under `/atc`.

| Command | Access | Purpose |
| --- | --- | --- |
| `/atc help [category]` | Everyone | Contextual in-game command reference. |
| `/atc status` | Everyone | Current gameplay configuration summary. |
| `/atc client particles ...` | Player with optional client install | Personal particle preference. |
| `/atc debug ...` | Operator | Runtime diagnostics in the actionbar and server log. |
| `/atc feature caps ...` | Operator | CAPS behavior controls. |
| `/atc feature fatigue ...` | Operator | Vocal Fatigue controls. |
| `/atc feature antispam ...` | Operator | Scan-rate controls. |
| `/atc entity ...` | Operator | Vanilla and modded entity management. |
| `/atc ignore ...` | Operator | Player or global immunity rules. |
| `/atc trollmode ...` | Operator | Player-specific chaos profile. |
| `/atc preset ...` | Operator | Built-in presets, custom presets, undo, and reset. |
| `/atc config ...` | Operator | Live gameplay configuration. |

Use `/atc help` for the full command tree supported by the installed build.

---

## ◈ Development Status

The repository remains structured as a multi-loader Architectury project.

- `1.20.1` — stable source line used by the current Forge 1.20.1 release.
- `dev/fabric-1.20.1` — ongoing Fabric 1.20.1 networking and chat compatibility work.
- `1.19.2` — ongoing Forge and Fabric 1.19.2 compatibility work.

The presence of a loader module or development branch does not mean a supported binary is currently available. Public support is determined by the files attached to an official release.

---

## ◈ Support the Project

<p align="center">
  <a href="https://url-shortener.curseforge.com/zFhxc"><img src="https://img.shields.io/badge/BisectHosting-Get_25%25_OFF-FF6C2F?style=flat-square&logoColor=white" alt="BisectHosting 25% Off"></a>
  <a href="https://ko-fi.com/bielzinrx"><img src="https://img.shields.io/badge/Ko--fi-Support_the_project-FF5E5B?style=flat-square&logo=kofi&logoColor=white" alt="Ko-fi"></a>
</p>

<p align="center">
  <a href="https://github.com/bielzinrx/Attract-To-Chat/issues"><img src="https://img.shields.io/badge/GitHub-Report_a_Bug-181717?style=flat-square&logo=github&logoColor=white" alt="Report a Bug"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat/tree/1.20.1"><img src="https://img.shields.io/badge/GitHub-Stable_Source-181717?style=flat-square&logo=github&logoColor=white" alt="Stable Source"></a>
  <a href="https://github.com/bielzinrx/Attract-To-Chat/tree/dev/fabric-1.20.1"><img src="https://img.shields.io/badge/GitHub-Fabric_Development-181717?style=flat-square&logo=github&logoColor=white" alt="Fabric Development"></a>
  <a href="https://modrinth.com/mod/attract-to-chat"><img src="https://img.shields.io/badge/Modrinth-Project_Page-1bd96a?style=flat-square&logo=modrinth&logoColor=white" alt="Modrinth"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/attract-to-chat"><img src="https://img.shields.io/badge/CurseForge-Project_Page-f16436?style=flat-square&logo=curseforge&logoColor=white" alt="CurseForge"></a>
  <a href="https://www.planetminecraft.com/mod/attract-to-chat-mob-attraction-by-chat-messages/"><img src="https://img.shields.io/badge/Planet_Minecraft-Project_Page-6ca740?style=flat-square&logo=minecraft&logoColor=white" alt="Planet Minecraft"></a>
</p>

<p align="center">
  Created by <strong>bielzinrx</strong> · Contributor <strong>Theus452</strong> · Tester <strong>kots_luffyzin</strong>
</p>
