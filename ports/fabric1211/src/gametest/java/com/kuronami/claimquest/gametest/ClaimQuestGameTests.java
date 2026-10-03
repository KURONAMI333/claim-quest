package com.kuronami.claimquest.gametest;

import com.kuronami.claimquest.ClaimQuest;
import java.lang.reflect.Field;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** Dev-only Fabric GameTest entrypoint with explicit dependency-mode assertions. */
public final class ClaimQuestGameTests implements FabricGameTest {
    private static final int TIMEOUT_TICKS = 600;
    private static final String FULL = "full";
    private static final String WITHOUT_FTB = "without-ftb";
    private static final String WITHOUT_CHUNKS = "without-ftb-chunks";

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = TIMEOUT_TICKS)
    public void claimsProgressAndRemainHighWaterAcrossUnclaimReclaim(GameTestHelper helper) {
        if (!onlyMode(helper, FULL)) return;
        assertAllHostsPresent(helper);
        FtbClaimQuestTest.run(helper, helper.getLevel());
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = TIMEOUT_TICKS)
    public void dependencyGatedClaimTaskWaitsForPrerequisite(GameTestHelper helper) {
        if (!onlyMode(helper, FULL)) return;
        assertAllHostsPresent(helper);
        FtbGatingQuestTest.dependencyGate(helper, helper.getLevel());
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = TIMEOUT_TICKS)
    public void sequentialClaimTasksProgressInOrder(GameTestHelper helper) {
        if (!onlyMode(helper, FULL)) return;
        assertAllHostsPresent(helper);
        FtbGatingQuestTest.sequentialGate(helper, helper.getLevel());
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = TIMEOUT_TICKS)
    public void teamMembersClaimsDriveOneSharedTask(GameTestHelper helper) {
        if (!onlyMode(helper, FULL)) return;
        assertAllHostsPresent(helper);
        FtbTeamQuestTest.run(helper, helper.getLevel());
    }

    /** Claim Quest must load without any FTB host classes or mod IDs. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void noFtbRuntimeLeavesOptionalIntegrationInert(GameTestHelper helper) {
        if (!onlyMode(helper, WITHOUT_FTB)) return;
        helper.assertTrue(FabricLoader.getInstance().isModLoaded(ClaimQuest.MODID),
                "Claim Quest did not load in the no-FTB fixture");
        helper.assertFalse(anyHostPresent(), "an FTB host unexpectedly loaded in -PwithoutFtb mode");
        assertClassAbsent(helper, "dev.ftb.mods.ftbquests.api.FTBQuestsAPI");
        helper.succeed();
    }

    /** Quests, Teams and Library stay present when only the Chunks provider is omitted. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void chunksOptionalPathKeepsTaskRegistered(GameTestHelper helper) {
        if (!onlyMode(helper, WITHOUT_CHUNKS)) return;
        helper.assertTrue(FabricLoader.getInstance().isModLoaded("ftbquests"), "FTB Quests missing");
        helper.assertTrue(FabricLoader.getInstance().isModLoaded("ftbteams"), "FTB Teams missing");
        helper.assertTrue(FabricLoader.getInstance().isModLoaded("ftblibrary"), "FTB Library missing");
        helper.assertFalse(FabricLoader.getInstance().isModLoaded("ftbchunks"),
                "FTB Chunks should be absent");
        try {
            Class<?> holder = Class.forName("com.kuronami.claimquest.ClaimQuestTaskTypes");
            Field claim = holder.getField("CLAIM");
            Object type = claim.get(null);
            helper.assertTrue(type != null, "claimquest:claim was not registered without FTB Chunks");
            Object typeId = type.getClass().getMethod("getTypeId").invoke(type);
            helper.assertTrue("claimquest:claim".equals(String.valueOf(typeId)),
                    "unexpected registered task type id: " + typeId);
        } catch (ReflectiveOperationException e) {
            helper.fail("could not inspect Claim Quest task registration: " + e);
        }
        helper.succeed();
    }

    private static boolean onlyMode(GameTestHelper helper, String expected) {
        String mode = System.getProperty("claimquest.fixture.mode", FULL);
        helper.assertTrue(FULL.equals(mode) || WITHOUT_FTB.equals(mode) || WITHOUT_CHUNKS.equals(mode),
                "unknown fixture mode: " + mode);
        if (!expected.equals(mode)) {
            helper.succeed();
            return false;
        }
        return true;
    }

    private static void assertAllHostsPresent(GameTestHelper helper) {
        FabricLoader loader = FabricLoader.getInstance();
        helper.assertTrue(loader.isModLoaded(ClaimQuest.MODID), "Claim Quest main mod missing in full mode");
        helper.assertTrue(loader.isModLoaded("ftbquests"), "FTB Quests missing in full mode");
        helper.assertTrue(loader.isModLoaded("ftbchunks"), "FTB Chunks missing in full mode");
        helper.assertTrue(loader.isModLoaded("ftbteams"), "FTB Teams missing in full mode");
        helper.assertTrue(loader.isModLoaded("ftblibrary"), "FTB Library missing in full mode");
    }

    private static boolean anyHostPresent() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded("ftbquests") || loader.isModLoaded("ftbchunks")
                || loader.isModLoaded("ftbteams") || loader.isModLoaded("ftblibrary");
    }

    private static void assertClassAbsent(GameTestHelper helper, String name) {
        try {
            Class.forName(name, false, ClaimQuestGameTests.class.getClassLoader());
            helper.fail("optional class unexpectedly present: " + name);
        } catch (ClassNotFoundException expected) {
            // The host API is absent from the actual runtime classpath.
        }
    }
}
