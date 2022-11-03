package dev.thomazz.pledge.api;

import dev.thomazz.pledge.PledgeImpl;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main API access point for settings up Pledge
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
	 * Sets the range for the {@link PacketFrame} ids.
	 *
	 * @param start - Starting id for range
	 * @param end   - Ending id for range
	 */
	Pledge setRange(int start, int end);

	/**
	 * Sets the policy for the Pledge netty handler on how to create {@link PacketFrame}s
	 * Note: Only change this setting if you know what you are doing!
	 *
	 * @param policy - Write policy
	 */
	Pledge setPacketWritePolicy(PacketWritePolicy policy);

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
