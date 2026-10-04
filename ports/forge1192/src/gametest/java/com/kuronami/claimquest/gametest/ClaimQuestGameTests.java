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
        if (!fixtureAllowsFtbTests(helper, "claimingChunksProgressesClaimTask")) return;
        FtbClaimQuestTest.run(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void dependencyGatedClaimTaskWaitsForPrerequisite(GameTestHelper helper) {
        if (!fixtureAllowsFtbTests(helper, "dependencyGatedClaimTaskWaitsForPrerequisite")) return;
        FtbGatingQuestTest.dependencyGate(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void multipleClaimTasksFollowHostProgressionMode(GameTestHelper helper) {
        if (!fixtureAllowsFtbTests(helper, "multipleClaimTasksFollowHostProgressionMode")) return;
        FtbGatingQuestTest.hostTaskProgression(helper, helper.getLevel());
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty5x5x5", timeoutTicks = 600)
    public static void teamMembersClaimsDriveOneSharedTask(GameTestHelper helper) {
        if (!fixtureAllowsFtbTests(helper, "teamMembersClaimsDriveOneSharedTask")) return;
        FtbTeamQuestTest.run(helper, helper.getLevel());
    }

    /**
     * The explicit fixture mode prevents a full FTB-enabled run from silently
     * succeeding when dependencies are missing. The without-ftb fixture proves
     * the optional-host path is loaded and inert.
     */
    private static boolean fixtureAllowsFtbTests(GameTestHelper helper, String testName) {
        String mode = System.getProperty("claimquest.fixture.mode", "full");
        boolean claimQuestLoaded = ModList.get().isLoaded(ClaimQuest.MODID);
        boolean allFtbPresent = ModList.get().isLoaded("ftbquests")
                && ModList.get().isLoaded("ftbchunks")
                && ModList.get().isLoaded("ftbteams")
                && ModList.get().isLoaded("ftblibrary");
        boolean anyFtbPresent = ModList.get().isLoaded("ftbquests")
                || ModList.get().isLoaded("ftbchunks")
                || ModList.get().isLoaded("ftbteams")
                || ModList.get().isLoaded("ftblibrary");

        if (!claimQuestLoaded) {
            helper.fail(testName + ": Claim Quest did not load");
        }
        if ("full".equals(mode)) {
            if (!allFtbPresent) {
                helper.fail(testName + ": full fixture requires FTB Quests, Chunks, Teams, and Library");
            }
            return true;
        }
        if ("without-ftb".equals(mode)) {
            if (anyFtbPresent) {
                helper.fail(testName + ": without-ftb fixture requires all four FTB host mods absent");
            }
            ClaimQuest.LOGGER.info("{}: Claim Quest loaded with all FTB hosts absent; inert path verified", testName);
            helper.succeed();
            return false;
        }
        helper.fail(testName + ": unknown claimquest.fixture.mode='" + mode + "'");
        return false;
    }
}
