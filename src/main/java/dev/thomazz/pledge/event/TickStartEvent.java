package dev.thomazz.pledge.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called at the start of a server tick.
 */
public class TickStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return TickStartEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return TickStartEvent.handlers;
    }
}
