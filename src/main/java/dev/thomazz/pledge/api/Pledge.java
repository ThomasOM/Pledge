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
	 * @param start - Sets start for range
	 * @param end   - Sets end for range
	 */
	Pledge setRange(int start, int end);

	/**
	 * Sets the policy for the Pledge netty handler on how to create {@link PacketFrame}s
	 * Note: Only change this setting if you know what you are doing!
	 *
	 * @param policy - Set policy
	 * @return       -
	 */
	Pledge setPacketWritePolicy(PacketWritePolicy policy);

	/**
	 * Tracks packets for the current tick, creating a new {@link PacketFrame}.
	 *
	 * @param player - Player to create frame for
	 * @return       - Created frame
	 */
	PacketFrame createFrame(Player player);

	/**
	 * Gets the next {@link PacketFrame} the player will be sent.
	 * Returns an empty result if no frame was created for the current tick using {@link #createFrame(Player)}
	 *
	 * @param player - Player to get frame for
	 * @return       - Next frame
	 */
	 Optional<PacketFrame> getNextFrame(Player player);
}
