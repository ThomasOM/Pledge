package dev.thomazz.pledge.network.handler;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class PacketFrameWriteFlushHandler extends PacketFrameWriteHandler {
    private boolean written = false;

    public PacketFrameWriteFlushHandler(PlayerHandler playerHandler, SignalPacketProvider signalProvider) {
        super(playerHandler, signalProvider);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        // Need to use a different handler
        if (this.written) {
            throw new IllegalStateException("Detected multiple writes! Please change the packet write policy to WRITE");
        }

        // Queue all written packets to be flushed later
        this.written = true;
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // If the operation was a writeAndFlush call it means we haven't reached the network tick yet
        if (!this.written) {
            super.flush(ctx);
        }

        this.written = false;
    }
}
