package com.kuronami.claimquest;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.event.ClaimedChunkEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandSourceStack;

/** Bridges FTB Chunks claim events into the registered FTB Quests task. */
final class FtbChunksIntegration {

    private static boolean warned;

    private FtbChunksIntegration() {
    }

    static void register() {
        ClaimedChunkEvent.AFTER_CLAIM.register(FtbChunksIntegration::onChunkClaimed);
    }

    private static void onChunkClaimed(CommandSourceStack source, ClaimedChunk chunk) {
        try {
            ServerQuestFile file = ServerQuestFile.getInstance().orElse(null);
            if (file == null || file.isLoading()) {
                return;
            }
            ChunkTeamData chunkTeamData = chunk.getTeamData();
            if (chunkTeamData == null) {
                return;
            }
            Team team = chunkTeamData.getTeam();
            if (team == null) {
                return;
            }

            long claimedCount = chunkTeamData.getClaimedChunks().size();
            TeamData data = file.getOrCreateTeamData(team);
            for (ClaimTask task : file.collect(ClaimTask.class)) {
                task.onClaimedCountChanged(data, claimedCount);
            }
        } catch (Exception | LinkageError t) {
            if (!warned) {
                warned = true;
                ClaimQuest.LOGGER.warn("Claim progress dispatch failed: {}", t.toString());
            }
            ClaimQuest.LOGGER.debug("Claim progress dispatch failure detail", t);
        }
    }
}
