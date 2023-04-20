package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PacketFrameOutboundTailHandler extends ChannelOutboundHandlerAdapter implements AutoCloseable {
    public static final String HANDLER_NAME = "pledge_frame_outbound_tail";

    private final PlayerHandler playerHandler;
    private boolean discard = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Normal behaviour before handler has started
        if (!this.playerHandler.isActive()) {
            super.write(ctx, msg, promise);
            return;
        }

        if (this.discard) {
            ReferenceCountUtil.release(msg);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void close() {
        this.discard = true;
    }

    public PacketFrameOutboundTailHandler open() {
        this.discard = false;
        return this;
    }
}
