package dev.thomazz.pledge.pinger;

import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Utility to send pings automatically and the start and end of a tick.
 * Several events can be listened to through a {@link ClientPingerListener}, such as when pings are sent and received.
 */
public interface ClientPinger {
    /**
     * Start of the ID range for pings used by this instance.
     * <p>
     * @return - Start ID
     */
    int startId();

    /**
     * End of the ID range for pings used by this instance.
     * <p>
     * @return - End ID
     */
    int endId();

    /**
     * Determines if a player should be registered to this {@link ClientPinger}
     * Always registers players by default unless a different predicate is provided.
     * <p>
     * @param condition - If player should be registered or not
     */
    void filter(Predicate<Player> condition);

    /**
     * Attaches a client ping listener to this {@link ClientPinger}
     * <p>
     * @param listener - Listener to attach
     */
    void attach(ClientPingerListener listener);
}
