package dev.thomazz.pledge.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called at the end of a server tick.
 */
public class TickEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return TickEndEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return TickEndEvent.handlers;
    }
}
