package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class PacketFrameEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final PacketFrame frame;
	private final FrameEventType type;

	@Override
	public HandlerList getHandlers() {
		return PacketFrameEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return PacketFrameEvent.handlers;
	}
}
