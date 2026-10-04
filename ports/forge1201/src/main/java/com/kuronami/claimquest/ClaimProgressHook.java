package com.kuronami.claimquest;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.event.ClaimedChunkEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandSourceStack;

/**
 * Bridges FTB Chunks claim events into FTB Quests task progress.
 *
 * <p>Only loaded when both FTB Quests and FTB Chunks are present. The owner
 * team is taken from the claimed chunk itself ({@link ClaimedChunk#getTeamData()}),
 * so claims made by any member credit the team the chunk belongs to.
 */
public final class ClaimProgressHook {

    private static boolean warned;

    private ClaimProgressHook() {
    }

    public static void register() {
        ClaimedChunkEvent.AFTER_CLAIM.register(ClaimProgressHook::onChunkClaimed);
    }

    private static void onChunkClaimed(CommandSourceStack source, ClaimedChunk chunk) {
        try {
            ServerQuestFile file = ServerQuestFile.INSTANCE;
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
            // A dispatch failure must not break FTB Chunks' claim handling.
            // Warn once, keep the stack at debug — later claims may succeed.
            if (!warned) {
                warned = true;
                ClaimQuest.LOGGER.warn("Claim progress dispatch failed: {}", t.toString());
            }
            ClaimQuest.LOGGER.debug("Claim progress dispatch failure detail", t);
        }
    }
}
