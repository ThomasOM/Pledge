package dev.thomazz.pledge.pinger;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public abstract class AbstractPinger implements Pinger, Listener {
	private final Queue<Integer> expectingIds = new ConcurrentLinkedQueue<>();

	protected final PledgeImpl api;
	protected final Player player;
	protected final Channel channel;

	protected final int startId;
	protected final int endId;

	protected int id;

	public AbstractPinger(PledgeImpl api, Player player, Channel channel, PingerOptions options) {
		this.api = api;
		this.player = player;
		this.channel = channel;

		PingPacketProvider provider = api.getPingProvider();
		int upperBound = provider.getUpperBound();
		int lowerBound = provider.getLowerBound();

		this.startId = Math.max(Math.min(upperBound, options.getStartId()), lowerBound);
		this.endId = Math.max(Math.min(upperBound, options.getEndId()), lowerBound);

		if (this.startId != options.getStartId()) {
			this.api.getLogger().warning("Changed start ID to fit bounds: " + options.getStartId() + " -> " + this.startId);
		}

		if (this.endId != options.getEndId()) {
			this.api.getLogger().warning("Changed end ID to fit bounds: " + options.getEndId() + " -> " + this.endId);
		}
	}

	@Override
	public int startId() {
		return this.startId;
	}

	@Override
	public int endId() {
		return this.endId;
	}

	public abstract void onPongReceive(PongReceiveEvent event);

	public abstract void onTickStart();

	public abstract void onTickEnd();

	public boolean isInRange(int id) {
		return id >= Math.min(this.startId, this.endId) && id <= Math.max(this.startId, this.endId);
	}

	public int pullId() {
		boolean direction = this.endId - this.startId > 0;
		int oldId = this.id;
		int newId = oldId + (direction ? 1 : -1);

		if (direction ? newId > this.endId : newId < this.endId) {
			newId = this.startId;
		}

		this.id = newId;

		this.expectingIds.add(oldId);
		return oldId;
	}

	public boolean confirm(Integer id) {
		if (id.equals(this.expectingIds.peek())) {
			this.expectingIds.poll();
			return true;
		}

		return false;
	}

	public void cleanUp() {
	}
}
