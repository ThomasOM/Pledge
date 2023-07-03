package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.packet.PacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketFrameInboundHandler extends ChannelInboundHandlerAdapter {
    public static final String HANDLER_NAME = "pledge_frame_inbound";

    private final PlayerHandler playerHandler;
    private final PacketProvider packetProvider;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Pass received signaling packet ids to the player handler
        Integer id = this.packetProvider.idFromPacket(msg);
        if (id != null) {
            this.playerHandler.processId(id);
        }

        super.channelRead(ctx, msg);
    }
}
