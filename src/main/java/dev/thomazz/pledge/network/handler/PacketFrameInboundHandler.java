package dev.thomazz.pledge.network.handler;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.packet.PacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketFrameInboundHandler extends ChannelInboundHandlerAdapter {
    private final PlayerHandler playerHandler;
    private final PacketProvider signalProvider;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Pass received signaling packet ids to the player handler and filtering the packets out for the next handler
        Integer id = this.signalProvider.idFromPacket(msg);
        if (id != null && this.playerHandler.processId(id)) {
            return;
        }

        super.channelRead(ctx, msg);
    }
}
