package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called right before when a packet frame is sent to the player client.
 * <p>
 * Note: This event is called from the netty event loop
 */
@Getter
public class PacketFrameSendEvent extends PacketFrameEvent {
    private static final HandlerList handlers = new HandlerList();

    public PacketFrameSendEvent(Player player, PacketFrame current) {
        super(player, current, true);
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameSendEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameSendEvent.handlers;
    }
}
