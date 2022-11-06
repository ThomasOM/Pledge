package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.Pledge;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when a frame isn't received back within a certain amount of ticks.
 * The amount of ticks until timeout can be set using {@link Pledge#setTimeoutTicks(int)}
 */
@Getter
public class PacketFrameTimeoutEvent extends PacketFrameEvent {
    private static final HandlerList handlers = new HandlerList();

    public PacketFrameTimeoutEvent(Player player, PacketFrame frame) {
        super(player, frame, false);
    }

    @Override
    public HandlerList getHandlers() {
        return PacketFrameTimeoutEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PacketFrameTimeoutEvent.handlers;
    }
}
