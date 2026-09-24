package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimQuest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * End-to-end check on a real headless server: a fake player put through the
 * real login path gets an FTB Teams player team, claims chunks via the real
 * FTB Chunks API, and the {@code claimquest:claim} task loaded from the SNBT
 * questpack must progress to completion.
 *
 * <p>All FTB-typed code lives in {@link FtbClaimQuestTest}: this class must
 * stay free of FTB (and ClaimTask) references in method bodies so it still
 * loads when the {@code -PwithoutFtb} run has no FTB jars — the GameTest
 * registration scan does {@code Class.forName} on every holder class.
 */
@GameTestHolder(ClaimQuest.MODID)
public class ClaimQuestGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void claimingChunksProgressesClaimTask(GameTestHelper helper) {
        if (!ModList.get().isLoaded("ftbquests") || !ModList.get().isLoaded("ftbchunks")) {
            ClaimQuest.LOGGER.warn(
                    "claimingChunksProgressesClaimTask skipped: FTB Quests/Chunks not loaded");
            helper.succeed();
            return;
        }
        FtbClaimQuestTest.run(helper, helper.getLevel());
    }
}
