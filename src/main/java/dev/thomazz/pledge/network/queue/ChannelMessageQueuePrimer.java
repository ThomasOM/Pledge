package dev.thomazz.pledge.network.queue;

import dev.thomazz.pledge.packet.PacketWhitelist;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelMessageQueuePrimer extends ChannelOutboundHandlerAdapter {
    private final ChannelMessageQueueHandler queueHandler;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Let whitelisted packets pass through the queue
        if (PacketWhitelist.isWhiteListed(msg)) {
            QueueMode lastMode = this.queueHandler.getMode();
            this.queueHandler.setMode(QueueMode.PASS);
            try {
                super.write(ctx, msg, promise);
                super.flush(ctx);
            } finally {
                this.queueHandler.setMode(lastMode);
            }
            return;
        }

        super.write(ctx, msg, promise);
    }
}
