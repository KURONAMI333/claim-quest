package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.data.Team;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * The FTB-coupled body of the claim-progress test. Loaded only after the
 * caller's {@code ModList} guard confirms FTB Quests + FTB Chunks are present,
 * so this class may freely reference FTB types (and {@link ClaimTask}, whose
 * superclass chain lives in FTB Quests).
 *
 * <p>The SNBT parse itself is covered: if {@code type: "claimquest:claim"}
 * failed to resolve, {@code file.collect(ClaimTask.class)} comes back empty.
 */
final class FtbClaimQuestTest {

    private FtbClaimQuestTest() {}

    static void run(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = java.util.Objects.requireNonNull(ServerQuestFile.INSTANCE, "no ServerQuestFile");
        FakeClient client = FakeClient.login(level, "cq-claimer");
        ChunkPos firstChunk = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<TeamData> dataRef = new AtomicReference<>();
        AtomicReference<ClaimTask> taskRef = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    TestAssertions.assertFalse(helper, file.collect(ClaimTask.class).isEmpty(),
                            "no claimquest:claim task loaded from test questpack — "
                                    + "type resolution failed or chapter SNBT not read");
                    taskRef.set(FtbTestSupport.claimTask(
                            FtbTestSupport.requireQuest(file, TestQuestIds.SINGLE), 0));
                    Team team = FtbTestSupport.requirePlayerTeam(client.player());
                    TeamData data = file.getData(team);
                    dataRef.set(data);
                    TestAssertions.assertTrue(helper, data.getProgress(taskRef.get()) == 0L,
                            "fresh claim task already has progress");
                    FtbTestSupport.claimFreeChunks(client.player(), level, firstChunk, 2);
                })
                .thenExecuteAfter(5, () -> {
                    TestAssertions.assertTrue(helper, dataRef.get().getProgress(taskRef.get()) == 2L,
                            "claiming 2 chunks did not set progress to 2, got "
                                    + dataRef.get().getProgress(taskRef.get()));
                    TestAssertions.assertFalse(helper, dataRef.get().isCompleted(taskRef.get()),
                            "task completed before reaching its target");
                    FtbTestSupport.claimFreeChunks(client.player(), level,
                            new ChunkPos(firstChunk.x + 2, firstChunk.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TestAssertions.assertTrue(helper, dataRef.get().isCompleted(taskRef.get()),
                            "claim task not completed after reaching target chunk count");
                    TestAssertions.assertTrue(helper, dataRef.get().isCompleted(taskRef.get().quest),
                            "single-task quest not completed when its task completed");
                })
                .thenSucceed();
    }
}
