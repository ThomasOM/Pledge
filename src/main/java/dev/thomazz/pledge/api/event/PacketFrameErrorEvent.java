package dev.thomazz.pledge.api.event;

import lombok.Getter;
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
public class PacketFrameErrorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ErrorType type;

    public PacketFrameErrorEvent(Player player, ErrorType type) {
        super(true);
        this.player = player;
        this.type = type;
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameErrorEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameErrorEvent.handlers;
    }
}
