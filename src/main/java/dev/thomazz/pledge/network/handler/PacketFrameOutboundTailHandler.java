package dev.thomazz.pledge.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketFrameOutboundTailHandler extends ChannelOutboundHandlerAdapter {
    public static final String HANDLER_NAME = "pledge_frame_outbound_queue";
    private boolean discard = false;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!this.discard) {
            super.write(ctx, msg, promise);
            super.flush(ctx);
        }
    }
}
