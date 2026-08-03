# Attract to Chat 2.1.0

## Overview

Attract to Chat 2.1.0 expands the original Forge-only implementation into a shared Forge/Fabric codebase for Minecraft 1.19.2 and 1.20.1. The update replaces permanent target assignment with a temporary sound-investigation system designed to cooperate with existing mob AI.

## Platform and architecture

- Added Forge and Fabric builds for Minecraft 1.19.2 and 1.20.1.
- Added shared `common`, `forge`, and `fabric` modules.
- Added platform abstractions for loader detection, entity registries, configuration paths, client presence, and goal access.
- Replaced the old Forge TOML configuration with `config/attracttochat-common.json`.
- Added live configuration reload, value validation, migration support, temporary-file writes, and atomic replacement when supported.
- Added runtime cleanup for removed mobs, stopped servers, disconnected players, and changed entity rules.

## Chat sound engine

- Added message scoring based on uppercase characters, uppercase saturation, and exclamation marks.
- Added a runtime toggle for the complete CAPS feature.
- Added configurable base hearing range and direct uppercase range bonus.
- Added terrain muffling through solid blocks.
- Added safely clamped hearing ranges, target limits, loaded-chunk checks, and destination validation.
- Added temporary investigation instead of permanent aggression.
- Messages beginning with `!`, `@`, `#`, or `/` do not trigger attraction.
- Existing combat and survival behavior takes priority over chat investigation.

## Mob behavior

- Added registry-based entity configuration for vanilla and modded mobs.
- Added autocomplete from the active entity registry.
- Added allow-list and exclusion-list entity modes.
- Added dedicated handling for flying navigation and slime movement.
- Added loud-message reactions for Endermen.
- Sleeping villagers can wake for sufficiently loud messages.
- Villagers immediately prioritize escaping zombies over investigating chat.
- Unsafe, unloaded, unreachable, and invalid destinations are rejected.

## Administration

- Replaced the old separate `/attract...` commands with the unified `/atc` command tree.
- Added categorized help and runtime status output.
- Added independent debug output in the actionbar and server log.
- Added live feature toggles for CAPS, vocal fatigue, and anti-spam.
- Added runtime configuration for range, speed, cooldowns, fatigue limits, particles, and entity behavior.
- Added player ignore rules and persistent global ignore through `@a`.
- Added per-player Troll Mode.

## Presets

- Added built-in `safe`, `casual`, `chaos`, and `silent` presets.
- Added persistent custom presets created from the current server configuration.
- Custom presets include the complete enabled-entity list.
- Added save, update, rename, delete, list, apply, status, reset, and autocomplete support.
- Added smart undo that restores only preset-managed values and preserves later manual edits whenever possible.
- Preset restore data persists across server restarts.

## Vocal fatigue and anti-spam

- Reworked vocal fatigue into server-side player data without a registered custom MobEffect.
- Added persistent fatigue and mute duration across logout and login.
- Honey reduces fatigue and milk clears fatigue and active vocal lock.
- Added optional scan cooldown and sliding-window anti-spam.
- Anti-spam affects attraction scans without deleting or hiding chat messages.

## Particles and client behavior

- Added path-based vanilla particles for mob investigations.
- Core gameplay remains server-first and does not require the mod on clients.
- An optional client installation allows each player to disable investigation particles for themselves.

## Removed or corrected

- Removed the old registered Vocal Fatigue effect and its registry synchronization requirements.
- Removed the obsolete generic feedback toggle and messages.
- Debug output no longer depends on ordinary player feedback settings.
- Corrected villager investigation priority around zombies.
- Corrected preset restoration and custom entity-list persistence.
