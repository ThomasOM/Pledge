package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.PacketWritePolicy;
import dev.thomazz.pledge.api.Pledge;
import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.packet.SignalPacketProviderFactory;
import dev.thomazz.pledge.util.PacketVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PledgeImpl implements Pledge, Listener {
	@Getter
	private static PledgeImpl instance;

	private final SignalPacketProvider signalPacketProvider = SignalPacketProviderFactory.build();
	private final Map<Player, PlayerHandler> playerHandlers = new HashMap<>();

	private PacketWritePolicy packetWritePolicy = PacketWritePolicy.WRITE_FLUSH;
	private JavaPlugin plugin;

	// Default values, can modify through API
	private int rangeStart = -2000;
	private int rangeEnd = -3000;

	private Optional<PlayerHandler> getHandler(Player player) {
		return Optional.ofNullable(this.playerHandlers.get(player));
	}

	@Override
	public PledgeImpl start(JavaPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getLogger().info("Starting up Pledge");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		return this;
	}

	@Override
	public PledgeImpl setRange(int start, int end) {
		PacketVersion version = PacketVersion.getCurrentVersion();
		if (version == PacketVersion.LEGACY
			&& (start > (int) Short.MAX_VALUE || start < (int) Short.MIN_VALUE)) {
			throw new IllegalArgumentException("Invalid range for legacy packet version!"
				+ "limits: " + Short.MIN_VALUE + " - " + Short.MAX_VALUE);
		}

		this.rangeStart = start;
		this.rangeEnd = end;
		return this;
	}

	@Override
	public Pledge setPacketWritePolicy(PacketWritePolicy policy) {
		this.packetWritePolicy = policy;
		return this;
	}

	@Override
	public PacketFrame createFrame(Player player) {
		return this.getHandler(player).map(PlayerHandler::createNextFrame)
			.orElseThrow(() -> new IllegalArgumentException("No handler present for player!"));
	}

	@Override
	public Optional<PacketFrame> getNextFrame(Player player) {
		return this.getHandler(player).flatMap(PlayerHandler::getNextFrame);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		try {
			PlayerHandler handler = new PlayerHandler(player);
			this.playerHandlers.put(player, handler);
		} catch (Exception e) {
			this.plugin.getLogger().info("Can not create Pledge player handler!");
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.playerHandlers.remove(event.getPlayer());
	}

	public static PledgeImpl init() {
		if (PledgeImpl.instance != null) {
			throw new IllegalStateException("Can not instantiate multiple Pledge instances!");
		}

		return PledgeImpl.instance = new PledgeImpl();
	}
}
