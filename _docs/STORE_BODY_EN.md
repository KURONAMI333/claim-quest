<!-- Summary 正本は _docs/STORE_SUMMARY_EN.txt。公開時はこのコメント行を本文に含めない -->
<!-- GitHub URL は公開予定の意図URL。repo 未作成の段階では 404 -->

FTB Quests has no task type that tracks how much land a team has claimed with FTB Chunks, so "claim 25 chunks" quests get faked with item turn-ins or command rewards. This addon adds the missing task.

In the quest editor, add a task and choose **Claim Chunks**, then set **Chunks to claim** — that is the whole setup. On every claim the mod reads the owning team's current claimed-chunk total and feeds it to every matching task the team can progress, including dependent and sequential quests.

Progress is a high-water mark: it only ever rises. Unclaiming cannot push it back, re-claiming the same chunk cannot farm it, and once FTB Quests marks the task complete it stays complete. Claims by any member credit the team that owns the chunk, so multiplayer groups share progress automatically — single player is just a one-person team.

The server tracks progress. Clients need the mod too, so the task type displays correctly in the quest editor and quest view.

Works with FTB Quests and FTB Chunks; FTB Teams and FTB Library come along as their dependencies. All four FTB mods are published on CurseForge only.

<p><a href="https://www.patreon.com/KURONAMI333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/patreon.png" width="440" height="156" alt="Support my mods on Patreon"></a> <a href="https://x.com/kuronami333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/x.png" width="300" height="156" alt="Follow @kuronami333 on X"></a></p>

Bugs and questions: comment on this page, or DM @kuronami333 on X.

All Rights Reserved. Free to put in any modpack, on any platform, monetised or not - no permission needed, no credit required. Source: https://github.com/KURONAMI333/claim-quest
