package dev.thomazz.pledge.network.handler;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.PacketFlushEvent;
import dev.thomazz.pledge.packet.PacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PacketFrameOutboundHandler extends ChannelOutboundHandlerAdapter {
	private final PlayerHandler playerHandler;
	private final PacketProvider packetProvider;
	private final PacketFrameOutboundQueueHandler queueHandler;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// Communicate to queue handler that some packets are allowed to pass
		if (this.packetProvider.isDisconnect(msg) || this.packetProvider.isKeepAlive(msg)) {
			this.queueHandler.setState(PacketQueueState.PASS);
		} else {
			this.queueHandler.setState(PacketQueueState.QUEUE_LAST);
		}

		super.write(ctx, msg, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// Call flush event right before flushing to allow for some final changes to the queue
		Player player = this.playerHandler.getPlayer();
		Bukkit.getPluginManager().callEvent(new PacketFlushEvent(player, this.queueHandler.getMessageQueue()));

		// Try to wrap the queue in signaling packets if desired
		Optional<PacketFrame> next = this.playerHandler.getNextFrame();
		if (next.isPresent()) {
			PacketFrame frame = next.get();
			int id1 = frame.getId1();
			int id2 = frame.getId2();

			Object packet1 = this.packetProvider.buildPacket(id1);
			Object packet2 = this.packetProvider.buildPacket(id2);

			this.queueHandler.setState(PacketQueueState.QUEUE_FIRST);
			ctx.write(packet1);
			this.queueHandler.setState(PacketQueueState.QUEUE_LAST);
			ctx.write(packet2);

			this.playerHandler.queueFrame();
		}

		// Finally flush all packets at once
		super.flush(ctx);
	}
}
