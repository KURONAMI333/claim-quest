package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
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
 * Multi-member acceptance: two players in one FTB Teams party claim chunks;
 * their claims must accumulate on the single shared {@link TeamData} of the
 * party so one claim task reaches the team's total exactly once — no
 * per-player counting, no progress inflated beyond the real claim count.
 */
final class FtbTeamQuestTest {

    private FtbTeamQuestTest() {}

    static void run(GameTestHelper helper, ServerLevel level) {
        ServerQuestFile file = java.util.Objects.requireNonNull(ServerQuestFile.INSTANCE, "no ServerQuestFile");
        Quest quest = FtbTestSupport.requireQuest(file, TestQuestIds.SINGLE);
        ClaimTask task = FtbTestSupport.claimTask(quest, 0);
        FakeClient owner = FakeClient.login(level, "cq-owner");
        FakeClient mate = FakeClient.login(level, "cq-mate");
        ChunkPos base = new ChunkPos(helper.absolutePos(new BlockPos(1, 1, 1)));
        AtomicReference<Team> partyRef = new AtomicReference<>();
        AtomicReference<TeamData> dataRef = new AtomicReference<>();
        AtomicReference<ChunkPos> mateChunkRef = new AtomicReference<>();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    Team party = FtbTestSupport.createParty(owner.player());
                    FtbTestSupport.joinParty(party, mate.player());
                    partyRef.set(party);
                    helper.assertTrue(
                            FtbTestSupport.requirePlayerTeam(owner.player()).getId()
                                    .equals(party.getId()),
                            "owner's effective team is not the created party");
                    helper.assertTrue(
                            FtbTestSupport.requirePlayerTeam(mate.player()).getId()
                                    .equals(party.getId()),
                            "mate's effective team is not the party after join");
                    helper.assertTrue(
                            party.getMembers().contains(owner.player().getUUID())
                                    && party.getMembers().contains(mate.player().getUUID()),
                            "party does not contain both members");
                    TeamData data = file.getOrCreateTeamData(party);
                    dataRef.set(data);
                    helper.assertTrue(data.getProgress(task) == 0L,
                            "fresh team claim task already has progress");
                    FtbTestSupport.claimFreeChunks(owner.player(), level, base, 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task) == 1L,
                            "owner's claim did not set shared progress to 1, got "
                                    + data.getProgress(task));
                    helper.assertTrue(FtbTestSupport.claimedCount(partyRef.get()) == 1,
                            "party claimed-chunk total is not 1 after first claim");
                    mateChunkRef.set(FtbTestSupport.claimFreeChunks(mate.player(), level,
                            new ChunkPos(base.x + 1, base.z), 1).get(0));
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.getProgress(task) == 2L,
                            "mate's claim did not raise shared progress to the team total 2, got "
                                    + data.getProgress(task));
                    ClaimedChunk mateChunk = FtbTestSupport.claimedChunkAt(
                            level.dimension(), mateChunkRef.get());
                    helper.assertTrue(mateChunk != null,
                            "mate's claimed chunk not registered");
                    if (mateChunk != null) {
                        helper.assertTrue(
                                mateChunk.getTeamData().getTeam().getId()
                                        .equals(partyRef.get().getId()),
                                "mate's chunk is owned by a different team than the party");
                    }
                    helper.assertTrue(FtbTestSupport.claimedCount(partyRef.get()) == 2,
                            "party claimed-chunk total is not 2 after members' claims");
                    FtbTestSupport.claimFreeChunks(mate.player(), level,
                            new ChunkPos(base.x + 2, base.z), 1);
                })
                .thenExecuteAfter(5, () -> {
                    TeamData data = dataRef.get();
                    helper.assertTrue(data.isCompleted(task),
                            "shared claim task not completed at team total 3, progress "
                                    + data.getProgress(task));
                    helper.assertTrue(data.isCompleted(quest),
                            "quest not completed when shared task completed");
                    helper.assertTrue(FtbTestSupport.claimedCount(partyRef.get()) == 3,
                            "party claimed-chunk total is not 3 after all claims");
                    helper.assertTrue(data.getProgress(task) == FtbTestSupport.claimedCount(partyRef.get()),
                            "task progress differs from the team's real claim count — "
                                    + "claims were counted more than once");
                })
                .thenSucceed();
    }
}
