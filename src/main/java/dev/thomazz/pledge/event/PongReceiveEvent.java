package dev.thomazz.pledge.event;

import dev.thomazz.pledge.pinger.Pinger;
import dev.thomazz.pledge.pinger.PingerEventContext;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a pong packet is received from a {@link Player}
 * If in use, a {@link Pinger} can add {@link PingerEventContext} to the event.
 */
@Getter
@Setter
public class PongReceiveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int id;
    private boolean cancelled = false;

    @Nullable
    private PingerEventContext context;

    public PongReceiveEvent(Player player, int id) {
        super(true);
        this.player = player;
        this.id = id;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return PongReceiveEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PongReceiveEvent.handlers;
    }
}
