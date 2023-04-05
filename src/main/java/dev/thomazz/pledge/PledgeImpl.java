package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.Pledge;
import dev.thomazz.pledge.packet.PacketBundleBuilder;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.packet.PacketProviderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.thomazz.pledge.util.TickEndTask;
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
import org.bukkit.scheduler.BukkitTask;

@Getter
public class PledgeImpl implements Pledge, Listener {
    @Getter
    private static PledgeImpl instance;

    private final PacketBundleBuilder packetBundleBuilder = new PacketBundleBuilder();
    private final PacketProvider packetProvider = PacketProviderFactory.build();
    private final Map<UUID, PlayerHandler> playerHandlers = new HashMap<>();

    private JavaPlugin plugin;

    private BukkitTask tickTask;
    private TickEndTask tickEndTask;
    private int tick;

    private int timeoutTicks = 400;
    private int frameInterval = 0;

    // Default values, can modify through API
    private int rangeStart = -2000;
    private int rangeEnd = -3000;

    // Determines if this instance is active or not
    private boolean started = false;
    private boolean destroyed = false;

    private void createHandler(Player player) {
        try {
            PlayerHandler handler = new PlayerHandler(player);
            this.playerHandlers.put(player.getUniqueId(), handler);
        } catch (Exception e) {
            this.plugin.getLogger().severe("Could not create Pledge player handler!");
            e.printStackTrace();
        }
    }

    private void removeHandler(Player player) {
        this.playerHandlers.remove(player.getUniqueId());
    }

    private Optional<PlayerHandler> getHandler(Player player) {
        return this.getHandler(player.getUniqueId());
    }

    private Optional<PlayerHandler> getHandler(UUID playerId) {
        return Optional.ofNullable(this.playerHandlers.get(playerId));
    }

    private void validateActive() {
        if (this.destroyed) {
            throw new IllegalStateException("Pledge instance is no longer active because it has been destroyed!");
        }
    }

    private void validateBounds(int rangeId) {
        int min = this.packetProvider.getLowerBound();
        int max = this.packetProvider.getUpperBound();

        if (rangeId < min || rangeId > max) {
            throw new IllegalArgumentException("Invalid range for packet provider!"
                + "limits: " + min + " - " + max);
        }
    }

    private void tickStart() {
        // Frame creation for all online players if a frame interval is set
        if (this.frameInterval > 0) {
            this.playerHandlers.values().stream()
                .filter(handler -> handler.getCreationTicks() >= this.frameInterval)
                .forEach(PlayerHandler::createNextFrame);
        }

        // Tick player handlers
        this.playerHandlers.values().forEach(PlayerHandler::tickStart);
        this.tick++;
    }

    private void tickEnd() {
        this.playerHandlers.values().forEach(PlayerHandler::tickEnd);
    }

    @Override
    public PledgeImpl start(JavaPlugin plugin) {
        this.validateActive();

        if (this.started) {
            throw new IllegalStateException("Already started Pledge instance!");
        }

        this.plugin = plugin;
        this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickStart, 0L, 1L);
        this.tickEndTask = new TickEndTask(this::tickEnd).start();

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

        // End tick task if it was created
        this.tickTask.cancel();
        this.tickEndTask.cancel();

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
        this.validateBounds(start);
        this.validateBounds(end);

        this.rangeStart = start;
        this.rangeEnd = end;
        return this;
    }

    @Override
    public Pledge setTimeoutTicks(int ticks) {
        this.validateActive();
        this.timeoutTicks = ticks;
        return this;
    }

    @Override
    public Pledge setFrameInterval(int interval) {
        this.validateActive();
        this.frameInterval = interval;
        return this;
    }

    @Override
    public PacketFrame getOrCreateFrame(Player player) {
        return this.getOrCreateFrame(player.getUniqueId());
    }

    @Override
    public PacketFrame getOrCreateFrame(UUID playerId) {
        this.validateActive();
        return this.getHandler(playerId).map(PlayerHandler::createNextFrame)
            .orElseThrow(() -> new IllegalArgumentException("No handler present for player!"));
    }

    @Override
    public Optional<PacketFrame> getFrame(Player player) {
        return this.getFrame(player.getUniqueId());
    }

    @Override
    public Optional<PacketFrame> getFrame(UUID playerId) {
        this.validateActive();
        return this.getHandler(playerId).flatMap(PlayerHandler::getNextFrame);
    }

    @Override
    public boolean supportsBundles() {
        return this.packetBundleBuilder.isSupported();
    }

    // Lowest priority to have data be available on join event
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.createHandler(event.getPlayer());
    }

    // Highest priority to always inject handlers after other plugins
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinLate(PlayerJoinEvent event) {
        this.getHandler(event.getPlayer()).ifPresent(handler -> handler.inject(this));
    }

    // If for some reason we want this to be available on the quit event
    @EventHandler(priority = EventPriority.HIGHEST)
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
