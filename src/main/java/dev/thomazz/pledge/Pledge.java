package dev.thomazz.pledge;

import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerOptions;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Main API object
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
     * @param options - Options
     * @return        - Client pinger instance
     */
    ClientPinger createPinger(@NotNull ClientPingerOptions options);

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
            PledgeImpl.instance = new PledgeImpl(plugin);
        }

        return PledgeImpl.instance;
    }
}
