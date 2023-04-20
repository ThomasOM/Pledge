package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when an error is detected in the frame order.
 * For extra info see {@link ErrorType}
 * <p>
 * Note: This event is called from the netty event loop
 */
@Getter
public class PacketFrameErrorEvent extends PacketFrameEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ErrorType type;

    public PacketFrameErrorEvent(Player player, PacketFrame frame, ErrorType type) {
        super(player, frame, true);
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
