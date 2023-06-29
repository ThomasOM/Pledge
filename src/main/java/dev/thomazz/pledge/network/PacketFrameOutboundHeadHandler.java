package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PlayerHandler;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.ActivateHandlerEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class PacketFrameOutboundHeadHandler extends ChannelOutboundHandlerAdapter {
	public static final String HANDLER_NAME = "pledge_frame_outbound_head";

	private final PledgeImpl pledge;
	private final PlayerHandler playerHandler;
	private final PacketFrameOutboundTailHandler tailHandler;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// Only handle packet messages
		if (msg instanceof ByteBuf) {
			super.write(ctx, msg, promise);
			return;
		}

		// Login event handling
		if (this.pledge.getPacketProvider().isLogin(msg)) {
			Bukkit.getPluginManager().callEvent(new ActivateHandlerEvent(this.playerHandler.getPlayer(), ctx.channel()));
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
		if (this.pledge.getPacketProvider().isDisconnect(msg) || this.pledge.getPacketProvider().isKeepAlive(msg)) {
			this.tailHandler.setQueue(false);
			try {
				super.write(ctx, msg, promise);
				super.flush(ctx);
			} finally {
				this.tailHandler.setQueue(true);
			}
		} else {
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

	public void start() {
		this.playerHandler.setActive(true);
	}
}
