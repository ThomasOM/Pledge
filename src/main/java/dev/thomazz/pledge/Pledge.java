package dev.thomazz.pledge;

import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.frame.FrameClientPinger;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Main API object to
 */
public interface Pledge {
    /**
     * Sends a player a ping packet with a certain ID.
     * Can listen to events after sending the ping.
     * <p>
     * @param player - Player to send ping
     * @param id     - ID of the ping
     */
    void sendPing(@NotNull Player player, int id);

    /**
     * Gets the networking channel for a {@link Player} if available.
     * <p>
     * @param player - Player to get channel for
     * @return       - Networking channel
     */
    Optional<Channel> getChannel(@NotNull Player player);

    /**
     * Creates a client pinger.
     * See documentation in {@link ClientPinger} for more info.
     * <p>
     * @param startId - Start ID for ping range
     * @param endId   - End ID for ping range
     * @return        - Client pinger instance
     */
    ClientPinger createPinger(int startId, int endId);

    /**
     * Creates a frame client pinger.
     * See documentation in {@link FrameClientPinger} for more info.
     * <p>
     * @param startId - Start ID for ping range
     * @param endId   - End ID for ping range
     * @return        - Frame client pinger instance
     */
    FrameClientPinger createFramePinger(int startId, int endId);

    /**
     * Destroys the API instance.
     * A new API instance can be retrieved and created using {@link PledgeImpl#getOrCreate(Plugin)}
     */
    void destroy();

    /**
     * Creates a new API instance using the provided plugin to register listeners.
     * If an API instance already exists, it returns the existing one instead.
     * The API instance can be destroyed using {@link PledgeImpl#destroy()}
     * <p>
     * @param plugin - Plugin to register listeners under
     * @return       - API instance
     */
    static Pledge getOrCreate(@NotNull Plugin plugin) {
        if (PledgeImpl.instance == null) {
            PledgeImpl.instance = new PledgeImpl(plugin).start();
        }

        return PledgeImpl.instance;
    }
}
