package com.kuronami.claimquest.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/** Uses Fabric's vanilla-backed fake player, which traverses PlayerList.placeNewPlayer. */
record FakeClient(ServerPlayer player) {
    static FakeClient login(GameTestHelper helper, String ignoredName) {
        return new FakeClient(helper.makeMockServerPlayerInLevel());
    }
}
