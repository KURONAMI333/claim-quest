package com.kuronami.claimquest.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.kuronami.claimquest.ClaimTask;
import dev.ftb.mods.ftbchunks.data.ClaimResult;
import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.FTBChunksTeamData;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * FTB-dependent test helpers. Every FTB-typed signature lives here so the
 * class is only loaded when FTB Chunks/Teams are actually present — the
 * caller guards with {@code ModList} before touching it.
 */
final class FtbTestSupport {

    private FtbTestSupport() {}

    /** The player's effective team, created by FTB Teams on login. */
    static Team requirePlayerTeam(ServerPlayer player) {
        Team team = FTBTeamsAPI.getPlayerTeam(player);
        if (team == null) {
            throw new IllegalStateException(
                    "FTB Teams did not create a team for fake player " + player.getGameProfile().getName());
        }
        return team;
    }

    /**
     * Claims {@code count} unclaimed chunks scanning +x from {@code from},
     * via the real FTB Chunks API, and returns the positions actually claimed.
     *
     * <p>The GameTest world and its FTB Chunks data persist across runs and
     * batches, so a fixed claim row can land on chunks already claimed by
     * another team. Skipping occupied chunks keeps the test position-agnostic;
     * what matters is that this player's team ends up with {@code count} more
     * real claims.
     *
     * @throws IllegalStateException when a claim fails or no free chunk is found
     */
    static List<ChunkPos> claimFreeChunks(ServerPlayer player, ServerLevel level,
                                        ChunkPos from, int count) {
        List<ChunkPos> claimed = new ArrayList<>(count);
        for (int x = from.x; claimed.size() < count && x < from.x + 1024; x++) {
            ChunkPos pos = new ChunkPos(x, from.z);
            if (claimedChunkAt(level.dimension(), pos) != null) continue;
            ClaimResult r = FTBChunksAPI.claimAsPlayer(player, level.dimension(), pos, false);
            if (!r.isSuccess()) {
                throw new IllegalStateException("claimAsPlayer failed at " + pos
                        + ": " + r.claimResultName());
            }
            claimed.add(pos);
        }
        if (claimed.size() < count) {
            throw new IllegalStateException("no free chunk found scanning +x from " + from);
        }
        return claimed;
    }

    /** Looks up a quest loaded from the SNBT pack by its hex id. */
    static Quest requireQuest(ServerQuestFile file, long id) {
        QuestObject obj = file.get(id);
        if (!(obj instanceof Quest quest)) {
            throw new IllegalStateException("quest 0x" + Long.toHexString(id).toUpperCase()
                    + " not loaded — chapter SNBT was not read or the id was dropped");
        }
        return quest;
    }

    /** The {@code index}-th task of {@code quest}, which must be a claim task. */
    static ClaimTask claimTask(Quest quest, int index) {
        Task task = quest.tasks.get(index);
        if (!(task instanceof ClaimTask claim)) {
            throw new IllegalStateException("task " + index + " of quest 0x"
                    + Long.toHexString(quest.id).toUpperCase() + " is not a claimquest:claim task");
        }
        return claim;
    }

    /** Creates a real FTB Teams party for {@code owner} (the real create path). */
    static Team createParty(ServerPlayer owner) {
        try {
            return FTBTeamsAPI.getManager()
                    .createParty(owner, "cq-test-party", "", Color4I.RED).getRight();
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("createPartyTeam failed: " + e.getMessage(), e);
        }
    }

    /** Real party join: {@code player} becomes a MEMBER of {@code party}. */
    static void joinParty(Team party, ServerPlayer player) {
        if (!(party instanceof PartyTeam partyTeam)) {
            throw new IllegalStateException("created team is not a party team: " + party);
        }
        try {
            partyTeam.join(player.createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("party join failed: " + e.getMessage(), e);
        }
    }

    /** Live claimed-chunk total of a team, via the real FTB Chunks manager. */
    static int claimedCount(Team team) {
        FTBChunksTeamData data = FTBChunksAPI.getManager().getData(team);
        return data == null ? 0 : data.getClaimedChunks().size();
    }

    /** The claimed chunk at {@code pos}, or null when unclaimed. */
    static ClaimedChunk claimedChunkAt(ResourceKey<Level> dimension, ChunkPos pos) {
        return FTBChunksAPI.getManager().getChunk(new ChunkDimPos(dimension, pos));
    }
}
