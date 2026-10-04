# Claim Quest Forge 1.19.2 port

This cell targets Minecraft 1.19.2 on Forge 43.4.5, with FTB Quests 1902.5.10-build.497, FTB Chunks 1902.4.7-build.420, FTB Teams 1902.2.14-build.123, FTB Library 1902.4.2-build.701, Architectury 6.5.82, and Item Filters file 4553326.

The add-on advances a team-shared Claim Chunks task from the current claimed count using a high-water mark. Quest dependency gating is retained through FTB Quests 1902 TeamData.canStartTasks. The exact FTB Quests 1902 API does not expose a sequential-task toggle or task-order gate, so this cell does not claim that behavior; its GameTest records the host's actual independent progress behavior for two tasks in the same quest.

FTB Quests and FTB Chunks remain optional to Claim Quest itself; without the host mods it loads inert. Quests 1902 declares Item Filters as a required dependency, so the candidate runtime includes Item Filters 1902.2.9-build.51 (CurseForge file ID 4553326).

Validation status and evidence are held outside this source tree in the shared addon-major-ports handoff. No Gradle build or runtime GameTest was run by the implementation owner.
