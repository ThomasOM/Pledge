package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.packet.PacketBundleBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
@RequiredArgsConstructor
public class PacketFrameOutboundTailHandler extends ChannelOutboundHandlerAdapter {
    public static final String HANDLER_NAME = "pledge_frame_outbound_tail";

    private final Deque<QueuedMessage> messageQueue = new ConcurrentLinkedDeque<>();

    private final PledgeImpl pledge;
    private final PlayerHandler playerHandler;

    private boolean queue = true;
    private boolean last = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Normal behaviour before handler has started
        if (!this.playerHandler.isActive()) {
            super.write(ctx, msg, promise);
            return;
        }

        // Add to queue or
        if (this.queue) {
            if (this.last) {
                this.messageQueue.addLast(new QueuedMessage(msg, promise));
            } else {
                this.messageQueue.addFirst(new QueuedMessage(msg, promise));
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }

    // Called to drain all queued packets lower into the pipeline and finally flush
    public void drain(ChannelHandlerContext ctx, @Nullable PacketFrame frame) throws Exception {
        // Call flush event right before flushing to allow for some final changes to the queue
        Player player = this.playerHandler.getPlayer();
        Bukkit.getPluginManager().callEvent(new PacketFlushEvent(player, this.messageQueue));

        if (frame != null) {
            int id1 = frame.getId1();
            int id2 = frame.getId2();

            // Transactions
            Object packet1 = this.pledge.getPacketProvider().buildPacket(id1);
            Object packet2 = this.pledge.getPacketProvider().buildPacket(id2);
            this.wrapFirst(ctx, packet1);
            this.wrapLast(ctx, packet2);

            // Packet bundle support
            PacketBundleBuilder bundleBuilder = this.pledge.getPacketBundleBuilder();
            if (bundleBuilder.isSupported()) {
                this.wrapFirst(ctx, bundleBuilder.buildDelimiter());
                this.wrapLast(ctx, bundleBuilder.buildDelimiter());
            }

            // Call frame send event
            Bukkit.getPluginManager().callEvent(new PacketFrameSendEvent(player, frame));
        }

        while (!this.messageQueue.isEmpty()) {
            final QueuedMessage queuedMessage = this.messageQueue.pollFirst();
            ctx.write(queuedMessage.getPacket(), queuedMessage.getPromise());
        }

        // Finally flush all packets at once
        super.flush(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // Only allow flushing from the drain function
        if (!this.playerHandler.isActive() || !this.queue) {
            super.flush(ctx);
        }
    }

    private void wrapFirst(ChannelHandlerContext ctx, Object packet) {
        this.last = false;
        ctx.channel().write(packet);
    }

    private void wrapLast(ChannelHandlerContext ctx, Object packet) {
        this.last = true;
        ctx.channel().write(packet);
    }
}
