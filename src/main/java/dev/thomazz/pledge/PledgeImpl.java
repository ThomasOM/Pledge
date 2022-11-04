package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.Pledge;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.packet.PacketProviderFactory;
import dev.thomazz.pledge.packet.PacketVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PledgeImpl implements Pledge, Listener {
    @Getter
    private static PledgeImpl instance;

    private final PacketProvider packetProvider = PacketProviderFactory.build();
    private final Map<Player, PlayerHandler> playerHandlers = new HashMap<>();

    private JavaPlugin plugin;

    // Default values, can modify through API
    private int rangeStart = -2000;
    private int rangeEnd = -3000;

    // Determines if this instance is active or not
    private boolean started = false;
    private boolean destroyed = false;

    private void createHandler(Player player) {
        try {
            PlayerHandler handler = new PlayerHandler(player);
            this.playerHandlers.put(player, handler);
        } catch (Exception e) {
            this.plugin.getLogger().severe("Can not create Pledge player handler!");
            e.printStackTrace();
        }
    }

    private void removeHandler(Player player) {
        this.playerHandlers.remove(player);
    }

    private Optional<PlayerHandler> getHandler(Player player) {
        return Optional.ofNullable(this.playerHandlers.get(player));
    }

    private void validateActive() {
        if (this.destroyed) {
            throw new IllegalStateException("Pledge instance is no longer active because it has been destroyed!");
        }
    }

    private void validateLegacyBounds(int rangeId) {
        if (rangeId < (int) Short.MIN_VALUE || rangeId > 0) {
            throw new IllegalArgumentException("Invalid range for legacy packet version!"
                + "limits: " + Short.MIN_VALUE + " - " + -1);
        }
    }

    @Override
    public PledgeImpl start(JavaPlugin plugin) {
        this.validateActive();

        if (this.started) {
            throw new IllegalStateException("Already started Pledge instance!");
        }

        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Started up Pledge");

        // Mainly for reload support or when starting later
        Bukkit.getOnlinePlayers().forEach(this::createHandler);

        this.started = true;
        return this;
    }

    @Override
    public void destroy() {
        this.validateActive();

        // Unregister listening for player join and quit
        HandlerList.unregisterAll(this);

        // Clean up all of the player handlers
        this.playerHandlers.values().forEach(PlayerHandler::cleanUp);
        this.playerHandlers.clear();

        // Clear instance to allow creation of a new one
        PledgeImpl.instance = null;
        this.destroyed = true;
    }

    @Override
    public PledgeImpl setRange(int start, int end) {
        this.validateActive();

        // Validate bounds for range in legacy versions
        PacketVersion version = PacketVersion.getCurrentVersion();
        if (version == PacketVersion.LEGACY) {
            this.validateLegacyBounds(start);
            this.validateLegacyBounds(end);
        }

        this.rangeStart = start;
        this.rangeEnd = end;
        return this;
    }

    @Override
    public PacketFrame getOrCreateFrame(Player player) {
        this.validateActive();
        return this.getHandler(player).map(PlayerHandler::createNextFrame)
            .orElseThrow(() -> new IllegalArgumentException("No handler present for player!"));
    }

    @Override
    public Optional<PacketFrame> getFrame(Player player) {
        this.validateActive();
        return this.getHandler(player).flatMap(PlayerHandler::getNextFrame);
    }

    // Highest priority to try to always register the handlers after other plugins did their job
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.createHandler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.removeHandler(event.getPlayer());
    }

    public static PledgeImpl init() {
        if (PledgeImpl.instance != null) {
            throw new IllegalStateException("Can not instantiate multiple Pledge instances!");
        }

        return PledgeImpl.instance = new PledgeImpl();
    }
}
