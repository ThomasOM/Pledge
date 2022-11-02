package dev.thomazz.pledge.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Queue;

/**
 * Called right before all packets are flushed through the pipeline in the packet frame handler.
 * Allows you to modify the packets sent or to track certain packets right before flushing them.
 */
@Getter
@RequiredArgsConstructor
public class PacketFlushEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Queue<Object> packets;

    @Override
    public HandlerList getHandlers() {
        return PacketFlushEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFlushEvent.handlers;
    }
}
