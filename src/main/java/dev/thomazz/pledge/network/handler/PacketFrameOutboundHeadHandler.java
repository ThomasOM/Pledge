package dev.thomazz.pledge.network.handler;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.packet.PacketBundleBuilder;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.ChannelHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PacketFrameOutboundHeadHandler extends ChannelOutboundHandlerAdapter {
	public static final String HANDLER_NAME = "pledge_frame_outbound_head";

	private final Queue<PacketFrame> flushFrames = new ConcurrentLinkedQueue<>();
	private final Deque<Object> messageQueue = new ArrayDeque<>();

	private final PledgeImpl pledge;
	private final PlayerHandler playerHandler;
	private final PacketProvider packetProvider;
	private final PacketFrameOutboundTailHandler tailHandler;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// Communicate to queue handler that some packets should be discarded or not since we queue them here
		if (this.packetProvider.isDisconnect(msg) || this.packetProvider.isKeepAlive(msg)) {
			this.tailHandler.setDiscard(false);
			super.write(ctx, msg, promise);
			super.flush(ctx);
		} else {
			this.messageQueue.add(msg);
			this.tailHandler.setDiscard(true);
			super.write(ctx, msg, promise);
		}
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// Call flush event right before flushing to allow for some final changes to the queue
		Player player = this.playerHandler.getPlayer();
		Bukkit.getPluginManager().callEvent(new PacketFlushEvent(player, this.messageQueue));

		// Try to wrap the queue in signaling packets if desired
		PacketFrame frame = this.flushFrames.poll();
		if (frame != null) {
			int id1 = frame.getId1();
			int id2 = frame.getId2();

			// Transactions
			Object packet1 = this.packetProvider.buildPacket(id1);
			Object packet2 = this.packetProvider.buildPacket(id2);
			this.messageQueue.addFirst(packet1);
			this.messageQueue.addLast(packet2);

			// Packet bundle support
			PacketBundleBuilder bundleBuilder = this.pledge.getPacketBundleBuilder();
			if (bundleBuilder.isSupported()) {
				this.messageQueue.addFirst(bundleBuilder.buildDelimiter());
				this.messageQueue.addLast(bundleBuilder.buildDelimiter());
			}

			// Call frame send event
			Bukkit.getPluginManager().callEvent(new PacketFrameSendEvent(player, frame));
		}

		// Drain all queued packets to target and flush
		this.tailHandler.setDiscard(false);
		try {
			ChannelHandlerContext target = ctx.pipeline().context(PacketFrameOutboundTailHandler.HANDLER_NAME);

			Object packet;
			while ((packet = this.messageQueue.poll()) != null) {
				target.write(ChannelHelper.encodeAndCompress(ctx, packet));
			}

			super.flush(ctx);
		} finally {
			this.tailHandler.setDiscard(true);
		}
	}

	public void flushFrame(PacketFrame frame) {
		this.flushFrames.add(frame);
	}
}
