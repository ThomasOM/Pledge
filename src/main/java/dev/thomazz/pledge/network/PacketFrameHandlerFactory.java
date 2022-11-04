package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.network.handler.PacketFrameInboundHandler;
import dev.thomazz.pledge.network.handler.PacketFrameOutboundHandler;
import dev.thomazz.pledge.packet.PacketProvider;
import io.netty.channel.ChannelHandler;

public final class PacketFrameHandlerFactory {
    public static ChannelHandler buildInbound(PlayerHandler handler, PacketProvider provider) {
        return new PacketFrameInboundHandler(handler, provider);
    }

    public static ChannelHandler buildOutbound(PlayerHandler handler, PacketProvider provider) {
        return new PacketFrameOutboundHandler(handler, provider);
    }
}
