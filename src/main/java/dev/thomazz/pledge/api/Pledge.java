package dev.thomazz.pledge.api;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.TransactionListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main API Interface.
 */
public interface Pledge {

    /**
     * Sets the range the action number of transaction packets should vary between.
     * By default, 'min' is equal to {@link Short#MIN_VALUE} and 'max' is equal to -1
     * A 'max' value equal to or above 0 should not be used since it can interfere with inventory actions.
     * Can not be set while running.
     *
     * @param min - Minimum value of the action number
     * @param max - Maximum value of the action number
     */
    Pledge range(short min, short max);

    /**
     * Direction in which the action number of transaction packets are counted.
     * By default, {@link Direction#NEGATIVE} is used.
     * Can not be set while running.
     *
     * @param direction - Direction of action number counting
     */
    Pledge direction(Direction direction);

    /**
     * Starts the task to send transactions through each player channel on the start and end of the tick.
     *
     * @param plugin - The plugin the transaction task should be registered for.
     */
    void start(JavaPlugin plugin);

    /**
     * Forcibly stops the transactions being sent.
     * Ejects the injected elements and cleans up created resources.
     * Only recommended to use when disabling your plugin.
     */
    void destroy();

    /**
     * Adds a transaction listener to pass events to.
     *
     * @param listener - The listener
     */
    void addListener(TransactionListener listener);

    /**
     * Removes a transaction listener from receiving events.
     *
     * @param listener - The listener
     */
    void removeListener(TransactionListener listener);

    /**
     * Builds the underlying base object and injects into the server.
     *
     * @param events - Determines if events should be injected for the channel initializer.
     * If you already listen to transactions yourself and don't plan to use {@link TransactionListener}, you can disable this.
     *
     * @return - {@link Pledge} object that has been built
     */
    static Pledge build(boolean events) {
        if (PledgeImpl.INSTANCE != null) {
            throw new IllegalStateException("Can not create multiple instances of " + Pledge.class.getName() + "!");
        }

        PledgeImpl.INSTANCE = new PledgeImpl(events);
        return PledgeImpl.INSTANCE;
    }
}
