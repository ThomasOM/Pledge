package dev.thomazz.pledge;

import dev.thomazz.pledge.event.PingSendEvent;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.network.NetworkPongHandler;
import dev.thomazz.pledge.packet.PacketBundleGenerator;
import dev.thomazz.pledge.packet.ping.PingPacketProviderFactory;
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.AbstractPinger;
import dev.thomazz.pledge.pinger.Pinger;
import dev.thomazz.pledge.pinger.PingerOptions;
import dev.thomazz.pledge.pinger.impl.BundlePingerImpl;
import dev.thomazz.pledge.pinger.impl.LegacyPingerImpl;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Getter
public class PledgeImpl implements Pledge, Listener {
    static PledgeImpl instance;

    private final Plugin plugin;
    private final Logger logger;

    private final BukkitTask startTask;
    private final TickEndTask endTask;

    private final PingPacketProvider pingProvider;
    private final PacketBundleGenerator bundleGenerator;

    private final Map<Player, Channel> playerChannels = new HashMap<>();
    private final Map<Player, AbstractPinger> playerPingers = new LinkedHashMap<>();


    PledgeImpl(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.pingProvider = PingPacketProviderFactory.buildPingProvider();
        this.bundleGenerator = new PacketBundleGenerator();

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

        // Clean up pinger
        AbstractPinger pinger = this.playerPingers.get(player);
        if (pinger != null) {
            pinger.cleanUp();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        this.setupPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.teardownPlayer(player, false);
    }

    @EventHandler
    void onTickStart(TickStartEvent ignored) {
        this.playerPingers.values().forEach(AbstractPinger::onTickStart);
    }

    @EventHandler
    void onTickEnd(TickEndEvent ignored) {
        this.playerPingers.values().forEach(AbstractPinger::onTickEnd);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPongReceive(PongReceiveEvent event) {
        AbstractPinger pinger = this.playerPingers.get(event.getPlayer());
        if (pinger != null && pinger.isInRange(event.getId())) {
            pinger.onPongReceive(event);
        }
    }

    @Override
    public void sendPing(@NotNull Player player, int id) {
        // Keep within ranges
        int max = Math.max(this.pingProvider.getUpperBound(), this.pingProvider.getLowerBound());
        int min = Math.min(this.pingProvider.getUpperBound(), this.pingProvider.getLowerBound());
        int pingId = Math.max(Math.min(id, max), min);

        // Run on channel event loop
        Channel channel = this.playerChannels.get(player);
        ChannelUtils.runInEventLoop(channel, () -> this.sendPingRaw(player, channel, pingId));
    }

    @Override
    public Pinger createPinger(@NotNull Player player, @NotNull PingerOptions options) {
        if (this.playerPingers.containsKey(player)) {
            throw new IllegalStateException("Player already has a pinger!");
        }

        Channel channel = this.playerChannels.get(player);
        if (channel == null) {
            throw new IllegalStateException("Player channel not found!");
        }

        AbstractPinger pinger;
        if (options.isUseBundles() && this.bundleGenerator.isSupported()) {
            pinger = new BundlePingerImpl(this, player, channel, options);
        } else {
            pinger = new LegacyPingerImpl(this, player, channel, options);
        }

        this.playerPingers.put(player, pinger);
        return pinger;
    }

    @Override
    public void destroy() {
        if (!this.equals(PledgeImpl.instance)) {
            throw new IllegalStateException("API object not the same as current instance!");
        }

        // Teardown for all players
        Bukkit.getOnlinePlayers().forEach(player -> this.teardownPlayer(player, true));
        this.playerPingers.values().forEach(AbstractPinger::cleanUp);

        this.startTask.cancel();
        this.endTask.cancel();

        PledgeImpl.instance = null;
    }

    public void sendPingRaw(Player player, Channel channel, int pingId) {
        try {
            Object packet = this.pingProvider.buildPacket(pingId);
            Bukkit.getPluginManager().callEvent(new PingSendEvent(player, pingId));
            channel.writeAndFlush(packet);
        } catch (Exception ex) {
            this.logger.severe(String.format("Failed to send ping! Player:%s Id:%o", player.getName(), pingId));
            ex.printStackTrace();
        }
    }

    public void sendBundleRaw(Player player, Channel channel) {
        try {
            Object packet = this.bundleGenerator.buildBundlePacket();
            channel.writeAndFlush(packet);
        } catch (Exception ex) {
            this.logger.severe(String.format("Failed to send bundle packet! Player:%s", player.getName()));
            ex.printStackTrace();
        }
    }
}
