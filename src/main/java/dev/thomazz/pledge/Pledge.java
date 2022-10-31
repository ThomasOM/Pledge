package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PledgeSettings;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.packet.SignalPacketProviderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@RequiredArgsConstructor
public class Pledge implements Listener {
	@Getter
	private static Pledge instance;

	private final SignalPacketProvider signalPacketProvider = SignalPacketProviderFactory.build();
	private final Map<UUID, PlayerHandler> playerHandlers = new HashMap<>();

	private final JavaPlugin plugin;
	private final PledgeSettings settings;

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		try {
			PlayerHandler handler = new PlayerHandler(player);
			this.playerHandlers.put(player.getUniqueId(), handler);
		} catch (Exception e) {
			this.plugin.getLogger().info("Can not create PledgeApi player handler!");
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.playerHandlers.remove(event.getPlayer().getUniqueId());
	}

	public static void init(JavaPlugin plugin, PledgeSettings settings) {
		if (Pledge.instance != null) {
			throw new IllegalStateException("Can not instantiate multiple PledgeApi instances!");
		}

		Pledge.instance = new Pledge(plugin, settings);
	}
}
