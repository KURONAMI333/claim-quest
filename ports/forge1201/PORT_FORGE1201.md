# Claim Quest Forge 1.20.1 port

This isolated cell ports the published NeoForge 1.21.1 implementation to Forge 1.20.1. It keeps the task semantics, optional FTB runtime behavior, English/Japanese language keys, logo, license, and the FTB-backed GameTest scenarios.

## Verified API basis

The Forge artifacts were fetched from the official FTB releases Maven and inspected directly with `jar tf` / `javap`:

- FTB Quests Forge `2001.4.22`: `Task.writeData(CompoundTag)`, `Task.readData(CompoundTag)`, `FriendlyByteBuf` network hooks, `TaskTypes.register`, `ServerQuestFile.INSTANCE`, and `BaseQuestFile.collect(Class)` / `getOrCreateTeamData(Team)`.
- FTB Chunks Forge `2001.3.8`: `ClaimedChunkEvent.AFTER_CLAIM` has an `after(CommandSourceStack, ClaimedChunk)` callback; `ClaimedChunk.getTeamData()` and `ChunkTeamData.getClaimedChunks()` are present.
- FTB Teams Forge `2001.3.2`; FTB Library Forge `2001.2.13`.
- Forge 1.20.1 `47.4.13`, Java 17, Gradle 8.10.2.

The APIs intentionally differ from NeoForge: Forge 1.20.1 uses `FMLJavaModLoadingContext.get().getModEventBus()` from its no-argument `@Mod` constructor, and `mods.toml` uses `mandatory=true/false`. Minecraft NBT serialization has no `HolderLookup.Provider`, quest networking uses `FriendlyByteBuf`, `ServerQuestFile` exposes the singleton field, and GameTest players use the three-argument `ServerPlayer` constructor plus two-argument `PlayerList.placeNewPlayer`. The 1.20.1 pack format is 15 and its GameTest NBT folder is `structures/`; the empty template NBT stores DataVersion 3465.

## Validation state

- FTB artifacts fetched and bytecode signatures checked: done.
- Source/API and metadata port: prepared.
- Lightweight source scan: no NeoForge-only class/import names remain in `src/`.
- Local API-backed `javac --release 17 -proc:none` compile of every main and GameTest Java source: passed against the cached Forge 1.20.1/47.4.13 mapped jar and selected FTB Forge artifacts. This is a source compile only, not a Gradle build or a running GameTest. Captured command template, exact 2,945-entry classpath, and separate raw stdout/stderr logs are in `../../../verification/claimquest-forge1201/`.
- Gradle compile, GameTest server with FTB, and FTB-absent inert launch: pending shared Windows build allocation. Do not treat this source draft as an accepted release candidate until those complete.
- No publication, version bump, or release upload performed.

## Build tasks

```powershell
.\gradlew.bat --offline --no-daemon --max-workers=1 build
.\gradlew.bat --offline --no-daemon --max-workers=1 runGameTestServer
.\gradlew.bat --offline --no-daemon --max-workers=1 -PwithoutFtb runGameTestServer
```
