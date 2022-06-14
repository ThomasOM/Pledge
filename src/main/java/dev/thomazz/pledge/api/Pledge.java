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
     * A 'max' value equal to or higher than 0 should never be used since it can interfere with normal inventory actions.
     * A range larger than at least 800 is recommended, assuming you time players out after 20 seconds.
     * Can not be set while running.
     *
     * By default, 'min' is equal to {@link Short#MIN_VALUE} and 'max' is equal to -1
     *
     * @param min - Minimum value of the action number
     * @param max - Maximum value of the action number
     */
    Pledge range(int min, int max);

    /**
     * Direction in which the action number of transaction packets are counted.
     * Can not be set while running.
     *
     * By default, {@link Direction#NEGATIVE} is used.
     *
     * @param direction - Direction of action number counting
     */
    Pledge direction(Direction direction);

    /**
     * Whether events should be turned on or not.
     * If you want to use a {@link TransactionListener}, make sure to enable this.
     * Can not be set while running.
     *
     * By default, this is set to false.
     *
     * @param value - If events should be enabled or not
     */
    Pledge events(boolean value);

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
     * @return - {@link Pledge} object that has been built
     */
    static Pledge build() {
        if (PledgeImpl.INSTANCE != null) {
            throw new IllegalStateException("Can not create multiple instances of " + Pledge.class.getSimpleName() + "!");
        }

        PledgeImpl.INSTANCE = new PledgeImpl();
        return PledgeImpl.INSTANCE;
    }
}
