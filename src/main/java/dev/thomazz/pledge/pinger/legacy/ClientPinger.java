package dev.thomazz.pledge.pinger.legacy;

import dev.thomazz.pledge.pinger.Pinger;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Several events can be listened to with a {@link ClientPingerListener}, such as when pings are sent and received.
 */
public interface ClientPinger extends Pinger {
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
