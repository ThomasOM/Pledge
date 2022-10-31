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
	 * Sets the tick examiner to determine if the packets in a tick should be tracked with a {@link PacketFrame}.
	 *
	 * @param examiner - Examiner to set.
	 */
	Pledge setPacketExaminer(PacketTickExaminer examiner);

	/**
	 * Gets the next {@link PacketFrame} the player will be sent.
	 * When tracking a packet and you need to know the corresponding {@link PacketFrame} you can use this method.
	 *
	 * @param player - Player to get frame for
	 * @return       - Current frame for packet that will be sent to player
	 */
	 Optional<PacketFrame> getCurrentFrame(Player player);
}
