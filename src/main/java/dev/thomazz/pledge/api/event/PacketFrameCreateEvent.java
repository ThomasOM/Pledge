package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.Pledge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when a packet frame is created by {@link Pledge#getOrCreateFrame(UUID)}
 * Can also be created by the internal tick task specified with {@link Pledge#setFrameInterval(int)}
 */
public class PacketFrameCreateEvent extends PacketFrameEvent {
    private static final HandlerList handlers = new HandlerList();

    public PacketFrameCreateEvent(Player player, PacketFrame created) {
        super(player, created, !Bukkit.isPrimaryThread()); // Event can be either on or off main thread
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameCreateEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameCreateEvent.handlers;
    }
}
