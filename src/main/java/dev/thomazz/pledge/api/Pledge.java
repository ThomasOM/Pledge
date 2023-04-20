package dev.thomazz.pledge.api;

import java.util.Optional;
import java.util.UUID;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.PacketFrameTimeoutEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main API instance for Pledge
 */
public interface Pledge {
    /**
     * Sets up Pledge to start tracking packets using {@link PacketFrame}s.
     * Only a single instance should be active at a time to prevent undesirable behaviour.
     * <p>
     * @return - API instance
     */
    static Pledge build() {
        return new PledgeImpl();
    }

    /**
     * Starts tracking packets and listening using the provided plugin instance.
     * <p>
     * @param plugin - Plugin instance to use
     */
    Pledge start(JavaPlugin plugin);

    /**
     * Ends and cleans up the API instance, clearing all old data.
     * This does not need to be called on shutdown, but should be used if support plugin reloading is desired.
     */
    void destroy();

    /**
     * Sets the range for the {@link PacketFrame} ids.
     * <p>
     * @param start - Starting id for range
     * @param end   - Ending id for range
     */
    Pledge setRange(int start, int end);

    /**
     * Sets the amount of ticks for a {@link PacketFrameTimeoutEvent} to be called,
     * after not receiving a response for a {@link PacketFrame}.
     * Default value is 400 ticks (20 seconds)
     * <p>
     * @param ticks - Amount of unresponsive ticks until calling the timeout event (<= 0 disables this feature)
     */
    Pledge setTimeoutTicks(int ticks);

    /**
     * Setting to send frames automatically after a certain amount of ticks have passed without any frames created.
     * Default value for this is 0, causing no frames to be created and sent automatically.
     * <p>
     * @param interval - Interval to automatically create packet frames for (<= 0 disables this feature)
     */
    Pledge setFrameInterval(int interval);

    /**
     * Tracks packets for the current tick, creating a new {@link PacketFrame}.
     * If a frame is already created for the player on this current tick, it simply returns the already existing frame.
     * <p>
     * Note: Players need to be in the PLAY state for creating frames, creating a frame before that will cause an error.
     * <p>
     * @param player - Player to create frame for
     * @return       - Created frame or current frame if one was already created this tick
     */
    PacketFrame getOrCreateFrame(Player player);

    /**
     * Same as {@link #getOrCreateFrame(Player)}, but instead using the player uuid.
     */
    PacketFrame getOrCreateFrame(UUID playerId);

    /**
     * Gets the {@link PacketFrame} for the player in the current server tick.
     * Returns an empty result if no {@link PacketFrame} was created with {@link Pledge#getOrCreateFrame(Player)}.
     * <p>
     * @param player - Player to get frame for
     * @return       - Next frame
     */
    Optional<PacketFrame> getFrame(Player player);

    /**
     * Same as {@link #getFrame(Player)}, but instead using the player uuid.
     */
    Optional<PacketFrame> getFrame(UUID playerId);

    /**
     * @return - If the current server version supports packet bundles.
     */
    boolean supportsBundles();
}
