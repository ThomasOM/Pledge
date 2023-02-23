package dev.thomazz.pledge.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
@Setter
public class PacketFrameOutboundQueueHandler extends ChannelOutboundHandlerAdapter {
    public static final String HANDLER_NAME = "pledge_frame_outbound_queue";

    private final Deque<Object> messageQueue = new ArrayDeque<>();

    // State used to determine what to do with arriving messages
    private PacketQueueState state = PacketQueueState.QUEUE_LAST;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        switch (this.state) {
            case QUEUE_LAST:
                this.messageQueue.addLast(msg);
                break;
            case QUEUE_FIRST:
                this.messageQueue.addFirst(msg);
                break;
            case PASS:
                super.write(ctx, msg, promise);
                super.flush(ctx);
                break;
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // Drain packet queue writing into handler context
        while (!this.messageQueue.isEmpty()) {
            ctx.write(this.messageQueue.poll());
        }

        super.flush(ctx);
    }
}
