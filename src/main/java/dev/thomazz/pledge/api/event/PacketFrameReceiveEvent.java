package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when receiving a response from the client corresponding to a {@link PacketFrame}
 * For extra info see {@link ReceiveType}
 * <p>
 * Note: This event is called from the netty event loop
 */
@Getter
public class PacketFrameReceiveEvent extends PacketFrameEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ReceiveType type;

    public PacketFrameReceiveEvent(Player player, PacketFrame frame, ReceiveType type) {
        super(player, frame, true);
        this.type = type;
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameReceiveEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameReceiveEvent.handlers;
    }
}
