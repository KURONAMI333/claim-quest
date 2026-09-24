# Claim Quest

> Adds a **Claim Chunks** task type to FTB Quests. It progresses as your team's claimed-chunk count grows, so quest makers can reward territory expansion without any command-block plumbing. Server-side bridge; no content of its own.

[![License: All Rights Reserved](https://img.shields.io/badge/License-All_Rights_Reserved-red.svg)](LICENSE)
Homepage (intended CurseForge page, not yet published): https://www.curseforge.com/minecraft/mc-mods/claim-quest
Bugs and questions: comment on the CurseForge page, or DM @kuronami333 on X (https://x.com/kuronami333).

---

## Supported Loaders / Versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | — | — |

Requires **FTB Quests** to do anything useful and **FTB Chunks** (+ FTB Teams) to make progress. Both are declared optional: without FTB Quests the mod loads but registers nothing; without FTB Chunks the task type exists but never progresses.

---

## Why?

FTB Quests ships no task type that tracks how much land a team has claimed with FTB Chunks. Pack makers who want a "claim 25 chunks" quest end up faking it with item tasks or command rewards. This mod adds the missing task type directly into the quest editor.

## How it works

- Registers a `claimquest:claim` task type with FTB Quests at common setup.
- Listens to FTB Chunks' `AFTER_CLAIM` event. On every claim it reads the owning team's current claimed-chunk total and feeds it to every `claimquest:claim` task that team can progress.
- Progress is a high-water mark: it only ever rises toward the target. Unclaiming and re-claiming the same chunk cannot farm progress, and once FTB Quests marks the task complete it stays complete.
- Claims by any team member credit the team that owns the chunk, not the individual player.

## For quest makers

1. Install this mod alongside FTB Quests, FTB Chunks, FTB Teams, and FTB Library.
2. In the quest editor, add a task and choose **Claim Chunks**.
3. Set **Chunks to claim** (`value`) to the target count, e.g. `25`.

The task tracks the team's *current* claimed-chunk total, not "claims made while the quest was active" — a team that already holds 30 chunks completes a `value: 25` task on the next claim event.

## For pack makers

- The mod does nothing without FTB Quests loaded, and the task cannot progress without FTB Chunks. Shipping it in a pack without either is harmless but pointless.
- There are no config options, no items, no blocks, and no client-side behaviour.

## Development

```bash
./gradlew build
```

Headless verification against real FTB jars:

```bash
./gradlew runGameTestServer          # FTB present
./gradlew -PwithoutFtb runGameTestServer  # FTB absent, mod must stay inert
```

## License

[All Rights Reserved](LICENSE) — modpack inclusion welcome, no credit required.

## Credits

- Author: KURONAMI
