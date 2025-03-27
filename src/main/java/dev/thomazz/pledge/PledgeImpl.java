package dev.thomazz.pledge;

import dev.thomazz.pledge.event.PingSendEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.network.NetworkPongHandler;
import dev.thomazz.pledge.packet.ping.PingPacketProviderFactory;
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.Pinger;
import dev.thomazz.pledge.pinger.PingerOptions;
import dev.thomazz.pledge.pinger.bundle.BundlePinger;
import dev.thomazz.pledge.pinger.bundle.BundlePingerImpl;
import dev.thomazz.pledge.pinger.legacy.ClientPinger;
import dev.thomazz.pledge.pinger.legacy.ClientPingerImpl;
import dev.thomazz.pledge.pinger.legacy.ClientPingerOptions;
import dev.thomazz.pledge.util.ChannelAccess;
import dev.thomazz.pledge.util.ChannelUtils;
import dev.thomazz.pledge.util.TickEndTask;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Getter
public class PledgeImpl implements Pledge, Listener {
    static PledgeImpl instance;

    private final Plugin plugin;
    private final Logger logger;
    private final PingPacketProvider packetProvider;

    private final BukkitTask startTask;
    private final TickEndTask endTask;

    private final List<Pinger> pingers = new ArrayList<>();
    private final Map<Player, Channel> playerChannels = new HashMap<>();

    PledgeImpl(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.packetProvider = PingPacketProviderFactory.buildPingProvider();

        PluginManager manager = Bukkit.getPluginManager();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        this.startTask = scheduler.runTaskTimer(plugin, () -> manager.callEvent(new TickStartEvent()), 0L, 1L);
        this.endTask = TickEndTask.create(() -> manager.callEvent(new TickEndEvent()));

        // Setup for all players
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);

        // Register as listener after setup
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupPlayer(Player player) {
        Channel channel = ChannelAccess.getChannel(player);
        this.playerChannels.put(player, channel);

        // Inject pong listener
        channel.pipeline().addBefore(
            "packet_handler",
            "pledge_packet_listener",
            new NetworkPongHandler(this, player)
        );
    }

    private void teardownPlayer(Player player, boolean cleanPipeline) {
        Channel channel = this.playerChannels.remove(player);

        // Eject pong listener
        if (cleanPipeline && channel.pipeline().get(NetworkPongHandler.class) != null) {
            channel.pipeline().remove(NetworkPongHandler.class);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogin(PlayerLoginEvent event) {
        this.setupPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        this.teardownPlayer(event.getPlayer(), false);
    }

    @Override
    public void sendPing(@NotNull Player player, int id) {
        // Keep within ranges
        int max = Math.max(this.packetProvider.getUpperBound(), this.packetProvider.getLowerBound());
        int min = Math.min(this.packetProvider.getUpperBound(), this.packetProvider.getLowerBound());
        int pingId = Math.max(Math.min(id, max), min);

        // Run on channel event loop
        this.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel, () ->
                this.sendPingRaw(player, channel, pingId)
            )
        );
    }

    public void sendPingRaw(Player player, Channel channel, int pingId) {
        try {
            Object packet = this.packetProvider.buildPacket(pingId);
            Bukkit.getPluginManager().callEvent(new PingSendEvent(player, pingId));
            channel.writeAndFlush(packet);
        } catch (Exception ex) {
            this.logger.severe(String.format("Failed to send ping! Player:%s Id:%o", player.getName(), pingId));
            ex.printStackTrace();
        }
    }

    public void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    @Override
    public Optional<Channel> getChannel(@NotNull Player player) {
        return Optional.ofNullable(this.playerChannels.get(player));
    }

    @Override
    public BundlePinger createPinger(@NotNull PingerOptions options) {
        BundlePinger pinger = new BundlePingerImpl(this, options);
        this.pingers.add(pinger);
        return pinger;
    }

    @Override
    public ClientPinger createPinger(@NotNull ClientPingerOptions options) {
        ClientPinger pinger = new ClientPingerImpl(this, options);
        this.pingers.add(pinger);
        return pinger;
    }

    @Override
    public void destroy() {
        if (!this.equals(PledgeImpl.instance)) {
            throw new IllegalStateException("API object not the same as current instance!");
        }

        // Teardown for all players
        Bukkit.getOnlinePlayers().forEach(player -> this.teardownPlayer(player, true));

        this.pingers.forEach(Pinger::destroy);

        this.startTask.cancel();
        this.endTask.cancel();

        PledgeImpl.instance = null;
    }
}
