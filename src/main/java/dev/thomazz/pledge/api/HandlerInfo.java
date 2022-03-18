package dev.thomazz.pledge.api;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

/**
 * Provides the channel and player object through events.
 */
public interface HandlerInfo {

    /**
     * Gets the netty channel of the connection.
     *
     * @return - The channel, might be null if cleaned
     */
    Channel getChannel();

    /**
     * Gets player associated with the channel, could be null if the player is not initialized yet.
     *
     * @return - The player, might be null if not initialized or removed
     */
    Player getPlayer();
}
