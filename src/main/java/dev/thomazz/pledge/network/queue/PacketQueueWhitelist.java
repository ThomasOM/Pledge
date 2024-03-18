package dev.thomazz.pledge.network.queue;

import com.google.common.collect.ImmutableList;
import dev.thomazz.pledge.util.MinecraftReflection;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PacketQueueWhitelist {
    private final List<Class<?>> whitelistedPackets = PacketQueueWhitelist.buildWhitelistedPackets();

    private List<Class<?>> buildWhitelistedPackets() {
        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        PacketQueueWhitelist.addGamePacket(builder, "PacketPlayOutKeepAlive");
        PacketQueueWhitelist.addGamePacket(builder, "ClientboundKeepAlivePacket");
        PacketQueueWhitelist.addGamePacket(builder, "PacketPlayOutKickDisconnect");
        PacketQueueWhitelist.addGamePacket(builder, "ClientboundDisconnectPacket");
        return builder.build();
    }

    private void addGamePacket(ImmutableList.Builder<Class<?>> builder, String packetName) {
        try {
            builder.add(MinecraftReflection.gamePacket(packetName));
        } catch (Exception ignored) {
        }
    }

    // If a packet should be added to the packet queue or instantly sent to players
    public boolean isWhitelisted(Object packet) {
        return PacketQueueWhitelist.whitelistedPackets.stream().anyMatch(type -> type.isInstance(packet));
    }
}
