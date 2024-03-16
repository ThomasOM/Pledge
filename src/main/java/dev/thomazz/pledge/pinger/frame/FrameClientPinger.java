package dev.thomazz.pledge.pinger.frame;

import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.frame.data.Frame;
import org.bukkit.entity.Player;

/**
 * Implementation of a {@link ClientPinger} with extra functionality to determine for each tick if pings should be sent.
 * <p>
 * If a frame is created using {@link #getOrCreate(Player)},
 * all packets for the current server tick will have a ping sent before and after them.
 */
public interface FrameClientPinger extends ClientPinger {
    /**
     * Creates a frame, scheduling pings to be sent before and after all packets in the current server tick.
     * <p>
     * @param player - Player to create frame for
     * @return       - IDs of pings sent before {@link Frame#getStartId()} and after packets {@link Frame#getEndId()}
     */
    Frame getOrCreate(Player player);

    /**
     * Attaches a listener to listen to any events for {@link Frame} objects.
     * <p>
     * @param listener - Listener to attach
     */
    void attach(FrameListener listener);
}
