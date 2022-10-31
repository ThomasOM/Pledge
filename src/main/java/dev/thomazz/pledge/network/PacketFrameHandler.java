package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.Pledge;
import dev.thomazz.pledge.api.PacketFrame;
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

	private boolean allowPass = false;
	private boolean written = false;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Integer id = this.signalProvider.idFromPacket(msg);
		if (id != null) {
			this.playerHandler.pushId(id);
		}

		super.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (this.allowPass) {
			super.write(ctx, msg, promise);
		} else {
			this.written = true;
			this.packetQueue.add(msg);
		}
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		if (this.allowPass) {
			super.flush(ctx);
		} else if (!this.written) {
			this.flushPacketQueue(ctx);
		}

		this.written = false;
	}

	// Flushes all of the pending packets in the internal packet queue
	private void flushPacketQueue(ChannelHandlerContext ctx) {
		try {
			this.allowPass = true;

			// Try to wrap the packet queue in signaling packets if desired
			this.tryWrapPacketQueue();

			Object packet;
			while ((packet = this.packetQueue.pollFirst()) != null) {
				ctx.write(packet);
			}

			ctx.flush();
		} finally {
			this.allowPass = false;
		}
	}

	public void tryWrapPacketQueue() {
		try {
			if (this.tickExaminer.shouldTrack(this.packetQueue)) {
				int id1 = this.playerHandler.pullId();
				int id2 = this.playerHandler.pullId();
				this.packetQueue.addFirst(this.signalProvider.buildPacket(id1));
				this.packetQueue.addLast(this.signalProvider.buildPacket(id2));
				this.playerHandler.declareFrame(new PacketFrame(id1, id2));
			}
		} catch (Exception e) {
			Pledge.getInstance().getPlugin().getLogger().severe("PledgeApi could not wrap packet queue!");
			e.printStackTrace();
		}
	}
}
