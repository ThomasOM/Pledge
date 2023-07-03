package dev.thomazz.pledge.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An initial transaction to validate the connection is sent.
 * The main purpose of this is to synchronize the client connection with the server.
 * When using a forwarding proxy the client connection can still send packets intended for different servers.
 * After this event has been called, all responses from the client can be guaranteed to be intended for this server.
 */
@Getter
public class ConnectionValidateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int validationId;

    public ConnectionValidateEvent(Player player, int validationId) {
        super(true);
        this.player = player;
        this.validationId = validationId;
    }

    @Override
    public HandlerList getHandlers() {
        return ConnectionValidateEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return ConnectionValidateEvent.handlers;
    }
}
