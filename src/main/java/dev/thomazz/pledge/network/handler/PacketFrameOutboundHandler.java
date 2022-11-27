package dev.thomazz.pledge.network.handler;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.packet.PacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PacketFrameOutboundHandler extends ChannelOutboundHandlerAdapter {
	private final PlayerHandler playerHandler;
	private final PacketProvider packetProvider;

	private final Deque<Object> packetQueue = new ArrayDeque<>(32);

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// Forcibly write and flush disconnect and keep-alive packets
		if (this.packetProvider.isDisconnect(msg) || this.packetProvider.isKeepAlive(msg)) {
			super.write(ctx, msg, promise);
			ctx.flush();
			return;
		}

		this.packetQueue.add(msg);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// Call flush event right before flushing to allow for some final changes
		Player player = this.playerHandler.getPlayer();
		Bukkit.getPluginManager().callEvent(new PacketFlushEvent(player, this.packetQueue));

		// Try to wrap the packet queue in signaling packets if desired
		this.wrapPacketQueue();

		// Drain packet queue writing into handler context
		while (!this.packetQueue.isEmpty()) {
			ctx.write(this.packetQueue.poll());
		}

		// Finally flush all packets at once
		ctx.flush();
	}

	// Wraps packet queue in two signaling packets, creating a new packet frame
	protected void wrapPacketQueue() throws Exception {
		Optional<PacketFrame> next = this.playerHandler.getNextFrame();
		if (next.isPresent()) {
			PacketFrame frame = next.get();
			int id1 = frame.getId1();
			int id2 = frame.getId2();

			Object packet1 = this.packetProvider.buildPacket(id1);
			Object packet2 = this.packetProvider.buildPacket(id2);

			this.packetQueue.addFirst(packet1);
			this.packetQueue.addLast(packet2);

			this.playerHandler.queueFrame();
		}
	}
}
