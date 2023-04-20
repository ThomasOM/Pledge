package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.api.event.ActivateHandlerEvent;
import dev.thomazz.pledge.packet.PacketBundleBuilder;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.ChannelHelper;
import io.netty.buffer.ByteBuf;
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
		// What the fuck
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) msg;
			this.pledge.getPlugin().getLogger().severe("buf: " + buf.readableBytes());
			ctx.channel().pipeline().forEach(entry -> this.pledge.getPlugin().getLogger().severe(entry.getKey()));
			super.write(ctx, msg, promise);
			return;
		}

		// Login event handling
		if (this.packetProvider.isLogin(msg)) {
			Player player = this.playerHandler.getPlayer();
			Bukkit.getPluginManager().callEvent(new ActivateHandlerEvent(player));
			super.write(ctx, msg, promise);
			this.start();
			return;
		}

		// Normal behaviour before handler starts
		if (!this.playerHandler.isActive()) {
			super.write(ctx, msg, promise);
			return;
		}

		// Communicate to queue handler that some packets should be discarded or not since we queue them here
		if (this.packetProvider.isDisconnect(msg) || this.packetProvider.isKeepAlive(msg)) {
			try (AutoCloseable ignored = this.tailHandler.open()) {
				super.write(ctx, msg, promise);
				super.flush(ctx);
			}
		} else {
			this.messageQueue.add(msg);
			super.write(ctx, msg, promise);
		}
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// Do not allow explicit flushing when started
		if (!this.playerHandler.isActive()) {
			super.flush(ctx);
		}
	}

	// Called to drain all queued packets lower into the pipeline and finally flush
	public void drain(ChannelHandlerContext ctx) throws Exception {
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

		// Drain all queued packets after the tail handler
		ChannelHandlerContext target = ctx.pipeline().context(PacketFrameOutboundTailHandler.HANDLER_NAME);

		Object packet;
		while ((packet = this.messageQueue.poll()) != null) {
			target.write(ChannelHelper.encodeAndCompress(ctx, packet));
		}

		// Finally flush all packets at once
		super.flush(ctx);
	}

	public void flushFrame(PacketFrame frame) {
		this.flushFrames.add(frame);
	}

	public void start() {
		this.playerHandler.setActive(true);
	}
}
