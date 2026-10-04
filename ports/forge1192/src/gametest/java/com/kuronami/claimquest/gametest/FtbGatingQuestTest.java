package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.data.Team;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Gating acceptance: a claim task must not progress while its quest's
 * dependencies are unmet. The host 1902 API has no task-sequence toggle, so a
 * separate test records how two same-quest tasks actually progress instead of
 * pretending a sequence gate exists.
 *
 * <p>Every claim feeds every claim task of the claiming team, so the gated
 * tasks see real claim events while blocked — the assert that their progress
 * stays at 0 across two claims is what proves the hook honours FTB Quests'
 * {@code canStartTasks} dependency gate rather than feeding progress unconditionally.
 */
final class FtbGatingQuestTest {

    private FtbGatingQuestTest() {}

    /**
     * Quest A (claim 3) is the prerequisite of quest B (claim 4,
     * {@code dependencies: [A]}). B's task must stay at 0 for the two claims
     * that leave A incomplete, then run to completion once A is done.
     */
    static void dependencyGate(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = java.util.Objects.requireNonNull(ServerQuestFile.INSTANCE, "no ServerQuestFile");
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
                    TeamData data = file.getData(team);
                    dataRef.set(data);
                    TestAssertions.assertTrue(helper, data.areDependenciesComplete(questA),
                            "prerequisite quest should have no unmet dependencies");
                    TestAssertions.assertFalse(helper, data.areDependenciesComplete(questB),
                            "dependent quest reported deps complete before prerequisite");
                    TestAssertions.assertFalse(helper, data.canStartTasks(questB),
                            "dependent quest tasks startable before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level, base, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.getProgress(taskA) == 1L,
                            "prerequisite task did not progress after 1 claim, got "
                                    + data.getProgress(taskA));
                    TestAssertions.assertTrue(helper, data.getProgress(taskB) == 0L,
                            "dependent task progressed before prerequisite completed, got "
                                    + data.getProgress(taskB));
                    TestAssertions.assertFalse(helper, data.isCompleted(taskB),
                            "dependent task completed before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 1, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.getProgress(taskA) == 2L,
                            "prerequisite task did not progress after 2 claims, got "
                                    + data.getProgress(taskA));
                    TestAssertions.assertTrue(helper, data.getProgress(taskB) == 0L,
                            "dependent task progressed while prerequisite still incomplete, got "
                                    + data.getProgress(taskB));
                    TestAssertions.assertFalse(helper, data.isCompleted(questB),
                            "dependent quest completed before prerequisite");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 2, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.isCompleted(taskA),
                            "prerequisite task not completed after 3 claims");
                    TestAssertions.assertTrue(helper, data.isCompleted(questA),
                            "prerequisite quest not completed when its task completed");
                    TestAssertions.assertFalse(helper, data.isCompleted(taskB),
                            "dependent task completed before reaching its target");
                    TestAssertions.assertTrue(helper, data.getProgress(taskB) <= 3L,
                            "dependent task progress beyond team claim count, got "
                                    + data.getProgress(taskB));
                    TestAssertions.assertTrue(helper, data.canStartTasks(questB),
                            "dependent quest still not startable after prerequisite completed");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 3, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.isCompleted(taskB),
                            "dependent task not completed after prerequisite met and target reached, progress "
                                    + data.getProgress(taskB));
                    TestAssertions.assertTrue(helper, data.isCompleted(questB),
                            "dependent quest not completed when its task completed");
                })
                .thenSucceed();
    }

    /**
     * FTB Quests 1902 has no sequential-task flag/API. Record the supported
     * behavior: both tasks receive the team's claim count independently.
     */
    static void hostTaskProgression(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = java.util.Objects.requireNonNull(ServerQuestFile.INSTANCE, "no ServerQuestFile");
        Quest quest = FtbTestSupport.requireQuest(file, TestQuestIds.MULTI_TASK);
        ClaimTask task1 = FtbTestSupport.claimTask(quest, 0);
        ClaimTask task2 = FtbTestSupport.claimTask(quest, 1);
        FakeClient client = FakeClient.login(level, "cq-multi");
        ChunkPos base = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<TeamData> dataRef = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    Team team = FtbTestSupport.requirePlayerTeam(client.player());
                    TeamData data = file.getData(team);
                    dataRef.set(data);
                    FtbTestSupport.claimFreeChunks(client.player(), level, base, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.getProgress(task1) == 1L,
                            "first task did not progress after 1 claim");
                    TestAssertions.assertTrue(helper, data.getProgress(task2) == 1L,
                            "second task did not follow host's independent-task behavior");
                    TestAssertions.assertFalse(helper, data.isCompleted(task1), "first task completed too early");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 1, base.z), 2);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.isCompleted(task1), "first task did not reach target 3");
                    TestAssertions.assertTrue(helper, data.getProgress(task2) == 3L,
                            "second task did not progress independently before its target");
                    TestAssertions.assertFalse(helper, data.isCompleted(task2), "second task completed before target 4");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(base.x + 3, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    TestAssertions.assertTrue(helper, data.isCompleted(task2), "second task did not reach target 4");
                    TestAssertions.assertTrue(helper, data.isCompleted(quest), "multi-task quest did not complete");
                })
                .thenSucceed();
    }

}
