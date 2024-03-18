package dev.thomazz.pledge.pinger;

import org.bukkit.entity.Player;

/**
 * Listener to attach to a {@link ClientPinger}
 */
public interface ClientPingerListener {
    /**
     * Called when a player receives the first transaction ID of the {@link ClientPinger}.
     * After this the player can be considered active on the server.
     * <p>
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    void onValidation(Player player, int id);

    /**
     * Called when a ping is sent at the start of the tick to a player.
     * <p>
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    void onPingSendStart(Player player, int id);

    /**
     * Called when a ping is sent at the end of the tick to a player.
     * <p>
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    void onPingSendEnd(Player player, int id);

    /**
     * Called when the response to a ping that was sent at the start of the tick to a player is received.
     * <p>
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    void onPongReceiveStart(Player player, int id);

    /**
     * Called when the response to a ping that was sent at the end of the tick to a player is received.
     * <p>
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    void onPongReceiveEnd(Player player, int id);
}
