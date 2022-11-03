package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when receiving a response from the client corresponding to a {@link PacketFrame}
 * For extra info see {@link ReceiveType}
 *
 * Note: This event is called from the netty event loop
 */
@Getter
@RequiredArgsConstructor
public class PacketFrameReceiveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final PacketFrame frame;
	private final ReceiveType type;

	@Override
	public HandlerList getHandlers() {
		return PacketFrameReceiveEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return PacketFrameReceiveEvent.handlers;
	}
}
