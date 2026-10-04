package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimQuest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

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
        if (!ftbPresent(helper, "claimingChunksProgressesClaimTask")) return;
        FtbClaimQuestTest.run(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void dependencyGatedClaimTaskWaitsForPrerequisite(GameTestHelper helper) {
        if (!ftbPresent(helper, "dependencyGatedClaimTaskWaitsForPrerequisite")) return;
        FtbGatingQuestTest.dependencyGate(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void sequentialClaimTasksProgressInOrder(GameTestHelper helper) {
        if (!ftbPresent(helper, "sequentialClaimTasksProgressInOrder")) return;
        FtbGatingQuestTest.sequentialGate(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void teamMembersClaimsDriveOneSharedTask(GameTestHelper helper) {
        if (!ftbPresent(helper, "teamMembersClaimsDriveOneSharedTask")) return;
        FtbTeamQuestTest.run(helper, helper.getLevel());
    }

    /**
     * FTB-gated entry point: without FTB Quests/Chunks there is nothing to
     * verify — log and succeed so the {@code -PwithoutFtb} run still proves
     * the mod loads inert rather than dropping the tests.
     */
    private static boolean ftbPresent(GameTestHelper helper, String testName) {
        if (ModList.get().isLoaded("ftbquests") && ModList.get().isLoaded("ftbchunks")) {
            return true;
        }
        ClaimQuest.LOGGER.warn("{} skipped: FTB Quests/Chunks not loaded", testName);
        helper.succeed();
        return false;
    }
}
