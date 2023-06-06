package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.packet.PacketBundleBuilder;
import dev.thomazz.pledge.util.ChannelHelper;
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

    private final Deque<Object> messageQueue = new ConcurrentLinkedDeque<>();

    private final PledgeImpl pledge;
    private final PlayerHandler playerHandler;
    private boolean queue = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Normal behaviour before handler has started
        if (!this.playerHandler.isActive()) {
            super.write(ctx, msg, promise);
            return;
        }

        if (this.queue) {
            this.messageQueue.add(msg);
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
            this.messageQueue.addFirst(ChannelHelper.encodeAndCompress(ctx, packet1));
            this.messageQueue.addLast(ChannelHelper.encodeAndCompress(ctx, packet2));

            // Packet bundle support
            PacketBundleBuilder bundleBuilder = this.pledge.getPacketBundleBuilder();
            if (bundleBuilder.isSupported()) {
                this.messageQueue.addFirst(ChannelHelper.encodeAndCompress(ctx, bundleBuilder.buildDelimiter()));
                this.messageQueue.addLast(ChannelHelper.encodeAndCompress(ctx, bundleBuilder.buildDelimiter()));
            }

            // Call frame send event
            Bukkit.getPluginManager().callEvent(new PacketFrameSendEvent(player, frame));
        }

        // Drain all queued packets after the tail handler
        ChannelHandlerContext target = ctx.pipeline().context(PacketFrameOutboundTailHandler.HANDLER_NAME);

        Object message;
        while ((message = this.messageQueue.pollFirst()) != null) {
            target.write(message);
        }

        // Finally flush all packets at once
        super.flush(ctx);
    }
}
