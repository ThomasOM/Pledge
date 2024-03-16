package dev.thomazz.pledge.packet;

import com.google.common.collect.ImmutableList;
import dev.thomazz.pledge.util.MinecraftReflection;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PacketWhitelist {
    private final List<Class<?>> whitelistedPackets = PacketWhitelist.buildWhitelistedPackets();

    private List<Class<?>> buildWhitelistedPackets() {
        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        PacketWhitelist.addGamePacket(builder, "PacketPlayOutKeepAlive");
        PacketWhitelist.addGamePacket(builder, "ClientboundKeepAlivePacket");
        PacketWhitelist.addGamePacket(builder, "PacketPlayOutKickDisconnect");
        PacketWhitelist.addGamePacket(builder, "ClientboundDisconnectPacket");
        return builder.build();
    }

    private void addGamePacket(ImmutableList.Builder<Class<?>> builder, String packetName) {
        try {
            builder.add(MinecraftReflection.gamePacket(packetName));
        } catch (Exception ignored) {
        }
    }

    public boolean isWhiteListed(Object packet) {
        return PacketWhitelist.whitelistedPackets.stream().anyMatch(type -> type.isInstance(packet));
    }
}
