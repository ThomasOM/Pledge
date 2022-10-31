package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.PacketTickExaminer;
import dev.thomazz.pledge.api.event.ErrorType;
import dev.thomazz.pledge.api.event.PacketFrameSendEvent;
import dev.thomazz.pledge.api.event.ReceiveType;
import dev.thomazz.pledge.api.event.PacketFrameErrorEvent;
import dev.thomazz.pledge.api.event.PacketFrameReceiveEvent;
import dev.thomazz.pledge.network.PacketFrameHandler;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;
import io.netty.channel.Channel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Getter
public class PlayerHandler {
	private final Queue<PacketFrame> frameQueue = new ConcurrentLinkedQueue<>();

	private final Player player;
	private final Channel channel;

	private final int rangeStart;
	private final int rangeEnd;

	private int sendingId;
	private PacketFrame sendingFrame;
	private PacketFrame receivingFrame;

	public PlayerHandler(Player player) throws Exception {
		this.player = player;
		this.channel = MinecraftUtil.getChannelFromPlayer(player);

		PledgeImpl pledge = PledgeImpl.getInstance();
		this.sendingId = this.rangeStart = pledge.getRangeStart();
		this.rangeEnd = pledge.getRangeEnd();

		this.sendingFrame = new PacketFrame(this.getAndUpdateId(), this.getAndUpdateId());
		this.injectPacketFrameHandler(pledge);
	}

	private void injectPacketFrameHandler(PledgeImpl pledge) {
		SignalPacketProvider provider = pledge.getSignalPacketProvider();
		PacketTickExaminer examiner = pledge.getPacketTickExaminer();

		PacketFrameHandler packetFrame = new PacketFrameHandler(this, provider, examiner);
		this.channel.pipeline().addAfter("packet_handler", "pledge_frame_handler", packetFrame);
	}

	private int getAndUpdateId() {
		int previous = this.sendingId;

		int increment = Integer.compare(this.rangeEnd - this.rangeStart, 0);
		this.sendingId += increment;
		if (this.sendingId > this.rangeEnd) {
			this.sendingId = this.rangeStart;
		}

		return previous;
	}

	private void callEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);
	}

	public void receiveId(int id) {
		// Make sure the ID is within the range
		if (id >= Math.min(this.rangeStart, this.rangeEnd) && id <= Math.max(this.rangeStart, this.rangeEnd)) {
			return;
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
	}

	public void incrementFrame() {
		PacketFrame current = this.sendingFrame;
		this.frameQueue.offer(current);
		this.sendingFrame = new PacketFrame(this.getAndUpdateId(), this.getAndUpdateId());
		Bukkit.getPluginManager().callEvent(new PacketFrameSendEvent(this.player, current, this.sendingFrame));
	}
}
