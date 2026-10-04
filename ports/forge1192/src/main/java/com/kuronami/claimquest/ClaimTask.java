package com.kuronami.claimquest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * "Claim Chunks" task: progresses as the team's claimed-chunk count grows.
 *
 * <p>Progress is the team's <em>current</em> claimed-chunk total, applied as a
 * high-water mark ({@link #onClaimedCountChanged} only raises it). That makes
 * the task inherently reclaim-safe: unclaiming and re-claiming the same or
 * another chunk cannot farm progress, and nothing extra needs persisting. Once
 * FTB Quests marks the task completed it stays completed, as with every task.
 *
 * <p>{@code value} (target chunk count) is edited per-task in the quest GUI
 * and round-trips through SNBT and the network sync like any built-in field.
 */
public class ClaimTask extends Task {

    private long value = 1;

    public ClaimTask(Quest quest) {
        super(quest);
    }

    @Override
    public TaskType getType() {
        return ClaimQuestTaskTypes.CLAIM;
    }

    @Override
    public long getMaxProgress() {
        return value;
    }

    @Override
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putLong("value", value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        value = Math.max(nbt.getLong("value"), 1L);
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = Math.max(buffer.readVarLong(), 1L);
    }

    /**
     * Feeds the team's current claimed-chunk count into this task.
     *
     * <p>Gating mirrors what FTB Quests 1902 checks before a task can receive
     * progress: the task must not be completed and its quest must be startable
     * (dependencies done). This FTB Quests API has no sequential-task gate.
     */
    public void onClaimedCountChanged(TeamData data, long claimedCount) {
        if (data.isCompleted(this) || !data.canStartTasks(quest)) {
            return;
        }
        long target = Math.min(claimedCount, value);
        if (target > data.getProgress(this)) {
            data.setProgress(this, target);
        }
    }
}
