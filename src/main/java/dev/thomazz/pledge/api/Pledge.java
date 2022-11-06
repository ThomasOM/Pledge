package dev.thomazz.pledge.api;

import dev.thomazz.pledge.PledgeImpl;
import java.util.Optional;

import dev.thomazz.pledge.api.event.PacketFrameTimeoutEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main API access point for Pledge
 */
public interface Pledge {
    /**
     * Sets up Pledge to start tracking packets using {@link PacketFrame}s.
     *
     * @return - API instance
     */
    static Pledge build() {
        return PledgeImpl.init();
    }

    /**
     * Starts tracking packets and listening using the provided plugin instance.
     *
     * @param plugin - Plugin instance to use
     */
    Pledge start(JavaPlugin plugin);

    /**
     * Ends and cleans up the API instance, clearing all of the old data.
     * This does not need to be called on shutdown, but should be used if support plugin reloading is desired.
     */
    void destroy();

    /**
     * Sets the range for the {@link PacketFrame} ids.
     *
     * @param start - Starting id for range
     * @param end   - Ending id for range
     */
    Pledge setRange(int start, int end);

    /**
     * Sets the amount of ticks for a {@link PacketFrameTimeoutEvent} to be called,
     * after not receiving a response for a sent {@link PacketFrame}.
     * Default value is 400 ticks (20 seconds)
     *
     * @param ticks - Amount of unresponsive ticks until calling the timeout event (<= 0 disables this feature)
     */
    Pledge setTimeoutTicks(int ticks);

    /**
     * Sets a fixed interval in ticks where frames will be created and sent automatically for players.
     * Default value for this is 0, causing no frames to be created and sent automatically.
     *
     * @param interval - Interval to automatically create packet frames for (<= 0 disables this feature)
     */
    Pledge setFrameInterval(int interval);

    /**
     * Tracks packets for the current tick, creating a new {@link PacketFrame}.
     * If a frame is already created for the player on this current tick, it simply returns the already existing frame.
     *
     * @param player - Player to create frame for
     * @return       - Created frame or current frame if one was already created this tick
     */
    PacketFrame getOrCreateFrame(Player player);

    /**
     * Gets the {@link PacketFrame} for the player in the current server tick.
     * Returns an empty result if no {@link PacketFrame} was created with {@link Pledge#getOrCreateFrame(Player)}.
     *
     * @param player - Player to get frame for
     * @return       - Next frame
     */
    Optional<PacketFrame> getFrame(Player player);
}
