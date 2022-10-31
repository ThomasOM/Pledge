package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.PacketTickExaminer;
import dev.thomazz.pledge.api.PledgeSettings;
import dev.thomazz.pledge.api.event.ErrorType;
import dev.thomazz.pledge.api.event.FrameEventType;
import dev.thomazz.pledge.api.event.PacketFrameErrorEvent;
import dev.thomazz.pledge.api.event.PacketFrameEvent;
import dev.thomazz.pledge.network.PacketFrameHandler;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;
import io.netty.channel.Channel;
import java.util.ArrayDeque;
import java.util.Queue;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Getter
public class PlayerHandler {
	private final Queue<PacketFrame> frameQueue = new ArrayDeque<>();

	private final Player player;
	private final Channel channel;

	private final int rangeStart;
	private final int rangeEnd;

	private int serverId;
	private PacketFrame receivingFrame;

	public PlayerHandler(Player player) throws Exception {
		this.player = player;
		this.channel = MinecraftUtil.getChannelFromPlayer(player);

		Pledge pledge = Pledge.getInstance();
		PledgeSettings settings = pledge.getSettings();

		this.serverId = this.rangeStart = settings.getRangeStart();
		this.rangeEnd = settings.getRangeEnd();

		this.injectPacketFrameHandler(pledge);
	}

	private void injectPacketFrameHandler(Pledge pledge) {
		SignalPacketProvider provider = pledge.getSignalPacketProvider();
		PacketTickExaminer examiner = pledge.getSettings().getPacketTickExaminer();

		PacketFrameHandler packetFrame = new PacketFrameHandler(this, provider, examiner);
		this.channel.pipeline().addAfter("packet_handler", "pledge_frame_handler", packetFrame);
	}

	public int pullId() {
		int previous = this.serverId;
		this.updateId();
		return previous;
	}

	public void pushId(int id) {
		// Make sure the ID is within the range
		if (id >= Math.min(this.rangeStart, this.rangeEnd) && id <= Math.max(this.rangeStart, this.rangeEnd)) {
			return;
		}

		if (this.receivingFrame == null) {
			PacketFrame frame = this.frameQueue.poll();
			if (frame != null && frame.getId1() == id) {
				this.receivingFrame = frame;
				this.callEvent(new PacketFrameEvent(this.player, frame, FrameEventType.RECEIVE_START));
			} else {
				this.callEvent(new PacketFrameErrorEvent(this.player, ErrorType.MISSING_FRAME));
			}
		} else {
			if (this.receivingFrame.getId2() == id) {
				this.callEvent(new PacketFrameEvent(this.player, this.receivingFrame, FrameEventType.RECEIVE_END));
				this.receivingFrame = null;
			} else {
				this.callEvent(new PacketFrameErrorEvent(this.player, ErrorType.INCOMPLETE_FRAME));
			}
		}
	}

	public void declareFrame(PacketFrame frame) {
		this.frameQueue.add(frame);
		Bukkit.getPluginManager().callEvent(new PacketFrameEvent(this.player, frame, FrameEventType.SEND));
	}

	private void updateId() {
		int increment = Integer.compare(this.rangeEnd - this.rangeStart, 0);
		this.serverId += increment;
		if (this.serverId > this.rangeEnd) {
			this.serverId = this.rangeStart;
		}
	}

	private void callEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);
	}
}
