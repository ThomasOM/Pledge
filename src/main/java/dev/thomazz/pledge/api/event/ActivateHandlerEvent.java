package dev.thomazz.pledge.api.event;

import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the play login packet is sent to the player from the server.
 * This indicates the start of the play game state and activates the player handler.
 */
@Getter
public class ActivateHandlerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Channel channel;

    public ActivateHandlerEvent(Player player, Channel channel) {
        super(true);
        this.player = player;
        this.channel = channel;
    }

    @Override
    public HandlerList getHandlers() {
        return ActivateHandlerEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return ActivateHandlerEvent.handlers;
    }
}
