package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Real claim/unclaim/reclaim acceptance for the Fabric event adapter. */
final class FtbClaimQuestTest {
    private FtbClaimQuestTest() {}

    static void run(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = ServerQuestFile.getInstance()
                .orElseThrow(() -> new IllegalStateException("no ServerQuestFile"));
        helper.assertFalse(file.collect(ClaimTask.class).isEmpty(),
                "no claimquest:claim task loaded from test questpack");
        ClaimTask task = FtbTestSupport.claimTask(
                FtbTestSupport.requireQuest(file, TestQuestIds.SINGLE), 0);
        FakeClient client = FakeClient.login(helper, "cq-highwater");
        ChunkPos start = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<TeamData> dataRef = new AtomicReference<>();
        AtomicReference<Team> teamRef = new AtomicReference<>();
        AtomicReference<List<ChunkPos>> firstClaims = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    Team team = FtbTestSupport.requirePlayerTeam(client.player());
                    TeamData data = file.getOrCreateTeamData(team);
                    teamRef.set(team);
                    dataRef.set(data);
                    helper.assertTrue(data.getProgress(task) == 0L, "fresh task already has progress");
                    firstClaims.set(FtbTestSupport.claimFreeChunks(client.player(), level, start, 2));
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task) == 2L,
                            "two claims did not set progress to 2, got " + data.getProgress(task));
                    helper.assertTrue(FtbTestSupport.claimedCount(teamRef.get()) == 2,
                            "team should own exactly two claims before unclaim");
                    FtbTestSupport.unclaim(client.player(), level, firstClaims.get().get(0));
                    helper.assertTrue(FtbTestSupport.claimedCount(teamRef.get()) == 1,
                            "unclaim did not lower live FTB claim count to one");
                    helper.assertTrue(data.getProgress(task) == 2L,
                            "task progress decreased after unclaim; progress must be a high-water mark");
                })
                .thenExecuteAfter(5, () -> {
                    FtbTestSupport.reclaim(client.player(), level, firstClaims.get().get(0));
                    helper.assertTrue(FtbTestSupport.claimedCount(teamRef.get()) == 2,
                            "reclaim of same chunk should restore the live count to two");
                    helper.assertTrue(dataRef.get().getProgress(task) == 2L,
                            "reclaim farmed progress above the previous high-water mark");
                    FtbTestSupport.claimFreeChunks(client.player(), level, start, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task) == 3L,
                            "third distinct live claim did not advance progress to 3, got "
                                    + data.getProgress(task));
                    helper.assertTrue(data.isCompleted(task), "task not completed at target 3");
                    helper.assertTrue(data.isCompleted(task.getQuest()), "quest not completed with task");
                })
                .thenSucceed();
    }
}
