<!-- Summary 正本は _docs/STORE_SUMMARY_EN.txt。公開時はこのコメント行を本文に含めない -->
<!-- GitHub URL は公開予定の意図URL。repo 未作成の段階では 404 -->

A new task type in the FTB Quests editor: **Claim Chunks** completes when your team's claimed-chunk total reaches the target — so "claim 25 chunks" quests finally work without command-block plumbing.

FTB Quests ships no task that watches how much land a team has claimed with FTB Chunks. Pack makers end up faking it with item tasks or external triggers. This addon bridges the two mods: every claim event updates every matching task automatically.

**Features**

- Adds **Claim Chunks** to the FTB Quests task list — pick it in the quest editor, set *Chunks to claim*, done
- Progress follows the owning team's live claimed-chunk total, credited on every claim
- High-water mark: progress only ever rises. Unclaiming and re-claiming cannot farm it, and a completed task stays completed
- Team-based, not player-based: a claim by any member credits the team that owns the chunk
- The server tracks progress; install the mod on the client too so the custom task type appears correctly in the FTB Quests editor and task display. No items, blocks, or config file are added

**For quest makers**

- In the quest editor, add a task and choose **Claim Chunks**
- Set *Chunks to claim* (`value`) to the target count — e.g. `25`
- The task tracks the team's *current* total, not claims made while the quest was active: a team already holding 30 chunks completes a `25`-chunk task on its next claim

**Dependencies**

- [FTB Quests](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge) — required for the mod to do anything. Without it the jar still loads, but registers nothing
- [FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) — required for progress. Without it the task type exists in the editor but never advances
- [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams) + [FTB Library](https://www.curseforge.com/minecraft/mc-mods/ftb-library-forge) — pulled in by the two above; install them the way you normally would
- All four FTB mods are published on CurseForge only, so a Modrinth-only setup cannot resolve them

**Scope and limitations**

- Claims are counted through FTB Chunks only; other claim/protection mods are not consulted
- Unclaims never reduce progress — by design, so territory quests cannot be gamed or pushed backward
- Single player is simply a one-person team and works the same way

<p><a href="https://www.patreon.com/KURONAMI333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/patreon.png" width="440" height="156" alt="Support my mods on Patreon"></a> <a href="https://x.com/kuronami333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/x.png" width="300" height="156" alt="Follow @kuronami333 on X"></a></p>

Bugs and questions: comment on the CurseForge page, or DM @kuronami333 on X.

All Rights Reserved. Free to put in any modpack, on any platform, monetised or not - no permission needed, no credit required. Source: https://github.com/KURONAMI333/claim-quest
