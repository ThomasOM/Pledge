package dev.thomazz.pledge.pinger.bundle;

import dev.thomazz.pledge.pinger.Pinger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@RequiredArgsConstructor
public class BundlePingData {
	private final Queue<Integer> expectingIds = new ConcurrentLinkedQueue<>();
	private final Pinger pinger;
	private final Player player;

	private int id;

	public int pullId() {
		int startId = this.pinger.startId();
		int endId = this.pinger.endId();

		boolean direction = endId - startId > 0;
		int oldId = this.id;
		int newId = oldId + (direction ? 1 : -1);

		if (direction ? newId > endId : newId < endId) {
			newId = startId;
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
}
