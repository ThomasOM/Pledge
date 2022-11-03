package dev.thomazz.pledge.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an error is detected in the frame order.
 * For extra info see {@link ErrorType}
 *
 * Note: This event is called from the netty event loop
 */
@Getter
@RequiredArgsConstructor
public class PacketFrameErrorEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final ErrorType type;

	@Override
	public HandlerList getHandlers() {
		return PacketFrameErrorEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return PacketFrameErrorEvent.handlers;
	}
}
