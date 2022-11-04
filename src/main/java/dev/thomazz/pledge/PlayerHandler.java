package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.ErrorType;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.api.event.ReceiveType;
import dev.thomazz.pledge.api.event.PacketFrameErrorEvent;
import dev.thomazz.pledge.api.event.PacketFrameReceiveEvent;
import dev.thomazz.pledge.network.PacketFrameHandlerFactory;
import dev.thomazz.pledge.network.delegation.DelegateChannelFactory;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class PlayerHandler {
	private final Queue<PacketFrame> frameQueue = new ConcurrentLinkedQueue<>();

	@Getter
	private final Player player;

	private final Channel channel;
	private final Channel original;

	private final int rangeStart;
	private final int rangeEnd;

	private int id;
	private PacketFrame nextFrame;
	private PacketFrame receivingFrame;

	public PlayerHandler(Player player) throws Exception {
		this.player = player;

		this.original = MinecraftUtil.getChannel(player);
		this.channel = DelegateChannelFactory.buildDelegateChannel(this.original);
		MinecraftUtil.setChannel(player, this.channel);

		PledgeImpl pledge = PledgeImpl.getInstance();
		this.id = this.rangeStart = pledge.getRangeStart();
		this.rangeEnd = pledge.getRangeEnd();

		this.nextFrame = new PacketFrame(this.getAndUpdateId(), this.getAndUpdateId());
		this.injectPacketFrameHandlers(pledge);
	}

	private void injectPacketFrameHandlers(PledgeImpl pledge) {
		PacketProvider provider = pledge.getPacketProvider();
		ChannelHandler outbound = PacketFrameHandlerFactory.buildOutbound(this, provider);
		ChannelHandler inbound =  PacketFrameHandlerFactory.buildInbound(this, provider);

		// We want to be right after the encoder and decoder so there's no interference with other packet listeners
		this.channel.pipeline().addAfter("encoder", "pledge_frame_outbound", outbound);
		this.channel.pipeline().addAfter("decoder", "pledge_frame_inbound", inbound);
	}

	private int getAndUpdateId() {
		int previous = this.id;

		int increment = Integer.compare(this.rangeEnd - this.rangeStart, 0);
		this.id += increment;
		if (this.rangeEnd > this.rangeStart ? this.id > this.rangeEnd : this.id < this.rangeEnd) {
			this.id = this.rangeStart;
		}

		return previous;
	}

	private void callEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);
	}

	public boolean processId(int id) {
		// Make sure the ID is within the range
		if (id < Math.min(this.rangeStart, this.rangeEnd) || id > Math.max(this.rangeStart, this.rangeEnd)) {
			return false;
		}

		if (this.receivingFrame == null) {
			PacketFrame frame = this.frameQueue.peek();
			if (frame != null && frame.getId1() == id) {
				this.receivingFrame = this.frameQueue.poll();
				this.callEvent(new PacketFrameReceiveEvent(this.player, frame, ReceiveType.RECEIVE_START));
			} else {
				this.callEvent(new PacketFrameErrorEvent(this.player, ErrorType.MISSING_FRAME));
			}
		} else {
			if (this.receivingFrame.getId2() == id) {
				this.callEvent(new PacketFrameReceiveEvent(this.player, this.receivingFrame, ReceiveType.RECEIVE_END));
				this.receivingFrame = null;
			} else {
				this.callEvent(new PacketFrameErrorEvent(this.player, ErrorType.INCOMPLETE_FRAME));
			}
		}

		return true;
	}

	public void queueFrame() {
		this.frameQueue.offer(this.nextFrame);
		Bukkit.getPluginManager().callEvent(new PacketFrameSendEvent(this.player, this.nextFrame));
		this.nextFrame = null;
	}

	public Optional<PacketFrame> getNextFrame() {
		return Optional.ofNullable(this.nextFrame);
	}

	// Creates a new frame for the current tick if there is not already one
	public PacketFrame createNextFrame() {
		if (this.nextFrame == null) {
			this.nextFrame = new PacketFrame(this.getAndUpdateId(), this.getAndUpdateId());
		}

		return this.nextFrame;
	}

	public void cleanUp() {
		try {
			MinecraftUtil.setChannel(this.player, this.original);
			this.channel.pipeline().remove("pledge_frame_outbound");
			this.channel.pipeline().remove("pledge_frame_inbound");
		} catch (NoSuchElementException ignored) {
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
