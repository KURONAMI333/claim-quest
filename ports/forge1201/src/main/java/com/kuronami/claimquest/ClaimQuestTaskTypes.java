package com.kuronami.claimquest;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

/**
 * Holder for this mod's task type. Only touched when FTB Quests is loaded.
 */
public final class ClaimQuestTaskTypes {

    public static TaskType CLAIM;

    private ClaimQuestTaskTypes() {
    }

    public static void register() {
        CLAIM = TaskTypes.register(
                new ResourceLocation(ClaimQuest.MODID, "claim"),
                ClaimTask::new,
                () -> ItemIcon.getItemIcon(Items.MAP));
    }
}
