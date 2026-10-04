package com.kuronami.claimquest.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * A real {@link ServerPlayer} put through the full login path
 * ({@code PlayerList#placeNewPlayer}) on an {@link EmbeddedChannel}.
 *
 * <p>Going through {@code placeNewPlayer} (rather than a bare constructor)
 * registers the player in the player list and level, adds them to
 * {@code level.players()}, and fires {@code PlayerLoggedInEvent} — which is
 * what FTB Teams listens to when it auto-creates a player team.
 */
record FakeClient(ServerPlayer player, EmbeddedChannel channel) {

    static FakeClient login(ServerLevel level, String name) {
        ServerPlayer player = new ServerPlayer(
                level.getServer(), level, new GameProfile(UUID.randomUUID(), name));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player);
        return new FakeClient(player, channel);
    }
}
