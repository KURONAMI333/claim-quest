package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Gating acceptance: a claim task must not progress while its quest's
 * dependencies are unmet, or while an earlier task in a
 * {@code require_sequential_tasks} quest is incomplete; and it must complete
 * once the prerequisite is satisfied.
 *
 * <p>Every claim feeds every claim task of the claiming team, so the gated
 * tasks see real claim events while blocked — the assert that their progress
 * stays at 0 across two claims is what proves the hook honours FTB Quests'
 * {@code canStartTasks}/{@code checkTaskSequence} gates rather than feeding
 * progress unconditionally.
 */
final class FtbGatingQuestTest {

    private FtbGatingQuestTest() {}

    /**
     * Quest A (claim 3) is the prerequisite of quest B (claim 4,
     * {@code dependencies: [A]}). B's task must stay at 0 for the two claims
     * that leave A incomplete, then run to completion once A is done.
     */
    static void dependencyGate(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = ServerQuestFile.getInstance()
                .orElseThrow(() -> new IllegalStateException("no ServerQuestFile"));
        Quest questA = FtbTestSupport.requireQuest(file, TestQuestIds.DEP_A);
        Quest questB = FtbTestSupport.requireQuest(file, TestQuestIds.DEP_B);
        ClaimTask taskA = FtbTestSupport.claimTask(questA, 0);
        ClaimTask taskB = FtbTestSupport.claimTask(questB, 0);
        FakeClient client = FakeClient.login(level, "cq-dep");
        ChunkPos base = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<TeamData> dataRef = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    Team team = FtbTestSupport.requirePlayerTeam(client.player());
                    TeamData data = file.getOrCreateTeamData(team);
                    dataRef.set(data);
                    helper.assertTrue(data.areDependenciesComplete(questA),
                            "prerequisite quest should have no unmet dependencies");
                    helper.assertFalse(data.areDependenciesComplete(questB),
                            "dependent quest reported deps complete before prerequisite");
                    helper.assertFalse(data.canStartTasks(questB),
                            "dependent quest tasks startable before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level, base, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(taskA) == 1L,
                            "prerequisite task did not progress after 1 claim, got "
                                    + data.getProgress(taskA));
                    helper.assertTrue(data.getProgress(taskB) == 0L,
                            "dependent task progressed before prerequisite completed, got "
                                    + data.getProgress(taskB));
                    helper.assertFalse(data.isCompleted(taskB),
                            "dependent task completed before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 1, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(taskA) == 2L,
                            "prerequisite task did not progress after 2 claims, got "
                                    + data.getProgress(taskA));
                    helper.assertTrue(data.getProgress(taskB) == 0L,
                            "dependent task progressed while prerequisite still incomplete, got "
                                    + data.getProgress(taskB));
                    helper.assertFalse(data.isCompleted(questB),
                            "dependent quest completed before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 2, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.isCompleted(taskA),
                            "prerequisite task not completed after 3 claims");
                    helper.assertTrue(data.isCompleted(questA),
                            "prerequisite quest not completed when its task completed");
                    helper.assertFalse(data.isCompleted(taskB),
                            "dependent task completed before reaching its target");
                    helper.assertTrue(data.getProgress(taskB) <= 3L,
                            "dependent task progress beyond team claim count, got "
                                    + data.getProgress(taskB));
                    helper.assertTrue(data.canStartTasks(questB),
                            "dependent quest still not startable after prerequisite completed");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 3, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.isCompleted(taskB),
                            "dependent task not completed after prerequisite met and target reached, progress "
                                    + data.getProgress(taskB));
                    helper.assertTrue(data.isCompleted(questB),
                            "dependent quest not completed when its task completed");
                })
                .thenSucceed();
    }

    /**
     * Quest S has {@code require_sequential_tasks: true} with claim tasks of
     * value 3 then 4. The second task must stay at 0 for the two claims that
     * leave the first incomplete, then complete on the next claim.
     */
    static void sequentialGate(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = ServerQuestFile.getInstance()
                .orElseThrow(() -> new IllegalStateException("no ServerQuestFile"));
        Quest questS = FtbTestSupport.requireQuest(file, TestQuestIds.SEQUENTIAL);
        ClaimTask task1 = FtbTestSupport.claimTask(questS, 0);
        ClaimTask task2 = FtbTestSupport.claimTask(questS, 1);
        FakeClient client = FakeClient.login(level, "cq-seq");
        ChunkPos base = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<TeamData> dataRef = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(questS.getRequireSequentialTasks(),
                            "test quest did not load require_sequential_tasks from SNBT");
                    Team team = FtbTestSupport.requirePlayerTeam(client.player());
                    TeamData data = file.getOrCreateTeamData(team);
                    dataRef.set(data);
                    FtbTestSupport.claimFreeChunks(client.player(), level, base, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task1) == 1L,
                            "first sequential task did not progress after 1 claim, got "
                                    + data.getProgress(task1));
                    helper.assertTrue(data.getProgress(task2) == 0L,
                            "second sequential task progressed while first incomplete, got "
                                    + data.getProgress(task2));
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 1, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task1) == 2L,
                            "first sequential task did not progress after 2 claims, got "
                                    + data.getProgress(task1));
                    helper.assertTrue(data.getProgress(task2) == 0L,
                            "second sequential task progressed while first incomplete, got "
                                    + data.getProgress(task2));
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 2, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.isCompleted(task1),
                            "first sequential task not completed after 3 claims");
                    helper.assertFalse(data.isCompleted(task2),
                            "second sequential task completed before reaching its target");
                    helper.assertTrue(data.getProgress(task2) <= 3L,
                            "second sequential task progress beyond team claim count, got "
                                    + data.getProgress(task2));
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 3, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.isCompleted(task2),
                            "second sequential task not completed after first done and target reached, progress "
                                    + data.getProgress(task2));
                    helper.assertTrue(data.isCompleted(questS),
                            "sequential quest not completed when its tasks completed");
                })
                .thenSucceed();
    }
}
