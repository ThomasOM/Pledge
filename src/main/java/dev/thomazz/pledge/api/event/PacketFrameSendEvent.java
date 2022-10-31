package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a packet frame is sent to the player.
 */
@Getter
@RequiredArgsConstructor
public class PacketFrameSendEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final PacketFrame current;
	private final PacketFrame next;

	@Override
	public HandlerList getHandlers() {
		return PacketFrameSendEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return PacketFrameSendEvent.handlers;
	}
}
