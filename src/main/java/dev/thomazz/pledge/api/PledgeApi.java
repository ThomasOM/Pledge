package dev.thomazz.pledge.api;

import dev.thomazz.pledge.Pledge;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main API access point for settings up PledgeApi
 */
public final class PledgeApi {
	/**
	 * Sets up PledgeApi to start tracking packets.
	 *
	 * @param plugin
	 * @param settings
	 */
	public static void setup(JavaPlugin plugin, PledgeSettings settings) {
		Pledge.init(plugin, settings);
	}
}
