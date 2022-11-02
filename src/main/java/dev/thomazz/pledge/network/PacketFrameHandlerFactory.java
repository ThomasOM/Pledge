package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.api.PacketWritePolicy;
import dev.thomazz.pledge.network.handler.PacketFrameInboundHandler;
import dev.thomazz.pledge.network.handler.PacketFrameWriteFlushHandler;
import dev.thomazz.pledge.network.handler.PacketFrameWriteHandler;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import io.netty.channel.ChannelHandler;

public final class PacketFrameHandlerFactory {
    public static ChannelHandler buildInbound(PlayerHandler handler, SignalPacketProvider provider) {
        return new PacketFrameInboundHandler(handler, provider);
    }

    public static ChannelHandler buildOutbound(PlayerHandler handler, SignalPacketProvider provider, PacketWritePolicy policy) {
        switch (policy) {
            default:
            case WRITE:
                return new PacketFrameWriteHandler(handler, provider);
            case WRITE_FLUSH:
                return new PacketFrameWriteFlushHandler(handler, provider);
        }
    }
}
