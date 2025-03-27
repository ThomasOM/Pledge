package dev.thomazz.pledge.pinger.bundle;

import org.bukkit.entity.Player;

public interface BundlePingerListener {
	/**
	 * Called when a ping is sent at the start of the tick to a player.
	 * <p>
	 * @param player - Player that the ping response is sent to
	 * @param id     - ID of ping
	 */
	default void onPingSend(Player player, int id) {}

	/**
	 * Called when the response to a ping that was sent to a player is received.
	 * <p>
	 * @param player - Player that the ping response is received from
	 * @param id     - ID of ping
	 */
	default void onPongReceive(Player player, int id) {}


	/**
	 * Called when an unexpected ping response is received from a player.
	 * <p>
	 * @param player - Player that the ping response is received from
	 * @param id     - ID of ping
	 */
	default void onPongReceiveInvalid(Player player, int id) {}
}
