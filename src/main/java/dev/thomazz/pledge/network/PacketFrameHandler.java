package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.api.PacketTickExaminer;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketFrameHandler extends ChannelDuplexHandler {
	private final PlayerHandler playerHandler;
	private final SignalPacketProvider signalProvider;
	private final PacketTickExaminer tickExaminer;

	private final Deque<Object> packetQueue = new ArrayDeque<>(32);
	private boolean written = false;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// Pass received signaling packet ids to the player handler
		Integer id = this.signalProvider.idFromPacket(msg);
		if (id != null) {
			this.playerHandler.receiveId(id);
		}

		super.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		// Queue all written packets to be flushed later
		this.written = true;
		this.packetQueue.add(msg);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// If the operation was a writeAndFlush call it means we haven't reached the network tick yet
		if (!this.written) {
			this.flushPacketQueue(ctx);
		}

		this.written = false;
	}

	// Flushes all of the pending packets in the internal packet queue
	private void flushPacketQueue(ChannelHandlerContext ctx) throws Exception {
		// Try to wrap the packet queue in signaling packets if desired
		this.tryWrapPacketQueue();

		// Drain packet queue writing into handler context
		while (!this.packetQueue.isEmpty()) {
			ctx.write(this.packetQueue.poll());
		}

		// Finally flush all packets at once
		ctx.flush();
	}

	// Wraps packet queue in two signaling packets, creating a new packet frame
	public void tryWrapPacketQueue() throws Exception {
		if (this.tickExaminer.shouldTrack(this.packetQueue)) {
			int id1 = this.playerHandler.getSendingFrame().getId1();
			int id2 = this.playerHandler.getSendingFrame().getId2();

			this.packetQueue.addFirst(this.signalProvider.buildPacket(id1));
			this.packetQueue.addLast(this.signalProvider.buildPacket(id2));

			this.playerHandler.incrementFrame();
		}
	}
}
