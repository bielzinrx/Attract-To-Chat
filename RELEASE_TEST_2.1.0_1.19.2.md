# Attract to Chat 2.1.0 — Minecraft 1.19.2 Release Test

Run this checklist once on Fabric and once on Forge. Use Java 17, a clean test world, the matching ATC JAR, and Fabric API 0.76.0 or newer on Fabric.

## Automated validation already completed

- [x] `./gradlew clean build` completed for the 1.19.2 multi-loader project.
- [x] Common automated tests passed.
- [x] Fabric dedicated server reached `Done`, created schema 15 config, registered commands, and stopped cleanly.
- [x] Forge dedicated server reached `Done`, created schema 15 config, registered commands, and stopped cleanly.
- [x] Forge Prism client test confirmed that typing, editing with Backspace, and canceling chat produce zero attraction scans.
- [x] Forge Prism client test confirmed that pressing Enter once produces exactly one attraction scan on the server thread.
- [x] `/atc config particles enable` was rejected on both dedicated servers.
- [x] No Walkie-Chat reference was found in source or final JARs.

## Chat

- [x] Open chat and type several characters without pressing Enter. No mob may react and no attraction/debug scan may be logged while the text is only in the input box.
- [x] Press Enter once after typing. The submitted message must produce exactly one attraction scan.
- [ ] Send `hello`. A nearby enabled mob investigates the position where the message was sent.
- [ ] Send `HELLO`. It reaches farther and produces a stronger/faster reaction than lowercase text.
- [ ] Send `hello!!`. Exclamation marks increase the reaction even when CAPS is disabled.
- [ ] Try to send an empty or whitespace-only message. It must not create an investigation.
- [ ] Send `/say hello`, `/me hello`, and a normal command. Each command must be processed once as a command and must not create a normal chat attraction event.
- [ ] Send messages beginning with `!`, `@`, and `#`. They must remain silent to ATC.
- [ ] Place equal mobs at similar distances in open terrain and behind solid walls. The obstructed mob must have reduced effective hearing through Terrain Muffling.
- [ ] Enable debug and send one message. Confirm that one message produces one attraction scan, without duplicated diagnostics.

## Mobs

- [ ] Test a zombie or another terrestrial hostile. It reaches a valid ground destination near the message.
- [ ] Test a villager in safety. It investigates normal chat.
- [ ] Put a zombie near the villager, then send chat. The villager prioritizes fleeing the zombie.
- [ ] Give an enabled hostile mob a live combat target, then send chat. Combat remains the priority.
- [ ] Test a slime or magma cube. It turns and jumps toward the destination without freezing.
- [ ] Test an aquatic mob in water. It uses a water-compatible path and does not seek invalid ground.
- [ ] Test a flying mob such as a ghast, phantom, blaze, or vex. It receives an air-compatible destination.
- [ ] Shout near an Enderman. Any teleport lands in a safe, loaded position.
- [ ] Send chat above a drop of more than four blocks. A ground mob resolves a standable floor below when it is inside the hearing range.
- [ ] Send chat over an unloaded, blocked, or unreachable destination. ATC skips it safely without a crash or permanent stuck goal.
- [ ] If another entity mod is available, add one compatible mob with `/atc entity add <modid:entity>` and test attraction.
- [ ] Kill or unload an investigating mob. Its ATC state is cleaned up without an error.

## Commands

- [ ] Run `/atc help` and each suggested category. No raw translation key appears.
- [ ] Verify autocomplete for commands, presets, players, and registered entity IDs.
- [ ] Run `/atc feature caps enable`, `disable`, and `status`; repeat the current state and confirm the “already enabled/disabled” feedback.
- [ ] Add and remove one player with `/atc ignore`; verify that only that player becomes silent.
- [ ] Add and remove `@a`; verify that current and newly joined players are ignored while it is active.
- [ ] Add and remove one player from Troll Mode; verify increased range, speed, and target lock only for that player.
- [ ] Apply `safe`, `casual`, `chaos`, and `silent`; verify `/atc preset status`, `undo`, and `reset`.
- [ ] Save, update, rename, list, apply, and delete one custom preset.
- [ ] Enable Vocal Fatigue, reach its threshold, verify the temporary chat lock, then disable it.
- [ ] Enable Anti-Spam, test cooldown and window limits, then disable it.
- [ ] Toggle debug and verify readable actionbar/server diagnostics with formatted coordinates.
- [ ] Confirm `/atc client particles enable|disable` exists only when the player has the optional client mod.
- [ ] Confirm `/atc config particles` does not exist and is not suggested.

## Optional client

- [ ] Join a Fabric ATC server with a vanilla Fabric client that does not have ATC. Connection and core gameplay must work.
- [ ] Join a Forge ATC server with a vanilla Forge client that does not have ATC. Connection and core gameplay must work.
- [ ] On a client without ATC, run public commands and trigger fatigue/anti-spam feedback. All messages must be readable, with no raw translation key.
- [ ] Join Fabric with ATC installed on the client. The `client` help category and personal particle command appear after presence registration.
- [ ] Join Forge with ATC installed on the client. The `client` help category and personal particle command appear after presence registration.
- [ ] With two players, enable particles for only one. Only the opted-in player sees investigation paths.
- [ ] Run `/atc client particles disable`, reconnect, and verify that the preference remains disabled.
- [ ] Confirm that the server never sends a required ATC packet to a client without the mod.

## Configuration and persistence

- [ ] Start with no config. `config/attracttochat-common.json` is created with `configVersion: 15`.
- [ ] Change range, CAPS, speed, ignore rules, Troll Mode, features, and presets through commands; confirm the file is saved.
- [ ] Make a valid manual edit while the server is running. It is applied live.
- [ ] Make an invalid manual edit. The last valid runtime configuration remains active and the invalid file does not erase it.
- [ ] Restart the server. Config values, custom presets, undo state, ignore rules, Troll Mode, fatigue state, and personal particle choices persist where applicable.
- [ ] Confirm no `walkie`, `Walkie-Chat`, `BLOCK_RECEPTION`, or `PROXIMITY_CHAT` entry appears in config, commands, logs, or dependencies.

## Release sign-off

- [ ] Fabric gameplay checklist passed.
- [ ] Forge gameplay checklist passed.
- [ ] Vanilla-client compatibility passed on both loaders.
- [ ] Optional-client presence and particle opt-in passed on both loaders.
- [ ] No unresolved crash, duplication, stuck investigation, raw translation key, or config-loss bug remains.
