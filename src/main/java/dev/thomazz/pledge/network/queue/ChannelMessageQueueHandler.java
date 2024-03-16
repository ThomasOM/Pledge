package dev.thomazz.pledge.network.queue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Setter
@Getter
public class ChannelMessageQueueHandler extends ChannelOutboundHandlerAdapter {
    private final Deque<Object> messageQueue = new ConcurrentLinkedDeque<>();
    private QueueMode mode = QueueMode.PASS;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        switch (this.mode) {
            default:
            case ADD_FIRST:
                this.messageQueue.addFirst(msg);
                promise.setSuccess();
                break;
            case ADD_LAST:
                this.messageQueue.addLast(msg);
                promise.setSuccess();
                break;
            case PASS:
                super.write(ctx, msg, promise);
                break;
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.drain(ctx);
        super.close(ctx, promise);
    }

    public void drain(ChannelHandlerContext ctx) {
        this.messageQueue.forEach(ctx::write);
        this.messageQueue.clear();
        ctx.flush();
    }
}
