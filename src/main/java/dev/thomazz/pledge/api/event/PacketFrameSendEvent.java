package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a packet frame is sent to the player.
 *
 * Note: This event is called from the netty event loop
 */
@Getter
public class PacketFrameSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final PacketFrame current;

    public PacketFrameSendEvent(Player player, PacketFrame current) {
        super(true);
        this.player = player;
        this.current = current;
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameSendEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameSendEvent.handlers;
    }
}
