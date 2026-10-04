package com.kuronami.claimquest.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

/** Boolean assertions compatible with the Minecraft 1.19.2 GameTest API. */
final class TestAssertions {
    private TestAssertions() {}

    static void assertTrue(GameTestHelper helper, boolean value, String message) {
        if (!value) helper.fail(message);
    }

    static void assertFalse(GameTestHelper helper, boolean value, String message) {
        if (value) helper.fail(message);
    }
}
